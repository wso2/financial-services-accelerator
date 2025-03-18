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
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Refresh Token Grant Access Token Test.
 */
class RefreshTokenGrantAccessToken extends FSConnectorTest {

    String clientId
    String refreshToken
    String idToken
    String refreshedAccessToken
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    void authoriseConsent() {

        clientId = configuration.getAppInfoClientID()
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
        //Consent initiation
        doDefaultInitiation(initiationPayload)
        Assert.assertNotNull(consentId)

        //Consent Authorisation
		doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        Assert.assertNotNull(code)
    }

	@Test
	void "Generate refresh token grant access token with pkjwt authentication"() {

		authoriseConsent()

		//Get User Access Token
        Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, code, consentScopes)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
		Assert.assertNotNull(accessToken)
		Assert.assertNotNull(refreshToken)

        Response refreshTokenResponse = getRefreshGrantTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, [scope],
				refreshToken.toString())

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

	@Test (enabled = true)
	void "Generate refresh token grant access token for Regulatory Application"() {

		configuration.setTppNumber(1)
		doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId, code, consentScopes)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertNotNull(accessToken)
		Assert.assertNotNull(refreshToken)

		Response refreshTokenResponse = getRefreshGrantTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId, [scope],
				refreshToken.toString())

		Assert.assertEquals(refreshTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertNotNull(accessToken)
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, "expires_in").toString(),
				ConnectorTestConstants.TOKEN_EXPIRY_TIME)
		Assert.assertNotNull(TestUtil.parseResponseBody(refreshTokenResponse, "scope"))
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, "token_type"), ConnectorTestConstants.BEARER)
	}

	@Test
	void "Generate refresh token grant access token without refresh token"() {

		//Do Consent Initiation
		configuration.setTppNumber(1)
		doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId, code, consentScopes)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertNotNull(accessToken)
		Assert.assertNotNull(refreshToken)

		//Get Refresh Token Grant User Access Token
		Response refreshTokenResponse = getRefreshGrantTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId, [scope],
				"")

		Assert.assertEquals(refreshTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Missing parameters: refresh_token")
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_REQUEST)
	}

	@Test
	void "Generate refresh token grant access token with invalid refresh token"() {

		//Do Consent Initiation
		configuration.setTppNumber(1)
		doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

		//Get Refresh Token Grant User Access Token
		String invalidRefreshToken = "09309de5-b7b5-3310-8767-6b14b3dc7fe8"
		Response refreshTokenResponse = getRefreshGrantTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId, [scope],
				invalidRefreshToken)

		Assert.assertEquals(refreshTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Persisted access token data not found")
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_GRANT)
	}

	@Test
	void "Generate refresh token grant access token without client id"() {

		//Do Consent Initiation
		configuration.setTppNumber(1)
		doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId, code, consentScopes)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertNotNull(accessToken)
		Assert.assertNotNull(refreshToken)

		//Get Refresh Token Grant User Access Token
		Response refreshTokenResponse = getRefreshGrantTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, "", [scope],
				refreshToken.toString())

		Assert.assertEquals(refreshTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Client ID not found in the request.")
		Assert.assertEquals(TestUtil.parseResponseBody(refreshTokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_CLIENT)
	}

	@Test
	void "Generate refresh token grant access token for a revoked user access token"() {

		//Do Consent Initiation
		configuration.setTppNumber(1)
		doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

		//Get User Access Token
		Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId, code, consentScopes)
		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
		Assert.assertNotNull(accessToken)
		Assert.assertNotNull(refreshToken)

		//Revoke the token
		Response revokeResponse = TokenRequestBuilder.doTokenRevocation(accessToken.toString(),configuration.getAppInfoClientID())
		Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

		//Get Refresh Token Grant User Access Token
		Response refreshTokenResponse = getRefreshGrantTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD,
				configuration.getAppInfoClientID(), [scope], refreshToken.toString())

		Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
				"Persisted access token data not found")
		Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
				ConnectorTestConstants.INVALID_GRANT)
	}

}
