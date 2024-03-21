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

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for tests.
 */
public class CommonTestUtil {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public static final String X509_CERT_INSTANCE_NAME = "X.509";
    private static X509Certificate expiredSelfCertificate = null;
    public static final String EIDAS_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIEjDCCA3SgAwIBAgILAKTSmx6PZuerUKkwDQYJKoZIhvcNAQELBQAwSDELMAkG" +
            "A1UEBhMCREUxDDAKBgNVBAoMA0JEUjERMA8GA1UECwwISVQgLSBEZXYxGDAWBgNV" +
            "BAMMD1BTRDIgVGVzdCBTdWJDQTAeFw0xODEyMTIxNDIzMjBaFw0xOTA2MTAxNDIz" +
            "MjBaMIHFMQswCQYDVQQGEwJERTEkMCIGA1UECgwbSGFuc2VhdGljIEJhbmsgR21i" +
            "SCAmIENvIEtHMRAwDgYDVQQHDAdIYW1idXJnMRAwDgYDVQQIDAdoYW1idXJnMSAw" +
            "HgYDVQQJDBdCcmFtZmVsZGVyIENoYXVzc2VlIDEwMTEdMBsGA1UEAwwUd3d3Lmhh" +
            "bnNlYXRpY2JhbmsuZGUxGzAZBgNVBGETElBTRERFLUJBRklOLTEyMzQ1NjEOMAwG" +
            "A1UEERMFMjIxNzcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCq+FfA" +
            "Yg8kcypd0HWhZqW8vtm/1KV+CVbertirwc3nbeufIha82kmJr/0ybxPhdJuPSXPA" +
            "9YPnGyg4aHBjwGWJhI5sMxynVB6+JrENu1wp4MSUr6BUrNvpiYo7uU2mEe9jEheQ" +
            "vqQ45vPw1f2B1YSZgQ5OaSAeLnOqjwDoHseT+mNSJbRznJguwb7hLl78VCuJeYrB" +
            "8E1AfJrrKWAVov6TldInq8xP47kspJCheIrEMZskehvuvn11ir24CnTrFe6G4B2v" +
            "e5VDR40YbYGD/yD/m8Y2/Y5BGZGw7ty5RqS0ubB99lRkc13KpkAEI45QWQyXVTIF" +
            "cORodKZHdoSwcORZAgMBAAGjgfgwgfUwgYgGCCsGAQUFBwEDBHwwejB4BgYEAIGY" +
            "JwIwbjA5MBEGBwQAgZgnAQQMBlBTUF9JQzARBgcEAIGYJwECDAZQU1BfUEkwEQYH" +
            "BACBmCcBAwwGUFNQX0FJDCdGZWRlcmFsIEZpbmFuY2lhbCBTdXBlcnZpc29yeSBB" +
            "dXRob3JpdHkMCERFLUJBRklOMAsGA1UdDwQEAwIFoDAdBgNVHSUEFjAUBggrBgEF" +
            "BQcDAQYIKwYBBQUHAwIwGwYDVR0RBBQwEoIQaGFuc2VhdGljYmFuay5kZTAfBgNV" +
            "HSMEGDAWgBRczgCARJu0XDNe5JrFOPI4CIgFojANBgkqhkiG9w0BAQsFAAOCAQEA" +
            "l8IGrflLcPpmpKIxlmASRtPk96Dh5E3Is2dDCO/yiv2TKoBjyGRYLSPKD7mS1YBr" +
            "g2/l2yPK6+5V5n/pIZ3V8SezfFlzs65i1Jc9XwB/236BjgRMXuAMJmB0Rjo31Nd0" +
            "o/FAOEvIpNh0+GVz6SnY07qry2BWCYAqyHSih20Wjj6yOHHvQtXRaQijQSwo5WGS" +
            "grHH0Thh+MBlyc8iNajrrNxKRYWyXrpGpukMOR4CYWp2CeC22+zLQIEI9gcnl0hr" +
            "/yBbG/db3ZujZvjW34KreOkxjzc+/bhQlubv7KrSruj1OoDzq8e+ELCoNII2JU3h" +
            "R783WZESc2tbq1LYOH5wNg==" +
            "-----END CERTIFICATE-----";
    public static final String TEST_CLIENT_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIFLTCCBBWgAwIBAgIEWcbiiDANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjMxMTE1MDUxMDA4WhcNMjQxMjE1" +
            "MDU0MDA4WjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZ" +
            "BgNVBAsTEjAwMTU4MDAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWakZRdVE0ZVFiTkNN" +
            "U3FkQ29nMjFuRjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKi36HD0" +
            "prx1N3pfafJc6pSMg0i0jOiQrt+WZ6GphKUhA2JrbBxWTvabX1Q7hifl5UbkBOkn" +
            "n3SCyqBplPSqksYLdPSckb2J7UVLj76O7RkELoFD+vSrQ8FWEn8lXv4UiS8ylvlr" +
            "IPa8+pVtAwOuI3/YFmqB1lcHgBI8EELDUWHXSp3OhkcKjrm76t1sQz0tsmi5aHZR" +
            "3FXb0dACQO+dGakIerqzUmAdccLtWuSJMT+b7c7IDDm/IR/MV7iol04yeBkOlWre" +
            "IKOqHU16PI3N/PSMEt96JAUKZ/n0VC0x4YHaBYDX27sf4Gypubgmkcd4IbG/8XEu" +
            "Hq3Ik84oEpP5QEUCAwEAAaOCAfkwggH1MA4GA1UdDwEB/wQEAwIGwDAVBgNVHSUE" +
            "DjAMBgorBgEEAYI3CgMMMIHgBgNVHSAEgdgwgdUwgdIGCysGAQQBqHWBBgFkMIHC" +
            "MCoGCCsGAQUFBwIBFh5odHRwOi8vb2IudHJ1c3Rpcy5jb20vcG9saWNpZXMwgZMG" +
            "CCsGAQUFBwICMIGGDIGDVXNlIG9mIHRoaXMgQ2VydGlmaWNhdGUgY29uc3RpdHV0" +
            "ZXMgYWNjZXB0YW5jZSBvZiB0aGUgT3BlbkJhbmtpbmcgUm9vdCBDQSBDZXJ0aWZp" +
            "Y2F0aW9uIFBvbGljaWVzIGFuZCBDZXJ0aWZpY2F0ZSBQcmFjdGljZSBTdGF0ZW1l" +
            "bnQwbQYIKwYBBQUHAQEEYTBfMCYGCCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rp" +
            "cy5jb20vb2NzcDA1BggrBgEFBQcwAoYpaHR0cDovL29iLnRydXN0aXMuY29tL29i" +
            "X3BwX2lzc3VpbmdjYS5jcnQwOgYDVR0fBDMwMTAvoC2gK4YpaHR0cDovL29iLnRy" +
            "dXN0aXMuY29tL29iX3BwX2lzc3VpbmdjYS5jcmwwHwYDVR0jBBgwFoAUUHORxiFy" +
            "03f0/gASBoFceXluP1AwHQYDVR0OBBYEFKjCef/JxD+ND9eSb7hQlmEhSxUqMA0G" +
            "CSqGSIb3DQEBCwUAA4IBAQCnKH9FdLmJMruX2qfbrpT0qaV8bP7xa9UDRYSMsAWC" +
            "2kqCxs8CJmARt5+xsxBW6P65+mkLS2vXgQl7J8RTMiQVnHJvvNaldYnV6odsYOqv" +
            "v+vGib8Qe0gKWSjih+Gd1Ct4UQFtn6P3ph+6OBB0OieZb7DYXqPJrX5UlG7K2fQ4" +
            "0MdFgBdeQZ3iNkXi43UIrQ5cF4cjYavmEFRmYeHya8AKfNCiWly15mNazW/X6SWf" +
            "7pz+yk/l+gBv0wm3QT7ANXGf8izgoh6T5fmixPXSbdn8RUIV0kXp2TRRZ+CYUWBP" +
            "Jc3PvRXiiEEo2eHLXfEHG2jzrt1iKnjk6hzuC1hUzK0t" +
            "-----END CERTIFICATE-----";
    public static final String EXPIRED_SELF_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIDiTCCAnGgAwIBAgIENx3SZjANBgkqhkiG9w0BAQsFADB1MQswCQYDVQQGEwJs" +
            "azEQMA4GA1UECBMHd2VzdGVybjEQMA4GA1UEBxMHY29sb21ibzENMAsGA1UEChME" +
            "d3NvMjEUMBIGA1UECxMLb3BlbmJhbmtpbmcxHTAbBgNVBAMTFG9wZW5iYW5raW5n" +
            "LndzbzIuY29tMB4XDTIwMDMwMTEyMjE1MVoXDTIwMDUzMDEyMjE1MVowdTELMAkG" +
            "A1UEBhMCbGsxEDAOBgNVBAgTB3dlc3Rlcm4xEDAOBgNVBAcTB2NvbG9tYm8xDTAL" +
            "BgNVBAoTBHdzbzIxFDASBgNVBAsTC29wZW5iYW5raW5nMR0wGwYDVQQDExRvcGVu" +
            "YmFua2luZy53c28yLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB" +
            "AKWMb1mhSthxi5vmQcvEnt0rauYv8uFWjGyiuCkk5wQbArybGXyC8rrZf5qNNY4s" +
            "RG2+Yimxph2Z8MWWPFBebTIABPuRcVDquX7fL4+8FZJTH3JLwfT+slunAA4473mZ" +
            "9s2fAVu6CmQf1V09+fEbMGI9WWh53g19wg5WdlToOX4g5lh4QtGRpbWpEWaYrKzS" +
            "B5EWOUI7lroFtv6s9OpEO59VAkXWKUbT98T8TCYqiDH+nMy3k+GbVawxXeHYHQr+" +
            "XlbcChPaCwhMXspqKG49xaJmrOuRMoAWCBGUW8r2RDhQ+FP5V/sTRMqKmBv9gTe6" +
            "RJwoKPlDt+0aX9vaFjKpjPcCAwEAAaMhMB8wHQYDVR0OBBYEFGH0gyeHIz1+ONGI" +
            "PuGnAhrS3apoMA0GCSqGSIb3DQEBCwUAA4IBAQCVEakh1SLnZOz2IK0ISbAV5UBb" +
            "nerLNDl+X+YSYsCQM1SBcXDjlkSAeP3ErJEO3RW3wdRQjLRRHomwSCSRE84SUfSL" +
            "VPIbeR7jm4sS9x5rnlGF6iqhYh2MlZD/hFxdrGoYv8g/JN4FFFMXRmmaQ8ouYJwc" +
            "4ZoxRdCXszeI5Zp2+b14cs/nf4geYliHtcDr/w7fkvQ0hn+c1lTihbW0/eE32aUK" +
            "SULAmjx0sCDfDAQItP79CC7jCW0TFN0CMORw/+fzp/dnVboSZ2MgcuRIH1Ez+6/1" +
            "1QJD2SrkkaRSEaXI6fe9jgHVhnqK9V3y3WAuzEKjaKw6jV8BjkXAA4dQj1Re" +
            "-----END CERTIFICATE-----";

    public static final String WRONGLY_FORMATTED_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIFljCCA36gAwIBAgIJAN5zDsVzPq0aMA0GCSqGSIb3DQEBBQUAMIGsMQswCQYD      " +
            "VQQGEwJMSzELMAkGA1UECAwCV1AxDDAKBgNVBAcMA0NPTDEaMBgGA1UECgwRV1NP" +
            "MiAoVUspIExJTUlURUQxFDASBgNVBAsMC09wZW5CYW5raW5nMS4wLAYDVQQDDCVP" +
            "cGVuQmFua2luZyBQcmUtUHJvZHVjdGlvbiBJc3N1aW5nIENBMSAwHgYJKoZIhvcN" +
            "AQkBFhFtYWxzaGFuaUB3c28yLmNvbTAeFw0yMjAxMTgwNzI3NDJaFw0yNDAxMTgw" +
            "NzI3NDJaMHMxCzAJBgNVBAYTAkdCMRowGAYDVQQKDBFXU08yIChVSykgTElNSVRF" +
            "RDErMCkGA1UEYQwiUFNER0ItT0ItVW5rbm93bjAwMTU4MDAwMDFIUVFyWkFBWDEb" +
            "MBkGA1UEAwwSMDAxNTgwMDAwMUhRUXJaQUFYMIIBIjANBgkqhkiG9w0BAQEFAAOC" +
            "AQ8AMIIBCgKCAQEA59+TouW8sLFWk7MUht40v+DDglinjL2qmQ+wP3YNtvza/7Ue" +
            "KZ+gWw92jd0v99xZz7c5KOgtTgctAmIU1qjGLwzHzn/fl/ZrO4spGLIbU7RwGHA7" +
            "BSpB4k0vGdpCBigaaILHhBrAczDJ1BLYMS4lg69+6fYTeY2s0Khv92NWl8TXorAH" +
            "W0D8KrbZ3chWIynZamNu8KN6s+GL5jyu6pzJpXVNOXiUdRr4U9fLctw7qPw4RbBM" +
            "edXohmVFwMTQ7lMKax+wHOjfQDQW7KuZxRRYiUqB3hjyhrKlIpjjWtnxLclymTAI" +
            "TRMqFlH8KFq/rVBGQ8F3SnDp90E25RbSWdfNRwIDAQABo4HyMIHvMA4GA1UdDwEB" +
            "/wQEAwIHgDAdBgNVHSUEFjAUBggrBgEFBQcDAgYIKwYBBQUHAwEwHQYDVR0OBBYE" +
            "FNxNxhzaeU3VdIMlXkNiYbnjheOnMIGeBggrBgEFBQcBAwSBkTCBjjATBgYEAI5G" +
            "AQYwCQYHBACORgEGAzB3BgYEAIGYJwIwbTBGMEQGBwQAgZgnAQEMBlBTUF9BUwYH" +
            "BACBmCcBAgwGUFNQX1BJBgcEAIGYJwEDDAZQU1BfQUkGBwQAgZgnAQQMBlBTUF9J" +
            "QwwbRmluYW5jaWFsIENvbmR1Y3QgQXV0aG9yaXR5DAZHQi1GQ0EwDQYJKoZIhvcN" +
            "AQEFBQADggIBABBM63bCwANVRR44wFCZysbppYAT4mms3dUqoP3XCUXaO3+7zNWa" +
            "siZ90cje3fuiTD5SAyykm/I/mlgVx92ZbYFW0VG7IVkuC7Fid5iPywHX7Bm1xmEY" +
            "bL1AtAm4sBzE1Kw5dnB1L30do7sp9fuJCdom5/fhrh2GyLBd0iA62qQ+F9uALrC0" +
            "bub0KnGaEf9g1UltgxuqguoYoHb46ICJ03kMGZMC5BcjDDEbDQQ3kT+g9evaBUBm" +
            "3A3cNJURF7/07iLEfHNYrMxDLIw6aC4svbcx+IquO81xpTCefhTU4UFSLN1/DXWW" +
            "qrjCqkvHE53mb33QCXmnsooTP8pABG2q2+w5EC9yeX6Fln6M8VwZL5P2stELWXZE" +
            "876kCo0LkmoP3s6Z62bF4u9hJvM9mQRvmDVqN2Y7eLMty4qmGEmAYYiHOG+FXNKo" +
            "io9MXbB3B7tdeM4g2HlQGfRIrTrfAOu2cH1l1ZwHZgx7oCXN1nuZgE3r07kJx4Bn" +
            "DXCRpXoZq4pB3AlzcWEPh51/SS8Wsz52CNSDGoMB7HPkNnoDrYoibb1LFrOwJ3IM" +
            "VUKCSnt1QdnrKtMVMTd0iI4uk7kCKt7QFeiizN+oW6BI/MNm6mHEWd9CKWmrZT56" +
            "wU3ZM7vgwugq9tAs+oi8Lf3ZODuXAsiSpgcd6dceatoqeyB4E+6kp0Ge" +
            "-----END CERTIFICATE-----";

    public static void injectEnvironmentVariable(String key, String value)
            throws ReflectiveOperationException {

        Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");

        Field unmodifiableMapField = getAccessibleField(processEnvironment, "theUnmodifiableEnvironment");
        Object unmodifiableMap = unmodifiableMapField.get(null);
        injectIntoUnmodifiableMap(key, value, unmodifiableMap);

        Field mapField = getAccessibleField(processEnvironment, "theEnvironment");
        Map<String, String> map = (Map<String, String>) mapField.get(null);
        map.put(key, value);
    }

    private static Field getAccessibleField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public static synchronized X509Certificate getExpiredSelfCertificate()
            throws OpenBankingException {
        if (expiredSelfCertificate == null) {
            expiredSelfCertificate = CertificateUtils.parseCertificate(EXPIRED_SELF_CERT);
        }
        return expiredSelfCertificate;
    }

    private static void injectIntoUnmodifiableMap(String key, String value, Object map)
            throws ReflectiveOperationException {

        Class unmodifiableMap = Class.forName("java.util.Collections$UnmodifiableMap");
        Field field = getAccessibleField(unmodifiableMap, "m");
        Object obj = field.get(map);
        ((Map<String, String>) obj).put(key, value);
    }

    public static Optional<X509Certificate> parseTransportCert(String strTransportCert) throws CertificateException {

        // decoding pem formatted transport cert
        byte[] decodedTransportCert = Base64.getDecoder().decode(strTransportCert
                .replace(BEGIN_CERT, "").replace(END_CERT, ""));

        X509Certificate transportCert = (X509Certificate) CertificateFactory.getInstance(X509_CERT_INSTANCE_NAME)
                .generateCertificate(new ByteArrayInputStream(decodedTransportCert));

        return Optional.ofNullable(transportCert);
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
