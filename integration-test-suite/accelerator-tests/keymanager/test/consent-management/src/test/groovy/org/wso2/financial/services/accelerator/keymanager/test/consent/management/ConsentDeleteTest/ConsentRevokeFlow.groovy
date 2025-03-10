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

package org.wso2.financial.services.accelerator.keymanager.test.consent.management.ConsentDeleteTest

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAcceleratorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.ConsentRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Consent Revocation Flow.
 */
class ConsentRevokeFlow extends FSAcceleratorTest {

    ConsentRequestBuilder consentRequestBuilder = new ConsentRequestBuilder()

    //Consent scopes as a list of Strings
    private List<String> consentScopesString = [
            AcceleratorTestConstants.ApiScope.ACCOUNTS.getScopeString(),
    ]

    @BeforeClass(alwaysRun = true)
    void init() {
        consentPath = AcceleratorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
    }

    @Test
    void "OB-1722_Verify Consent Revoke for valid consent"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorize consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        Assert.assertNotNull(code)
        Assert.assertNotNull(TestUtil.getIdTokenFromUrl(automation.currentUrl.get()))

        //Consent Revocation
        doConsentRevocation(consentId)
        Assert.assertEquals(consentRevocationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_204)
    }

    @Test
    void "OB-1723_Verify Consent Revoke for valid consent without Authorization Header"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorize consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        Assert.assertNotNull(code)
        Assert.assertNotNull(TestUtil.getIdTokenFromUrl(automation.currentUrl.get()))

        //Consent Revocation
        doConsentRevocationWithoutAuthorizationHeader()
        Assert.assertEquals(consentRevocationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "OB-1724_Verify Consent Revoke for valid consent with Incorrect Content Type Header"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorize consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        Assert.assertNotNull(code)
        Assert.assertNotNull(TestUtil.getIdTokenFromUrl(automation.currentUrl.get()))

        //Consent Revocation
        doConsentRevocationWithIncorrectContentTypeHeader()
        Assert.assertEquals(consentRevocationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_415)

    }

    @Test
    void "OB-1725_Verify Consent Revoke for valid consent with Incorrect Consent ID"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorize consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        Assert.assertNotNull(code)

        //Consent Revocation
        String incorrectConsentID = "2171e7f0-641c-4f4e-9a9d-cfbbdd02b85b99"
        doConsentRevocation(incorrectConsentID)
        Assert.assertEquals(consentRevocationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "OB-1726_Verify Consent Revoke for valid consent with Incorrect Consent Path"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorize consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        Assert.assertNotNull(code)

        //Consent Revocation
        doConsentRevocationWithIncorrectConsentPath()
        Assert.assertEquals(consentRevocationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_404)

    }

    @Test
    void "Verify Consent Revoke without client ID"() {
        consentPath = AcceleratorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        Response response = consentRequestBuilder.buildKeyManagerRequest("")
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/${consentId}")

        Assert.assertEquals(response.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Revoke with invalid client ID"() {
        consentPath = AcceleratorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        Response response = consentRequestBuilder.buildKeyManagerRequest("tyionwbbvqhhwvh")
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/${consentId}")

        Assert.assertEquals(response.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Revoke without consent ID"() {
        consentPath = AcceleratorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        Response response = consentRequestBuilder.buildKeyManagerRequest("")
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/")

        Assert.assertEquals(response.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Revoke with invalid consent ID"() {
        consentPath = AcceleratorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        Response response = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/12345678")

        Assert.assertEquals(response.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Revoke invalid consent type"() {
        consentPath = AcceleratorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        Response response = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .baseUri(configuration.getISServerUrl())
                .delete(AcceleratorTestConstants.COF_CONSENT_PATH + "/${consentId}")

        Assert.assertEquals(response.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent Revoke for already revoked consent"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        Assert.assertNotNull(code)

        //Consent Revocation
        doConsentRevocation(consentId)
        Assert.assertEquals(consentRevocationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_204)

        //Revoke Same Consent Again
        doConsentRevocation(consentId)
        Assert.assertEquals(consentRevocationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_400)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentRevocationResponse, ConnectorTestConstants.ERROR_DESCRIPTION)
//                .contains(ConnectorTestConstants.CONSENT_ALREADY_REVOKED))
    }
}
