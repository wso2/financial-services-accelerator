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

package com.wso2.openbanking.accelerator.common.test.util;

import com.wso2.openbanking.accelerator.common.test.util.testutils.JWTUtilsTestDataProvider;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

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
}

