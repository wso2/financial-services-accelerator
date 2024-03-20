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

package com.wso2.openbanking.accelerator.gateway.executor.impl.mtls.cert.validation.executor;

import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.gateway.cache.CertificateRevocationCache;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.executor.service.CertValidationService;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.gateway.executor.util.TestValidationUtil;
import com.wso2.openbanking.accelerator.gateway.internal.TPPCertValidatorDataHolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;

/**
 * Test cases for MTLSCertValidationExecutor class.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CertificateValidationUtils.class, TPPCertValidatorDataHolder.class,
        CertValidationService.class, CertificateRevocationCache.class})
public class CertRevocationValidationExecutorTest {

    CertRevocationValidationExecutor certRevocationValidationExecutor;
    @Mock
    TPPCertValidatorDataHolder tppCertValidatorDataHolder;
    @Mock
    CertValidationService certValidationService;

    private X509Certificate testPeerCertificate;
    private X509Certificate testPeerCertificateIssuer;
    private X509Certificate eidasPeerCertificate;
    private X509Certificate expiredPeerCertificate;

    @BeforeClass
    public void init() throws CertificateValidationException, CertificateException, OpenBankingException {
        MockitoAnnotations.initMocks(this);
        this.certRevocationValidationExecutor = new CertRevocationValidationExecutor();

        this.testPeerCertificate = TestValidationUtil.getTestClientCertificate();
        this.testPeerCertificateIssuer = TestValidationUtil.getTestClientCertificateIssuer();
        this.eidasPeerCertificate = TestValidationUtil.getTestEidasCertificate();
        this.expiredPeerCertificate = TestValidationUtil.getExpiredSelfCertificate();
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }


    @Test(description = "When expired certificate is provided, then should return true")
    public void testIsCertValidWithExpiredCert() {
        Assert.assertTrue(CertificateUtils.isExpired(expiredPeerCertificate));
    }

    @Test(description = "When certificate validation success, then should return false")
    public void testIsCertRevokedWithNonCachedCert() throws Exception {
        CertificateRevocationCache mock = Mockito.mock(CertificateRevocationCache.class);
        PowerMockito.mockStatic(CertificateRevocationCache.class);
        PowerMockito.when(CertificateRevocationCache.getInstance())
                .thenReturn(mock);

        boolean isCertRevoked = WhiteboxImpl.invokeMethod(this.certRevocationValidationExecutor,
                "isCertRevoked", testPeerCertificate);
        Assert.assertFalse(isCertRevoked);
    }

    @Test(description = "When cached certificate provided, then return false")
    public void testIsCertRevokedWithCachedCert() throws Exception {
        CertificateRevocationCache mock = Mockito.mock(CertificateRevocationCache.class);
        Mockito.doReturn(true).when(mock).getFromCache(Mockito.any(GatewayCacheKey.class));

        PowerMockito.mockStatic(CertificateRevocationCache.class);
        PowerMockito.when(CertificateRevocationCache.getInstance())
                .thenReturn(mock);

        boolean isCertRevoked = WhiteboxImpl.invokeMethod(this.certRevocationValidationExecutor,
                "isCertRevoked", testPeerCertificate);

        Assert.assertFalse(isCertRevoked);
    }

    @Test(description = "When self signed certificate provided, then should return true")
    public void testIsCertRevocationSuccessWithSelfSignedCert() throws Exception {
        Mockito.when(tppCertValidatorDataHolder.isCertificateRevocationValidationEnabled()).thenReturn(true);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        boolean isCertRevocationSuccess = WhiteboxImpl.invokeMethod(this.certRevocationValidationExecutor,
                "isCertRevocationSuccess", expiredPeerCertificate);

        Assert.assertTrue(isCertRevocationSuccess);
    }

    @Test(description = "When isCertificateRevocationValidationEnabled is false, then should return true")
    public void testIsCertRevocationSuccessWithDisabledRevocationValidation() throws Exception {
        Mockito.when(tppCertValidatorDataHolder.isCertificateRevocationValidationEnabled()).thenReturn(false);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        boolean isCertRevocationSuccess = WhiteboxImpl.invokeMethod(this.certRevocationValidationExecutor,
                "isCertRevocationSuccess", expiredPeerCertificate);

        Assert.assertTrue(isCertRevocationSuccess);
    }

    @Test(description = "When certificate issuer is in excluded list, then should return true")
    public void testIsCertRevocationSuccessWithExcludedIssuers() throws Exception {
        Mockito.when(tppCertValidatorDataHolder.isCertificateRevocationValidationEnabled()).thenReturn(true);
        Mockito.when(tppCertValidatorDataHolder.getCertificateRevocationValidationExcludedIssuers())
                .thenReturn(Collections.singletonList(eidasPeerCertificate.getIssuerDN().getName()));

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        boolean isCertRevocationSuccess = WhiteboxImpl.invokeMethod(this.certRevocationValidationExecutor,
                "isCertRevocationSuccess", eidasPeerCertificate);

        Assert.assertTrue(isCertRevocationSuccess);
    }

    @Test(description = "When peer certificate is valid, then should return true")
    public void testIsCertRevocationSuccessWithValidCerts() throws Exception {
        Mockito.when(tppCertValidatorDataHolder.getCertificateRevocationValidationExcludedIssuers()).thenReturn(
                Collections.singletonList(""));
        Mockito.when(tppCertValidatorDataHolder.isCertificateRevocationValidationEnabled()).thenReturn(true);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito
                .when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        PowerMockito.mockStatic(CertificateValidationUtils.class);
        PowerMockito.when(CertificateValidationUtils.getIssuerCertificateFromTruststore(
                Mockito.any(X509Certificate.class))).thenReturn(testPeerCertificateIssuer);

        Mockito.when(certValidationService.verify(Mockito.any(X509Certificate.class),
                Mockito.any(X509Certificate.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(true);

        PowerMockito.mockStatic(CertValidationService.class);
        PowerMockito
                .when(CertValidationService.getInstance())
                .thenReturn(certValidationService);

        boolean isCertRevocationSuccess = WhiteboxImpl.invokeMethod(this.certRevocationValidationExecutor,
                "isCertRevocationSuccess", testPeerCertificate);

        Assert.assertTrue(isCertRevocationSuccess);
    }

    @Test(description = "When peer certificate is invalid, then should throw CertificateValidationException")
    public void testIsCertRevocationSuccessWithInValidCert() throws Exception {
        Mockito.when(tppCertValidatorDataHolder.getCertificateRevocationValidationExcludedIssuers())
                .thenReturn(Collections.singletonList(""));
        Mockito.when(tppCertValidatorDataHolder.isCertificateRevocationValidationEnabled()).thenReturn(true);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito
                .when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        Mockito.when(certValidationService.verify(Mockito.any(X509Certificate.class),
                Mockito.any(X509Certificate.class), Mockito.anyInt())).thenReturn(true);

        PowerMockito.mockStatic(CertValidationService.class);
        PowerMockito
                .when(CertValidationService.getInstance())
                .thenReturn(certValidationService);

        boolean isCertRevocationSuccess = WhiteboxImpl.invokeMethod(this.certRevocationValidationExecutor,
                "isCertRevocationSuccess", testPeerCertificate);

        Assert.assertFalse(isCertRevocationSuccess);
    }

    @Test(description = "When certificate revocation validation not configured, then should return true")
    public void testIsCertRevocationSuccessWithFalseCertificateRevocationValidation() throws Exception {
        Mockito.when(tppCertValidatorDataHolder.isCertificateRevocationValidationEnabled()).thenReturn(false);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito
                .when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        boolean isCertRevocationSuccess = WhiteboxImpl.invokeMethod(this.certRevocationValidationExecutor,
                "isCertRevocationSuccess", testPeerCertificate);

        Assert.assertTrue(isCertRevocationSuccess);
    }

}
