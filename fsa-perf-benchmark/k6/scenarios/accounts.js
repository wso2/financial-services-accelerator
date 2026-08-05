import http from 'k6/http';
import { check } from 'k6';
import { config } from '../config.js';
import { authHeader } from '../lib/auth.js';
import { safeJson } from '../lib/safe-json.js';

// ---------------------------------------------------------------------------
// Individual endpoint functions — used by combined-sequential.js
// Each function uses the pre-obtained APPLICATION_USER token from config.
// This token has aut=APPLICATION_USER + consent_id claim, required by the
// AISP API's ClaimBasedResourceAccessValidationMediator and FSA scope logic.
// Run scripts/setup-user-auth.js before the test to refresh the token.
// ---------------------------------------------------------------------------

export function getAccounts() {
  const token = config.applicationUserToken;
  if (!token) return;
  const hdrs = { ...authHeader(token), 'Accept': 'application/json', 'x-fapi-interaction-id': `${Date.now()}` };
  const res = http.get(`${config.apimHost}${config.aispContext}/accounts`, {
    headers: hdrs,
    tags: { name: 'APIM_GetAccounts' },
  });
  check(res, { 'accounts: status 200': (r) => r.status === 200 });
}

export function getBalances() {
  const token = config.applicationUserToken;
  if (!token) return;
  const hdrs = { ...authHeader(token), 'Accept': 'application/json', 'x-fapi-interaction-id': `${Date.now()}` };
  // Fetch accountId silently — excluded from metrics
  const setupRes = http.get(`${config.apimHost}${config.aispContext}/accounts`, {
    headers: hdrs,
    tags: { name: 'APIM_Setup' },
  });
  const accountId = safeJson(setupRes, 'Data.Account.0.AccountId');
  if (!accountId) return;
  const res = http.get(`${config.apimHost}${config.aispContext}/accounts/${accountId}/balances`, {
    headers: hdrs,
    tags: { name: 'APIM_GetBalances' },
  });
  check(res, { 'balances: status 200': (r) => r.status === 200 });
}

export function getTransactions() {
  const token = config.applicationUserToken;
  if (!token) return;
  const hdrs = { ...authHeader(token), 'Accept': 'application/json', 'x-fapi-interaction-id': `${Date.now()}` };
  // Fetch accountId silently — excluded from metrics
  const setupRes = http.get(`${config.apimHost}${config.aispContext}/accounts`, {
    headers: hdrs,
    tags: { name: 'APIM_Setup' },
  });
  const accountId = safeJson(setupRes, 'Data.Account.0.AccountId');
  if (!accountId) return;
  const res = http.get(`${config.apimHost}${config.aispContext}/accounts/${accountId}/transactions`, {
    headers: hdrs,
    tags: { name: 'APIM_GetTransactions' },
  });
  check(res, { 'transactions: status 200': (r) => r.status === 200 });
}

// ---------------------------------------------------------------------------
// Combined flow — kept for reference, not used by sequential tests
// ---------------------------------------------------------------------------

export function accountsFlow() {
  const token = config.applicationUserToken;
  if (!token) return;
  const headers = { ...authHeader(token), 'Accept': 'application/json', 'x-fapi-interaction-id': `${Date.now()}` };

  const accountsRes = http.get(`${config.apimHost}${config.aispContext}/accounts`, {
    headers,
    tags: { name: 'APIM_GetAccounts' },
  });
  check(accountsRes, { 'accounts: status 200': (r) => r.status === 200 });

  const accountId = safeJson(accountsRes, 'Data.Account.0.AccountId');
  if (!accountId) return;

  const balRes = http.get(`${config.apimHost}${config.aispContext}/accounts/${accountId}/balances`, {
    headers,
    tags: { name: 'APIM_GetBalances' },
  });
  check(balRes, { 'balances: status 200': (r) => r.status === 200 });

  const txRes = http.get(`${config.apimHost}${config.aispContext}/accounts/${accountId}/transactions`, {
    headers,
    tags: { name: 'APIM_GetTransactions' },
  });
  check(txRes, { 'transactions: status 200': (r) => r.status === 200 });
}
