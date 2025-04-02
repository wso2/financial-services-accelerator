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
-- are represented in BIGINT.
-- Since the database systems does not support adding default unix time to the database columns, the default data
-- storing is handled within the database queries.

CREATE TABLE FS_NOTIFICATION (
    NOTIFICATION_ID varchar2(36) NOT NULL,
    CLIENT_ID varchar2(255) NOT NULL,
    RESOURCE_ID varchar2(255) NOT NULL,
    STATUS varchar2(10) NOT NULL,
     UPDATED_TIMESTAMP BIGINT NOT NULL,
    PRIMARY KEY (NOTIFICATION_ID)
);

CREATE TABLE FS_NOTIFICATION_EVENT (
    EVENT_ID number(10) NOT NULL,
    NOTIFICATION_ID varchar2(36) NOT NULL,
    EVENT_TYPE varchar2(200) NOT NULL,
    EVENT_INFO varchar2(1000) NOT NULL,
    PRIMARY KEY (EVENT_ID),
    CONSTRAINT FK_NotificationEvent FOREIGN KEY (NOTIFICATION_ID) REFERENCES FS_NOTIFICATION(NOTIFICATION_ID)
);

-- Generate ID using sequence and trigger
CREATE SEQUENCE FS_NOTIFICATION_EVENT_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER FS_NOTIFICATION_EVENT_seq_tr
 BEFORE INSERT ON FS_NOTIFICATION_EVENT FOR EACH ROW
 WHEN (NEW.EVENT_ID IS NULL)
BEGIN
 SELECT FS_NOTIFICATION_EVENT_seq.NEXTVAL INTO :NEW.EVENT_ID FROM DUAL;
END;

CREATE TABLE FS_NOTIFICATION_ERROR (
    NOTIFICATION_ID varchar2(36) NOT NULL,
    ERROR_CODE varchar2(255) NOT NULL,
    DESCRIPTION varchar2(255) NOT NULL,
    PRIMARY KEY (NOTIFICATION_ID),
    CONSTRAINT FK_NotificationError FOREIGN KEY (NOTIFICATION_ID) REFERENCES FS_NOTIFICATION(NOTIFICATION_ID)
)

CREATE TABLE FS_NOTIFICATION_SUBSCRIPTION (
    SUBSCRIPTION_ID varchar(36) NOT NULL,
    CLIENT_ID varchar(255) NOT NULL,
    REQUEST JSON NOT NULL,
    CALLBACK_URL varchar(255),
    TIMESTAMP BIGINT NOT NULL,
    SPEC_VERSION varchar(255),
    STATUS varchar(255) NOT NULL,
    PRIMARY KEY (SUBSCRIPTION_ID)
);

CREATE TABLE FS_NOTIFICATION_SUBSCRIBED_EVENTS (
    SUBSCRIPTION_ID varchar(36) NOT NULL,
    EVENT_TYPE varchar(255) NOT NULL,
    PRIMARY KEY (SUBSCRIPTION_ID, EVENT_TYPE),
    CONSTRAINT FK_NotificationSubEvents FOREIGN KEY (SUBSCRIPTION_ID) REFERENCES FS_NOTIFICATION_SUBSCRIPTION(SUBSCRIPTION_ID)
);
