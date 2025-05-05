DROP PROCEDURE IF EXISTS `WSO2_OB_CONSENT_CLEANUP_SP`;

DELIMITER $$

CREATE PROCEDURE `WSO2_OB_CONSENT_CLEANUP_SP`(
    IN consentTypes VARCHAR(1024),
    IN clientIds VARCHAR(4096),
    IN consentStatuses VARCHAR(1024),
    IN purgeConsentsOlderThanXNumberOfDays INT,
    IN lastUpdatedTime BIGINT,
    IN backupTables BOOLEAN,
    IN enableAudit BOOLEAN,
    IN analyzeTables BOOLEAN,
    IN enableDataRetention BOOLEAN
)
BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
    DECLARE batchSize INT;
    DECLARE chunkSize INT;
    DECLARE checkCount INT;
    DECLARE sleepTime FLOAT;
    DECLARE rowCount INT;
    DECLARE enableLog BOOLEAN;
    DECLARE logLevel VARCHAR(10);
    DECLARE cursorTable VARCHAR(255);
    DECLARE BACKUP_TABLE VARCHAR(255);
    DECLARE cursorLoopFinished INTEGER DEFAULT 0;

    -- Data retention variables
    DECLARE enableDataRetentionForAuthResourceAndMapping BOOLEAN;
    DECLARE enableDataRetentionForObConsentFile BOOLEAN;
    DECLARE enableDataRetentionForObConsentAttribute BOOLEAN;
    DECLARE enableDataRetentionForObConsentStatusAudit BOOLEAN;

    -- Consent Data Purging Parameters
    DECLARE olderThanTimePeriodForPurging BIGINT;

    DECLARE backupTablesCursor CURSOR FOR
        SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA in (SELECT DATABASE()) AND
                TABLE_NAME IN ('OB_CONSENT','OB_CONSENT_AUTH_RESOURCE','OB_CONSENT_MAPPING','OB_CONSENT_FILE','OB_CONSENT_ATTRIBUTE','OB_CONSENT_STATUS_AUDIT');

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET cursorLoopFinished = 1;

-- -----------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
    SET batchSize = 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
    SET chunkSize = 500000;    -- SET TEMP TABLE CHUNK SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 1000000]
    SET checkCount = 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE CONSENTS COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
    SET sleepTime = 2;          -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
    SET rowCount=0;
    SET enableLog = TRUE;       -- ENABLE LOGGING [DEFAULT : FALSE]
    SET logLevel = 'TRACE';    -- SET LOG LEVELS : TRACE , DEBUG

    CASE WHEN (backupTables IS NULL)
        THEN SET backupTables = TRUE;    -- SET IF CONSENT TABLE NEEDS TO BACKUP BEFORE DELETE     [DEFAULT : TRUE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (enableAudit IS NULL)
        THEN SET enableAudit = FALSE;    -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED CONSENTS USING A TABLE   [DEFAULT : FALSE] [IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (analyzeTables IS NULL)
        THEN SET analyzeTables = FALSE; -- SET TRUE FOR Analyze the tables TO IMPROVE QUERY PERFORMANCE [DEFAULT : FALSE]
        ELSE BEGIN END;
    END CASE;

    -- Data Retention Configs (Configure if data retention is enabled)

    CASE WHEN (enableDataRetention IS NULL)
        THEN SET enableDataRetention = FALSE; -- SET TRUE FOR ENABLE DATA RETENTION (ARCHIVE PURGED DATA) [DEFAULT : FALSE]
        ELSE BEGIN END;
    END CASE;

    SET enableDataRetentionForAuthResourceAndMapping = TRUE; -- ENABLE STORING AUTH RESOURCE AND CONSENT MAPPING TABLES FOR RETENTION DATA.
    SET enableDataRetentionForObConsentFile = TRUE; -- ENABLE STORING OB_CONSENT_FILE TABLE FOR RETENTION DATA.
    SET enableDataRetentionForObConsentAttribute = TRUE; -- ENABLE STORING OB_CONSENT_ATTRIBUTE TABLE FOR RETENTION DATA.
    SET enableDataRetentionForObConsentStatusAudit = TRUE; -- ENABLE STORING OB_CONSENT_STATUS_AUDIT TABLE FOR RETENTION DATA.


    -- Consent Data Purging Configs

    CASE WHEN (consentTypes IS NULL)
        THEN SET consentTypes = '';              -- SET CONSENT_TYPES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'accounts,payments', LEAVE AS EMPTY TO SKIP)
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (clientIds IS NULL)
        THEN SET clientIds = '';                 -- SET CLIENT_IDS WHICH SHOULD BE ELIGIBLE FOR PURGING. (LEAVE AS EMPTY TO SKIP)
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (consentStatuses IS NULL)
        THEN SET consentStatuses = '';           -- SET CONSENT_STATUSES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'expired,revoked' LEAVE AS EMPTY TO SKIP)
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (purgeConsentsOlderThanXNumberOfDays IS NULL)
        THEN SET olderThanTimePeriodForPurging = 60 * 60 * 24 * 365;  -- SET TIME PERIOD (SECONDS) TO DELETE CONSENTS OLDER THAN N DAYS. (DEFAULT 365 DAYS) (CHECK BELOW FOR FOR INFO.)
        ELSE SET olderThanTimePeriodForPurging = 60 * 60 * 24 * purgeConsentsOlderThanXNumberOfDays;
    END CASE;

    CASE WHEN (lastUpdatedTime IS NULL)
        THEN SET lastUpdatedTime = unix_timestamp() - olderThanTimePeriodForPurging;   -- SET LAST_UPDATED_TIME FOR PURGING, (IF CONSENT'S UPDATED TIME IS OLDER THAN THIS VALUE THEN IT'S ELIGIBLE FOR PURGING, CHECK BELOW FOR FOR INFO.)
        ELSE BEGIN END;
    END CASE;

-- HERE IF WE WISH TO PURGE CONSENTS WITH LAST UPDATED_TIME OLDER THAN 31 DAYS (1 MONTH), WE CAN CONFIGURE olderThanTimePeriodForPurging = 60 * 60 * 24 * 31
-- THIS VALUE IS IN SECONDS (60 (1 MINUTE) * 60 (1 HOUR) * 24 (24 HOURS = 1 DAY) * 31 (31 DAYS = 1 MONTH))
-- OR ELSE WE CAN SET THE INPUT PARAMETER purgeConsentsOlderThanXNumberOfDays_in = 31 , FOR PURGE CONSENTS WITH LAST UPDATED_TIME OLDER THAN 31 DAYS.
-- IF WE WISH TO CONFIGURE EXACT TIMESTAMP OF THE LAST UPDATED_TIME RATHER THAN A TIME PERIOD, WE CAN IGNORE CONFIGURING olderThanTimePeriodForPurging, purgeConsentsOlderThanXNumberOfDays_in
-- AND ONLY CONFIGURE lastUpdatedTime WITH EXACT UNIX TIMESTAMP.
-- EX : `SET lastUpdatedTime = 1660737878;`

    IF (enableLog)
    THEN
        SELECT 'WSO2_OB_CONSENT_CLEANUP_SP STARTED ... !' AS 'INFO LOG';
    END IF;

    IF (enableAudit)
    THEN
        SET backupTables = TRUE;    -- BACKUP TABLES IS REQUIRED HENCE THE AUDIT IS ENABLED.
    END IF;


    IF (backupTables)
    THEN
        IF (enableLog)
        THEN
            SELECT 'TABLE BACKUP STARTED ... !' AS 'INFO LOG';
        END IF;

        OPEN backupTablesCursor;
        backupLoop: loop
            fetch backupTablesCursor into cursorTable;

            IF cursorLoopFinished = 1 THEN
                LEAVE backupLoop;
            END IF;

            SELECT CONCAT('BAK_', cursorTable) into BACKUP_TABLE;

            SET @dropTab=CONCAT("DROP TABLE IF EXISTS ", BACKUP_TABLE);
            PREPARE stmtDrop FROM @dropTab;
            EXECUTE stmtDrop;
            DEALLOCATE PREPARE stmtDrop;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SET @printstate=CONCAT("SELECT 'BACKING UP ",cursorTable," CONSENTS INTO ", BACKUP_TABLE, " 'AS' TRACE LOG' , COUNT(1) FROM ", cursorTable);
                PREPARE stmtPrint FROM @printstate;
                EXECUTE stmtPrint;
                DEALLOCATE PREPARE stmtPrint;
            END IF;

            SET @cretTab=CONCAT("CREATE TABLE ", BACKUP_TABLE," SELECT * FROM ",cursorTable);
            PREPARE stmtDrop FROM @cretTab;
            EXECUTE stmtDrop;
            DEALLOCATE PREPARE stmtDrop;

            IF (enableLog  AND logLevel IN ('DEBUG','TRACE') )
            THEN
                SET @printstate= CONCAT("SELECT 'BACKING UP ",BACKUP_TABLE," COMPLETED ! ' AS 'DEBUG LOG', COUNT(1) FROM ", BACKUP_TABLE);
                PREPARE stmtPrint FROM @printstate;
                EXECUTE stmtPrint;
                DEALLOCATE PREPARE stmtPrint;
            END IF;
        END loop backupLoop;
        CLOSE backupTablesCursor;
    END IF;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING AUDITLOG TABLES FOR DELETING CONSENTS
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    IF (enableAudit)
    THEN
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AUDITLOG_OB_CONSENT_CLEANUP' and TABLE_SCHEMA in (SELECT DATABASE())))
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'CREATING AUDIT TABLE AUDITLOG_OB_CONSENT_CLEANUP .. !';
            END IF;
            CREATE TABLE AUDITLOG_OB_CONSENT_CLEANUP SELECT * FROM OB_CONSENT WHERE 1 = 2;
            ALTER TABLE AUDITLOG_OB_CONSENT_CLEANUP ADD COLUMN `AUDIT_TIMESTAMP` TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        ELSE
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'USING AUDIT TABLE AUDITLOG_OB_CONSENT_CLEANUP ..!';
            END IF;
        END IF;
    END IF;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING RETENTION TABLES IF NOT EXISTS
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    IF (enableDataRetention)
    THEN
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_OB_CONSENT' and TABLE_SCHEMA in (SELECT DATABASE()))) THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'CREATING RETENTION TABLE RET_OB_CONSENT .. !' AS 'INFO LOG';
            END IF;
            CREATE TABLE RET_OB_CONSENT SELECT * FROM OB_CONSENT WHERE 1 = 2;
        END IF;
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_OB_CONSENT_AUTH_RESOURCE' and TABLE_SCHEMA in (SELECT DATABASE()))) THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'CREATING RETENTION TABLE RET_OB_CONSENT_AUTH_RESOURCE .. !' AS 'INFO LOG';
            END IF;
            CREATE TABLE RET_OB_CONSENT_AUTH_RESOURCE SELECT * FROM OB_CONSENT_AUTH_RESOURCE WHERE 1 = 2;
        END IF;
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_OB_CONSENT_MAPPING' and TABLE_SCHEMA in (SELECT DATABASE()))) THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'CREATING RETENTION TABLE RET_OB_CONSENT_MAPPING .. !' AS 'INFO LOG';
            END IF;
            CREATE TABLE RET_OB_CONSENT_MAPPING SELECT * FROM OB_CONSENT_MAPPING WHERE 1 = 2;
        END IF;
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_OB_CONSENT_FILE' and TABLE_SCHEMA in (SELECT DATABASE()))) THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'CREATING RETENTION TABLE RET_OB_CONSENT_FILE .. !' AS 'INFO LOG';
            END IF;
            CREATE TABLE RET_OB_CONSENT_FILE SELECT * FROM OB_CONSENT_FILE WHERE 1 = 2;
        END IF;
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_OB_CONSENT_ATTRIBUTE' and TABLE_SCHEMA in (SELECT DATABASE()))) THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'CREATING RETENTION TABLE RET_OB_CONSENT_ATTRIBUTE .. !' AS 'INFO LOG';
            END IF;
            CREATE TABLE RET_OB_CONSENT_ATTRIBUTE SELECT * FROM OB_CONSENT_ATTRIBUTE WHERE 1 = 2;
        END IF;
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_OB_CONSENT_STATUS_AUDIT' and TABLE_SCHEMA in (SELECT DATABASE()))) THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'CREATING RETENTION TABLE RET_OB_CONSENT_STATUS_AUDIT .. !' AS 'INFO LOG';
            END IF;
            CREATE TABLE RET_OB_CONSENT_STATUS_AUDIT SELECT * FROM OB_CONSENT_STATUS_AUDIT WHERE 1 = 2;
        END IF;
    END IF;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CALCULATING CONSENTS IN OB_CONSENT TABLE
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    IF (enableLog)
    THEN
        SELECT 'CALCULATING CONSENTS IN OB_CONSENT TABLE .... !' AS 'INFO LOG';

        IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
        THEN
            SELECT  COUNT(1)  into rowcount FROM OB_CONSENT;
            SELECT 'TOTAL CONSENTS ON OB_CONSENT TABLE BEFORE DELETE' AS 'DEBUG LOG',rowcount;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            SELECT COUNT(1) into @cleanupCount FROM OB_CONSENT WHERE (consentStatuses = '' OR FIND_IN_SET(CURRENT_STATUS, consentStatuses) > 0) AND
                    (consentTypes = '' OR FIND_IN_SET(CONSENT_TYPE, consentTypes) > 0) AND (clientIds = '' OR FIND_IN_SET(CLIENT_ID, clientIds) > 0) AND UPDATED_TIME < lastUpdatedTime;
            SELECT 'TOTAL CONSENTS SHOULD BE DELETED FROM OB_CONSENT' AS 'TRACE LOG', @cleanupCount;
            SELECT 'NOTE: ACTUAL DELETION WILL HAPPEN ONLY WHEN DELETE COUNT IS LARGER THAN CHECKCOUNT .... !' AS 'TRACE LOG';
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            set rowcount  = (rowcount - @cleanupCount);
            SELECT 'TOTAL CONSENTS SHOULD BE RETAIN IN OB_CONSENT' AS 'TRACE LOG', rowcount;
        END IF;
    END IF;

-- ------------------------------------------------------
-- BATCH DELETE CONSENT DATA
-- ------------------------------------------------------
    IF (enableLog)
    THEN
        SELECT 'CONSENT PURGING STARTED .... !' AS 'INFO LOG';
    END IF;

    CONSENT_CHUNK_LOOP: REPEAT

        DROP TEMPORARY TABLE IF EXISTS CHUNK_OB_CONSENT;

        CREATE TEMPORARY TABLE CHUNK_OB_CONSENT SELECT CONSENT_ID FROM OB_CONSENT WHERE (consentStatuses = '' OR FIND_IN_SET(CURRENT_STATUS, consentStatuses) > 0) AND
            (consentTypes = '' OR FIND_IN_SET(CONSENT_TYPE, consentTypes) > 0) AND (clientIds = '' OR FIND_IN_SET(CLIENT_ID, clientIds) > 0) AND UPDATED_TIME < lastUpdatedTime LIMIT chunkSize;

        SELECT COUNT(1) INTO @chunkCount FROM CHUNK_OB_CONSENT;

        IF (@chunkCount<checkCount)
        THEN
            LEAVE CONSENT_CHUNK_LOOP;
        END IF;

        CREATE INDEX IDX_CHK_OB_CONSENT ON CHUNK_OB_CONSENT(CONSENT_ID);

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            SELECT 'PROCESSING CHUNK OB_CONSENT STARTED .... !' AS 'TRACE LOG',@chunkCount ;
        END IF;

        IF (enableAudit)
        THEN
            INSERT INTO AUDITLOG_OB_CONSENT_CLEANUP SELECT OBC.*, CURRENT_TIMESTAMP() FROM  OB_CONSENT AS OBC INNER JOIN CHUNK_OB_CONSENT AS CHK WHERE OBC.CONSENT_ID = CHK.CONSENT_ID;
        END IF;

        CONSENT_BATCH_LOOP: REPEAT

            DROP TEMPORARY TABLE IF EXISTS BATCH_OB_CONSENT;

            CREATE TEMPORARY TABLE BATCH_OB_CONSENT SELECT CONSENT_ID FROM CHUNK_OB_CONSENT LIMIT batchSize;

            SELECT COUNT(1) INTO @batchCount FROM BATCH_OB_CONSENT;

            IF (@batchCount=0 )
            THEN
                LEAVE CONSENT_BATCH_LOOP;
            END IF;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH PROCESSING STARTED ' AS 'TRACE LOG',  @batchCount;
            END IF;

            -- STORING RETENTION DATA IN RETENTION DB
            IF (enableDataRetention) THEN

                IF (enableLog) THEN
                    SELECT 'INSERTING OB_CONSENT DATA TO RET_OB_CONSENT TABLE !' AS 'INFO LOG';
                END IF;
                INSERT INTO RET_OB_CONSENT SELECT A.* FROM OB_CONSENT AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;

                -- STORE OB_CONSENT_AUTH_RESOURCE AND OB_CONSENT_MAPPING RETENTION DATA IF ENABLED.
                IF (enableDataRetentionForAuthResourceAndMapping) THEN
                    IF (enableLog) THEN
                        SELECT 'INSERTING OB_CONSENT_AUTH_RESOURCE DATA TO RET_OB_CONSENT_AUTH_RESOURCE TABLE !' AS 'INFO LOG';
                    END IF;
                    INSERT INTO RET_OB_CONSENT_AUTH_RESOURCE SELECT A.* FROM OB_CONSENT_AUTH_RESOURCE AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;

                    IF (enableLog) THEN
                        SELECT 'INSERTING OB_CONSENT_MAPPING DATA TO RET_OB_CONSENT_MAPPING TABLE !' AS 'INFO LOG';
                    END IF;
                    INSERT INTO RET_OB_CONSENT_MAPPING SELECT OBCM.* FROM OB_CONSENT_MAPPING AS OBCM INNER JOIN OB_CONSENT_AUTH_RESOURCE AS OBAR ON OBCM.AUTH_ID = OBAR.AUTH_ID INNER JOIN BATCH_OB_CONSENT AS B ON OBAR.CONSENT_ID = B.CONSENT_ID;
                END IF;

                -- STORE OB_CONSENT_STATUS_AUDIT RETENTION DATA IF ENABLED.
                IF (enableDataRetentionForObConsentStatusAudit)
                THEN
                    IF (enableLog) THEN
                        SELECT 'INSERTING OB_CONSENT_STATUS_AUDIT DATA TO RET_OB_CONSENT_STATUS_AUDIT TABLE !' AS 'INFO LOG';
                    END IF;
                    INSERT INTO RET_OB_CONSENT_STATUS_AUDIT SELECT A.* FROM OB_CONSENT_STATUS_AUDIT AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;
                END IF;

                -- STORE OB_CONSENT_FILE RETENTION DATA IF ENABLED.
                IF (enableDataRetentionForObConsentFile)
                THEN
                    IF (enableLog) THEN
                        SELECT 'INSERTING OB_CONSENT_FILE DATA TO RET_OB_CONSENT_FILE TABLE !' AS 'INFO LOG';
                    END IF;
                    INSERT INTO RET_OB_CONSENT_FILE SELECT A.* FROM OB_CONSENT_FILE AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;
                END IF;

                -- STORE OB_CONSENT_ATTRIBUTE RETENTION DATA IF ENABLED.
                IF (enableDataRetentionForObConsentAttribute)
                THEN
                    IF (enableLog) THEN
                        SELECT 'INSERTING OB_CONSENT_ATTRIBUTE DATA TO RET_OB_CONSENT_ATTRIBUTE TABLE !' AS 'INFO LOG';
                    END IF;
                    INSERT INTO RET_OB_CONSENT_ATTRIBUTE SELECT A.* FROM OB_CONSENT_ATTRIBUTE AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;
                END IF;
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_CONSENT_ATTRIBUTE
            -- ------------------------------------------------------
            DELETE A FROM OB_CONSENT_ATTRIBUTE AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH DELETE FINISHED FOR OB_CONSENT_ATTRIBUTE :' AS 'TRACE LOG',  row_count();
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_CONSENT_FILE
            -- ------------------------------------------------------
            DELETE A FROM OB_CONSENT_FILE AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH DELETE FINISHED FOR OB_CONSENT_FILE :' AS 'TRACE LOG',  row_count();
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_CONSENT_STATUS_AUDIT
            -- ------------------------------------------------------
            DELETE A FROM OB_CONSENT_STATUS_AUDIT AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH DELETE FINISHED FOR OB_CONSENT_STATUS_AUDIT :' AS 'TRACE LOG',  row_count();
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_CONSENT_MAPPING
            -- ------------------------------------------------------
            DELETE OBCM FROM OB_CONSENT_MAPPING AS OBCM INNER JOIN OB_CONSENT_AUTH_RESOURCE AS OBAR ON OBCM.AUTH_ID = OBAR.AUTH_ID INNER JOIN BATCH_OB_CONSENT AS B ON OBAR.CONSENT_ID = B.CONSENT_ID;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH DELETE FINISHED FOR OB_CONSENT_MAPPING :' AS 'TRACE LOG',  row_count();
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_CONSENT_AUTH_RESOURCE
            -- ------------------------------------------------------
            DELETE A FROM OB_CONSENT_AUTH_RESOURCE AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH DELETE FINISHED FOR OB_CONSENT_AUTH_RESOURCE :' AS 'TRACE LOG',  row_count();
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_CONSENT
            -- ------------------------------------------------------
            DELETE A FROM OB_CONSENT AS A INNER JOIN BATCH_OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID;

            SELECT row_count() INTO rowCount;

            IF (enableLog)
            THEN
                SELECT 'BATCH DELETE FINISHED ON OB_CONSENT DATA :' AS 'INFO LOG', rowCount;
            END IF;

            DELETE A
            FROM CHUNK_OB_CONSENT AS A
                     INNER JOIN BATCH_OB_CONSENT AS B
                                ON A.CONSENT_ID = B.CONSENT_ID;

            IF ((rowCount > 0))
            THEN
                DO SLEEP(sleepTime);
            END IF;
        UNTIL rowCount=0 END REPEAT;
    UNTIL @chunkCount=0 END REPEAT;

    IF (enableLog )
    THEN
        SELECT 'CONSENT DELETE ON OB_CONSENT_ATTRIBUTE, OB_CONSENT_FILE, OB_CONSENT_AUTH_RESOURCE, OB_CONSENT_MAPPING, OB_CONSENT_STATUS_AUDIT, OB_CONSENT
             COMPLETED .... !' AS 'INFO LOG';
    END IF;

    IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
    THEN
        SELECT 'TOTAL CONSENTS ON OB_CONSENT TABLE AFTER DELETE' AS 'DEBUG LOG', COUNT(1) FROM OB_CONSENT;
    END IF;

    -- ------------------------------------------------------
    -- OPTIMIZING TABLES FOR BETTER PERFORMANCE
    -- ------------------------------------------------------

    IF (analyzeTables)
    THEN

        IF (enableLog)
        THEN
            SELECT 'TABLE ANALYZING STARTED .... !' AS 'INFO LOG';
        END IF;

        ANALYZE TABLE OB_CONSENT;
        ANALYZE TABLE OB_CONSENT_AUTH_RESOURCE;
        ANALYZE TABLE OB_CONSENT_MAPPING;
        ANALYZE TABLE OB_CONSENT_FILE;
        ANALYZE TABLE OB_CONSENT_ATTRIBUTE;
        ANALYZE TABLE OB_CONSENT_STATUS_AUDIT;

    END IF;

-- ------------------------------------------------------
    IF (enableLog)
    THEN
        SELECT 'WSO2_OB_CONSENT_CLEANUP_SP() COMPLETED .... !' AS 'INFO LOG';
    END IF;

END$$

DELIMITER ;
