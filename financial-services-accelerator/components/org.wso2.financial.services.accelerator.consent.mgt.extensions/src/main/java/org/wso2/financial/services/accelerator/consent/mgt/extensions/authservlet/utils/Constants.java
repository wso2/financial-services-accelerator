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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils;

/**
 * Constants required for auth servlet implementations.
 */
public class Constants {

    private Constants() {
        // do not required to create instances
    }

    public static final String REQUESTED_CLAIMS = "requestedClaims";
    public static final String MANDATORY_CLAIMS = "mandatoryClaims";
    public static final String CLAIM_SEPARATOR = ",";
    public static final String USER_CLAIMS_CONSENT_ONLY = "userClaimsConsentOnly";
    public static final String OIDC_SCOPES = "OIDCScopes";
    public static final String DISPLAY_SCOPES = "displayScopes";
    public static final String OPENID_SCOPES = "openid_scopes";
    public static final String OPENID_USER_CLAIMS = "openidUserClaims";
    public static final String OPENID_USER_CLAIMS_KEY = "openid.user.claims";
    public static final String REQUEST_ACCESS_PROFILE = "requestAccessProfile";
    public static final String REQUEST_ACCESS_PROFILE_KEY = "request.access.profile";
    public static final String REQUESTED_ATTRIBUTES = "requestedAttributes";
    public static final String REQUESTED_ATTRIBUTES_KEY = "requested.attributes";
    public static final String SELECTING_ATTRIBUTE = "bySelectingFollowingAttributes";
    public static final String SELECTING_ATTRIBUTE_KEY = "by.selecting.following.attributes";
    public static final String CLAIM_RECOMMENDATION = "mandatoryClaimsRecommendation";
    public static final String CLAIM_RECOMMENDATION_KEY = "mandatory.claims.recommendation";
    public static final String CONTINUE_DEFAULT = "continueDefault";
    public static final String CONTINUE = "continue";
    public static final String DENY = "deny";

    public static final String CONSUMER_ACCOUNTS = "consumerAccounts";
}
