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

// The Identity Server URL need not be specifically configured unless the app is deployed in a different server.
let serverUrl;
if (USE_DEFAULT_CONFIGS) {
    serverUrl = window.location.origin;
} else {
    serverUrl = "https://localhost:9446";
}

export const CONFIG = {
    SERVER_URL: serverUrl,
    BACKEND_URL: serverUrl + "/consentapproval/ca/admin"
};
