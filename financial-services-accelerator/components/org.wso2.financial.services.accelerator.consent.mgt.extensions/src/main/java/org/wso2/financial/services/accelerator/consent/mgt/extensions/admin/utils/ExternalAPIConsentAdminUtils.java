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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.utils;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentRevokeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentRevokeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentSearchRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentSearchResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ExternalAPIUtil;

import java.util.UUID;

/**
 * Utility class for handling consent admin external API requests.
 */
public class ExternalAPIConsentAdminUtils {

    private static final Gson gson = new Gson();

    /**
     * Method to call external service for revoke
     *
     * @param requestDTO - Request DTO
     * @return - ExternalAPIAdminConsentRevokeResponseDTO respomse
     */
    public static ExternalAPIAdminConsentRevokeResponseDTO callExternalService(ExternalAPIAdminConsentRevokeRequestDTO
                                                                                 requestDTO)
            throws FinancialServicesException {
        JSONObject requestJson = requestDTO.toJson();
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_REVOKE);
        return gson.fromJson(responseJson.toString(), ExternalAPIAdminConsentRevokeResponseDTO.class);
    }

    /**
     * Method to call external service for search
     *
     * @param requestDTO - Request DTO
     * @return - ExternalAPIAdminConsentSearchResponseDTO respomse
     */
    public static ExternalAPIAdminConsentSearchResponseDTO callExternalService(ExternalAPIAdminConsentSearchRequestDTO
                                                                                 requestDTO)
            throws FinancialServicesException {
        JSONObject requestJson = requestDTO.toJson();
        JSONObject responseJson = callExternalService(requestJson,
                ServiceExtensionTypeEnum.ENRICH_CONSENT_SEARCH_RESPONSE);
        return gson.fromJson(responseJson.toString(), ExternalAPIAdminConsentSearchResponseDTO.class);
    }

    /**
     * Method to call external service with a json
     *
     * @param requestJson - Request json
     * @return -JSONObject respomse
     */
    public static JSONObject callExternalService(
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
}
