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

package org.wso2.financial.services.accelerator.test.preconfiguration.steps

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * This class is used to create a Key Manager in WSO2 API Manager.
 * It registers an application, generates an access token, and then creates a Key Manager.
 * It also disables the resident Key Manager after creating a new one.
 */
class CreateKeyManager extends FSAPIMConnectorTest{

    String adminUserName, clientId, clientSecret, adminUrl
    String accessToken
    List<String> scopesList

    @BeforeTest
    void init() {
        dcrPath = configuration.getApimServerUrl() + ConnectorTestConstants.INTERNAL_APIM_DCR_ENDPOINT
        adminUrl = configuration.getApimServerUrl() + ConnectorTestConstants.INTERNAL_APIM_ADMIN_ENDPOINT_V4
        adminUserName = configuration.getUserIsAsKeyManagerAdminName()

        scopesList = Arrays.asList("apim:admin", "apim:admin_alert_manage", "apim:admin_application_view",
                "apim:admin_operations", "apim:admin_settings", "apim:admin_tier_manage", "apim:admin_tier_view",
                "apim:api_category", "apim:api_import_export", "apim:api_product_import_export", "apim:api_provider_change",
                "apim:api_workflow_approve", "apim:api_workflow_view", "apim:app_import_export", "apim:app_owner_change",
                "apim:bl_manage", "apim:bl_view", "apim:bot_data", "apim:environment_manage", "apim:environment_read",
                "apim:gov_policy_manage", "apim:gov_policy_read", "apim:gov_result_read", "apim:gov_rule_manage",
                "apim:gov_rule_read", "apim:keymanagers_manage", "apim:llm_provider_manage", "apim:mediation_policy_create",
                "apim:mediation_policy_view", "apim:monetization_usage_publish", "apim:organization_manage", "apim:organization_read",
                "apim:policies_import_export", "apim:role_manage", "apim:scope_manage", "apim:tenantInfo", "apim:tenant_theme_manage",
                "apim:tier_manage", "apim:tier_view", "openid"
        )
    }

    @Test
    void "Create Application"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildIsAsKeyManagerRegistrationRequest()
                .body(ClientRegistrationRequestBuilder.getApimDcrClaims(adminUserName))
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
    void "Generate Key Manager"(){

        Response keyManagerResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(RequestPayloads.keyManagerPayload())
                .post(adminUrl + "/key-managers")

        Assert.assertEquals(keyManagerResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(keyManagerResponse, "id"))
    }

    @Test(dependsOnMethods = "Generate Key Manager")
    void "Disable Resident Key Manager"() {

        RestAssured.defaultParser = Parser.JSON

        //Get the Id of Resident Key Manager
        Response residentKeyManagerResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .get(adminUrl + "/key-managers")

        String keyManagerId = residentKeyManagerResponse.jsonPath().getString("list.find { it.type == 'default' }.id")

        //Get the payload of the Resident Key Manager
        Response residentKeyManagerPayloadResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .get(adminUrl + "/key-managers/" + keyManagerId)

        //Convert response to Map and disable the key manager
        String responseBody = residentKeyManagerPayloadResponse.getBody().asString();
        if (responseBody == null || responseBody.trim().isEmpty()) {
            System.out.println("Response body is empty. Cannot convert to Map.")
            return
        }

        // Convert JSON body to a Map
        Map<String, Object> payload = residentKeyManagerPayloadResponse.as(Map.class)
        payload.put("enabled", false)

        //Convert map back to JSON string
        ObjectMapper mapper = new ObjectMapper()
        String updatedPayload = mapper.writeValueAsString(payload)

        // Disable the Resident Key Manager
        Response disableKeyManagerResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(updatedPayload)
                .put(adminUrl + "/key-managers/" + keyManagerId)

        Assert.assertEquals(disableKeyManagerResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)

        //Verify the resident key manager is disabled
        residentKeyManagerPayloadResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .get(adminUrl + "/key-managers/" + keyManagerId)

        Assert.assertEquals(residentKeyManagerPayloadResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(residentKeyManagerPayloadResponse, "enabled"), "false",
                "Resident Key Manager should be disabled")
    }
}
