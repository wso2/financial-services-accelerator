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

const USE_DEFAULT_CONFIGS = true;

// The Identity Server URL need not be specifically configured unless the SCP is deployed in a different server.
let serverUrl;
if (USE_DEFAULT_CONFIGS) {
  serverUrl = window.location.origin;
} else {
  serverUrl = "https://localhost:9446";
}

export const CONFIG = {
  SERVER_URL: serverUrl,
  TENANT_DOMAIN: "carbon.super",
  TOKEN_ENDPOINT: serverUrl + "/oauth2/token",
  AUTHORIZE_ENDPOINT: serverUrl + "/consentmgr/scp_oauth2_authorize",
  LOGOUT_URL: serverUrl + "/oidc/logout",
  REDIRECT_URI: serverUrl + "/consentmgr/scp_oauth2_callback",
  BACKEND_URL: serverUrl + "/consentmgr/scp",
  DEVICE_REGISTRATION_URL:
    serverUrl + "/api/openbanking/ciba/push-auth/discovery-data",
  IS_DEVELOPMENT: process.env.NODE_ENV === "development",
  NUMBER_OF_CONSENTS: 20,
  NUMBER_OF_CONSENTS_PER_PAGE: [5, 10, 15, 20, 25],
};
