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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;

/**
 * Test for MTLS enforcement validator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityCommonUtil.class})
public class MTLSEnforcementValidatorTest extends PowerMockTestCase {

    MockHttpServletResponse response;
    MockHttpServletRequest request;
    FilterChain filterChain;

    @BeforeMethod
    public void beforeMethod() throws ReflectiveOperationException, IOException {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.spy(FilterChain.class);

    }

    @Test(description = "Test the complete flow with certificate header configured")
    public void certificateHeaderValidation() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        MTLSEnforcementValidator mtlsEnforcementValidator = Mockito.spy(MTLSEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(mtlsEnforcementValidator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        filter.doFilter(request, response, filterChain);

        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(description = "Test the complete flow with certificate passed as a attribute")
    public void certificateAttributeValidation() throws Exception {

        PowerMockito.mockStatic(IdentityCommonUtil.class);
        MTLSEnforcementValidator mtlsEnforcementValidator = Mockito.spy(MTLSEnforcementValidator.class);
        IdentityCommonUtil util = Mockito.mock(IdentityCommonUtil.class);

        X509Certificate cert = TestUtil.getCertificate(TestConstants.CERTIFICATE_CONTENT);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.setAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE,
                TestUtil.getCertificate(TestConstants.CERTIFICATE_CONTENT));

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(mtlsEnforcementValidator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        PowerMockito.when(IdentityCommonUtil.getCertificateFromAttribute(cert)).thenReturn(cert);

        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(description = "Test whether the certificate header is present")
    public void noCertificateHeaderValidation() throws IOException, OpenBankingException, ServletException {
        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        PowerMockito.mockStatic(IdentityCommonUtil.class);
        MTLSEnforcementValidator mtlsEnforcementValidator = Mockito.spy(MTLSEnforcementValidator.class);

        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(mtlsEnforcementValidator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);

        filter.doFilter(request, response, filterChain);
        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_request");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Transport certificate not found in the request");
    }



    @Test(description = "Test the certificate in attribute is passed as a header")
    public void certificateIsPresentInAttributeTest() throws IOException, OpenBankingException, ServletException {
        MTLSEnforcementValidator mtlsEnforcementValidator = Mockito.spy(MTLSEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);

        X509Certificate cert = TestUtil.getCertificate(TestConstants.CERTIFICATE_CONTENT);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.setAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE,
                TestUtil.getCertificate(TestConstants.CERTIFICATE_CONTENT));

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(mtlsEnforcementValidator);

        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        PowerMockito.when(IdentityCommonUtil.getCertificateFromAttribute(cert)).thenReturn(cert);

        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(description = "Test whether the certificate attribute is valid")
    public void invalidCertificateHeaderValidation() throws IOException, OpenBankingException, ServletException {
        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        MTLSEnforcementValidator mtlsEnforcementValidator = Mockito.spy(MTLSEnforcementValidator.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);

        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.setAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE, null);
        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(mtlsEnforcementValidator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);

        filter.doFilter(request, response, filterChain);
        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_request");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Transport certificate not found in the request");

    }

    @Test(description = "Test whether the certificate passed through the certificate header is valid")
    public void invalidCertificateValidation() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        PowerMockito.mockStatic(IdentityCommonUtil.class);
        MTLSEnforcementValidator mtlsEnforcementValidator = Mockito.spy(MTLSEnforcementValidator.class);

        request.addHeader(TestConstants.CERTIFICATE_HEADER, "test");
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(mtlsEnforcementValidator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);

        filter.doFilter(request, response, filterChain);
        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_client");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Invalid transport certificate. Certificate passed through the request not valid");
    }
}
