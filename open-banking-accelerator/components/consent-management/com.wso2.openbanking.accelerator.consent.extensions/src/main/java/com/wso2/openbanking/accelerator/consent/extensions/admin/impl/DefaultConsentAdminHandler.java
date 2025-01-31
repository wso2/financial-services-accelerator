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

package com.wso2.openbanking.accelerator.consent.extensions.admin.impl;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminData;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminHandler;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.util.jobs.ExpiredConsentStatusUpdateJob;
import com.wso2.openbanking.accelerator.consent.extensions.util.jobs.RetentionDatabaseSyncJob;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentFile;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.handler.EventNotificationPersistenceServiceHandler;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Consent admin handler default implementation.
 */
public class DefaultConsentAdminHandler implements ConsentAdminHandler {
    private static final Log log = LogFactory.getLog(DefaultConsentAdminHandler.class);
    private static final String AUTHORISED = "authorised";
    private static final String FETCH_FROM_RETENTION_DB_QUERY_PARAM = "fetchFromRetentionDatabase";
    public static final String ACCOUNT_IDS_QUERY_PARAM_NAME = "accountIDs";
    public static final String ACCOUNT_ID = "accountId";
    public static final String DATA = "data";
    public static final String TOTAL = "total";
    public static final String COUNT = "count";
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";
    public static final String METADATA = "metadata";
    public static final String CONSENT_MAPPING_RESOURCES = "consentMappingResources";


    @Override
    public void handleSearch(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();

        ArrayList<String> consentIDs = null;
        ArrayList<String> clientIDs = null;
        ArrayList<String> consentTypes = null;
        ArrayList<String> consentStatuses = null;
        ArrayList<String> userIDs = null;
        Long fromTime = null;
        Long toTime = null;
        Integer limit = null;
        Integer offset = null;
        boolean fetchFromRetentionDatabase = false;

        Map queryParams = consentAdminData.getQueryParams();

        if (validateAndGetQueryParam(queryParams, "consentIDs") != null) {
            consentIDs = new ArrayList<>(Arrays.asList(validateAndGetQueryParam(queryParams, "consentIDs").
                    split(",")));
        }
        if (validateAndGetQueryParam(queryParams, "clientIDs") != null) {
            clientIDs = new ArrayList<>(Arrays.asList(validateAndGetQueryParam(queryParams, "clientIDs").
                    split(",")));
        }
        if (validateAndGetQueryParam(queryParams, "consentTypes") != null) {
            consentTypes = new ArrayList<>(Arrays.asList(validateAndGetQueryParam(queryParams, "consentTypes").
                    split(",")));
        }
        if (validateAndGetQueryParam(queryParams, "consentStatuses") != null) {
            consentStatuses = new ArrayList<>(Arrays.asList(validateAndGetQueryParam(queryParams, "consentStatuses").
                    split(",")));

        }
        if (validateAndGetQueryParam(queryParams, "userIDs") != null) {
            userIDs = new ArrayList<>(Arrays.asList(validateAndGetQueryParam(queryParams, "userIDs").
                    split(",")));
        }
        if (validateAndGetQueryParam(queryParams, "fromTime") != null) {
            try {
                fromTime = Long.parseLong(validateAndGetQueryParam(queryParams, "fromTime"));
            } catch (NumberFormatException e) {
                log.error("Number format incorrect in search for parameter fromTime. Ignoring parameter");
            }
        }
        if (validateAndGetQueryParam(queryParams, "toTime") != null) {
            try {
                toTime = Long.parseLong(validateAndGetQueryParam(queryParams, "toTime"));
            } catch (NumberFormatException e) {
                log.error("Number format incorrect in search for parameter toTime. Ignoring parameter");
            }
        }
        if (validateAndGetQueryParam(queryParams, LIMIT) != null) {
            try {
                limit = Integer.parseInt(validateAndGetQueryParam(queryParams, LIMIT));
            } catch (NumberFormatException e) {
                log.error("Number format incorrect in search for parameter limit. Ignoring parameter");
            }
        }
        if (validateAndGetQueryParam(queryParams, OFFSET) != null) {
            try {
                offset = Integer.parseInt(validateAndGetQueryParam(queryParams, OFFSET));
            } catch (NumberFormatException e) {
                log.error("Number format incorrect in search for parameter offset. Ignoring parameter");
            }
        }
        if (validateAndGetQueryParam(queryParams, FETCH_FROM_RETENTION_DB_QUERY_PARAM) != null) {
            fetchFromRetentionDatabase = Boolean.parseBoolean(validateAndGetQueryParam(queryParams,
                    FETCH_FROM_RETENTION_DB_QUERY_PARAM));
        }
        int count, total = 0;

        try {
            ArrayList<DetailedConsentResource> results = ConsentExtensionsDataHolder.getInstance()
                    .getConsentCoreService().searchDetailedConsents(consentIDs, clientIDs, consentTypes,
                            consentStatuses, userIDs, fromTime, toTime, limit, offset, fetchFromRetentionDatabase);
            JSONArray searchResults = new JSONArray();
            for (DetailedConsentResource result : results) {
                searchResults.add(ConsentExtensionUtils.detailedConsentToJSON(result));
            }
            response.appendField(DATA, searchResults);
            count = searchResults.size();
            total = results.size();
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        //retrieve the total of the data set queried
        if (limit != null || offset != null) {
            try {
                ArrayList<DetailedConsentResource> results = ConsentExtensionsDataHolder.getInstance()
                        .getConsentCoreService().searchDetailedConsents(consentIDs, clientIDs, consentTypes,
                                consentStatuses, userIDs, fromTime, toTime, null, null, fetchFromRetentionDatabase);
                total = results.size();
            } catch (ConsentManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        JSONObject metadata = new JSONObject();
        metadata.appendField(COUNT, count);
        metadata.appendField(OFFSET, offset);
        metadata.appendField(LIMIT, limit);
        metadata.appendField(TOTAL, total);

        response.appendField(METADATA, metadata);
        consentAdminData.setResponseStatus(ResponseStatus.OK);
        consentAdminData.setResponsePayload(response);

        // Filter consent data based on the accounts if accounts are available in the query params.
        if (consentAdminData.getQueryParams().containsKey(ACCOUNT_IDS_QUERY_PARAM_NAME)) {
            filterConsentsByAccount(consentAdminData);
        }
    }

    /**
     * Filter the consent data based on the accounts.
     * @param consentAdminData Consent admin data.
     */
    public void filterConsentsByAccount(ConsentAdminData consentAdminData) {

        ArrayList accounts = ((ArrayList) consentAdminData.getQueryParams().get(ACCOUNT_IDS_QUERY_PARAM_NAME));
        if (accounts.size() > 0) {
            JSONArray filteredConsentData = new JSONArray();
            for (Object consentObj : (JSONArray) consentAdminData.getResponsePayload().get(DATA)) {
                JSONObject consent = (JSONObject) consentObj;
                JSONArray consentMappingResources = (JSONArray) consent.get(CONSENT_MAPPING_RESOURCES);
                for (Object consentMappingResource : consentMappingResources) {
                    JSONObject consentMappingResourceObject = (JSONObject) consentMappingResource;
                    if (accounts.contains(consentMappingResourceObject.get(ACCOUNT_ID))) {
                        filteredConsentData.add(consent);
                        break;
                    }
                }
            }
            JSONObject responseMetadata = (JSONObject) consentAdminData.getResponsePayload().get(METADATA);
            responseMetadata.put(TOTAL, filteredConsentData.size());
            responseMetadata.put(COUNT, filteredConsentData.size());
            consentAdminData.getResponsePayload().put(DATA, filteredConsentData);
        }
    }

    private String validateAndGetQueryParam(Map queryParams, String key) {
        if (queryParams.containsKey(key) && (((ArrayList) queryParams.get(key)).get(0) instanceof String)) {
            return (String) ((ArrayList) queryParams.get(key)).get(0);
        }
        return null;
    }

    @Override
    public void handleRevoke(ConsentAdminData consentAdminData) throws ConsentException {

        try {
            Map queryParams = consentAdminData.getQueryParams();

            String consentId = validateAndGetQueryParam(queryParams, "consentID");
            if (consentId == null) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory parameter consent ID not available");
            } else {
                ConsentResource consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentId, false);

                if (!AUTHORISED.equalsIgnoreCase(consentResource.getCurrentStatus())) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            "Consent is not in a revocable status");
                } else {
                    boolean success = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                            .revokeConsentWithReason(validateAndGetQueryParam(queryParams, "consentID"), "revoked",
                                    validateAndGetQueryParam(queryParams, "userID"),
                                    ConsentCoreServiceConstants.CONSENT_REVOKE_FROM_DASHBOARD_REASON);
                    if (success) {
                        // persist a new notification to the DB
                        // This is a sample event notification persisting. This can be modified in the Toolkit level
                        if (OpenBankingConfigParser.getInstance().isRealtimeEventNotificationEnabled()) {
                            JSONObject notificationInfo = new JSONObject();
                            notificationInfo.put("consentID", consentId);
                            notificationInfo.put("status", "Consent Revocation");
                            notificationInfo.put("timeStamp", System.currentTimeMillis());
                            EventNotificationPersistenceServiceHandler.getInstance().persistRevokeEvent(
                                    consentResource.getClientID(), consentId,
                                    "Consent Revocation", notificationInfo);
                        }
                    }
                }
            }
            consentAdminData.setResponseStatus(ResponseStatus.OK);
            consentAdminData.setResponseStatus(ResponseStatus.NO_CONTENT);
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while revoking consents");
        }
    }

    public void handleConsentAmendmentHistoryRetrieval(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();
        String consentID = null;
        Map queryParams = consentAdminData.getQueryParams();

        if (validateAndGetQueryParam(queryParams, "consentId") != null) {
            consentID = validateAndGetQueryParam(queryParams, "consentId");
        }

        if (StringUtils.isBlank(consentID)) {
            log.error("Request missing the mandatory query parameter consentId");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory query parameter consentId " +
                    "not available");
        }

        int count = 0;

        try {
            ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
            Map<String, ConsentHistoryResource> results = consentCoreService.getConsentAmendmentHistoryData(consentID);

            JSONArray consentHistory = new JSONArray();
            for (Map.Entry<String, ConsentHistoryResource> result : results.entrySet()) {
                JSONObject consentResourceJSON = new JSONObject();
                ConsentHistoryResource consentHistoryResource = result.getValue();
                DetailedConsentResource detailedConsentHistory = consentHistoryResource.getDetailedConsentResource();
                consentResourceJSON.appendField("historyId", result.getKey());
                consentResourceJSON.appendField("amendedReason", consentHistoryResource.getReason());
                consentResourceJSON.appendField("amendedTime", detailedConsentHistory.getUpdatedTime());
                consentResourceJSON.appendField("consentData",
                        ConsentExtensionUtils.detailedConsentToJSON(detailedConsentHistory));
                consentHistory.add(consentResourceJSON);
            }
            response.appendField("consentID", consentID);
            response.appendField("currentConsent",
                    ConsentExtensionUtils.detailedConsentToJSON(consentCoreService.getDetailedConsent(consentID)));
            response.appendField("consentAmendmentHistory", consentHistory);
            count = consentHistory.size();
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving consent amendment history data", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        JSONObject metadata = new JSONObject();
        metadata.appendField("amendmentCount", count);
        response.appendField(METADATA, metadata);
        consentAdminData.setResponseStatus(ResponseStatus.OK);
        consentAdminData.setResponsePayload(response);
    }

    @Override
    public void handleConsentExpiry(ConsentAdminData consentAdminData) throws ConsentException {

        try {
            ExpiredConsentStatusUpdateJob.updateExpiredStatues();
            consentAdminData.setResponseStatus(ResponseStatus.OK);
            consentAdminData.setResponseStatus(ResponseStatus.NO_CONTENT);
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving expiring consents", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @Override
    public void handleTemporaryRetentionDataSyncing(ConsentAdminData consentAdminData) throws ConsentException {

        if (OpenBankingConfigParser.getInstance().isRetentionDataDBSyncEnabled()) {
            consentAdminData.setResponseStatus(ResponseStatus.BAD_REQUEST);
            log.error("Retention data DB sync periodical job is already enabled");
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    "Retention data DB sync periodical job is already enabled");
        }
        try {
            RetentionDatabaseSyncJob.syncRetentionDatabase();
            consentAdminData.setResponseStatus(ResponseStatus.NO_CONTENT);
        } catch (ConsentManagementException e) {
            log.error("Error while triggering retention data DB sync method", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleConsentStatusAuditSearch(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();
        ArrayList<String> consentIDs = null;
        Integer limit = null;
        Integer offset = null;
        boolean fetchFromRetentionDatabase = false;

        Map queryParams = consentAdminData.getQueryParams();

        if (validateAndGetQueryParam(queryParams, "consentIDs") != null) {
            consentIDs = new ArrayList<>(Arrays.asList(validateAndGetQueryParam(queryParams, "consentIDs").
                    split(",")));
        }
        if (validateAndGetQueryParam(queryParams, LIMIT) != null) {
            try {
                limit = Integer.parseInt(validateAndGetQueryParam(queryParams, LIMIT));
            } catch (NumberFormatException e) {
                log.error("Number format incorrect in search for parameter limit. Ignoring parameter");
            }
        }
        if (validateAndGetQueryParam(queryParams, OFFSET) != null) {
            try {
                offset = Integer.parseInt(validateAndGetQueryParam(queryParams, OFFSET));
            } catch (NumberFormatException e) {
                log.error("Number format incorrect in search for parameter offset. Ignoring parameter");
            }
        }
        if (validateAndGetQueryParam(queryParams, FETCH_FROM_RETENTION_DB_QUERY_PARAM) != null) {
            fetchFromRetentionDatabase = Boolean.parseBoolean(validateAndGetQueryParam(queryParams,
                    FETCH_FROM_RETENTION_DB_QUERY_PARAM));
        }
        int count, total = 0;

        try {
            ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
            ArrayList<ConsentStatusAuditRecord> results = consentCoreService.getConsentStatusAuditRecords(consentIDs,
                    limit, offset, fetchFromRetentionDatabase);

            JSONArray consentAuditRecords = new JSONArray();
            for (ConsentStatusAuditRecord statusAuditRecord : results) {
                JSONObject statusAuditRecordJSON = new JSONObject();
                statusAuditRecordJSON.appendField("statusAuditId", statusAuditRecord.getStatusAuditID());
                statusAuditRecordJSON.appendField("consentId", statusAuditRecord.getConsentID());
                statusAuditRecordJSON.appendField("currentStatus", statusAuditRecord.getCurrentStatus());
                statusAuditRecordJSON.appendField("actionTime", statusAuditRecord.getActionTime());
                statusAuditRecordJSON.appendField("reason", statusAuditRecord.getReason());
                statusAuditRecordJSON.appendField("actionBy", statusAuditRecord.getActionBy());
                statusAuditRecordJSON.appendField("previousStatus", statusAuditRecord.getPreviousStatus());
                consentAuditRecords.add(statusAuditRecordJSON);
            }
            response.appendField(DATA, consentAuditRecords);
            count = consentAuditRecords.size();
            total = results.size();
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving consent status audit data");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        //retrieve the total of the data set queried
        if (limit != null || offset != null) {
            try {
                ArrayList<ConsentStatusAuditRecord> results = ConsentExtensionsDataHolder.getInstance()
                        .getConsentCoreService().getConsentStatusAuditRecords(consentIDs,
                                null, null, fetchFromRetentionDatabase);
                total = results.size();
            } catch (ConsentManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        JSONObject metadata = new JSONObject();
        metadata.appendField(COUNT, count);
        metadata.appendField(OFFSET, offset);
        metadata.appendField(LIMIT, limit);
        metadata.appendField(TOTAL, total);
        response.appendField(METADATA, metadata);
        consentAdminData.setResponseStatus(ResponseStatus.OK);
        consentAdminData.setResponsePayload(response);
    }

    @Override
    public void handleConsentFileSearch(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();
        String consentID = null;
        boolean fetchFromRetentionDatabase = false;
        Map queryParams = consentAdminData.getQueryParams();

        if (validateAndGetQueryParam(queryParams, "consentId") != null) {
            consentID = validateAndGetQueryParam(queryParams, "consentId");
        }

        if (StringUtils.isBlank(consentID)) {
            log.error("Request missing the mandatory query parameter consentId");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory query parameter consentId " +
                    "not available");
        }
        if (validateAndGetQueryParam(queryParams, FETCH_FROM_RETENTION_DB_QUERY_PARAM) != null) {
            fetchFromRetentionDatabase = Boolean.parseBoolean(validateAndGetQueryParam(queryParams,
                    FETCH_FROM_RETENTION_DB_QUERY_PARAM));
        }

        try {
            ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
            ConsentFile file = consentCoreService.getConsentFile(consentID, fetchFromRetentionDatabase);
            response.appendField("consentFile", file.getConsentFile());
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving consent file");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        consentAdminData.setResponseStatus(ResponseStatus.OK);
        consentAdminData.setResponsePayload(response);
    }
}
