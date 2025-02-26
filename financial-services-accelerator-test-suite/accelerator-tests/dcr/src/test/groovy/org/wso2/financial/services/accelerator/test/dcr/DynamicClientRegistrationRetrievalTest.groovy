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

package org.wso2.financial.services.accelerator.test.dcr

import io.restassured.RestAssured
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.dcr.util.DCRConstants
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Dynamic Client Registration retrieval Flow Tests.
 */
class DynamicClientRegistrationRetrievalTest extends FSConnectorTest {

    ConfigurationService configuration = new ConfigurationService()
    private String accessToken
    private String clientId
    private String configClientId
    private List<ConnectorTestConstants.ApiScope> scopes = [ConnectorTestConstants.ApiScope.ACCOUNTS]
    private String registrationPath
    String SSA
    private String invalidClientId = "invalid_client_id"
    private String invalidAccessToken = "invalid_token"

    @BeforeClass(alwaysRun = true)
    void setup() {

        configClientId = configuration.getAppInfoClientID(0)
        registrationPath = configuration.getServerBaseURL() + DCRConstants.REGISTRATION_ENDPOINT
        configuration.setTppNumber(1)
        SSA = new File(configuration.getAppDCRSSAPath()).text

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA))
                .when()
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        accessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, scopes)
        Assert.assertNotNull(accessToken)
    }

    @Test
    void "Retrieve registration details with invalid clientId"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForGetAndDelete(accessToken)
                .get(registrationPath + "/" + invalidClientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token is not bound to the client id sent in the request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "unauthorized_request")
    }

    @Test
    void "Retrieve registration details with an invalid access token"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForGetAndDelete(invalidAccessToken)
                .get(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.DESCRIPTION),
                "Access failure for API: /open-banking/v3.3.0, version: v3.3.0 status: (900901) - Invalid " +
                        "Credentials. Make sure you have provided the correct security credentials")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_MSG),
                "Invalid Credentials")
    }

    @Test
    void "Retrieve registration details with a clientId that does not match with the access token"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForGetAndDelete(accessToken)
                .get(registrationPath + "/" + configClientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token is not bound to the client id sent in the request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "unauthorized_request")
    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForGetAndDelete(accessToken)
                .delete(registrationPath + "/" + clientId)
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)
    }
}
