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
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.file.Paths


/**
 * Dynamic Client Registration End to End Flow Tests.
 */
class ClientRegistrationTests extends FSConnectorTest {

    String ssa
    private String accessToken
    private String clientId
    ClientRegistrationRequestBuilder registrationRequestBuilder
    public List<ConnectorTestConstants.ApiScope> consentScopes = [
            ConnectorTestConstants.ApiScope.ACCOUNTS
    ]

    @BeforeClass(alwaysRun = true)
    void setup() {

        dcrPath = configuration.getISServerUrl() + ConnectorTestConstants.REGISTRATION_ENDPOINT
        ssa = new File(configuration.getAppDCRSSAPath()).text
        registrationRequestBuilder = new ClientRegistrationRequestBuilder()
    }

    @Test
    void "TC0101003_Invoke registration request with invalid redirectURI"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithDefinedRedirectURI(ssa,
                        "invalid_redirect_uri"))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid redirect_uris found in the Request")
    }

    @Test
    void "TC0101004_Invoke registration request with null value for redirectURI"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithNullRedirectURI(ssa))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris not found in the request")
    }

    @Test
    void "TC0101005_Invoke registration request without redirectURI"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithoutRedirectURI(ssa))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris cannot be empty")
    }

    //TODO: Issue: https://github.com/wso2/financial-services-accelerator/issues/475
    @Test (enabled = false)
    void "Invoke registration request structured with multiple redirect urls"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithMultipleRedirectURI(ssa,
                        configuration.getAppDCRRedirectUri(), configuration.getAppDCRAlternateRedirectUri()))
                .post(dcrPath)

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(clientId)

        String responseBody = registrationResponse.getBody().asString()
        JSONObject jsonObject = new JSONObject(responseBody)
        JSONArray redirectUris = jsonObject.getJSONArray("redirect_uris")

        Assert.assertTrue(redirectUris.getString(0).equalsIgnoreCase(configuration.getAppDCRRedirectUri()))
        Assert.assertTrue(redirectUris.getString(1).equalsIgnoreCase(configuration.getAppDCRAlternateRedirectUri()))

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request structured with multiple redirect urls one having invalid url"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithMultipleRedirectURI(ssa,
                        configuration.getAppDCRRedirectUri(), "https://inavlid_url"))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Redirect URIs do not match with the software statement")
    }

    @Test
    void "Invoke registration request with redirectURI having localhost"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithDefinedRedirectURI(ssa,
                        "https://localhost:9446"))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid redirect_uris found in the Request")
    }

    @Test
    void "Invoke registration request with redirectURI not matching with the redirect urls in ssa"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithDefinedRedirectURI(ssa,
                        "https://www.google.com/redirects/redirect3"))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Redirect URIs do not match with the software statement")
    }

    @Test
    void "TC0101025_Invoke registration request with invalid iss claim"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa, "SP1"))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid issuer, issuer should be the same as the software id")
    }

    @Test
    void "Invoke registration request without iss claim"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        // Remove the "iss" claim
        payload.remove("iss")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter iss not found in the request")
    }

    @Test
    void "Invoke registration request with an invalid token_endpoint_auth_method"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa,
                        configuration.getAppDCRSoftwareId(), "MTLS"))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid token endpoint authentication method requested.")
    }

    @Test
    void "Invoke registration request with token_endpoint_auth_method priate_key_jwt"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request with token_endpoint_auth_method tls_client_auth"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request with null value for token_endpoint_auth_method"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa,
                        configuration.getAppDCRSoftwareId(), null))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid token endpoint authentication method requested.")
    }

    @Test
    void "Invoke registration request without token_endpoint_auth_method"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.remove("token_endpoint_auth_method")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter tokenEndpointAuthMethod not found in the request")
    }

    @Test
    void "Invoke registration request with an invalid grant_type"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithInvalidGrantTypes(ssa,
                        "invalid_grant_type"))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "Invoke registration request with null value for grant_type"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaimsWithInvalidGrantTypes(ssa,
                        null))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "Invoke registration request without grant_type"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.remove("grant_types")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter grantTypes cannot be empty")
    }

    @Test
    void "Invoke registration request with an invalid response_types"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.put("response_types", "token")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request with an null response_types"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.put("response_types", "null")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        //Accelerator does not have response_types validation support
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request without response_types"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("response_types")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request without software_id"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.remove("software_id")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request without scopes"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.remove("scope")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter scope not found in the request")
    }

    @Test
    void "TC0101012_Invoke registration request with invalid SSA"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.put("software_statement", "invalid_ssa")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Signature validation failed for the software statement")
    }

    @Test
    void "TC0101014_Invoke registration request with null value for SSA"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.put("software_statement", null)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter softwareStatement not found in the request")
    }

    @Test
    void "TC0101015_Invoke registration request without SSA"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.remove("software_statement")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter softwareStatement not found in the request")
    }

    @Test
    void "TC0101021_Invoke registration request without application_type"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.remove("application_type")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter applicationType not found in the request")
    }

    @Test
    void "TC0101019_Invoke registration request with an invalid application_type"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.put("application_type", "pc")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid applicationType provided")
    }

    @Test
    void "TC0101020_Invoke registration request with null value for application_type"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.put("application_type", null)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter applicationType not found in the request")
    }

    @Test
    void "TC0101018_Invoke registration request without id_token_signed_response_alg"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.remove("id_token_signed_response_alg")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter idTokenSignatureAlgorithm not found in the request")
    }

    @Test
    void "TC0101024_Invoke registration request without request_object_signing_alg"() {

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.remove("request_object_signing_alg")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter requestObjectSignatureAlgorithm not found in the request")
    }

    @Test
    void "TC0101026_Invoke registration request with an invalid token_endpoint_auth_signing_alg" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.put("token_endpoint_auth_signing_alg", "mtls")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "TC0101027_Invoke registration request with an null token_endpoint_auth_signing_alg" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(
                ssa))

        payload.put("token_endpoint_auth_signing_alg", null)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token endpoint auth signing alg must be specified if token_endpoint_auth_method is private_key_jwt.")
    }

    @Test
    void "TC0101028_Invoke registration request without token_endpoint_auth_signing_alg for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.remove("token_endpoint_auth_signing_alg")
        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "TC0101028_Invoke registration request without token_endpoint_auth_signing_alg for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.remove("token_endpoint_auth_signing_alg")
        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "TC0101029_Invoke registration request with a software statement having disallowed characters for software_client_name" (){

        String invalidSsaPath = Paths.get(configuration.getTestArtifactLocation(),
                "DynamicClientRegistration", "uk", "tpp1", "ssa_invalidClientName.txt")

        File ssaFile = new File(invalidSsaPath)

        String ssa = TestUtil.getFileContent(ssaFile).toString().trim()

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    //TODO: IS issue: https://github.com/wso2/financial-services-accelerator/issues/472
    @Test (enabled = false)
    void "Invoke registration request without client_name" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("client_name")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
    }

    @Test
    void "Invoke registration request with token_type_extension not match with SSA" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("token_type_extension", "JSON")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request enabling require_signed_request_object for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("require_signed_request_object", true)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request disabling require_signed_request_object for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.put("require_signed_request_object", false)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Require request object value is incompatible with FAPI requirements")
    }

    @Test
    void "Invoke registration request enabling require_signed_request_object for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("require_signed_request_object", true)
        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request disabling require_signed_request_object for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("require_signed_request_object", false)
        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Require request object value is incompatible with FAPI requirements")
    }

    @Test
    void "Invoke registration request without require_signed_request_object for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("require_signed_request_object")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Require request object value is incompatible with FAPI requirements")
    }

    @Test
    void "Invoke registration request disabling tls_client_certificate_bound_access_tokens for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD))

        payload.put("tls_client_certificate_bound_access_tokens", false)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Certificate bound access tokens is required. 'None' binding type is found.")
    }

    @Test
    void "Invoke registration request disabling tls_client_certificate_bound_access_tokens tls_client_auth" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("tls_client_certificate_bound_access_tokens", false)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Certificate bound access tokens is required. 'None' binding type is found.")
    }

    @Test
    void "Invoke registration request without tls_client_certificate_bound_access_tokens" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("tls_client_certificate_bound_access_tokens")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Certificate bound access tokens is required. 'None' binding type is found.")
    }

    @Test
    void "Invoke registration request without token_endpoint_allow_reuse_pvt_key_jwt for for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD))

        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Requested client authentication method incompatible with the Private Key JWT Reuse config value.")
    }

    @Test
    void "Invoke registration request enabling token_endpoint_allow_reuse_pvt_key_jwt for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD))

        payload.put("token_endpoint_allow_reuse_pvt_key_jwt", true)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        //Get Application Access Token
        def jti = String.valueOf(System.currentTimeMillis())
        Response tokenResponse = TokenRequestBuilder.getApplicationAccessTokenResponseWithDefinedJti(consentScopes,
                clientId, jti)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)

        //Get Application Access Token with same jti
        Response newTokenResponse = TokenRequestBuilder.getApplicationAccessTokenResponseWithDefinedJti(consentScopes,
                clientId, jti)

        accessToken = TestUtil.parseResponseBody(newTokenResponse, "access_token")
        Assert.assertEquals(newTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)

        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request disabling token_endpoint_allow_reuse_pvt_key_jwt for private_key_jwt method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD))

        payload.put("token_endpoint_allow_reuse_pvt_key_jwt", false)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        //Get Application Access Token
        def jti = String.valueOf(System.currentTimeMillis())
        Response tokenResponse = TokenRequestBuilder.getApplicationAccessTokenResponseWithDefinedJti(consentScopes,
                clientId, jti)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)

        //Get Application Access Token with same jti
        Response newTokenResponse = TokenRequestBuilder.getApplicationAccessTokenResponseWithDefinedJti(consentScopes,
                clientId, jti)

        Assert.assertEquals(newTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(newTokenResponse, ConnectorTestConstants.ERROR),
                "invalid_request")
        Assert.assertEquals(TestUtil.parseResponseBody(newTokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "JWT Token with jti: " + jti +" has been replayed")

        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request without token_endpoint_allow_reuse_pvt_key_jwt for for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        deleteApplicationIfExist(clientId)
    }

    @Test
    void "Invoke registration request enabling token_endpoint_allow_reuse_pvt_key_jwt for for tls_client_auth method" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("token_endpoint_allow_reuse_pvt_key_jwt", true)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Requested client authentication method incompatible with the Private Key JWT Reuse config value.")
    }

    @Test
    void "Invoke registration request with client_name having disallowed characters" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa,
                configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD))

        payload.put("client_name", "WSO2 Open Banking TPP @@||++(Sandbox)")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Client Name is not adhering to the regex: ^[a-zA-Z0-9._-]+(?: [a-zA-Z0-9._-]+)*\$")
    }

    @Test
    void "Invoke registration request without jwks_uri" (){

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))

        payload.remove("jwks_uri")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        //Update the application with the jwks_uri
        payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))
        payload.put("jwks_uri", "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/${configuration.getAppDCRSoftwareId()}.jwks")

        def updateRegistrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        deleteApplicationIfExist(clientId)
    }

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
