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

package com.wso2.openbanking.accelerator.common.test.util.eidas.certificate.extractor;

import com.wso2.openbanking.accelerator.common.test.util.CommonTestUtil;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Certificate content extractor test.
 */
public class CertificateContentExtractorTest {


    @Test
    public void testExtractValidCertificate() throws Exception {

        X509Certificate cert =
                CommonTestUtil.parseTransportCert(CommonTestUtil.EIDAS_CERT).orElse(null);

        CertificateContent extract = CertificateContentExtractor.extract(cert);

        Assert.assertTrue(extract.getPspRoles().size() == 3);
        Assert.assertTrue(extract.getPspRoles().contains("PSP_AI"));
        Assert.assertTrue(extract.getPspRoles().contains("PSP_PI"));
        Assert.assertTrue(extract.getPspRoles().contains("PSP_IC"));
        Assert.assertTrue(extract.getPspAuthorisationNumber().equals("PSDDE-BAFIN-123456"));
        Assert.assertTrue(extract.getName().equals("www.hanseaticbank.de"));
        Assert.assertTrue(extract.getNcaName().equals("Federal Financial Supervisory Authority"));
        Assert.assertTrue(extract.getNcaId().equals("DE-BAFIN"));
    }

    @Test
    public void testExtractPSD2RoleFromCert() throws Exception {

        X509Certificate cert =
                CommonTestUtil.parseTransportCert(CommonTestUtil.EIDAS_CERT).orElse(null);

        CertificateContent extract = CertificateContentExtractor.extract(cert);

        Assert.assertTrue(extract.getPsd2Roles().size() == 3);
        Assert.assertTrue(extract.getPsd2Roles().contains("AISP"));
        Assert.assertTrue(extract.getPsd2Roles().contains("PISP"));
        Assert.assertTrue(extract.getPsd2Roles().contains("CBPII"));
    }

    @Test
    public void testExtractInvalidCertificate() throws CertificateException {

        X509Certificate cert = CommonTestUtil
                .parseTransportCert(CommonTestUtil.TEST_CLIENT_CERT).orElse(null);
        try {
            CertificateContentExtractor.extract(cert);
        } catch (Exception ex) {
            Assert.assertEquals("X509 V3 Extensions not found in the certificate.", ex.getMessage());
        }
    }
}
