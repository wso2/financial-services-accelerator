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
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Mutual TLS Client AUthentication Validation Test.
 */
class MtlsClientAuthenticationTest extends FSConnectorTest {

    String clientId
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    @BeforeClass
    void init() {
        //Create Regulatory Application with tls_client_auth method
        clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD)
    }

    //TODO: Enable the methods in the class after fixing IS server issues.
    @Test(enabled = false)
    void "MTLS Client Authentication with mtls header, client id, client_assertion and client assertion type"(){

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Request does not follow the registered token endpoint auth method tls_client_auth")
    }

    @Test
    void "Validate token request for client with tls_client_auth method when sending request without client id"() {

        Response tokenResponse = getApplicationAccessTokenResponseWithoutClientId(ConnectorTestConstants.TLS_AUTH_METHOD,
                [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Client ID not found in the request.")
    }

    @Test(enabled = false)
    void "Validate token request for tls client with only client_assertion"() {

        Response tokenResponse = getApplicationAccessTokenTLSWithAssertion([scope], clientId)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Request does not follow the registered token endpoint auth method tls_client_auth")
    }

    @Test(enabled = false)
    void "MTLS Client Authentication with both x-wso2-mutual-auth-cert header and client_assertion"() {

        Response tokenResponse = getApplicationAccessTokenResponseWithCertAndAssertion(ConnectorTestConstants.TLS_AUTH_METHOD,
                [scope], clientId)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Request does not follow the registered token endpoint auth method tls_client_auth")
    }

    @Test(enabled = false)
    void "MTLS Client Authentication with both x-wso2-mutual-auth-cert header and client_assertion_type"() {

        Response tokenResponse = getApplicationAccessTokenResponseWithoutAssertion(ConnectorTestConstants.TLS_AUTH_METHOD,
                clientId, [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Unsupported client authentication mechanism")
    }

    @Test
    void "MTLS Client Authentication when sending request without client id"(){

        Response tokenResponse = getApplicationAccessTokenResponseWithoutClientId(ConnectorTestConstants.TLS_AUTH_METHOD,
                [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Client ID not found in the request.")
    }

    @AfterClass
    void cleanup() {
        //Delete the application created for the test
        deleteApplication(clientId, ConnectorTestConstants.TLS_AUTH_METHOD)
    }

}
