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

package com.wso2.openbanking.accelerator.gateway.executor.service;

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCache;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.exception.TPPValidationException;
import com.wso2.openbanking.accelerator.common.model.PSD2RoleEnum;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.cache.TppValidationCache;
import com.wso2.openbanking.accelerator.gateway.executor.model.RevocationStatus;
import com.wso2.openbanking.accelerator.gateway.executor.revocation.OCSPValidator;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for certificate validation service.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class, OCSPValidator.class, TPPCertValidatorDataHolder.class,
        TppValidationCache.class})
public class CertValidationServiceTest {

    @Mock
    OpenBankingConfigParser openBankingConfigParser;
    @Mock
    TPPCertValidatorDataHolder tppCertValidatorDataHolder;
    @Mock
    TppValidationCache tppValidationCache;
    CertValidationService certValidationService;
    private X509Certificate testPeerCertificate;
    private X509Certificate testPeerCertificateIssuer;
    private X509Certificate eidasPeerCertificate;

    @BeforeClass
    public void init() throws CertificateValidationException, CertificateException, OpenBankingException {
        MockitoAnnotations.initMocks(this);
        certValidationService = CertValidationService.getInstance();
        this.testPeerCertificate = TestValidationUtil.getTestClientCertificate();
        this.testPeerCertificateIssuer = TestValidationUtil.getTestClientCertificateIssuer();
        this.eidasPeerCertificate = TestValidationUtil.getTestEidasCertificate();
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test(description = "when valid certificate provided, then should return true")
    public void testVerifyWithValidCertificate() throws Exception {
        Map<Integer, String> validators = new HashMap<>();
        validators.put(1, "OCSP");

        Mockito.when(openBankingConfigParser.getCertificateRevocationValidators()).thenReturn(validators);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        PowerMockito.mockStatic(OCSPValidator.class);
        PowerMockito.when(OCSPValidator.getOCSPRevocationStatus(Mockito.any(X509Certificate.class),
                Mockito.any(X509Certificate.class), Mockito.anyInt(), Mockito.anyListOf(String.class),
                Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(RevocationStatus.GOOD);

        boolean isVerified = this.certValidationService
                .verify(testPeerCertificate, testPeerCertificateIssuer, 1);
        Assert.assertTrue(isVerified);
    }

    @Test(description = "when valid certificate provided, then should return true")
    public void testUpdatedVerifyWithValidCertificate() throws Exception {
        Map<Integer, String> validators = new HashMap<>();
        validators.put(1, "OCSP");

        Mockito.when(openBankingConfigParser.getCertificateRevocationValidators()).thenReturn(validators);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        PowerMockito.mockStatic(OCSPValidator.class);
        PowerMockito.when(OCSPValidator.getOCSPRevocationStatus(Mockito.any(X509Certificate.class),
                        Mockito.any(X509Certificate.class), Mockito.anyInt(), Mockito.anyListOf(String.class),
                        Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(RevocationStatus.GOOD);

        boolean isVerified = this.certValidationService
                .verify(testPeerCertificate, testPeerCertificateIssuer, 1, 5000,
                        5000, 5000);
        Assert.assertTrue(isVerified);
    }

    @Test(description = "when invalid certificate provided, then should return false")
    public void testVerifyWithValidInvalidCertificate() throws Exception {
        Map<Integer, String> validators = new HashMap<>();
        validators.put(1, "OCSP");

        Mockito.when(openBankingConfigParser.getCertificateRevocationValidators()).thenReturn(validators);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        PowerMockito.mockStatic(OCSPValidator.class);
        PowerMockito.when(OCSPValidator.getOCSPRevocationStatus(Mockito.any(X509Certificate.class),
                Mockito.any(X509Certificate.class), Mockito.anyInt(), Mockito.anyListOf(String.class),
                Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(RevocationStatus.REVOKED);

        boolean isVerified = this.certValidationService
                .verify(testPeerCertificate, testPeerCertificateIssuer, 1);
        Assert.assertFalse(isVerified);

        boolean isVerifiedUpdated = this.certValidationService
                .verify(testPeerCertificate, testPeerCertificateIssuer, 1,
                        5000, 5000, 5000);
        Assert.assertFalse(isVerifiedUpdated);
    }

    @Test(description = "when invalid validator configured, then should return false")
    public void testVerifyWithInvalidValidator() throws Exception {
        Map<Integer, String> validators = new HashMap<>();
        validators.put(1, "INVALID VALIDATOR");

        Mockito.when(openBankingConfigParser.getCertificateRevocationValidators()).thenReturn(validators);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        boolean isVerified = this.certValidationService
                .verify(testPeerCertificate, testPeerCertificateIssuer, 1);
        Assert.assertFalse(isVerified);

        boolean isVerifiedUpdated = this.certValidationService
                .verify(testPeerCertificate, testPeerCertificateIssuer, 1,
                        5000, 5000, 5000);
        Assert.assertFalse(isVerifiedUpdated);
    }

    @Test(description = "when cert roles and scope roles are equal, then return true")
    public void testIsRequiredRolesMatchWithValidScopes() throws Exception {
        boolean isValid = WhiteboxImpl.invokeMethod(this.certValidationService,
                "isRequiredRolesMatchWithScopes", eidasPeerCertificate,
                Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP));

        Assert.assertTrue(isValid);
    }

    @Test(description = "when cert roles and scope roles are not equal, then throw TPPValidationException",
    expectedExceptions = {TPPValidationException.class})
    public void testIsRequiredRolesMatchWithInvalidScopes() throws Exception {
        WhiteboxImpl.invokeMethod(this.certValidationService,
                "isRequiredRolesMatchWithScopes", eidasPeerCertificate,
                Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP, PSD2RoleEnum.ASPSP));
    }

    @Test(description = "When tpp certificate and roles are valid, then should return true")
    public void testValidateTPPWithValidRoles() throws TPPValidationException,
            CertificateValidationException, OpenBankingException {
        Mockito.when(tppCertValidatorDataHolder.isTppValidationEnabled()).thenReturn(true);
        Mockito.when(tppCertValidatorDataHolder.getTPPValidationServiceImpl()).thenReturn("/dummy/path");

        TPPValidationService mockTppValidationService = Mockito.mock(TPPValidationService.class);
        Mockito.when(mockTppValidationService.getCacheKey(Mockito.any(X509Certificate.class),
                Mockito.anyListOf(PSD2RoleEnum.class), Mockito.anyMapOf(String.class, Object.class)))
                .thenReturn("dummy-cache-key");
        Mockito.when(mockTppValidationService.validate(Mockito.any(X509Certificate.class),
                Mockito.anyListOf(PSD2RoleEnum.class), Mockito.anyMapOf(String.class, Object.class))).thenReturn(true);

        Mockito.when(tppCertValidatorDataHolder.getTppValidationService()).thenReturn(mockTppValidationService);
        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        PowerMockito.mockStatic(TppValidationCache.class);
        PowerMockito.when(TppValidationCache.getInstance())
                .thenReturn(tppValidationCache);

        Assert.assertTrue(certValidationService.validateTppRoles(eidasPeerCertificate,
                Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP)));
    }

    @Test(description = "When role is failing, then should return false ")
    public void testValidateTPPWithValidationError() throws TPPValidationException,
            CertificateValidationException, OpenBankingException {
        Mockito.when(tppCertValidatorDataHolder.isTppValidationEnabled()).thenReturn(true);
        Mockito.when(tppCertValidatorDataHolder.getTPPValidationServiceImpl()).thenReturn("/dummy/path");

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        TPPValidationService mockTppValidationService = Mockito.mock(TPPValidationService.class);
        Mockito.when(mockTppValidationService.getCacheKey(Mockito.any(X509Certificate.class),
                Mockito.anyListOf(PSD2RoleEnum.class), Mockito.anyMapOf(String.class, Object.class)))
                .thenReturn("dummy-cache-key");

        Mockito.when(tppCertValidatorDataHolder.getTppValidationService()).thenReturn(mockTppValidationService);
        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        Mockito.when(tppValidationCache.getFromCacheOrRetrieve(Mockito.any(GatewayCacheKey.class),
                Mockito.any(OpenBankingBaseCache.OnDemandRetriever.class))).thenThrow(OpenBankingException.class);
        PowerMockito.mockStatic(TppValidationCache.class);
        PowerMockito.when(TppValidationCache.getInstance())
                .thenReturn(tppValidationCache);

        Assert.assertFalse(certValidationService.validateTppRoles(eidasPeerCertificate,
                Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP)));
    }

    @Test(description = "When TPPValidationImpl path configuration is empty, then should throw TPPValidationException",
            expectedExceptions = {TPPValidationException.class})
    public void testValidateTPPWithEmptyImplConfig() throws TPPValidationException, CertificateValidationException {
        Mockito.when(tppCertValidatorDataHolder.isTppValidationEnabled()).thenReturn(true);
        Mockito.when(tppCertValidatorDataHolder.getTPPValidationServiceImpl()).thenReturn("");

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        Assert.assertFalse(certValidationService
                .validateTppRoles(eidasPeerCertificate, Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP)));
    }

    @Test(description = "When TPPValidationImpl path configuration is invalid, then throw TPPValidationException",
            expectedExceptions = {TPPValidationException.class})
    public void testValidateTPPWithInvalidImplConfig() throws TPPValidationException, CertificateValidationException {
        Mockito.when(tppCertValidatorDataHolder.isTppValidationEnabled()).thenReturn(true);
        Mockito.when(tppCertValidatorDataHolder.getTPPValidationServiceImpl()).thenReturn("dummy-path");
        Mockito.when(tppCertValidatorDataHolder.getTppValidationService()).thenReturn(null);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        Assert.assertFalse(certValidationService
                .validateTppRoles(eidasPeerCertificate, Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP)));
    }

    @Test(description = "When tpp certificate and roles are valid, then should return true")
    public void testValidateTPPWithCustomRoleValidation()
            throws TPPValidationException, CertificateValidationException {
        Mockito.when(tppCertValidatorDataHolder.isTppValidationEnabled()).thenReturn(false);
        Mockito.when(tppCertValidatorDataHolder.isPsd2RoleValidationEnabled()).thenReturn(true);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        Assert.assertTrue(certValidationService
                .validateTppRoles(eidasPeerCertificate, Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP)));
    }

    @Test(description = "When both tppValidationEnabled and psd2RoleValidationEnabled are false, " +
            "then should throw TPPValidationException", expectedExceptions = TPPValidationException.class)
    public void testValidateTPPWithInvalidConfigs() throws TPPValidationException, CertificateValidationException {
        Mockito.when(tppCertValidatorDataHolder.isTppValidationEnabled()).thenReturn(false);
        Mockito.when(tppCertValidatorDataHolder.isPsd2RoleValidationEnabled()).thenReturn(false);

        PowerMockito.mockStatic(TPPCertValidatorDataHolder.class);
        PowerMockito.when(TPPCertValidatorDataHolder.getInstance())
                .thenReturn(tppCertValidatorDataHolder);

        Assert.assertTrue(certValidationService
                .validateTppRoles(eidasPeerCertificate, Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP)));
    }

    @Test(description = "when valid certificate and roles provided, then return true")
    public void testIsRequiredRolesMatchWithScopes() throws Exception {
        List<PSD2RoleEnum> requiredPSD2Roles = Arrays.asList(PSD2RoleEnum.AISP, PSD2RoleEnum.PISP);

        boolean isValid = WhiteboxImpl.invokeMethod(this.certValidationService,
                "isRequiredRolesMatchWithScopes", eidasPeerCertificate, requiredPSD2Roles);

        Assert.assertTrue(isValid);
    }

    @Test(description = "when invalid certificate and roles provided, then throw TPPValidationException",
            expectedExceptions = {TPPValidationException.class})
    public void testIsRequiredRolesMatchWithScopesWithInvalidRoles() throws Exception {
        WhiteboxImpl.invokeMethod(this.certValidationService,
                "isRequiredRolesMatchWithScopes", eidasPeerCertificate, Collections.singletonList(PSD2RoleEnum.ASPSP));
    }

    @Test(description = "when valid certificate provided, then should return GOOD revocation status")
    public void testIsRevokedWithValidCert() throws Exception {
        OCSPValidator mockOCSPValidator = Mockito.mock(OCSPValidator.class);
        Mockito.when(mockOCSPValidator.checkRevocationStatus(Mockito.any(X509Certificate.class),
                Mockito.any(X509Certificate.class))).thenReturn(RevocationStatus.GOOD);

        RevocationStatus result = WhiteboxImpl.invokeMethod(this.certValidationService,
                "isRevoked", mockOCSPValidator, eidasPeerCertificate, testPeerCertificateIssuer);

        Assert.assertSame(result, RevocationStatus.GOOD);
    }

    @Test(description = "when valid certificate provided, then should return UNKNOWN revocation status")
    public void testIsRevokedWithInvalidCert() throws Exception {
        OCSPValidator mockOCSPValidator = Mockito.mock(OCSPValidator.class);
        Mockito.when(mockOCSPValidator.checkRevocationStatus(Mockito.any(X509Certificate.class),
                Mockito.any(X509Certificate.class))).thenThrow(CertificateValidationException.class);

        RevocationStatus result = WhiteboxImpl.invokeMethod(this.certValidationService,
                "isRevoked", mockOCSPValidator, eidasPeerCertificate, testPeerCertificateIssuer);

        Assert.assertSame(result, RevocationStatus.UNKNOWN);
    }

}
