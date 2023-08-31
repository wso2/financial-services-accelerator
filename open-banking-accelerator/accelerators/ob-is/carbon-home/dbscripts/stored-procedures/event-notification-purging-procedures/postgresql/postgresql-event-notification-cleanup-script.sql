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

CREATE OR REPLACE PROCEDURE WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP(
    IN eventStatuses VARCHAR(1024),
    IN clientIds VARCHAR(4096),
    IN purgeEventsOlderThanXNumberOfDays INT,
    IN lastUpdatedTime BIGINT,
    IN purgeNonExistingResourceIds BOOLEAN,
    IN backupTables BOOLEAN,
    IN enableAudit BOOLEAN,
    IN enableReindexing BOOLEAN,
    IN enableTblAnalyzing BOOLEAN
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

tablesCursor CURSOR FOR SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND
tablename  IN ('ob_notification','ob_notification_event','ob_notification_error');


BEGIN

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
batchSize := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
chunkSize := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES     [DEFAULT : 500000]
checkCount := 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE EVENT NOTIFICATIONS COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]

CASE WHEN (purgeNonExistingResourceIds IS NULL)
    THEN purgeNonExistingResourceIds := FALSE;    -- SET TRUE FOR PURGE UNTRACEABLE NOTIFICATION EVENTS. [DEFAULT : FALSE]
ELSE
END CASE;

CASE WHEN (backupTables IS NULL)
    THEN backupTables := TRUE;    -- SET IF EVENT NOTIFICATION TABLES NEEDS TO BACKUP BEFORE DELETE [DEFAULT : TRUE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    ELSE
END CASE;

sleepTime := 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
enableLog := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]
logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE , DEBUG

CASE WHEN (enableAudit IS NULL)
    THEN enableAudit := FALSE;  -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED EVENT NOTIFICATIONS USING A TABLE [DEFAULT : FALSE] [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
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

-- ------------------------------------------
-- EVENT NOTIFICATION DATA PURGING CONFIGS
-- ------------------------------------------

CASE WHEN (eventStatuses IS NULL)
    THEN eventStatuses = '';     -- SET EVENT_NOTIFICATION_STATUSES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'ACK,ERR')
    ELSE
END CASE;

CASE WHEN (clientIds IS NULL)
    THEN clientIds = '';        -- SET CLIENT_IDS WHICH SHOULD BE ELIGIBLE FOR PURGING. (LEAVE AS EMPTY TO SKIP)
    ELSE
END CASE;

CASE WHEN (purgeEventsOlderThanXNumberOfDays IS NULL)
    THEN olderThanTimePeriodForPurging = 60 * 60 * 24 * 365;  -- SET TIME PERIOD (SECONDS) TO DELETE EVENT NOTIFICATION OLDER THAN N DAYS. (DEFAULT 365 DAYS) (CHECK BELOW FOR FOR INFO.)
    ELSE olderThanTimePeriodForPurging = 60 * 60 * 24 * purgeEventsOlderThanXNumberOfDays;
END CASE;

CASE WHEN (lastUpdatedTime IS NULL)
    THEN lastUpdatedTime = cast(extract(epoch from now())as bigint) - olderThanTimePeriodForPurging;   -- SET LAST_UPDATED_TIME FOR PURGING, (IF EVENT NOTIFICATION'S UPDATED TIME IS OLDER THAN THIS VALUE THEN IT'S ELIGIBLE FOR PURGING, CHECK BELOW FOR FOR INFO.)
    ELSE
END CASE;


-- HERE IF WE WISH TO PURGE EVENT NOTIFICATION WITH LAST UPDATED_TIME OLDER THAN 31 DAYS (1 MONTH), WE CAN CONFIGURE olderThanTimePeriodForPurging = 60 * 60 * 24 * 31
-- THIS VALUE IS IN SECONDS (60 (1 MINUTE) * 60 (1 HOUR) * 24 (24 HOURS = 1 DAY) * 31 (31 DAYS = 1 MONTH))
-- OR ELSE WE CAN SET THE INPUT PARAMETER purgeEventsOlderThanXNumberOfDays = 31 , FOR PURGE EVENT NOTIFICATION WITH LAST UPDATED_TIME OLDER THAN 31 DAYS.
-- IF WE WISH TO CONFIGURE EXACT TIMESTAMP OF THE LAST UPDATED_TIME RATHER THAN A TIME PERIOD, WE CAN IGNORE CONFIGURING olderThanTimePeriodForPurging, purgeEventsOlderThanXNumberOfDays
-- AND ONLY CONFIGURE lastUpdatedTime WITH EXACT UNIX TIMESTAMP.
-- EX : `SET lastUpdatedTime = 1660737878;`

-- ------------------------------------------------------
-- BACKUP EVENT_NOTIFICATION TABLES
-- ------------------------------------------------------

IF (enableLog) THEN
RAISE NOTICE 'WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP STARTED .... !';
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

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('auditlog_ob_event_notification_cleanup');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'CREATING AUDIT TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP .. !';
        END IF;
        CREATE TABLE auditlog_ob_event_notification_cleanup as SELECT * FROM ob_notification WHERE 1 = 2;
        ALTER TABLE auditlog_ob_event_notification_cleanup ADD COLUMN AUDIT_TIMESTAMP TIMESTAMP DEFAULT NOW();
    ELSE
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'USING AUDIT TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP ..!';
        END IF;
    END IF;
END IF;

-- ------------------------------------------------------
-- CALCULATING EVENT NOTIFICATIONS IN OB_NOTIFICATION TABLE
-- ------------------------------------------------------

IF (enableLog) THEN
    RAISE NOTICE '';
    RAISE NOTICE 'CALCULATING EVENT NOTIFICATIONS ON OB_NOTIFICATION .... !';

    IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
    SELECT COUNT(1) INTO rowcount FROM ob_notification;
    RAISE NOTICE 'TOTAL EVENT NOTIFICATIONS ON OB_NOTIFICATION TABLE BEFORE DELETE: %',rowcount;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
        SELECT COUNT(1) INTO cleanupCount FROM ob_notification A WHERE (STRPOS( LOWER(','||eventStatuses||','), ','||LOWER(A.STATUS)||',') > 0) AND
            ((clientIds = '') IS NOT FALSE OR STRPOS(LOWER(','||clientIds||','), ','||LOWER(A.CLIENT_ID)||',') > 0) AND
            A.UPDATED_TIMESTAMP < to_timestamp(lastUpdatedTime)
            OR A.RESOURCE_ID IN (
                SELECT B.RESOURCE_ID FROM OB_NOTIFICATION B LEFT JOIN OB_CONSENT C ON B.RESOURCE_ID = C.CONSENT_ID WHERE C.CONSENT_ID IS NULL
                    AND purgeNonExistingResourceIds
            );
    RAISE NOTICE 'TOTAL EVENT NOTIFICATIONS SHOULD BE DELETED FROM OB_NOTIFICATION: %',cleanupCount;
    RAISE NOTICE 'NOTE: ACTUAL DELETION WILL HAPPEN ONLY WHEN DELETE COUNT IS LARGER THAN CHECKCOUNT .... !';
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    rowcount := (rowcount - cleanupCount);
    RAISE NOTICE 'TOTAL EVENT NOTIFICATIONS SHOULD BE RETAIN IN OB_NOTIFICATION: %',rowcount;
    END IF;
END IF;

-- ------------------------------------------------------
-- BATCH DELETE EVENT NOTIFICATIONS DATA
-- ------------------------------------------------------

IF (enableLog)
THEN
RAISE NOTICE '';
RAISE NOTICE 'EVENT NOTIFICATIONS PURGING STARTED .... !';
END IF;

LOOP

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('chunk_ob_event_notifications');
    IF (rowcount = 1)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE '';
        RAISE NOTICE 'DROPPING EXISTING TABLE chunk_ob_event_notifications  !';
        END IF;
        DROP TABLE chunk_ob_event_notifications;
    END IF;

    CREATE TABLE chunk_ob_event_notifications (NOTIFICATION_ID VARCHAR);

    INSERT INTO chunk_ob_event_notifications (NOTIFICATION_ID) SELECT NOTIFICATION_ID FROM ob_notification A WHERE (
        STRPOS( LOWER(','||eventStatuses||','), ','||LOWER(A.STATUS)||',') > 0) AND
       ((clientIds = '') IS NOT FALSE OR STRPOS(LOWER(','||clientIds||','), ','||LOWER(A.CLIENT_ID)||',') > 0) AND
       A.UPDATED_TIMESTAMP < to_timestamp(lastUpdatedTime) OR
       A.RESOURCE_ID IN (
            SELECT B.RESOURCE_ID FROM OB_NOTIFICATION B LEFT JOIN OB_CONSENT C ON B.RESOURCE_ID = C.CONSENT_ID WHERE C.CONSENT_ID IS NULL AND
             purgeNonExistingResourceIds
        ) LIMIT chunkSize;
    GET diagnostics chunkCount := ROW_COUNT;

    IF (chunkCount < checkCount)
    THEN
    EXIT;
    END IF;

    CREATE INDEX idx_chunk_ob_event_notifications ON chunk_ob_event_notifications (NOTIFICATION_ID);

    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
    RAISE NOTICE '';
    RAISE NOTICE 'PROCEEDING WITH NEW CHUNK TABLE chunk_ob_event_notifications  %',chunkCount;
    RAISE NOTICE '';
    END IF;

    IF (enableAudit)
    THEN
    INSERT INTO auditlog_ob_event_notification_cleanup SELECT OBN.*, NOW() FROM ob_notification OBN , chunk_ob_event_notifications CHK WHERE OBN.NOTIFICATION_ID=CHK.NOTIFICATION_ID;
   	COMMIT;
	END IF;

    LOOP
        SELECT count(1) INTO rowcount from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('batch_ob_event_notification');
        IF (rowcount = 1)
        THEN
        DROP TABLE batch_ob_event_notification;
        END IF;

        CREATE TABLE batch_ob_event_notification (NOTIFICATION_ID VARCHAR);

        INSERT INTO batch_ob_event_notification (NOTIFICATION_ID) SELECT NOTIFICATION_ID FROM chunk_ob_event_notifications LIMIT batchSize;
        GET diagnostics batchCount := ROW_COUNT;

        IF ((batchCount = 0))
        THEN
        EXIT WHEN batchCount=0;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE '';
        RAISE NOTICE 'BATCH DELETE START ON EVENT NOTIFICATIONS DATA WITH : %',batchCount;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE OB_NOTIFICATION_EVENT
        -- ------------------------------------------------------
        DELETE FROM ob_notification_event where NOTIFICATION_ID in (select NOTIFICATION_ID from batch_ob_event_notification);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON ob_notification_event WITH : %',deleteCount;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE OB_NOTIFICATION_ERROR
        -- ------------------------------------------------------
        DELETE FROM ob_notification_error where NOTIFICATION_ID in (select NOTIFICATION_ID from batch_ob_event_notification);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON ob_notification_error WITH : %',deleteCount;
        END IF;

        -- ------------------------------------------------------
        -- BATCH DELETE OB_NOTIFICATION
        -- ------------------------------------------------------
        DELETE FROM ob_notification where NOTIFICATION_ID in (select NOTIFICATION_ID from batch_ob_event_notification);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON ob_notification WITH : %',deleteCount;
        END IF;

        DELETE FROM chunk_ob_event_notifications WHERE NOTIFICATION_ID in (select NOTIFICATION_ID from batch_ob_event_notification);

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'DELETED BATCH ON  chunk_ob_event_notifications !';
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
RAISE NOTICE 'EVENT NOTIFICATION DATA DELETE COMPLETED .... !';
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
RAISE NOTICE 'WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP COMPLETED .... !';
RAISE NOTICE '';
END IF;

END;
$$
LANGUAGE 'plpgsql';
