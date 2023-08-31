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

package com.wso2.openbanking.accelerator.demosite.endpoint.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.demosite.endpoint.model.JWTGeneratorEndpointErrorResponse;
import com.wso2.openbanking.accelerator.demosite.endpoint.model.PayloadData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

/**
 * Utils class for the demo-site JWTGenerator endpoint
 */
public class GeneratorUtil {

    private static final Log log = LogFactory.getLog(GeneratorUtil.class);
    private static final JWSAlgorithm DEFAULT_ALGORITHM = JWSAlgorithm.PS256;
    private static String certPath = "";
    private static String keyPath = "";
    private static String serverDomain = "";
    private static String port = "";
    private static boolean isExternalLink = false;
    private static String kid = "";
    private static Certificate certificate = null;
    private static JWSSigner signer = null;

    /**
     * Generate the JWT required for token assertion, request object and DCR payload
     *
     * @param requestData Request data object which contains request parameters
     * @return The signed JWT
     * @throws OpenBankingException
     */
    public static String generateJWT(PayloadData requestData) throws OpenBankingException {

        JWSHeader header;
        Payload payload;
        String appName = null;

        if (certificate == null || signer == null || kid.equals("") || serverDomain.equals("") || port.equals("")) {
            try {
                updateConfigurations();
            } catch (OpenBankingException e) {
                String error = "Error in updating certificates";
                log.error(error, e);
                throw new OpenBankingException(error, e);
            }
        }

        if (requestData.getType().toLowerCase(Locale.ENGLISH).contains("dcr")) {
            // For DCR app registrations the software ID is set to newApp from the frontend
            if (requestData.getSoftwareId().equals("newApp")) {
                appName = UUID.randomUUID().toString();
            } else {
                appName = requestData.getSoftwareId();
            }
            requestData.setSsa(generateSSA(requestData, appName));
        }
        header = generateHeader(requestData);
        payload = generatePayload(requestData, appName);

        try {
            return signJWT(header, payload);
        } catch (OpenBankingException e) {
            String error = "Error while signing JWT/JWS";
            log.error(error, e);
            throw new OpenBankingException(error, e);
        }
    }

    /**
     * Download and update the stored certificates
     *
     * @throws OpenBankingException
     */
    public static String updateConfigurations() throws OpenBankingException {
        try {
            InputStream configurations = GeneratorUtil.class.getClassLoader()
                    .getResourceAsStream("configurations.properties");
            Properties configurationProperties = new Properties();
            configurationProperties.load(configurations);
            certPath = configurationProperties.getProperty("CertificateConfigs.CertUrl");
            keyPath = configurationProperties.getProperty("CertificateConfigs.KeyUrl");
            isExternalLink = Boolean.parseBoolean(
                    configurationProperties.getProperty("CertificateConfigs.IsExternalLink"));
            serverDomain = configurationProperties.getProperty("PayloadConfigs.IamDomain");
            port = configurationProperties.getProperty("PayloadConfigs.Port");
        } catch (IOException e) {
            String error = "Error occurred while reading the configurations";
            log.error(error, e);
            throw new OpenBankingException(error, e);
        }

        try {
            certificate = getPublicSigningCert();
            signer = new RSASSASigner((PrivateKey) getSigningKey());
            try {
                kid = getThumbPrint(certificate);
                log.info("The certificates were updated successfully");
                return "Certificates updated successfully";
            } catch (OpenBankingException e) {
                String error = "Error when getting thumbprint of primary public cert";
                log.error(error, e);
                throw new OpenBankingException(error, e);
            }
        } catch (OpenBankingException e) {
            String error = "Error when retrieving primary public cert";
            log.error(error, e);
            throw new OpenBankingException(error, e);
        }
    }

    /**
     * Generate SHA-1 DER Thumbprint.
     *
     * @param certificate
     * @return Thumbprint
     * @throws OpenBankingException
     */
    private static String getThumbPrint(Certificate certificate) throws OpenBankingException {

        try {
            X509Certificate x509cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(certificate.getEncoded()));
            return RSAKey.parse(x509cert).computeThumbprint("SHA-1").toString();
        } catch (CertificateException | JOSEException e) {
            String error = "Error occurred while generating SHA-1 JWK thumbprint";
            log.error(error, e);
            throw new OpenBankingException(error, e);
        }
    }

    /**
     * Generate the payload of the JWT
     *
     * @param requestData Request data object which contains request parameters
     * @param appName Name of the application to be created
     * @return The payload of the JWT
     */
    private static Payload generatePayload(PayloadData requestData, String appName) {

        long initiationTime = Instant.now().getEpochSecond();
        long expirationTime = initiationTime + 3600;
        long jtiValue = initiationTime + 10;
        String apiName = requestData.getApiName().toLowerCase(Locale.ENGLISH);
        String payloadString = "";

        if (apiName.contains("authorize")) {
            payloadString = "{\n" +
                    " \"max_age\": 86400,\n" +
                    " \"aud\": \"" + serverDomain + ":" + port + "/oauth2/token\",\n" +
                    " \"scope\": \"" + requestData.getScopes() + "\",\n" +
                    " \"iss\": \"" + requestData.getClientId() + "\",\n" +
                    " \"claims\": {\n" +
                    "   \"id_token\": {\n" +
                    "     \"acr\": {\n" +
                    "       \"values\": [\n" +
                    "         \"urn:openbanking:psd2:sca\",\n" +
                    "         \"urn:openbanking:psd2:ca\"\n" +
                    "       ],\n" +
                    "       \"essential\": true\n" +
                    "     },\n" +
                    "     \"openbanking_intent_id\": {\n" +
                    "       \"value\": \"" + requestData.getConsentId() + "\",\n" +
                    "       \"essential\": true\n" +
                    "     }\n" +
                    "   },\n" +
                    "   \"userinfo\": {\n" +
                    "     \"openbanking_intent_id\": {\n" +
                    "       \"value\": \"" + requestData.getConsentId() + "\",\n" +
                    "       \"essential\": true\n" +
                    "     }\n" +
                    "   }\n" +
                    " },\n" +
                    " \"response_type\": \"code id_token\",\n" +
                    " \"redirect_uri\": \"" + requestData.getRedirectUri() + "\",\n" +
                    " \"state\": \"YWlzcDozMTQ2\",\n" +
                    " \"exp\": " + expirationTime + ",\n" +
                    " \"nonce\": \"n-0S6_WzA2M0000025\",\n" +
                    " \"client_id\": \"" + requestData.getClientId() + "\"\n" +
                    "}";
        } else if (apiName.contains("token")) {
            payloadString = "{\n" +
                    "  \"iss\": \"" + requestData.getClientId() + "\",\n" +
                    "  \"sub\": \"" + requestData.getClientId() + "\",\n" +
                    "  \"exp\": " + expirationTime + ",\n" +
                    "  \"iat\": " + initiationTime + ",\n" +
                    "  \"jti\": \"" + jtiValue + "\",\n" +
                    " \"aud\": \"" + serverDomain + ":" + port + "/oauth2/token\"\n" +
                    "}";
        } else if (apiName.contains("dynamic")) {
            payloadString = "{\n" +
                    "  \"iss\": \"" + appName + "\",\n" +
                    "  \"iat\": " + initiationTime + ",\n" +
                    "  \"exp\": " + expirationTime + ",\n" +
                    "  \"jti\": \"" + jtiValue + "\",\n" +
                    "  \"aud\": \"https://localbank.com\",\n" +
                    "  \"scope\": \"accounts payments\",\n" +
                    "  \"token_endpoint_auth_method\": \"private_key_jwt\",\n" +
                    "  \"token_endpoint_auth_signing_alg\": \"PS256\",\n" +
                    "  \"grant_types\": [\n" +
                    "    \"authorization_code\",\n" +
                    "    \"client_credentials\",\n" +
                    "    \"refresh_token\"\n" +
                    "  ],\n" +
                    "  \"response_types\": [\n" +
                    "    \"code id_token\"\n" +
                    "  ],\n" +
                    "  \"id_token_signed_response_alg\": \"PS256\",\n" +
                    "  \"request_object_signing_alg\": \"PS256\",\n" +
                    "  \"application_type\": \"web\",\n" +
                    "  \"software_id\": \"" + appName + "\",\n" +
                    "  \"redirect_uris\": [\n" +
                    "    \"" + serverDomain + "/ob/authenticationendpoint/auth_code.do\"\n" +
                    "  ],\n" +
                    "  \"software_statement\": \"" + requestData.getSsa() + "\"\n" +
                    "}";
        }
        return new Payload(payloadString);
    }

    /**
     * Extract the signing key from the keystore
     *
     * @return The signing key
     * @throws OpenBankingException
     */
    private static PrivateKey getSigningKey() throws OpenBankingException {
        try {
            if (isExternalLink) {
                Files.copy(new URL(keyPath).openStream(), Paths.get("key.key"), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(GeneratorUtil.class.getClassLoader().getResourceAsStream("signing.key"),
                        Paths.get("key.key"), StandardCopyOption.REPLACE_EXISTING);
            }
            String privateKeyPath = "key.key";
            String keyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)), StandardCharsets.UTF_8);
            keyContent = keyContent.replace("-----BEGIN PRIVATE KEY-----", "");
            keyContent = keyContent.replace("-----END PRIVATE KEY-----", "");
            keyContent = keyContent.replace("\n", "");
            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyContent)));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            String error = "Error in extracting the signing private key";
            log.error(error, e);
            throw new OpenBankingException(error, e);
        }
    }

    /**
     * Extract the certificate from the keystore
     *
     * @return The certificate
     * @throws OpenBankingException
     */
    private static Certificate getPublicSigningCert() throws OpenBankingException {
        try {
            if (isExternalLink) {
                Files.copy(new URL(certPath).openStream(), Paths.get("cert.pem"), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(GeneratorUtil.class.getClassLoader().getResourceAsStream("signing.pem"),
                        Paths.get("cert.pem"), StandardCopyOption.REPLACE_EXISTING);
            }
            String publicCertPath = "cert.pem";
            return CertificateFactory.getInstance("X.509").generateCertificate(
                    new FileInputStream(publicCertPath));
        } catch (CertificateException | IOException e) {
            String error = "Error in extracting the signing certificate";
            log.error(error, e);
            throw new OpenBankingException(error, e);
        }
    }

    /**
     * Sign the JWT including header and payload content
     *
     * @param header Header content of the JWT
     * @param payload   Payload content of the JWT
     * @return signed JWT
     * @throws OpenBankingException
     */
    private static String signJWT(JWSHeader header, Payload payload) throws OpenBankingException {

        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(signer);
            log.info("The JWT was generated successfully");
            return jwsObject.serialize();
        } catch (JOSEException e) {
            String error = "Unable to sign JWT with signer";
            log.error(error, e);
            throw new OpenBankingException(error, e);
        }
    }

    /**
     * Generate SSA payload of the DCR App
     *
     * @param requestData Request data object which contains request parameters
     * @param appName Name of the application to be created
     * @return SSA payload
     * @throws OpenBankingException
     */
    private static String generateSSA(PayloadData requestData, String appName) throws OpenBankingException {
        long initiationTime = Instant.now().getEpochSecond();
        long expirationTime = initiationTime + 3600;
        long jtiValue = initiationTime + 10;
        String requestType = requestData.getType();

        String ssaContent = new String(Base64.getUrlDecoder()
                .decode(requestData.getSsa().split("\\.")[1]), StandardCharsets.UTF_8);
        JSONObject ssaContentObject = new JSONObject(ssaContent);
        ssaContentObject.remove("iat");
        ssaContentObject.put("iat", initiationTime);
        ssaContentObject.remove("exp");
        ssaContentObject.put("exp", expirationTime);
        ssaContentObject.remove("jti");
        ssaContentObject.put("jti", String.valueOf(jtiValue));
        ssaContentObject.remove("software_id");
        ssaContentObject.put("software_id", appName);
        ssaContentObject.remove("software_client_id");
        ssaContentObject.put("software_client_id", appName);
        ssaContentObject.remove("software_redirect_uris");
        String[] redirectUris = { serverDomain + "/ob/authenticationendpoint/auth_code.do" };
        ssaContentObject.put("software_redirect_uris", redirectUris);
        requestData.setType("ssa");

        JWSHeader header = generateHeader(requestData);
        requestData.setType(requestType);
        try {
            return signJWT(header, new Payload(ssaContentObject.toString()));
        } catch (OpenBankingException e) {
            String error = "Error while signing JWT/JWS";
            log.error(error, e);
            throw new OpenBankingException(error, e);
        }
    }

    /**
     * Generate the header of the JWT
     *
     * @param requestData Request data object which contains request parameters
     * @return The header of the JWT
     */
    private static JWSHeader generateHeader(PayloadData requestData) {

        String type = requestData.getType().toLowerCase(Locale.ENGLISH);
        JWSHeader header;
        if (type.contains("ssa")) {
            header = new JWSHeader.Builder(DEFAULT_ALGORITHM)
                    .keyID(kid)
                    .type(JOSEObjectType.JWT)
                    .build();
        } else if (type.contains("dcr")) {
            header = new JWSHeader.Builder(DEFAULT_ALGORITHM)
                    .keyID(kid)
                    .build();
        } else {
            header = new JWSHeader.Builder(DEFAULT_ALGORITHM)
                    .keyID(kid)
                    .build();
        }
        return header;
    }

    /**
     * Generate error response in case of an exception that may occur when processing the request
     *
     * @param httpStatusCode Status code of the error encountered
     * @param errorDescription Description of the error encountered
     * @return Error response
     */
    public static JWTGeneratorEndpointErrorResponse createErrorResponse(int httpStatusCode, String errorDescription) {

        JWTGeneratorEndpointErrorResponse demositeErrorResponse = new JWTGeneratorEndpointErrorResponse();
        net.minidev.json.JSONObject errorResponse = new net.minidev.json.JSONObject();
        errorResponse.put("error_description", errorDescription);
        demositeErrorResponse.setPayload(errorResponse);
        demositeErrorResponse.setHttpStatusCode(httpStatusCode);

        return demositeErrorResponse;
    }

}
