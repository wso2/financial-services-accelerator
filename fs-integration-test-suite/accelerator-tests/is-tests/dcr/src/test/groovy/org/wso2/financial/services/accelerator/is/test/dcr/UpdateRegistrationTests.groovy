/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.financial.services.accelerator.is.test.dcr

import io.restassured.response.Response
import org.json.JSONArray
import org.json.JSONObject
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset
import java.nio.file.Paths

/**
 * Update Registration Tests.
 */
class UpdateRegistrationTests extends FSConnectorTest {

    public List<ConnectorTestConstants.ApiScope> consentScopes = [
            ConnectorTestConstants.ApiScope.ACCOUNTS
    ]
    ClientRegistrationRequestBuilder registrationRequestBuilder
    String ssa

    @BeforeClass
    void generateAccessToken() {
        dcrPath = ConnectorTestConstants.REGISTRATION_ENDPOINT
        registrationRequestBuilder = new ClientRegistrationRequestBuilder()

        ssa = new File(configuration.getAppDCRSSAPath()).text

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa))
                .post(dcrPath)

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, consentScopes)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
    }

    @Test
    void "TC0103002_Update client request with an invalid clientId"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithDefinedRedirectURI(ssa,
                        configuration.getAppDCRAlternateRedirectUri()))
                .put(dcrPath + "invalid_client_id")

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test (priority = 1)
    void "TC0103003_Update client request with an invalid access token"() {

        configuration.setPsuNumber(1)
        def authToken = "${configuration.getUserPSUName()}:" +
                "${configuration.getUserPSUPWD()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest(basicHeader)
                .body(registrationRequestBuilder.getRegularClaimsWithDefinedRedirectURI(ssa,
                        configuration.getAppDCRAlternateRedirectUri()))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test (enabled = false)
    void "TC0103007_Update client request with an invalid redirectURI"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithDefinedRedirectURI(ssa,
                        "invalid_redirect_uri"))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid redirect_uris found in the Request")
    }

    @Test (enabled = false)
    void "TC0103008_Update client request with null value for redirectURI"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithNullRedirectURI(ssa))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid redirect_uris found in the Request")
    }

    @Test
    void "TC0103009_Update client request without redirectURI"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithoutRedirectURI(ssa))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris cannot be empty")
    }

    //TODO: Issue: https://github.com/wso2/financial-services-accelerator/issues/475
    @Test (enabled = false)
    void "Update client request with multiple redirect urls"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithMultipleRedirectURI(ssa,
                        configuration.getAppDCRRedirectUri(), configuration.getAppDCRAlternateRedirectUri()))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        String responseBody = registrationResponse.getBody().asString()
        JSONObject jsonObject = new JSONObject(responseBody)
        JSONArray redirectUris = jsonObject.getJSONArray("redirect_uris")

        Assert.assertTrue(redirectUris.getString(0).equalsIgnoreCase(configuration.getAppDCRRedirectUri()))
        Assert.assertTrue(redirectUris.getString(1).equalsIgnoreCase(configuration.getAppDCRAlternateRedirectUri()))
    }

    @Test (enabled = false)
    void "Update registration request with multiple redirect urls one having invalid url"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithMultipleRedirectURI(ssa,
                        configuration.getAppDCRRedirectUri(), "inavlid_url"))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Redirect URIs do not match with the software statement")
    }

    @Test (enabled = false)
    void "Update registration request with redirectURI having localhost"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithDefinedRedirectURI(ssa,
                        "https://localhost:9446"))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid redirect_uris found in the Request")
    }

    @Test (enabled = false)
    void "Update registration request with redirectURI not matching with the redirect urls in ssa"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithDefinedRedirectURI(ssa,
                        "https://www.google.com/redirects/redirect3"))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Redirect URIs do not match with the software statement")
    }

    @Test
    void "Update registration request with invalid iss claim"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa, "SP1"))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid issuer, issuer should be the same as the software id")
    }

    @Test
    void "Update registration request without iss claim"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        // Remove the "iss" claim
        payload.remove("iss")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter iss not found in the request")
    }

    @Test
    void "Update registration request with an invalid token_endpoint_auth_method"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa,
                        configuration.getAppDCRSoftwareId(), "MTLS"))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid token endpoint authentication method requested.")
    }

    @Test
    void "Update registration request with null value for token_endpoint_auth_method"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa,
                        configuration.getAppDCRSoftwareId(), null))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid token endpoint authentication method requested.")
    }

    @Test
    void "Update registration request without token_endpoint_auth_method"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("token_endpoint_auth_method")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter tokenEndpointAuthMethod not found in the request")
    }

    @Test
    void "Update registration request with an invalid grant_type"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithInvalidGrantTypes(ssa,
                        "invalid_grant_type"))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "Update registration request with null value for grant_type"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithInvalidGrantTypes(ssa, null))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "Update registration request without grant_type"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("grant_types")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter grantTypes cannot be empty")
    }

    @Test
    void "Update registration request without response_types"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("response_types")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "Update registration request without software_id"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("software_id")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "Update registration request without scopes"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("scope")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter scope not found in the request")
    }

    @Test
    void "Update registration request with invalid SSA"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("software_statement", "invalid_ssa")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Signature validation failed for the software statement")
    }

    @Test
    void "Update registration request with null value for SSA"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("software_statement", null)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter softwareStatement not found in the request")
    }

    @Test
    void "Update registration request without SSA"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("software_statement")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter softwareStatement not found in the request")
    }

    @Test
    void "Update registration request without application_type"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("application_type")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter applicationType not found in the request")
    }

    @Test
    void "Update registration request with an invalid application_type"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("application_type", "pc")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid applicationType provided")
    }

    @Test
    void "Update registration request with null value for application_type"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("application_type", "null")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid applicationType provided")
    }

    @Test
    void "Update registration request without id_token_signed_response_alg"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("id_token_signed_response_alg")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter idTokenSignatureAlgorithm not found in the request")
    }

    @Test
    void "Update registration request without request_object_signing_alg"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("request_object_signing_alg")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter requestObjectSignatureAlgorithm not found in the request")
    }

    @Test
    void "Update registration request with an invalid token_endpoint_auth_signing_alg" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("token_endpoint_auth_signing_alg", "mtls")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Update registration request with an null token_endpoint_auth_signing_alg" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("token_endpoint_auth_signing_alg", null)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token endpoint auth signing alg must be specified if token_endpoint_auth_method is private_key_jwt.")
    }

    @Test
    void "Update registration request without token_endpoint_auth_signing_alg for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("token_endpoint_auth_signing_alg")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token endpoint auth signing alg must be specified if token_endpoint_auth_method is private_key_jwt.")
    }

    //TODO: IS issue: https://github.com/wso2/financial-services-accelerator/issues/472
    @Test (enabled = false)
    void "Update registration request without client_name" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("client_name")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
    }

    @Test
    void "Update registration request enabling require_signed_request_object for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("require_signed_request_object", true)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "Update registration request disabling require_signed_request_object for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("require_signed_request_object", false)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Require request object value is incompatible with FAPI requirements")
    }

    @Test
    void "Update registration request without require_signed_request_object for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("require_signed_request_object")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Require request object value is incompatible with FAPI requirements")
    }

    @Test
    void "Update registration request with a software statement having disallowed characters for software_client_name" (){

        String invalidSsaPath = Paths.get(configuration.getTestArtifactLocation(),
                "DynamicClientRegistration", "uk", "tpp1", "ssa_invalidClientName.txt")

        File ssaFile = new File(invalidSsaPath)

        String ssa = TestUtil.getFileContent(ssaFile).toString()

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa))
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        //TODO: Uncomment after fixing the issue: https://github.com/wso2/financial-services-accelerator/issues/480
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
//                "invalid_client_metadata")
    }

    @Test
    void "Update registration request disabling tls_client_certificate_bound_access_tokens for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD))

        payload.put("tls_client_certificate_bound_access_tokens", false)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Certificate bound access tokens is required. 'None' binding type is found.")
    }

    @Test
    void "Update registration request without tls_client_certificate_bound_access_tokens" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("tls_client_certificate_bound_access_tokens")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Certificate bound access tokens is required. 'None' binding type is found.")
    }

    @Test
    void "Update registration request without token_endpoint_allow_reuse_pvt_key_jwt for for private_key_jwt method" (){

        if(!configuration.getIsVersion().equalsIgnoreCase("7.0.0")) {

            JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                    configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD))

            payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

            def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                    .body(payload.toString())
                    .put(dcrPath + clientId)

            Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                    "invalid_client_metadata")
            Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                    "Requested client authentication method incompatible with the Private Key JWT Reuse config value.")
        }
    }

    @Test
    void "Update registration request enabling token_endpoint_allow_reuse_pvt_key_jwt for for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD))

        payload.put("token_endpoint_allow_reuse_pvt_key_jwt", true)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test (priority = 2)
    void "Update registration request with client_name having disallowed characters" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("client_name", "WSO2 Open Banking TPP @@||++(Sandbox)")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Client Name is not adhering to the regex: ^[a-zA-Z0-9._-]+(?: [a-zA-Z0-9._-]+)*\$")
    }

    @Test (priority = 2)
    void "Create tls_client_auth_app"() {

        deleteApplicationIfExist(clientId)

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, consentScopes)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)

    }

    @Test (priority = 2, dependsOnMethods = ["Create tls_client_auth_app"])
    void "Update registration request enabling require_signed_request_object for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("require_signed_request_object", true)
        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test (priority = 2, dependsOnMethods = ["Create tls_client_auth_app"])
    void "Update registration request disabling require_signed_request_object for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("require_signed_request_object", false)
        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Require request object value is incompatible with FAPI requirements")
    }

    @Test (priority = 2, dependsOnMethods = ["Create tls_client_auth_app"])
    void "Update registration request without token_endpoint_allow_reuse_pvt_key_jwt for for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test (priority = 2, dependsOnMethods = ["Create tls_client_auth_app"])
    void "Update registration request disabling tls_client_certificate_bound_access_tokens tls_client_auth" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("tls_client_certificate_bound_access_tokens", false)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
    }

    @Test (priority = 2, dependsOnMethods = ["Create tls_client_auth_app"])
    void "Update registration request without token_endpoint_auth_signing_alg for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.remove("token_endpoint_auth_signing_alg")
        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @AfterClass
    void deleteApplication() {

        deleteApplicationIfExist(clientId)
    }

    /**
     * Delete Application if exist.
     * @param clientId
     */
    void deleteApplicationIfExist(String clientId) {

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, consentScopes)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .delete(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)
    }
}