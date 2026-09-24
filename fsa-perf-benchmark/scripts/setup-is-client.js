#!/usr/bin/env node
// scripts/setup-is-client.js
//
// Registers a fresh DCR client directly at IS (not through APIM).
// Used by the is-crud test which hits IS endpoints only.
//
// POST https://obiam:9446/api/identity/oauth2/dcr/v1.1/register/
// Auth: Basic admin credentials
//
// The resulting client_id is written to test-config.json as `clientId`.

'use strict';

const fs    = require('fs');
const https = require('https');
const path  = require('path');

const CONFIG_PATH = path.join(__dirname, '../k6/test-config.json');

function httpsRequest(method, urlStr, body, headers) {
  return new Promise((resolve, reject) => {
    const url = new URL(urlStr);
    const options = {
      hostname:           url.hostname,
      port:               url.port || 443,
      path:               url.pathname + url.search,
      method,
      headers,
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

async function main() {
  const cfg = JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf8'));

  const isHost     = cfg.isHost || 'https://obiam:9446';
  const softwareId = cfg.dcrSoftwareId;
  const adminUser  = cfg.consentAdminUser     || 'is_admin@wso2.com';
  const adminPass  = cfg.consentAdminPassword || 'wso2123';
  const adminAuth  = 'Basic ' + Buffer.from(`${adminUser}:${adminPass}`).toString('base64');

  // Decode software statement to extract org_id and client_name
  let orgId      = '0015800001HQQrZAAX';
  let clientName = 'WSO2_Open_Banking_TPP2__Sandbox_';
  try {
    const ssPayload = JSON.parse(
      Buffer.from(cfg.dcrSoftwareStatement.split('.')[1], 'base64url').toString()
    );
    if (ssPayload.org_id)              orgId      = ssPayload.org_id;
    if (ssPayload.software_client_name) clientName = ssPayload.software_client_name.replace(/[\s()]/g, '_');
  } catch (_) {}

  const jwksUri = `https://keystore.openbankingtest.org.uk/${orgId}/${softwareId}.jwks`;

  // ── Step 1: delete any stale apps ────────────────────────────────────────
  // IS DCR registers apps under client_name; APIM DCR uses software_id as name.
  // Search both to ensure a clean slate.
  const namesToClean = [softwareId, clientName];
  for (const appName of namesToClean) {
    console.log(`[IS DCR setup] Checking for existing app (name=${appName})...`);
    const searchRes = await httpsRequest(
      'GET',
      `${isHost}/api/server/v1/applications?filter=name+eq+${encodeURIComponent(appName)}&limit=1`,
      null,
      { Authorization: adminAuth, Accept: 'application/json' },
    );
    if (searchRes.status !== 200) {
      console.warn(`[IS DCR setup] App search returned ${searchRes.status} — skipping.`);
      continue;
    }
    let sb;
    try { sb = JSON.parse(searchRes.body); } catch (_) { sb = {}; }
    const apps = (sb && sb.applications) || [];
    if (apps.length > 0) {
      const appId = apps[0].id;
      console.log(`[IS DCR setup] Deleting stale app id=${appId} (name=${appName})...`);
      const delRes = await httpsRequest(
        'DELETE',
        `${isHost}/api/server/v1/applications/${appId}`,
        null,
        { Authorization: adminAuth },
      );
      if (delRes.status === 204) {
        console.log('[IS DCR setup] Stale app deleted.');
      } else {
        console.warn(`[IS DCR setup] Delete returned ${delRes.status}: ${delRes.body}`);
      }
    } else {
      console.log(`[IS DCR setup] No stale app found for name=${appName}.`);
    }
  }

  // ── Step 2: register via IS DCR endpoint ─────────────────────────────────
  const now         = Math.floor(Date.now() / 1000);
  const requestBody = JSON.stringify({
    iss:                                       softwareId,
    iat:                                       now,
    exp:                                       now + 3600,
    jti:                                       Date.now().toString(),
    aud:                                       'https://localbank.com',
    scope:                                     'accounts payments fundsconfirmations',
    token_endpoint_auth_method:                'private_key_jwt',
    token_endpoint_auth_signing_alg:           'PS256',
    grant_types:                               ['authorization_code', 'client_credentials', 'refresh_token'],
    response_types:                            ['code id_token'],
    id_token_signed_response_alg:              'PS256',
    request_object_signing_alg:                'PS256',
    application_type:                          'web',
    software_id:                               softwareId,
    redirect_uris:                             [cfg.dcrRedirectUri],
    token_endpoint_allow_reuse_pvt_key_jwt:    false,
    tls_client_certificate_bound_access_tokens: true,
    require_signed_request_object:             true,
    token_type_extension:                      'JWT',
    jwks_uri:                                  jwksUri,
    client_name:                               clientName,
    ext_application_display_name:              clientName,
    software_statement:                        cfg.dcrSoftwareStatement,
  });

  let regRes;
  for (let attempt = 1; attempt <= 3; attempt++) {
    console.log(`[IS DCR setup] Registering client at IS DCR (attempt ${attempt}/3)...`);
    regRes = await httpsRequest(
      'POST',
      `${isHost}/api/identity/oauth2/dcr/v1.1/register/`,
      requestBody,
      {
        Authorization:    adminAuth,
        'Content-Type':   'application/json',
        Accept:           'application/json',
        'Content-Length': Buffer.byteLength(requestBody),
      },
    );
    if (regRes.status === 201) break;
    console.warn(`[IS DCR setup] Registration returned ${regRes.status}: ${regRes.body}`);
    if (attempt < 3) {
      console.log('[IS DCR setup] Retrying in 3s...');
      await new Promise(r => setTimeout(r, 3000));
    }
  }

  if (regRes.status !== 201) {
    console.error(`[IS DCR setup] Registration failed after 3 attempts (status=${regRes.status}):`);
    console.error(regRes.body);
    process.exit(1);
  }

  let regBody;
  try { regBody = JSON.parse(regRes.body); } catch (e) {
    console.error('[IS DCR setup] Failed to parse registration response:', regRes.body);
    process.exit(1);
  }

  const clientId = regBody.client_id;
  if (!clientId) {
    console.error('[IS DCR setup] No client_id in registration response:', regRes.body);
    process.exit(1);
  }
  console.log(`[IS DCR setup] Registered: client_id=${clientId}`);

  // ── Step 3: write clientId to test-config.json ───────────────────────────
  cfg.clientId = clientId;
  fs.writeFileSync(CONFIG_PATH, JSON.stringify(cfg, null, 2) + '\n');
  console.log(`[IS DCR setup] test-config.json updated: clientId=${clientId}`);
}

main().catch(err => {
  console.error('[IS DCR setup] Error:', err.message);
  process.exit(1);
});
