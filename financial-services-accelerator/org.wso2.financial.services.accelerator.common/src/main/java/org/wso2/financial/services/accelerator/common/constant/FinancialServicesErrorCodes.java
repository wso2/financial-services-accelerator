/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.common.constant;

/**
 * Class containing the error codes for Financial Services.
 */
public class FinancialServicesErrorCodes {

    public static final String BAD_REQUEST_CODE = "400";
    public static final String UNAUTHORIZED_CODE = "401";
    public static final String FORBIDDEN_CODE = "403";
    public static final String NOT_FOUND_CODE = "404";
    public static final String NOT_ALLOWED_CODE = "405";
    public static final String NOT_ACCEPTABLE_CODE = "406";
    public static final String UNSUPPORTED_MEDIA_TYPE_CODE = "415";
    public static final String SERVER_ERROR_CODE = "500";

    public static final String INVALID_GRANT_TYPE_CODE = "200001";
    public static final String CONSENT_VALIDATION_REQUEST_FAILURE = "200002";
    public static final String INVALID_MTLS_CERT_CODE = "200003";
    public static final String TPP_VALIDATION_FAILED_CODE = "200004";
    public static final String INVALID_SIGNATURE = "200005";
    public static final String SCP_USER_VALIDATION_FAILED_CODE = "200006";
    public static final String MISSING_MTLS_CERT_CODE = "200007";
    public static final String EXPIRED_MTLS_CERT_CODE = "200008";
    public static final String REVOKED_MTLS_CERT_CODE = "200009";
    public static final String INVALID_SIGNATURE_CODE = "200010";
    public static final String MISSING_CONTENT_TYPE = "200011";
    public static final String INVALID_CONTENT_TYPE = "200012";
    public static final String MISSING_REQUEST_PAYLOAD = "200013";
    public static final String INVALID_CHARS_IN_HEADER_ERROR = "200014";
    public static final String MISSING_HEADER_PARAM_CLIENT_ID = "200015";
    public static final String ERROR_IN_EVENT_POLLING_REQUEST = "200016";

    // Error titles
    public static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type";

    public static final String REGISTRATION_INTERNAL_ERROR = "Error occurred while registering application";
    public static final String REGISTATION_DELETE_ERROR = "Error occurred while deleting application";
    public static final String REGISTRATION_UPDATE_ERROR = "Error occurred while updating application";

    public static final String EXECUTOR_JWS_SIGNATURE_NOT_FOUND = "Error occurred in JWS Executor";
    public static final String JWS_SIGNATURE_HANDLE_ERROR = "Error occurred while validating JWS Signature";
}
