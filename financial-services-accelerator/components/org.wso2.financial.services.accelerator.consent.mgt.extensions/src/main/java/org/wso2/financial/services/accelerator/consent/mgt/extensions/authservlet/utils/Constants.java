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
    public static final String ESCAPED_JSON = "escapedJson";

    public static final String CONSENT_AUTHORIZE_JSP_PATH = "Consent.AuthorizeJSP.Path";

    // consent page parameters
    public static final String RESOURCE_BUNDLE_DATA = "resourceBundleData";
    public static final String APP_REQUESTS_DETAILS_KEY = "app.requests.details";
    public static final String DATA_REQUESTED_KEY = "data.requested";
    public static final String REQUESTED_PERMISSIONS_KEY = "requested.permissions";
    public static final String ON_FOLLOWING_ACCOUNTS_KEY = "on.following.accounts";
    public static final String SELECT_ACCOUNTS_KEY = "select.accounts";
    public static final String SELECT_DEFAULT_KEY = "select.accounts.default";
    public static final String NO_CONSUMER_ACCOUNTS_KEY = "no.consumer.accounts";
    public static final String RE_AUTHENTICATION_DISCLAIMER_KEY = "re.authentication.disclaimer";
    public static final String IF_STOP_DATA_SHARING_KEY = "if.stop.data.sharing";
    public static final String DO_YOU_CONFIRM_KEY = "do.you.confirm";
    public static final String OK_BUTTON_KEY = "button.ok";
    public static final String DENY_BUTTON_KEY = "button.deny";
    public static final String GO_BACK_BUTTON_KEY = "button.goback";
    public static final String APP_REQUESTS_DETAILS = "appRequestsDetails";
    public static final String DATA_REQUESTED = "dataRequested";
    public static final String REQUESTED_PERMISSIONS = "requestedPermissions";
    public static final String ON_FOLLOWING_ACCOUNTS = "onFollowingAccounts";
    public static final String SELECT_ACCOUNTS = "selectAccounts";
    public static final String SELECT_DEFAULT = "defaultSelect";
    public static final String NO_CONSUMER_ACCOUNTS = "noConsumerAccounts";
    public static final String RE_AUTHENTICATION_DISCLAIMER = "reAuthenticationDisclaimer";
    public static final String IF_STOP_DATA_SHARING = "ifStopDataSharing";
    public static final String DO_YOU_CONFIRM = "doYouConfirm";
    public static final String OK_BUTTON = "buttonOk";
    public static final String DENY_BUTTON = "buttonDeny";
    public static final String GO_BACK_BUTTON = "buttonGoBack";
}
