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
package com.wso2.openbanking.accelerator.common.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Util class for jwt related functions.
 */
public class JWTUtils {

    private static final Log log = LogFactory.getLog(JWTUtils.class);
    private static final String RS = "RS";
    private static final String ALGORITHM_RSA = "RSA";

    /**
     * Decode request JWT.
     *
     * @param jwtToken jwt sent by the tpp
     * @param jwtPart  expected jwt part (header, body)
     * @return json object containing requested jwt part
     * @throws ParseException if an error occurs while parsing the jwt
     */
    public static JSONObject decodeRequestJWT(String jwtToken, String jwtPart) throws ParseException {

        JSONObject jsonObject = new JSONObject();

        JWSObject plainObject = JWSObject.parse(jwtToken);

        if ("head".equals(jwtPart)) {
            jsonObject = plainObject.getHeader().toJSONObject();
        } else if ("body".equals(jwtPart)) {
            jsonObject = plainObject.getPayload().toJSONObject();
        }

        return jsonObject;

    }

    /**
     * Validate the signed JWT by querying a jwks.
     *
     * @param jwtString signed json web token
     * @param jwksUri   endpoint displaying the key set for the signing certificates
     * @param algorithm the signing algorithm for jwt
     * @return true if signature is valid
     * @throws ParseException    if an error occurs while parsing the jwt
     * @throws BadJOSEException  if the jwt is invalid
     * @throws JOSEException     if an error occurs while processing the jwt
     * @throws MalformedURLException if an error occurs while creating the URL object
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external https call")
    public static boolean validateJWTSignature(String jwtString, String jwksUri, String algorithm)
            throws ParseException, BadJOSEException, JOSEException, MalformedURLException {

        int defaultConnectionTimeout = 3000;
        int defaultReadTimeout = 3000;
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWT jwt = JWTParser.parse(jwtString);
        // set the Key Selector for the jwks_uri.
        Map<String, RemoteJWKSet<SecurityContext>> jwkSourceMap = new ConcurrentHashMap<>();
        RemoteJWKSet<SecurityContext> jwkSet = jwkSourceMap.get(jwksUri);
        if (jwkSet == null) {
            int connectionTimeout = Integer.parseInt(OpenBankingConfigParser.getInstance().getJWKSConnectionTimeOut());
            int readTimeout = Integer.parseInt(OpenBankingConfigParser.getInstance().getJWKSReadTimeOut());
            int sizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT;
            if (connectionTimeout == 0 && readTimeout == 0) {
                connectionTimeout = defaultConnectionTimeout;
                readTimeout = defaultReadTimeout;
            }
            DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever(
                    connectionTimeout,
                    readTimeout,
                    sizeLimit);
            jwkSet = new RemoteJWKSet<>(new URL(jwksUri), resourceRetriever);
            jwkSourceMap.put(jwksUri, jwkSet);
        }
        // The expected JWS algorithm of the access tokens (agreed out-of-band).
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.parse(algorithm);
        //Configure the JWT processor with a key selector to feed matching public RSA keys sourced from the JWK set URL.
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, jwkSet);
        jwtProcessor.setJWSKeySelector(keySelector);
        // Process the token, set optional context parameters.
        SimpleSecurityContext securityContext = new SimpleSecurityContext();
        jwtProcessor.process((SignedJWT) jwt, securityContext);
        return true;
    }

    /**
     * Validates the signature of a given JWT against a given public key.
     *
     * @param signedJWT the signed JWT to be validated
     * @param publicKey the public key that is used for validation
     * @return true if signature is valid else false
     * @throws NoSuchAlgorithmException if the given algorithm doesn't exist
     * @throws InvalidKeySpecException if the provided key is invalid
     * @throws JOSEException if an error occurs during the signature validation process
     */
    @Generated(message = "Excluding from code coverage as KeyFactory does not initialize")
    public static boolean validateJWTSignature(SignedJWT signedJWT, String publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException, OpenBankingException {

        byte[] publicKeyData = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyData);
        String algorithm = signedJWT.getHeader().getAlgorithm().getName();
        KeyFactory kf;
        if (algorithm.indexOf(RS) == 0) {
            kf = KeyFactory.getInstance(ALGORITHM_RSA);
        } else {
            throw new OpenBankingException("Algorithm " + algorithm + "not yet supported.");
        }
        RSAPublicKey rsapublicKey = (RSAPublicKey) kf.generatePublic(spec);
        JWSVerifier verifier = new RSASSAVerifier(rsapublicKey);
        return signedJWT.verify(verifier);
    }

    /**
     * Validate legitimacy of a JWS.
     *
     * @param jwsString JWT string
     * @return true if a given jwsString adheres a valid JWS Format
     */
    public static boolean isValidJWSFormat(String jwsString) {

        return StringUtils.isBlank(jwsString) ? false :
                StringUtils.countMatches(jwsString, OpenBankingConstants.DOT_SEPARATOR) == 2;
    }

    /**
     * Parses the provided JWT string into a SignedJWT object.
     *
     * @param jwtString the JWT string to parse
     * @return the parsed SignedJWT object
     * @throws IllegalArgumentException if the provided token identifier is not a parsable JWT
     *
     */
    public static SignedJWT getSignedJWT(String jwtString) throws ParseException {

        if (isValidJWSFormat(jwtString)) {
            return SignedJWT.parse(jwtString);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Provided token identifier is not a parsable JWT: %s", jwtString));
            }
            throw new IllegalArgumentException("Provided token identifier is not a parsable JWT.");
        }
    }

    /**
     * Checks if the given expiration time is valid based on the current system time and a default time skew.
     *
     * @param defaultTimeSkew defaultTimeSkew to adjust latency issues.
     * @param expirationTime  the exp of the jwt that should be validated.
     * @return True if the expiration time is valid considering the default time skew; false otherwise.
     */
    public static boolean isValidExpiryTime(Date expirationTime, long defaultTimeSkew) {

        if (expirationTime != null) {
            long timeStampSkewMillis = defaultTimeSkew * 1000;
            long expirationTimeInMillis = expirationTime.getTime();
            long currentTimeInMillis = System.currentTimeMillis();
            return (currentTimeInMillis + timeStampSkewMillis) <= expirationTimeInMillis;
        } else {
            return false;
        }
    }

    /**
     * Checks if the given "not before" time is valid based on the current system time and a default time skew.
     *
     * @param defaultTimeSkew defaultTimeSkew to adjust latency issues.
     * @param notBeforeTime nbf of the jwt that should be validated
     * @return True if the "not before" time is valid considering the default time skew; false otherwise.
     */
    public static boolean isValidNotValidBeforeTime(Date notBeforeTime, long defaultTimeSkew)  {

        if (notBeforeTime != null) {
            long timeStampSkewMillis = defaultTimeSkew * 1000;
            long notBeforeTimeMillis = notBeforeTime.getTime();
            long currentTimeInMillis = System.currentTimeMillis();
            return currentTimeInMillis + timeStampSkewMillis >= notBeforeTimeMillis;
        } else {
            return false;
        }
    }
}

