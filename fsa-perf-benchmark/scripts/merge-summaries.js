#!/usr/bin/env node
// scripts/merge-summaries.js
//
// Merges multiple k6 --summary-export JSON files into one combined summary.
// Used by run-test.sh to aggregate per-scenario summaries (produced when each
// scenario runs in its own k6 invocation after a container restart) into a
// single file that html-report.js can consume.
//
// Usage:
//   node scripts/merge-summaries.js <file1.json> [file2.json ...] > merged.json

'use strict';

const fs   = require('fs');
const path = require('path');

const files = process.argv.slice(2);
if (files.length === 0) {
  console.error('Usage: merge-summaries.js <file1.json> [file2.json ...]');
  process.exit(1);
}

const summaries = files.map(f => JSON.parse(fs.readFileSync(f, 'utf8')));

// Sum each scenario's testRunDurationMs so html-report.js uses the correct
// total elapsed time rather than deriving a spurious duration from count/rate.
const totalDurationMs = summaries.reduce(
  (s, summary) => s + (summary.state?.testRunDurationMs || 0), 0
);

// ---------------------------------------------------------------------------
// Merge helpers
// ---------------------------------------------------------------------------

function mergeTrend(metrics) {
  // avg: weighted mean; min/max: extremes; percentiles: conservative max.
  const totalCount = metrics.reduce((s, m) => s + (m.count || 0), 0);
  const weightedAvg = totalCount === 0 ? 0
    : metrics.reduce((s, m) => s + (m.avg || 0) * (m.count || 0), 0) / totalCount;
  return {
    count: totalCount,
    avg:   weightedAvg,
    min:   Math.min(...metrics.map(m => m.min ?? Infinity)),
    max:   Math.max(...metrics.map(m => m.max ?? 0)),
    med:   Math.max(...metrics.map(m => m.med   ?? 0)),
    'p(90)': Math.max(...metrics.map(m => m['p(90)'] ?? 0)),
    'p(95)': Math.max(...metrics.map(m => m['p(95)'] ?? 0)),
    'p(99)': Math.max(...metrics.map(m => m['p(99)'] ?? 0)),
  };
}

function mergeRate(metrics) {
  const passes = metrics.reduce((s, m) => s + (m.passes || 0), 0);
  const fails  = metrics.reduce((s, m) => s + (m.fails  || 0), 0);
  const total  = passes + fails;
  return { passes, fails, value: total === 0 ? 0 : passes / total };
}

function mergeCounter(metrics) {
  const count = metrics.reduce((s, m) => s + (m.count || 0), 0);
  // Derive rate from the summed scenario durations so throughput reflects the
  // full sequential test wall-clock time, not a sum of per-run rates which
  // would produce a nonsense value (each rate was already divided by its own
  // run duration).
  const rate = totalDurationMs > 0
    ? count / (totalDurationMs / 1000)
    : metrics.reduce((s, m) => s + (m.rate || 0), 0);
  return { count, rate };
}

function mergeGauge(metrics) {
  return {
    value: Math.max(...metrics.map(m => m.value ?? 0)),
    min:   Math.min(...metrics.map(m => m.min ?? Infinity)),
    max:   Math.max(...metrics.map(m => m.max ?? 0)),
  };
}

function isTrend(m)   { return 'avg' in m && 'count' in m && 'min' in m; }
function isRate(m)    { return 'passes' in m && 'fails' in m; }
function isCounter(m) { return 'count' in m && 'rate' in m && !('avg' in m); }
function isGauge(m)   { return 'value' in m && !('passes' in m) && !('count' in m); }

// ---------------------------------------------------------------------------
// Collect all metric keys across all files
// ---------------------------------------------------------------------------

const allKeys = new Set();
summaries.forEach(s => Object.keys(s.metrics || {}).forEach(k => allKeys.add(k)));

// ---------------------------------------------------------------------------
// Merge each metric
// ---------------------------------------------------------------------------

const merged = {};

for (const key of allKeys) {
  const rawSamples = summaries.map(s => s.metrics?.[key]).filter(Boolean);
  if (rawSamples.length === 0) continue;

  // --summary-export wraps stats in a nested .values object; normalize so
  // the type predicates and merge functions always see flat stat objects.
  const samples = rawSamples.map(m => m.values || m);

  const first = samples[0];
  let result;

  if (isTrend(first))        result = mergeTrend(samples);
  else if (isRate(first))    result = mergeRate(samples);
  else if (isCounter(first)) result = mergeCounter(samples);
  else if (isGauge(first))   result = mergeGauge(samples);
  else {
    process.stderr.write(`merge-summaries: unknown metric shape for "${key}", keeping first file's data\n`);
    result = { ...first };
  }

  // Thresholds live on the raw metric object, not inside .values.
  const thresholdMaps = rawSamples.map(m => m.thresholds).filter(Boolean);
  if (thresholdMaps.length > 0) {
    const thresholds = {};
    for (const t of thresholdMaps) {
      for (const [expr, failed] of Object.entries(t)) {
        thresholds[expr] = (thresholds[expr] || false) || failed;
      }
    }
    result.thresholds = thresholds;
  }

  merged[key] = result;
}

// ---------------------------------------------------------------------------
// Output
// ---------------------------------------------------------------------------

const state = totalDurationMs > 0 ? { testRunDurationMs: totalDurationMs } : {};
const output = { metrics: merged, state, root_group: summaries[0].root_group || {} };
process.stdout.write(JSON.stringify(output, null, 2) + '\n');
