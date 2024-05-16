package com.wso2.openbanking.accelerator.common.test.util.testutils;

import org.testng.annotations.DataProvider;

import java.util.Date;

/**
 * Data Provider for JWTUtilsTest.
 */
public class JWTUtilsDataProvider {

    @DataProvider(name = "jwtStrings")
    public Object[][] getJwtStrings() {
        return new Object[][] {
                {"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFt" +
                        "ZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQSflK.xwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                        true},
                // Valid JWT String
                {"", false}, // Empty String
                {null, false}, // Null String
                {"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", false}, // Invalid JWT String with less than 2 dots
                {"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lI" +
                        "iwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c.extra", false},
                // Invalid JWT String with more than 2 dots
                {"  eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwi" +
                        "aWF0IjoxNT.E2MjM5MDIyfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ", true},
                // JWT String with whitespace
                {"...", false}, // JWT String with only dots
                {"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.InvalidBase64.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                        true},
                // JWT String with valid segments but invalid encoding
                {"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lI" +
                        "iwiaWF0IjoxNTE2MjM5MDIyfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c.invalidJSON", true}
                // JWT String with valid segments and valid encoding but invalid JSON
        };
    }

    @DataProvider(name = "validParsableJwtStrings")
    public Object[][] getValidParsablejwtStrings() {
        String parsableJwtString =
                "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1My05NTQ3LT" +
                "RlNGYtYmUwNi04ZGIyZWVkNTYzYjMiLCJsb2dpbkhpbnQiOiJhZG1pbkB3c28yLmNvb" +
                "SIsImlhdCI6MTcxNDkyOTk2MCwianRpIjoiNmU0MWM4N2UtYWJmNi00ZjU1LTliNjQt" +
                "NjYwMWFlODg2NjZjIiwiZXhwIjoxNzE0OTMxNzYwLCJuYmYiOjE3MTQ5Mjk5NjB9.WB" +
                "7qvq3w6htUop600H5C4HwL-r0wb8GekJE6X4-zrFn2IofEcwV0yisSE5fH8uyrzdmVm" +
                "OiBgFXY9Y9cUVlS6t9HMbhlzs2qY0bVzDYVNG7GjgnYIcyh3lx9obqL9O3DJKNre5GS" +
                "3b-ATPN6VvYC9F2KnwwuoNky-3Wlcw3G9-E";
        return new String[][] {
                {parsableJwtString}
        };
    }

    @DataProvider(name = "validNotParsableJwtStrings")
    public Object[][] getValidNotParsableJwtStrings() {
        String notParsableJwtString =
                "not.parsable.jwt";
        return new String[][] {
                {notParsableJwtString}
        };
    }

    @DataProvider(name = "notValidJwtStrings")
    public Object[][] getNotValidJwtStrings() {
        String notValidJwtString =
                "not.validJWT";
        return new String[][] {
                {notValidJwtString}
        };
    }

    @DataProvider(name = "expiryTimeProvider")
    public Object[][] getExpiryTime() {

        return new Object[][]{
                {null, 0, false},
                {new Date(System.currentTimeMillis() - 10 * 1000), 0, false},
                {new Date(System.currentTimeMillis() + 10 * 100), 0, true}
        };
    }

    @DataProvider(name = "nbfProvider")
    public Object[][] getNbf() {

        return new Object[][]{
                {null, 0, false},
                {new Date(System.currentTimeMillis() + 10 * 1000), 0, false},
                {new Date(System.currentTimeMillis() - 3 * 1000), 0, true}
        };
    }
}
