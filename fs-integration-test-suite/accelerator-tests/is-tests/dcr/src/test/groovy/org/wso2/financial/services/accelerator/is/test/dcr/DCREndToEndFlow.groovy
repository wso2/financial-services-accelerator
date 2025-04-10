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

package org.wso2.financial.services.accelerator.is.test.dcr

import io.restassured.response.Response
import org.json.JSONObject
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Dynamic Client Registration End to End Flow Tests.
 */
class DCREndToEndFlow extends FSConnectorTest {

    ConfigurationService configuration = new ConfigurationService()
    private String accessToken
    private String clientId
    private List<ConnectorTestConstants.ApiScope> consentScopes = [ConnectorTestConstants.ApiScope.ACCOUNTS]
    private String registrationPath
    String ssa
    ClientRegistrationRequestBuilder registrationRequestBuilder

    @BeforeClass(alwaysRun = true)
    void setup() {
        configuration.setTppNumber(1)

        dcrPath = configuration.getISServerUrl() + ConnectorTestConstants.REGISTRATION_ENDPOINT
        configuration.setTppNumber(1)
        ssa = new File(configuration.getAppDCRSSAPath()).text
        registrationRequestBuilder = new ClientRegistrationRequestBuilder()
    }




    @Test(groups = "SmokeTest")
    void "Invoke registration request structured as a JWS"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa))
                .post(dcrPath)

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"client_id"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"client_secret"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"client_secret_expires_at"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"redirect_uris"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"token_endpoint_auth_method"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"grant_types"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"scope"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"software_statement"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"application_type"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"id_token_signed_response_alg"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"request_object_signing_alg"))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Invoke registration request structured as a JWS")
    void "Get access token"() {

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, consentScopes)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "Get access token")
    void "Retrieve registration details with a valid clientId and access token"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildKeyManagerRegistrationRequest()
                .get(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"client_id"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"redirect_uris"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"token_endpoint_auth_method"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"grant_types"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"scope"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"software_statement"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"application_type"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"id_token_signed_response_alg"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"request_object_signing_alg"))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Retrieve registration details with a valid clientId and access token")
    void "Update client request with a valid details"() {

        def ssa = new File(configuration.getAppDCRSSAPath()).text

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa))
        payload.put("scope", "fundsconfirmations")

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .put(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"client_id"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"redirect_uris"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"token_endpoint_auth_method"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"grant_types"))
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse,"scope"), "fundsconfirmations")
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"software_statement"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"application_type"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"id_token_signed_response_alg"))
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"request_object_signing_alg"))
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "Update client request with a valid details")
    void "Delete client with a valid clientId and access token"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildKeyManagerRegistrationRequest()
                .delete(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)
    }
}
