/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.util;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for IdentityCommonUtils.
 */
public class IdentityCommonUtilsTest {

    MockedStatic<IdentityExtensionsDataHolder> identityExtensionsDataHolderMockedStatic;

    @BeforeClass
    public void beforeClass() throws IdentityApplicationManagementException, IdentityOAuthAdminException,
            APIResourceMgtException {

        IdentityExtensionsDataHolder identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();
        Map<String, Object> confMap = new HashMap<>();
        confMap.put(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME, "consent_id");
        identityExtensionsDataHolder.setConfigurationMap(confMap);

        identityExtensionsDataHolderMockedStatic = Mockito.mockStatic(IdentityExtensionsDataHolder.class);
        identityExtensionsDataHolderMockedStatic.when(IdentityExtensionsDataHolder::getInstance)
                .thenReturn(identityExtensionsDataHolder);
    }

    @AfterClass
    public void afterClass() {
        identityExtensionsDataHolderMockedStatic.close();
    }

    @Test
    public void testGetConsentIdFromScopes() {
        String[] scopes = {"dummy-scope1", "dummy-scope2", "consent_id_ConsentId", "dummy-scope3", "FS_ConsentId",
                "TIME_ConsentId", "x5t#_ConsentId"};
        Assert.assertTrue(Arrays.toString(scopes).contains("consent_id"));
        String[] modifiedScopes = IdentityCommonUtils.removeInternalScopes(scopes);
        Assert.assertFalse(Arrays.toString(modifiedScopes).contains("consent_id"));
        Assert.assertFalse(Arrays.toString(modifiedScopes).contains("FS_"));
        Assert.assertFalse(Arrays.toString(modifiedScopes).contains("TIME_"));
        Assert.assertFalse(Arrays.toString(modifiedScopes).contains("x5t#_"));
    }

    @Test(description = "when valid transport cert, return x509 certificate")
    public void testParseCertificate() throws FinancialServicesException {
        Assert.assertNotNull(IdentityCommonUtils.parseCertificate(TestConstants.TEST_CLIENT_CERT));
    }

    @Test (expectedExceptions = FinancialServicesException.class)
    public void testParseCertificateWithInvalidCert() throws FinancialServicesException {
        Assert.assertNull(IdentityCommonUtils.parseCertificate("-----INVALID CERTIFICATE-----"));
    }

    @Test
    public void testParseCertificateWithInvalidBase64CharactersCert() throws FinancialServicesException {
        Assert.assertNotNull(IdentityCommonUtils.parseCertificate(TestConstants.WRONGLY_FORMATTED_CERT));
    }

    @Test
    public void testParseCertificateWithEmptyCert() throws FinancialServicesException {
        Assert.assertNull(IdentityCommonUtils.parseCertificate(""));
    }

    @Test(description = "when certificate expired, return true")
    public void testIsExpiredWithExpiredCert() throws FinancialServicesException {
        X509Certificate testCert = IdentityCommonUtils.parseCertificate(TestConstants.EXPIRED_SELF_CERT);
        Assert.assertNotNull(testCert);
        Assert.assertTrue(hasExpired(testCert));
    }

    @Test(description = "when valid certificate, return false")
    public void testIsExpired() throws FinancialServicesException {
        X509Certificate testCert = IdentityCommonUtils.parseCertificate(TestConstants.TEST_CLIENT_CERT);
        Assert.assertNotNull(testCert);
        Assert.assertFalse(hasExpired(testCert));
    }

    /**
     * Test util method to check cert expiry.
     *
     * @param peerCertificate
     * @return
     */
    public static boolean hasExpired(X509Certificate peerCertificate) {
        try {
            peerCertificate.checkValidity();
        } catch (CertificateException e) {
            return true;
        }
        return false;
    }
}
