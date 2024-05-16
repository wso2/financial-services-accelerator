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
                    "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1My05NTQ3LTRlNGYtYmUwNi04ZGIyZWVkNTYzY" +
                    "jMiLCJsb2dpbkhpbnQiOiJhZG1pbkB3c28yLmNvbSIsImlhdCI6MTcxNTc0ODQ5MSwianRpIjoiNGIxYjUwZDQtYjRjNi0" +
                    "0YTQ2LWIxNWQtMmI2ODRiNzQzOTZhIiwiZXhwIjoxNzE1NzUwMjkxLCJuYmYiOjE3MTU3NDg0OTF9.jGJcBz8eDlE2uc1u" +
                    "EzhWZl72aZVkPXxeIe04OHQw0rqFz74DAATuXgKIvQXEXurz7HV19O-MHdnxZ4CI-Zz4aQQzCZ4P_MTM7pQYTPlZw2Zftq" +
                    "rFEKL03TwxwKHgFuoVd2_OwTAHc5ASEhl_fSMS-IjN_8lR08XApj5CdyG8ras";
    @DataProvider(name = "JWTProvider")
    public Object[][] getJWT() {
        return new String[][]{
                {validAppAuthIdentifier, validPublicKey},
        };
    }
}

