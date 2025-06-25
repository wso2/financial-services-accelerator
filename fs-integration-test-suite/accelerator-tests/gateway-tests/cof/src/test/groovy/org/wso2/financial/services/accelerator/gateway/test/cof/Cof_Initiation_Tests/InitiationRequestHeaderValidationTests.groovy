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

package org.wso2.financial.services.accelerator.gateway.test.cof.Cof_Initiation_Tests

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.CofRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Funds Confirmation Initiation Request Header Validation Tests.
 */
class InitiationRequestHeaderValidationTests extends FSAPIMConnectorTest {

    def basicHeader
    public List<ConnectorTestConstants.ApiScope> scope = [
            ConnectorTestConstants.ApiScope.COF
    ]

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.COF_CONSENT_API_PATH
        initiationPayload = CofRequestPayloads.cofInitiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.COF_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Funds Confirmation Initiation with invalid content-type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.XML)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)

        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_0_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Request Content-Type header does not match any allowed types"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_0_CODE),
                "200012")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_0_MSG),
                "Request Content-Type header does not match any allowed types")
    }

    @Test
    void "Funds Confirmation Initiation with Headers with Capital Case"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID_CAPS, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID_CAPS, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Funds Confirmation Initiation with an access token of authorization code type"() {

        doDefaultInitiation()
        doCofAuthorization(scopeList)
        Assert.assertNotNull(userAccessToken)

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Incorrect Access Token Type provided"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.CODE),
                "200001")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                "Access failure for API: grant type validation failed.")
    }

    @Test
    void "Funds Confirmation Initiation Without Authorization Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Make sure your API invocation call has a header: 'Authorization"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.CODE),
                "900902")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.MISSING_CREDENTIALS_ERROR)
    }

    @Test
    void "Funds Confirmation Initiation With Invalid Authorization Header"() {

        def accessToken = TestUtil.getBasicAuthHeader(configuration.getUserPSUName(),
                configuration.getUserPSUPWD())

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${accessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/cbpii, version: v3.1 status: (900901)"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.INVALID_CREDENTIALS_ERROR)
    }

    @Test
    void "Initiation Request With Invalid Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.TEXT)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("HTTP 406 Not Acceptable"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.STATUS_CODE_406.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
    }
}
