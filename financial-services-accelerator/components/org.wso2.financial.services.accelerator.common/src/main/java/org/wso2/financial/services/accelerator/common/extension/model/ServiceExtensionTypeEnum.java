/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.common.extension.model;

/**
 * Service extension type enum
 */
public enum ServiceExtensionTypeEnum {

    POST_TOKEN_GENERATION("post-token-generation"),
    PRE_CONSENT_AUTHORIZATION("pre-consent-authorization"),
    CONSENT_PERSISTENCE("consent-persistence"),
    CONSENT_MANAGE("consent-manage"),
    CONSENT_MANAGE_GET("consent-manage-get"),
    CONSENT_MANAGE_DELETE("consent-manage-delete");

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
