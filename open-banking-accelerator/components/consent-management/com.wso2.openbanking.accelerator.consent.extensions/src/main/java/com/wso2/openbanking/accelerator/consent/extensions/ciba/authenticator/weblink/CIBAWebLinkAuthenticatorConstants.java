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

package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink;

/**
 * CIBA web link authenticator Constants
 */
public class CIBAWebLinkAuthenticatorConstants {

    public static final String AUTHENTICATOR_NAME = "ciba-weblink";
    public static final String AUTHENTICATOR_FRIENDLY_NAME = "CIBA Web Link Authenticator";
    public static final String NOTIFICATION_TRIGGER_EVENT = "CIBA_WEBLINK_NOTIFICATION_EVENT";
    public static final String NOTIFICATION_HANDLER_NAME = "cibaWebLinkNotificationHandler";
    public static final String REQUEST_OBJECT = "request_object";
    public static final String OPEN_BANKING_INTENT_ID = "openbanking_intent_id";
    public static final String VALUE = "value";
    public static final String USER_INFO = "userinfo";
    public static final String CLAIMS = "claims";
    public static final String AUTHORIZE_URL_PATH = "/oauth2/authorize?";
    public static final String LOGIN_HINT = "login_hint";

}
