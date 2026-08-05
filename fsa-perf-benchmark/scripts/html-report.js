#!/usr/bin/env node
// html-report.js
// Generates a self-contained HTML performance report from a k6 --summary-export JSON.
//
// Columns: Request, Thread Count, Samples, Error %, Average, Min, Max,
//          90th %ile, 95th %ile, 99th %ile, Throughput/s
// Charts:  Response time overview, Percentile comparison,
//          Throughput/s per endpoint, Error % per endpoint
//
// Usage:
//   node results/html-report.js <summary.json> [tier] [durationSeconds] [outputFile]
//
//   node results/html-report.js results/fs-is-summary.json medium 180 results/report.html
//   node results/html-report.js results/combined-summary.json high 300 results/report.html
//
// tier defaults to "medium", durationSeconds defaults to 180.
// outputFile defaults to results/perf-report.html

'use strict';
const fs = require('fs');
const path = require('path');

const [, , summaryFile, tier = 'custom', durArg = '0', outArg] = process.argv;

if (!summaryFile) {
  console.error('Usage: node results/html-report.js <summary.json> [tier] [durationSeconds] [outputFile]');
  process.exit(1);
}
if (!fs.existsSync(summaryFile)) {
  console.error(`File not found: ${summaryFile}`);
  process.exit(1);
}

const OUTPUT_FILE = outArg || path.join(path.dirname(summaryFile), 'perf-report.html');

const data = JSON.parse(fs.readFileSync(summaryFile, 'utf8'));
const metrics = data.metrics || {};

// Auto-detect duration from summary when not supplied on the CLI.
function detectDuration(argVal) {
  const n = parseFloat(argVal);
  if (n > 0) return n;
  if (data.state && data.state.testRunDurationMs) return data.state.testRunDurationMs / 1000;
  const hr = metrics['http_reqs'];
  if (hr) { const v = hr.values || hr; if (v.rate && v.count) return v.count / v.rate; }
  return 180;
}
const DURATION_SEC = detectDuration(durArg);

// Auto-detect peak VUs from summary when tier is not a known preset.
const _vusMax = (() => {
  const m = metrics['vus_max'];
  if (!m) return null;
  const v = m.values || m;
  return v.value != null ? v.value : null;
})();

const TIER_VUS = {
  low:    { identity_server: 5,  accounts: 10, payments: 5  },
  medium: { identity_server: 10, accounts: 20, payments: 10 },
  high:   { identity_server: 20, accounts: 40, payments: 20 },
};
const vus = TIER_VUS[tier] || null;

const ENDPOINTS = [
  { name: 'IS_SearchConsents',         scenario: 'identity_server', method: 'GET',    path: '/api/fs/consent/admin/search' },
  { name: 'IS_CreateAccountConsent',   scenario: 'identity_server', method: 'POST',   path: '/api/fs/consent/manage/account-access-consents' },
  { name: 'IS_GetAccountConsent',      scenario: 'identity_server', method: 'GET',    path: '/api/fs/consent/manage/account-access-consents/{id}' },
  { name: 'IS_RevokeAccountConsent',   scenario: 'identity_server', method: 'DELETE', path: '/api/fs/consent/manage/account-access-consents/{id}' },
  { name: 'APIM_GetAccounts',          scenario: 'accounts',        method: 'GET',    path: '{aispContext}/accounts' },
  { name: 'APIM_GetBalances',          scenario: 'accounts',        method: 'GET',    path: '{aispContext}/accounts/{id}/balances' },
  { name: 'APIM_GetTransactions',      scenario: 'accounts',        method: 'GET',    path: '{aispContext}/accounts/{id}/transactions' },
  { name: 'APIM_CreatePaymentConsent', scenario: 'payments',        method: 'POST',   path: '{pispContext}/domestic-payment-consents' },
  { name: 'APIM_GetPaymentConsent',    scenario: 'payments',        method: 'GET',    path: '{pispContext}/domestic-payment-consents/{id}' },
  { name: 'APIM_DCR_Register',         scenario: 'dcr',             method: 'POST',   path: '/open-banking/v3.3.0/register' },
  { name: 'APIM_DCR_Delete',           scenario: 'dcr',             method: 'DELETE', path: '/open-banking/v3.3.0/register/{clientId}' },
];

function statsFor(name) {
  const m = metrics[`http_req_duration{name:${name}}`];
  const errM = metrics[`http_req_failed{name:${name}}`];
  if (!m) return null;

  const v = m.values || m;
  if (v.avg == null && v.count == null) return null;

  const count = v.count != null ? v.count : null;

  let errRate = null;
  if (errM) {
    const ev = errM.values || errM;
    if (ev.value != null) {
      errRate = ev.value * 100;
    } else if (ev.passes != null && ev.fails != null && ev.passes + ev.fails > 0) {
      errRate = (ev.passes / (ev.passes + ev.fails)) * 100;
    }
  }

  const throughput = count != null && DURATION_SEC > 0 ? count / DURATION_SEC : null;

  return {
    count,
    errRate,
    throughput,
    avg:  v.avg,
    min:  v.min,
    max:  v.max,
    p90:  v['p(90)'],
    p95:  v['p(95)'],
    p99:  v['p(99)'],
  };
}

function fmt(n, digits = 1, placeholder = '-') {
  return n == null ? placeholder : Number(n.toFixed(digits));
}

const rows = ENDPOINTS
  .map((e) => {
    const stats = statsFor(e.name);
    if (!stats) return null;
    const threadCount = vus ? (vus[e.scenario] ?? '-') : (_vusMax ?? '-');
    return { ...e, ...stats, threadCount };
  })
  .filter(Boolean);

if (rows.length === 0) {
  console.error(`No matching endpoint metrics found in ${summaryFile}.`);
  process.exit(1);
}

// Overall checks health
let checksHtml = '';
const checksMetric = metrics.checks;
if (checksMetric) {
  const cv = checksMetric.values || checksMetric;
  const passes = cv.passes ?? 0;
  const fails  = cv.fails  ?? 0;
  const total  = passes + fails;
  if (total > 0) {
    const failRate = (fails / total) * 100;
    if (fails === total) {
      checksHtml = `<div class="alert alert-danger">⚠ Every check failed (${fails}/${total}). This run captured error-response timings. Verify credentials and endpoint paths before trusting numbers below.</div>`;
    } else if (failRate > 5) {
      checksHtml = `<div class="alert alert-warning">⚠ ${failRate.toFixed(1)}% of checks failed (${fails}/${total}). Some rows may reflect error timings — review Error % column.</div>`;
    } else {
      checksHtml = `<div class="alert alert-success">✓ Checks: ${passes}/${total} passed (${failRate.toFixed(2)}% failed)</div>`;
    }
  }
}

// Table rows
const tableRows = rows.map((r) => {
  const errClass = r.errRate != null && r.errRate > 5 ? 'class="err-high"' : (r.errRate != null && r.errRate > 0 ? 'class="err-low"' : '');
  return `<tr>
    <td class="endpoint-name">${r.name}<span class="method-path">${r.method} ${r.path}</span></td>
    <td>${r.threadCount}</td>
    <td>${r.count != null ? r.count : '<em>n/a</em>'}</td>
    <td ${errClass}>${r.errRate != null ? r.errRate.toFixed(2) + '%' : '<em>n/a</em>'}</td>
    <td>${fmt(r.avg)}</td>
    <td>${fmt(r.min)}</td>
    <td>${fmt(r.max)}</td>
    <td>${fmt(r.p90)}</td>
    <td>${fmt(r.p95)}</td>
    <td>${fmt(r.p99)}</td>
    <td>${r.throughput != null ? r.throughput.toFixed(2) : '<em>n/a</em>'}</td>
  </tr>`;
}).join('\n');

// Chart data (JSON-safe)
const labels    = JSON.stringify(rows.map(r => r.name));
const avgData   = JSON.stringify(rows.map(r => fmt(r.avg)));
const minData   = JSON.stringify(rows.map(r => fmt(r.min)));
const maxData   = JSON.stringify(rows.map(r => fmt(r.max)));
const p90Data   = JSON.stringify(rows.map(r => fmt(r.p90)));
const p95Data   = JSON.stringify(rows.map(r => fmt(r.p95)));
const p99Data   = JSON.stringify(rows.map(r => fmt(r.p99)));
const tpData    = JSON.stringify(rows.map(r => r.throughput != null ? parseFloat(r.throughput.toFixed(2)) : 0));
const errData   = JSON.stringify(rows.map(r => r.errRate != null ? parseFloat(r.errRate.toFixed(2)) : 0));
const tcData    = JSON.stringify(rows.map(r => typeof r.threadCount === 'number' ? r.threadCount : 0));

const runDate = new Date().toISOString().replace('T', ' ').slice(0, 19) + ' UTC';

const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Performance Test Report</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      margin: 0; background: #f4f6f9; color: #1a1a2e;
    }
    header {
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 60%, #0f3460 100%);
      color: #fff; padding: 2rem 2.5rem;
    }
    header h1 { margin: 0 0 0.4rem; font-size: 1.7rem; letter-spacing: 0.02em; }
    header .meta { font-size: 0.85rem; opacity: 0.75; }
    main { max-width: 1400px; margin: 0 auto; padding: 1.5rem 2rem 3rem; }
    h2 { font-size: 1.1rem; color: #0f3460; border-left: 4px solid #e94560; padding-left: 0.6rem; margin: 2rem 0 0.8rem; }

    /* alerts */
    .alert { border-radius: 6px; padding: 0.7rem 1rem; margin-bottom: 1rem; font-size: 0.9rem; }
    .alert-danger  { background: #fde8e8; border: 1px solid #e94560; color: #7b1c2e; }
    .alert-warning { background: #fff3cd; border: 1px solid #ffc107; color: #664d03; }
    .alert-success { background: #d1fae5; border: 1px solid #10b981; color: #065f46; }

    /* summary cards */
    .cards { display: flex; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem; }
    .card {
      background: #fff; border-radius: 10px; padding: 1rem 1.4rem;
      box-shadow: 0 2px 8px rgba(0,0,0,.07); min-width: 140px; flex: 1;
    }
    .card .label { font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.06em; color: #666; }
    .card .value { font-size: 1.5rem; font-weight: 700; color: #0f3460; margin-top: 0.2rem; }
    .card .unit  { font-size: 0.75rem; color: #888; }

    /* table */
    .table-wrap { overflow-x: auto; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,.07); }
    table { width: 100%; border-collapse: collapse; background: #fff; font-size: 0.85rem; }
    thead th {
      background: #0f3460; color: #fff; padding: 0.7rem 0.8rem;
      text-align: right; white-space: nowrap; font-weight: 600;
    }
    thead th:first-child { text-align: left; }
    tbody tr:nth-child(even) { background: #f8fafc; }
    tbody tr:hover { background: #eef2ff; }
    tbody td { padding: 0.55rem 0.8rem; text-align: right; border-bottom: 1px solid #eee; }
    tbody td:first-child { text-align: left; }
    .endpoint-name { font-weight: 600; display: flex; flex-direction: column; }
    .method-path { font-weight: 400; font-size: 0.75rem; color: #666; font-family: monospace; }
    td.err-high { color: #c0392b; font-weight: 700; }
    td.err-low  { color: #e67e22; }

    /* charts grid */
    .charts-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(480px, 1fr)); gap: 1.5rem; margin-top: 0.5rem; }
    .chart-card {
      background: #fff; border-radius: 10px; padding: 1.2rem 1.4rem;
      box-shadow: 0 2px 8px rgba(0,0,0,.07);
    }
    .chart-card h3 { margin: 0 0 1rem; font-size: 0.9rem; color: #0f3460; text-transform: uppercase; letter-spacing: 0.05em; }
    .chart-card canvas { max-width: 100%; }

    footer { text-align: center; font-size: 0.75rem; color: #aaa; padding: 1rem; }
  </style>
</head>
<body>
<header>
  <h1>Performance Test Report</h1>
  <div class="meta">
    Source: <strong>${summaryFile}</strong> &nbsp;|&nbsp;
    Peak VUs: <strong>${_vusMax ?? tier}</strong> &nbsp;|&nbsp;
    Test duration: <strong>${DURATION_SEC.toFixed(0)}s</strong> &nbsp;|&nbsp;
    Generated: <strong>${runDate}</strong>
  </div>
</header>

<main>
  ${checksHtml}

  <!-- summary cards -->
  <h2>Summary</h2>
  <div class="cards">
    <div class="card">
      <div class="label">Endpoints</div>
      <div class="value">${rows.length}</div>
    </div>
    <div class="card">
      <div class="label">Total Samples</div>
      <div class="value">${rows.reduce((s, r) => s + (r.count || 0), 0).toLocaleString()}</div>
    </div>
    <div class="card">
      <div class="label">Avg Response</div>
      <div class="value">${(rows.reduce((s, r) => s + (r.avg || 0), 0) / rows.length).toFixed(0)}</div>
      <div class="unit">ms (across all endpoints)</div>
    </div>
    <div class="card">
      <div class="label">Max p99</div>
      <div class="value">${Math.max(...rows.map(r => r.p99 || 0)).toFixed(0)}</div>
      <div class="unit">ms</div>
    </div>
    <div class="card">
      <div class="label">Total Throughput</div>
      <div class="value">${rows.reduce((s, r) => s + (r.throughput || 0), 0).toFixed(2)}</div>
      <div class="unit">req/s</div>
    </div>
    <div class="card">
      <div class="label">Max Error %</div>
      <div class="value" style="color:${rows.some(r => (r.errRate || 0) > 5) ? '#e94560' : '#10b981'}">
        ${Math.max(...rows.map(r => r.errRate != null ? r.errRate : 0)).toFixed(2)}%
      </div>
    </div>
  </div>

  <!-- aggregate table -->
  <h2>Aggregate Report</h2>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>Request</th>
          <th>Thread Count</th>
          <th>Samples</th>
          <th>Error %</th>
          <th>Average (ms)</th>
          <th>Min (ms)</th>
          <th>Max (ms)</th>
          <th>90th %ile (ms)</th>
          <th>95th %ile (ms)</th>
          <th>99th %ile (ms)</th>
          <th>Throughput/s</th>
        </tr>
      </thead>
      <tbody>
        ${tableRows}
      </tbody>
    </table>
  </div>

  <!-- charts -->
  <h2>Charts</h2>
  <div class="charts-grid">

    <div class="chart-card">
      <h3>Response Time Overview (ms)</h3>
      <canvas id="chartResponseTime"></canvas>
    </div>

    <div class="chart-card">
      <h3>Percentile Comparison (ms)</h3>
      <canvas id="chartPercentiles"></canvas>
    </div>

    <div class="chart-card">
      <h3>Throughput per Endpoint (req/s)</h3>
      <canvas id="chartThroughput"></canvas>
    </div>

    <div class="chart-card">
      <h3>Error % per Endpoint</h3>
      <canvas id="chartErrors"></canvas>
    </div>

    <div class="chart-card">
      <h3>Thread Count per Endpoint</h3>
      <canvas id="chartThreads"></canvas>
    </div>

    <div class="chart-card">
      <h3>Min / Avg / Max Response Time (ms)</h3>
      <canvas id="chartMinAvgMax"></canvas>
    </div>

  </div>
</main>

<footer>Generated by fsa-perf-benchmark html-report.js &mdash; Financial Services Accelerator Performance Tests</footer>

<script>
(function () {
  const LABELS  = ${labels};
  const AVG     = ${avgData};
  const MIN_D   = ${minData};
  const MAX_D   = ${maxData};
  const P90     = ${p90Data};
  const P95     = ${p95Data};
  const P99     = ${p99Data};
  const TP      = ${tpData};
  const ERR     = ${errData};
  const TC      = ${tcData};

  const PALETTE = [
    '#0f3460','#e94560','#16213e','#1a7431','#c0392b',
    '#2980b9','#8e44ad','#d35400','#16a085','#7f8c8d',
    '#f39c12'
  ];

  const sharedOptions = (yLabel) => ({
    responsive: true,
    plugins: {
      legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } },
      tooltip: { mode: 'index', intersect: false }
    },
    scales: {
      x: { ticks: { font: { size: 10 }, maxRotation: 45 } },
      y: { beginAtZero: true, title: { display: true, text: yLabel, font: { size: 11 } } }
    }
  });

  // 1. Response Time Overview: Avg + p95 + p99 grouped bar
  new Chart(document.getElementById('chartResponseTime'), {
    type: 'bar',
    data: {
      labels: LABELS,
      datasets: [
        { label: 'Average',  data: AVG, backgroundColor: 'rgba(15,52,96,0.8)' },
        { label: '95th %ile',data: P95, backgroundColor: 'rgba(233,69,96,0.8)' },
        { label: '99th %ile',data: P99, backgroundColor: 'rgba(195,46,64,0.8)' },
      ]
    },
    options: sharedOptions('ms')
  });

  // 2. Percentile Comparison: p90 / p95 / p99
  new Chart(document.getElementById('chartPercentiles'), {
    type: 'bar',
    data: {
      labels: LABELS,
      datasets: [
        { label: 'p90', data: P90, backgroundColor: 'rgba(41,128,185,0.85)' },
        { label: 'p95', data: P95, backgroundColor: 'rgba(142,68,173,0.85)' },
        { label: 'p99', data: P99, backgroundColor: 'rgba(192,57,43,0.85)' },
      ]
    },
    options: sharedOptions('ms')
  });

  // 3. Throughput
  new Chart(document.getElementById('chartThroughput'), {
    type: 'bar',
    data: {
      labels: LABELS,
      datasets: [{
        label: 'Throughput (req/s)',
        data: TP,
        backgroundColor: LABELS.map((_, i) => PALETTE[i % PALETTE.length] + 'cc'),
        borderColor:      LABELS.map((_, i) => PALETTE[i % PALETTE.length]),
        borderWidth: 1,
      }]
    },
    options: {
      ...sharedOptions('req/s'),
      plugins: { legend: { display: false }, tooltip: { mode: 'index', intersect: false } }
    }
  });

  // 4. Error %
  new Chart(document.getElementById('chartErrors'), {
    type: 'bar',
    data: {
      labels: LABELS,
      datasets: [{
        label: 'Error %',
        data: ERR,
        backgroundColor: ERR.map(v => v > 5 ? 'rgba(192,57,43,0.85)' : v > 0 ? 'rgba(211,84,0,0.75)' : 'rgba(39,174,96,0.75)'),
        borderWidth: 1,
      }]
    },
    options: {
      ...sharedOptions('%'),
      plugins: { legend: { display: false }, tooltip: { mode: 'index', intersect: false } }
    }
  });

  // 5. Thread Count
  new Chart(document.getElementById('chartThreads'), {
    type: 'bar',
    data: {
      labels: LABELS,
      datasets: [{
        label: 'Thread Count (VUs)',
        data: TC,
        backgroundColor: LABELS.map((_, i) => PALETTE[i % PALETTE.length] + 'b3'),
        borderWidth: 1,
      }]
    },
    options: {
      ...sharedOptions('VUs'),
      plugins: { legend: { display: false }, tooltip: { mode: 'index', intersect: false } }
    }
  });

  // 6. Min / Avg / Max floating bar
  new Chart(document.getElementById('chartMinAvgMax'), {
    type: 'bar',
    data: {
      labels: LABELS,
      datasets: [
        { label: 'Min',     data: MIN_D, backgroundColor: 'rgba(39,174,96,0.8)' },
        { label: 'Average', data: AVG,   backgroundColor: 'rgba(41,128,185,0.8)' },
        { label: 'Max',     data: MAX_D, backgroundColor: 'rgba(192,57,43,0.75)' },
      ]
    },
    options: sharedOptions('ms')
  });
})();
</script>
</body>
</html>`;

fs.writeFileSync(OUTPUT_FILE, html, 'utf8');
console.log(`Report written to: ${OUTPUT_FILE}`);
