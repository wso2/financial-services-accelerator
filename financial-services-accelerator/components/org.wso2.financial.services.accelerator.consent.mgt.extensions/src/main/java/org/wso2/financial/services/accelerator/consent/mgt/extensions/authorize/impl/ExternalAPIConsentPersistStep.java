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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentPersistStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ExternalAPIUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.Map;
import java.util.UUID;

/**
 * Consent persist step external service implementation.
 */
public class ExternalAPIConsentPersistStep implements ConsentPersistStep {

    private final ConsentCoreService consentCoreService;
    private static final Log log = LogFactory.getLog(ExternalAPIConsentPersistStep.class);

    public ExternalAPIConsentPersistStep() {

        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
    }

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        ConsentData consentData = consentPersistData.getConsentData();
        try {
            if (consentData == null) {
                log.error("Consent data is not available");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, AuthErrorCode.SERVER_ERROR.name(),
                        "Consent data is not available");
            }
            String consentId = consentData.getConsentId();
            if (consentId == null) {
                log.error("Consent Id is not available in consent data");
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Consent Id is not available in consent data", consentData.getState());
            }
            if (consentData.getAuthResource() == null) {
                log.error("Authorization resource is not available in consent data");
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Authorization resource is not available in consent data", consentData.getState());
            }
            // Call external service
            Map<String, Object> metadata = consentPersistData.getMetadata();
            metadata.put("persist-payload", consentPersistData.getPayload());

            ExternalAPIPreConsentPersistRequestDTO requestDTO = new ExternalAPIPreConsentPersistRequestDTO(
                    consentId, consentPersistData.getApproval(), consentData.getType(),
                    consentPersistData.getMetadata());
            ExternalAPIPreConsentPersistResponseDTO responseDTO = callExternalService(requestDTO);

            persistConsent(responseDTO, consentData);

        } catch (FinancialServicesException e) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    e.getMessage(), consentData.getState());
        }
    }

    /**
     * Call external API to get consent data to be persisted.
     *
     * @param requestDTO request data
     * @return ExternalAPIPreConsentPersistResponseDTO
     */
    private ExternalAPIPreConsentPersistResponseDTO callExternalService(
            ExternalAPIPreConsentPersistRequestDTO requestDTO) throws FinancialServicesException {

        ExternalServiceRequest externalServiceRequest = createExternalServiceRequest(requestDTO);
        ExternalServiceResponse externalServiceResponse;

        try {
            externalServiceResponse = ServiceExtensionUtils.invokeExternalServiceCall(
                    externalServiceRequest, ServiceExtensionTypeEnum.PRE_CONSENT_PERSISTENCE);
        } catch (FinancialServicesException e) {
            throw new ConsentManagementException(e.getMessage());
        }
        if (externalServiceResponse.getStatus().equals(StatusEnum.ERROR)) {
            throw new FinancialServicesException(externalServiceResponse.getData()
                    .path(FinancialServicesConstants.ERROR_MESSAGE)
                    .asText(FinancialServicesConstants.DEFAULT_ERROR_MESSAGE));
        }
        JSONObject responseJson = new JSONObject(externalServiceResponse.getData().toString());
        return new Gson().fromJson(responseJson.toString(), ExternalAPIPreConsentPersistResponseDTO.class);
    }

    /**
     * Persist consent data.
     *
     * @param responseDTO external service response data
     * @param consentData consent data
     * @throws ConsentManagementException
     */
    private void persistConsent(ExternalAPIPreConsentPersistResponseDTO responseDTO,
                                ConsentData consentData) throws ConsentManagementException {
        String primaryUserId = consentData.getUserId();
        String primaryAuthId = consentCoreService.getDetailedConsent(consentData.getConsentId()).
                getAuthorizationResources().get(0).getAuthorizationID();
        DetailedConsentResource detailedConsentResource = ExternalAPIUtil.constructDetailedConsentResource(
                responseDTO, consentData.getConsentResource(), primaryAuthId, primaryUserId);
        consentCoreService.updateConsentAndCreateAuthResources(detailedConsentResource, consentData.getUserId());
    }

    /**
     * Create request object to be sent to the external service.
     *
     * @param requestDTO request data
     * @return ExternalServiceRequest
     */
    private ExternalServiceRequest createExternalServiceRequest(ExternalAPIPreConsentPersistRequestDTO requestDTO) {

        JSONObject requestJson = new JSONObject(requestDTO);
        return new ExternalServiceRequest(UUID.randomUUID().toString(), requestJson);
    }

}
