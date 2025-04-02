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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentAuthorizeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentAuthorizeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.UUID;

/**
 * Consent retrieval step default implementation.
 */
public class ExternalAPIConsentRetrievalStep implements ConsentRetrievalStep {

    private final ConsentCoreService consentCoreService;
    private static final Log log = LogFactory.getLog(ExternalAPIConsentRetrievalStep.class);

    public ExternalAPIConsentRetrievalStep() {
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
    }

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }
        String requestObject = ConsentAuthorizeUtil.extractRequestObject(consentData.getSpQueryParams());
        String consentId;

        try {
            consentId = ConsentAuthorizeUtil.extractConsentId(requestObject);
            setMandatoryConsentData(consentId, consentData);
            ExternalAPIPreConsentAuthorizeRequestDTO requestDTO = new ExternalAPIPreConsentAuthorizeRequestDTO(
                    consentData);

            log.debug("Calling external service to get data to be displayed");
            ExternalAPIPreConsentAuthorizeResponseDTO responseDTO = callExternalService(requestDTO);
            JSONArray consentDataJsonArray = new JSONArray(new Gson().toJson(responseDTO.getConsentData()));
            JSONArray consumerDataJsonArray = new JSONArray(new Gson().toJson(responseDTO.getConsumerData()));

            // Set data to json object to be displayed in consent page.
            jsonObject.put("consentData", consentDataJsonArray);
            jsonObject.put("accounts", consumerDataJsonArray);
        } catch (FinancialServicesException e) {
            // ToDo: Improve error handling
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    e.getMessage(), consentData.getState());
        }
    }

    /**
     * Set mandatory consent data required by retrieval endpoint and persist steps.
     *
     * @param consentId   consent id
     * @param consentData consent data
     * @throws ConsentManagementException ConsentManagementException
     */
    private void setMandatoryConsentData(String consentId, ConsentData consentData) throws ConsentManagementException {

        ConsentResource consentResource = consentCoreService.getConsent(consentId, false);
        AuthorizationResource authorizationResource = consentCoreService.searchAuthorizations(consentId).get(0);

        consentData.setConsentId(consentId);
        consentData.setType(consentResource.getConsentType());
        consentData.setConsentResource(consentResource);
        consentData.setAuthResource(authorizationResource);
    }

    /**
     * Create request object to be sent to the external retrieve API.
     *
     * @param requestDTO request data
     * @return ExternalServiceRequest
     */
    private ExternalServiceRequest createExternalServiceRequest(ExternalAPIPreConsentAuthorizeRequestDTO requestDTO) {

        JSONObject requestJson = requestDTO.toJson();
        return new ExternalServiceRequest(UUID.randomUUID().toString(), requestJson);
    }

    /**
     * Call external API to get consent data to be displayed.
     *
     * @param requestDTO request data
     * @return ExternalAPIConsentRetrievalResponseDTO
     */
    private ExternalAPIPreConsentAuthorizeResponseDTO callExternalService(
            ExternalAPIPreConsentAuthorizeRequestDTO requestDTO) throws FinancialServicesException {

        ExternalServiceRequest externalServiceRequest = createExternalServiceRequest(requestDTO);
        ExternalServiceResponse externalServiceResponse = ServiceExtensionUtils.invokeExternalServiceCall(
                externalServiceRequest, ServiceExtensionTypeEnum.PRE_CONSENT_AUTHORIZATION);
        if (externalServiceResponse.getStatus().equals(StatusEnum.ERROR)) {
            throw new FinancialServicesException(externalServiceResponse.getData()
                    .path(FinancialServicesConstants.ERROR_MESSAGE)
                    .asText(FinancialServicesConstants.DEFAULT_ERROR_MESSAGE));
        }
        JSONObject responseJson = new JSONObject(externalServiceResponse.getData().toString());
        return new Gson().fromJson(responseJson.toString(), ExternalAPIPreConsentAuthorizeResponseDTO.class);
    }

}
