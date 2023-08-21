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
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

/**
 * Test for CRL validator.
 */
@PrepareForTest({TPPCertValidatorDataHolder.class, HTTPClientUtils.class, CertificateValidationUtils.class})
@PowerMockIgnore({"javax.security.auth.x500.*", "jdk.internal.reflect.*"})
public class CRLValidatorTest extends PowerMockTestCase {

    String path = "src/test/resources";
    File file = new File(path);
    String absolutePathForTestResources = file.getAbsolutePath();

    private CRLValidator crlValidator;
    private X509Certificate eidasPeerCertificate;
    private X509Certificate eidasPeerCertificateIssuer;
    private X509Certificate expiredPeerCertificate;

    @BeforeClass
    public void initClass() throws CertificateValidationException, CertificateException, OpenBankingException {
        this.eidasPeerCertificate = TestValidationUtil.getTestEidasCertificate();
        this.eidasPeerCertificateIssuer = TestValidationUtil.getTestEidasCertificateIssuer();
        this.expiredPeerCertificate = TestValidationUtil.getExpiredSelfCertificate();
        this.crlValidator = new CRLValidator(3);
    }

    @BeforeMethod
    public void initMethods() throws IOException, OpenBankingException {
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();

        File file = new File(absolutePathForTestResources + "/test_crl_entries.pem");
        byte[] crlBytes = FileUtils.readFileToString(file, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
        InputStream inStream = new ByteArrayInputStream(crlBytes);

        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);
    }

    @Test
    public void testCRLValidatorConstructor() {
        Assert.assertSame(new CRLValidator(3).getRetryCount(), 3);
    }

    @Test(description = "when valid certificate provided, then X509URL should not be null")
    public void testDownloadCRLFromWeb() throws Exception {
        TPPCertValidatorDataHolder tppCertValidatorDataHolder = Mockito.mock(TPPCertValidatorDataHolder.class);
        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        // Date needs to be an old date than X509 next update date
        Date dateMock = new SimpleDateFormat("dd/MM/yyyy").parse("17/03/2021");
        PowerMockito.mockStatic(CertificateValidationUtils.class);
        PowerMockito.when(CertificateValidationUtils.getNewDate()).thenReturn(dateMock);

        Assert.assertNotNull(this.crlValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer));

    }

    @Test(description = "when valid proxy provided, then X509URL should not be null")
    public void testDownloadCRLFromWebWithProxy() throws Exception {
        TPPCertValidatorDataHolder tppCertValidatorDataHolder = Mockito.mock(TPPCertValidatorDataHolder.class);
        Mockito.doReturn(true).when(tppCertValidatorDataHolder).isCertificateRevocationProxyEnabled();
        Mockito.doReturn("localhost").when(tppCertValidatorDataHolder).getCertificateRevocationProxyHost();
        Mockito.doReturn(8080).when(tppCertValidatorDataHolder).getCertificateRevocationProxyPort();

        // Date needs to be an old date than X509 next update date
        Date dateMock = new SimpleDateFormat("dd/MM/yyyy").parse("17/03/2021");
        PowerMockito.mockStatic(CertificateValidationUtils.class);
        PowerMockito.when(CertificateValidationUtils.getNewDate()).thenReturn(dateMock);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        Assert.assertNotNull(this.crlValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer));
    }

    @Test(description = "when invalid proxy host provided, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testDownloadCRLFromWebWithInvalidProxy() throws CertificateValidationException {
        TPPCertValidatorDataHolder tppCertValidatorDataHolder = Mockito.mock(TPPCertValidatorDataHolder.class);
        Mockito.doReturn(true).when(tppCertValidatorDataHolder).isCertificateRevocationProxyEnabled();
        Mockito.doReturn(" ").when(tppCertValidatorDataHolder).getCertificateRevocationProxyHost();
        Mockito.doReturn(8080).when(tppCertValidatorDataHolder).getCertificateRevocationProxyPort();

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        Assert.assertNotNull(this.crlValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer));
    }

    @Test(description = "when invalid http response received, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testDownloadCRLFromWebWithInvalidHTTPResponse() throws CertificateValidationException, IOException,
            OpenBankingException {
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.doReturn(HttpStatus.SC_BAD_REQUEST).when(statusLineMock).getStatusCode();

        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();

        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        TPPCertValidatorDataHolder tppCertValidatorDataHolder = Mockito.mock(TPPCertValidatorDataHolder.class);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        Assert.assertNotNull(this.crlValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer));
    }

    @Test(description = "when invalid http response received, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testDownloadCRLFromWebWhenThrowingIOException() throws Exception {
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doThrow(IOException.class).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        TPPCertValidatorDataHolder tppCertValidatorDataHolder = Mockito.mock(TPPCertValidatorDataHolder.class);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        Assert.assertNotNull(this.crlValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer));
    }

    @Test(description = "when invalid http response received, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testDownloadCRLFromWebWhenThrowingCertificateException() throws Exception {
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doThrow(CertificateException.class).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        TPPCertValidatorDataHolder tppCertValidatorDataHolder = Mockito.mock(TPPCertValidatorDataHolder.class);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        Assert.assertNotNull(this.crlValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer));
    }

    @Test(description = "when invalid http response received, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testDownloadCRLFromWebWhenThrowingCRLException() throws Exception {
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doThrow(CRLException.class).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        TPPCertValidatorDataHolder tppCertValidatorDataHolder = Mockito.mock(TPPCertValidatorDataHolder.class);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        Assert.assertNotNull(this.crlValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer));
    }

    @Test(description = "when invalid http response received, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testDownloadCRLFromWebWhenThrowingMalformedURLException() throws Exception {
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doThrow(MalformedURLException.class).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        TPPCertValidatorDataHolder tppCertValidatorDataHolder = Mockito.mock(TPPCertValidatorDataHolder.class);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance()).thenReturn(tppCertValidatorDataHolder);

        Assert.assertNotNull(this.crlValidator.checkRevocationStatus(eidasPeerCertificate, eidasPeerCertificateIssuer));
    }

    @Test(description = "when invalid X509URL, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testIsValidX509CRLFromIssuer() throws Exception {
        X509CRL x509CRLMock = Mockito.mock(X509CRL.class);
        WhiteboxImpl.invokeMethod(this.crlValidator, "isValidX509Crl", x509CRLMock,
                eidasPeerCertificate, eidasPeerCertificateIssuer);
    }

    @Test(description = "when X509URL next update date is invalid, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testIsValidX509CRLFromNextUpdate() throws Exception {
        X509CRL x509CRLMock = Mockito.mock(X509CRL.class);
        final Date today = new Date();
        final Date yesterday = new Date(today.getTime() - (1000 * 60 * 60 * 24));
        Mockito.doReturn(yesterday).when(x509CRLMock).getNextUpdate();

        WhiteboxImpl.invokeMethod(this.crlValidator, "isValidX509CRLFromNextUpdate", x509CRLMock,
                today, today);
    }

    @Test(description = "when X509URL next update date is null, then return false")
    public void testIsValidX509CRLFromNextUpdateWithNullDate() throws Exception {
        X509CRL x509CRLMock = Mockito.mock(X509CRL.class);
        boolean result = WhiteboxImpl.invokeMethod(this.crlValidator,
                "isValidX509CRLFromNextUpdate", x509CRLMock, null, null);

        Assert.assertFalse(result);
    }

    @Test(description = "when X509URL verification failed, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testIsValidX509CRLFromIssuerWithFailedVerification() throws Exception {
        X509CRL x509CRLMock = Mockito.mock(X509CRL.class);
        Mockito.doReturn(eidasPeerCertificate.getIssuerDN()).when(x509CRLMock).getIssuerDN();
        Mockito.doThrow(CRLException.class).when(x509CRLMock).verify(Mockito.any(PublicKey.class));

        WhiteboxImpl.invokeMethod(this.crlValidator, "isValidX509CRLFromIssuer", x509CRLMock,
                eidasPeerCertificate, eidasPeerCertificateIssuer);
    }

    @Test(description = "when CRL URL list is empty, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testGetCRLRevocationStatusWithEmptyCRLUrls() throws CertificateValidationException {
        CRLValidator.getCRLRevocationStatus(null, null, 0, Collections.emptyList(), false, "", 0);
    }

    @Test(description = "when invalid cert provided, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testGetCRLUrlsWithInvalidCert() throws CertificateValidationException {
        CRLValidator.getCRLUrls(expiredPeerCertificate);
    }

    @Test(description = "when certificate is revoked, then return revoked revocation status")
    public void testGetRevocationStatusFromCRLWithRevokedCert() throws Exception {
        X509CRL x509CRLMock = Mockito.mock(X509CRL.class);
        Mockito.doReturn(true).when(x509CRLMock).isRevoked(eidasPeerCertificate);

        RevocationStatus actual = WhiteboxImpl.invokeMethod(this.crlValidator, "getRevocationStatusFromCRL",
                x509CRLMock, eidasPeerCertificate);

        Assert.assertSame(actual, RevocationStatus.REVOKED);
    }
}
