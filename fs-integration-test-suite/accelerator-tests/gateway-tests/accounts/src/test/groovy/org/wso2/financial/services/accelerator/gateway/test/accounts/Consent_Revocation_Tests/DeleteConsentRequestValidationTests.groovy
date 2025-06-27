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

package org.wso2.financial.services.accelerator.gateway.test.accounts.Consent_Revocation_Tests

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Consent Revocation Request Validation Tests
 */
class DeleteConsentRequestValidationTests extends FSAPIMConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Delete Consent With Empty Consent Id"() {

        doDefaultAccountInitiation()

        def consentid = ""

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/" + consentid)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_405)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG)
        Assert.assertTrue(errorMessage.contains("Method not allowed for given API resource"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "405")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Runtime Error")
    }

    @Test
    void "Delete Consent Without Consent Id Parameter"() {

        doDefaultAccountInitiation()

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/")

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_405)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG)
        Assert.assertTrue(errorMessage.contains("Method not allowed for given API resource"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "405")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Runtime Error")
    }

    @Test
    void "Delete Already Revoked Consent"() {

        //Revoke a Consent
        doDefaultAccountInitiation()
        consentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)

        //Delete the previously revoked consent
        def revokedConsentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(revokedConsentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(revokedConsentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Consent is already in revoked or rejected state"))
        Assert.assertEquals(TestUtil.parseResponseBody(revokedConsentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "400")
        Assert.assertEquals(TestUtil.parseResponseBody(revokedConsentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_delete")
    }

    @Test
    void "Delete Already Rejected Consent"() {

        //Reject a Consent
        doDefaultAccountInitiation()
        doAccountConsentAuthorisationDeny()
        doAccountConsentRetrieval()
        Assert.assertEquals(consentStatus, "Rejected")

        //Delete the previously Rejected consent
        def rejectedConsentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(rejectedConsentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(rejectedConsentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Consent is already in revoked or rejected state"))
        Assert.assertEquals(TestUtil.parseResponseBody(rejectedConsentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "400")
        Assert.assertEquals(TestUtil.parseResponseBody(rejectedConsentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_delete")
    }

    @Test
    void "Delete an account consent in AwaitingAuthorisation State"() {

        //Revoke a Consent
        doDefaultAccountInitiation()
        consentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)

    }

    @Test
    void "Delete an account consent in Authorised State"() {

        //Revoke a Consent
        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()
        consentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)

    }
}
