// k6/config.js
// Primary configuration is read from test-config.json in this directory.
// Every field can be overridden at runtime with a -e ENV_VAR=value flag —
// useful for CI or when you want to switch one value without editing the file.
//
// To run: just edit test-config.json, then:
//   k6 run --insecure-skip-tls-verify \
//          --summary-export=results/fs-is-summary.json \
//          k6/tests/fs-is.js

import encoding from 'k6/encoding';

const _f = JSON.parse(open('./test-config.json'));

// Env var wins over file value; file value wins over hardcoded default.
function _v(fileVal, envVal, fallback) {
  if (envVal) return envVal;
  if (fileVal !== undefined && fileVal !== '') return fileVal;
  return fallback;
}

const _adminUser = _v(_f.consentAdminUser, __ENV.CONSENT_ADMIN_USER, 'is_admin@wso2.com');
const _adminPass = _v(_f.consentAdminPassword, __ENV.CONSENT_ADMIN_PASSWORD, 'wso2123');

export const config = {
  // --- Suite selection ---
  // "is"       → IS consent endpoints only
  // "is+apim"  → IS + APIM accounts & payments endpoints
  suite: _v(_f.suite, __ENV.SUITE, 'is'),

  // --- Connection ---
  isHost:   _v(_f.isHost,   __ENV.IS_HOST,   'https://localhost:9446'),
  apimHost: _v(_f.apimHost, __ENV.APIM_HOST,  'https://localhost:8243'),

  // --- Client identity (IS — used for IS CRUD and token requests via private_key_jwt) ---
  clientId:            _v(_f.clientId,           __ENV.CLIENT_ID,           'CHANGE_ME'),
  clientSecret:        _v(_f.clientSecret,        __ENV.CLIENT_SECRET,        ''),
  clientPrivateKeyPem: _v(_f.clientPrivateKey,    __ENV.CLIENT_PRIVATE_KEY,   ''),
  clientKeyId:         _v(_f.clientKeyId,         __ENV.CLIENT_KEY_ID,        ''),
  redirectUri:         _v(_f.redirectUri,         __ENV.REDIRECT_URI,         ''),

  // --- APIM client (DCR-registered — written by scripts/setup-dcr-client.js) ---
  // Uses tls_client_auth: IS validates the transport cert at the token endpoint;
  // APIM enforces cert binding on API calls.  No JWT assertion required.
  mtlsClientId:        _v(_f.mtlsClientId,         __ENV.MTLS_CLIENT_ID,       ''),

  // --- APPLICATION_USER token for AISP data access ---
  // Pre-obtained via authorization code flow (scripts/setup-user-auth.js).
  // FSA IS replaces scope=accounts with consent_id claim; AISP data endpoints
  // require aut=APPLICATION_USER (enforced by ClaimBasedResourceAccessValidationMediator).
  applicationUserToken:     _v(_f.applicationUserToken,     __ENV.APPLICATION_USER_TOKEN,     ''),
  applicationUserConsentId: _v(_f.applicationUserConsentId, __ENV.APPLICATION_USER_CONSENT_ID, ''),

  // --- DCR ---
  dcrSoftwareStatement: _v(_f.dcrSoftwareStatement, __ENV.DCR_SOFTWARE_STATEMENT, ''),
  dcrSoftwareId:        _v(_f.dcrSoftwareId,        __ENV.DCR_SOFTWARE_ID,        ''),
  dcrRedirectUri:       _v(_f.dcrRedirectUri,        __ENV.DCR_REDIRECT_URI,       ''),

  // --- mTLS transport certificate (used in tlsAuth for gateway DCR tests) ---
  transportCertPem: _v(_f.transportCertPem, __ENV.TRANSPORT_CERT_PEM, ''),
  transportKeyPem:  _v(_f.transportKeyPem,  __ENV.TRANSPORT_KEY_PEM,  ''),

  // --- Consent admin auth ---
  // Built from consentAdminUser + consentAdminPassword in test-config.json.
  // Override the whole header directly with -e CONSENT_ADMIN_AUTH="Basic ..."
  consentAdminAuthHeader: __ENV.CONSENT_ADMIN_AUTH ||
    `Basic ${encoding.b64encode(`${_adminUser}:${_adminPass}`)}`,

  // --- API spec ---
  fapiFinancialId: _v(_f.fapiFinancialId, __ENV.FAPI_FINANCIAL_ID, 'open-bank'),
  aispContext:     _v(_f.aispContext,     __ENV.AISP_CONTEXT,      '/open-banking/v3.1/aisp'),
  pispContext:     _v(_f.pispContext,     __ENV.PISP_CONTEXT,      '/open-banking/v3.1/pisp'),

  // --- Misc ---
  userId: _v(_f.testUserId, __ENV.TEST_USER_ID, 'psu@wso2.com'),

  // --- Load profile (edit in test-config.json) ---
  peakVUs:          parseInt(_v(_f.peakVUs,          __ENV.PEAK_VUS,           '10')),
  warmupDuration:   _v(_f.warmupDuration,   __ENV.WARMUP_DURATION,   '30s'),
  steadyDuration:   _v(_f.steadyDuration,   __ENV.STEADY_DURATION,   '5m'),
  rampDownDuration: _v(_f.rampDownDuration, __ENV.RAMP_DOWN_DURATION, '30s'),

  // --- Search scenario parameters ---
  // Used by k6/scenarios/consent-search.js and k6/tests/consent-search.js.
  // searchLimit matches the portal default page size (10 rows per page).
  searchLimit:        parseInt(_v(_f.searchLimit,        __ENV.SEARCH_LIMIT,         '10')),
  searchUserId:       _v(_f.searchUserId,       __ENV.SEARCH_USER_ID,       'psu@wso2.com'),
  // searchConsentId: fallback consentId for IS_ByConsentId if setup() returns empty.
  // Leave blank to disable the consentId filter when no ID is available.
  searchConsentId:    _v(_f.searchConsentId,    __ENV.SEARCH_CONSENT_ID,    ''),
  // searchDeepOffset: OFFSET value for the deep-pagination scenario.
  // Set high enough that the DB must skip a meaningful number of rows.
  searchDeepOffset:   parseInt(_v(_f.searchDeepOffset,  __ENV.SEARCH_DEEP_OFFSET,   '200')),
  // searchLargeLimit: page size for the large-page-size scenario.
  searchLargeLimit:   parseInt(_v(_f.searchLargeLimit,  __ENV.SEARCH_LARGE_LIMIT,   '200')),

  // --- Run identity (for cross-run comparison) ---
  // Set RUN_ID to git SHA or timestamp from your run script so every metric
  // sample carries a unique run marker usable in Prometheus/Grafana queries.
  runId:      __ENV.RUN_ID      || 'local',
  fsaVersion: __ENV.FSA_VERSION || 'unknown',
};
