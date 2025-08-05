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

import com.nimbusds.jwt.JWT
import org.json.JSONObject
import org.testng.Assert
import org.wso2.bfsi.test.framework.CommonTest
import org.wso2.bfsi.test.framework.automation.WaitForRedirectAutomationStep
import org.wso2.bfsi.test.framework.request_builder.SignedObject
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
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.ConsentRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.EventNotificationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * Base class for Accelerator Test.
 * All common functions that directly required for test class are implemented in here.
 */
class FSConnectorTest extends CommonTest{

    ConfigurationService configuration = new ConfigurationService()
    protected static Logger log = LogManager.getLogger(FSConnectorTest.class.getName())
    ConsentRequestBuilder consentRequestBuilder = new ConsentRequestBuilder()

    @BeforeClass(alwaysRun = true)
    void "Initialize Test Suite"() {
        FSRestAsRequestBuilder.init()
    }

    public String redirectURL
    protected String consentId
    String accessToken, refreshToken
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
    final String incorrectConsentPath = ConnectorTestConstants.INCORRECT_CONSENT_PATH
    final userId = "${configuration.getUserPSUName()}"
    def automation
    String dcrPath
    String clientId

    String eventCreationPayload
    final String eventCreationPath = ConnectorTestConstants.URL_EVENT_CREATE
    Response eventCreationResponse
    String resourceID
    String pollingPayload
    final String pollingPath = ConnectorTestConstants.URL_EVENT_POLLING
    Response pollingResponse
    String subscriptionPayload
    String subscriptionUpdatePayload
    final String subscriptionPath = ConnectorTestConstants.URL_EVENT_SUBSCRIPTION
    String subscriptionId
    JWTGenerator generator
    public String authoriseUrl
    Response subscriptionResponse
    Response subscriptionRetrievalResponse
    Response subscriptionUpdateResponse
    Response subscriptionDeletionResponse
    String ssa
    ClientRegistrationRequestBuilder registrationRequestBuilder

    //Consent scopes
    public List<ConnectorTestConstants.ApiScope> consentScopes = [
            ConnectorTestConstants.ApiScope.OPEN_ID,
            ConnectorTestConstants.ApiScope.ACCOUNTS
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
     * Get Application access token
     * @param authMethodType
     * @param clientId
     * @param scopeString
     * @return
     */
    Response getApplicationAccessTokenResponseWithCustomExp(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                                            String clientId = configuration.getAppInfoClientID(),
                                                            List <ConnectorTestConstants.ApiScope> scopeString, long exp) {


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
    Response getApplicationAccessTokenResponseWithoutAssertion(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                                               String clientId = configuration.getAppInfoClientID(),
                                                               List <ConnectorTestConstants.ApiScope> scopeString) {


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
    Response getApplicationAccessTokenResponseWithoutClientId(String authMethod, List <ConnectorTestConstants.ApiScope> scopeString) {


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
    Response getApplicationAccessTokenTLSWithAssertion(List <ConnectorTestConstants.ApiScope> scopeString, String clientId) {


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
                                                                   List <ConnectorTestConstants.ApiScope> scopeString, String clientId) {


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

    /**
     * Get User access token
     * @param authMethodType
     * @param clientId
     * @param authCode
     * @param scopes
     * @return
     */
    Response getUserAccessTokenResponse(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                        String clientId = configuration.getAppInfoClientID(),
                                        String authCode, List<ConnectorTestConstants.ApiScope> scopeString,
                                        boolean isCodeVerifierRequired = false) {
        return TokenRequestBuilder.getUserAccessTokenResponse(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId, authCode,
                isCodeVerifierRequired)
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

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Payment Consent Initiation Step.
     */
    void  doDefaultInitiationForPayments() {

        //initiation
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
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

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
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
    void  doDefaultInitiation(String payload, String clientId = configuration.getAppInfoClientID()) {

        //initiation without ReadAccountsDetail
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(clientId)
                .body(payload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }


    /**
     * Account Consent Initiation Step with without ReadAccountsDetail permission .
     */
    void  doDefaultInitiationWithoutReadAccountsDetail() {

        //initiation without ReadAccountsDetail
        consentResponse = consentRequestBuilder.buildBasicRequest(accessToken)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayloadPayloadWithoutReadAccountsDetail)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Initiation Step with without ReadTransactionsDetail permission .
     */
    void  doDefaultInitiationWithoutReadTransactionsDetail() {

        //initiation without ReadTransactionsDetail
        consentResponse = consentRequestBuilder.buildBasicRequest(accessToken)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayloadWithoutReadTransactionsDetail)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Initiation Step with without ReadBalances permission .
     */
    void  doDefaultInitiationWithoutReadBalances() {

        //initiation without ReadBalances
        consentResponse = consentRequestBuilder.buildBasicRequest(accessToken)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayloadPayloadWithoutReadAccountsDetail)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
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
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
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
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString();
    }


    /**
     * Account Consent Validate.
     * @param consentId
     */
    Response doConsentValidate(String validatePath, String claims) {

        def host = configuration.getISServerUrl().split("//")[1].replace(
                "8343", "8243")

        SignedObject signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(ConnectorTestConstants.SIGNING_ALGORITHM)

        consentValidateResponse = consentRequestBuilder.buildKeyManagerRequestForJWT(configuration.getAppInfoClientID())
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))

                .baseUri(configuration.getISServerUrl())
                .body(signedObject.getSignedRequest(claims))
                .post(validatePath)

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

        this.consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Retrieval Step.
     */
    void doConsentRetrievalWithoutConsentID() {

        //initiation
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/")

        this.consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }


    /**
     * Account Consent Retrieval Step without Authorization Header.
     * @param consentId
     */
    void doConsentRetrievalWithoutAuthorizationHeader(String consentId) {

        //Retrieval of a Consent without Authorization Header
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, "${configuration.getAppInfoClientID()}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        this.consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
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

        this.consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
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

        this.consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
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

        this.consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
    }

    /**
     * Account Consent Revocation Step.
     * @param consentId
     */
    Response doConsentRevocation(String consentId) {

        //initiation
        consentRevocationResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
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
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
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
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .contentType(ContentType.XML)
                .queryParam("consentID", ConnectorTestConstants.CONSENT_ID)
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
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .delete(consentPath + "/" + ConnectorTestConstants.CONSENT_ID)

        return consentRevocationResponse
    }

    /**
     * Account Consent Revocation Step with Incorrect Consent Path
     */
    Response doConsentRevocationWithIncorrectConsentPath() {

        //initiation
        consentRevocationResponse = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
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
     * Account Validation Request
     * @param payload
     */
    void doAccountValidation(String payload) {

        SignedObject signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(ConnectorTestConstants.ALG_RS512)

        accountValidationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequest(payload))
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.ACCOUNT_VALIDATE_PATH)
    }

    /**
     * Method to get Password Grant Access Token.
     * @param scope
     * @param clientId
     * @param username
     * @param password
     * @param authMethod
     */
    Response getPasswordGrantTokenResponse(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                           String username = configuration.getUserTPPName(),
                                           String password = configuration.getUserTPPPWD(),
                                           String clientId = configuration.getAppInfoClientID(),
                                           List <ConnectorTestConstants.ApiScope> scopeString ) {

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
    Response getRefreshGrantTokenResponse(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                          String clientId = configuration.getAppInfoClientID(),
                                          List <ConnectorTestConstants.ApiScope> scopeString, String refreshToken) {

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

    /**
     * Generate Basic Auth Header for the given username and password.
     * @param userName
     * @param password
     * @return
     */
    static String getBasicAuthHeader(String userName, String password){

        def authToken = TestUtil.getBasicAuthHeader(userName, password)
        def basicHeader = "Basic ${authToken}"

        return basicHeader
    }

    /**
     * Event polling
     */
    void doDefaultEventCreation() {
        //initiation
        eventCreationResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.URLENC)
                .header(ConnectorTestConstants.X_WSO2_RESOURCE_ID, resourceID)
                .baseUri(configuration.getISServerUrl())
                .body(constructEventCreationPayload(eventCreationPayload))
                .post(eventCreationPath)

    }

    /**
     * Event polling
     */
    void doDefaultEventPolling() {
        //initiation
        pollingResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.URLENC)
                .baseUri(configuration.getISServerUrl())
                .body(constructPollingPayload(pollingPayload))
                .post(pollingPath)
    }

    /**
     * Event Subscription Creation
     */
    void doDefaultSubscriptionCreation() {

        subscriptionResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        subscriptionId = TestUtil.parseResponseBody(subscriptionResponse,
                ConnectorTestConstants.PATH_EVENT_SUBSCRIPTION_ID)
    }

    /**
     * Event Subscription Retrieval
     */
    void doDefaultSubscriptionRetrieval() {

        subscriptionRetrievalResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .get(subscriptionPath + "/" + subscriptionId)
    }

    /**
     * Bulk Event Subscription Retrieval
     */
    void doDefaultSubscriptionBulkRetrieval() {

        subscriptionRetrievalResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .get(subscriptionPath)
    }

    /**
     * Bulk Event Subscription Retrieval
     */
    void doDefaultSubscriptionRetrievalByEventType() {

        subscriptionRetrievalResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .get(subscriptionPath + ConnectorTestConstants.URL_EVENT_SUBSCRIPTION_BY_EVENT_TYPE)
    }

    /**
     * Event Subscription Update
     */
    void doDefaultSubscriptionUpdate() {

        subscriptionUpdateResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionUpdatePayload)
                .put(subscriptionPath + "/" + subscriptionId)
    }

    /**
     * Event Subscription Deletion
     */
    void doDefaultSubscriptionDeletion() {

        subscriptionDeletionResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .delete(subscriptionPath + "/" + subscriptionId)
    }

    static String constructEventCreationPayload(String jsonPayload) {

        return "request=" + getBase64EncodedPayload(jsonPayload)
    }

    static String constructPollingPayload(String jsonPayload) {

        return "request=" + getBase64EncodedPayload(jsonPayload)
    }

    /**
     * Method to get the Base64 encoded the payload
     * @param payload  Payload to be encoded
     * @return Base64 encoded payload
     */
    static String getBase64EncodedPayload(String payload) {
        return Base64.encoder.encodeToString(payload.getBytes(Charset.defaultCharset()))
    }

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

    String createApplication(String softwareId = configuration.getAppDCRSoftwareId(),
                                    String tokenEndpointAuthMethod = ConnectorTestConstants.PKJWT_AUTH_METHOD) {

        dcrPath = configuration.getISServerUrl() + ConnectorTestConstants.REGISTRATION_ENDPOINT
        ssa = new File(configuration.getAppDCRSSAPath()).text
        registrationRequestBuilder = new ClientRegistrationRequestBuilder()

        JSONObject payload = new JSONObject(registrationRequestBuilder.getRegularClaims(ssa, softwareId,
                tokenEndpointAuthMethod))

        if(tokenEndpointAuthMethod.equalsIgnoreCase(ConnectorTestConstants.TLS_AUTH_METHOD)) {
            payload.remove("token_endpoint_allow_reuse_pvt_key_jwt")
        }

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(payload.toString())
                .post(dcrPath)

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        return clientId
    }

    Response deleteApplication(String clientId, String tokenEndpointAuthMethod = ConnectorTestConstants.PKJWT_AUTH_METHOD) {

        Response tokenResponse = getApplicationAccessTokenResponse(tokenEndpointAuthMethod, clientId, consentScopes)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .delete(dcrPath + clientId)

        return registrationResponse

    }

    /**
     * Account Consent Initiation Step with without ReadAccountsDetail permission .
     * @permissionsList
     */
    Response doConsentInitiation(String payload, String clientId=configuration.getAppInfoClientID()) {

        //initiation without ReadAccountsDetail
        consentResponse = consentRequestBuilder.buildKeyManagerRequest(clientId)
                .body(payload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        return consentResponse
    }

    /**
     * Method for Pushed Authorisation Request
     * @param requestObjectClaims
     * @param clientId
     * @param isStateParamRequired
     * @param algorithm
     * @return
     */
    Response doPushAuthorisationRequest(List <ConnectorTestConstants.ApiScope> scopeString, String consentId,
                                        String clientId = configuration.getAppInfoClientID(),
                                        String algorithm = null,
                                        String redirectUrl = configuration.getAppDCRRedirectUri(),
                                        String responseType = ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN,
                                        boolean isStateParamRequired = true) {

        Response parResponse
        generator = new JWTGenerator()

        //Generate Client Assertion
        String assertionString = generator.getClientAssertionJwt(clientId)
        String scopes = scopeString.collect { it.scopeString }.join(' ')

        //Generate Request Object Claim
        JWT getRequestObjectClaim = generator.getRequestObjectClaim(scopes, consentId, redirectUrl, clientId, responseType,
                true, UUID.randomUUID().toString())

        def bodyContent = [
                (ConnectorTestConstants.CLIENT_ID_KEY)            : (clientId),
                (ConnectorTestConstants.CLIENT_ASSERTION_TYPE_KEY): (ConnectorTestConstants.CLIENT_ASSERTION_TYPE),
                (ConnectorTestConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                (ConnectorTestConstants.REQUEST_KEY)         : getRequestObjectClaim.getParsedString()
        ]

        if (algorithm != null) {
            generator.setSigningAlgorithm(algorithm)
        }

        if (isStateParamRequired) {

            parResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)
        } else {

            parResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)
        }

        return parResponse
    }

    /**
     * Method for Consent Authorisation via Request URI
     * @param requestUri
     * @param clientId
     */
    void doConsentAuthorisationViaRequestUri(URI requestUri, String clientId = null) {

        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()

        if (clientId != null) {
            authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

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

    /**
     * Method to get User Access Token Response with Code Verifier
     * @param authMethodType
     * @param clientId
     * @param authCode
     * @param scopeString
     * @return
     */
    Response getUserAccessTokenResponseWithCodeVerifier(String authMethodType = ConnectorTestConstants.PKJWT_AUTH_METHOD,
                                        String clientId = configuration.getAppInfoClientID(),
                                        String authCode, List<ConnectorTestConstants.ApiScope> scopeString) {
        return TokenRequestBuilder.getUserAccessTokenResponse(authMethodType,
                scopeString.stream().map { it.scopeString }.toList(), clientId, authCode)
    }

    /**
     * Method for Pushed Authorisation Request without Client ID
     * @param scopeString
     * @param consentId
     * @param clientId
     * @param algorithm
     * @param redirectUrl
     * @param responseType
     * @param isStateParamRequired
     * @return
     */
    Response doPushAuthorisationRequestWithoutClientId(List <ConnectorTestConstants.ApiScope> scopeString, String consentId,
                                        String clientId = configuration.getAppInfoClientID(),
                                        String algorithm = null,
                                        String redirectUrl = configuration.getAppDCRRedirectUri(),
                                        String responseType = ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN,
                                        boolean isStateParamRequired = true) {

        Response parResponse
        generator = new JWTGenerator()

        //Generate Client Assertion
        String assertionString = generator.getClientAssertionJwt(clientId)
        String scopes = scopeString.collect { it.scopeString }.join(' ')

        //Generate Request Object Claim
        JWT getRequestObjectClaim = generator.getRequestObjectClaim(scopes, consentId, redirectUrl, clientId, responseType,
                true, UUID.randomUUID().toString())

        def bodyContent = [
                (ConnectorTestConstants.CLIENT_ASSERTION_TYPE_KEY): (ConnectorTestConstants.CLIENT_ASSERTION_TYPE),
                (ConnectorTestConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                (ConnectorTestConstants.REQUEST_KEY)         : getRequestObjectClaim.getParsedString()
        ]

        if (algorithm != null) {
            generator.setSigningAlgorithm(algorithm)
        }

        if (isStateParamRequired) {

            parResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)
        } else {

            parResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)
        }

        return parResponse
    }

    /**
     * Method for Pushed Authorisation Request without Request Object
     * @param scopeString
     * @param consentId
     * @param clientId
     * @param algorithm
     * @param redirectUrl
     * @param responseType
     * @param isStateParamRequired
     * @return
     */
    Response doPushAuthorisationRequestWithoutRequestObject(List <ConnectorTestConstants.ApiScope> scopeString, String consentId,
                                        String clientId = configuration.getAppInfoClientID(),
                                        String algorithm = null,
                                        String redirectUrl = configuration.getAppDCRRedirectUri(),
                                        String responseType = ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN,
                                        boolean isStateParamRequired = true) {

        Response parResponse
        generator = new JWTGenerator()

        //Generate Client Assertion
        String assertionString = generator.getClientAssertionJwt(clientId)
        String scopes = scopeString.collect { it.scopeString }.join(' ')

        //Generate Request Object Claim
        def bodyContent = [
                (ConnectorTestConstants.CLIENT_ID_KEY)            : (clientId),
                (ConnectorTestConstants.CLIENT_ASSERTION_TYPE_KEY): (ConnectorTestConstants.CLIENT_ASSERTION_TYPE),
                (ConnectorTestConstants.CLIENT_ASSERTION_KEY)     : assertionString
        ]

        if (algorithm != null) {
            generator.setSigningAlgorithm(algorithm)
        }

        if (isStateParamRequired) {

            parResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)
        } else {

            parResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)
        }

        return parResponse
    }

    /**
     * Method for Pushed Authorisation Request with Request Object Claims
     * @param requestObjectClaims
     * @param clientId
     * @param algorithm
     * @param isStateParamRequired
     * @return
     */
    Response doPushAuthorisationRequest(JWT requestObjectClaims, String clientId = configuration.getAppInfoClientID(),
                                        String algorithm = null, boolean isStateParamRequired = true) {

        Response parResponse
        generator = new JWTGenerator()

        //Generate Client Assertion
        String assertionString = generator.getClientAssertionJwt(clientId)

        def bodyContent = [
                (ConnectorTestConstants.CLIENT_ID_KEY)            : (clientId),
                (ConnectorTestConstants.CLIENT_ASSERTION_TYPE_KEY): (ConnectorTestConstants.CLIENT_ASSERTION_TYPE),
                (ConnectorTestConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                (ConnectorTestConstants.REQUEST_KEY)         : requestObjectClaims.getParsedString()
        ]

        if (algorithm != null) {
            generator.setSigningAlgorithm(algorithm)
        }

        if (isStateParamRequired) {

            parResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)
        } else {

            parResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)
        }

        return parResponse
    }
}
