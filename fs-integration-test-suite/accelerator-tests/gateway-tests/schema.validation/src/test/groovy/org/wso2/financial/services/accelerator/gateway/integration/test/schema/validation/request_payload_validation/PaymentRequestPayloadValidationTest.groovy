/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.financial.services.accelerator.gateway.integration.test.schema.validation.request_payload_validation

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
 * Request Payload Validation Tests.
 */
class PaymentRequestPayloadValidationTest extends FSAPIMConnectorTest {

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
	void "OB-676_API call by passing -1 for amount param"() {

		def paymentMap = ["Amount":"-1"]
		def initiationPayload = PaymentRequestPayloads.initiationPayloadPayloadWithModifiableParams(paymentMap)

		doDefaultPaymentInitiationWithUpdatedPayload(initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
		Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
			.contains("does not match input string \"-1\""))
	}

	@Test
	void "OB-677_API call by passing integer value for amount"() {

		def paymentMap = ["Amount":"20"]
		def initiationPayload = PaymentRequestPayloads.initiationPayloadPayloadWithModifiableParams(paymentMap)

		doDefaultPaymentInitiationWithUpdatedPayload(initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
	}

	@Test
	void "OB-680_API call by passing value less than minimum length"() {

		def paymentMap = ["InstructionIdentification":""]
		def initiationPayload = PaymentRequestPayloads.initiationPayloadPayloadWithModifiableParams(paymentMap)

		doDefaultPaymentInitiationWithUpdatedPayload(initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
		Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
			.contains("String \"\" is too short (length: 0, required minimum: 1)"))
	}

	@Test
	void "OB-681_API call by passing value greater than maximum length"() {

		def paymentMap = ["InstructionIdentification":"ACME41212345678ACME412ACME412ACME412ACM" +
				"E412ACME412ACME412ACME412ACME412ACME41"]
		def initiationPayload = PaymentRequestPayloads.initiationPayloadPayloadWithModifiableParams(paymentMap)

		doDefaultPaymentInitiationWithUpdatedPayload(initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
		Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
			.contains("is too long (length: 77, maximum allowed: 35)"))
	}

	@Test
	void "OB-685_API call with incorrect reg exp pattern"() {

		def paymentMap = ["Currency":"RUPEE"]
		def initiationPayload = PaymentRequestPayloads.initiationPayloadPayloadWithModifiableParams(paymentMap)

		doDefaultPaymentInitiationWithUpdatedPayload(initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
		Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
				"Schema validation failed in the Request: [Path '/Data/Initiation/InstructedAmount/Currency'] ECMA " +
						"262 regex \"^[A-Z]{3,3}\$\" does not match input string \"RUPEE\""))
	}

	@Test
	void "OB-780_API call by passing empty string for optional SecondaryIdentification"() {

		def paymentMap = ["":""]
		def initiationPayload = RequestPayloads.initiationPayloadWithIntegerValueForString(paymentMap)

		doDefaultPaymentInitiationWithUpdatedPayload(initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
	}
}
