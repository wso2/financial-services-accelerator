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

package com.wso2.openbanking.accelerator.authentication.webapp.util;

/**
 * Constants required for auth webapp.
 */
public class Constants {

    public static final String IS_ERROR = "isError";
    public static final String REQUESTED_CLAIMS = "requestedClaims";
    public static final String MANDATORY_CLAIMS = "mandatoryClaims";
    public static final String CLAIM_SEPARATOR = ",";
    public static final String USER_CLAIMS_CONSENT_ONLY = "userClaimsConsentOnly";
    public static final String SESSION_DATA_KEY_CONSENT = "sessionDataKeyConsent";
    public static final String DEFAULT = "default";
    public static final String CONSENT_MGT = "consentmgt";
    public static final String COMMONAUTH_ID = "commonAuthId";
    public static final String OIDC_SCOPES = "OIDCScopes";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String STATE = "state";
    public static final String ERROR_URI_FRAGMENT = "#error=";
    public static final String ERROR_DESCRIPTION_PARAMETER = "&error_description=";
    public static final String STATE_PARAMETER = "&state=";
    public static final String CONFIG_FILE_NAME = "configurations.properties";
    public static final String LOCATION_OF_CREDENTIALS = "ConsentAPICredentials.IsConfiguredInWebapp";
    public static final String USERNAME_IN_WEBAPP_CONFIGS = "ConsentAPICredentials.Username";
    public static final String PASSWORD_IN_WEBAPP_CONFIGS = "ConsentAPICredentials.Password";
    public static final String USERNAME_IN_OB_CONFIGS = "Consent.ConsentAPICredentials.Username";
    public static final String PASSWORD_IN_OB_CONFIGS = "Consent.ConsentAPICredentials.Password";
}
