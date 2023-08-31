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

package com.wso2.openbanking.accelerator.gateway.executor.util;

import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import javax.security.cert.CertificateEncodingException;

/**
 * Test for certificate validation utils.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest(KeyStore.class)
public class CertificateValidationUtilsTest extends PowerMockTestCase {

    String path = "src/test/resources";
    File file = new File(path);
    String absolutePathForTestResources = file.getAbsolutePath();

    @Test(description = "when valid certificate, then return java.security.cert.X509Certificate")
    public void testConvertWithValidCert() {
        javax.security.cert.X509Certificate testCert = TestValidationUtil
                .getCertFromStr(TestValidationUtil.TEST_CLIENT_CERT);

        Assert.assertNotNull(CertificateValidationUtils.convert(testCert));
        Assert.assertTrue(CertificateValidationUtils.convert(testCert).orElse(null) instanceof X509Certificate);
    }

    @Test(description = "when null certificate, then return Optional empty")
    public void testConvertWithNullCert() {
        Assert.assertFalse(CertificateValidationUtils.convert(null)
                .isPresent());
    }

    @Test(description = "when valid certificate, then return java.security.cert.X509Certificate")
    public void testConvertCertWithValidCert() throws CertificateException {
        javax.security.cert.X509Certificate testCert = TestValidationUtil
                .getCertFromStr(TestValidationUtil.TEST_CLIENT_CERT);

        Assert.assertNotNull(CertificateValidationUtils.convertCert(testCert));
        Assert.assertTrue(CertificateValidationUtils.convertCert(testCert)
                .orElse(null) instanceof X509Certificate);
    }

    @Test(description = "when null certificate, then return Optional empty")
    public void testConvertCertWithNullCert() throws CertificateException {
        Assert.assertFalse(CertificateValidationUtils.convertCert(null)
                .isPresent());
    }

    @Test(description = "when client store is null, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testGetIssuerCertificateFromNullTruststore() throws CertificateValidationException,
            OpenBankingException {

        X509Certificate peerCertificate = CertificateUtils.parseCertificate(TestValidationUtil.TEST_CLIENT_CERT);

        X509Certificate issuerCertificate = CertificateValidationUtils
                .getIssuerCertificateFromTruststore(peerCertificate);

        Assert.assertNotNull(issuerCertificate);
    }

    @Test(description = "when valid peer certificate, then issuer certificate should return")
    public void testGetIssuerCertificateFromTruststore() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException, CertificateValidationException, OpenBankingException {

        X509Certificate peerCertificate = CertificateUtils.parseCertificate(TestValidationUtil.TEST_CLIENT_CERT);

        CertificateValidationUtils.loadTrustStore(absolutePathForTestResources + "/client-truststore.jks",
                "wso2carbon".toCharArray());
        X509Certificate issuerCertificate = CertificateValidationUtils
                .getIssuerCertificateFromTruststore(peerCertificate);

        Assert.assertNotNull(issuerCertificate);
        Assert.assertEquals(issuerCertificate.getSubjectDN().getName(), peerCertificate.getIssuerDN().getName());
    }

    @Test(description = "when error occured, then should set error true in OBAPIRequestContext object")
    public void testHandleExecutorErrors() {
        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        CertificateValidationException exception = new CertificateValidationException("dummy exception");

        CertificateValidationUtils.handleExecutorErrors(exception, obapiRequestContext);

        Mockito.verify(obapiRequestContext, Mockito.times(1)).setError(true);
        Mockito.verify(obapiRequestContext, Mockito.times(1))
                .setErrors(Mockito.any(ArrayList.class));
    }

    @Test(description = "should return current date")
    public void testGetNewDate() {
        Instant actual = CertificateValidationUtils.getNewDate().toInstant().truncatedTo(ChronoUnit.DAYS);
        Instant expected = new Date().toInstant().truncatedTo(ChronoUnit.DAYS);
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "when uninitialized keystore, then throw CertificateValidationException",
            expectedExceptions = CertificateValidationException.class)
    public void testRetrieveCertificateFromTruststore() throws KeyStoreException, CertificateValidationException {
        PowerMockito.mockStatic(KeyStore.class);
        KeyStore keyStoreMock = PowerMockito.mock(KeyStore.class);

        CertificateValidationUtils.retrieveCertificateFromTruststore(null, keyStoreMock);
    }

    @Test(description = "when invalid encoded certificate, then return empty",
            expectedExceptions = CertificateException.class)
    public void testConvertWithInvalidCertEncoding() throws CertificateEncodingException, CertificateException {
        javax.security.cert.X509Certificate x509CertificateMock = Mockito
                .mock(javax.security.cert.X509Certificate.class);
        Mockito.doThrow(CertificateEncodingException.class).when(x509CertificateMock).getEncoded();

        Optional<X509Certificate> convertedCert = CertificateValidationUtils.convertCert(x509CertificateMock);
        Assert.assertFalse(convertedCert.isPresent());
    }

    @Test(description = "when error occurred while converting, then return empty",
            expectedExceptions = CertificateException.class)
    public void testConvertWithInvalidCert() throws CertificateEncodingException, CertificateException {
        javax.security.cert.X509Certificate x509CertificateMock = Mockito
                .mock(javax.security.cert.X509Certificate.class);
        Mockito.doThrow(CertificateException.class).when(x509CertificateMock).getEncoded();

        Optional<X509Certificate> convertedCert = CertificateValidationUtils.convertCert(x509CertificateMock);
        Assert.assertFalse(convertedCert.isPresent());
    }

    @Test(description = "when valid java.security.cert.Certificate is provided, " +
            "then return java.security.cert.X509Certificate")
    public void testConvertCertToX509CertWithValidCert() throws OpenBankingException, CertificateException {
        Certificate testCert = TestValidationUtil.getTestClientCertificate();

        Assert.assertTrue(CertificateValidationUtils.convertCertToX509Cert(testCert).isPresent());
        Assert.assertTrue(CertificateValidationUtils.convertCertToX509Cert(testCert).get() instanceof X509Certificate);
    }

    @Test(expectedExceptions = CertificateException.class, description = "When an invalid certificate is provided, " +
            "throw a CertificateException")
    public void testConvertCertToX509CertException() throws CertificateException {

        CertificateValidationUtils.convertCertToX509Cert(TestValidationUtil.getEmptyTestCertificate());
    }
}
