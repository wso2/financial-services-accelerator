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

CREATE OR ALTER PROCEDURE WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP
AS

BEGIN

DECLARE @rowCount INT;
DECLARE @enableLog BIT;
DECLARE @logLevel VARCHAR(10);

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET @enableLog = 'TRUE'; -- ENABLE LOGGING [DEFAULT : TRUE]
SET @logLevel = 'TRACE'; -- SET LOG LEVELS : TRACE



IF (@enableLog = 1) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP STARTED .... !';
END

-- ---------------------

SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('OB_CONSENT');
IF (@rowCount = 1)
BEGIN
IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED ON OB_CONSENT TABLE !';
END
INSERT INTO dbo.OB_CONSENT SELECT A.* FROM dbo.BAK_OB_CONSENT AS A LEFT JOIN dbo.OB_CONSENT AS B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
SELECT @rowCount =  @@rowcount;
IF (@enableLog = 1 ) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT WITH '+CAST(@rowCount as varchar)
END
END

-- ---------------------

SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('OB_CONSENT_ATTRIBUTE');
IF (@rowCount = 1)
BEGIN
IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_ATTRIBUTE TABLE !';
END
INSERT INTO dbo.OB_CONSENT_ATTRIBUTE SELECT A.* FROM dbo.BAK_OB_CONSENT_ATTRIBUTE AS A LEFT JOIN dbo.OB_CONSENT_ATTRIBUTE AS B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
SELECT @rowCount =  @@rowcount;
IF (@enableLog = 1 ) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_ATTRIBUTE WITH '+CAST(@rowCount as varchar)
END
END

-- ---------------------

SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('OB_CONSENT_FILE');
IF (@rowCount = 1)
BEGIN
IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_FILE TABLE !';
END
INSERT INTO dbo.OB_CONSENT_FILE SELECT A.* FROM dbo.BAK_OB_CONSENT_FILE AS A LEFT JOIN dbo.OB_CONSENT_FILE AS B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
SELECT @rowCount =  @@rowcount;
IF (@enableLog = 1 ) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_FILE WITH '+CAST(@rowCount as varchar)
END
END

-- ---------------------

SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('OB_CONSENT_STATUS_AUDIT');
IF (@rowCount = 1)
BEGIN
IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_STATUS_AUDIT TABLE !';
END
INSERT INTO dbo.OB_CONSENT_STATUS_AUDIT SELECT A.* FROM dbo.BAK_OB_CONSENT_STATUS_AUDIT AS A LEFT JOIN dbo.OB_CONSENT_STATUS_AUDIT AS B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
SELECT @rowCount =  @@rowcount;
IF (@enableLog = 1 ) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_STATUS_AUDIT WITH '+CAST(@rowCount as varchar)
END
END

-- ---------------------

SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('OB_CONSENT_AUTH_RESOURCE');
IF (@rowCount = 1)
BEGIN
IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_AUTH_RESOURCE TABLE !';
END
INSERT INTO dbo.OB_CONSENT_AUTH_RESOURCE SELECT A.* FROM dbo.BAK_OB_CONSENT_AUTH_RESOURCE AS A LEFT JOIN dbo.OB_CONSENT_AUTH_RESOURCE AS B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL;
SELECT @rowCount =  @@rowcount;
IF (@enableLog = 1 ) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_AUTH_RESOURCE WITH '+CAST(@rowCount as varchar)
END
END

-- ---------------------

SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('OB_CONSENT_MAPPING');
IF (@rowCount = 1)
BEGIN
IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_MAPPING TABLE !';
END
INSERT INTO dbo.OB_CONSENT_MAPPING SELECT A.* FROM dbo.BAK_OB_CONSENT_MAPPING AS A LEFT JOIN dbo.OB_CONSENT_MAPPING AS B ON A.MAPPING_ID = B.MAPPING_ID WHERE B.MAPPING_ID IS NULL;
SELECT @rowCount =  @@rowcount;
IF (@enableLog = 1 ) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_AUTH_RESOURCE WITH '+CAST(@rowCount as varchar)
END
END

IF (@enableLog = 1) BEGIN
SELECT  '[' + convert(varchar, getdate(), 121) + '] WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP COMPLETED .... !';
END

END
