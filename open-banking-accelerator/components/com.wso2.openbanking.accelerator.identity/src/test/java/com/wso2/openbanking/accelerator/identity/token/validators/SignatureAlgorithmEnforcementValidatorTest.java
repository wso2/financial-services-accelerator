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
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;

/**
 * Test for signature algorithm enforcement validator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityCommonUtil.class, OAuthServerConfiguration.class})
public class SignatureAlgorithmEnforcementValidatorTest extends PowerMockTestCase {

    MockHttpServletResponse response;
    MockHttpServletRequest request;
    FilterChain filterChain;

    @BeforeMethod
    public void beforeMethod() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.spy(FilterChain.class);

    }

    @Test(description = "Test the complete signature algorithm validation flow")
    public void signatureAlgorithmValidationTest() throws Exception {

        SignatureAlgorithmEnforcementValidator validator = Mockito.spy(SignatureAlgorithmEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION);
        request.setAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE,
                TestUtil.getCertificate(TestConstants.CERTIFICATE_CONTENT));
        Mockito.doReturn("PS256").when(validator)
                .getRegisteredSigningAlgorithm("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("PS256").when(validator)
                .getRequestSigningAlgorithm(TestConstants.CLIENT_ASSERTION);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        filter.doFilter(request, response, filterChain);

        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(description = "Test when registered algorithm and signed algorithm differ")
    public void signatureInvalidAlgorithmValidationTest() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);

        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        SignatureAlgorithmEnforcementValidator validator = Mockito.spy(SignatureAlgorithmEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION);
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);

        Mockito.doReturn("PS256").when(validator)
                .getRegisteredSigningAlgorithm("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("RS256").when(validator)
                .getRequestSigningAlgorithm(TestConstants.CLIENT_ASSERTION);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("iYpRm64b2vmvmKDhdL6KZD9z6fca"))
                .thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        filter.doFilter(request, response, filterChain);

        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_UNAUTHORIZED);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), IdentityCommonConstants
                .OAUTH2_INVALID_CLIENT_MESSAGE);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Registered algorithm does not match with the token signed algorithm");
    }

    @Test(description = "Test the validity of the signed assertion without client ID")
    public void signatureInvalidAssertionValidationTest() throws Exception {

        SignatureAlgorithmEnforcementValidator validator = Mockito.spy(SignatureAlgorithmEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION_NO_HEADER);
        Mockito.doReturn("PS256").when(validator)
                .getRegisteredSigningAlgorithm("iYpRm64b2vmvmKDhdL6KZD9z6fca");

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        filter.doFilter(request, response, filterChain);

        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_UNAUTHORIZED);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_request");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Error occurred while parsing the signed assertion");
    }

    @Test(description = "Test the validity of the signed assertion with client ID")
    public void invalidClientAssertionValidationTest() throws Exception {

        SignatureAlgorithmEnforcementValidator validator = Mockito.spy(SignatureAlgorithmEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, "test");
        request.setAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE,
                TestUtil.getCertificate(TestConstants.CERTIFICATE_CONTENT));

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        filter.doFilter(request, response, filterChain);

        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_UNAUTHORIZED);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_request");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Error occurred while parsing the signed assertion");
    }

    @Test(description = "Client ID enforcement test")
    public void clientIdEnforcementTest() throws Exception {

        SignatureAlgorithmEnforcementValidator validator = Mockito.spy(SignatureAlgorithmEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        Mockito.doReturn("PS256").when(validator)
                .getRegisteredSigningAlgorithm("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("PS256").when(validator)
                .getRequestSigningAlgorithm(TestConstants.CLIENT_ASSERTION);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        filter.doFilter(request, response, filterChain);

        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_request");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Unable to find client id in the request");
    }
}
