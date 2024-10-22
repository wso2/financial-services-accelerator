/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;


/**
 * Consent persist step default implementation.
 */
public class DefaultConsentPersistStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(DefaultConsentPersistStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        try {
            ConsentData consentData = consentPersistData.getConsentData();
            ConsentResource consentResource;

            if (consentData.getConsentId() == null && consentData.getConsentResource() == null) {
                log.error("Consent ID not available in consent data");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Consent ID not available in consent data");
            }

            if (consentData.getConsentResource() == null) {
                consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentData.getConsentId(), false);
            } else {
                consentResource = consentData.getConsentResource();
            }

            if (consentData.getAuthResource() == null) {
                log.error("Auth resource not available in consent data");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Auth resource not available in consent data");
            }

            consentPersist(consentPersistData, consentResource);


        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while persisting consent");
        }
    }

    /**
     * This method defined to handle consent persistence based on the consent type.
     *
     * @param consentPersistData    Consent Persist Data Object
     * @param consentResource       Consent Resource Object
     * @throws ConsentManagementException
     */
    public static void consentPersist(ConsentPersistData consentPersistData, ConsentResource consentResource)
            throws ConsentManagementException {

        ConsentData consentData = consentPersistData.getConsentData();

        JSONObject payload = consentPersistData.getPayload();

        if (payload.get(ConsentExtensionConstants.ACCOUNT_IDS) == null ||
                !(payload.get(ConsentExtensionConstants.ACCOUNT_IDS) instanceof JSONArray)) {
            log.error(ErrorConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ErrorConstants.ACCOUNT_ID_NOT_FOUND_ERROR);
        }

        JSONArray accountIds = (JSONArray) payload.get(ConsentExtensionConstants.ACCOUNT_IDS);
        ArrayList<String> accountIdsString = new ArrayList<>();
        for (Object account : accountIds) {
            if (!(account instanceof String)) {
                log.error(ErrorConstants.ACCOUNT_ID_FORMAT_ERROR);
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        ErrorConstants.ACCOUNT_ID_FORMAT_ERROR);
            }
            accountIdsString.add((String) account);
        }
        String consentStatus;

        if (consentPersistData.getApproval()) {
            consentStatus = ConsentExtensionConstants.AUTHORISED_STATUS;
        } else {
            consentStatus = ConsentExtensionConstants.REJECTED_STATUS;
        }

        ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                .bindUserAccountsToConsent(consentResource, consentData.getUserId(),
                        consentData.getAuthResource().getAuthorizationID(), accountIdsString, consentStatus,
                        consentStatus);
    }
}
