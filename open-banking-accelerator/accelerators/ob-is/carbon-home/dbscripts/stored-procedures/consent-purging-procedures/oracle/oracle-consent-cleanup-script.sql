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
CREATE OR REPLACE PROCEDURE WSO2_OB_CONSENT_CLEANUP_SP (
    consentTypes_in IN VARCHAR2,
    clientIds_in IN VARCHAR2,
    consentStatuses_in IN VARCHAR2,
    purgeConsentsOlderThanXNumberOfDays_in IN NUMBER,
    lastUpdatedTime_in IN NUMBER,
    backupTables_in IN BOOLEAN,
    enableAudit_in IN BOOLEAN,
    enableStsGthrn_in IN BOOLEAN,
    enableRebuildIndexes_in IN BOOLEAN,
    enableDataRetention_in IN BOOLEAN
) IS

-- ------------------------------------------
-- VARIABLE DECLARATION
-- ------------------------------------------

systime TIMESTAMP := systimestamp;
utcTime TIMESTAMP := sys_extract_utc(systimestamp);
deleteCount INT := 0;
chunkCount INT := 0;
batchCount INT := 0;
ROWCOUNT INT := 0;
cleaupCount INT := 0;
CURRENT_SCHEMA VARCHAR(100);
backupTable VARCHAR(100);
cursorTable VARCHAR(100);

CURSOR backupTablesCursor is
SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = CURRENT_SCHEMA AND
TABLE_NAME IN ('OB_CONSENT','OB_CONSENT_AUTH_RESOURCE','OB_CONSENT_MAPPING','OB_CONSENT_FILE','OB_CONSENT_ATTRIBUTE','OB_CONSENT_STATUS_AUDIT');

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------

batchSize INT := 10000; -- BATCH WISE DELETE [DEFAULT : 10000]
chunkSize INT := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
checkCount INT := 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE CONSENTS COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
sleepTime FLOAT := 2;  -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
enableLog BOOLEAN := TRUE ; -- ENABLE LOGGING [DEFAULT : TRUE]
logLevel VARCHAR(10) := 'TRACE'; -- SET LOG LEVELS : TRACE , DEBUG
enableDataRetentionForAuthResourceAndMapping BOOLEAN := TRUE; -- ENABLE STORING AUTH RESOURCE AND CONSENT MAPPING TABLES FOR RETENTION DATA.
enableDataRetentionForObConsentFile BOOLEAN := TRUE; -- ENABLE STORING OB_CONSENT_FILE TABLE FOR RETENTION DATA.
enableDataRetentionForObConsentAttribute BOOLEAN := TRUE; -- ENABLE STORING OB_CONSENT_ATTRIBUTE TABLE FOR RETENTION DATA.
enableDataRetentionForObConsentStatusAudit BOOLEAN := TRUE; -- ENABLE STORING OB_CONSENT_STATUS_AUDIT TABLE FOR RETENTION DATA.

backupTables BOOLEAN := TRUE;
enableAudit BOOLEAN := FALSE;
enableStsGthrn BOOLEAN := FALSE;
enableRebuildIndexes BOOLEAN := FALSE;
enableDataRetention BOOLEAN := FALSE;

-- Consent Data Purging Configs
consentTypes VARCHAR(1024) := '';
clientIds VARCHAR(4096) := '';
consentStatuses VARCHAR(1024) := '';
olderThanTimePeriodForPurging NUMBER := 60 * 60 * 24 * 365;
lastUpdatedTime NUMBER := EXTRACT(DAY FROM(utcTime - to_timestamp('1970-01-01', 'YYYY-MM-DD'))) * 86400 + to_number(TO_CHAR(utcTime, 'SSSSS')) - olderThanTimePeriodForPurging;

BEGIN

backupTables:= backupTables_in;  -- SET IF CONSENTS TABLE NEEDS TO BACKUP BEFORE DELETE, WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
enableAudit := enableAudit_in; -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED CONSENTS USING A TABLE [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
enableStsGthrn := enableStsGthrn_in; -- SET TRUE FOR GATHER SCHEMA LEVEL STATS TO IMPROVE QUERY PERFORMANCE
enableRebuildIndexes := enableRebuildIndexes_in; -- SET TRUE FOR REBUILD INDEXES TO IMPROVE QUERY PERFORMANCE
enableDataRetention := enableDataRetention_in; -- SET TRUE FOR ENABLE DATA RETENTION (ARCHIVE PURGED DATA) [DEFAULT : FALSE]

-- Consent Data Purging Configs
consentTypes := consentTypes_in;              -- SET CONSENT_TYPES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'accounts,payments', LEAVE AS EMPTY TO SKIP)
clientIds := clientIds_in;                 -- SET CLIENT_IDS WHICH SHOULD BE ELIGIBLE FOR PURGING. (LEAVE AS EMPTY TO SKIP)
consentStatuses := consentStatuses_in;           -- SET CONSENT_STATUSES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'expired,revoked', LEAVE AS EMPTY TO SKIP)

CASE WHEN purgeConsentsOlderThanXNumberOfDays_in IS NOT NULL
    THEN olderThanTimePeriodForPurging := 60 * 60 * 24 * purgeConsentsOlderThanXNumberOfDays_in;  -- SET TIME PERIOD (SECONDS) TO DELETE CONSENTS OLDER THAN N DAYS (DEFAULT 365). (CHECK BELOW FOR FOR INFO.)
    ELSE olderThanTimePeriodForPurging := 60 * 60 * 24 * 365;
END CASE;

CASE WHEN lastUpdatedTime_in IS NOT NULL
    THEN lastUpdatedTime := lastUpdatedTime_in;   -- SET LAST_UPDATED_TIME FOR PURGING, (IF CONSENT'S UPDATED TIME IS OLDER THAN THIS VALUE THEN IT'S ELIGIBLE FOR PURGING, CHECK BELOW FOR FOR INFO.)
    ELSE lastUpdatedTime := EXTRACT(DAY FROM(utcTime - to_timestamp('1970-01-01', 'YYYY-MM-DD'))) * 86400 + to_number(TO_CHAR(utcTime, 'SSSSS')) - olderThanTimePeriodForPurging;
END CASE;

-- HERE IF WE WISH TO PURGE CONSENTS WITH LAST UPDATED_TIME OLDER THAN 31 DAYS (1 MONTH), WE CAN CONFIGURE olderThanTimePeriodForPurging = 60 * 60 * 24 * 31
-- THIS VALUE IS IN SECONDS (60 (1 MINUTE) * 60 (1 HOUR) * 24 (24 HOURS = 1 DAY) * 31 (31 DAYS = 1 MONTH))
-- OR ELSE WE CAN SET THE INPUT PARAMETER purgeConsentsOlderThanXNumberOfDays_in = 31 , FOR PURGE CONSENTS WITH LAST UPDATED_TIME OLDER THAN 31 DAYS.
-- IF WE WISH TO CONFIGURE EXACT TIMESTAMP OF THE LAST UPDATED_TIME RATHER THAN A TIME PERIOD, WE CAN IGNORE CONFIGURING olderThanTimePeriodForPurging, purgeConsentsOlderThanXNumberOfDays_in
-- AND ONLY CONFIGURE lastUpdatedTime WITH EXACT UNIX TIMESTAMP.
-- EX : `SET lastUpdatedTime = 1660737878;`

-- ------------------------------------------------------
-- CREATING LOG TABLE FOR DELETING CONSENTS
-- ------------------------------------------------------

SELECT SYS_CONTEXT( 'USERENV', 'CURRENT_SCHEMA' ) INTO CURRENT_SCHEMA FROM DUAL;

IF (enableLog)
THEN
SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('LOG_WSO2_OB_CONSENT_CLEANUP_SP');
    IF (ROWCOUNT = 1) then
    EXECUTE IMMEDIATE 'DROP TABLE LOG_WSO2_OB_CONSENT_CLEANUP_SP';
    COMMIT;
    END if;
EXECUTE IMMEDIATE 'CREATE TABLE LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP VARCHAR(250) , LOG VARCHAR(250)) NOLOGGING';
COMMIT;
EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''LOG_WSO2_OB_CONSENT_CLEANUP_SP STARTED .... !'')';
EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''USING SCHEMA :'||CURRENT_SCHEMA||''')';
COMMIT;
END IF;


IF (enableAudit)
THEN
backupTables := TRUE;    -- BACKUP TABLES IS REQUIRED BE TRUE, HENCE THE AUDIT IS ENABLED.
END IF;

-- ------------------------------------------------------
-- BACKUP TABLES
-- ------------------------------------------------------


IF (backupTables)
THEN
      IF (enableLog)
      THEN
          EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TABLE BACKUP STARTED ... !'')';
          COMMIT;
      END IF;

      FOR cursorTable IN backupTablesCursor
      LOOP

      SELECT REPLACE(''||cursorTable.TABLE_NAME||'','OB_','BAK_OB_') INTO backupTable FROM DUAL;

      IF (enableLog AND logLevel IN ('TRACE'))
      THEN
          EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BACKING UP '||cursorTable.TABLE_NAME||' INTO '||backupTable||' STARTED '')';
          COMMIT;
      END IF;

      SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper(backupTable);
      IF (ROWCOUNT = 1)
      THEN
          EXECUTE IMMEDIATE 'DROP TABLE '||backupTable;
          COMMIT;
      END if;

      EXECUTE IMMEDIATE 'CREATE TABLE '||backupTable||' AS (SELECT * FROM '||cursorTable.TABLE_NAME||')';
      ROWCOUNT:= sql%rowcount;
      COMMIT;

      IF (enableLog  AND logLevel IN ('TRACE','DEBUG') )
      THEN
          EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BACKING UP '||cursorTable.TABLE_NAME||' COMPLETED WITH : '||ROWCOUNT||''')';
          COMMIT;
      END IF;

      END LOOP;
      IF (enableLog)
      THEN
          EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
          COMMIT;
      END IF;
END IF;


-- ------------------------------------------------------
-- CREATING AUDIT TABLES FOR CONSENTS DELETION FOR THE FIRST TIME RUN
-- ------------------------------------------------------
IF (enableAudit)
THEN

    SELECT count(1) into ROWCOUNT FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = 'AUDITLOG_OB_CONSENT_CLEANUP';
    IF (ROWCOUNT =0 )
    THEN
        IF (enableLog  AND logLevel IN ('TRACE') )
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATING AUDIT TABLE AUDITLOG_OB_CONSENT_CLEANUP .. ! '')';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE AUDITLOG_OB_CONSENT_CLEANUP as (SELECT * FROM OB_CONSENT WHERE 1 = 2)';
        EXECUTE IMMEDIATE 'ALTER TABLE AUDITLOG_OB_CONSENT_CLEANUP ADD AUDIT_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP';
        COMMIT;
    ELSE
        IF (enableLog  AND logLevel IN ('TRACE') )
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''USING AUDIT TABLE AUDITLOG_OB_CONSENT_CLEANUP'')';
            COMMIT;
        END IF;
    END IF;


    IF (enableLog  AND logLevel IN ('TRACE'))
    THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
        COMMIT;
    END IF;

END IF;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING RETENTION TABLES IF NOT EXISTS
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (enableDataRetention)
THEN

    SELECT count(1) into ROWCOUNT FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = 'RET_OB_CONSENT';
    IF (ROWCOUNT =0 )
    THEN
        IF (enableLog  AND logLevel IN ('TRACE') )
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATING RETENTION TABLE RET_OB_CONSENT .. !'')';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE RET_OB_CONSENT as (SELECT * FROM OB_CONSENT WHERE 1 = 2)';
        COMMIT;
    END IF;
    
    SELECT count(1) into ROWCOUNT FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = 'RET_OB_CONSENT_AUTH_RESOURCE';
    IF (ROWCOUNT =0 )
    THEN
        IF (enableLog  AND logLevel IN ('TRACE') )
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATING RETENTION TABLE RET_OB_CONSENT_AUTH_RESOURCE .. !'')';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE RET_OB_CONSENT_AUTH_RESOURCE as (SELECT * FROM OB_CONSENT_AUTH_RESOURCE WHERE 1 = 2)';
        COMMIT;
    END IF;
    
    SELECT count(1) into ROWCOUNT FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = 'RET_OB_CONSENT_MAPPING';
    IF (ROWCOUNT =0 )
    THEN
        IF (enableLog  AND logLevel IN ('TRACE') )
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATING RETENTION TABLE RET_OB_CONSENT_MAPPING .. !'')';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE RET_OB_CONSENT_MAPPING as (SELECT * FROM OB_CONSENT_MAPPING WHERE 1 = 2)';
        COMMIT;
    END IF;
    
    SELECT count(1) into ROWCOUNT FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = 'RET_OB_CONSENT_FILE';
    IF (ROWCOUNT =0 )
    THEN
        IF (enableLog  AND logLevel IN ('TRACE') )
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATING RETENTION TABLE RET_OB_CONSENT_FILE .. !'')';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE RET_OB_CONSENT_FILE as (SELECT * FROM OB_CONSENT_FILE WHERE 1 = 2)';
        COMMIT;
    END IF;
    
    SELECT count(1) into ROWCOUNT FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = 'RET_OB_CONSENT_ATTRIBUTE';
    IF (ROWCOUNT =0 )
    THEN
        IF (enableLog  AND logLevel IN ('TRACE') )
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATING RETENTION TABLE RET_OB_CONSENT_ATTRIBUTE .. !'')';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE RET_OB_CONSENT_ATTRIBUTE as (SELECT * FROM OB_CONSENT_ATTRIBUTE WHERE 1 = 2)';
        COMMIT;
    END IF;
    
    SELECT count(1) into ROWCOUNT FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = 'RET_OB_CONSENT_STATUS_AUDIT';
    IF (ROWCOUNT =0 )
    THEN
        IF (enableLog  AND logLevel IN ('TRACE') )
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATING RETENTION TABLE RET_OB_CONSENT_STATUS_AUDIT .. !'')';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE RET_OB_CONSENT_STATUS_AUDIT as (SELECT * FROM OB_CONSENT_STATUS_AUDIT WHERE 1 = 2)';
        COMMIT;
    END IF;

END IF;


---- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
---- CALCULATING CONSENTS COUNTS IN OB_CONSENT
---- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
IF (enableLog)
THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CALCULATING CONSENTS COUNTS IN OB_CONSENT TABLE .... !'')';
    COMMIT;

    IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
    THEN
        SELECT COUNT(1) INTO ROWCOUNT FROM OB_CONSENT;
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL CONSENTS ON OB_CONSENT TABLE BEFORE DELETE : '||ROWCOUNT||''')';
        COMMIT;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
        SELECT COUNT(1) INTO cleaupCount FROM OB_CONSENT  WHERE (consentStatuses IS NULL OR INSTR(LOWER(','||consentStatuses||','), ','||LOWER(CURRENT_STATUS)||',') > 0) AND (consentTypes IS NULL OR  INSTR(LOWER(','||consentTypes||','), ','||LOWER(CONSENT_TYPE)||',') > 0) AND (clientIds IS NULL OR INSTR(LOWER(','||clientIds||','), ','||LOWER(CLIENT_ID)||',') > 0) AND UPDATED_TIME < lastUpdatedTime;
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL CONSENTS SHOULD BE DELETED FROM OB_CONSENT : '||cleaupCount||''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''NOTE: ACTUAL DELETION WILL HAPPEN ONLY WHEN DELETE COUNT IS LARGER THAN CHECKCOUNT .... !'')';
        COMMIT;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
        ROWCOUNT := (ROWCOUNT - cleaupCount);
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL CONSENTS SHOULD BE RETAIN IN OB_CONSENT : '||ROWCOUNT||''')';
        COMMIT;
    END IF;
END IF;

-- ------------------------------------------------------
-- BATCH DELETE CONSENT DATA
-- ------------------------------------------------------
IF (enableLog)
THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CONSENT PURGING STARTED ... ! '')';
    COMMIT;
END IF;

LOOP
      SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('CHUNK_OB_CONSENT');
      IF (ROWCOUNT = 1) then
          EXECUTE IMMEDIATE 'DROP TABLE CHUNK_OB_CONSENT';
          COMMIT;
      END if;

      EXECUTE IMMEDIATE 'CREATE TABLE CHUNK_OB_CONSENT (CONSENT_ID VARCHAR(255),CONSTRAINT CHNK_CHUNK_OB_CONSENT_PRI PRIMARY KEY (CONSENT_ID)) NOLOGGING';
      COMMIT;
      EXECUTE IMMEDIATE q'!INSERT /*+ APPEND */ INTO CHUNK_OB_CONSENT (CONSENT_ID) SELECT CONSENT_ID FROM OB_CONSENT WHERE rownum <= :chunkSize AND (
              (:consentStatuses IS NULL OR INSTR( LOWER( ','||:consentStatuses||','), LOWER( ','||CURRENT_STATUS||',') ) > 0) AND
              (:consentTypes IS NULL OR  INSTR( LOWER( ','||:consentTypes||','), LOWER( ','||CONSENT_TYPE||',') ) > 0) AND
              (:clientIds IS NULL OR INSTR( LOWER( ','||:clientIds||','), LOWER(','||CLIENT_ID||',') ) > 0) AND
              UPDATED_TIME < :lastUpdatedTime )!' using chunkSize,consentStatuses,consentStatuses,consentTypes,consentTypes,clientIds,clientIds,lastUpdatedTime;
      chunkCount:=  sql%Rowcount;
      COMMIT;

      EXIT WHEN chunkCount < checkCount ;

      IF (enableLog AND logLevel IN ('TRACE'))
      THEN
          EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CHUNK TABLE CHUNK_OB_CONSENT CREATED WITH : '||chunkCount||''')';
          COMMIT;
      END IF;

      IF (enableAudit)
      THEN
          EXECUTE IMMEDIATE 'INSERT INTO AUDITLOG_OB_CONSENT_CLEANUP SELECT OBC.*, CURRENT_TIMESTAMP FROM OB_CONSENT OBC , CHUNK_OB_CONSENT CHK WHERE OBC.CONSENT_ID=CHK.CONSENT_ID';
          COMMIT;
      END IF;

      LOOP
          SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('BATCH_OB_CONSENT');
          IF (ROWCOUNT = 1) then
              EXECUTE IMMEDIATE 'DROP TABLE BATCH_OB_CONSENT';
              COMMIT;
          END IF;

          EXECUTE IMMEDIATE 'CREATE TABLE BATCH_OB_CONSENT (CONSENT_ID VARCHAR(255),CONSTRAINT BATCH_OB_CONSENT_PRI PRIMARY KEY (CONSENT_ID)) NOLOGGING';
          COMMIT;

          EXECUTE IMMEDIATE 'INSERT /*+ APPEND */ INTO BATCH_OB_CONSENT (CONSENT_ID) SELECT CONSENT_ID FROM CHUNK_OB_CONSENT WHERE rownum <= '||batchSize||'';
          batchCount:= sql%rowcount;
          COMMIT;

          EXIT WHEN batchCount = 0 ;

          IF (enableLog AND logLevel IN ('TRACE'))
              THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE START ON CONSENT TABLES WITH BATCH_COUNT : '||batchCount||''')';
              COMMIT;
          END IF;

          -- STORING RETENTION DATA IN RETENTION DB
          IF (enableDataRetention) THEN

              IF (enableLog  AND logLevel IN ('TRACE') ) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''INSERTING OB_CONSENT DATA TO RET_OB_CONSENT TABLE !'')';
                COMMIT;
              END IF;
              EXECUTE IMMEDIATE 'INSERT INTO RET_OB_CONSENT SELECT * FROM OB_CONSENT where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
              COMMIT;
              
              -- STORE OB_CONSENT_AUTH_RESOURCE AND OB_CONSENT_MAPPING RETENTION DATA IF ENABLED.
              IF (enableDataRetentionForAuthResourceAndMapping) THEN
                  IF (enableLog  AND logLevel IN ('TRACE') ) THEN
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''INSERTING OB_CONSENT_AUTH_RESOURCE DATA TO RET_OB_CONSENT_AUTH_RESOURCE TABLE !'')';
                    COMMIT;
                  END IF;
                  EXECUTE IMMEDIATE 'INSERT INTO RET_OB_CONSENT_AUTH_RESOURCE SELECT * FROM OB_CONSENT_AUTH_RESOURCE where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
                  COMMIT;    

                  IF (enableLog  AND logLevel IN ('TRACE') ) THEN
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''INSERTING OB_CONSENT_MAPPING DATA TO RET_OB_CONSENT_MAPPING TABLE !'')';
                    COMMIT;
                  END IF;
                  EXECUTE IMMEDIATE 'INSERT INTO RET_OB_CONSENT_MAPPING SELECT * FROM OB_CONSENT_MAPPING WHERE MAPPING_ID IN ( SELECT MAPPING_ID FROM OB_CONSENT_MAPPING OBCM
                                                                     INNER JOIN OB_CONSENT_AUTH_RESOURCE OBAR ON OBCM.AUTH_ID = OBAR.AUTH_ID
                                                                     INNER JOIN BATCH_OB_CONSENT B ON OBAR.CONSENT_ID = B.CONSENT_ID)';
                  COMMIT;                                                     
              END IF;

              -- STORE OB_CONSENT_STATUS_AUDIT RETENTION DATA IF ENABLED.
              IF (enableDataRetentionForObConsentStatusAudit) THEN
                  IF (enableLog  AND logLevel IN ('TRACE') ) THEN
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''INSERTING OB_CONSENT_STATUS_AUDIT DATA TO RET_OB_CONSENT_STATUS_AUDIT TABLE !'')';
                    COMMIT;
                  END IF;
                  EXECUTE IMMEDIATE 'INSERT INTO RET_OB_CONSENT_STATUS_AUDIT SELECT * FROM OB_CONSENT_STATUS_AUDIT where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
                  COMMIT;                                                 
              END IF;              
                            
              -- STORE OB_CONSENT_FILE RETENTION DATA IF ENABLED.
              IF (enableDataRetentionForObConsentFile) THEN
                  IF (enableLog  AND logLevel IN ('TRACE') ) THEN
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''INSERTING OB_CONSENT_FILE DATA TO RET_OB_CONSENT_FILE TABLE !'')';
                    COMMIT;
                  END IF;
                  EXECUTE IMMEDIATE 'INSERT INTO RET_OB_CONSENT_FILE SELECT * FROM OB_CONSENT_FILE where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
                  COMMIT;                                                 
              END IF;
                            
              -- STORE OB_CONSENT_ATTRIBUTE RETENTION DATA IF ENABLED.
              IF (enableDataRetentionForObConsentAttribute) THEN
                  IF (enableLog  AND logLevel IN ('TRACE') ) THEN
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''INSERTING OB_CONSENT_ATTRIBUTE DATA TO RET_OB_CONSENT_ATTRIBUTE TABLE !'')';
                    COMMIT;
                  END IF;
                  EXECUTE IMMEDIATE 'INSERT INTO RET_OB_CONSENT_ATTRIBUTE SELECT * FROM OB_CONSENT_ATTRIBUTE where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
                  COMMIT;                                                 
              END IF;
                            
          END IF;
          -- ------------------------------------------------------
          -- BATCH DELETE OB_CONSENT_ATTRIBUTE
          -- ------------------------------------------------------
          IF ((batchCount > 0))
          THEN
              EXECUTE IMMEDIATE 'DELETE OB_CONSENT_ATTRIBUTE where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
              deleteCount:= sql%rowcount;
          COMMIT;
          END IF;

          IF (enableLog)
          THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE FINISHED ON OB_CONSENT_ATTRIBUTE WITH : '||deleteCount||''')';
              COMMIT;
          END IF;

          -- ------------------------------------------------------
          -- BATCH DELETE OB_CONSENT_FILE
          -- ------------------------------------------------------
          IF ((batchCount > 0))
          THEN
              EXECUTE IMMEDIATE 'DELETE OB_CONSENT_FILE where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
              deleteCount:= sql%rowcount;
          COMMIT;
          END IF;

          IF (enableLog)
          THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE FINISHED ON OB_CONSENT_FILE WITH : '||deleteCount||''')';
              COMMIT;
          END IF;

          -- ------------------------------------------------------
          -- BATCH DELETE OB_CONSENT_STATUS_AUDIT
          -- ------------------------------------------------------
          IF ((batchCount > 0))
          THEN
              EXECUTE IMMEDIATE 'DELETE OB_CONSENT_STATUS_AUDIT where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
              deleteCount:= sql%rowcount;
          COMMIT;
          END IF;

          IF (enableLog)
          THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE FINISHED ON OB_CONSENT_STATUS_AUDIT WITH : '||deleteCount||''')';
              COMMIT;
          END IF;

          -- ------------------------------------------------------
          -- BATCH DELETE OB_CONSENT_MAPPING
          -- ------------------------------------------------------
          IF ((batchCount > 0))
          THEN
              EXECUTE IMMEDIATE 'DELETE FROM OB_CONSENT_MAPPING WHERE MAPPING_ID IN ( SELECT MAPPING_ID FROM OB_CONSENT_MAPPING OBCM
                                                                     INNER JOIN OB_CONSENT_AUTH_RESOURCE OBAR ON OBCM.AUTH_ID = OBAR.AUTH_ID
                                                                     INNER JOIN BATCH_OB_CONSENT B ON OBAR.CONSENT_ID = B.CONSENT_ID)';
              deleteCount:= sql%rowcount;
          COMMIT;
          END IF;

          IF (enableLog)
          THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE FINISHED ON OB_CONSENT_MAPPING WITH : '||deleteCount||''')';
              COMMIT;
          END IF;

          -- ------------------------------------------------------
          -- BATCH DELETE OB_CONSENT_AUTH_RESOURCE
          -- ------------------------------------------------------
          IF ((batchCount > 0))
          THEN
              EXECUTE IMMEDIATE 'DELETE OB_CONSENT_AUTH_RESOURCE where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
              deleteCount:= sql%rowcount;
          COMMIT;
          END IF;

          IF (enableLog)
          THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE FINISHED ON OB_CONSENT_AUTH_RESOURCE WITH : '||deleteCount||''')';
              COMMIT;
          END IF;

          -- ------------------------------------------------------
          -- BATCH DELETE OB_CONSENT
          -- ------------------------------------------------------
          IF ((batchCount > 0))
          THEN
              EXECUTE IMMEDIATE 'DELETE OB_CONSENT where CONSENT_ID in (select CONSENT_ID from  BATCH_OB_CONSENT)';
              deleteCount:= sql%rowcount;
          COMMIT;
          END IF;

          IF (enableLog)
          THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE FINISHED ON OB_CONSENT WITH : '||deleteCount||''')';
              COMMIT;
          END IF;

          EXECUTE IMMEDIATE 'DELETE CHUNK_OB_CONSENT WHERE CONSENT_ID in (select CONSENT_ID from BATCH_OB_CONSENT)';
          COMMIT;

          IF (enableLog AND logLevel IN ('TRACE'))
          THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED BATCH ON  CHUNK_OB_CONSENT !'')';
              COMMIT;
          END IF;

          EXIT WHEN deleteCount = 0 ;

          IF ((deleteCount > 0))
          THEN
              IF (enableLog AND logLevel IN ('TRACE'))
              THEN
              EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SLEEPING ...'')';
              COMMIT;
              END IF;
          DBMS_LOCK.SLEEP(sleepTime);
          END IF;
      END LOOP;
END LOOP;

IF (enableLog)
THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE ON CONSENT DATA COMPLETED .... !'')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
END IF;
COMMIT;


IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
THEN
    SELECT COUNT(1) INTO ROWCOUNT FROM OB_CONSENT;
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL CONSENTS ON OB_CONSENT TABLE AFTER DELETE :'||ROWCOUNT||''')';
    COMMIT;

    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    COMMIT;
END IF;


-- ------------------------------------------------------
-- REBUILDING INDEXES
-- ------------------------------------------------------

IF(enableRebuildIndexes)
THEN
      FOR cursorTable IN backupTablesCursor
      LOOP
            FOR INDEX_ENTRY IN (SELECT INDEX_NAME FROM ALL_INDEXES WHERE  TABLE_NAME=''||cursorTable.TABLE_NAME||'' AND INDEX_TYPE='NORMAL' AND OWNER = CURRENT_SCHEMA)
            LOOP
                IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
                THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''REBUILDING INDEXES ON '||cursorTable.TABLE_NAME||' TABLE : '||INDEX_ENTRY.INDEX_NAME||''')';
                COMMIT;
                END IF;
                EXECUTE IMMEDIATE 'ALTER INDEX ' || INDEX_ENTRY.INDEX_NAME || ' REBUILD';
                COMMIT;
            END LOOP;
      END LOOP;

      IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
      THEN
      EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
      END IF;
      COMMIT;
END IF;


-- ------------------------------------------------------
-- STATS GATHERING FOR OPTIMUM PERFORMANCE
-- ------------------------------------------------------

IF(enableStsGthrn)
THEN
    IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
    THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SCHEMA LEVEL STATS GATHERING JOB STARTED.'')';
    COMMIT;
    END IF;

    BEGIN
    dbms_stats.gather_schema_stats(CURRENT_SCHEMA,DBMS_STATS.AUTO_SAMPLE_SIZE);
    END;

    IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
    THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SCHEMA LEVEL STATS GATHERING JOB COMPLETED.'')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    COMMIT;
    END IF;
END IF;

IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
THEN
EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''WSO2_OB_CONSENT_CLEANUP_SP COMPLETED .... !'')';
COMMIT;
END IF;

END;
