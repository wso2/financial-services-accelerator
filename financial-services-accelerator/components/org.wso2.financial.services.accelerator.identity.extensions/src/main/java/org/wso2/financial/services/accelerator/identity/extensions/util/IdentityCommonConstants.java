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
    public static final String BFSI_PREFIX = "BFSI_";
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
}
