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

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset

class CreateConsentAPIAccessRole extends FSAPIMConnectorTest {

    String roleManagementUrl
    String apiResourceUrl
    String roleId
    def basicHeader
    String apiResourceName = "OB-internal-api-resource"
    String scope = "ob-internal-api-access"
    String roleName = "OBInternalApiAccessRole"

    @BeforeClass
    void init() {
        roleManagementUrl = configuration.getISServerUrl() + ConnectorTestConstants.INTERNAL_APIM_SCIME2_ROLES_ENDPOINT
        apiResourceUrl = configuration.getISServerUrl() + ConnectorTestConstants.INTERNAL_API_RESOURCE_ENDPOINT

        def authToken = "${configuration.getUserKeyManagerAdminName()}:" +
                "${configuration.getUserKeyManagerAdminPWD()}"

        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"
    }

    @Test
    void "Create API Resource in IS console"() {

        Response apiResourceResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .contentType("application/json; charset=UTF-8")
                .accept("application/json")
                .body(RequestPayloads.getAPIResourcePayload(apiResourceName, scope))
                .post(apiResourceUrl)

        Assert.assertEquals(apiResourceResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        String apiResourceId = apiResourceResponse.jsonPath().getString("id")
        Assert.assertNotNull(apiResourceId, "API Resource ID should not be null")
    }

    @Test(dependsOnMethods = "Create API Resource in IS console")
    void "Create OBInternalApiAccessRole Role in IS console"() {

        //Retrieve organization ID
        Response orgDetailResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .accept("application/json")
                .baseUri(configuration.getISServerUrl())
                .get("api/users/v1/me/organizations/root/descendants")

        Assert.assertEquals(orgDetailResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        String orgId = TestUtil.parseResponseBody(orgDetailResponse, "[0].id")

        Response roleCreationResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .contentType("application/scim+json; charset=UTF-8")
                .accept("application/scim+json")
                .body(RequestPayloads.getRoleCreationPayload(roleName, orgId))
                .post(roleManagementUrl)

        Assert.assertEquals(roleCreationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        roleId = roleCreationResponse.jsonPath().getString("id")
        Assert.assertNotNull(roleId, "Role ID should not be null")
    }

    @Test(dependsOnMethods = "Create OBInternalApiAccessRole Role in IS console")
    void "Assign OBInternalApiAccessRole to Key Manager admin"(){

        //Get the admin user Id
        Response scimRoleResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .contentType("application/scim+json; charset=UTF-8")
                .accept("application/scim+json")
                .body(RequestPayloads.getScimUserSearchPayload(configuration.getUserKeyManagerAdminName()))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.INTERNAL_APIM_SCIME2_USER_ENDPOINT + "/.search")

        Assert.assertEquals(scimRoleResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        String userId = TestUtil.parseResponseBody(scimRoleResponse, "Resources[0].id")

        String permissions = "{" +
                "   \"display\": \"${scope}\"," +
                "   \"value\": \"${scope}\","+
                "}"
        // Build role assignment payload
        def rolePayload = RequestPayloads.assignUserRole(roleName, userId, permissions)

        //Assign the role to the user
        Response assignRoleResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .contentType("application/scim+json; charset=UTF-8")
                .accept("application/scim+json")
                .body(rolePayload)
                .baseUri(configuration.getISServerUrl())
                .put(ConnectorTestConstants.INTERNAL_APIM_SCIME2_ROLES_ENDPOINT + "/${roleId}")

        Assert.assertEquals(assignRoleResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }
}
