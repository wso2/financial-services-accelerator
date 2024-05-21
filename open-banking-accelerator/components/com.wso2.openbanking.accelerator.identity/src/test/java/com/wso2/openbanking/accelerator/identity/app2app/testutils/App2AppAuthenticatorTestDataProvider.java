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
 * Data Provider class for testing App2AppAuthenticator.
 */
public class App2AppAuthenticatorTestDataProvider {
    private static final String validAppAuthIdentifier =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1My05NTQ3LTRlNGYtYmUwNi04ZGIyZWVkNTYzYjMiLCJsb" +
            "2dpbl9oaW50IjoiYWRtaW5Ad3NvMi5jb20iLCJpYXQiOjE3MTYyNjQ5NTUsImp0aSI6IjA1NDU1Zjc1LTkwMmUtNDFhNi04ZDg4LWV" +
            "jZTUwZDM2OTc2NSIsImRpZ2VzdCI6IlNIQS0yNTY9RWtIOGZQZ1oyVFkyWEduczhjNVZ2Y2U4aDNEQjgzVit3NDd6SGl5WWZpUT0iL" +
            "CJleHAiOjE3MTYyNjY3NTUsIm5iZiI6MTcxNjI2NDk1NX0.C0OGMkkaosP2FSLFtqmCgRhrCG7nCJCDLsikkbFWwc5NdzxCFyYUQVI" +
            "Zx4HIRQdabg5K8Ox-WYeqwdhajaKs5Uk63tz5UjlPzX0IKsklXgnWUxdMwfrYsu-znTce0Tc-Ph0h8a8jXF2CKTOfWxwuQvgevSqJe" +
            "-K6zrbJmO8imu4";
    @DataProvider(name = "app_auth_identifier_provider")
    public Object[][] getAppAuthIdentifier() {

        return new String[][]{
                {validAppAuthIdentifier, "true"},
                {null, "false"},
                {"", "false"},
        };
    }

    @DataProvider(name = "sessionDataKeyProvider")
    public Object[][] getSessionDataKey() {

        return new String[][]{
                {null},
                {""},
                {"550e8400-e29b-41d4-a716-446655440000"},
                {"aaaaaaa-bbbb-Cccc-dddd-eeeeeeeeeeeee"}
        };
    }

    @DataProvider(name = "JWTProvider")
    public Object[][] getJWT() {
        return new String[][]{
                {validAppAuthIdentifier},
        };
    }
}

