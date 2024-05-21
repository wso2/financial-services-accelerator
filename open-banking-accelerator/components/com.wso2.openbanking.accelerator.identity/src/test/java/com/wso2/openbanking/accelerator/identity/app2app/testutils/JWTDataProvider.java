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

package com.wso2.openbanking.accelerator.identity.app2app.testutils;

import org.testng.annotations.DataProvider;

/**
 * JWT Data provider for App2AppAuthValidation Testing.
 */
public class JWTDataProvider {

    private final String validPublicKey =
                    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLyl7YvRhy57IbxuhV4n7OZw0mmnnXNsDJmL4YQNXy2bRCs59pJb+TYO" +
                    "HsR1xCsq3WH7bX1Ik/EI3weQd2zcxNbtDAUSXSy7jRBuFm1Sk52lASBbmdeOstiqlsg9ptIp/o7u1366cRjn32cXhhsR0y" +
                    "/spUGy8IiXz9rJfP5bEgHQIDAQ";
    private final String validAppAuthIdentifier =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1My05NTQ3LTRlNGYtYmUwNi04ZGIyZWVkNTYzYjMiLCJsb" +
            "2dpbl9oaW50IjoiYWRtaW5Ad3NvMi5jb20iLCJpYXQiOjE3MTYyNjQ5NTUsImp0aSI6IjA1NDU1Zjc1LTkwMmUtNDFhNi04ZDg4LWV" +
            "jZTUwZDM2OTc2NSIsImRpZ2VzdCI6IlNIQS0yNTY9RWtIOGZQZ1oyVFkyWEduczhjNVZ2Y2U4aDNEQjgzVit3NDd6SGl5WWZpUT0iL" +
            "CJleHAiOjE3MTYyNjY3NTUsIm5iZiI6MTcxNjI2NDk1NX0.C0OGMkkaosP2FSLFtqmCgRhrCG7nCJCDLsikkbFWwc5NdzxCFyYUQVI" +
            "Zx4HIRQdabg5K8Ox-WYeqwdhajaKs5Uk63tz5UjlPzX0IKsklXgnWUxdMwfrYsu-znTce0Tc-Ph0h8a8jXF2CKTOfWxwuQvgevSqJe" +
            "-K6zrbJmO8imu4";
    private final String  appAuthIdentifierMissingDigest =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1My05NTQ3LTRlNGYtYmUwNi04ZGIyZWVkNTYzYjMiLCJsb" +
            "2dpbl9oaW50IjoiYWRtaW5Ad3NvMi5jb20iLCJpYXQiOjE3MTYyNjcyMDMsImp0aSI6ImZkNDhmOWMzLTYyZDMtNDUzZS04MWY2LTF" +
            "kMGE4ZDIzM2YzZiIsImV4cCI6MTcxNjI2OTAwMywibmJmIjoxNzE2MjY3MjAzfQ.C_G5-_McCMTz6D01XpPVfrdGlPLaKli9cqWL5K" +
            "nd5ntlDq5ww7J769EJdCGt-S5sfgg5hrPRhyIWK2MJwavGTMzsp1vGdUQXQkT7z68_20k82Lms67tQLIM1VUCDc9rqz5Pule5bVqbY" +
            "oZFmFlHU0Hcmvy166J6c9HlySyMC994";
    private final String appAuthIdentifierInvalidDigest =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1My05NTQ3LTRlNGYtYmUwNi04ZGIyZWVkNTYzYjMiLCJsb" +
            "2dpbl9oaW50IjoiYWRtaW5Ad3NvMi5jb20iLCJpYXQiOjE3MTYyNjc0MjYsImp0aSI6IjYyM2ZhZDY3LTc0ZDMtNDk4OS04YTc1LTE" +
            "2OWYxNDQzOGUwZiIsImRpZ2VzdCI6IlNIQS0yNTY9WUJlc3lUWnhIMWtBVitMTTNKMzZDdzQrVXlQYWlKS0VydVhsdGxsbS9DRT0iL" +
            "CJleHAiOjE3MTYyNjkyMjYsIm5iZiI6MTcxNjI2NzQyNn0.hsuj0osE-o_hyOif7eUvVFIfJpmzF2bDqeINj2Qq2XMQ1Lbnf7LgYMG" +
            "POzmtMi1Jp9Ivwl_3Wt35PcCVko2LI2TIoG-JB8MMeWc1okwwdWGP8Rz5TWCnaXiPGeeFw4PjuV3JMbWeTFafqUFtJUX7pU-8q_hiQ" +
            "zxK1mGjRTjDXRA";
    private final String validRequestObject =
            "eyJraWQiOiI3ZUo4U19aZ3ZsWXhGQUZTZ2hWOXhNSlJPdmsiLCJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJtYXhfYWdlIjo4N" +
            "jQwMCwiYXVkIjoiaHR0cHM6Ly8xOTIuMTY4LjQzLjQ5Ojk0NDYvb2F1dGgyL3Rva2VuIiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQ" +
            "iLCJpc3MiOiI2RWZaSTVOUnByTm9tZlFQWElQZjFSN0ZsNUVhIiwiY2xhaW1zIjp7ImlkX3Rva2VuIjp7ImFjciI6eyJ2YWx1ZXMiO" +
            "lsidXJuOm9wZW5iYW5raW5nOnBzZDI6c2NhIiwidXJuOm9wZW5iYW5raW5nOnBzZDI6Y2EiXSwiZXNzZW50aWFsIjp0cnVlfSwib3B" +
            "lbmJhbmtpbmdfaW50ZW50X2lkIjp7InZhbHVlIjoiZTkyNmE2MzItYzlkMy00MmEwLWEyM2YtMWEwMWZhNDAwOWU3IiwiZXNzZW50a" +
            "WFsIjp0cnVlfX0sInVzZXJpbmZvIjp7Im9wZW5iYW5raW5nX2ludGVudF9pZCI6eyJ2YWx1ZSI6ImU5MjZhNjMyLWM5ZDMtNDJhMC1" +
            "hMjNmLTFhMDFmYTQwMDllNyIsImVzc2VudGlhbCI6dHJ1ZX19fSwicmVzcG9uc2VfdHlwZSI6ImNvZGUgaWRfdG9rZW4iLCJyZWRpc" +
            "mVjdF91cmkiOiJodHRwczovL3d3dy5tb2NrY29tcGFueS5jb20vcmVkaXJlY3RzL3JlZGlyZWN0MSIsInN0YXRlIjoiWVdsemNEb3p" +
            "NVFE0IiwiZXhwIjoxODA3MjMzNDc4LCJub25jZSI6Im4tMFM2X1d6QTJNbCIsImNsaWVudF9pZCI6IjZFZlpJNU5ScHJOb21mUVBYS" +
            "VBmMVI3Rmw1RWEifQ.nKapNc1N5AHxil-xbVpSXrDRsGYkn1YHe1jURxZMVRluDWnyRmjVce9AJ5lCl338Jg0EsU4CNmLwOSu7zmtl" +
            "DCFz4fCIHLj1Q8A-C5I9cWE-nAlV1HnCR_3V7cTU4YE13ZIH7bMCqOPfBX_fpDkJeDXoSnRHQtipMPqIwNfmv7Kf4SjPpZ7kT5zmDn" +
            "cHsUqotpPVoPka_-Nal0KL_-PknC31pKECcxakOFNTeAeiODZN5JIyKGFtq10jQaJi7YvDKsGg1l3rv1gUdJ4s5eXqmnxJUu4J6ocY" +
            "h26Nz3l_Xc1p7XIm2HPhvSW3DpbNpE8Ej0kJkI9FgWz77QACkiO4Hg";

    @DataProvider(name = "ValidJWTProvider")
    public Object[][] getDigest() {
        return new String[][]{
                {validAppAuthIdentifier, validPublicKey, null},
                {validAppAuthIdentifier, validPublicKey, validRequestObject}
        };
    }

    @DataProvider(name = "invalidDigestProvider")
    public Object[][] getInvalidDigest() {
        return new String[][]{
                {appAuthIdentifierMissingDigest, validPublicKey, validRequestObject},
                {appAuthIdentifierInvalidDigest, validPublicKey, validRequestObject}
        };
    }
}

