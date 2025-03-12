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
            ConsentData initialConsentData = consentPersistData.getConsentData();
            ConsentResource consentResource;

            if (initialConsentData == null) {
                log.error("Consent data is not available");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, AuthErrorCode.SERVER_ERROR.name(),
                        "Consent data is not available");
            }

            if (initialConsentData.getConsentId() == null) {
                log.error("Consent ID not available in consent data");
                throw new ConsentException(initialConsentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Consent ID not available in consent data", initialConsentData.getState());
            }

            if (initialConsentData.getAuthResource() == null) {
                log.error("Auth resource not available in consent data");
                throw new ConsentException(initialConsentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Auth resource not available in consent data", initialConsentData.getState());
            }

            if (initialConsentData.getConsentResource() == null) {
                consentResource = consentCoreService.getConsent(initialConsentData.getConsentId(), false);
                initialConsentData.setConsentResource(consentResource);
                consentPersistData.setConsentData(initialConsentData);
            }

            // Call external service and get updated consent persist data.
            ConsentPersistData updatedConsentPersistData = getExternalConsentPersistData(consentPersistData);
            ConsentData consentData = updatedConsentPersistData.getConsentData();
            JSONObject accountsPayload = updatedConsentPersistData.getPayload();

            consentCoreService.bindUserAccountsToConsent(consentData.getConsentResource(), consentData.getUserId(),
                    consentData.getAuthResource().getAuthorizationID(),
                    getConsentedAccounts(accountsPayload, true), "Authorized", "Authorized");


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

    private ExternalServiceRequest createExternalServiceRequest(ConsentPersistData consentPersistData) {

        JSONObject payload = new JSONObject();
        String consentPersistDataJsonString = new Gson().toJson(consentPersistData);
        payload.append(CONSENT_PERSIST_DATA_OBJECT_KEY, consentPersistDataJsonString);
        Request eventRequest = new Request(payload, new HashMap<>());
        return new ExternalServiceRequest(UUID.randomUUID().toString(), eventRequest, "");
    }

    private ConsentPersistData getExternalConsentPersistData(ConsentPersistData consentPersistData) {
        ExternalServiceRequest externalServiceRequest = createExternalServiceRequest(consentPersistData);
        JSONObject response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.CONSENT_PERSISTENCE);

        // Navigate through the JSON structure
        JSONObject request = response.getJSONObject("request");
        JSONObject payload = request.getJSONObject("payload");
        JSONArray consentPersistDataArray = payload.getJSONArray("consent_persist_data");

        // Since it’s an array with one element, extract the first element
        if (consentPersistDataArray.length() > 0) {
            String consentPersistDataJson = consentPersistDataArray.getString(0);
            return ConsentPersistData.fromJson(consentPersistDataJson);
        }
        return null;
    }

}
