/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.ConsentAdminHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ConsentAdminData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.utils.ConsentAdminUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.ArrayList;
import java.util.Map;

/**
 * Consent admin handler default implementation.
 */
public class DefaultConsentAdminHandler implements ConsentAdminHandler {
    private static final Log log = LogFactory.getLog(DefaultConsentAdminHandler.class);

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
    public void handleRevoke(ConsentAdminData consentAdminData) throws ConsentException {

        try {
            Map queryParams = consentAdminData.getQueryParams();

            String consentId = ConsentAdminUtils.validateAndGetQueryParam(queryParams,
                    ConsentExtensionConstants.CC_CONSENT_ID);
            if (consentId == null) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory parameter consent ID not available");
            } else {
                ConsentResource consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentId, false);

                if (!ConsentExtensionConstants.AUTHORIZED_STATUS.equals(consentResource.getCurrentStatus())) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            "Consent is not in a revocable status");
                } else {
                    ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                            .revokeConsentWithReason(ConsentAdminUtils.validateAndGetQueryParam(queryParams,
                                    ConsentExtensionConstants.CC_CONSENT_ID),
                                    ConsentExtensionConstants.REVOKED_STATUS,
                                    ConsentAdminUtils.validateAndGetQueryParam(queryParams, "userID"),
                                    ConsentExtensionConstants.CONSENT_REVOKE_FROM_DASHBOARD_REASON);
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
            response.put(ConsentExtensionConstants.AMENDMENT_HISTORY, consentHistory);
            count = consentHistory.length();
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
    public void handleConsentStatusAuditSearch(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();
        Integer limit = null;
        Integer offset = null;

        Map queryParams = consentAdminData.getQueryParams();

        ArrayList<String> consentIDs =  ConsentAdminUtils.getArrayListFromQueryParam(ConsentAdminUtils
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
}
