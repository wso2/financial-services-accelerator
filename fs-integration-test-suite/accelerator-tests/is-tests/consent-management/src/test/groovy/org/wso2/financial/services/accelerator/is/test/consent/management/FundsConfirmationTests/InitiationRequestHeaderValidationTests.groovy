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

package org.wso2.financial.services.accelerator.is.test.consent.management.FundsConfirmationTests

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.CofRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Funds Confirmation Initiation Request Header Validation Tests.
 */
class InitiationRequestHeaderValidationTests extends FSConnectorTest {

    def basicHeader
    public List<ConnectorTestConstants.ApiScope> scope = [
            ConnectorTestConstants.ApiScope.COF
    ]

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.COF_CONSENT_PATH
        initiationPayload = CofRequestPayloads.cofInitiationPayload
        basicHeader = getBasicAuthHeader(configuration.getUserKeyManagerAdminName(),
                configuration.getUserKeyManagerAdminPWD())
    }

    @Test
    void "Funds Confirmation Initiation with invalid content-type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.XML)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Funds Confirmation Initiation with Headers with Capital Case"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID_CAPS, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID_CAPS, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Funds Confirmation Initiation with an access token of authorization code type"() {

        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.COF_TYPE)

        doDefaultInitiation()
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scope)
        String userAccessToken = getUserAccessToken(code, scopeList)

        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
        Assert.assertEquals(errorMessage, "AuthenticationHandler not found.")
    }

    @Test
    void "Funds Confirmation Initiation Without Authorization Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
        Assert.assertEquals(errorMessage, "AuthenticationHandler not found.")
    }

    @Test
    void "Funds Confirmation Initiation With Invalid Authorization Header"() {

        def accessToken = getBasicAuthHeader(configuration.getUserPSUName(),
                configuration.getUserPSUPWD())

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, accessToken)
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
        Assert.assertEquals(errorMessage, "Operation is not permitted. You do not have permissions to make this request.")
    }

    @Test
    void "Initiation Request With Invalid Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_406)
    }
}
