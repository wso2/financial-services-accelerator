#!/usr/bin/env node
// compare-variants.js
// Reads a baseline and an extended k6 summary JSON (as produced by
// run-comparison.sh) and prints a markdown side-by-side comparison table
// showing p95, p99, error rate, and throughput overhead for every endpoint.
//
// Usage:
//   node results/compare-variants.js <baseline-summary.json> <extended-summary.json>
//   node results/compare-variants.js results/comparison/baseline-summary.json \
//                                    results/comparison/extended-summary.json

const fs = require('fs');

const baseFile = process.argv[2];
const extFile  = process.argv[3];

if (!baseFile || !extFile) {
  console.error('Usage: node results/compare-variants.js <baseline-summary.json> <extended-summary.json>');
  process.exit(1);
}
for (const f of [baseFile, extFile]) {
  if (!fs.existsSync(f)) { console.error(`File not found: ${f}`); process.exit(1); }
}

const baseData = JSON.parse(fs.readFileSync(baseFile, 'utf8'));
const extData  = JSON.parse(fs.readFileSync(extFile,  'utf8'));

const baseMetrics = baseData.metrics || {};
const extMetrics  = extData.metrics  || {};

// All endpoints this framework can produce — rows absent from both summaries
// are skipped silently.
const ENDPOINTS = [
  { name: 'IS_SearchConsents',        method: 'GET',    path: '/api/fs/consent/admin/search' },
  { name: 'IS_CreateAccountConsent',  method: 'POST',   path: '/api/fs/consent/manage/account-access-consents' },
  { name: 'IS_GetAccountConsent',     method: 'GET',    path: '/api/fs/consent/manage/account-access-consents/{id}' },
  { name: 'IS_RevokeAccountConsent',  method: 'DELETE', path: '/api/fs/consent/manage/account-access-consents/{id}' },
  { name: 'APIM_GetAccounts',         method: 'GET',    path: '{aispContext}/accounts' },
  { name: 'APIM_GetBalances',         method: 'GET',    path: '{aispContext}/accounts/{id}/balances' },
  { name: 'APIM_GetTransactions',     method: 'GET',    path: '{aispContext}/accounts/{id}/transactions' },
  { name: 'APIM_CreatePaymentConsent',method: 'POST',   path: '{pispContext}/domestic-payment-consents' },
  { name: 'APIM_GetPaymentConsent',   method: 'GET',    path: '{pispContext}/domestic-payment-consents/{id}' },
];

function statsFor(metrics, name) {
  const m    = metrics[`http_req_duration{name:${name}}`];
  const errM = metrics[`http_req_failed{name:${name}}`];
  if (!m) return null;
  const v = m.values || m;
  if (v.avg == null && v.count == null) return null;

  let errRate = null;
  if (errM) {
    const ev = errM.values || errM;
    if (ev.value != null) {
      errRate = ev.value * 100;
    } else if (ev.passes != null && ev.fails != null && ev.passes + ev.fails > 0) {
      errRate = (ev.passes / (ev.passes + ev.fails)) * 100;
    }
  }

  return {
    count:  v.count  ?? null,
    avg:    v.avg    ?? null,
    p95:    v['p(95)'] ?? null,
    p99:    v['p(99)'] ?? null,
    errRate,
  };
}

function detectDuration(data) {
  if (data.state && data.state.testRunDurationMs) return data.state.testRunDurationMs / 1000;
  const hr = data.metrics?.['http_reqs'];
  if (hr) { const v = hr.values || hr; if (v.rate && v.count) return v.count / v.rate; }
  return null;
}

function pctChange(base, ext) {
  if (base == null || ext == null || base === 0) return null;
  return ((ext - base) / base) * 100;
}

function fmt(n, digits = 1) { return n == null ? '-' : n.toFixed(digits); }
function fmtPct(n) {
  if (n == null) return '-';
  const sign = n > 0 ? '+' : '';
  const arrow = n > 5 ? ' ▲' : n < -5 ? ' ▼' : '';
  return `${sign}${n.toFixed(1)}%${arrow}`;
}

// Build rows — only endpoints present in at least one summary
const rows = ENDPOINTS.map((e) => {
  const b = statsFor(baseMetrics, e.name);
  const x = statsFor(extMetrics,  e.name);
  if (!b && !x) return null;
  return {
    ...e,
    base: b,
    ext:  x,
    p95Delta:   pctChange(b?.p95,    x?.p95),
    p99Delta:   pctChange(b?.p99,    x?.p99),
    avgDelta:   pctChange(b?.avg,    x?.avg),
    errDelta:   pctChange(b?.errRate, x?.errRate),
  };
}).filter(Boolean);

if (rows.length === 0) {
  console.log('No matching endpoint metrics found in either summary.');
  process.exit(0);
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------
const baseDur = detectDuration(baseData);
const extDur  = detectDuration(extData);
const baseVUs = (baseMetrics['vus_max']?.values || baseMetrics['vus_max'])?.value ?? '-';
const extVUs  = (extMetrics['vus_max']?.values  || extMetrics['vus_max'])?.value  ?? '-';

console.log('# Baseline vs Extended — Performance Comparison\n');
console.log(`| | Baseline | Extended |`);
console.log(`|---|---|---|`);
console.log(`| **Peak VUs** | ${baseVUs} | ${extVUs} |`);
console.log(`| **Test duration** | ${baseDur ? baseDur.toFixed(0) + 's' : '-'} | ${extDur ? extDur.toFixed(0) + 's' : '-'} |`);
console.log(`| **Source** | \`${baseFile}\` | \`${extFile}\` |`);

// ---------------------------------------------------------------------------
// Latency comparison table
// ---------------------------------------------------------------------------
console.log('\n## Latency (ms)\n');
console.log('| Endpoint | Baseline avg | Extended avg | Avg Δ | Baseline p95 | Extended p95 | p95 Δ | Baseline p99 | Extended p99 | p99 Δ |');
console.log('|---|---|---|---|---|---|---|---|---|---|');
rows.forEach((r) => {
  console.log(
    `| ${r.name}` +
    ` | ${fmt(r.base?.avg)} | ${fmt(r.ext?.avg)} | ${fmtPct(r.avgDelta)}` +
    ` | ${fmt(r.base?.p95)} | ${fmt(r.ext?.p95)} | ${fmtPct(r.p95Delta)}` +
    ` | ${fmt(r.base?.p99)} | ${fmt(r.ext?.p99)} | ${fmtPct(r.p99Delta)} |`
  );
});

// ---------------------------------------------------------------------------
// Error rate comparison table
// ---------------------------------------------------------------------------
console.log('\n## Error rate (%)\n');
console.log('| Endpoint | Baseline | Extended | Δ |');
console.log('|---|---|---|---|');
rows.forEach((r) => {
  const bErr = r.base?.errRate != null ? `${r.base.errRate.toFixed(2)}%` : '-';
  const xErr = r.ext?.errRate  != null ? `${r.ext.errRate.toFixed(2)}%`  : '-';
  console.log(`| ${r.name} | ${bErr} | ${xErr} | ${fmtPct(r.errDelta)} |`);
});

// ---------------------------------------------------------------------------
// Sample count
// ---------------------------------------------------------------------------
console.log('\n## Samples\n');
console.log('| Endpoint | Baseline | Extended |');
console.log('|---|---|---|');
rows.forEach((r) => {
  console.log(`| ${r.name} | ${fmt(r.base?.count, 0)} | ${fmt(r.ext?.count, 0)} |`);
});

// ---------------------------------------------------------------------------
// Summary observations
// ---------------------------------------------------------------------------
console.log('\n## Observations\n');
const regressions = rows.filter((r) => r.p95Delta != null && r.p95Delta > 10);
const improvements = rows.filter((r) => r.p95Delta != null && r.p95Delta < -10);
if (regressions.length > 0) {
  console.log('**Regressions (p95 >10% slower with extensions):**');
  regressions.forEach((r) => console.log(`- ${r.name}: ${fmtPct(r.p95Delta)} (${fmt(r.base?.p95)}ms → ${fmt(r.ext?.p95)}ms)`));
  console.log('');
}
if (improvements.length > 0) {
  console.log('**Improvements (p95 >10% faster with extensions):**');
  improvements.forEach((r) => console.log(`- ${r.name}: ${fmtPct(r.p95Delta)} (${fmt(r.base?.p95)}ms → ${fmt(r.ext?.p95)}ms)`));
  console.log('');
}
if (regressions.length === 0 && improvements.length === 0) {
  console.log('No endpoint changed by more than 10% in p95 latency between baseline and extended.');
}
