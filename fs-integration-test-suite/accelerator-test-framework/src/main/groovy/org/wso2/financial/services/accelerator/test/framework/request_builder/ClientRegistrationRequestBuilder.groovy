/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.framework.request_builder

import com.nimbusds.jose.JWSObject
import groovy.json.JsonSlurper
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.commons.lang3.StringUtils
import org.wso2.bfsi.test.framework.util.RestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Client Registration Request Builder Class.
 */
class ClientRegistrationRequestBuilder {

    static ConfigurationService configurationService = new ConfigurationService()
    static JWTGenerator jwtGenerator = new JWTGenerator()
    static String DISALLOWED_CHARS_PATTERN = '([~!#$;%^&*+={}\\s\\|\\\\<>\\\"\'\\/,\\]\\[\\(\\)])'

    /**
     * Build Client Registration Request.
     * @return dcr request
     */
    static RequestSpecification buildRegistrationRequest() {

        def authToken = "${configurationService.getUserKeyManagerAdminName()}:" +
                "${configurationService.getUserKeyManagerAdminPWD()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        return FSRestAsRequestBuilder.buildBasicRequest()
                .contentType(ContentType.JSON.toString())
                .accept(ContentType.JSON.toString())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .baseUri(configurationService.getISServerUrl())
    }

    /**
     * Build Client Registration Request.
     * @return dcr request
     */
    static RequestSpecification buildRegistrationRequestWithClaims(String claims) {

        return buildRegistrationRequest(ContentType.JSON.toString())
                .body(claims)
    }

    /**
     * Build Client Registration Request.
     * @return dcr request
     */
    static RequestSpecification buildRegistrationRequestForGetAndDelete(String accessToken) {

        return buildRegistrationRequest("application/json")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "${ConnectorTestConstants.BEARER} ${accessToken}")
    }

    /**
     * Build Client Registration Request.
     * @return dcr request
     */
    static RequestSpecification buildRegistrationRequestForUpdate(String accessToken, String claims) {

        return buildRegistrationRequest(ContentType.JSON.toString())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(jwtGenerator.getSignedRequestObject(claims))
    }

    /**
     * Build Client Registration Request.
     * @return dcr request
     */
    static RequestSpecification buildKeyManageRegistrationRequestWithClaims(String claims) {

        return buildKeyManagerRegistrationRequest()
                .body(claims)
    }

    /**
     * Get a registration request without encoding.
     *
     * @param accessToken
     * @return
     */
    static RequestSpecification buildPlainRequest(String claims) {

        return RestAsRequestBuilder.buildBasicRequest()
                .contentType("application/json")
                .body(claims)
                .accept("application/json")
    }


    /**
     * Build Client Registration Request.
     * @return dcr request
     */
    static RequestSpecification buildRegistrationRequestWithoutContentType() {

        return RestAsRequestBuilder.buildBasicRequest()
                .header("charset", StandardCharsets.UTF_8.toString())
                .baseUri(configurationService.getServerBaseURL())
    }

    /**
     * Build Client Registration Request.
     * @return dcr request
     */
    static RequestSpecification buildKeyManagerRegistrationRequest() {

        def authToken = "${obConfigurationService.getUserKeyManagerAdminName()}:" +
                "${obConfigurationService.getUserKeyManagerAdminPWD()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        return FSRestAsRequestBuilder.buildBasicRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
    }

    /**
     * Get Regular Claims for DCR Request.
     * @param ssa
     * @param iss
     * @param time
     * @param tokenEndpointAuthMethod
     * @param tokenEndpointAuthAlg
     * @param idTokenSignedAlg
     * @param reqObjSignedAlg
     * @return request claims
     */
    static String getRegularClaims(String ssa, String iss = configurationService.getAppDCRSoftwareId(),
                                   String tokenEndpointAuthMethod = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                   long time = Instant.now().toEpochMilli(),
                                   String tokenEndpointAuthAlg = ConnectorTestConstants.ALG_PS256,
                                   String idTokenSignedAlg = ConnectorTestConstants.ALG_PS256,
                                   String reqObjSignedAlg = ConnectorTestConstants.ALG_PS256) {

        long currentTimeInMillis = System.currentTimeMillis()
        long currentTimeInSeconds = currentTimeInMillis / 1000

        String ssaBody = decodeRequestJWT(ssa, "body")
        def json = new JsonSlurper().parseText(ssaBody)

        return """
             {
                "iss": "${iss}",
                "iat": ${currentTimeInSeconds},
                "exp": ${currentTimeInSeconds + 3600},
                "jti": "${currentTimeInMillis}",
                "aud": "https://localbank.com",
                "scope": "accounts payments fundsconfirmations",
                "token_endpoint_auth_method": "${tokenEndpointAuthMethod}",
                "token_endpoint_auth_signing_alg": "${tokenEndpointAuthAlg}",
                "grant_types": [
                    "authorization_code",
                    "client_credentials",
                    "refresh_token"
                    ],
                "response_types": [
                    "code id_token"
                    ],
                "id_token_signed_response_alg": "${idTokenSignedAlg}",
                "id_token_encrypted_response_alg": "RSA-OAEP",
                "id_token_encrypted_response_enc": "A256GCM",
                "request_object_signing_alg": "${reqObjSignedAlg}",                            
                "application_type": "web",
                "software_id": "${iss}",
                "redirect_uris": [
                    "${configurationService.getAppInfoRedirectURL()}"
                    ],
                "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
                "token_endpoint_allow_reuse_pvt_key_jwt":false,
                "tls_client_certificate_bound_access_tokens":true,
                "require_signed_request_object":true,
                "token_type_extension":"JWT",
                "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${iss}.jwks",
                "client_name": "${iss}",
                "ext_application_display_name":"${getSafeApplicationName(json['software_client_name'])}"
                "software_statement": "${ssa}"
            }
        """
    }

    /**
     * Get Regular Claims for DCR Request with invalid redirect uri.
     * @param ssa
     */
    static String getRegularClaimsWithInvalidRedirectURI(String ssa, String redirectUri) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 ${redirectUri}
               ],
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_signed_response_alg": "${ConnectorTestConstants.ALG_PS256}",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    /**
     * Get Regular Claims for DCR Request without redirect Uri.
     * @param ssa
     */
    static String getRegularClaimsWithoutRedirectURI(String ssa) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_signed_response_alg": "${ConnectorTestConstants.ALG_PS256}",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    /**
     * Get Regular Claims without Token Endpoint Auth Method.
     * @param ssa
     * @param iss
     * @param time
     * @param tokenEndpointAuthAlg
     * @param idTokenSignedAlg
     * @param reqObjSignedAlg
     * @return request body without Token Endpoint Auth Method claim
     */
    static String getRegularClaimsWithoutTokenEndpointAuthMethod(String ssa, iss = ConnectorTestConstants.SOFTWARE_ID,
                                                                 String time = Instant.now().toEpochMilli(),
                                                                 String tokenEndpointAuthAlg = ConnectorTestConstants.ALG_PS256,
                                                                 String idTokenSignedAlg = ConnectorTestConstants.ALG_PS256,
                                                                 String reqObjSignedAlg = ConnectorTestConstants.ALG_PS256) {
        return """
             {
                 "iss": "${iss}",
                 "iat": ${time},
                 "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
                 "jti": "92713892-5514-11e9-8647-d663bd873d93",
                 "aud": "https://localbank.com",
                 "scope": "accounts payments",
                 "token_endpoint_auth_signing_alg": "${tokenEndpointAuthAlg}",
                 "grant_types": [
                     "client_credentials",
                     "authorization_code",
                     "refresh_token"
                     ],
                 "response_types": [
                     "code id_token"
                     ],
                 "id_token_signed_response_alg": "${idTokenSignedAlg}",
                 "request_object_signing_alg": "${reqObjSignedAlg}",                            
                 "application_type": "web",
                 "software_id": "${ConnectorTestConstants.SOFTWARE_ID}",
                 "redirect_uris": [
                     "${ConnectorTestConstants.REDIRECT_URI}"
                     ],
                 "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
                 "token_endpoint_allow_reuse_pvt_key_jwt":false,
                 "tls_client_certificate_bound_access_tokens":true,
                 "require_signed_request_object":true,
                 "token_type_extension":"JWT",
                 "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${iss}.jwks",
                 "client_name": "${configurationService.getAppDCRSoftwareId()}",
                 "software_statement": "${ssa}"
             }
         """
    }

    /**
     * Get Regular Claims for DCR Request with invalid grant types.
     * @param ssa
     */
    static String getRegularClaimsWithInvalidGrantTypes(String ssa, String grantType) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 "${configurationService.getAppDCRRedirectUri()}"
               ],
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  ${grantType}
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_signed_response_alg": "${ConnectorTestConstants.ALG_PS256}",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    /**
     * Get Regular Claims without Grant Types.
     * @param ssa
     * @param iss
     * @param time
     * @param tokenEndpointAuthMethod
     * @param tokenEndpointAuthAlg
     * @param idTokenSignedAlg
     * @param reqObjSignedAlg
     * @return request body without grant types
     */
    static String getRegularClaimsWithoutGrantType(String ssa, iss = ConnectorTestConstants.SOFTWARE_ID,
                                                   String time = Instant.now().toEpochMilli(),
                                                   String tokenEndpointAuthMethod = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                                   String tokenEndpointAuthAlg = ConnectorTestConstants.ALG_PS256,
                                                   String idTokenSignedAlg = ConnectorTestConstants.ALG_PS256,
                                                   String reqObjSignedAlg = ConnectorTestConstants.ALG_PS256) {
        return """
             {
                 "iss": "${iss}",
                 "iat": ${time},
                 "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
                 "jti": "92713892-5514-11e9-8647-d663bd873d93",
                 "aud": "https://localbank.com",
                 "scope": "accounts payments",
                 "token_endpoint_auth_method": "${tokenEndpointAuthMethod}",
                 "token_endpoint_auth_signing_alg": "${tokenEndpointAuthAlg}",
                 "response_types": [
                     "code id_token"
                     ],
                 "id_token_signed_response_alg": "${idTokenSignedAlg}",
                 "request_object_signing_alg": "${reqObjSignedAlg}",                            
                 "application_type": "web",
                 "software_id": "${ConnectorTestConstants.SOFTWARE_ID}",
                 "redirect_uris": [
                     "${ConnectorTestConstants.REDIRECT_URI}"
                     ],
                 "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
                 "token_endpoint_allow_reuse_pvt_key_jwt":false,
                 "tls_client_certificate_bound_access_tokens":true,
                 "require_signed_request_object":true,
                 "token_type_extension":"JWT",
                 "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${iss}.jwks",
                 "client_name": "${iss}",
                 "software_statement": "${ssa}"
             }
         """
    }

    /**
     * Get Regular Claims for DCR Request without SSA.
     */
    static String getRegularClaimsWithoutSSA() {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 "${configurationService.getAppDCRRedirectUri()}"
               ],
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_signed_response_alg": "${ConnectorTestConstants.ALG_PS256}",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks"
         }
         """
    }

    /**
     * Get Regular Claims for DCR Request Without Request Obj Signing Alg.
     * @param ssa
     */
    static String getRegularClaimsWithoutRequestObjSigningAlg(String ssa) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 "${configurationService.getAppDCRRedirectUri()}"
               ],
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "id_token_signed_response_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    /**
     * Get Regular Claims for DCR Request Without Id Token Signed Response Alg.
     * @param ssa
     */
    static String getRegularClaimsWithoutIdTokenSignedResponseAlg(String ssa) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 "${configurationService.getAppDCRRedirectUri()}"
               ],
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    /**
     * Get Regular Claims for DCR Request Without Id Token Signed Response Alg.
     * @param ssa
     */
    static String getRegularClaimsWithoutTokenEPAuthSigningAlg(String ssa) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 "${configurationService.getAppDCRRedirectUri()}"
               ],
               "id_token_signed_response_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    /**
     * Get Regular Claims For DCR Update Request.
     * @param ssa
     * @param iss
     * @param time
     * @param tokenEndpointAuthMethod
     * @param tokenEndpointAuthAlg
     * @param idTokenSignedAlg
     * @param reqObjSignedAlg
     * @return request claims
     */
    static String getUpdateRegularClaims(String ssa, String iss = configurationService.getAppDCRSoftwareId(),
                                         String tokenEndpointAuthMethod = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                         String time = System.currentTimeMillis() / 1000L,
                                         String tokenEndpointAuthAlg = ConnectorTestConstants.ALG_PS256,
                                         String idTokenSignedAlg = ConnectorTestConstants.ALG_PS256,
                                         String reqObjSignedAlg = ConnectorTestConstants.ALG_PS256) {


        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${iss}",
               "iat": ${time},
               "exp": ${time + 3600},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 "${configurationService.getAppDCRRedirectUri()}"
               ],
               "token_endpoint_auth_signing_alg": "${tokenEndpointAuthAlg}",
               "token_endpoint_auth_method": "${tokenEndpointAuthMethod}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_signed_response_alg": "${idTokenSignedAlg}",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${reqObjSignedAlg}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${iss}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    /**
     * Retrieve Service Provider from Carbon Console.
     * @param appName
     * @return
     */
    static Response retrieveServiceProviderCreated(String appName) {

        def response = RestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY,
                        "Basic " + TestUtil.getBasicAuthHeader(
                                configurationService.getUserKeyManagerAdminName(),
                                configurationService.getUserKeyManagerAdminPWD()))
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .queryParam("filter", "name+eq+$appName")
                .urlEncodingEnabled(false)
                .baseUri(configurationService.getISServerUrl())
                .get(ConnectorTestConstants.SP_INTERNAL_ENDPOINT)

        return response
    }

    /**
     * Retrieve Oauth Application from Dev Portal.
     * @param appName
     * @param accessToken
     * @return
     */
    static Response retrieveOauthAppFromDevPortal(String appName, String accessToken) {

        def response = RestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "${ConnectorTestConstants.BEARER} $accessToken")
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .queryParam("query", appName)
                .baseUri(configurationService.getApimServerUrl())
                .get(ConnectorTestConstants.OAUTH_APP_INTERNAL_ENDPOINT)

        return response
    }

    /**
     * Retrieve Oauth Application from Dev Portal.
     * @param appId
     * @param accessToken
     * @return
     */
    static Response retrieveSubscriptionForApp(String appId, String accessToken) {

        def response = RestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "${ConnectorTestConstants.BEARER} $accessToken")
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .queryParam("applicationId", appId)
                .urlEncodingEnabled(false)
                .baseUri(configurationService.getApimServerUrl())
                .get(ConnectorTestConstants.API_INTERNAL_ENDPOINT)

        return response
    }

    static String generateSsaWithInvalidRole(String softwareId = ConnectorTestConstants.SOFTWARE_ID,
                                             String time = Instant.now().toEpochMilli()) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${softwareId}",
               "iat": ${time},
               "jti": "${currentTimeInMillis}",
               "software_environment": "sandbox",
               "software_mode": "Test",
               "software_id": "${softwareId}",
               "software_client_id": "${softwareId}",
               "software_client_name": "Test APP (Sandbox)",
               "software_client_description": "This TPP Is created for testing purposes. ",
               "software_version": 1.5,
               "software_client_uri": "https://wso2.com",
               "software_redirect_uris": [
               "${configurationService.getAppDCRRedirectUri()}"
                 ],
               "software_roles": [
                "ASP",
                "PISP",
                "CBPII"
                 ],
               "organisation_competent_authority_claims": {
               "authority_id": "OBGBR",
               "registration_id": "Unknown0015800001HQQrZAAX",
               "status": "Active",
               "authorisations": [
                 {
                   "member_state": "GB",
                   "roles": [
                     "AISP",
                     "PISP"
                   ]
                 },
                 {
                   "member_state": "IE",
                   "roles": [
                     "AISP",
                     "PISP"
                   ]
                 },
                 {
                   "member_state": "NL",
                   "roles": [
                     "AISP",
                     "PISP"
                   ]
                 }
               ]
              },
             "software_logo_uri": "https://wso2.com/wso2.jpg",
             "org_status": "Active",
             "org_id": "0015800001HQQrZAAX",
             "org_name": "WSO2 (UK) LIMITED",
             "org_contacts": [
               {
                 "name": "Technical",
                 "email": "sachinis@wso2.com",
                 "phone": "+94774274374",
                 "type": "Technical"
               },
               {
                 "name": "Business",
                 "email": "sachinis@wso2.com",
                 "phone": "+94774274374",
                 "type": "Business"
               }
             ],
             "org_jwks_endpoint": "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/0015800001HQQrZAAX.jwks",
             "org_jwks_revoked_endpoint": "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/revoked/0015800001HQQrZAAX.jwks",
             "software_jwks_endpoint": "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${softwareId}.jwks",
             "software_jwks_revoked_endpoint": "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/revoked/${softwareId}.jwks",
             "software_policy_uri": "https://wso2.com",
             "software_tos_uri": "https://wso2.com",
             "software_on_behalf_of_org": "WSO2 Open Banking"
           }
         """
    }

    /**
     * Get Regular Claims for DCR Request with defined redirect uri.
     * @param ssa
     */
    static String getRegularClaimsWithDefinedRedirectURI(String ssa, String redirectUri) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 "${redirectUri}"
               ],
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
 
             }"""
    }


    /**
     * Get Regular Claims for DCR Request with null redirect uri.
     * @param ssa
     */
    static String getRegularClaimsWithNullRedirectURI(String ssa) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": null,
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_signed_response_alg": "${ConnectorTestConstants.ALG_PS256}",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    /**
     * Get Regular Claims for DCR Request with multiple redirect uris.
     * @param ssa
     */
    static String getRegularClaimsWithMultipleRedirectURI(String ssa, String redirectUri1, String redirectUri2) {

        long currentTimeInMillis = System.currentTimeMillis()

        return """
             {
               "iss": "${configurationService.getAppDCRSoftwareId()}",
               "iat": ${Instant.now().toEpochMilli()},
               "exp": ${Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli()},
               "jti": "${currentTimeInMillis}",
               "aud": "https://localbank.com",
               "software_id": "${configurationService.getAppDCRSoftwareId()}",
               "scope": "accounts payments",
               "redirect_uris": [
                 ${redirectUri1}, ${redirectUri2}
               ],
               "token_endpoint_auth_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "token_endpoint_auth_method": "${ConnectorTestConstants.PKJWT_AUTH_METHOD}",
               "grant_types": [
                  "authorization_code",
                  "client_credentials",
                  "refresh_token"
               ],
               "response_types": [
                  "code id_token"
               ],
               "application_type": "web",
               "id_token_signed_response_alg": "${ConnectorTestConstants.ALG_PS256}",
               "id_token_encrypted_response_alg": "RSA-OAEP",
               "id_token_encrypted_response_enc": "A256GCM",
               "request_object_signing_alg": "${ConnectorTestConstants.ALG_PS256}",
               "ext_application_display_name": "WSO2_Open_Banking_TPP2__Sandbox_",
               "token_endpoint_allow_reuse_pvt_key_jwt":false,
               "tls_client_certificate_bound_access_tokens":true,
               "require_signed_request_object":true,
               "token_type_extension":"JWT",
               "jwks_uri":"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configurationService.getAppDCRSoftwareId()}.jwks",
               "client_name": "${configurationService.getAppDCRSoftwareId()}",
               "software_statement": "${ssa}"
         }
         """
    }

    static String decodeRequestJWT(String jwtToken, String jwtPart) throws ParseException {

        JWSObject plainObject = JWSObject.parse(jwtToken);

        if ("head".equals(jwtPart)) {
            return plainObject.getHeader().toString();
        } else if ("body".equals(jwtPart)) {
            return plainObject.getPayload().toString();
        }

        return StringUtils.EMPTY;
    }

    static String getSafeApplicationName(String applicationName) {

        String sanitizedInput = applicationName.trim().replaceAll(DISALLOWED_CHARS_PATTERN, "_");

        return StringUtils.abbreviate(sanitizedInput, 70);
    }
}
