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

package org.wso2.financial.services.accelerator.gateway.test.payments.Payment_Initiation_Tests

import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.keystore.KeyStore
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PaymentRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.JWSHeaders
import org.wso2.financial.services.accelerator.test.framework.utility.PaymentsDataProviders
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.time.Instant

import static org.wso2.financial.services.accelerator.test.framework.utility.PaymentsDataProviders.*

class PaymentsInitiationRequestHeaderValidationTests extends FSAPIMConnectorTest {

    List<ConnectorTestConstants.ApiScope> scopeList

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_API_PATH
        initiationPayload = RequestPayloads.initiationPaymentPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "OBA-900_Payment Initiation Request with valid x-jws-signature header"() {

        //initiation
        doDefaultInitiationForPayments(applicationAccessToken, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertNotNull(consentId)
    }

    @Test
    void "OBA-899_Payment Initiation Without x-jws-signature"() {

        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
                .contains(ConnectorTestConstants.X_JWS_SIGNATURE_MISSING))
    }

    @Test
    void "OBA-901_Payment Initiation with x-jws-signature header having unsupported alg"() {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.getRequestHeader(ConnectorTestConstants.RS256),
                        initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
                .contains("The RS256 algorithm is not supported"))
    }

    @Test
    void "OBA-902_Payment Initiation with x-jws-signature header having invalid kid"() {

        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(), "1234")

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
                .contains("kid does not resolve to a valid signing certificate"))
    }

    @Test
    void "OBA-903_Payment Initiation with x-jws-signature header having invalid iss"() {

        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
                configuration.getAppKeyStoreSigningKid(), "CN=0123456789HQQrZAAX")

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
                .contains("Error due to iss claim validation failed"))
    }

    @Test
    void "OBA-904_Payment Initiation with x-jws-signature header having invalid optional claims typ"() {

        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().minus(2).toString(), "JSON")

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
                .contains("Error occurred due to invalid type"))
    }

    @Test
    void "US-908_Payment Initiation with x-jws-signature header having invalid optional claims cty"() {

        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().minus(2).toString(),
                ConnectorTestConstants.TYP_JOSE,  "application/jwt")

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
                .contains("Error occurred due to invalid cty claim"))
    }

    @Test
    void "US-920_Payment Initiation with x-jws-signature header having present date and time for iat"() {

        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().toString())

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.CREATED)
    }

    @Test
    void "US-918_Payment Initiation with x-jws-signature header having future date for iat"() {

        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().plus(2).toString())

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
                .contains("iat claim cannot be a future date"))
    }

    @Test
    void "US-919_Payment Initiation with x-jws-signature header having past date for iat"() {

        String jwsHeader = jwsSignatureRequestBuilder.getRequestHeader(configuration.getCommonSigningAlgorithm(),
                configuration.getAppKeyStoreSigningKid(), KeyStore.getApplicationCertificateSubjectDn(),
                ConnectorTestConstants.JWS_TAN, Instant.now().getEpochSecond().minus(2).toString())

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.CREATED)
    }

    @Test
    void "US-906_Payment Initiation with x-jws-signature header having crit with unsupported claim"() {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,
                        TestUtil.generateXjwsSignature(JWSHeaders.jwsHeaderWithUnsupportedClaims, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "unrecognised critical parameter")
    }

    @Test
    void "US-913_Payment Initiation with x-jws-signature header having invalid tan"() {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,
                        TestUtil.generateXjwsSignature(JWSHeaders.jwsHeaderWithInvalidTan, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Error occurred due to invalid tan claim")
    }

    @Test(dataProvider = "jwsHeadersWithMissingCriticalClaims", dataProviderClass = PaymentsDataProviders.class)
    void "Initiation request with missing critical claims in x-jws-signature header"(String jwsHeader) {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE),
                ConnectorTestConstants.OBIE_ERROR_SIGNATURE_MISSING_CLAIM)
    }

    @Test(dataProvider = "jwsHeadersWithInvalidClaims", dataProviderClass = PaymentsDataProviders.class)
    void "Initiation request with invalid claims in x-jws-signature header"(String jwsHeader) {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
    }

    @Test
    void "Validate Payments Initiation With Invalid Authorisation Header"(){

        //Generate User Access token
        doDefaultInitiation()
        Assert.assertNotNull(consentId)

        doPaymentConsentAuthorisation(scopeList)
        Assert.assertNotNull(code)
        Assert.assertNotNull(userAccessToken)

        //initiation with user access token
        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE), "Claim Mismatch")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
                "The claim configured in the system and the claim provided in the token do not align. " +
                        "Please ensure the claims match.")
    }

    @Test
    void "Validate_Payments Initiation Without Authorisation Header"(){

        //initiation
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.UNAUTHORIZED)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
                .contains("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization : " +
                        "Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or 'ApiKey : API_KEY'"))
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE)
                .contains("Missing Credentials"))
    }

    //TODO: https://github.com/wso2/financial-services-accelerator/issues/681
    @Test
    void "Validate Payments Initiation With Invalid Content-type"() {

        //initiation
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.XML)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(ContentType.JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
        Assert.assertEquals(ConnectorTestConstants.OBIE_ERROR_HEADER_INVALID,
                TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE))
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE)
                .contains("Request Content-Type header does not match any allowed types"))
    }

    @Test
    void "Validate_Payments Initiation Request with duplicate x-idempotency-key"() {

        String idempotencyKey = TestUtil.idempotency

        //initiation request 1
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)

        //initiation request 2
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .accept(ContentType.JSON)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(jwsSignatureRequestBuilder.requestHeader,
                        PaymentRequestPayloads.modifiedInitiationPaymentPayload))
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .body(PaymentRequestPayloads.modifiedInitiationPaymentPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_CODE),
                ConnectorTestConstants.OBIE_ERROR_HEADER_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_MESSAGE),
                "Payloads are not similar. Hence this is not a valid idempotent request")
    }

    @Test
    void "TC0401099_Validate_Payments_Initiation_Request_with_same_payload_same_x-idempotency-key"() {

        String idempotencyKey = TestUtil.idempotency

        //initiation request 1
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        //initiation request 2
        Response consentResponse2 = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertEquals(consentResponse2.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID),
                TestUtil.parseResponseBody(consentResponse2, ConnectorTestConstants.DATA_CONSENT_ID))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS),
                TestUtil.parseResponseBody(consentResponse2, ConnectorTestConstants.DATA_STATUS))

    }

    @Test
    void "TC0401102_Initiation request with a valid JOSE header and an invalid signature"() {

        String idempotencyKey = TestUtil.idempotency

        //initiation
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .accept(ContentType.JSON)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateInvalidXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_CODE),
//                ConnectorTestConstants.OBIE_ERROR_SIGNATURE_INVALID)
    }


    @Test
    void "TC0401114_Validate_Payments_Initiation_With_Headers with Capital Case"() {

        def paymentMap = [:]

        //initiation
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY_CAPS, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE_CAPS,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID_CAPS, ConnectorTestConstants.INTERACTION_ID)
                .header(ConnectorTestConstants.X_FAPI_CUSTOMER_LAST_LOGGED_TIME_CAPS,
                        ConnectorTestConstants.X_FAPI_CUSTOMER_LAST_LOGGED_TIME_VALUE)
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
    }

    @Test
    void "TC0401115_Validate_Payments_Initiation_Request_with_same_payload_different_x-idempotency-key"() {

        String idempotencyKey = TestUtil.getIdempotency()

        //initiation request 1
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        String idempotencyKey2 = TestUtil.getIdempotency()

        //initiation request 2
        Response consentResponse2 = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey2)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertEquals(consentResponse2.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertNotEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID),
                TestUtil.parseResponseBody(consentResponse2, ConnectorTestConstants.DATA_CONSENT_ID))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS),
                TestUtil.parseResponseBody(consentResponse2, ConnectorTestConstants.DATA_STATUS))
    }
  
    @Test
    void "Validate_Payments Initiation request without x-idempotency-key"() {

        //initiation
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_JWS_SIGNATURE,TestUtil.generateXjwsSignature(
                        jwsSignatureRequestBuilder.requestHeader, initiationPayload))
                .body(initiationPayload)
                .baseUri(configuration.getServerBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE)
                .contains(ConnectorTestConstants.OBIE_ERROR_HEADER_MISSING))
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_MESSAGE).contains(
                "Schema validation failed in the Request: Header parameter 'x-idempotency-key' is required on path"))
    }
}
