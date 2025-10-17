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
package org.wso2.financial.services.accelerator.gateway.integration.test.schema.validation.request_header_validation

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

import java.time.OffsetDateTime

/**
 * Request Header Validation Tests
 */
class RequestHeaderValidationTest extends FSAPIMConnectorTest {


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
	void "OB-954_Send API call with all the mandatory headers"() {

		//Do Consent Initiation
		doDefaultAccountInitiation()

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentId)
	}

	@Test
	void "OB-647_Send API call without the mandatory Authorisation Header"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
				.contentType(ContentType.JSON)
				.header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
				.accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
				.header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
				.body(initiationPayload)
				.baseUri(configuration.getServerBaseURL())
				.post(consentPath)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.MISSING_CREDENTIALS)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
				"Invalid Credentials. Make sure your API invocation call has a header: 'null : Bearer ACCESS_TOKEN' " +
						"or 'null : Basic ACCESS_TOKEN' or 'ApiKey : API_KEY'")
	}

	@Test
	void "OB-651_Send API call with all the mandatory and optional headers"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
				.contentType(ContentType.JSON)
				.header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
				.header(ConnectorTestConstants.X_FAPI_AUTH_DATE, TestUtil.getDateTimeInHttpFormat())
				.header(ConnectorTestConstants.X_FAPI_IP_ADDRESS, TestUtil.getIpAddress())
				.header(ConnectorTestConstants.X_CUSTOMER_USER_AGENT, configuration.getUserPSUName())
				.body(initiationPayload)
				.baseUri(configuration.getServerBaseURL())
				.post(consentPath)

		consentId = TestUtil.parseResponseBody(consentResponse, "Data.ConsentId")

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentId)
	}

	@Test
	void "OB-652_API call without Bearer attribute for Authorisation header"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
				.contentType(ContentType.JSON)
				.header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Basic ${applicationAccessToken}")
				.body(initiationPayload)
				.baseUri(configuration.getServerBaseURL())
				.post(consentPath)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.MISSING_CREDENTIALS)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
				"Invalid Credentials. Make sure your API invocation call has a header: 'null : Bearer ACCESS_TOKEN' " +
						"or 'null : Basic ACCESS_TOKEN' or 'ApiKey : API_KEY'")
	}

	@Test
	void "OB-655_Send API call with unsupported mandatory header name"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
				.contentType(ContentType.JSON)
				.header("Auth", "Bearer ${applicationAccessToken}")
				.body(initiationPayload)
				.baseUri(configuration.getServerBaseURL())
				.post(consentPath)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.MISSING_CREDENTIALS)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
				"Invalid Credentials. Make sure your API invocation call has a header: 'null : Bearer ACCESS_TOKEN' " +
						"or 'null : Basic ACCESS_TOKEN' or 'ApiKey : API_KEY'")
	}

	@Test
	void "OB-656_API call with invalid date format for optional x-fapi-auth-date header"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
				.contentType(ContentType.JSON)
				.header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
				.header(ConnectorTestConstants.X_FAPI_AUTH_DATE, OffsetDateTime.now().toString())
				.body(initiationPayload)
				.baseUri(configuration.getServerBaseURL())
				.post(consentPath)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
				"Schema validation failed in the Request: ECMA 262 regex \"^(Mon|Tue|Wed|Thu|Fri|Sat|Sun)"))
	}

	@Test
	void "OB-776_API call without Content Type header"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
				.header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
				.body(initiationPayload)
				.baseUri(configuration.getServerBaseURL())
				.post(consentPath)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
				ConnectorTestConstants.STATUS_CODE_400.toString())
		Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
				.contains("Schema validation failed in the Request: Request Content-Type header '[text/plain; " +
						"charset=ISO-8859-1]' does not match any allowed types. Must be one of: [application/json; charset=utf-8]."))
	}

	@Test
	void "OB-777_API call with incorrect Authorisation Header format"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
				.contentType(ContentType.JSON)
				.header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer 1234")
				.body(initiationPayload)
				.baseUri(configuration.getServerBaseURL())
				.post(consentPath)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.INVALID_CREDENTIALS)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
				"Access failure for API: /open-banking/v3.1/aisp, version: v3.1 status: (900901) - " +
						"Invalid Credentials. Make sure you have provided the correct security credentials")
	}

	@Test
	void "OB-779_Send API call with invalid jwt for Authorisation header"() {

		//Do Consent Initiation
		consentResponse = FSRestAsRequestBuilder.buildRequest()
				.contentType(ContentType.JSON)
				.header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken + "!QAZ"}")
				.body(initiationPayload)
				.baseUri(configuration.getServerBaseURL())
				.post(consentPath)

		Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
				ConnectorTestConstants.INVALID_CREDENTIALS)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
				"Invalid Credentials. Make sure you have provided the correct security credentials")
	}
}
