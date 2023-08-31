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

package com.wso2.openbanking.scp.webapp.util;

/**
 * Constants required for scp webapp.
 */
public class Constants {

    private Constants() {
        // No public instances
    }

    // OAUTH Constants
    public static final String CLIENT_ID = "client_id";
    public static final String GRANT_TYPE = "grant_type";
    public static final String OAUTH_CODE = "code";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String OAUTH_SCOPE = "scope";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String ID_TOKEN = "id_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String EXPIRES_IN = "expires_in";
    public static final String ID_TOKEN_HINT = "id_token_hint";
    public static final String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";

    public static final String COOKIE_BASE_NAME = "OB_SCP_";
    public static final String ACCESS_TOKEN_COOKIE_NAME = COOKIE_BASE_NAME + "AT";
    public static final String ID_TOKEN_COOKIE_NAME = COOKIE_BASE_NAME + "IT";
    public static final String REFRESH_TOKEN_COOKIE_NAME = COOKIE_BASE_NAME + "RT";
    public static final String SCP_TOKEN_VALIDITY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String PART_ONE = "_P1";
    public static final String PART_TWO = "_P2";
    public static final String DEFAULT_BASE_PATH = "/consentmgr";
    public static final int DEFAULT_COOKIE_MAX_AGE = 3600; //(60*60) = 1h

    public static final String CONFIG_FILE_NAME = "configurations.properties";
    public static final String LOCATION_OF_CREDENTIALS = "Consent.Portal.Params.IsConfiguredInWebapp";
    public static final String CONFIGURED_CLIENT_ID = "Consent.Portal.ClientCredentials.ClientId";
    public static final String CONFIGURED_CLIENT_SECRET = "Consent.Portal.ClientCredentials.ClientSecret";
    public static final String IS_BASE_URL = "Consent.Portal.Params.IdentityServerBaseUrl";
    public static final String APIM_BASE_URL = "Consent.Portal.Params.ApiManagerServerBaseUrl";
    public static final String CONFIGURED_USERNAME = "Consent.ConsentAPICredentials.Username";
    public static final String CONFIGURED_PASSWORD = "Consent.ConsentAPICredentials.Password";

    // Paths
    public static final String PATH_TOKEN = "/oauth2/token";
    public static final String PATH_LOGOUT = "/oidc/logout";
    public static final String PATH_CALLBACK = "/consentmgr/scp_oauth2_callback";
    public static final String PATH_AUTHORIZE = "/oauth2/authorize";
    public static final String PATH_APIM_CONSENT_SEARCH_V1 = "/ob_consent/v1/admin/search";
    public static final String PATH_APIM_CONSENT_REVOKE_V1 = "/ob_consent/v1/admin/revoke";
    public static final String PATH_IS_CONSENT_RETRIEVE = "/api/openbanking/consent/authorize/retrieve";
    public static final String PATH_IS_CONSENT_PERSIST = "/api/openbanking/consent/authorize/persist";
}
