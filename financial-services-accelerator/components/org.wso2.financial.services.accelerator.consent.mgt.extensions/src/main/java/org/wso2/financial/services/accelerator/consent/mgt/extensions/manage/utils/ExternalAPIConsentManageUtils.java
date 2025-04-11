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
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ExternalAPIUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRetrieveRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRetrieveResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateResponseDTO;

import java.util.UUID;

/**
 * Utility class for handling consent management external API requests.
 */
public class ExternalAPIConsentManageUtils {

    /**
     * Method to call external service for pre consent generation.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws ConsentManagementException - Consent Management Exception
     */
    public static ExternalAPIPreConsentGenerateResponseDTO callExternalService(
            ExternalAPIPreConsentGenerateRequestDTO requestDTO) throws ConsentManagementException {

        JSONObject requestJson = new JSONObject(requestDTO);
        JSONObject responseJson = callExternalService(requestJson,
                   ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_CREATION);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIPreConsentGenerateResponseDTO.class);
    }

    /**
     * Method to call external service for post consent generation.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws ConsentManagementException - Consent Management Exception
     */
    public static ExternalAPIPostConsentGenerateResponseDTO callExternalService(
            ExternalAPIPostConsentGenerateRequestDTO requestDTO) throws ConsentManagementException {

        JSONObject requestJson = requestDTO.toJson();
        JSONObject responseJson = callExternalService(requestJson,
                   ServiceExtensionTypeEnum.ENRICH_CONSENT_CREATION_RESPONSE);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIPostConsentGenerateResponseDTO.class);
    }

    /**
     * Method to call external service for consent revocation.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws ConsentManagementException - Consent Management Exception
     */
    public static ExternalAPIConsentRevokeResponseDTO callExternalService(
            ExternalAPIConsentRevokeRequestDTO requestDTO) throws ConsentManagementException {

        JSONObject requestJson = requestDTO.toJson();
        JSONObject responseJson = callExternalService(requestJson,
                   ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_REVOKE);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIConsentRevokeResponseDTO.class);
    }

    /**
     * Method to call external service for consent retrieval.
     *
     * @param requestDTO - Request DTO
     * @return - Response DTO
     * @throws ConsentManagementException - Consent Management Exception
     */
    public static ExternalAPIConsentRetrieveResponseDTO callExternalService(
            ExternalAPIConsentRetrieveRequestDTO requestDTO) throws ConsentManagementException {

        JSONObject requestJson = requestDTO.toJson();
        JSONObject responseJson = callExternalService(requestJson,
                   ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_RETRIEVAL);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIConsentRetrieveResponseDTO.class);
    }

    /**
     * Method to call external service using a Json.
     *
     * @param requestJson - Request JSON
     * @param serviceType - Service Type
     * @return - Response JSON
     * @throws ConsentManagementException - Consent Management Exception
     */
    private static JSONObject callExternalService(
            JSONObject requestJson, ServiceExtensionTypeEnum serviceType) throws ConsentManagementException {

        try {
            ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                    UUID.randomUUID().toString(), requestJson);
            ExternalServiceResponse response =
                    ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest, serviceType);
            if (response.getStatus().equals(StatusEnum.ERROR)) {
                ExternalAPIUtil.handleResponseError(response);
            }
            return new JSONObject(response.getData().toString());
        } catch (FinancialServicesException e) {
            throw new ConsentManagementException("Error occurred while invoking external service call", e);
        }
    }
}
