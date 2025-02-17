/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.identity.extensions.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants for testing.
 */
public class TestConstants {

    public static final String CERTIFICATE_HEADER = "x-wso2-mutual-auth-cert";
    public static final String CERTIFICATE_CONTENT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIFODCCBCCgAwIBAgIEWcbiiTANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH\n" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy\n" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjMxMTE1MDUxMDMxWhcNMjQxMjE1\n" +
            "MDU0MDMxWjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZ\n" +
            "BgNVBAsTEjAwMTU4MDAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWakZRdVE0ZVFiTkNN\n" +
            "U3FkQ29nMjFuRjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJslGjTm\n" +
            "0tWwnnKgC7WNqUSYNxblURkJyoD5UuSmzpsM5nlUBAxYxBgztTo062LJELzUTzA/\n" +
            "9kgLIMMgj+wG1OS475QCgeyoDmwf0SPuFRBl0G0AjxAvJzzs2aijzxiYRbKUa4gm\n" +
            "O1KPU3Xlz89mi8lwjTZlxtGk3ABwBG4f5na5TY7uZMlgWPXDnTg7Cc1H4mrMbEFk\n" +
            "UaXmb6ZhhGtp0JL04+4Lp16QWrgiHrlop+P8bd+pwmmOmLuglTIEh+v993j+7v8B\n" +
            "XYqdmYQ3noiOhK9ynFPD1A7urrm71Pgkuq+Wk5HCvMiBK7zZ4Sn9FDovykDKZTFY\n" +
            "MloVDXLhmfDQrmcCAwEAAaOCAgQwggIAMA4GA1UdDwEB/wQEAwIHgDAgBgNVHSUB\n" +
            "Af8EFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwgeAGA1UdIASB2DCB1TCB0gYLKwYB\n" +
            "BAGodYEGAWQwgcIwKgYIKwYBBQUHAgEWHmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9w\n" +
            "b2xpY2llczCBkwYIKwYBBQUHAgIwgYYMgYNVc2Ugb2YgdGhpcyBDZXJ0aWZpY2F0\n" +
            "ZSBjb25zdGl0dXRlcyBhY2NlcHRhbmNlIG9mIHRoZSBPcGVuQmFua2luZyBSb290\n" +
            "IENBIENlcnRpZmljYXRpb24gUG9saWNpZXMgYW5kIENlcnRpZmljYXRlIFByYWN0\n" +
            "aWNlIFN0YXRlbWVudDBtBggrBgEFBQcBAQRhMF8wJgYIKwYBBQUHMAGGGmh0dHA6\n" +
            "Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDUGCCsGAQUFBzAChilodHRwOi8vb2IudHJ1\n" +
            "c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNydDA6BgNVHR8EMzAxMC+gLaArhilo\n" +
            "dHRwOi8vb2IudHJ1c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNybDAfBgNVHSME\n" +
            "GDAWgBRQc5HGIXLTd/T+ABIGgVx5eW4/UDAdBgNVHQ4EFgQU7T6cMtCSQTT5JWW3\n" +
            "O6vifRUSdpkwDQYJKoZIhvcNAQELBQADggEBAE9jrd/AE65vy3SEWdmFKPS4su7u\n" +
            "EHy+KH18PETV6jMF2UFIJAOx7jl+5a3O66NkcpxFPeyvSuH+6tAAr2ZjpoQwtW9t\n" +
            "Z9k2KSOdNOiJeQgjavwQC6t/BHI3yXWOIQm445BUN1cV9pagcRJjRyL3SPdHVoRf\n" +
            "IbF7VI/+ULHwWdZYPXxtwUoda1mQFf6a+2lO4ziUHb3U8iD90FBURzID7WJ1ODSe\n" +
            "B5zE/hG9Sxd9wlSXvl1oNmc/ha5oG/7rJpRqrx5Dcq3LEoX9iZZ3knHLkCm/abIQ\n" +
            "7Nff8GQytuGhnGZxmGFYKDXdKElcl9dAlZ3bIK2I+I6jD2z2XvSfrhFyRjU=\n" +
            "-----END CERTIFICATE-----";

    public static final String VALID_REQUEST = "eyJraWQiOiJEd01LZFdNbWo3UFdpbnZvcWZReVhWenlaNlEiLCJ0eXAiOiJKV1" +
            "QiLCJhbGciOiJQUzI1NiJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ2L29hdXRoMi90b2tlbiIsIm1heF9hZ2UiOjg2NDAw" +
            "LCJjcml0IjpbImI2NCIsImh0dHA6Ly9vcGVuYmFua2luZy5vcmcudWsvaWF0IiwiaHR0cDovL29wZW5iYW5raW5nLm9yZy51ay9pc3M" +
            "iLCJodHRwOi8vb3BlbmJhbmtpbmcub3JnLnVrL3RhbiJdLCJzY29wZSI6ImFjY291bnRzIG9wZW5pZCIsImV4cCI6MTk1NDcwODcxMC" +
            "wiY2xhaW1zIjp7ImlkX3Rva2VuIjp7ImFjciI6eyJ2YWx1ZXMiOlsidXJuOm9wZW5iYW5raW5nOnBzZDI6Y2EiLCJ1cm46b3BlbmJhb" +
            "mtpbmc6cHNkMjpzY2EiXSwiZXNzZW50aWFsIjp0cnVlfSwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjp7InZhbHVlIjoiOTRmODU3M2Mt" +
            "NjA4MC00MDI1LThkOWItMDhlM2U5MjAwZGU3IiwiZXNzZW50aWFsIjp0cnVlfX0sInVzZXJpbmZvIjp7Im9wZW5iYW5raW5nX2ludGV" +
            "udF9pZCI6eyJ2YWx1ZSI6Ijk0Zjg1NzNjLTYwODAtNDAyNS04ZDliLTA4ZTNlOTIwMGRlNyIsImVzc2VudGlhbCI6dHJ1ZX19fSwiaX" +
            "NzIjoiMlgwbjlXU05tUGlxM1hUQjhkdEMwU2hzNXI4YSIsInJlc3BvbnNlX3R5cGUiOiJjb2RlIGlkX3Rva2VuIiwicmVkaXJlY3Rfd" +
            "XJpIjoiaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9yZWRpcmVjdHMvcmVkaXJlY3QxIiwic3RhdGUiOiIwcE4wTkJUSGN2Iiwibm9uY2Ui" +
            "OiJqQlhoT21PS0NCIiwiY2xpZW50X2lkIjoiMlgwbjlXU05tUGlxM1hUQjhkdEMwU2hzNXI4YSJ9.BjQIMnlqevJgj1zL25hmfKA9Ab" +
            "7qm2GvrkZAkxbTMNOxkxtKxQSt9QkLfycITEeM9YdGV5rh2FMdXxyex-WtO_H0G9zlKtsDyrsUw3_HWaBDd-61Hz6n65Few_f6bwtIg" +
            "HtW8oDeKpylUu01OsYtB_s4nnDw42ZCKv7zGzkTDkyoxoM2_b_AUqh-F3PNY9Arru5m1-FGDYi_zl4iQ3d3um_NYnhPhmC2wz2R9yms" +
            "flXBn9bd-d6nPKl_ftGnqmiBua7oKMd-3CMCFW9Uxig82PHbwHcuy1hYqa_7JoE58Zkr6baGur3YtgJ2381_8t5v19DJvjqhaodabfe" +
            "uNWR3GA";

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

    public static final String SSA = "eyJhbGciOiJQUzI1NiIsImtpZCI6ImNJWW8tNXpYNE9UV1pwSHJtbWlaRFZ4QUNKTSIsInR5cCI6Ik" +
            "pXVCJ9.eyJpc3MiOiJPcGVuQmFua2luZyBMdGQiLCJpYXQiOjE3MTY5NTU2MTMsImp0aSI6ImJhM2JhZjNjODQ3MjQ1ZmEiLCJzb2Z0" +
            "d2FyZV9lbnZpcm9ubWVudCI6InNhbmRib3giLCJzb2Z0d2FyZV9tb2RlIjoiVGVzdCIsInNvZnR3YXJlX2lkIjoib1E0S29hYXZwT3V" +
            "vRTdydlFzWkVPViIsInNvZnR3YXJlX2NsaWVudF9pZCI6Im9RNEtvYWF2cE91b0U3cnZRc1pFT1YiLCJzb2Z0d2FyZV9jbGllbnRfbm" +
            "FtZSI6IldTTzIgT3BlbiBCYW5raW5nIFRQUDIgKFNhbmRib3gpIiwic29mdHdhcmVfY2xpZW50X2Rlc2NyaXB0aW9uIjoiV1NPMiBPc" +
            "GVuIEJhbmtpbmcgVFBQMiBmb3IgdGVzdGluZyIsInNvZnR3YXJlX3ZlcnNpb24iOiIxLjUiLCJzb2Z0d2FyZV9jbGllbnRfdXJpIjoi" +
            "aHR0cHM6Ly93d3cuZ29vZ2xlLmNvbSIsInNvZnR3YXJlX3JlZGlyZWN0X3VyaXMiOlsiaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9yZWR" +
            "pcmVjdHMvcmVkaXJlY3QxIl0sInNvZnR3YXJlX3JvbGVzIjpbIlBJU1AiLCJBSVNQIiwiQ0JQSUkiXSwib3JnYW5pc2F0aW9uX2NvbX" +
            "BldGVudF9hdXRob3JpdHlfY2xhaW1zIjp7ImF1dGhvcml0eV9pZCI6Ik9CR0JSIiwicmVnaXN0cmF0aW9uX2lkIjoiVW5rbm93bjAwM" +
            "TU4MDAwMDFIUVFyWkFBWCIsInN0YXR1cyI6IkFjdGl2ZSIsImF1dGhvcmlzYXRpb25zIjpbeyJtZW1iZXJfc3RhdGUiOiJHQiIsInJv" +
            "bGVzIjpbIlBJU1AiLCJBSVNQIiwiQ0JQSUkiXX0seyJtZW1iZXJfc3RhdGUiOiJJRSIsInJvbGVzIjpbIlBJU1AiLCJDQlBJSSIsIkF" +
            "JU1AiXX0seyJtZW1iZXJfc3RhdGUiOiJOTCIsInJvbGVzIjpbIlBJU1AiLCJBSVNQIiwiQ0JQSUkiXX1dfSwic29mdHdhcmVfbG9nb1" +
            "91cmkiOiJodHRwczovL3d3dy5nb29nbGUuY29tIiwib3JnX3N0YXR1cyI6IkFjdGl2ZSIsIm9yZ19pZCI6IjAwMTU4MDAwMDFIUVFyW" +
            "kFBWCIsIm9yZ19uYW1lIjoiV1NPMiAoVUspIExJTUlURUQiLCJvcmdfY29udGFjdHMiOlt7Im5hbWUiOiJUZWNobmljYWwiLCJlbWFp" +
            "bCI6InNhY2hpbmlzQHdzbzIuY29tIiwicGhvbmUiOiIrOTQ3NzQyNzQzNzQiLCJ0eXBlIjoiVGVjaG5pY2FsIn0seyJuYW1lIjoiQnV" +
            "zaW5lc3MiLCJlbWFpbCI6InNhY2hpbmlzQHdzbzIuY29tIiwicGhvbmUiOiIrOTQ3NzQyNzQzNzQiLCJ0eXBlIjoiQnVzaW5lc3MifV" +
            "0sIm9yZ19qd2tzX2VuZHBvaW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub3JnLnVrLzAwMTU4MDAwMDFIUVFyW" +
            "kFBWC8wMDE1ODAwMDAxSFFRclpBQVguandrcyIsIm9yZ19qd2tzX3Jldm9rZWRfZW5kcG9pbnQiOiJodHRwczovL2tleXN0b3JlLm9w" +
            "ZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYL3Jldm9rZWQvMDAxNTgwMDAwMUhRUXJaQUFYLmp3a3MiLCJzb2Z" +
            "0d2FyZV9qd2tzX2VuZHBvaW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub3JnLnVrLzAwMTU4MDAwMDFIUVFyWk" +
            "FBWC9vUTRLb2FhdnBPdW9FN3J2UXNaRU9WLmp3a3MiLCJzb2Z0d2FyZV9qd2tzX3Jldm9rZWRfZW5kcG9pbnQiOiJodHRwczovL2tle" +
            "XN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYL3Jldm9rZWQvb1E0S29hYXZwT3VvRTdydlFzWkVP" +
            "Vi5qd2tzIiwic29mdHdhcmVfcG9saWN5X3VyaSI6Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20iLCJzb2Z0d2FyZV90b3NfdXJpIjoiaHR" +
            "0cHM6Ly93d3cuZ29vZ2xlLmNvbSIsInNvZnR3YXJlX29uX2JlaGFsZl9vZl9vcmciOiJXU08yIE9wZW4gQmFua2luZyJ9.VnfXrppGm" +
            "CcYGWLcmT3gB22r297Vc0ppibLrl0mv8PGkb7oZLEkIqaAdz9OBwFjehDiHlIzOrCzNgzJD5GvyacZSiorFkpzBpbV80q-n_-uFTugE" +
            "7mrCVnNfTsb1SBEdoWCRn_BbzH-T2YstqLWPhb_fHkDSFTGJeSnFGp1EcMXVmx8P-pCgsoTS-kXEPDXD7F4iZjZwcFfxDpe_N8FvAUC" +
            "28l3Tzm1au4bLjrI0T94PVoEJEmAk9AU_somFy_XEvuKuuULfccW3CDR6KGbvXV7MVPNA5XTT5g_H9bRxcPX4ZaaWfIaRVfZ28d_ZCR" +
            "tsUIiLgm_6eZkiD-7Eh3qyVQ";

    public static final String DCR_APP_REQUEST = "{\n" +
            "  \"redirectUris\": [" +
            "       \"https://www.google.com/redirects/redirect1\"" +
            "   ],\n" +
            "  \"clientName\": \"9ZzFFBxSLGEjPZogRAbvFd\",\n" +
            "  \"grantTypes\": [\n" +
            "       \"authorization_code\", \n" +
            "       \"client_credentials\", \n" +
            "       \"refresh_token\"" +
            "   ],\n" +
            "  \"jwksURI\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/" +
            "                   9ZzFFBxSLGEjPZogRAbvFd.jwks\",\n" +
            "  \"tokenType\": \"JWT\",\n" +
            "  \"extApplicationDisplayName\": \"WSO2_Open_Banking_TPP__Sandbox_\",\n" +
            "  \"tokenEndpointAuthMethod\": \"private_key_jwt\",\n" +
            "  \"tokenEndpointAuthSignatureAlgorithm\": \"PS256\",\n" +
            "  \"idTokenSignatureAlgorithm\": \"PS256\",\n" +
            "  \"idTokenEncryptionAlgorithm\": \"RSA-OAEP\",\n" +
            "  \"idTokenEncryptionMethod\": \"A256GCM\",\n" +
            "  \"requestObjectSignatureAlgorithm\": \"PS256\",\n" +
            "  \"requireSignedRequestObject\": true,\n" +
            "  \"tlsClientCertificateBoundAccessTokens\": true,\n" +
            "  \"softwareStatement\":" + SSA + ",\n" +
            "  \"additionalAttributes\": {\n" +
            "       \"software_id\": \"9ZzFFBxSLGEjPZogRAbvFd\", \n" +
            "       \"aud\": [\"https://localbank.com\"], \n" +
            "       \"application_type\": \"web\", \n" +
            "       \"scope\": \"accounts payments\", \n" +
            "       \"token_endpoint_allow_reuse_pvt_key_jwt\": false, \n" +
            "       \"iss\": \"9ZzFFBxSLGEjPZogRAbvFd\", \n" +
            "       \"exp\": 1739098988640, \n" +
            "       \"iat\": 1738839788640, \n" +
            "       \"jti\": 1738839788640, \n" +
            "       \"response_types\": [\"code id_token\"]" +
            "   }\n" +
            "}\n";

    public static final String DCR_APP_REQUEST_WITHOUT_MANDATORY_FIELDS = "{\n" +
            "  \"redirectUris\": [" +
            "       \"https://www.google.com/redirects/redirect1\"" +
            "   ],\n" +
            "  \"clientName\": \"9ZzFFBxSLGEjPZogRAbvFd\",\n" +
            "  \"grantTypes\": [\n" +
            "       \"authorization_code\", \n" +
            "       \"client_credentials\", \n" +
            "       \"refresh_token\"" +
            "   ],\n" +
            "  \"jwksURI\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/" +
            "                   9ZzFFBxSLGEjPZogRAbvFd.jwks\",\n" +
            "  \"tokenType\": \"JWT\",\n" +
            "  \"extApplicationDisplayName\": \"WSO2_Open_Banking_TPP__Sandbox_\",\n" +
            "  \"tokenEndpointAuthMethod\": \"private_key_jwt\",\n" +
            "  \"tokenEndpointAuthSignatureAlgorithm\": \"PS256\",\n" +
            "  \"idTokenSignatureAlgorithm\": \"PS256\",\n" +
            "  \"idTokenEncryptionAlgorithm\": \"RSA-OAEP\",\n" +
            "  \"idTokenEncryptionMethod\": \"A256GCM\",\n" +
            "  \"requestObjectSignatureAlgorithm\": \"PS256\",\n" +
            "  \"requireSignedRequestObject\": true,\n" +
            "  \"tlsClientCertificateBoundAccessTokens\": true,\n" +
            "  \"additionalAttributes\": {\n" +
            "       \"aud\": [\"https://localbank.com\"], \n" +
            "       \"application_type\": \"web\", \n" +
            "       \"token_endpoint_allow_reuse_pvt_key_jwt\": false, \n" +
            "       \"iss\": \"9ZzFFBxSLGEjPZogRAbvFd\", \n" +
            "       \"exp\": 1739098988640, \n" +
            "       \"iat\": 1738839788640, \n" +
            "       \"jti\": 1738839788640, \n" +
            "       \"response_types\": [\"code id_token\"]" +
            "   }\n" +
            "}\n";

    public static final String DCR_APP_REQUEST_WITH_DISALLOWED_VALUES = "{\n" +
            "  \"redirectUris\": [" +
            "       \"https://www.google.com/redirects/redirect1\"" +
            "   ],\n" +
            "  \"clientName\": \"9ZzFFBxSLGEjPZogRAbvFd\",\n" +
            "  \"grantTypes\": [\n" +
            "       \"authorization_code\", \n" +
            "       \"client_credentials\", \n" +
            "       \"refresh_token\"" +
            "   ],\n" +
            "  \"jwksURI\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/" +
            "                   9ZzFFBxSLGEjPZogRAbvFd.jwks\",\n" +
            "  \"tokenType\": \"JWT\",\n" +
            "  \"extApplicationDisplayName\": \"WSO2_Open_Banking_TPP__Sandbox_\",\n" +
            "  \"tokenEndpointAuthMethod\": \"private_key_jwt\",\n" +
            "  \"tokenEndpointAuthSignatureAlgorithm\": \"PS256\",\n" +
            "  \"idTokenSignatureAlgorithm\": \"PS256\",\n" +
            "  \"idTokenEncryptionAlgorithm\": \"RSA-OAEP\",\n" +
            "  \"idTokenEncryptionMethod\": \"A256GCM\",\n" +
            "  \"requestObjectSignatureAlgorithm\": \"PS256\",\n" +
            "  \"requireSignedRequestObject\": true,\n" +
            "  \"tlsClientCertificateBoundAccessTokens\": true,\n" +
            "  \"softwareStatement\":" + SSA + ",\n" +
            "  \"additionalAttributes\": {\n" +
            "       \"software_id\": \"9ZzFFBxSLGEjPZogRAbvFd\", \n" +
            "       \"aud\": [\"https://localbank.com\"], \n" +
            "       \"application_type\": \"web\", \n" +
            "       \"scope\": \"accounts payments unsupported_scope\", \n" +
            "       \"token_endpoint_allow_reuse_pvt_key_jwt\": false, \n" +
            "       \"iss\": \"9ZzFFBxSLGEjPZogRAbvFd\", \n" +
            "       \"exp\": 1739098988640, \n" +
            "       \"iat\": 1738839788640, \n" +
            "       \"jti\": 1738839788640, \n" +
            "       \"response_types\": [\"code id_token\"]" +
            "   }\n" +
            "}\n";

    public static Map<String, Object> getSSAParamMap() {
        Map<String, Object> ssaParams = new HashMap<>();

        ssaParams.put("software_mode", "Test");
        ssaParams.put("org_jwks_endpoint",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/0015800001HQQrZAAX.jwks");
        ssaParams.put("software_client_name", "WSO2 Open Banking TPP (Sandbox)");
        ssaParams.put("org_status", "Active");
        ssaParams.put("iss", "OpenBanking Ltd");
        ssaParams.put("software_tos_uri", "https://www.google.com");
        ssaParams.put("software_policy_uri", "https://www.google.com");
        ssaParams.put("software_id", "9ZzFFBxSLGEjPZogRAbvFd");
        ssaParams.put("software_environment", "sandbox");
        ssaParams.put("software_version", "1.5");
        ssaParams.put("org_name", "WSO2 (UK) LIMITED");
        ssaParams.put("iat", "Fri Jan 03 13:48:23 IST 2025");
        ssaParams.put("jti", "f8844552ceae49a3");
        ssaParams.put("software_client_id", "9ZzFFBxSLGEjPZogRAbvFd");
        ssaParams.put("software_client_description", "WSO2 Open Banking TPP for testing");
        ssaParams.put("software_jwks_endpoint",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/9ZzFFBxSLGEjPZogRAbvFd.jwks");
        ssaParams.put("software_client_uri", "https://www.google.com");
        ssaParams.put("software_on_behalf_of_org", "WSO2 Open Banking");
        ssaParams.put("software_logo_uri", "https://www.google.com");
        ssaParams.put("org_id", "0015800001HQQrZAAX");
        ssaParams.put("software_jwks_revoked_endpoint",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/revoked/9ZzFFBxSLGEjPZogRAbvFd.jwks");
        ssaParams.put("software_roles", "[\"PISP\",\"AISP\",\"CBPII\"]");
        ssaParams.put("org_jwks_revoked_endpoint",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/revoked/0015800001HQQrZAAX.jwks");
        ssaParams.put("registration_id", "Unknown0015800001HQQrZAAX");
        ssaParams.put("authority_id", "OBGBR");
        ssaParams.put("status", "Active");
        List<String> redirectUris = new ArrayList<>();
        redirectUris.add("https://www.google.com/redirects/redirect1");
        ssaParams.put("software_redirect_uris", redirectUris);

        return ssaParams;
    }

    public static Map<String, String> getSSAParamMapForRetrieval() {
        Map<String, String> ssaParams = new HashMap<>();

        ssaParams.put("software_mode", "Test");
        ssaParams.put("org_jwks_endpoint",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/0015800001HQQrZAAX.jwks");
        ssaParams.put("software_client_name", "WSO2 Open Banking TPP (Sandbox)");
        ssaParams.put("org_status", "Active");
        ssaParams.put("iss", "OpenBanking Ltd");
        ssaParams.put("software_tos_uri", "https://www.google.com");
        ssaParams.put("software_policy_uri", "https://www.google.com");
        ssaParams.put("software_id", "9ZzFFBxSLGEjPZogRAbvFd");
        ssaParams.put("software_environment", "sandbox");
        ssaParams.put("software_version", "1.5");
        ssaParams.put("org_name", "WSO2 (UK) LIMITED");
        ssaParams.put("iat", "Fri Jan 03 13:48:23 IST 2025");
        ssaParams.put("jti", "f8844552ceae49a3");
        ssaParams.put("software_client_id", "9ZzFFBxSLGEjPZogRAbvFd");
        ssaParams.put("software_client_description", "WSO2 Open Banking TPP for testing");
        ssaParams.put("software_jwks_endpoint",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/9ZzFFBxSLGEjPZogRAbvFd.jwks");
        ssaParams.put("software_client_uri", "https://www.google.com");
        ssaParams.put("software_on_behalf_of_org", "WSO2 Open Banking");
        ssaParams.put("software_logo_uri", "https://www.google.com");
        ssaParams.put("org_id", "0015800001HQQrZAAX");
        ssaParams.put("software_jwks_revoked_endpoint",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/revoked/9ZzFFBxSLGEjPZogRAbvFd.jwks");
        ssaParams.put("software_roles", "[\"PISP\",\"AISP\",\"CBPII\"]");
        ssaParams.put("org_jwks_revoked_endpoint",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/revoked/0015800001HQQrZAAX.jwks");
        ssaParams.put("registration_id", "Unknown0015800001HQQrZAAX");
        ssaParams.put("authority_id", "OBGBR");
        ssaParams.put("status", "Active");

        return ssaParams;
    }
}
