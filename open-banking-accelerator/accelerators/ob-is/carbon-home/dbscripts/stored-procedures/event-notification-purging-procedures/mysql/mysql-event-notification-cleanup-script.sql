/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

DROP PROCEDURE IF EXISTS `WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP`;

DELIMITER $$

CREATE PROCEDURE `WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP`(
    IN eventStatuses VARCHAR(1024),
    IN clientIds VARCHAR(4096),
    IN purgeEventsOlderThanXNumberOfDays INT,
    IN lastUpdatedTime BIGINT,
    IN purgeNonExistingResourceIds BOOLEAN,
    IN backupTables BOOLEAN,
    IN enableAudit BOOLEAN,
    IN analyzeTables BOOLEAN
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

    -- Event Notification Data Purging Parameters
    DECLARE olderThanTimePeriodForPurging BIGINT;

    DECLARE backupTablesCursor CURSOR FOR
        SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA in (SELECT DATABASE()) AND
                TABLE_NAME IN ('OB_NOTIFICATION','OB_NOTIFICATION_EVENT','OB_NOTIFICATION_ERROR');

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET cursorLoopFinished = 1;

-- -----------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
    SET batchSize = 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
    SET chunkSize = 500000;    -- SET TEMP TABLE CHUNK SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 1000000]
    SET checkCount = 100;       -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE EVENT NOTIFICATION COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
    SET sleepTime = 2;          -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
    SET rowCount = 0;
    SET enableLog = TRUE;       -- ENABLE LOGGING [DEFAULT : FALSE]
    SET logLevel = 'TRACE';    -- SET LOG LEVELS : TRACE , DEBUG

    CASE WHEN (backupTables IS NULL)
        THEN SET backupTables = TRUE;    -- SET IF EVENT NOTIFICATION TABLES NEEDS TO BACKUP BEFORE DELETE     [DEFAULT : TRUE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (enableAudit IS NULL)
        THEN SET enableAudit = FALSE;    -- SET TRUE FOR KEEP TRACK OF ALL THE DELETED EVENT NOTIFICATIONS USING A TABLE   [DEFAULT : FALSE] [IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (analyzeTables IS NULL)
        THEN SET analyzeTables = FALSE; -- SET TRUE FOR Analyze the tables TO IMPROVE QUERY PERFORMANCE [DEFAULT : FALSE]
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (purgeNonExistingResourceIds IS NULL)
        THEN SET purgeNonExistingResourceIds = FALSE; -- SET TRUE FOR PURGE UNTRACEABLE NOTIFICATION EVENTS. [DEFAULT : FALSE]
        ELSE BEGIN END;
    END CASE;

    -- Event Notification Data Purging Configs

    CASE WHEN (eventStatuses IS NULL)
        THEN SET eventStatuses = '';              -- SET EVENT_NOTIFICATION_STATUSES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'ACK,ERR')
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (clientIds IS NULL)
        THEN SET clientIds = '';                 -- SET CLIENT_IDS WHICH SHOULD BE ELIGIBLE FOR PURGING. (LEAVE AS EMPTY TO SKIP)
        ELSE BEGIN END;
    END CASE;

    CASE WHEN (purgeEventsOlderThanXNumberOfDays IS NULL)
        THEN SET olderThanTimePeriodForPurging = 60 * 60 * 24 * 365;  -- SET TIME PERIOD (SECONDS) TO DELETE EVENT NOTIFICATION OLDER THAN N DAYS. (DEFAULT 365 DAYS) (CHECK BELOW FOR FOR INFO.)
        ELSE SET olderThanTimePeriodForPurging = 60 * 60 * 24 * purgeEventsOlderThanXNumberOfDays;
    END CASE;

    CASE WHEN (lastUpdatedTime IS NULL)
        THEN SET lastUpdatedTime = unix_timestamp() - olderThanTimePeriodForPurging;   -- SET LAST_UPDATED_TIME FOR PURGING, (IF EVENT NOTIFICATION'S UPDATED TIME IS OLDER THAN THIS VALUE THEN IT'S ELIGIBLE FOR PURGING, CHECK BELOW FOR FOR INFO.)
        ELSE BEGIN END;
    END CASE;

-- HERE IF WE WISH TO PURGE EVENT NOTIFICATION WITH LAST UPDATED_TIME OLDER THAN 31 DAYS (1 MONTH), WE CAN CONFIGURE olderThanTimePeriodForPurging = 60 * 60 * 24 * 31
-- THIS VALUE IS IN SECONDS (60 (1 MINUTE) * 60 (1 HOUR) * 24 (24 HOURS = 1 DAY) * 31 (31 DAYS = 1 MONTH))
-- OR ELSE WE CAN SET THE INPUT PARAMETER purgeEventsOlderThanXNumberOfDays = 31 , FOR PURGE EVENT NOTIFICATION WITH LAST UPDATED_TIME OLDER THAN 31 DAYS.
-- IF WE WISH TO CONFIGURE EXACT TIMESTAMP OF THE LAST UPDATED_TIME RATHER THAN A TIME PERIOD, WE CAN IGNORE CONFIGURING olderThanTimePeriodForPurging, purgeEventsOlderThanXNumberOfDays
-- AND ONLY CONFIGURE lastUpdatedTime WITH EXACT UNIX TIMESTAMP.
-- EX : `SET lastUpdatedTime = 1660737878;`

    IF (enableLog)
    THEN
        SELECT 'WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP STARTED ... !' AS 'INFO LOG';
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
                SET @printstate=CONCAT("SELECT 'BACKING UP ",cursorTable," DATA INTO ", BACKUP_TABLE, " 'AS' TRACE LOG' , COUNT(1) FROM ", cursorTable);
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
-- CREATING AUDITLOG TABLES FOR DELETING EVENT NOTIFICATION
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    IF (enableAudit)
    THEN
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP' and TABLE_SCHEMA in (SELECT DATABASE())))
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'CREATING AUDIT TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP .. !';
            END IF;
            CREATE TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP SELECT * FROM OB_NOTIFICATION WHERE 1 = 2;
            ALTER TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP ADD COLUMN `AUDIT_TIMESTAMP` TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        ELSE
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                SELECT 'USING AUDIT TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP ..!';
            END IF;
        END IF;
    END IF;


-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CALCULATING NOTIFICATION EVENTS IN OB_NOTIFICATION TABLE
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    IF (enableLog)
    THEN
        SELECT 'CALCULATING EVENT NOTIFICATIONS IN OB_NOTIFICATION TABLE .... !' AS 'INFO LOG';

        IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
        THEN
            SELECT  COUNT(1)  into rowcount FROM OB_NOTIFICATION;
            SELECT 'TOTAL EVENT NOTIFICATIONS ON OB_NOTIFICATION TABLE BEFORE DELETE' AS 'DEBUG LOG',rowcount;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            SELECT COUNT(1) into @cleanupCount FROM OB_NOTIFICATION A WHERE FIND_IN_SET(A.STATUS, eventStatuses) > 0 AND
                   (clientIds = '' OR FIND_IN_SET(A.CLIENT_ID, clientIds) > 0) AND A.UPDATED_TIMESTAMP < FROM_UNIXTIME(lastUpdatedTime)
                    OR A.RESOURCE_ID IN (
                            SELECT B.RESOURCE_ID FROM OB_NOTIFICATION B
                                LEFT JOIN OB_CONSENT C ON B.RESOURCE_ID = C.CONSENT_ID WHERE C.CONSENT_ID IS NULL AND purgeNonExistingResourceIds
                        );
            SELECT 'TOTAL EVENT NOTIFICATIONS SHOULD BE DELETED FROM OB_NOTIFICATION' AS 'TRACE LOG', @cleanupCount;
            SELECT 'NOTE: ACTUAL DELETION WILL HAPPEN ONLY WHEN DELETE COUNT IS LARGER THAN CHECKCOUNT .... !' AS 'TRACE LOG';
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            set rowcount  = (rowcount - @cleanupCount);
            SELECT 'TOTAL EVENT NOTIFICATIONS SHOULD BE RETAIN IN OB_NOTIFICATION' AS 'TRACE LOG', rowcount;
        END IF;
    END IF;

-- ------------------------------------------------------
-- BATCH DELETE EVENT NOTIFICATION DATA
-- ------------------------------------------------------
    IF (enableLog)
    THEN
        SELECT 'EVENT NOTIFICATIONS PURGING STARTED .... !' AS 'INFO LOG';
    END IF;

    EVENT_NOTIFICATION_CHUNK_LOOP: REPEAT

        DROP TEMPORARY TABLE IF EXISTS CHUNK_OB_EVENT_NOTIFICATION;

        CREATE TEMPORARY TABLE CHUNK_OB_EVENT_NOTIFICATION SELECT NOTIFICATION_ID FROM OB_NOTIFICATION A WHERE FIND_IN_SET(A.STATUS, eventStatuses) > 0 AND
           (clientIds = '' OR FIND_IN_SET(A.CLIENT_ID, clientIds) > 0) AND A.UPDATED_TIMESTAMP < FROM_UNIXTIME(lastUpdatedTime)
            OR A.RESOURCE_ID IN (
                SELECT B.RESOURCE_ID FROM OB_NOTIFICATION B LEFT JOIN OB_CONSENT C ON B.RESOURCE_ID = C.CONSENT_ID
                WHERE C.CONSENT_ID IS NULL AND purgeNonExistingResourceIds
            ) LIMIT chunkSize;

        SELECT COUNT(1) INTO @chunkCount FROM CHUNK_OB_EVENT_NOTIFICATION;

        IF (@chunkCount<checkCount)
        THEN
            LEAVE EVENT_NOTIFICATION_CHUNK_LOOP;
        END IF;

        CREATE INDEX IDX_CHK_OB_EVENT_NOTIFICATION ON CHUNK_OB_EVENT_NOTIFICATION(NOTIFICATION_ID);

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            SELECT 'PROCESSING CHUNK OB_NOTIFICATION STARTED .... !' AS 'TRACE LOG',@chunkCount ;
        END IF;

        IF (enableAudit)
        THEN
            INSERT INTO AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP SELECT OBN.*, CURRENT_TIMESTAMP() FROM OB_NOTIFICATION AS OBN INNER JOIN CHUNK_OB_EVENT_NOTIFICATION AS CHK WHERE OBN.NOTIFICATION_ID = CHK.NOTIFICATION_ID;
        END IF;

        EVENT_NOTIFICATION_BATCH_LOOP: REPEAT

            DROP TEMPORARY TABLE IF EXISTS BATCH_OB_EVENT_NOTIFICATION;

            CREATE TEMPORARY TABLE BATCH_OB_EVENT_NOTIFICATION SELECT NOTIFICATION_ID FROM CHUNK_OB_EVENT_NOTIFICATION LIMIT batchSize;

            SELECT COUNT(1) INTO @batchCount FROM BATCH_OB_EVENT_NOTIFICATION;

            IF (@batchCount=0 )
            THEN
                LEAVE EVENT_NOTIFICATION_BATCH_LOOP;
            END IF;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH PROCESSING STARTED ' AS 'TRACE LOG',  @batchCount;
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_NOTIFICATION_EVENT
            -- ------------------------------------------------------
            DELETE A
            FROM OB_NOTIFICATION_EVENT AS A
                     INNER JOIN BATCH_OB_EVENT_NOTIFICATION AS B
                                ON A.NOTIFICATION_ID = B.NOTIFICATION_ID;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH DELETE FINISHED FOR OB_NOTIFICATION_EVENT :' AS 'TRACE LOG',  row_count();
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_NOTIFICATION_ERROR
            -- ------------------------------------------------------
            DELETE A
            FROM OB_NOTIFICATION_ERROR AS A
                     INNER JOIN BATCH_OB_EVENT_NOTIFICATION AS B
                                ON A.NOTIFICATION_ID = B.NOTIFICATION_ID;

            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                SELECT 'BATCH DELETE FINISHED FOR OB_NOTIFICATION_ERROR :' AS 'TRACE LOG',  row_count();
            END IF;

            -- ------------------------------------------------------
            -- BATCH DELETE OB_NOTIFICATION
            -- ------------------------------------------------------
            DELETE A
            FROM OB_NOTIFICATION AS A
                     INNER JOIN BATCH_OB_EVENT_NOTIFICATION AS B
                                ON A.NOTIFICATION_ID = B.NOTIFICATION_ID;

            SELECT row_count() INTO rowCount;

            IF (enableLog)
            THEN
                SELECT 'BATCH DELETE FINISHED ON EVENT_NOTIFICATION DATA :' AS 'INFO LOG', rowCount;
            END IF;

            DELETE A
            FROM CHUNK_OB_EVENT_NOTIFICATION AS A
                     INNER JOIN BATCH_OB_EVENT_NOTIFICATION AS B
                                ON A.NOTIFICATION_ID = B.NOTIFICATION_ID;

            IF ((rowCount > 0))
            THEN
                DO SLEEP(sleepTime);
            END IF;
        UNTIL rowCount=0 END REPEAT;
    UNTIL @chunkCount=0 END REPEAT;

    IF (enableLog )
    THEN
        SELECT 'EVENT NOTIFICATION DELETE ON OB_NOTIFICATION, OB_NOTIFICATION_ERROR, OB_NOTIFICATION_EVENT COMPLETED .... !' AS 'INFO LOG';
    END IF;

    IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
    THEN
        SELECT 'TOTAL EVENT NOTIFICATIONS ON OB_NOTIFICATION TABLE AFTER DELETE' AS 'DEBUG LOG', COUNT(1) FROM OB_NOTIFICATION;
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

        ANALYZE TABLE OB_NOTIFICATION;
        ANALYZE TABLE OB_NOTIFICATION_ERROR;
        ANALYZE TABLE OB_NOTIFICATION_EVENT;

    END IF;

-- ------------------------------------------------------
    IF (enableLog)
    THEN
        SELECT 'WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP() COMPLETED .... !' AS 'INFO LOG';
    END IF;

END$$

DELIMITER ;
