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

package org.wso2.financial.services.accelerator.test.consent.management.ConsentAuthorisationTest

import org.wso2.openbanking.test.framework.automation.OBBrowserAutomation
import org.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import org.wso2.openbanking.test.framework.utility.OBTestUtil
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAcceleratorTest
import org.wso2.financial.services.accelerator.test.framework.automation.consent.AuthorizationFlowNavigationAutomationStep
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder.*

/**
 * Authorisation Flow Validation Tests.
 */
class AuthorizationFlowValidationTest extends FSAcceleratorTest {

    @BeforeClass
    void init() {
        consentPath = AcceleratorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
    }

    @Test
    void "OB-436_Generate authorization code when valid request object is present in the authorization request"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        Assert.assertNotNull(code)
        Assert.assertNotNull(TestUtil.getIdTokenFromUrl(automation.currentUrl.get()))
    }

    @Test
    void "OB-437_Generate authorization code when Scope is invalid within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Invalid Scope
        List<AcceleratorTestConstants.ApiScope> invalidScopes = [
                AcceleratorTestConstants.ApiScope.INVALID_SCOPE
        ]

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthRequestWithoutOpenidScopeInReqObj(configuration.getAppInfoClientID(),
                consentId, invalidScopes, true, AcceleratorTestConstants.SCOPE_PARAMETER,
                getScopeString(consentScopes)).toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                AcceleratorTestConstants.INVALID_SCOPE_ERROR)
    }

    @Test (enabled = true)
    void "OB-438_Generate authorization code when Scope is invalid within Authorization URL"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Invalid Scope
        List<AcceleratorTestConstants.ApiScope> invalidScopes = [
                AcceleratorTestConstants.ApiScope.INVALID_SCOPE
        ]

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, consentScopes, true, AcceleratorTestConstants.SCOPE_PARAMETER,
                getScopeStringWithoutOpenIdScope(invalidScopes)).toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                AcceleratorTestConstants.INVALID_AUTH_URL_SCOPE_ERROR.replace("@@CLIENT_ID",
                        configuration.getAppInfoClientID()))
    }

    @Test
    void "OB-439_Generate authorization code when Response Type is invalid within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, consentScopes, true, AcceleratorTestConstants.RESPONSE_TYPE_PARAMETER,
                "invalid response").toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                (AcceleratorTestConstants.INVALID_RESPONSE_TYPE_ERROR))
    }

    @Test
    void "OB-440_Generate authorization code when Redirect URL Type is empty within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, consentScopes, true, AcceleratorTestConstants.REDIRECT_URI_PARAMETER,
                "").toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                    def error = driver.findElement(By.xpath(PageObjects.LBL_REDIRECT_ERROR)).getText()

                    Assert.assertTrue(error.contains(AcceleratorTestConstants.EMPTY_REDIRECT_URI_ERROR))
                }
                .execute()
    }

    @Test (enabled = true)
    void "OB-441_Generate authorization code when Redirect URL is mismatched with registered URL within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, consentScopes, true, AcceleratorTestConstants.REDIRECT_URI_PARAMETER,
                AcceleratorTestConstants.INVALID_REDIRECT_URI).toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                    def error = driver.findElement(By.xpath(PageObjects.LBL_REDIRECT_ERROR)).getText()

                    Assert.assertTrue(error.contains(AcceleratorTestConstants.MISMATCHED_REDIRECT_URI_ERROR))
                }
                .execute()
    }

    @Test //TODO: Update assertion to validate from UI
    void "OB-442_Generate authorization code without Redirect URL in Authorization Request"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithoutRedirectUri(configuration.getAppInfoClientID(),
                consentScopes, consentId).toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                    def error = driver.findElement(By.xpath(PageObjects.LBL_REDIRECT_ERROR)).getText()

                    Assert.assertTrue(error.contains(AcceleratorTestConstants.EMPTY_REDIRECT_URI_ERROR))
                }
                .execute()
    }

    @Test (enabled = true)
    void "OB-443_Generate authorization code without Scope in Authorization Request"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithoutScope(configuration.getAppInfoClientID(),
                consentScopes, consentId).toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()

        Assert.assertEquals(TestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                AcceleratorTestConstants.INVALID_AUTH_URL_SCOPE_ERROR.replace("@@CLIENT_ID", configuration.getAppInfoClientID()))
    }

    @Test
    void "OB-444_Generate authorization code when Request Object is signed with expired certificate"() {

        String appKeystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String password = "wso2carbon"
        String alias = "tpp4-sig"

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithDefinedCert(configuration.getAppInfoClientID(),
                consentScopes, appKeystoreLocation, password, alias, consentId).toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()

        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(AcceleratorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
    }

    @Test
    void "OB-445_Generate authorization code when Request Object is signed with Invalid certificate"() {

        String appKeystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "berlin-certs", "eidas-qwac.jks")
        String password = "wso2carbon"
        String alias = "1"

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithDefinedCert(configuration.getAppInfoClientID(),
                consentScopes, appKeystoreLocation, password, alias, consentId).toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()

        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(AcceleratorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
    }

    @Test
    void "Generate authorization code when without openid scope within Request Object"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder authorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = authorisationBuilder.getAuthorizationRequestWithoutOpenIDScope(configuration.getAppInfoClientID(),
                consentId, [AcceleratorTestConstants.ApiScope.ACCOUNTS], true).toURI().toString()

        OBBrowserAutomation.AutomationContext automation
        automation = getBrowserAutomation(OBBrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        Assert.assertEquals(OBTestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                AcceleratorTestConstants.INVALID_AUTH_URL_SCOPE_ERROR.replace("@@CLIENT_ID", configuration.getAppInfoClientID()))

    }

    @Test
    void "Generate authorization code without openid scope within Authorization URL"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder authorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = authorisationBuilder.getAuthorizationRequestWithCustomValues(configuration.getAppInfoClientID(),
                consentId, [AcceleratorTestConstants.ApiScope.ACCOUNTS], true,
                AcceleratorTestConstants.SCOPE_PARAMETER, "invalid_scope").toURI().toString()

        OBBrowserAutomation.AutomationContext automation
        automation = getBrowserAutomation(OBBrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep(new WaitForRedirectAutomationStep())
                .execute()
        Assert.assertEquals(OBTestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                AcceleratorTestConstants.INVALID_AUTH_URL_SCOPE_ERROR.replace("@@CLIENT_ID", configuration.getAppInfoClientID()))
    }

    @Test
    void "Generate authorization code when audience value is not id token issuer"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder authorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = authorisationBuilder.getAuthorizationRequestWithCustomAud(configuration.getAppInfoClientID(),
                consentId, consentScopes, true).toURI().toString()

        OBBrowserAutomation.AutomationContext automation
        automation = getBrowserAutomation(OBBrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()

        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(AcceleratorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
        Assert.assertEquals(OBTestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                AcceleratorTestConstants.INVALID_PARAM_ERROR)
    }

    @Test
    void "Generate authorization code when unsupported signing algorithm is sent"() {

        //Create Consent
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder authorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = authorisationBuilder.getAuthorizationRequestWithCustomAlgorithm(configuration.getAppInfoClientID(),
                consentId, [AcceleratorTestConstants.ApiScope.ACCOUNTS], true).toURI().toString()

        OBBrowserAutomation.AutomationContext automation
        automation = getBrowserAutomation(OBBrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        Assert.assertEquals(OBTestUtil.getErrorDescriptionFromUrl(URLDecoder.decode(automation.currentUrl.get())),
                AcceleratorTestConstants.INVALID_SIG_ALGO_ERROR)
    }
}
