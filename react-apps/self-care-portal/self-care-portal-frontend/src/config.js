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

// The Identity Server URL need not be specifically configured unless the SCP is deployed in a different server.
let serverUrl;
if (window.env.USE_DEFAULT_CONFIGS) {
    serverUrl = window.location.origin;
} else {
    serverUrl = window.env.SERVER_URL;
}

export const CONFIG = {
    SERVER_URL: serverUrl,
    SPEC: window.env.SPEC,
    TENANT_DOMAIN:window.env.TENANT_DOMAIN,
    TOKEN_ENDPOINT: serverUrl + "/oauth2/token",
    AUTHORIZE_ENDPOINT: serverUrl + "/consentmgr/scp_oauth2_authorize",
    LOGOUT_URL: serverUrl + "/oidc/logout",
    REDIRECT_URI: serverUrl + "/consentmgr/scp_oauth2_callback",
    BACKEND_URL: serverUrl + "/consentmgr/scp",
    NUMBER_OF_CONSENTS: window.env.NUMBER_OF_CONSENTS,
    VERSION: window.env.VERSION,
    DEVICE_REGISTRATION_URL: serverUrl + "/api/openbanking/ciba/push-auth/discovery-data",
    IS_DEV_TOOLS_ENABLE: window.env.IS_DEV_TOOLS_ENABLE
};
