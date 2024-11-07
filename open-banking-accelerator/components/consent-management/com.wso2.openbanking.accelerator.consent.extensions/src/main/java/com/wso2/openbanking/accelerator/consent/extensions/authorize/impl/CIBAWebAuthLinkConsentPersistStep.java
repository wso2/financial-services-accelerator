/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.consent.extensions.authorize.impl;

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Consent persistence step for CIBA web auth links flow
 */
public class CIBAWebAuthLinkConsentPersistStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(CIBAWebAuthLinkConsentPersistStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        try {
            if (!ConsentExtensionUtils.isCibaWebAuthLinkFlow(consentPersistData.getConsentData())) {
                // Enabling execution for CIBA web auth link flows.
                return;
            }
            bindUserToConsent(consentPersistData);
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while persisting consent");
        }
    }

    /**
     * Method to update authenticated state / accountIds of the user to the authorisation resources.
     *
     * @param consentPersistData ConsentPersistData
     * @throws ConsentManagementException exception
     */
    protected void bindUserToConsent(ConsentPersistData consentPersistData) throws ConsentManagementException {

        ConsentData consentData = consentPersistData.getConsentData();
        if (consentData.getConsentId() == null && consentData.getConsentResource() == null) {
            log.error("Consent ID not available in consent data");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Consent ID not available in consent data");
        }
        DetailedConsentResource detailedConsent = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                .getDetailedConsent(consentData.getConsentId());
        ConsentResource consentResource = new ConsentResource(detailedConsent.getConsentID(),
                detailedConsent.getClientID(), detailedConsent.getReceipt(), detailedConsent.getConsentType(),
                detailedConsent.getConsentFrequency(), detailedConsent.getValidityPeriod(),
                detailedConsent.isRecurringIndicator(), detailedConsent.getCurrentStatus(),
                detailedConsent.getCreatedTime(), detailedConsent.getUpdatedTime());

        if (!(consentResource.getCurrentStatus().equals(OpenBankingConstants.AWAITING_AUTHORISATION_STATUS) ||
                consentResource.getCurrentStatus().equals(
                        OpenBankingConstants.AWAITING_FURTHER_AUTHORISATION_STATUS))) {
            log.error("Consent not in authorizable state");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not in authorizable state");
        }

        ArrayList<AuthorizationResource> authorizationResources = detailedConsent.getAuthorizationResources();
        List<AuthorizationResource> userAuthorizationResources = authorizationResources.stream()
                .filter(e -> consentData.getUserId().equalsIgnoreCase(e.getUserID()))
                .collect(Collectors.toList());

        if (userAuthorizationResources.size() != 1) {
            log.error("User's auth resource is not available or multiple entries found for the given consent");
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    "User's auth resource is not available or multiple entries found for the given consent");
        }

        AuthorizationResource userAuthorizationResource = userAuthorizationResources.get(0);
        if (!userAuthorizationResource.getAuthorizationStatus().equalsIgnoreCase(
                OpenBankingConstants.CREATED_AUTHORISATION_RESOURCE_STATE)) {
            log.error("User auth resource is not in authorizable state");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "User auth resource is not in authorizable state");
        }

        JSONObject payload = consentPersistData.getPayload();
        if (payload.get("accountIds") == null || !(payload.get("accountIds") instanceof JSONArray)) {
            log.error("Account IDs not available in persist request");
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    "Account IDs not available in persist request");
        }
        JSONArray accountIds = (JSONArray) payload.get("accountIds");
        ArrayList<String> accountIdsString = new ArrayList<>();
        for (Object account : accountIds) {
            if (!(account instanceof String)) {
                log.error("Account IDs format error in persist request");
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        "Account IDs format error in persist request");
            }
            accountIdsString.add((String) account);
        }

        String authStatus;
        if (consentPersistData.getApproval()) {
            authStatus = ConsentExtensionConstants.AUTHORISED_STATUS;
            userAuthorizationResource.setAuthorizationStatus(authStatus);
        } else {
            authStatus = ConsentExtensionConstants.REJECTED_STATUS;
            userAuthorizationResource.setAuthorizationStatus(authStatus);
        }

        // Check existing authorisation resources entries.
        List<AuthorizationResource> rejectedAuthResources = authorizationResources.stream()
                .filter(e -> e.getAuthorizationStatus().equals(ConsentExtensionConstants.REJECTED_STATUS))
                .collect(Collectors.toList());

        List<AuthorizationResource> createdAuthResources = authorizationResources.stream()
                .filter(e -> e.getAuthorizationStatus().equals(
                        OpenBankingConstants.CREATED_AUTHORISATION_RESOURCE_STATE))
                .collect(Collectors.toList());

        List<AuthorizationResource> authorisedAuthResources = authorizationResources.stream()
                .filter(e -> e.getAuthorizationStatus().equals(ConsentExtensionConstants.AUTHORISED_STATUS))
                .collect(Collectors.toList());

        if (!rejectedAuthResources.isEmpty()) {
            ConsentExtensionsDataHolder.getInstance().getConsentCoreService().bindUserAccountsToConsent(
                    consentResource, consentData.getUserId(), userAuthorizationResource.getAuthorizationID(),
                    accountIdsString, authStatus, OpenBankingConstants.REJECTED_STATUS);
        } else if (!createdAuthResources.isEmpty() && authorisedAuthResources.isEmpty()) {
            ConsentExtensionsDataHolder.getInstance().getConsentCoreService().bindUserAccountsToConsent(
                    consentResource, consentData.getUserId(), userAuthorizationResource.getAuthorizationID(),
                    accountIdsString, authStatus, OpenBankingConstants.AWAITING_AUTHORISATION_STATUS);
        } else if (!createdAuthResources.isEmpty()) {
            ConsentExtensionsDataHolder.getInstance().getConsentCoreService().bindUserAccountsToConsent(
                    consentResource, consentData.getUserId(), userAuthorizationResource.getAuthorizationID(),
                    accountIdsString, authStatus, OpenBankingConstants.AWAITING_FURTHER_AUTHORISATION_STATUS);
        } else if (!authorisedAuthResources.isEmpty()) {
            ConsentExtensionsDataHolder.getInstance().getConsentCoreService().bindUserAccountsToConsent(
                    consentResource, consentData.getUserId(), userAuthorizationResource.getAuthorizationID(),
                    accountIdsString, authStatus, ConsentExtensionConstants.AUTHORISED_STATUS);
        }

    }

}
