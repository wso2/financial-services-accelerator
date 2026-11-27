// k6/tests/apim-crud.js
//
// APIM CRUD Operations — API Manager gateway endpoints, run sequentially so
// each action is isolated and its latency is measured independently.
//
// Scenario map
// ─────────────────────────────────────────────────────────────────────────────
// Slot  Tag                        Method   Path
//  0    APIM_GetAccounts           GET      {aispContext}/accounts
//  1    APIM_GetBalances           GET      {aispContext}/accounts/{id}/balances
//  2    APIM_GetTransactions       GET      {aispContext}/accounts/{id}/transactions
//  3    APIM_CreatePaymentConsent  POST     {pispContext}/domestic-payment-consents
//  4    APIM_GetPaymentConsent     GET      {pispContext}/domestic-payment-consents/{id}
//
// GetBalances and GetTransactions each fetch an accountId first (tagged
// APIM_Setup, excluded from metrics) before measuring the target call.
// GetPaymentConsent creates a throwaway consent (APIM_Setup) before the GET.
//
// Prerequisites:
//   - A valid OAuth2 client with accounts and payments scopes configured in
//     k6/test-config.json (clientId, clientSecret or clientPrivateKey).
//   - A running APIM gateway reachable at apimHost.
//
// Run:
//   k6 run --insecure-skip-tls-verify \
//          --summary-export=results/apim-crud-summary.json \
//          k6/tests/apim-crud.js

import { sleep } from 'k6';
import { config } from '../config.js';
import { getAccounts, getBalances, getTransactions } from '../scenarios/accounts.js';
import { createPaymentConsent, getPaymentConsent } from '../scenarios/payments.js';

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

const VARIANT       = __ENV.VARIANT       || 'baseline';
const ONLY_SCENARIO = __ENV.ONLY_SCENARIO || '';

// When ONLY_SCENARIO is set the test runs a single isolated scenario at slot 0.
// run-test.sh uses this to restart containers between scenarios.
const ALL_SCENARIOS = {
  get_accounts:           { ...scenarioAt(0), exec: 'testGetAccounts'          },
  get_balances:           { ...scenarioAt(1), exec: 'testGetBalances'          },
  get_transactions:       { ...scenarioAt(2), exec: 'testGetTransactions'      },
  create_payment_consent: { ...scenarioAt(3), exec: 'testCreatePaymentConsent' },
  get_payment_consent:    { ...scenarioAt(4), exec: 'testGetPaymentConsent'    },
};

function buildScenarios() {
  if (ONLY_SCENARIO) {
    if (!ALL_SCENARIOS[ONLY_SCENARIO]) throw new Error(`Unknown scenario: ${ONLY_SCENARIO}`);
    return { [ONLY_SCENARIO]: { ...ALL_SCENARIOS[ONLY_SCENARIO], startTime: '0s' } };
  }
  return ALL_SCENARIOS;
}

export const options = {
  // Present the transport certificate for all requests to IS and APIM:
  //  - IS token endpoint: validates cert for tls_client_auth
  //  - APIM gateway: enforces FAPI cert binding on every API call
  tlsAuth: [
    {
      domains: ['obiam', 'obam', 'localhost'],
      cert: config.transportCertPem,
      key:  config.transportKeyPem,
    },
  ],

  tags: {
    suite:   'apim-crud',
    variant: VARIANT,
    peakVUs: `${config.peakVUs}`,
    runId:   config.runId,
    version: config.fsaVersion,
  },

  summaryTrendStats: ['count', 'avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  scenarios: buildScenarios(),

  thresholds: {
    http_req_duration: ['p(95)<1500', 'p(99)<3000'],
    http_req_failed:   ['rate<0.01'],

    'http_req_duration{name:APIM_GetAccounts}':          ['p(95)<800'],
    'http_req_duration{name:APIM_GetBalances}':          ['p(95)<800'],
    'http_req_duration{name:APIM_GetTransactions}':      ['p(95)<1000'],
    'http_req_duration{name:APIM_CreatePaymentConsent}': ['p(95)<1200'],
    'http_req_duration{name:APIM_GetPaymentConsent}':    ['p(95)<800'],

    'http_req_failed{name:APIM_GetAccounts}':            ['rate<=1.0'],
    'http_req_failed{name:APIM_GetBalances}':            ['rate<=1.0'],
    'http_req_failed{name:APIM_GetTransactions}':        ['rate<=1.0'],
    'http_req_failed{name:APIM_CreatePaymentConsent}':   ['rate<=1.0'],
    'http_req_failed{name:APIM_GetPaymentConsent}':      ['rate<=1.0'],
  },
};

// ---------------------------------------------------------------------------
// Scenario exec functions
// ---------------------------------------------------------------------------

export function testGetAccounts() {
  getAccounts();
  sleep(0.5);
}

// Fetches accountId via APIM_Setup call, then measures GET /balances.
export function testGetBalances() {
  getBalances();
  sleep(0.5);
}

// Fetches accountId via APIM_Setup call, then measures GET /transactions.
export function testGetTransactions() {
  getTransactions();
  sleep(0.5);
}

export function testCreatePaymentConsent() {
  createPaymentConsent();
  sleep(0.5);
}

// Creates a throwaway consent via APIM_Setup call, then measures GET /domestic-payment-consents/{id}.
export function testGetPaymentConsent() {
  getPaymentConsent();
  sleep(0.5);
}
