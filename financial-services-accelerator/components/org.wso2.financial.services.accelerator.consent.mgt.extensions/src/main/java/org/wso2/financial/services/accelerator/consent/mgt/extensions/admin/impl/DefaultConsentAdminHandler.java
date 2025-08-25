/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.ConsentAdminHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ConsentAdminData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentRevokeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentRevokeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentSearchRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentSearchResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.SearchTypeEnum;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.utils.ConsentAdminUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.utils.ExternalAPIConsentAdminUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentOperationEnum;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.job.ExpiredConsentStatusUpdateJob;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.ArrayList;
import java.util.Map;

/**
 * Consent admin handler default implementation.
 */
public class DefaultConsentAdminHandler implements ConsentAdminHandler {
    private static final Log log = LogFactory.getLog(DefaultConsentAdminHandler.class);
    ConsentCoreService consentCoreService;
    boolean isExtensionsEnabled;
    boolean isExternalPreConsentRevocationEnabled;
    boolean isExternalEnrichConsentSearchResponseEnabled;

    public DefaultConsentAdminHandler() {

        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        isExtensionsEnabled = configParser.isServiceExtensionsEndpointEnabled();
        isExternalPreConsentRevocationEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_REVOKE);
        isExternalEnrichConsentSearchResponseEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.ENRICH_CONSENT_SEARCH_RESPONSE);
    }

    @Override
    public void handleSearch(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();

        ArrayList<String> consentIDs;
        ArrayList<String> clientIDs;
        ArrayList<String> consentTypes;
        ArrayList<String> consentStatuses;
        ArrayList<String> userIDs;
        Long fromTime = null;
        Long toTime = null;
        Integer limit = null;
        Integer offset = null;


        Map queryParams = consentAdminData.getQueryParams();

        consentIDs = ConsentAdminUtils.getArrayListFromQueryParam(ConsentAdminUtils
                .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.CONSENT_IDS));
        clientIDs = ConsentAdminUtils.getArrayListFromQueryParam(ConsentAdminUtils
                .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.CLIENT_IDS));
        consentTypes = ConsentAdminUtils.getArrayListFromQueryParam(ConsentAdminUtils
                .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.CONSENT_TYPES));
        consentStatuses = ConsentAdminUtils.getArrayListFromQueryParam(ConsentAdminUtils
                .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.CONSENT_STATUSES));
        userIDs = ConsentAdminUtils.getArrayListFromQueryParam(ConsentAdminUtils
                .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.USER_IDS));

        try {
            long fromTimeValue = ConsentAdminUtils.getLongFromQueryParam(ConsentAdminUtils
                    .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.FROM_TIME));
            fromTime = fromTimeValue == 0 ? null : fromTimeValue;
        } catch (NumberFormatException e) {
            log.warn("Number format incorrect in search for parameter fromTime. Ignoring parameter");
        }
        try {
            long toTimeValue = ConsentAdminUtils.getLongFromQueryParam(ConsentAdminUtils
                    .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.TO_TIME));
            toTime = toTimeValue == 0 ? null : toTimeValue;
        } catch (NumberFormatException e) {
            log.warn("Number format incorrect in search for parameter toTime. Ignoring parameter");
        }
        try {
            int limitValue = ConsentAdminUtils.getIntFromQueryParam(ConsentAdminUtils
                    .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.LIMIT));
            limit = limitValue == 0 ? null : limitValue;
        } catch (NumberFormatException e) {
            log.warn("Number format incorrect in search for parameter limit. Ignoring parameter");
        }
        try {
            int offsetValue = ConsentAdminUtils.getIntFromQueryParam(ConsentAdminUtils
                    .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.OFFSET));
            offset = offsetValue == 0 ? null : offsetValue;
        } catch (NumberFormatException e) {
            log.warn("Number format incorrect in search for parameter limit. Ignoring parameter");
        }

        int count, total = 0;

        try {
            ArrayList<DetailedConsentResource> results = ConsentExtensionsDataHolder.getInstance()
                    .getConsentCoreService().searchDetailedConsents(consentIDs, clientIDs,
                            consentTypes, consentStatuses, userIDs, fromTime, toTime, limit, offset);
            JSONArray searchResults = new JSONArray();
            for (DetailedConsentResource result : results) {
                searchResults.put(ConsentAdminUtils.detailedConsentToJSON(result));
            }
            response.put(ConsentExtensionConstants.DATA.toLowerCase(), searchResults);
            count = searchResults.length();
            total = results.size();
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        //retrieve the total of the data set queried
        if (limit != null || offset != null) {
            try {
                ArrayList<DetailedConsentResource> results = ConsentExtensionsDataHolder.getInstance()
                        .getConsentCoreService().searchDetailedConsents(consentIDs,
                                clientIDs, consentTypes, consentStatuses, userIDs, fromTime, toTime, null, null);
                total = results.size();
            } catch (ConsentManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
            }
        }

        //if the OpenAPI extension is enabled for admin-consent search
        if (isExtensionsEnabled && isExternalEnrichConsentSearchResponseEnabled) {
            // Call external service to enrich consent search response
            ExternalAPIAdminConsentSearchRequestDTO externalAPISearchRequest =
                    new ExternalAPIAdminConsentSearchRequestDTO(SearchTypeEnum.BULK_SEARCH.getValue(),
                            response.getJSONArray(ConsentExtensionConstants.DATA.toLowerCase()),
                            consentAdminData.getQueryParams());
            try {
                ExternalAPIAdminConsentSearchResponseDTO responseDTO =
                        ExternalAPIConsentAdminUtils.callExternalService(externalAPISearchRequest);
                consentAdminData.setResponseStatus(ResponseStatus.OK);
                JSONObject enrichedSearchResult = new JSONObject();
                JSONArray enrichedSearchResultArray = responseDTO.getEnrichedSearchResult();

                enrichedSearchResult.put(ConsentExtensionConstants.DATA.toLowerCase(), enrichedSearchResultArray);
                JSONObject metadata = new JSONObject();
                metadata.put(ConsentExtensionConstants.COUNT, enrichedSearchResultArray.length());
                metadata.put(ConsentExtensionConstants.OFFSET, offset);
                metadata.put(ConsentExtensionConstants.LIMIT, limit);
                metadata.put(ConsentExtensionConstants.TOTAL, total);

                enrichedSearchResult.put(ConsentExtensionConstants.METADATA, metadata);
                consentAdminData.setResponsePayload(enrichedSearchResult);
            } catch (FinancialServicesException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
                        ConsentOperationEnum.CONSENT_SEARCH);
            }
        } else {
            JSONObject metadata = new JSONObject();
            metadata.put(ConsentExtensionConstants.COUNT, count);
            metadata.put(ConsentExtensionConstants.OFFSET, offset);
            metadata.put(ConsentExtensionConstants.LIMIT, limit);
            metadata.put(ConsentExtensionConstants.TOTAL, total);

            response.put(ConsentExtensionConstants.METADATA, metadata);
            consentAdminData.setResponseStatus(ResponseStatus.OK);
            consentAdminData.setResponsePayload(response);
        }
        // Filter consent data based on the accounts if accounts are available in the query params.
        if (consentAdminData.getQueryParams().containsKey(ConsentExtensionConstants.ACCOUNT_IDS)) {
            filterConsentsByAccount(consentAdminData);
        }
    }

    @Override
    public void handleRevoke(ConsentAdminData consentAdminData) throws ConsentException {

        try {
            Map queryParams = consentAdminData.getQueryParams();

            String consentId = ConsentAdminUtils.validateAndGetQueryParam(queryParams,
                    ConsentExtensionConstants.CC_CONSENT_ID);
            if (consentId == null) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory parameter consent ID not available");
            } else {
                String userId = ConsentAdminUtils.validateAndGetQueryParam(queryParams,
                        ConsentExtensionConstants.USER_ID_PARAM);
                ConsentCoreService coreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
                ConsentResource consentResource = coreService
                        .getConsent(consentId , false);
                //if the OpenAPI extension is enabled for admin-consent revoke
                if (isExtensionsEnabled && isExternalPreConsentRevocationEnabled) {
                    // Call external service before revoking consent.
                    try {
                        ExternalAPIConsentResourceRequestDTO externalAPIConsentResource =
                                new ExternalAPIConsentResourceRequestDTO(consentResource);
                        ExternalAPIAdminConsentRevokeRequestDTO requestDTO = new
                                ExternalAPIAdminConsentRevokeRequestDTO(
                                externalAPIConsentResource, consentAdminData.getAbsolutePath(),
                                null);
                        ExternalAPIAdminConsentRevokeResponseDTO responseDTO =
                                ExternalAPIConsentAdminUtils.callExternalService(requestDTO);
                        coreService.revokeConsentWithReason(consentId,
                                responseDTO.getRevocationStatusName(),
                                userId, responseDTO.getRequireTokenRevocation(),
                                ConsentExtensionConstants.CONSENT_REVOKE_FROM_DASHBOARD_REASON);
                    } catch (FinancialServicesException e) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
                                ConsentOperationEnum.CONSENT_DELETE);
                    }
                } else {
                    if (!ConsentExtensionConstants.AUTHORIZED_STATUS.equals(consentResource.getCurrentStatus())) {
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                "Consent is not in a revocable status");
                    } else {
                        coreService
                                .revokeConsentWithReason(ConsentAdminUtils.validateAndGetQueryParam(queryParams,
                                                ConsentExtensionConstants.CC_CONSENT_ID),
                                        ConsentExtensionConstants.REVOKED_STATUS,
                                        ConsentAdminUtils.validateAndGetQueryParam(queryParams, "userID"),
                                        ConsentExtensionConstants.CONSENT_REVOKE_FROM_DASHBOARD_REASON);
                    }
                }
            }

            consentAdminData.setResponseStatus(ResponseStatus.OK);
            consentAdminData.setResponseStatus(ResponseStatus.NO_CONTENT);
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while revoking consents", e);
        }
    }

    @Override
    public void handleConsentAmendmentHistoryRetrieval(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();
        Map queryParams = consentAdminData.getQueryParams();

        String consentId = ConsentAdminUtils.validateAndGetQueryParam(queryParams, "consentId");

        if (StringUtils.isBlank(consentId)) {
            log.error("Request missing the mandatory query parameter consentId");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory query parameter consentId " +
                    "not available");
        }

        int count = 0;

        try {
            Map<String, ConsentHistoryResource> results = ConsentExtensionsDataHolder.getInstance()
                    .getConsentCoreService().getConsentAmendmentHistoryData(consentId);

            JSONArray consentHistory = new JSONArray();
            for (Map.Entry<String, ConsentHistoryResource> result : results.entrySet()) {
                JSONObject consentResourceJSON = new JSONObject();
                ConsentHistoryResource consentHistoryResource = result.getValue();
                DetailedConsentResource detailedConsentHistory = consentHistoryResource.getDetailedConsentResource();
                consentResourceJSON.put(ConsentExtensionConstants.HISTORY_ID, result.getKey());
                consentResourceJSON.put(ConsentExtensionConstants.AMENDED_REASON,
                        consentHistoryResource.getReason());
                consentResourceJSON.put(ConsentExtensionConstants.AMENDED_TIME,
                        detailedConsentHistory.getUpdatedTime());
                consentResourceJSON.put(ConsentExtensionConstants.CONSENT_DATA,
                        ConsentAdminUtils.detailedConsentToJSON(detailedConsentHistory));
                consentHistory.put(consentResourceJSON);
            }
            response.put(ConsentExtensionConstants.CC_CONSENT_ID, consentId);
            response.put(ConsentExtensionConstants.CURRENT_CONSENT,
                    ConsentAdminUtils.detailedConsentToJSON(ConsentExtensionsDataHolder.getInstance()
                            .getConsentCoreService().getDetailedConsent(consentId)));
            //if the OpenAPI extension is enabled for admin-consent search
            if (isExtensionsEnabled && isExternalEnrichConsentSearchResponseEnabled) {
                // Call external service to enrich consent search response
                ExternalAPIAdminConsentSearchRequestDTO externalAPISearchRequest =
                        new ExternalAPIAdminConsentSearchRequestDTO(SearchTypeEnum.AMENDMENT_HISTORY.getValue(),
                                consentHistory,
                                consentAdminData.getQueryParams());
                try {
                    ExternalAPIAdminConsentSearchResponseDTO responseDTO =
                            ExternalAPIConsentAdminUtils.callExternalService(externalAPISearchRequest);
                    response.put(ConsentExtensionConstants.AMENDMENT_HISTORY, responseDTO.getEnrichedSearchResult());
                    count = responseDTO.getEnrichedSearchResult().length();

                } catch (FinancialServicesException e) {
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
                            ConsentOperationEnum.CONSENT_SEARCH);
                }
            } else {

                response.put(ConsentExtensionConstants.AMENDMENT_HISTORY, consentHistory);
                count = consentHistory.length();
            }
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving consent amendment history data", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        JSONObject metadata = new JSONObject();
        metadata.put(ConsentExtensionConstants.AMENDMENT_COUNT, count);
        response.put(ConsentExtensionConstants.METADATA, metadata);
        consentAdminData.setResponseStatus(ResponseStatus.OK);
        consentAdminData.setResponsePayload(response);
    }

    @Override
    @Generated(message = "Ignoring since method contains no logics")
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
    public void handleConsentStatusAuditSearch(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();
        Integer limit = null;
        Integer offset = null;

        Map queryParams = consentAdminData.getQueryParams();

        ArrayList<String> consentIDs = ConsentAdminUtils.getArrayListFromQueryParam(ConsentAdminUtils
                .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.CONSENT_IDS));
        try {
            limit = ConsentAdminUtils.getIntFromQueryParam(ConsentAdminUtils
                    .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.LIMIT));
        } catch (NumberFormatException e) {
            log.warn("Number format incorrect in search for parameter limit. Ignoring parameter");
        }
        try {
            offset = ConsentAdminUtils.getIntFromQueryParam(ConsentAdminUtils
                    .validateAndGetQueryParam(queryParams, ConsentExtensionConstants.OFFSET));
        } catch (NumberFormatException e) {
            log.warn("Number format incorrect in search for parameter offset. Ignoring parameter");
        }
        int count, total = 0;

        try {
            ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
            ArrayList<ConsentStatusAuditRecord> results = consentCoreService.getConsentStatusAuditRecords(consentIDs,
                    limit, offset);

            JSONArray consentAuditRecords = new JSONArray();
            for (ConsentStatusAuditRecord statusAuditRecord : results) {
                JSONObject statusAuditRecordJSON = new JSONObject();
                statusAuditRecordJSON.put(ConsentExtensionConstants.STATUS_AUDIT_ID,
                        statusAuditRecord.getStatusAuditID());
                statusAuditRecordJSON.put(ConsentExtensionConstants.CC_CONSENT_ID,
                        statusAuditRecord.getConsentID());
                statusAuditRecordJSON.put(ConsentExtensionConstants.CURRENT_STATUS,
                        statusAuditRecord.getCurrentStatus());
                statusAuditRecordJSON.put(ConsentExtensionConstants.ACTION_TIME,
                        statusAuditRecord.getActionTime());
                statusAuditRecordJSON.put(ConsentExtensionConstants.REASON, statusAuditRecord.getReason());
                statusAuditRecordJSON.put(ConsentExtensionConstants.ACTION_BY, statusAuditRecord.getActionBy());
                statusAuditRecordJSON.put(ConsentExtensionConstants.PREVIOUS_STATUS,
                        statusAuditRecord.getPreviousStatus());
                consentAuditRecords.put(statusAuditRecordJSON);
            }
            response.put(ConsentExtensionConstants.DATA.toLowerCase(), consentAuditRecords);
            count = consentAuditRecords.length();
            total = results.size();
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving consent status audit data");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        //retrieve the total of the data set queried
        if (limit != null || offset != null) {
            try {
                ArrayList<ConsentStatusAuditRecord> results = ConsentExtensionsDataHolder.getInstance()
                        .getConsentCoreService().getConsentStatusAuditRecords(consentIDs,
                                null, null);
                total = results.size();
            } catch (ConsentManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
            }
        }

        JSONObject metadata = new JSONObject();
        metadata.put(ConsentExtensionConstants.COUNT, count);
        metadata.put(ConsentExtensionConstants.OFFSET, offset);
        metadata.put(ConsentExtensionConstants.LIMIT, limit);
        metadata.put(ConsentExtensionConstants.TOTAL, total);
        response.put(ConsentExtensionConstants.METADATA, metadata);
        consentAdminData.setResponseStatus(ResponseStatus.OK);
        consentAdminData.setResponsePayload(response);
    }

    @Override
    public void handleConsentFileSearch(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();
        Map queryParams = consentAdminData.getQueryParams();

        String consentId = ConsentAdminUtils.validateAndGetQueryParam(queryParams, "consentId");

        if (StringUtils.isBlank(consentId)) {
            log.error("Request missing the mandatory query parameter consentId");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory query parameter consentId " +
                    "not available");
        }

        try {
            ConsentFile file = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                    .getConsentFile(consentId);
            response.put(ConsentExtensionConstants.CONSENT_FILE, file.getConsentFile());
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving consent file");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        consentAdminData.setResponseStatus(ResponseStatus.OK);
        consentAdminData.setResponsePayload(response);
    }


    /**
     * Filter the consent data based on the accounts.
     * @param consentAdminData Consent admin data.
     */
    public void filterConsentsByAccount(ConsentAdminData consentAdminData) {

        ArrayList accounts = ((ArrayList) consentAdminData.getQueryParams()
                .get(ConsentExtensionConstants.ACCOUNT_IDS));
        if (accounts.size() > 0) {
            JSONArray filteredConsentData = new JSONArray();
            for (Object consentObj : (JSONArray) consentAdminData.getResponsePayload()
                    .get(ConsentExtensionConstants.DATA_CC)) {
                JSONObject consent = (JSONObject) consentObj;
                JSONArray consentMappingResources = (JSONArray) consent
                        .get(ConsentExtensionConstants.MAPPING_RESOURCES);
                for (Object consentMappingResource : consentMappingResources) {
                    JSONObject consentMappingResourceObject = (JSONObject) consentMappingResource;
                    if (accounts.contains(consentMappingResourceObject.get(ConsentExtensionConstants.ACCOUNT_ID_CC))) {
                        filteredConsentData.put(consent);
                        break;
                    }
                }
            }
            JSONObject responseMetadata = (JSONObject) consentAdminData.getResponsePayload()
                    .get(ConsentExtensionConstants.METADATA);
            responseMetadata.put(ConsentExtensionConstants.TOTAL, filteredConsentData.length());
            responseMetadata.put(ConsentExtensionConstants.COUNT, filteredConsentData.length());
            consentAdminData.getResponsePayload().put(ConsentExtensionConstants.DATA_CC, filteredConsentData);
        }
    }


}
