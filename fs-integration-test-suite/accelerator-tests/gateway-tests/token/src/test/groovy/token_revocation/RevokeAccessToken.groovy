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
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 *
 */
class RevokeAccessToken extends FSAPIMConnectorTest{

    String clientId
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)
    }

    @Test
    void "OBA-927_Revoke Application Access Token"() {

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), [scope])

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        applicationAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertNotNull(applicationAccessToken)

        Response revokeResponse = TokenRequestBuilder.doTokenRevocation(applicationAccessToken.toString(), clientId,
                "access_token", ConnectorTestConstants.PKJWT_AUTH_METHOD)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Introspect the access token to verify it is revoked
        Response introspectResponse = getTokenIntrospectionResponse(applicationAccessToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")

        //TODO: https://github.com/wso2/financial-services-accelerator/issues/689
        //Create Accounts Initiation
//        doDefaultAccountInitiation()
//        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "OBA-928_Revoke User Access Token"() {

        //Get User Access Token
        authoriseConsent()

        //Revoke the user access token
        Response revokeResponse = TokenRequestBuilder.doTokenRevocation(userAccessToken.toString(), clientId)
        Assert.assertEquals(revokeResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Introspect the access token to verify it is revoked
        Response introspectResponse = getTokenIntrospectionResponse(userAccessToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")

        //Introspect the refresh token to verify it's status
        introspectResponse = getTokenIntrospectionResponse(refreshToken)
        Assert.assertEquals(introspectResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //TODO: Need to check the behaviour of the refresh token with IS team: https://github.com/wso2-enterprise/wso2-iam-internal/issues/3990
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse, "active"), "false")

        accountsPath = ConnectorTestConstants.TRANSACTIONS_SINGLE_PATH
        doDefaultAccountRetrieval()
        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "OBA-929_Revoke Refresh Token"() {

        //Get User Access Token
        authoriseConsent()

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

        //Refresh the token after token revocation
        Response refreshResponse = getRefreshTokenGrantToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), refreshToken.toString(), scopeList)

        Assert.assertEquals(refreshResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(refreshResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Persisted access token data not found")
    }

    @Test
    void "OBA-930_Revoke already revoked token"() {

        //Get User Access Token
        authoriseConsent()

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

        //Get User Access Token
        authoriseConsent()

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

    @Test
    void "Not revoking access token upon creation of new token when the renew_token_without_revoking_existing configuration is enabled"(){

        //Get User Access Token
        authoriseConsent()

        //Introspect the access token to verify it is revoked
        def userAccessToken1 = userAccessToken

        Response introspectResponse1 = getTokenIntrospectionResponse(userAccessToken1)
        Assert.assertEquals(introspectResponse1.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse1, "active"), "true")

        //Get a New User Access Token
        authoriseConsent()

        //Introspect the access token to verify it is revoked
        def userAccessToken2 = userAccessToken

        //Introspect the second access token to verify it is revoked
        Response introspectResponse2 = getTokenIntrospectionResponse(userAccessToken2)
        Assert.assertEquals(introspectResponse2.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponse2, "active"), "true")

        //Verify the first access token is not revoked
        Response introspectResponseForFirstToken = getTokenIntrospectionResponse(userAccessToken1)
        Assert.assertEquals(introspectResponseForFirstToken.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(introspectResponseForFirstToken, "active"), "true")

        //Retrieve accounts from first user access token
        accountsPath = ConnectorTestConstants.TRANSACTIONS_SINGLE_PATH
        doAccountRetrievalWithDefinedAccessToken(userAccessToken1)
        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Retrieve accounts from second user access token
        accountsPath = ConnectorTestConstants.TRANSACTIONS_SINGLE_PATH
        doAccountRetrievalWithDefinedAccessToken(userAccessToken2)
        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    /**
     * Authorises the consent and retrieves the user access token.
     */
    void authoriseConsent() {

        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)

        //Initiate the consent
        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)

        //Authorise Consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)
        Assert.assertNotNull(code)

        //Get User Access Token
        Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code.toString(), scopeList)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        userAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        refreshToken = TestUtil.parseResponseBody(tokenResponse, "refresh_token")
        Assert.assertNotNull(userAccessToken)
    }
}
