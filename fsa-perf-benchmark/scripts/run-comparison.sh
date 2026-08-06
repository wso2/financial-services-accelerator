#!/usr/bin/env bash
# run-comparison.sh
# Runs one test suite twice — once labelled "baseline" and once "extended" —
# writing a summary JSON for each. After both runs, compare-variants.js
# produces a side-by-side latency and error-rate table.
#
# Usage:
#   ./scripts/run-comparison.sh <baseline|extended> <is-crud|apim-crud|dcr-latency|is-search>
#
#   ./scripts/run-comparison.sh baseline  is-crud
#   # ...toggle the extension on/off, restart containers, re-seed data...
#   ./scripts/run-comparison.sh extended  is-crud
#   node scripts/compare-variants.js \
#        results/comparison/baseline-is-crud.json \
#        results/comparison/extended-is-crud.json
#
# Required setup before EACH invocation:
#   1. Set the extension to the state you are labelling (on for "extended",
#      off for "baseline") and restart the relevant containers so no warm
#      caches or JIT state carry over from the other variant.
#   2. Re-seed the fixed test dataset so both variants query identical data.
#
# All test configuration (hosts, credentials, VUs, durations) is read from
# k6/test-config.json. Fill that file in before running.
#
# Env vars (optional):
#   COOLDOWN_SECONDS   pause between variant runs (default 60)

set -euo pipefail

VARIANT="${1:?usage: run-comparison.sh <baseline|extended> <is-crud|apim-crud|dcr-latency|is-search>}"
SUITE="${2:?usage: run-comparison.sh <baseline|extended> <is-crud|apim-crud|dcr-latency|is-search>}"

case "$VARIANT" in
  baseline|extended) ;;
  *) echo "unknown variant '$VARIANT' - expected baseline or extended" >&2; exit 1 ;;
esac
COOLDOWN="${COOLDOWN_SECONDS:-60}"

RUN_ID="${RUN_ID:-$(git rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)}"
FSA_VERSION="${FSA_VERSION:-unknown}"
echo "run-id: $RUN_ID  fsa-version: $FSA_VERSION"

case "$SUITE" in
  is-crud)     TEST_FILE="k6/tests/is-crud.js" ;;
  apim-crud)   TEST_FILE="k6/tests/apim-crud.js" ;;
  dcr-latency) TEST_FILE="k6/tests/dcr-latency.js" ;;
  is-search)   TEST_FILE="k6/tests/is-search.js" ;;
  *) echo "unknown suite '$SUITE' - expected is-crud, apim-crud, dcr-latency, or is-search" >&2; exit 1 ;;
esac

OUT_DIR="results/comparison"
mkdir -p "$OUT_DIR"

SUMMARY="$OUT_DIR/${VARIANT}-${SUITE}.json"

echo "=== variant=$VARIANT suite=$SUITE ==="
k6 run --insecure-skip-tls-verify \
  -e VARIANT="$VARIANT" \
  -e RUN_ID="${RUN_ID}" \
  -e FSA_VERSION="${FSA_VERSION}" \
  --summary-export="$SUMMARY" \
  "$TEST_FILE" \
  || true   # threshold failures don't abort before the compare step

echo ""
echo "Done: $SUMMARY"
echo "Once both variants have been run, compare them with:"
echo "  node scripts/compare-variants.js \\"
echo "       results/comparison/baseline-${SUITE}.json \\"
echo "       results/comparison/extended-${SUITE}.json"
