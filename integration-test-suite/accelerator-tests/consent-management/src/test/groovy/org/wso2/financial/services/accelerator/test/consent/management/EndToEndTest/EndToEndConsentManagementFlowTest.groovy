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

package org.wso2.financial.services.accelerator.test.consent.management.EndToEndTest

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAcceleratorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.openbanking.test.framework.utility.OBTestUtil

/**
 * End to End Consent Management Flow Tests.
 */
class EndToEndConsentManagementFlowTest extends FSAcceleratorTest {

    Map<String, String> map

    EndToEndConsentManagementFlowTest(Map<String, String> map) {

        this.map = map
    }

    @Test
    void "Verify Create Application Access Token"() {

        List<AcceleratorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(map.get("consentType"))
        Response response = getApplicationAccessTokenResponse(AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)

        def accessToken = TestUtil.parseResponseBody(response, "access_token")
        def scopes = TestUtil.parseResponseBody(response, "scope")
        log.info("Got app access token $accessToken")


        Assert.assertNotNull(accessToken)
        Assert.assertEquals(scopes, scopeList.get(0).scopeString)
    }

    @Test (dependsOnMethods = "Verify Create Application Access Token")
    void "Verify Create the consent with valid inputs"() {

        consentPath = map.get("initiationPath")
        initiationPayload = map.get("initiationPayload")

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)
    }

    @Test (dependsOnMethods = "Verify Create the consent with valid inputs")
    void "Verify Retrieving for a Created Consent"() {

        consentPath = map.get("initiationPath")

        doConsentRetrieval(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(OBTestUtil.parseResponseBody(consentResponse, "Data.Status"), "AwaitingAuthorisation")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Consent")
    void "Generate authorization code when valid request object is present in the authorization request"() {

        //Authorise Consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        Assert.assertNotNull(code)
        Assert.assertNotNull(OBTestUtil.getIdTokenFromUrl(automation.currentUrl.get()))
    }

    @Test (dependsOnMethods = "Generate authorization code when valid request object is present in the authorization request")
    void "Verify Create User Access Token"() {

        List<AcceleratorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(map.get("consentType"))
        Response response = getUserAccessTokenResponse(AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code, scopeList)

        def accessToken = TestUtil.parseResponseBody(response, "access_token")
        def idToken = TestUtil.parseResponseBody(response, "id_token")
        def scopes = TestUtil.parseResponseBody(response, "scope")
        log.info("Got app access token $accessToken")

        Assert.assertNotNull(accessToken)
        Assert.assertNotNull(idToken)
        Assert.assertTrue(scopes.contains(scopeList.get(0).scopeString))
    }

    @Test (dependsOnMethods = "Verify Create User Access Token")
    void "Verify Retrieving for a Created Consent After authorizing"() {

        consentPath = map.get("initiationPath")

        doConsentRetrieval(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(OBTestUtil.parseResponseBody(consentResponse, "Data.Status"), "Authorised")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Consent After authorizing")
    void "Validate Retrieval on valid account for requestUri"() {

        def accessToken = GenerateBasicHeader()
        def selectedAccount = "1234"
        def requestUri
        def validationPayload
        if (AcceleratorTestConstants.ACCOUNTS_TYPE == map.get("consentType")) {
            validationPayload = RequestPayloads.buildValidationAccountsPayload(accessToken, userId, consentId)
        } else if (AcceleratorTestConstants.COF_TYPE == map.get("consentType")) {
            requestUri = map.get("submissionPath")
            validationPayload = RequestPayloads.buildCofValidationPayload(accessToken, userId, consentId, requestUri)
        } else {
            requestUri = map.get("submissionPath")
            validationPayload = RequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId, requestUri)
        }

        doAccountValidation(validationPayload)

        Assert.assertEquals(accountValidationResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(Boolean.parseBoolean(OBTestUtil.parseResponseBody(accountValidationResponse, AcceleratorTestConstants.IS_VALID)),
                true)
        Assert.assertNotNull(OBTestUtil.parseResponseBody(accountValidationResponse, "consentInformation"))
    }

    @Test (dependsOnMethods = "Validate Retrieval on valid account for requestUri")
    void "Revoke a Created Consent"() {

        doConsentRevocation(consentId)

        Assert.assertEquals(consentRevocationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_204)
    }

}
