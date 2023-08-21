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
 CREATE OR REPLACE FUNCTION WSO2_OB_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP() RETURNS void AS $$
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
RAISE NOTICE 'WSO2_OB_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP STARTED .... !';
RAISE NOTICE '';
END IF;


-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('ob_notification');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON ob_notification TABLE !';
END IF;
INSERT INTO ob_notification SELECT A.* FROM bak_ob_notification A LEFT JOIN ob_notification B ON A.NOTIFICATION_ID = B.NOTIFICATION_ID WHERE B.NOTIFICATION_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON ob_notification WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('ob_notification_event');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON ob_notification_event TABLE !';
END IF;
INSERT INTO ob_notification_event SELECT A.* FROM bak_ob_notification_event A LEFT JOIN ob_notification_event B ON A.NOTIFICATION_ID = B.NOTIFICATION_ID WHERE B.NOTIFICATION_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON ob_notification_event WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM PG_CATALOG.PG_TABLES WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME IN ('ob_notification_error');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED ON ob_notification_error TABLE !';
END IF;
INSERT INTO ob_notification_error SELECT A.* FROM bak_ob_notification_error A LEFT JOIN ob_notification_error B ON A.NOTIFICATION_ID = B.NOTIFICATION_ID WHERE B.NOTIFICATION_ID IS NULL;
GET DIAGNOSTICS rowcount := ROW_COUNT;
IF (enableLog ) THEN
RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON ob_notification_error WITH %',ROWCOUNT;
END IF;
END IF;

-- ---------------------

IF (enableLog) THEN
RAISE NOTICE 'WSO2_OB_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP COMPLETED .... !';
RAISE NOTICE '';
END IF;

END;
$$
LANGUAGE 'plpgsql';
