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
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.gateway.test.accounts.util.AbstractAccountsFlow
import org.wso2.financial.services.accelerator.gateway.test.accounts.util.AccountsDataProviders
import org.wso2.financial.services.accelerator.gateway.test.accounts.util.AccountPayloads

import java.time.OffsetDateTime

/**
 * Accounts Flow Retrieval Tests.
 */
class AccountsRetrievalRequestBodyValidationTests extends AbstractAccountsFlow {

    @Test(dataProvider = "AccountsResources", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Retrieval Request"(String resource) {

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

    //TODO
//    @Test(dataProvider = "AccountsResourcesWithoutPermissions", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Retrieval without required permission Permission"(String[] params) {

        def resource = params[0]
        def payload = params[1]
        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(payload)
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
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Consent Enforcement Error")
    }

    @Test
    void "Accounts Retrieval after consent is expired"() {

        OffsetDateTime updatedExpirationInstant = OffsetDateTime.now().plusMinutes(1)

        def updatedPayload = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(AccountPayloads.permissionsArray)},
                "ExpirationDateTime":"${updatedExpirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
            """.stripIndent()
        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(updatedPayload)
        consentId = TestUtil.parseResponseBody(consentResponse, "Data.ConsentId")

        doAccountConsentAuthorisation()

        println "\nWaiting for consent to expire..."
        sleep(60000)
        doDefaultAccountRetrieval()

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Provided consent is expired"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Consent Enforcement Error")

    }
}
