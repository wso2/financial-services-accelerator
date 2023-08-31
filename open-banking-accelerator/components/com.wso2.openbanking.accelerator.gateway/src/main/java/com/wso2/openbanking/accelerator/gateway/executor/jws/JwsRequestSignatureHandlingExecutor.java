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

package com.wso2.openbanking.accelerator.gateway.executor.jws;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.identity.retriever.JWKRetriever;
import com.wso2.openbanking.accelerator.common.identity.retriever.sp.CommonServiceProviderRetriever;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.exception.OpenBankingExecutorException;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.HttpMethod;

/**
 * Class to handle JWS Signature validation for requests.
 */
public class JwsRequestSignatureHandlingExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(JwsRequestSignatureHandlingExecutor.class);
    private static final String B64_CLAIM_KEY = "b64";
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
    private static final String DOT_SYMBOL = ".";

    private String xWso2ApiVersion = null;
    private String xWso2ApiType = null;
    private String signatureHeaderName = getSignatureHeaderName();

    /**
     * Method to handle pre request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    @Generated(message = "Excluded from code coverage since it is covered by other methods")
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    @Generated(message = "Excluded from code coverage since it is covered by other methods")
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        if (obapiRequestContext.isError()) {
            return;
        }

        String messageID = obapiRequestContext.getMsgInfo().getMessageId();

        log.info(String.format("Executing JwsSignatureHandlingExecutor postProcessRequest for request %s", messageID));

        if (StringUtils.isEmpty(getXWso2ApiVersion())) {
            setXWso2ApiVersion(obapiRequestContext.getApiRequestInfo().getVersion());
        }

        if (StringUtils.isEmpty(getXWso2ApiType())) {
            setXWso2ApiType(obapiRequestContext.getApiRequestInfo().getContext());
        }

        // Retrieve headers and payload.
        Map<String, String> requestHeaders = obapiRequestContext.getMsgInfo().getHeaders();

        Boolean isApplicable = preProcessValidation(obapiRequestContext, requestHeaders);
        if (!isApplicable) {
            return;
        }

        Optional<String> payload = Optional.empty();

        if (requestHeaders.containsKey(GatewayConstants.CONTENT_TYPE_TAG)) {
            if (requestHeaders.get(GatewayConstants.CONTENT_TYPE_TAG).contains(GatewayConstants.TEXT_XML_CONTENT_TYPE)
                    || requestHeaders.get(GatewayConstants.CONTENT_TYPE_TAG).contains(
                    GatewayConstants.APPLICATION_XML_CONTENT_TYPE)) {
                try {
                    payload = Optional.of(GatewayUtils.getXMLPayloadToSign(obapiRequestContext.getMsgInfo()
                            .getPayloadHandler().consumeAsString()));
                } catch (Exception e) {
                    GatewayUtils.handleRequestInternalServerError(obapiRequestContext,
                            "Internal Server Error, Unable to process Payload",
                            OpenBankingErrorCodes.SERVER_ERROR_CODE);
                }
            } else {
                payload = Optional.ofNullable(obapiRequestContext.getRequestPayload());
            }
        } else {
            payload = Optional.ofNullable(obapiRequestContext.getRequestPayload());
        }

        // If the payload can be parsed.
        if (log.isDebugEnabled()) {
            log.debug(String.format("Request %s is Applicable for JWS Validation", messageID));
        }


        // Retrieve consumer key from headers.
        Optional<String> clientID = Optional.ofNullable(obapiRequestContext.getApiRequestInfo().getConsumerKey());
        // Retrieve x-jws-signature from headers.
        String jwsSignature = requestHeaders.get(signatureHeaderName);
        // Retrieve context properties.
        Map<String, String> contextProps = obapiRequestContext.getContextProps();

        // The sent header value should not be empty.
        if (StringUtils.isEmpty(jwsSignature)) {
            log.error(OpenBankingErrorCodes.EXECUTOR_JWS_SIGNATURE_NOT_FOUND);
            handleJwsSignatureErrors(obapiRequestContext, "Empty JWS Signature",
                    OpenBankingErrorCodes.INVALID_SIGNATURE_CODE);
            return;
        }

        // Adding jws signature to the context properties
        contextProps.put(signatureHeaderName, jwsSignature);
        obapiRequestContext.setContextProps(contextProps);
        // Now JWS Signature is part of the context properties of the req object.

        if (clientID.isPresent()) {
            if (payload.isPresent() && !payload.get().matches("\"\"")) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Built ClientID %s for request", clientID.get()));
                    log.debug("Payload extracted from request");
                }

                boolean verified = false;
                try {
                    verified = validateMessageSignature(clientID.get(), jwsSignature, obapiRequestContext,
                            payload.get());
                } catch (OpenBankingException | OpenBankingExecutorException e) {
                    log.error("Unable to validate message signature for the client ID " + clientID.get(), e);
                    handleJwsSignatureErrors(obapiRequestContext, e.getMessage(),
                            OpenBankingErrorCodes.INVALID_SIGNATURE_CODE);
                    return;
                }
                if (!verified) {
                    log.error("Signature validation failed for the client ID " + clientID.get());
                    handleJwsSignatureErrors(obapiRequestContext, "Invalid JWS Signature",
                            OpenBankingErrorCodes.INVALID_SIGNATURE_CODE);
                }
            } else {
                if (HttpMethod.POST.equals(obapiRequestContext.getMsgInfo().getMessageId()) ||
                        HttpMethod.PUT.equals(obapiRequestContext.getMsgInfo().getMessageId())) {
                    handleJwsSignatureErrors(obapiRequestContext, "Request payload cannot be empty",
                            OpenBankingErrorCodes.MISSING_REQUEST_PAYLOAD);
                }
            }
        }
    }

    /**
     * Method to validate if the request mandates to execute JWS Signature Validation.
     *
     * @param obapiRequestContext OB request context object
     * @param requestHeaders OB request header Map
     */
    @Generated(message = "Removed from unit test coverage since Common module is required")
    public Boolean preProcessValidation(OBAPIRequestContext obapiRequestContext, Map<String, String> requestHeaders) {
        // Return if the request contains an error.
        // Check mandated apis at toolkit level.
        return !obapiRequestContext.isError() && OpenBankingConfigParser.getInstance().isJwsSignatureValidationEnabled()
                && !HttpMethod.GET.equals(obapiRequestContext.getMsgInfo().getHttpMethod())
                && !HttpMethod.DELETE.equals(obapiRequestContext.getMsgInfo().getHttpMethod());
    }

    /**
     * Method to handle pre response.
     *
     * @param obapiResponseContext OB response context object
     */

    @Override
    @Generated(message = "Excluded from code coverage since it is covered by other methods")
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    @Generated(message = "Excluded from code coverage since it is covered by other methods")
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Claims to be validated in JWS header.
     *
     * @param obapiRequestContext OB request context object
     * @param claims jose header claims
     * @param appName application name
     * @param jwksURI jwksUrl in URL format
     * @return boolean
     */
    public boolean validateClaims(OBAPIRequestContext obapiRequestContext,
                                  JWSHeader claims, String appName, String jwksURI) {
        // Implemented in toolkit level.

        // RSA PSS & EC digital signature algorithms are allowed.
        List<String> allowedSigningAlgorithms = OpenBankingConfigParser.getInstance().getJwsRequestSigningAlgorithms();

        if (!allowedSigningAlgorithms.contains(claims.getAlgorithm().getName())) {
            log.error("The " + claims.getAlgorithm().getName() + " algorithm is not supported" +
                    " by the Solution");
            handleJwsSignatureErrors(obapiRequestContext, "The " + claims.getAlgorithm()
                            .getName() + " algorithm is not supported by the Solution",
                    OpenBankingErrorCodes.INVALID_SIGNATURE_CODE);
        }
        return true;
    }

    /**
     * Method to validate JWS Signature.
     *
     * @param clientID client ID of the Application
     * @param detachedContentJws JWS in header
     * @param obapiRequestContext OB Request context
     * @param payload HTTP request payload
     * @return boolean
     */

    @Generated(message = "Excluded from code coverage since method includes accessing jwks_uri")
    private boolean validateMessageSignature(String clientID, String detachedContentJws,
                                             OBAPIRequestContext obapiRequestContext, String payload)
            throws OpenBankingExecutorException, OpenBankingException {

        // Convert Detached JWS into standard JWS.
        String reconstructedJws;
        try {
            reconstructedJws = this.reconstructJws(detachedContentJws, payload);
        } catch (OpenBankingExecutorException e) {
            log.error("Unable to reconstruct JWS", e);
            throw new OpenBankingExecutorException("Malformed JWS Signature", e);
        }

        JWSVerifier verifier = null;
        JWSObject jwsObject;
        RSAKey rsaKey = null;
        ECKey ecKey = null;

        // Parse JWSObject to retrieve headers.
        try {
            jwsObject = JWSObject.parse(reconstructedJws);
        } catch (ParseException e) {
            log.error("Unable to parse JWS signature" , e);
            throw new OpenBankingExecutorException("Unable to parse JWS signature", e);
        }

        // Retrieve JWK set
        String jwksURI;
        String appName = null;
        try {
            appName = (new CommonServiceProviderRetriever()).getAppPropertyFromSPMetaData(clientID, "software_id");
        } catch (OpenBankingException e) {
            log.error("Error while retrieving the app name", e);
            throw new OpenBankingExecutorException("Error while retrieving the app name", e);
        }

        try {
            jwksURI = getJwksUrl(clientID);

            // Get JWKSet from cache or retrieve from onDemand retriever
            JWKSet jwkSet = getJwkSet(jwksURI, appName);

            // Get public key from JWK used for signing.
            try {
                JWK key = retrievePublicKey(jwkSet, jwsObject);
                // Public key of the Signing certificate is retrieved - Available 1 key with use:"Sig"
                if (key != null) {
                    X509Certificate x509Certificate = key.getParsedX509CertChain().get(0);
                    // kty: "RSA"
                    if (key.getKeyType().getValue().equals("RSA")) {
                        rsaKey = RSAKey.parse(x509Certificate);
                        //kty: "EC"
                    } else if (key.getKeyType().getValue().equals("EC")) {
                        ecKey = ECKey.parse(x509Certificate);
                        //log error if the kty is not supported for the allowed signing alg.
                    } else {
                        String errorMessage = String.format("The kty %s of the Key is not supported",
                                key.getKeyType().getValue());
                        log.error(errorMessage);
                        throw new OpenBankingExecutorException(errorMessage);
                    }
                } else {
                    log.error("Public key of the signing certificate not found in JWK set");
                    throw new OpenBankingExecutorException("Public key of the signing certificate not found in JWK " +
                            "set");
                }
            } catch (JOSEException e) {
                log.error("Certificate not valid", e);
                throw new OpenBankingExecutorException("Certificate not valid", e);
            }

        } catch (OpenBankingException e) {
            log.error("Unable to validate JWS Signature retrieving public key", e);
            throw new OpenBankingExecutorException("Unable to validate JWS Signature retrieving public key", e);
        }

        // Validating "iss" , "tan", "alg", "kid"
        boolean areClaimsValid = validateClaims(obapiRequestContext, jwsObject.getHeader(), appName, jwksURI);

        try {


            JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(jwsObject.getHeader().getAlgorithm().getName());

            Set<String> criticalParameters  = new HashSet<>(Arrays.asList(differedCriticalClaims()));

            if (JWSAlgorithm.Family.RSA.contains(jwsAlgorithm)) {
                // Define JWSVerifier for JWS signed with RSA Signing alg.
                verifier = new RSASSAVerifier(
                        rsaKey != null ? rsaKey.toRSAPublicKey() : null, criticalParameters);
            } else if (JWSAlgorithm.Family.EC.contains(jwsAlgorithm)) {
                // Define JWSVerifier for JWS signed with EC Signing alg.
                verifier = new ECDSAVerifier(
                        ecKey != null ? ecKey.toECPublicKey() : null, criticalParameters);
            } else {
                String errorMessage = "The " + jwsObject.getHeader().getAlgorithm().getName() + " algorithm is not " +
                        "supported by the Solution";
                log.error(errorMessage);
                throw new OpenBankingExecutorException(errorMessage);

            }

        }  catch (JOSEException e) {
            log.error("Invalid Signing Algorithm" , e);
            throw new OpenBankingExecutorException("Invalid JWS Signature,signed with invalid " +
                    "algorithm", e);
        }

        // If claims are verified, verify signature.
        // Since asymmetric alg is used, the signature can be verified using public key only.

        boolean verified;

        if (areClaimsValid) {
            try {
                // Check if payload is b64 encoded or un-encoded
                if (isB64HeaderVerifiable(jwsObject)) {
                    // b64=true
                    verified = jwsObject.verify(verifier);
                } else {
                    // b64=false
                    // Produces the signature with un-encoded payload.
                    // which is the encoded header + ".." + the encoded signature
                    String[] jwsParts = StringUtils.split(detachedContentJws, DOT_SYMBOL);

                    JWSHeader header = JWSHeader.parse(new Base64URL(jwsParts[0]));
                    Base64URL signature = new Base64URL(jwsParts[1]);
                    verified = verifier.verify(header, getSigningInput(header, payload), signature);
                }
                if (verified) {
                    return true;
                }
            } catch (JOSEException e) {
                log.error("Unable to verify JWS signature", e);
                throw new OpenBankingExecutorException("Unable to verify JWS signature", e);
            } catch (ParseException e) {
                log.error("Error occurred while parsing the JWS Header", e);
                throw new OpenBankingExecutorException("Error occurred while parsing the JWS Header", e);
            }
        }
        return false;
    }

    /**
     * Method to reconstruct a detached JWS with encoded payload.
     *
     * @param jwsSignature Detached JWS
     * @param payload HTTP request payload
     * @return boolean
     */
    private String reconstructJws(String jwsSignature, String payload) throws OpenBankingExecutorException {

        // GET requests and DELETE requests will not need message signing.
        if (StringUtils.isEmpty(payload)) {
            throw new OpenBankingExecutorException("Payload is required for JWS reconstruction");
        }

        String[] jwsParts = jwsSignature.split("\\.");

        if (log.isDebugEnabled()) {
            log.debug(String.format("Found %d parts in JWS for reconstruction", jwsParts.length));
        }

        // Add Base64Url encoded payload.
        if (jwsParts.length == 3) {
            jwsParts[1] = Base64URL.encode(payload).toString();

            // Reconstruct JWS with `.` deliminator
            return String.join(DOT_SYMBOL, jwsParts);
        } else if (jwsParts.length == 5) {
            throw new OpenBankingExecutorException("Not supported for signed and encrypted JWTs.");
        }

        throw new OpenBankingExecutorException("Required number of parts not found in JWS for reconstruction");
    }

    /**
     * If the b64 header is not available or is true, it is verifiable.
     *
     * @param jwsObject The reconstructed jws object parsed from x-jws-signature
     * @return Boolean
     */
    private boolean isB64HeaderVerifiable(JWSObject jwsObject) {

        JWSHeader jwsHeader = jwsObject.getHeader();
        Object b64Value = jwsHeader.getCustomParam(B64_CLAIM_KEY);
        return b64Value != null ? ((Boolean) b64Value) : true;
    }

    /**
     * Method to retrieve payload from File Payment Upload requests.
     *
     * @param header
     * @param jwsPayload
     * @return signing input
     */
    private byte[] getSigningInput(JWSHeader header, String jwsPayload) {

        String combinedInput = header.toBase64URL().toString() + DOT_SYMBOL + jwsPayload;
        return combinedInput.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Method to handle errors in JWS Signature validation.
     *
     * @param obapiRequestContext OB request context object
     * @param message error message
     */
    public void handleJwsSignatureErrors(
            OBAPIRequestContext obapiRequestContext, String message, String errorCode) {

        OpenBankingExecutorError error = new OpenBankingExecutorError(errorCode,
                OpenBankingErrorCodes.JWS_SIGNATURE_HANDLE_ERROR, message, OpenBankingErrorCodes.BAD_REQUEST_CODE);
        setErrorsToRequestContext(obapiRequestContext, error);
    }

    public void setErrorsToRequestContext(OBAPIRequestContext obapiRequestContext, OpenBankingExecutorError error) {
        ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
        executorErrors.add(error);
        obapiRequestContext.setError(true);
        obapiRequestContext.setErrors(executorErrors);
    }

    /**
     * Method to retrieve JWKS Url from service Provider properties.
     * @param clientID
     * @return
     * @throws OpenBankingException
     */
    @Generated(message = "Excluded from code coverage since method includes service call")
    private String getJwksUrl(String clientID) throws OpenBankingException {

        String jwksURI;
        try {
            jwksURI = (new CommonServiceProviderRetriever()).getAppPropertyFromSPMetaData(clientID, "jwksURI");
            if (jwksURI == null) {
                throw new OpenBankingException("The JWK set URL must not be null");
            }
        } catch (OpenBankingException e) {
            log.error("JWKS URL is not found", e);
            throw new OpenBankingException("The JWKS_URI is not found for client ID " + clientID, e);
        }
        return jwksURI;
    }

    /**
     * Method to retrieve the JWKSet from JWKS URI.
     *
     * @param jwksURI
     * @param appName
     * @return
     * @throws OpenBankingException
     */
    @Generated(message = "Excluded from code coverage since method includes accessing jwks_uri")
    private JWKSet getJwkSet(String jwksURI, String appName) throws OpenBankingException {
        JWKSet jwkSet;
        try {
            jwkSet = new JWKRetriever().getJWKSet(new URL(jwksURI), appName);
        } catch (MalformedURLException e) {
            log.error("Provided JWKS URL is malformed", e);
            throw new OpenBankingException("The provided JWKS_URI is malformed", e);
        }

        return jwkSet;
    }

    /**
     * Method to retrieve the public key from JWKSet.
     *
     * @param jwkSet
     * @param jwsObject
     * @return
     */
    @Generated(message = "Excluded from code coverage since method includes accessing JWKSet")
    private JWK retrievePublicKey(JWKSet jwkSet, JWSObject jwsObject) {

        JWK key = null;
        // First get the key with given kid, use as sig and operation as verify from the list.
        JWKMatcher keyMatcherWithKidUseAndOperation =
                new JWKMatcher.Builder().keyID(jwsObject.getHeader().getKeyID())
                        .keyUse(KeyUse.SIGNATURE)
                        .keyOperation(KeyOperation.VERIFY)
                        .build();
        List<JWK> jwkList = new JWKSelector(keyMatcherWithKidUseAndOperation).select(jwkSet);

        if (jwkList.isEmpty()) {
            // If empty, then get the key with given kid and use as sig from the list.
            JWKMatcher keyMatcherWithKidAndUse = new JWKMatcher.Builder()
                    .keyID(jwsObject.getHeader().getKeyID())
                    .keyUse(KeyUse.SIGNATURE).build();
            jwkList = new JWKSelector(keyMatcherWithKidAndUse).select(jwkSet);

            if (jwkList.isEmpty()) {
                // fail over defaults to ->, then get the key with given kid.
                JWKMatcher keyMatcherWithKid = new JWKMatcher.Builder().keyID(jwsObject.getHeader().getKeyID()).build();
                jwkList = new JWKSelector(keyMatcherWithKid).select(jwkSet);
            }
        }

        if (jwkList.isEmpty()) {
            log.error("No matching KID found to retrieve public key in JWK set");
        } else {
            key = jwkList.get(0);
        }

        return key;
    }

    /**
     * Differed critical parameters for validation.
     *
     * @return list of critical claims
     */
    public String[] differedCriticalClaims() {
        // Implemented at toolkit level

        return new String[0];
    }

    public void setXWso2ApiVersion(String xWso2ApiVersion) {

        this.xWso2ApiVersion = xWso2ApiVersion;
    }

    public String getXWso2ApiVersion() {

        return this.xWso2ApiVersion;
    }

    public String getXWso2ApiType() {

        return xWso2ApiType;
    }

    public void setXWso2ApiType(String xWso2ApiType) {

        this.xWso2ApiType = xWso2ApiType;
    }

    // If the header name is different at toolkit level,
    // this method need to override.
    public String getSignatureHeaderName() {

        return "x-jws-signature";
    }

}
