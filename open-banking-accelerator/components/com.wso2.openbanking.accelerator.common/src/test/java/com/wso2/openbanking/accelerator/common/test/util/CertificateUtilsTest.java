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

package com.wso2.openbanking.accelerator.common.test.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.security.cert.X509Certificate;

/**
 * Certificate Util test class.
 */
public class CertificateUtilsTest {

    @Test(description = "when valid transport cert, return x509 certificate")
    public void testParseCertificate() throws OpenBankingException {
        Assert.assertNotNull(CertificateUtils
                .parseCertificate(CommonTestUtil.TEST_CLIENT_CERT));
    }

    @Test (expectedExceptions = OpenBankingException.class)
    public void testParseCertificateWithInvalidCert() throws OpenBankingException {
        Assert.assertNull(CertificateUtils
                .parseCertificate("-----INVALID CERTIFICATE-----"));
    }

    @Test
    public void testParseCertificateWithInvalidBase64CharactersCert() throws OpenBankingException {
        Assert.assertNotNull(CertificateUtils
                .parseCertificate(CommonTestUtil.WRONGLY_FORMATTED_CERT));
    }

    @Test
    public void testParseCertificateWithEmptyCert() throws OpenBankingException {
        Assert.assertNull(CertificateUtils
                .parseCertificate(""));
    }

    @Test(description = "when certificate expired, return true")
    public void testIsExpiredWithExpiredCert() throws OpenBankingException {
        X509Certificate testCert = CertificateUtils
                .parseCertificate(CommonTestUtil.EXPIRED_SELF_CERT);
        Assert.assertNotNull(testCert);
        Assert.assertTrue(CommonTestUtil.hasExpired(testCert));
    }

    @Test(description = "when valid certificate, return false")
    public void testIsExpired() throws OpenBankingException {
        X509Certificate testCert = CertificateUtils
                .parseCertificate(CommonTestUtil.TEST_CLIENT_CERT);
        Assert.assertNotNull(testCert);
        Assert.assertFalse(CommonTestUtil.hasExpired(testCert));
    }
}
