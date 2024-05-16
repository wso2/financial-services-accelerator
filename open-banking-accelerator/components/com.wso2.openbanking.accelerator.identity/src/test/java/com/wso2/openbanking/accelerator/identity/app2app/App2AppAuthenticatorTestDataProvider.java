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

package com.wso2.openbanking.accelerator.identity.app2app;

import org.testng.annotations.DataProvider;

/**
 * Data Provider class for testing App2AppAuthenticator.
 */
public class App2AppAuthenticatorTestDataProvider {
    private static final String validAppAuthIdentifier =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1My05NTQ3LT" +
            "RlNGYtYmUwNi04ZGIyZWVkNTYzYjMiLCJsb2dpbkhpbnQiOiJhZG1pbkB3c28yLmNvb" +
            "SIsImlhdCI6MTcxNDkyOTk2MCwianRpIjoiNmU0MWM4N2UtYWJmNi00ZjU1LTliNjQt" +
            "NjYwMWFlODg2NjZjIiwiZXhwIjoxNzE0OTMxNzYwLCJuYmYiOjE3MTQ5Mjk5NjB9.WB" +
            "7qvq3w6htUop600H5C4HwL-r0wb8GekJE6X4-zrFn2IofEcwV0yisSE5fH8uyrzdmVm" +
            "OiBgFXY9Y9cUVlS6t9HMbhlzs2qY0bVzDYVNG7GjgnYIcyh3lx9obqL9O3DJKNre5GS" +
            "3b-ATPN6VvYC9F2KnwwuoNky-3Wlcw3G9-E";
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
