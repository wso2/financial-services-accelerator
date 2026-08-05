# Performance Benchmark — WSO2 Financial Services Accelerator

A k6 load-test suite for the [financial-services-accelerator](https://github.com/wso2/financial-services-accelerator) project, covering Identity Server (IS) and API Manager (APIM) endpoints.

Each endpoint runs in **isolation** — one at a time with its own container restart and client setup — so results represent each endpoint's individual performance rather than a blended number.

---

## Table of Contents

1. [Test overview](#1-test-overview)
2. [Directory structure](#2-directory-structure)
3. [Prerequisites](#3-prerequisites)
4. [Configuration](#4-configuration)
5. [Running a test](#5-running-a-test)
6. [Load profile](#6-load-profile)
7. [Test data setup (is-search only)](#7-test-data-setup-is-search-only)
8. [HTML reports](#8-html-reports)
9. [Endpoint reference](#9-endpoint-reference)
10. [Caveats](#10-caveats)

---

## 1. Test overview

| Command | What it measures | Approx. duration (defaults) |
|---|---|---|
| `./scripts/run-test.sh is-crud` | IS consent create and get | ~30 m (2 scenarios) |
| `./scripts/run-test.sh apim-crud` | APIM accounts, balances, transactions, payment consents | ~75 m (5 scenarios) |
| `./scripts/run-test.sh dcr` | APIM DCR registration latency (POST /register) | steady duration only |
| `./scripts/run-test.sh is-search` | IS consent search — 14 filter scenarios | ~3 h 40 m |

`run-test.sh` handles the full per-scenario lifecycle automatically:
1. Restart the relevant Docker containers
2. Wait for health checks to pass
3. Register a DCR client (and obtain an APPLICATION_USER token where required)
4. Run a warm-up pass (results discarded)
5. Run the measured pass → save a per-scenario summary JSON
6. Merge summaries and generate the HTML report

---

## 2. Directory structure

```
fsa-perf-benchmark/
├── README.md
├── scripts/
│   ├── run-test.sh                ← main entry point — see §5
│   ├── setup-dcr-client.js        ← registers a DCR client via APIM gateway; writes clientId + mtlsClientId
│   ├── setup-is-client.js         ← registers a DCR client directly with IS (is-crud); writes clientId
│   ├── setup-user-auth.js         ← auth code flow to obtain APPLICATION_USER token; writes token to config
│   ├── merge-summaries.js         ← merges per-scenario summary JSONs into one combined file
│   ├── html-report.js             ← generates a self-contained HTML report from a summary JSON
│   ├── compare-variants.js        ← side-by-side baseline vs extended comparison
│   ├── run-comparison.sh          ← orchestrates baseline/extended variant runs
│   └── generate_consent_data.sql  ← seeds 1 M consent records (is-search prerequisite)
├── k6/
│   ├── test-config.json           ← all user inputs; auto-updated by setup scripts at runtime
│   ├── config.js                  ← reads test-config.json, exports the config object
│   ├── lib/
│   │   ├── auth.js                ← OAuth2 token helpers (private_key_jwt, mTLS)
│   │   └── safe-json.js           ← safe response JSON parser
│   ├── scenarios/
│   │   ├── identity-server.js     ← IS consent CRUD functions
│   │   ├── consent-search.js      ← IS consent search functions (14 filter variants)
│   │   ├── dcr.js                 ← DCR register / delete functions
│   │   ├── accounts.js            ← APIM accounts / balances / transactions functions
│   │   └── payments.js            ← APIM payment consent functions
│   └── tests/
│       ├── is-crud.js             ← IS CRUD test entry point
│       ├── apim-crud.js           ← APIM CRUD test entry point
│       ├── dcr-latency.js         ← DCR latency test entry point
│       └── is-search.js           ← IS search test entry point (14 scenarios)
└── results/                       ← generated summary JSONs and HTML reports (gitignored)
```

---

## 3. Prerequisites

### Step 1 — Deploy IS and APIM with Docker

Follow the official WSO2 guide to set up the Financial Services Accelerator environment using Docker:

**[https://ob.docs.wso2.com/en/latest/install-and-setup/deploy-with-docker/](https://ob.docs.wso2.com/en/latest/install-and-setup/deploy-with-docker/)**

Once the deployment is complete, the following containers must be running before executing any benchmark:

| Container | Role | Version | Port |
|---|---|---|---|
| `obiam` | Identity Server | 7.1.0 | 9446 |
| `obam` | API Manager | 4.5.0 | 8243 |
| `mysql-db` | MySQL | 8.0.41 | 3307 |

> These are the versions used for this benchmark. Other versions may work but have not been tested.

### Step 2 — Start a MySQL 8 container

The benchmark uses a dedicated MySQL 8 container named `mysql-db`. It must be on the **same Docker network** as `obiam` and `obam` so the containers can reach each other by hostname.

First, find the network name used by the base product containers:

```bash
docker inspect obiam --format '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}'
```

Then start MySQL on that network:

```bash
docker run -d \
  --name mysql-db \
  --network <network-from-above> \
  -e MYSQL_ROOT_PASSWORD=<db-password> \
  -p 3307:3306 \
  mysql:8.0.41
```

> Set `<db-password>` to match the `dbPass` value in `k6/test-config.json`.

### Step 3 — Install test tooling

- [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) v2.0+ — `brew install k6`
- Node.js 18+ — for setup scripts and report generation

---

## 4. Configuration

All inputs live in **`k6/test-config.json`**. The file is committed to the repository with placeholder values (e.g. `<PKCS8-PEM-signing-private-key>`). Replace every placeholder with a real value before running any test. **Never commit the file once it contains real credentials.**

Fields with an empty string default (`""`) are auto-populated by the setup scripts — leave them empty.

### Always required

| Field | Description |
|---|---|
| `isHost` | IS base URL, e.g. `https://obiam:9446` |
| `apimHost` | APIM gateway URL, e.g. `https://obam:8243` |
| `consentAdminUser` | IS admin username |
| `consentAdminPassword` | IS admin password |
| `fapiFinancialId` | Value for the `x-fapi-financial-id` header |

### Required for is-crud and apim-crud (DCR + mTLS)

| Field | Description |
|---|---|
| `transportCertPem` | OB transport certificate PEM — presented for mTLS on every request |
| `transportKeyPem` | Private key for the transport certificate PEM |
| `dcrSoftwareStatement` | SSA JWT from the certificate directory |
| `dcrSoftwareId` | `software_id` matching the SSA |
| `dcrRedirectUri` | Redirect URI for the registration request |
| `clientPrivateKey` | PKCS8 PEM signing key (PS256) |
| `clientKeyId` | `kid` for the signing key JWT header |

### Required for apim-crud only

| Field | Description |
|---|---|
| `testUserId` | PSU username, e.g. `psu@wso2.com` |
| `testUserPass` | PSU password |
| `aispContext` | AISP API context, e.g. `/open-banking/v3.1/aisp` |
| `pispContext` | PISP API context, e.g. `/open-banking/v3.1/pisp` |
| `testAccountIds` | *(optional)* account IDs for consent mapping; defaults to `["ACC00000001","ACC00000002"]` |

### Auto-populated by setup scripts

| Field | Written by |
|---|---|
| `clientId` | `setup-is-client.js` or `setup-dcr-client.js` |
| `mtlsClientId` | `setup-dcr-client.js` |
| `applicationUserToken` | `setup-user-auth.js` |
| `applicationUserConsentId` | `setup-user-auth.js` |

### Required for is-search

| Field | Description |
|---|---|
| `clientId` | OAuth2 client ID of an existing IS application |
| `searchUserId` | PSU user ID used when seeding consent records |
| `searchLimit` | Default page size for search requests |
| `searchDeepOffset` | Offset for the deep-pagination scenario |
| `searchLargeLimit` | Page size for the large-page-size scenario |

### Load profile tunables

| Field | Default | Description |
|---|---|---|
| `peakVUs` | `50` | Max virtual users during the steady state |
| `warmupDuration` | `1m` | Time to ramp from 0 → `peakVUs` |
| `steadyDuration` | `1m` | Time to hold `peakVUs` — the measured window |
| `rampDownDuration` | `1m` | Time to ramp from `peakVUs` → 0 |

For a production benchmark, use `peakVUs=50`, `warmupDuration=2m`, `steadyDuration=12m`, `rampDownDuration=1m`.

---

## 5. Running a test

> All commands assume you are in the `fsa-perf-benchmark/` directory and the required Docker containers are running.

### IS CRUD Operations

Measures IS consent create and get. Requires only the `obiam` container.

```bash
./scripts/run-test.sh is-crud
```

`run-test.sh` automatically registers a DCR client with IS (`setup-is-client.js`) before each scenario.

Output: `results/is-crud-report.html`

---

### APIM CRUD Operations

Measures APIM accounts, balances, transactions, and payment consent endpoints. Requires `obiam` and `obam`.

```bash
./scripts/run-test.sh apim-crud
```

`run-test.sh` automatically performs the following before each scenario:
1. Registers a DCR client via the APIM gateway (`setup-dcr-client.js`)
2. Patches the IS service provider to use `tls_client_auth`
3. Runs the auth code flow to obtain an APPLICATION_USER token (`setup-user-auth.js`)
4. Inserts consent and account mapping records into the database

Output: `results/apim-crud-report.html`

---

### DCR Latency

Measures POST /register response time on the APIM gateway using a single VU. Each iteration registers a new client (measured) then immediately deletes it (cleanup). Requires `obiam` and `obam`.

```bash
./scripts/run-test.sh dcr
```

No client setup needed — the test manages its own client per iteration.

Output: `results/dcr-latency-report.html`

---

### IS Search Operations

Measures the IS consent search API across 14 filter scenarios. Requires `obiam` and a seeded database.

**Prerequisite: seed 1 million consent records first — see [§7](#7-test-data-setup-is-search-only).**

```bash
./scripts/run-test.sh is-search
```

Output: `results/is-search-report.html`

---

### Running a subset of scenarios

Pass a comma-separated list of scenario names as the second argument:

```bash
./scripts/run-test.sh apim-crud get_accounts,get_balances,get_transactions
./scripts/run-test.sh is-crud create_consent
```

---

## 6. Load profile

Each scenario uses the same three-stage ramp profile:

```
Ramp-up ──► Steady state ──► Ramp-down
```

| Phase | Duration field | Description |
|---|---|---|
| Ramp-up | `warmupDuration` | VUs increase from 0 to `peakVUs` |
| Steady state | `steadyDuration` | VUs held at `peakVUs` — the measured window |
| Ramp-down | `rampDownDuration` | VUs decrease to 0 |

Each scenario also gets a **warm-up pass** (same profile, results discarded) before the measured run so JIT compilation and caches are primed.

---

## 7. Test data setup (is-search only)

> Skip this section for `is-crud`, `apim-crud`, and `dcr`.

The search test requires a large dataset. `scripts/generate_consent_data.sql` seeds **1,000,000 consent records** across all consent types and statuses.

**Data distribution**

| Dimension | Distribution |
|---|---|
| `CONSENT_TYPE` | accounts 50% · payments 35% · fundsconfirmations 15% |
| `CURRENT_STATUS` | Authorised 38% · Expired 30% · Revoked 22% · Consumed 10% |
| `UPDATED_TIME` | 2% last 24 h · 8% last 2–7 d · 15% last 8–30 d · 75% older |

**Step 1 — Export DB credentials**

```bash
DB_HOST=$(jq -r '.dbHost' k6/test-config.json)
DB_USER=$(jq -r '.dbUser' k6/test-config.json)
DB_PASS=$(jq -r '.dbPass' k6/test-config.json)
DB_NAME=$(jq -r '.dbName' k6/test-config.json)
```

> Requires [`jq`](https://stedolan.github.io/jq/) — `brew install jq`.

**Step 2 — Drop and recreate the consent database**

```bash
docker exec -it mysql-db mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" -e "
DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME;
ALTER DATABASE $DB_NAME CHARACTER SET latin1 COLLATE latin1_swedish_ci;
"
```

**Step 3 — Apply the consent schema**

```bash
docker cp /path/to/financial-services-accelerator/accelerators/fs-is/carbon-home/dbscripts/financial-services/consent/mysql.sql \
  mysql-db:/tmp/fs_consentdb.sql

docker exec -it mysql-db mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" "$DB_NAME" \
  -e "SOURCE /tmp/fs_consentdb.sql"
```

**Step 4 — Seed 1,000,000 records**

```bash
docker exec -i mysql-db mysql -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" "$DB_NAME" \
  < scripts/generate_consent_data.sql
```

> **Expected runtime:** 10–30 minutes. The script commits every 1,000 rows and prints progress every 100,000 rows, so partial progress is preserved if interrupted.

---

## 8. HTML reports

`run-test.sh` generates the HTML report automatically at the end of each run. To regenerate manually:

```bash
node scripts/html-report.js results/is-crud-summary.json     medium 0 results/is-crud-report.html
node scripts/html-report.js results/apim-crud-summary.json   medium 0 results/apim-crud-report.html
node scripts/html-report.js results/dcr-latency-summary.json dcr    0 results/dcr-latency-report.html
node scripts/html-report.js results/is-search-summary.json   medium 0 results/is-search-report.html
```

Reports are self-contained HTML files — open directly in any browser, no server required.

### Baseline vs extended comparison

To compare two variants (e.g., with and without an extension):

```bash
node scripts/compare-variants.js \
  results/comparison/baseline-summary.json \
  results/comparison/extended-summary.json
```

Or use `run-comparison.sh` to automate the full baseline → extended workflow.

---

## 9. Endpoint reference

### IS CRUD (`is-crud`)

| Scenario | Tag | Method | Path |
|---|---|---|---|
| `create_consent` | IS_CreateAccountConsent | POST | `/api/fs/consent/manage/account-access-consents` |
| `get_consent` | IS_GetAccountConsent | GET | `/api/fs/consent/manage/account-access-consents/{id}` |

The `get_consent` scenario provisions a throwaway consent first (tagged `IS_Setup`, excluded from metrics) then measures only the GET call.

### APIM CRUD (`apim-crud`)

| Scenario | Tag | Method | Path |
|---|---|---|---|
| `get_accounts` | APIM_GetAccounts | GET | `{aispContext}/accounts` |
| `get_balances` | APIM_GetBalances | GET | `{aispContext}/accounts/{id}/balances` |
| `get_transactions` | APIM_GetTransactions | GET | `{aispContext}/accounts/{id}/transactions` |
| `create_payment_consent` | APIM_CreatePaymentConsent | POST | `{pispContext}/payment-consents` |
| `get_payment_consent` | APIM_GetPaymentConsent | GET | `{pispContext}/payment-consents/{id}` |

`get_balances` and `get_transactions` fetch an `accountId` first (tagged `APIM_Setup`, excluded from metrics). `get_payment_consent` creates a throwaway consent (`APIM_Setup`) before the measured GET.

### DCR Latency (`dcr`)

| Tag | Method | Path | Measured |
|---|---|---|---|
| APIM_DCR_Register | POST | `/open-banking/v3.3.0/register` | Yes |
| APIM_DCR_Delete | DELETE | `/open-banking/v3.3.0/register/{id}` | No — cleanup only |

### IS Search (`is-search`)

14 scenarios run sequentially; each uses the same ramp-up → steady-state → ramp-down profile.

| Scenario | Tag | Filter combination |
|---|---|---|
| `portal_accounts_load` | IS_PortalAccountsLoad | `consentTypes=accounts` |
| `portal_payments_load` | IS_PortalPaymentsLoad | `consentTypes=payments` |
| `accounts_active_tab` | IS_AccountsActiveTab | `consentTypes=accounts` + `consentStatuses=Authorised` |
| `accounts_inactive_tab` | IS_AccountsInactiveTab | `consentTypes=accounts` + `consentStatuses=Expired,Revoked` |
| `payments_active_tab` | IS_PaymentsActiveTab | `consentTypes=payments` + `consentStatuses=Authorised` |
| `payments_inactive_tab` | IS_PaymentsInactiveTab | `consentTypes=payments` + `consentStatuses=Consumed,Expired,Revoked` |
| `cof_active_tab` | IS_CofActiveTab | `consentTypes=fundsconfirmations` + `consentStatuses=Authorised` |
| `by_consent_id` | IS_ByConsentId | + `consentIds` equality lookup |
| `by_client_id` | IS_ByClientId | + `clientIds` filter |
| `by_user_id` | IS_ByUserId | + `userIds` filter |
| `date_narrow` | IS_DateNarrow | + `fromTime/toTime` last 24 h |
| `date_wide` | IS_DateWide | + `fromTime/toTime` last 30 d |
| `deep_pagination` | IS_DeepPagination | high `offset` |
| `large_page` | IS_LargePageSize | high `limit` |

---

## 10. Caveats

- **OB spec profile**: paths assume UK Open Banking v3.1. For Berlin Group, CDS (AU), or a custom profile, update `aispContext`/`pispContext` in `test-config.json` and the payload shapes in `k6/scenarios/accounts.js` and `payments.js`.
- **mTLS**: APIM tests present the transport cert/key from `test-config.json` on every request via k6's `tlsAuth`. Ensure the certificate is valid and trusted by the APIM gateway.
- **Token expiry**: the APPLICATION_USER token written by `setup-user-auth.js` is valid for one hour. `run-test.sh` refreshes it automatically before each scenario. If running tests manually, re-run `node scripts/setup-user-auth.js` if the token expires mid-run.
- **SearchConsents response size**: without query parameters this endpoint returns all consents and can produce very large payloads. The benchmark adds `?limit=25&clientId=...` to keep response sizes bounded.
