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
 * Operation type enum
 */
public enum OperationEnum {

    ADDITIONAL_ID_TOKEN_CLAIMS_FOR_AUTHZ_RESPONSE("additionalIdTokenClaimsForAuthzResponse"),
    ADDITIONAL_ID_TOKEN_CLAIMS_FOR_TOKEN_RESPONSE("additionalIdTokenClaimsForTokenResponse"),
    GET_APPROVED_SCOPES("getApprovedScopes"),
    GET_REFRESH_TOKEN_VALIDITY_PERIOD("getRefreshTokenValidityPeriod"),
    VALIDATE_REQUEST_OBJECT("validateRequestObject"),
    APPEND_PARAMETERS_TO_TOKEN_RESPONSE("appendParametersToTokenResponse"),
    ISSUE_REFRESH_TOKEN("issueRefreshToken");

    private final String operation;

    OperationEnum(String value) {
        this.operation = value;
    }

    public static OperationEnum fromString(String operation) {

        for (OperationEnum operationEnum : OperationEnum.values()) {
            if (operationEnum.operation.equalsIgnoreCase(operation)) {
                return operationEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return operation;
    }

}
