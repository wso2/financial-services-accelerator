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

package org.wso2.financial.services.accelerator.is.test.consent.management.ConsentOperationsFlowTests

import io.restassured.response.Response
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.automation.WaitForRedirectAutomationStep
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.automation.consent.BasicAuthAutomationStep
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.constant.PaymentRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.util.concurrent.TimeUnit

/**
 * Consent Flow Types Validation Test.
 * This test class is to validate the consent types supported by the IS connector. It will enable consent flow
 * types per scope and test whether the correct flow is executed. In this step accounts scope will follow a
 * scope based consent flow and payments scope will follow a pre initiated consent flow.
 *
 * Before running the tests, make sure to enable the consent flow types in the deployment.toml as below.
 * [financial_services.consent.pre_initiated]
 * scopes=["payments"]

 * [financial_services.consent.scope_based]
 * scopes=["accounts"]
 *
 * [financial_services.identity]
 * append_consent_id_to_token_introspect_response=true
 */
class ConsentFlowTypesValidationTest extends FSConnectorTest {

    // Test scope based consent flow for accounts scope
    @Test
    void "Authorise consent without a pre initiation step for accounts scope"() {

        consentId = ""

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithoutIntentId(
                configuration.getAppInfoClientID(), [ConnectorTestConstants.ApiScope.ACCOUNTS],
                true).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)

                    WebDriverWait wait = new WebDriverWait(driver, 10)
                    if ((driver.findElements(By.xpath(PageObjects.CHK_SALARY_SAVER_ACC))).displayed) {
                        driver.findElement(By.xpath(PageObjects.CHK_SALARY_SAVER_ACC)).click()
                    }

                    WebElement btnApprove = wait.until(
                            ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_APPROVE)))
                    btnApprove.click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        code = TestUtil.getHybridCodeFromUrl(automation.currentUrl.get())
    }

    @Test (dependsOnMethods = "Authorise consent without a pre initiation step for accounts scope")
    void "Verify Create User Access Token for scope based flow"() {

        List<ConnectorTestConstants.ApiScope> scopeList = [ConnectorTestConstants.ApiScope.ACCOUNTS]
        Response response = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code, scopeList)

        accessToken = TestUtil.parseResponseBody(response, "access_token")
        def idToken = TestUtil.parseResponseBody(response, "id_token")
        def scopes = TestUtil.parseResponseBody(response, "scope")
        log.info("Got app access token $accessToken")

        Assert.assertNotNull(accessToken)
        Assert.assertNotNull(idToken)
        Assert.assertTrue(scopes.contains(scopeList.get(0).scopeString))
    }

    @Test (dependsOnMethods = "Verify Create User Access Token for scope based flow")
    void "Introspection call for user access token"() {

        Response tokenResponse = getTokenIntrospectionResponse(accessToken)
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "active"), "true")
        Assert.assertNull(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.GRANT_TYPE))
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.CNF))
        consentId = TestUtil.parseResponseBody(tokenResponse, "consent_id")
    }

    @Test (dependsOnMethods = "Introspection call for user access token")
    void "Validate Retrieval on valid account for requestUri for scope based flow"() {

        def accessToken = GenerateBasicHeader()
        def validationPayload = AccountsRequestPayloads.buildValidationAccountsPayload(accessToken, userId, consentId)
        def validateURL = ConnectorTestConstants.ACCOUNT_VALIDATE_PATH

        doConsentValidate(validateURL, validationPayload)

        Assert.assertEquals(consentValidateResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(Boolean.parseBoolean(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.IS_VALID)),
                true)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentValidateResponse, "consentInformation"))
    }

    // Test Pre-Initiated consent flow for payments scope
    @Test(dependsOnMethods = "Validate Retrieval on valid account for requestUri for scope based flow")
    void "Verify Create Application Access Token"() {

        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils
                .getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)
        Response response = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)

        def accessToken = TestUtil.parseResponseBody(response, "access_token")
        def scopes = TestUtil.parseResponseBody(response, "scope")
        log.info("Got app access token $accessToken")


        Assert.assertNotNull(accessToken)
        Assert.assertEquals(scopes, scopeList.get(0).scopeString)
    }

    @Test (dependsOnMethods = "Verify Create Application Access Token")
    void "Verify Create the consent with valid inputs"() {

        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_PATH
        initiationPayload = PaymentRequestPayloads.initiationPaymentPayload

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test (dependsOnMethods = "Verify Create the consent with valid inputs")
    void "Verify Retrieving for a Created Consent"() {

        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_PATH

        doConsentRetrieval(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "Data.Status"), "AwaitingAuthorisation")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Consent")
    void "Generate authorization code when valid request object is present in the authorization request"() {

        //Authorise Consent
        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils
                .getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        Assert.assertNotNull(code)
        Assert.assertNotNull(TestUtil.getIdTokenFromUrl(automation.currentUrl.get()))
    }

    @Test (dependsOnMethods = "Generate authorization code when valid request object is present in the authorization request")
    void "Verify Create User Access Token"() {

        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils
                .getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)
        Response response = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
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

        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_PATH

        doConsentRetrieval(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "Data.Status"), "Authorised")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Consent After authorizing")
    void "Validate Retrieval on valid account for requestUri"() {

        def accessToken = GenerateBasicHeader()
        def validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId)
        def validateURL = ConnectorTestConstants.PAYMENT_VALIDATE_PATH

        doConsentValidate(validateURL, validationPayload)

        Assert.assertEquals(consentValidateResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(Boolean.parseBoolean(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.IS_VALID)),
                true)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentValidateResponse, "consentInformation"))
    }
}
