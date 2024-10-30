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

package com.wso2.openbanking.accelerator.identity.token;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.token.util.TestConstants;
import com.wso2.openbanking.accelerator.identity.token.util.TestUtil;
import com.wso2.openbanking.accelerator.identity.token.validators.ClientAuthenticatorValidator;
import com.wso2.openbanking.accelerator.identity.token.validators.MTLSEnforcementValidator;
import com.wso2.openbanking.accelerator.identity.token.validators.OBIdentityFilterValidator;
import com.wso2.openbanking.accelerator.identity.token.validators.SignatureAlgorithmEnforcementValidator;
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
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import static org.testng.Assert.assertEquals;

/**
 * Test for token filter.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityCommonUtil.class})
public class TokenFilterTest extends PowerMockTestCase {

    MockHttpServletRequest request;
    MockHttpServletResponse response;
    FilterChain filterChain;

    @BeforeMethod
    public void beforeMethod() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.spy(FilterChain.class);
    }

    @Test(description = "Test the validators are omitted in non regulatory scenario")
    public void nonRegulatoryTest() throws Exception {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(false);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    @Test(description = "Test client id extraction from header in non-regulatory app scenario")
    public void nonRegulatoryAppWithAuthorizationHeaderTest() throws Exception {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        String credentials = "client_id" + ':' + "client_secret";
        String authToken = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        request.addHeader("Authorization", "Basic " + authToken);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("client_id")).thenReturn(false);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    @Test(description = "Test the certificate in context/header is mandated")
    public void noCertificateTest() throws IOException, OpenBankingException, ServletException, NoSuchFieldException,
            IllegalAccessException {

        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");

        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader())
                .thenReturn(IdentityCommonConstants.CERTIFICATE_HEADER);

        Field privateField = TokenFilter.class.getDeclaredField(TestConstants.IS_TRANSPORT_CERT_MANDATORY_FIELD_NAME);
        privateField.setAccessible(true);
        privateField.set(filter, true);

        filter.doFilter(request, response, filterChain);
        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_request");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Transport certificate not found in the request");
    }

    @Test(description = "Test the certificate in attribute is present if config is disabled")
    public void certificateIsNotPresentInAttributeTest() throws IOException, OpenBankingException, ServletException,
            NoSuchFieldException, IllegalAccessException {

        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, false);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);

        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);

        Field privateField = TokenFilter.class.getDeclaredField(TestConstants.IS_TRANSPORT_CERT_MANDATORY_FIELD_NAME);
        privateField.setAccessible(true);
        privateField.set(filter, true);

        filter.doFilter(request, response, filterChain);
        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_request");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Transport certificate not found in the request");

    }

    @Test(description = "Test the certificate in the attribute is overridden in header")
    public void certificateInAttributeOverriddenTest() throws IOException, OpenBankingException, ServletException {

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.addHeader(TestConstants.CERTIFICATE_HEADER, "invalid");
        request.setAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE,
                TestUtil.getCertificate(TestConstants.CERTIFICATE_CONTENT));

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    @Test(description = "Test the validators are omitted if nothing is configured")
    public void noValidatorsConfiguredTest() throws Exception {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    @Test(description = "Test the client ID is enforced")
    public void clientIdEnforcementTest() throws Exception {

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        filter.doFilter(request, response, filterChain);

        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_request");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Unable to find client id in the request");
    }

    @Test(description = "Test client auth and signature enforcement validators engaged")
    public void clientAuthSignatureEnforcementValidatorTest() throws Exception {

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        ClientAuthenticatorValidator clientAuthValidator = Mockito.spy(ClientAuthenticatorValidator.class);
        SignatureAlgorithmEnforcementValidator signatureAlgorithmValidator =
                Mockito.spy(SignatureAlgorithmEnforcementValidator.class);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE,
                IdentityCommonConstants.OAUTH_JWT_BEARER_GRANT_TYPE);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION);
        request.setAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE,
                TestUtil.getCertificate(TestConstants.CERTIFICATE_CONTENT));

        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        Mockito.doReturn("private_key_jwt").when(clientAuthValidator)
                .retrieveRegisteredAuthMethod("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("PS256").when(signatureAlgorithmValidator)
                .getRegisteredSigningAlgorithm("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("PS256").when(signatureAlgorithmValidator)
                .getRequestSigningAlgorithm(TestConstants.CLIENT_ASSERTION);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(clientAuthValidator);
        validators.add(signatureAlgorithmValidator);

        Mockito.doReturn(validators).when(filter).getValidators();
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    @Test(description = "Test client auth and mtls enforcement validators engaged")
    public void clientAuthMTLSEnforcementValidatorTest() throws Exception {

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        ClientAuthenticatorValidator clientAuthValidator = Mockito.spy(ClientAuthenticatorValidator.class);
        MTLSEnforcementValidator mtlsEnforcementValidator =
                Mockito.spy(MTLSEnforcementValidator.class);

        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE,
                IdentityCommonConstants.OAUTH_JWT_BEARER_GRANT_TYPE);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION);
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);

        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        Mockito.doReturn("private_key_jwt").when(clientAuthValidator)
                .retrieveRegisteredAuthMethod("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(clientAuthValidator);
        validators.add(mtlsEnforcementValidator);

        Mockito.doReturn(validators).when(filter).getValidators();
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    @Test(description = "Test mtls and signature enforcement validators engaged")
    public void signatureMTLSeEnforcementValidatorTest() throws Exception {

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        MTLSEnforcementValidator mtlsEnforcementValidator =
                Mockito.spy(MTLSEnforcementValidator.class);
        SignatureAlgorithmEnforcementValidator signatureAlgorithmValidator =
                Mockito.spy(SignatureAlgorithmEnforcementValidator.class);

        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE,
                IdentityCommonConstants.OAUTH_JWT_BEARER_GRANT_TYPE);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION);
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        Mockito.doReturn("PS256").when(signatureAlgorithmValidator)
                .getRegisteredSigningAlgorithm("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("PS256").when(signatureAlgorithmValidator)
                .getRequestSigningAlgorithm(TestConstants.CLIENT_ASSERTION);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(mtlsEnforcementValidator);
        validators.add(signatureAlgorithmValidator);

        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);

        Mockito.doReturn(validators).when(filter).getValidators();
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    @Test(description = "Test client auth, signature and mtls enforcement validators engaged")
    public void allValidatorTest() throws Exception {

        SignatureAlgorithmEnforcementValidator signatureValidator =
                Mockito.spy(SignatureAlgorithmEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        MTLSEnforcementValidator mtlsEnforcementValidator = Mockito.spy(MTLSEnforcementValidator.class);
        ClientAuthenticatorValidator clientAuthenticatorValidator = Mockito.spy(ClientAuthenticatorValidator.class);

        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE,
                IdentityCommonConstants.OAUTH_JWT_BEARER_GRANT_TYPE);

        Mockito.doReturn("private_key_jwt").when(clientAuthenticatorValidator)
                .retrieveRegisteredAuthMethod("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("PS256").when(signatureValidator)
                .getRegisteredSigningAlgorithm("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("PS256").when(signatureValidator)
                .getRequestSigningAlgorithm(TestConstants.CLIENT_ASSERTION);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(mtlsEnforcementValidator);
        validators.add(signatureValidator);
        validators.add(clientAuthenticatorValidator);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        Mockito.doReturn(validators).when(filter).getValidators();
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }
}
