/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
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

import static org.testng.Assert.assertEquals;

/**
 * class for MTLSCertificateValidator Test.
 */
@PrepareForTest({IdentityCommonUtil.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class MTLSCertificateValidatorTest extends PowerMockTestCase {

    MockHttpServletResponse response;
    MockHttpServletRequest request;
    FilterChain filterChain;
    TokenFilter filter;

    @BeforeMethod
    public void beforeMethod() throws ReflectiveOperationException, IOException, OpenBankingException {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.spy(FilterChain.class);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        MTLSCertificateValidator mtlsCertificateValidator = Mockito.spy(MTLSCertificateValidator.class);
        validators.add(mtlsCertificateValidator);

        filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.setAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE, null);

    }

    @Test(description = "Test whether the expired certificate fails")
    public void testMTLSCertValidationWithExpiredCertificate() throws IOException, ServletException {

        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.EXPIRED_CERTIFICATE_CONTENT);

        filter.doFilter(request, response, filterChain);
        Map<String, String> responseMap = TestUtil.getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_UNAUTHORIZED);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_client");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "Invalid mutual TLS request. Client certificate is expired");

    }

    @Test(description = "Test whether the expired certificate fails")
    public void testMTLSCertValidationWithValidCertificate() throws IOException, ServletException {

        //TODO : Rename back to TestConstants.CERTIFICATE_CONTENT
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_TEMP_CONTENT);
        filter.doFilter(request, response, filterChain);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }
}
