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

    PRE_PROCESS_CLIENT_CREATION("pre_process_client_creation"),
    PRE_PROCESS_CLIENT_UPDATE("pre_process_client_update"),
    PRE_PROCESS_CLIENT_RETRIEVAL("pre_process_client_retrieval"),
    PRE_PROCESS_CONSENT_CREATION("pre_process_consent_creation"),
    ENRICH_CONSENT_CREATION_RESPONSE("enrich_consent_creation_response"),
    PRE_PROCESS_CONSENT_FILE_UPLOAD("pre_process_consent_file_upload"),
    ENRICH_CONSENT_FILE_RESPONSE("enrich_consent_file_response"),
    VALIDATE_CONSENT_FILE_RETRIEVAL("validate_consent_file_retrieval"),
    PRE_PROCESS_CONSENT_RETRIEVAL("pre_process_consent_retrieval"),
    ENRICH_CONSENT_SEARCH("enrich_consent_search"),
    PRE_PROCESS_CONSENT_REVOKE("pre_process_consent_revoke"),
    POPULATE_CONSENT_AUTHORIZE_SCREEN("populate_consent_authorize_screen"),
    PERSIST_AUTHORIZED_CONSENT("persist_authorized_consent"),
    VALIDATE_CONSENT_ACCESS("validate_consent_access"),
    ISSUE_REFRESH_TOKEN("issue_refresh_token"),
    VALIDATE_AUTHORIZATION_REQUEST("validate_authorization_request"),
    VALIDATE_EVENT_SUBSCRIPTION("validate_event_subscription"),
    ENRICH_EVENT_SUBSCRIPTION_RESPONSE("enrich_event_subscription_response"),
    VALIDATE_EVENT_CREATION("validate_event_creation"),
    VALIDATE_EVENT_POLLING("validate_event_polling"),
    ENRICH_EVENT_POLLING_RESPONSE("enrich_event_polling_response"),
    MAP_ACCELERATOR_ERROR_RESPONSE("map_accelerator_error_response");

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
