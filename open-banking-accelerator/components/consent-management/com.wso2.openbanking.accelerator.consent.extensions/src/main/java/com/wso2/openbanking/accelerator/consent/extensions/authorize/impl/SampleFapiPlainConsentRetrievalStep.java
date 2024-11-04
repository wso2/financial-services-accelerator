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
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;

import java.util.UUID;

/**
 * Consent retrieval step sample implementation for FAPI plain flow.
 */
public class SampleFapiPlainConsentRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(SampleFapiPlainConsentRetrievalStep.class);

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }

        // Removes request_uri cache entry to avoid reusing the same request object stored in cache
        removeRequestUriCacheEntry(consentData.getSpQueryParams());

        JSONArray permissions = new JSONArray();

        if (consentData.getScopeString().contains(ConsentExtensionConstants.ACCOUNTS)) {
            permissions.addAll(ConsentExtensionConstants.VALID_PERMISSIONS);
        } else {
            permissions.add(ConsentExtensionConstants.DEFAULT_PERMISSION);
        }

        String consentID = UUID.randomUUID().toString();
        ConsentResource consentResource = new ConsentResource(consentData.getClientId(), permissions.toJSONString(),
                ConsentExtensionConstants.ACCOUNTS, ConsentExtensionConstants.AUTHORISED_STATUS);
        consentResource.setConsentID(consentID);

        DetailedConsentResource createdConsent;
        try {
            createdConsent = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                    .createAuthorizableConsent(consentResource, consentData.getUserId(),
                            "created", "authorization", true);
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        consentData.setConsentId(createdConsent.getConsentID());
        consentData.setType(createdConsent.getConsentType());
        consentData.setConsentResource(consentResource);
        consentData.setAuthResource(createdConsent.getAuthorizationResources().get(0));

        JSONArray consentDataJSON = new JSONArray();

        JSONObject jsonElementPermissions = new JSONObject();
        jsonElementPermissions.appendField(ConsentExtensionConstants.TITLE, ConsentExtensionConstants.PERMISSIONS);
        jsonElementPermissions.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA), permissions);

        consentDataJSON.add(jsonElementPermissions);

        jsonObject.appendField(ConsentExtensionConstants.CONSENT_DATA, consentDataJSON);

        jsonObject.appendField(ConsentExtensionConstants.ACCOUNTS, ConsentExtensionUtils.getDummyAccounts());

    }

    private void removeRequestUriCacheEntry(String spQueryParams) {
        if (spQueryParams != null && spQueryParams.contains(ConsentExtensionConstants.REQUEST_URI_PARAMETER)) {
            String[] requestUri = spQueryParams
                    .substring(ConsentExtensionConstants.REQUEST_URI_PARAMETER.length())
                    .replaceAll("\\%3A", ":")
                    .split(":");
            String sessionKey = requestUri[requestUri.length - 1];
            SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionKey);
            log.debug("Removing request_uri entry from cache");
            SessionDataCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

}
