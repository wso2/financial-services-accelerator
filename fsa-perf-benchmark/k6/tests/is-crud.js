// k6/tests/is-crud.js
//
// CRUD Operations — IS consent lifecycle endpoints, run sequentially so each
// action is isolated and its latency is measured independently.
//
// Scenario map
// ─────────────────────────────────────────────────────────────────────────────
// Slot  Tag                       Method    Path
//  0    IS_CreateAccountConsent   POST      /api/fs/consent/manage/account-access-consents
//  1    IS_GetAccountConsent      GET       /api/fs/consent/manage/account-access-consents/{id}
//
// GET creates its own throwaway consent (tagged IS_Setup so those requests are
// excluded from the per-endpoint metrics) then measures only the target operation.
//
// Run:
//   k6 run --insecure-skip-tls-verify \
//          --summary-export=results/is-crud-summary.json \
//          k6/tests/is-crud.js

import { sleep } from 'k6';
import { config } from '../config.js';
import {
  createAccountConsent,
  createSetupConsent,
  getAccountConsent,
} from '../scenarios/identity-server.js';

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

const VARIANT      = __ENV.VARIANT       || 'baseline';
const ONLY_SCENARIO = __ENV.ONLY_SCENARIO || '';

// When ONLY_SCENARIO is set the test runs a single isolated scenario at slot 0.
// run-test.sh uses this to restart containers between scenarios.
const ALL_SCENARIOS = {
  create_consent: { ...scenarioAt(0), exec: 'testCreateConsent' },
  get_consent:    { ...scenarioAt(1), exec: 'testGetConsent'    },
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
    suite:   'is-crud',
    variant: VARIANT,
    peakVUs: `${config.peakVUs}`,
    runId:   config.runId,
    version: config.fsaVersion,
  },

  summaryTrendStats: ['count', 'avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  scenarios: buildScenarios(),

  thresholds: {
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
    http_req_failed:   ['rate<0.01'],

    'http_req_duration{name:IS_CreateAccountConsent}': ['p(95)<700'],
    'http_req_duration{name:IS_GetAccountConsent}':    ['p(95)<500'],

    'http_req_failed{name:IS_CreateAccountConsent}':   ['rate<=1.0'],
    'http_req_failed{name:IS_GetAccountConsent}':      ['rate<=1.0'],
  },
};

// ---------------------------------------------------------------------------
// Scenario exec functions
// ---------------------------------------------------------------------------

export function testCreateConsent() {
  createAccountConsent();
  sleep(0.5);
}

// Creates a throwaway consent (IS_Setup, excluded from metrics) then GETs it.
export function testGetConsent() {
  const id = createSetupConsent();
  if (!id) { sleep(0.5); return; }
  getAccountConsent(id);
  sleep(0.5);
}

