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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.List;

/**
 * Consent retrieval step default implementation.
 */
public class DefaultConsentRetrievalStep implements ConsentRetrievalStep {

    private final List<String> preInitiatedConsentScopes;
    private final List<String> scopeBasedConsentScopes;
    private static final Log log = LogFactory.getLog(DefaultConsentRetrievalStep.class);

    public DefaultConsentRetrievalStep() {

        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        preInitiatedConsentScopes = configParser.getPreInitiatedConsentScopes();
        scopeBasedConsentScopes = configParser.getScopeBasedConsentScopes();
    }

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }
        ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        String requestObject = ConsentAuthorizeUtil.extractRequestObject(consentData.getSpQueryParams());
        JSONObject requestParameters = ConsentAuthorizeUtil.getRequestObjectJson(requestObject);
        String scope = ConsentAuthorizeUtil.extractField(requestObject, FinancialServicesConstants.SCOPE);
        JSONObject consentDataJSON;
        ConsentResource consentResource;
        boolean isPreInitiatedConsentFlow = FinancialServicesUtils.isPreInitiatedConsentFlow(scope,
                preInitiatedConsentScopes, scopeBasedConsentScopes);
        if (log.isDebugEnabled()) {
            log.debug("Pre-initiated consent flow check result: " + isPreInitiatedConsentFlow);
        }
        try {
            if (isPreInitiatedConsentFlow) {

                log.debug("Extracting consent ID from pre-initiated consent request object.");
                String consentId = ConsentAuthorizeUtil.extractConsentIdFromRequestObject(requestObject);
                if (consentId == null) {
                    log.error("intent_id not found in request object");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "intent_id not found in request object");
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieved consentId: %s for pre-initiated consent flow",
                            consentId.replaceAll("\n\r", "")));
                }

                consentData.setConsentId(consentId);
                consentResource = consentCoreService.getConsent(consentId, false);

                if (!ConsentExtensionConstants.AWAIT_AUTHORISE_STATUS.equals(consentResource.getCurrentStatus())) {
                    log.error("Consent not in authorizable state");
                    /* Currently throwing error as 400 response. Developer also have the option of appending a field
                       IS_ERROR to the jsonObject and showing it to the user in the webapp. If so, the IS_ERROR have
                       to be checked in any later steps. */
                    throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                            "Consent not in authorizable state", consentData.getState());
                }

                AuthorizationResource authorizationResource = consentCoreService.searchAuthorizations(consentId).get(0);
                if (!authorizationResource.getAuthorizationStatus().equals(ConsentExtensionConstants.CREATED_STATUS)) {
                    log.error("Authorisation not in authorizable state");
                    /* Currently throwing error as 400 response. Developer also have the option of appending a
                       field IS_ERROR to the jsonObject and showing it to the user in the webapp. If so,
                       the IS_ERROR have to be checked in any later steps. */
                    throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                            "Authorisation not in authorisable state", consentData.getState());
                }

                consentData.setAuthResource(authorizationResource);
                consentDataJSON = ConsentAuthorizeUtil.getConsentDataForPreInitiatedConsent(consentResource);
            } else {
                // Create a default type consent resource using data from request object.
                String receipt = ConsentAuthorizeUtil.getReceiptFromRequestObject(requestObject);
                long defaultValidityPeriod = System.currentTimeMillis() / 1000 + 3600;

                consentResource = new ConsentResource(consentData.getClientId(), receipt,
                        ConsentExtensionConstants.DEFAULT, ConsentExtensionConstants.CREATED_STATUS);
                consentResource.setValidityPeriod(defaultValidityPeriod);
                consentDataJSON = ConsentAuthorizeUtil.getConsentDataForScope(scope);
            }
            consentData.setType(consentResource.getConsentType());
            consentData.setConsentResource(consentResource);
            jsonObject.put(ConsentExtensionConstants.CONSENT_DATA, consentDataJSON);

            /* Appending Dummy data for Accounts consent. In real-world scenario should be separate step
             calling accounts service */
            // Append only when consent type is accounts or no initiated account for payment consents

            if (!isPreInitiatedConsentFlow ||
                    ConsentExtensionConstants.ACCOUNTS.equals(consentResource.getConsentType()) ||
                    (ConsentExtensionConstants.PAYMENTS.equals(consentResource.getConsentType()) &&
                            !consentDataJSON.has(ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT))) {
                JSONArray accountsJSON = ConsentAuthorizeUtil.appendDummyAccountID();
                JSONObject consumerDataJSON = new JSONObject();
                consumerDataJSON.put(ConsentExtensionConstants.ACCOUNTS, accountsJSON);
                jsonObject.put(ConsentExtensionConstants.CONSUMER_DATA, consumerDataJSON);
            }

            // Set request parameters as metadata to be used in persistence extension
            consentData.addData(ConsentExtensionConstants.REQUEST_PARAMETERS, requestParameters);

            // Storing consent metadata for attribute retrieval at persistence
            consentData.setMetaDataMap(ConsentAuthorizeUtil.getConsentMapFromJSONObject(jsonObject));

        } catch (JsonProcessingException e) {
            log.error(e.getMessage().replaceAll("\n\r", ""), e);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    e.getMessage().replaceAll("\n\r", ""), consentData.getState());
        } catch (ConsentManagementException e) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    "Exception occurred while getting consent data", consentData.getState());
        }
    }

}
