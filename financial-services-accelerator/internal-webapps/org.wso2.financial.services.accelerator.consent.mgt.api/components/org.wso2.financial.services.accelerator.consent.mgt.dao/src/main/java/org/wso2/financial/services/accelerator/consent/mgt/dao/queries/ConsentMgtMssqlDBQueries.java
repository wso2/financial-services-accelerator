/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package  org.wso2.financial.services.accelerator.consent.mgt.dao.queries;

import org.apache.commons.lang3.StringUtils;

/**
 * The Microsoft SQL database queries used by the consent management DAO layer.
 */
public class ConsentMgtMssqlDBQueries extends ConsentMgtCommonDBQueries {

    public String getSearchConsentsPreparedStatement(String whereClause, boolean shouldLimit, boolean shouldOffset,
                                                     String userIdFilterClause) {

        String selectClause = "(SELECT * FROM FS_CONSENT)";
        String joinType = " LEFT ";

        if (StringUtils.isNotEmpty(userIdFilterClause)) {
            joinType = " INNER ";
            userIdFilterClause = "AND " + userIdFilterClause;
        }

        if (whereClause.trim().isEmpty()) {
            whereClause = " WHERE ";
        } else {
            whereClause = whereClause + " AND ";
        }

        StringBuilder query = new StringBuilder("SELECT OBC.CONSENT_ID, " +
                " (SELECT receipt FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS RECEIPT, " +
                " (SELECT client_id FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS CLIENT_ID, " +
                " (SELECT consent_type FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS CONSENT_TYPE, " +
                " (SELECT current_status FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS CURRENT_STATUS, " +
                " (SELECT consent_frequency FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS CONSENT_FREQUENCY, " +
                " (SELECT validity_time FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS VALIDITY_TIME, " +
                " (SELECT recurring_indicator FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS RECURRING_INDICATOR, " +
                " (SELECT created_time FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS CONSENT_CREATED_TIME, " +
                " (SELECT updated_time FROM FS_CONSENT WHERE consent_id = obc.consent_id " +
                "           order by consent_id offset 0 rows FETCH next 1 rows only ) AS CONSENT_UPDATED_TIME, " +

                "( SELECT   String_agg(att_key , '||') within GROUP (ORDER BY att_key) " +
                "       FROM     FS_CONSENT_ATTRIBUTE " +
                "       WHERE    consent_id = OBC.consent_id " +
                "       GROUP BY consent_id ) AS ATT_KEY, " +

                "( SELECT   String_agg(att_value , '||') within GROUP (ORDER BY att_key) " +
                "       FROM     FS_CONSENT_ATTRIBUTE " +
                "       WHERE    consent_id = OBC.consent_id " +
                "       GROUP BY consent_id ) AS ATT_VALUE,                                    " +

                "( SELECT   String_agg(auth_id , '||') within GROUP (ORDER BY auth_id) " +
                "       FROM     FS_CONSENT_AUTH_RESOURCE " +
                "       WHERE    consent_id = OBC.consent_id " +
                "       GROUP BY consent_id ) AS AUTH_ID, " +

                "( SELECT   String_agg(auth_status , '||') within GROUP (ORDER BY auth_id) " +
                "       FROM     FS_CONSENT_AUTH_RESOURCE " +
                "       WHERE    consent_id = OBC.consent_id " +
                "       GROUP BY consent_id ) AS AUTH_STATUS,        " +

                "( SELECT   String_agg(auth_type , '||') within GROUP (ORDER BY auth_id) " +
                "       FROM     FS_CONSENT_AUTH_RESOURCE " +
                "       WHERE    consent_id = OBC.consent_id " +
                "       GROUP BY consent_id ) AS AUTH_TYPE, " +

                "( SELECT   String_agg(updated_time , '||') within GROUP (ORDER BY auth_id) " +
                "       FROM     FS_CONSENT_AUTH_RESOURCE " +
                "       WHERE    consent_id = OBC.consent_id " +
                "       GROUP BY consent_id ) AS UPDATED_TIME,   " +

                "( SELECT   String_agg(user_id , '||') within GROUP (ORDER BY auth_id) " +
                "       FROM     FS_CONSENT_AUTH_RESOURCE " +
                "       WHERE    consent_id = OBC.consent_id " +
                "       GROUP BY consent_id ) AS USER_ID," +


                "( SELECT   String_agg(OCM2.AUTH_ID , '||') within GROUP (ORDER BY OCM2.mapping_id) " +
                "   FROM     FS_CONSENT_MAPPING OCM2 " +
                "   JOIN FS_CONSENT_AUTH_RESOURCE ocar2 ON ocar2.auth_id = OCM2.auth_id            " +
                "   WHERE ocar2.consent_id = obc.consent_id) AS AUTH_MAPPING_ID  , " +

                "( SELECT   String_agg(OCM2.account_id , '||') within GROUP (ORDER BY OCM2.mapping_id) " +
                "   FROM     FS_CONSENT_MAPPING OCM2 " +
                "   JOIN FS_CONSENT_AUTH_RESOURCE ocar2 ON ocar2.auth_id = OCM2.auth_id            " +
                "   WHERE ocar2.consent_id = obc.consent_id) AS ACCOUNT_ID  , " +

                "( SELECT   String_agg(OCM2.mapping_id , '||') within GROUP (ORDER BY OCM2.mapping_id) " +
                "   FROM     FS_CONSENT_MAPPING OCM2 " +
                "   JOIN FS_CONSENT_AUTH_RESOURCE ocar2 ON ocar2.auth_id = OCM2.auth_id            " +
                "   WHERE ocar2.consent_id = obc.consent_id) AS MAPPING_ID  , " +

                "( SELECT   String_agg(OCM2.mapping_status , '||') within GROUP (ORDER BY OCM2.mapping_id) " +
                "   FROM     FS_CONSENT_MAPPING OCM2 " +
                "   JOIN FS_CONSENT_AUTH_RESOURCE ocar2 ON ocar2.auth_id = OCM2.auth_id            " +
                "   WHERE ocar2.consent_id = obc.consent_id) AS MAPPING_STATUS  , " +

                "( SELECT   String_agg(OCM2.permission , '||') within GROUP (ORDER BY OCM2.mapping_id) " +
                "   FROM     FS_CONSENT_MAPPING OCM2 " +
                "   JOIN FS_CONSENT_AUTH_RESOURCE ocar2 ON ocar2.auth_id = OCM2.auth_id            " +
                "   WHERE ocar2.consent_id = obc.consent_id) AS PERMISSION   " +

                
                "FROM " + selectClause + "AS OBC " +
                "LEFT JOIN FS_CONSENT_ATTRIBUTE CA ON OBC.CONSENT_ID=CA.CONSENT_ID " +
                joinType + "JOIN FS_CONSENT_AUTH_RESOURCE OCAR ON OBC.CONSENT_ID=OCAR.CONSENT_ID "
                + userIdFilterClause +
                "LEFT JOIN FS_CONSENT_MAPPING OCM ON OCAR.AUTH_ID=OCM.AUTH_ID " + whereClause +
                " (OBC.UPDATED_TIME >= COALESCE(?, OBC.UPDATED_TIME) " +
                " AND OBC.UPDATED_TIME <= COALESCE(?, OBC.UPDATED_TIME)) GROUP BY obc.consent_id " +
                " ORDER BY UPDATED_TIME DESC ");

        if (shouldLimit && shouldOffset) {
            query.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        } else if (shouldLimit) {
            query.append("OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY ");
        }

        return query.toString();
    }

    /**
     * SQL query for get consent status audit records by consentIds.
     * @param whereClause conditions
     * @param shouldLimit limit
     * @param shouldOffset offset
     * @return SQL query
     */
    public String getConsentStatusAuditRecordsByConsentIdsPreparedStatement(String whereClause, boolean shouldLimit,
                                                                            boolean shouldOffset) {

        StringBuilder query =
                new StringBuilder("SELECT * FROM FS_CONSENT_STATUS_AUDIT " + whereClause);

        if (shouldLimit && shouldOffset) {
            query.append("ORDER BY STATUS_AUDIT_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        } else if (shouldLimit) {
            query.append("ORDER BY STATUS_AUDIT_ID OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY ");
        }
        return query.toString();
    }

    /**
     * Util method to get the limit offset order for differentiate oracle and mssql pagination.
     * @return is limit is before in prepared statement than offset
     */
    public boolean isLimitBeforeThanOffset() {

        return false;
    }
}
