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

package com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.util.Constants;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import static com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.util.Utils.i18n;


/**
 * ConsentMgrAuthServletImpl
 * <p>
 * The consent management implementation of servlet extension that handles self-care portal use cases.
 */
public class ConsentMgrAuthServletImpl implements OBAuthServletInterface {

    @Override
    public Map<String, Object> updateRequestAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {

        Map<String, Object> updatedRequestData = new HashMap<>();

        boolean userClaimsConsentOnly = Boolean.parseBoolean(request.getParameter(Constants.USER_CLAIMS_CONSENT_ONLY));
        updatedRequestData.put("userClaimsConsentOnly", userClaimsConsentOnly);

        boolean displayScopes = (boolean) request.getSession().getAttribute("displayScopes");
        if (displayScopes) {
            JSONArray openIdScopesArray = dataSet.getJSONArray("openid_scopes");
            if (openIdScopesArray != null) {
                List<String> oidScopes = new ArrayList<>();
                for (int scopeIndex = 0; scopeIndex < openIdScopesArray.length(); scopeIndex++) {
                    oidScopes.add(openIdScopesArray.getString(scopeIndex));
                }
                updatedRequestData.put(Constants.OIDC_SCOPES, oidScopes);
            }
        }

        // Strings
        updatedRequestData.put("openidUserClaims", i18n(resourceBundle, "openid.user.claims"));
        updatedRequestData.put("requestAccessProfile", i18n(resourceBundle, "request.access.profile"));
        updatedRequestData.put("requestedAttributes", i18n(resourceBundle, "requested.attributes"));
        updatedRequestData.put("bySelectingFollowingAttributes",
                i18n(resourceBundle, "by.selecting.following.attributes"));
        updatedRequestData.put("mandatoryClaimsRecommendation",
                i18n(resourceBundle, "mandatory.claims.recommendation"));
        updatedRequestData.put("continueDefault", i18n(resourceBundle, "continue"));
        updatedRequestData.put("deny", i18n(resourceBundle, "deny"));

        return updatedRequestData;
    }

    @Generated(message = "ignoring since method doesn't contain a logic")
    @Override
    public Map<String, Object> updateSessionAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {
        return new HashMap<>();
    }

    @Generated(message = "ignoring since method doesn't contain a logic")
    @Override
    public Map<String, Object> updateConsentData(HttpServletRequest request) {
        return new HashMap<>();
    }

    @Generated(message = "ignoring since method doesn't contain a logic")
    @Override
    public Map<String, String> updateConsentMetaData(HttpServletRequest request) {
        return new HashMap<>();
    }

    @Generated(message = "ignoring since method doesn't contain a logic")
    @Override
    public String getJSPPath() {
        return "/default_consent.jsp";
    }
}
