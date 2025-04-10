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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.impl;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentOperationEnum;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.ConsentManageHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentPayloadValidationResult;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRetrieveRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRetrieveResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ConsentManageConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ConsentManageUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ExternalAPIConsentManageUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.Map;

/**
 * Consent manage handler default implementation.
 */
public class DefaultConsentManageHandler implements ConsentManageHandler {

    private static final Log log = LogFactory.getLog(DefaultConsentManageHandler.class);
    ConsentCoreService consentCoreService;
    boolean isExtensionsEnabled;
    boolean isExternalPreConsentRetrievalEnabled;
    boolean isExternalPreConsentGenerationEnabled;
    boolean isExternalPostConsentGenerationEnabled;
    boolean isExternalPreConsentRevocationEnabled;

    public DefaultConsentManageHandler() {

        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        isExtensionsEnabled = configParser.isServiceExtensionsEndpointEnabled();
        isExternalPreConsentRetrievalEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.PRE_CONSENT_RETRIEVAL);
        isExternalPreConsentGenerationEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.PRE_CONSENT_GENERATION);
        isExternalPostConsentGenerationEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.POST_CONSENT_GENERATION);
        isExternalPreConsentRevocationEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.PRE_CONSENT_REVOCATION);
    }

    @Override
    public void handleGet(ConsentManageData consentManageData) throws ConsentException {

        //Check whether client ID exists
        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.",
                    ConsentOperationEnum.CONSENT_RETRIEVE);
        }

        //Validate Initiation headers
        ConsentPayloadValidationResult headerValidationResult = ConsentManageUtils.getConsentManageValidator()
                .validateRequestHeaders(consentManageData);
        if (!headerValidationResult.isValid()) {
            log.error(headerValidationResult.getErrorMessage().replaceAll("[\r\n]+", " "));
            throw new ConsentException(headerValidationResult.getHttpCode(), headerValidationResult.getErrorMessage(),
                    ConsentOperationEnum.CONSENT_RETRIEVE);
        }

        if (consentManageData.getRequestPath() == null) {
            log.error("Resource Path Not Found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Path Not Found",
                    ConsentOperationEnum.CONSENT_RETRIEVE);
        }

        String resourcePath = consentManageData.getRequestPath();
        String[] requestPathArray = resourcePath.split("/");
        if (requestPathArray.length < 2 || StringUtils.isEmpty(requestPathArray[0])) {
            log.error("Invalid Request Path");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid Request Path",
                    ConsentOperationEnum.CONSENT_RETRIEVE);
        }
        String consentId = consentManageData.getRequestPath().split("/")[1];
        if (ConsentExtensionUtils.isConsentIdValid(consentId)) {
            try {
                ConsentResource consent = consentCoreService.getConsent(consentId, false);
                if (consent == null) {
                    log.error("Consent not found");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not found",
                            ConsentOperationEnum.CONSENT_RETRIEVE);
                }
                String consentType = ConsentExtensionUtils.getConsentType(consentManageData.getRequestPath());
                if (!consentType.equals(consent.getConsentType())) {
                    log.error("Consent Type mismatch");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent Type mismatch",
                            ConsentOperationEnum.CONSENT_RETRIEVE);
                }
                // Check whether the client id is matching
                if (!consent.getClientID().equals(consentManageData.getClientId())) {
                    log.error("Client ID mismatch");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID mismatch",
                            ConsentOperationEnum.CONSENT_RETRIEVE);
                }

                if (isExtensionsEnabled && isExternalPreConsentRetrievalEnabled) {
                    // Call external service before retrieving consent.
                    Map<String, String> consentAttributes =
                            consentCoreService.getConsentAttributes(consentId).getConsentAttributes();
                    consent.setConsentAttributes(consentAttributes);
                    ExternalAPIConsentRetrieveRequestDTO requestDTO = new ExternalAPIConsentRetrieveRequestDTO(
                            consentId, consent, resourcePath);
                    ExternalAPIConsentRetrieveResponseDTO responseDTO = ExternalAPIConsentManageUtils.
                            callExternalService(requestDTO);
                    consentManageData.setResponsePayload(responseDTO.getResponseData());
                } else {
                    JSONObject receiptJSON = new JSONObject(consent.getReceipt());
                    consentManageData.setResponsePayload(ConsentExtensionUtils.getInitiationRetrievalResponse(
                            receiptJSON, consent));
                }
                consentManageData.setResponseStatus(ResponseStatus.OK);
            } catch (ConsentManagementException | JSONException e) {
                log.error("Error Occurred while handling the request", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error Occurred while handling the request", ConsentOperationEnum.CONSENT_RETRIEVE);
            }
        } else {
            log.error("Invalid consent Id found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid consent Id found",
                    ConsentOperationEnum.CONSENT_RETRIEVE);
        }
    }

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        //Check whether client ID exists
        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.",
                    ConsentOperationEnum.CONSENT_CREATE);
        }

        //Validate Initiation headers
        ConsentPayloadValidationResult headerValidationResult = ConsentManageUtils.getConsentManageValidator()
                .validateRequestHeaders(consentManageData);
        if (!headerValidationResult.isValid()) {
            log.error(headerValidationResult.getErrorMessage().replaceAll("[\r\n]+", " "));
            throw new ConsentException(headerValidationResult.getHttpCode(), headerValidationResult.getErrorMessage(),
                    ConsentOperationEnum.CONSENT_CREATE);
        }

        if (consentManageData.getRequestPath() == null) {
            log.error("Resource Path Not Found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Path Not Found",
                    ConsentOperationEnum.CONSENT_CREATE);
        }

        try {
            DetailedConsentResource createdConsent;
            if (isExtensionsEnabled && isExternalPreConsentGenerationEnabled) {
                // Call external service before generating consent
                ExternalAPIPreConsentGenerateRequestDTO preRequestDTO =
                        new ExternalAPIPreConsentGenerateRequestDTO(consentManageData);
                ExternalAPIPreConsentGenerateResponseDTO preResponseDTO = ExternalAPIConsentManageUtils.
                        callExternalService(preRequestDTO);
                createdConsent = generateConsent(preResponseDTO, consentManageData.getClientId());
            } else {
                String consentType = ConsentManageUtils.getConsentManageValidator().getConsentType(consentManageData);
                //Validate Initiation request
                ConsentPayloadValidationResult validationResponse = ConsentManageUtils.getConsentManageValidator()
                        .validateRequestPayload(consentManageData, consentType);
                if (!validationResponse.isValid()) {
                    log.error(validationResponse.getErrorMessage().replaceAll("[\r\n]+", " "));
                    throw new ConsentException(validationResponse.getHttpCode(), validationResponse.getErrorCode(),
                            validationResponse.getErrorMessage());
                }

                ConsentResource requestedConsent = new ConsentResource(consentManageData.getClientId(),
                        consentManageData.getPayload().toString(), consentType,
                        ConsentExtensionConstants.AWAIT_AUTHORISE_STATUS);

                createdConsent = consentCoreService.createAuthorizableConsent(requestedConsent,
                        null, ConsentExtensionConstants.CREATED_STATUS, ConsentExtensionConstants.DEFAULT_AUTH_TYPE,
                        true);
            }

            if (isExtensionsEnabled && isExternalPostConsentGenerationEnabled) {
                // Call external service after generating consent
                ConsentResource createdConsentResource = consentCoreService.getConsent(createdConsent.getConsentID(),
                        false);
                createdConsentResource.setConsentAttributes(createdConsent.getConsentAttributes());
                ExternalAPIPostConsentGenerateRequestDTO postRequestDTO = new ExternalAPIPostConsentGenerateRequestDTO(
                        createdConsentResource, consentManageData.getRequestPath());
                ExternalAPIPostConsentGenerateResponseDTO postResponseDTO = ExternalAPIConsentManageUtils.
                        callExternalService(postRequestDTO);

                consentManageData.setResponsePayload(postResponseDTO.getResponseData());
            } else {
                consentManageData.setResponsePayload(ConsentExtensionUtils
                        .getInitiationResponse(consentManageData.getPayload(), createdConsent));
            }
            consentManageData.setResponseStatus(ResponseStatus.CREATED);

        } catch (ConsentManagementException e) {
            log.error("Error Occurred while handling the request", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error Occurred while handling the request", ConsentOperationEnum.CONSENT_CREATE);
        }

    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        //Check whether client ID exists
        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.",
                    ConsentOperationEnum.CONSENT_DELETE);
        }

        //Validate Initiation headers
        ConsentPayloadValidationResult headerValidationResult = ConsentManageUtils.getConsentManageValidator()
                .validateRequestHeaders(consentManageData);
        if (!headerValidationResult.isValid()) {
            log.error(headerValidationResult.getErrorMessage().replaceAll("[\r\n]+", " "));
            throw new ConsentException(headerValidationResult.getHttpCode(), headerValidationResult.getErrorMessage(),
                    ConsentOperationEnum.CONSENT_DELETE);
        }

        String[] requestPathArray;
        String resourcePath = consentManageData.getRequestPath();
        if (resourcePath == null) {
            log.error("Resource Path Not Found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Path Not Found",
                    ConsentOperationEnum.CONSENT_DELETE);
        } else {
            requestPathArray = resourcePath.split("/");
        }

        if (requestPathArray.length < 2 || StringUtils.isEmpty(requestPathArray[0])) {
            log.error("Invalid Request Path");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid Request Path",
                    ConsentOperationEnum.CONSENT_DELETE);
        }
        String consentId = requestPathArray[1];
        if (ConsentExtensionUtils.isConsentIdValid(consentId)) {
            try {
                boolean consentRevocationSuccess;
                boolean shouldRevokeTokens;
                String revocationStatusName;
                ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

                if (consentResource == null) {
                    log.error("Consent not found");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not found",
                            ConsentOperationEnum.CONSENT_DELETE);
                }

                String consentType = ConsentExtensionUtils.getConsentType(consentManageData.getRequestPath());
                if (!consentType.equals(consentResource.getConsentType())) {
                    log.error("Consent Type mismatch");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent Type mismatch",
                            ConsentOperationEnum.CONSENT_DELETE);
                }

                if (!consentResource.getClientID().equals(consentManageData.getClientId())) {
                    //Throwing this error in a generic manner since client will not be able to identify if consent
                    // exists if consent does not belong to them
                    log.error(ConsentManageConstants.NO_CONSENT_FOR_CLIENT_ERROR);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            ConsentManageConstants.NO_CONSENT_FOR_CLIENT_ERROR, ConsentOperationEnum.CONSENT_DELETE);
                }

                if (isExtensionsEnabled && isExternalPreConsentRevocationEnabled) {
                    // Call external service before revoking consent.
                    Map<String, String> consentAttributes =
                            consentCoreService.getConsentAttributes(consentId).getConsentAttributes();
                    consentResource.setConsentAttributes(consentAttributes);
                    ExternalAPIConsentRevokeRequestDTO requestDTO = new ExternalAPIConsentRevokeRequestDTO(
                            consentResource, resourcePath);
                    ExternalAPIConsentRevokeResponseDTO responseDTO = ExternalAPIConsentManageUtils.
                            callExternalService(requestDTO);
                    shouldRevokeTokens = responseDTO.getRequireTokenRevocation();
                    revocationStatusName = responseDTO.getRevocationStatusName();
                } else {
                    if (ConsentExtensionConstants.REVOKED_STATUS.equals(consentResource.getCurrentStatus()) ||
                            ConsentExtensionConstants.REJECTED_STATUS.equals(consentResource.getCurrentStatus())) {
                        log.error("Consent is already in revoked or rejected state");
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                "Consent is already in revoked or rejected state");
                    }
                    shouldRevokeTokens = ConsentExtensionConstants.AUTHORIZED_STATUS.equals(
                            consentResource.getCurrentStatus());
                    revocationStatusName = ConsentExtensionConstants.REVOKED_STATUS;
                }
                consentRevocationSuccess = consentCoreService.revokeConsent(consentId,
                        revocationStatusName, null, shouldRevokeTokens);
                if (!consentRevocationSuccess) {
                    log.error("Token revocation unsuccessful");
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Token revocation unsuccessful", ConsentOperationEnum.CONSENT_DELETE);
                }
                consentManageData.setResponseStatus(ResponseStatus.NO_CONTENT);
            } catch (ConsentManagementException e) {
                log.error(e.getMessage().replaceAll("[\r\n]+", ""));
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage().replaceAll("[\r\n]+", ""), ConsentOperationEnum.CONSENT_DELETE);
            }
        } else {
            log.error("Request Path Invalid");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Request Path Invalid",
                    ConsentOperationEnum.CONSENT_DELETE);
        }

    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {

        log.error("Method PUT is not supported");
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method PUT is not supported",
                ConsentOperationEnum.CONSENT_UPDATE);
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {

        log.error("Method PATCH is not supported");
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method PATCH is not supported",
                ConsentOperationEnum.CONSENT_PARTIAL_UPDATE);
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

    private DetailedConsentResource generateConsent(
            ExternalAPIPreConsentGenerateResponseDTO responseDTO, String clientId) throws ConsentException {

        try {
            ConsentResource consentResource = new ConsentResource(clientId,
                    new Gson().toJson(responseDTO.getConsentPayload()), responseDTO.getConsentType(),
                    responseDTO.getConsentFrequency(), responseDTO.getValidityTime(),
                    responseDTO.getRecurringIndicator(), responseDTO.getConsentStatus(),
                    responseDTO.getConsentAttributes());

            return consentCoreService.createAuthorizableConsent(
                    consentResource, null, responseDTO.getAuthorizationStatus(),
                    responseDTO.getAuthorizationType(), true);
        } catch (ConsentManagementException e) {
            log.error("Error persisting consent", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error persisting consent");
        }
    }

}
