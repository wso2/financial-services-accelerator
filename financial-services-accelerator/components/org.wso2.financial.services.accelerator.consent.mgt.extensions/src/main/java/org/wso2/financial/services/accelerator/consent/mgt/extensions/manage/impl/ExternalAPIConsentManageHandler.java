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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.impl;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.Request;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.ConsentManageHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentGenerateResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.HashMap;
import java.util.UUID;

/**
 * Consent manage handler default implementation.
 */
public class ExternalAPIConsentManageHandler implements ConsentManageHandler {

    private static final Log log = LogFactory.getLog(ExternalAPIConsentManageHandler.class);
    private static ConsentCoreService consentCoreService;

    public ExternalAPIConsentManageHandler() {
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
    }

    @Override
    public void handleGet(ConsentManageData consentManageData) throws ConsentException {

        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.");
        }

        if (consentManageData.getRequestPath() == null) {
            log.error("Resource Path Not Found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Path Not Found");
        }

        String[] requestPathArray = consentManageData.getRequestPath().split("/");
        if (requestPathArray.length < 2 || StringUtils.isEmpty(requestPathArray[0])) {
            log.error("Invalid Request Path");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid Request Path");
        }
        String consentId = consentManageData.getRequestPath().split("/")[1];
        if (ConsentExtensionUtils.isConsentIdValid(consentId)) {
            try {
                // Get consent by consent ID from database
                ConsentResource consentResource = consentCoreService.getConsent(consentId, false);
                if (consentResource == null) {
                    log.error("Consent not found");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not found in the database");
                }

                ExternalAPIConsentGenerateRequestDTO requestDTO = new ExternalAPIConsentGenerateRequestDTO(
                        consentManageData);
                //requestDTO.setConsentResource(consentResource);
                ExternalAPIConsentGenerateResponseDTO responseDTO = callExternalService(requestDTO,
                        ServiceExtensionTypeEnum.PRE_CONSENT_RETRIEVAL);

                JSONObject receiptJSON = new JSONObject(new Gson().toJson(responseDTO.getReceipt()));
                ConsentResource newConsentResource = new ConsentResource(consentResource.getClientID(),
                        receiptJSON.toString(), consentResource.getConsentType(),
                        responseDTO.getAuthorizationStatus()
                );

                consentManageData.setResponsePayload(ConsentExtensionUtils.getInitiationRetrievalResponse(receiptJSON,
                        newConsentResource));
                consentManageData.setResponseStatus(ResponseStatus.OK);
            } catch (ConsentManagementException | JSONException e) {
                log.error("Error Occurred while handling the request", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error Occurred while handling the request");
            }
        } else {
            log.error("Invalid consent Id found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid consent Id found");
        }
    }

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.");
        }

        if (consentManageData.getRequestPath() == null) {
            log.error("Resource Path Not Found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Path Not Found");
        }

        ExternalAPIConsentGenerateRequestDTO requestDTO = new ExternalAPIConsentGenerateRequestDTO(consentManageData);
        ExternalAPIConsentGenerateResponseDTO responseDTO = callExternalService(requestDTO,
                ServiceExtensionTypeEnum.PRE_CONSENT_GENERATION);

        DetailedConsentResource createdConsent = generateConsent(responseDTO, consentManageData.getClientId());
        consentManageData.setResponsePayload(ConsentExtensionUtils.getInitiationResponse(
                consentManageData.getPayload(), createdConsent));
        consentManageData.setResponseStatus(ResponseStatus.CREATED);

    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.");
        }

        String[] requestPathArray;
        if (consentManageData.getRequestPath() == null) {
            log.error("Resource Path Not Found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Path Not Found");
        } else {
            requestPathArray = consentManageData.getRequestPath().split("/");
        }

        if (requestPathArray.length < 2 || StringUtils.isEmpty(requestPathArray[0])) {
            log.error("Invalid Request Path");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid Request Path");
        }
        String consentId = requestPathArray[1];
        if (ConsentExtensionUtils.isConsentIdValid(consentId)) {
            try {
                // Get consent by consent ID from database
                ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

                if (consentResource == null) {
                    log.error("Consent not found");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not found");
                }

                ExternalAPIConsentGenerateRequestDTO requestDTO = new ExternalAPIConsentGenerateRequestDTO(
                        consentManageData);
                //requestDTO.setConsentResource(consentResource);
                ExternalAPIConsentGenerateResponseDTO responseDTO = callExternalService(requestDTO,
                        ServiceExtensionTypeEnum.PRE_CONSENT_REVOCATION);

                boolean shouldRevokeTokens = false;
                boolean success = consentCoreService.revokeConsent(consentId,
                        ConsentExtensionConstants.REVOKED_STATUS, null, shouldRevokeTokens);
                if (!success) {
                    log.error("Consent revocation unsuccessful");
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Consent revocation unsuccessful");
                }
                consentManageData.setResponseStatus(ResponseStatus.NO_CONTENT);
            } catch (ConsentManagementException e) {
                log.error(e.getMessage().replaceAll("[\r\n]+", ""));
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage().replaceAll("[\r\n]+", ""));
            }
        } else {
            log.error("Request Path Invalid");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Request Path Invalid");
        }

    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {

        log.error("Method PUT is not supported");
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method PUT is not supported");
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {

        log.error("Method PATCH is not supported");
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method PATCH is not supported");
    }

    @Override
    public void handleFileUploadPost(ConsentManageData consentManageData) throws ConsentException {

        log.error("Method File Upload POST is not supported");
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method File Upload POST is not supported");
    }

    @Override
    public void handleFileGet(ConsentManageData consentManageData) throws ConsentException {

        log.error("Method File Upload GET is not supported");
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method File Upload GET is not supported");
    }

    private ExternalAPIConsentGenerateResponseDTO callExternalService(
            ExternalAPIConsentGenerateRequestDTO requestDTO, ServiceExtensionTypeEnum serviceType)
            throws ConsentException {

        JSONObject requestJson = new JSONObject(new Gson().toJson(requestDTO));
        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                UUID.randomUUID().toString(), new Request(requestJson, new HashMap<>()));
        JSONObject responseJson = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                serviceType);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIConsentGenerateResponseDTO.class);

    }

    private DetailedConsentResource generateConsent(ExternalAPIConsentGenerateResponseDTO responseDTO, String clientId)
            throws ConsentException {

        try {
            ConsentResource consentResource = new ConsentResource(clientId,
                    new Gson().toJson(responseDTO.getReceipt()), responseDTO.getConsentType(),
                    responseDTO.getConsentFrequency(), responseDTO.getValidityTime(),
                    responseDTO.getRecurringIndicator(), responseDTO.getAuthorizationStatus());

            DetailedConsentResource createdConsent = consentCoreService.createAuthorizableConsent(
                    consentResource, null, responseDTO.getConsentStatus(),
                    responseDTO.getAuthorizationType(), true
            );
            return createdConsent;
        } catch (ConsentManagementException e) {
            log.error("Error persisting consent", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error persisting consent");
        }
    }

}
