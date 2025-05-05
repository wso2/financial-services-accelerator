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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.FSAuthServletInterface;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils.Constants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import static org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils.Utils.i18n;

/**
 * ISDefaultAuthServletImpl
 */
public class ISDefaultAuthServletImpl implements FSAuthServletInterface {

    @Override
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getParameter(Constants.REQUESTED_CLAIMS)
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 1
    public Map<String, Object> updateRequestAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {

        Map<String, Object> returnMaps = new HashMap<>();

        // Claims
        if (request.getParameter(Constants.REQUESTED_CLAIMS) != null) {

            String[] requestedClaimList = request.getParameter(Constants.REQUESTED_CLAIMS)
                    .split(Constants.CLAIM_SEPARATOR);

            returnMaps.put(Constants.REQUESTED_CLAIMS, Utils.splitClaims(requestedClaimList));
        }

        if (request.getParameter(Constants.MANDATORY_CLAIMS) != null) {
            String[] mandatoryClaimList = request.getParameter(Constants.MANDATORY_CLAIMS)
                    .split(Constants.CLAIM_SEPARATOR);

            returnMaps.put(Constants.MANDATORY_CLAIMS, Utils.splitClaims(mandatoryClaimList));
        }


        // Scopes
        /*This parameter decides whether the consent page will only be used to get consent for sharing claims with the
        Service Provider. If this param is 'true' and user has already given consents for the OIDC scopes, we will be
        hiding the scopes being displayed and the approve always button.
        */
        boolean userClaimsConsentOnly = Boolean.parseBoolean(request.getParameter(Constants.USER_CLAIMS_CONSENT_ONLY));
        returnMaps.put(Constants.USER_CLAIMS_CONSENT_ONLY, userClaimsConsentOnly);

        List<String> oidScopes = new ArrayList<>();
        boolean displayScopes = (boolean) request.getSession().getAttribute(Constants.DISPLAY_SCOPES);

        if (userClaimsConsentOnly) {
            // If we are getting consent for user claims only, we don't need to display OIDC scopes in the consent page
        } else {
            if (displayScopes) {
                JSONArray openIdScopesArray = dataSet.getJSONArray(Constants.DISPLAY_SCOPES);
                if (openIdScopesArray != null) {
                    for (int scopeIndex = 0; scopeIndex < openIdScopesArray.length(); scopeIndex++) {
                        oidScopes.add(openIdScopesArray.getString(scopeIndex));
                    }
                    returnMaps.put(Constants.OIDC_SCOPES, oidScopes);
                }
            }
        }


        // Strings
        returnMaps.put(Constants.OPENID_USER_CLAIMS, i18n(resourceBundle, Constants.OPENID_USER_CLAIMS_KEY));
        returnMaps.put(Constants.REQUEST_ACCESS_PROFILE, i18n(resourceBundle, Constants.REQUEST_ACCESS_PROFILE_KEY));
        returnMaps.put(Constants.REQUESTED_ATTRIBUTES, i18n(resourceBundle, Constants.REQUESTED_ATTRIBUTES_KEY));
        returnMaps.put(Constants.SELECTING_ATTRIBUTE, i18n(resourceBundle, Constants.SELECTING_ATTRIBUTE_KEY));
        returnMaps.put(Constants.CLAIM_RECOMMENDATION,
                i18n(resourceBundle, Constants.CLAIM_RECOMMENDATION_KEY));
        returnMaps.put(Constants.CONTINUE_DEFAULT, i18n(resourceBundle, Constants.CONTINUE));
        returnMaps.put(Constants.DENY, i18n(resourceBundle, Constants.DENY));

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
