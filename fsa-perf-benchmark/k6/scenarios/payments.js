import http from 'k6/http';
import { check, sleep } from 'k6';
import { config } from '../config.js';
import { getMtlsToken, authHeader } from '../lib/auth.js';
import { safeJson } from '../lib/safe-json.js';

function paymentConsentPayload() {
  return JSON.stringify({
    Data: {
      Initiation: {
        InstructionIdentification: `instr-${Date.now()}`,
        EndToEndIdentification: `e2e-${Date.now()}`,
        InstructedAmount: { Amount: '10.00', Currency: 'GBP' },
        CreditorAccount: { SchemeName: 'OB.SortCodeAccountNumber', Identification: '11223321325698', Name: 'Test Account' },
      },
    },
    Risk: {},
  });
}

// ---------------------------------------------------------------------------
// Individual endpoint functions — used by combined-sequential.js
// ---------------------------------------------------------------------------

export function createPaymentConsent() {
  const token = getMtlsToken('payments openid');
  if (!token) return;
  const hdrs = {
    ...authHeader(token),
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'x-fapi-interaction-id': `${Date.now()}`,
    'x-idempotency-key': `pay-${Date.now()}`,
  };
  const res = http.post(
    `${config.apimHost}${config.pispContext}/payment-consents`,
    paymentConsentPayload(),
    { headers: hdrs, tags: { name: 'APIM_CreatePaymentConsent' } }
  );
  check(res, { 'payment consent: status 201': (r) => r.status === 201 });
}

export function getPaymentConsent() {
  const token = getMtlsToken('payments openid');
  if (!token) return;
  const hdrs = {
    ...authHeader(token),
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'x-fapi-interaction-id': `${Date.now()}`,
  };
  // Create a consent silently — excluded from metrics
  const setupHdrs = { ...hdrs, 'x-idempotency-key': `setup-${Date.now()}` };
  const setupRes = http.post(
    `${config.apimHost}${config.pispContext}/payment-consents`,
    paymentConsentPayload(),
    { headers: setupHdrs, tags: { name: 'APIM_Setup' } }
  );
  const consentId = safeJson(setupRes, 'Data.ConsentId');
  if (!consentId) return;
  sleep(0.2);
  const res = http.get(
    `${config.apimHost}${config.pispContext}/payment-consents/${consentId}`,
    { headers: hdrs, tags: { name: 'APIM_GetPaymentConsent' } }
  );
  check(res, { 'get payment consent: status 200': (r) => r.status === 200 });
}

// ---------------------------------------------------------------------------
// Combined flow — kept for reference, not used by sequential tests
// ---------------------------------------------------------------------------

export function paymentsFlow() {
  const token = getMtlsToken('payments openid');
  if (!token) return;
  const headers = {
    ...authHeader(token),
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'x-fapi-interaction-id': `${Date.now()}`,
  };

  const consentRes = http.post(
    `${config.apimHost}${config.pispContext}/payment-consents`,
    paymentConsentPayload(),
    { headers, tags: { name: 'APIM_CreatePaymentConsent' } }
  );
  check(consentRes, { 'payment consent: status 201': (r) => r.status === 201 });

  const consentId = safeJson(consentRes, 'Data.ConsentId');
  sleep(0.2);
  if (!consentId) return;

  const getRes = http.get(`${config.apimHost}${config.pispContext}/payment-consents/${consentId}`, {
    headers,
    tags: { name: 'APIM_GetPaymentConsent' },
  });
  check(getRes, { 'get payment consent: status 200': (r) => r.status === 200 });
}
