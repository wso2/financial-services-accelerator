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

package org.wso2.financial.services.accelerator.gateway.non.regulatory.scenarios

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Non-Regulatory API and Non-Regulatory Application Test Class.
 */
class NonRegulatoryApiAndNonRegulatoryApplication extends FSAPIMConnectorTest {

    def orderId
    String clientSecret

    @BeforeClass
    void init() {

        clientId = configuration.getNonRegulatoryAppClientID()
        clientSecret = configuration.getNonRegulatoryAppClientSecret()
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.OPENID)

        //Get application access token
        applicationAccessToken = getApplicationAccessTokenForNonRegulatoryApp(clientId, clientSecret, scopeList)
    }

    @Test
    void "Invoke Get endpoint in Non-Regulatory API"() {

        applicationAccessToken = getApplicationAccessTokenForNonRegulatoryApp(clientId, clientSecret, scopeList)

        def response = FSRestAsRequestBuilder.buildBasicRequest()
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .baseUri(configuration.getServerBaseURL())
                .get(ConnectorTestConstants.PIZZA_SHACK_MENU_PATH)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(response, "name"))
        Assert.assertNotNull(TestUtil.parseResponseBody(response, "description"))

    }

    @Test
    void "Invoke Post endpoint in Non-Regulatory API"() {

        def response = FSRestAsRequestBuilder.buildBasicRequest()
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .body(RequestPayloads.getPizzaShackOrderPayload())
                .baseUri(configuration.getServerBaseURL())
                .post(ConnectorTestConstants.PIZZA_SHACK_ORDER_PATH)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        orderId = TestUtil.parseResponseBody(response, "orderId")
        Assert.assertNotNull(orderId)
    }

    @Test (dependsOnMethods = "Invoke Post endpoint in Non-Regulatory API")
    void "Invoke Delete endpoint in Non-Regulatory API"() {

        def response = FSRestAsRequestBuilder.buildBasicRequest()
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .body(RequestPayloads.getPizzaShackOrderPayload())
                .baseUri(configuration.getServerBaseURL())
                .delete(ConnectorTestConstants.PIZZA_SHACK_ORDER_PATH + "/${orderId}")

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "Invoke Non-Regulatory API via Regulatory Application"() {

        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.OPENID)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)

        def response = FSRestAsRequestBuilder.buildBasicRequest()
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .baseUri(configuration.getServerBaseURL())
                .get(ConnectorTestConstants.PIZZA_SHACK_MENU_PATH)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(response, "name"))
        Assert.assertNotNull(TestUtil.parseResponseBody(response, "description"))

    }

    @Test
    void "Invoke Regulatory Api via Non-Regulatory Application"() {

        consentPath = ConnectorTestConstants.AISP_PATH + "account-access-consents"
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.OPENID)

        applicationAccessToken = getApplicationAccessTokenForNonRegulatoryApp(clientId, clientSecret, scopeList)

        doDefaultAccountInitiation()
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "The access token does not allow you to access the requested resource")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "User is NOT authorized to access the Resource: /account-access-consents. Scope validation failed.")
    }
}
