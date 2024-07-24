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

package com.wso2.openbanking.accelerator.consent.mgt.dao.queries;

import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.apache.commons.lang.StringUtils;

/**
 * The common database queries used by the consent management DAO layer.
 */
public class ConsentMgtCommonDBQueries {

    public String getStoreConsentPreparedStatement() {

        return "INSERT INTO OB_CONSENT (CONSENT_ID, RECEIPT, CREATED_TIME, UPDATED_TIME, CLIENT_ID, CONSENT_TYPE, " +
                "CURRENT_STATUS, CONSENT_FREQUENCY, VALIDITY_TIME, RECURRING_INDICATOR) VALUES (?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?)";
    }

    public String getStoreAuthorizationPreparedStatement() {

        return "INSERT INTO OB_CONSENT_AUTH_RESOURCE (AUTH_ID, CONSENT_ID, AUTH_TYPE, USER_ID, AUTH_STATUS, " +
                "UPDATED_TIME) VALUES (?, ?, ?, ?, ?, ?)";
    }

    public String getStoreConsentMappingPreparedStatement() {

        return "INSERT INTO OB_CONSENT_MAPPING (MAPPING_ID, AUTH_ID, ACCOUNT_ID, PERMISSION, MAPPING_STATUS) VALUES " +
                "(?, ?, ?, ?, ?)";
    }

    public String getStoreConsentStatusAuditRecordPreparedStatement() {

        return "INSERT INTO OB_CONSENT_STATUS_AUDIT (STATUS_AUDIT_ID, CONSENT_ID, CURRENT_STATUS, ACTION_TIME, " +
                "REASON, ACTION_BY, PREVIOUS_STATUS) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    public String getStoreConsentAttributesPreparedStatement() {

        return "INSERT INTO OB_CONSENT_ATTRIBUTE (CONSENT_ID, ATT_KEY, ATT_VALUE) VALUES (?, ?, ?)";
    }

    public String getStoreConsentFilePreparedStatement() {

        return "INSERT INTO OB_CONSENT_FILE (CONSENT_ID, CONSENT_FILE) VALUES (?, ?)";
    }

    public String getUpdateConsentStatusPreparedStatement() {

        return "UPDATE OB_CONSENT SET CURRENT_STATUS = ?, UPDATED_TIME = ? WHERE CONSENT_ID = ?";
    }

    public String getUpdateConsentMappingStatusPreparedStatement() {

        return "UPDATE OB_CONSENT_MAPPING SET MAPPING_STATUS = ? WHERE MAPPING_ID = ?";
    }

    public String getUpdateConsentMappingPermissionPreparedStatement() {

        return "UPDATE OB_CONSENT_MAPPING SET PERMISSION = ? WHERE MAPPING_ID = ?";
    }

    public String getUpdateAuthorizationStatusPreparedStatement() {

        return "UPDATE OB_CONSENT_AUTH_RESOURCE SET AUTH_STATUS = ?, UPDATED_TIME = ? WHERE AUTH_ID = ?";
    }

    public String getUpdateAuthorizationUserPreparedStatement() {

        return "UPDATE OB_CONSENT_AUTH_RESOURCE SET USER_ID = ?, UPDATED_TIME = ? WHERE AUTH_ID = ?";
    }

    public String getGetConsentFileResourcePreparedStatement(boolean fetchFromRetentionTables) {

        // table prefix is to fetch from the consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (fetchFromRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "SELECT * FROM " + tablePrefix + "OB_CONSENT_FILE WHERE CONSENT_ID = ?";
    }

    public String getGetConsentAttributesPreparedStatement() {

        return "SELECT * FROM OB_CONSENT_ATTRIBUTE WHERE CONSENT_ID = ?";
    }

    public String getGetConsentPreparedStatement() {

        return "SELECT * FROM OB_CONSENT WHERE CONSENT_ID = ?";
    }

    public String getGetDetailedConsentPreparedStatement(boolean fetchFromRetentionTables) {

        // table prefix is to fetch from the consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (fetchFromRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "SELECT obc.CONSENT_ID," +
                "RECEIPT, " +
                "CLIENT_ID, " +
                "CONSENT_TYPE, " +
                "CURRENT_STATUS, " +
                "CONSENT_FREQUENCY, " +
                "VALIDITY_TIME, " +
                "RECURRING_INDICATOR, " +
                "CREATED_TIME AS CONSENT_CREATED_TIME, " +
                "obc.UPDATED_TIME AS CONSENT_UPDATED_TIME, " +
                "ca.ATT_KEY, " +
                "ca.ATT_VALUE, " +
                "ocar.AUTH_ID, " +
                "ocar.AUTH_STATUS, " +
                "ocar.AUTH_TYPE, " +
                "ocar.UPDATED_TIME AS AUTH_UPDATED_TIME, " +
                "ocar.USER_ID, " +
                "cm.ACCOUNT_ID, " +
                "cm.MAPPING_ID, " +
                "cm.MAPPING_STATUS, " +
                "cm.PERMISSION " +
                "FROM " +
                tablePrefix + "OB_CONSENT obc " +
                "LEFT JOIN " +
                tablePrefix + "OB_CONSENT_ATTRIBUTE ca ON obc.CONSENT_ID=ca.CONSENT_ID " +
                "LEFT JOIN " +
                tablePrefix + "OB_CONSENT_AUTH_RESOURCE ocar ON obc.CONSENT_ID=ocar.CONSENT_ID " +
                "LEFT JOIN " +
                tablePrefix + "OB_CONSENT_MAPPING cm ON ocar.AUTH_ID=cm.AUTH_ID " +
                "WHERE obc.CONSENT_ID = ?";
    }

    public String getSearchConsentsPreparedStatement(String whereClause, boolean shouldLimit, boolean shouldOffset,
                                                     String userIdFilterClause) {

        String selectClause = "(SELECT * FROM OB_CONSENT " + whereClause + ")";
        String joinType = "LEFT ";
        if (StringUtils.isNotEmpty(userIdFilterClause)) {
            joinType = "INNER ";
            userIdFilterClause = "AND " + userIdFilterClause;
        }

        StringBuilder query = new StringBuilder("SELECT OBC.CONSENT_ID, " +
                "RECEIPT, " +
                "CLIENT_ID, " +
                "CONSENT_TYPE, " +
                "OBC.CURRENT_STATUS AS CURRENT_STATUS," +
                "CONSENT_FREQUENCY," +
                "VALIDITY_TIME," +
                "RECURRING_INDICATOR," +
                "OBC.CREATED_TIME AS CONSENT_CREATED_TIME," +
                "OBC.UPDATED_TIME AS CONSENT_UPDATED_TIME," +
                "Group_concat(distinct CA.att_key order by CA.att_key SEPARATOR '||' ) AS ATT_KEY, " +
                "Group_concat(distinct CA.att_value order by CA.att_key SEPARATOR '||') AS ATT_VALUE, " +


                "( SELECT   Group_concat( OCAR2.auth_id order by OCAR2.auth_id SEPARATOR '||') " +
                "         FROM OB_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS AUTH_ID, " +

                "( SELECT   Group_concat(OCAR2.auth_status order by OCAR2.auth_id SEPARATOR '||') " +
                "         FROM OB_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS AUTH_STATUS, " +

                "( SELECT   Group_concat(OCAR2.auth_type order by OCAR2.auth_id SEPARATOR '||')  " +
                "         FROM OB_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS AUTH_TYPE, " +

                "( SELECT   Group_concat(OCAR2.updated_time order by OCAR2.auth_id SEPARATOR '||')  " +
                "         FROM OB_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS UPDATED_TIME, " +

                "( SELECT   Group_concat(OCAR2.user_id order by OCAR2.auth_id SEPARATOR '||') " +
                "         FROM OB_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS USER_ID," +


                " ( SELECT   Group_concat(OCM2.auth_id order by OCM2.mapping_id SEPARATOR '||') " +
                "           FROM OB_CONSENT_MAPPING OCM2 " +
                "           JOIN OB_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "           WHERE OCAR2.consent_id = OBC.consent_id) AS AUTH_MAPPING_ID  , " +

                "( SELECT   Group_concat(OCM2.account_id order by OCM2.mapping_id SEPARATOR '||') " +
                "           FROM OB_CONSENT_MAPPING OCM2 " +
                "           JOIN OB_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "           WHERE OCAR2.consent_id = OBC.consent_id) AS ACCOUNT_ID  , " +

                "( SELECT   Group_concat(OCM2.mapping_id order by OCM2.mapping_id SEPARATOR '||')  " +
                "           FROM OB_CONSENT_MAPPING OCM2 " +
                "           JOIN OB_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "           WHERE OCAR2.consent_id = OBC.consent_id) AS MAPPING_ID  , " +

                "( SELECT   Group_concat(OCM2.mapping_status order by OCM2.mapping_id SEPARATOR '||') " +
                "           FROM OB_CONSENT_MAPPING OCM2 " +
                "           JOIN OB_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "           WHERE OCAR2.consent_id = OBC.consent_id) AS MAPPING_STATUS , " +

                "( SELECT   Group_concat(OCM2.permission order by OCM2.mapping_id SEPARATOR '||') " +
                "           FROM OB_CONSENT_MAPPING OCM2 " +
                "           JOIN OB_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "           WHERE OCAR2.consent_id = OBC.consent_id) AS PERMISSION " +

                "FROM " +
                selectClause +
                "AS OBC " +
                "LEFT JOIN OB_CONSENT_ATTRIBUTE CA ON OBC.CONSENT_ID=CA.CONSENT_ID " +
                joinType + "JOIN OB_CONSENT_AUTH_RESOURCE OCAR ON OBC.CONSENT_ID=OCAR.CONSENT_ID "
                + userIdFilterClause +
                "LEFT JOIN OB_CONSENT_MAPPING OCM ON OCAR.AUTH_ID=OCM.AUTH_ID WHERE " +
                "(OBC.UPDATED_TIME >= COALESCE(?, OBC.UPDATED_TIME) " +
                "AND OBC.UPDATED_TIME <= COALESCE(?, OBC.UPDATED_TIME)) " +
                "group by OBC.CONSENT_ID ORDER BY OBC.UPDATED_TIME DESC ");

        if (shouldLimit && shouldOffset) {
            query.append(" LIMIT ? OFFSET ? ");
        } else if (shouldLimit) {
            query.append(" LIMIT ? ");
        }

        return query.toString();
    }

    public String getGetConsentWithConsentAttributesPreparedStatement() {

        return "SELECT OB_CONSENT.CONSENT_ID, RECEIPT, CREATED_TIME, UPDATED_TIME, CLIENT_ID, CONSENT_TYPE, " +
                "CURRENT_STATUS, CONSENT_FREQUENCY, VALIDITY_TIME, RECURRING_INDICATOR, " +
                "OB_CONSENT_ATTRIBUTE.ATT_KEY, OB_CONSENT_ATTRIBUTE.ATT_VALUE FROM OB_CONSENT RIGHT JOIN " +
                "OB_CONSENT_ATTRIBUTE ON OB_CONSENT.CONSENT_ID = OB_CONSENT_ATTRIBUTE.CONSENT_ID WHERE OB_CONSENT" +
                ".CONSENT_ID = ?";
    }

    public String getGetConsentAttributesByNamePreparedStatement() {

        return "SELECT CONSENT_ID, ATT_VALUE FROM OB_CONSENT_ATTRIBUTE WHERE ATT_KEY = ?";
    }

    public String getConsentIdByConsentAttributeNameAndValuePreparedStatement() {


        return "SELECT CONSENT_ID FROM OB_CONSENT_ATTRIBUTE WHERE ATT_KEY = ? AND ATT_VALUE = ?";
    }

    public String getGetAuthorizationResourcePreparedStatement() {

        return "SELECT * FROM OB_CONSENT_AUTH_RESOURCE WHERE AUTH_ID = ?";
    }

    public String getGetConsentMappingResourcesPreparedStatement() {

        return "SELECT * FROM OB_CONSENT_MAPPING WHERE AUTH_ID = ?";
    }

    public String getGetConsentMappingResourcesForStatusPreparedStatement() {

        return "SELECT * FROM OB_CONSENT_MAPPING WHERE AUTH_ID = ? AND MAPPING_STATUS = ?";
    }

    public String getDeleteConsentAttributePreparedStatement() {

        return "DELETE FROM OB_CONSENT_ATTRIBUTE WHERE CONSENT_ID = ? AND ATT_KEY = ?";
    }

    public String getGetConsentStatusAuditRecordsPreparedStatement(boolean fetchFromRetentionTables) {

        // table prefix is to fetch from the consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (fetchFromRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "SELECT * FROM " + tablePrefix + "OB_CONSENT_STATUS_AUDIT WHERE CONSENT_ID = COALESCE(?, CONSENT_ID) " +
                "AND CURRENT_STATUS = COALESCE(?, CURRENT_STATUS) AND ACTION_BY = COALESCE(?, ACTION_BY) " +
                "AND STATUS_AUDIT_ID = COALESCE (?, STATUS_AUDIT_ID) AND ACTION_TIME >= COALESCE(?, ACTION_TIME) " +
                "AND ACTION_TIME <= COALESCE(?, ACTION_TIME)";
    }

    public String getSearchAuthorizationResourcesPreparedStatement(String whereClause) {

        return "SELECT * FROM OB_CONSENT_AUTH_RESOURCE" + whereClause;
    }

    public String getUpdateConsentReceiptPreparedStatement() {

        return "UPDATE OB_CONSENT SET RECEIPT = ? WHERE CONSENT_ID = ?";
    }

    public String getUpdateConsentValidityTimePreparedStatement() {

        return "UPDATE OB_CONSENT SET VALIDITY_TIME = ?, UPDATED_TIME = ? WHERE CONSENT_ID = ?";
    }

    public String getSearchExpiringConsentPreparedStatement(String statusesEligibleForExpirationCondition) {

        return "SELECT OBC.CONSENT_ID " +
                " FROM   OB_CONSENT_ATTRIBUTE CA " +
                " JOIN   OB_CONSENT OBC " +
                " ON     CA.CONSENT_ID = OBC.CONSENT_ID " +
                " WHERE  CA.ATT_KEY = ? AND OBC.CURRENT_STATUS IN " + statusesEligibleForExpirationCondition;
    }

    public String getInsertConsentHistoryPreparedStatement() {

        return "INSERT INTO OB_CONSENT_HISTORY (TABLE_ID, RECORD_ID, HISTORY_ID, CHANGED_VALUES, " +
                "REASON, EFFECTIVE_TIMESTAMP) VALUES (?, ?, ?, ?, ?, ?)";
    }

    public String getGetConsentHistoryPreparedStatement(String whereClause) {

        return "SELECT * FROM OB_CONSENT_HISTORY " + whereClause + "ORDER BY EFFECTIVE_TIMESTAMP DESC";
    }

    /**
     * SQL query for delete consent attributes.
     * @param executeOnRetentionTables  whether to execute on retention tables
     * @return SQL query for delete consent attributes
     */
    public String getDeleteConsentAttributeByConsentIdPreparedStatement(boolean executeOnRetentionTables) {

        // table prefix is to execute on consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (executeOnRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "DELETE FROM " + tablePrefix + "OB_CONSENT_ATTRIBUTE WHERE CONSENT_ID = ?";
    }

    /**
     * SQL query for delete consent file.
     * @param executeOnRetentionTables whether to execute on retention tables
     * @return SQL query for delete consent file
     */
    public String getDeleteConsentFileResourcePreparedStatement(boolean executeOnRetentionTables) {

        // table prefix is to execute on consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (executeOnRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "DELETE FROM " + tablePrefix + "OB_CONSENT_FILE WHERE CONSENT_ID = ?";
    }

    /**
     * SQL query for delete consent mapping by auth id.
     * @param executeOnRetentionTables  whether to execute on retention tables
     * @return  SQL query for delete consent mapping by auth id
     */
    public String getDeleteConsentMappingByAuthIdPreparedStatement(boolean executeOnRetentionTables) {

        // table prefix is to execute on consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (executeOnRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "DELETE OBCM FROM " + tablePrefix + "OB_CONSENT_MAPPING OBCM INNER JOIN " + tablePrefix +
                "OB_CONSENT_AUTH_RESOURCE OBAR ON OBCM.AUTH_ID = OBAR.AUTH_ID WHERE OBAR.CONSENT_ID = ?";
    }

    /**
     * SQL query for delete auth resource.
     * @param executeOnRetentionTables  whether to execute on retention tables
     * @return SQL query for delete auth resource
     */
    public String getDeleteAuthorizationResourcePreparedStatement(boolean executeOnRetentionTables) {

        // table prefix is to execute on consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (executeOnRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "DELETE FROM " + tablePrefix + "OB_CONSENT_AUTH_RESOURCE WHERE CONSENT_ID = ?";
    }

    /**
     * SQL query for consent status audit record.
     * @param executeOnRetentionTables  whether to execute on retention tables
     * @return  SQL query for consent status audit record
     */
    public String getDeleteConsentStatusAuditRecordsPreparedStatement(boolean executeOnRetentionTables) {

        // table prefix is to execute on consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (executeOnRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "DELETE FROM " + tablePrefix + "OB_CONSENT_STATUS_AUDIT WHERE CONSENT_ID = ?";
    }

    /**
     * SQL query for delete consent.
     * @param executeOnRetentionTables  whether to execute on retention tables
     * @return  SQL query for delete consent
     */
    public String getDeleteConsentPreparedStatement(boolean executeOnRetentionTables) {

        // table prefix is to execute on consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (executeOnRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "DELETE FROM " + tablePrefix + "OB_CONSENT WHERE CONSENT_ID = ?";
    }

    /**
     * SQL query for get list of consent_ids.
     * @param fetchFromRetentionTables  whether to fetch from retention tables
     * @return  SQL query for get list of consent_ids
     */
    public String getListOfConsentIdsPreparedStatement(boolean fetchFromRetentionTables) {

        // table prefix is to fetch from the consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (fetchFromRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        return "SELECT CONSENT_ID FROM " + tablePrefix + "OB_CONSENT";
    }

    /**
     * SQL query for get consent status audit records by consentIds.
     * @param whereClause conditions
     * @param shouldLimit   whether to consider the Limit parameter
     * @param shouldOffset  whether to consider the Offset parameter
     * @param fetchFromRetentionTables whether to fetch from retention tables
     * @return  SQL query for get consent status audit records by consentIds
     */
    public String getConsentStatusAuditRecordsByConsentIdsPreparedStatement(String whereClause, boolean shouldLimit,
                                                                               boolean shouldOffset,
                                                                               boolean fetchFromRetentionTables) {

        // table prefix is to fetch from the consent retention data (purged data) tables. (if enabled)
        String tablePrefix = "";
        if (fetchFromRetentionTables) {
            tablePrefix = ConsentMgtDAOConstants.RETENTION_TABLE_NAME_PREFIX;
        }
        StringBuilder query =
                new StringBuilder("SELECT * FROM " + tablePrefix + "OB_CONSENT_STATUS_AUDIT " + whereClause);

        if (shouldLimit && shouldOffset) {
            query.append(" LIMIT ? OFFSET ? ");
        } else if (shouldLimit) {
            query.append(" LIMIT ? ");
        }
        return query.toString();
    }

    /**
     * Util method to get the limit offset order for differentiate oracle and mssql pagination.
     * @return is limit is before in prepared statement than offset
     */
    public boolean isLimitBeforeThanOffset() {

        return true;
    }

}
