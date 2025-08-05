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

package grant_types

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Refresh Token Grant Access Token Test.
 */
class RefreshTokenGrantAccessToken extends FSAPIMConnectorTest {

    String clientId
    String refreshToken
    String idToken
    String refreshedAccessToken
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    void authoriseConsent(String client_id) {

		//Get Application Access Token
		applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
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
	void "Generate refresh token grant access token with pkjwt authentication"() {

		authoriseConsent(clientId)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, code, scopeList)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
		Assert.assertNotNull(accessToken)
		Assert.assertNotNull(refreshToken)

        Response refreshTokenResponse = getRefreshTokenGrantToken(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId,
				refreshToken.toString(), scopeList)

		Assert.assertEquals(refreshTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        refreshedAccessToken = TestUtil.parseResponseBody(refreshTokenResponse, "access_token")
        idToken = TestUtil.parseResponseBody(refreshTokenResponse, "id_token")
		Assert.assertNotNull(refreshedAccessToken)
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, "expires_in").toString(),
						ConnectorTestConstants.TOKEN_EXPIRY_TIME)
		Assert.assertNotNull(TestUtil.parseResponseBody(refreshTokenResponse, "scope"))
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, "token_type"), ConnectorTestConstants.BEARER)
	}

	@Test (dependsOnMethods = "Generate refresh token grant access token with pkjwt authentication")
	void "Validate keyid of refresh token grant access token jwt"() {

		HashMap<String, String> mapHeader = TestUtil.getJwtTokenHeader(refreshedAccessToken)

		Assert.assertNotNull(mapHeader.get(ConnectorTestConstants.KID))
	}

	@Test (dependsOnMethods = "Validate keyid of refresh token grant access token jwt")
	void "Validate additional claim binding to the refresh token grant access token jwt"() {

		HashMap<String, String> mapPayload = TestUtil.getJwtTokenPayload(refreshedAccessToken)

		Assert.assertTrue(mapPayload.get(ConnectorTestConstants.CNF).matches("x5t#S256:[a-zA-Z0-9-]+"))
	}


	@Test (dependsOnMethods = "Validate additional claim binding to the refresh token grant access token jwt")
	void "Introspection call for user access token generated from refresh token"() {

		Response introspectionResponse = getTokenIntrospectionResponse(refreshedAccessToken)
		Assert.assertEquals(introspectionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertEquals(TestUtil.parseResponseBody(introspectionResponse, "active"), "true")
		Assert.assertNull(TestUtil.parseResponseBody(introspectionResponse, ConnectorTestConstants.GRANT_TYPE))
		Assert.assertNotNull(TestUtil.parseResponseBody(introspectionResponse, ConnectorTestConstants.CNF))
	}

	@Test (dependsOnMethods = "Introspection call for user access token generated from refresh token")
	void "Send Account Retrieval request using access token"() {

		userAccessToken = refreshedAccessToken
		accountsPath = ConnectorTestConstants.AISP_PATH + "accounts"
		doDefaultAccountRetrieval()

		Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
	}


	@Test(priority = 2)
	void "OB-1135_Generate refresh token grant access token for a revoked user access token"() {

		//Consent initiation and authorisation
		authoriseConsent(clientId)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, code, scopeList)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
		refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
		Assert.assertNotNull(accessToken)
		Assert.assertNotNull(refreshToken)

		//Revoke the token
		Response revokeResponse = TokenRequestBuilder.doTokenRevocation(accessToken.toString(),clientId, ConnectorTestConstants.PKJWT_AUTH_METHOD)
		Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

		//Get Refresh Token Grant User Access Token
		Response refreshTokenResponse = getRefreshTokenGrantToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
				clientId, refreshToken.toString(), scopeList)

		Assert.assertEquals(refreshTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Persisted access token data not found")
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_GRANT)
	}
}
