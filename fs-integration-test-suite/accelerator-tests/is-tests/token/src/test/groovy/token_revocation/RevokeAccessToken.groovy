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

package token_revocation

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 *
 */
class RevokeAccessToken extends FSConnectorTest{

    String clientId
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    @BeforeClass
    void init() {
        //Create Regulatory Application with tls_client_auth method
        clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD)
    }

    @Test
    void "OBA-927_Revoke Application Access Token"() {

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD,
                clientId, [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertNotNull(accessToken)

        Response revokeResponse = TokenRequestBuilder.doTokenRevocation(accessToken.toString(),clientId)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Introspect the access token to verify it is revoked
        Response introspectResponse = getTokenIntrospectionResponse(accessToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")
    }

    @Test
    void "OBA-928_Revoke User Access Token"() {

        //Authorise the consent
        authoriseConsent(clientId)

        //Get User Access Token
        Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId,
                code, consentScopes)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
        Assert.assertNotNull(accessToken)

        //Revoke the user access token
        Response revokeResponse = TokenRequestBuilder.doTokenRevocation(accessToken.toString(),clientId)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Introspect the access token to verify it is revoked
        Response introspectResponse = getTokenIntrospectionResponse(accessToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")

        //Introspect the refresh token to verify it's status
        introspectResponse = getTokenIntrospectionResponse(refreshToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //TODO: Need to check the behaviour of the refresh token with IS team: https://github.com/wso2-enterprise/wso2-iam-internal/issues/3990
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")
    }

    @Test
    void "OBA-929_Revoke Refresh Token"() {

        //Authorise the consent
        authoriseConsent(clientId)

        //Get User Access Token
        Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId,
                code, consentScopes)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
        Assert.assertNotNull(accessToken)

        //Revoke the user refresh token
        Response revokeResponse = TokenRequestBuilder.doTokenRevocation(refreshToken.toString(),clientId)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Introspect the refresh token to verify it is revoked
        Response introspectResponse = getTokenIntrospectionResponse(refreshToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")

        //Introspect the access token to verify it's status
        introspectResponse = getTokenIntrospectionResponse(accessToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")
    }

    @Test
    void "OBA-930_Revoke already revoked token"() {

        //Authorise the consent
        authoriseConsent(clientId)

        //Get User Access Token
        Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId,
                code, consentScopes)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
        Assert.assertNotNull(accessToken)

        //Revoke the user access token
        Response revokeResponse = TokenRequestBuilder.doTokenRevocation(accessToken.toString(),clientId)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Introspect the access token to verify it is revoked
        Response introspectResponse = getTokenIntrospectionResponse(accessToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")

        //Introspect the refresh token to verify it's status
        introspectResponse = getTokenIntrospectionResponse(refreshToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Revoke the user access token which is already revoked
        revokeResponse = TokenRequestBuilder.doTokenRevocation(accessToken.toString(),clientId)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)


    }

    //Change the expiry time config and run the test
    @Test (enabled = false)
    void "OBA-931_Revoke expired access token"() {

        //Authorise the consent
        authoriseConsent(clientId)

        //Get User Access Token
        Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.TLS_AUTH_METHOD, clientId,
                code, consentScopes)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
        Assert.assertNotNull(accessToken)

        //Wait until the token expired
        sleep(300001)

        //Introspect the access token to verify it is expired
        Response introspectResponse = getTokenIntrospectionResponse(accessToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")

        //Revoke the user access token
        Response revokeResponse = TokenRequestBuilder.doTokenRevocation(accessToken.toString(),clientId)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "OBA-932_Revoke invalid access token"() {

        def invalidAccessToken = "dcsdcsvsde23131"

        //Revoke the user access token
        Response revokeResponse = TokenRequestBuilder.doTokenRevocation(invalidAccessToken,clientId)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    void authoriseConsent(String client_Id) {

        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
        //Consent initiation
        consentResponse = doConsentInitiation(initiationPayload)
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString()
        Assert.assertNotNull(consentId)

        //Consent Authorisation
        doConsentAuthorisation(client_Id, true, consentScopes)
        Assert.assertNotNull(code)
    }

    @AfterClass
    void cleanup() {
        //Delete the application created for the test
        deleteApplication(clientId, ConnectorTestConstants.TLS_AUTH_METHOD)
    }
}
