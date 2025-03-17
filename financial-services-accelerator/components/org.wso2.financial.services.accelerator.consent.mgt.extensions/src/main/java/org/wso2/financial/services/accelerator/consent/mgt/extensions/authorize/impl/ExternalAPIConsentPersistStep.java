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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.Request;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentPersistStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistExternalRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistExternalResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Consent persist step default implementation.
 */
public class ExternalAPIConsentPersistStep implements ConsentPersistStep {

    private ConsentCoreService consentCoreService;
    private static final Log log = LogFactory.getLog(ExternalAPIConsentPersistStep.class);
    private static final String CONSENT_PERSIST_DATA_OBJECT_KEY = "consent_persist_data";

    public ExternalAPIConsentPersistStep() {
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
    }

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        try {
            ConsentData consentData = consentPersistData.getConsentData();
            ConsentResource consentResource;

            if (consentData == null) {
                log.error("Consent data is not available");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, AuthErrorCode.SERVER_ERROR.name(),
                        "Consent data is not available");
            }

            if (consentData.getConsentId() == null) {
                log.error("Consent ID not available in consent data");
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Consent ID not available in consent data", consentData.getState());
            }

            if (consentData.getAuthResource() == null) {
                log.error("Auth resource not available in consent data");
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Auth resource not available in consent data", consentData.getState());
            }

            consentResource = consentCoreService.getConsent(consentData.getConsentId(), false);
            consentData.setConsentResource(consentResource);
            consentPersistData.setConsentData(consentData);

            // Call external service and get updated consent persist data.
            ConsentPersistExternalRequestDTO externalRequestDTO = getExternalRequestDTO(consentPersistData);
            ConsentPersistExternalResponseDTO externalResponseDTO = callExternalService(externalRequestDTO);
            updateConsentPersistData(consentPersistData, externalResponseDTO);

            consentCoreService.bindUserAccountsToConsent(consentResource, consentData.getUserId(),
                    consentData.getAuthResource().getAuthorizationID(),
                    getConsentedAccounts(consentPersistData.getPayload(), true), "Authorized", "Authorized");


        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while persisting consent", e);
        }
    }

    /**
     * Retrieve account ID consented by the user from the object.
     *
     * @param persistPayload payload to persist
     * @return Account data map
     */
    //ToDo: update method to match external request
    private static Map<String, ArrayList<String>> getConsentedAccounts(JSONObject persistPayload, boolean isApproved) {

        Map<String, ArrayList<String>> accountIDsMapWithPermissions = new HashMap<>();
        ArrayList<String> permissionsDefault = new ArrayList<>();
        permissionsDefault.add(ConsentExtensionConstants.PRIMARY);

        // Check whether payment account exists
        // Payment Account is the debtor account sent in the payload
        if (persistPayload.has(ConsentExtensionConstants.PAYMENT_ACCOUNT) &&
                StringUtils.isNotBlank(persistPayload.getString(ConsentExtensionConstants.PAYMENT_ACCOUNT))) {
            // Check whether account-id is in String format
            if (!(persistPayload.get(ConsentExtensionConstants.PAYMENT_ACCOUNT) instanceof String)) {
                log.error(ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
            }

            String paymentAccount = persistPayload.getString(ConsentExtensionConstants.PAYMENT_ACCOUNT);
            accountIDsMapWithPermissions.put(paymentAccount, permissionsDefault);
        } else if (persistPayload.has(ConsentExtensionConstants.COF_ACCOUNT) &&
                StringUtils.isNotBlank(persistPayload.getString(ConsentExtensionConstants.COF_ACCOUNT))) {
            // Check whether account-id is in String format
            if (!(persistPayload.get(ConsentExtensionConstants.COF_ACCOUNT) instanceof String)) {
                log.error(ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
            }

            String paymentAccount = persistPayload.getString(ConsentExtensionConstants.COF_ACCOUNT);
            accountIDsMapWithPermissions.put(paymentAccount, permissionsDefault);
        } else {
            //Check whether account Ids are in array format
            if (!(persistPayload.get(ConsentExtensionConstants.ACCOUNT_IDS) instanceof JSONArray)) {
                log.error(ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
            }

            //Check whether account Ids are strings
            JSONArray accountIds = persistPayload.getJSONArray(ConsentExtensionConstants.ACCOUNT_IDS);
            for (Object account : accountIds) {
                if (!(account instanceof String)) {
                    log.error(ConsentAuthorizeConstants.ACCOUNT_ID_FORMAT_ERROR);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            ConsentAuthorizeConstants.ACCOUNT_ID_FORMAT_ERROR);
                }
                if (((String) account).isEmpty()) {
                    if (isApproved) {
                        log.error(ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
                    } else {
                        account = "n/a";
                    }
                }
                accountIDsMapWithPermissions.put((String) account, permissionsDefault);
            }
        }
        return accountIDsMapWithPermissions;
    }

    private ConsentPersistExternalResponseDTO callExternalService(ConsentPersistExternalRequestDTO externalRequestDTO) {

        ExternalServiceRequest externalServiceRequest = createExternalServiceRequest(externalRequestDTO);
        JSONObject response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.CONSENT_PERSISTENCE);

        return new Gson().fromJson(response.toString(), ConsentPersistExternalResponseDTO.class);

    }

    private ExternalServiceRequest createExternalServiceRequest(ConsentPersistExternalRequestDTO externalRequestDTO) {

        JSONObject payload = new JSONObject();
        String consentPersistDataJsonString = new Gson().toJson(externalRequestDTO);
        payload.append(CONSENT_PERSIST_DATA_OBJECT_KEY, consentPersistDataJsonString);
        Request eventRequest = new Request(payload, new HashMap<>());
        return new ExternalServiceRequest(UUID.randomUUID().toString(), eventRequest, "");
    }

    private ConsentPersistExternalRequestDTO getExternalRequestDTO(ConsentPersistData consentPersistData) {
        ConsentPersistExternalRequestDTO externalRequestDTO = new ConsentPersistExternalRequestDTO();

        JSONObject payloadJson = consentPersistData.getPayload();
        externalRequestDTO.setPayload(payloadJson);
        return externalRequestDTO;
    }

    private void updateConsentPersistData(ConsentPersistData consentPersistData,
                                          ConsentPersistExternalResponseDTO externalResponseDTO) {

        consentPersistData.setPayload(externalResponseDTO.getPayload());
    }

}
