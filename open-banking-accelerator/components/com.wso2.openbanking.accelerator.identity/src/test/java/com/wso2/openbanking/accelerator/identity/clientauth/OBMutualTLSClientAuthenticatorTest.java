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

package com.wso2.openbanking.accelerator.identity.clientauth;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.token.util.TestConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnException;
import org.wso2.carbon.identity.oauth2.token.handler.clientauth.mutualtls.utils.MutualTLSUtil;

import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test for Open Banking mutual TLS client authenticator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityCommonUtil.class, MutualTLSUtil.class})
public class OBMutualTLSClientAuthenticatorTest extends PowerMockTestCase {

    MockHttpServletResponse response;
    MockHttpServletRequest request;
    OAuthClientAuthnContext clientAuthnContext = new OAuthClientAuthnContext();

    @BeforeClass
    public void beforeClass() {
        clientAuthnContext.setClientId("test");
    }

    @BeforeMethod
    public void beforeMethod() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

    }

    @Test(description = "Test whether can authenticate is engaged for mtls request")
    public void canAuthenticateTest() throws OpenBankingException {
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        Map<String, List> bodyParams = new HashMap<>();
        clientAuthnContext.setClientId("");
        bodyParams.put("client_id", Collections.singletonList("test"));

        OBMutualTLSClientAuthenticator authenticator = Mockito.spy(OBMutualTLSClientAuthenticator.class);

        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);

        boolean response = authenticator.canAuthenticate(request, bodyParams, clientAuthnContext);
        assertTrue(response);
    }

    @Test(description = "Test whether can authenticate is not engaged when request does not have a client ID")
    public void canAuthenticateNoClientIDTest() throws OpenBankingException {
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        OBMutualTLSClientAuthenticator authenticator = Mockito.spy(OBMutualTLSClientAuthenticator.class);

        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        boolean response = authenticator.canAuthenticate(request, null, clientAuthnContext);
        assertFalse(response);
    }

    @Test(description = "Test whether can authenticate is not engaged when request has invalid certificate")
    public void canAuthenticateInvalidCertTest() throws OpenBankingException {
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        OBMutualTLSClientAuthenticator authenticator = Mockito.spy(OBMutualTLSClientAuthenticator.class);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        request.addHeader(TestConstants.CERTIFICATE_HEADER, "test");
        try {
            authenticator.canAuthenticate(request, null, clientAuthnContext);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Transport certificate passed through the request not valid");
        }
    }

    @Test(description = "Test whether can authenticate is not engaged when request does not have a cert header")
    public void canAuthenticateNoCertHeaderTest() throws OpenBankingException {
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        OBMutualTLSClientAuthenticator authenticator = Mockito.spy(OBMutualTLSClientAuthenticator.class);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        request.setParameter(IdentityCommonConstants.CLIENT_ID, "test");
        boolean response = authenticator.canAuthenticate(request, null, clientAuthnContext);
        assertFalse(response);
    }

    @Test(description = "Test whether obtaining JWKS endpoint of the SP is succesful")
    public void getJWKSEndpointOfSPTest() throws OAuthClientAuthnException {
        PowerMockito.mockStatic(MutualTLSUtil.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.when(IdentityCommonUtil.getJWKURITransportCert()).thenReturn("dummy");
        OBMutualTLSClientAuthenticator authenticator = Mockito.spy(OBMutualTLSClientAuthenticator.class);
        String expectedUrl = "https://dummy.com";
        PowerMockito.when(MutualTLSUtil.getPropertyValue(Mockito.anyObject(), Mockito.anyObject()))
                .thenReturn(expectedUrl);
        URL url = authenticator.getJWKSEndpointOfSP(Mockito.anyObject(), Mockito.anyObject());
        assertEquals(url.getHost(), "dummy.com");
    }

    @Test(description = "Test whether obtaining JWKS endpoint of the SP is failing when empty JWKS URI is given")
    public void getJWKSEndpointOfSPEmptyTest() {
        PowerMockito.mockStatic(MutualTLSUtil.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.when(IdentityCommonUtil.getJWKURITransportCert()).thenReturn("");
        OBMutualTLSClientAuthenticator authenticator = Mockito.spy(OBMutualTLSClientAuthenticator.class);
        String expectedUrl = "https://dummy.com";
        PowerMockito.when(MutualTLSUtil.getPropertyValue(Mockito.anyObject(), Mockito.anyObject()))
                .thenReturn(expectedUrl);
        try {
            authenticator.getJWKSEndpointOfSP(Mockito.anyObject(), Mockito.anyObject());
        } catch (OAuthClientAuthnException e) {
            assertEquals(e.getErrorCode(), "server_error");
        }
    }

    @Test(description = "Test whether obtaining JWKS endpoint of the SP is failing when malformed URI is given")
    public void getJWKSEndpointOfSPMalformedURITest() {
        PowerMockito.mockStatic(MutualTLSUtil.class);
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.when(IdentityCommonUtil.getJWKURITransportCert()).thenReturn("");
        OBMutualTLSClientAuthenticator authenticator = Mockito.spy(OBMutualTLSClientAuthenticator.class);
        String expectedUrl = "dummy";
        PowerMockito.when(MutualTLSUtil.getPropertyValue(Mockito.anyObject(), Mockito.anyObject()))
                .thenReturn(expectedUrl);
        try {
            authenticator.getJWKSEndpointOfSP(Mockito.anyObject(), Mockito.anyObject());
        } catch (OAuthClientAuthnException e) {
            assertEquals(e.getErrorCode(), "server_error");
        }
    }

    private X509Certificate getCertificate(String certificateContent) {

        if (StringUtils.isNotBlank(certificateContent)) {
            // Build the Certificate object from cert content.
            try {
                return (X509Certificate) IdentityUtil.convertPEMEncodedContentToCertificate(certificateContent);
            } catch (CertificateException e) {
                //do nothing
            }
        }
        return null;
    }
}
