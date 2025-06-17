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

package grant_type_validation

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils

/**
 * Authorisation Code Grant Access Token Test.
 */
class AuthorisationCodeGrantAccessTokenTest extends FSAPIMConnectorTest {

	String clientId
	String idToken

    private ConfigurationService configuration = new ConfigurationService()

	void authoriseConsent() {

		//Get Application Access Token
		applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.TLS_AUTH_METHOD,
				configuration.getAppInfoClientID(), scopeList)

		//Consent initiation
		doDefaultAccountInitiation()
		Assert.assertNotNull(consentId)

		//Consent Authorisation
		doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)
		Assert.assertNotNull(code)
	}

	@BeforeClass
	void setup() {

		consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
		initiationPayload = RequestPayloads.initiationPayload
		scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

		//Create Regulatory Application with tls_client_auth method
		clientId = configuration.getAppInfoClientID()
	}

	@Test
	void "Generate authorization code grant access token with pkjwt authentication"() {

		authoriseConsent()
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, code, scopeList)

		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		userAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
		idToken = TestUtil.parseResponseBody(tokenResponse, "id_token")
		Assert.assertNotNull(userAccessToken)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
				ConnectorTestConstants.TOKEN_EXPIRY_TIME)
		Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
	}

	@Test (dependsOnMethods = "Generate authorization code grant access token with pkjwt authentication")
	void "Validate keyid of user access token jwt"() {

		HashMap<String, String> mapHeader = TestUtil.getJwtTokenHeader(userAccessToken)

		Assert.assertNotNull(mapHeader.get(ConnectorTestConstants.KID))
	}

	@Test (dependsOnMethods = "Validate keyid of user access token jwt")
	void "Validate additional claim binding to the user access token jwt"() {

		HashMap<String, String> mapPayload = TestUtil.getJwtTokenPayload(userAccessToken)

		Assert.assertTrue(mapPayload.get(ConnectorTestConstants.CNF).matches("x5t#S256:[a-zA-Z0-9-]+"))
	}

	@Test (dependsOnMethods = "Validate additional claim binding to the user access token jwt")
	void "Validate additional claims not binding to the id_token of user access token"() {

		HashMap<String, String> mapPayload = TestUtil.getJwtTokenPayload(idToken)

		Assert.assertNull(mapPayload.get(ConnectorTestConstants.CNF))
		Assert.assertNull(mapPayload.get(ConnectorTestConstants.CONSENT_ID))
	}

	@Test (dependsOnMethods = "Validate additional claims not binding to the id_token of user access token")
	void "Introspection call for user access token"() {

		Response tokenResponse = TokenRequestBuilder.getTokenIntrospectionResponse(userAccessToken)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "active"), "true")
		Assert.assertNull(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.GRANT_TYPE))
		Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.CNF))
	}

	@Test (dependsOnMethods = "Introspection call for user access token")
	void "Revoke user access token"() {

		Response revocationResponse = TokenRequestBuilder.doTokenRevocation(userAccessToken, clientId)
		Assert.assertEquals(revocationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
	}

	@Test (dependsOnMethods = "Introspection call for user access token")
	void "Send Account Retrieval request using revoked access token"() {

		accountsPath = ConnectorTestConstants.AISP_PATH + "accounts"
		doDefaultAccountRetrieval()

		Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
	}
}
