// k6/scenarios/consent-search.js
//
// Portal-realistic search scenarios for GET /api/fs/consent/admin/search.
//
// Scenario design mirrors the Consent Manager Portal (self-care-portal) call patterns:
//
//   getConsentsFromAPI()         → initial tab load: consentTypes only, no status filter
//   getConsentsFromAPIForSearch()→ search / tab switch: type + status (+ optional extras)
//
// Multi-value status strings are sent as a single comma-separated query value,
// matching the portal's string-concatenation approach (consent-api.js line 138):
//   consentStatuses=Expired,Revoked          (accounts / COF inactive tab)
//   consentStatuses=Consumed,Expired,Revoked (payments inactive tab)
//
// Exact values from specConfigurations.js:
//   accounts   → Active: "Authorised"            Inactive: "Expired,Revoked"
//   payments   → Active: "Authorised"            Inactive: "Consumed,Expired,Revoked"
//   cof        → Active: "Authorised"            Inactive: "Expired,Revoked"
//
// Parameters accepted by the endpoint (all optional, combined with AND):
//   consentIds      → FS_CONSENT.CONSENT_ID         (equality lookup)
//   clientIds       → FS_CONSENT.CLIENT_ID
//   consentTypes    → FS_CONSENT.CONSENT_TYPE
//   consentStatuses → FS_CONSENT.CURRENT_STATUS     (comma-separated for OR across values)
//   userIds         → FS_CONSENT_AUTH_RESOURCE.USER_ID
//                     *** triggers INNER JOIN instead of LEFT JOIN ***
//   fromTime        → FS_CONSENT.UPDATED_TIME >= fromTime (epoch ms)
//   toTime          → FS_CONSENT.UPDATED_TIME <= toTime   (epoch ms)
//   limit / offset  → LIMIT / OFFSET on the outer query

import http from 'k6/http';
import { check, sleep } from 'k6';
import { config } from '../config.js';

// ---------------------------------------------------------------------------
// Timestamps — computed once at script init; all VUs share the same windows.
// ---------------------------------------------------------------------------

const _now = Date.now();
const DAY  = 86_400_000;

export const TIME = {
  now:     _now,
  last24h: _now -      DAY,
  last7d:  _now -  7 * DAY,
  last30d: _now - 30 * DAY,
};

// ---------------------------------------------------------------------------
// Status constants (from specConfigurations.js)
// ---------------------------------------------------------------------------

const AUTHORISED    = 'Authorised';
const ACCT_INACTIVE = 'Expired,Revoked';          // accounts + COF inactive tab
const PMT_INACTIVE  = 'Consumed,Expired,Revoked'; // payments inactive tab

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function headers() {
  return {
    Authorization:           config.consentAdminAuthHeader,
    'x-wso2-client-id':      config.clientId,
    'x-fapi-financial-id':   config.fapiFinancialId,
    'x-fapi-interaction-id': `${Date.now()}-${Math.random().toString(16).slice(2)}`,
  };
}

// Build a query string, dropping keys whose value is null/undefined/''.
// Multi-value status strings (e.g. "Expired,Revoked") are passed as a single
// param value — encodeURIComponent encodes the comma as %2C, which the server
// URL-decodes back to a comma before splitting. Matches portal behaviour.
function qs(params) {
  return Object.entries(params)
    .filter(([, v]) => v !== null && v !== undefined && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');
}

function search(params, tag, required) {
  if (required) {
    for (const key of required) {
      const v = params[key];
      if (v === null || v === undefined || v === '' || v === 0) {
        check(null, { [`${tag}: required param '${key}' configured`]: () => false });
        return;
      }
    }
  }
  const res = http.get(
    `${config.isHost}/api/fs/consent/admin/search?${qs(params)}`,
    { headers: headers(), tags: { name: tag } },
  );
  check(res, { [`${tag}: status 200`]: (r) => r.status === 200 });
  return res;
}

// ---------------------------------------------------------------------------
// Scenario 1 — Portal initial load: accounts tab
//
// Mirrors getConsentsFromAPI() for admin role.
// URL pattern: ?consentTypes=accounts
// No status filter — the backend returns all statuses; the portal renders
// tabs client-side or triggers a follow-up filtered query on tab click.
// ---------------------------------------------------------------------------
export function portalAccountsLoad() {
  search({
    consentTypes: 'accounts',
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_PortalAccountsLoad');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 2 — Portal initial load: payments tab
//
// Same getConsentsFromAPI() pattern but for the payments consent type.
// ---------------------------------------------------------------------------
export function portalPaymentsLoad() {
  search({
    consentTypes: 'payments',
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_PortalPaymentsLoad');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 3 — Accounts active tab  (type + Authorised)
//
// getConsentsFromAPIForSearch() called when user selects the "Active" tab
// on the accounts consent type. The most-visited portal view.
// ---------------------------------------------------------------------------
export function accountsActiveTab() {
  search({
    consentTypes:    'accounts',
    consentStatuses: AUTHORISED,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_AccountsActiveTab');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 4 — Accounts inactive tab  (type + Expired,Revoked)
//
// "Inactive" tab on accounts: consentStatuses=Expired,Revoked as a single
// comma-separated value (not two separate params).
// ---------------------------------------------------------------------------
export function accountsInactiveTab() {
  search({
    consentTypes:    'accounts',
    consentStatuses: ACCT_INACTIVE,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_AccountsInactiveTab');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 5 — Payments active tab  (type + Authorised)
// ---------------------------------------------------------------------------
export function paymentsActiveTab() {
  search({
    consentTypes:    'payments',
    consentStatuses: AUTHORISED,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_PaymentsActiveTab');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 6 — Payments inactive tab  (type + Consumed,Expired,Revoked)
//
// Payments has three inactive statuses because a consumed (used) payment
// consent is different from expired/revoked.
// ---------------------------------------------------------------------------
export function paymentsInactiveTab() {
  search({
    consentTypes:    'payments',
    consentStatuses: PMT_INACTIVE,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_PaymentsInactiveTab');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 7 — COF (funds confirmation) active tab  (type + Authorised)
// ---------------------------------------------------------------------------
export function cofActiveTab() {
  search({
    consentTypes:    'fundsconfirmations',
    consentStatuses: AUTHORISED,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_CofActiveTab');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 8 — Specific consent ID lookup
//
// Admin drills into a single consent record by ID. Tests the equality
// lookup path on FS_CONSENT.CONSENT_ID. Uses a real ID fetched in setup();
// falls back to config.searchConsentId if setup returns empty.
// ---------------------------------------------------------------------------
export function searchByConsentId(consentId) {
  search({
    consentTypes:    'accounts',
    consentStatuses: AUTHORISED,
    consentIds:      consentId || config.searchConsentId || '',
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_ByConsentId');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 9 — Client ID filter  (TPP investigation)
//
// Admin checks all active accounts consents for a specific TPP application.
// Tests index efficiency on FS_CONSENT.CLIENT_ID combined with type + status.
// ---------------------------------------------------------------------------
export function searchByClientId() {
  search({
    consentTypes:    'accounts',
    consentStatuses: AUTHORISED,
    clientIds:       config.clientId,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_ByClientId');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 10 — User ID filter  *** INNER JOIN path ***
//
// Adding userIds changes the JOIN from LEFT to INNER JOIN on
// FS_CONSENT_AUTH_RESOURCE. Key bottleneck candidate — if USER_ID is not
// indexed on that table, the DB must join and filter in memory.
// ---------------------------------------------------------------------------
export function searchByUserId() {
  search({
    consentTypes:    'accounts',
    consentStatuses: AUTHORISED,
    userIds:         config.searchUserId,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_ByUserId', ['userIds']);
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 11 — Date range: narrow (last 24 hours)
//
// Tight predicate on UPDATED_TIME. Small result set; tests index efficiency
// on FS_CONSENT.UPDATED_TIME for range scans.
// ---------------------------------------------------------------------------
export function searchDateNarrow() {
  search({
    consentTypes:    'accounts',
    consentStatuses: AUTHORISED,
    fromTime: TIME.last24h,
    toTime:   TIME.now,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_DateNarrow');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 12 — Date range: wide (last 30 days)
//
// Broader range; result set grows. Shows how query time scales with the
// number of rows matching the date filter. Compare to narrow to detect
// linear degradation.
// ---------------------------------------------------------------------------
export function searchDateWide() {
  search({
    consentTypes:    'accounts',
    consentStatuses: AUTHORISED,
    fromTime: TIME.last30d,
    toTime:   TIME.now,
    limit:  config.searchLimit,
    offset: 0,
  }, 'IS_DateWide');
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 13 — Deep pagination
//
// High OFFSET on a type + status filtered result set. Forces the DB to
// skip a large number of rows even with LIMIT small. A known relational-DB
// performance problem without keyset pagination.
// ---------------------------------------------------------------------------
export function searchDeepPagination() {
  search({
    consentTypes:    'accounts',
    consentStatuses: AUTHORISED,
    limit:           config.searchLimit,
    offset:          config.searchDeepOffset,
  }, 'IS_DeepPagination', ['offset']);
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Scenario 14 — Large page size
//
// High LIMIT on a wide date window. Tests serialization and DB fetch cost
// independent of OFFSET depth.
// ---------------------------------------------------------------------------
export function searchLargePageSize() {
  search({
    consentTypes:    'accounts',
    consentStatuses: AUTHORISED,
    fromTime: TIME.last30d,
    toTime:   TIME.now,
    limit:    config.searchLargeLimit,
    offset:   0,
  }, 'IS_LargePageSize', ['limit']);
  sleep(0.5);
}
