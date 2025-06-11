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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.wso2.financial.services.accelerator.common.validator.FinancialServicesValidator;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentAuthorizeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentAuthorizeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Consent retrieval step default implementation.
 */
public class ExternalAPIConsentRetrievalStep implements ConsentRetrievalStep {

    private final ConsentCoreService consentCoreService;
    private final boolean isPreInitiatedConsent;
    private static final Log log = LogFactory.getLog(ExternalAPIConsentRetrievalStep.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static FinancialServicesValidator fsValidator = FinancialServicesValidator.getInstance();

    public ExternalAPIConsentRetrievalStep() {

        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        isPreInitiatedConsent = configParser.isPreInitiatedConsent();
    }

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }
        String requestObject = ConsentAuthorizeUtil.extractRequestObject(consentData.getSpQueryParams());
        JSONObject requestParameters = ConsentAuthorizeUtil.getRequestObjectJson(requestObject);
        String consentId = ConsentAuthorizeUtil.extractConsentId(requestObject);

        try {
            if (isPreInitiatedConsent) {
                setMandatoryConsentData(consentId, consentData);
            }

            ExternalAPIConsentResourceRequestDTO externalAPIConsentResource = null;
            String consentFileContent = "";
            if (consentId != null) {
                DetailedConsentResource detailedConsentResource = consentCoreService.getDetailedConsent(consentId);
                if (detailedConsentResource != null) {
                    externalAPIConsentResource = new ExternalAPIConsentResourceRequestDTO(detailedConsentResource);
                    try {
                        consentFileContent = consentCoreService.getConsentFile(consentId).getConsentFile();
                        externalAPIConsentResource.setFileContent(consentFileContent);
                    } catch (ConsentManagementException e) {
                        log.debug("No consent file found for the given consent Id: " +
                                consentId.replaceAll("\n\r", ""));
                    }
                }
            }
            ExternalAPIPreConsentAuthorizeRequestDTO requestDTO = new ExternalAPIPreConsentAuthorizeRequestDTO(
                    consentData, externalAPIConsentResource, requestParameters);

            log.debug("Calling external service to get data to be displayed");
            ExternalAPIPreConsentAuthorizeResponseDTO responseDTO = callExternalService(requestDTO);

            // Validating object
            String responseDTOViolation = fsValidator.getFirstViolation(responseDTO);

            if (!StringUtils.isEmpty(responseDTOViolation)) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        responseDTOViolation, consentData.getState());
            }

            // Filter out consent and consumer data
            // Append consumer data to json object to be displayed in consent page
            JSONObject consentDataJsonObject = new JSONObject(objectMapper.writeValueAsString(
                    responseDTO.getConsentData()));
            jsonObject.put(ConsentAuthorizeConstants.CONSENT_DATA, consentDataJsonObject);

            // Append consumer data, if exists, to json object
            if (responseDTO.getConsumerData() != null) {
                JSONObject consumerDataJsonObject = new JSONObject(objectMapper.writeValueAsString(
                        responseDTO.getConsumerData()));
                jsonObject.put(ConsentAuthorizeConstants.CONSUMER_DATA, consumerDataJsonObject);
            }

            // Set request parameters as metadata to be used in persistence extension
            consentData.addData(ConsentExtensionConstants.REQUEST_PARAMETERS, requestParameters);

            // Setting consent type as default for scope based consents
            if (!isPreInitiatedConsent) {
                consentData.setType(ConsentExtensionConstants.DEFAULT);
            }

            // Storing consent metadata for retrieval at persistence
            if (responseDTO.getMetadata() != null) {
                consentData.setMetaDataMap(responseDTO.getMetadata());
            }

        } catch (FinancialServicesException e) {
            // ToDo: Improve error handling
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    e.getMessage(), consentData.getState());
        } catch (JsonProcessingException e) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
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

        consentData.setConsentId(consentId);
        consentData.setType(consentResource.getConsentType());
        consentData.setConsentResource(consentResource);

        try {
            ArrayList<AuthorizationResource> authorizationResourceList = consentCoreService
                    .searchAuthorizations(consentId);
            if (!authorizationResourceList.isEmpty()) {
                AuthorizationResource authorizationResource = authorizationResourceList.get(0);
                consentData.setAuthResource(authorizationResource);
            }
        } catch (ConsentManagementException e) {
            if (log.isDebugEnabled()) {
                String sanitizedConsentId = consentId.replaceAll("[\r\n]", "");
                log.debug("No authorizations were found for the consent " + sanitizedConsentId +
                        ". Proceeding without adding authorization resources.");
            }
        }
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
            ExternalAPIPreConsentAuthorizeRequestDTO requestDTO)
            throws FinancialServicesException, JsonProcessingException {

        ExternalServiceRequest externalServiceRequest = createExternalServiceRequest(requestDTO);
        ExternalServiceResponse externalServiceResponse = ServiceExtensionUtils.invokeExternalServiceCall(
                externalServiceRequest, ServiceExtensionTypeEnum.POPULATE_CONSENT_AUTHORIZE_SCREEN);
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
        return objectMapper.readValue(responseJson.toString(), ExternalAPIPreConsentAuthorizeResponseDTO.class);
    }
    

}
