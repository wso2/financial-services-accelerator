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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common;

/**
 * Consent operation enum
 */
public enum ConsentOperationEnum {

    CONSENT_DEFAULT("consent_default"),
    CONSENT_CREATE("consent_create"),
    CONSENT_RETRIEVE("consent_retrieve"),
    CONSENT_DELETE("consent_delete"),
    CONSENT_UPDATE("consent_update"),
    CONSENT_PARTIAL_UPDATE("consent_partial_update");

    private final String operation;

    ConsentOperationEnum(String operation) {

        this.operation = operation;
    }

    @Override
    public String toString() {

        return operation;
    }

}
