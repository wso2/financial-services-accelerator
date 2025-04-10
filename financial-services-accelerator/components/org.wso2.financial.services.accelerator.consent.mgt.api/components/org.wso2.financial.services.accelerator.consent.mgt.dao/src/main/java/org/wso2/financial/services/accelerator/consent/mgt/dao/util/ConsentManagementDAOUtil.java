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

package org.wso2.financial.services.accelerator.consent.mgt.dao.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for consent management DAO.
 */
public class ConsentManagementDAOUtil {

    private static final Log log = LogFactory.getLog(ConsentManagementDAOUtil.class);

    private static final String SPACE = " ";
    private static final String COMMA = ",";
    private static final String PLACEHOLDER = "?";
    private static final String LEFT_PARENTHESIS = "(";
    private static final String RIGHT_PARENTHESIS = ")";

    private static final Map<String, String> DB_OPERATORS_MAP = new HashMap<>();

    static {
        DB_OPERATORS_MAP.put(ConsentMgtDAOConstants.IN,
                "IN");
        DB_OPERATORS_MAP.put(ConsentMgtDAOConstants.AND,
                "AND");
        DB_OPERATORS_MAP.put(ConsentMgtDAOConstants.OR,
                "OR");
        DB_OPERATORS_MAP.put(ConsentMgtDAOConstants.WHERE,
                "WHERE");
        DB_OPERATORS_MAP.put(ConsentMgtDAOConstants.PLACEHOLDER,
                "?,");
        DB_OPERATORS_MAP.put(ConsentMgtDAOConstants.PLAIN_PLACEHOLDER,
                "?");
        DB_OPERATORS_MAP.put(ConsentMgtDAOConstants.EQUALS,
                "=");
    }

    /**
     * Set data from the result set to ConsentResource object.
     *
     * @param resultSet result set
     * @return ConsentResource constructed using the result set
     */
    public static ConsentResource setDataToConsentResource(ResultSet resultSet) throws
            SQLException {

        return new ConsentResource(
                resultSet.getString(ConsentMgtDAOConstants.ORG_ID),
                resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID),
                resultSet.getString(ConsentMgtDAOConstants.CLIENT_ID),
                resultSet.getString(ConsentMgtDAOConstants.RECEIPT),
                resultSet.getString(ConsentMgtDAOConstants.CONSENT_TYPE),
                resultSet.getInt(ConsentMgtDAOConstants.CONSENT_FREQUENCY),
                resultSet.getLong(ConsentMgtDAOConstants.VALIDITY_TIME),
                resultSet.getBoolean(ConsentMgtDAOConstants.RECURRING_INDICATOR),
                resultSet.getString(ConsentMgtDAOConstants.CURRENT_STATUS),
                resultSet.getLong(ConsentMgtDAOConstants.CREATED_TIME),
                resultSet.getLong(ConsentMgtDAOConstants.UPDATED_TIME)
        );
    }

    /**
     * Set data from the result set to ConsentResource object with consent attributes.
     *
     * @param resultSet result set
     * @return ConsentResource with attributes constructed using the result set
     */
    public static ConsentResource setDataToConsentResourceWithAttributes(ResultSet resultSet)
            throws
            SQLException {
        ConsentResource consentResource = setDataToConsentResource(resultSet);
        Map<String, String> retrievedConsentAttributeMap = new HashMap<>();
        // Point the cursor to the beginning of the result set to read attributes
        resultSet.beforeFirst();
        while (resultSet.next()) {
            retrievedConsentAttributeMap.put(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY),
                    resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE));
        }
        consentResource.setConsentAttributes(retrievedConsentAttributeMap);

        return consentResource;
    }

    /**
     * Set data from the result set to DetailedConsentResource object.
     *
     * @param resultSet result set
     * @return detailedConsentResource consent resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    public static DetailedConsentResource setDataToDetailedConsentResource(ResultSet resultSet) throws
            SQLException,
            JsonProcessingException {

        Map<String, String> consentAttributesMap = new HashMap<>();
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        ArrayList<String> authIds = new ArrayList<>();
        ArrayList<String> consentMappingIds = new ArrayList<>();
        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();

        while (resultSet.next()) {
            detailedConsentResource = setConsentDataToDetailedConsentResource(resultSet);
            // Set data related to consent attributes
            if (StringUtils.isNotBlank(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY))) {
                String attributeValue = resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE);

                // skip adding all temporary session data to consent attributes
                if (!(JSONValue.isValidJson(attributeValue) &&
                        attributeValue.contains(ConsentMgtDAOConstants.SESSION_DATA_KEY))) {
                    consentAttributesMap.put(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY),
                            attributeValue);
                }
            }

            // Set data related to authorization resources
            if (authIds.isEmpty()) {
                if (resultSet.getString(ConsentMgtDAOConstants.AUTH_ID) != null) {
                    AuthorizationResource authorizationResource = setAuthorizationData(resultSet,
                            ConsentMgtDAOConstants.AUTH_UPDATED_TIME);

                    authorizationResources.add(authorizationResource);
                    authIds.add(authorizationResource.getAuthorizationID());
                }

            } else {
                if (!authIds.contains(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID))) {
                    if (resultSet.getString(ConsentMgtDAOConstants.AUTH_ID) != null) {

                        AuthorizationResource authorizationResource = setAuthorizationData(resultSet,
                                ConsentMgtDAOConstants.AUTH_UPDATED_TIME);

                        authorizationResources.add(authorizationResource);
                        authIds.add(authorizationResource.getAuthorizationID());
                    }
                }
            }

            // Set data related to consent account mappings
            // Check whether consentMappingIds is empty and result set consists a mapping id since at this moment
            //  there can be a situation where an auth resource is created and mapping resource is not created
            if (consentMappingIds.isEmpty() && resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null) {
                ConsentMappingResource consentMappingResource = getConsentMappingResourceWithData(resultSet);

                consentMappingResources.add(consentMappingResource);
                consentMappingIds.add(consentMappingResource.getMappingID());
            } else {
                // Check whether result set consists a mapping id since at this moment, there can be a situation
                //  where an auth resource is created and mapping resource is not created
                if (!consentMappingIds.contains(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID)) &&
                        resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null) {
                    ConsentMappingResource consentMappingResource = getConsentMappingResourceWithData(resultSet);

                    consentMappingResources.add(consentMappingResource);
                    consentMappingIds.add(consentMappingResource.getMappingID());
                }
            }
        }

        // Set consent attributes, auth resources and account mappings to detailed consent resource
        detailedConsentResource.setConsentAttributes(consentAttributesMap);
        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setConsentMappingResources(consentMappingResources);
        return detailedConsentResource;
    }


    /**
     * Set data from the result set to DetailedConsentResource object.
     *
     * @param resultSet result set
     * @return detailedConsentResource consent resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    public static DetailedConsentResource setDataToConsentResourceWithAuthorizationResource(ResultSet resultSet) throws
            SQLException,
            JsonProcessingException {

        Map<String, String> consentAttributesMap = new HashMap<>();
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        ArrayList<String> authIds = new ArrayList<>();
        ArrayList<String> consentMappingIds = new ArrayList<>();
        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();

        while (resultSet.next()) {
            detailedConsentResource = setConsentDataToDetailedConsentResource(resultSet);
            // Set data related to consent attributes
//            if (StringUtils.isNotBlank(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY))) {
//                String attributeValue = resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE);
//
//                // skip adding all temporary session data to consent attributes
//                if (!(JSONValue.isValidJson(attributeValue) &&
//                        attributeValue.contains(ConsentMgtDAOConstants.SESSION_DATA_KEY))) {
//                    consentAttributesMap.put(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY),
//                            attributeValue);
//                }
//            }

            // Set data related to authorization resources
            if (authIds.isEmpty()) {
                AuthorizationResource authorizationResource = setAuthorizationData(resultSet,
                        ConsentMgtDAOConstants.AUTH_UPDATED_TIME);

                authorizationResources.add(authorizationResource);
                authIds.add(authorizationResource.getAuthorizationID());
            } else {
                if (!authIds.contains(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID))) {
                    AuthorizationResource authorizationResource = setAuthorizationData(resultSet,
                            ConsentMgtDAOConstants.AUTH_UPDATED_TIME);

                    authorizationResources.add(authorizationResource);
                    authIds.add(authorizationResource.getAuthorizationID());
                }
            }

            // Set data related to consent account mappings
            // Check whether consentMappingIds is empty and result set consists a mapping id since at this moment
            //  there can be a situation where an auth resource is created and mapping resource is not created
            if (consentMappingIds.isEmpty() && resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null) {
                ConsentMappingResource consentMappingResource = getConsentMappingResourceWithData(resultSet);

                consentMappingResources.add(consentMappingResource);
                consentMappingIds.add(consentMappingResource.getMappingID());
            } else {
                // Check whether result set consists a mapping id since at this moment, there can be a situation
                //  where an auth resource is created and mapping resource is not created
                if (!consentMappingIds.contains(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID)) &&
                        resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null) {
                    ConsentMappingResource consentMappingResource = getConsentMappingResourceWithData(resultSet);

                    consentMappingResources.add(consentMappingResource);
                    consentMappingIds.add(consentMappingResource.getMappingID());
                }
            }
        }

        // Set consent attributes, auth resources and account mappings to detailed consent resource
        detailedConsentResource.setConsentAttributes(consentAttributesMap);
        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setConsentMappingResources(consentMappingResources);
        return detailedConsentResource;
    }

    /**
     * Set consent data from the result set to DetailedConsentResource object.
     *
     * @param resultSet result set
     * @return detailedConsentResource consent resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    public static DetailedConsentResource setConsentDataToDetailedConsentResource(ResultSet resultSet)
            throws
            SQLException {

        return new DetailedConsentResource(
                resultSet.getString(ConsentMgtDAOConstants.ORG_ID),
                resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID),
                resultSet.getString(ConsentMgtDAOConstants.CLIENT_ID),
                resultSet.getString(ConsentMgtDAOConstants.RECEIPT),
                resultSet.getString(ConsentMgtDAOConstants.CONSENT_TYPE),
                resultSet.getString(ConsentMgtDAOConstants.CURRENT_STATUS),
                resultSet.getInt(ConsentMgtDAOConstants.CONSENT_FREQUENCY),
                resultSet.getLong(ConsentMgtDAOConstants.VALIDITY_TIME),
                resultSet.getLong(ConsentMgtDAOConstants.CONSENT_CREATED_TIME),
                resultSet.getLong(ConsentMgtDAOConstants.CONSENT_UPDATED_TIME),
                resultSet.getBoolean(ConsentMgtDAOConstants.RECURRING_INDICATOR),
                new HashMap<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    /**
     * Set data from the result set to AuthorizationResource object.
     *
     * @param resultSet           result set
     * @param updateTimeParamName update time parameter name
     * @return authorizationResource authorization resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    public static AuthorizationResource setAuthorizationDataWithConsentMapping(ResultSet resultSet,
                                                                               String updateTimeParamName)
            throws
            SQLException,
            JsonProcessingException {


        AuthorizationResource authorizationResource = new AuthorizationResource(
                resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID),
                resultSet.getString(ConsentMgtDAOConstants.USER_ID),
                resultSet.getString(ConsentMgtDAOConstants.AUTH_STATUS),
                resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE),
                resultSet.getLong(updateTimeParamName)
        );
        authorizationResource.setAuthorizationID(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID));

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingStatus(resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS));
        consentMappingResource.setMappingID(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID));
        ObjectMapper objectMapper = new ObjectMapper();



        String resource = resultSet.getString(ConsentMgtDAOConstants.RESOURCE);
        if (resource != null) {
            // Convert JSON string to a Map
            Map<String, Object> map = objectMapper.readValue(resource, new TypeReference<Map<String, Object>>() {
            });
            // Convert Map to net.minidev.json.JSONObject
            JSONObject jsonObject = new JSONObject(map);

            consentMappingResource.setResource(jsonObject);
        }

        consentMappingResources.add(consentMappingResource);

        while (resultSet.next()) {
            consentMappingResource.setMappingStatus(resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS));
            consentMappingResource.setMappingID(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID));

            consentMappingResource.setResource(new JSONObject(objectMapper.readValue(resultSet.
                    getString(ConsentMgtDAOConstants.RESOURCE), new TypeReference<Map<String, Object>>() {
            })));
            consentMappingResources.add(consentMappingResource);


        }
        authorizationResource.setConsentMappingResource(consentMappingResources);
        return authorizationResource;
    }

    /**
     * Set data from the result set to AuthorizationResource object.
     *
     * @param resultSet           result set
     * @param updateTimeParamName update time parameter name
     * @return authorizationResource authorization resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    public static AuthorizationResource setAuthorizationData(ResultSet resultSet,
                                                             String updateTimeParamName)
            throws
            SQLException {

        AuthorizationResource authorizationResource = new AuthorizationResource(
                resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID),
                resultSet.getString(ConsentMgtDAOConstants.USER_ID),
                resultSet.getString(ConsentMgtDAOConstants.AUTH_STATUS),
                resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE),
                resultSet.getLong(updateTimeParamName)
        );
        authorizationResource.setAuthorizationID(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID));


        return authorizationResource;
    }

    /**
     * Return a consent mapping resource with data set from the result set.
     *
     * @param resultSet result set
     * @return a consent mapping resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    public static ConsentMappingResource getConsentMappingResourceWithData(ResultSet resultSet) throws
            SQLException,
            JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        ConsentMappingResource consentMappingResource = new ConsentMappingResource(
                resultSet.getString(ConsentMgtDAOConstants.AUTH_ID),


                new JSONObject(objectMapper.readValue(resultSet.
                        getString(ConsentMgtDAOConstants.RESOURCE), new TypeReference<Map<String, Object>>() {
                })),
                resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS)
        );
        consentMappingResource.setMappingID(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID));

        return consentMappingResource;
    }

    /**
     * Construct the where clause of thr prepared statement for consent search.
     *
     * @param applicableConditions map of applicable conditions
     * @return where clause of the prepared statement
     */
    public static String constructConsentSearchPreparedStatement(Map<String, ArrayList<String>> applicableConditions) {

        StringBuilder placeHoldersBuilder = new StringBuilder();
        StringBuilder whereClauseBuilder = new StringBuilder();
        whereClauseBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.WHERE));
        // If all lists are empty or null, return the default term "where"
        if (MapUtils.isEmpty(applicableConditions)) {
            return "";
        }
        for (Map.Entry<String, ArrayList<String>> entry : applicableConditions.entrySet()) {
            // Oracle only allows 1000 values to be used in a SQL "IN" clause. Since more than 1000 consent IDs
            // are used in some queries, "OR" clause is used
            if (entry.getKey().contains(ConsentMgtDAOConstants.CONSENT_ID)) {
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
                        whereClauseBuilder.length(),
                        DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.AND));
            } else {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    placeHoldersBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.PLACEHOLDER));
                }
                String placeHolders = StringUtils.removeEnd(placeHoldersBuilder.toString(),
                        COMMA);
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
                placeHoldersBuilder.delete(0,
                        placeHoldersBuilder.length());
            }
        }
        int size = whereClauseBuilder.length();
        //removing the last AND in the statement
        whereClauseBuilder.replace(size - 3,
                size,
                "");
        return whereClauseBuilder.toString();
    }

    /**
     * Construct the filter condition of the prepared statement for consent search.
     *
     * @param userIds map of user IDs
     * @return filter condition of the prepared statement
     */
    public static String constructUserIdListFilterCondition(Map<String, ArrayList<String>> userIds) {

        StringBuilder placeHoldersBuilder = new StringBuilder();
        StringBuilder userIdFilterBuilder = new StringBuilder();

        for (Map.Entry<String, ArrayList<String>> entry : userIds.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                placeHoldersBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.PLACEHOLDER));
            }
            String placeHolders = StringUtils.removeEnd(placeHoldersBuilder.toString(),
                    COMMA);
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
            placeHoldersBuilder.delete(0,
                    placeHoldersBuilder.length());
        }
        return userIdFilterBuilder.toString();
    }

    /**
     * Determine the order of parameters to set in the prepared statement.
     *
     * @param preparedStatement       dynamically constructed prepared statement
     * @param applicableConditionsMap map of applicable conditions
     * @param columnsMap              map of columns
     * @return ordered parameters map
     */
    public static TreeMap<Integer, ArrayList<String>> determineOrderOfParamsToSet(String preparedStatement,
                                                                                  Map<String, ArrayList<String>>
                                                                                          applicableConditionsMap,
                                                                                  Map<String, String> columnsMap) {

        int indexOfConsentIDsList;
        int indexOfClientIdsList;
        int indexOfConsentTypesList;
        int indexOfConsentStatusesList;
        int indexOfUserIDsList;

        // Tree map naturally sorts values in ascending order according to the key
        TreeMap<Integer, ArrayList<String>> sortedIndexesMap = new TreeMap<>();

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
        if (preparedStatement.contains(columnsMap.get(ConsentMgtDAOConstants.ORG_ID))) {
            indexOfConsentStatusesList = preparedStatement
                    .indexOf(columnsMap.get(ConsentMgtDAOConstants.ORG_ID));
            sortedIndexesMap.put(indexOfConsentStatusesList,
                    applicableConditionsMap.get(columnsMap.get(ConsentMgtDAOConstants.ORG_ID)));
        }
        if (preparedStatement.contains(columnsMap.get(ConsentMgtDAOConstants.USER_IDS))) {
            indexOfUserIDsList = preparedStatement.indexOf(columnsMap.get(ConsentMgtDAOConstants.USER_IDS));
            sortedIndexesMap.put(indexOfUserIDsList,
                    applicableConditionsMap.get(columnsMap.get(ConsentMgtDAOConstants.USER_IDS)));
        }
        return sortedIndexesMap;
    }

    /**
     * Sets search parameters to dynamically constructed prepared statement. The outer loop is used to iterate the
     * different AND clauses and the inner loop is to iterate the number of placeholders of the current AND clause.
     *
     * @param preparedStatement dynamically constructed prepared statement
     * @param orderedParamsMap  map with ordered AND conditions
     * @param parameterIndex    index which the parameter should be set
     * @return the final parameter index
     * @throws SQLException thrown if an error occurs in the process
     */
    public static int setDynamicConsentSearchParameters(PreparedStatement preparedStatement, Map<Integer,
            ArrayList<String>> orderedParamsMap, int parameterIndex) throws
            SQLException {

        for (Map.Entry<Integer, ArrayList<String>> entry : orderedParamsMap.entrySet()) {
            for (int valueIndex = 0; valueIndex < entry.getValue().size(); valueIndex++) {
                preparedStatement.setString(parameterIndex,
                        entry.getValue().get(valueIndex).trim());
                parameterIndex++;
            }
        }
        return parameterIndex;
    }

    /**
     * Get the size of the result set.
     *
     * @param resultSet result set
     * @return size of the result set
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    public static int getResultSetSize(ResultSet resultSet) throws
            SQLException {

        resultSet.last();
        int resultSetSize = resultSet.getRow();

        // Point result set back before first
        resultSet.beforeFirst();
        return resultSetSize;
    }

    /**
     * Construct the where clause of the prepared statement for consent auth search.
     *
     * @param applicableConditions map of applicable conditions
     * @return where clause of the prepared statement
     */
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
     * Generate the tableID based on the type of the consent data record to be stored in consent history table.
     *
     * @param consentDataType A predefined consent data category based on each consent database table
     * @return A identifier assigned for the relevant consent database table
     */
    public static String generateConsentTableId(String consentDataType) throws
            ConsentDataInsertionException {

        String tableId;
        if (ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA.equals(consentDataType)) {
            tableId = ConsentMgtDAOConstants.TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_FS_CONSENT);
        } else if (ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA.equals(consentDataType)) {
            tableId = ConsentMgtDAOConstants.TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_FS_CONSENT_AUTH_RESOURCE);
        } else if (ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA.equals(consentDataType)) {
            tableId = ConsentMgtDAOConstants.TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_FS_CONSENT_ATTRIBUTE);
        } else if (ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA.equals(consentDataType)) {
            tableId = ConsentMgtDAOConstants.TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_FS_CONSENT_MAPPING);
        } else {
            log.error(String.format("Can not find a table matching to the provided consentDataType : %s",
                    consentDataType.replaceAll("[\r\n]",
                            "")));
            throw new ConsentDataInsertionException("Error occurred while preparing to store consent amendment " +
                    "history data. Invalid consentDataType provided");
        }
        return tableId;
    }

    /**
     * Method to construct where clause for consent status audit search condition.
     *
     * @param consentIDs List of consent IDs
     * @return Filter condition for consent status audit
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
                    whereClauseBuilder.length(),
                    StringUtils.SPACE);
        }
        return whereClauseBuilder.toString();
    }

    /**
     * Construct the where clause of the prepared statement for consent history search.
     *
     * @param recordIdCount count of record IDs
     * @return where clause of the prepared statement
     */
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

    /**
     * construct a data map that includes the changed attributes of each consent amendment history entry and.
     * return a map of ConsentHistoryResources including this changed attributes data map
     *
     * @param consentId consent Id
     * @param resultSet result set
     * @return a map of ConsentHistoryResources
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    public static Map<String, ConsentHistoryResource> constructConsentHistoryRetrievalResult(String consentId,
                                                                                             ResultSet resultSet)
            throws
            SQLException {

        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();

        while (resultSet.next()) {
            String tableID = resultSet.getString(ConsentMgtDAOConstants.TABLE_ID);
            String recordID = resultSet.getString(ConsentMgtDAOConstants.RECORD_ID);
            String historyId = resultSet.getString(ConsentMgtDAOConstants.HISTORY_ID);
            String changedAttributesString = resultSet.getString(ConsentMgtDAOConstants.CHANGED_VALUES);
            String amendmentReason = resultSet.getString(ConsentMgtDAOConstants.REASON);
            long timestamp = resultSet.getLong(ConsentMgtDAOConstants.EFFECTIVE_TIMESTAMP);

            ConsentHistoryResource consentHistoryResource;
            Map<String, Object> changedAttributesJsonDataMap;
            if (consentAmendmentHistoryDataMap.containsKey(historyId)) {
                consentHistoryResource = consentAmendmentHistoryDataMap.get(historyId);
            } else {
                consentHistoryResource = new ConsentHistoryResource(consentId,
                        historyId);
                consentHistoryResource.setTimestamp(timestamp);
                consentHistoryResource.setReason(amendmentReason);
            }

            changedAttributesJsonDataMap = consentHistoryResource.getChangedAttributesJsonDataMap();

            if (ConsentMgtDAOConstants.TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_FS_CONSENT)
                    .equals(tableID)) {
                changedAttributesJsonDataMap.put(ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA,
                        changedAttributesString);
            } else if (ConsentMgtDAOConstants.TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_FS_CONSENT_ATTRIBUTE)
                    .equals(tableID)) {
                changedAttributesJsonDataMap.put(ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA,
                        changedAttributesString);
            } else if (ConsentMgtDAOConstants.TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_FS_CONSENT_AUTH_RESOURCE)
                    .equals(tableID)) {
                Map<String, Object> consentAuthResources;
                if (changedAttributesJsonDataMap.containsKey(ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA)) {
                    consentAuthResources = (Map<String, Object>) changedAttributesJsonDataMap
                            .get(ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA);
                } else {
                    consentAuthResources = new HashMap<>();
                }
                consentAuthResources.put(recordID,
                        changedAttributesString);
                changedAttributesJsonDataMap.put(ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA,
                        consentAuthResources);
            } else if (ConsentMgtDAOConstants.TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_FS_CONSENT_MAPPING)
                    .equals(tableID)) {
                Map<String, Object> consentMappingResources;
                if (changedAttributesJsonDataMap.containsKey(ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA)) {
                    consentMappingResources = (Map<String, Object>) changedAttributesJsonDataMap
                            .get(ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA);
                } else {
                    consentMappingResources = new HashMap<>();
                }
                consentMappingResources.put(recordID,
                        changedAttributesString);
                changedAttributesJsonDataMap.put(ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA,
                        consentMappingResources);
            } else {
                log.error(String.format("The retrieved tableId : %s has no corresponding consent data type to be" +
                                " matched",
                        tableID.replaceAll("[\r\n]",
                                "")));
            }
            consentHistoryResource.setChangedAttributesJsonDataMap(changedAttributesJsonDataMap);
            consentAmendmentHistoryDataMap.put(historyId,
                    consentHistoryResource);
        }
        return consentAmendmentHistoryDataMap;
    }

    /**
     * Method to construct excluded statuses search condition.
     *
     * @param statusesEligibleForExpiration List of statuses eligible for expiration
     * @return Filter condition for excluded statuses
     */
    public static String constructStatusesEligibleForExpirationCondition(List<String> statusesEligibleForExpiration) {

        StringBuilder placeHoldersBuilder = new StringBuilder();
        StringBuilder statusesEligibleForExpirationFilterBuilder = new StringBuilder();

        for (int i = 0; i < statusesEligibleForExpiration.size(); i++) {
            placeHoldersBuilder.append(DB_OPERATORS_MAP.get(ConsentMgtDAOConstants.PLACEHOLDER));
        }
        String placeHolders = StringUtils.removeEnd(placeHoldersBuilder.toString(),
                COMMA);
        statusesEligibleForExpirationFilterBuilder
                .append(SPACE)
                .append(LEFT_PARENTHESIS)
                .append(placeHolders)
                .append(RIGHT_PARENTHESIS)
                .append(SPACE);
        // Delete all content from old string builder except the starting left parenthesis
        placeHoldersBuilder.delete(0,
                placeHoldersBuilder.length());
        return statusesEligibleForExpirationFilterBuilder.toString();
    }
}
