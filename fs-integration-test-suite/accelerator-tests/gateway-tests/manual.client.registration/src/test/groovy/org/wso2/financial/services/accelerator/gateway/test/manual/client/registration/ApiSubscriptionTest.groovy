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

package org.wso2.financial.services.accelerator.gateway.test.manual.client.registration

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ApiDevportalRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Test class to handle API subscription for applications in API Manager.
 */
class ApiSubscriptionTest extends FSAPIMConnectorTest{

    String adminUserName, clientId, clientSecret, adminUrl
    String accessToken
    List<String> scopesList
    ApiDevportalRequestBuilder apiDevportalRequestBuilder
    List<String> applicationIds
    List<String> apiIds

    @BeforeTest
    void init() {
        dcrPath = configuration.getApimServerUrl() + ConnectorTestConstants.INTERNAL_APIM_DCR_ENDPOINT
        adminUrl = configuration.getApimServerUrl() + ConnectorTestConstants.INTERNAL_APIM_ADMIN_ENDPOINT_V4
        adminUserName = configuration.getUserKeyManagerAdminName()

        scopesList = Arrays.asList("apim:admin", "apim:api_key", "apim:app_import_export",
                "apim:app_manage", "apim:store_settings", "apim:sub_alert_manage", "apim:sub_manage",
                "apim:subscribe", "openid", "apim:subscribe"
        )

        apiDevportalRequestBuilder = new ApiDevportalRequestBuilder()
    }

    @Test
    void "Create Application"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildIsAsKeyManagerRegistrationRequest()
                .body(ClientRegistrationRequestBuilder.getApimDcrClaims(adminUserName, ConnectorTestConstants.DEVPORTAL_CLIENT_NAME))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        clientId = TestUtil.parseResponseBody(registrationResponse, "clientId")
        clientSecret = TestUtil.parseResponseBody(registrationResponse, "clientSecret")
    }

    @Test(dependsOnMethods = "Create Application")
    void "Generate Access Token"(){

        Response accessTokenResponse = TokenRequestBuilder.getAccessTokenInApim(clientId, clientSecret, scopesList)

        Assert.assertEquals(accessTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(accessTokenResponse, "access_token")
        Assert.assertNotNull(accessToken, "Access token should not be null")
    }

    @Test(dependsOnMethods = "Generate Access Token")
    void "Subscribe APIs to Applications"() {

        //Get all the application Ids
        applicationIds = apiDevportalRequestBuilder.retrieveApplicationIds(accessToken)
        Assert.assertFalse(applicationIds.isEmpty(), "Application IDs should not be empty")

        //Get all the API Ids
        apiIds = apiDevportalRequestBuilder.retrieveApiIds(accessToken)
        Assert.assertFalse(apiIds.isEmpty(), "API IDs should not be empty")

        //Subscribe to APIs
        Response getApiResponse = apiDevportalRequestBuilder.subscribeToApis(accessToken, applicationIds, apiIds)
        Assert.assertEquals(getApiResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }
}
