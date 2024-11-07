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

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.utils.ConsentRetrievalUtil;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Consent retrieval step default implementation.
 */
public class DefaultConsentRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(DefaultConsentRetrievalStep.class);

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }

        ConsentCoreServiceImpl consentCoreService = ConsentServiceUtil.getConsentService();

        try {
            // If this is a CIBA flow, consent ID is already set at the CIBAConsentRetrievalStep
            String consentId = consentData.getConsentId();
            if (consentId == null) {
                // If query params are null, this is a CIBA flow. Therefore consent ID should be set at CIBA consent
                // retrieval step
                if (consentData.getSpQueryParams() == null) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "CIBA consent retrieval step has not been " +
                            "executed successfully before default consent persist step");
                }
                String requestObject = ConsentRetrievalUtil.extractRequestObject(consentData.getSpQueryParams());
                consentId = ConsentRetrievalUtil.extractConsentId(requestObject);
                consentData.setConsentId(consentId);
            }
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!(consentResource.getCurrentStatus().equals(OpenBankingConstants.AWAITING_AUTHORISATION_STATUS) ||
                    consentResource.getCurrentStatus().equals(
                            OpenBankingConstants.AWAITING_FURTHER_AUTHORISATION_STATUS))) {
                log.error("Consent not in authorizable state");
                //Currently throwing error as 400 response. Developer also have the option of appending a field IS_ERROR
                // to the jsonObject and showing it to the user in the webapp. If so, the IS_ERROR have to be checked in
                // any later steps.
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not in authorizable state");
            }

            AuthorizationResource authorizationResource;

            if (ConsentExtensionUtils.isCibaWebAuthLinkFlow(consentData)) {
                ArrayList<AuthorizationResource> authorizationResources = ConsentExtensionsDataHolder.getInstance()
                        .getConsentCoreService().searchAuthorizations(consentId, consentData.getUserId());
                authorizationResource = authorizationResources.size() == 1 ? authorizationResources.get(0) : null;
            } else {
                authorizationResource = ConsentExtensionsDataHolder.getInstance()
                        .getConsentCoreService().searchAuthorizations(consentId).get(0);
            }

            if (authorizationResource == null || !authorizationResource.getAuthorizationStatus().equals("created")) {
                log.error("Authorization not in authorizable state");
                //Currently throwing error as 400 response. Developer also have the option of appending a field IS_ERROR
                // to the jsonObject and showing it to the user in the webapp. If so, the IS_ERROR have to be checked in
                // any later steps.
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Authorization not in authorizable state");
            }

            consentData.setType(consentResource.getConsentType());
            consentData.setAuthResource(authorizationResource);
            consentData.setConsentResource(consentResource);

            //Appending Consent Data
            JSONArray consentDataJSON = getConsentDataSet(consentResource);
            jsonObject.appendField(ConsentExtensionConstants.CONSENT_DATA, consentDataJSON);

            //Appending Dummy data for Accounts consent. Ideally should be separate step calling accounts service
            JSONArray accountsJSON = ConsentRetrievalUtil.appendDummyAccountID();
            jsonObject.appendField(ConsentExtensionConstants.ACCOUNTS, accountsJSON);

        } catch (ConsentException e) {
            JSONObject errorObj = (JSONObject) e.getPayload();
            JSONArray errorList = (JSONArray) errorObj.get("Errors");
            jsonObject.put(ConsentExtensionConstants.IS_ERROR,
                    ((JSONObject) errorList.get(0)).getAsString("Message"));
            return;
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while getting consent data");
        }
    }

    /**
     * Method to retrieve consent related data from the initiation payload.
     * @param consentResource  Consent Resource
     * @return  consent
     */
    public JSONArray getConsentDataSet(ConsentResource consentResource) {

        return ConsentRetrievalUtil.getConsentData(consentResource);
    }

}
