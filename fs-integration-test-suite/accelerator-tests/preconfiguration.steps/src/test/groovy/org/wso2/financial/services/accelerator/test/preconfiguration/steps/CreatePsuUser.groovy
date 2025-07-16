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

import groovy.json.JsonOutput
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

/**
 * This class is used to create a user in WSO2 IS Console and assign a role to that user.
 * It uses the SCIM2 API to create the user and assign the role.
 */
class CreatePsuUser extends FSAPIMConnectorTest{

    String scime2Userurl
    String userId
    def basicHeader

    @BeforeClass
    void init() {
        scime2Userurl = configuration.getISServerUrl() + ConnectorTestConstants.INTERNAL_APIM_SCIME2_USER_ENDPOINT

        def authToken = "${configuration.getUserKeyManagerAdminName()}:" +
                "${configuration.getUserKeyManagerAdminPWD()}"

        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"
    }

    @Test
    void "Create User in carbon console"() {

        Response scimUserResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .contentType("application/scim+json; charset=UTF-8")
                .accept("application/scim+json")
                .body(RequestPayloads.createUserPayload())
                .post(scime2Userurl)

        Assert.assertEquals(scimUserResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        userId = scimUserResponse.jsonPath().getString("id")
        Assert.assertNotNull(userId, "User ID should not be null")
    }

    //TODO: Need to publish the API before
//    @Test(dependsOnMethods = "Create User in carbon console")
    void "Assign role to user"(){

        def roleName = "consumer"

        //Get the Consumer Role Id
        Response scimRoleResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .contentType("application/scim+json; charset=UTF-8")
                .accept("application/scim+json")
                .body(RequestPayloads.getScimRolePayload(roleName))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.INTERNAL_APIM_SCIME2_ROLES_ENDPOINT + "/.search")

        Assert.assertEquals(scimRoleResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        String roleId = TestUtil.parseResponseBody(scimRoleResponse, "Resources[0].id")
        def permissions = scimRoleResponse.jsonPath().getList("Resources[0].permissions")
        def permissionsString = permissions.collect { JsonOutput.toJson(it) }.join(",\n")

        // Build role assignment payload
        def rolePayload = RequestPayloads.assignUserRole(roleName, userId, permissionsString)

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
