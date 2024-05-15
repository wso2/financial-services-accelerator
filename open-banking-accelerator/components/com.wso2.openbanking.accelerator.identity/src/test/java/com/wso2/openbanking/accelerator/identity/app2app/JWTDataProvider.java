package com.wso2.openbanking.accelerator.identity.app2app;

import org.testng.annotations.DataProvider;

public class JWTDataProvider {

    private final String validPublicKey ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLyl7YvRhy57IbxuhV4n7OZw0mmn" +
            "nXNsDJmL4YQNXy2bRCs59pJb+TYOHsR1xCsq3WH7bX1Ik/EI3weQd2zcxNbtDAUSXSy7jRBuFm1Sk52lASBbmdeOstiqlsg" +
            "9ptIp/o7u1366cRjn32cXhhsR0y/spUGy8IiXz9rJfP5bEgHQIDAQ";
    private final String validAppAuthIdentifier = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaWQiOiI1NTBmNDQ1M" +
            "y05NTQ3LTRlNGYtYmUwNi04ZGIyZWVkNTYzYjMiLCJsb2dpbkhpbnQiOiJhZG1pbkB3c28yLmNvbSIsImlhdCI6MTcxNTc0O" +
            "DQ5MSwianRpIjoiNGIxYjUwZDQtYjRjNi00YTQ2LWIxNWQtMmI2ODRiNzQzOTZhIiwiZXhwIjoxNzE1NzUwMjkxLCJuYmYiO" +
            "jE3MTU3NDg0OTF9.jGJcBz8eDlE2uc1uEzhWZl72aZVkPXxeIe04OHQw0rqFz74DAATuXgKIvQXEXurz7HV19O-MHdnxZ4CI-" +
            "Zz4aQQzCZ4P_MTM7pQYTPlZw2ZftqrFEKL03TwxwKHgFuoVd2_OwTAHc5ASEhl_fSMS-IjN_8lR08XApj5CdyG8ras";
    @DataProvider(name = "JWTProvider")
    public Object[][] getJWT() {
        return new String[][]{
                {validAppAuthIdentifier,validPublicKey},
        };
    }
}
