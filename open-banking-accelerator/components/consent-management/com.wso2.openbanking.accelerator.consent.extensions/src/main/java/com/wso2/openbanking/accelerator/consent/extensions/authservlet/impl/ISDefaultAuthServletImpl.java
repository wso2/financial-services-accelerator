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

import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.util.Constants;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.util.Utils;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * The default implementation of servlet extension that handles non OB use cases.
 * Required in other vanilla auth flows.
 */
public class ISDefaultAuthServletImpl implements OBAuthServletInterface {

    @Override
    public Map<String, Object> updateRequestAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {

        Map<String, Object> returnMaps = new HashMap<>();

        // Claims
        if (request.getParameter(Constants.REQUESTED_CLAIMS) != null) {

            String[] requestedClaimList = request.getParameter(Constants.REQUESTED_CLAIMS)
                    .split(Constants.CLAIM_SEPARATOR);

            returnMaps.put("requestedClaims", Utils.splitClaims(requestedClaimList));
        }

        if (request.getParameter(Constants.MANDATORY_CLAIMS) != null) {
            String[] mandatoryClaimList = request.getParameter(Constants.MANDATORY_CLAIMS)
                    .split(Constants.CLAIM_SEPARATOR);

            returnMaps.put("mandatoryClaims", Utils.splitClaims(mandatoryClaimList));
        }


        // Scopes
        /*This parameter decides whether the consent page will only be used to get consent for sharing claims with the
        Service Provider. If this param is 'true' and user has already given consents for the OIDC scopes, we will be
        hiding the scopes being displayed and the approve always button.
        */
        boolean userClaimsConsentOnly = Boolean.parseBoolean(request.getParameter(Constants.USER_CLAIMS_CONSENT_ONLY));
        returnMaps.put("userClaimsConsentOnly", userClaimsConsentOnly);

        List<String> oidScopes = new ArrayList<>();
        boolean displayScopes = (boolean) request.getSession().getAttribute("displayScopes");

        if (userClaimsConsentOnly) {
            // If we are getting consent for user claims only, we don't need to display OIDC scopes in the consent page
        } else {
            if (displayScopes) {
                JSONArray openIdScopesArray = dataSet.getJSONArray("openid_scopes");
                if (openIdScopesArray != null) {
                    for (int scopeIndex = 0; scopeIndex < openIdScopesArray.length(); scopeIndex++) {
                        oidScopes.add(openIdScopesArray.getString(scopeIndex));
                    }
                    returnMaps.put(Constants.OIDC_SCOPES, oidScopes);
                }
            }
        }


        // Strings
        returnMaps.put("openidUserClaims", Utils.i18n(resourceBundle, "openid.user.claims"));
        returnMaps.put("requestAccessProfile", Utils.i18n(resourceBundle, "request.access.profile"));
        returnMaps.put("requestedAttributes", Utils.i18n(resourceBundle, "requested.attributes"));
        returnMaps.put("bySelectingFollowingAttributes",
                Utils.i18n(resourceBundle, "by.selecting.following.attributes"));
        returnMaps.put("mandatoryClaimsRecommendation",
                Utils.i18n(resourceBundle, "mandatory.claims.recommendation"));
        returnMaps.put("continueDefault", Utils.i18n(resourceBundle, "continue"));
        returnMaps.put("deny", Utils.i18n(resourceBundle, "deny"));

        return returnMaps;

    }

    @Override
    public Map<String, Object> updateSessionAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> updateConsentData(HttpServletRequest request) {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> updateConsentMetaData(HttpServletRequest request) {
        return new HashMap<>();
    }

    @Override
    public String getJSPPath() {
        return "/default_consent.jsp";
    }
}
