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

package com.wso2.openbanking.accelerator.identity.util;

/**
 * Class containing the constants for Open Banking Common module.
 */
public class IdentityCommonConstants {

    public static final String CLIENT_ID = "client_id";
    public static final String REQUEST_URI = "request_uri";
    public static final String REQUEST = "request";
    public static final String STATE = "state";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String CARBON_HOME = "carbon.home";
    public static final String REGULATORY_COMPLIANCE = "regulatory";
    public static final String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";
    public static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg";
    public static final String REQUEST_OBJECT_SIGNING_ALG = "request_object_signing_alg";
    public static final String CLIENT_ID_ERROR = "Client id not found";
    public static final String OAUTH_CLIENT_ID = "client_id";
    public static final String OAUTH_CLIENT_SECRET = "client_secret";
    public static final String AUTHORIZATION_HEADER = "authorization";
    public static final String OAUTH_JWT_ASSERTION = "client_assertion";
    public static final String OAUTH_JWT_ASSERTION_TYPE = "client_assertion_type";
    public static final String OAUTH_JWT_BEARER_GRANT_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public static final String MTLS_AUTH_HEADER = "MutualTLS.ClientCertificateHeader";
    public static final String X509 = "X.509";
    public static final String TOKEN_FILTER = "Identity.Filters.TokenFilter";
    public static final String TOKEN_VALIDATORS = "Identity.TokenFilterValidators.Validator";
    public static final String CLAIM_PROVIDER = "Identity.Extensions.ClaimProvider";
    public static final String INTROSPECTION_DATA_PROVIDER = "Identity.Extensions.IntrospectionDataProvider";
    public static final String SIGNING_CERT_KID = "Identity.SigningCertificateKid";
    public static final String OAUTH_ERROR = "error";
    public static final String OAUTH_ERROR_DESCRIPTION = "error_description";
    public static final String JAVAX_SERVLET_REQUEST_CERTIFICATE = "javax.servlet.request.X509Certificate";
    public static final String OB_CONSENT_ID_PREFIX = "OB_CONSENT_ID_";
    public static final String OB_PREFIX = "OB_";
    public static final String TIME_PREFIX = "TIME_";
    public static final String TLS_CERT = "tls_cert";
    public static final String CERT_PREFIX = "x5t#";
    public static final String CERTIFICATE_HEADER = "x-wso2-mutual-auth-cert";
    public static final String CERTIFICATE_HEADER_ATTRIBUTE = "x-wso2-mutual-auth-cert-attribute";
    public static final String SPACE_SEPARATOR = " ";
    public static final String SCOPE = "scope";
    public static final String CONDITIONAL_COMMON_AUTH_SCRIPT_FILE_NAME = "common.auth.script.js";
    public static final String PRIMARY_AUTHENTICATOR_DISPLAYNAME = "SCA.PrimaryAuthenticator.DisplayName";
    public static final String PRIMARY_AUTHENTICATOR_NAME = "SCA.PrimaryAuthenticator.Name";
    public static final String IDENTITY_PROVIDER_NAME = "SCA.IdpName";
    public static final String IDENTITY_PROVIDER_STEP = "SCA.IdpStep";
    public static final String REQUEST_VALIDATOR = "Identity.Extensions.RequestObjectValidator";
    public static final String PUSH_AUTH_REQUEST_VALIDATOR = "Identity.Extensions.PushAuthRequestValidator";
    public static final String PUSH_AUTH_REQUEST_URL = "Identity.Extensions.PushAuthRequestUrl";
    public static final String RESPONSE_HANDLER = "Identity.Extensions.ResponseTypeHandler";
    public static final String ENABLE_TRANSPORT_CERT_AS_HEADER = "Identity.ClientTransportCertAsHeaderEnabled";
    public static final String ENABLE_SUBJECT_AS_PPID = "Identity.EnableSubjectPPID";
    public static final String REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveUserStoreDomainFromSubject";
    public static final String REMOVE_TENANT_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveTenantDomainFromSubject";

    public static final String AUTH_SERVLET_EXTENSION = "Identity.Extensions.AuthenticationWebApp.ServletExtension";
    public static final String CONSENT_ID_CLAIM_NAME = "Identity.ConsentIDClaimName";
    public static final String SP_ACCESS_TOKEN_INPUT_STREAM = "AccessTokenInputStream";
    public static final String INPUT_STREAM_VERSION = "1.0.0";
    public static final String ACCESS_TOKEN_ID = "accessTokenID";
    public static final String NOT_APPLICABLE = "N/A";
    public static final String CONSENT_JWT_PAYLOAD_VALIDATION = "Consent.Validation.JWTPayloadValidation";

    public static final String S_HASH = "s_hash";
    public static final String CODE = "code";
    public static final String DCR_INTERNAL_SCOPE = "OB_DCR";
    public static final String OPENID_SCOPE = "openid";
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    public static final String CARBON_SUPER = "carbon.super";
    public static final String REGISTRATION_ACCESS_TOKEN = "registration_access_token";
    public static final String REGISTRATION_CLIENT_URI = "registration_client_uri";
    public static final String PRIVATE_KEY = "pvt_key";
    public static final String ALG_ES256 = "ES256";
    public static final String ALG_PS256 = "PS256";
    public static final String DEFAULT_JWKS_URI = "software_jwks_endpoint";
    public static final String DEFAULT_REGISTRATION_CLIENT_URI = "https://localhost:8243/open-banking/0.1/register/";
    public static final String PAR_ENDPOINT = "/api/openbanking/push-authorization/par";

    public static final String TLS_CERT_JWKS = "Identity.MutualTLS.TransportCertificateJWKS";
    public static final String CLIENT_CERTIFICATE_ENCODE = "Identity.MutualTLS.ClientCertificateEncode";
    public static final String DCR_MODIFY_RESPONSE = "DCR.ModifyResponse";
    public static final String DCR_SCOPE = "DCR.Scope";
    public static final String DCR_REGISTRATION_CLIENT_URI = "DCR.RegistrationClientURI";

    //Error Constants
    public static final String OAUTH2_INVALID_CLIENT_MESSAGE = "invalid_client";
    public static final String OAUTH2_INVALID_REQUEST_MESSAGE = "invalid_request";
    public static final String OAUTH2_INTERNAL_SERVER_ERROR = "server_error";
}
