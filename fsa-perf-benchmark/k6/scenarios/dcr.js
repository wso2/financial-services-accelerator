// k6/scenarios/dcr.js
//
// DCR (Dynamic Client Registration) scenario functions.
//
// registerClient()       — POST /open-banking/v3.3.0/register
//   Posts a plain-JSON DCR request body with a unique jti per call.
//   Returns { clientId }. Tagged APIM_DCR_Register.
//
// deleteClient(clientId) — GET token + DELETE /open-banking/v3.3.0/register/{clientId}
//   Obtains a client_credentials Bearer token via private_key_jwt for the
//   registered client, then deletes the registration. Cleanup only.
//   Tagged APIM_DCR_Token + APIM_DCR_Delete.
//
// Required config (k6/test-config.json):
//   dcrSoftwareStatement  — SSA JWT
//   dcrSoftwareId         — software_id matching the SSA
//   dcrRedirectUri        — redirect URI for the registration request
//   clientPrivateKey      — PKCS8 PEM signing key for private_key_jwt assertion
//   clientKeyId           — kid for the JWT header

import http from 'k6/http';
import { check } from 'k6';
import encoding from 'k6/encoding';
import { config } from '../config.js';
import { safeJson } from '../lib/safe-json.js';

// ── PS256 signing (for client assertion only) ────────────────────────────────

let _signingKey = null;

function pemToArrayBuffer(pem) {
  const b64 = pem
    .replace(/-----BEGIN[^-]+-----/g, '')
    .replace(/-----END[^-]+-----/g, '')
    .replace(/\s+/g, '');
  return encoding.b64decode(b64, 'std', 'b');
}

async function getSigningKey() {
  if (_signingKey) return _signingKey;
  _signingKey = await crypto.subtle.importKey(
    'pkcs8',
    pemToArrayBuffer(config.clientPrivateKeyPem),
    { name: 'RSA-PSS', hash: { name: 'SHA-256' } },
    false,
    ['sign'],
  );
  return _signingKey;
}

async function buildClientAssertion(clientId) {
  const now = Math.floor(Date.now() / 1000);
  const header  = encoding.b64encode(
    JSON.stringify({ kid: config.clientKeyId, typ: 'JWT', alg: 'PS256' }),
    'rawurl',
  );
  const payload = encoding.b64encode(
    JSON.stringify({
      sub: clientId,
      iss: clientId,
      aud: `${config.isHost}/oauth2/token`,
      exp: now + 300,
      iat: now,
      jti: Date.now().toString(),
    }),
    'rawurl',
  );
  const signingInput = `${header}.${payload}`;
  const key = await getSigningKey();
  const inputBytes = new Uint8Array(signingInput.length);
  for (let i = 0; i < signingInput.length; i++) inputBytes[i] = signingInput.charCodeAt(i);
  const sigBuf = await crypto.subtle.sign(
    { name: 'RSA-PSS', saltLength: 32 },
    key,
    inputBytes,
  );
  const sigBytes = new Uint8Array(sigBuf);
  let bin = '';
  for (let i = 0; i < sigBytes.length; i++) bin += String.fromCharCode(sigBytes[i]);
  return `${signingInput}.${encoding.b64encode(bin, 'rawurl')}`;
}

// Registers a new DCR client. Returns the client_id on success, null on failure.
export function registerClient() {
  const now = Math.floor(Date.now() / 1000);

  const requestBody = JSON.stringify({
    iss:                             config.dcrSoftwareId,
    iat:                             now,
    exp:                             now + 3600,
    jti:                             Date.now().toString(),
    aud:                             'https://localbank.com',
    scope:                           'accounts payments fundsconfirmations',
    token_endpoint_auth_method:      'private_key_jwt',
    token_endpoint_auth_signing_alg: 'PS256',
    grant_types:                     ['authorization_code', 'client_credentials', 'refresh_token'],
    response_types:                  ['code id_token'],
    id_token_signed_response_alg:    'PS256',
    id_token_encrypted_response_alg: 'RSA-OAEP',
    id_token_encrypted_response_enc: 'A256GCM',
    request_object_signing_alg:      'PS256',
    application_type:                'web',
    software_id:                     config.dcrSoftwareId,
    redirect_uris:                   [config.dcrRedirectUri],
    software_statement:              config.dcrSoftwareStatement,
  });

  const res = http.post(
    `${config.apimHost}/open-banking/v3.3.0/register`,
    requestBody,
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'APIM_DCR_Register' },
    },
  );
check(res, { 'dcr register: status 201': (r) => r.status === 201 });
  const body = safeJson(res);
  if (!body) return null;
  return { clientId: body.client_id };
}

// Per-iteration cleanup — deletes the registered app via IS admin API (Basic auth)
// so the next iteration starts with a clean slate.
// The DCR client's JWKS is hosted on an external URL that the IS Docker container
// cannot reach, so the private_key_jwt token flow is not used for cleanup.
export function deleteClient() {
  const adminAuth = config.consentAdminAuthHeader;
  const appName   = config.dcrSoftwareId;

  const searchRes = http.get(
    `${config.isHost}/api/server/v1/applications?filter=name+eq+${appName}&limit=1`,
    { headers: { Authorization: adminAuth, Accept: 'application/json' }, tags: { name: 'IS_Admin_AppSearch' } },
  );
  check(searchRes, { 'dcr cleanup: app search 200': (r) => r.status === 200 });

  const body = safeJson(searchRes);
  if (!body) return;
  const apps = body.applications || [];
  if (apps.length === 0) return;

  const delRes = http.del(
    `${config.isHost}/api/server/v1/applications/${apps[0].id}`,
    null,
    { headers: { Authorization: adminAuth }, tags: { name: 'IS_Admin_AppDelete' } },
  );
  check(delRes, { 'dcr cleanup: app delete 204': (r) => r.status === 204 });
}
