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
package org.wso2.financial.services.accelerator.gateway.integration.test.schema.validation.resource_path_validation

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Resource Path Validation Tests
 */
class ResourcePathValidationTest extends FSAPIMConnectorTest {

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
	void "OB-951_Send API call with valid base path uri"() {

		//Do Consent Initiation
		doDefaultAccountInitiation()

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentId)
	}

	@Test
	void "OB-644_Send API call with invalid base path uri"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
						.contentType(ContentType.JSON)
						.header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
						.body(initiationPayload)
						.baseUri(configuration.getServerBaseURL())
						.post("open-banking/v3.1/pisp/account-access-consents")

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_404)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
						"No matching resource found for given API Request")
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
				"Runtime Error")
	}

	@Test
	void "OB-645_Send API call with unsupported endpoint method"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
						.contentType(ContentType.JSON)
						.header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
						.baseUri(configuration.getServerBaseURL())
						.get(consentPath)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_405)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
						"Method not allowed for given API resource")
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
				"Runtime Error")
	}
}
