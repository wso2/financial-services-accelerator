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
            "MIIF0zCCBLugAwIBAgIEWccIBzANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjQwNTI5MDMzMzU4WhcNMjUwNjI5" +
            "MDQwMzU4WjBzMQswCQYDVQQGEwJHQjEaMBgGA1UEChMRV1NPMiAoVUspIExJTUlU" +
            "RUQxKzApBgNVBGETIlBTREdCLU9CLVVua25vd24wMDE1ODAwMDAxSFFRclpBQVgx" +
            "GzAZBgNVBAMTEjAwMTU4MDAwMDFIUVFyWkFBWDCCASIwDQYJKoZIhvcNAQEBBQAD" +
            "ggEPADCCAQoCggEBAMzQLqrJcerlPQxUlG2u2X1YDeAcLLb+agetjHKmoyF3kqn+" +
            "qMg1ZWt+8iCaYB6uF2Kd2DPF9Z6HZu1i157nH6lcVydEDfrML+LtXNYVd5UDTBwT" +
            "DW+kPZs+Hkb4AX5Gjtw/B/XXqi1caGynZbGSlkF1fX+4O7fXuPI+n4e91PXOcIXZ" +
            "N06NkMCjwggDchaj43vYxHujbos4TiwlBq3cfTetGPLCJV5ShRZfVMiyhUuvP1Vi" +
            "ORGtzI01C52dngpZGVxxXEZXdPBVSiKXcyjvnWBLxRP74sDAWhY3MFzUXAQzyl3a" +
            "OVoQnVFeMcvTK/b9GIa7oWOAvjBOZuxvEKsRl0ECAwEAAaOCAo0wggKJMA4GA1Ud" +
            "DwEB/wQEAwIGwDCBkQYIKwYBBQUHAQMEgYQwgYEwEwYGBACORgEGMAkGBwQAjkYB" +
            "BgIwagYGBACBmCcCMGAwOTARBgcEAIGYJwECDAZQU1BfUEkwEQYHBACBmCcBAwwG" +
            "UFNQX0FJMBEGBwQAgZgnAQQMBlBTUF9JQwwbRmluYW5jaWFsIENvbmR1Y3QgQXV0" +
            "aG9yaXR5DAZHQi1GQ0EwFQYDVR0lBA4wDAYKKwYBBAGCNwoDDDCB4AYDVR0gBIHY" +
            "MIHVMIHSBgsrBgEEAah1gQYBZDCBwjAqBggrBgEFBQcCARYeaHR0cDovL29iLnRy" +
            "dXN0aXMuY29tL3BvbGljaWVzMIGTBggrBgEFBQcCAjCBhgyBg1VzZSBvZiB0aGlz" +
            "IENlcnRpZmljYXRlIGNvbnN0aXR1dGVzIGFjY2VwdGFuY2Ugb2YgdGhlIE9wZW5C" +
            "YW5raW5nIFJvb3QgQ0EgQ2VydGlmaWNhdGlvbiBQb2xpY2llcyBhbmQgQ2VydGlm" +
            "aWNhdGUgUHJhY3RpY2UgU3RhdGVtZW50MG0GCCsGAQUFBwEBBGEwXzAmBggrBgEF" +
            "BQcwAYYaaHR0cDovL29iLnRydXN0aXMuY29tL29jc3AwNQYIKwYBBQUHMAKGKWh0" +
            "dHA6Ly9vYi50cnVzdGlzLmNvbS9vYl9wcF9pc3N1aW5nY2EuY3J0MDoGA1UdHwQz" +
            "MDEwL6AtoCuGKWh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vYl9wcF9pc3N1aW5nY2Eu" +
            "Y3JsMB8GA1UdIwQYMBaAFFBzkcYhctN39P4AEgaBXHl5bj9QMB0GA1UdDgQWBBTB" +
            "Ty2SFwZ0fgzOSSWWBJdEgGHy3TANBgkqhkiG9w0BAQsFAAOCAQEAHaXaR4IKqekW" +
            "FCRQjhZTkpoyi2mkhJM15Xd0t+cxSekWENoYBH7ox7M7Akfoa+oEr58n0DAa/+Bl" +
            "JiFZ2mYScdvLzV81cQU1+LPHhxagWZW2fGV91eBRqdb1j80anF9LN4LJoLDoAObz" +
            "zturt8MsSzw6T/iLneWKqeil85+L6M/LnDWJTY+dq8Co/qAqk239OWiduY4pnEJ/" +
            "U7PCH8xz/gSPQQO7PgILAZjGrdo40S/PMzZmGmGBMwLubE8exU8x/Wzf3uUnSfDi" +
            "PuAsRcplfXbHUb+NXK6wEJINjFw69/VZD3c/La62eTNPobl/DfqXGjPMjORtQqOi" +
            "BVnF+AIWWA==" +
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
