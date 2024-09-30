/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
-- All the data related to time are stored in unix time stamp and therefore, the data types for the time related data
-- are represented in LONGINTEGER.
-- Since the database systems does not support adding default unix time to the database columns, the default data
-- storing is handled within the database queries.

CREATE TABLE FS_CONSENT (
  CONSENT_ID            VARCHAR(255) NOT NULL,
  RECEIPT               CLOB NOT NULL,
  CREATED_TIME          NUMBER NOT NULL,
  UPDATED_TIME          NUMBER NOT NULL,
  CLIENT_ID             VARCHAR(255) NOT NULL,
  CONSENT_TYPE          VARCHAR(64) NOT NULL,
  CURRENT_STATUS        VARCHAR(64) NOT NULL,
  CONSENT_FREQUENCY     INT,
  VALIDITY_TIME         NUMBER,
  RECURRING_INDICATOR   NUMBER(1) DEFAULT 0,
  PRIMARY KEY (CONSENT_ID)
);

CREATE TABLE FS_CONSENT_AUTH_RESOURCE (
  AUTH_ID           VARCHAR(255) NOT NULL,
  CONSENT_ID        VARCHAR(255) NOT NULL,
  AUTH_TYPE         VARCHAR(255) NOT NULL,
  USER_ID           VARCHAR(255),
  AUTH_STATUS       VARCHAR(255) NOT NULL,
  UPDATED_TIME      NUMBER NOT NULL,
  PRIMARY KEY(AUTH_ID),
  CONSTRAINT FK_FS_CONSENT_AUTH_RESOURCE FOREIGN KEY (CONSENT_ID) REFERENCES FS_CONSENT (CONSENT_ID)
);

CREATE TABLE FS_CONSENT_MAPPING (
  MAPPING_ID        VARCHAR(255) NOT NULL,
  AUTH_ID           VARCHAR(255) NOT NULL,
  ACCOUNT_ID        VARCHAR(255) NOT NULL,
  PERMISSION        VARCHAR(255) NOT NULL,
  MAPPING_STATUS    VARCHAR(255) NOT NULL,
  PRIMARY KEY(MAPPING_ID),
  CONSTRAINT FK_FS_CONSENT_MAPPING FOREIGN KEY (AUTH_ID) REFERENCES FS_CONSENT_AUTH_RESOURCE (AUTH_ID)
);

CREATE TABLE FS_CONSENT_STATUS_AUDIT (
  STATUS_AUDIT_ID   VARCHAR(255) NOT NULL,
  CONSENT_ID        VARCHAR(255) NOT NULL,
  CURRENT_STATUS    VARCHAR(255) NOT NULL,
  ACTION_TIME       NUMBER NOT NULL,
  REASON            VARCHAR(255),
  ACTION_BY         VARCHAR(255),
  PREVIOUS_STATUS   VARCHAR(255),
  PRIMARY KEY(STATUS_AUDIT_ID),
  CONSTRAINT FK_FS_CONSENT_STATUS_AUDIT FOREIGN KEY (CONSENT_ID) REFERENCES FS_CONSENT (CONSENT_ID)
);

CREATE TABLE FS_CONSENT_FILE (
  CONSENT_ID        VARCHAR(255) NOT NULL,
  CONSENT_FILE      CLOB,
  PRIMARY KEY(CONSENT_ID),
  CONSTRAINT FK_FS_CONSENT_FILE FOREIGN KEY (CONSENT_ID) REFERENCES FS_CONSENT (CONSENT_ID)
);

CREATE TABLE FS_CONSENT_ATTRIBUTE (
  CONSENT_ID      VARCHAR(255) NOT NULL,
  ATT_KEY         VARCHAR(255) NOT NULL,
  ATT_VALUE       VARCHAR(1023) NOT NULL,
  PRIMARY KEY(CONSENT_ID, ATT_KEY),
  CONSTRAINT FK_FS_CONSENT_ATTRIBUTE FOREIGN KEY (CONSENT_ID) REFERENCES FS_CONSENT (CONSENT_ID) ON DELETE CASCADE
);
