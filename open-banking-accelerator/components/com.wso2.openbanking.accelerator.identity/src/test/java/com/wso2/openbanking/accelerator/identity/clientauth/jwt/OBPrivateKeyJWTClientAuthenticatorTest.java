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

package com.wso2.openbanking.accelerator.identity.clientauth.jwt;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.token.handler.clientauth.jwt.validator.JWTValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for testing the OBPrivateKeyJWTClientAuthenticator class.
 */
public class OBPrivateKeyJWTClientAuthenticatorTest {

    private static final String JWT_ASSERTION_TYPE_VALUE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    MockHttpServletRequest request;
    OAuthClientAuthnContext clientAuthnContext = new OAuthClientAuthnContext();

    @BeforeMethod
    public void beforeMethod() {
        request = new MockHttpServletRequest();
    }

    @Test(description = "Test whether can authenticate is engaged for pvt key jwt request")
    public void canAuthenticateTest() {
        JWTValidator jwtValidatorMock = Mockito.mock(JWTValidator.class);
        OBPrivateKeyJWTClientAuthenticator authenticator = Mockito
                .spy(new OBPrivateKeyJWTClientAuthenticator(jwtValidatorMock));

        Map<String, List> bodyParams = new HashMap<>();
        bodyParams.put(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE, Collections
                .singletonList(JWT_ASSERTION_TYPE_VALUE));
        bodyParams.put(IdentityCommonConstants.OAUTH_JWT_ASSERTION, Collections
                .singletonList("test"));

        boolean response = authenticator.canAuthenticate(request, bodyParams, clientAuthnContext);
        assertTrue(response);
    }

    @Test(description = "Test whether can authenticate is not engaged when client assertion is not there")
    public void canAuthenticateWithoutClientAssertionTest() {
        JWTValidator jwtValidatorMock = Mockito.mock(JWTValidator.class);
        OBPrivateKeyJWTClientAuthenticator authenticator = Mockito
                .spy(new OBPrivateKeyJWTClientAuthenticator(jwtValidatorMock));

        Map<String, List> bodyParams = new HashMap<>();
        bodyParams.put(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE, Collections
                .singletonList(JWT_ASSERTION_TYPE_VALUE));

        boolean response = authenticator.canAuthenticate(request, bodyParams, clientAuthnContext);
        assertFalse(response);
    }

    @Test(description = "Test whether can authenticate is not engaged when client assertion type is not there")
    public void canAuthenticateWithoutClientAssertionTypeTest() {
        JWTValidator jwtValidatorMock = Mockito.mock(JWTValidator.class);
        OBPrivateKeyJWTClientAuthenticator authenticator = Mockito
                .spy(new OBPrivateKeyJWTClientAuthenticator(jwtValidatorMock));

        Map<String, List> bodyParams = new HashMap<>();
        bodyParams.put(IdentityCommonConstants.OAUTH_JWT_ASSERTION, Collections
                .singletonList("test"));

        boolean response = authenticator.canAuthenticate(request, bodyParams, clientAuthnContext);
        assertFalse(response);
    }
}
