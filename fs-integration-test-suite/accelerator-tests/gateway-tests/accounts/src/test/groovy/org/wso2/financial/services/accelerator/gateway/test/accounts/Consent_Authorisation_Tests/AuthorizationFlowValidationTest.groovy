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

package org.wso2.financial.services.accelerator.gateway.test.accounts.Consent_Authorisation_Tests

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.automation.WaitForRedirectAutomationStep
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.automation.consent.AuthorizationFlowNavigationAutomationStep
import org.wso2.financial.services.accelerator.test.framework.automation.consent.BasicAuthAutomationStep
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Authorization Flow Validation Tests.
 */
class AuthorizationFlowValidationTest extends FSAPIMConnectorTest {

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
    void "Generate authorization code when valid request object is present in the authorization request"() {

        //Create Consent
        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        doAccountConsentAuthorisation()

        Assert.assertNotNull(code)
        Assert.assertNotNull(userAccessToken)
    }

    @Test
    void "Generate authorization code when Request Object is signed with expired certificate"() {

        String appKeystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String password = "wso2carbon"
        String alias = "tpp4-sig"

        //Create Consent
        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithDefinedCert(configuration.getAppInfoClientID(),
                scopeList, appKeystoreLocation, password, alias, consentId).toURI().toString()
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
        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithDefinedCert(configuration.getAppInfoClientID(),
                scopeList, appKeystoreLocation, password, alias, consentId).toURI().toString()
        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()
        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(ConnectorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
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

        //Generate User Access Token
        userAccessToken = getUserAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code as String, scopeList)

        //Account Retrieval
        accountsPath = ConnectorTestConstants.AISP_PATH + "accounts"
        doDefaultAccountRetrieval()
        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
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
