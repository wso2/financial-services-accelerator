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

    PRE_CONSENT_GENERATION("pre_consent_generation"),
    POST_CONSENT_GENERATION("post_consent_generation"),
    PRE_CONSENT_RETRIEVAL("pre_consent_retrieval"),
    PRE_CONSENT_REVOCATION("pre_consent_revocation"),
    PRE_CONSENT_AUTHORIZATION("pre_consent_authorization"),
    CONSENT_VALIDATION("consent_validation"),
    PRE_ACCESS_TOKEN_GENERATION("pre_access_token_generation"),
    PRE_USER_AUTHORIZATION("pre_user_authorization"),
    POST_USER_AUTHORIZATION("post_user_authorization"),
    PRE_ID_TOKEN_GENERATION("pre_id_token_generation"),
    VALIDATE_DCR_CREATE_REQUEST("validate_dcr_app_creation"),
    VALIDATE_DCR_UPDATE_REQUEST("validate_dcr_app_update"),
    PRE_EVENT_SUBSCRIPTION("pre_event_subscription"),
    POST_EVENT_SUBSCRIPTION("post_event_subscription"),
    PRE_EVENT_CREATION("pre_event_creation"),
    PRE_EVENT_POLLING("pre_event_polling"),
    POST_EVENT_POLLING("post_event_polling"),
    ERROR_MAPPER("error_mapper");

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
