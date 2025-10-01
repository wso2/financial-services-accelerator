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

package org.wso2.financial.services.apim.mediation.policies.mtls.enforcement.utils;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Test class for MTLSEnforcementUtils.
 */
public class MTLSEnforcementUtilsTest {

    private String transportCertificate = "-----BEGIN CERTIFICATE-----" +
            "MIIFODCCBCCgAwIBAgIEWccwPTANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmt" +
            "pbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFByZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjUwMTAzMDc0NjU3Wh" +
            "cNMjYwMjAzMDgxNjU3WjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZBgNVBAsTEjAwMTU4M" +
            "DAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWOVp6RkZCeFNMR0VqUFpvZ1JBYnZGZDCCASIwDQYJKoZIhvcNAQEBBQADggEP" +
            "ADCCAQoCggEBAOw7SXoy2UKzjfoW73EnYX3zLH7eUgHpfQiRvjQvCl7/OAhKEoTr5v4Oz83X3ADXqCtakPiGu/f3xyN" +
            "547N6rKf+0wEfZBH0hPdxMOktTXP2p+SE93SLQTLiM9wRBhmyqZYK19jGRoVjqgApDRF6BwVOhlrsVGoRQrIGq3FeC5" +
            "qqi91Vm1pNGgyqaIU/tPUu7rVvlxV6gXszYPUzHnJZs3X1XCPteqFuPjnsuih/n84oS0XU+CeHzNQec3orK4YqYJYjc" +
            "msYRL7stryHHziTh/tghz9rFVxbFkgVa7VTYJSXcNEMUUd6NSeHPfk4oFdMkbuttz0bP+Pl7CVaWBKjRD8CAwEAAaOC" +
            "AgQwggIAMA4GA1UdDwEB/wQEAwIHgDAgBgNVHSUBAf8EFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwgeAGA1UdIASB2DC" +
            "B1TCB0gYLKwYBBAGodYEGAWQwgcIwKgYIKwYBBQUHAgEWHmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9wb2xpY2llczCBkw" +
            "YIKwYBBQUHAgIwgYYMgYNVc2Ugb2YgdGhpcyBDZXJ0aWZpY2F0ZSBjb25zdGl0dXRlcyBhY2NlcHRhbmNlIG9mIHRoZ" +
            "SBPcGVuQmFua2luZyBSb290IENBIENlcnRpZmljYXRpb24gUG9saWNpZXMgYW5kIENlcnRpZmljYXRlIFByYWN0aWNl" +
            "IFN0YXRlbWVudDBtBggrBgEFBQcBAQRhMF8wJgYIKwYBBQUHMAGGGmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDU" +
            "GCCsGAQUFBzAChilodHRwOi8vb2IudHJ1c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNydDA6BgNVHR8EMzAxMC+gLa" +
            "ArhilodHRwOi8vb2IudHJ1c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNybDAfBgNVHSMEGDAWgBRQc5HGIXLTd/T+A" +
            "BIGgVx5eW4/UDAdBgNVHQ4EFgQU1FXWTE3ZXm6coqvA7Rs265o6+B0wDQYJKoZIhvcNAQELBQADggEBAFq91bVcy21X" +
            "dKHAFSzXoIJ5zsqlw5om/eyUCplocbpYrurcHFK4avrRYOew7Cv/Ca0HkGj+6FNIpjxC/zaBkbKDKHNxVQyJmW42WMO" +
            "UC4rckYmm7AWKoz4irXUMFXb9yVs8MnHXqU4IrS0FsGWpZT51FQ90TS8nJoJqcLyGVTuk4f9mONfZlcJgM0gedMrlIW" +
            "JelMpIRAos+vM3PGBQGledYd9JLGtWxuE7bdzbbXnMPFmx3s8UsmpDoxiPNr8n7633mDfhNMLo5OBX6+qFGi0UNf62J" +
            "JWz2SHPIpt+9lBbsp3T9xstYaz4qa1QHh+sw+IZ/rr0lPMhaCxJpsONl0E=" +
            "-----END CERTIFICATE-----";

    @DataProvider(name = "certificateDataProvider")
    public Object[][] certificateDataProvider() {
        return new Object[][]{
                { transportCertificate, false },
                { URLEncoder.encode(transportCertificate, StandardCharsets.UTF_8), true },
        };
    }

    @Test(dataProvider = "certificateDataProvider")
    public void testParseCertificate(String certificate, boolean isEncoded)
            throws UnsupportedEncodingException, CertificateException {
        Certificate result = MTLSEnforcementUtils.parseCertificate(certificate, isEncoded);
        Assert.assertNotNull(result, "The certificate should not be null.");
        Assert.assertEquals(result.getType(), "X.509", "The certificate type should be X.509.");
    }

    @Test
    public void testParseCertificateFromHeaderWithNullCertificate()
            throws UnsupportedEncodingException, CertificateException {
        Certificate result = MTLSEnforcementUtils.parseCertificate(null, false);
        Assert.assertNull(result, "The certificate should be null when input is null.");
    }

    @Test(expectedExceptions = CertificateException.class)
    public void testParseCertificateFromHeaderWithInvalidCertificate()
            throws UnsupportedEncodingException, CertificateException {
        String invalidCertificate = "Invalid Certificate Content";
        MTLSEnforcementUtils.parseCertificate(invalidCertificate, false);
    }
}
