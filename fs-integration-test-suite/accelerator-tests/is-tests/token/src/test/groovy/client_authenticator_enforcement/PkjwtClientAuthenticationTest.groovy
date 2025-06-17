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

package client_authenticator_enforcement

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * PKJWT Client AUthentication Validation Test.
 */
class PkjwtClientAuthenticationTest extends FSConnectorTest {

    String clientId
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()
    ClientRegistrationRequestBuilder registrationRequestBuilder

    @BeforeClass
    void setup() {
        //Create Regulatory Application with tls_client_auth method
        clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD)
    }

    @Test (priority = 0)
    void "Validate token request with deleted client_id for iss and sub in client_assertion"() {

        //Token Request for the deleted client
        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                "deleted_client_id", [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)

        String actualErrorResponse = TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION)

        boolean isValidResponse = actualErrorResponse.equals("Client credentials are invalid.")
                || actualErrorResponse.equals("A valid OAuth client could not be found for client_id: deleted_client_id")

        Assert.assertTrue(isValidResponse)
    }

    @Test
    void "Validate token request with expired client assertion"() {

        long expireTime = (long) (System.currentTimeSeconds()).minus(600000000)

        Response tokenResponse = getApplicationAccessTokenResponseWithCustomExp(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, [scope], expireTime)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                "invalid_request")
        Assert.assertTrue(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION).
                contains("JWT Token is expired."))
    }

    @Test
    void "Validate token request for pkjwt client without client_assertion"() {

        Response tokenResponse = getApplicationAccessTokenResponseWithoutAssertion(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Unsupported client authentication mechanism")
    }

    @Test
    void "Validate token request for pkjwt client without client_id"() {

        Response tokenResponse = getApplicationAccessTokenResponseWithoutClientId(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Client ID not found in the request.")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
    }

    @Test
    void "pkjwt client authentication with both x-wso2-mutual-auth-cert header and client_assertion"() {

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, [scope])

        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "expires_in"))
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    @Test
    void "pkjwt client authentication with x-wso2-mutual-auth-cert header, client id and client_assertion and client assertion type"() {

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, [scope])

        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "expires_in"))
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    @AfterClass
    void cleanup() {
        //Delete the application created for the test
        deleteApplication(clientId, ConnectorTestConstants.PKJWT_AUTH_METHOD)
    }
}
