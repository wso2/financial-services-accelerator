package com.wso2.openbanking.accelerator.identity.app2app;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authenticator.push.common.exception.PushAuthTokenValidationException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

public class App2AppAuthUtil {

    private static final String DOT_SEPARATOR = ".";
    private static final long DEFAULT_TIME_SKEW_IN_SECONDS = 300L;
    private static final Log log = LogFactory.getLog(App2AppAuthUtil.class);

    /**
     * Validate legitimacy of JWT.
     *
     * @param jwtString JWT string
     */
    public static boolean isJWT(String jwtString) {
        if (jwtString == null){
            return false;
        }
        if (StringUtils.isBlank(jwtString)) {
            return false;
        }
        if (StringUtils.countMatches(jwtString, DOT_SEPARATOR) != 2) {
            return false;
        }
        try {
            JWTParser.parse(jwtString);
            return true;
        } catch (ParseException e) {
            if (log.isDebugEnabled()) {
                log.debug("Provided token identifier is not a parsable JWT.", e);
            }
            return false;
        }
    }

    public static SignedJWT getSignedJWT(String jwtString) throws ParseException {
        if (isJWT(jwtString)){
            return SignedJWT.parse(jwtString);
        }else{
            throw new IllegalArgumentException("Required parameter jwtString is invalid");
        }
    }

    public static JWTClaimsSet getJWTClaimsSet(String jwtString) throws ParseException{
        return getJWTClaimsSet(getSignedJWT(jwtString));
    }

    public static JWTClaimsSet getJWTClaimsSet(SignedJWT signedJWT) throws ParseException{
        return signedJWT.getJWTClaimsSet();
    }

    public static <T> T getClaim(String jwtString, String claim) throws ParseException{
        return getClaim(getJWTClaimsSet(jwtString),claim);
    }

    public static <T> T getClaim(JWTClaimsSet jwtClaimsSet ,String claim){
        Object claimObj = jwtClaimsSet.getClaim(claim);
        return (T) claimObj;
    }

    public static <T> T getClaim(SignedJWT signedJWT ,String claim) throws ParseException {
        return getClaim(getJWTClaimsSet(signedJWT),claim);
    }

    public static boolean validateJWT(SignedJWT signedJWT, String publicKey, String algorithm)
            throws PushAuthTokenValidationException{
        try {
            if (!validateSignature(signedJWT,publicKey,algorithm)){
                return false;
            }
            if (!validateExpiryTime(signedJWT)){
                return false;
            }
            if (!validateNotValidBefore(signedJWT)){
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            throw new PushAuthTokenValidationException("Error occurred while validating JWT. No such algorithm "
                                                        +algorithm,e);
        } catch (InvalidKeySpecException e) {
            throw new PushAuthTokenValidationException("Error occurred while validating JWT. Invalid Key Space",e);
        } catch (JOSEException e) {
            throw new PushAuthTokenValidationException("Error occurred while verifying JWT.",e);
        } catch (ParseException e) {
            throw new PushAuthTokenValidationException("Error occurred while parsing JWT.",e);
        }
    }

    public static boolean validateJWT(String jwtString,String publicKey, String algorithm) throws PushAuthTokenValidationException {
        SignedJWT signedJWT = null;
        try {
            signedJWT = getSignedJWT(jwtString);
        } catch (ParseException e) {
            throw new PushAuthTokenValidationException("Error occurred while parsing JWT.",e);
        }
        return validateJWT(signedJWT, publicKey, algorithm);
    }

    public static boolean validateSignature(SignedJWT signedJWT, String publicKey, String algorithm) throws
            NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        byte[] publicKeyData = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyData);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        RSAPublicKey rsapublicKey = (RSAPublicKey) kf.generatePublic(spec);
        JWSVerifier verifier = new RSASSAVerifier(rsapublicKey);
        return signedJWT.verify(verifier);
    }

    public static boolean validateSignature(String jwtString, String publicKey, String algorithm) throws
            NoSuchAlgorithmException, InvalidKeySpecException, JOSEException, ParseException {
        SignedJWT signedJWT = getSignedJWT(jwtString);
        return validateSignature(signedJWT,publicKey,algorithm);
    }

    public static boolean validateExpiryTime(JWTClaimsSet jwtClaimsSet){
        Date expirationTime = jwtClaimsSet.getExpirationTime();
        if (expirationTime != null) {
            long timeStampSkewMillis = DEFAULT_TIME_SKEW_IN_SECONDS * 1000;
            long expirationTimeInMillis = expirationTime.getTime();
            long currentTimeInMillis = System.currentTimeMillis();
            return (currentTimeInMillis + timeStampSkewMillis) <= expirationTimeInMillis;
        }else{
            return false;
        }
    }
    public static boolean validateExpiryTime(SignedJWT signedJWT) throws ParseException {
        JWTClaimsSet jwtClaimsSet = getJWTClaimsSet(signedJWT);
        return validateExpiryTime(jwtClaimsSet);
    }

    public static boolean validateExpiryTime(String jwtString) throws ParseException {
        SignedJWT signedJWT = getSignedJWT(jwtString);
        return validateExpiryTime(signedJWT);
    }

    public static boolean validateNotValidBefore(JWTClaimsSet jwtClaimsSet){
        Date notBeforeTime = jwtClaimsSet.getNotBeforeTime();
        if (notBeforeTime != null) {
            long timeStampSkewMillis = DEFAULT_TIME_SKEW_IN_SECONDS * 1000;
            long notBeforeTimeMillis = notBeforeTime.getTime();
            long currentTimeInMillis = System.currentTimeMillis();
            return currentTimeInMillis + timeStampSkewMillis >= notBeforeTimeMillis;

        } else {
            return false;
        }
    }
    public static boolean validateNotValidBefore(SignedJWT signedJWT) throws ParseException {
        JWTClaimsSet jwtClaimsSet = getJWTClaimsSet(signedJWT);
        return validateNotValidBefore(jwtClaimsSet);
    }

    public static boolean validateNotValidBefore(String jwtString) throws ParseException {
        SignedJWT signedJWT = getSignedJWT(jwtString);
        return validateNotValidBefore(signedJWT);
    }

}
