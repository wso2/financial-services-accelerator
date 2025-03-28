/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.financial.services.accelerator.is.test.dcr

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset

/**
 * Dynamic Client Registration End to End Flow Tests.
 */
class RegistrationDetailsRetrievalTests extends FSConnectorTest {

    public List<ConnectorTestConstants.ApiScope> consentScopes = [
            ConnectorTestConstants.ApiScope.ACCOUNTS
    ]
    ClientRegistrationRequestBuilder registrationRequestBuilder

    @BeforeClass
    void generateAccessToken() {

        dcrPath = ConnectorTestConstants.REGISTRATION_URL
        registrationRequestBuilder = new ClientRegistrationRequestBuilder()

        def ssa = new File(configuration.getAppDCRSSAPath()).text

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa))
                .post(dcrPath)

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)

        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, consentScopes)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
    }

    @Test
    void "TC0102002_Retrieve registration details with invalid clientId"() {

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .get(dcrPath + "invalid_client_id")

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "TC0102003_Retrieve registration details with an invalid access token"() {

        def authToken = "${configuration.getUserPSUName()}:" +
                "${configuration.getUserPSUPWD()}"

        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest(basicHeader)
                .get(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @AfterClass
    void cleanUp() {
        Response tokenResponse = getApplicationAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                clientId, consentScopes)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)

        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .delete(dcrPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)
    }
}
