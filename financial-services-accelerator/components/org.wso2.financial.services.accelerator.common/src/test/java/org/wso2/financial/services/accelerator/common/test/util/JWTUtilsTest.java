/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.common.test.util;


import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.test.util.testutils.JWTUtilsTestDataProvider;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;

import java.text.ParseException;
import java.util.Date;

/**
 * Test class for Unit Testing JWTUtils.
 */
public class JWTUtilsTest {

    @Test(dataProviderClass = JWTUtilsTestDataProvider.class, dataProvider = "jwtStrings")
    public void testIsJWT(String jwtString, boolean expected) {

        Assert.assertEquals(JWTUtils.isValidJWSFormat(jwtString), expected);
    }

    @Test(dataProviderClass = JWTUtilsTestDataProvider.class, dataProvider = "validParsableJwtStrings")
    public void testGetSignedJWT(String jwtString) throws ParseException {

        Assert.assertNotNull(JWTUtils.getSignedJWT(jwtString));
    }

    @Test(expectedExceptions = ParseException.class,
            dataProviderClass = JWTUtilsTestDataProvider.class, dataProvider = "validNotParsableJwtStrings")
    public void testGetSignedJWTWIthNotParsableJWT(String jwtString) throws ParseException {

        JWTUtils.getSignedJWT(jwtString);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            dataProviderClass = JWTUtilsTestDataProvider.class, dataProvider = "notValidJwtStrings")
    public void testGetSignedJWTWIthNotValidJWT(String jwtString) throws ParseException {

        JWTUtils.getSignedJWT(jwtString);
    }

    @Test(dataProviderClass = JWTUtilsTestDataProvider.class, dataProvider = "expiryTimeProvider")
    public void testValidExpirationTime(Date time, long timeSkew, boolean expected) {

        Assert.assertEquals(JWTUtils.isValidExpiryTime(time, timeSkew), expected);
    }

    @Test(dataProviderClass = JWTUtilsTestDataProvider.class, dataProvider = "nbfProvider")
    public void testValidNotValidBefore(Date time, long timeSkew, boolean expected) {

        Assert.assertEquals(JWTUtils.isValidNotValidBeforeTime(time, timeSkew), expected);
    }

    @Test
    public void testDecodeRequestJWT() throws ParseException {

        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6I" +
                "kpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTc0MTk0MzM0Nn0.NVWtXLE-41ykk2xkiVnWrN_f" +
                "InvonW6KeD6GfrtxmVDnJHuMXuro8qS-4iAN9Jzgh_j0R-ZiM-p_6kGqxsYVB4z3tcJ85kgVep2PpV-4" +
                "ZaDzoNPb24yEiV4Wr36SLLomnssqFuqNVe_LkJgoAMlT8zJpsLyNW9LAOmsmoEqQIR9r0T8nbLvZWsUN" +
                "fUbPjDMrijUcPh3z5TeAs0dqW938AGNJO93b_PB6qWWio7m9QtWkE95jhZVboYgrj3JYtqlycQN9Dj5F" +
                "BZ82WfQAL1tqkzFicKPErKXnlyoPreOGKJXUzT_m5OHnfybc_F78NqaFetIy8F0n-X9j7QMj2vRj_w";
        String header = "{\"typ\":\"JWT\",\"alg\":\"RS256\"}";
        String body = "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"admin\":true,\"iat\":1741943346}";

        String decodeJwtBody = JWTUtils.decodeRequestJWT(jwt, FinancialServicesConstants.JWT_BODY);
        Assert.assertEquals(decodeJwtBody.trim(), body);

        String decodeJwtHeader = JWTUtils.decodeRequestJWT(jwt, FinancialServicesConstants.JWT_HEAD);
        Assert.assertEquals(decodeJwtHeader, header);
    }
}

