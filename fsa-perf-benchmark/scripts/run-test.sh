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
# Per-scenario flow (is-crud / apim-crud):
#   For each scenario slot:
#     1. Restart containers
#     2. Wait until healthy
#     3. Run client setup (DCR + user-auth where needed)
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
    SETUP_SCRIPT=""
    SCENARIOS=()
    ;;
  *)
    echo "Unknown test: $TEST"
    usage
    ;;
esac

cd "$ROOT"

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

wait_for_healthy() {
  if echo "$CONTAINERS" | grep -q "obiam"; then
    echo "    Waiting for IS (obiam)..."
    docker exec obiam bash -c "
      until curl -sk https://localhost:9446/api/health \
             -o /dev/null -w '%{http_code}' | grep -qE '^[2345][0-9][0-9]$'; do
        sleep 5
      done
    "
    echo "    IS is ready."
  fi

  if echo "$CONTAINERS" | grep -q "obam"; then
    echo "    Waiting for APIM (obam) management plane..."
    docker exec obam bash -c "
      until wget -qSO /dev/null \
             'http://localhost:9763/api/am/publisher/v4/apis?limit=1' 2>&1 \
             | grep -q 'HTTP/'; do
        sleep 5
      done
    "
    echo "    APIM management plane is ready."
    echo "    Waiting for APIM gateway APIs to load (DCR endpoint)..."
    until [ "$(curl -sk -o /dev/null -w '%{http_code}' \
               "https://obam:8243/open-banking/v3.3.0/register" \
               -H 'Content-Type: application/json' \
               -d '{}')" != "404" ]; do
      sleep 10
    done
    echo "    APIM gateway is ready."
  fi
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
  # dcr / is-search: single restart + single k6 run (no per-scenario isolation)

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
         "$K6_FILE"
fi

# ---------------------------------------------------------------------------
# Report
# ---------------------------------------------------------------------------

echo ""
echo "==> Generating report: $REPORT"
node scripts/html-report.js "$SUMMARY" "$TIER" 0 "$REPORT"

echo ""
echo "==> Done: $REPORT"
