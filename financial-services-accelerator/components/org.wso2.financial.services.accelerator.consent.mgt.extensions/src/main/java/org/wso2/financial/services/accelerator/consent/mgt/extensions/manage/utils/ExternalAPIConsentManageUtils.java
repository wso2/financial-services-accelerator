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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ExternalAPIUtil;
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

import java.util.UUID;

/**
 * Utility class for handling consent management external API requests.
 */
public class ExternalAPIConsentManageUtils {

    private static final Gson gson = new Gson();
    private static final Log log = LogFactory.getLog(ExternalAPIConsentManageUtils.class);

    /**
     * Method to call external service for pre consent generation.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws FinancialServicesException - If there is an error while calling the external service
     */
    public static ExternalAPIPreConsentGenerateResponseDTO callExternalService(
            ExternalAPIPreConsentGenerateRequestDTO requestDTO) throws FinancialServicesException {

        log.debug("Calling external service for pre consent generation");
        JSONObject requestJson = new JSONObject(requestDTO);
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_CREATION);
        return gson.fromJson(responseJson.toString(), ExternalAPIPreConsentGenerateResponseDTO.class);
    }

    /**
     * Method to call external service for post consent generation.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws FinancialServicesException - If there is an error while calling the external service
     */
    public static ExternalAPIModifiedResponseDTO callExternalService(
            ExternalAPIPostConsentGenerateRequestDTO requestDTO) throws FinancialServicesException {

        JSONObject requestJson = requestDTO.toJson();
        log.debug("Calling external service for post consent generation");
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.ENRICH_CONSENT_CREATION_RESPONSE);
        return gson.fromJson(responseJson.toString(), ExternalAPIModifiedResponseDTO.class);
    }

    /**
     * Method to call external service for consent revocation.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws FinancialServicesException - If there is an error while calling the external service
     */
    public static ExternalAPIConsentRevokeResponseDTO callExternalService(
            ExternalAPIConsentRevokeRequestDTO requestDTO) throws FinancialServicesException {

        JSONObject requestJson = requestDTO.toJson();
        log.debug("Calling external service for consent revocation");
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_REVOKE);
        return gson.fromJson(responseJson.toString(), ExternalAPIConsentRevokeResponseDTO.class);
    }

    /**
     * Method to call external service for consent retrieval.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws FinancialServicesException - If there is an error while calling the external service
     */
    public static ExternalAPIModifiedResponseDTO callExternalService(
            ExternalAPIConsentRetrieveRequestDTO requestDTO) throws FinancialServicesException {

        JSONObject requestJson = requestDTO.toJson();
        log.debug("Calling external service for consent retrieval");
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_RETRIEVAL);
        return gson.fromJson(responseJson.toString(), ExternalAPIModifiedResponseDTO.class);
    }

    /**
     * Method to call external service for pre-process file upload.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws FinancialServicesException - If there is an error while calling the external service
     */
    public static ExternalAPIPreFileUploadResponseDTO callExternalService(
            ExternalAPIPreFileUploadRequestDTO requestDTO) throws FinancialServicesException {

        JSONObject requestJson = requestDTO.toJson();
        log.debug("Calling external service for pre-process file upload");
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_FILE_UPLOAD);
        return gson.fromJson(responseJson.toString(), ExternalAPIPreFileUploadResponseDTO.class);
    }

    /**
     * Method to call external service for post-process file upload.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws FinancialServicesException - If there is an error while calling the external service
     */
    public static ExternalAPIModifiedResponseDTO callExternalService(
            ExternalAPIPostFileUploadRequestDTO requestDTO) throws FinancialServicesException {

        JSONObject requestJson = new JSONObject(requestDTO);
        log.debug("Calling external service for post-process file upload");
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.ENRICH_CONSENT_FILE_RESPONSE);
        return gson.fromJson(responseJson.toString(), ExternalAPIModifiedResponseDTO.class);
    }

    /**
     * Method to call external service for file retrieval.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws FinancialServicesException - If there is an error while calling the external service
     */
    public static ExternalAPIModifiedResponseDTO callExternalServiceForFileRetrieval(
            ExternalAPIConsentRetrieveRequestDTO requestDTO) throws FinancialServicesException {

        JSONObject requestJson = requestDTO.toJson();
        log.debug("Calling external service for file retrieval");
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.VALIDATE_CONSENT_FILE_RETRIEVAL);
        return gson.fromJson(responseJson.toString(), ExternalAPIModifiedResponseDTO.class);
    }

    /**
     * Method to call external service using a Json.
     *
     * @param requestJson - Request JSON
     * @param serviceType - Service Type
     * @return - Response JSON
     * @throws FinancialServicesException - If there is an error while calling the external service
     */
    private static JSONObject callExternalService(
            JSONObject requestJson, ServiceExtensionTypeEnum serviceType) throws FinancialServicesException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Invoking external service call for service type: %s",
                    serviceType.toString().replaceAll("\r\n", " ")));
        }
        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                UUID.randomUUID().toString(), requestJson);
        ExternalServiceResponse response =
                ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest, serviceType);
        if (response.getStatus().equals(StatusEnum.ERROR)) {
            ExternalAPIUtil.handleResponseError(response);
        }
        if (response.getData() == null) {
            return new JSONObject();
        }
        return new JSONObject(response.getData().toString());
    }
}
