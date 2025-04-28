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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentPersistStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.AmendedResources;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ExternalAPIUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consent persist step external service implementation.
 */
public class ExternalAPIConsentPersistStep implements ConsentPersistStep {

    private final ConsentCoreService consentCoreService;
    private final boolean isPreInitiatedConsent;
    private static final Log log = LogFactory.getLog(ExternalAPIConsentPersistStep.class);

    public ExternalAPIConsentPersistStep() {

        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        isPreInitiatedConsent = configParser.isPreInitiatedConsent();
    }

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        ConsentData consentData = consentPersistData.getConsentData();
        String consentId;
        DetailedConsentResource detailedConsentResource = null;
        ExternalAPIConsentResourceRequestDTO externalAPIConsentResource = null;
        try {
            if (consentData == null) {
                log.error("Consent data is not available");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, AuthErrorCode.SERVER_ERROR.name(),
                        "Consent data is not available");
            }
            if (isPreInitiatedConsent) {
                consentId = consentData.getConsentId();
                detailedConsentResource = consentCoreService.getDetailedConsent(consentId);
                externalAPIConsentResource = new ExternalAPIConsentResourceRequestDTO(detailedConsentResource);
                if (consentId == null) {
                    log.error("Consent Id is not available in consent data");
                    throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                            "Consent Id is not available in consent data", consentData.getState());
                }
            } else if (consentData.getConsentId() != null) {
                consentId = consentData.getConsentId();
                detailedConsentResource = consentCoreService.getDetailedConsent(consentId);
                externalAPIConsentResource = new ExternalAPIConsentResourceRequestDTO(detailedConsentResource);
            } else {
                consentId = UUID.randomUUID().toString();
                consentData.setConsentId(consentId);
                consentData.setType(ConsentExtensionConstants.DEFAULT);

                // Getting commonAuthId to add as a consent attribute. This is to find the consent in later stages.
                if (consentPersistData.getBrowserCookies() != null) {
                    String commonAuthId = consentPersistData.getBrowserCookies().get(
                            ConsentExtensionConstants.COMMON_AUTH_ID);
                    consentData.getMetaDataMap().put(ConsentExtensionConstants.COMMON_AUTH_ID, commonAuthId);
                }
            }
            // Call external service
            Map<String, Object> consumerInputData = consentPersistData.getMetadata();
            consumerInputData.put(ConsentExtensionConstants.PERSIST_PAYLOAD, consentPersistData.getPayload());
            consumerInputData.put(ConsentExtensionConstants.USER_ID, consentData.getUserId());

            ExternalAPIPreConsentPersistRequestDTO requestDTO = new ExternalAPIPreConsentPersistRequestDTO(
                    consentId, externalAPIConsentResource, consumerInputData, consentPersistData.getApproval());
            ExternalAPIPreConsentPersistResponseDTO responseDTO = callExternalService(requestDTO);
            ExternalAPIConsentResourceResponseDTO responseConsentResource = responseDTO.getConsentResource();
            persistConsent(responseConsentResource, consentData);

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

        ExternalServiceRequest externalServiceRequest = ExternalAPIUtil.createExternalServiceRequest(requestDTO);
        ExternalServiceResponse externalServiceResponse;

        try {
            externalServiceResponse = ServiceExtensionUtils.invokeExternalServiceCall(
                    externalServiceRequest, ServiceExtensionTypeEnum.PERSIST_AUTHORIZED_CONSENT);
        } catch (FinancialServicesException e) {
            throw new ConsentManagementException(e.getMessage());
        }
        if (externalServiceResponse.getStatus().equals(StatusEnum.ERROR)) {
            String newConsentStatus = externalServiceResponse.getData().path(
                    ConsentExtensionConstants.NEW_CONSENT_STATUS).asText();
            if (StringUtils.isNotBlank(newConsentStatus)) {
                String consentId = requestDTO.getConsentId();
                consentCoreService.updateConsentStatus(consentId, newConsentStatus);
                if (log.isDebugEnabled()) {
                    log.debug("Status of the consent with id" + consentId.replaceAll("\n\r", "") +
                            "updated to " + newConsentStatus.replaceAll("\n\r", "") +
                            "according to the error response set by the api extension.");
                }
            }
            throw new FinancialServicesException(externalServiceResponse.getData()
                    .path(FinancialServicesConstants.ERROR_MESSAGE)
                    .asText(FinancialServicesConstants.DEFAULT_ERROR_MESSAGE));
        }
        JSONObject responseJson = new JSONObject(externalServiceResponse.getData().toString());
        return new Gson().fromJson(responseJson.toString(), ExternalAPIPreConsentPersistResponseDTO.class);
    }

    /**
     * Persist consent data using the consent core service.
     *
     * @param responseConsentResource updated consent resource received from external service
     * @param consentData             consent data
     * @throws ConsentManagementException ConsentManagementException
     */
    private void persistConsent(ExternalAPIConsentResourceResponseDTO responseConsentResource,
                                ConsentData consentData) throws ConsentManagementException {

        if (isPreInitiatedConsent) {
            // Get the existing authorization resource for the initiated consent
            String primaryUserId = consentData.getUserId();
            ArrayList<AuthorizationResource> existingAuthResources =
                    consentCoreService.searchAuthorizations(consentData.getConsentId());

            String primaryAuthId = null;
            if (existingAuthResources != null && !existingAuthResources.isEmpty()) {
                if (existingAuthResources.size() == 1) {
                    // Only one entry. Treat it as the primary authorization. Here userId can be null.
                    primaryAuthId = existingAuthResources.get(0).getAuthorizationID();
                } else {
                    //  Find the authResource with the same user ID as the primary user ID.
                    for (AuthorizationResource authResource : existingAuthResources) {
                        if (primaryUserId.equals(authResource.getUserID())) {
                            primaryAuthId = authResource.getAuthorizationID();
                            break;
                        }
                    }
                }
            }

            DetailedConsentResource detailedConsentResource = ExternalAPIUtil.constructDetailedConsentResource(
                    responseConsentResource, consentData.getConsentResource(), primaryAuthId, primaryUserId);
            consentCoreService.updateConsentAndCreateAuthResources(detailedConsentResource, consentData.getUserId());
        } else {
            DetailedConsentResource detailedConsentResource = ExternalAPIUtil.constructDetailedConsentResource(
                    responseConsentResource, consentData);
            consentCoreService.storeDetailedConsentResource(detailedConsentResource);
        }

        if (responseConsentResource.getAmendments() != null && !responseConsentResource.getAmendments().isEmpty()) {

            List<ExternalAPIConsentResourceResponseDTO.AmendedAuthorization> amendedAuthorizations =
                    responseConsentResource.getAmendments();
            AmendedResources amendedResources = ExternalAPIUtil.constructAmendedResources(amendedAuthorizations);
            persistAmendedResources(amendedResources);

        }
    }

    /**
     * Persist amended Authorizations and Consent Mappings.
     *
     * @param amendedResources Amended resources
     * @throws ConsentManagementException ConsentManagementException
     */
    private void persistAmendedResources(AmendedResources amendedResources) throws ConsentManagementException {

        consentCoreService.updateAuthorizationResources(amendedResources.getAmendedAuthResources());
        consentCoreService.createConsentMappingResources(amendedResources.getNewMappingResources());
        consentCoreService.updateConsentMappingResources(amendedResources.getAmendedMappingResources());
    }

}
