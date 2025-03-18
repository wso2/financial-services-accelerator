/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.common.extension.model;

/**
 * Service extension type enum
 */
public enum ServiceExtensionTypeEnum {

    PRE_CONSENT_GENERATION("pre-consent-generation"),
    PRE_CONSENT_RETRIEVAL("pre-consent-retrieval"),
    PRE_CONSENT_REVOCATION("pre-consent-revocation"),
    PRE_CONSENT_AUTHORIZATION("pre-consent-authorization"),
    CONSENT_VALIDATION("consent-validation"),
    PRE_ACCESS_TOKEN_GENERATION("pre-access-token-generation"),
    PRE_USER_AUTHORIZATION("pre-user-authorization"),
    POST_USER_AUTHORIZATION("post-user-authorization"),
    PRE_ID_TOKEN_GENERATION("pre-id-token-generation"),
    VALIDATE_DCR_CREATE_REQUEST("validate-dcr-app-creation"),
    VALIDATE_DCR_UPDATE_REQUEST("validate-dcr-app-update"),
    GET_DCR_ADDITIONAL_REQ_PARAMS("get-additional-dcr-request-parameters"),
    GET_DCR_RESPONSE_PARAMS("get-dcr-response-parameters"),
    GET_DCR_AUTH_SCRIPT("get-dcr-auth-script"),
    POST_DCR_APP_DELETE("post-dcr-app-delete");

    private final String type;

    ServiceExtensionTypeEnum(String value) {
        this.type = value;
    }

    public static ServiceExtensionTypeEnum fromString(String type) {

        for (ServiceExtensionTypeEnum serviceExtensionTypeEnum : ServiceExtensionTypeEnum.values()) {
            if (serviceExtensionTypeEnum.type.equalsIgnoreCase(type)) {
                return serviceExtensionTypeEnum;
            }
        }
        return null;
    }

    public String toString() {
        return String.valueOf(type);
    }

}
