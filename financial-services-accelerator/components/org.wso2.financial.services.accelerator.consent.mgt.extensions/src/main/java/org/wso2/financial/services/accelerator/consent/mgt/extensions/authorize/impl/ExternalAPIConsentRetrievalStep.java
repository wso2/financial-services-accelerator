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
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.Request;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIConsentRetrievalRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIConsentRetrievalResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.HashMap;
import java.util.UUID;

/**
 * Consent retrieval step default implementation.
 */
public class ExternalAPIConsentRetrievalStep implements ConsentRetrievalStep {

    private final ConsentCoreService consentCoreService;
    private static final Log log = LogFactory.getLog(ExternalAPIConsentRetrievalStep.class);
    private static final String CONSENT_DATA_OBJECT_KEY = "consent_data";

    public ExternalAPIConsentRetrievalStep() {
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
    }


    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }
        String requestObject = ConsentAuthorizeUtil.extractRequestObject(consentData.getSpQueryParams());
        String consentId = ConsentAuthorizeUtil.extractConsentId(requestObject);

        try {
            setMandatoryConsentData(consentId, consentData);
            ExternalAPIConsentRetrievalRequestDTO requestDTO = new ExternalAPIConsentRetrievalRequestDTO(consentData);

            log.debug("Calling external service to get consent data to be displayed");
            ExternalAPIConsentRetrievalResponseDTO responseDTO = callExternalService(requestDTO);
            JSONObject displayConsentData = new JSONObject(responseDTO.getDisplayConsentData());
            for (String key : displayConsentData.keySet()) {
                jsonObject.put(key, displayConsentData.get(key));
            }
        } catch (ConsentManagementException e) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    "Exception occurred while getting consent data", consentData.getState());
        }
    }

    /**
     * Set mandatory consent data.
     *
     * @param consentId
     * @param consentData
     * @throws ConsentManagementException
     */
    private void setMandatoryConsentData(String consentId, ConsentData consentData) throws ConsentManagementException {

        ConsentResource consentResource = consentCoreService.getConsent(consentId, false);
        AuthorizationResource authorizationResource = consentCoreService.searchAuthorizations(consentId).get(0);

        // Set the consent data to be used in consent persistence.
        consentData.setConsentId(consentId);
        consentData.setType(consentResource.getConsentType());
        consentData.setConsentResource(consentResource);
        consentData.setAuthResource(authorizationResource);
    }

    private ExternalServiceRequest createExternalServiceRequest(
            ExternalAPIConsentRetrievalRequestDTO requestDTO) {

        String requestJsonString = new Gson().toJson(requestDTO);
        JSONObject payload = new JSONObject();
        payload.put(CONSENT_DATA_OBJECT_KEY, requestJsonString);

        Request eventRequest = new Request(payload, new HashMap<>());
        return new ExternalServiceRequest(UUID.randomUUID().toString(), eventRequest, "");
    }

    private ExternalAPIConsentRetrievalResponseDTO callExternalService(
            ExternalAPIConsentRetrievalRequestDTO requestDTO) {

        ExternalServiceRequest externalServiceRequest = createExternalServiceRequest(requestDTO);
        JSONObject responseJson = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.PRE_CONSENT_AUTHORIZATION);
        return new Gson().fromJson(responseJson.toString(), ExternalAPIConsentRetrievalResponseDTO.class);
    }

}
