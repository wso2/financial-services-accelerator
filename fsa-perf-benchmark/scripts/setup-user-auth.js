#!/usr/bin/env node
// scripts/setup-user-auth.js
//
// Obtains an APPLICATION_USER access token (via FSA auth code flow) and writes
// it to test-config.json as `applicationUserToken`.
//
// Flow:
//   1. client_credentials token with scope=accounts openid
//   2. POST /open-banking/v3.1/aisp/account-access-consents → consentId
//   3. Authorise consent in DB: update FS_CONSENT + insert FS_CONSENT_AUTH_RESOURCE
//      + insert FS_CONSENT_MAPPING (account IDs from test-config.json testAccountIds)
//   4. Build PS256-signed JAR containing openbanking_intent_id=consentId
//   5. GET /oauth2/authorize?request=<JAR> (no PAR) → follow redirect to IS login page
//   6. POST commonauth with username/password
//   7. IS skips consent page (pre-inserted user consent records exist)
//   8. Extract auth code from redirect callback
//   9. POST /oauth2/token (mTLS) → APPLICATION_USER token
//  10. Write token to test-config.json
//
// Prerequisites:
//   - User consent records for psu@wso2.com with accounts+openid scopes must
//     exist in IDN_OAUTH2_USER_CONSENTED_SCOPES (inserted by setup script or
//     run generate_consent_data.sql).
//   - test-config.json must have mtlsClientId, transportCertPem, transportKeyPem,
//     clientPrivateKey, clientKeyId, dcrRedirectUri, testUserId.
//   - test-config.json may include testAccountIds (array of account ID strings).
//     Defaults to ["ACC00000001", "ACC00000002"] if not set.

'use strict';

const fs            = require('fs');
const https         = require('https');
const crypto        = require('crypto');
const path          = require('path');
const url           = require('url');
const { execSync }  = require('child_process');

const CONFIG_PATH = path.join(__dirname, '../k6/test-config.json');

// ---------------------------------------------------------------------------
// Low-level HTTP helper (no auto-redirect, full cookie support)
// ---------------------------------------------------------------------------

function httpsReq(method, urlStr, body, headers, certPem, keyPem) {
  return new Promise((resolve, reject) => {
    const u = new URL(urlStr);
    const opts = {
      hostname: u.hostname,
      port: u.port || 443,
      path: u.pathname + u.search,
      method,
      headers: headers || {},
      cert: certPem,
      key: keyPem,
      rejectUnauthorized: false,
    };
    const req = https.request(opts, res => {
      let data = '';
      res.on('data', c => { data += c; });
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body: data }));
    });
    req.on('error', reject);
    if (body) req.write(typeof body === 'string' ? body : JSON.stringify(body));
    req.end();
  });
}

// Follow a single redirect (HTTP 302/301/303) and return the destination URL.
function locationOf(res) {
  return res.headers['location'] || null;
}

// Extract Set-Cookie values into a flat map.
function parseCookies(setCookieHeaders) {
  const jar = {};
  if (!setCookieHeaders) return jar;
  const arr = Array.isArray(setCookieHeaders) ? setCookieHeaders : [setCookieHeaders];
  for (const c of arr) {
    const pair = c.split(';')[0].trim();
    const eqIdx = pair.indexOf('=');
    if (eqIdx > 0) jar[pair.slice(0, eqIdx)] = pair.slice(eqIdx + 1);
  }
  return jar;
}

function cookieHeader(jar) {
  return Object.entries(jar).map(([k, v]) => `${k}=${v}`).join('; ');
}

// ---------------------------------------------------------------------------
// PS256 JWT signing (for PAR request object / JAR)
// ---------------------------------------------------------------------------

function b64urlEncode(buf) {
  return (Buffer.isBuffer(buf) ? buf : Buffer.from(buf))
    .toString('base64')
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

function buildPS256JWT(payload, privateKeyPem, kid) {
  const header = { alg: 'PS256', typ: 'JWT', kid };
  const hdr = b64urlEncode(JSON.stringify(header));
  const pld = b64urlEncode(JSON.stringify(payload));
  const signingInput = `${hdr}.${pld}`;
  const sig = crypto.sign('SHA256', Buffer.from(signingInput), {
    key: privateKeyPem,
    padding: crypto.constants.RSA_PKCS1_PSS_PADDING,
    saltLength: 32,
  });
  return `${signingInput}.${b64urlEncode(sig)}`;
}

// ---------------------------------------------------------------------------
// FSA consent DB authorization (docker exec mysql)
// ---------------------------------------------------------------------------

function authoriseConsentInDb(consentId, userId, accountIds) {
  const authId = crypto.randomUUID();
  const nowMs  = Date.now();

  // Build FS_CONSENT_MAPPING inserts — one row per account ID.
  // The demo backend reads these account IDs from consentMappingResources
  // and returns them in the GET /accounts response.
  const mappingInserts = accountIds.map(accountId => {
    const mappingId = crypto.randomUUID();
    return `INSERT INTO fs_consentdb.FS_CONSENT_MAPPING (MAPPING_ID, AUTH_ID, ACCOUNT_ID, PERMISSION, MAPPING_STATUS)
     VALUES ('${mappingId}', '${authId}', '${accountId}', 'ReadAccountsDetail', 'active');`;
  }).join('\n');

  const sql = [
    `UPDATE fs_consentdb.FS_CONSENT SET CURRENT_STATUS='Authorised', UPDATED_TIME=${nowMs} WHERE CONSENT_ID='${consentId}';`,
    `INSERT INTO fs_consentdb.FS_CONSENT_AUTH_RESOURCE (AUTH_ID, CONSENT_ID, AUTH_TYPE, USER_ID, AUTH_STATUS, UPDATED_TIME)
     VALUES ('${authId}', '${consentId}', 'authorisation', '${userId}', 'Authorised', ${nowMs});`,
    mappingInserts,
  ].join('\n');

  execSync(
    `docker exec mysql-db mysql -uroot -p'p@ssw0rd' -e ${JSON.stringify(sql)}`,
    { stdio: 'pipe' },
  );
  console.log(`[user-auth] Consent ${consentId} marked authorised in DB (auth_id=${authId}).`);
  console.log(`[user-auth] FS_CONSENT_MAPPING populated with ${accountIds.length} account(s): ${accountIds.join(', ')}`);
}

// ---------------------------------------------------------------------------
// URL-encoded form body builder
// ---------------------------------------------------------------------------

function formEncode(obj) {
  return Object.entries(obj)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

async function main() {
  const cfg = JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf8'));

  const isHost      = cfg.isHost    || 'https://obiam:9446';
  const apimHost    = cfg.apimHost  || 'https://obam:8243';
  const clientId    = cfg.mtlsClientId;
  const redirectUri = cfg.dcrRedirectUri;
  const certPem     = cfg.transportCertPem;
  const keyPem      = cfg.transportKeyPem;
  // IS stores the transport cert as the SP certificate and verifies JAR signatures
  // against it — sign the JAR with the transport key, not the registration key.
  const privKeyPem  = cfg.transportKeyPem;
  const kid         = cfg.clientKeyId;
  const testUser    = cfg.testUserId    || 'psu@wso2.com';
  const testPass    = cfg.testUserPass  || 'wso2123';
  // Account IDs to map in FS_CONSENT_MAPPING. The demo backend returns these
  // as the accounts in GET /accounts. Defaults to two test accounts.
  const accountIds  = cfg.testAccountIds || ['ACC00000001', 'ACC00000002'];

  // ── Step 1: APPLICATION token (client_credentials, scope=accounts openid) ─
  console.log('[user-auth] Step 1: Getting APPLICATION token...');
  const ccRes = await httpsReq(
    'POST',
    `${isHost}/oauth2/token`,
    formEncode({
      grant_type: 'client_credentials',
      client_id:  clientId,
      scope:      'accounts openid',
    }),
    { 'Content-Type': 'application/x-www-form-urlencoded' },
    certPem, keyPem,
  );
  const ccBody = JSON.parse(ccRes.body);
  if (!ccBody.access_token) {
    console.error('[user-auth] Failed to get APPLICATION token:', ccRes.body);
    process.exit(1);
  }
  const appToken = ccBody.access_token;
  console.log('[user-auth] APPLICATION token obtained. scope:', ccBody.scope);

  // ── Step 2: Create account access consent ─────────────────────────────────
  console.log('[user-auth] Step 2: Creating account access consent...');
  function isoDateOffset(days) {
    const d = new Date(Date.now() + days * 86400000);
    return d.toISOString().replace(/\.\d{3}Z$/, '+00:00');
  }
  const consentPayload = JSON.stringify({
    Data: {
      Permissions: [
        'ReadAccountsBasic',
        'ReadAccountsDetail',
        'ReadBalances',
        'ReadTransactionsDetail',
      ],
      ExpirationDateTime:      isoDateOffset(365),
      TransactionFromDateTime: isoDateOffset(-3650),
      TransactionToDateTime:   isoDateOffset(365),
    },
    Risk: {},
  });
  const consentRes = await httpsReq(
    'POST',
    `${apimHost}/open-banking/v3.1/aisp/account-access-consents`,
    consentPayload,
    {
      Authorization:         `Bearer ${appToken}`,
      'Content-Type':        'application/json',
      'x-fapi-interaction-id': `setup-${Date.now()}`,
    },
    certPem, keyPem,
  );
  let consentBody;
  try { consentBody = JSON.parse(consentRes.body); } catch (_) { consentBody = {}; }
  const consentId = consentBody && consentBody.Data && consentBody.Data.ConsentId;
  if (!consentId) {
    console.error('[user-auth] Failed to create consent. status:', consentRes.status, consentRes.body);
    process.exit(1);
  }
  console.log('[user-auth] Consent created. ConsentId:', consentId);

  // ── Step 2b: pre-authorise consent in FSA DB so APIM validation passes ────
  // DefaultConsentValidator.validateAccountSubmission() calls
  // resolveUsernameFromUserId(sub) which returns the plain username (no tenant
  // domain suffix) and compares it against FS_CONSENT_AUTH_RESOURCE.USER_ID.
  // Store the username as-is (e.g. "psu@wso2.com") — not the SCIM UUID and
  // not with "@carbon.super" appended.
  console.log('[user-auth] Consent USER_ID:', testUser);
  authoriseConsentInDb(consentId, testUser, accountIds);

  // ── Step 3: Build signed JAR (PS256 request object) ──────────────────────
  console.log('[user-auth] Step 3: Building JAR...');
  const now   = Math.floor(Date.now() / 1000);
  const nbf   = now - 60;
  const nonce = crypto.randomBytes(8).toString('hex');
  const state = crypto.randomBytes(8).toString('hex');

  // PKCE
  const codeVerifier  = crypto.randomBytes(32).toString('base64url');
  const codeChallenge = crypto.createHash('sha256').update(codeVerifier).digest('base64url');

  const jarPayload = {
    iss:           clientId,
    aud:           `${isHost}/oauth2/token`,
    iat:           now,
    nbf,
    exp:           nbf + 3600,
    jti:           crypto.randomBytes(8).toString('hex'),
    scope:         'accounts openid',
    response_type: 'code id_token',
    client_id:     clientId,
    redirect_uri:  redirectUri,
    nonce,
    state,
    code_challenge:        codeChallenge,
    code_challenge_method: 'S256',
    claims: {
      id_token: {
        openbanking_intent_id: { value: consentId, essential: true },
        acr:                   { essential: true, values: ['urn:openbanking:psd2:sca'] },
      },
    },
  };
  const jar = buildPS256JWT(jarPayload, privKeyPem, kid);
  console.log('[user-auth] JAR built (first 80 chars):', jar.slice(0, 80));

  // ── Step 4: GET authorize with JAR as request param (no PAR) ─────────────
  // Pass the signed JAR directly as ?request=<JAR> instead of using PAR.
  console.log('[user-auth] Step 4: GET /oauth2/authorize (request object inline)...');
  const authUrl = `${isHost}/oauth2/authorize?response_type=${encodeURIComponent('code id_token')}&client_id=${encodeURIComponent(clientId)}&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent('accounts openid')}&request=${encodeURIComponent(jar)}`;

  let cookieJar = {};

  // First GET — expect 302 to login page
  const authRes = await httpsReq('GET', authUrl, null, {}, certPem, keyPem);
  Object.assign(cookieJar, parseCookies(authRes.headers['set-cookie']));

  let loginUrl = locationOf(authRes);
  if (!loginUrl) {
    console.error('[user-auth] Expected redirect from /oauth2/authorize, got status:', authRes.status, authRes.body.slice(0, 500));
    process.exit(1);
  }

  // Make absolute if needed
  if (!loginUrl.startsWith('http')) loginUrl = `${isHost}${loginUrl}`;
  console.log('[user-auth] Redirected to login URL:', loginUrl.slice(0, 120));

  // Follow redirects until we reach the login page (contains 'sessionDataKey')
  let maxHops = 5;
  while (!loginUrl.includes('sessionDataKey') && maxHops-- > 0) {
    const r = await httpsReq('GET', loginUrl, null, { Cookie: cookieHeader(cookieJar) }, certPem, keyPem);
    Object.assign(cookieJar, parseCookies(r.headers['set-cookie']));
    const next = locationOf(r);
    if (!next) break;
    loginUrl = next.startsWith('http') ? next : `${isHost}${next}`;
    console.log('[user-auth] Following redirect to:', loginUrl.slice(0, 120));
  }

  // Extract sessionDataKey from URL
  const loginUrlObj = new URL(loginUrl.startsWith('http') ? loginUrl : `${isHost}${loginUrl}`);
  const sessionDataKey = loginUrlObj.searchParams.get('sessionDataKey');
  if (!sessionDataKey) {
    console.error('[user-auth] Could not extract sessionDataKey from:', loginUrl);
    process.exit(1);
  }
  console.log('[user-auth] sessionDataKey:', sessionDataKey);

  // ── Step 5: POST login credentials ────────────────────────────────────────
  console.log('[user-auth] Step 5: Submitting login credentials...');
  const loginPostUrl = `${isHost}/commonauth`;
  const loginBody = formEncode({
    username:       testUser,
    password:       testPass,
    sessionDataKey: sessionDataKey,
    tocommonauth:   'true',
  });
  const loginRes = await httpsReq(
    'POST',
    loginPostUrl,
    loginBody,
    {
      'Content-Type': 'application/x-www-form-urlencoded',
      Cookie: cookieHeader(cookieJar),
      Referer: loginUrl,
    },
    certPem, keyPem,
  );
  Object.assign(cookieJar, parseCookies(loginRes.headers['set-cookie']));
  let nextUrl = locationOf(loginRes);
  if (!nextUrl) {
    console.error('[user-auth] Login POST did not redirect. status:', loginRes.status, loginRes.body.slice(0, 500));
    process.exit(1);
  }
  if (!nextUrl.startsWith('http')) nextUrl = `${isHost}${nextUrl}`;
  console.log('[user-auth] Login redirect to:', nextUrl.slice(0, 120));

  // ── Step 6: Follow redirects until we reach the callback URL ─────────────
  console.log('[user-auth] Step 6: Following post-login redirects...');
  let authCode = null;
  maxHops = 10;

  while (maxHops-- > 0) {
    // If we've reached the callback URI, extract the code
    if (nextUrl.startsWith(redirectUri) || nextUrl.includes('code=')) {
      const cbUrl = new URL(nextUrl.startsWith('http') ? nextUrl : `${redirectUri}${nextUrl}`);
      authCode = cbUrl.searchParams.get('code');
      if (authCode) break;
      // Some flows return code in hash fragment; handle that
      const hash = cbUrl.hash;
      if (hash) {
        const hp = new URLSearchParams(hash.slice(1));
        authCode = hp.get('code');
        if (authCode) break;
      }
    }

    // Don't follow redirects to external URLs (google.com etc.)
    if (!nextUrl.startsWith(isHost) && !nextUrl.startsWith('https://obiam') && !nextUrl.startsWith('https://obam')) {
      // We're at the callback — extract code
      const cbUrl = new URL(nextUrl);
      authCode = cbUrl.searchParams.get('code');
      const hash = cbUrl.hash;
      if (!authCode && hash) {
        const hp = new URLSearchParams(hash.slice(1));
        authCode = hp.get('code');
      }
      break;
    }

    const r = await httpsReq('GET', nextUrl, null, { Cookie: cookieHeader(cookieJar) }, certPem, keyPem);
    Object.assign(cookieJar, parseCookies(r.headers['set-cookie']));
    const loc = locationOf(r);
    if (!loc) {
      // No redirect — check if body has code (form_post response_mode)
      if (r.body.includes('code')) {
        const m = r.body.match(/name="code"\s+value="([^"]+)"/);
        if (m) { authCode = m[1]; break; }
      }
      console.error('[user-auth] No redirect at', nextUrl, 'status:', r.status, r.body.slice(0, 300));
      process.exit(1);
    }
    nextUrl = loc.startsWith('http') ? loc : `${isHost}${loc}`;
    console.log('[user-auth] Redirect to:', nextUrl.slice(0, 120));
  }

  if (!authCode) {
    console.error('[user-auth] Could not extract auth code. Last URL:', nextUrl);
    process.exit(1);
  }
  console.log('[user-auth] Auth code obtained:', authCode.slice(0, 20) + '...');

  // ── Step 7: Exchange code for APPLICATION_USER token ──────────────────────
  console.log('[user-auth] Step 7: Exchanging code for token...');
  const tokenRes = await httpsReq(
    'POST',
    `${isHost}/oauth2/token`,
    formEncode({
      grant_type:    'authorization_code',
      code:          authCode,
      redirect_uri:  redirectUri,
      client_id:     clientId,
      code_verifier: codeVerifier,
    }),
    { 'Content-Type': 'application/x-www-form-urlencoded' },
    certPem, keyPem,
  );

  let tokenBody;
  try { tokenBody = JSON.parse(tokenRes.body); } catch (_) { tokenBody = {}; }
  if (!tokenBody.access_token) {
    console.error('[user-auth] Token exchange failed. status:', tokenRes.status, tokenRes.body);
    process.exit(1);
  }

  const accessToken = tokenBody.access_token;
  const expiresIn   = tokenBody.expires_in || 3600;

  // Decode and log the token claims (without verification)
  try {
    const parts = accessToken.split('.');
    const claims = JSON.parse(Buffer.from(parts[1], 'base64').toString());
    console.log('[user-auth] Token claims:', JSON.stringify(claims, null, 2));
  } catch (_) {}

  console.log('[user-auth] APPLICATION_USER token obtained. expires_in:', expiresIn);

  // ── Step 8: Write to test-config.json ─────────────────────────────────────
  cfg.applicationUserToken     = accessToken;
  cfg.applicationUserTokenExp  = Math.floor(Date.now() / 1000) + expiresIn;
  cfg.applicationUserConsentId = consentId;
  fs.writeFileSync(CONFIG_PATH, JSON.stringify(cfg, null, 2) + '\n');
  console.log('[user-auth] test-config.json updated with applicationUserToken.');
  console.log('[user-auth] Done.');
}

main().catch(err => {
  console.error('[user-auth] Fatal error:', err.message);
  console.error(err.stack);
  process.exit(1);
});
