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

package org.wso2.financial.services.accelerator.common.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;
import org.wso2.financial.services.accelerator.common.internal.FinancialServicesCommonDataHolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
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
     * @throws ParseException        if an error occurs while parsing the jwt
     * @throws BadJOSEException      if the jwt is invalid
     * @throws JOSEException         if an error occurs while processing the jwt
     * @throws MalformedURLException if an error occurs while creating the URL
     *                               object
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
            int connectionTimeout = Integer.parseInt(FinancialServicesConfigParser.getInstance()
                    .getJWKSConnectionTimeOut());
            int readTimeout = Integer.parseInt(FinancialServicesConfigParser.getInstance().getJWKSReadTimeOut());
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
        // Configure the JWT processor with a key selector to feed matching public RSA
        // keys sourced from the JWK set URL.
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
     * @throws InvalidKeySpecException  if the provided key is invalid
     * @throws JOSEException            if an error occurs during the signature
     *                                  validation process
     */
    @Generated(message = "Excluding from code coverage as KeyFactory does not initialize in testsuite")
    public static boolean isValidSignature(SignedJWT signedJWT, String publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException, FinancialServicesException {

        byte[] publicKeyData = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyData);
        // Example : RS256
        String algorithm = signedJWT.getHeader().getAlgorithm().getName();
        KeyFactory kf = getKeyFactory(algorithm);
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

        return StringUtils.isBlank(jwsString) ? false
                : StringUtils.countMatches(jwsString, FinancialServicesConstants.DOT_SEPARATOR) == 2;
    }

    /**
     * Parses the provided JWT string into a SignedJWT object.
     *
     * @param jwtString the JWT string to parse
     * @return the parsed SignedJWT object
     * @throws IllegalArgumentException if the provided token identifier is not a
     *                                  parsable JWT
     *
     */
    public static SignedJWT getSignedJWT(String jwtString) throws ParseException {

        if (isValidJWSFormat(jwtString)) {
            return SignedJWT.parse(jwtString);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Provided token identifier is not a parsable JWT: %s",
                        jwtString.replaceAll("[\r\n]", "")));
            }
            throw new IllegalArgumentException("Provided token identifier is not a parsable JWT.");
        }
    }

    /**
     * Checks if the given expiration time is valid based on the current system time
     * and a default time skew.
     *
     * @param defaultTimeSkew defaultTimeSkew to adjust latency issues.
     * @param expirationTime  the exp of the jwt that should be validated.
     * @return True if the expiration time is valid considering the default time
     *         skew; false otherwise.
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
     * Checks if the given "not before" time is valid based on the current system
     * time and a default time skew.
     *
     * @param defaultTimeSkew defaultTimeSkew to adjust latency issues.
     * @param notBeforeTime   nbf of the jwt that should be validated
     * @return True if the "not before" time is valid considering the default time
     *         skew; false otherwise.
     */
    public static boolean isValidNotValidBeforeTime(Date notBeforeTime, long defaultTimeSkew) {

        if (notBeforeTime != null) {
            long timeStampSkewMillis = defaultTimeSkew * 1000;
            long notBeforeTimeMillis = notBeforeTime.getTime();
            long currentTimeInMillis = System.currentTimeMillis();
            return currentTimeInMillis + timeStampSkewMillis >= notBeforeTimeMillis;
        } else {
            return false;
        }
    }

    /**
     * Returns a KeyFactory instance for the specified algorithm.
     *
     * @param algorithm the algorithm name, such as "RS256".
     * @return the KeyFactory instance.
     * @throws FinancialServicesException if the provided algorithm is not
     *                                    supported.
     * @throws NoSuchAlgorithmException   if the specified algorithm is invalid.
     */
    @Generated(message = "Excluding from code coverage as KeyFactory does not initialize in testsuite")
    private static KeyFactory getKeyFactory(String algorithm) throws FinancialServicesException,
            NoSuchAlgorithmException {

        // In here if the algorithm is directly passes (like RS256) it will generate
        // exceptions
        // hence Base algorithm should be passed (Example: RSA)
        if (algorithm.indexOf(RS) == 0) {
            return KeyFactory.getInstance(ALGORITHM_RSA);
        } else {
            throw new FinancialServicesException("Algorithm " + algorithm + " not yet supported.");
        }
    }

    /**
     * Validate a JWT signature by providing the alias in the client truststore.
     * Skipped in unit tests since @KeystoreManager cannot be mocked
     *
     * @param jwtString string value of the JWT to be validated
     * @param alias     alias in the trust store
     * @return boolean value depicting whether the signature is valid
     * @throws ConsentManagementException error with message mentioning the cause
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external call")
    public static boolean validateJWTSignatureWithPublicKey(String jwtString, String alias)
            throws ConsentManagementException {

        Certificate certificate = getCertificateFromAlias(alias);

        if (certificate == null) {
            throw new ConsentManagementException("Certificate not found for provided alias");
        }
        PublicKey publicKey = certificate.getPublicKey();

        try {
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
            return SignedJWT.parse(jwtString).verify(verifier);
        } catch (JOSEException | java.text.ParseException e) {
            log.error("Error occurred while validating JWT signature", e);
            throw new ConsentManagementException("Error occurred while validating JWT signature");
        }

    }

    /**
     * Util method to get the certificate from the trust store by alias.
     * 
     * @param alias Alias of the certificate
     * @return Certificate instance
     * @throws ConsentManagementException Error while retrieving certificate from
     *                                    truststore
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external call")
    public static Certificate getCertificateFromAlias(String alias) throws ConsentManagementException {
        try {
            KeyStore trustStore = getTrustStore();
            return trustStore.getCertificate(alias);
        } catch (KeyStoreException | ConsentManagementException e) {
            throw new ConsentManagementException("Error while retrieving certificate from truststore");
        }
    }

    /**
     * Util method to get the configured trust store by carbon config or cached
     * instance.
     *
     * @return Keystore instance of the truststore
     * @throws ConsentManagementException Error when loading truststore or carbon
     *                                    truststore config unavailable
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external call")
    public static KeyStore getTrustStore() throws ConsentManagementException {
        if (FinancialServicesCommonDataHolder.getInstance().getTrustStore() == null) {
            String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
            String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
            String trustStoreType = System.getProperty("javax.net.ssl.trustStoreType");

            if (trustStoreLocation == null || trustStorePassword == null || trustStoreType == null) {
                log.error("Either of the Trust store configs (Location, Password or Type) is not available");
                throw new ConsentManagementException("Trust store config not available");
            }

            try (InputStream keyStoreStream = new FileInputStream(trustStoreLocation)) {
                KeyStore trustStore = KeyStore.getInstance(trustStoreType); // or "PKCS12"
                trustStore.load(keyStoreStream, trustStorePassword.toCharArray());
                FinancialServicesCommonDataHolder.getInstance().setTrustStore(trustStore);
            } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
                throw new ConsentManagementException("Error while loading truststore.", e);
            }
        }
        return FinancialServicesCommonDataHolder.getInstance().getTrustStore();
    }

    /**
     * Sign a string body using the carbon default key pair.
     * Skipped in unit tests since @KeystoreManager cannot be mocked
     *
     * @param body the body that needs to be signed as a string
     * @return string value of the signed JWT
     * @throws Exception error if the tenant is invalid
     */
    public static String signJWTWithDefaultKey(String body) throws Exception {
        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(-1234);
        Key privateKey = keyStoreManager.getDefaultPrivateKey();
        return generateJWT(body, privateKey);
    }

    /**
     * Util method to generate JWT using a payload and a private key. RS256 is the
     * algorithm used
     *
     * @param payload    The payload body to be signed
     * @param privateKey The private key for the JWT to be signed with
     * @return String signed JWT
     */
    public static String generateJWT(String payload, Key privateKey) {

        if (privateKey == null || payload == null) {
            log.debug("Null value passed for payload or key. Cannot generate JWT");
            throw new FinancialServicesRuntimeException("Payload and key cannot be null");
        }

        if (!(privateKey instanceof RSAPrivateKey)) {
            throw new FinancialServicesRuntimeException("Private key should be an instance of RSAPrivateKey");
        }

        JWSSigner signer = new RSASSASigner((RSAPrivateKey) privateKey);
        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256);

        SignedJWT signedJWT = null;
        try {
            signedJWT = new SignedJWT(headerBuilder.build(), JWTClaimsSet.parse(payload));
            signedJWT.sign(signer);
        } catch (ParseException | JOSEException e) {
            throw new FinancialServicesRuntimeException("Error occurred while signing JWT");
        }
        return signedJWT.serialize();
    }
}
