#!/usr/bin/env bash
# scripts/run-test.sh — full end-to-end test runner.
#
# Usage:
#   ./scripts/run-test.sh <test> [scenario1,scenario2,...]
#
# Tests:
#   dcr        — DCR latency          (k6/tests/dcr-latency.js)
#   is-crud    — IS CRUD operations   (k6/tests/is-crud.js)
#   apim-crud  — APIM CRUD operations (k6/tests/apim-crud.js)
#   is-search  — IS search operations (k6/tests/is-search.js)
#
# Optional second argument: comma-separated list of scenarios to run.
# If omitted, all scenarios for that test are run.
# Example: ./scripts/run-test.sh apim-crud get_accounts,get_balances,get_transactions
#
# Per-scenario flow (is-crud / apim-crud / is-search):
#   is-search only — runs once before the loop:
#     0. Register IS DCR client, truncate consent tables, seed searchRecordCount records
#   For each scenario slot:
#     1. Restart containers
#     2. Wait until healthy
#     3. Run client setup (DCR + user-auth where needed; skipped for is-search)
#     4. Warm-up pass for that scenario (results discarded)
#     5. Measured run for that scenario → per-scenario summary JSON
#   After all scenarios:
#     6. Merge per-scenario summaries into one combined summary
#     7. Generate HTML report

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$SCRIPT_DIR/.."

TEST="${1:-}"
SCENARIO_FILTER="${2:-}"   # optional comma-separated list e.g. "get_accounts,get_balances"

usage() {
  echo "Usage: $0 <test> [scenario1,scenario2,...]"
  echo "  Tests: dcr | is-crud | apim-crud | is-search"
  exit 1
}

[ -z "$TEST" ] && usage

# ---------------------------------------------------------------------------
# Per-test configuration
# ---------------------------------------------------------------------------

case "$TEST" in
  dcr)
    K6_FILE="k6/tests/dcr-latency.js"
    SUMMARY="results/dcr-latency-summary.json"
    REPORT="results/dcr-latency-report.html"
    TIER="dcr"
    CONTAINERS="obiam obam"
    SETUP_SCRIPT=""
    SCENARIOS=()
    ;;
  is-crud)
    K6_FILE="k6/tests/is-crud.js"
    SUMMARY="results/is-crud-summary.json"
    REPORT="results/is-crud-report.html"
    TIER="medium"
    CONTAINERS="obiam"
    SETUP_SCRIPT="is"
    SCENARIOS=("create_consent" "get_consent")
    ;;
  apim-crud)
    K6_FILE="k6/tests/apim-crud.js"
    SUMMARY="results/apim-crud-summary.json"
    REPORT="results/apim-crud-report.html"
    TIER="medium"
    CONTAINERS="obiam obam"
    SETUP_SCRIPT="apim"
    SCENARIOS=("get_accounts" "get_balances" "get_transactions" "create_payment_consent" "get_payment_consent")
    ;;
  is-search)
    K6_FILE="k6/tests/is-search.js"
    SUMMARY="results/is-search-summary.json"
    REPORT="results/is-search-report.html"
    TIER="medium"
    CONTAINERS="obiam"
    SETUP_SCRIPT="is-search"
    SCENARIOS=(
      "portal_accounts_load"
      "portal_payments_load"
      "accounts_active_tab"
      "accounts_inactive_tab"
      "payments_active_tab"
      "payments_inactive_tab"
      "cof_active_tab"
      "by_consent_id"
      "by_client_id"
      "by_user_id"
      "date_narrow"
      "date_wide"
      "deep_pagination"
      "large_page"
    )
    ;;
  *)
    echo "Unknown test: $TEST"
    usage
    ;;
esac

cd "$ROOT"
mkdir -p results

# Apply optional scenario filter (second argument)
if [ -n "$SCENARIO_FILTER" ] && [ "${#SCENARIOS[@]}" -gt 0 ]; then
  FILTERED=()
  IFS=',' read -ra REQUESTED <<< "$SCENARIO_FILTER"
  for req in "${REQUESTED[@]}"; do
    for s in "${SCENARIOS[@]}"; do
      if [ "$s" = "$req" ]; then
        FILTERED+=("$s")
        break
      fi
    done
  done
  if [ "${#FILTERED[@]}" -eq 0 ]; then
    echo "ERROR: None of the requested scenarios (${SCENARIO_FILTER}) are valid for test '${TEST}'."
    echo "Valid scenarios: ${SCENARIOS[*]}"
    exit 1
  fi
  SCENARIOS=("${FILTERED[@]}")
  echo "==> Scenario filter applied: running ${SCENARIOS[*]}"
fi

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

# Read a single field from k6/test-config.json.
read_config() {
  node -e "process.stdout.write(String(require('./k6/test-config.json')['$1'] || ''))"
}

wait_for_healthy() {
  if echo "$CONTAINERS" | grep -q "obiam"; then
    echo "    Waiting for IS (obiam)..."
    docker exec obiam bash -c "
      attempts=0
      until curl -sk https://localhost:9446/api/health \
             -o /dev/null -w '%{http_code}' | grep -qE '^[2345][0-9][0-9]$'; do
        attempts=\$((attempts + 1))
        if [ \$attempts -ge 36 ]; then
          echo 'ERROR: IS (obiam) did not become healthy after 3 minutes.' >&2
          exit 1
        fi
        sleep 5
      done
    "
    echo "    IS is ready."
  fi

  if echo "$CONTAINERS" | grep -q "obam"; then
    echo "    Waiting for APIM (obam) management plane..."
    docker exec obam bash -c "
      attempts=0
      until wget -qSO /dev/null \
             'http://localhost:9763/api/am/publisher/v4/apis?limit=1' 2>&1 \
             | grep -q 'HTTP/'; do
        attempts=\$((attempts + 1))
        if [ \$attempts -ge 36 ]; then
          echo 'ERROR: APIM (obam) management plane did not become healthy after 3 minutes.' >&2
          exit 1
        fi
        sleep 5
      done
    "
    echo "    APIM management plane is ready."
    echo "    Waiting for APIM gateway APIs to load (DCR endpoint)..."
    attempts=0
    until [ "$(curl -sk -o /dev/null -w '%{http_code}' \
               "https://obam:8243/open-banking/v3.3.0/register" \
               -H 'Content-Type: application/json' \
               -d '{}')" != "404" ]; do
      attempts=$((attempts + 1))
      if [ $attempts -ge 30 ]; then
        echo "ERROR: APIM gateway DCR endpoint did not load after 5 minutes." >&2
        exit 1
      fi
      sleep 10
    done
    echo "    APIM gateway is ready."
  fi
}

run_search_data_setup() {
  echo "    Registering IS DCR client for search test..."
  node scripts/setup-is-client.js

  local CLIENT_ID SEARCH_USER DB_HOST DB_USER DB_PASS DB_NAME
  CLIENT_ID=$(read_config clientId)
  SEARCH_USER=$(read_config searchUserId)
  DB_HOST=$(read_config dbHost)
  DB_USER=$(read_config dbUser)
  DB_PASS=$(read_config dbPass)
  DB_NAME=$(read_config dbName)

  echo "    Truncating consent tables..."
  docker exec mysql-db mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" "$DB_NAME" -e "
    SET FOREIGN_KEY_CHECKS=0;
    TRUNCATE TABLE FS_CONSENT_MAPPING;
    TRUNCATE TABLE FS_CONSENT_AUTH_RESOURCE;
    TRUNCATE TABLE FS_CONSENT_STATUS_AUDIT;
    TRUNCATE TABLE FS_CONSENT;
    SET FOREIGN_KEY_CHECKS=1;
  "
  echo "    Consent tables truncated."

  local RECORD_COUNT
  RECORD_COUNT=$(read_config searchRecordCount)
  RECORD_COUNT="${RECORD_COUNT:-1000000}"

  echo "    Seeding ${RECORD_COUNT} consent records (clientId=${CLIENT_ID})..."
  sed \
    -e "s|SET @primary_client_id = .*|SET @primary_client_id = '${CLIENT_ID}';|" \
    -e "s|SET @primary_user_id   = .*|SET @primary_user_id   = '${SEARCH_USER}';|" \
    -e "s|SET @total_records     = .*|SET @total_records     = ${RECORD_COUNT};|" \
    scripts/generate_consent_data.sql \
  | docker exec -i mysql-db mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" "$DB_NAME"
  echo "    Seeding complete."
}

run_client_setup() {
  if [ "$SETUP_SCRIPT" = "is" ]; then
    echo "    Registering IS DCR client..."
    node scripts/setup-is-client.js
  elif [ "$SETUP_SCRIPT" = "apim" ]; then
    echo "    Registering DCR client via APIM gateway..."
    node scripts/setup-dcr-client.js
    echo "    Obtaining APPLICATION_USER token..."
    node scripts/setup-user-auth.js
  fi
}

# ---------------------------------------------------------------------------
# Per-scenario loop (is-crud / apim-crud)
# ---------------------------------------------------------------------------

if [ "${#SCENARIOS[@]}" -gt 0 ]; then
  SCENARIO_SUMMARIES=()
  TOTAL="${#SCENARIOS[@]}"

  # is-search: register DCR client and seed DB once before the scenario loop.
  if [ "$SETUP_SCRIPT" = "is-search" ]; then
    echo ""
    echo "==> [Pre-loop] Search test setup: DCR client + DB seed"
    run_search_data_setup
  fi

  for i in "${!SCENARIOS[@]}"; do
    SCENARIO="${SCENARIOS[$i]}"
    SLOT_NUM=$((i + 1))
    SCENARIO_SUMMARY="results/${TEST}-${SCENARIO}-summary.json"

    echo ""
    echo "========================================================"
    echo "  Scenario ${SLOT_NUM}/${TOTAL}: ${SCENARIO}"
    echo "========================================================"

    # Step A — Restart containers
    echo ""
    echo "--> [A] Restarting containers: $CONTAINERS"
    docker restart $CONTAINERS

    # Step B — Wait for healthy
    echo ""
    echo "--> [B] Waiting for containers to be healthy..."
    wait_for_healthy

    # Step C — Client setup
    echo ""
    echo "--> [C] Client setup..."
    run_client_setup

    # Step D — Warm-up pass (single scenario, results discarded)
    echo ""
    echo "--> [D] Warm-up pass for scenario: $SCENARIO"
    k6 run --insecure-skip-tls-verify \
           --summary-export=/dev/null \
           -e ONLY_SCENARIO="$SCENARIO" \
           -e STEADY_DURATION=1m \
           -e PEAK_VUS=5 \
           "$K6_FILE" \
      || true

    # Step E — Measured run (single scenario)
    echo ""
    echo "--> [E] Measured run for scenario: $SCENARIO"
    k6 run --insecure-skip-tls-verify \
           --summary-export="$SCENARIO_SUMMARY" \
           -e ONLY_SCENARIO="$SCENARIO" \
           "$K6_FILE" \
      || true   # threshold failures don't abort the loop

    SCENARIO_SUMMARIES+=("$SCENARIO_SUMMARY")
  done

  # Step F — Merge summaries
  echo ""
  echo "==> Merging ${#SCENARIO_SUMMARIES[@]} scenario summaries..."
  node scripts/merge-summaries.js "${SCENARIO_SUMMARIES[@]}" > "$SUMMARY"
  echo "    Merged summary: $SUMMARY"

else
  # dcr: single restart + single k6 run (no per-scenario isolation)

  echo ""
  echo "==> [1/5] Restarting containers: $CONTAINERS"
  docker restart $CONTAINERS

  echo ""
  echo "==> [2/5] Waiting for containers to be healthy..."
  wait_for_healthy

  echo ""
  echo "==> [3/5] Client setup..."
  run_client_setup

  echo ""
  echo "==> [4/5] Running warm-up pass..."
  k6 run --insecure-skip-tls-verify \
         --summary-export=/dev/null \
         -e STEADY_DURATION=1m \
         -e PEAK_VUS=5 \
         "$K6_FILE" \
    || true

  echo ""
  echo "==> [5/5] Running measured test: $K6_FILE"
  k6 run --insecure-skip-tls-verify \
         --summary-export="$SUMMARY" \
         "$K6_FILE" \
    || true   # threshold failures don't abort before the report step
fi

# ---------------------------------------------------------------------------
# Report
# ---------------------------------------------------------------------------

echo ""
echo "==> Generating report: $REPORT"
node scripts/html-report.js "$SUMMARY" "$TIER" 0 "$REPORT"

echo ""
echo "==> Done: $REPORT"
