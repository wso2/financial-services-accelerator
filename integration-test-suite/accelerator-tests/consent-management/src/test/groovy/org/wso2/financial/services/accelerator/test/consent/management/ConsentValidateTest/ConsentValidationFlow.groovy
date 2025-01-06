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

package org.wso2.financial.services.accelerator.test.consent.management.ConsentValidateTest

import org.wso2.openbanking.test.framework.request_builder.SignedObject
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAcceleratorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset

/**
 * Consent Validation Tests.
 */
class ConsentValidationFlow extends FSAcceleratorTest {

    SignedObject signedObject
    def validationPayload
    def requestUri

    @BeforeClass
    void init() {
        consentPath = AcceleratorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
        accessToken = GenerateBasicHeader()
        signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(AcceleratorTestConstants.ALG_RS512)
    }

    //Enable the test when [open_banking.consent.validation.jwt.payload] enabled=false in deployment.toml file
    @Test (enabled = false)
    void "OB-1720_Send Validate Request with JSON payload"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl().split("//")[1].replace("9446", "8243")

        //Consent Validate Request
        validationPayload = RequestPayloads.buildValidationAccountsPayload(accessToken, userId, consentId)

        accountValidationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .body(signedObject.getSignedRequest(validationPayload))
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

        Assert.assertEquals(accountValidationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "OB-1966_Verify Validation of a created Consent when authorization flow is denied"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Denial
        consentDenial(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()

        //Consent Validate Request
        doConsentValidate(consentId)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertTrue(consentValidateResponse.jsonPath().get("isValid").toString()
                .contains(AcceleratorTestConstants.IS_VALID_FALSE))
    }

    @Test
    void "OB-1967_Verify Validation of a created Consent when consent does not have sufficient permissions"() {

        //Consent Initiation
        doDefaultInitiation(RequestPayloads.initiationPayloadWithoutReadAccountsDetail)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()

        //Consent Validate Request
        doConsentValidate(consentId)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertTrue(consentValidateResponse.jsonPath().get("isValid").toString()
                .contains(AcceleratorTestConstants.IS_VALID_FALSE))
        Assert.assertTrue(consentValidateResponse.jsonPath().get("errorMessage").toString()
                .contains(AcceleratorTestConstants.PERMISSION_MISMATCH_ERROR))
    }

    @Test
    void "OB-1968_Verify Validation of a created Consent when incorrect validation path sent"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()

        //Consent Validate Request

        validationPayload = RequestPayloads.buildValidationAccountsPayload(accessToken, userId, consentId)

        accountValidationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequest(validationPayload))
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${accessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                AcceleratorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(AcceleratorTestConstants.INCORRECT_ACCOUNT_VALIDATE_PATH)

        Assert.assertEquals(accountValidationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_404)
    }

    @Test
    void "OB-1969_Verify Validation of a created Consent without Content Type Header"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()

        def authToken = "${configuration.getUserKeyManagerAdminName()}:" +
                "${configuration.getUserKeyManagerAdminPWD()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        //Consent Validate Request
        validationPayload = RequestPayloads.buildValidationAccountsPayload(accessToken, userId, consentId)

        accountValidationResponse = FSRestAsRequestBuilder.buildRequest()
                .body(signedObject.getSignedRequest(validationPayload))
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${accessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                AcceleratorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(AcceleratorTestConstants.ACCOUNT_VALIDATE_PATH)

        Assert.assertEquals(accountValidationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "OB-1970_Verify Validation of a created Consent with Invalid Content Type Header"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()

        //Consent Validate Request
        validationPayload = RequestPayloads.buildValidationAccountsPayload(accessToken, userId, consentId)

        accountValidationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.HTML)
                .body(signedObject.getSignedRequest(validationPayload))
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${accessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                AcceleratorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(AcceleratorTestConstants.ACCOUNT_VALIDATE_PATH)

        Assert.assertEquals(accountValidationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "OB-1971_Verify Validation of a created Consent with Invalid Accept Type Header"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()

        //Consent Validate Request
        validationPayload = RequestPayloads.buildValidationAccountsPayload(accessToken, userId, consentId)

        Response validationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequest(validationPayload))
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${accessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JWT)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                AcceleratorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(AcceleratorTestConstants.ACCOUNT_VALIDATE_PATH)

        Assert.assertEquals(validationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_406)
    }

    @Test
    void "OB-1721_Send Validate Request with JWT payload"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        //Consent Validate Request
        validationPayload = RequestPayloads.buildValidationAccountsPayload(accessToken, userId, consentId)
        doAccountValidation(validationPayload)

        Assert.assertEquals(accountValidationResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_200)
    }
}
