import http from 'k6/http';
import { check, sleep } from 'k6';
import { config } from '../config.js';
import { safeJson } from '../lib/safe-json.js';

function accountConsentPayload() {
  return JSON.stringify({
    Data: {
      Permissions: ['ReadAccountsBasic', 'ReadAccountsDetail', 'ReadBalances', 'ReadTransactionsBasic'],
      ExpirationDateTime: new Date(Date.now() + 2 * 24 * 3600 * 1000).toISOString(),
      TransactionFromDateTime: new Date(Date.now() - 3 * 24 * 3600 * 1000).toISOString(),
      TransactionToDateTime: new Date(Date.now()).toISOString(),
    },
    Risk: {},
  });
}

function headers() {
  return {
    'Content-Type': 'application/json',
    Authorization: config.consentAdminAuthHeader,
    'x-wso2-client-id': config.clientId,
    'x-fapi-financial-id': config.fapiFinancialId,
    'x-fapi-interaction-id': `${Date.now()}-${Math.random().toString(16).slice(2)}`,
  };
}

export function createAccountConsent() {
  const res = http.post(
    `${config.isHost}/api/fs/consent/manage/account-access-consents`,
    accountConsentPayload(),
    { headers: headers(), tags: { name: 'IS_CreateAccountConsent' } }
  );
  check(res, { 'create consent: status 201': (r) => r.status === 201 });
  return safeJson(res, 'Data.ConsentId');
}

// Creates a consent as prerequisite setup — tagged IS_Setup so it is excluded
// from per-endpoint metrics. Used by the GET and DELETE isolated scenarios.
export function createSetupConsent() {
  const res = http.post(
    `${config.isHost}/api/fs/consent/manage/account-access-consents`,
    accountConsentPayload(),
    { headers: headers(), tags: { name: 'IS_Setup' } }
  );
  return safeJson(res, 'Data.ConsentId');
}

export function getAccountConsent(consentId) {
  const res = http.get(
    `${config.isHost}/api/fs/consent/manage/account-access-consents/${consentId}`,
    { headers: headers(), tags: { name: 'IS_GetAccountConsent' } }
  );
  check(res, { 'get consent: status 200': (r) => r.status === 200 });
}

export function revokeAccountConsent(consentId) {
  const res = http.del(
    `${config.isHost}/api/fs/consent/manage/account-access-consents/${consentId}`,
    null,
    { headers: headers(), tags: { name: 'IS_RevokeAccountConsent' } }
  );
  check(res, { 'revoke consent: status 204': (r) => r.status === 204 });
}

export function searchConsents() {
  const url = `${config.isHost}/api/fs/consent/admin/search?limit=25&clientIds=${encodeURIComponent(config.clientId)}`;
  const res = http.get(url, { headers: headers(), tags: { name: 'IS_SearchConsents' } });
  check(res, { 'search consents: status 200': (r) => r.status === 200 });
}

export function identityServerFlow() {
  searchConsents();

  const consentId = createAccountConsent();
  sleep(0.3);
  if (consentId) {
    getAccountConsent(consentId);
    sleep(0.3);
    revokeAccountConsent(consentId);
  }
}
