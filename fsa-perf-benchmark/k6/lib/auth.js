import http from 'k6/http';
import { check } from 'k6';
import encoding from 'k6/encoding';
import { config } from '../config.js';
import { safeJson } from './safe-json.js';

// ---------------------------------------------------------------------------
// private_key_jwt (PS256) support
// ---------------------------------------------------------------------------

// Cached per-VU CryptoKey — imported once on first use, reused every iteration.
let _cryptoKey = null;

function pemToArrayBuffer(pem) {
  const b64 = pem
    .replace(/-----BEGIN[^-]+-----/g, '')
    .replace(/-----END[^-]+-----/g, '')
    .replace(/\s+/g, '');
  return encoding.b64decode(b64, 'std', 'b');
}

async function getCryptoKey() {
  if (_cryptoKey) return _cryptoKey;
  _cryptoKey = await crypto.subtle.importKey(
    'pkcs8',
    pemToArrayBuffer(config.clientPrivateKeyPem),
    { name: 'RSA-PSS', hash: { name: 'SHA-256' } },
    false,
    ['sign'],
  );
  return _cryptoKey;
}

async function buildClientAssertion() {
  const now = Math.floor(Date.now() / 1000);
  const header = encoding.b64encode(
    JSON.stringify({ kid: config.clientKeyId, typ: 'JWT', alg: 'PS256' }),
    'rawurl',
  );
  const payload = encoding.b64encode(
    JSON.stringify({
      sub: config.clientId,
      aud: `${config.isHost}/oauth2/token`,
      iss: config.clientId,
      exp: now + 300,
      iat: now,
      jti: `${Date.now()}`,
    }),
    'rawurl',
  );

  const signingInput = `${header}.${payload}`;
  const key = await getCryptoKey();
  const inputBytes = new Uint8Array(signingInput.length);
  for (let i = 0; i < signingInput.length; i++) inputBytes[i] = signingInput.charCodeAt(i);
  const sigBuffer = await crypto.subtle.sign(
    { name: 'RSA-PSS', saltLength: 32 },
    key,
    inputBytes,
  );

  const sigBytes = new Uint8Array(sigBuffer);
  let binary = '';
  for (let i = 0; i < sigBytes.length; i++) binary += String.fromCharCode(sigBytes[i]);
  return `${signingInput}.${encoding.b64encode(binary, 'rawurl')}`;
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

// Builds the token request body for whichever auth method is configured:
//   - CLIENT_PRIVATE_KEY set → private_key_jwt (PS256)
//   - CLIENT_SECRET set      → client_secret_post
async function tokenBody(scope) {
  if (config.clientPrivateKeyPem) {
    const assertion = await buildClientAssertion();
    const body = {
      grant_type: 'client_credentials',
      client_assertion_type: 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
      client_id: config.clientId,
      client_assertion: assertion,
      scope,
    };
    if (config.redirectUri) body.redirect_uri = config.redirectUri;
    return body;
  }

  // client_secret_post fallback
  const body = {
    grant_type: 'client_credentials',
    client_id: config.clientId,
    client_secret: config.clientSecret,
    scope,
  };
  if (config.redirectUri) body.redirect_uri = config.redirectUri;
  return body;
}

// Token for APIM CRUD tests — uses tls_client_auth (no JWT assertion needed).
// IS validates the transport cert presented at the TLS layer; the caller must
// configure tlsAuth in k6 options (see k6/tests/apim-crud.js).
// mtlsClientId is written by scripts/setup-dcr-client.js.
export function getMtlsToken(scope = 'accounts openid') {
  const res = http.post(
    `${config.isHost}/oauth2/token`,
    {
      grant_type: 'client_credentials',
      client_id:  config.mtlsClientId,
      scope,
    },
    { tags: { name: 'IS_Token' } },
  );

  check(res, {
    'token: status 200':       (r) => r.status === 200,
    'token: has access_token': (r) => !!safeJson(r, 'access_token'),
  });

  return safeJson(res, 'access_token');
}

export async function getAccessToken(scope = 'accounts openid') {
  const res = http.post(
    `${config.isHost}/oauth2/token`,
    await tokenBody(scope),
    { tags: { name: 'IS_Token' } },
  );

  check(res, {
    'token: status 200': (r) => r.status === 200,
    'token: has access_token': (r) => !!safeJson(r, 'access_token'),
  });

  return safeJson(res, 'access_token');
}

export function authHeader(token) {
  return { Authorization: `Bearer ${token}` };
}

export async function introspectToken(token) {
  if (!token) return;

  let body;
  if (config.clientPrivateKeyPem) {
    const assertion = await buildClientAssertion();
    body = {
      token,
      client_assertion_type: 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
      client_id: config.clientId,
      client_assertion: assertion,
    };
  } else {
    body = {
      token,
      client_id: config.clientId,
      client_secret: config.clientSecret,
    };
  }

  const res = http.post(`${config.isHost}/oauth2/introspect`, body, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    tags: { name: 'IS_IntrospectToken' },
  });
  check(res, { 'introspect: status 200': (r) => r.status === 200 });
}
