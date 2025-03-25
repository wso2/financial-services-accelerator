/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.identity.extensions.util;

/**
 * Class containing the constants for Open Banking Common module.
 */
public class IdentityCommonConstants {

    public static final String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    public static final String CLIENT_ID = "client_id";
    public static final String REQUEST_URI = "request_uri";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String REQUEST_OBJECT_SIGNING_ALG = "request_object_signing_alg";
    public static final String CLIENT_ID_ERROR = "Client id not found";
    public static final String CERTIFICATE_HEADER = "x-wso2-mutual-auth-cert";
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public static final String MTLS_AUTH_HEADER = "MutualTLS.ClientCertificateHeader";
    public static final String X509 = "X.509";
    public static final String FS_PREFIX = "FS_";
    public static final String TIME_PREFIX = "TIME_";
    public static final String CERT_PREFIX = "x5t#";
    public static final String SPACE_SEPARATOR = " ";
    public static final String SCOPE = "scope";
    public static final String OPENID_SCOPE = "openid";
    public static final String S_HASH = "s_hash";
    public static final String CODE = "code";
    public static final String SUBJECT_CLAIM = "sub";
    public static final String CNF_CLAIM = "cnf";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String CONDITIONAL_COMMON_AUTH_SCRIPT_FILE_NAME = "common.auth.script.js";
    public static final String ARRAY_ELEMENT_SEPARATOR = "#";
    public static final String SOFTWARE_STATEMENT = "software_statement";
    public static final String SOFTWARE_ID = "software_id";
    public static final String ISS = "iss";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String USER_DEFINED_RESOURCE = "User-defined-oauth2-resource";
    public static final String RBAC_POLICY = "RBAC";
    public static final String APPLICATION_TYPE = "application_type";
    public static final String RESPONSE_TYPES = "response_types";
    public static final String SSA_REDIRECT_URIS = "redirect_uris";;
    public static final String SSA_LOGO_URI = "logo_uri";
    public static final String SSA_POLICY_URI = "policy_uri";
    public static final String SSA_TOS_URI = "tos_uri";
    public static final String SSA_CLIENT_URI = "client_uri";
    public static final String INVALID_CLIENT_METADATA = "invalid_client_metadata";
    public static final String SERVER_ERROR = "server_error";
    public static final String INCLUDE_IN_RESPONSE = "IncludeInResponse";
    public static final String KEY = "Key";
    public static final String ENABLE = "Enable";
    public static final String CLASS = "Class";
    public static final String PRIORITY = "Priority";
    public static final String ADDITIONAL_ATTRIBUTES = "additionalAttributes";
    public static final String REQUIRED = "Required";
    public static final String ALLOWED_VALUES = "AllowedValues";
    public static final String SSA_ISSUER_VALIDATOR = "SSAIssuerValidator";
    public static final String JTI = "jti";

    // Service Extension constants
    public static final String USER_ID = "userId";
    public static final String SCOPES = "scopes";
    public static final String CONSENT_ID = "consentId";
    public static final String VALIDITY_PERIOD = "validityPeriod";
    public static final String GRANT_TYPE = "grantType";
    public static final String APP_REG_REQUEST = "appRegistrationRequest";
    public static final String APP_UPDATE_REQUEST = "appUpdateRequest";
    public static final String SSA_PARAMS = "ssaParams";
    public static final String SP_PROPERTIES = "spProperties";
}

