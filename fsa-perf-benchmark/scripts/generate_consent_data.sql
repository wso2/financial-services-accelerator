-- ============================================================================
-- FSA Performance Benchmark — Consent Data Population Script
-- ============================================================================
-- Generates 1,000,000 realistic consent records for load testing of the
-- GET /api/fs/consent/admin/search endpoint.
--
-- Generated rows
--   FS_CONSENT               1,000,000
--   FS_CONSENT_AUTH_RESOURCE 1,000,000
--   FS_CONSENT_MAPPING       ~1,500,000  (accounts get 2 mappings; others 1)
--   FS_CONSENT_STATUS_AUDIT  1,000,000
--
-- Data distribution
--   CONSENT_TYPE   accounts 50% | payments 35% | fundsconfirmations 15%
--   CURRENT_STATUS Authorised 38% | Expired 30% | Revoked 22% | Consumed 10%
--   UPDATED_TIME   2% last 24 h | 8% last 2–7 d | 15% last 8–30 d | 75% older
--   CLIENT_ID      all 1,000,000 records share @primary_client_id
--   USER_ID        5,000 PSUs; slot 0 = @primary_user_id (200 records)
--   ACCOUNT_ID     10,000 distinct bank accounts
--
-- All timestamps are computed relative to NOW() at execution time, so the
-- date-range search scenarios (last 24 h, last 30 d) always hit live data
-- regardless of when this script is run.
--
-- Configuration
--   Edit @primary_client_id and @primary_user_id below to match your
--   k6/test-config.json (clientId / searchUserId) before running.
--
-- Usage
--   mysql -h <host> -u <user> -p <database> < scripts/generate_consent_data.sql
--
-- Expected runtime: 10–30 min on typical hardware.
-- Commits every 1,000 rows to keep the undo log small.
-- ============================================================================


-- ── Configuration ─────────────────────────────────────────────────────────────
-- Match to k6/test-config.json
SET @primary_client_id = 'eLeNklWedBlm0ozXtsF_mdNqY_sa';
SET @primary_user_id   = 'psu@wso2.com';

-- ── Optional: wipe existing data before re-seeding ───────────────────────────
-- Uncomment the block below if you want a clean slate each run.
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE FS_CONSENT_STATUS_AUDIT;
-- TRUNCATE TABLE FS_CONSENT_MAPPING;
-- TRUNCATE TABLE FS_CONSENT_AUTH_RESOURCE;
-- TRUNCATE TABLE FS_CONSENT;
-- SET FOREIGN_KEY_CHECKS = 1;

-- ── Pre-run row counts ────────────────────────────────────────────────────────
SELECT 'Pre-run row counts' AS info;
SELECT 'FS_CONSENT'               AS tbl, COUNT(*) AS existing_rows FROM FS_CONSENT
UNION ALL
SELECT 'FS_CONSENT_AUTH_RESOURCE',         COUNT(*)               FROM FS_CONSENT_AUTH_RESOURCE
UNION ALL
SELECT 'FS_CONSENT_MAPPING',               COUNT(*)               FROM FS_CONSENT_MAPPING
UNION ALL
SELECT 'FS_CONSENT_STATUS_AUDIT',          COUNT(*)               FROM FS_CONSENT_STATUS_AUDIT;


-- ── Stored procedure ──────────────────────────────────────────────────────────
DELIMITER $$

DROP PROCEDURE IF EXISTS _fsa_generate_consents$$

CREATE PROCEDURE _fsa_generate_consents(
    IN p_total         INT,
    IN p_client_id     VARCHAR(255),
    IN p_user_id       VARCHAR(255)
)
BEGIN
    -- ── Local variables ───────────────────────────────────────────────────────
    DECLARE v_i          INT     DEFAULT 0;
    DECLARE v_now_ms     BIGINT  DEFAULT CAST(UNIX_TIMESTAMP(NOW(3)) * 1000 AS UNSIGNED);

    DECLARE v_consent_id  VARCHAR(255);
    DECLARE v_auth_id     VARCHAR(255);
    DECLARE v_map_id1     VARCHAR(255);
    DECLARE v_map_id2     VARCHAR(255);
    DECLARE v_audit_id    VARCHAR(255);

    DECLARE v_type        VARCHAR(64);
    DECLARE v_status      VARCHAR(64);
    DECLARE v_client_id   VARCHAR(255);
    DECLARE v_user_id     VARCHAR(255);
    DECLARE v_acct_id     VARCHAR(255);

    DECLARE v_created_ms  BIGINT;
    DECLARE v_updated_ms  BIGINT;
    DECLARE v_validity_ms BIGINT;
    DECLARE v_offset_ms   BIGINT;

    DECLARE v_r           DOUBLE;
    DECLARE v_age_bucket  INT;

    -- ── Error handler: commit progress and restore autocommit ─────────────────
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1
            @err_code = MYSQL_ERRNO,
            @err_msg  = MESSAGE_TEXT;
        COMMIT;
        SET autocommit = 1;
        SELECT CONCAT(
            'ERROR at row ', v_i,
            ': [', @err_code, '] ', @err_msg,
            ' — partial data up to row ', v_i, ' has been committed.'
        ) AS error_info;
        RESIGNAL;
    END;

    SET autocommit = 0;

    WHILE v_i < p_total DO

        -- ── Consent type  (50% accounts | 35% payments | 15% fundsconfirmations)
        SET v_r = RAND();
        IF     v_r < 0.50 THEN SET v_type = 'accounts';
        ELSEIF v_r < 0.85 THEN SET v_type = 'payments';
        ELSE                    SET v_type = 'fundsconfirmations';
        END IF;

        -- ── Status (per-type distribution) ───────────────────────────────────
        SET v_r = RAND();
        IF v_type = 'payments' THEN
            -- 35% Authorised | 30% Consumed | 20% Expired | 15% Revoked
            IF     v_r < 0.35 THEN SET v_status = 'Authorised';
            ELSEIF v_r < 0.65 THEN SET v_status = 'Consumed';
            ELSEIF v_r < 0.85 THEN SET v_status = 'Expired';
            ELSE                    SET v_status = 'Revoked';
            END IF;
        ELSE
            -- 40% Authorised | 35% Expired | 25% Revoked  (accounts & COF)
            IF     v_r < 0.40 THEN SET v_status = 'Authorised';
            ELSEIF v_r < 0.75 THEN SET v_status = 'Expired';
            ELSE                    SET v_status = 'Revoked';
            END IF;
        END IF;

        -- ── Timestamps (dynamic, relative to NOW() at call time) ──────────────
        -- UPDATED_TIME bucket: 2% last 24 h | 8% last 2–7 d | 15% last 8–30 d | 75% older
        -- MOD(v_i, 100) is deterministic so bucket counts are exact.
        -- RAND() within each bucket avoids uniform clustering inside the window.
        SET v_age_bucket = MOD(v_i, 100);

        IF v_age_bucket < 2 THEN
            -- Last 0–24 hours
            SET v_offset_ms = FLOOR(RAND() * 86400000);
        ELSEIF v_age_bucket < 10 THEN
            -- Last 1–7 days
            SET v_offset_ms = FLOOR(86400000 + RAND() * 6 * 86400000);
        ELSEIF v_age_bucket < 25 THEN
            -- Last 7–30 days
            SET v_offset_ms = FLOOR(7 * 86400000 + RAND() * 23 * 86400000);
        ELSE
            -- 30 days – 2 years ago
            SET v_offset_ms = FLOOR(30 * 86400000 + RAND() * 700 * 86400000);
        END IF;

        -- UPDATED_TIME: now minus the age offset
        SET v_updated_ms  = v_now_ms - v_offset_ms;
        -- CREATED_TIME: 0–7 days before UPDATED_TIME
        SET v_created_ms  = v_updated_ms - FLOOR(RAND() * 7 * 86400000);
        -- VALIDITY_TIME: 30–365 days after creation (cycles across rows via MOD)
        SET v_validity_ms = v_created_ms + CAST((30 + MOD(v_i, 335)) AS UNSIGNED) * 86400000;

        -- ── Identifiers ───────────────────────────────────────────────────────
        -- All records use the supplied client ID so every consent type is
        -- reachable by the single TPP client used in the k6 test suite.
        SET v_client_id = p_client_id;

        -- slot 0 → k6 searchUserId target (1 in 5,000 = 200 records)
        IF MOD(v_i, 5000) = 0 THEN
            SET v_user_id = p_user_id;
        ELSE
            SET v_user_id = CONCAT('user_', LPAD(MOD(v_i, 5000), 5, '0'), '@wso2.com');
        END IF;

        SET v_acct_id = CONCAT('ACC', LPAD(MOD(v_i, 10000) + 1, 8, '0'));

        -- ── Primary keys (UUID) ───────────────────────────────────────────────
        SET v_consent_id = UUID();
        SET v_auth_id    = UUID();
        SET v_map_id1    = UUID();
        SET v_map_id2    = UUID();
        SET v_audit_id   = UUID();

        -- ══════════════════════════════════════════════════════════════════════
        -- FS_CONSENT
        -- ══════════════════════════════════════════════════════════════════════
        INSERT INTO FS_CONSENT
            (CONSENT_ID, RECEIPT, CREATED_TIME, UPDATED_TIME, CLIENT_ID,
             CONSENT_TYPE, CURRENT_STATUS, CONSENT_FREQUENCY, VALIDITY_TIME,
             RECURRING_INDICATOR)
        VALUES (
            v_consent_id,

            -- RECEIPT: type-specific open banking JSON payload
            CASE v_type
                WHEN 'accounts' THEN JSON_OBJECT(
                    'Data', JSON_OBJECT(
                        'Permissions', JSON_ARRAY(
                            'ReadAccountsBasic', 'ReadAccountsDetail',
                            'ReadBalances', 'ReadTransactionsDetail'
                        ),
                        'ExpirationDateTime', DATE_FORMAT(
                            FROM_UNIXTIME(v_validity_ms / 1000), '%Y-%m-%dT%H:%i:%sZ'),
                        'TransactionFromDateTime', DATE_FORMAT(
                            DATE_SUB(NOW(), INTERVAL 90 DAY), '%Y-%m-%dT%H:%i:%sZ'),
                        'TransactionToDateTime', DATE_FORMAT(
                            DATE_ADD(NOW(), INTERVAL 90 DAY), '%Y-%m-%dT%H:%i:%sZ')
                    ),
                    'Risk', JSON_OBJECT()
                )
                WHEN 'payments' THEN JSON_OBJECT(
                    'Data', JSON_OBJECT(
                        'ReadRefundAccount', 'No',
                        'Initiation', JSON_OBJECT(
                            'InstructionIdentification', CONCAT('INSTR', LPAD(v_i, 12, '0')),
                            'EndToEndIdentification',    CONCAT('E2E',   LPAD(v_i, 12, '0')),
                            'InstructedAmount', JSON_OBJECT(
                                'Amount',   FORMAT(10.00 + MOD(v_i, 9990), 2),
                                'Currency', 'GBP'
                            ),
                            'CreditorAccount', JSON_OBJECT(
                                'SchemeName',     'UK.OBIE.SortCodeAccountNumber',
                                'Identification', LPAD(MOD(v_i, 99999999), 8, '0'),
                                'Name',           CONCAT('Payee ', MOD(v_i, 1000))
                            )
                        )
                    ),
                    'Risk', JSON_OBJECT('PaymentContextCode', 'EcommerceGoods')
                )
                ELSE -- fundsconfirmations
                    JSON_OBJECT(
                        'Data', JSON_OBJECT(
                            'ExpirationDateTime', DATE_FORMAT(
                                FROM_UNIXTIME(v_validity_ms / 1000), '%Y-%m-%dT%H:%i:%sZ'),
                            'DebtorAccount', JSON_OBJECT(
                                'SchemeName',     'UK.OBIE.IBAN',
                                'Identification', CONCAT(
                                    'GB', LPAD(MOD(v_i, 99), 2, '0'),
                                    'LOYD', LPAD(MOD(v_i, 99999999), 8, '0')
                                ),
                                'Name', CONCAT('COF Holder ', MOD(v_i, 1000))
                            )
                        ),
                        'Risk', JSON_OBJECT()
                    )
            END,

            v_created_ms,
            v_updated_ms,
            v_client_id,
            v_type,
            v_status,
            IF(v_type = 'accounts', 4, NULL),   -- CONSENT_FREQUENCY: 4 for AIS
            v_validity_ms,
            v_type = 'accounts'                 -- RECURRING_INDICATOR: true for AIS
        );

        -- ══════════════════════════════════════════════════════════════════════
        -- FS_CONSENT_AUTH_RESOURCE
        -- ══════════════════════════════════════════════════════════════════════
        INSERT INTO FS_CONSENT_AUTH_RESOURCE
            (AUTH_ID, CONSENT_ID, AUTH_TYPE, USER_ID, AUTH_STATUS, UPDATED_TIME)
        VALUES (
            v_auth_id,
            v_consent_id,
            'authorization_code',
            v_user_id,
            IF(v_status = 'Authorised', 'authorized', 'inactive'),
            v_updated_ms
        );

        -- ══════════════════════════════════════════════════════════════════════
        -- FS_CONSENT_MAPPING
        -- ══════════════════════════════════════════════════════════════════════
        INSERT INTO FS_CONSENT_MAPPING
            (MAPPING_ID, AUTH_ID, ACCOUNT_ID, PERMISSION, MAPPING_STATUS)
        VALUES (
            v_map_id1,
            v_auth_id,
            v_acct_id,
            IF(v_type = 'payments', 'DebtorAccount', 'ReadAccountsDetail'),
            IF(v_status = 'Authorised', 'active', 'inactive')
        );

        -- Accounts: second mapping simulates multi-account / multi-permission linkage
        IF v_type = 'accounts' THEN
            INSERT INTO FS_CONSENT_MAPPING
                (MAPPING_ID, AUTH_ID, ACCOUNT_ID, PERMISSION, MAPPING_STATUS)
            VALUES (
                v_map_id2,
                v_auth_id,
                CONCAT('ACC', LPAD(MOD(v_i + 5000, 10000) + 1, 8, '0')),
                'ReadBalances',
                IF(v_status = 'Authorised', 'active', 'inactive')
            );
        END IF;

        -- ══════════════════════════════════════════════════════════════════════
        -- FS_CONSENT_STATUS_AUDIT
        -- ══════════════════════════════════════════════════════════════════════
        INSERT INTO FS_CONSENT_STATUS_AUDIT
            (STATUS_AUDIT_ID, CONSENT_ID, CURRENT_STATUS, ACTION_TIME,
             REASON, ACTION_BY, PREVIOUS_STATUS)
        VALUES (
            v_audit_id,
            v_consent_id,
            v_status,
            v_updated_ms,
            CASE v_status
                WHEN 'Expired'  THEN 'Consent validity period ended'
                WHEN 'Revoked'  THEN 'Revoked by PSU'
                WHEN 'Consumed' THEN 'Payment successfully processed'
                ELSE NULL
            END,
            IF(v_status = 'Revoked', v_user_id, 'system'),
            IF(v_status = 'Authorised', NULL, 'Authorised')
        );

        -- ── Batch commit ──────────────────────────────────────────────────────
        SET v_i = v_i + 1;

        IF MOD(v_i, 1000) = 0 THEN
            COMMIT;

            -- Progress line every 100,000 rows
            IF MOD(v_i, 100000) = 0 THEN
                SELECT CONCAT(
                    FORMAT(v_i, 0), ' / ', FORMAT(p_total, 0), ' rows  (',
                    ROUND(v_i * 100.0 / p_total, 0), '%)'
                ) AS progress;
            END IF;
        END IF;

    END WHILE;

    COMMIT;
    SET autocommit = 1;

    SELECT CONCAT(
        'Done — ', FORMAT(p_total, 0), ' consent records generated.'
    ) AS result;
END$$

DELIMITER ;


-- ── Execute ───────────────────────────────────────────────────────────────────
SELECT CONCAT(
    'Starting generation of 1,000,000 records.',
    '  Primary client: ', @primary_client_id,
    '  Primary user: ',   @primary_user_id
) AS status;

CALL _fsa_generate_consents(1000000, @primary_client_id, @primary_user_id);

DROP PROCEDURE IF EXISTS _fsa_generate_consents;


-- ── Post-run verification ─────────────────────────────────────────────────────
SELECT 'Post-run row counts' AS info;
SELECT 'FS_CONSENT'               AS tbl, COUNT(*) AS row_count FROM FS_CONSENT
UNION ALL
SELECT 'FS_CONSENT_AUTH_RESOURCE',         COUNT(*)                FROM FS_CONSENT_AUTH_RESOURCE
UNION ALL
SELECT 'FS_CONSENT_MAPPING',               COUNT(*)                FROM FS_CONSENT_MAPPING
UNION ALL
SELECT 'FS_CONSENT_STATUS_AUDIT',          COUNT(*)                FROM FS_CONSENT_STATUS_AUDIT;

-- Type × status distribution sanity check
SELECT 'Type x status distribution' AS info;
SELECT
    CONSENT_TYPE,
    CURRENT_STATUS,
    COUNT(*)                            AS cnt,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 1) AS pct
FROM FS_CONSENT
GROUP BY CONSENT_TYPE, CURRENT_STATUS
ORDER BY CONSENT_TYPE, cnt DESC;

-- Timestamp distribution (verifies date-range search scenarios will hit data)
SELECT 'UPDATED_TIME bucket distribution' AS info;
SELECT
    SUM(UPDATED_TIME >= UNIX_TIMESTAMP(NOW()) * 1000 - 86400000)          AS last_24h,
    SUM(UPDATED_TIME >= UNIX_TIMESTAMP(NOW()) * 1000 - 7  * 86400000)     AS last_7d,
    SUM(UPDATED_TIME >= UNIX_TIMESTAMP(NOW()) * 1000 - 30 * 86400000)     AS last_30d,
    COUNT(*)                                                               AS total
FROM FS_CONSENT;

-- Confirm all records belong to the configured client
SELECT 'Client ID verification' AS info;
SELECT CLIENT_ID, COUNT(*) AS cnt
FROM FS_CONSENT
GROUP BY CLIENT_ID;
