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
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Authorisation Code Grant Access Token Test.
 */
class AuthorisationCodeGrantAccessTokenTest extends FSConnectorTest {

	String clientId
	String idToken
	ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

	void authoriseConsent(String client_Id) {

		consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
		initiationPayload = RequestPayloads.initiationPayload
		//Consent initiation
		consentResponse = doConsentInitiation(initiationPayload, clientId)
		consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
		Assert.assertNotNull(consentId)

		//Consent Authorisation
		doConsentAuthorisation(client_Id, true, consentScopes)
		Assert.assertNotNull(code)
	}

	@BeforeClass
	void setup() {
		//Create Regulatory Application with tls_client_auth method
		clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD)
	}

	@Test
	void "Generate authorization code grant access token with pkjwt authentication"() {

		authoriseConsent(clientId)
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId,
				code, consentScopes)

		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
		idToken = TestUtil.parseResponseBody(tokenResponse, "id_token")
		Assert.assertNotNull(accessToken)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
				ConnectorTestConstants.TOKEN_EXPIRY_TIME)
		Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
	}

	@Test (dependsOnMethods = "Generate authorization code grant access token with pkjwt authentication")
	void "Validate keyid of user access token jwt"() {

		HashMap<String, String> mapHeader = TestUtil.getJwtTokenHeader(accessToken)

		Assert.assertNotNull(mapHeader.get(ConnectorTestConstants.KID))
	}

	@Test (dependsOnMethods = "Validate keyid of user access token jwt")
	void "Validate additional claim binding to the user access token jwt"() {

		HashMap<String, String> mapPayload = TestUtil.getJwtTokenPayload(accessToken)

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

		Response tokenResponse = getTokenIntrospectionResponse(accessToken)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "active"), "true")
		Assert.assertNull(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.GRANT_TYPE))
		Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.CNF))
	}

	@Test(priority = 1)
	void "Generate authorization code grant access token without authorization code"() {

		//Get User Access Token
		Response tokenResponse = TokenRequestBuilder.getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
				consentScopes.stream().map { it.scopeString }.toList(), clientId, "")

		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Missing parameters: code")
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_REQUEST)
	}

	@Test(priority = 1)
	void "Generate authorization code grant access token with invalid authorization code"() {

		code = "77405043-f982-3252"

		//Get User Access Token
		Response tokenResponse = TokenRequestBuilder.getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
				consentScopes.stream().map { it.scopeString }.toList(), clientId, code)

		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Invalid authorization code received from token request")
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_GRANT)
	}

	@Test(priority = 1)
	void "Generate authorization code grant access token with an already used authorization code"() {

		//Do Consent Initiation and Authorisation
		authoriseConsent(clientId)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId,
				code, consentScopes)

		def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertNotNull(accessToken)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
				ConnectorTestConstants.TOKEN_EXPIRY_TIME)
		Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)

		//Get User Access Token for the same authorisation code
		Response tokenResponseNew = TokenRequestBuilder.getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
				consentScopes.stream().map { it.scopeString }.toList(), clientId, code)

		Assert.assertEquals(tokenResponseNew.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponseNew, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Inactive authorization code received from token request")
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponseNew, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_GRANT)
	}

	@Test (priority = 1)
	void "Generate authorization code grant access token by expired authorization code"() {

		//Do Consent Initiation and Authorisation
		authoriseConsent(clientId)

		sleep(300000)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId,
				code, consentScopes)

		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Expired or Revoked authorization code received from token request")
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_GRANT)
	}

	@Test (priority = 2)
	void "Generate authorization code grant access token for Regulatory Application"() {

		//delete application if exist
		if(clientId != null) {
			deleteApplication(clientId, ConnectorTestConstants.PKJWT_AUTH_METHOD)
		}

		clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD)

		authoriseConsent(clientId)

		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId,
				code, consentScopes)

		def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertNotNull(accessToken)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
				ConnectorTestConstants.TOKEN_EXPIRY_TIME)
		Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
	}

	@Test (priority = 3)
	void "Generate authorization code grant access token with code bound to deleted app"() {

		//delete application if exist
		if(clientId != null) {
			deleteApplication(clientId, ConnectorTestConstants.PKJWT_AUTH_METHOD)
		}

		clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD)

		//Do Consent Initiation and Authorisation
		authoriseConsent(clientId)

		deleteApplication(clientId, ConnectorTestConstants.PKJWT_AUTH_METHOD)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId,
				code, consentScopes)

		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)

		String actualErrorResponse = TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION)

		boolean isValidResponse = actualErrorResponse.equals("Client credentials are invalid.")
				|| actualErrorResponse.equals("A valid OAuth client could not be found for client_id: deleted_client_id")

		Assert.assertTrue(isValidResponse)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_CLIENT)
	}
}
