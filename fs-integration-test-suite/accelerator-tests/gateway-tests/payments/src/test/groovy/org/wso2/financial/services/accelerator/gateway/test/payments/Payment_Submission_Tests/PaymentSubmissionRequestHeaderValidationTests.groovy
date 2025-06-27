/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 *
 */
/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 *
 */

package org.wso2.financial.services.accelerator.gateway.test.payments.Payment_Submission_Tests

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PaymentRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Tests for validating the request headers in payment submission requests.
 */
class PaymentSubmissionRequestHeaderValidationTests extends FSAPIMConnectorTest {

    List<ConnectorTestConstants.ApiScope> scopeList
    String registrationPath
    String SSA

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_API_PATH
        initiationPayload = PaymentRequestPayloads.initiationPaymentPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)
        submissionPath = ConnectorTestConstants.PISP_PATH + ConnectorTestConstants.PAYMENT_SUBMISSION_PATH
    }

    void prePaymentSubmissionStep(){

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)

        //initiation
        doDefaultInitiationForPayments(applicationAccessToken, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertNotNull(consentId)

        doPaymentConsentAuthorisation(scopeList)
        Assert.assertNotNull(userAccessToken)

        submissionPayload = PaymentRequestPayloads.getSubmissionPaymentPayload(consentId)
    }

    @Test
    void "OBA-915_Payment Submission Without x-jws-signature"() {

        prePaymentSubmissionStep()
        Assert.assertNotNull(code)

        def submissionResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .body(submissionPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(submissionPath)

        Assert.assertEquals(submissionResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_ERRORS_MSG)
                .contains(ConnectorTestConstants.X_JWS_SIGNATURE_MISSING))
    }

    @Test
    void "OBA-916_Domestic Payment Submission Request with valid x-jws-signature header"() {

        //Payment Initiation and authorisation
        prePaymentSubmissionStep()
        Assert.assertNotNull(code)

        //Payment Submission
        doDefaultPaymentSubmission(userAccessToken, submissionPayload)

        Assert.assertEquals(submissionResponse.statusCode(),ConnectorTestConstants.CREATED)
        Assert.assertNotNull(paymentID)
    }

//    @Test
    void "OBA-796_Payment Submission request with Consent bound to deleted application"() {

        configuration.setTppNumber(2)
        registrationPath = configuration.getServerBaseURL() + ConnectorTestConstants.REGISTRATION_ENDPOINT
        SSA = new File(configuration.getAppDCRSSAPath()).text

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA))
                .when()
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        //Payment Initiation and authorisation
        prePaymentSubmissionStep()
        Assert.assertNotNull(code)

        //Delete the application
        def deleteResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForGetAndDelete(applicationAccessToken)
                .delete(registrationPath + "/" + clientId)

        Assert.assertEquals(deleteResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)

        //Send Payment Submission
        submissionResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil
                        .generateXjwsSignature(jwsSignatureRequestBuilder.requestHeader, submissionPayload))
                .body(submissionPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(submissionPath)

        Assert.assertEquals(deleteResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

//    @Test
//    void "Payment submission with x-jws-signature header having crit with unsupported claim"() {
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,
//                        TestUtil.generateXjwsSignature(JWSHeaders.jwsHeaderWithUnsupportedClaims, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "unrecognised critical parameter")
//    }
//
//    @Test
//    void "Payment submission with x-jws-signature header having unsupported alg"() {
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
//                        jwsSignatureRequestBuilder.getRequestHeader(ConnectorTestConstants.RS256),
//                        submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
//                .contains("The RS256 algorithm is not supported"))
//    }
//
//    @Test
//    void "Payment Submission with x-jws-signature header having invalid kid"() {
//
//        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(), "1234")
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
//                .contains("kid does not resolve to a valid signing certificate"))
//    }
//
//    @Test
//    void "Payment Submission with x-jws-signature header having invalid iss"() {
//
//        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
//                configuration.getAppKeyStoreSigningKid(), "CN=0123456789HQQrZAAX")
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
//                .contains("Error due to iss claim validation failed"))
//    }
//
//    @Test
//    void "Payment Submission with x-jws-signature header having invalid optional claims typ"() {
//
//        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
//                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
//                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().minus(2).toString(), "JSON")
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
//                .contains("Error occurred due to invalid type"))
//    }
//
//    @Test
//    void "US-908_Payment Submission with x-jws-signature header having invalid optional claims cty"() {
//
//        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
//                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
//                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().minus(2).toString(),
//                ConnectorTestConstants.TYP_JOSE,  "application/jwt")
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
//                .contains("Error occurred due to invalid cty claim"))
//    }
//
//    @Test
//    void "US-920_Payment Submission with x-jws-signature header having present date and time for iat"() {
//
//        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
//                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
//                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().toString())
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.CREATED)
//    }
//
//    @Test
//    void "US-918_Payment Submission with x-jws-signature header having future date for iat"() {
//
//        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
//                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
//                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().plus(2).toString())
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
//                .contains("iat claim cannot be a future date"))
//    }
//
//    @Test
//    void "US-919_Payment Submission with x-jws-signature header having past date for iat"() {
//
//        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
//                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
//                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().minus(2).toString())
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.CREATED)
//    }
//
//    @Test
//    void "US-906_Payment Submission with x-jws-signature header having crit with unsupported claim"() {
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,
//                        TestUtil.generateXjwsSignature(JWSHeaders.jwsHeaderWithUnsupportedClaims, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "unrecognised critical parameter")
//    }
//
//    @Test
//    void "US-913_Payment Submission with x-jws-signature header having invalid tan"() {
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,
//                        TestUtil.generateXjwsSignature(JWSHeaders.jwsHeaderWithInvalidTan, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "Error occurred due to invalid tan claim")
//    }
//
//    @Test(dataProvider = "jwsHeadersWithMissingCriticalClaims", dataProviderClass = PaymentsDataProviders.class)
//    void "Submission request with missing critical claims in x-jws-signature header"(String jwsHeader) {
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE),
//                ConnectorTestConstants.OBIE_ERROR_SIGNATURE_MISSING_CLAIM)
//    }
//
//    @Test(dataProvider = "jwsHeadersWithInvalidClaims", dataProviderClass = PaymentsDataProviders.class)
//    void "Submission request with invalid claims in x-jws-signature header"(String jwsHeader) {
//
//        prePaymentSubmissionStep()
//        Assert.assertNotNull(code)
//
//        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
//                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
//                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, submissionPayload))
//                .body(submissionPayload)
//                .baseUri(configuration.getServerBaseURL())
//                .post(submissionPath)
//
//        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
//    }

    @Test
    void "Payment Submission Request with mismatching initiation payload"() {

        //Payment Initiation and authorisation
        prePaymentSubmissionStep()
        Assert.assertNotNull(code)

        String submissionPayload = PaymentRequestPayloads.getModifiedSubmissionPaymentPayload(consentId)

        //Payment Submission
        submissionResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil
                        .generateXjwsSignature(jwsSignatureRequestBuilder.requestHeader, submissionPayload))
                .body(submissionPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(submissionPath)

        Assert.assertEquals(submissionResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Consent Enforcement Error")
        Assert.assertEquals(TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Initiation payloads does not match")
    }
}
