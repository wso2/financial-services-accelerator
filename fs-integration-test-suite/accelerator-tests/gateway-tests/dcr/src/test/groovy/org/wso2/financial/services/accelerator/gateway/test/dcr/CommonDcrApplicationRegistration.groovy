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

package org.wso2.financial.services.accelerator.gateway.test.dcr

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.util.RestAsRequestBuilder
import org.wso2.financial.services.accelerator.gateway.test.dcr.util.DCRConstants
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Dynamic Client Registration End to End Flow Tests.
 */
class CommonDcrApplicationRegistration extends FSConnectorTest {

    ConfigurationService configuration = new ConfigurationService()
    private String accessToken
    private String clientId
    private List<ConnectorTestConstants.ApiScope> scopes = [ConnectorTestConstants.ApiScope.ACCOUNTS]
    private String registrationPath
    String SSA
    JWTGenerator jwtGenerator = new JWTGenerator()
    File xmlFile = ConnectorTestConstants.CONFIG_FILE

    @BeforeClass(alwaysRun = true)
    void setup() {

        registrationPath = configuration.getServerBaseURL() + DCRConstants.REGISTRATION_ENDPOINT
        configuration.setTppNumber(0)
        SSA = new File(configuration.getAppDCRSSAPath()).text
        xmlFile = ConnectorTestConstants.CONFIG_FILE
    }

    @Test(groups = "SmokeTest")
    void "Invoke registration request structured as a JWS"() {

       def registrationResponse = RestAsRequestBuilder.buildRequest()
                .contentType("application/json")
                .body(ClientRegistrationRequestBuilder.getRegularClaimsForGateway(SSA))
                .accept("application/json")
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"client_id"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"redirect_uris"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"token_endpoint_auth_method"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"grant_types"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"scope"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"software_statement"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"application_type"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"id_token_signed_response_alg"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"request_object_signing_alg"));

        TestUtil.writeXMLContent(xmlFile.toString(), "Application", "ClientID", clientId,
                configuration.getTppNumber())
    }
}
