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

package com.wso2.open.banking.application.info.endpoint.api.constants;

/**
 * MetaDataSQLStatements.
 *
 * <p>This specifies SQL Statements for retrieving all consent id's for consent manager application
 *
 */
public class MetaDataSQLStatements {

    /**
     * SQL query to retrieve list of clientIds.
     * @return
     */
    public String getAllClientIds() {

        return "SELECT DISTINCT CLIENT_ID FROM OB_CONSENT";
    }

}
