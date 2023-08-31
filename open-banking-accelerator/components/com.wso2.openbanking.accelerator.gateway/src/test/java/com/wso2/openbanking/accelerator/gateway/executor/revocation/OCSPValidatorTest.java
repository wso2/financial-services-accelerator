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

package com.wso2.openbanking.accelerator.gateway.executor.revocation;

import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.gateway.executor.model.RevocationStatus;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.gateway.executor.util.TestValidationUtil;
import com.wso2.openbanking.accelerator.gateway.internal.TPPCertValidatorDataHolder;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.cert.ocsp.UnknownStatus;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Test for OCSP validator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({TPPCertValidatorDataHolder.class, HTTPClientUtils.class})
public class OCSPValidatorTest extends PowerMockTestCase {

    @Mock
    TPPCertValidatorDataHolder tppCertValidatorDataHolder;
    OCSPValidator ocspValidator;

    private X509Certificate testPeerCertificateIssuer;
    private X509Certificate eidasPeerCertificate;
    private X509Certificate eidasPeerCertificateIssuer;
    private X509Certificate expiredPeerCertificate;

    @BeforeClass
    public void init() throws CertificateValidationException, CertificateException, OpenBankingException {
        MockitoAnnotations.initMocks(this);
        this.ocspValidator = new OCSPValidator(1);
        this.testPeerCertificateIssuer = TestValidationUtil.getTestClientCertificateIssuer();
        this.eidasPeerCertificate = TestValidationUtil.getTestEidasCertificate();
        this.eidasPeerCertificateIssuer = TestValidationUtil.getTestEidasCertificateIssuer();
        this.expiredPeerCertificate = TestValidationUtil.getExpiredSelfCertificate();
    }

    @Test(description = "when valid certificate provided, then list of crl urls should return")
    public void testGetAIALocationsWithValidCert() throws CertificateValidationException {
        List<String> crlUrls = OCSPValidator.getAIALocations(eidasPeerCertificate);
        Assert.assertFalse(crlUrls.isEmpty());
    }

    @Test(description = "when invalid certificate provided, then CertificateValidationException should throw",
            expectedExceptions = CertificateValidationException.class)
    public void testGetAIALocationsWithInvalidCert() throws CertificateValidationException {
        OCSPValidator.getAIALocations(expiredPeerCertificate);
    }

    @Test(description = "when valid certificate provided, then OCSP object should return")
    public void testGenerateOCSPRequestWithValidCert() throws Exception {
        OCSPReq ocspRequest = WhiteboxImpl.invokeMethod(this.ocspValidator,
                "generateOCSPRequest", testPeerCertificateIssuer, expiredPeerCertificate.getSerialNumber());
        Assert.assertNotNull(ocspRequest);
    }

    @Test
    public void testSetRequestProperties() throws Exception {
        byte[] bytes = "test msg".getBytes(StandardCharsets.UTF_8);
        HttpPost httpPost = new HttpPost();

        WhiteboxImpl.invokeMethod(this.ocspValidator,
                "setRequestProperties", bytes, httpPost);

        Assert.assertTrue(httpPost.containsHeader(CertificateValidationUtils.HTTP_CONTENT_TYPE));
        Assert.assertTrue(httpPost.containsHeader(CertificateValidationUtils.HTTP_ACCEPT));
        Assert.assertEquals(httpPost.getEntity().getContentType().getValue(), CertificateValidationUtils.CONTENT_TYPE);
    }

    @Test(description = "when invalid proxy provided, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testCheckRevocationStatusWithInvalidProxy() throws Exception {
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        Mockito.when(tppCertValidatorDataHolder.isCertificateRevocationProxyEnabled()).thenReturn(true);
        Mockito.when(tppCertValidatorDataHolder.getCertificateRevocationProxyHost()).thenReturn(" ");

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        this.ocspValidator
                .checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer);
    }

    @Test(description = "when issuer cert is null, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testCheckRevocationStatusWithNullCerts() throws CertificateValidationException {
        this.ocspValidator.checkRevocationStatus(null, null);
    }

    @Test(description = "when invalid response received, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testCheckRevocationStatusWithInvalidResponse() throws CertificateValidationException, IOException,
            OpenBankingException {
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.doReturn(HttpStatus.SC_BAD_REQUEST).when(statusLineMock).getStatusCode();

        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        Mockito.doReturn(true).when(tppCertValidatorDataHolder).isCertificateRevocationProxyEnabled();
        Mockito.doReturn("localhost").when(tppCertValidatorDataHolder).getCertificateRevocationProxyHost();
        Mockito.doReturn(8080).when(tppCertValidatorDataHolder).getCertificateRevocationProxyPort();

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        this.ocspValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer);
    }

    @Test(description = "when invalid response received, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testCheckRevocationStatusWhenThrowingIOException() throws CertificateValidationException, IOException,
            OpenBankingException {
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.doThrow(IOException.class).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        TPPCertValidatorDataHolder tppCertValidatorDataHolderMock = Mockito.mock(TPPCertValidatorDataHolder.class);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolderMock);

        this.ocspValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer);
    }

    @Test
    public void testOCSPValidatorConstructor() {
        Assert.assertSame(new OCSPValidator(3).getRetryCount(), 3);
    }


    @Test(description = "when certificate is good, then return good revocation status")
    public void testGetRevocationStatusFromOCSGood() throws Exception {
        SingleResp singleRespMock = Mockito.mock(SingleResp.class);
        Mockito.doReturn(CertificateStatus.GOOD).when(singleRespMock).getCertStatus();

        RevocationStatus revocationStatus = WhiteboxImpl.invokeMethod(this.ocspValidator,
                "getRevocationStatusFromOCSP", singleRespMock);

        Assert.assertSame(revocationStatus, RevocationStatus.GOOD);
    }

    @Test(description = "when certificate is revoked, then return revoked revocation status")
    public void testGetRevocationStatusFromOCSRevoked() throws Exception {
        SingleResp singleRespMock = Mockito.mock(SingleResp.class);
        RevokedStatus revokedStatusMock = Mockito.mock(RevokedStatus.class);
        Mockito.doReturn(revokedStatusMock).when(singleRespMock).getCertStatus();

        RevocationStatus revocationStatus = WhiteboxImpl.invokeMethod(this.ocspValidator,
                "getRevocationStatusFromOCSP", singleRespMock);

        Assert.assertSame(revocationStatus, RevocationStatus.REVOKED);
    }

    @Test(description = "when certificate status is unknown, then return unknown revocation status")
    public void testGetRevocationStatusFromOCSUnknown() throws Exception {
        SingleResp singleRespMock = Mockito.mock(SingleResp.class);
        UnknownStatus unknownStatusMock = Mockito.mock(UnknownStatus.class);
        Mockito.doReturn(unknownStatusMock).when(singleRespMock).getCertStatus();

        RevocationStatus revocationStatus = WhiteboxImpl.invokeMethod(this.ocspValidator,
                "getRevocationStatusFromOCSP", singleRespMock);

        Assert.assertSame(revocationStatus, RevocationStatus.UNKNOWN);
    }
}
