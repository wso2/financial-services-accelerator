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

package grant_type_validation

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Client Credential Grant Access Token Test.
 */
class ClientCredentialGrantAccessTokenTest extends FSConnectorTest {

    String clientId
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    @BeforeClass
    void setup() {
        //Create Regulatory Application with tls_client_auth method
        clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD)
    }

    @Test
    void "Generate client credential grant access token for deleted client"() {

        //Token Request for the deleted client
        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                "deleted_client_id", [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
        Assert.assertTrue(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION).contains(
                "Client credentials are invalid"))
    }

    @Test
    void "Validate token request for client with token_endpoint_auth_method private_key_jwt"() {

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    @Test (dependsOnMethods = "Validate token request for client with token_endpoint_auth_method private_key_jwt")
    void "Validate keyId of application access token jwt"() {

        HashMap<String, String> mapHeader = TestUtil.getJwtTokenHeader(accessToken)

        Assert.assertNotNull(mapHeader.get(ConnectorTestConstants.KID))
    }

    @Test (dependsOnMethods = "Validate token request for client with token_endpoint_auth_method private_key_jwt")
    void "Validate additional claim binding to the application access token jwt"() {

        HashMap<String, String> mapPayload = TestUtil.getJwtTokenPayload(accessToken)

        Assert.assertTrue(mapPayload.get(ConnectorTestConstants.CNF).matches("x5t#S256:[a-zA-Z0-9-]+"))
    }

    @Test (dependsOnMethods = "Validate additional claim binding to the application access token jwt")
    void "Introspection call for application access token"() {

        Response tokenResponse = getTokenIntrospectionResponse(accessToken)
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "active"), "true")
        Assert.assertNull(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.GRANT_TYPE))
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.CNF))

        deleteApplication(clientId, ConnectorTestConstants.PKJWT_AUTH_METHOD)
    }

    @Test (priority = 1)
    void "Validate token request for client with token_endpoint_auth_method tls_client_auth"() {

        clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD)

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD,
                clientId, [scope])

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)

        deleteApplication(clientId, ConnectorTestConstants.TLS_AUTH_METHOD)
    }
}
