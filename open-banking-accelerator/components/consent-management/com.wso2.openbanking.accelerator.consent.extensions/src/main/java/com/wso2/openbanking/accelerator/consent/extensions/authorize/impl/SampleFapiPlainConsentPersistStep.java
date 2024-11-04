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

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Consent persist step sample implementation for FAPI plain flow.
 */
public class SampleFapiPlainConsentPersistStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(SampleFapiPlainConsentPersistStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        if (consentPersistData.getApproval()) {
            try {
                ConsentData consentData = consentPersistData.getConsentData();
                JSONObject payloadData = consentPersistData.getPayload();

                JSONArray accountIds = (JSONArray) payloadData.get(ConsentExtensionConstants.ACCOUNT_IDS);
                ArrayList<String> accountIdsString = new ArrayList<>();
                for (Object account : accountIds) {
                    if (!(account instanceof String)) {
                        log.error("Account IDs format error in persist request");
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                "Account IDs format error in persist request");
                    }
                    accountIdsString.add((String) account);
                }

                ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .bindUserAccountsToConsent(consentData.getConsentResource(), consentData.getUserId(),
                                consentData.getAuthResource().getAuthorizationID(), accountIdsString,
                                ConsentExtensionConstants.AUTHORISED_STATUS,
                                ConsentExtensionConstants.AUTHORISED_STATUS);
            } catch (ConsentManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Exception occurred while persisting consent");
            }
        }
    }

}
