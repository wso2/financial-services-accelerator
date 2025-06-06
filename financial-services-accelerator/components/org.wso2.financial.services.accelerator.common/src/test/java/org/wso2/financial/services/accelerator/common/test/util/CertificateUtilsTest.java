/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.common.test.util;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.test.util.testutils.CommonTestUtil;
import org.wso2.financial.services.accelerator.common.util.CertificateUtils;

import java.security.cert.X509Certificate;

/**
 * Certificate Util test class.
 */
public class CertificateUtilsTest {

    private X509Certificate expiredX509Cert;

    @BeforeClass
    public void init() throws FinancialServicesException {
        this.expiredX509Cert = CommonTestUtil.getExpiredSelfCertificate();
    }

    @Test(description = "when valid transport cert, return x509 certificate")
    public void testParseCertificate() throws FinancialServicesException {

        Assert.assertNotNull(CertificateUtils.parseCertificate(CommonTestUtil.TEST_CLIENT_CERT));
    }

    @Test (expectedExceptions = FinancialServicesException.class)
    public void testParseCertificateWithInvalidCert() throws FinancialServicesException {
        Assert.assertNull(CertificateUtils
                .parseCertificate("-----INVALID CERTIFICATE-----"));
    }

    @Test
    public void testParseCertificateWithInvalidBase64CharactersCert() throws FinancialServicesException {

        Assert.assertNotNull(CertificateUtils.parseCertificate(CommonTestUtil.WRONGLY_FORMATTED_CERT));
    }

    @Test
    public void testParseCertificateWithEmptyCert() throws FinancialServicesException {

        Assert.assertNull(CertificateUtils.parseCertificate(""));
    }

    @Test(description = "when certificate expired, return true")
    public void testIsExpiredWithExpiredCert() throws FinancialServicesException {

        X509Certificate testCert = CertificateUtils.parseCertificate(CommonTestUtil.EXPIRED_SELF_CERT);
        Assert.assertNotNull(testCert);
        Assert.assertTrue(CommonTestUtil.hasExpired(testCert));
    }

    @Test
    public void testIsCertValidWithExpiredCert() {
        Assert.assertTrue(CertificateUtils.isExpired(expiredX509Cert));
    }

}
