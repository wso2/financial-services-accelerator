/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.dao.queries;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The common database queries used by the consent management DAO layer.
 */
public class ConsentMgtCommonDBQueries {

    public String getStoreConsentPreparedStatement() {

        return "INSERT INTO FS_CONSENT (ORG_INFO, CONSENT_ID, RECEIPT, CREATED_TIME, UPDATED_TIME, CLIENT_ID, " +
                "CONSENT_TYPE, " +
                "CURRENT_STATUS, EXPIRY_TIME, RECURRING_INDICATOR) VALUES ( COALESCE(?, " +
                "'DEFAULT_ORG')" +
                ", ?, ?, ?, ?, ?, " +
                "?, " +
                "?, ?, ?)";
    }

    public String getGetConsentPreparedStatement() {

        return "SELECT * FROM FS_CONSENT WHERE CONSENT_ID = ?";
    }

    public String getGetConsentWithConsentAttributesPreparedStatement() {

        return "SELECT FS_CONSENT.CONSENT_ID, ORG_INFO, RECEIPT, CREATED_TIME, UPDATED_TIME, CLIENT_ID, CONSENT_TYPE," +
                " " +
                "CURRENT_STATUS, EXPIRY_TIME, RECURRING_INDICATOR, " +
                "FS_CONSENT_ATTRIBUTE.ATT_KEY, FS_CONSENT_ATTRIBUTE.ATT_VALUE FROM FS_CONSENT LEFT JOIN " +
                "FS_CONSENT_ATTRIBUTE ON FS_CONSENT.CONSENT_ID = FS_CONSENT_ATTRIBUTE.CONSENT_ID WHERE FS_CONSENT" +
                ".CONSENT_ID = ?";
    }

    public String getGetDetailedConsentPreparedStatement() {

        return "SELECT obc.CONSENT_ID," +
                "ORG_INFO, " +
                "RECEIPT, " +
                "CLIENT_ID, " +
                "CONSENT_TYPE, " +
                "CURRENT_STATUS, " +
                "EXPIRY_TIME, " +
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
                "ocar.RESOURCE " +
                "FROM FS_CONSENT obc " +
                "LEFT JOIN FS_CONSENT_ATTRIBUTE ca ON obc.CONSENT_ID=ca.CONSENT_ID " +
                "LEFT JOIN FS_CONSENT_AUTH_RESOURCE ocar ON obc.CONSENT_ID=ocar.CONSENT_ID " +
                "WHERE (obc.CONSENT_ID = ? AND obc.ORG_INFO = COALESCE(?, obc.ORG_INFO)) ";
    }

    public String getUpdateConsentStatusPreparedStatement() {

        return "UPDATE FS_CONSENT SET CURRENT_STATUS = ?, UPDATED_TIME = ? WHERE CONSENT_ID = ?";
    }

    public String getUpdateConsentExpiryTimePreparedStatement() {

        return "UPDATE FS_CONSENT SET EXPIRY_TIME = ?, UPDATED_TIME = ? WHERE CONSENT_ID = ?";
    }

    public String getStoreAuthorizationPreparedStatement() {

        return "INSERT INTO FS_CONSENT_AUTH_RESOURCE (AUTH_ID, CONSENT_ID, AUTH_TYPE, USER_ID, AUTH_STATUS, RESOURCE," +
                " " +
                "UPDATED_TIME) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    public String getGetAuthorizationResourcePreparedStatement() {

        return "SELECT ACR.*\n" +
                "FROM FS_CONSENT_AUTH_RESOURCE ACR  \n" +
                "LEFT JOIN FS_CONSENT C ON ACR.CONSENT_ID = C.CONSENT_ID  \n" +
                "WHERE ACR.AUTH_ID = ? \n" +
                "AND (C.ORG_INFO = ?)";
    }


    public String getUpdateAuthorizationResourcePreparedStatement() {

        return "UPDATE FS_CONSENT_AUTH_RESOURCE SET AUTH_STATUS = ?, AUTH_TYPE = ?, USER_ID = ?, RESOURCE = ?, " +
                "UPDATED_TIME = ? " +
                "WHERE AUTH_ID = ?";
    }

    public String getDeleteAuthorizationResourcePreparedStatement() {
        return "DELETE FROM FS_CONSENT_AUTH_RESOURCE WHERE AUTH_ID = ?";
    }


    public String getStoreConsentAttributesPreparedStatement() {

        return "INSERT INTO FS_CONSENT_ATTRIBUTE (CONSENT_ID, ATT_KEY, ATT_VALUE) VALUES (?, ?, ?)";
    }

    public String getGetConsentAttributesPreparedStatement() {

        return "SELECT * FROM FS_CONSENT_ATTRIBUTE WHERE CONSENT_ID = ?";
    }

    public String getGetConsentAttributesByNamePreparedStatement() {

        return "SELECT CONSENT_ID, ATT_VALUE FROM FS_CONSENT_ATTRIBUTE WHERE ATT_KEY = ?";
    }

    public String getConsentIdByConsentAttributeNameAndValuePreparedStatement() {

        return "SELECT CONSENT_ID FROM FS_CONSENT_ATTRIBUTE WHERE ATT_KEY = ? AND ATT_VALUE = ?";
    }

    public String getUpdateConsentAttributesPreparedStatement() {

        return "UPDATE FS_CONSENT_ATTRIBUTE SET ATT_VALUE = ? WHERE CONSENT_ID = ? and ATT_KEY = ?";
    }

    public String getDeleteConsentAttributePreparedStatement() {

        return "DELETE FROM FS_CONSENT_ATTRIBUTE WHERE CONSENT_ID = ? AND ATT_KEY = ?";
    }


    public String getSearchConsentsPreparedStatement(String whereClause, boolean shouldLimit, boolean shouldOffset,
                                                     String userIdFilterClause) {

        String selectClause = "(SELECT * FROM FS_CONSENT " + whereClause + ")";
        String joinType = "LEFT ";
        if (StringUtils.isNotEmpty(userIdFilterClause)) {
            joinType = "INNER ";
            userIdFilterClause = "AND " + userIdFilterClause;
        }

        StringBuilder query = new StringBuilder("SELECT OBC.CONSENT_ID, " +
                "ORG_INFO, " +
                "RECEIPT, " +
                "CLIENT_ID, " +
                "CONSENT_TYPE, " +
                "OBC.CURRENT_STATUS AS CURRENT_STATUS," +
                "EXPIRY_TIME," +
                "RECURRING_INDICATOR," +
                "OBC.CREATED_TIME AS CONSENT_CREATED_TIME," +
                "OBC.UPDATED_TIME AS CONSENT_UPDATED_TIME," +
                "Group_concat(distinct CA.att_key order by CA.att_key SEPARATOR '||' ) AS ATT_KEY, " +
                "Group_concat(distinct CA.att_value order by CA.att_key SEPARATOR '||') AS ATT_VALUE, " +


                "( SELECT   Group_concat( OCAR2.auth_id order by OCAR2.auth_id SEPARATOR '||') " +
                "         FROM FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS AUTH_ID, " +

                "( SELECT   Group_concat(OCAR2.auth_status order by OCAR2.auth_id SEPARATOR '||') " +
                "         FROM FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS AUTH_STATUS, " +

                "( SELECT   Group_concat(OCAR2.auth_type order by OCAR2.auth_id SEPARATOR '||')  " +
                "         FROM FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS AUTH_TYPE, " +

                "( SELECT   Group_concat(OCAR2.resource order by OCAR2.auth_id SEPARATOR '||')  " +
                "         FROM FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS RESOURCE, " +

                "( SELECT   Group_concat(OCAR2.updated_time order by OCAR2.auth_id SEPARATOR '||')  " +
                "         FROM FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS UPDATED_TIME, " +

                "( SELECT   Group_concat(OCAR2.user_id order by OCAR2.auth_id SEPARATOR '||') " +
                "         FROM FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "         WHERE OCAR2.consent_id = OBC.consent_id " +
                "         GROUP BY OCAR2.consent_id ) AS USER_ID " +


                "FROM " +
                selectClause +
                "AS OBC " +
                "LEFT JOIN FS_CONSENT_ATTRIBUTE CA ON OBC.CONSENT_ID=CA.CONSENT_ID " +
                joinType + "JOIN FS_CONSENT_AUTH_RESOURCE OCAR ON OBC.CONSENT_ID=OCAR.CONSENT_ID "
                + userIdFilterClause +
                " WHERE " +
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

    public String getSearchAuthorizationResourcesPreparedStatement(String whereClause) {

        return "SELECT * FROM FS_CONSENT_AUTH_RESOURCE" + whereClause;
    }

    public String getStoreConsentStatusAuditRecordPreparedStatement() {

        return "INSERT INTO FS_CONSENT_STATUS_AUDIT (STATUS_AUDIT_ID, CONSENT_ID, CURRENT_STATUS, ACTION_TIME, " +
                "REASON, ACTION_BY, PREVIOUS_STATUS) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    public String getGetConsentStatusAuditRecordsPreparedStatement() {

        return "SELECT * FROM FS_CONSENT_STATUS_AUDIT WHERE CONSENT_ID = COALESCE(?, CONSENT_ID) " +
                "AND CURRENT_STATUS = COALESCE(?, CURRENT_STATUS) AND ACTION_BY = COALESCE(?, ACTION_BY) " +
                "AND STATUS_AUDIT_ID = COALESCE (?, STATUS_AUDIT_ID) AND ACTION_TIME >= COALESCE(?, ACTION_TIME) " +
                "AND ACTION_TIME <= COALESCE(?, ACTION_TIME)";
    }

    /**
     * SQL query for get consent status audit records by consentIds.
     *
     * @param whereClause  conditions
     * @param shouldLimit  whether to consider the Limit parameter
     * @param shouldOffset whether to consider the Offset parameter
     * @return SQL query for get consent status audit records by consentIds
     */
    public String getConsentStatusAuditRecordsByConsentIdsPreparedStatement(String whereClause, boolean shouldLimit,
                                                                            boolean shouldOffset) {

        StringBuilder query =
                new StringBuilder("SELECT * FROM FS_CONSENT_STATUS_AUDIT " + whereClause);

        if (shouldLimit && shouldOffset) {
            query.append(" LIMIT ? OFFSET ? ");
        } else if (shouldLimit) {
            query.append(" LIMIT ? ");
        }
        return query.toString();
    }

    /**
     * Util method to get the limit offset order for differentiate oracle and mssql pagination.
     *
     * @return is limit is before in prepared statement than offset
     */
    public boolean isLimitBeforeThanOffset() {

        return true;
    }

    public String getInsertConsentHistoryPreparedStatement() {

        return "INSERT INTO FS_CONSENT_HISTORY (TABLE_ID, AUDIT_RECORD_ID, RECORD_ID, HISTORY_ID, " +
                "CHANGED_VALUES, " +
                "REASON, EFFECTIVE_TIMESTAMP) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    public String getGetConsentHistoryPreparedStatement(String whereClause) {

        return "SELECT * FROM FS_CONSENT_HISTORY " + whereClause +
                "ORDER BY EFFECTIVE_TIMESTAMP DESC";
    }

    public String getSearchExpiringConsentPreparedStatement(String statusesEligibleForExpirationCondition) {

        return "SELECT OBC.CONSENT_ID " +
                " FROM   FS_CONSENT_ATTRIBUTE CA " +
                " JOIN   FS_CONSENT OBC " +
                " ON     CA.CONSENT_ID = OBC.CONSENT_ID " +
                " WHERE  CA.ATT_KEY = ? AND OBC.CURRENT_STATUS IN " + statusesEligibleForExpirationCondition;
    }

    public List<String> getDeleteConsentCascadeStatements() {
        List<String> statements = new ArrayList<>();

        statements.add("DELETE FROM FS_CONSENT_ATTRIBUTE WHERE CONSENT_ID = ?");
        statements.add("DELETE FROM FS_CONSENT_AUTH_RESOURCE WHERE CONSENT_ID = ?");
        statements.add("DELETE FROM FS_CONSENT_STATUS_AUDIT WHERE CONSENT_ID = ?");
        statements.add("DELETE FROM FS_CONSENT WHERE CONSENT_ID = ?");

        return statements;
    }


}
