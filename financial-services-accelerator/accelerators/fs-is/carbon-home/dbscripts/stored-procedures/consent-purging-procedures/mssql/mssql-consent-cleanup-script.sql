CREATE OR ALTER PROCEDURE WSO2_FS_CONSENT_CLEANUP_SP (
    @consentTypes VARCHAR(1024),
    @clientIds VARCHAR(4096),
    @consentStatuses VARCHAR(1024),
    @purgeConsentsOlderThanXNumberOfDays INT,
    @lastUpdatedTime BIGINT,
    @backupTables BIT,
    @enableAudit BIT,
    @rebuildIndexes BIT,
    @updateStats BIT,
    @enableDataRetention BIT
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

    -- Data retention variables
DECLARE @enableDataRetentionForAuthResourceAndMapping BIT;
DECLARE @enableDataRetentionForFsConsentFile BIT;
DECLARE @enableDataRetentionForFsConsentAttribute BIT;
DECLARE @enableDataRetentionForFsConsentStatusAudit BIT;

DECLARE backupTablesCursor CURSOR FOR
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('FS_CONSENT','FS_CONSENT_AUTH_RESOURCE','FS_CONSENT_MAPPING','FS_CONSENT_FILE','FS_CONSENT_ATTRIBUTE','FS_CONSENT_STATUS_AUDIT')

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET @batchSize = 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
SET @chunkSize = 500000;      -- CHUNK WISE DELETE FOR LARGE TABLES [DEFULT : 500000]
SET @checkCount = 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE CONSENT COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
SET @sleepTime = '00:00:02.000';  -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
SET @enableLog = 'TRUE';       -- ENABLE LOGGING [DEFAULT : FALSE]
SET @logLevel = 'TRACE';    -- SET LOG LEVELS : TRACE , DEBUG

SET @enableDataRetentionForAuthResourceAndMapping = 'TRUE';
SET @enableDataRetentionForFsConsentFile = 'TRUE';
SET @enableDataRetentionForFsConsentAttribute = 'TRUE';
SET @enableDataRetentionForFsConsentStatusAudit = 'TRUE';

IF (@enableDataRetention IS NULL)
BEGIN
SET @enableDataRetention = 'FALSE';    -- SET TRUE FOR ENABLE DATA RETENTION (ARCHIVE PURGED DATA) [DEFAULT : FALSE]
END;

IF (@backupTables IS NULL)
BEGIN
SET @backupTables = 'TRUE';    -- SET IF CONSENT TABLE NEEDS TO BACKUP BEFORE DELETE  [DEFAULT : TRUE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
END;

IF (@enableAudit IS NULL)
BEGIN
SET @enableAudit = 'FALSE'; -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED CONSENT USING A TABLE    [DEFAULT : FALSE] [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
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
-- CONSENT DATA PURGING CONFIGS
-- ------------------------------------------

IF (@consentTypes IS NULL)
BEGIN
SET @consentTypes = '';              -- SET CONSENT_TYPES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'accounts,payments', LEAVE AS EMPTY TO SKIP)
END;

IF (@clientIds IS NULL)
BEGIN
SET @clientIds = '';                 -- SET CLIENT_IDS WHICH SHOULD BE ELIGIBLE FOR PURGING. (LEAVE AS EMPTY TO SKIP)
END;

IF (@consentStatuses IS NULL)
BEGIN
SET @consentStatuses = '';           -- SET CONSENT_STATUSES WHICH SHOULD BE ELIGIBLE FOR PURGING. (Ex : 'expired,revoked', LEAVE AS EMPTY TO SKIP)
END;

IF (@purgeConsentsOlderThanXNumberOfDays IS NULL)
    BEGIN
        SET @olderThanTimePeriodForPurging = 60 * 60 * 24 * 365;  -- SET TIME PERIOD (SECONDS) TO DELETE CONSENTS OLDER THAN N DAYS. (DEFAULT 365 DAYS) (CHECK BELOW FOR FOR INFO.)
    END
ELSE
    BEGIN
        SET @olderThanTimePeriodForPurging = 60 * 60 * 24 * @purgeConsentsOlderThanXNumberOfDays;
    END;

IF (@lastUpdatedTime IS NULL)
BEGIN
SET @lastUpdatedTime = DATEDIFF(SECOND,'1970-01-01', GETUTCDATE()) - @olderThanTimePeriodForPurging;   -- SET LAST_UPDATED_TIME FOR PURGING, (IF CONSENT'S UPDATED TIME IS OLDER THAN THIS VALUE THEN IT'S ELIGIBLE FOR PURGING, CHECK BELOW FOR FOR INFO.)
END;

-- HERE IF WE WISH TO PURGE CONSENTS WITH LAST UPDATED_TIME OLDER THAN 31 DAYS (1 MONTH), WE CAN CONFIGURE olderThanTimePeriodForPurging = 60 * 60 * 24 * 31
-- THIS VALUE IS IN SECONDS (60 (1 MINUTE) * 60 (1 HOUR) * 24 (24 HOURS = 1 DAY) * 31 (31 DAYS = 1 MONTH))
-- OR ELSE WE CAN SET THE INPUT PARAMETER purgeConsentsOlderThanXNumberOfDays_in = 31 , FOR PURGE CONSENTS WITH LAST UPDATED_TIME OLDER THAN 31 DAYS.
-- IF WE WISH TO CONFIGURE EXACT TIMESTAMP OF THE LAST UPDATED_TIME RATHER THAN A TIME PERIOD, WE CAN IGNORE CONFIGURING olderThanTimePeriodForPurging, purgeConsentsOlderThanXNumberOfDays_in
-- AND ONLY CONFIGURE lastUpdatedTime WITH EXACT UNIX TIMESTAMP.
-- EX : `SET lastUpdatedTime = 1660737878;`


IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_FS_CONSENT_CLEANUP_SP STARTED ... !' AS 'INFO LOG';
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
-- CREATING AUDIT TABLES FOR CONSENT DELETION FOR THE FIRST TIME RUN
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@enableAudit = 1)
BEGIN
	IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AUDITLOG_FS_CONSENT_CLEANUP'))
	BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING AUDIT TABLE AUDITLOG_FS_CONSENT_CLEANUP .. !';
			END
			Select * into dbo.AUDITLOG_FS_CONSENT_CLEANUP  from  dbo.FS_CONSENT where 1 =2;
			ALTER TABLE AUDITLOG_FS_CONSENT_CLEANUP ADD AUDIT_TIMESTAMP datetime DEFAULT CURRENT_TIMESTAMP;
	END
	ELSE
	BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] USING AUDIT TABLE AUDITLOG_FS_CONSENT_CLEANUP ..!';
			END
	END
END

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING RETENTION TABLES IF NOT EXISTS
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@enableDataRetention = 1)
BEGIN
    IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_FS_CONSENT'))
    BEGIN
        IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING RETENTION TABLE RET_FS_CONSENT .. !';
        END
        Select * into dbo.RET_FS_CONSENT  from  dbo.FS_CONSENT where 1 =2;
    END
    
    IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_FS_CONSENT_AUTH_RESOURCE'))
    BEGIN
        IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING RETENTION TABLE RET_FS_CONSENT_AUTH_RESOURCE .. !';
        END
        Select * into dbo.RET_FS_CONSENT_AUTH_RESOURCE  from  dbo.FS_CONSENT_AUTH_RESOURCE where 1 =2;
    END
    
    IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_FS_CONSENT_MAPPING'))
    BEGIN
        IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING RETENTION TABLE RET_FS_CONSENT_MAPPING .. !';
        END
        Select * into dbo.RET_FS_CONSENT_MAPPING  from  dbo.FS_CONSENT_MAPPING where 1 =2;
    END
    
    IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_FS_CONSENT_FILE'))
    BEGIN
        IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING RETENTION TABLE RET_FS_CONSENT_FILE .. !';
        END
        Select * into dbo.RET_FS_CONSENT_FILE  from  dbo.FS_CONSENT_FILE where 1 =2;
    END
    
    IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_FS_CONSENT_ATTRIBUTE'))
    BEGIN
        IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING RETENTION TABLE RET_FS_CONSENT_ATTRIBUTE .. !';
        END
        Select * into dbo.RET_FS_CONSENT_ATTRIBUTE  from  dbo.FS_CONSENT_ATTRIBUTE where 1 =2;
    END
    
    IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'RET_FS_CONSENT_STATUS_AUDIT'))
    BEGIN
        IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING RETENTION TABLE RET_FS_CONSENT_STATUS_AUDIT .. !';
        END
        Select * into dbo.RET_FS_CONSENT_STATUS_AUDIT  from  dbo.FS_CONSENT_STATUS_AUDIT where 1 =2;
    END
END

---- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
---- CALCULATING CONSENTS IN FS_CONSENT TABLE
---- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@enableLog = 1)
BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] CALCULATING CONSENTS IN FS_CONSENT TABLE .... !';

		IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
		BEGIN
		SELECT @rowcount = COUNT(1) FROM FS_CONSENT;
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL CONSENTS ON FS_CONSENT TABLE BEFORE DELETE :'+CAST(@rowCount as varchar);
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		SELECT @cleaupCount = COUNT(1) FROM FS_CONSENT WHERE (@consentStatuses = '' OR CHARINDEX( ',' + LOWER(CURRENT_STATUS) + ',', LOWER(',' + @consentStatuses + ',')) > 0) AND
            (@consentTypes = '' OR CHARINDEX(',' + LOWER(CONSENT_TYPE) + ',', LOWER(',' + @consentTypes + ',')) > 0) AND
            (@clientIds = '' OR CHARINDEX(',' + LOWER(CLIENT_ID) + ',', LOWER(',' + @clientIds + ',')) > 0) AND UPDATED_TIME < @lastUpdatedTime;
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL CONSENTS SHOULD BE DELETED FROM FS_CONSENT : '+ CAST(@cleaupCount as varchar);
        SELECT '[' + convert(varchar, getdate(), 121) + '] NOTE: ACTUAL DELETION WILL HAPPEN ONLY WHEN DELETE COUNT IS LARGER THAN CHECKCOUNT .... !';
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		select @rowcount  = (@rowcount - @cleaupCount);
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL CONSENTS SHOULD BE RETAIN IN FS_CONSENT : '+CAST(@rowCount as varchar);
		END
END

---- ------------------------------------------------------
---- BATCH DELETE CONSENT DATA
---- ------------------------------------------------------

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] CONSENT PURGING STARTED .... !';
END


WHILE (1=1)
BEGIN
		IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHUNK_FS_CONSENT'))
		BEGIN
		DROP TABLE CHUNK_FS_CONSENT;
		END

		CREATE TABLE CHUNK_FS_CONSENT (CONSENT_ID VARCHAR (255),CONSTRAINT CHUNK_FS_CONSENT_PRI PRIMARY KEY (CONSENT_ID));

        INSERT INTO CHUNK_FS_CONSENT (CONSENT_ID) SELECT TOP (@chunkSize) CONSENT_ID FROM FS_CONSENT WHERE
                (@consentStatuses = '' OR CHARINDEX(',' + LOWER(CURRENT_STATUS) + ',', LOWER(',' + @consentStatuses + ',')) > 0) AND
                (@consentTypes = '' OR  CHARINDEX(',' + LOWER(CONSENT_TYPE) + ',', LOWER(',' + @consentTypes + ',')) > 0) AND
                (@clientIds = '' OR CHARINDEX(',' + LOWER(CLIENT_ID) + ',', LOWER(',' + @clientIds + ',')) > 0) AND UPDATED_TIME < @lastUpdatedTime;
        SELECT @chunkCount =  @@rowcount;

		IF (@chunkCount < @checkCount)
		BEGIN
		BREAK;
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] CHUNK TABLE CHUNK_FS_CONSENT CREATED WITH : '+CAST(@chunkCount as varchar);
		END

		IF (@enableAudit=1)
		BEGIN
		INSERT INTO dbo.AUDITLOG_FS_CONSENT_CLEANUP SELECT FSC.*, CURRENT_TIMESTAMP FROM FS_CONSENT FSC , CHUNK_FS_CONSENT CHK WHERE FSC.CONSENT_ID=CHK.CONSENT_ID;
		END

		WHILE (1=1)
		BEGIN
			IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BATCH_FS_CONSENT'))
			BEGIN
			DROP TABLE BATCH_FS_CONSENT;
			END

			CREATE TABLE BATCH_FS_CONSENT (CONSENT_ID VARCHAR (255),CONSTRAINT BATCH_FS_CONSENT_PRI PRIMARY KEY (CONSENT_ID));

			INSERT INTO BATCH_FS_CONSENT (CONSENT_ID) SELECT TOP (@batchSize) CONSENT_ID FROM CHUNK_FS_CONSENT ;
			SELECT @batchCount =  @@rowcount;

			IF(@batchCount = 0)
			BEGIN
			BREAK;
			END

			IF ((@batchCount > 0))
			BEGIN

				IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE START ON CONSENT TABLES WITH BATCH COUNT : '+CAST(@batchCount as varchar);
				END

                IF (@enableDataRetention = 1)
                BEGIN
                    IF (@enableLog = 1)
                    BEGIN
                    SELECT '[' + convert(varchar, getdate(), 121) + '] INSERTING FS_CONSENT DATA TO RET_FS_CONSENT TABLE !';
                    END
                    INSERT INTO dbo.RET_FS_CONSENT SELECT * FROM FS_CONSENT where CONSENT_ID in (select CONSENT_ID from BATCH_FS_CONSENT);

                    -- STORE FS_CONSENT_AUTH_RESOURCE AND FS_CONSENT_MAPPING RETENTION DATA IF ENABLED.
                    IF (@enableDataRetentionForAuthResourceAndMapping = 1)
                    BEGIN
                        IF (@enableLog = 1)
                        BEGIN
                        SELECT '[' + convert(varchar, getdate(), 121) + '] INSERTING FS_CONSENT_AUTH_RESOURCE DATA TO RET_FS_CONSENT_AUTH_RESOURCE TABLE !';
                        END
                        INSERT INTO dbo.RET_FS_CONSENT_AUTH_RESOURCE SELECT * FROM FS_CONSENT_AUTH_RESOURCE where CONSENT_ID in (select CONSENT_ID from  BATCH_FS_CONSENT);
                        
                        IF (@enableLog = 1)
                        BEGIN
                        SELECT '[' + convert(varchar, getdate(), 121) + '] INSERTING FS_CONSENT_MAPPING DATA TO RET_FS_CONSENT_MAPPING TABLE !';
                        END
                        INSERT INTO dbo.RET_FS_CONSENT_MAPPING SELECT * FROM FS_CONSENT_MAPPING WHERE MAPPING_ID IN ( SELECT MAPPING_ID FROM FS_CONSENT_MAPPING FSCM
                                                                                         INNER JOIN FS_CONSENT_AUTH_RESOURCE FSAR ON FSCM.AUTH_ID = FSAR.AUTH_ID
                                                                                         INNER JOIN BATCH_FS_CONSENT B ON FSAR.CONSENT_ID = B.CONSENT_ID);
                    END
                    
                    -- STORE FS_CONSENT_STATUS_AUDIT RETENTION DATA IF ENABLED.
                    IF (@enableDataRetentionForFsConsentStatusAudit = 1)
                    BEGIN
                        IF (@enableLog = 1)
                        BEGIN
                        SELECT '[' + convert(varchar, getdate(), 121) + '] INSERTING FS_CONSENT_STATUS_AUDIT DATA TO RET_FS_CONSENT_STATUS_AUDIT TABLE !';
                        END
                        INSERT INTO dbo.RET_FS_CONSENT_STATUS_AUDIT SELECT * FROM FS_CONSENT_STATUS_AUDIT where CONSENT_ID in (select CONSENT_ID from  BATCH_FS_CONSENT);
                    END
                    
                    -- STORE FS_CONSENT_FILE RETENTION DATA IF ENABLED.
                    IF (@enableDataRetentionForFsConsentFile = 1)
                    BEGIN
                        IF (@enableLog = 1)
                        BEGIN
                        SELECT '[' + convert(varchar, getdate(), 121) + '] INSERTING FS_CONSENT_FILE DATA TO RET_FS_CONSENT_FILE TABLE !';
                        END
                        INSERT INTO dbo.RET_FS_CONSENT_FILE SELECT * FROM FS_CONSENT_FILE where CONSENT_ID in (select CONSENT_ID from  BATCH_FS_CONSENT);
                    END
                    
                    -- STORE FS_CONSENT_ATTRIBUTE RETENTION DATA IF ENABLED.
                    IF (@enableDataRetentionForFsConsentAttribute = 1)
                    BEGIN
                        IF (@enableLog = 1)
                        BEGIN
                        SELECT '[' + convert(varchar, getdate(), 121) + '] INSERTING FS_CONSENT_ATTRIBUTE DATA TO RET_FS_CONSENT_ATTRIBUTE TABLE !';
                        END
                        INSERT INTO dbo.RET_FS_CONSENT_ATTRIBUTE SELECT * FROM FS_CONSENT_ATTRIBUTE where CONSENT_ID in (select CONSENT_ID from  BATCH_FS_CONSENT);
                    END
                END


                -- ------------------------------------------------------
                -- BATCH DELETE FS_CONSENT_ATTRIBUTE
                -- ------------------------------------------------------

				DELETE FS_CONSENT_ATTRIBUTE where CONSENT_ID in (select CONSENT_ID from  BATCH_FS_CONSENT);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON FS_CONSENT_ATTRIBUTE WITH : '+CAST(@deleteCount as varchar);
				END


                -- ------------------------------------------------------
                -- BATCH DELETE FS_CONSENT_FILE
                -- ------------------------------------------------------

				DELETE FS_CONSENT_FILE where CONSENT_ID in (select CONSENT_ID from  BATCH_FS_CONSENT);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON FS_CONSENT_FILE WITH : '+CAST(@deleteCount as varchar);
				END


                -- ------------------------------------------------------
                -- BATCH DELETE FS_CONSENT_STATUS_AUDIT
                -- ------------------------------------------------------

				DELETE FS_CONSENT_STATUS_AUDIT where CONSENT_ID in (select CONSENT_ID from BATCH_FS_CONSENT);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON FS_CONSENT_STATUS_AUDIT WITH : '+CAST(@deleteCount as varchar);
				END


                -- ------------------------------------------------------
                -- BATCH DELETE FS_CONSENT_MAPPING
                -- ------------------------------------------------------

				DELETE FS_CONSENT_MAPPING WHERE MAPPING_ID IN ( SELECT MAPPING_ID FROM FS_CONSENT_MAPPING FSCM
                                                                     INNER JOIN FS_CONSENT_AUTH_RESOURCE FSAR ON FSCM.AUTH_ID = FSAR.AUTH_ID
                                                                     INNER JOIN BATCH_FS_CONSENT B ON FSAR.CONSENT_ID = B.CONSENT_ID);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON FS_CONSENT_MAPPING WITH : '+CAST(@deleteCount as varchar);
				END


                -- ------------------------------------------------------
                -- BATCH DELETE FS_CONSENT_AUTH_RESOURCE
                -- ------------------------------------------------------

				DELETE FS_CONSENT_AUTH_RESOURCE where CONSENT_ID in (select CONSENT_ID from  BATCH_FS_CONSENT);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON FS_CONSENT_AUTH_RESOURCE WITH : '+CAST(@deleteCount as varchar);
				END


                -- ------------------------------------------------------
                -- BATCH DELETE FS_CONSENT
                -- ------------------------------------------------------

				DELETE FS_CONSENT where CONSENT_ID in (select CONSENT_ID from  BATCH_FS_CONSENT);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON FS_CONSENT WITH : '+CAST(@deleteCount as varchar);
				END

				DELETE CHUNK_FS_CONSENT WHERE CONSENT_ID in (select CONSENT_ID from BATCH_FS_CONSENT);

				IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED BATCH ON CHUNK_FS_CONSENT !';
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
SELECT '[' + convert(varchar, getdate(), 121) + '] CONSENTS DELETE ON TABLES COMPLETED .... !';
END

IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
BEGIN
SELECT @rowcount = COUNT(1) FROM FS_CONSENT;
SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL CONSENTS ON FS_CONSENT TABLE AFTER DELETE :'+CAST(@rowCount as varchar);
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
SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_FS_CONSENT_CLEANUP_SP COMPLETED .... !' AS 'INFO LOG';
END

END
