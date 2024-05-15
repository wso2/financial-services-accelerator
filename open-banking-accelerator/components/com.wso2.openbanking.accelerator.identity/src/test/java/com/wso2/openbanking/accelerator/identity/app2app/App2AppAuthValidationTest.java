package com.wso2.openbanking.accelerator.identity.app2app;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.cache.JTICache;
import com.wso2.openbanking.accelerator.identity.app2app.exception.JWTValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.model.AppAuthValidationJWT;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Date;

import static org.powermock.api.mockito.PowerMockito.mockStatic;


@PrepareForTest({JTICache.class, JWTUtils.class})
@PowerMockIgnore({"javax.net.ssl.*", "jdk.internal.reflect.*"})
public class App2AppAuthValidationTest {

    @Test(dataProviderClass = JWTDataProvider.class,
            dataProvider = "JWTProvider")
    public void validationTest(String jwtString, String publicKey) throws ParseException,
            JWTValidationException, JOSEException, NoSuchAlgorithmException, InvalidKeySpecException {
        mockStatic(JTICache.class);
        mockStatic(JWTUtils.class);
        Mockito.when(JTICache.getJtiDataFromCache(Mockito.anyString())).thenReturn(null);
        Mockito.when(JWTUtils.validateJWTSignature(Mockito.any(SignedJWT.class),Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Mockito.when(JWTUtils.validateExpiryTime(Mockito.any(Date.class),Mockito.any(long.class)))
                .thenReturn(true);
        Mockito.when(JWTUtils.validateNotValidBefore(Mockito.any(Date.class),Mockito.any(long.class)))
                .thenReturn(true);
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        AppAuthValidationJWT appAuthValidationJWT = new AppAuthValidationJWT(signedJWT);
        appAuthValidationJWT.setPublicKey(publicKey);
        appAuthValidationJWT.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
        App2AppAuthUtils.validateSecret(appAuthValidationJWT);
    }

    @Test(expectedExceptions = JWTValidationException.class,
            dataProviderClass = JWTDataProvider.class,
            dataProvider = "JWTProvider")
    public void validationTestJTIReplayed(String jwtString, String publicKey) throws ParseException,
            JWTValidationException,JOSEException, NoSuchAlgorithmException, InvalidKeySpecException {
        mockStatic(JTICache.class);
        mockStatic(JWTUtils.class);
        Mockito.when(JTICache.getJtiDataFromCache(Mockito.anyString())).thenReturn("NotNullJTI");
        Mockito.when(JWTUtils.validateJWTSignature(Mockito.any(SignedJWT.class),Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Mockito.when(JWTUtils.validateExpiryTime(Mockito.any(Date.class),Mockito.any(long.class)))
                .thenReturn(true);
        Mockito.when(JWTUtils.validateNotValidBefore(Mockito.any(Date.class),Mockito.any(long.class)))
                .thenReturn(true);
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        AppAuthValidationJWT appAuthValidationJWT = new AppAuthValidationJWT(signedJWT);
        appAuthValidationJWT.setPublicKey(publicKey);
        appAuthValidationJWT.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
        App2AppAuthUtils.validateSecret(appAuthValidationJWT);
    }

    @Test(expectedExceptions = JWTValidationException.class,
            dataProviderClass = JWTDataProvider.class,
            dataProvider = "JWTProvider")
    public void validationTestJWTExpired(String jwtString, String publicKey) throws ParseException,
            JWTValidationException,JOSEException, NoSuchAlgorithmException, InvalidKeySpecException {
        mockStatic(JTICache.class);
        mockStatic(JWTUtils.class);
        Mockito.when(JTICache.getJtiDataFromCache(Mockito.anyString())).thenReturn(null);
        Mockito.when(JWTUtils.validateJWTSignature(Mockito.any(SignedJWT.class),Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Mockito.when(JWTUtils.validateExpiryTime(Mockito.any(Date.class),Mockito.any(long.class)))
                .thenReturn(false);
        Mockito.when(JWTUtils.validateNotValidBefore(Mockito.any(Date.class),Mockito.any(long.class)))
                .thenReturn(true);
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        AppAuthValidationJWT appAuthValidationJWT = new AppAuthValidationJWT(signedJWT);
        appAuthValidationJWT.setPublicKey(publicKey);
        appAuthValidationJWT.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
        App2AppAuthUtils.validateSecret(appAuthValidationJWT);
    }

    @Test(expectedExceptions = JWTValidationException.class,
            dataProviderClass = JWTDataProvider.class,
            dataProvider = "JWTProvider")
    public void validationTestJWTNotActive(String jwtString, String publicKey) throws ParseException,
            JWTValidationException,JOSEException, NoSuchAlgorithmException, InvalidKeySpecException {
        mockStatic(JTICache.class);
        mockStatic(JWTUtils.class);
        Mockito.when(JTICache.getJtiDataFromCache(Mockito.anyString())).thenReturn(null);
        Mockito.when(JWTUtils.validateJWTSignature(Mockito.any(SignedJWT.class),Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Mockito.when(JWTUtils.validateExpiryTime(Mockito.any(Date.class),Mockito.any(long.class)))
                .thenReturn(true);
        Mockito.when(JWTUtils.validateNotValidBefore(Mockito.any(Date.class),Mockito.any(long.class)))
                .thenReturn(false);
        SignedJWT signedJWT = SignedJWT.parse(jwtString);
        AppAuthValidationJWT appAuthValidationJWT = new AppAuthValidationJWT(signedJWT);
        appAuthValidationJWT.setPublicKey(publicKey);
        appAuthValidationJWT.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
        App2AppAuthUtils.validateSecret(appAuthValidationJWT);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
