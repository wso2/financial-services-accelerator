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

package org.wso2.financial.services.accelerator.gateway.test.accounts.Accounts_Retrieval_Tests

import groovy.json.JsonOutput
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.AccountsDataProviders
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil


import java.time.OffsetDateTime

/**
 * Accounts Flow Retrieval Tests.
 */
class AccountsRetrievalRequestBodyValidationTests extends FSAPIMConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test(dataProvider = "AccountsResources", dataProviderClass = AccountsDataProviders.class)
    void "OBA-794_Account retrieval request with valid consent id"(String resource) {

        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .baseUri(configuration.getServerBaseURL())
                .get(resource)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test(dataProvider = "AccountsResourcesWithoutPermissions", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Retrieval without required permission Permission"(String[] params) {

        def resource = params[0]
        def payload = params[1]
        Response consentResponse = doDefaultInitiationWithUpdatedPayload(payload)
        consentId = TestUtil.parseResponseBody(consentResponse, "Data.ConsentId")

        doAccountConsentAuthorisation()
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .baseUri(configuration.getServerBaseURL())
                .get(resource)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
//        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
//        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Consent Enforcement Error")
    }

    @Test
    void "OBA-792_Account Retrieval Request with expired consent id"() {

        OffsetDateTime updatedExpirationInstant = OffsetDateTime.now().plusMinutes(1)

        def updatedPayload = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(AccountsRequestPayloads.permissionsArray)},
                "ExpirationDateTime":"${updatedExpirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
            """.stripIndent()
        Response consentResponse = doDefaultInitiationWithUpdatedPayload(updatedPayload)
        consentId = TestUtil.parseResponseBody(consentResponse, "Data.ConsentId")

        doAccountConsentAuthorisation()

        println "\nWaiting for consent to expire..."
        sleep(60000)

        accountsPath = ConnectorTestConstants.AISP_PATH + "accounts"
        doDefaultAccountRetrieval()

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Provided consent is expired"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Consent Enforcement Error")

    }

    @Test
    void "OBA-795_Accounts retrieval request with revoked consent"() {

        //Initiate and authorise consent
        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        //Revoke the Consent
        doConsentRevocation(consentId)
        Assert.assertEquals(consentRevocationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)

        //Verify Status of the consent changed to Revoked
        doAccountConsentRetrieval()
        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS),
                ConnectorTestConstants.REVOKED_STATUS)

        //Send Account Retrieve Request
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .baseUri(configuration.getServerBaseURL())
                .get(ConnectorTestConstants.ACCOUNTS_PATH)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
//        Assert.assertTrue(errorMessage.contains("Consent is not in the correct state"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Consent Enforcement Error")

    }

    @Test (groups = ["3.1.5", "3.1.6", "3.1.7", "3.1.8", "3.1.9", "3.1.10", "3.1.11"])
    void "TC0205021_Accounts Retrieval without ReadAccountDetail Permission"() {

        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        accountsPath = ConnectorTestConstants.ACCOUNTS_PATH

        def payload = AccountsRequestPayloads.initiationPayloadWithoutReadAccountDetails
        doDefaultInitiationWithUpdatedPayload(payload)
        consentId = TestUtil.parseResponseBody(consentResponse, "Data.ConsentId")

        doAccountConsentAuthorisation()
        doDefaultAccountRetrieval()

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }
}
