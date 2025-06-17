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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentOperationEnum;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ExternalAPIUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.ConsentManageHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentPayloadValidationResult;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRetrieveRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIModifiedResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostFileUploadRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreFileUploadRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreFileUploadResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ConsentManageConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ConsentManageUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ExternalAPIConsentManageUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.time.OffsetDateTime;
import java.util.HashMap;
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
    boolean isExternalPreFileUploadEnabled;
    boolean isExternalPostFileUploadEnabled;
    boolean isExternalPreFileRetrievalEnabled;
    String idempotencyHeaderName;
    IdempotencyValidator idempotencyValidator;

    public DefaultConsentManageHandler() {

        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        isExtensionsEnabled = configParser.isServiceExtensionsEndpointEnabled();
        isExternalPreConsentRetrievalEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_RETRIEVAL);
        isExternalPreConsentGenerationEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_CREATION);
        isExternalPostConsentGenerationEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.ENRICH_CONSENT_CREATION_RESPONSE);
        isExternalPreConsentRevocationEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_REVOKE);
        isExternalPreFileUploadEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_FILE_UPLOAD);
        isExternalPostFileUploadEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.ENRICH_CONSENT_FILE_RESPONSE);
        isExternalPreFileRetrievalEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.VALIDATE_CONSENT_FILE_RETRIEVAL);
        idempotencyHeaderName = configParser.getIdempotencyHeaderName();
        idempotencyValidator = new IdempotencyValidator();
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
                    ExternalAPIConsentResourceRequestDTO externalAPIConsentResource =
                            new ExternalAPIConsentResourceRequestDTO(consent);
                    ExternalAPIConsentRetrieveRequestDTO requestDTO = new ExternalAPIConsentRetrieveRequestDTO(
                            externalAPIConsentResource, consentManageData);
                    ExternalAPIModifiedResponseDTO responseDTO = ExternalAPIConsentManageUtils.
                            callExternalService(requestDTO);
                    if (responseDTO.getModifiedResponse() != null) {
                        consentManageData.setResponsePayload(responseDTO.getModifiedResponse());
                    } else {
                        consentManageData.setResponsePayload(new JSONObject());
                    }
                    if (responseDTO.getResponseHeaders() != null) {
                        consentManageData.setResponseHeaders(responseDTO.getResponseHeaders());
                    } else {
                        consentManageData.setResponseHeaders(new HashMap<>());
                    }
                } else {
                    String consentType = ConsentExtensionUtils.getConsentType(consentManageData.getRequestPath());
                    if (!consentType.equals(consent.getConsentType())) {
                        log.error(ConsentManageConstants.CONSENT_TYPE_MISMATCH_ERROR);
                        throw new ConsentException(ResponseStatus.BAD_REQUEST, ConsentManageConstants.
                                CONSENT_TYPE_MISMATCH_ERROR, ConsentOperationEnum.CONSENT_RETRIEVE);
                    }
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

        //Perform idempotency validation
        if (consentManageData.getHeaders().containsKey(idempotencyHeaderName) &&
                idempotencyValidator.isIdempotent(consentManageData, ConsentOperationEnum.CONSENT_CREATE)) {
            return;
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

                createdConsent = consentCoreService.createAuthorizableConsent(requestedConsent, null,
                        ConsentExtensionConstants.CREATED_STATUS, ConsentExtensionConstants.DEFAULT_AUTH_TYPE,
                        true);
            }

            //Store idempotency key as a consent attribute
            if (consentManageData.getHeaders().containsKey(idempotencyHeaderName)) {
                Map<String, String> consentAttributes = new HashMap();
                consentAttributes.put(ConsentExtensionConstants.IDEMPOTENCY_KEY, consentManageData.getHeaders()
                        .get(idempotencyHeaderName));
                consentCoreService.storeConsentAttributes(createdConsent.getConsentID(), consentAttributes);
            }

            if (isExtensionsEnabled && isExternalPostConsentGenerationEnabled) {
                // Call external service after generating consent
                DetailedConsentResource createdConsentResource = consentCoreService.getDetailedConsent(
                        createdConsent.getConsentID());
                ExternalAPIConsentResourceRequestDTO externalAPIConsentResource =
                        new ExternalAPIConsentResourceRequestDTO(createdConsentResource);
                ExternalAPIPostConsentGenerateRequestDTO postRequestDTO = new ExternalAPIPostConsentGenerateRequestDTO(
                        externalAPIConsentResource, consentManageData.getRequestPath());
                ExternalAPIModifiedResponseDTO postResponseDTO = ExternalAPIConsentManageUtils.
                        callExternalService(postRequestDTO);

                if (postResponseDTO.getModifiedResponse() != null) {
                    consentManageData.setResponsePayload(postResponseDTO.getModifiedResponse());
                } else {
                    consentManageData.setResponsePayload(new JSONObject());
                }
                if (postResponseDTO.getResponseHeaders() != null) {
                    consentManageData.setResponseHeaders(postResponseDTO.getResponseHeaders());
                } else {
                    consentManageData.setResponseHeaders(new HashMap<>());
                }
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
                    ExternalAPIConsentResourceRequestDTO externalAPIConsentResource =
                            new ExternalAPIConsentResourceRequestDTO(consentResource);
                    ExternalAPIConsentRevokeRequestDTO requestDTO = new ExternalAPIConsentRevokeRequestDTO(
                            externalAPIConsentResource, resourcePath, consentManageData.getAllowedExtensionHeaders());
                    ExternalAPIConsentRevokeResponseDTO responseDTO = ExternalAPIConsentManageUtils.
                            callExternalService(requestDTO);
                    shouldRevokeTokens = responseDTO.getRequireTokenRevocation();
                    revocationStatusName = responseDTO.getRevocationStatusName();
                } else {
                    String consentType = ConsentExtensionUtils.getConsentType(consentManageData.getRequestPath());
                    if (!consentType.equals(consentResource.getConsentType())) {
                        log.error(ConsentManageConstants.CONSENT_TYPE_MISMATCH_ERROR);
                        throw new ConsentException(ResponseStatus.BAD_REQUEST, ConsentManageConstants.
                                CONSENT_TYPE_MISMATCH_ERROR, ConsentOperationEnum.CONSENT_DELETE);
                    }

                    if (ConsentExtensionConstants.REVOKED_STATUS.equals(consentResource.getCurrentStatus()) ||
                            ConsentExtensionConstants.REJECTED_STATUS.equals(consentResource.getCurrentStatus())) {
                        log.error("Consent is already in revoked or rejected state");
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                "Consent is already in revoked or rejected state", ConsentOperationEnum.CONSENT_DELETE);
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

        //Check whether client ID exists
        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID is missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID id missing in the request.",
                    ConsentOperationEnum.CONSENT_FILE_UPLOAD);
        }
        String[] requestPathArray;
        String resourcePath = consentManageData.getRequestPath();
        if (resourcePath == null) {
            log.error("Resource path not found in the request");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource path not found in the request",
                    ConsentOperationEnum.CONSENT_FILE_UPLOAD);
        } else {
            requestPathArray = resourcePath.split("/");
        }
        if (requestPathArray.length < 2 || StringUtils.isEmpty(requestPathArray[0])) {
            log.error("Invalid Request Path");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Provided request path is invalid",
                    ConsentOperationEnum.CONSENT_FILE_UPLOAD);
        }
        String consentId = requestPathArray[1];
        if (!ConsentExtensionUtils.isConsentIdValid(consentId)) {
            log.error("Invalid Request Path. Consent Id format is not valid.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Provided request path is invalid",
                    ConsentOperationEnum.CONSENT_FILE_UPLOAD);
        }

        //Perform idempotency validation
        if (consentManageData.getHeaders().containsKey(idempotencyHeaderName) &&
                idempotencyValidator.isIdempotent(consentManageData, ConsentOperationEnum.CONSENT_FILE_UPLOAD)) {
            return;
        }
        try {
            DetailedConsentResource consentResource = consentCoreService.getDetailedConsent(consentId);
            if (consentResource == null) {
                log.error("Provided consent id is not found");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Provided consent id is not found",
                        ConsentOperationEnum.CONSENT_FILE_UPLOAD);
            }

            Object fileFromRequest = consentManageData.getPayload();
            if (!(fileFromRequest instanceof String)) {
                log.error("Invalid file content found in the request.");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid file content found in the request.",
                        ConsentOperationEnum.CONSENT_FILE_UPLOAD);
            }
            String fileContent = (String) fileFromRequest;
            ConsentFile consentFile = new ConsentFile(consentId, fileContent);

            if (isExtensionsEnabled && isExternalPreFileUploadEnabled) {
                // Call external service to validate file upload request
                ExternalAPIConsentResourceRequestDTO externalAPIConsentResource =
                        new ExternalAPIConsentResourceRequestDTO(consentResource);
                ExternalAPIPreFileUploadRequestDTO preRequestDTO =
                        new ExternalAPIPreFileUploadRequestDTO(externalAPIConsentResource, consentManageData);
                ExternalAPIPreFileUploadResponseDTO preResponseDTO = ExternalAPIConsentManageUtils.
                        callExternalService(preRequestDTO);

                consentCoreService.createConsentFile(consentFile, preResponseDTO.getConsentStatus(),
                        preResponseDTO.getUserId());
            } else {
                String applicableStatusForFileUpload = ConsentExtensionConstants.AWAIT_UPLOAD_STATUS;
                String newConsentStatus = ConsentExtensionConstants.AWAIT_AUTHORISE_STATUS;
                String userId = consentResource.getAuthorizationResources().get(0).getUserID();

                consentCoreService.createConsentFile(consentFile, newConsentStatus,
                        userId, applicableStatusForFileUpload);
            }
            //Store idempotency key as a consent attribute
            if (consentManageData.getHeaders().containsKey(idempotencyHeaderName)) {
                Map<String, String> consentAttributes = new HashMap();
                consentAttributes.put(ConsentExtensionConstants.FILE_UPLOAD_IDEMPOTENCY_KEY,
                        consentManageData.getHeaders().get(idempotencyHeaderName));
                consentAttributes.put(ConsentExtensionConstants.FILE_UPLOAD_CREATED_TIME,
                        String.valueOf(System.currentTimeMillis() / 1000));
                consentCoreService.storeConsentAttributes(consentId, consentAttributes);
            }

            String createdTime = OffsetDateTime.now().toString();

            if (isExtensionsEnabled && isExternalPostFileUploadEnabled) {
                // Call external service to enrich response
                ExternalAPIPostFileUploadRequestDTO postRequestDTO = new ExternalAPIPostFileUploadRequestDTO(consentId,
                        createdTime);
                ExternalAPIModifiedResponseDTO postResponseDTO = ExternalAPIConsentManageUtils.
                        callExternalService(postRequestDTO);

                if (postResponseDTO.getModifiedResponse() != null) {
                    consentManageData.setResponsePayload(postResponseDTO.getModifiedResponse());
                } else {
                    consentManageData.setResponsePayload(new JSONObject());
                }
                if (postResponseDTO.getResponseHeaders() != null) {
                    consentManageData.setResponseHeaders(postResponseDTO.getResponseHeaders());
                } else {
                    consentManageData.setResponseHeaders(new HashMap<>());
                }
            }
            consentManageData.setResponseStatus(ResponseStatus.OK);
        } catch (ConsentManagementException e) {
            log.error("Error Occurred while handling the request", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
                    ConsentOperationEnum.CONSENT_FILE_UPLOAD);
        }
    }

    @Override
    public void handleFileGet(ConsentManageData consentManageData) throws ConsentException {

        //Check whether client ID exists
        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID is missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID id missing in the request.",
                    ConsentOperationEnum.CONSENT_FILE_RETRIEVAL);
        }
        String[] requestPathArray;
        String resourcePath = consentManageData.getRequestPath();
        if (resourcePath == null) {
            log.error("Resource path not found in the request");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource path not found in the request",
                    ConsentOperationEnum.CONSENT_FILE_RETRIEVAL);
        } else {
            requestPathArray = resourcePath.split("/");
        }
        if (requestPathArray.length < 2 || StringUtils.isEmpty(requestPathArray[0])) {
            log.error("Invalid Request Path");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Provided request path is invalid",
                    ConsentOperationEnum.CONSENT_FILE_RETRIEVAL);
        }

        String consentId = requestPathArray[1];
        if (ConsentExtensionUtils.isConsentIdValid(consentId)) {
            try {
                ConsentResource consent = consentCoreService.getConsent(consentId, false);
                if (consent == null) {
                    log.error("Consent not found");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not found",
                            ConsentOperationEnum.CONSENT_FILE_RETRIEVAL);
                }
                // Check whether the client id is matching
                if (!consent.getClientID().equals(consentManageData.getClientId())) {
                    log.error("Client ID mismatch");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID mismatch",
                            ConsentOperationEnum.CONSENT_FILE_RETRIEVAL);
                }
                ConsentFile consentFile = consentCoreService.getConsentFile(consentId);

                if (isExtensionsEnabled && isExternalPreFileRetrievalEnabled) {
                    // Call external service before sending response.
                    Map<String, String> consentAttributes =
                            consentCoreService.getConsentAttributes(consentId).getConsentAttributes();
                    consent.setConsentAttributes(consentAttributes);
                    ExternalAPIConsentResourceRequestDTO externalAPIConsentResource =
                            new ExternalAPIConsentResourceRequestDTO(consent);
                    ExternalAPIConsentRetrieveRequestDTO requestDTO = new ExternalAPIConsentRetrieveRequestDTO(
                            externalAPIConsentResource, consentManageData);
                    // Following line is executed to handle failure scenarios.
                    ExternalAPIConsentManageUtils.callExternalServiceForFileRetrieval(requestDTO);
                } else {
                    String consentType = ConsentExtensionUtils.getConsentType(consentManageData.getRequestPath());
                    if (!consentType.equals(consent.getConsentType())) {
                        log.error(ConsentManageConstants.CONSENT_TYPE_MISMATCH_ERROR);
                        throw new ConsentException(ResponseStatus.BAD_REQUEST, ConsentManageConstants.
                                CONSENT_TYPE_MISMATCH_ERROR, ConsentOperationEnum.CONSENT_FILE_RETRIEVAL);
                    }
                }
                consentManageData.setResponsePayload(consentFile.getConsentFile());
                consentManageData.setResponseStatus(ResponseStatus.OK);
            } catch (ConsentManagementException | JSONException e) {
                log.error("Error Occurred while handling the request", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error Occurred while handling the request", ConsentOperationEnum.CONSENT_FILE_RETRIEVAL);
            }
        } else {
            log.error("Invalid consent-id found");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid consent-id found",
                    ConsentOperationEnum.CONSENT_FILE_RETRIEVAL);
        }
    }

    private DetailedConsentResource generateConsent(ExternalAPIPreConsentGenerateResponseDTO responseDTO,
                                                    String clientId) throws ConsentException {

        ExternalAPIConsentResourceResponseDTO externalAPIConsentResource = responseDTO.getConsentResource();
        try {
            DetailedConsentResource detailedConsentResource = ExternalAPIUtil.constructDetailedConsentResource(
                    externalAPIConsentResource, clientId);
            return consentCoreService.storeDetailedConsentResource(detailedConsentResource);
        } catch (ConsentManagementException e) {
            log.error("Error persisting consent", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error persisting consent",
                    ConsentOperationEnum.CONSENT_CREATE);
        }
    }

}
