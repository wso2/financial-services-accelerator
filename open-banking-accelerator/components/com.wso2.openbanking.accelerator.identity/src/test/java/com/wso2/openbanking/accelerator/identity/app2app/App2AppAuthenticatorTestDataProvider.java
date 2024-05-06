package com.wso2.openbanking.accelerator.identity.app2app;

import org.testng.annotations.DataProvider;

public class App2AppAuthenticatorTestDataProvider {
    @DataProvider(name = "UsernameAndPasswordProvider")
    public Object[][] getWrongUsernameAndPassword() {

        return new String[][]{
                {"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1My05NTQ3LTRlNGYtYmUwNi04ZGIyZWVkNTYzYjMiLCJsb2dpbkhpbnQiOiJhZG1pbkB3c28yLmNvbSIsImlhdCI6MTcxNDkyOTk2MCwianRpIjoiNmU0MWM4N2UtYWJmNi00ZjU1LTliNjQtNjYwMWFlODg2NjZjIiwiZXhwIjoxNzE0OTMxNzYwLCJuYmYiOjE3MTQ5Mjk5NjB9.WB7qvq3w6htUop600H5C4HwL-r0wb8GekJE6X4-zrFn2IofEcwV0yisSE5fH8uyrzdmVmOiBgFXY9Y9cUVlS6t9HMbhlzs2qY0bVzDYVNG7GjgnYIcyh3lx9obqL9O3DJKNre5GS3b-ATPN6VvYC9F2KnwwuoNky-3Wlcw3G9-E", "true"},
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
}
