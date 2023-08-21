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

CREATE OR ALTER PROCEDURE WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP (
    @eventStatuses VARCHAR(1024),
    @clientIds VARCHAR(4096),
    @purgeEventsOlderThanXNumberOfDays INT,
    @lastUpdatedTime BIGINT,
    @purgeNonExistingResourceIds BIT,
    @backupTables BIT,
    @enableAudit BIT,
    @rebuildIndexes BIT,
    @updateStats BIT
)
AS

BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE @batchSize INT;
DECLARE @chunkSize INT;
DECLARE @checkCount INT;
DECLARE @sleepTime AS VARCHAR(12);
DECLARE @rowCount INT;
DECLARE @cleaupCount INT;
DECLARE @enableLog BIT;
DECLARE @logLevel VARCHAR(10);
DECLARE @cusrBackupTable VARCHAR(100);
DECLARE @SQL NVARCHAR(MAX);
DECLARE @backupTable VARCHAR(100);
DECLARE @chunkCount INT;
DECLARE @batchCount INT;
DECLARE @deleteCount INT;
DECLARE @olderThanTimePeriodForPurging bigint;
DECLARE @purgeNonExistingResourceIdsInternal INT;

DECLARE backupTablesCursor CURSOR FOR
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('OB_NOTIFICATION','OB_NOTIFICATION_EVENT','OB_NOTIFICATION_ERROR')

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET @batchSize = 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
SET @chunkSize = 500000;      -- CHUNK WISE DELETE FOR LARGE TABLES [DEFULT : 500000]
SET @checkCount = 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE EVENT NOTIFICATION COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
SET @sleepTime = '00:00:02.000';  -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
SET @enableLog = 'TRUE';       -- ENABLE LOGGING [DEFAULT : FALSE]
SET @logLevel = 'TRACE';    -- SET LOG LEVELS : TRACE , DEBUG
SET @purgeNonExistingResourceIdsInternal = 2;


IF (@purgeNonExistingResourceIds = 1)
BEGIN
SET @purgeNonExistingResourceIdsInternal = 1;    -- SET TRUE (1) FOR PURGE UNTRACEABLE NOTIFICATION EVENTS. [DEFAULT : FALSE]
END
ELSE
BEGIN
SET @purgeNonExistingResourceIdsInternal = 2;
END

IF (@backupTables IS NULL)
BEGIN
SET @backupTables = 'TRUE';    -- SET IF EVENT NOTIFICATION TABLES NEEDS TO BACKUP BEFORE DELETE, WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
END;

IF (@enableAudit IS NULL)
BEGIN
SET @enableAudit = 'FALSE'; -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED EVENT NOTIFICATIONS USING A TABLE    [DEFAULT : FALSE] [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
END;

IF (@rebuildIndexes IS NULL)
BEGIN
SET @rebuildIndexes = 'FALSE'; -- SET TRUE FOR REBUILD INDEXES TO IMPROVE QUERY PERFORMANCE [DEFAULT : FALSE]
END;

IF (@updateStats IS NULL)
BEGIN
SET @updateStats = 'FALSE'; -- SET TRUE FOR GATHER TABLE STATS TO IMPROVE QUERY PERFORMANCE [DEFAULT : FALSE]
END;

-- ------------------------------------------
-- EVENT NOTIFICATION DATA PURGING CONFIGS
-- ------------------------------------------

IF (@eventStatuses IS NULL)
BEGIN
SET @eventStatuses = '';              -- SET EVENT_NOTIFICATION_STATUSES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'ACK,ERR')
END;

IF (@clientIds IS NULL)
BEGIN
SET @clientIds = '';                 -- SET CLIENT_IDS WHICH SHOULD BE ELIGIBLE FOR PURGING. (LEAVE AS EMPTY TO SKIP)
END;

IF (@purgeEventsOlderThanXNumberOfDays IS NULL)
    BEGIN
        SET @olderThanTimePeriodForPurging = 60 * 60 * 24 * 365;  -- SET TIME PERIOD (SECONDS) TO DELETE EVENT NOTIFICATION OLDER THAN N DAYS. (DEFAULT 365 DAYS) (CHECK BELOW FOR FOR INFO.)
    END
ELSE
    BEGIN
        SET @olderThanTimePeriodForPurging = 60 * 60 * 24 * @purgeEventsOlderThanXNumberOfDays;
    END;

IF (@lastUpdatedTime IS NULL)
BEGIN
SET @lastUpdatedTime = DATEDIFF(SECOND,'1970-01-01', GETUTCDATE()) - @olderThanTimePeriodForPurging;   -- SET LAST_UPDATED_TIME FOR PURGING, (IF EVENT NOTIFICATION'S UPDATED TIME IS OLDER THAN THIS VALUE THEN IT'S ELIGIBLE FOR PURGING, CHECK BELOW FOR FOR INFO.)
END;

-- HERE IF WE WISH TO PURGE EVENT NOTIFICATION WITH LAST UPDATED_TIME OLDER THAN 31 DAYS (1 MONTH), WE CAN CONFIGURE olderThanTimePeriodForPurging = 60 * 60 * 24 * 31
-- THIS VALUE IS IN SECONDS (60 (1 MINUTE) * 60 (1 HOUR) * 24 (24 HOURS = 1 DAY) * 31 (31 DAYS = 1 MONTH))
-- OR ELSE WE CAN SET THE INPUT PARAMETER purgeEventsOlderThanXNumberOfDays = 31 , FOR PURGE EVENT NOTIFICATION WITH LAST UPDATED_TIME OLDER THAN 31 DAYS.
-- IF WE WISH TO CONFIGURE EXACT TIMESTAMP OF THE LAST UPDATED_TIME RATHER THAN A TIME PERIOD, WE CAN IGNORE CONFIGURING olderThanTimePeriodForPurging, purgeEventsOlderThanXNumberOfDays
-- AND ONLY CONFIGURE lastUpdatedTime WITH EXACT UNIX TIMESTAMP.
-- EX : `SET lastUpdatedTime = 1660737878;`


IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP STARTED ... !' AS 'INFO LOG';
END;

IF (@enableAudit = 1)
BEGIN
SET @backupTables = 'TRUE'; -- BACKUP TABLES IS REQUIRED BE TRUE, HENCE THE AUDIT IS ENABLED.
END;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- BACKUP TABLES
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@backupTables = 1)
BEGIN

	IF (@enableLog = 1)
	BEGIN
	SELECT '[' + convert(varchar, getdate(), 121) + '] TABLE BACKUP STARTED ... !' AS 'INFO LOG';
	END;

	OPEN backupTablesCursor;
	FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable

	WHILE @@FETCH_STATUS = 0
	BEGIN
		SELECT @backupTable = 'BAK_'+@cusrBackupTable;
		IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = @backupTable))
		BEGIN
		SELECT @SQL = 'DROP TABLE dbo.' +@backupTable;
		EXEC sp_executesql @SQL;
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		SELECT @SQL = 'SELECT ''BACKING UP '+@cusrBackupTable+' INTO '+@backupTable+' STARTED WITH : '' as ''DEBUG LOG'', COUNT_BIG(*) as ''COUNT'' FROM dbo.'+@cusrBackupTable;
		EXEC sp_executesql @SQL;
		END

		SELECT @SQL = 'SELECT * INTO '+@backupTable+' FROM dbo.' +@cusrBackupTable;
		EXEC sp_executesql @SQL;

		IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
		BEGIN
		SELECT @SQL = 'SELECT ''BACKING UP '+@cusrBackupTable+' INTO '+@backupTable+' COMPLETED WITH : '' as ''DEBUG LOG'', COUNT_BIG(*) as ''COUNT'' FROM dbo.'+@backupTable;
		EXEC sp_executesql @SQL;
		END
		FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable
	END
	CLOSE backupTablesCursor;

END

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING AUDIT TABLES FOR EVENT NOTIFICATION DELETION FOR THE FIRST TIME RUN
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@enableAudit = 1)
BEGIN
	IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP'))
	BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING AUDIT TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP .. !';
			END
			Select * into dbo.AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP  from  dbo.OB_NOTIFICATION where 1 =2;
			ALTER TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP ADD AUDIT_TIMESTAMP datetime DEFAULT CURRENT_TIMESTAMP;
	END
	ELSE
	BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] USING AUDIT TABLE AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP ..!';
			END
	END
END


---- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
---- CALCULATING EVENT NOTIFICATIONS IN OB_NOTIFICATION TABLE
---- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@enableLog = 1)
BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] CALCULATING EVENT NOTIFICATIONS IN OB_NOTIFICATION TABLE .... !';

		IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
		BEGIN
		SELECT @rowcount = COUNT(1) FROM OB_NOTIFICATION;
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL EVENT NOTIFICATIONS ON OB_NOTIFICATION TABLE BEFORE DELETE :'+CAST(@rowCount as varchar);
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
        SELECT @cleaupCount = COUNT(1) FROM OB_NOTIFICATION A WHERE (CHARINDEX( ',' + LOWER(A.STATUS) + ',', LOWER(',' + @eventStatuses + ',')) > 0) AND
            (@clientIds = '' OR CHARINDEX(',' + LOWER(A.CLIENT_ID) + ',', LOWER(',' + @clientIds + ',')) > 0) AND DATEDIFF(second,{d '1970-01-01'},A.updated_timestamp) < @lastUpdatedTime OR
            A.RESOURCE_ID in (SELECT B.RESOURCE_ID FROM OB_NOTIFICATION B LEFT JOIN OB_CONSENT C ON B.RESOURCE_ID = C.CONSENT_ID WHERE C.CONSENT_ID IS NULL AND 1 = @purgeNonExistingResourceIdsInternal);

		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL EVENT NOTIFICATIONS SHOULD BE DELETED FROM OB_NOTIFICATION : ' + CAST(@cleaupCount as varchar);
        SELECT '[' + convert(varchar, getdate(), 121) + '] NOTE: ACTUAL DELETION WILL HAPPEN ONLY WHEN DELETE COUNT IS LARGER THAN CHECKCOUNT .... !';
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		select @rowcount  = (@rowcount - @cleaupCount);
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL EVENT NOTIFICATIONS SHOULD BE RETAIN IN OB_NOTIFICATION : ' + CAST(@rowCount as varchar);
		END
END

---- ------------------------------------------------------
---- BATCH DELETE EVENT NOTIFICATION DATA
---- ------------------------------------------------------

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] EVENT NOTIFICATION PURGING STARTED .... !';
END


WHILE (1=1)
BEGIN
		IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHUNK_OB_EVENT_NOTIFICATION'))
		BEGIN
		DROP TABLE CHUNK_OB_EVENT_NOTIFICATION;
		END

		CREATE TABLE CHUNK_OB_EVENT_NOTIFICATION (NOTIFICATION_ID VARCHAR (255),CONSTRAINT CHUNK_OB_EVENT_NOTIFICATION_PRI PRIMARY KEY (NOTIFICATION_ID));

        INSERT INTO CHUNK_OB_EVENT_NOTIFICATION (NOTIFICATION_ID) SELECT TOP (@chunkSize) NOTIFICATION_ID FROM OB_NOTIFICATION A WHERE
           (CHARINDEX( ',' + LOWER(A.STATUS) + ',', LOWER(',' + @eventStatuses + ',')) > 0) AND
           (@clientIds = '' OR CHARINDEX(',' + LOWER(A.CLIENT_ID) + ',', LOWER(',' + @clientIds + ',')) > 0) AND
           DATEDIFF(second,{d '1970-01-01'},A.updated_timestamp) < @lastUpdatedTime OR
           A.RESOURCE_ID in (SELECT B.RESOURCE_ID FROM OB_NOTIFICATION B LEFT JOIN OB_CONSENT C ON B.RESOURCE_ID = C.CONSENT_ID WHERE C.CONSENT_ID IS NULL AND 1 = @purgeNonExistingResourceIdsInternal);

        SELECT @chunkCount =  @@rowcount;

		IF (@chunkCount < @checkCount)
		BEGIN
		BREAK;
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] CHUNK TABLE CHUNK_OB_EVENT_NOTIFICATION CREATED WITH : '+CAST(@chunkCount as varchar);
		END

		IF (@enableAudit=1)
		BEGIN
		INSERT INTO dbo.AUDITLOG_OB_EVENT_NOTIFICATION_CLEANUP SELECT OBN.*, CURRENT_TIMESTAMP FROM OB_NOTIFICATION OBN , CHUNK_OB_EVENT_NOTIFICATION CHK WHERE OBN.NOTIFICATION_ID = CHK.NOTIFICATION_ID;
		END

		WHILE (1=1)
		BEGIN
			IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BATCH_OB_EVENT_NOTIFICATION'))
			BEGIN
			DROP TABLE BATCH_OB_EVENT_NOTIFICATION;
			END

			CREATE TABLE BATCH_OB_EVENT_NOTIFICATION (NOTIFICATION_ID VARCHAR (255),CONSTRAINT BATCH_OB_EVENT_NOTIFICATION_PRI PRIMARY KEY (NOTIFICATION_ID));

			INSERT INTO BATCH_OB_EVENT_NOTIFICATION (NOTIFICATION_ID) SELECT TOP (@batchSize) NOTIFICATION_ID FROM CHUNK_OB_EVENT_NOTIFICATION ;
			SELECT @batchCount =  @@rowcount;

			IF(@batchCount = 0)
			BEGIN
			BREAK;
			END

			IF ((@batchCount > 0))
			BEGIN

				IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE START ON EVENT NOTIFICATION TABLES WITH BATCH COUNT : '+CAST(@batchCount as varchar);
				END

                -- ------------------------------------------------------
                -- BATCH DELETE OB_NOTIFICATION_EVENT
                -- ------------------------------------------------------

				DELETE OB_NOTIFICATION_EVENT where NOTIFICATION_ID in (select NOTIFICATION_ID from  BATCH_OB_EVENT_NOTIFICATION);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON OB_NOTIFICATION_EVENT WITH : '+CAST(@deleteCount as varchar);
				END


                -- ------------------------------------------------------
                -- BATCH DELETE OB_NOTIFICATION_ERROR
                -- ------------------------------------------------------

				DELETE OB_NOTIFICATION_ERROR where NOTIFICATION_ID in (select NOTIFICATION_ID from  BATCH_OB_EVENT_NOTIFICATION);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON OB_NOTIFICATION_ERROR WITH : '+CAST(@deleteCount as varchar);
				END


                -- ------------------------------------------------------
                -- BATCH DELETE OB_NOTIFICATION
                -- ------------------------------------------------------

				DELETE OB_NOTIFICATION where NOTIFICATION_ID in (select NOTIFICATION_ID from  BATCH_OB_EVENT_NOTIFICATION);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON OB_NOTIFICATION WITH : '+CAST(@deleteCount as varchar);
				END

				DELETE CHUNK_OB_EVENT_NOTIFICATION WHERE NOTIFICATION_ID in (select NOTIFICATION_ID from BATCH_OB_EVENT_NOTIFICATION);

				IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED BATCH ON CHUNK_OB_EVENT_NOTIFICATION !';
				END

				IF ((@deleteCount > 0))
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] SLEEPING ...';
				WAITFOR DELAY @sleepTime;
				END
			END
		END
END

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] EVENT NOTIFICATION DELETE ON TABLES COMPLETED .... !';
END

IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
BEGIN
SELECT @rowcount = COUNT(1) FROM OB_NOTIFICATION;
SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL EVENT NOTIFICATIONS ON OB_NOTIFICATION TABLE AFTER DELETE :'+CAST(@rowCount as varchar);
END

-- ------------------------------------------------------
-- REBUILDING INDEXES
-- ------------------------------------------------------

IF (@rebuildIndexes = 1)
BEGIN

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] INDEX REBUILDING STARTED ...!';

END
	OPEN backupTablesCursor;
	FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable

	WHILE @@FETCH_STATUS = 0
	BEGIN

		IF (@enableLog = 1)
		BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] INDEX REBUILDING FOR TABLE :'+@cusrBackupTable;
		END

		SELECT @SQL = 'ALTER INDEX ALL ON '+@cusrBackupTable+' REBUILD WITH (ONLINE = ON)';
		EXEC sp_executesql @SQL;

		FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable
	END
	CLOSE backupTablesCursor;

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] INDEX REBUILDING FINISHED ...!';
END

END

-- ------------------------------------------------------
-- UPDATE TABLE STATS
-- ------------------------------------------------------

IF (@updateStats = 1)
BEGIN

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] UPDATE DATABASE STATISTICS JOB STARTED ...!';

END
	OPEN backupTablesCursor;
	FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable

	WHILE @@FETCH_STATUS = 0
	BEGIN

		IF (@enableLog = 1)
		BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] UPDATE TABLE STATICTICS :'+@cusrBackupTable;
		END

		SELECT @SQL = 'UPDATE STATISTICS '+@cusrBackupTable;
		EXEC sp_executesql @SQL;

		FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable
	END
	CLOSE backupTablesCursor;

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] UPDATE DATABASE STATISTICS JOB FINISHED ...!';
END

END

deallocate backupTablesCursor;

IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP COMPLETED .... !' AS 'INFO LOG';
END

END
