/**
 * Copyright (c) 2023-2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.identity.auth.extensions.authz.request;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.oltu.oauth2.as.validator.CodeValidator;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.validators.OAuthValidator;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.common.CodeTokenResponseValidator;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Test for OB authorization request for request_uri support.
 */
@PrepareForTest({IdentityCommonUtil.class, OAuthServerConfiguration.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class OBOAuthAuthzRequestTest extends PowerMockTestCase {

    private OBOAuthAuthzRequest obOAuthAuthzRequest;
    private HttpServletRequest mockRequest;
    private static final String STATE = "abc";
    private static final String SAMPLE_REQUEST_URI = "urn:ietf:params:oauth:request_uri:abc";

    @BeforeMethod
    public void beforeMethod() throws OAuthProblemException {

        // Mock HttpServletRequest
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getParameter(IdentityCommonConstants.CLIENT_ID)).thenReturn("1234567");

        // Mock IdentityCommonUtil
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.when(IdentityCommonUtil.handleOAuthProblemException(any(), any(), any()))
                .thenReturn(OAuthProblemException.error("invalid_request").description("Error message").state(STATE));
        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_STATE))
                .thenReturn(STATE);

        // Mock OAuthServerConfiguration
        OAuthServerConfiguration oAuthServerConfigurationMock = PowerMockito.mock(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfigurationMock);

        // Mock supported response type validators
        Map<String, Class<? extends OAuthValidator<HttpServletRequest>>>
                supportedResponseTypeValidators = new Hashtable<>();
        supportedResponseTypeValidators.put("code", CodeValidator.class);
        supportedResponseTypeValidators.put("code id_token", CodeTokenResponseValidator.class);

        PowerMockito.when(oAuthServerConfigurationMock.getSupportedResponseTypeValidators())
                .thenReturn(supportedResponseTypeValidators);
    }

    @Test
    public void testInitValidatorForCodeResponseType() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code"});
        mockParameterMap.put(IdentityCommonConstants.REQUEST_URI,
                new String[]{SAMPLE_REQUEST_URI});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code");

        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_RESPONSE_TYPE))
                .thenReturn("code");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        // Assertion
        assertEquals(obOAuthAuthzRequest.initValidator().getClass(), CodeValidator.class);
    }

    @Test
    public void testInitValidatorForHybridResponseType() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code id_token"});
        mockParameterMap.put(IdentityCommonConstants.REQUEST_URI,
                new String[]{SAMPLE_REQUEST_URI});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(mockRequest.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("abc.com");
        when(mockRequest.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid");

        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_RESPONSE_TYPE))
                .thenReturn("code id_token");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        // Assertion
        assertEquals(obOAuthAuthzRequest.initValidator().getClass(), CodeTokenResponseValidator.class);
    }

    @Test(expectedExceptions = OAuthProblemException.class)
    public void testInitValidatorWithoutResponseTypeParam() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.REQUEST_URI,
                new String[]{SAMPLE_REQUEST_URI});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);

        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_RESPONSE_TYPE))
                .thenReturn(null);

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);
    }

    @Test(expectedExceptions = OAuthProblemException.class)
    public void testInitValidatorWithUnsupportedResponseTypeParam() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.REQUEST_URI,
                new String[]{SAMPLE_REQUEST_URI});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);

        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_RESPONSE_TYPE))
                .thenReturn("unsupported");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);
    }

    @Test
    public void testValidGetScopesFromRequestURI() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code id_token"});
        mockParameterMap.put(IdentityCommonConstants.REQUEST_URI,
                new String[]{SAMPLE_REQUEST_URI});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(mockRequest.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("abc.com");
        when(mockRequest.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid");
        when(mockRequest.getParameter(IdentityCommonConstants.REQUEST_URI)).thenReturn(SAMPLE_REQUEST_URI);

        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_SCOPE))
                .thenReturn("openid");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        // Assertion
        assertEquals(obOAuthAuthzRequest.getScopes(), new HashSet<>(Collections.singletonList("openid")));
    }

    @Test
    public void testValidGetScopesFromRequest() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code id_token"});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(mockRequest.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("abc.com");
        when(mockRequest.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        // Assertion
        assertEquals(obOAuthAuthzRequest.getScopes(), new HashSet<>(Collections.singletonList("openid")));
    }

    @Test
    public void testValidGetResponseTypeFromRequestURI() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code id_token"});
        mockParameterMap.put(IdentityCommonConstants.REQUEST_URI,
                new String[]{SAMPLE_REQUEST_URI});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(mockRequest.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("abc.com");
        when(mockRequest.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid");
        when(mockRequest.getParameter(IdentityCommonConstants.REQUEST_URI)).thenReturn(SAMPLE_REQUEST_URI);

        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_RESPONSE_TYPE))
                .thenReturn("code id_token");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        // Assertion
        assertEquals(obOAuthAuthzRequest.getResponseType(), "code id_token");
    }

    @Test
    public void testValidGetResponseTypeFromRequest() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code id_token"});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(mockRequest.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("abc.com");
        when(mockRequest.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        // Assertion
        assertEquals(obOAuthAuthzRequest.getResponseType(), "code id_token");
    }

    @Test
    public void testValidGetStateFromRequestURI() throws OAuthProblemException, OAuthSystemException {

        // Mock
        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code id_token"});
        mockParameterMap.put(IdentityCommonConstants.REQUEST_URI,
                new String[]{SAMPLE_REQUEST_URI});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(mockRequest.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("abc.com");
        when(mockRequest.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid");
        when(mockRequest.getParameter(IdentityCommonConstants.REQUEST_URI)).thenReturn(SAMPLE_REQUEST_URI);

        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_STATE))
                .thenReturn("abc");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        // Assertion
        assertEquals(obOAuthAuthzRequest.getState(), "abc");
    }

    @Test
    public void testInvalidGetStateFromRequestURI() throws OAuthProblemException, OAuthSystemException {

        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code id_token"});
        mockParameterMap.put(IdentityCommonConstants.REQUEST_URI,
                new String[]{SAMPLE_REQUEST_URI});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(mockRequest.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("abc.com");
        when(mockRequest.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid");
        when(mockRequest.getParameter(IdentityCommonConstants.REQUEST_URI)).thenReturn(SAMPLE_REQUEST_URI);

        // Simulate an exception being thrown when decoding the state
        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(mockRequest, OAuth.OAUTH_STATE))
                .thenThrow(OAuthProblemException.error("invalid_request").description("Invalid state").state("abc"));

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        assertEquals(obOAuthAuthzRequest.getState(), null);
    }

    @Test
    public void testValidGetScopesFromRequest_WhenRequestURIIsAbsent() throws OAuthProblemException,
            OAuthSystemException {

        Map<String, String[]> mockParameterMap = new HashMap<>();
        mockParameterMap.put(IdentityCommonConstants.RESPONSE_TYPE, new String[]{"code id_token"});
        mockParameterMap.put(IdentityCommonConstants.SCOPE, new String[]{"openid"});
        mockParameterMap.put(IdentityCommonConstants.REQUEST,
                new String[]{Base64.getEncoder().encodeToString(
                        "{\"scope\": \"openid\", \"redirect_uri\": \"http://example.com\"}".getBytes(
                                StandardCharsets.UTF_8))});
        mockParameterMap.put(IdentityCommonConstants.REDIRECT_URI, new String[]{"http://example.com"});

        when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);
        when(mockRequest.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(mockRequest.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid");
        when(mockRequest.getParameter(IdentityCommonConstants.REQUEST)).thenReturn(
                Base64.getEncoder().encodeToString(
                        "{\"scope\": \"openid\", \"redirect_uri\": \"http://example.com\"}".getBytes(
                                StandardCharsets.UTF_8)));
        when(mockRequest.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("http://example.com");

        obOAuthAuthzRequest = new OBOAuthAuthzRequest(mockRequest);

        assertEquals(obOAuthAuthzRequest.getScopes(), new HashSet<>(Collections.singletonList("openid")));
    }

}
