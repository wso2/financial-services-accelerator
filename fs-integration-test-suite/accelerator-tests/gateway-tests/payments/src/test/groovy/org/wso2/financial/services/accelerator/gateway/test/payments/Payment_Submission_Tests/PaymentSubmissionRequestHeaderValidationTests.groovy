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
        Assert.assertTrue(TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_MESSAGE)
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
}
