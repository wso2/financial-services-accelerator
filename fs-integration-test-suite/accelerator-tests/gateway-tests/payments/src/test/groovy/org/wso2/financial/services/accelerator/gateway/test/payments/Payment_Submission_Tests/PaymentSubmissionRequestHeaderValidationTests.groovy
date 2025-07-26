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
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.keystore.KeyStore
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PaymentRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.JWSHeaders
import org.wso2.financial.services.accelerator.test.framework.utility.PaymentsDataProviders
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.lang.reflect.Method
import java.time.Instant

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

    //Skip JWS Signature Validation tests if dcrEnabled is false
    void skipIfDCRDisabled(String testName) {
        boolean dcrEnabled = Boolean.parseBoolean(System.getProperty("dcrEnabled", "false"))
        if (!dcrEnabled) {
            println "⚠️ Skipping DCR test: $testName"
            throw new SkipException("Skipping DCR test: $testName because dcrEnabled=false")
        }
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
    void "OBA-916_Domestic Payment Submission Request with valid x-jws-signature header"() {

        //Payment Initiation and authorisation
        prePaymentSubmissionStep()
        Assert.assertNotNull(code)

        //Payment Submission
        doDefaultPaymentSubmission(userAccessToken, submissionPayload)

        Assert.assertEquals(submissionResponse.statusCode(),ConnectorTestConstants.CREATED)
        Assert.assertNotNull(paymentID)
    }

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
