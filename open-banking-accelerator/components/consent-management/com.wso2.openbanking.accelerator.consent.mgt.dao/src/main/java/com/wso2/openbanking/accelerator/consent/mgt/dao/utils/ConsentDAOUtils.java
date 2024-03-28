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

package com.wso2.openbanking.accelerator.consent.mgt.dao.utils;

import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utils class for consent module.
 */
public class ConsentDAOUtils {

    private static final String SPACE = " ";
    private static final String COMMA = ",";
    private static final String QUOTE = "\'";
    private static final String PLACEHOLDER = "?";
    private static final String LEFT_PARENTHESIS = "(";
    private static final String RIGHT_PARENTHESIS = ")";
    private static final Map<String, String> DB_OPERATORS_MAP = new HashMap<String, String>() {
        {
            put(ConsentMgtDAOConstants.IN, "IN");
            put(ConsentMgtDAOConstants.AND, "AND");
            put(ConsentMgtDAOConstants.OR, "OR");
            put(ConsentMgtDAOConstants.WHERE, "WHERE");
            put(ConsentMgtDAOConstants.PLACEHOLDER, "?,");
            put(ConsentMgtDAOConstants.PLAIN_PLACEHOLDER, "?");
            put(ConsentMgtDAOConstants.EQUALS, "=");
        }
    };

    public static String constructConsentSearchPreparedStatement(Map<String, ArrayList> applicableConditions) {

        StringBuilder placeHoldersBuilder = new StringBuilder();
        StringBuilder whereClauseBuilder = new StringBuilder();
        whereClauseBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.WHERE));
        // If all lists are empty or null, return the default term "where"
        if (MapUtils.isEmpty(applicableConditions)) {
            return "";
        }
        for (Map.Entry<String, ArrayList> entry : applicableConditions.entrySet()) {
            // Oracle only allows 1000 values to be used in a SQL "IN" clause. Since more than 1000 consent IDs
            // are used in some queries, "OR" clause is used
            if (entry.getKey().contains("CONSENT_ID")) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    whereClauseBuilder
                            .append(SPACE)
                            .append(entry.getKey())
                            .append(SPACE)
                            .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.EQUALS))
                            .append(SPACE)
                            .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.PLAIN_PLACEHOLDER))
                            .append(SPACE)
                            .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.OR));
                }
                // Delete last OR from the statement
                whereClauseBuilder.replace(whereClauseBuilder.length() - 2,
                        whereClauseBuilder.length(), DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.AND));
            } else {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    placeHoldersBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.PLACEHOLDER));
                }
                String placeHolders = StringUtils.removeEnd(placeHoldersBuilder.toString(), COMMA);
                whereClauseBuilder
                        .append(SPACE)
                        .append(entry.getKey())
                        .append(SPACE)
                        .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.IN))
                        .append(LEFT_PARENTHESIS)
                        .append(placeHolders)
                        .append(RIGHT_PARENTHESIS)
                        .append(SPACE)
                        .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.AND));
                // Delete all content from old string builder except the starting left parenthesis
                placeHoldersBuilder.delete(0, placeHoldersBuilder.length());
            }
        }
        int size = whereClauseBuilder.length();
        //removing the last AND in the statement
        whereClauseBuilder.replace(size - 3, size, "");
        return whereClauseBuilder.toString();
    }

    public static String constructUserIdListFilterCondition(Map<String, ArrayList> userIds) {

        StringBuilder placeHoldersBuilder = new StringBuilder();
        StringBuilder userIdFilterBuilder = new StringBuilder();

        for (Map.Entry<String, ArrayList> entry : userIds.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                placeHoldersBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.PLACEHOLDER));
            }
            String placeHolders = StringUtils.removeEnd(placeHoldersBuilder.toString(), COMMA);
            userIdFilterBuilder
                    .append(SPACE)
                    .append(entry.getKey())
                    .append(SPACE)
                    .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.IN))
                    .append(LEFT_PARENTHESIS)
                    .append(placeHolders)
                    .append(RIGHT_PARENTHESIS)
                    .append(SPACE);
            // Delete all content from old string builder except the starting left parenthesis
            placeHoldersBuilder.delete(0, placeHoldersBuilder.length());
        }
        return userIdFilterBuilder.toString();
    }

    public static String constructAuthSearchPreparedStatement(Map<String, String> applicableConditions) {

        StringBuilder whereClauseBuilder = new StringBuilder();

        // If all lists are empty or null, return the default term "where"
        if (MapUtils.isEmpty(applicableConditions)) {
            return whereClauseBuilder.toString();
        }

        whereClauseBuilder.append(SPACE).append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.WHERE));

        int count = 0;
        for (Map.Entry<String, String> entry : applicableConditions.entrySet()) {

            if (count > 0) {
                whereClauseBuilder.append(SPACE).append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.AND));
            }
            whereClauseBuilder
                    .append(SPACE)
                    .append(entry.getKey())
                    .append(SPACE)
                    .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.EQUALS))
                    .append(SPACE)
                    .append(PLACEHOLDER);
            count++;
        }
        return whereClauseBuilder.toString();
    }

    /**
     * Method to construct where clause for consent status audit search condition.
     * @param consentIDs   List of consent IDs
     * @return  Filter condition for consent status audit
     */
    public static String constructConsentAuditRecordSearchPreparedStatement(ArrayList<String> consentIDs) {

        StringBuilder whereClauseBuilder = new StringBuilder();
        if (!CollectionUtils.isEmpty(consentIDs)) {
            whereClauseBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.WHERE));
            for (int count = 0; count < consentIDs.size(); count++) {
                whereClauseBuilder
                        .append(SPACE)
                        .append(ConsentMgtDAOConstants.CONSENT_ID)
                        .append(SPACE)
                        .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.EQUALS))
                        .append(SPACE)
                        .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.PLAIN_PLACEHOLDER))
                        .append(SPACE)
                        .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.OR));
            }
            // Delete last OR from the statement
            whereClauseBuilder.replace(whereClauseBuilder.length() - 2,
                    whereClauseBuilder.length(), StringUtils.SPACE);
        }
        return whereClauseBuilder.toString();
    }

    public static TreeMap<Integer, ArrayList> determineOrderOfParamsToSet(String preparedStatement, Map<String,
            ArrayList> applicableConditionsMap, Map<String, String> columnsMap) {

        int indexOfConsentIDsList;
        int indexOfClientIdsList;
        int indexOfConsentTypesList;
        int indexOfConsentStatusesList;
        int indexOfUserIDsList;

        // Tree map naturally sorts values in ascending order according to the key
        TreeMap<Integer, ArrayList> sortedIndexesMap = new TreeMap<>();

        /* Check whether the where condition clauses are in the prepared statement and get the index if exists to
           determine the order */
        if (preparedStatement.contains(columnsMap.get(ConsentMgtDAOConstants.CONSENT_IDS))) {
            indexOfConsentIDsList = preparedStatement.indexOf(columnsMap.get(ConsentMgtDAOConstants.CONSENT_IDS));
            sortedIndexesMap.put(indexOfConsentIDsList,
                    applicableConditionsMap.get(columnsMap.get(ConsentMgtDAOConstants.CONSENT_IDS)));
        }
        if (preparedStatement.contains(columnsMap.get(ConsentMgtDAOConstants.CLIENT_IDS))) {
            indexOfClientIdsList = preparedStatement.indexOf(columnsMap.get(ConsentMgtDAOConstants.CLIENT_IDS));
            sortedIndexesMap.put(indexOfClientIdsList,
                    applicableConditionsMap.get(columnsMap.get(ConsentMgtDAOConstants.CLIENT_IDS)));
        }
        if (preparedStatement.contains(columnsMap.get(ConsentMgtDAOConstants.CONSENT_TYPES))) {
            indexOfConsentTypesList = preparedStatement.indexOf(columnsMap.get(ConsentMgtDAOConstants.CONSENT_TYPES));
            sortedIndexesMap.put(indexOfConsentTypesList,
                    applicableConditionsMap.get(columnsMap.get(ConsentMgtDAOConstants.CONSENT_TYPES)));
        }
        if (preparedStatement.contains(columnsMap.get(ConsentMgtDAOConstants.CONSENT_STATUSES))) {
            indexOfConsentStatusesList = preparedStatement
                    .indexOf(columnsMap.get(ConsentMgtDAOConstants.CONSENT_STATUSES));
            sortedIndexesMap.put(indexOfConsentStatusesList,
                    applicableConditionsMap.get(columnsMap.get(ConsentMgtDAOConstants.CONSENT_STATUSES)));
        }
        if (preparedStatement.contains(columnsMap.get(ConsentMgtDAOConstants.USER_IDS))) {
            indexOfUserIDsList = preparedStatement.indexOf(columnsMap.get(ConsentMgtDAOConstants.USER_IDS));
            sortedIndexesMap.put(indexOfUserIDsList,
                    applicableConditionsMap.get(columnsMap.get(ConsentMgtDAOConstants.USER_IDS)));
        }
        return sortedIndexesMap;
    }

    /**
     * Method to construct excluded statuses search condition.
     *
     * @param statusesEligibleForExpiration  List of statuses eligible for expiration
     * @return  Filter condition for excluded statuses
     */
    public static String constructStatusesEligibleForExpirationCondition(List<String> statusesEligibleForExpiration) {

        StringBuilder placeHoldersBuilder = new StringBuilder();
        StringBuilder statusesEligibleForExpirationFilterBuilder = new StringBuilder();

        for (int i = 0; i < statusesEligibleForExpiration.size(); i++) {
            placeHoldersBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.PLACEHOLDER));
        }
        String placeHolders = StringUtils.removeEnd(placeHoldersBuilder.toString(), COMMA);
        statusesEligibleForExpirationFilterBuilder
                .append(SPACE)
                .append(LEFT_PARENTHESIS)
                .append(placeHolders)
                .append(RIGHT_PARENTHESIS)
                .append(SPACE);
        // Delete all content from old string builder except the starting left parenthesis
        placeHoldersBuilder.delete(0, placeHoldersBuilder.length());
        return statusesEligibleForExpirationFilterBuilder.toString();
    }

    public static String constructConsentHistoryPreparedStatement(int recordIdCount) {

        StringBuilder whereClauseBuilder = new StringBuilder();
        whereClauseBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.WHERE));

        for (int count = 0; count < recordIdCount; count++) {
            whereClauseBuilder.append(SPACE)
                    .append(LEFT_PARENTHESIS)
                    .append(ConsentMgtDAOConstants.RECORD_ID)
                    .append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.EQUALS))
                    .append(PLACEHOLDER)
                    .append(RIGHT_PARENTHESIS);
            if (count < recordIdCount - 1) {
                whereClauseBuilder.append(SPACE).append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.OR));
            }
        }
        return whereClauseBuilder.toString();
    }
}
