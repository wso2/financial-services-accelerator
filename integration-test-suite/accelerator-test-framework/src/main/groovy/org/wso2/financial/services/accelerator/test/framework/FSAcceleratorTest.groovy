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

package org.wso2.financial.services.accelerator.test.framework

import org.wso2.openbanking.test.framework.OBTest
import org.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import org.wso2.openbanking.test.framework.request_builder.SignedObject
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.annotations.BeforeClass
import org.wso2.financial.services.accelerator.test.framework.automation.consent.BasicAuthAutomationStep
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.ConsentRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.util.concurrent.TimeUnit

/**
 * Base class for Accelerator Test.
 * All common functions that directly required for test class are implemented in here.
 */
class FSAcceleratorTest extends OBTest{

    ConfigurationService configuration = new ConfigurationService()
    protected static Logger log = LogManager.getLogger(FSAcceleratorTest.class.getName())
    ConsentRequestBuilder consentRequestBuilder = new ConsentRequestBuilder()

    @BeforeClass(alwaysRun = true)
    void "Initialize Test Suite"() {
        FSRestAsRequestBuilder.init()
    }

    public String redirectURL
    protected String consentId
    String accessToken
    String code
    String denyResponse
    Response consentResponse
    Response consentRevocationResponse
    Response consentValidateResponse
    Response accountValidationResponse
    String consentPath
    String initiationPayload
    String initiationIncorrectPayload = RequestPayloads.initiationIncorrectPayload
    String initiationPayloadPayloadWithoutReadAccountsDetail = RequestPayloads.initiationPayloadWithoutReadAccountsDetail
    String initiationPayloadWithoutReadTransactionsDetail = RequestPayloads.initiationPayloadWithoutReadTransactionsDetail
    String initiationPayloadPayloadWithoutReadBalances = RequestPayloads.initiationPayloadWithoutReadBalances
    final String incorrectConsentPath = AcceleratorTestConstants.INCORRECT_CONSENT_PATH
    final userId = "${configuration.getUserPSUName()}"
    def automation

    //Consent scopes
    public List<AcceleratorTestConstants.ApiScope> consentScopes = [
            AcceleratorTestConstants.ApiScope.OPEN_ID,
            AcceleratorTestConstants.ApiScope.ACCOUNTS
    ]
    /**
     * Set redirect URL of application
     * can be used in any testcase
     * @param url
     */
    void setRedirectURL(String url) {
        this.redirectURL = url
    }

    String getRedirectURL() {
        if (this.redirectURL == null) {
            this.redirectURL = configuration.getAppInfoRedirectURL()
        }
        return this.redirectURL
    }

    String GenerateBasicHeader() {

        return consentRequestBuilder.GenerateBasicHeader()
    }

    /**
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    String getApplicationAccessToken(String authMethodType = AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                                     String clientId = configuration.getAppInfoClientID(),
                                     List <AcceleratorTestConstants.ApiScope> scopeString ) {

        Response response = TokenRequestBuilder.getApplicationAccessTokenResponse(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId)

        def accessToken = TestUtil.parseResponseBody(response, "access_token")
        log.info("Got app access token $accessToken")

        if (accessToken != null) {
            addToContext(AcceleratorTestConstants.APP_ACCESS_TKN, accessToken)
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
    Response getApplicationAccessTokenResponse(String authMethodType = AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                                               String clientId = configuration.getAppInfoClientID(),
                                               List <AcceleratorTestConstants.ApiScope> scopeString ) {


        Response response = TokenRequestBuilder.getApplicationAccessTokenResponse(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId)

        return response
    }

    /**
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    Response getApplicationAccessTokenResponseWithCustomExp(String authMethodType = AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                                                            String clientId = configuration.getAppInfoClientID(),
                                                            List <AcceleratorTestConstants.ApiScope> scopeString, long exp) {


        Response response = TokenRequestBuilder.getApplicationAccessTokenResponseWithCustomExp(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId, exp)

        return response
    }

    /**
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    Response getApplicationAccessTokenResponseWithoutAssertion(String authMethodType = AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                                                               String clientId = configuration.getAppInfoClientID(),
                                                               List <AcceleratorTestConstants.ApiScope> scopeString) {


        Response response = TokenRequestBuilder.getApplicationAccessTokenResponseWithoutAssertion(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId)

        return response
    }

    /**
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    Response getApplicationAccessTokenResponseWithoutClientId(String authMethod, List <AcceleratorTestConstants.ApiScope> scopeString) {


        Response response = TokenRequestBuilder.getApplicationAccessTokenResponseWithoutClientId(authMethod,
                scopeString.stream().map { it.scopeString }.toList())

        return response
    }

    /**
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    Response getApplicationAccessTokenTLSWithAssertion(List <AcceleratorTestConstants.ApiScope> scopeString, String clientId) {


        Response response = TokenRequestBuilder.getApplicationAccessTokenTLSWithAndAssertion(
                scopeString.stream().map { it.scopeString }.toList(), clientId)

        return response
    }

    /**
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    Response getApplicationAccessTokenResponseWithCertAndAssertion(String authMethod,
                                                                   List <AcceleratorTestConstants.ApiScope> scopeString, String clientId) {


        Response response = TokenRequestBuilder.getApplicationAccessTokenResponseWithCertAndAssertion(authMethod,
                scopeString.stream().map { it.scopeString }.toList(), clientId)

        return response
    }

    /**
     * Get User access token
     * @param authMethodType
     * @param clientId
     * @param authCode
     * @param scopes
     * @return
     */
    String getUserAccessToken(String authMethodType = AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                              String clientId = configuration.getAppInfoClientID(),
                              String authCode, List<AcceleratorTestConstants.ApiScope> scopeString) {
        String accessToken = TokenRequestBuilder.getUserAccessToken(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId, authCode)
        if (accessToken != null) {
            addToContext(AcceleratorTestConstants.USER_ACCESS_TKN, accessToken)
        } else {
            log.error("User access Token is null")
        }
        return accessToken
    }

    /**
     * Get User access token
     * @param authMethodType
     * @param clientId
     * @param authCode
     * @param scopes
     * @return
     */
    Response getUserAccessTokenResponse(String authMethodType = AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                                        String clientId = configuration.getAppInfoClientID(),
                                        String authCode, List<AcceleratorTestConstants.ApiScope> scopeString) {
        return TokenRequestBuilder.getUserAccessTokenResponse(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId, authCode)
    }

    RequestSpecification buildKeyManagerRequest(String clientID) {
        return consentRequestBuilder.buildKeyManagerRequest(clientID)
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
     * Account Consent Initiation Step.
     */
    void  doDefaultInitiation() {

        //initiation
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Initiation Step without Authorization Header.
     */
    void  doDefaultInitiationWithoutAuthorizationHeader() {

        //initiation without Authorization Header
        consentResponse = consentRequestBuilder.buildKeyManagerRequestWithoutAuthorizationHeader(configuration.getAppInfoClientID())
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Initiation Step without Content Type Header.
     */
    void  doDefaultInitiationWithoutContentTypeHeader() {

        //initiation without Content Type Header
        consentResponse = consentRequestBuilder.buildBasicRequestWithoutContentType()
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

    }

    /**
     * Account Consent Initiation Step with incorrect request Payload.
     */
    void  doDefaultInitiationWithIncorrectPayload() {

        //initiation with Incorrect Payload
        consentResponse = consentRequestBuilder.buildBasicRequestWithIncorrectRequestPayload()
                .body(initiationIncorrectPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
    }

    /**
     * Account Consent Initiation Step with an Invalid Content Type Value.
     */
    void  doDefaultInitiationWithInvalidContentTypeValue() {

        //initiation with Invalid Content Type Value
        consentResponse = consentRequestBuilder.buildBasicRequestWithInvalidValueContentType()
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
    }

    /**
     * Account Consent Initiation Step with Invalid Accept Header Value
     */
    void  doDefaultInitiationWithInvalidAcceptHeaderValue() {

        //initiation with Invalid Content Type Value
        consentResponse = consentRequestBuilder.buildBasicRequestWithInvalidAcceptHeaderValue()
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
    }

    /**
     * Account Consent Initiation Step with without ReadAccountsDetail permission .
     * @permissionsList
     */
    void  doDefaultInitiation(String payload) {

        //initiation without ReadAccountsDetail
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(payload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }


    /**
     * Account Consent Initiation Step with without ReadAccountsDetail permission .
     */
    void  doDefaultInitiationWithoutReadAccountsDetail() {

        //initiation without ReadAccountsDetail
        consentResponse = consentRequestBuilder.buildBasicRequest(accessToken)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayloadPayloadWithoutReadAccountsDetail)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Initiation Step with without ReadTransactionsDetail permission .
     */
    void  doDefaultInitiationWithoutReadTransactionsDetail() {

        //initiation without ReadTransactionsDetail
        consentResponse = consentRequestBuilder.buildBasicRequest(accessToken)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayloadWithoutReadTransactionsDetail)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Initiation Step with without ReadBalances permission .
     */
    void  doDefaultInitiationWithoutReadBalances() {

        //initiation without ReadBalances
        consentResponse = consentRequestBuilder.buildBasicRequest(accessToken)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayloadPayloadWithoutReadAccountsDetail)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }


    /**
     * Default Initiation without Certain Permission
     * @param withoutReadAccountsDetail
     * @param withoutReadTransactionsDetail
     * @param withoutReadBalances
     */
    void doDefaultInitiationWithoutCertainPermissions(boolean withoutReadAccountsDetail, boolean withoutReadTransactionsDetail, boolean withoutReadBalances) {

        // Build the consent request
        TokenRequestBuilder consentRequestBuilder = consentRequestBuilder.buildBasicRequest(accessToken)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayloadPayloadWithoutReadAccountsDetail)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath) as TokenRequestBuilder;

        // Set the appropriate payload based on the permission requirements
        if (withoutReadAccountsDetail) {
            consentRequestBuilder.body(initiationPayloadPayloadWithoutReadAccountsDetail);
        }
        if (withoutReadTransactionsDetail) {
            consentRequestBuilder.body(initiationPayloadWithoutReadTransactionsDetail);
        }
        if (withoutReadBalances) {
            consentRequestBuilder.body(initiationPayloadPayloadWithoutReadBalances);
        }

        // Perform the consent initiation
        consentResponse = consentRequestBuilder.post(consentPath) as Response;

        // Parse the consent ID from the response body
        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString();
    }


    /**
     * Account Consent Validate.
     * @param consentId
     */
    Response doConsentValidate(String consentId) {

        def host = configuration.getISServerUrl().split("//")[1].replace(
                "8343", "8243")

        SignedObject signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(AcceleratorTestConstants.SIGNING_ALGORITHM)

        consentValidateResponse = consentRequestBuilder.buildKeyManagerRequestForJWT(configuration.getAppInfoClientID())
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                AcceleratorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))

                .baseUri(configuration.getISServerUrl())
                .body(signedObject.getSignedRequest(RequestPayloads.buildValidationPayload(userId, consentId, host,
                        "/accounts")))
                .post(AcceleratorTestConstants.ACCOUNT_VALIDATE_PATH)

        return consentValidateResponse
    }

    /**
     * Account Consent Retrieval Step.
     * @param consentId
     */
    void doConsentRetrieval(String consentId) {

        //initiation
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        this.consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Retrieval Step.
     */
    void doConsentRetrievalWithoutConsentID() {

        //initiation
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/")

        this.consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }


    /**
     * Account Consent Retrieval Step without Authorization Header.
     * @param consentId
     */
    void doConsentRetrievalWithoutAuthorizationHeader(String consentId) {

        //Retrieval of a Consent without Authorization Header
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.X_WSO2_CLIENT_ID_KEY, "${configuration.getAppInfoClientID()}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        this.consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Retrieval Step with Incorrect request path.
     * @param consentId
     */
    void doConsentRetrievalWithIncorrectRequestPath(String consentId) {

        //Retrieval of a Consent with incorrect Consent Request Path
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .baseUri(configuration.getISServerUrl())
                .get(incorrectConsentPath + "/${consentId}")

        this.consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Retrieval Step with Incorrect Access token type
     * @param consentId
     */
    void doConsentRetrievalWithIncorrectAccessTokenType() {

        //Retrieval of a Consent with incorrect Access Token Type
        consentResponse = consentRequestBuilder.buildBasicRetrievalRequestWithIncorrectAccessToken(accessToken)
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        this.consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Retrieval Step with Different Search Params
     * @param consentId
     */
    void doConsentRetrievalWithDifferentSearchParams(String consentId) {

        //Retrieval of a Consent with different search params
        consentResponse = consentRequestBuilder.buildBasicRequestWithDifferentSearchParams(accessToken)
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        this.consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Revocation Step.
     * @param consentId
     */
    Response doConsentRevocation(String consentId) {

        //initiation
        consentRevocationResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/${consentId}")

        return consentRevocationResponse
    }

    /**
     * Account Consent Revocation Step without Authorization header
     * @param consentId
     */
    Response doConsentRevocationWithoutAuthorizationHeader() {

        //initiation
        consentRevocationResponse = FSRestAsRequestBuilder.buildRequest()
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .queryParam("consentID", consentId)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/${consentId}")

        return consentRevocationResponse
    }

    /**
     * Account Consent Revocation Step without Authorization header
     * @param consentId
     */
    Response doConsentRevocationWithIncorrectContentTypeHeader() {

        //initiation
        consentRevocationResponse = FSRestAsRequestBuilder.buildRequest()
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .contentType(ContentType.XML)
                .queryParam("consentID", AcceleratorTestConstants.CONSENT_ID)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/${consentId}")

        return consentRevocationResponse
    }

    /**
     * Account Consent Revocation Step without Incorrect Consent ID
     * @param consentId
     */
    Response doConsentRevocationWithIncorrectConsentID() {

        //initiation
        consentRevocationResponse = FSRestAsRequestBuilder.buildRequest()
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .header(AcceleratorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/" + AcceleratorTestConstants.CONSENT_ID)

        return consentRevocationResponse
    }

    /**
     * Account Consent Revocation Step with Incorrect Consent Path
     */
    Response doConsentRevocationWithIncorrectConsentPath() {

        //initiation
        consentRevocationResponse = FSRestAsRequestBuilder.buildRequest()
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${AcceleratorTestConstants.BEARER} ${accessToken}")
                .contentType(ContentType.JSON)
                .queryParam("consentID", consentId)
                .baseUri(configuration.getISServerUrl())
                .delete(incorrectConsentPath + "/${consentId}")

        return consentRevocationResponse
    }

    /**Consent Authorisation.
     *
     * @param clientId
     * @param consentID
     * @param isRegulatory
     * @param scopes
     */
    void doConsentAuthorisation(String clientId, boolean isRegulatory = true, List <AcceleratorTestConstants.ApiScope> scopes) {
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(clientId,
                consentId, scopes, true).toURI().toString()

        automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
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
    void consentDenial(String clientId, boolean isRegulatory = true, List <AcceleratorTestConstants.ApiScope> scopes) {

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(clientId,
                consentId, scopes, true).toURI().toString()

        def automation = getBrowserAutomation(AcceleratorTestConstants.DEFAULT_DELAY)
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
     * Account Validation Request
     * @param payload
     */
    void doAccountValidation(String payload) {

        SignedObject signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(AcceleratorTestConstants.ALG_RS512)

        accountValidationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequest(payload))
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                AcceleratorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(AcceleratorTestConstants.ACCOUNT_VALIDATE_PATH)
    }

    /**
     * Method to get Password Grant Access Token.
     * @param scope
     * @param clientId
     * @param username
     * @param password
     * @param authMethod
     */
    Response getPasswordGrantTokenResponse(String authMethodType = AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                                           String username = configuration.getUserTPPName(),
                                           String password = configuration.getUserTPPPWD(),
                                           String clientId = configuration.getAppInfoClientID(),
                                           List <AcceleratorTestConstants.ApiScope> scopeString ) {

        Response response = TokenRequestBuilder.getPasswordGrantAccessToken(
                scopeString.stream().map { it.scopeString }.toList(), clientId,
                username, password, authMethodType,)
        return response
    }

    /**
     * Method to get Password Grant Access Token.
     * @param scope
     * @param clientId
     * @param username
     * @param password
     * @param authMethod
     */
    Response getRefreshGrantTokenResponse(String authMethodType = AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                                          String clientId = configuration.getAppInfoClientID(),
                                          List <AcceleratorTestConstants.ApiScope> scopeString, String refreshToken) {

        Response response = TokenRequestBuilder.getRefreshGrantTokenResponse(
                scopeString.stream().map { it.scopeString }.toList(), clientId, refreshToken, authMethodType)
        return response
    }

    /**
     * Get Base Access Token Request without TLS cert in the context.
     * @return access token request
     */
    static RequestSpecification getAccessTokenRequestWithoutCertInContext() {

        return TokenRequestBuilder.accessTokenRequestWithoutCertInContext
    }
}
