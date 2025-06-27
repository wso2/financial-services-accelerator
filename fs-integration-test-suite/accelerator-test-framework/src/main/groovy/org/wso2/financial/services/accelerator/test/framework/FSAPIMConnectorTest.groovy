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

package org.wso2.financial.services.accelerator.test.framework

import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.wso2.bfsi.test.framework.CommonTest
import org.wso2.bfsi.test.framework.automation.WaitForRedirectAutomationStep
import org.wso2.financial.services.accelerator.test.framework.automation.consent.BasicAuthAutomationStep
import org.wso2.financial.services.accelerator.test.framework.configuration.APIConfigurationService
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.CofRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.*
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.util.concurrent.TimeUnit

/**
 * Base class for Accelerator Test.
 * All common functions that directly required for test class are implemented in here.
 */
class FSAPIMConnectorTest extends CommonTest{

    @BeforeClass(alwaysRun = true)
    void "Initialize Test Suite"() {
        FSRestAsRequestBuilder.init()
    }

    ConfigurationService configuration = new ConfigurationService()
    protected static Logger log = LogManager.getLogger(FSAPIMConnectorTest.class.getName())
    ConsentRequestBuilder consentRequestBuilder = new ConsentRequestBuilder()
    JWSSignatureRequestBuilder jwsSignatureRequestBuilder = new JWSSignatureRequestBuilder()

    protected String consentId
    String accessToken, refreshToken
    String code
    String denyResponse
    Response consentResponse
    Response consentRevocationResponse
    String consentPath
    String initiationPayload
    final String incorrectConsentPath = ConnectorTestConstants.INCORRECT_CONSENT_PATH
    def automation
    String dcrPath
    String clientId
    String submissionPath
    String submissionPayload
    JWTGenerator generator

    String applicationAccessToken
    String consentStatus
    String userAccessToken
    String paymentID
    Response submissionResponse
    List<ConnectorTestConstants.ApiScope> scopeList
    Response retrievalResponse
    String accountsPath
    Response publisherResponse
    APIConfigurationService apiConfiguration

    /**
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    String getApplicationAccessToken(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                     String clientId = configuration.getAppInfoClientID(),
                                     List <ConnectorTestConstants.ApiScope> scopeString ) {

        Response response = TokenRequestBuilder.getApplicationAccessTokenResponse(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId)

        def accessToken = TestUtil.parseResponseBody(response, "access_token")
        log.info("Got app access token $accessToken")

        if (accessToken != null) {
            addToContext(ConnectorTestConstants.APP_ACCESS_TKN, accessToken)
        } else {
            log.error("Application access Token is null")
        }
        return accessToken
    }

    /**
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    Response getApplicationAccessTokenResponse(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                               String clientId = configuration.getAppInfoClientID(),
                                               List <ConnectorTestConstants.ApiScope> scopeString ) {


        Response response = TokenRequestBuilder.getApplicationAccessTokenResponse(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId)

        return response
    }

    /**
     * Token Introspection Request.
     * @param accessToken
     */
    static Response getTokenIntrospectionResponse(String accessToken) {

        return TokenRequestBuilder.getTokenIntrospectionResponse(accessToken)
    }

    RequestSpecification buildKeyManagerRequestWithoutAuthorizationHeader(String clientID) {
        return consentRequestBuilder.buildKeyManagerRequestWithoutAuthorizationHeader(clientID)
    }
    /**
     * Consent Initiation Step.
     */
    void  doDefaultInitiation() {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**Consent Authorisation.
     *
     * @param clientId
     * @param consentID
     * @param isRegulatory
     * @param scopes
     */
    void doConsentAuthorisation(String clientId, boolean isRegulatory = true, List <ConnectorTestConstants.ApiScope> scopes) {
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(clientId,
                consentId, scopes, true).toURI().toString()

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

    /**Consent Authorisation.
     *
     * @param clientId
     * @param consentID
     * @param isRegulatory
     * @param scopes
     */
    void consentDenial(String clientId, boolean isRegulatory = true, List <ConnectorTestConstants.ApiScope> scopes) {

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(clientId,
                consentId, scopes, true).toURI().toString()

        def automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)

                    WebDriverWait wait = new WebDriverWait(driver, 60)
                    if ((driver.findElements(By.xpath(PageObjects.CHK_SALARY_SAVER_ACC))).displayed) {
                        driver.findElement(By.xpath(PageObjects.CHK_SALARY_SAVER_ACC)).click()
                    }

                    WebElement btnConfirm = wait.until(
                            ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_DENY)))
                    btnConfirm.click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        // Get User denied message From URL
        denyResponse = TestUtil.getErrorDescriptionFromUrlWhenDenied(automation.currentUrl.get())
    }

    /**
     * Consent Initiation Step with without ReadAccountsDetail permission .
     * @permissionsList
     */
    Response doConsentInitiation(String payload) {

        //initiation without ReadAccountsDetail
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(payload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        return consentResponse
    }

    /**
     * Payment Consent Initiation Step.
     */
    void  doDefaultInitiationForPayments(String applicationAccessToken, String payload) {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsSignatureRequestBuilder.requestHeader,
                        payload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Payment Consent Retrieval.
     * @param consentId
     */
    void doConsentRetrieval() {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        consentStatus = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
    }

    /**
     * Payment Consent Authorisation.
     * @param consentId
     */
    void doPaymentConsentAuthorisation(List<ConnectorTestConstants.ApiScope> scopes) {

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(configuration.getAppInfoClientID(),
                consentId, scopes, true).toURI().toString()


        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->

                    WebDriverWait wait = new WebDriverWait(driver, 10)
                    wait = new WebDriverWait(driver, 60)
                    if ((driver.findElements(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH))).size != 0) {
                        driver.findElement(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH)).click()
                    }
                    WebElement btnApprove = wait.until(
                            ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.SUBMIT_XPATH)))
                    btnApprove.click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        code = TestUtil.getHybridCodeFromUrl(automation.currentUrl.get())

        userAccessToken = getUserAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code.toString(), scopes)
    }

    /**
     * Account Consent Initiation Step
     */
    Response doDefaultPaymentInitiationWithUpdatedPayload(String updatedPayload) {
        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .baseUri(configuration.getServerBaseURL())
                .body(updatedPayload)
                .post(consentPath)

        return consentResponse
    }

    /**
     * Get User access token
     * @param authMethodType
     * @param clientId
     * @param authCode
     * @param scopes
     * @return
     */
    String getUserAccessToken(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                              String clientId = configuration.getAppInfoClientID(),
                              String authCode, List<ConnectorTestConstants.ApiScope> scopeString) {

        String accessToken = TokenRequestBuilder.getUserAccessToken(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId, authCode)
        if (accessToken != null) {
            addToContext(ConnectorTestConstants.USER_ACCESS_TKN, accessToken)
        } else {
            log.error("User access Token is null")
        }
        return accessToken
    }

    /**Consent Authorisation.
     *
     * @param clientId
     * @param consentID
     * @param isRegulatory
     * @param scopes
     */
    void consentPaymentDenial(String clientId, boolean isRegulatory = true, List <ConnectorTestConstants.ApiScope> scopes) {

        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils
                .getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(clientId,
                consentId, scopeList, true).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->

                    WebDriverWait wait = new WebDriverWait(driver, 10)
                    wait = new WebDriverWait(driver, 60)
                    if ((driver.findElements(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH))).size != 0) {
                        driver.findElement(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH)).click()
                    }
                    WebElement btnConfirm = wait.until(
                            ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_DENY)))
                    btnConfirm.click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()
    }

    /**
     * Payment Submission Step.
     */
    void doDefaultPaymentSubmission(String userAccessToken, String payload) {

        submissionResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil
                        .generateXjwsSignature(jwsSignatureRequestBuilder.requestHeader, payload))
                .body(payload)
                .baseUri(configuration.getServerBaseURL())
                .post(submissionPath)

        paymentID = TestUtil.parseResponseBody(submissionResponse, "Data.PaymentId")
        consentStatus = TestUtil.parseResponseBody(submissionResponse, "Data.Status")
    }

    /**
     * Cof Authorization Step.
     */
    void doCofAuthorization(List<ConnectorTestConstants.ApiScope> scopes) {

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(configuration.getAppInfoClientID(),
                consentId, scopes, true).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->

                    WebDriverWait wait = new WebDriverWait(driver, 60)

                    WebElement btnconfirm = wait.until(
                            ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.SUBMIT_XPATH)))
                    btnconfirm.click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        //Get Code from URL
        code = TestUtil.getHybridCodeFromUrl(automation.currentUrl.get())

        // Get User Access Token
        userAccessToken = getUserAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code.toString(), scopes)

    }

    /**
     * Consent Revocation Step.
     * @param consentId
     */
    Response doConsentRevocation(String consentId) {

        //initiation
        consentRevocationResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/${consentId}")

        return consentRevocationResponse
    }

    /**
     * Consent Submission Step.
     */
    Response doDefaultSubmission() {

        submissionResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getServerBaseURL())
                .body(CofRequestPayloads.getCofSubmissionPayload(consentId))
                .post(ConnectorTestConstants.CBPII_PATH + ConnectorTestConstants.COF_SUBMISSION_PATH)

        return submissionResponse
    }

    /**
     * Payment Submission Step.
     * @param payload
     */
    void doDefaultPaymentSubmission(String payload) {

        submissionResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil
                        .generateXjwsSignature(jwsSignatureRequestBuilder.requestHeader, payload))
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .body(payload)
                .baseUri(configuration.getServerBaseURL())
                .post(submissionPath)
    }

    /**
     * Account Consent Initiation Step
     */
    void doDefaultAccountInitiation() {
        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID)
        consentStatus = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
    }

    /**
     * Account Consent Retrieval.
     * @param consentId
     */
    void doAccountConsentRetrieval() {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        consentStatus = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
    }

    /**
     * Account Consent Authorisation.
     * @param consentId
     */
    void doAccountConsentAuthorisation() {

        //initiation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)
        Assert.assertNotNull(code)
        Assert.assertNotNull(TestUtil.getIdTokenFromUrl(automation.currentUrl.get()))

        userAccessToken = getUserAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code as String, scopeList)
    }

    /**
     * Retrieve Accounts List
     */
    void doDefaultAccountRetrieval() {
        // Check consent valid status
        retrievalResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(accountsPath)
    }

    /**
     *  Consent Initiation Step with defined payload
     */
    Response doDefaultInitiationWithUpdatedPayload(String updatedPayload) {
        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .body(updatedPayload)
                .post(consentPath)

        return consentResponse
    }

    /**
     * Cof Initiation Step with defined payload
     * @param updatedPayload
     * @return
     */
    Response doDefaultCofInitiationWithUpdatedPayload(String updatedPayload) {
        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getServerBaseURL())
                .body(updatedPayload)
                .post(consentPath)

        return consentResponse
    }

    /**
     * Account Consent Revocation Step.
     * @param consentId
     */
    Response doAccountConsentRevocation(String consentId) {

        //initiation
        consentRevocationResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/${consentId}")

        return consentRevocationResponse
    }

    /**
     * Account Consent Authorisation Deny scenario.
     * @param consentId
     */
    void doAccountConsentAuthorisationDeny() {

        //initiation
        consentDenial(configuration.getAppInfoClientID(), true, scopeList)
    }

    /**
     * Account Consent Retrieval Step with Incorrect request path.
     * @param consentId
     */
    void doConsentRetrievalWithIncorrectRequestPath(String consentId) {

        //Retrieval of a Consent with incorrect Consent Request Path
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(incorrectConsentPath + "/${consentId}")

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID)
    }

    /**
     * Default Payment ID Retrieval Step.
     * @param payload
     */
    void doDefaultPaymentIdRetrieval() {

        submissionResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(submissionPath)
    }

    /**
     * Consent Authorisation without Consent ID.
     * @param clientId
     * @param isRegulatory
     * @param scopes
     */
    void doConsentAuthorisationWithoutConsentId(String clientId, boolean isRegulatory = true, List <ConnectorTestConstants.ApiScope> scopes) {

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequestWithoutConsentId(clientId,
                scopes, true).toURI().toString()

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

    Response getUserAccessTokenResponse(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                        String clientId = configuration.getAppInfoClientID(),
                                        String authCode, List<ConnectorTestConstants.ApiScope> scopeString) {
        return TokenRequestBuilder.getUserAccessTokenResponse(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId, authCode)
    }

    /**
     * Get Application Access Token for Non-Regulatory App.
     * @param clientId
     * @param clientSecret
     * @param scopeString
     * @return
     */
    String getApplicationAccessTokenForNonRegulatoryApp(String clientId = configuration.getNonRegulatoryAppClientID(),
                                                        String clientSecret = configuration.getNonRegulatoryAppClientSecret(),
                                                        List <ConnectorTestConstants.ApiScope> scopeString ) {

        Response response = TokenRequestBuilder.getApplicationAccessTokenForNonRegulatoryApp(scopeString.stream().map { it.scopeString }.toList(),
                clientId, clientSecret)

        def accessToken = TestUtil.parseResponseBody(response, "access_token")
        log.info("Got app access token $accessToken")

        if (accessToken != null) {
            addToContext(ConnectorTestConstants.APP_ACCESS_TKN, accessToken)
        } else {
            log.error("Application access Token is null")
        }
        return accessToken
    }

    /**
     * Cof Authorization Deny scenario.
     * @param scopes
     */
    void doCofConsentDeny(List<ConnectorTestConstants.ApiScope> scopes) {

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(configuration.getAppInfoClientID(),
                consentId, scopes, true).toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->

                    WebDriverWait wait = new WebDriverWait(driver, 60)

                    WebElement btnConfirm = wait.until(
                            ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_DENY)))
                    btnConfirm.click()
                }
                .execute()

        // Get User denied message From URL
        denyResponse = TestUtil.getErrorDescriptionFromUrlWhenDenied(automation.currentUrl.get())
    }

    /**
     * Payment Consent Deny scenario.
     * @param scopes
     */
    void doPaymentConsentDeny(List<ConnectorTestConstants.ApiScope> scopes) {

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(configuration.getAppInfoClientID(),
                consentId, scopes, true).toURI().toString()


        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->

                    WebDriverWait wait = new WebDriverWait(driver, 10)
                    wait = new WebDriverWait(driver, 60)
                    if ((driver.findElements(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH))).size != 0) {
                        driver.findElement(By.xpath(PageObjects.PAYMENTS_SELECT_XPATH)).click()
                    }
                    WebElement btnConfirm = wait.until(
                            ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_DENY)))
                    btnConfirm.click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        // Get User denied message From URL
        denyResponse = TestUtil.getErrorDescriptionFromUrlWhenDenied(automation.currentUrl.get())
    }

    /**
     * Method to get Refresh Grant Access Token.
     * @param authMethodType
     * @param clientId
     * @param refreshToken
     * @param scopeString
     * @return
     */
    Response getRefreshTokenGrantToken(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                     String clientId = configuration.getAppInfoClientID(), String refreshToken,
                                     List <ConnectorTestConstants.ApiScope> scopeString ) {

        Response response = TokenRequestBuilder.getRefreshGrantTokenResponse(scopeString.stream().map{ it.scopeString }.toList(),
                clientId, refreshToken, authMethodType)

        return response
    }

    /**
     * Payment Consent Initiation Step.
     */
    void  doDefaultPaymentInitiation() {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Retrieval Step with defined access token.
     * @param accessToken
     */
    void doAccountRetrievalWithDefinedAccessToken(String accessToken) {
        // Check consent valid status
        retrievalResponse = consentRequestBuilder.buildBasicRequest(accessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(accountsPath)
    }
}
