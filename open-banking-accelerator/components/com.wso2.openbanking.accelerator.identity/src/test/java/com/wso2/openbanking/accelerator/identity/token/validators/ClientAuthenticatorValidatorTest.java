/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.identity.token.validators;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.token.DefaultTokenFilter;
import com.wso2.openbanking.accelerator.identity.token.TokenFilter;
import com.wso2.openbanking.accelerator.identity.token.util.TestConstants;
import com.wso2.openbanking.accelerator.identity.token.util.TestUtil;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.http.HttpStatus;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;

/**
 * Test for client authenticator validator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityCommonUtil.class})
public class ClientAuthenticatorValidatorTest extends PowerMockTestCase {

    MockHttpServletResponse response;
    MockHttpServletRequest request;
    FilterChain filterChain;

    @BeforeMethod
    public void beforeMethod() throws ReflectiveOperationException, IOException {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.spy(FilterChain.class);

    }

    @Test(description = "Test whether authentication follows the registered client authentication method PKJWT")
    public void privateKeyJWTValidatorTest() throws IOException, ServletException, OpenBankingException {

        ClientAuthenticatorValidator validator = Mockito.spy(ClientAuthenticatorValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE,
                IdentityCommonConstants.OAUTH_JWT_BEARER_GRANT_TYPE);
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);
        Mockito.doReturn("private_key_jwt").when(validator)
                .retrieveRegisteredAuthMethod("iYpRm64b2vmvmKDhdL6KZD9z6fca");

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        filter.doFilter(request, response, filterChain);

        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(description = "Test fail scenario authentication follows the registered client authentication method PKJWT")
    public void privateKeyJWTValidatorNegativeTest() throws IOException, ServletException, OpenBankingException {

        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        PowerMockito.mockStatic(IdentityCommonUtil.class);
        ClientAuthenticatorValidator validator = Mockito.spy(ClientAuthenticatorValidator.class);

        request.setParameter("client_assertion", TestConstants.CLIENT_ASSERTION);
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);
        Mockito.doReturn("private_key_jwt").when(validator)
                .retrieveRegisteredAuthMethod("iYpRm64b2vmvmKDhdL6KZD9z6fca");

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("iYpRm64b2vmvmKDhdL6KZD9z6fca"))
                .thenReturn(true);
        filter.doFilter(request, response, filterChain);

        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), IdentityCommonConstants
                .OAUTH2_INVALID_REQUEST_MESSAGE);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Request does not follow the registered token endpoint auth method private_key_jwt");
    }

    @Test(description = "Test client ID enforcement in client authentication method mtls")
    public void privateKeyClientIdEnforcementTest() throws IOException, ServletException, OpenBankingException {

        ClientAuthenticatorValidator validator = Mockito.spy(ClientAuthenticatorValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);

        request.setParameter("client_assertion_type", IdentityCommonConstants.OAUTH_JWT_BEARER_GRANT_TYPE);
        Mockito.doReturn("private_key_jwt").when(validator)
                .retrieveRegisteredAuthMethod("iYpRm64b2vmvmKDhdL6KZD9z6fca");

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        filter.doFilter(request, response, filterChain);

        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), IdentityCommonConstants
                .OAUTH2_INVALID_REQUEST_MESSAGE);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Unable to find client id in the request");
    }

    @Test(description = "Test whether authentication follows the registered client authentication method mtls")
    public void mtlsValidatorTest() throws IOException, ServletException, OpenBankingException {

        PowerMockito.mockStatic(IdentityCommonUtil.class);
        ClientAuthenticatorValidator validator = Mockito.spy(ClientAuthenticatorValidator.class);
        request.setParameter("client_id", "iYpRm64b2vmvmKDhdL6KZD9z6fca");
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);
        Mockito.doReturn("tls_client_auth").when(validator)
                .retrieveRegisteredAuthMethod("iYpRm64b2vmvmKDhdL6KZD9z6fca");

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(description = "Test client ID enforcement in client authentication method mtls")
    public void mtlsValidatorClientIdEnforcementTest() throws IOException, ServletException, OpenBankingException {

        ClientAuthenticatorValidator validator = Mockito.spy(ClientAuthenticatorValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);
        Mockito.doReturn("tls_client_auth").when(validator)
                .retrieveRegisteredAuthMethod("iYpRm64b2vmvmKDhdL6KZD9z6fca");

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        filter.doFilter(request, response, filterChain);

        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), IdentityCommonConstants
                .OAUTH2_INVALID_REQUEST_MESSAGE);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Unable to find client id in the request");
    }
}
