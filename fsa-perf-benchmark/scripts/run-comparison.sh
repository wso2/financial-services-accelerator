#!/usr/bin/env bash
# run-comparison.sh
# Runs one test suite (fs-is, fs-apim, or combined) once per concurrency
# tier (low/medium/high) for a single VARIANT, with a cooldown pause between
# tiers, writing one summary.json per combination. Run it once per variant,
# toggling the extension in between.
#
# Usage:
#   ./scripts/run-comparison.sh <baseline|extended> <fs-is|combined>
#
#   ./scripts/run-comparison.sh baseline fs-is
#   ./scripts/run-comparison.sh baseline combined
#   ...toggle the extension on, restart the service, re-seed test data...
#   ./scripts/run-comparison.sh extended fs-is
#   ./scripts/run-comparison.sh extended combined
#
# Note: there's no fs-apim-only suite - fs-apim's gateway calls always
# depend on fs-is for a token, so fs-apim can't be exercised in isolation.
# Use "fs-is" to test fs-is alone, and "combined" to test fs-is + fs-apim
# together (which is where fs-apim's own numbers show up).
#
# Required setup before EACH invocation (see README "Toggling the extension"):
#   1. Set the extension to the state you're about to label (on for
#      "extended", off for "baseline") and restart fs-is/fs-apim so no
#      warm caches or JIT state carry over from the other variant.
#   2. Re-seed the fixed test dataset (same client IDs / test users every
#      time) so both variants query identical data.
#
# Env vars (all optional, same as the k6 test files):
#   IS_HOST, APIM_HOST, CLIENT_ID, CLIENT_SECRET, COOLDOWN_SECONDS

set -euo pipefail

VARIANT="${1:?usage: run-comparison.sh <baseline|extended> <fs-is|combined>}"
SUITE="${2:?usage: run-comparison.sh <baseline|extended> <fs-is|combined>}"
COOLDOWN="${COOLDOWN_SECONDS:-60}"
TIERS=(low medium high)

# Stable run identity: git SHA if available, otherwise a timestamp.
# Injected into every k6 metric sample via the runId tag so all samples from
# this invocation can be grouped in Prometheus/Grafana without ambiguity.
RUN_ID="${RUN_ID:-$(git rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)}"
FSA_VERSION="${FSA_VERSION:-unknown}"
echo "run-id: $RUN_ID  fsa-version: $FSA_VERSION"

case "$SUITE" in
  fs-is) TEST_FILE="k6/tests/fs-is.js" ;;
  combined) TEST_FILE="k6/tests/combined.js" ;;
  *) echo "unknown suite '$SUITE' - expected fs-is or combined" >&2; exit 1 ;;
esac

OUT_DIR="results/comparison"
mkdir -p "$OUT_DIR"

for TIER in "${TIERS[@]}"; do
  echo "=== variant=$VARIANT suite=$SUITE tier=$TIER ==="
  k6 run \
    -e VARIANT="$VARIANT" \
    -e CONCURRENCY_TIER="$TIER" \
    -e RUN_ID="${RUN_ID}" \
    -e FSA_VERSION="${FSA_VERSION}" \
    -e IS_HOST="${IS_HOST:-https://localhost:9446}" \
    -e APIM_HOST="${APIM_HOST:-https://localhost:8243}" \
    -e CLIENT_ID="${CLIENT_ID:-}" \
    -e CLIENT_SECRET="${CLIENT_SECRET:-}" \
    --summary-export="$OUT_DIR/${VARIANT}-${SUITE}-${TIER}.json" \
    "$TEST_FILE"

  if [ "$TIER" != "${TIERS[-1]}" ]; then
    echo "cooldown ${COOLDOWN}s before next tier..."
    sleep "$COOLDOWN"
  fi
done

echo ""
echo "Done: $OUT_DIR/${VARIANT}-${SUITE}-{low,medium,high}.json"
echo "Once both variants have been run for this suite, compare them with:"
echo "  node results/compare-variants.js results/comparison $SUITE"
