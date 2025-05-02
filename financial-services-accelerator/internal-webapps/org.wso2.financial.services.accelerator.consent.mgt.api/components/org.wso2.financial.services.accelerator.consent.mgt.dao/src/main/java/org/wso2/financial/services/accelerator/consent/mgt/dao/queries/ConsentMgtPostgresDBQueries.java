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

package org.wso2.financial.services.accelerator.consent.mgt.dao.queries;

import org.apache.commons.lang3.StringUtils;

/**
 * The PostgreSQL database queries used by the consent management DAO layer.
 */
public class ConsentMgtPostgresDBQueries extends ConsentMgtCommonDBQueries {

    /**
     * This method returns the detailed consent search query. It constructs the query according to the provided
     * parameters dynamically. This queries all consent attributes, authorization resources, mapping resources, consent
     * data of the provided consent according to the parameters provided. To avoid fetching same rows multiple times,
     * the string_agg function is used to concatenate values using a delimiter. The delimited results are later
     * processed and set to the result.
     *
     * @param whereClause the pre-constructed where dynamic where clause
     * @param shouldLimit flag that indicates the limit
     * @param shouldOffset flag that indicates the offset
     * @param userIdFilterClause the pre-constructed user id filter condition
     * @return the constructed prepared statement for consent search function
     */
    public String getSearchConsentsPreparedStatement(String whereClause, boolean shouldLimit, boolean shouldOffset,
                                                     String userIdFilterClause) {

        String selectClause = "(SELECT * FROM FS_CONSENT " + whereClause + ")";
        String joinType = "LEFT ";
        if (StringUtils.isNotEmpty(userIdFilterClause)) {
            joinType = "INNER ";
            userIdFilterClause = "AND " + userIdFilterClause;
        }

        StringBuilder query = new StringBuilder("SELECT " +
                "  OBC.CONSENT_ID, " +
                "  RECEIPT, " +
                "  CLIENT_ID, " +
                "  CONSENT_TYPE, " +
                "  OBC.CURRENT_STATUS AS CURRENT_STATUS, " +
                "  CONSENT_FREQUENCY, " +
                "  EXPIRY_TIME, " +
                "  RECURRING_INDICATOR, " +
                "  OBC.CREATED_TIME AS CONSENT_CREATED_TIME, " +
                "  OBC.UPDATED_TIME AS CONSENT_UPDATED_TIME, " +
                "  String_agg(" +
                "    CA.att_key :: varchar, '||' order by CA.att_key :: varchar" +
                "  ) AS ATT_KEY," +
                "  String_agg(" +
                "    CA.att_value :: varchar, '||' order by CA.att_key :: varchar" +
                "  ) AS ATT_VALUE," +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCAR2.auth_id:: varchar, '||' order by OCAR2.auth_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id " +
                "    GROUP BY " +
                "      OCAR2.consent_id" +
                "  ) AS AUTH_ID, " +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCAR2.auth_status :: varchar, '||' order by OCAR2.auth_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id " +
                "    GROUP BY " +
                "      OCAR2.consent_id" +
                "  ) AS AUTH_STATUS, " +
                "  (" +
                "    select" +
                "    String_agg(" +
                "    OCAR2.auth_type :: varchar, '||' order by OCAR2.auth_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id " +
                "    GROUP BY " +
                "      OCAR2.consent_id" +
                "  ) AS AUTH_TYPE, " +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCAR2.updated_time :: varchar, '||' order by OCAR2.auth_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id " +
                "    GROUP BY " +
                "      OCAR2.consent_id" +
                "  ) AS UPDATED_TIME, " +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCAR2.user_id :: varchar, '||' order by OCAR2.auth_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_AUTH_RESOURCE OCAR2 " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id " +
                "    GROUP BY " +
                "      OCAR2.consent_id" +
                "  ) AS USER_ID, " +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCAR2.auth_id:: varchar, '||' order by OCM2.mapping_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_MAPPING OCM2 " +
                "      JOIN FS_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id" +
                "  ) AS AUTH_MAPPING_ID, " +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCM2.account_id :: varchar, '||' order by OCM2.mapping_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_MAPPING OCM2 " +
                "      JOIN FS_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id" +
                "  ) AS ACCOUNT_ID, " +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCM2.mapping_id :: varchar, '||' order by OCM2.mapping_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_MAPPING OCM2 " +
                "      JOIN FS_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id" +
                "  ) AS MAPPING_ID, " +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCM2.mapping_status :: varchar, '||' order by OCM2.mapping_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_MAPPING OCM2 " +
                "      JOIN FS_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id" +
                "  ) AS MAPPING_STATUS, " +
                "  (" +
                "    SELECT " +
                "    String_agg(" +
                "    OCM2.permission :: varchar, '||' order by OCM2.mapping_id :: varchar" +
                "  )" +
                "    FROM " +
                "      FS_CONSENT_MAPPING OCM2 " +
                "      JOIN FS_CONSENT_AUTH_RESOURCE OCAR2 ON OCAR2.auth_id = OCM2.auth_id " +
                "    WHERE " +
                "      OCAR2.consent_id = OBC.consent_id" +
                "  ) AS PERMISSION" +
                " FROM " +
                selectClause +
                "AS OBC " +
                "LEFT JOIN FS_CONSENT_ATTRIBUTE CA ON OBC.CONSENT_ID=CA.CONSENT_ID " +
                joinType + "JOIN FS_CONSENT_AUTH_RESOURCE OCAR ON OBC.CONSENT_ID=OCAR.CONSENT_ID "
                + userIdFilterClause +
                "LEFT JOIN FS_CONSENT_MAPPING OCM ON OCAR.AUTH_ID=OCM.AUTH_ID WHERE " +
                "(OBC.UPDATED_TIME >= COALESCE(?, OBC.UPDATED_TIME) " +
                "AND OBC.UPDATED_TIME <= COALESCE(?, OBC.UPDATED_TIME)) " +
                "group by OBC.CONSENT_ID," +
                "OBC.RECEIPT," +
                "OBC.CLIENT_ID," +
                "OBC.CONSENT_TYPE," +
                "CONSENT_FREQUENCY," +
                "EXPIRY_TIME," +
                "RECURRING_INDICATOR," +
                "OBC.CURRENT_STATUS," +
                "OBC.CREATED_TIME," +
                "OBC.UPDATED_TIME " +
                "ORDER BY OBC.UPDATED_TIME DESC");

        if (shouldLimit && shouldOffset) {
            query.append(" LIMIT ? OFFSET ? ");
        } else if (shouldLimit) {
            query.append(" LIMIT ? ");
        }

        return query.toString();
    }
}
