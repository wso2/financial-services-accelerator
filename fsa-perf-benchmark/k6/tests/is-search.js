// k6/tests/consent-search.js
//
// Sequential search performance test for the Consent Manager Portal.
// Runs each scenario in isolation — one at a time with its own ramp-up,
// steady-state, and ramp-down — so results are directly comparable across
// filter combinations without cross-scenario load interference.
//
// 14 scenarios × (warmupDuration + steadyDuration + rampDownDuration + 30s buffer)
// At defaults (2m ramp + 12m steady + 1m ramp-down): ~14 × 15.5m ≈ 3h 37m total.
// Reduce steadyDuration in test-config.json for a shorter exploratory run.
//
// SETUP: fetches one real consentId on startup so scenario 8 (IS_ByConsentId)
// exercises an equality lookup rather than a no-op empty filter.
//
// Scenario map
// ─────────────────────────────────────────────────────────────────────────────
// Slot  Tag                     Pattern              Bottleneck signal
//  0    IS_PortalAccountsLoad   type only            full-scan cost baseline
//  1    IS_PortalPaymentsLoad   type only            payments type distribution
//  2    IS_AccountsActiveTab    type + Authorised    most common portal query
//  3    IS_AccountsInactiveTab  type + Expired,Rev.  multi-value status parse
//  4    IS_PaymentsActiveTab    type + Authorised    payments type path
//  5    IS_PaymentsInactiveTab  type + Con,Exp,Rev   3-value OR on status
//  6    IS_CofActiveTab         type + Authorised    COF type path
//  7    IS_ByConsentId          type+status+id       equality lookup (pk path)
//  8    IS_ByClientId           type+status+clientId CLIENT_ID index
//  9    IS_ByUserId             type+status+userId   INNER JOIN bottleneck
// 10    IS_DateNarrow           type+status+24h      UPDATED_TIME index tight
// 11    IS_DateWide             type+status+30d      UPDATED_TIME range scan
// 12    IS_DeepPagination       type+status+offset   OFFSET cost
// 13    IS_LargePageSize        type+status+high lim serialization cost
//
// What to look for:
//   IS_PortalAccountsLoad markedly slower than IS_AccountsActiveTab
//     → type-only filter cannot use the status index; check query plan
//   IS_ByUserId significantly slower than IS_ByClientId
//     → INNER JOIN on FS_CONSENT_AUTH_RESOURCE is the bottleneck;
//       verify USER_ID is indexed on that table
//   IS_DateWide latency >> IS_DateNarrow
//     → UPDATED_TIME index degrades with range width; normal, but check
//       if the range starts to resemble a full scan
//   IS_DeepPagination slower than IS_AccountsActiveTab at same result count
//     → OFFSET bottleneck; consider keyset pagination
//   IS_LargePageSize slower than IS_AccountsActiveTab at same offset
//     → serialization / fetch overhead; consider response streaming
//
// Run:
//   k6 run --insecure-skip-tls-verify \
//          -e RUN_ID=$(git rev-parse --short HEAD) \
//          -e FSA_VERSION=5.0.0 \
//          --summary-export=results/consent-search-summary.json \
//          k6/tests/consent-search.js

import http from 'k6/http';
import { sleep } from 'k6';
import { safeJson } from '../lib/safe-json.js';
import { config } from '../config.js';
import {
  portalAccountsLoad,
  portalPaymentsLoad,
  accountsActiveTab,
  accountsInactiveTab,
  paymentsActiveTab,
  paymentsInactiveTab,
  cofActiveTab,
  searchByConsentId,
  searchByClientId,
  searchByUserId,
  searchDateNarrow,
  searchDateWide,
  searchDeepPagination,
  searchLargePageSize,
} from '../scenarios/consent-search.js';

// ---------------------------------------------------------------------------
// Setup — fetch one real consentId for the IS_ByConsentId scenario.
// Returns { consentId } to all scenario exec functions as their first arg.
// Falls back to '' (empty) on failure — the search call will run without a
// consentId filter rather than crashing.
// ---------------------------------------------------------------------------

export function setup() {
  const res = http.get(
    `${config.isHost}/api/fs/consent/admin/search?consentTypes=accounts&limit=1`,
    {
      headers: {
        Authorization:         config.consentAdminAuthHeader,
        'x-wso2-client-id':    config.clientId,
        'x-fapi-financial-id': config.fapiFinancialId,
      },
    },
  );
  const body = safeJson(res);
  const consentId =
    (body && body.Data && body.Data[0] && body.Data[0].consentId) || '';
  return { consentId };
}

// ---------------------------------------------------------------------------
// Duration helpers
// ---------------------------------------------------------------------------

function toSecs(str) {
  let total = 0;
  const re = /(\d+(?:\.\d+)?)\s*(h|m|s|ms)/g;
  let match;
  while ((match = re.exec(str)) !== null) {
    const n = parseFloat(match[1]);
    if (match[2] === 'h')  total += n * 3600;
    if (match[2] === 'm')  total += n * 60;
    if (match[2] === 's')  total += n;
    if (match[2] === 'ms') total += n / 1000;
  }
  if (total === 0) throw new Error(`toSecs: could not parse duration "${str}" — check warmupDuration/steadyDuration/rampDownDuration in test-config.json`);
  return total;
}

const SCENARIO_SECS =
  toSecs(config.warmupDuration) +
  toSecs(config.steadyDuration) +
  toSecs(config.rampDownDuration);

const BUFFER_SECS = 30;
const SLOT        = SCENARIO_SECS + BUFFER_SECS;

function stages() {
  return [
    { duration: config.warmupDuration,   target: config.peakVUs },
    { duration: config.steadyDuration,   target: config.peakVUs },
    { duration: config.rampDownDuration, target: 0 },
  ];
}

function scenarioAt(slot) {
  return {
    executor:  'ramping-vus',
    startVUs:  0,
    startTime: `${slot * SLOT}s`,
    stages:    stages(),
  };
}

// ---------------------------------------------------------------------------
// Options
// ---------------------------------------------------------------------------

const VARIANT       = __ENV.VARIANT        || 'baseline';
const ONLY_SCENARIO = __ENV.ONLY_SCENARIO  || '';

// When ONLY_SCENARIO is set the test runs a single isolated scenario at slot 0.
// run-test.sh uses this to restart the IS container between scenarios.
const ALL_SCENARIOS = {
  // Portal initial load — type filter only (no status)
  portal_accounts_load:  { ...scenarioAt(0),  exec: 'testPortalAccountsLoad'  },
  portal_payments_load:  { ...scenarioAt(1),  exec: 'testPortalPaymentsLoad'  },

  // Tab navigation — type + status
  accounts_active_tab:   { ...scenarioAt(2),  exec: 'testAccountsActiveTab'   },
  accounts_inactive_tab: { ...scenarioAt(3),  exec: 'testAccountsInactiveTab' },
  payments_active_tab:   { ...scenarioAt(4),  exec: 'testPaymentsActiveTab'   },
  payments_inactive_tab: { ...scenarioAt(5),  exec: 'testPaymentsInactiveTab' },
  cof_active_tab:        { ...scenarioAt(6),  exec: 'testCofActiveTab'        },

  // Advanced search filters
  by_consent_id:         { ...scenarioAt(7),  exec: 'testByConsentId'         },
  by_client_id:          { ...scenarioAt(8),  exec: 'testByClientId'          },
  by_user_id:            { ...scenarioAt(9),  exec: 'testByUserId'            },
  date_narrow:           { ...scenarioAt(10), exec: 'testDateNarrow'          },
  date_wide:             { ...scenarioAt(11), exec: 'testDateWide'            },

  // Pagination
  deep_pagination:       { ...scenarioAt(12), exec: 'testDeepPagination'      },
  large_page:            { ...scenarioAt(13), exec: 'testLargePageSize'       },
};

function buildScenarios() {
  if (ONLY_SCENARIO) {
    if (!ALL_SCENARIOS[ONLY_SCENARIO]) throw new Error(`Unknown scenario: ${ONLY_SCENARIO}`);
    return { [ONLY_SCENARIO]: { ...ALL_SCENARIOS[ONLY_SCENARIO], startTime: '0s' } };
  }
  return ALL_SCENARIOS;
}

export const options = {
  tags: {
    suite:   'is-search',
    variant: VARIANT,
    peakVUs: `${config.peakVUs}`,
    runId:   config.runId,
    version: config.fsaVersion,
  },

  summaryTrendStats: ['count', 'avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  scenarios: buildScenarios(),

  thresholds: {
    // Aggregate — all search scenarios blended.
    http_req_duration: ['p(95)<3000', 'p(99)<5000'],
    http_req_failed:   ['rate<0.01'],

    // Per-scenario latency thresholds.
    // Set conservatively as starting points — run a baseline and tighten
    // each to (observed p95) + 20% headroom.

    // Portal initial load: no status filter; more rows pass the WHERE clause.
    'http_req_duration{name:IS_PortalAccountsLoad}':  ['p(95)<3000'],
    'http_req_duration{name:IS_PortalPaymentsLoad}':  ['p(95)<3000'],

    // Tab views: type + single or multi-status filter; should benefit from index.
    'http_req_duration{name:IS_AccountsActiveTab}':   ['p(95)<1000'],
    'http_req_duration{name:IS_AccountsInactiveTab}': ['p(95)<1000'],
    'http_req_duration{name:IS_PaymentsActiveTab}':   ['p(95)<1000'],
    'http_req_duration{name:IS_PaymentsInactiveTab}': ['p(95)<1200'],
    'http_req_duration{name:IS_CofActiveTab}':        ['p(95)<1000'],

    // Advanced filters.
    'http_req_duration{name:IS_ByConsentId}':         ['p(95)<500'],
    'http_req_duration{name:IS_ByClientId}':          ['p(95)<1000'],

    // INNER JOIN path: budget more time; flag if >> IS_ByClientId.
    'http_req_duration{name:IS_ByUserId}':            ['p(95)<1500'],

    // Date ranges.
    'http_req_duration{name:IS_DateNarrow}':          ['p(95)<800'],
    'http_req_duration{name:IS_DateWide}':            ['p(95)<1500'],

    // Pagination.
    'http_req_duration{name:IS_DeepPagination}':      ['p(95)<3000'],
    'http_req_duration{name:IS_LargePageSize}':       ['p(95)<2000'],

    // Error-rate thresholds (rate <= 1.0 always passes) — forces per-endpoint
    // error counts into --summary-export so post-processing can inspect them.
    'http_req_failed{name:IS_PortalAccountsLoad}':    ['rate<=1.0'],
    'http_req_failed{name:IS_PortalPaymentsLoad}':    ['rate<=1.0'],
    'http_req_failed{name:IS_AccountsActiveTab}':     ['rate<=1.0'],
    'http_req_failed{name:IS_AccountsInactiveTab}':   ['rate<=1.0'],
    'http_req_failed{name:IS_PaymentsActiveTab}':     ['rate<=1.0'],
    'http_req_failed{name:IS_PaymentsInactiveTab}':   ['rate<=1.0'],
    'http_req_failed{name:IS_CofActiveTab}':          ['rate<=1.0'],
    'http_req_failed{name:IS_ByConsentId}':           ['rate<=1.0'],
    'http_req_failed{name:IS_ByClientId}':            ['rate<=1.0'],
    'http_req_failed{name:IS_ByUserId}':              ['rate<=1.0'],
    'http_req_failed{name:IS_DateNarrow}':            ['rate<=1.0'],
    'http_req_failed{name:IS_DateWide}':              ['rate<=1.0'],
    'http_req_failed{name:IS_DeepPagination}':        ['rate<=1.0'],
    'http_req_failed{name:IS_LargePageSize}':         ['rate<=1.0'],
  },
};

// ---------------------------------------------------------------------------
// Scenario exec functions
// Each receives (data) from setup() — only testByConsentId uses it.
// ---------------------------------------------------------------------------

export function testPortalAccountsLoad()  { portalAccountsLoad();              sleep(0); }
export function testPortalPaymentsLoad()  { portalPaymentsLoad();              sleep(0); }
export function testAccountsActiveTab()   { accountsActiveTab();               sleep(0); }
export function testAccountsInactiveTab() { accountsInactiveTab();             sleep(0); }
export function testPaymentsActiveTab()   { paymentsActiveTab();               sleep(0); }
export function testPaymentsInactiveTab() { paymentsInactiveTab();             sleep(0); }
export function testCofActiveTab()        { cofActiveTab();                    sleep(0); }
export function testByConsentId(data)     { searchByConsentId(data.consentId); sleep(0); }
export function testByClientId()          { searchByClientId();                sleep(0); }
export function testByUserId()            { searchByUserId();                  sleep(0); }
export function testDateNarrow()          { searchDateNarrow();                sleep(0); }
export function testDateWide()            { searchDateWide();                  sleep(0); }
export function testDeepPagination()      { searchDeepPagination();            sleep(0); }
export function testLargePageSize()       { searchLargePageSize();             sleep(0); }
