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

package com.wso2.openbanking.accelerator.identity.common;

import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnService;

/**
 * Exporter service to facilitate access to identity services in data holder from other modules.
 */
public class IdentityServiceExporter {

    private static OAuthClientAuthnService oAuthClientAuthnService;

    public static OAuthClientAuthnService getOAuthClientAuthnService() {
        return oAuthClientAuthnService;
    }

    public static void setOAuthClientAuthnService(OAuthClientAuthnService oAuthClientAuthnService) {
        IdentityServiceExporter.oAuthClientAuthnService = oAuthClientAuthnService;
    }
}
