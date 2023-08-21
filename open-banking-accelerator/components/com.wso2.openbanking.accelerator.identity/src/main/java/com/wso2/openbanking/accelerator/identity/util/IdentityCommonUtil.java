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

package com.wso2.openbanking.accelerator.identity.util;

import com.google.common.base.Charsets;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCacheKey;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility Class for Identity Open Banking.
 */
public class IdentityCommonUtil {

    private static final Log log = LogFactory.getLog(IdentityCommonUtil.class);
    private static IdentityCache identityCache;

    /**
     * Get the configured certificate header name.
     *
     * @return value of the cert header name configuration
     */
    public static String getMTLSAuthHeader() {

        return Optional.ofNullable(IdentityUtil.getProperty(IdentityCommonConstants.MTLS_AUTH_HEADER))
                .orElse("CONFIG_NOT_FOUND");
    }

    /**
     * Remove the internal scopes from the space delimited list of authorized scopes.
     *
     * @param scopes Authorized scopes of the token
     * @return scopes by removing the internal scopes
     */
    public static String[] removeInternalScopes(String[] scopes) {

        String consentIdClaim = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                .get(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();

        if (scopes != null && scopes.length > 0) {
            List<String> scopesList = new LinkedList<>(Arrays.asList(scopes));
            scopesList.removeIf(s -> s.startsWith(consentIdClaim));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.OB_PREFIX));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.TIME_PREFIX));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.CERT_PREFIX));
            return scopesList.toArray(new String[scopesList.size()]);
        }
        return scopes;
    }

    /**
     * Cache regulatory property if exists.
     *
     * @param clientId clientId ClientId of the application
     * @return the regulatory property from cache if exists or from sp metadata
     * @throws OpenBankingException
     */
    @Generated(message = "Excluding from code coverage since it requires a cache initialization/service call")
    public static synchronized boolean getRegulatoryFromSPMetaData(String clientId) throws OpenBankingException {

        if (StringUtils.isNotEmpty(clientId)) {
            // Skip My account and Console service providers with non opaque clientIds
            if (clientId.equalsIgnoreCase("CONSOLE") ||
                    clientId.equalsIgnoreCase("MY_ACCOUNT")) {
                return false;
            }

            if (identityCache == null) {
                log.debug("Creating new Identity cache");
                identityCache = new IdentityCache();
            }

            IdentityCacheKey identityCacheKey = IdentityCacheKey.of(clientId
                    .concat("_").concat(OpenBankingConstants.REGULATORY));
            Object regulatoryProperty = null;

            regulatoryProperty = identityCache.getFromCacheOrRetrieve(identityCacheKey,
                    () -> new IdentityCommonHelper().getAppPropertyFromSPMetaData(clientId,
                            IdentityCommonConstants.REGULATORY_COMPLIANCE));

            if (regulatoryProperty != null) {
                return Boolean.parseBoolean(regulatoryProperty.toString());
            } else {
                throw new OpenBankingException("Unable to retrieve regulatory property from sp metadata");
            }
        } else {
            throw new OpenBankingException(IdentityCommonConstants.CLIENT_ID_ERROR);
        }
    }

    public static ServiceProviderProperty getServiceProviderProperty(String spPropertyName, String spPropertyValue) {

        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
        serviceProviderProperty.setValue(spPropertyValue);
        serviceProviderProperty.setName(spPropertyName);
        serviceProviderProperty.setDisplayName(spPropertyName);
        return serviceProviderProperty;
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
     * Validate a JWT signature by providing the alias in the client truststore.
     * Skipped in unit tests since @KeystoreManager cannot be mocked
     *
     * @param jwtString string value of the JWT to be validated
     * @param alias     alias in the trust store
     * @return boolean value depicting whether the signature is valid
     * @throws OpenBankingException error with message mentioning the cause
     */
    public static boolean validateJWTSignatureWithPublicKey(String jwtString, String alias)
            throws OpenBankingException {

        Certificate certificate;
        try {
            KeyStore trustStore = getTrustStore();
            certificate = trustStore.getCertificate(alias);
        } catch (Exception e) {
            throw new OpenBankingException("Error while retrieving certificate from truststore");
        }

        if (certificate == null) {
            throw new OpenBankingException("Certificate not found for provided alias");
        }
        PublicKey publicKey = certificate.getPublicKey();

        try {
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
            return SignedJWT.parse(jwtString).verify(verifier);
        } catch (JOSEException | ParseException e) {
            throw new OpenBankingException("Error occurred while validating JWT signature");
        }

    }

    /**
     * Util method to get the configured trust store by carbon config or cached instance.
     *
     * @return Keystore instance of the truststore
     * @throws Exception Error when loading truststore or carbon truststore config unavailable
     */
    public static KeyStore getTrustStore() throws Exception {
        if (IdentityExtensionsDataHolder.getInstance().getTrustStore() == null) {
            String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
            String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
            String trustStoreType = System.getProperty("javax.net.ssl.trustStoreType");

            if (trustStoreLocation == null || trustStorePassword == null || trustStoreType == null) {
                throw new Exception("Trust store config not available");
            }

            try (InputStream keyStoreStream = new FileInputStream(trustStoreLocation)) {
                KeyStore keyStore = KeyStore.getInstance(trustStoreType); // or "PKCS12"
                keyStore.load(keyStoreStream, trustStorePassword.toCharArray());
                IdentityExtensionsDataHolder.getInstance().setTrustStore(keyStore);
            } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
                throw new Exception("Error while loading truststore.", e);
            }
        }
        return IdentityExtensionsDataHolder.getInstance().getTrustStore();
    }

    /**
     * Util method to generate JWT using a payload and a private key. RS256 is the algorithm used
     *
     * @param payload    The payload body to be signed
     * @param privateKey The private key for the JWT to be signed with
     * @return String signed JWT
     */
    public static String generateJWT(String payload, Key privateKey) {

        if (privateKey == null || payload == null) {
            log.debug("Null value passed for payload or key. Cannot generate JWT");
            throw new OpenBankingRuntimeException("Payload and key cannot be null");
        }

        if (!(privateKey instanceof RSAPrivateKey)) {
            throw new OpenBankingRuntimeException("Private key should be an instance of RSAPrivateKey");
        }

        JWSSigner signer = new RSASSASigner((RSAPrivateKey) privateKey);
        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256);

        SignedJWT signedJWT = null;
        try {
            signedJWT = new SignedJWT(headerBuilder.build(), JWTClaimsSet.parse(payload));
            signedJWT.sign(signer);
        } catch (ParseException | JOSEException e) {
            throw new OpenBankingRuntimeException("Error occurred while signing JWT");
        }
        return signedJWT.serialize();
    }

    /**
     * Util method to generate SP meta data using service provider.
     *
     * @param serviceProvider The service provider
     * @return SP meta data as a Map
     */
    public static Map<String, Object> getSpMetaData(ServiceProvider serviceProvider) {

        Map<String, String> originalData = Arrays.stream(serviceProvider.getSpProperties())
                .collect(Collectors.toMap(ServiceProviderProperty::getName, ServiceProviderProperty::getValue));

        Map<String, Object> spMetaDataMap = new HashMap<>();

        for (Map.Entry<String, String> data : originalData.entrySet()) {

            if (data.getValue().contains(DCRCommonConstants.ARRAY_ELEMENT_SEPERATOR)) {
                ArrayList<String> dataList = new ArrayList<>(Arrays.asList(data.getValue()
                        .split(DCRCommonConstants.ARRAY_ELEMENT_SEPERATOR)));
                spMetaDataMap.put(data.getKey(), dataList);
            } else {
                spMetaDataMap.put(data.getKey(), data.getValue());
            }
        }
        return spMetaDataMap;
    }

    /**
     * Method to obtain Hash Value for a given String, default algorithm SHA256withRSA.
     *
     * @param value String value that required to be Hashed
     * @return Hashed String
     * @throws IdentityOAuth2Exception
     */
    public static String getHashValue(String value, String digestAlgorithm) throws IdentityOAuth2Exception {

        if (digestAlgorithm == null) {
            JWSAlgorithm digAlg = OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(
                    OAuthServerConfiguration.getInstance().getIdTokenSignatureAlgorithm());
            digestAlgorithm = OAuth2Util.mapDigestAlgorithm(digAlg);
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IdentityOAuth2Exception("Error creating the hash value. Invalid Digest Algorithm: " +
                    digestAlgorithm);
        }
        //generating hash value
        md.update(value.getBytes(Charsets.UTF_8));
        byte[] digest = md.digest();
        int leftHalfBytes = digest.length / 2;
        byte[] leftmost = new byte[leftHalfBytes];
        System.arraycopy(digest, 0, leftmost, 0, leftHalfBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(leftmost)
                .replace("\n", "").replace("\r", "");
    }

    /**
     * This method returns the configuration value on whether the JWT payload validation needs to be performed in the
     * consent validation endpoint.
     * @return config value
     */
    public static boolean getConsentJWTPayloadValidatorConfigEnabled() {
        return Boolean.parseBoolean(String.valueOf(IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                .getOrDefault(IdentityCommonConstants.CONSENT_JWT_PAYLOAD_VALIDATION, true)));
    }

    /**
     * This method returns the configured JWK URI value of the transport certificate.
     * @return
     */
    public static String getJWKURITransportCert() {
        return String.valueOf(IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                .getOrDefault(IdentityCommonConstants.TLS_CERT_JWKS, IdentityCommonConstants.DEFAULT_JWKS_URI));
    }

    /**
     * This method will return the configured DCR scope.
     * @return
     */
    public static String getDCRScope() {
        return String.valueOf(IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                .getOrDefault(IdentityCommonConstants.DCR_SCOPE, IdentityCommonConstants.DCR_INTERNAL_SCOPE));
    }

    public static Boolean getDCRModifyResponseConfig() {
        return Boolean.parseBoolean(String.valueOf(IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                .getOrDefault(IdentityCommonConstants.DCR_MODIFY_RESPONSE, "false")));
    }

    /**
     * Retrieve certificate from servlet request attribute.
     * @param certObject certificate Object.
     * @return X509Certificate certificate.
     */
    public static X509Certificate getCertificateFromAttribute(Object certObject) {

        if (certObject instanceof X509Certificate[]) {
            X509Certificate[] cert = (X509Certificate[]) certObject;
            return cert[0];
        } else if (certObject instanceof X509Certificate) {
            return (X509Certificate) certObject;
        }
        return null;
    }

    /**
     * Method to decode request object and retrieve values.
     *
     * @param request HTTP Servlet request.
     * @param key key to retrieve.
     * @return value.
     */
    public static String decodeRequestObjectAndGetKey(HttpServletRequest request, String key)
            throws OAuthProblemException {

        if (request.getParameterMap().containsKey(IdentityCommonConstants.REQUEST_URI) &&
                request.getParameter(IdentityCommonConstants.REQUEST_URI) != null) {

            // Consider as PAR request
            String[] requestUri = request.getParameter(IdentityCommonConstants.REQUEST_URI).split(":");
            String requestUriRef = requestUri[requestUri.length - 1];
            SessionDataCacheEntry valueFromCache = SessionDataCache.getInstance()
                    .getValueFromCache(new SessionDataCacheKey(requestUriRef));
            if (valueFromCache != null) {
                String essentialClaims = valueFromCache.getoAuth2Parameters().getEssentialClaims();
                if (essentialClaims != null) {
                    String[] essentialClaimsWithExpireTime = essentialClaims.split(":");
                    essentialClaims = essentialClaimsWithExpireTime[0];
                    essentialClaims = essentialClaims.split("\\.")[1];
                    byte[] requestObject;
                    try {
                        requestObject = Base64.getDecoder().decode(essentialClaims);
                    } catch (IllegalArgumentException e) {

                        // Decode if the requestObject is base64-url encoded.
                        requestObject = Base64.getUrlDecoder().decode(essentialClaims);
                    }
                    org.json.JSONObject
                            requestObjectVal =
                            new org.json.JSONObject(new String(requestObject, StandardCharsets.UTF_8));
                    return requestObjectVal.has(key) ? requestObjectVal.getString(key) : null;
                }
            } else {
                throw OAuthProblemException.error("invalid_request_uri")
                        .description("Provided request URI is not valid");
            }
        }
        return null;

    }

    public static OAuthProblemException handleOAuthProblemException(String errorCode, String message, String state) {

        return OAuthProblemException.error(errorCode).description(message).state(state);
    }

}
