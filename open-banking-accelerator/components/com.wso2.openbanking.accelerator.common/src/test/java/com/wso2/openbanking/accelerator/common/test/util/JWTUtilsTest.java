package com.wso2.openbanking.accelerator.common.test.util;

import com.wso2.openbanking.accelerator.common.test.util.testutils.JWTUtilsDataProvider;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Date;

/**
 * Test class for Unit Testing JWTUtils.
 */
public class JWTUtilsTest {

    @Test(dataProviderClass = JWTUtilsDataProvider.class, dataProvider = "jwtStrings")
    public void testIsJWT(String jwtString, boolean expected) {

        Assert.assertEquals(JWTUtils.isJWT(jwtString), expected);
    }

    @Test(dataProviderClass = JWTUtilsDataProvider.class, dataProvider = "validParsableJwtStrings")
    public void testGetSignedJWT(String jwtString) throws ParseException {

        Assert.assertNotNull(JWTUtils.getSignedJWT(jwtString));
    }

    @Test(expectedExceptions = ParseException.class,
            dataProviderClass = JWTUtilsDataProvider.class, dataProvider = "validNotParsableJwtStrings")
    public void testGetSignedJWTWIthNotParsableJWT(String jwtString) throws ParseException {

        JWTUtils.getSignedJWT(jwtString);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            dataProviderClass = JWTUtilsDataProvider.class, dataProvider = "notValidJwtStrings")
    public void testGetSignedJWTWIthNotValidJWT(String jwtString) throws ParseException {

        JWTUtils.getSignedJWT(jwtString);
    }

    @Test(dataProviderClass = JWTUtilsDataProvider.class, dataProvider = "expiryTimeProvider")
    public void testValidExpirationTime(Date time, long timeSkew, boolean expected) {

        Assert.assertEquals(JWTUtils.validateExpiryTime(time, timeSkew), expected);
    }

    @Test(dataProviderClass = JWTUtilsDataProvider.class, dataProvider = "nbfProvider")
    public void testValidNotValidBefore(Date time, long timeSkew, boolean expected) {

        Assert.assertEquals(JWTUtils.validateNotValidBefore(time, timeSkew), expected);
    }
}
