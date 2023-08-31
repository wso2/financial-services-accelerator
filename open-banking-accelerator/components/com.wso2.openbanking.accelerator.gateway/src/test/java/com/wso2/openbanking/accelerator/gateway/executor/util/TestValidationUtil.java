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

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Base64;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

/**
 * Test for validation utils.
 */
public class TestValidationUtil {

    public static final String TEST_CLIENT_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIFODCCBCCgAwIBAgIEWcZEPjANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjEwOTA4MDUyODEyWhcNMjIxMDA4" +
            "MDU1ODEyWjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZ" +
            "BgNVBAsTEjAwMTU4MDAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWdTNaV2xmOVl0NDJk" +
            "eVpnSXZ6a3ZxYjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALqlz2yg" +
            "mP4yqmWfvSkus6LrSvB1kknauQAnU3MgL7Eg+ZrlGljtgL0PJ3gPR9kkRG4fts2v" +
            "sxbnrART4YTs/AVagSxahnXrVnj/GlFVbO+cWpMadnYLl+pe7k4n1IdtD7m3WIpV" +
            "Zlwwgj/LQSD+b57Te+MkpCRoKFIWQMW0Eh5M6Mftb1MIN5h3zR/QLmEuREUzPshB" +
            "3CIMHv9LX2St8mA6n5sH/gIJOQW7breP7N7QAsOjKhgOhy4vEWx+Ig7VjCH4EU7I" +
            "AIHKSYhLICTBPKF5c1yTp/gMCE086VyMDu7i52jNKz2VsVX13qNr/7t2wVKaoQ2Z" +
            "frUA3uq7HX0vEe8CAwEAAaOCAgQwggIAMA4GA1UdDwEB/wQEAwIHgDAgBgNVHSUB" +
            "Af8EFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwgeAGA1UdIASB2DCB1TCB0gYLKwYB" +
            "BAGodYEGAWQwgcIwKgYIKwYBBQUHAgEWHmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9w" +
            "b2xpY2llczCBkwYIKwYBBQUHAgIwgYYMgYNVc2Ugb2YgdGhpcyBDZXJ0aWZpY2F0" +
            "ZSBjb25zdGl0dXRlcyBhY2NlcHRhbmNlIG9mIHRoZSBPcGVuQmFua2luZyBSb290" +
            "IENBIENlcnRpZmljYXRpb24gUG9saWNpZXMgYW5kIENlcnRpZmljYXRlIFByYWN0" +
            "aWNlIFN0YXRlbWVudDBtBggrBgEFBQcBAQRhMF8wJgYIKwYBBQUHMAGGGmh0dHA6" +
            "Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDUGCCsGAQUFBzAChilodHRwOi8vb2IudHJ1" +
            "c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNydDA6BgNVHR8EMzAxMC+gLaArhilo" +
            "dHRwOi8vb2IudHJ1c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNybDAfBgNVHSME" +
            "GDAWgBRQc5HGIXLTd/T+ABIGgVx5eW4/UDAdBgNVHQ4EFgQUt/iQ/+ksD95pZUol" +
            "YF8R+2838bgwDQYJKoZIhvcNAQELBQADggEBAH7/dvG7jm6xN1G0nziOHN/GSdJt" +
            "6wxodmRr/nDGBiHjONS2qq6wSSaN/QfUfe5OPbICi6dDNDgJpk1ZJKWXpdBW3K0e" +
            "3mjOvEjMSC6V/iu8T6NT4PWF9IGc10I93z/NbVYFahjfLtuBzBKwr7DbASYawzVF" +
            "rUa7CGbzk+nUGoqoMV/0eF+UtjDx2NYoGov7WK07XDFxsJJOjq0lA7SB3/3BqttW" +
            "J+iX9CafGYP2v9hjjOz1y7Jbr66Kd9tBK9C0+5bHvO84VoupUl8iateeBiFPqd+p" +
            "gLzORyiwIa7lsLvx273Fz3iOvX2Ksg9I/qhWABZ4adm//G45+GDGKFebzLo=" +
            "-----END CERTIFICATE-----";

    public static final String TEST_CLIENT_CERT_ISSUER = "-----BEGIN CERTIFICATE-----" +
            "MIIDtzCCAp+gAwIBAgIQDOfg5RfYRv6P5WD8G/AwOTANBgkqhkiG9w0BAQUFADBl" +
            "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3" +
            "d3cuZGlnaWNlcnQuY29tMSQwIgYDVQQDExtEaWdpQ2VydCBBc3N1cmVkIElEIFJv" +
            "b3QgQ0EwHhcNMDYxMTEwMDAwMDAwWhcNMzExMTEwMDAwMDAwWjBlMQswCQYDVQQG" +
            "EwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3d3cuZGlnaWNl" +
            "cnQuY29tMSQwIgYDVQQDExtEaWdpQ2VydCBBc3N1cmVkIElEIFJvb3QgQ0EwggEi" +
            "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCtDhXO5EOAXLGH87dg+XESpa7c" +
            "JpSIqvTO9SA5KFhgDPiA2qkVlTJhPLWxKISKityfCgyDF3qPkKyK53lTXDGEKvYP" +
            "mDI2dsze3Tyoou9q+yHyUmHfnyDXH+Kx2f4YZNISW1/5WBg1vEfNoTb5a3/UsDg+" +
            "wRvDjDPZ2C8Y/igPs6eD1sNuRMBhNZYW/lmci3Zt1/GiSw0r/wty2p5g0I6QNcZ4" +
            "VYcgoc/lbQrISXwxmDNsIumH0DJaoroTghHtORedmTpyoeb6pNnVFzF1roV9Iq4/" +
            "AUaG9ih5yLHa5FcXxH4cDrC0kqZWs72yl+2qp/C3xag/lRbQ/6GW6whfGHdPAgMB" +
            "AAGjYzBhMA4GA1UdDwEB/wQEAwIBhjAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQW" +
            "BBRF66Kv9JLLgjEtUYunpyGd823IDzAfBgNVHSMEGDAWgBRF66Kv9JLLgjEtUYun" +
            "pyGd823IDzANBgkqhkiG9w0BAQUFAAOCAQEAog683+Lt8ONyc3pklL/3cmbYMuRC" +
            "dWKuh+vy1dneVrOfzM4UKLkNl2BcEkxY5NM9g0lFWJc1aRqoR+pWxnmrEthngYTf" +
            "fwk8lOa4JiwgvT2zKIn3X/8i4peEH+ll74fg38FnSbNd67IJKusm7Xi+fT8r87cm" +
            "NW1fiQG2SVufAQWbqz0lwcy2f8Lxb4bG+mRo64EtlOtCt/qMHt1i8b5QZ7dsvfPx" +
            "H2sMNgcWfzd8qVttevESRmCD1ycEvkvOl77DZypoEd+A5wwzZr8TDRRu838fYxAe" +
            "+o0bJW1sj6W3YQGx0qMmoRBxna3iw/nDmVG3KwcIzi7mULKn+gpFL6Lw8g==" +
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
    public static final String EIDAS_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIF2DCCBMCgAwIBAgIEWcYJGDANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjAxMjE1MDY1ODMxWhcNMjIwMTE1" +
            "MDcyODMxWjBzMQswCQYDVQQGEwJHQjEaMBgGA1UEChMRV1NPMiAoVUspIExJTUlU" +
            "RUQxKzApBgNVBGETIlBTREdCLU9CLVVua25vd24wMDE1ODAwMDAxSFFRclpBQVgx" +
            "GzAZBgNVBAMTEjAwMTU4MDAwMDFIUVFyWkFBWDCCASIwDQYJKoZIhvcNAQEBBQAD" +
            "ggEPADCCAQoCggEBAN4RybsCYch4OAzJz3bfVAsz04lcuGYz1DE21l6PKkrABU3k" +
            "AYWUw9YtLWDVfA4nemSd5vb9dNJJoY6bvLTBbWBpWqOmq+lzXB4WrGuF5v4BaE8U" +
            "OeuVoIxKg9sV2mHAOaflVX8cz0dZSAbf1h+lvRRzIlX4TgN2ApZACIdtcBZfooOj" +
            "1F070MM9gyLw2A3cOew4MXaaZZFHP0CzQWlRyftaw0mYrx7m2iUK+4d4zEgEjC05" +
            "kdEpkdTtXvuTla/ER9O7DSnx++qKoRcEkqloOF/Rz7uhRhGfQHy6JwrNrZOr9khS" +
            "90pEejBnr8Is9BLqaRwE6COAPq/C+w5ZQ4pd9oMCAwEAAaOCApIwggKOMA4GA1Ud" +
            "DwEB/wQEAwIHgDCBiwYIKwYBBQUHAQMEfzB9MBMGBgQAjkYBBjAJBgcEAI5GAQYD" +
            "MGYGBgQAgZgnAjBcMDUwMwYHBACBmCcBAgwGUFNQX1BJBgcEAIGYJwEDDAZQU1Bf" +
            "QUkGBwQAgZgnAQQMBlBTUF9JQwwbRmluYW5jaWFsIENvbmR1Y3QgQXV0aG9yaXR5" +
            "DAZHQi1GQ0EwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIHgBgNV" +
            "HSAEgdgwgdUwgdIGCysGAQQBqHWBBgFkMIHCMCoGCCsGAQUFBwIBFh5odHRwOi8v" +
            "b2IudHJ1c3Rpcy5jb20vcG9saWNpZXMwgZMGCCsGAQUFBwICMIGGDIGDVXNlIG9m" +
            "IHRoaXMgQ2VydGlmaWNhdGUgY29uc3RpdHV0ZXMgYWNjZXB0YW5jZSBvZiB0aGUg" +
            "T3BlbkJhbmtpbmcgUm9vdCBDQSBDZXJ0aWZpY2F0aW9uIFBvbGljaWVzIGFuZCBD" +
            "ZXJ0aWZpY2F0ZSBQcmFjdGljZSBTdGF0ZW1lbnQwbQYIKwYBBQUHAQEEYTBfMCYG" +
            "CCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rpcy5jb20vb2NzcDA1BggrBgEFBQcw" +
            "AoYpaHR0cDovL29iLnRydXN0aXMuY29tL29iX3BwX2lzc3VpbmdjYS5jcnQwOgYD" +
            "VR0fBDMwMTAvoC2gK4YpaHR0cDovL29iLnRydXN0aXMuY29tL29iX3BwX2lzc3Vp" +
            "bmdjYS5jcmwwHwYDVR0jBBgwFoAUUHORxiFy03f0/gASBoFceXluP1AwHQYDVR0O" +
            "BBYEFN0LLFBaqNtl17Ds7a+4EwedY69oMA0GCSqGSIb3DQEBCwUAA4IBAQBpyV93" +
            "NoWNDg8PhcTWrxQFRLSvNCaDfKQw7MVzK7pl9cFnugZPXUg67KmLiJ+GzI9HHym/" +
            "yfd3Vwx5SNtfQVACmStKsLGv6kRGJcUAIgICV8ZGVlbsWpKam2ck7wR2138QD8s1" +
            "igAIaSWzHyHlkPjy44hRDbLpEYhRf9c2bUYGYnkMUBhmhI3ZhbopR3Zac/1/VBlA" +
            "VR7G0VQiloTHoQUL6OkaTnfdOEjU9Eeo8lQgrGjob5aCWrrPe4ExCyAZdn0NgE69" +
            "womfyrqwLoQpiUGmOSZCuOgWmPe8OrbpGIaodZz2Wk5qgR5xrVkNDfvgM/nXm1r8" +
            "HxriBi5shkweEW6g" +
            "-----END CERTIFICATE-----";

    public static final String EIDAS_CERT_ISSUER = "-----BEGIN CERTIFICATE-----" +
            "MIIGEzCCA/ugAwIBAgIEWcT9RzANBgkqhkiG9w0BAQsFADBQMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxKzApBgNVBAMTIk9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIFJvb3QgQ0EwHhcNMTcwOTIyMTI0NjU3WhcNMjcwOTIyMTMx" +
            "NjU3WjBTMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNV" +
            "BAMTJU9wZW5CYW5raW5nIFByZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwggEiMA0G" +
            "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCyyrRg2jF01jXhX3IR44p338ZBozn8" +
            "WkZaCN8MB+AlBfuXHD6mC/0v+N/Z4XI6E5pzArmTho8D6a6JDpAHmmefqGSqOXVb" +
            "clYv1tHFjmC1FtKqkFHTTMyhl41nEMo0dnvWA45bMsGm0yMi/tEM5Vb5dSY4Zr/2" +
            "LWgUTDFUisgUbyIIHT+L6qxPUPCpNuEd+AWVc9K0SlmhaC+UIfVO83gE1+9ar2dO" +
            "NSFaK/a445Us6MnqgKvfkvKdaR06Ok/EhGgiAZORcyZ61EYFVVzJewy5NrFSF3mw" +
            "iPYvMxoT5bxcwAEvxqBXpTDv8njQfR+cgZDeloeK1UqmW/DpR+jj3KNHAgMBAAGj" +
            "ggHwMIIB7DAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIBADCB4AYD" +
            "VR0gBIHYMIHVMIHSBgsrBgEEAah1gQYBZDCBwjAqBggrBgEFBQcCARYeaHR0cDov" +
            "L29iLnRydXN0aXMuY29tL3BvbGljaWVzMIGTBggrBgEFBQcCAjCBhgyBg1VzZSBv" +
            "ZiB0aGlzIENlcnRpZmljYXRlIGNvbnN0aXR1dGVzIGFjY2VwdGFuY2Ugb2YgdGhl" +
            "IE9wZW5CYW5raW5nIFJvb3QgQ0EgQ2VydGlmaWNhdGlvbiBQb2xpY2llcyBhbmQg" +
            "Q2VydGlmaWNhdGUgUHJhY3RpY2UgU3RhdGVtZW50MGoGCCsGAQUFBwEBBF4wXDAy" +
            "BggrBgEFBQcwAoYmaHR0cDovL29iLnRydXN0aXMuY29tL29idGVzdHJvb3RjYS5j" +
            "cnQwJgYIKwYBBQUHMAGGGmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDcGA1Ud" +
            "HwQwMC4wLKAqoCiGJmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vYl9wcF9yb290Y2Eu" +
            "Y3JsMB8GA1UdIwQYMBaAFOw4jgva8/k3PpDefV9q5mDNeUKDMB0GA1UdDgQWBBRQ" +
            "c5HGIXLTd/T+ABIGgVx5eW4/UDANBgkqhkiG9w0BAQsFAAOCAgEAdRg2H9uLwzlG" +
            "qvHGjIz0ydM1tElujEcWJp5MeiorikK0rMOlxVU6ZFBlXPfO1APu0cZXxfHwWs91" +
            "zoNCpGXebC6tiDFQ3+mI4qywtippjBqb6Sft37NlkXDzQETomsY7wETuUJ31xFA0" +
            "FccI8WlAUzUOBE8OAGo5kAZ4FTa/nkd8c2wmuwSp+9/s+gQe0K9BkxywoP1WAEdU" +
            "AaKW3RE9yuTbHA/ZF/zz4/Rpw/FB/hYhOxvDV6qInl5B7ErSH4r4v4D2jiE6apAc" +
            "n5LT+e0aBa/EgGAxgyAgrYpw1s+TCUJot+227xRvXxeeZzXa2igsd+C845BGiSlt" +
            "hzr0mqYDYEWJMfApZ+BlMtxa7K9T3D2l6XMv12RoNnEWe6H5xazTvBLiTibW3c5i" +
            "j8WWKJNtQbgmooRPaKJIl+0rm54MFH0FDxJ+P4mAR6qa8JS911nS26iCsE9FQVK5" +
            "1djuct349FYBOVM595/GkkTz9k1vXw1BdD71lNjI00Yjf73AAtvL/X4CpRz92Nag" +
            "shS2Ia5a3qjjFrjx7z4h7QtMJGjuUsjTI/c+yjIYwAZ5gelF5gz7l2dn3g6B40pu" +
            "7y1EewlfIQh/HVMF0ZpF29XL6+7siYQCGhP5cNJ04fotzqDPaT2XlOhE3yNkjp82" +
            "uzCWvhLUJgE3D9V9PL0XD/ykNEP0Fio=" +
            "-----END CERTIFICATE-----";

    public static final String REQUEST_BODY_WITH_SSA = "eyJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1NiIsImtpZCI6IkR3TUtk" +
            "V01tajdQV2ludm9xZlF5WFZ6eVo2USJ9.eyJpc3MiOiI5YjV1c0RwYk50bXhEY1R6czdHektwIiwiaWF0IjoxNjAxOTgy" +
            "MDQyLCJleHAiOjE2MDcyNTI0NDIsImp0aSI6IjE2MDE5ODIwNDYiLCJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo4MjQzL" +
            "3Rva2VuIiwic2NvcGUiOiJhY2NvdW50cyBwYXltZW50cyIsInRva2VuX2VuZHBvaW50X2F1dGhfbWV0aG9kIjoicHJpdm" +
            "F0ZV9rZXlfand0IiwiZ3JhbnRfdHlwZXMiOlsiYXV0aG9yaXphdGlvbl9jb2RlIiwicmVmcmVzaF90b2tlbiJdLCJyZXN" +
            "wb25zZV90eXBlcyI6WyJjb2RlIGlkX3Rva2VuIl0sImlkX3Rva2VuX3NpZ25lZF9yZXNwb25zZV9hbGciOiJQUzI1NiIs" +
            "InJlcXVlc3Rfb2JqZWN0X3NpZ25pbmdfYWxnIjoiUFMyNTYiLCJzb2Z0d2FyZV9pZCI6IjliNXVzRHBiTnRteERjVHpzN" +
            "0d6S3AiLCJhcHBsaWNhdGlvbl90eXBlIjoid2ViIiwicmVkaXJlY3RfdXJpcyI6WyJodHRwczovL3dzbzIuY29tIl0sIn" +
            "Rva2VuX2VuZHBvaW50X2F1dGhfc2lnbmluZ19hbGciOiJQUzI1NiIsInNvZnR3YXJlX3N0YXRlbWVudCI6ImV5SmhiR2N" +
            "pT2lKUVV6STFOaUlzSW10cFpDSTZJa2g2WVRsMk5XSm5SRXBqVDI1b1kxVmFOMEpOZDJKVFRGODBUbFl3WjFOR2RrbHFZ" +
            "Vk5ZWkVNdE1XTTlJaXdpZEhsd0lqb2lTbGRVSW4wLmV5SnBjM01pT2lKUGNHVnVRbUZ1YTJsdVp5Qk1kR1FpTENKcFlYU" +
            "WlPakUxT1RJek5qUTFOamdzSW1wMGFTSTZJak5rTVdJek5UazFaV1poWXpSbE16WWlMQ0p6YjJaMGQyRnlaVjlsYm5acG" +
            "NtOXViV1Z1ZENJNkluTmhibVJpYjNnaUxDSnpiMlowZDJGeVpWOXRiMlJsSWpvaVZHVnpkQ0lzSW5OdlpuUjNZWEpsWDJ" +
            "sa0lqb2lPV0kxZFhORWNHSk9kRzE0UkdOVWVuTTNSM3BMY0NJc0luTnZablIzWVhKbFgyTnNhV1Z1ZEY5cFpDSTZJamxp" +
            "TlhWelJIQmlUblJ0ZUVSalZIcHpOMGQ2UzNBaUxDSnpiMlowZDJGeVpWOWpiR2xsYm5SZmJtRnRaU0k2SWxkVFR6SWdUM" +
            "0JsYmlCQ1lXNXJhVzVuSUZSUVVDQW9VMkZ1WkdKdmVDa2lMQ0p6YjJaMGQyRnlaVjlqYkdsbGJuUmZaR1Z6WTNKcGNIUn" +
            "BiMjRpT2lKVWFHbHpJRlJRVUNCSmN5QmpjbVZoZEdWa0lHWnZjaUIwWlhOMGFXNW5JSEIxY25CdmMyVnpMaUFpTENKemI" +
            "yWjBkMkZ5WlY5MlpYSnphVzl1SWpveExqVXNJbk52Wm5SM1lYSmxYMk5zYVdWdWRGOTFjbWtpT2lKb2RIUndjem92TDNk" +
            "emJ6SXVZMjl0SWl3aWMyOW1kSGRoY21WZmNtVmthWEpsWTNSZmRYSnBjeUk2V3lKb2RIUndjem92TDNkemJ6SXVZMjl0S" +
            "Wwwc0luTnZablIzWVhKbFgzSnZiR1Z6SWpwYklrRkpVMUFpTENKUVNWTlFJbDBzSW05eVoyRnVhWE5oZEdsdmJsOWpiMj" +
            "F3WlhSbGJuUmZZWFYwYUc5eWFYUjVYMk5zWVdsdGN5STZleUpoZFhSb2IzSnBkSGxmYVdRaU9pSlBRa2RDVWlJc0luSmx" +
            "aMmx6ZEhKaGRHbHZibDlwWkNJNklsVnVhMjV2ZDI0d01ERTFPREF3TURBeFNGRlJjbHBCUVZnaUxDSnpkR0YwZFhNaU9p" +
            "SkJZM1JwZG1VaUxDSmhkWFJvYjNKcGMyRjBhVzl1Y3lJNlczc2liV1Z0WW1WeVgzTjBZWFJsSWpvaVIwSWlMQ0p5YjJ4b" +
            "GN5STZXeUpCU1ZOUUlpd2lVRWxUVUNKZGZTeDdJbTFsYldKbGNsOXpkR0YwWlNJNklrbEZJaXdpY205c1pYTWlPbHNpUV" +
            "VsVFVDSXNJbEJKVTFBaVhYMHNleUp0WlcxaVpYSmZjM1JoZEdVaU9pSk9UQ0lzSW5KdmJHVnpJanBiSWtGSlUxQWlMQ0p" +
            "RU1ZOUUlsMTlYWDBzSW5OdlpuUjNZWEpsWDJ4dloyOWZkWEpwSWpvaWFIUjBjSE02THk5M2MyOHlMbU52YlM5M2MyOHlM" +
            "bXB3WnlJc0ltOXlaMTl6ZEdGMGRYTWlPaUpCWTNScGRtVWlMQ0p2Y21kZmFXUWlPaUl3TURFMU9EQXdNREF4U0ZGUmNsc" +
            "EJRVmdpTENKdmNtZGZibUZ0WlNJNklsZFRUeklnS0ZWTEtTQk1TVTFKVkVWRUlpd2liM0puWDJOdmJuUmhZM1J6SWpwYm" +
            "V5SnVZVzFsSWpvaVZHVmphRzVwWTJGc0lpd2laVzFoYVd3aU9pSnpZV05vYVc1cGMwQjNjMjh5TG1OdmJTSXNJbkJvYjI" +
            "1bElqb2lLemswTnpjME1qYzBNemMwSWl3aWRIbHdaU0k2SWxSbFkyaHVhV05oYkNKOUxIc2libUZ0WlNJNklrSjFjMmx1" +
            "WlhOeklpd2laVzFoYVd3aU9pSnpZV05vYVc1cGMwQjNjMjh5TG1OdmJTSXNJbkJvYjI1bElqb2lLemswTnpjME1qYzBNe" +
            "mMwSWl3aWRIbHdaU0k2SWtKMWMybHVaWE56SW4xZExDSnZjbWRmYW5kcmMxOWxibVJ3YjJsdWRDSTZJbWgwZEhCek9pOH" +
            "ZhMlY1YzNSdmNtVXViM0JsYm1KaGJtdHBibWQwWlhOMExtOXlaeTUxYXk4d01ERTFPREF3TURBeFNGRlJjbHBCUVZndk1" +
            "EQXhOVGd3TURBd01VaFJVWEphUVVGWUxtcDNhM01pTENKdmNtZGZhbmRyYzE5eVpYWnZhMlZrWDJWdVpIQnZhVzUwSWpv" +
            "aWFIUjBjSE02THk5clpYbHpkRzl5WlM1dmNHVnVZbUZ1YTJsdVozUmxjM1F1YjNKbkxuVnJMekF3TVRVNE1EQXdNREZJV" +
            "VZGeVdrRkJXQzl5WlhadmEyVmtMekF3TVRVNE1EQXdNREZJVVZGeVdrRkJXQzVxZDJ0eklpd2ljMjltZEhkaGNtVmZhbm" +
            "RyYzE5bGJtUndiMmx1ZENJNkltaDBkSEJ6T2k4dmEyVjVjM1J2Y21VdWIzQmxibUpoYm10cGJtZDBaWE4wTG05eVp5NTF" +
            "heTh3TURFMU9EQXdNREF4U0ZGUmNscEJRVmd2T1dJMWRYTkVjR0pPZEcxNFJHTlVlbk0zUjNwTGNDNXFkMnR6SWl3aWMy" +
            "OW1kSGRoY21WZmFuZHJjMTl5WlhadmEyVmtYMlZ1WkhCdmFXNTBJam9pYUhSMGNITTZMeTlyWlhsemRHOXlaUzV2Y0dWd" +
            "VltRnVhMmx1WjNSbGMzUXViM0puTG5Wckx6QXdNVFU0TURBd01ERklVVkZ5V2tGQldDOXlaWFp2YTJWa0x6bGlOWFZ6Uk" +
            "hCaVRuUnRlRVJqVkhwek4wZDZTM0F1YW5kcmN5SXNJbk52Wm5SM1lYSmxYM0J2YkdsamVWOTFjbWtpT2lKb2RIUndjem9" +
            "2TDNkemJ6SXVZMjl0SWl3aWMyOW1kSGRoY21WZmRHOXpYM1Z5YVNJNkltaDBkSEJ6T2k4dmQzTnZNaTVqYjIwaUxDSnpi" +
            "MlowZDJGeVpWOXZibDlpWldoaGJHWmZiMlpmYjNKbklqb2lWMU5QTWlCUGNHVnVJRUpoYm10cGJtY2lmUS5DQTE0b2dkY" +
            "3BOd29IaUlKb3o2bVR4TnBNMndScnFpWkFjYm1LMFJuRHgyR0ROM0JIWW5aRzBFcTZWZ3lQYlByY1J5ZldsOGpRczJFU3" +
            "NXYzVKU0J3ZWpIYnZwbng3a1ZCeVlrRzQ0ZGhvemFQQU5FWmx0Tmo0TTkxMkNnSGVLUGRfZDB1SUQ4ZElVcThfczJrWU1" +
            "zb0NjY0JxR3lGVEl5bVZLMDFIWF9YXy1UN25wR19vdkU4Q0xnaWxNRmtpank1UGlGQzgzaG9weGl4ZVFmUmdkbUhDUl8x" +
            "Ym9rc2JGREszUlBJRWU1UGlPRHZYOHZsV0I4aVVHeTdQR3paMGlrWEJEMGx4OXAxQUpFeVlGM3gxcENqc1NIOHRKQzVFN" +
            "UNHMHhaTFFQUGtUM0FfU3BqaVVoNUVsTmROY21UUG93MkxWU3hQOVF1c040dldwRU1VTmQ5cHcifQ.kq8UsDUcb6Ee55w" +
            "4U4JhiifyUB0sSiTAnobLV1bwujfS2msdUfxDHqVjyrvx4NvPd54sXg3_k1EIRHLT4vT-zUkojqtWiB_v2ndo5UqvPUrI" +
            "FoqY0IQznKBfD6cLlGQ0laYqxm_GJWAEdEv_O8Ggw_z1DMiZZRHF9Oln9zZtT95JcGeJ8JCQVDkaX_AM-fZrVaixfD4iB" +
            "fy-n4H6LHCy94c1DrCM9wEGr7XfHLAVNdZe2Qbyjf1sVEPukK_ccw4AYcWUo3UJQ2WIKxZL4fBmb_3Z0ez9k31k6in86H" +
            "g4tHO9itXSVJvvzn8oAaYXXQrxfk4N1CojV3zk1bkhy6In3Q";

    public static final String REQUEST_BODY_WITH_SSA_SINGLE_ROLE = "eyJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1NiIsIm" +
            "tpZCI6IkR3TUtkV01tajdQV2ludm9xZlF5WFZ6eVo2USJ9.eyJpc3MiOiI5YjV1c0RwYk50bXhEY1R6czdHektwIiwiaW" +
            "F0IjoxNjAxOTgyMDQyLCJleHAiOjE2MDcyNTI0NDIsImp0aSI6IjE2MDE5ODIwNDYiLCJhdWQiOiJodHRwczovL2xvY2F" +
            "saG9zdDo4MjQzL3Rva2VuIiwic2NvcGUiOiJhY2NvdW50cyBwYXltZW50cyIsInRva2VuX2VuZHBvaW50X2F1dGhfbWV0" +
            "aG9kIjoicHJpdmF0ZV9rZXlfand0IiwiZ3JhbnRfdHlwZXMiOlsiYXV0aG9yaXphdGlvbl9jb2RlIiwicmVmcmVzaF90b" +
            "2tlbiJdLCJyZXNwb25zZV90eXBlcyI6WyJjb2RlIGlkX3Rva2VuIl0sImlkX3Rva2VuX3NpZ25lZF9yZXNwb25zZV9hbG" +
            "ciOiJQUzI1NiIsInJlcXVlc3Rfb2JqZWN0X3NpZ25pbmdfYWxnIjoiUFMyNTYiLCJzb2Z0d2FyZV9pZCI6IjliNXVzRHB" +
            "iTnRteERjVHpzN0d6S3AiLCJhcHBsaWNhdGlvbl90eXBlIjoid2ViIiwicmVkaXJlY3RfdXJpcyI6WyJodHRwczovL3dz" +
            "bzIuY29tIl0sInRva2VuX2VuZHBvaW50X2F1dGhfc2lnbmluZ19hbGciOiJQUzI1NiIsInNvZnR3YXJlX3N0YXRlbWVud" +
            "CI6ImV5SmhiR2NpT2lKUVV6STFOaUlzSW10cFpDSTZJa2g2WVRsMk5XSm5SRXBqVDI1b1kxVmFOMEpOZDJKVFRGODBUbF" +
            "l3WjFOR2RrbHFZVk5ZWkVNdE1XTTlJaXdpZEhsd0lqb2lTbGRVSW4wLmV5SnBjM01pT2lKUGNHVnVRbUZ1YTJsdVp5Qk1" +
            "kR1FpTENKcFlYUWlPakUxT1RJek5qUTFOamdzSW1wMGFTSTZJak5rTVdJek5UazFaV1poWXpSbE16WWlMQ0p6YjJaMGQy" +
            "RnlaVjlsYm5acGNtOXViV1Z1ZENJNkluTmhibVJpYjNnaUxDSnpiMlowZDJGeVpWOXRiMlJsSWpvaVZHVnpkQ0lzSW5Od" +
            "lpuUjNZWEpsWDJsa0lqb2lPV0kxZFhORWNHSk9kRzE0UkdOVWVuTTNSM3BMY0NJc0luTnZablIzWVhKbFgyTnNhV1Z1ZE" +
            "Y5cFpDSTZJamxpTlhWelJIQmlUblJ0ZUVSalZIcHpOMGQ2UzNBaUxDSnpiMlowZDJGeVpWOWpiR2xsYm5SZmJtRnRaU0k" +
            "2SWxkVFR6SWdUM0JsYmlCQ1lXNXJhVzVuSUZSUVVDQW9VMkZ1WkdKdmVDa2lMQ0p6YjJaMGQyRnlaVjlqYkdsbGJuUmZa" +
            "R1Z6WTNKcGNIUnBiMjRpT2lKVWFHbHpJRlJRVUNCSmN5QmpjbVZoZEdWa0lHWnZjaUIwWlhOMGFXNW5JSEIxY25CdmMyV" +
            "npMaUFpTENKemIyWjBkMkZ5WlY5MlpYSnphVzl1SWpveExqVXNJbk52Wm5SM1lYSmxYMk5zYVdWdWRGOTFjbWtpT2lKb2" +
            "RIUndjem92TDNkemJ6SXVZMjl0SWl3aWMyOW1kSGRoY21WZmNtVmthWEpsWTNSZmRYSnBjeUk2V3lKb2RIUndjem92TDN" +
            "kemJ6SXVZMjl0SWwwc0luTnZablIzWVhKbFgzSnZiR1Z6SWpvaVFVbFRVQ0lzSW05eVoyRnVhWE5oZEdsdmJsOWpiMjF3" +
            "WlhSbGJuUmZZWFYwYUc5eWFYUjVYMk5zWVdsdGN5STZleUpoZFhSb2IzSnBkSGxmYVdRaU9pSlBRa2RDVWlJc0luSmxaM" +
            "mx6ZEhKaGRHbHZibDlwWkNJNklsVnVhMjV2ZDI0d01ERTFPREF3TURBeFNGRlJjbHBCUVZnaUxDSnpkR0YwZFhNaU9pSk" +
            "JZM1JwZG1VaUxDSmhkWFJvYjNKcGMyRjBhVzl1Y3lJNlczc2liV1Z0WW1WeVgzTjBZWFJsSWpvaVIwSWlMQ0p5YjJ4bGN" +
            "5STZXeUpCU1ZOUUlpd2lVRWxUVUNKZGZTeDdJbTFsYldKbGNsOXpkR0YwWlNJNklrbEZJaXdpY205c1pYTWlPbHNpUVVs" +
            "VFVDSXNJbEJKVTFBaVhYMHNleUp0WlcxaVpYSmZjM1JoZEdVaU9pSk9UQ0lzSW5KdmJHVnpJanBiSWtGSlUxQWlMQ0pRU" +
            "1ZOUUlsMTlYWDBzSW5OdlpuUjNZWEpsWDJ4dloyOWZkWEpwSWpvaWFIUjBjSE02THk5M2MyOHlMbU52YlM5M2MyOHlMbX" +
            "B3WnlJc0ltOXlaMTl6ZEdGMGRYTWlPaUpCWTNScGRtVWlMQ0p2Y21kZmFXUWlPaUl3TURFMU9EQXdNREF4U0ZGUmNscEJ" +
            "RVmdpTENKdmNtZGZibUZ0WlNJNklsZFRUeklnS0ZWTEtTQk1TVTFKVkVWRUlpd2liM0puWDJOdmJuUmhZM1J6SWpwYmV5" +
            "SnVZVzFsSWpvaVZHVmphRzVwWTJGc0lpd2laVzFoYVd3aU9pSnpZV05vYVc1cGMwQjNjMjh5TG1OdmJTSXNJbkJvYjI1b" +
            "Elqb2lLemswTnpjME1qYzBNemMwSWl3aWRIbHdaU0k2SWxSbFkyaHVhV05oYkNKOUxIc2libUZ0WlNJNklrSjFjMmx1Wl" +
            "hOeklpd2laVzFoYVd3aU9pSnpZV05vYVc1cGMwQjNjMjh5TG1OdmJTSXNJbkJvYjI1bElqb2lLemswTnpjME1qYzBNemM" +
            "wSWl3aWRIbHdaU0k2SWtKMWMybHVaWE56SW4xZExDSnZjbWRmYW5kcmMxOWxibVJ3YjJsdWRDSTZJbWgwZEhCek9pOHZh" +
            "MlY1YzNSdmNtVXViM0JsYm1KaGJtdHBibWQwWlhOMExtOXlaeTUxYXk4d01ERTFPREF3TURBeFNGRlJjbHBCUVZndk1EQ" +
            "XhOVGd3TURBd01VaFJVWEphUVVGWUxtcDNhM01pTENKdmNtZGZhbmRyYzE5eVpYWnZhMlZrWDJWdVpIQnZhVzUwSWpvaW" +
            "FIUjBjSE02THk5clpYbHpkRzl5WlM1dmNHVnVZbUZ1YTJsdVozUmxjM1F1YjNKbkxuVnJMekF3TVRVNE1EQXdNREZJVVZ" +
            "GeVdrRkJXQzl5WlhadmEyVmtMekF3TVRVNE1EQXdNREZJVVZGeVdrRkJXQzVxZDJ0eklpd2ljMjltZEhkaGNtVmZhbmRy" +
            "YzE5bGJtUndiMmx1ZENJNkltaDBkSEJ6T2k4dmEyVjVjM1J2Y21VdWIzQmxibUpoYm10cGJtZDBaWE4wTG05eVp5NTFhe" +
            "Th3TURFMU9EQXdNREF4U0ZGUmNscEJRVmd2T1dJMWRYTkVjR0pPZEcxNFJHTlVlbk0zUjNwTGNDNXFkMnR6SWl3aWMyOW" +
            "1kSGRoY21WZmFuZHJjMTl5WlhadmEyVmtYMlZ1WkhCdmFXNTBJam9pYUhSMGNITTZMeTlyWlhsemRHOXlaUzV2Y0dWdVl" +
            "tRnVhMmx1WjNSbGMzUXViM0puTG5Wckx6QXdNVFU0TURBd01ERklVVkZ5V2tGQldDOXlaWFp2YTJWa0x6bGlOWFZ6UkhC" +
            "aVRuUnRlRVJqVkhwek4wZDZTM0F1YW5kcmN5SXNJbk52Wm5SM1lYSmxYM0J2YkdsamVWOTFjbWtpT2lKb2RIUndjem92T" +
            "DNkemJ6SXVZMjl0SWl3aWMyOW1kSGRoY21WZmRHOXpYM1Z5YVNJNkltaDBkSEJ6T2k4dmQzTnZNaTVqYjIwaUxDSnpiMl" +
            "owZDJGeVpWOXZibDlpWldoaGJHWmZiMlpmYjNKbklqb2lWMU5QTWlCUGNHVnVJRUpoYm10cGJtY2lmUS5vc1RWRlBqa1h" +
            "CNnh3SDNJVE04OTVpRXNOamgyMnJjR1pSdnFQOWh6cV9TX2l6UENQNTJLQ3U0ZWJkZ1o5WnpXTmdBVkp4X3hKY2dZajhs" +
            "TXV6S3VWa3RFX1M0bnl2REhsSU1sblBvRGd0UlNZcElDM05LcDVWY3QyaVd0bzhkNVpWVXZJQUhDTXpXZjllVDdGSURZQ" +
            "2syUEhhWHNmblNLSVM3NWE4WEJXbUo1ckgzT0xoZ0RudFhHalduNW0wdVdYUDhmOWZ0TmNvSUdMa050bEp0cXV3S2dYcF" +
            "JULWJIQVNSWUE4c0lsMnFETTUtay00TEY3S1hhZDBWR2ZfcmJxclVjWnlMbEt6bldaRmM4X1Q3bHZEbnEwQVJIYWtLaUU" +
            "yZGhkUlRHTHhuX21yR2lKRXg0dVFsMTlrNWF0T0FzRlJ0X0liMjdCM1lLS3V6VGxOcDRjQ2cifQ.Z0J-sLdpWmvk5MEOE" +
            "We-MwO_-pve2zo0OA-7JfG5cFrsQ1WPuvb-mjjxqSjxtER3IRONRWULmTIDnuu9FCX1oQ4e_0HAmh3AQMa2sp9n8fMwHc" +
            "RxmXQcOuAfYajHV5i318xcSveOrapD6jFiqLgLiLtTK3-tJB7wDg-sdxuwSI9HSldR3sBjQDTPLOLFGEJ5jJ4bl8JkKQ6" +
            "zHcFkYyxMpJw7zT13hyiWvR--0WVgP8g4yYeCobPXUNmBEI7zZMuYVlO5C6l-RKFPstaasqX81zWG3ES8TVZM8rMFKqN_" +
            "QoctY4HMshtk58h2W4NP4UJZyYgsGNNb2hVI5IffCVxqIw";

    private static final Log log = LogFactory.getLog(TestValidationUtil.class);
    private static java.security.cert.X509Certificate testClientCertificate = null;
    private static java.security.cert.X509Certificate testClientCertificateIssuer = null;
    private static java.security.cert.X509Certificate testEidasCertificate = null;
    private static java.security.cert.X509Certificate testEidasCertificateIssuer = null;
    private static java.security.cert.X509Certificate expiredSelfCertificate = null;

    protected static X509Certificate getCertFromStr(String pemEncodedCert) {
        byte[] decodedTransportCert = Base64.getDecoder().decode(pemEncodedCert
                .replace(CertificateValidationUtils.BEGIN_CERT, "")
                .replace(CertificateValidationUtils.END_CERT, ""));

        InputStream inputStream = new ByteArrayInputStream(decodedTransportCert);
        X509Certificate x509Certificate = null;
        try {
            x509Certificate = X509Certificate.getInstance(inputStream);
        } catch (CertificateException e) {
            log.error("Exception occured while parsing test certificate. Caused by, ", e);
        }
        return x509Certificate;
    }

    public static synchronized java.security.cert.X509Certificate getTestClientCertificate()
            throws OpenBankingException {
        if (testClientCertificate == null) {
            testClientCertificate = CertificateUtils.parseCertificate(TEST_CLIENT_CERT);
        }
        return testClientCertificate;
    }

    public static synchronized java.security.cert.X509Certificate getTestClientCertificateIssuer()
            throws OpenBankingException {
        if (testClientCertificateIssuer == null) {
            testClientCertificateIssuer = CertificateUtils.parseCertificate(TEST_CLIENT_CERT_ISSUER);
        }
        return testClientCertificateIssuer;
    }

    public static synchronized java.security.cert.X509Certificate getTestEidasCertificate()
            throws OpenBankingException {
        if (testEidasCertificate == null) {
            testEidasCertificate = CertificateUtils.parseCertificate(EIDAS_CERT);
        }
        return testEidasCertificate;
    }

    public static synchronized java.security.cert.X509Certificate getTestEidasCertificateIssuer()
            throws OpenBankingException {
        if (testEidasCertificateIssuer == null) {
            testEidasCertificateIssuer = CertificateUtils.parseCertificate(EIDAS_CERT_ISSUER);
        }
        return testEidasCertificateIssuer;
    }

    public static synchronized java.security.cert.X509Certificate getExpiredSelfCertificate()
            throws OpenBankingException {
        if (expiredSelfCertificate == null) {
            expiredSelfCertificate = CertificateUtils.parseCertificate(EXPIRED_SELF_CERT);
        }
        return expiredSelfCertificate;
    }

    public static Certificate getEmptyTestCertificate() {

        return new Certificate("X.509") {
            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }

            @Override
            public void verify(PublicKey key) {

            }

            @Override
            public void verify(PublicKey key, String sigProvider) {

            }

            @Override
            public String toString() {
                return null;
            }

            @Override
            public PublicKey getPublicKey() {
                return null;
            }
        };
    }
}

