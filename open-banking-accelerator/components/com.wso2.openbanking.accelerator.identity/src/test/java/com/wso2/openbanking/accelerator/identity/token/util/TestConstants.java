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
            "MIIFODCCBCCgAwIBAgIEWca5LzANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjMwNDE3MDQ1ODE2WhcNMjQwNTE3" +
            "MDUyODE2WjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZ" +
            "BgNVBAsTEjAwMTU4MDAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWb1E0S29hYXZwT3Vv" +
            "RTdydlFzWkVPVjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJeMaWpz" +
            "mwYZ25cDHLSEWhcwaa/JJXgwupZJifByhaao8m/Hhx8PZpXOXz7GcfiNVmz3w1cA" +
            "FXvfrNh4A7rc2vjp9shNQ6bQnbOKVjoN+rNxskYjxpvLOllCUaii5kjdRF5r0YE9" +
            "7t3hH7GdATT56Js9aomykbeYodG1vN4eDcgArn1fO7q+6+0Ew2Mla5X+T/fsfu+1" +
            "4tXMLx7AAQSCzGfsYnJp6fCJQ4uk1d5mlYWd+cM2gWf1eQ5sHeL1K9B+czos57NF" +
            "hsVUBvPCPLmratanj78tN8O6zOxAs1UEckf+z1rLK3D2NCqv9FnfB7saLKhp58vQ" +
            "qoRnOiW+lr1Z4bsCAwEAAaOCAgQwggIAMA4GA1UdDwEB/wQEAwIHgDAgBgNVHSUB" +
            "Af8EFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwgeAGA1UdIASB2DCB1TCB0gYLKwYB" +
            "BAGodYEGAWQwgcIwKgYIKwYBBQUHAgEWHmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9w" +
            "b2xpY2llczCBkwYIKwYBBQUHAgIwgYYMgYNVc2Ugb2YgdGhpcyBDZXJ0aWZpY2F0" +
            "ZSBjb25zdGl0dXRlcyBhY2NlcHRhbmNlIG9mIHRoZSBPcGVuQmFua2luZyBSb290" +
            "IENBIENlcnRpZmljYXRpb24gUG9saWNpZXMgYW5kIENlcnRpZmljYXRlIFByYWN0" +
            "aWNlIFN0YXRlbWVudDBtBggrBgEFBQcBAQRhMF8wJgYIKwYBBQUHMAGGGmh0dHA6" +
            "Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDUGCCsGAQUFBzAChilodHRwOi8vb2IudHJ1" +
            "c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNydDA6BgNVHR8EMzAxMC+gLaArhilo" +
            "dHRwOi8vb2IudHJ1c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNybDAfBgNVHSME" +
            "GDAWgBRQc5HGIXLTd/T+ABIGgVx5eW4/UDAdBgNVHQ4EFgQUSoZfmnXGAPddPqfH" +
            "WVOvkxD89MgwDQYJKoZIhvcNAQELBQADggEBABHzHOJzn4DPHay8xGzlWJIxxe+X" +
            "sNtupR5V/ouEGCzJMUPmegYeK2Kiv+Z9nJKnbspgqLil52yauKWRmiXif4FWoOPR" +
            "wspR9ijnynCgIp6z3EAOawbe28HkaGEfAi8PMqdNAYLKpXg35TUnbP+p2Q55Grq9" +
            "EpSR2APQfJ4TjgLgKjqLRf/RjJAY9hJbQJIUl07esYf8hH7mX6uHDCywzic+UEQ3" +
            "tUfo7PgWmnmtyUdFcW1qAl4P80a5fb8Wq0gNu6gN5tK2bg5TfSo3Gp2It8NVu/dY" +
            "7q3ur7CAYTXrThjg4GXUQgVqYgV3pHbr1LTAiRtac7RBhMNPCklZA78RpTM=" +
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
