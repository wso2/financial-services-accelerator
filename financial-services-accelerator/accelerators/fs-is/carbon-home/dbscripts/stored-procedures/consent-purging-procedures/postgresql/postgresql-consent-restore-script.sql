CREATE OR REPLACE FUNCTION WSO2_FS_CONSENT_CLEANUP_DATA_RESTORE_SP() RETURNS void AS $$
DECLARE

rowcount bigint;
enableLog boolean;
logLevel VARCHAR(10);

BEGIN

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
enableLog := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]
logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE



IF (enableLog) THEN
RAISE NOTICE 'WSO2_FS_CONSENT_CLEANUP_DATA_RESTORE_SP STARTED .... !';
RAISE NOTICE '';
END IF;


-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('fs_consent');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON fs_consent TABLE !';
END IF;
INSERT INTO fs_consent SELECT A.* FROM bak_fs_consent A LEFT JOIN fs_consent B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON fs_consent WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('fs_consent_attribute');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON fs_consent_attribute TABLE !';
END IF;
INSERT INTO fs_consent_attribute SELECT A.* FROM bak_fs_consent_attribute A LEFT JOIN fs_consent_attribute B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON fs_consent_attribute WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('fs_consent_file');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON fs_consent_file TABLE !';
END IF;
INSERT INTO fs_consent_file SELECT A.* FROM bak_fs_consent_file A LEFT JOIN fs_consent_file B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON fs_consent_file WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('fs_consent_status_audit');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON fs_consent_status_audit TABLE !';
END IF;
INSERT INTO fs_consent_status_audit SELECT A.* FROM bak_fs_consent_status_audit A LEFT JOIN fs_consent_status_audit B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON fs_consent_status_audit WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('fs_consent_auth_resource');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON fs_consent_auth_resource TABLE !';
END IF;
INSERT INTO fs_consent_auth_resource SELECT A.* FROM bak_fs_consent_auth_resource A LEFT JOIN fs_consent_auth_resource B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON fs_consent_auth_resource WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('fs_consent_mapping');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON fs_consent_mapping TABLE !';
END IF;
INSERT INTO fs_consent_mapping SELECT A.* FROM bak_fs_consent_mapping A LEFT JOIN fs_consent_mapping B ON A.MAPPING_ID = B.MAPPING_ID WHERE B.MAPPING_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON fs_consent_mapping WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

IF (enableLog) THEN
RAISE NOTICE 'WSO2_FS_CONSENT_CLEANUP_DATA_RESTORE_SP COMPLETED .... !';
RAISE NOTICE '';
END IF;

END;
$$
LANGUAGE 'plpgsql';
