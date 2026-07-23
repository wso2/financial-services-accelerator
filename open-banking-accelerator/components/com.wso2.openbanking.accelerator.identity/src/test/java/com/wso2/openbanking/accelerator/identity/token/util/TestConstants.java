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

package com.wso2.openbanking.accelerator.identity.token.util;

/**
 * Test constants.
 */
public class TestConstants {
    public static final String IS_TRANSPORT_CERT_MANDATORY_FIELD_NAME = "isTransportCertMandatory";
    public static final String TARGET_STREAM = "targetStream";
    public static final String CERTIFICATE_HEADER = "x-wso2-mutual-auth-cert";
    public static final String EXPIRED_CERTIFICATE_CONTENT = "-----BEGIN CERTIFICATE-----" +
            "MIIFODCCBCCgAwIBAgIEWcWGxDANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMTkwNTE2MDg0NDQ2WhcNMjAwNjE2" +
            "MDkxNDQ2WjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZ" +
            "BgNVBAsTEjAwMTU4MDAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWc0Zna2k3Mk9pcXda" +
            "TkZPWmc2T2FqaTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANoVwx4E" +
            "iWnQs89lj8vKSy/xTbZU2AHS9tFNz7wVa+rkpFyLVPtQW8AthG4hlfrBYMne7/P9" +
            "c1Fi/q+n7eomWvJJo44GV44GJhegM6yyRaIcQdpxe9x9G4twWK4cY+VU3TfE6Dbd" +
            "DdmAt7ai4KFbbpB33N8RwXoeGZdwxZFNPmfaoZZbz5p9+aSMQf1UyExcdlPXah77" +
            "PDZDwAnyy5kYXUPS59S78+p4twqZXyZu9hd+Su5Zod5UObRJ4F5LQzZPS1+KzBje" +
            "JM0o8qoRRZTZkLNnmmQw503KXp/LCLrSbFU2ZLGy3bQpKFFc5I6tZiy67ELNzLWo" +
            "DzngEbApwhX+jtsCAwEAAaOCAgQwggIAMA4GA1UdDwEB/wQEAwIHgDAgBgNVHSUB" +
            "Af8EFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwgeAGA1UdIASB2DCB1TCB0gYLKwYB" +
            "BAGodYEGAWQwgcIwKgYIKwYBBQUHAgEWHmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9w" +
            "b2xpY2llczCBkwYIKwYBBQUHAgIwgYYMgYNVc2Ugb2YgdGhpcyBDZXJ0aWZpY2F0" +
            "ZSBjb25zdGl0dXRlcyBhY2NlcHRhbmNlIG9mIHRoZSBPcGVuQmFua2luZyBSb290" +
            "IENBIENlcnRpZmljYXRpb24gUG9saWNpZXMgYW5kIENlcnRpZmljYXRlIFByYWN0" +
            "aWNlIFN0YXRlbWVudDBtBggrBgEFBQcBAQRhMF8wJgYIKwYBBQUHMAGGGmh0dHA6" +
            "Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDUGCCsGAQUFBzAChilodHRwOi8vb2IudHJ1" +
            "c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNydDA6BgNVHR8EMzAxMC+gLaArhilo" +
            "dHRwOi8vb2IudHJ1c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNybDAfBgNVHSME" +
            "GDAWgBRQc5HGIXLTd/T+ABIGgVx5eW4/UDAdBgNVHQ4EFgQU5eqvEZ6ZdQS5bq/X" +
            "dzP5XY/fUXUwDQYJKoZIhvcNAQELBQADggEBAIg8bd/bIh241ewS79lXU058VjCu" +
            "JC+4QtcI2XiGV3dBpg10V6Kb6E/h8Gru04uVZW1JK52ivVb5NYs6r8txRsTBIaA8" +
            "Cr03LJqEftclL9NbkPZnpEkUfqCBfujNQF8XWaQgXIIA+io1UzV1TG3K9XCa/w2S" +
            "sTANKfF8qK5kRsy6z9OGPUE+Oi3DUt+E9p5LCq6n5Bkp9YRGmyYRPs8JMkJmq3sf" +
            "wtXOy27LE4exJRuZsF1CA78ObaRytuE3DJcnIRdhOcjWieS/MxZD7bzuuAPu5ySX" +
            "i2/qxT3AlWtHtxrz0mKSC3rlgYAHCzCAHoASWKpf5tnB3TodPVZ6DYOu7oI=" +
            "-----END CERTIFICATE-----";

    public static final String CERTIFICATE_CONTENT = "-----BEGIN CERTIFICATE-----" +
            "MIIFLTCCBBWgAwIBAgIEWceUUTANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjYwNzEzMDk0NzUxWhcNMjcwNzEz" +
            "MTAxNzUxWjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZ" +
            "BgNVBAsTEjAwMTU4MDAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWb1E0S29hYXZwT3Vv" +
            "RTdydlFzWkVPVjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMtHTAKv" +
            "wy8OkhElmoQkhpxY0t8sSnrkMJaPwMnbPbVu/MA2F/yGIAE76jbf/hpepaiXimZ1" +
            "7s4mf6ve778eY22Ds6TvUDgTkrHIldzYE1z5+h1Y0soZca/Rv7yhkDepyDezhoC3" +
            "Vp41MrKhNxx2ABPNULLhBS6OWjcaPt0oHhCilwI7cUGZKizeJjO61oKo0Jmi/rPL" +
            "vuCJu6GgwrvcyUmTRDMimoiK85/4ZY8mod8eV2FmyqIAPIrbqrrfWp1CokyG8dke" +
            "wO2xsJ3GppnJaKiqoExl2Lh1ri2mCMJXunpezfrbwarC7zXuZgUuWE7DNghq0oVc" +
            "/v/I6jfDiRnwW2kCAwEAAaOCAfkwggH1MA4GA1UdDwEB/wQEAwIGwDAVBgNVHSUE" +
            "DjAMBgorBgEEAYI3CgMMMIHgBgNVHSAEgdgwgdUwgdIGCysGAQQBqHWBBgFkMIHC" +
            "MCoGCCsGAQUFBwIBFh5odHRwOi8vb2IudHJ1c3Rpcy5jb20vcG9saWNpZXMwgZMG" +
            "CCsGAQUFBwICMIGGDIGDVXNlIG9mIHRoaXMgQ2VydGlmaWNhdGUgY29uc3RpdHV0" +
            "ZXMgYWNjZXB0YW5jZSBvZiB0aGUgT3BlbkJhbmtpbmcgUm9vdCBDQSBDZXJ0aWZp" +
            "Y2F0aW9uIFBvbGljaWVzIGFuZCBDZXJ0aWZpY2F0ZSBQcmFjdGljZSBTdGF0ZW1l" +
            "bnQwbQYIKwYBBQUHAQEEYTBfMCYGCCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rp" +
            "cy5jb20vb2NzcDA1BggrBgEFBQcwAoYpaHR0cDovL29iLnRydXN0aXMuY29tL29i" +
            "X3BwX2lzc3VpbmdjYS5jcnQwOgYDVR0fBDMwMTAvoC2gK4YpaHR0cDovL29iLnRy" +
            "dXN0aXMuY29tL29iX3BwX2lzc3VpbmdjYS5jcmwwHwYDVR0jBBgwFoAUUHORxiFy" +
            "03f0/gASBoFceXluP1AwHQYDVR0OBBYEFEZcuwtHczYvODnEACs6wtlEQDAnMA0G" +
            "CSqGSIb3DQEBCwUAA4IBAQCrssvOFZiIEXK9MIuwOiD0WKhbRP7xozQ6pBcjhn15" +
            "yZqFKeBUp9QFg8dJHIMb+8KUV+Yn+RJg/OI9omvQOiU86Nzuof+HLMzfgK1/WUm7" +
            "cyI1CyfEZ30lVLVNgKp4TSDmczRocUNS5SwU//F9GOvyN4BNtSA9lITPP4NxKFjL" +
            "gLlwO6AQlFTbEPHyiuMzwqln20SlQcRDNI7b5tS6U/QGXEeTkrDLMhq+1BLri0G7" +
            "ZFHtTPMdiNg+VJaAqZyt91QcMLwIHBDAXyCRLc+0zcx0MntDoP/Mj0g1eVbqymiW" +
            "EsUFTY0Li1qDIr2rQSO9CZshYBS3rBrMKbnbMpYT/Pzf" +
            "-----END CERTIFICATE-----";

    public static final String CLIENT_ASSERTION = "eyJraWQiOiJqeVJVY3l0MWtWQ2xjSXZsVWxjRHVrVlozdFUiLCJhbGciOiJQUzI1" +
            "NiJ9.eyJzdWIiOiJpWXBSbTY0YjJ2bXZtS0RoZEw2S1pEOXo2ZmNhIiwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTQ0My9vYXV0a" +
            "DIvdG9rZW4iLCJpc3MiOiJpWXBSbTY0YjJ2bXZtS0RoZEw2S1pEOXo2ZmNhIiwiZXhwIjoxNjEwNjMxNDEyLCJpYXQiOjE2MTA2MDE" +
            "0MTIsImp0aSI6IjE2MTA2MDE0MTI5MDAifQ.tmMTlCL-VABhFTA6QQ6UPvUydKuzynidepAa8oZGEBfVyAsiW5IF01NKYD0ynpXXJC" +
            "Q6hcbWK0FEGity67p6DeI9LT-xAnaKwZY7H8rbuxWye2vhanM0jVa1vggsmwWYyOR4k55ety9lP1MkcGZpaK48qoaqsX_X7GCSGXzq" +
            "BncTEPYfCpVUQtS4ctwoCl06TFbY2Lfm9E24z1rfmU9xPc7au6LpKRLMMHQ8QXuc-FhnWdgEFv_3tAai2ovVmrqEfwj6Z6Ew5bFeI9" +
            "jtCR4TSol47hzDwldx5rH7m2OPUx66yEtGrM7UU62fC-4nxplZ69fjlHN4KQ62PxEaCQs0_A";

    public static final String CLIENT_ASSERTION_NO_HEADER =
            "eyJraWQiOiJqeVJVY3l0MWtWQ2xjSXZsVWxjRHVrVlozdFUiLCJhbGciOiJQUzI1" +
            "NiJ.eyJzdWIiOiJpWXBSbTY0YjJ2bXZtS0RoZEw2S1pEOXo2ZmNhIiwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTQ0My9vYXV0a" +
            "DIvdG9rZW4iLCJpc3MiOiJpWXBSbTY0YjJ2bXZtS0RoZEw2S1pEOXo2ZmNhIiwiZXhwIjoxNjEwNjMxNDEyLCJpYXQiOjE2MTA2MDE" +
            "0MTIsImp0aSI6IjE2MTA2MDE0MTI5MDAifQ.tmMTlCL-VABhFTA6QQ6UPvUydKuzynidepAa8oZGEBfVyAsiW5IF01NKYD0ynpXXJC" +
            "Q6hcbWK0FEGity67p6DeI9LT-xAnaKwZY7H8rbuxWye2vhanM0jVa1vggsmwWYyOR4k55ety9lP1MkcGZpaK48qoaqsX_X7GCSGXzq" +
            "BncTEPYfCpVUQtS4ctwoCl06TFbY2Lfm9E24z1rfmU9xPc7au6LpKRLMMHQ8QXuc-FhnWdgEFv_3tAai2ovVmrqEfwj6Z6Ew5bFeI9" +
            "jtCR4TSol47hzDwldx5rH7m2OPUx66yEtGrM7UU62fC-4nxplZ69fjlHN4KQ62PxEaCQs0_A";
}
