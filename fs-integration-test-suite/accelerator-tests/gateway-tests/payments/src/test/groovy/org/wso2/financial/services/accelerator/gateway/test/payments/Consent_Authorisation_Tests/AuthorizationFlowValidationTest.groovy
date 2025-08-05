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

package org.wso2.financial.services.accelerator.gateway.test.payments.Consent_Authorisation_Tests


import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.automation.consent.AuthorizationFlowNavigationAutomationStep
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PaymentRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Authorization Flow Validation Tests.
 */
class AuthorizationFlowValidationTest extends FSAPIMConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_API_PATH
        initiationPayload = PaymentRequestPayloads.initiationPaymentPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Deny consent in consent authorisation"() {

        //Create Consent
        doDefaultPaymentInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        doPaymentConsentDeny(scopeList)
        Assert.assertEquals(denyResponse, "User+denied+the+consent")

        //Retrieve Consent Status
        doConsentRetrieval()
        Assert.assertEquals(consentStatus, "Rejected")
    }
}
