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
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ExternalAPIUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.ConsentManageHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRetrieveRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRetrieveResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.Map;
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

        String clientId = consentManageData.getClientId();
        String resourcePath = consentManageData.getRequestPath();

        if (StringUtils.isEmpty(clientId)) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.");
        }

        if (resourcePath == null) {
            log.error("Resource path not found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource path not found");
        }

        String[] requestPathArray = resourcePath.split("/");
        if (requestPathArray.length < 2 || StringUtils.isEmpty(requestPathArray[0])) {
            log.error("Invalid resource path");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid Request Path");
        }
        String consentId = resourcePath.split("/")[1];
        if (ConsentExtensionUtils.isConsentIdValid(consentId)) {
            try {
                // Get consent by consent ID from database
                ConsentResource consentResource = consentCoreService.getConsent(consentId, false);
                ConsentAttributes consentAttributes = consentCoreService.getConsentAttributes(consentId);
                if (consentResource == null) {
                    log.error("Consent not found in the database");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not found in the database");
                }
                consentResource.setConsentAttributes(consentAttributes.getConsentAttributes());
                ExternalAPIConsentRetrieveRequestDTO requestDTO = new ExternalAPIConsentRetrieveRequestDTO(consentId,
                        consentResource, resourcePath);
                ExternalAPIConsentRetrieveResponseDTO responseDTO = callExternalService(requestDTO);

                consentManageData.setResponsePayload(responseDTO.getResponseData());
                consentManageData.setResponseStatus(ResponseStatus.OK);
            } catch (FinancialServicesException e) {
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

        try {

            // Call external service before generating consent
            ExternalAPIPreConsentGenerateRequestDTO preRequestDTO =
                    new ExternalAPIPreConsentGenerateRequestDTO(consentManageData);
            ExternalAPIPreConsentGenerateResponseDTO preResponseDTO = callExternalService(preRequestDTO);

            DetailedConsentResource createdConsent = generateConsent(preResponseDTO, consentManageData.getClientId());
            ConsentResource createdConsentResource = consentCoreService.getConsent(createdConsent.getConsentID(),
                    false);
            // ToDo: Fix dao layer error to remove this line.
            //  https://github.com/wso2/financial-services-accelerator/issues/404
            createdConsentResource.setConsentAttributes(createdConsent.getConsentAttributes());

            // Call external service after generating consent
            ExternalAPIPostConsentGenerateRequestDTO postRequestDTO = new ExternalAPIPostConsentGenerateRequestDTO(
                    createdConsentResource, consentManageData.getRequestPath());
            ExternalAPIPostConsentGenerateResponseDTO postResponseDTO = callExternalService(postRequestDTO);

            consentManageData.setResponsePayload(postResponseDTO.getResponseData());
            consentManageData.setResponseStatus(ResponseStatus.CREATED);

        } catch (FinancialServicesException e) {
            log.error("Error Occurred while calling external service", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error Occurred while calling external service");
        }

    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        String clientId = consentManageData.getClientId();
        String resourcePath = consentManageData.getRequestPath();

        if (StringUtils.isEmpty(clientId)) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.");
        }

        String[] requestPathArray;
        if (resourcePath == null) {
            log.error("Resource Path Not Found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Path Not Found");
        } else {
            requestPathArray = resourcePath.split("/");
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

                Map<String, String> consentAttributes =
                        consentCoreService.getConsentAttributes(consentId).getConsentAttributes();
                consentResource.setConsentAttributes(consentAttributes);
                ExternalAPIConsentRevokeRequestDTO requestDTO = new ExternalAPIConsentRevokeRequestDTO(consentResource,
                        resourcePath);
                ExternalAPIConsentRevokeResponseDTO responseDTO = callExternalService(requestDTO);

                boolean shouldRevokeTokens = responseDTO.getRequireTokenRevocation();
                boolean success = consentCoreService.revokeConsent(consentId, responseDTO.getRevocationStatusName(),
                        null, shouldRevokeTokens);
                if (!success) {
                    log.error("Consent revocation unsuccessful");
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Consent revocation unsuccessful");
                }
                consentManageData.setResponseStatus(ResponseStatus.NO_CONTENT);
            } catch (FinancialServicesException e) {
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

    private ExternalAPIPreConsentGenerateResponseDTO callExternalService(
            ExternalAPIPreConsentGenerateRequestDTO requestDTO)
            throws FinancialServicesException {
        JSONObject requestJson = new JSONObject(requestDTO);
        JSONObject responseJson = callExternalService(requestJson, ServiceExtensionTypeEnum.PRE_CONSENT_GENERATION);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIPreConsentGenerateResponseDTO.class);
    }

    private ExternalAPIPostConsentGenerateResponseDTO callExternalService(
            ExternalAPIPostConsentGenerateRequestDTO requestDTO)
            throws FinancialServicesException {
        JSONObject requestJson = requestDTO.toJson();
        JSONObject responseJson = callExternalService(requestJson, ServiceExtensionTypeEnum.POST_CONSENT_GENERATION);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIPostConsentGenerateResponseDTO.class);
    }

    private ExternalAPIConsentRevokeResponseDTO callExternalService(ExternalAPIConsentRevokeRequestDTO requestDTO)
            throws FinancialServicesException {
        JSONObject requestJson = requestDTO.toJson();
        JSONObject responseJson = callExternalService(requestJson, ServiceExtensionTypeEnum.PRE_CONSENT_REVOCATION);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIConsentRevokeResponseDTO.class);
    }

    private ExternalAPIConsentRetrieveResponseDTO callExternalService(ExternalAPIConsentRetrieveRequestDTO requestDTO)
            throws FinancialServicesException {
        JSONObject requestJson = requestDTO.toJson();
        JSONObject responseJson = callExternalService(requestJson, ServiceExtensionTypeEnum.PRE_CONSENT_RETRIEVAL);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIConsentRetrieveResponseDTO.class);
    }

    private JSONObject callExternalService(
            JSONObject requestJson, ServiceExtensionTypeEnum serviceType) throws FinancialServicesException {

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                UUID.randomUUID().toString(), requestJson);
        ExternalServiceResponse response =
                ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest, serviceType);

        if (response.getStatus().equals(StatusEnum.ERROR)) {
            ExternalAPIUtil.handleResponseError(response);
        }
        return new JSONObject(response.getData().toString());
    }

    private DetailedConsentResource generateConsent(
            ExternalAPIPreConsentGenerateResponseDTO responseDTO, String clientId) throws ConsentException {

        try {
            ConsentResource consentResource = new ConsentResource(clientId,
                    new Gson().toJson(responseDTO.getConsentPayload()), responseDTO.getConsentType(),
                    responseDTO.getConsentFrequency(), responseDTO.getValidityTime(),
                    responseDTO.getRecurringIndicator(), "created");

            DetailedConsentResource createdConsent = consentCoreService.createAuthorizableConsent(
                    consentResource, null, responseDTO.getConsentStatus(),
                    "authorisation", true
            );
            return createdConsent;
        } catch (ConsentManagementException e) {
            log.error("Error persisting consent", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error persisting consent");
        }
    }

}
