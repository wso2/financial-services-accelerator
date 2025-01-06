/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.dcr

import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.dcr.util.DCRConstants
import org.wso2.financial.services.accelerator.test.framework.FSAcceleratorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants

import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Dynamic Client Registration End to End Flow Tests.
 */
class DCREndToEndFlow extends FSAcceleratorTest {

    ConfigurationService configuration = new ConfigurationService()
    private String accessToken
    private String clientId
    private String invalidAccessToken
    private String configClientId
    private String invalidClientId = "invalid_client_id"
    private AcceleratorTestConstants.ApiScope scopes = AcceleratorTestConstants.ApiScope.CDR_REGISTRATION
    private String registrationPath
    String SSA = new File(configuration.getAppDCRSSAPath()).text

    @BeforeTest(alwaysRun = true)
    void setup() {

        configClientId = configuration.getAppInfoClientID()
        registrationPath = configuration.getServerBaseURL() + DCRConstants.REGISTRATION_ENDPOINT
        configuration.setTppNumber(1)
    }

    @Test(groups = "SmokeTest")
    void "TC0101001_Invoke registration request structured as a JWS"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequest()
                .body(JWTGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder.getRegularClaims(SSA)))
                .when()
                .post(registrationPath)

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"client_id"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"redirect_uris"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"token_endpoint_auth_method"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"grant_types"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"scope"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"software_statement"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"application_type"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"id_token_signed_response_alg"));
        Assert.assertNotNull(TestUtil.parseResponseBody(registrationResponse,"request_object_signing_alg"));

//        accessToken = UKRequestBuilder.getApplicationToken(scopes, clientId)
//        Assert.assertNotNull(accessToken)
    }
}
