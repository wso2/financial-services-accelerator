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

package org.wso2.financial.services.accelerator.is.test.consent.management.ConsentRetrieval

import io.restassured.http.ContentType
import io.restassured.response.Response
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
 * Consent Retrieval  class
 */
class ConsentRetrievalFlow extends FSConnectorTest {

    //Consent scopes as a list of Strings
    private List<String> consentScopesString = [
            ConnectorTestConstants.ApiScope.ACCOUNTS.getScopeString(),
    ]

    @BeforeClass(alwaysRun = true)
    void init(){
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
    }

    @Test
    void "Verify Retrieval of a Created Consent"() {

        doDefaultInitiation()
        doConsentRetrieval(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "Verify Retrieval of a Created Consent without consent ID"() {

        doDefaultInitiation()
        doConsentRetrievalWithoutConsentID()
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Retrieval of a Created Consent without Authorization header"() {

        doDefaultInitiation()
        doConsentRetrievalWithoutAuthorizationHeader(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Get Accounts Initiation With Invalid Authorization Header"() {

        def basicHeader = getBasicAuthHeader(configuration.getUserPSUName(),
                configuration.getUserPSUPWD())

        doDefaultInitiation()
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, "${configuration.getAppInfoClientID()}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_403)
    }

    @Test
    void "Verify Retrieval of a Created Consent with Incorrect request path"() {

        doDefaultInitiation()
        doConsentRetrievalWithIncorrectRequestPath(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Verify Retrieval of a Created Consent with Authorization Code Type Access"() {

        doDefaultInitiation()
        Assert.assertNotNull(consentId)

        //Authorize consent and generate user access token
        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils
                .getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)
        Response response = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code, scopeList)

        def accessToken = TestUtil.parseResponseBody(response, "access_token")

        consentResponse = consentRequestBuilder.buildBasicRequest(accessToken)
                .baseUri(configuration.getISServerUrl())
                .get(incorrectConsentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Verify Retrieval of a Created Consent with incorrect Consent ID"() {

        String incorrectConsentID = 'c1b6c5c9-1ec9-4ccf-8f68-e18df87777bfaaa'

        doDefaultInitiation()
        doConsentRetrieval(incorrectConsentID)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Retrieval of a Created Consent with different search params"() {

        doDefaultInitiation()
        doConsentRetrievalWithDifferentSearchParams(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Verify Retrieval of a consent with valid inputs"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        doConsentRetrieval(consentId as String)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        String status = TestUtil.parseResponseBody(consentResponse, "Data.Status")
        Assert.assertEquals(status, "AwaitingAuthorisation")
    }

    @Test
    void "Verify Retrieval of a consent After Authorisation"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        doConsentAuthorisation(configuration.getAppInfoClientID(), true, [ConnectorTestConstants.ApiScope.ACCOUNTS])

        doConsentRetrieval(consentId as String)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        String status = TestUtil.parseResponseBody(consentResponse, "Data.Status")
        Assert.assertEquals(status, "Authorised")
    }

    @Test
    void "Verify Consent Retrieval without client ID"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = buildKeyManagerRequest("")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Retrieval invalid client ID"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = buildKeyManagerRequest("tyionwbbvqhhwvh")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Retrieval without consent ID"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = buildKeyManagerRequest("")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Retrieval with invalid consent ID"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = buildKeyManagerRequest("")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/12345678")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Retrieval invalid consent type"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .get(ConnectorTestConstants.COF_CONSENT_PATH + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Retrieval without Authorisation header"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = buildKeyManagerRequestWithoutAuthorizationHeader(configuration.getAppInfoClientID())
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Verify Consent Retrieval with incorrect Access Token type"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        doConsentRetrievalWithIncorrectAccessTokenType()

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Verify Consent Retrieval with incorrect request path"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        doConsentRetrievalWithIncorrectRequestPath(consentId as String)

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Get Accounts Initiation With Headers with Capital Case"() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload

        def basicHeader = getBasicAuthHeader(configuration.getUserKeyManagerAdminName(),
                configuration.getUserKeyManagerAdminPWD())

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID_CAPS, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID_CAPS, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        String status = TestUtil.parseResponseBody(consentResponse, "Data.Status")
        Assert.assertEquals(status, "AwaitingAuthorisation")
    }
}
