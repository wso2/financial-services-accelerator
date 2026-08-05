#!/usr/bin/env node
// scripts/setup-dcr-client.js
//
// Registers a fresh DCR client and writes the client_id to test-config.json.
//
// Strategy (two-step):
//   1. POST DCR registration with token_endpoint_auth_method=private_key_jwt.
//      This succeeds (201) and creates both the APIM consumer app and the IS SP.
//   2. Immediately PATCH the IS SP's inbound OIDC config to change auth to
//      tls_client_auth so k6 can obtain tokens using the transport cert
//      (private_key_jwt fails because the external JWKS endpoint returns 403).
//
// The resulting client_id is written to test-config.json as `mtlsClientId`.
// k6 tests that call APIM use getMtlsToken() (tls_client_auth, no JWT assertion)
// and present the transport cert via tlsAuth for both IS and APIM requests.
//
// Usage:
//   node scripts/setup-dcr-client.js
//   (called automatically by scripts/run-test.sh before apim-crud)

'use strict';

const fs    = require('fs');
const https = require('https');
const path  = require('path');

const CONFIG_PATH = path.join(__dirname, '../k6/test-config.json');

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function httpsRequest(method, urlStr, body, headers, certPem, keyPem) {
  return new Promise((resolve, reject) => {
    const url = new URL(urlStr);
    const options = {
      hostname:           url.hostname,
      port:               url.port || 443,
      path:               url.pathname + url.search,
      method,
      headers,
      cert:               certPem,
      key:                keyPem,
      rejectUnauthorized: false,
    };
    const req = https.request(options, res => {
      let data = '';
      res.on('data', chunk => { data += chunk; });
      res.on('end',  () => resolve({ status: res.statusCode, body: data }));
    });
    req.on('error', reject);
    if (body) req.write(body);
    req.end();
  });
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

async function main() {
  const cfg = JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf8'));

  const isHost     = cfg.isHost     || 'https://obiam:9446';
  const apimHost   = cfg.apimHost   || 'https://obam:8243';
  const softwareId = cfg.dcrSoftwareId;
  const certPem    = cfg.transportCertPem;
  const keyPem     = cfg.transportKeyPem;

  const adminUser  = cfg.consentAdminUser     || 'is_admin@wso2.com';
  const adminPass  = cfg.consentAdminPassword || 'wso2123';
  const adminAuth  = 'Basic ' + Buffer.from(`${adminUser}:${adminPass}`).toString('base64');

  // ── Step 1: delete any stale DCR app in IS ────────────────────────────────
  console.log(`[DCR setup] Checking for existing app (name=${softwareId})...`);

  const searchRes = await httpsRequest(
    'GET',
    `${isHost}/api/server/v1/applications?filter=name+eq+${softwareId}&limit=1`,
    null,
    { Authorization: adminAuth, Accept: 'application/json' },
    certPem, keyPem,
  );

  if (searchRes.status === 200) {
    let searchBody;
    try { searchBody = JSON.parse(searchRes.body); } catch (_) { searchBody = {}; }
    const apps = (searchBody && searchBody.applications) || [];

    if (apps.length > 0) {
      const appId = apps[0].id;
      console.log(`[DCR setup] Deleting stale app id=${appId}...`);
      const delRes = await httpsRequest(
        'DELETE',
        `${isHost}/api/server/v1/applications/${appId}`,
        null,
        { Authorization: adminAuth },
        certPem, keyPem,
      );
      if (delRes.status === 204) {
        console.log('[DCR setup] Stale app deleted.');
      } else {
        console.warn(`[DCR setup] Delete returned ${delRes.status}: ${delRes.body}`);
      }
    } else {
      console.log('[DCR setup] No stale app found.');
    }
  } else {
    console.warn(`[DCR setup] IS app search returned ${searchRes.status} — skipping stale cleanup.`);
  }

  // ── Step 2: register new DCR client with private_key_jwt ─────────────────
  // private_key_jwt makes APIM return 201 and creates both the IS SP and the
  // APIM consumer app.  We'll switch the IS SP to tls_client_auth in step 3.
  const now = Math.floor(Date.now() / 1000);
  const requestBody = JSON.stringify({
    iss:                             softwareId,
    iat:                             now,
    exp:                             now + 3600,
    jti:                             Date.now().toString(),
    aud:                             'https://localbank.com',
    scope:                           'accounts payments fundsconfirmations',
    token_endpoint_auth_method:              'private_key_jwt',
    token_endpoint_auth_signing_alg:         'PS256',
    grant_types:                     ['authorization_code', 'client_credentials', 'refresh_token'],
    response_types:                  ['code id_token'],
    id_token_signed_response_alg:    'PS256',
    id_token_encrypted_response_alg: 'RSA-OAEP',
    id_token_encrypted_response_enc: 'A256GCM',
    request_object_signing_alg:      'PS256',
    application_type:                'web',
    software_id:                     softwareId,
    redirect_uris:                   [cfg.dcrRedirectUri],
    software_statement:              cfg.dcrSoftwareStatement,
  });

  // Retry up to 3 times with a 3s delay — IS may take a moment to clean up
  // the previous app before accepting a new registration for the same software_id.
  let regRes;
  for (let attempt = 1; attempt <= 3; attempt++) {
    console.log(`[DCR setup] Registering DCR client (attempt ${attempt}/3)...`);
    regRes = await httpsRequest(
      'POST',
      `${apimHost}/open-banking/v3.3.0/register`,
      requestBody,
      { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(requestBody) },
      certPem, keyPem,
    );
    if (regRes.status === 201) break;
    console.warn(`[DCR setup] Registration returned ${regRes.status}: ${regRes.body}`);
    if (attempt < 3) {
      console.log('[DCR setup] Retrying in 3s...');
      await new Promise(r => setTimeout(r, 3000));
    }
  }

  if (regRes.status !== 201) {
    console.error(`[DCR setup] Registration failed after 3 attempts (status=${regRes.status}):`);
    console.error(regRes.body);
    process.exit(1);
  }

  let regBody;
  try { regBody = JSON.parse(regRes.body); } catch (e) {
    console.error('[DCR setup] Failed to parse registration response:', regRes.body);
    process.exit(1);
  }

  const clientId = regBody.client_id;
  if (!clientId) {
    console.error('[DCR setup] No client_id in registration response:', regRes.body);
    process.exit(1);
  }
  console.log(`[DCR setup] Registered: client_id=${clientId}`);

  // ── Step 3: patch IS SP to use tls_client_auth ────────────────────────────
  // private_key_jwt token requests fail because IS cannot reach the external
  // JWKS endpoint (keystore.openbankingtest.org.uk returns 403).
  // Switching to tls_client_auth lets IS validate via the transport cert instead.
  console.log(`[DCR setup] Patching IS SP to tls_client_auth...`);

  const searchRes2 = await httpsRequest(
    'GET',
    `${isHost}/api/server/v1/applications?filter=name+eq+${softwareId}&limit=1`,
    null,
    { Authorization: adminAuth, Accept: 'application/json' },
    certPem, keyPem,
  );

  let appId;
  if (searchRes2.status === 200) {
    let sb;
    try { sb = JSON.parse(searchRes2.body); } catch (_) { sb = {}; }
    const apps = (sb && sb.applications) || [];
    if (apps.length > 0) {
      appId = apps[0].id;
    }
  }

  if (!appId) {
    console.error('[DCR setup] Could not find IS SP after registration — cannot patch auth method.');
    process.exit(1);
  }

  // GET current OIDC inbound config
  const oidcGetRes = await httpsRequest(
    'GET',
    `${isHost}/api/server/v1/applications/${appId}/inbound-protocols/oidc`,
    null,
    { Authorization: adminAuth, Accept: 'application/json' },
    certPem, keyPem,
  );

  if (oidcGetRes.status !== 200) {
    console.error(`[DCR setup] Failed to GET OIDC config (status=${oidcGetRes.status}): ${oidcGetRes.body}`);
    process.exit(1);
  }

  let oidcConfig;
  try { oidcConfig = JSON.parse(oidcGetRes.body); } catch (e) {
    console.error('[DCR setup] Failed to parse OIDC config:', oidcGetRes.body);
    process.exit(1);
  }

  // Switch auth method to tls_client_auth.
  // Removing tokenEndpointAllowReusePvtKeyJwt avoids a 400 from IS when the
  // field conflicts with the new auth method.
  if (!oidcConfig.clientAuthentication) oidcConfig.clientAuthentication = {};
  oidcConfig.clientAuthentication.tokenEndpointAuthMethod = 'tls_client_auth';
  delete oidcConfig.clientAuthentication.tokenEndpointAllowReusePvtKeyJwt;

  const oidcPutBody = JSON.stringify(oidcConfig);
  const oidcPutRes = await httpsRequest(
    'PUT',
    `${isHost}/api/server/v1/applications/${appId}/inbound-protocols/oidc`,
    oidcPutBody,
    {
      Authorization: adminAuth,
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(oidcPutBody),
    },
    certPem, keyPem,
  );

  if (oidcPutRes.status === 200) {
    console.log('[DCR setup] IS SP patched to tls_client_auth.');
  } else {
    console.warn(`[DCR setup] IS SP OIDC PUT returned ${oidcPutRes.status}: ${oidcPutRes.body}`);
  }

  // ── Step 3b: patch app certificate to PEM (transport cert) ────────────────
  // The FSA DCR handler sets the app certificate to the external JWKS URI
  // (keystore.openbankingtest.org.uk) which returns 403. Switch it to PEM so
  // IS validates JAR signatures against the local transport cert instead.
  console.log('[DCR setup] Patching IS SP certificate to PEM (transport cert)...');
  const certBody = certPem.split('\n').filter(l => !l.startsWith('-----') && l.trim()).join('');
  const certPatchBody = JSON.stringify({
    advancedConfigurations: { certificate: { type: 'PEM', value: certBody } },
  });
  const certPatchRes = await httpsRequest(
    'PATCH',
    `${isHost}/api/server/v1/applications/${appId}`,
    certPatchBody,
    {
      Authorization: adminAuth,
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(certPatchBody),
    },
    certPem, keyPem,
  );
  if (certPatchRes.status === 200) {
    console.log('[DCR setup] IS SP certificate set to PEM.');
  } else {
    console.warn(`[DCR setup] IS SP cert PATCH returned ${certPatchRes.status}: ${certPatchRes.body}`);
  }

  // ── Step 3c: re-assert tls_client_auth (cert PATCH may reset OIDC fields) ─
  const oidcVerifyRes = await httpsRequest(
    'GET',
    `${isHost}/api/server/v1/applications/${appId}/inbound-protocols/oidc`,
    null,
    { Authorization: adminAuth, Accept: 'application/json' },
    certPem, keyPem,
  );
  if (oidcVerifyRes.status === 200) {
    let oidcVerify;
    try { oidcVerify = JSON.parse(oidcVerifyRes.body); } catch (_) { oidcVerify = {}; }
    const currentMethod = oidcVerify.clientAuthentication?.tokenEndpointAuthMethod;
    if (currentMethod !== 'tls_client_auth') {
      console.log(`[DCR setup] Auth method drifted to '${currentMethod}' after cert patch — re-asserting tls_client_auth...`);
      if (!oidcVerify.clientAuthentication) oidcVerify.clientAuthentication = {};
      oidcVerify.clientAuthentication.tokenEndpointAuthMethod = 'tls_client_auth';
      delete oidcVerify.clientAuthentication.tokenEndpointAllowReusePvtKeyJwt;
      const reAssertBody = JSON.stringify(oidcVerify);
      const reAssertRes = await httpsRequest(
        'PUT',
        `${isHost}/api/server/v1/applications/${appId}/inbound-protocols/oidc`,
        reAssertBody,
        {
          Authorization: adminAuth,
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(reAssertBody),
        },
        certPem, keyPem,
      );
      if (reAssertRes.status === 200) {
        console.log('[DCR setup] tls_client_auth re-asserted.');
      } else {
        console.warn(`[DCR setup] Re-assert returned ${reAssertRes.status}: ${reAssertRes.body}`);
      }
    } else {
      console.log('[DCR setup] Auth method confirmed: tls_client_auth.');
    }
  }

  // ── Step 4: write mtlsClientId and clientId to test-config.json ─────────
  cfg.mtlsClientId = clientId;
  cfg.clientId     = clientId;  // IS CRUD uses x-wso2-client-id: clientId
  fs.writeFileSync(CONFIG_PATH, JSON.stringify(cfg, null, 2) + '\n');
  console.log(`[DCR setup] test-config.json updated: mtlsClientId=${clientId} clientId=${clientId}`);
}

main().catch(err => {
  console.error('[DCR setup] Error:', err.message);
  process.exit(1);
});
