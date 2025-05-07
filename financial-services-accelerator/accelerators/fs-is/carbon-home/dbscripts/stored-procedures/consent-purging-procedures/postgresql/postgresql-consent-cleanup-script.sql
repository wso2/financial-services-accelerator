CREATE OR REPLACE PROCEDURE WSO2_FS_CONSENT_CLEANUP_SP(
    IN consentTypes VARCHAR(1024),
    IN clientIds VARCHAR(4096),
    IN consentStatuses VARCHAR(1024),
    IN purgeConsentsOlderThanXNumberOfDays INT,
    IN lastUpdatedTime BIGINT,
    IN backupTables BOOLEAN,
    IN enableAudit BOOLEAN,
    IN enableReindexing BOOLEAN,
    IN enableTblAnalyzing BOOLEAN,
    IN enableDataRetention BOOLEAN
) AS $$
DECLARE

batchSize int;
chunkSize int;
checkCount int;
sleepTime float;
enableLog boolean;
logLevel VARCHAR(10);
backupTable text;
indexTable text;
notice text;
cusrRecord record;
rowcount bigint :=0;
cleanupCount bigint :=0;
deleteCount INT := 0;
chunkCount INT := 0;
batchCount INT := 0;
olderThanTimePeriodForPurging bigint;

-- Data retention variables
enableDataRetentionForAuthResourceAndMapping boolean;
enableDataRetentionForFsConsentFile boolean;
enableDataRetentionForFsConsentAttribute boolean;
enableDataRetentionForFsConsentStatusAudit boolean;

tablesCursor CURSOR FOR SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND
tablename  IN ('fs_consent','fs_consent_auth_resource','fs_consent_mapping','fs_consent_file','fs_consent_attribute','fs_consent_status_audit');


BEGIN

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
batchSize := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
chunkSize := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES     [DEFAULT : 500000]
checkCount := 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE CONSENT COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]

CASE WHEN (backupTables IS NULL)
    THEN backupTables := TRUE;    -- SET IF CONSENT TABLE NEEDS TO BACKUP BEFORE DELETE     [DEFAULT : TRUE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    ELSE
END CASE;

sleepTime := 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
enableLog := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]
logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE , DEBUG

CASE WHEN (enableAudit IS NULL)
    THEN enableAudit := FALSE;  -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED CONSENTS USING A TABLE    [DEFAULT : FALSE] [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
    ELSE
END CASE;

CASE WHEN (enableReindexing IS NULL)
    THEN enableReindexing := FALSE; -- SET TRUE FOR GATHER SCHEMA LEVEL STATS TO IMPROVE QUERY PERFORMANCE [DEFAULT : FALSE]
    ELSE
END CASE;

CASE WHEN (enableTblAnalyzing IS NULL)
    THEN enableTblAnalyzing := FALSE;	-- SET TRUE FOR Rebuild Indexes TO IMPROVE QUERY PERFORMANCE [DEFAULT : FALSE]
    ELSE
END CASE;

-- Data Retention Configs (Configure if data retention is enabled)

CASE WHEN (enableDataRetention IS NULL)
    THEN enableDataRetention := FALSE;	-- SET TRUE FOR ENABLE DATA RETENTION (ARCHIVE PURGED DATA) [DEFAULT : FALSE]
    ELSE
END CASE;

enableDataRetentionForAuthResourceAndMapping := TRUE; -- ENABLE STORING AUTH RESOURCE AND CONSENT MAPPING TABLES FOR RETENTION DATA.
enableDataRetentionForFsConsentFile := TRUE; -- ENABLE STORING FS_CONSENT_FILE TABLE FOR RETENTION DATA.
enableDataRetentionForFsConsentAttribute := TRUE; -- ENABLE STORING FS_CONSENT_ATTRIBUTE TABLE FOR RETENTION DATA.
enableDataRetentionForFsConsentStatusAudit := TRUE; -- ENABLE STORING FS_CONSENT_STATUS_AUDIT TABLE FOR RETENTION DATA.

-- ------------------------------------------
-- CONSENT DATA PURGING CONFIGS
-- ------------------------------------------

CASE WHEN (consentTypes IS NULL)
    THEN consentTypes = '';     -- SET CONSENT_TYPES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'accounts,payments', LEAVE AS EMPTY TO SKIP)
    ELSE
END CASE;

CASE WHEN (clientIds IS NULL)
    THEN clientIds = '';        -- SET CLIENT_IDS WHICH SHOULD BE ELIGIBLE FOR PURGING. (LEAVE AS EMPTY TO SKIP)
    ELSE
END CASE;

CASE WHEN (consentStatuses IS NULL)
    THEN consentStatuses = '';  -- SET CONSENT_STATUSES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'expired,revoked', LEAVE AS EMPTY TO SKIP)
    ELSE
END CASE;

CASE WHEN (purgeConsentsOlderThanXNumberOfDays IS NULL)
    THEN olderThanTimePeriodForPurging = 60 * 60 * 24 * 365;  -- SET TIME PERIOD (SECONDS) TO DELETE CONSENTS OLDER THAN N DAYS. (DEFAULT 365 DAYS) (CHECK BELOW FOR FOR INFO.)
    ELSE olderThanTimePeriodForPurging = 60 * 60 * 24 * purgeConsentsOlderThanXNumberOfDays;
END CASE;

CASE WHEN (lastUpdatedTime IS NULL)
    THEN lastUpdatedTime = cast(extract(epoch from now())as bigint) - olderThanTimePeriodForPurging;   -- SET LAST_UPDATED_TIME FOR PURGING, (IF CONSENT'S UPDATED TIME IS OLDER THAN THIS VALUE THEN IT'S ELIGIBLE FOR PURGING, CHECK BELOW FOR FOR INFO.)
    ELSE
END CASE;


-- HERE IF WE WISH TO PURGE CONSENTS WITH LAST UPDATED_TIME OLDER THAN 31 DAYS (1 MONTH), WE CAN CONFIGURE olderThanTimePeriodForPurging = 60 * 60 * 24 * 31
-- THIS VALUE IS IN SECONDS (60 (1 MINUTE) * 60 (1 HOUR) * 24 (24 HOURS = 1 DAY) * 31 (31 DAYS = 1 MONTH))
-- OR ELSE WE CAN SET THE INPUT PARAMETER purgeConsentsOlderThanXNumberOfDays_in = 31 , FOR PURGE CONSENTS WITH LAST UPDATED_TIME OLDER THAN 31 DAYS.
-- IF WE WISH TO CONFIGURE EXACT TIMESTAMP OF THE LAST UPDATED_TIME RATHER THAN A TIME PERIOD, WE CAN IGNORE CONFIGURING olderThanTimePeriodForPurging, purgeConsentsOlderThanXNumberOfDays_in
-- AND ONLY CONFIGURE lastUpdatedTime WITH EXACT UNIX TIMESTAMP.
-- EX : `SET lastUpdatedTime = 1660737878;`

-- ------------------------------------------------------
-- BACKUP CONSENT TABLES
-- ------------------------------------------------------

IF (enableLog) THEN
RAISE NOTICE 'WSO2_FS_CONSENT_CLEANUP_SP STARTED .... !';
RAISE NOTICE '';
END IF;

IF (enableAudit)
THEN
backupTables:=TRUE;
END IF;

IF (backupTables)
THEN
      IF (enableLog) THEN
      RAISE NOTICE 'TABLE BACKUP STARTED ... !';
      END IF;

      OPEN tablesCursor;
      LOOP
          FETCH tablesCursor INTO cusrRecord;
          EXIT WHEN NOT FOUND;
          backupTable := 'bak_'||cusrRecord.tablename;

          EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING backupTable;
          IF (rowcount = 1)
          THEN
              IF (enableLog AND logLevel IN ('TRACE')) THEN
              RAISE NOTICE 'TABLE ALREADY EXISTS HENCE DROPPING TABLE %',backupTable;
              END IF;
              EXECUTE 'DROP TABLE '||quote_ident(backupTable);
          END IF;

          IF (enableLog AND logLevel IN ('TRACE')) THEN
          EXECUTE 'SELECT COUNT(1) FROM '||quote_ident(cusrRecord.tablename) INTO rowcount;
          notice := cusrRecord.tablename||' NUMBER OF ROWS: '||rowcount;
          RAISE NOTICE 'BACKING UP %',notice;
          END IF;

          EXECUTE 'CREATE TABLE '||quote_ident(backupTable)||' as SELECT * FROM '||quote_ident(cusrRecord.tablename);

          IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
          EXECUTE 'SELECT COUNT(1) FROM '||quote_ident(backupTable) INTO rowcount;
          notice := cusrRecord.tablename||' TABLE INTO '||backupTable||' TABLE COMPLETED WITH : '||rowcount;
          RAISE NOTICE 'BACKING UP %',notice;
          RAISE NOTICE '';
          END IF;
      END LOOP;
      CLOSE tablesCursor;
END IF;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING AUDIT TABLES FOR DELETING
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (enableAudit)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'CREATING AUDIT TABLES ... !';
    END IF;

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('auditlog_fs_consent_cleanup');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'CREATING AUDIT TABLE AUDITLOG_FS_CONSENT_CLEANUP .. !';
        END IF;
        CREATE TABLE auditlog_fs_consent_cleanup as SELECT * FROM fs_consent WHERE 1 = 2;
        ALTER TABLE auditlog_fs_consent_cleanup ADD COLUMN AUDIT_TIMESTAMP TIMESTAMP DEFAULT NOW();
    ELSE
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'USING AUDIT TABLE AUDITLOG_FS_CONSENT_CLEANUP ..!';
        END IF;
    END IF;
END IF;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING RETENTION TABLES IF NOT EXISTS
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
IF (enableDataRetention)
THEN

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('ret_fs_consent');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'CREATING RETENTION TABLE ret_fs_consent .. !';
        END IF;
        CREATE TABLE ret_fs_consent as SELECT * FROM fs_consent WHERE 1 = 2;
    END IF;

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('ret_fs_consent_auth_resource');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'CREATING RETENTION TABLE ret_fs_consent_auth_resource .. !';
        END IF;
        CREATE TABLE ret_fs_consent_auth_resource as SELECT * FROM fs_consent_auth_resource WHERE 1 = 2;
    END IF;

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('ret_fs_consent_mapping');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'CREATING RETENTION TABLE ret_fs_consent_mapping .. !';
        END IF;
        CREATE TABLE ret_fs_consent_mapping as SELECT * FROM fs_consent_mapping WHERE 1 = 2;
    END IF;

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('ret_fs_consent_file');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'CREATING RETENTION TABLE ret_fs_consent_file .. !';
        END IF;
        CREATE TABLE ret_fs_consent_file as SELECT * FROM fs_consent_file WHERE 1 = 2;
    END IF;
    
    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('ret_fs_consent_attribute');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'CREATING RETENTION TABLE ret_fs_consent_attribute .. !';
        END IF;
        CREATE TABLE ret_fs_consent_attribute as SELECT * FROM fs_consent_attribute WHERE 1 = 2;
    END IF;
    
    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('ret_fs_consent_status_audit');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'CREATING RETENTION TABLE ret_fs_consent_status_audit .. !';
        END IF;
        CREATE TABLE ret_fs_consent_status_audit as SELECT * FROM fs_consent_status_audit WHERE 1 = 2;
    END IF;
END IF;


-- ------------------------------------------------------
-- CALCULATING CONSENTS IN FS_CONSENT TABLE
-- ------------------------------------------------------

IF (enableLog) THEN
    RAISE NOTICE '';
    RAISE NOTICE 'CALCULATING CONSENTS ON FS_CONSENT .... !';

    IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
    SELECT COUNT(1) INTO rowcount FROM fs_consent;
    RAISE NOTICE 'TOTAL CONSENTS ON FS_CONSENT TABLE BEFORE DELETE: %',rowcount;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    SELECT COUNT(1) INTO cleanupCount FROM fs_consent WHERE
        ((consentStatuses = '') IS NOT FALSE OR STRPOS( LOWER(','||consentStatuses||','), ','||LOWER(CURRENT_STATUS)||',') > 0) AND
        ((consentTypes = '') IS NOT FALSE OR STRPOS( LOWER(','||consentTypes||','), ','||LOWER(CONSENT_TYPE)||',') > 0) AND
        ((clientIds = '') IS NOT FALSE OR STRPOS( LOWER(','||clientIds||','), ','||LOWER(CLIENT_ID)||',') > 0) AND
        UPDATED_TIME < lastUpdatedTime;
    RAISE NOTICE 'TOTAL CONSENTS SHOULD BE DELETED FROM FS_CONSENT: %',cleanupCount;
    RAISE NOTICE 'NOTE: ACTUAL DELETION WILL HAPPEN ONLY WHEN DELETE COUNT IS LARGER THAN CHECKCOUNT .... !';
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    rowcount := (rowcount - cleanupCount);
    RAISE NOTICE 'TOTAL CONSENTS SHOULD BE RETAIN IN FS_CONSENT: %',rowcount;
    END IF;
END IF;

-- ------------------------------------------------------
-- BATCH DELETE CONSENT DATA
-- ------------------------------------------------------

IF (enableLog)
THEN
RAISE NOTICE '';
RAISE NOTICE 'CONSENTS PURGING STARTED .... !';
END IF;

LOOP

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('chunk_fs_consent');
    IF (rowcount = 1)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE '';
        RAISE NOTICE 'DROPPING EXISTING TABLE chunk_fs_consent  !';
        END IF;
        DROP TABLE chunk_fs_consent;
    END IF;

    CREATE TABLE chunk_fs_consent (CONSENT_ID VARCHAR);

    INSERT INTO chunk_fs_consent (CONSENT_ID) SELECT CONSENT_ID FROM fs_consent WHERE
            ((consentStatuses = '') IS NOT FALSE OR STRPOS(LOWER(','||consentStatuses||','), ','||LOWER(CURRENT_STATUS)||',') > 0) AND
            ((consentTypes = '') IS NOT FALSE OR STRPOS( LOWER(','||consentTypes||','), ','||LOWER(CONSENT_TYPE)||',') > 0) AND
            ((clientIds = '') IS NOT FALSE OR STRPOS( LOWER(','||clientIds||','), ','||LOWER(CLIENT_ID)||',') > 0) AND
            UPDATED_TIME < lastUpdatedTime LIMIT chunkSize;
    GET diagnostics chunkCount := ROW_COUNT;

    IF (chunkCount < checkCount)
    THEN
    EXIT;
    END IF;

    CREATE INDEX idx_chunk_fs_consent ON chunk_fs_consent (CONSENT_ID);

    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
    RAISE NOTICE '';
    RAISE NOTICE 'PROCEEDING WITH NEW CHUNK TABLE chunk_fs_consent  %',chunkCount;
    RAISE NOTICE '';
    END IF;

    IF (enableAudit)
    THEN
    INSERT INTO auditlog_fs_consent_cleanup SELECT FSC.*, NOW() FROM fs_consent FSC , chunk_fs_consent CHK WHERE FSC.CONSENT_ID=CHK.CONSENT_ID;
   	COMMIT;
	END IF;

    LOOP
        SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('batch_fs_consent');
        IF (rowcount = 1)
        THEN
        DROP TABLE batch_fs_consent;
        END IF;

        CREATE TABLE batch_fs_consent (CONSENT_ID VARCHAR);

        INSERT INTO batch_fs_consent (CONSENT_ID) SELECT CONSENT_ID FROM chunk_fs_consent LIMIT batchSize;
        GET diagnostics batchCount := ROW_COUNT;

        IF ((batchCount = 0))
        THEN
        EXIT WHEN batchCount=0;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE '';
        RAISE NOTICE 'BATCH DELETE START ON CONSENT DATA WITH : %',batchCount;
        END IF;

        -- STORING RETENTION DATA IN RETENTION DB
        IF (enableDataRetention) THEN

            IF (enableLog AND logLevel IN ('TRACE')) THEN
                RAISE NOTICE '';
                RAISE NOTICE 'INSERTING FS_CONSENT DATA TO ret_fs_consent TABLE !';
            END IF;
            INSERT INTO ret_fs_consent SELECT * FROM fs_consent where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);

            -- STORE FS_CONSENT_AUTH_RESOURCE AND FS_CONSENT_MAPPING RETENTION DATA IF ENABLED.
            IF (enableDataRetentionForAuthResourceAndMapping) THEN
                IF (enableLog AND logLevel IN ('TRACE')) THEN
                    RAISE NOTICE '';
                    RAISE NOTICE 'INSERTING FS_CONSENT DATA TO ret_fs_consent_auth_resource TABLE !';
                END IF;
                INSERT INTO ret_fs_consent_auth_resource SELECT * FROM fs_consent_auth_resource where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
                
                IF (enableLog AND logLevel IN ('TRACE')) THEN
                    RAISE NOTICE '';
                    RAISE NOTICE 'INSERTING FS_CONSENT DATA TO ret_fs_consent_mapping TABLE !';
                END IF;
                INSERT INTO ret_fs_consent_mapping SELECT * FROM fs_consent_mapping where MAPPING_ID in (SELECT MAPPING_ID FROM fs_consent_mapping FSCM
                                                                                            INNER JOIN fs_consent_auth_resource FSAR ON FSCM.AUTH_ID = FSAR.AUTH_ID
                                                                                            INNER JOIN batch_fs_consent B ON FSAR.CONSENT_ID = B.CONSENT_ID);
            END IF;

            -- STORE FS_CONSENT_STATUS_AUDIT RETENTION DATA IF ENABLED.
            IF (enableDataRetentionForFsConsentStatusAudit)
            THEN
                IF (enableLog AND logLevel IN ('TRACE')) THEN
                    RAISE NOTICE '';
                    RAISE NOTICE 'INSERTING FS_CONSENT DATA TO ret_fs_consent_status_audit TABLE !';
                END IF;
                INSERT INTO ret_fs_consent_status_audit SELECT * FROM fs_consent_status_audit where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
            END IF;

            -- STORE FS_CONSENT_FILE RETENTION DATA IF ENABLED.
            IF (enableDataRetentionForFsConsentFile)
            THEN
                IF (enableLog AND logLevel IN ('TRACE')) THEN
                    RAISE NOTICE '';
                    RAISE NOTICE 'INSERTING FS_CONSENT DATA TO ret_fs_consent_file TABLE !';
                END IF;
                INSERT INTO ret_fs_consent_file SELECT * FROM fs_consent_file where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
            END IF;

            -- STORE FS_CONSENT_ATTRIBUTE RETENTION DATA IF ENABLED.
            IF (enableDataRetentionForFsConsentAttribute)
            THEN
                IF (enableLog AND logLevel IN ('TRACE')) THEN
                    RAISE NOTICE '';
                    RAISE NOTICE 'INSERTING FS_CONSENT DATA TO ret_fs_consent_attribute TABLE !';
                END IF;
                INSERT INTO ret_fs_consent_attribute SELECT * FROM fs_consent_attribute where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
            END IF;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE FS_CONSENT_ATTRIBUTE
        -- ------------------------------------------------------
        DELETE FROM fs_consent_attribute where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON fs_consent_attribute WITH : %',deleteCount;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE FS_CONSENT_FILE
        -- ------------------------------------------------------
        DELETE FROM fs_consent_file where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON fs_consent_file WITH : %',deleteCount;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE FS_CONSENT_STATUS_AUDIT
        -- ------------------------------------------------------
        DELETE FROM fs_consent_status_audit where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON fs_consent_status_audit WITH : %',deleteCount;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE FS_CONSENT_MAPPING
        -- ------------------------------------------------------
        DELETE FROM fs_consent_mapping where MAPPING_ID in (SELECT MAPPING_ID FROM fs_consent_mapping FSCM
                            INNER JOIN fs_consent_auth_resource FSAR ON FSCM.AUTH_ID = FSAR.AUTH_ID
                            INNER JOIN batch_fs_consent B ON FSAR.CONSENT_ID = B.CONSENT_ID);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON fs_consent_mapping WITH : %',deleteCount;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE FS_CONSENT_AUTH_RESOURCE
        -- ------------------------------------------------------
        DELETE FROM fs_consent_auth_resource where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON fs_consent_auth_resource WITH : %',deleteCount;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE FS_CONSENT
        -- ------------------------------------------------------
        DELETE FROM fs_consent where CONSENT_ID in (select CONSENT_ID from batch_fs_consent);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON fs_consent WITH : %',deleteCount;
        END IF;

        DELETE FROM chunk_fs_consent WHERE CONSENT_ID in (select CONSENT_ID from batch_fs_consent);

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'DELETED BATCH ON  chunk_fs_consent !';
        END IF;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'SLEEPING ...';
        END IF;
        perform pg_sleep(sleepTime);

    END LOOP;
END LOOP;

IF (enableLog)
THEN
RAISE NOTICE '';
RAISE NOTICE 'CONSENT DATA DELETE COMPLETED .... !';
END IF;

-- ------------------------------------------------------
-- REBUILDING INDEXES
-- ------------------------------------------------------
IF (enableReindexing)
THEN
    OPEN tablesCursor;
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'INDEX REBUILDING STARTED ...!';
    END IF;
    LOOP
        FETCH tablesCursor INTO cusrRecord;
        EXIT WHEN NOT FOUND;
        IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
        RAISE NOTICE 'INDEX REBUILDING FOR TABLE %',cusrRecord.tablename;
        END IF;
        EXECUTE 'REINDEX TABLE '||quote_ident(cusrRecord.tablename);
    END LOOP;
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'INDEX REBUILDING COMPLETED ...!';
    END IF;
    CLOSE tablesCursor;
    RAISE NOTICE '';
END IF;

-- ------------------------------------------------------
-- ANALYSING TABLES
-- ------------------------------------------------------
IF (enableTblAnalyzing)
THEN
    OPEN tablesCursor;
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'TABLE ANALYZING STARTED ...!';
    END IF;
    LOOP
        FETCH tablesCursor INTO cusrRecord;
        EXIT WHEN NOT FOUND;
        IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
        RAISE NOTICE 'TABLE ANALYZING FOR TABLE %',cusrRecord.tablename;
        END IF;
        EXECUTE 'ANALYZE '||quote_ident(cusrRecord.tablename);
    END LOOP;
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'TABLE ANALYZING COMPLETED ...!';
    END IF;
    CLOSE tablesCursor;
    RAISE NOTICE '';
END IF;

IF (enableLog) THEN
RAISE NOTICE '';
RAISE NOTICE 'WSO2_FS_CONSENT_CLEANUP_SP COMPLETED .... !';
RAISE NOTICE '';
END IF;

END;
$$
LANGUAGE 'plpgsql';
