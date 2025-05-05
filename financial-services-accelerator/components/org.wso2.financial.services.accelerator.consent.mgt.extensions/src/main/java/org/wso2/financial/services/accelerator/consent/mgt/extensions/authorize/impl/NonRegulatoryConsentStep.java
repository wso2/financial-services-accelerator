/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * </p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * </p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Retrieval step for non regulatory applications.
 */
public class NonRegulatoryConsentStep implements ConsentRetrievalStep {

    private static final String OPENID_SCOPES = "openid_scopes";
    private static final String OPENID = "openid";
    private static final String CONSENT_MGT = "consentmgt";
    private static final String DEFAULT = "default";
    private static final String CONSENT_READ_ALL_SCOPE = "consents:read_all";
    private static final String CONSENT_READ_SELF_SCOPE = "consents:read_self";
    private static final Map<String, String> CONSENT_SCOPES = new HashMap<>();


    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) {
        if (consentData.isRegulatory()) {
            return;
        }

        if (consentData.getScopeString().toLowerCase().contains(CONSENT_MGT)) {
            consentData.setType(CONSENT_MGT);
        } else if (consentData.getScopeString().contains(OPENID)) {
            consentData.setType(DEFAULT);
        }
        String scopeString = consentData.getScopeString();
        addScopesArray(scopeString, jsonObject);
    }

    /**
     * Add scopes to json object.
     *
     * @param scopeString scopes string
     * @param jsonObject  json object
     */
    private void addScopesArray(String scopeString, JSONObject jsonObject) {

        if (StringUtils.isNotBlank(scopeString)) {
            // Remove "openid" from the scope list to display.
            List<String> openIdScopes = Stream.of(scopeString.split(" "))
                    .filter(x -> !StringUtils.equalsIgnoreCase(x, OPENID))
                    // if scope found in CONSENT_SCOPES map return meaningful scope value, else return scope
                    .map(scope -> getConsentScopes().getOrDefault(scope, scope))
                    .collect(Collectors.toList());
            JSONArray scopeArray = new JSONArray();
            scopeArray.putAll(openIdScopes);
            jsonObject.put(OPENID_SCOPES, scopeArray);
        }
    }

    protected synchronized Map<String, String> getConsentScopes() {
        if (CONSENT_SCOPES.isEmpty()) {
            // Add meaningful string value to scopes
            CONSENT_SCOPES.put(CONSENT_READ_ALL_SCOPE, "Manage all consents");
            CONSENT_SCOPES.put(CONSENT_READ_SELF_SCOPE, "Manage your consents");
        }
        return CONSENT_SCOPES;
    }
}
