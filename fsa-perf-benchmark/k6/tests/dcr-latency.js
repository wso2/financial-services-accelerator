// k6/tests/dcr-latency.js
//
// DCR Latency — measures the response time of POST /api/fs/dcr/v3.0/register.
//
// Runs with a single VU (one cert, one signing key) for the configured
// steady-state duration. Each iteration:
//   1. POST /register        ← measured  (APIM_DCR_Register)
//   2. DELETE /register/{id} ← cleanup   (APIM_DCR_Delete, not the target metric)
//
// The DELETE ensures the registered app is removed before the next iteration
// so each POST is a fresh registration without a client_id conflict.
//
// mTLS: the transport certificate (transportCertPem / transportKeyPem) is
// attached to every request via k6 tlsAuth so APIM can authenticate the TPP.
//
// Prerequisites (set in k6/test-config.json):
//   transportCertPem     — OB transport certificate (PEM) for mTLS
//   transportKeyPem      — private key for the transport certificate (PEM)
//   dcrSoftwareStatement — SSA JWT issued by the certificate authority / directory
//   dcrSoftwareId        — software_id matching the SSA
//   dcrRedirectUri       — redirect URI for the registration
//   clientPrivateKeyPem  — RSA private key (PKCS8 PEM) used to sign the DCR request JWT
//   clientKeyId          — kid for the JWT header
//
// Run:
//   k6 run --insecure-skip-tls-verify \
//          --summary-export=results/dcr-latency-summary.json \
//          k6/tests/dcr-latency.js

import http from 'k6/http';
import { sleep } from 'k6';
import { config } from '../config.js';
import { registerClient, deleteClient } from '../scenarios/dcr.js';

// ---------------------------------------------------------------------------
// Options — single VU, steady-state duration only (no ramp needed for 1 VU)
// ---------------------------------------------------------------------------

export const options = {
  tags: {
    suite:   'apim-dcr-latency',
    runId:   config.runId,
    version: config.fsaVersion,
  },

  summaryTrendStats: ['count', 'avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  // mTLS — present the transport certificate on every outbound connection to APIM
  tlsAuth: [
    {
      cert: config.transportCertPem,
      key:  config.transportKeyPem,
    },
  ],

  scenarios: {
    dcr_post: {
      executor: 'constant-vus',
      vus:      1,
      duration: config.steadyDuration,
    },
  },

  thresholds: {
    // Target metric — DCR POST response time
    'http_req_duration{name:APIM_DCR_Register}': ['p(95)<3000', 'p(99)<5000'],
    'http_req_failed{name:APIM_DCR_Register}':   ['rate<0.01'],

    // IS admin cleanup — must succeed so each iteration starts clean
    'http_req_failed{name:IS_Admin_AppSearch}':  ['rate<0.01'],
    'http_req_failed{name:IS_Admin_AppDelete}':  ['rate<0.01'],
  },
};

// ---------------------------------------------------------------------------
// Setup — delete any stale DCR registration left from a previous run.
// Uses the IS admin REST API (Basic auth) to find the application by
// software_id name and delete it — no DCR Bearer token required.
// ---------------------------------------------------------------------------

export function setup() {
  const adminAuth = config.consentAdminAuthHeader;
  const appName   = config.dcrSoftwareId;

  // Search IS for any application whose name matches the software_id
  const searchRes = http.get(
    `${config.isHost}/api/server/v1/applications?filter=name+eq+${appName}&limit=1`,
    { headers: { Authorization: adminAuth, Accept: 'application/json' } },
  );

  if (searchRes.status !== 200) {
    console.log(`setup: IS app search returned ${searchRes.status} — skipping stale cleanup`);
    return;
  }

  let body;
  try { body = JSON.parse(searchRes.body); } catch (_) { return; }

  const apps = (body && body.applications) || [];
  if (apps.length === 0) {
    console.log(`setup: no stale DCR app found for software_id=${appName}`);
    return;
  }

  const appId = apps[0].id;
  console.log(`setup: deleting stale DCR app id=${appId} name=${appName}`);

  const delRes = http.del(
    `${config.isHost}/api/server/v1/applications/${appId}`,
    null,
    { headers: { Authorization: adminAuth } },
  );
  console.log(`setup: delete status=${delRes.status}`);
}

// ---------------------------------------------------------------------------
// Default function — one iteration per VU loop
// ---------------------------------------------------------------------------

export default function () {
  // Step 1: Register — measured
  registerClient();
  sleep(0.5);

  // Step 2: Delete via IS admin API — cleanup so next iteration starts clean
  deleteClient();
  sleep(0.5);
}
