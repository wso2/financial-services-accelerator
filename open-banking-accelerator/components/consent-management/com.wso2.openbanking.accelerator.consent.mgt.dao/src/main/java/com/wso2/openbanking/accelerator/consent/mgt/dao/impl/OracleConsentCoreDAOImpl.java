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

package com.wso2.openbanking.accelerator.consent.mgt.dao.impl;

import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataRetrievalException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.queries.ConsentMgtOracleDBQueries;
import com.wso2.openbanking.accelerator.consent.mgt.dao.utils.ConsentDAOUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * DAO implementation for Oracle specific methods.
 */
public class OracleConsentCoreDAOImpl extends ConsentCoreDAOImpl {

    private static Log log = LogFactory.getLog(OracleConsentCoreDAOImpl.class);
    private static final String GROUP_BY_SEPARATOR = "\\|\\|";
    static final Map<String, String> COLUMNS_MAP = new HashMap<String, String>() {
        {
            put(ConsentMgtDAOConstants.CONSENT_IDS, "OBC.CONSENT_ID");
            put(ConsentMgtDAOConstants.CLIENT_IDS, "OBC.CLIENT_ID");
            put(ConsentMgtDAOConstants.CONSENT_TYPES, "OBC.CONSENT_TYPE");
            put(ConsentMgtDAOConstants.CONSENT_STATUSES, "OBC.CURRENT_STATUS");
            put(ConsentMgtDAOConstants.USER_IDS, "OCAR.USER_ID");
        }
    };

    public OracleConsentCoreDAOImpl(ConsentMgtOracleDBQueries sqlStatements) {

        super(sqlStatements);
    }

    @Override
    public ArrayList<DetailedConsentResource> searchConsents(Connection connection, ArrayList<String> consentIDs,
                                                             ArrayList<String> clientIDs,
                                                             ArrayList<String> consentTypes,
                                                             ArrayList<String> consentStatuses,
                                                             ArrayList<String> userIDs, Long fromTime,
                                                             Long toTime, Integer limit, Integer offset)
            throws OBConsentDataRetrievalException {

        boolean shouldLimit = true;
        boolean shouldOffset = true;
        int parameterIndex = 0;
        Map<String, ArrayList> applicableConditionsMap = new HashMap<>();

        validateAndSetSearchConditions(applicableConditionsMap, consentIDs, clientIDs, consentTypes, consentStatuses);

        if (limit == null) {
            shouldLimit = false;
        }
        if (offset == null) {
            shouldOffset = false;
        }

        // logic to set the prepared statement
        String constructedConditions =
                ConsentDAOUtils.constructConsentSearchPreparedStatement(applicableConditionsMap);

        String userIDFilterCondition = "";
        Map<String, ArrayList> userIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userIDs)) {
            userIdMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.USER_IDS), userIDs);
            userIDFilterCondition = ConsentDAOUtils.constructUserIdListFilterCondition(userIdMap);
        }
        String searchConsentsPreparedStatement =
                sqlStatements.getSearchConsentsPreparedStatement(constructedConditions, shouldLimit,
                        shouldOffset, userIDFilterCondition);

        try (PreparedStatement searchConsentsPreparedStmt =
                     connection.prepareStatement(searchConsentsPreparedStatement, ResultSet.TYPE_SCROLL_INSENSITIVE,
                             ResultSet.CONCUR_UPDATABLE)) {

            //determine order of user Ids to set
            if (CollectionUtils.isNotEmpty(userIDs)) {
                Map<Integer, ArrayList> orderedUserIdsMap = ConsentDAOUtils
                        .determineOrderOfParamsToSet(userIDFilterCondition, userIdMap, COLUMNS_MAP);
                parameterIndex = setDynamicConsentSearchParameters(searchConsentsPreparedStmt, orderedUserIdsMap,
                        ++parameterIndex);
                parameterIndex = parameterIndex - 1;
            }

            /* Since we don't know the order of the set condition clauses, have to determine the order of them to set
               the actual values to the  prepared statement */
            Map<Integer, ArrayList> orderedParamsMap = ConsentDAOUtils
                    .determineOrderOfParamsToSet(constructedConditions, applicableConditionsMap, COLUMNS_MAP);
            parameterIndex = setDynamicConsentSearchParameters(searchConsentsPreparedStmt, orderedParamsMap,
                    ++parameterIndex);
            parameterIndex = parameterIndex - 1;

            log.debug("Setting parameters to prepared statement to search consents");

            if (fromTime != null) {
                searchConsentsPreparedStmt.setLong(++parameterIndex, fromTime);
            } else {
                searchConsentsPreparedStmt.setNull(++parameterIndex, Types.BIGINT);
            }

            if (toTime != null) {
                searchConsentsPreparedStmt.setLong(++parameterIndex, toTime);
            } else {
                searchConsentsPreparedStmt.setNull(++parameterIndex, Types.BIGINT);
            }

            if (offset != null && limit != null) {
                searchConsentsPreparedStmt.setInt(++parameterIndex, offset);
            }
            if (limit != null) {
                searchConsentsPreparedStmt.setInt(++parameterIndex, limit);
            }

            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();

            try (ResultSet resultSet = searchConsentsPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    int resultSetSize = getResultSetSize(resultSet);
                    detailedConsentResources = constructDetailedConsentsSearchResult(resultSet, resultSetSize);
                }
                return detailedConsentResources;
            } catch (SQLException e) {
                log.error("Error occurred while searching detailed consent resources", e);
                throw new OBConsentDataRetrievalException("Error occurred while searching detailed " +
                        "consent resources", e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_SEARCH_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_SEARCH_ERROR_MSG, e);
        }
    }

    ArrayList<DetailedConsentResource> constructDetailedConsentsSearchResult(ResultSet resultSet, int resultSetSize)
            throws SQLException {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();

        while (resultSet.next()) {

            Map<String, String> consentAttributesMap = new HashMap<>();
            ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
            ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
            DetailedConsentResource detailedConsentResource = new DetailedConsentResource();

            setConsentDataToDetailedConsentInSearchResponse(resultSet, detailedConsentResource);

            // Set consent attributes to map if available
            if (resultSet.getString(ConsentMgtDAOConstants.ATT_KEY) != null &&
                    StringUtils.isNotBlank(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY)
                            .replaceAll(GROUP_BY_SEPARATOR, ""))) {
                // fetch attribute keys and values from group_concat
                String[] attKeys = resultSet.getString(ConsentMgtDAOConstants.ATT_KEY).split(GROUP_BY_SEPARATOR);
                String[] attValues = resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE).split(GROUP_BY_SEPARATOR);
                // check if all attribute keys has values
                if (attKeys.length == attValues.length) {
                    for (int index = 0; index < attKeys.length; index++) {
                        if (!attKeys[index].isEmpty()) {
                            consentAttributesMap.put(attKeys[index], attValues[index]);
                        }
                    }
                }
            }
            // Set authorization data
            setAuthorizationDataInResponseForGroupedQuery(authorizationResources, resultSet,
                    detailedConsentResource.getConsentID());
            // Set consent account mapping data if available
            setAccountConsentMappingDataInResponse(consentMappingResources, resultSet);

            detailedConsentResource.setConsentAttributes(consentAttributesMap);
            detailedConsentResource.setAuthorizationResources(authorizationResources);
            detailedConsentResource.setConsentMappingResources(consentMappingResources);

            detailedConsentResources.add(detailedConsentResource);

        }
        return detailedConsentResources;
    }

    void setConsentDataToDetailedConsentInSearchResponse(ResultSet resultSet,
                                                         DetailedConsentResource detailedConsentResource)
            throws SQLException {

        Optional<String> consentId = Arrays.stream(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID)
                .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> clientId = Arrays.stream(resultSet.getString(ConsentMgtDAOConstants.CLIENT_ID)
                .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> receipt = Arrays.stream(resultSet.getString(ConsentMgtDAOConstants.RECEIPT)
                .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> createdTime = Arrays.stream(resultSet.getString(ConsentMgtDAOConstants.CONSENT_CREATED_TIME)
                .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> consentUpdatedTime = Arrays.stream(
                resultSet.getString(ConsentMgtDAOConstants.CONSENT_UPDATED_TIME)
                        .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> consentType = Arrays.stream(resultSet.getString(ConsentMgtDAOConstants.CONSENT_TYPE)
                .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> currentStatus = Arrays.stream(resultSet.getString(ConsentMgtDAOConstants.CURRENT_STATUS)
                .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> frequency = Arrays.stream(resultSet.getString(ConsentMgtDAOConstants.CONSENT_FREQUENCY)
                .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> validityTime = Arrays.stream(resultSet.getString(ConsentMgtDAOConstants.VALIDITY_TIME)
                .split(GROUP_BY_SEPARATOR)).distinct().findFirst();
        Optional<String> recurringIndicator = Arrays.stream(
                resultSet.getString(ConsentMgtDAOConstants.RECURRING_INDICATOR)
                        .split(GROUP_BY_SEPARATOR)).distinct().findFirst();

        if (consentId.isPresent() && clientId.isPresent()) {
            detailedConsentResource.setConsentID(consentId.get());
            detailedConsentResource.setClientID(clientId.get());
        } else {
            throw new SQLException("CLIENT_ID and CONSENT_ID could not be null.");
        }
        receipt.ifPresent(detailedConsentResource::setReceipt);
        consentType.ifPresent(detailedConsentResource::setConsentType);
        currentStatus.ifPresent(detailedConsentResource::setCurrentStatus);
        createdTime.ifPresent(e -> detailedConsentResource.setCreatedTime(Long.parseLong(e)));
        consentUpdatedTime.ifPresent(e -> detailedConsentResource.setUpdatedTime(Long.parseLong(e)));
        frequency.ifPresent(e -> detailedConsentResource.setConsentFrequency(Integer.parseInt(e)));
        validityTime.ifPresent(e -> detailedConsentResource.setValidityPeriod(Long.parseLong(e)));
        recurringIndicator.ifPresent(e -> detailedConsentResource.setRecurringIndicator(Boolean.parseBoolean(e)));
    }

    protected void setAuthorizationDataInResponseForGroupedQuery(ArrayList<AuthorizationResource>
                                                                         authorizationResources,
                                                                 ResultSet resultSet, String consentId)
            throws SQLException {

        //identify duplicate auth data
        Set<String> authIdSet = new HashSet<>();

        // fetch values from group_concat
        String[] authIds = resultSet.getString(ConsentMgtDAOConstants.AUTH_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] authTypes = resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE).split(GROUP_BY_SEPARATOR) : null;
        String[] authStatues = resultSet.getString(ConsentMgtDAOConstants.AUTH_STATUS) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_STATUS).split(GROUP_BY_SEPARATOR) : null;
        String[] updatedTimes = resultSet.getString(ConsentMgtDAOConstants.UPDATED_TIME) != null ?
                resultSet.getString(ConsentMgtDAOConstants.UPDATED_TIME).split(GROUP_BY_SEPARATOR) : null;
        String[] userIds = resultSet.getString(ConsentMgtDAOConstants.USER_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.USER_ID).split(GROUP_BY_SEPARATOR) : null;

        for (int index = 0; index < (authIds != null ? authIds.length : 0); index++) {
            if (!authIdSet.contains(authIds[index])) {
                AuthorizationResource authorizationResource = new AuthorizationResource();
                authIdSet.add(authIds[index]);
                authorizationResource.setAuthorizationID(authIds[index]);
                authorizationResource.setConsentID(consentId);
                if (authTypes != null && authTypes.length > index) {
                    authorizationResource.setAuthorizationType(authTypes[index]);
                }
                if (authStatues != null && authStatues.length > index) {
                    authorizationResource.setAuthorizationStatus(authStatues[index]);
                }
                if (updatedTimes != null && updatedTimes.length > index) {
                    authorizationResource.setUpdatedTime(Long.parseLong(updatedTimes[index]));
                }
                if (userIds != null && userIds.length > index) {
                    authorizationResource.setUserID(userIds[index]);
                }
                authorizationResources.add(authorizationResource);
            }
        }

    }

    protected void setAccountConsentMappingDataInResponse(ArrayList<ConsentMappingResource> consentMappingResources,
                                                          ResultSet resultSet) throws SQLException {

        //identify duplicate mappingIds
        Set<String> mappingIdSet = new HashSet<>();

        // fetch values from group_concat
        String[] authIds = resultSet.getString(ConsentMgtDAOConstants.AUTH_MAPPING_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_MAPPING_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] mappingIds = resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] accountIds = resultSet.getString(ConsentMgtDAOConstants.ACCOUNT_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.ACCOUNT_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] mappingStatues = resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS) != null ?
                resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS).split(GROUP_BY_SEPARATOR) : null;
        String[] permissions = resultSet.getString(ConsentMgtDAOConstants.PERMISSION) != null ?
                resultSet.getString(ConsentMgtDAOConstants.PERMISSION).split(GROUP_BY_SEPARATOR) : null;

        for (int index = 0; index < (mappingIds != null ? mappingIds.length : 0); index++) {
            if (!mappingIdSet.contains(mappingIds[index])) {
                ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                if (authIds != null && authIds.length > index) {
                    consentMappingResource.setAuthorizationID(authIds[index]);
                }
                consentMappingResource.setMappingID(mappingIds[index]);
                if (accountIds != null && accountIds.length > index) {
                    consentMappingResource.setAccountID(accountIds[index]);
                }
                if (mappingStatues != null && mappingStatues.length > index) {
                    consentMappingResource.setMappingStatus(mappingStatues[index]);
                }
                if (permissions != null && permissions.length > index) {
                    consentMappingResource.setPermission(permissions[index]);
                }
                consentMappingResources.add(consentMappingResource);
                mappingIdSet.add(mappingIds[index]);
            }
        }

    }

    void validateAndSetSearchConditions(Map<String, ArrayList> applicableConditionsMap, ArrayList<String> consentIDs,
                                        ArrayList<String> clientIDs,
                                        ArrayList<String> consentTypes,
                                        ArrayList<String> consentStatuses) {

        log.debug("Validate applicable search conditions");

        if (CollectionUtils.isNotEmpty(consentIDs)) {
            applicableConditionsMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_IDS), consentIDs);
        }
        if (CollectionUtils.isNotEmpty(clientIDs)) {
            applicableConditionsMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.CLIENT_IDS), clientIDs);
        }
        if (CollectionUtils.isNotEmpty(consentTypes)) {
            applicableConditionsMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_TYPES), consentTypes);
        }
        if (CollectionUtils.isNotEmpty(consentStatuses)) {
            applicableConditionsMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_STATUSES), consentStatuses);
        }
    }
}
