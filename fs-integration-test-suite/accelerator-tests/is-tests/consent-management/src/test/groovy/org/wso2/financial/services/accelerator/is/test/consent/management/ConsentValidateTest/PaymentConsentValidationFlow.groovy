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

package org.wso2.financial.services.accelerator.is.test.consent.management.ConsentValidateTest

import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PaymentRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.bfsi.test.framework.request_builder.SignedObject

/**
 * Payment Consent Validation Tests.
 */
class PaymentConsentValidationFlow extends FSConnectorTest {

    SignedObject signedObject
    def validationPayload
    List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_PATH
        initiationPayload = PaymentRequestPayloads.initiationPaymentPayload
        accessToken = GenerateBasicHeader()
        signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(ConnectorTestConstants.ALG_RS512)
    }

    @Test
    void "Send Validate Request with JWT payload"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        //Consent Validate Request
        validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId)
        doConsentValidate(ConnectorTestConstants.PAYMENT_VALIDATE_PATH, validationPayload)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "Verify Validation of a created Consent when authorization flow is denied"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Denial
        consentDenial(configuration.getAppInfoClientID(), true, scopeList)

        //Consent Validate Request
        validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId)
        doConsentValidate(ConnectorTestConstants.PAYMENT_VALIDATE_PATH, validationPayload)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.IS_VALID),
                "false")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_CODE),
                "Bad Request")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_MESSAGE),
                "Consent is not in the correct state")
    }

    @Test
    void "Verify Validation of a created Consent when incorrect validation path sent"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        def host = configuration.getISServerUrl()

        //Consent Validate Request
        validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId)

        consentValidateResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequest(validationPayload))
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${accessToken}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.INCORRECT_ACCOUNT_VALIDATE_PATH)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Verify Validation of a created Consent without Content Type Header"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        //Consent Validate Request
        validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId)

        consentValidateResponse = FSRestAsRequestBuilder.buildRequest()
                .body(signedObject.getSignedRequest(validationPayload))
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${accessToken}")
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                )
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.PAYMENT_VALIDATE_PATH)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Verify Validation of a created Consent with Invalid Content Type Header"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        //Consent Validate Request
        validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId)

        consentValidateResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.HTML)
                .body(signedObject.getSignedRequest(validationPayload))
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${accessToken}")
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.PAYMENT_VALIDATE_PATH)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Verify Validation of a created Consent with Invalid Accept Type Header"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        //Consent Validate Request
        validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId)

        Response validationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequest(validationPayload))
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${accessToken}")
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .accept(ConnectorTestConstants.CONTENT_TYPE_JWT)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.PAYMENT_VALIDATE_PATH)

        Assert.assertEquals(validationResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_406)
    }

    @Test
    void "Verify Validation of a created Consent with mismatching user Id"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        //Consent Validate Request
        validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, "userId", consentId)
        doConsentValidate(ConnectorTestConstants.PAYMENT_VALIDATE_PATH, validationPayload)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.IS_VALID),
                "false")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_MESSAGE),
                "Invalid User Id")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_CODE),
                "Bad Request")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.HTTP_CODE),
                "400")
    }

    @Test
    void "Verify Validation of a created Consent with mismatching client Id"() {

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        //Consent Validate Request
        validationPayload = PaymentRequestPayloads.buildPaymentValidationPayload(accessToken, userId, consentId, "clientId")
        doConsentValidate(ConnectorTestConstants.PAYMENT_VALIDATE_PATH, validationPayload)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.IS_VALID),
                "false")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_MESSAGE),
                "Invalid Client Id")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_CODE),
                "Forbidden")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.HTTP_CODE),
                "403")
    }
}
