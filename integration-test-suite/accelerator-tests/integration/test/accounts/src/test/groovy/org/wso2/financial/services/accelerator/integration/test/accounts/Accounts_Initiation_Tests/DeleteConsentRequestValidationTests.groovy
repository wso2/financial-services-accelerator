/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.integration.test.accounts.Accounts_Initiation_Tests

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.integration.test.accounts.util.AbstractAccountsFlow

/**
 * Consent Revocation Request Validation Tests
 */
class DeleteConsentRequestValidationTests extends AbstractAccountsFlow {

    @Test
    void "TC0203008_Delete Consent With Empty Consent Id"() {

        doDefaultAccountInitiation()

        def consentid = ""

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/" + consentid)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_405)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Method not allowed for given API resource"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "405")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Runtime Error")
    }

    @Test
    void "TC0203009_Delete Consent Without Consent Id Parameter"() {

        doDefaultAccountInitiation()

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_405)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Method not allowed for given API resource"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "405")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Runtime Error")
    }

    @Test
    void "TC0203011_Delete Already Revoked Consent"() {

        //Revoke a Consent
        doDefaultAccountInitiation()
        consentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_204)

        //Delete the previously revoked consent
        def revokedConsentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(revokedConsentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(revokedConsentResponse, AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Consent is already in revoked or rejected state"))
        Assert.assertEquals(TestUtil.parseResponseBody(revokedConsentResponse,AcceleratorTestConstants.ERROR_ERRORS_CODE),
                "400")
        Assert.assertEquals(TestUtil.parseResponseBody(revokedConsentResponse,AcceleratorTestConstants.ERROR_ERRORS_MSG),
                AcceleratorTestConstants.CONSENT_MGT_ERROR)
    }

    @Test
    void "TC0203020_Delete Already Rejected Consent"() {

        //Reject a Consent
        doDefaultAccountInitiation()
        doAccountConsentAuthorisationDeny()
        doAccountConsentRetrieval()
        Assert.assertEquals(consentStatus, "Rejected")

        //Delete the previously Rejected consent
        def rejectedConsentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(rejectedConsentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(rejectedConsentResponse, AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Consent is already in revoked or rejected state"))
        Assert.assertEquals(TestUtil.parseResponseBody(rejectedConsentResponse,AcceleratorTestConstants.ERROR_ERRORS_CODE),
                "400")
        Assert.assertEquals(TestUtil.parseResponseBody(rejectedConsentResponse,AcceleratorTestConstants.ERROR_ERRORS_MSG),
                AcceleratorTestConstants.CONSENT_MGT_ERROR)
    }

    @Test
    void "US-317_Delete an account consent in AwaitingAuthorisation State"() {

        //Revoke a Consent
        doDefaultAccountInitiation()
        consentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_204)

    }

    @Test
    void "US-318_Delete an account consent in Authorised State"() {

        //Revoke a Consent
        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()
        consentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_204)

    }
}
