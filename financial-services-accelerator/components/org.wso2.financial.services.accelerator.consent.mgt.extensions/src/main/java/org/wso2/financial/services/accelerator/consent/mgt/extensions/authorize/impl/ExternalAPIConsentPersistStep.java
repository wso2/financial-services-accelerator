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

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentPersistStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.AmendedResources;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
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
    private final List<String> preInitiatedConsentScopes;
    private final List<String> scopeBasedConsentScopes;
    private static final Log log = LogFactory.getLog(ExternalAPIConsentPersistStep.class);
    private static final Gson gson = new Gson();

    public ExternalAPIConsentPersistStep() {

        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        preInitiatedConsentScopes = configParser.getPreInitiatedConsentScopes();
        scopeBasedConsentScopes = configParser.getScopeBasedConsentScopes();
    }

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        ConsentData consentData = consentPersistData.getConsentData();
        JSONObject consentPersistPayload = consentPersistData.getPayload();
        String consentId;
        DetailedConsentResource detailedConsentResource = null;
        ExternalAPIConsentResourceRequestDTO externalAPIConsentResource = null;

        // Get request object parameters
        JSONObject requestParameters = new JSONObject();
        if (consentData != null && consentData.getMetaDataMap()
                .getOrDefault(ConsentExtensionConstants.REQUEST_PARAMETERS, null) != null) {

            requestParameters = (JSONObject) consentData.getMetaDataMap()
                    .get(ConsentExtensionConstants.REQUEST_PARAMETERS);
            consentData.getMetaDataMap().remove(ConsentExtensionConstants.REQUEST_PARAMETERS);
        }

        // If there are no request object parameters, add the scope sent as a query parameter.
        if (requestParameters.isEmpty() && consentData != null &&
                consentData.getScopeString() != null) {
            requestParameters.put(FinancialServicesConstants.SCOPE,
                    consentData.getScopeString());
        }

        try {
            if (consentData == null) {
                log.error("Consent data is not available");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, AuthErrorCode.SERVER_ERROR.name(),
                        "Consent data is not available");
            }

            boolean isPreInitiatedConsentFlow = FinancialServicesUtils.isPreInitiatedConsentFlow(
                    consentData.getScopeString(), preInitiatedConsentScopes, scopeBasedConsentScopes);
            if (log.isDebugEnabled()) {
                log.debug("Pre-initiated consent flow check result: " + isPreInitiatedConsentFlow);
            }

            if (isPreInitiatedConsentFlow) {
                consentId = consentData.getConsentId();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Processing pre-initiated consent with ID: %s",
                            consentId.replaceAll("\n\r", "")));
                }
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

                // Getting commonAuthId to add as a consent attribute. This is to find the consent in later stages.
                if (consentPersistData.getBrowserCookies() != null) {
                    String commonAuthId = consentPersistData.getBrowserCookies().get(
                            ConsentExtensionConstants.COMMON_AUTH_ID);
                    consentData.getMetaDataMap().put(ConsentExtensionConstants.COMMON_AUTH_ID, commonAuthId);
                }
            }

            Map<String, Object> consentMetadata = consentData.getMetaDataMap();
            if (consentMetadata != null && !consentMetadata.isEmpty()) {
                // Reconstruct authorizedData
                ConsentAuthorizeUtil.addAuthorizedDataObject(consentPersistPayload, consentMetadata);

                // Add additional properties
                ConsentAuthorizeUtil.addIsReauthorization(consentPersistPayload, consentMetadata);

                // Remove attributes only used for reconstructing authorizedData object
                ConsentAuthorizeUtil.trimPersistPayload(consentPersistPayload);
                ConsentAuthorizeUtil.trimConsentMetaData(consentMetadata);

                // Append metadata to userGrantedData
                JSONObject metadataJSON;
                if (consentPersistPayload.has(ConsentAuthorizeConstants.METADATA)) {
                    // Metadata from consent confirm servlet
                    metadataJSON = consentPersistPayload.getJSONObject(ConsentAuthorizeConstants.METADATA);
                } else {
                    metadataJSON = new JSONObject();
                }

                // Consent metadata from populate consent page api call
                JSONObject consentMetadataJSON = new JSONObject(consentMetadata);
                consentMetadataJSON.keySet().forEach(k -> metadataJSON.put(k, consentMetadataJSON.get(k)));

                // Add all metadata to persist payload
                consentPersistPayload.put(ConsentAuthorizeConstants.METADATA, metadataJSON);
            }

            // Call external service
            ExternalAPIPreConsentPersistRequestDTO.UserGrantedDataDTO userGrantedData = new
                    ExternalAPIPreConsentPersistRequestDTO.UserGrantedDataDTO(consentPersistPayload,
                    requestParameters, consentData.getUserId());

            ExternalAPIPreConsentPersistRequestDTO requestDTO = new ExternalAPIPreConsentPersistRequestDTO(
                    consentId, externalAPIConsentResource, userGrantedData, consentPersistData.getApproval());
            ExternalAPIPreConsentPersistResponseDTO responseDTO = callExternalService(requestDTO);
            ExternalAPIConsentResourceResponseDTO responseConsentResource = responseDTO.getConsentResource();
            persistConsent(responseConsentResource, consentData);
            ConsentAuthorizeUtil.publishConsentApprovalStatus(consentPersistData);

        } catch (FinancialServicesException e) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    e.getMessage(), consentData.getState());
        } catch (JsonProcessingException e) {
            log.error("A JSON object mapping has failed", e);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                    e.getMessage(), consentData.getState());
        } catch (IllegalStateException e) {
            log.error(e.getMessage().replaceAll("\n\r", ""), e);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                    e.getMessage().replaceAll("\n\r", ""), consentData.getState());
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
        log.debug("Invoking external service for consent persistence with service type: PERSIST_AUTHORIZED_CONSENT");
        ExternalServiceResponse externalServiceResponse = ServiceExtensionUtils.invokeExternalServiceCall(
                externalServiceRequest, ServiceExtensionTypeEnum.PERSIST_AUTHORIZED_CONSENT);

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
        return gson.fromJson(responseJson.toString(), ExternalAPIPreConsentPersistResponseDTO.class);
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

        boolean isPreInitiatedConsentFlow = FinancialServicesUtils.isPreInitiatedConsentFlow(
                consentData.getScopeString(), preInitiatedConsentScopes, scopeBasedConsentScopes);

        if (isPreInitiatedConsentFlow) {
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
