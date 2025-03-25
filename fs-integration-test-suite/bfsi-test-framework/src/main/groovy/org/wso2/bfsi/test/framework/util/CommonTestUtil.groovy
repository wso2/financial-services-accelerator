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

package org.wso2.bfsi.test.framework.util

import com.fasterxml.uuid.Generators
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import io.restassured.http.Header
import io.restassured.response.Response
import org.apache.commons.lang3.StringUtils
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jose4j.jws.JsonWebSignature
import org.jose4j.lang.JoseException
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService
import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.wso2.bfsi.test.framework.keystore.KeyStore
import org.xml.sax.SAXException

import javax.mail.Header
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyStoreException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.Signature
import java.security.UnrecoverableEntryException
import java.security.interfaces.RSAPrivateKey
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *  BFSI domain Util class
 *  keeps utility features that needed for bfsi layer
 */
class CommonTestUtil {

    private static final String B64_CLAIM_KEY = "b64";
    public static final String INVALID_KEYSTORE_PASSWORD = "wso2carbon";
    public static final String INVALID_KEYSTORE_ALIAS = "tpp3-invalid";

    static SSLSocketFactory sslSocketFactory;

    static Logger log = LogManager.getLogger(CommonTestUtil.class.getName());

    static CommonConfigurationService obConfiguration = new CommonConfigurationService()

    static String getParamListAsString(List<String> params, String delimiter) {

        String result = "";
        for (String param : params) {
            result = result.concat(param + delimiter);
        }
        return result.substring(0, result.length() - 1);
    }

    // Static initialize the SSL socket factory if MTLS is enabled.
    static {
        SSLSocketFactoryCreator sslSocketFactoryCreator = new SSLSocketFactoryCreator();
        if (obConfiguration.getAppTransportMLTSEnable()) {
            try {
                sslSocketFactory = sslSocketFactoryCreator.create();

                // Skip hostname verification.
                sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            } catch (TestFrameworkException e) {
                log.error("Unable to create the SSL socket factory", e);
            }
        }
    }

    static SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * Method to process a JSON Object and return a preferred value.
     *
     * @param response JSON Response
     * @param jsonPath Path of Required value
     * @return Value of requested key
     */
    static String parseResponseBody(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }

    static List<String> parseResponseBodyList(Response response, String jsonPath) {
        String res = response.jsonPath().getString(jsonPath)
                .replace("[", "").replace("]", "")
        return new ArrayList<String>(Arrays.asList(res.split(",")));
    }

    /**
     * Extract Authorisation code from redirect URL of hybriod flow response.
     *
     * @param codeUrl redirection url.
     * @return authorisation code.
     */
    static String getHybridCodeFromUrl(String codeUrl) {
        return codeUrl.split("#")[1].split("&")[1].split("code")[1].substring(1);
    }

    /**
     * Get decoded URL.
     *
     * @param authUrl authorisation url
     * @return encoded url
     * @throws UnsupportedEncodingException exception
     */
    static String getDecodedUrl(String authUrl) throws UnsupportedEncodingException {
        return URLDecoder.decode(authUrl.split("&")[1].split("=")[1], "UTF8");
    }

    /**
     * Extract Error Description From URL
     *
     * @param url
     * @return Error Description
     */
    static String getErrorDescriptionFromUrl(String url) {
        if (url.contains("error_description=")) {
            return url.split("error_description=")[1].split("&")[0].replaceAll("\\+", " ")
        } else {
            return url.split("oauthErrorMsg=")[1].split("&")[0].replaceAll("\\+", " ")
        }
    }

    /***
     * Extract id token from authorisation URL of authorisation flow.
     *
     * @param url
     * @return
     */
    public static String getIdTokenFromUrl(String url) {
        try {
            return url.split("id_token=")[1].split("&")[0];
        } catch (Exception e) {
            log.error("Unable to find id token in URL", e);
        }
        return null;
    }

    /**
     * Generate digest for a given payload.
     *
     * @param payload digest payload
     * @param algorithm digest alogrithm (i.e. SHA-256)
     * @return base64 encoded digest value
     * @throws TestFrameworkException exception
     */
    static String generateDigest(String payload, String algorithm)
            throws TestFrameworkException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] digestHash = messageDigest.digest(payload.getBytes(StandardCharsets.UTF_8));

            if (log.isDebugEnabled()) {
                log.debug("Digest payload: " + payload);
            }
            StringBuilder digestHashHex = new StringBuilder();
            for (byte b : digestHash) {
                digestHashHex.append(String.format("%02x", b));
            }
            return Base64.getEncoder()
                    .encodeToString(new BigInteger(digestHashHex.toString(), 16).toByteArray());
        } catch (NoSuchAlgorithmException e) {
            throw new TestFrameworkException("Error occurred while generating the digest", e);
        }
    }

    /**
     * Generate a signature for a given headers.
     *
     * @param headers headers that are required to sign
     * @param signatureAlgorithm signature algorithm
     * @return signature string
     * @throws TestFrameworkException exception
     */
    static String generateSignature(List<Header> headers,
                                    String signatureAlgorithm) throws TestFrameworkException {
        try {
            Signature rsa = Signature.getInstance(signatureAlgorithm);
            KeyStore keyStore = KeyStore.getApplicationKeyStore()
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(obConfiguration.getAppKeyStoreAlias(),
                    obConfiguration.getAppKeyStorePWD().toCharArray());
            rsa.initSign(privateKey);

            StringBuilder signatureHeader = new StringBuilder();
            for (Header header : headers) {
                signatureHeader.append(header.getName().toLowerCase())
                        .append(": ")
                        .append(header.getValue())
                        .append("\n");
            }
            String signingPayload = signatureHeader.substring(0, signatureHeader.length() - 1);

            if (log.isDebugEnabled()) {
                log.debug("Signing payload: " + signingPayload);
            }
            rsa.update(signingPayload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rsa.sign());
        } catch (Exception e) {
            throw new TestFrameworkException("Unable to generate the signature.", e);
        }
    }

    /**
     * Method to hexify array of bytes.
     *
     * @param bytes Required byte[]
     * @return hexified String
     */
    static String hexify(byte[] bytes) {

        if (bytes == null) {
            String errorMsg = "Invalid byte array: 'NULL'";
            throw new IllegalArgumentException(errorMsg);
        } else {
            char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6',
                    '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            StringBuilder buf = new StringBuilder(bytes.length * 2);

            for (byte aByte : bytes) {
                buf.append(hexDigits[(aByte & 240) >> 4]);
                buf.append(hexDigits[aByte & 15]);
            }
            return buf.toString();
        }
    }

    /**
     * Method to extract code value from the redirected URL.
     *
     * @param codeUrl URL
     * @return code String
     */
    static Optional<String> getCodeFromUrl(String codeUrl) throws URISyntaxException {

        URIBuilder uriBuilder = new URIBuilder(codeUrl);

        return uriBuilder.getQueryParams()
                .stream()
                .filter(pair -> "code".equals(pair.getName()))
                .findFirst()
                .map(NameValuePair::getValue);
    }

    /**
     * Method to construct the JWS detaching the payload part from it.
     *
     * @param jws Signed JWS signature
     * @return the detached JWS
     * @throws TestFrameworkException exception
     */
    static String constructDetachedJws(String jws) throws TestFrameworkException {

        if (StringUtils.isEmpty(jws)) {
            throw new TestFrameworkException("JWS is required for detached JWS construction");
        }

        // Split JWS by `.` deliminator
        String[] jwsParts = jws.split("\\.");

        // Redact payload and reconstruct JWS.
        if (jwsParts.length > 1) {
            jwsParts[1] = StringUtils.EMPTY;
            // Reconstruct JWS with `.` deliminator
            return String.join(".", jwsParts);
        }
        throw new TestFrameworkException("Required number of parts not "
                + "found in JWS for reconstruction");
    }

    /**
     * Method for generate JWS signature for given key
     * @param header
     * @param requestBody
     * @param pwd
     * @param alias
     * @return
     */
    static String generateXjwsSignatureWithKey(String header, String requestBody, String pwd, String alias) {

        try {
            Key key = KeyStore.getSigningKey(obConfiguration.getAppKeyStoreLocation(), pwd, alias)
            if (key instanceof RSAPrivateKey) {

                JWSHeader jwsHeader = JWSHeader.parse(header);
                Object b64ValueObject = jwsHeader.getCustomParam(B64_CLAIM_KEY);
                boolean b64Value = b64ValueObject != null ? ((Boolean) b64ValueObject) : true;

                // Create a new JsonWebSignature
                JsonWebSignature jws = new JsonWebSignature();

                // Set the payload, or signed content, on the JWS object
                jws.setPayload(requestBody);

                // Set the signature algorithm on the JWS that will integrity protect the payload
                jws.setAlgorithmHeaderValue(String.valueOf(jwsHeader.getAlgorithm()));

                // Setting headers
                jws.setKeyIdHeaderValue(jwsHeader.getKeyID());
                jws.setCriticalHeaderNames(jwsHeader.getCriticalParams().toArray(new String[0]));

                if (b64ValueObject != null) {
                    jws.getHeaders().setObjectHeaderValue(B64_CLAIM_KEY, b64Value);
                }

                for (Map.Entry<String, Object> entry : jwsHeader.getCustomParams().entrySet()) {
                    jws.getHeaders().setObjectHeaderValue(entry.getKey(), entry.getValue());
                }

                // Set the signing key on the JWS
                jws.setKey(key);

                // Sign the JWS and produce the detached JWS representation, which
                // is a string consisting of two dots ('.') separated base64url-encoded
                // parts in the form Header..Signature
                return jws.getDetachedContentCompactSerialization();
            }

        } catch (ParseException | KeyStoreException e) {
            log.error("Error occurred while reading the KeyStore file.", e);
            throw new TestFrameworkException("Error occurred while reading the KeyStore file. ", e)
        } catch (NoSuchAlgorithmException | JoseException e) {
            log.error("Error occurred while signing.", e);
            throw new TestFrameworkException("Error occurred while signing. ", e)
        } catch (UnrecoverableEntryException e) {
            log.error("Error occurred while retrieving the cert key.", e);
            throw new TestFrameworkException("Error occurred while retrieving the cert key. ", e)
        } catch (TestFrameworkException e) {
            log.error("Error occurred while reading the certificate thumb print.", e);
            throw new TestFrameworkException("Error occurred while reading the certificate thumb print. ", e)
        }
        return " ";
    }

    /**
     * Generate X-JWS Signature.
     *
     * @param header Headers from the request
     * @param requestBody Request payload
     * @return x-jws-signature
     */
    static String generateXjwsSignature(String header, String requestBody) {
        return generateXjwsSignatureWithKey(header, requestBody
                , obConfiguration.getAppKeyStorePWD(), obConfiguration.getAppKeyStoreAlias())
    }

    /**
     * Signs the JWS with an invalid key.
     * @param header
     * @param requestBody
     * @return
     */
    static String generateInvalidXjwsSignature(String header, String requestBody) {
        return generateXjwsSignatureWithKey(header, requestBody
                , INVALID_KEYSTORE_PASSWORD, INVALID_KEYSTORE_ALIAS)
    }

    /**
     * Get Mapped JWS Algorithm.
     *
     * @param algorithm configured algorithm in the test-config
     * @return Mapped JWS Algorithm for the configured string
     * @throws NoSuchAlgorithmException when algorithm configured is not supported
     */
    static JWSAlgorithm getMappedJwsAlgorithm(String algorithm)
            throws NoSuchAlgorithmException {

        if ("RS256".equalsIgnoreCase(algorithm)) {
            return JWSAlgorithm.RS256;
        } else if ("RS384".equalsIgnoreCase(algorithm)) {
            return JWSAlgorithm.RS384;
        } else if ("RS512".equalsIgnoreCase(algorithm)) {
            return JWSAlgorithm.RS512;
        } else if ("PS256".equalsIgnoreCase(algorithm)) {
            return JWSAlgorithm.PS256;
        } else {
            log.error("signing Algorithm configured in the test-config is not supported",
                    new NoSuchAlgorithmException("signing Algorithm configured "
                            + "in the test-config is not supported"))
            throw new NoSuchAlgorithmException("signing Algorithm configured "
                    + "in the test-config is not supported");
        }
    }

    /**
     * Update XML nodes in test-config.xml file.
     *
     * @param xmlFile path to test-config.xml file
     * @param parentNode Parent Node of the corresponding child node
     * @param childNode Child Node which need to update
     * @param value value need to be wriiten to the child node
     * @param tppNumber index of tpp
     */
    static void writeXMLContent(String xmlFile, String parentNode, String childNode,
                                String value, Integer tppNumber) throws TestFrameworkException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder()
            Document document = documentBuilder.parse(xmlFile);
            NodeList parentnode = document.getElementsByTagName(parentNode);
            Element nodeElement = (Element) parentnode.item(tppNumber);
            // Update value of status tag
            Element statusTag = (Element) nodeElement.getElementsByTagName(childNode).item(0);
            statusTag.setTextContent(String.valueOf(value));

            saveXMLContent(document, xmlFile);

        } catch (ParserConfigurationException e) {
            log.error("Error while creating a new instance of a DocumentBuilder.", e)
            throw new TestFrameworkException("Error while creating a new instance of a DocumentBuilder. ", e)
        } catch (SAXException e) {
            log.error("Error while parsing the content of the given URI as an XML document.", e)
            throw new TestFrameworkException("Error while parsing the content of the given URI as an XML document. ", e)
        } catch (IOException e) {
            log.error("Failed or interrupted I/O operations.", e)
            throw new TestFrameworkException("Failed or interrupted I/O operations. ", e)
        } catch (NullPointerException e) {
            log.error("Null Value for TPP", e)
            throw new TestFrameworkException("Null Value for TPP ", e)
        } catch (Exception e) {
            throw new TestFrameworkException("Error while write to xml file", e)
        }

    }

    /**
     * Save the updated Configuration File
     *
     * @param document XML document
     * @param xmlFile path to test-config.xml file
     */
    static void saveXMLContent(Document document, String xmlFile) throws TestFrameworkException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(xmlFile);
            transformer.transform(domSource, streamResult);

        } catch (TransformerConfigurationException e) {
            log.error("Failed to create a Transformer instance", e)
            throw new TestFrameworkException("Failed to create a Transformer instance ", e)
        } catch (TransformerException e) {
            log.error("Error while transforming the XML Source to a Result.", e)
            throw new TestFrameworkException("Error while transforming the XML Source to a Result. ", e)
        }
    }

    /**
     * Get header of the JWT token
     * @param jwtToken
     * @return
     */
    static Map<String, String> getJwtTokenHeader(String jwtToken) {
        String[] chunks = jwtToken.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String header = new String(decoder.decode(chunks[0]));

        Map<String, String> mapHeader = new HashMap<String, String>();
        String[] partsHeader = header.replaceAll("[{}]|\"", "").split(",");
        for (String part : partsHeader) {
            String[] keyValue = part.split(":");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            mapHeader.put(key, value);
        }
        return mapHeader;
    }

    /**
     * Get Payload of the JWT token
     * @param jwtToken
     * @return
     */
    static Map<String, String> getJwtTokenPayload(String jwtToken) {

        String[] chunks = jwtToken.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));

        Map<String, String> mapPayload = new HashMap<String, String>();
        String[] partsPayload = payload.replaceAll("[{}]|\"", "").split(",");
        for (String part : partsPayload) {
            String[] keyValue = part.split(":");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (keyValue.length >= 3) {
                value = keyValue[1].trim() + ":" + keyValue[2].trim();
            }

            mapPayload.put(key, value);
        }
        return mapPayload;
    }

    /**
     * Get ISO_8601 Standard date time
     * Eg: 2019-09-30T04:44:05.271Z
     *
     * @return String value of the current date time
     */
    static String getDateAndTime() {

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleformat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        String date = simpleformat.format(cal.getTime());

        return date;

    }

    /**
     * Generate X_FAPI_INTERACTION_ID
     */
    static String generateUUID() {
        UUID uuid = Generators.timeBasedGenerator().generate()
        return uuid.toString()
    }

    /**
     * Get ISO_8601 Standard date time
     * @return String value of the date time after 11.50hours
     */
    static String getTommorowDateAndTime() {
        long n = 11.10;
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.plusHours(n).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    /**
     * Get date in MM/dd/yyyy format
     * @return String value of the current date
     */
    static String getDate() {
        String pattern = "MM/dd/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern)
        String date = simpleDateFormat.format(new Date())
        return date;
    }

    /**
     * Generate Base64(clientID:ClientSectret)
     *
     * @return basic authorization header value
     */
    static String getBasicAuthorizationHeader(Integer tpp = null) {
        String headerString = obConfiguration.getAppInfoClientID(tpp) + ":" + obConfiguration.getAppInfoClientSecret(tpp)
        return Base64.encoder.encodeToString(headerString.getBytes(Charset.forName("UTF-8")))
    }



}

