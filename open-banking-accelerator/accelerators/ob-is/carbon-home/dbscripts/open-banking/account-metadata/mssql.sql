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

--For account metadata feature run the following queries against the openbank_openbankingdb--
CREATE TABLE IF NOT EXISTS OB_ACCOUNT_METADATA (
    ACCOUNT_ID VARCHAR(100) NOT NULL,
    USER_ID VARCHAR(100) NOT NULL,
    METADATA_KEY VARCHAR(100) NOT NULL,
    METADATA_VALUE VARCHAR(100) NOT NULL,
    LAST_UPDATE_TIMESTAMP DATETIME NOT NULL DEFAULT getdate(),
    PRIMARY KEY (USER_ID,ACCOUNT_ID,METADATA_KEY)
);
