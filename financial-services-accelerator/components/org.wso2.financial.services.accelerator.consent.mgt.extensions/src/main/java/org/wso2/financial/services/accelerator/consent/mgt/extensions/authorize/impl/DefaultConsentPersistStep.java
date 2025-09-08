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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentPersistStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.AccountDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentDataDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsumerAccountDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.PermissionDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.PopulateConsentAuthorizeScreenDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Consent persist step default implementation.
 */
public class DefaultConsentPersistStep implements ConsentPersistStep {

    private final List<String> preInitiatedConsentScopes;
    private final List<String> scopeBasedConsentScopes;
    private static final Log log = LogFactory.getLog(DefaultConsentPersistStep.class);

    public DefaultConsentPersistStep() {

        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        preInitiatedConsentScopes = configParser.getPreInitiatedConsentScopes();
        scopeBasedConsentScopes = configParser.getScopeBasedConsentScopes();
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

            boolean ifPreInitiatedConsentFlow = FinancialServicesUtils.isPreInitiatedConsentFlow(
                    consentData.getScopeString(), preInitiatedConsentScopes, scopeBasedConsentScopes);
            log.debug("Pre-initiated consent flow check result: " + ifPreInitiatedConsentFlow);

            if (ifPreInitiatedConsentFlow && consentData.getConsentId() == null) {
                log.error("Consent ID not available in consent data");
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Consent ID not available in consent data", consentData.getState());
            }

            if (consentData.getConsentResource() == null) {
                consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentData.getConsentId(), false);
            } else {
                consentResource = consentData.getConsentResource();
            }

            if (ifPreInitiatedConsentFlow && consentData.getAuthResource() == null) {
                log.error("Auth resource not available in consent data");
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Auth resource not available in consent data", consentData.getState());
            }

            consentPersist(consentPersistData, consentResource);

        } catch (ConsentManagementException e) {
            log.error(e.getMessage().replaceAll("\n\r", ""), e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while persisting consent");
        }
    }

    public void consentPersist(ConsentPersistData consentPersistData, ConsentResource consentResource)
            throws ConsentManagementException {

        ConsentData consentData = consentPersistData.getConsentData();
        boolean isApproved = consentPersistData.getApproval();
        JSONObject payload = consentPersistData.getPayload();
        String userId = consentPersistData.getConsentData().getUserId();
        String authorizationId;
        String consentStatus;
        String authStatus;

        consentStatus = isApproved ? ConsentExtensionConstants.AUTHORIZED_STATUS :
                ConsentExtensionConstants.REJECTED_STATUS;
        authStatus = isApproved ? ConsentExtensionConstants.AUTHORIZED_STATUS :
                ConsentExtensionConstants.REJECTED_STATUS;

        ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();

        boolean ifPreInitiatedConsentFlow = FinancialServicesUtils.isPreInitiatedConsentFlow(
                consentData.getScopeString(), preInitiatedConsentScopes, scopeBasedConsentScopes);
        log.debug("Pre-initiated consent flow check result: " + ifPreInitiatedConsentFlow);

        // Create the consent if it is not pre initiated.
        if (ifPreInitiatedConsentFlow) {
            authorizationId = consentData.getAuthResource().getAuthorizationID();
            if (log.isDebugEnabled()) {
                log.debug(String.format("Using existing authorization ID: %s", authorizationId.replaceAll("\n\r", "")));
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Creating new authorizable consent for client: %s",
                        consentData.getClientId().replaceAll("\n\r", "")));
            }
            DetailedConsentResource createdConsent = consentCoreService.createAuthorizableConsent(
                    consentResource, userId, authStatus, ConsentExtensionConstants.DEFAULT_AUTH_TYPE, true);
            String consentId = createdConsent.getConsentID();
            authorizationId = consentCoreService.searchAuthorizations(consentId).get(0).getAuthorizationID();
            // Getting commonAuthId to add as a consent attribute. This is to find the consent in later stages.
            if (consentPersistData.getBrowserCookies() != null) {
                String commonAuthId = consentPersistData.getBrowserCookies().get(
                        ConsentExtensionConstants.COMMON_AUTH_ID);
                Map<String, String> consentAttributes = new HashMap<>();
                consentAttributes.put(ConsentExtensionConstants.COMMON_AUTH_ID, commonAuthId);
                consentCoreService.storeConsentAttributes(consentId, consentAttributes);
            }
        }
        consentCoreService.bindUserAccountsToConsent(consentResource, consentData.getUserId(), authorizationId,
                getConsentedAccounts(payload, consentData.getMetaDataMap(), isApproved), authStatus, consentStatus);
    }

    /**
     * Retrieve account ID consented by the user from the object.
     *
     * @param persistPayload payload to persist
     * @return Account data map
     */
    private static Map<String, ArrayList<String>> getConsentedAccounts(JSONObject persistPayload,
                                                                       Map<String, Object> metaDataMap,
                                                                       boolean isApproved) {

        Map<String, ArrayList<String>> accountIDsMapWithPermissions = new HashMap<>();
        ArrayList<String> permissionsDefault = new ArrayList<>();
        permissionsDefault.add(ConsentExtensionConstants.PRIMARY);
        boolean hasAuthorizedAccounts = false;

        // Extract accounts and permissions in metadata
        PopulateConsentAuthorizeScreenDTO populateResponseDTO = (PopulateConsentAuthorizeScreenDTO) metaDataMap
                .get(ConsentAuthorizeConstants.EXTERNAL_API_PRE_CONSENT_AUTHORIZE_RESPONSE);

        // Extract and separate permissions, consumer accounts and consent initiated accounts
        ConsentDataDTO consentData = populateResponseDTO.getConsentData();

        List<PermissionDTO> permissions = consentData.getPermissions();
        List<AccountDTO> initiatedAccountsForConsent = consentData.getInitiatedAccountsForConsent();
        List<ConsumerAccountDTO> consumerAccounts = null;
        if (populateResponseDTO.getConsumerData() != null) {
            consumerAccounts = populateResponseDTO.getConsumerData().getAccounts();
        }

        // Build consumer account name to consumer account object map
        Map<String, ConsumerAccountDTO> accountNameToObjectMap = new HashMap<>();
        if (consumerAccounts != null) {
            for (ConsumerAccountDTO consumerAccount: consumerAccounts) {
                accountNameToObjectMap.put(consumerAccount.getDisplayName(), consumerAccount);
            }
        }

        // Set of all accounts to map with default permission
        Set<AccountDTO> allAccountsSet = new HashSet<>();

        // Append all consent initiated accounts
        if (initiatedAccountsForConsent != null) {
            allAccountsSet.addAll(initiatedAccountsForConsent);
        }

        // Append all permission initiated accounts
        if (permissions != null) {
            Set<AccountDTO> allAccountsWithPermissionsSet = new HashSet<>();
            for (PermissionDTO permission: permissions) {
                List<AccountDTO> initiatedAccounts = permission.getInitiatedAccounts();
                if (initiatedAccounts != null) {
                    allAccountsWithPermissionsSet.addAll(initiatedAccounts);
                }
            }
            allAccountsSet.addAll(allAccountsWithPermissionsSet);
        }

        // Append all selected consumer accounts
        boolean allowMultipleAccounts = Boolean.TRUE.equals(consentData.getAllowMultipleAccounts());
        JSONObject requestParameters =
                persistPayload.optJSONObject(ConsentAuthorizeConstants.REQUEST_PARAMETERS);
        if (requestParameters != null) {
            for (String key: requestParameters.keySet()) {

                JSONArray parameterValues = requestParameters.optJSONArray(key);
                if (parameterValues == null) {
                    continue;
                }

                boolean foundOneAccount = false;
                for (int i = 0; i < parameterValues.length(); i++) {

                    ConsumerAccountDTO accountObject = accountNameToObjectMap.getOrDefault(parameterValues.getString(i),
                            null);
                    if (accountObject == null) {
                        continue;
                    }

                    // allowMultipleAccounts validation
                    if (foundOneAccount && !allowMultipleAccounts) {
                        log.error("Found multiple account selections when only one is allowed");
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                "Found multiple account selections when only one is allowed");
                    }

                    foundOneAccount = true;
                    allAccountsSet.add(accountObject);
                }
            }
        }

        // Map accounts in the set of all accounts to default permission
        for (AccountDTO account: allAccountsSet) {
            hasAuthorizedAccounts = true;
            accountIDsMapWithPermissions.put(account.getAccountId(), permissionsDefault);
        }

        if (!hasAuthorizedAccounts && isApproved) {
            log.error(ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ConsentAuthorizeConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
        } else {
            // in case a consent is denied with no account mappings
            accountIDsMapWithPermissions.put("n/a", permissionsDefault);
        }

        return accountIDsMapWithPermissions;
    }
}
