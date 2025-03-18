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

package org.wso2.financial.services.accelerator.is.test.consent.management.AccountsApiTests

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Initiation Request Header Validation Tests.
 */
class InitiationRequestHeaderValidationTests extends FSConnectorTest{

    String basicHeader

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        basicHeader = getBasicAuthHeader(configuration.getUserKeyManagerAdminName(),
                configuration.getUserKeyManagerAdminPWD())
    }

    @Test
    void "Initiation Request With Authorization Code Type Access Token"() {

        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

        doDefaultInitiation()
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        String userAccessToken = getUserAccessToken(code, scopeList)

        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION),
                "AuthenticationHandler not found.")
    }

    @Test
    void "Initiation Request With x-fapi-financial-id Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Initiation Request With empty x-fapi-interaction-id Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, "")
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)

        def consentStatus = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
        Assert.assertEquals(consentStatus, "AwaitingAuthorisation")
    }

    @Test
    void "Initiation Request With TPP Defined x-fapi-interaction-id Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)

        def consentStatus = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
        Assert.assertEquals(consentStatus, "AwaitingAuthorisation")
    }

    @Test
    void "Initiation Request Without Authorization Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION),
                "AuthenticationHandler not found.")
    }

    @Test
    void "Initiation Request With Invalid Authorization Header"() {

        def authHeader = getBasicAuthHeader(configuration.getUserPSUName(),
                configuration.getUserPSUPWD())

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, authHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_DESCRIPTION)
        Assert.assertEquals(errorMessage, "AuthenticationHandler not found.")
    }

    @Test
    void "Initiation Request Without Content Type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
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
    void "Initiation Request With Invalid Content Type"() {

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
    void "Initiation Request With Invalid Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_406)
    }

    @Test
    void "Initiation Request With Headers with Capital Case"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID_CAPS, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID_CAPS, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.X_FAPI_CUSTOMER_LAST_LOGGED_TIME_CAPS,
                        ConnectorTestConstants.X_FAPI_CUSTOMER_LAST_LOGGED_TIME_VALUE)
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Initiation Request With x-fapi-customer-last_logged_time Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.X_FAPI_CUSTOMER_LAST_LOGGED_TIME_CAPS,
                        ConnectorTestConstants.X_FAPI_CUSTOMER_LAST_LOGGED_TIME_VALUE)
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Initiation Request With x-fapi-auth-date Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.X_FAPI_AUTH_DATE, ConnectorTestConstants.X_FAPI_AUTH_DATE_VALUE)
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }
}
