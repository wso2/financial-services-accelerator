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

package org.wso2.financial.services.accelerator.is.test.consent.management.ConsentAuthorisationTest

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.automation.consent.AuthorizationFlowNavigationAutomationStep
import org.wso2.financial.services.accelerator.test.framework.automation.consent.BasicAuthAutomationStep
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.bfsi.test.framework.automation.WaitForRedirectAutomationStep

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder.getScopeString
import static org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder.getScopeStringWithoutOpenIdScope

/**
 * Authorisation Flow Validation Tests.
 */
class AuthorizationFlowValidationTest extends FSConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
    }

    @Test
    void "Generate authorization code when valid request object is present in the authorization request"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        Assert.assertNotNull(code)
        Assert.assertNotNull(TestUtil.getIdTokenFromUrl(automation.currentUrl.get()))
    }

    @Test
    void "Generate authorization code when Scope is invalid within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Invalid Scope
        List<ConnectorTestConstants.ApiScope> invalidScopes = [
                ConnectorTestConstants.ApiScope.INVALID_SCOPE
        ]

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthRequestWithoutOpenidScopeInReqObj(configuration.getAppInfoClientID(),
                consentId, invalidScopes, true, ConnectorTestConstants.SCOPE_PARAMETER,
                getScopeString(consentScopes)).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                ConnectorTestConstants.INVALID_SCOPE_ERROR)
    }

    @Test (enabled = true)
    void "Generate authorization code when Scope is invalid within Authorization URL"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Invalid Scope
        List<ConnectorTestConstants.ApiScope> invalidScopes = [
                ConnectorTestConstants.ApiScope.INVALID_SCOPE
        ]

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, consentScopes, true, ConnectorTestConstants.SCOPE_PARAMETER,
                getScopeStringWithoutOpenIdScope(invalidScopes)).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                ConnectorTestConstants.INVALID_AUTH_URL_SCOPE_ERROR.replace("@@CLIENT_ID",
                        configuration.getAppInfoClientID()))
    }

    @Test
    void "Generate authorization code when Response Type is invalid within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, consentScopes, true, ConnectorTestConstants.RESPONSE_TYPE_PARAMETER,
                "invalid response").toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                (ConnectorTestConstants.INVALID_RESPONSE_TYPE_ERROR))
    }

    @Test
    void "Generate authorization code when Redirect URL Type is empty within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, consentScopes, true, ConnectorTestConstants.REDIRECT_URI_PARAMETER,
                "").toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                    def error = driver.findElement(By.xpath(PageObjects.LBL_REDIRECT_ERROR)).getText()

                    Assert.assertTrue(error.contains(ConnectorTestConstants.EMPTY_REDIRECT_URI_ERROR))
                }
                .execute()
    }

    @Test (enabled = true)
    void "Generate authorization code when Redirect URL is mismatched with registered URL within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, consentScopes, true, ConnectorTestConstants.REDIRECT_URI_PARAMETER,
                ConnectorTestConstants.INVALID_REDIRECT_URI).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                    def error = driver.findElement(By.xpath(PageObjects.LBL_REDIRECT_ERROR)).getText()

                    Assert.assertTrue(error.contains(ConnectorTestConstants.MISMATCHED_REDIRECT_URI_ERROR))
                }
                .execute()
    }

    @Test //TODO: Update assertion to validate from UI
    void "Generate authorization code without Redirect URL in Authorization Request"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithoutRedirectUri(configuration.getAppInfoClientID(),
                consentScopes, consentId).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                    def error = driver.findElement(By.xpath(PageObjects.LBL_REDIRECT_ERROR)).getText()

                    Assert.assertTrue(error.contains(ConnectorTestConstants.EMPTY_REDIRECT_URI_ERROR))
                }
                .execute()
    }

    @Test (enabled = true)
    void "Generate authorization code without Scope in Authorization Request"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithoutScope(configuration.getAppInfoClientID(),
                consentScopes, consentId).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                ConnectorTestConstants.INVALID_AUTH_URL_SCOPE_ERROR.replace("@@CLIENT_ID", configuration.getAppInfoClientID()))
    }

    @Test
    void "Generate authorization code when Request Object is signed with expired certificate"() {

        String appKeystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "sgsMuc8ACBgBzinpr8oJ8B.key")

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithDefinedPemCert(configuration.getAppInfoClientID(),
                consentScopes, appKeystoreLocation, consentId).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()

        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(ConnectorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
    }

    @Test
    void "Generate authorization code when Request Object is signed with Invalid certificate"() {

        String appKeystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "berlin-certs", "eidas-qwac.jks")
        String password = "wso2carbon"
        String alias = "1"

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithDefinedCert(configuration.getAppInfoClientID(),
                consentScopes, appKeystoreLocation, password, alias, consentId).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()

        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(ConnectorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
    }

    @Test
    void "Generate authorization code when without openid scope within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder authorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = authorisationBuilder.getAuthorizationRequestWithoutOpenIDScope(configuration.getAppInfoClientID(),
                consentId, [ConnectorTestConstants.ApiScope.ACCOUNTS], true).toURI().toString()

        BrowserAutomation.AutomationContext automation
        automation = getBrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                ConnectorTestConstants.INVALID_AUTH_URL_SCOPE_ERROR.replace("@@CLIENT_ID", configuration.getAppInfoClientID()))

    }

    @Test
    void "Generate authorization code without openid scope within Authorization URL"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder authorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = authorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, [ConnectorTestConstants.ApiScope.ACCOUNTS], true,
                ConnectorTestConstants.SCOPE_PARAMETER, "invalid_scope").toURI().toString()

        BrowserAutomation.AutomationContext automation
        automation = getBrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep(new WaitForRedirectAutomationStep())
                .execute()
        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                ConnectorTestConstants.INVALID_AUTH_URL_SCOPE_ERROR.replace("@@CLIENT_ID", configuration.getAppInfoClientID()))
    }

    @Test
    void "Generate authorization code when audience value is not id token issuer"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder authorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = authorisationBuilder.getAuthorizationRequestWithCustomAud(configuration.getAppInfoClientID(),
                consentId, consentScopes, true).toURI().toString()

        BrowserAutomation.AutomationContext automation
        automation = getBrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()

        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(ConnectorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                ConnectorTestConstants.INVALID_PARAM_ERROR)
    }

    @Test
    void "Generate authorization code when unsupported signing algorithm is sent"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder authorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = authorisationBuilder.getAuthorizationRequestWithCustomAlgorithm(configuration.getAppInfoClientID(),
                consentId, [ConnectorTestConstants.ApiScope.ACCOUNTS], true).toURI().toString()

        BrowserAutomation.AutomationContext automation
        automation = getBrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
               .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                ConnectorTestConstants.INVALID_SIG_ALGO_ERROR)
    }

    //TODO: Enable the test by setting [financial_services.consent] is_pre_initiated_consent=false
    @Test (enabled = false)
    void "OBA-683_Authorise consent without a pre initiation step"() {

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

                    if ((driver.findElements(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH))).displayed) {
                        driver.findElement(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH)).click()
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

    //TODO: Enable the test by setting [financial_services.consent] auth_flow_consent_id_source="requestParam" and
    // use client id of a non-fapi compliance application to perform the test.
    @Test (enabled = false)
    void "OBA-682_Authorise consent by defining auth_flow_consent_id_source=requestParam"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        String scope = "ais:" + consentId + " openid"
        List<String> scopeList = Arrays.asList(scope.split(" "))

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithRequestParams(
                configuration.getAppInfoClientID(), scope, consentId).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)

                    WebDriverWait wait = new WebDriverWait(driver, 10)
                    if ((driver.findElements(By.xpath(PageObjects.CHK_SALARY_SAVER_ACC))).displayed) {
                        driver.findElement(By.xpath(PageObjects.CHK_SALARY_SAVER_ACC)).click()
                    }

                    if ((driver.findElements(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH))).displayed) {
                        driver.findElement(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH)).click()
                    }
                    WebElement btnApprove = wait.until(
                            ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_APPROVE)))
                    btnApprove.click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        code = TestUtil.getHybridCodeFromUrl(automation.currentUrl.get())

        JWTGenerator acceleratorJWTGenerator = new JWTGenerator()
        acceleratorJWTGenerator.setScopes(scopeList)
        String jwt = acceleratorJWTGenerator.getUserAccessTokenJwt(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, code)

        def tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(jwt)
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        def refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
        def idToken = TestUtil.parseResponseBody(tokenResponse, "id_token")
        def responseScope = TestUtil.parseResponseBody(tokenResponse, "scope")

        Assert.assertNotNull(accessToken)
        Assert.assertNotNull(refreshToken)
        Assert.assertNotNull(idToken)
        Assert.assertEquals(responseScope, scope)
    }
}
