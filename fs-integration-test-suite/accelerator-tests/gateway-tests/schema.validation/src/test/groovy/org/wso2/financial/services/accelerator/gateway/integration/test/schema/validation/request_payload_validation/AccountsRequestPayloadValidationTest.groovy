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
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Request Payload Validation Tests.
 */
class AccountsRequestPayloadValidationTest extends FSAPIMConnectorTest {

	@BeforeClass
	void init() {
		consentPath = ConnectorTestConstants.AISP_PATH + "account-access-consents"
		initiationPayload = AccountsRequestPayloads.initiationPayload
		scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

		//Get application access token
		applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
				configuration.getAppInfoClientID(), scopeList)
	}

	@Test
	void "OB-976_API call with valid payload"() {

		//Do Consent Initiation
		doDefaultAccountInitiation()

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentId)
	}

	@Test
	void "OB-669_API call with optional payload params"() {

		//Do Consent Initiation
		doDefaultAccountInitiation()

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentId)
	}

	@Test
	void "OB-670_API call with missing mandatory params in the payload"() {

		//Do Consent Initiation
		consentResponse = doDefaultInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithoutPermissions)

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
			"Schema validation failed in the Request: [Path '/Data'] Object has missing " +
				"required properties ([\"Permissions\"]), ")
	}

	@Test
	void "OB-671_API call with empty json payload"() {

		consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadEmptyJsonPayload)

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
			"Schema validation failed in the Request: Object has missing required properties ([\"Data\",\"Risk\"]), ")
	}

	@Test
	void "OB-672_API call with invalid payload format"() {

		consentResponse = doDefaultInitiationWithUpdatedPayload("[]")

		Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
			"Schema validation failed in the Request: Instance type (array) does not match any allowed " +
				"primitive type (allowed: [\"object\"]), ")
	}

	@Test
	void "OB-673_API call without Data param in the payload"() {

		//Do Consent Initiation
		consentResponse = doDefaultInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithoutDataProperty)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
			"Schema validation failed in the Request: Object instance has properties which are not allowed by the schema:" +
				" [\"\"], Object has missing required properties ([\"Data\"]), ")
	}

	@Test
	void "OB-674_API call with empty request body"() {

		//Do Consent Initiation
		consentResponse = doDefaultInitiationWithUpdatedPayload("")

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
			"Schema validation failed in the Request: A request body is required but none found., ")
	}

	@Test
	void "OB-679_API call by passing unsupported enum value in the payload"() {

		//Do Consent Initiation
		consentResponse = doDefaultInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithInvalidPermission)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
			"Schema validation failed in the Request: [Path '/Data/Permissions/0'] Instance value " +
				"(\"ReadAccountsDetails\") not found in enum (possible values: [\"ReadAccountsDetail\",\"ReadBalances\"," +
				"\"ReadTransactionsDetail\"]), ")
	}

	@Test
	void "OB-682_API call by passing payload param with empty array"() {

		//Do Consent Initiation
		consentResponse = doDefaultInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithEmptyPermission)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
			"Schema validation failed in the Request: [Path '/Data/Permissions'] Array is too short: must have at " +
				"least 1 elements but instance has 0 elements, ")
	}

	@Test
	void "OB-684_API call by passing array param as a string element"() {

		//Do Consent Initiation
		consentResponse = doDefaultInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithoutArrayFormat)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION),
			"Schema validation failed in the Request: [Path '/Data/Permissions'] Instance type (string) does not " +
				"match any allowed primitive type (allowed: [\"array\"]), ")
	}

	@Test
	void "OB-686_API call by passing invalid date time for payload param"() {

		//Do Consent Initiation
		consentResponse = doDefaultInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithInvalidDate)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
				ConnectorTestConstants.BAD_REQUEST)
		Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION).contains(
			"invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z]"))
	}
}
