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

package org.wso2.financial.services.accelerator.gateway.test.payments.EndToEndTest

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PaymentRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * End to End Payments Flow Tests.
 */
class EndToEndPaymentsFlowTest extends FSAPIMConnectorTest {

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
    void "Verify Payments Initiation Request"() {

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test (dependsOnMethods = "Verify Payments Initiation Request")
    void "Verify Retrieving for a Created Payments Consent"() {

        doConsentRetrieval()
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID)

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, "AwaitingAuthorisation")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Payments Consent")
    void "Verify Authorize Consent in AwaitingAuthorisation Status"() {

        //Authorise Consent
        doPaymentConsentAuthorisation(scopeList)

        Assert.assertNotNull(code)
        Assert.assertNotNull(userAccessToken)
    }

    @Test (dependsOnMethods = "Verify Authorize Consent in AwaitingAuthorisation Status")
    void "Verify Retrieving for a Created Consent After authorizing"() {

        doConsentRetrieval()
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID)

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, "Authorised")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Consent After authorizing")
    void "Payments Submission"() {

        submissionPath = ConnectorTestConstants.PISP_PATH + ConnectorTestConstants.PAYMENT_SUBMISSION_PATH
        doDefaultPaymentSubmission(PaymentRequestPayloads.getSubmissionPaymentPayload(consentId))
        Assert.assertEquals(submissionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        paymentID = TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.DATA_PAYMENT_ID)

        Assert.assertNotNull(paymentID)
    }

    @Test (dependsOnMethods = "Payments Submission")
    void "Verify Retrieving Payments"() {

        submissionPath = ConnectorTestConstants.PISP_PATH + ConnectorTestConstants.PAYMENT_SUBMISSION_PATH + "/" + paymentID
        doDefaultPaymentIdRetrieval()
        Assert.assertEquals(submissionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

}
