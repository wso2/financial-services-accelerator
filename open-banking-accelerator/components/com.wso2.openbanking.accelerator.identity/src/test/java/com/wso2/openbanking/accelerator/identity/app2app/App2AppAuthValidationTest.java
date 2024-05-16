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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.cache.JTICache;
import com.wso2.openbanking.accelerator.identity.app2app.exception.JWTValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.model.DeviceVerificationToken;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Date;

/**
 * Test class for unit testing App2AppAuthValidations.
 */
@PrepareForTest({JTICache.class, JWTUtils.class})
@PowerMockIgnore({"javax.net.ssl.*", "jdk.internal.reflect.*"})
public class App2AppAuthValidationTest {

    @Test(dataProviderClass = JWTDataProvider.class,
            dataProvider = "JWTProvider")
    public void validationTest(String jwtString, String publicKey) throws ParseException,
            JWTValidationException, JOSEException, NoSuchAlgorithmException, InvalidKeySpecException {

        PowerMockito.mockStatic(JTICache.class);
        PowerMockito.mockStatic(JWTUtils.class);
        Mockito.when(JTICache.getJtiDataFromCache(Mockito.anyString())).thenReturn(null);
        Mockito.when(JWTUtils.validateJWTSignature(Mockito.any(SignedJWT.class), Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Mockito.when(JWTUtils.validateExpiryTime(Mockito.any(Date.class), Mockito.any(long.class)))
                .thenReturn(true);
        Mockito.when(JWTUtils.validateNotValidBefore(Mockito.any(Date.class), Mockito.any(long.class)))
                .thenReturn(true);
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        DeviceVerificationToken deviceVerificationToken = new DeviceVerificationToken(signedJWT);
        deviceVerificationToken.setPublicKey(publicKey);
        deviceVerificationToken.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
        App2AppAuthUtils.validateToken(deviceVerificationToken);

    }

    @Test(expectedExceptions = JWTValidationException.class,
            dataProviderClass = JWTDataProvider.class,
            dataProvider = "JWTProvider")
    public void validationTestJTIReplayed(String jwtString, String publicKey) throws ParseException,
            JWTValidationException, JOSEException, NoSuchAlgorithmException, InvalidKeySpecException {

        PowerMockito.mockStatic(JTICache.class);
        PowerMockito.mockStatic(JWTUtils.class);
        Mockito.when(JTICache.getJtiDataFromCache(Mockito.anyString())).thenReturn("NotNullJTI");
        Mockito.when(JWTUtils.validateJWTSignature(Mockito.any(SignedJWT.class), Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Mockito.when(JWTUtils.validateExpiryTime(Mockito.any(Date.class), Mockito.any(long.class)))
                .thenReturn(true);
        Mockito.when(JWTUtils.validateNotValidBefore(Mockito.any(Date.class), Mockito.any(long.class)))
                .thenReturn(true);
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        DeviceVerificationToken deviceVerificationToken = new DeviceVerificationToken(signedJWT);
        deviceVerificationToken.setPublicKey(publicKey);
        deviceVerificationToken.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
        App2AppAuthUtils.validateToken(deviceVerificationToken);

    }

    @Test(expectedExceptions = JWTValidationException.class,
            dataProviderClass = JWTDataProvider.class,
            dataProvider = "JWTProvider")
    public void validationTestJWTExpired(String jwtString, String publicKey) throws ParseException,
            JWTValidationException, JOSEException, NoSuchAlgorithmException, InvalidKeySpecException {

        PowerMockito.mockStatic(JTICache.class);
        PowerMockito.mockStatic(JWTUtils.class);
        Mockito.when(JTICache.getJtiDataFromCache(Mockito.anyString())).thenReturn(null);
        Mockito.when(JWTUtils.validateJWTSignature(Mockito.any(SignedJWT.class), Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Mockito.when(JWTUtils.validateExpiryTime(Mockito.any(Date.class), Mockito.any(long.class)))
                .thenReturn(false);
        Mockito.when(JWTUtils.validateNotValidBefore(Mockito.any(Date.class), Mockito.any(long.class)))
                .thenReturn(true);
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        DeviceVerificationToken deviceVerificationToken = new DeviceVerificationToken(signedJWT);
        deviceVerificationToken.setPublicKey(publicKey);
        deviceVerificationToken.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
        App2AppAuthUtils.validateToken(deviceVerificationToken);

    }

    @Test(expectedExceptions = JWTValidationException.class,
            dataProviderClass = JWTDataProvider.class,
            dataProvider = "JWTProvider")
    public void validationTestJWTNotActive(String jwtString, String publicKey) throws ParseException,
            JWTValidationException, JOSEException, NoSuchAlgorithmException, InvalidKeySpecException {

        PowerMockito.mockStatic(JTICache.class);
        PowerMockito.mockStatic(JWTUtils.class);
        Mockito.when(JTICache.getJtiDataFromCache(Mockito.anyString())).thenReturn(null);
        Mockito.when(JWTUtils.validateJWTSignature(Mockito.any(SignedJWT.class), Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Mockito.when(JWTUtils.validateExpiryTime(Mockito.any(Date.class), Mockito.any(long.class)))
                .thenReturn(true);
        Mockito.when(JWTUtils.validateNotValidBefore(Mockito.any(Date.class), Mockito.any(long.class)))
                .thenReturn(false);
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        DeviceVerificationToken deviceVerificationToken = new DeviceVerificationToken(signedJWT);
        deviceVerificationToken.setPublicKey(publicKey);
        deviceVerificationToken.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
        App2AppAuthUtils.validateToken(deviceVerificationToken);

    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
