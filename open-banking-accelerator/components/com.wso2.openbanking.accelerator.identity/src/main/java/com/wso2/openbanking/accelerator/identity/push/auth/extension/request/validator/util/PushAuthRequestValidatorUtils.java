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

package com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.constants.PushAuthRequestConstants;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.exception.PushAuthRequestValidatorException;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.oauth2.validators.jwt.JWKSBasedJWTValidator;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.oauth2.util.OAuth2Util.getX509CertOfOAuthApp;
import static org.wso2.carbon.identity.openidconnect.model.Constants.JWKS_URI;
import static org.wso2.carbon.identity.openidconnect.model.Constants.JWT_PART_DELIMITER;
import static org.wso2.carbon.identity.openidconnect.model.Constants.NUMBER_OF_PARTS_IN_JWS;

/**
 * The utility functions required for the push authorization module.
 */
public class PushAuthRequestValidatorUtils {

    private static Log log = LogFactory.getLog(PushAuthRequestValidatorUtils.class);
    private static final String OIDC_IDP_ENTITY_ID = "IdPEntityId";
    private static final String OAUTH2_TOKEN_EP_URL = "OAuth2TokenEPUrl";
    private static final String OIDC_ID_TOKEN_ISSUER_ID = "OAuth.OpenIDConnect.IDTokenIssuerID";
    private static final ArrayList<String> ALLOWED_FORM_BODY_PARAMS = new ArrayList<String>() {
        {
            add("client_id");
            add("client_assertion");
            add("client_assertion_type");
        }
    };

    /**
     * Check whether push auth request only contains client_id, client_assertion and client_assertion_type when request
     * parameter is present.
     */
    public static void validateRequestFormBody(Map<String, Object> parameters)
            throws PushAuthRequestValidatorException {

        for (Map.Entry<String, Object> parameter: parameters.entrySet()) {
            if (!PushAuthRequestConstants.REQUEST.equalsIgnoreCase(parameter.getKey()) &&
                    !ALLOWED_FORM_BODY_PARAMS.contains(parameter.getKey())) {
                log.error("Invalid parameters found in the request");
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST, "Invalid parameters found in the request");
            }
        }
    }

    /**
     * Check whether the algorithm used to sign the request object is valid.
     */
    public static void validateSignatureAlgorithm(Object algorithm) throws PushAuthRequestValidatorException {

        boolean isValid = false;
        if (algorithm != null && StringUtils.isNotBlank((String) algorithm)) {
            List<String> allowedAlgorithmsList = new ArrayList<>();
            Object allowedAlgorithms = IdentityExtensionsDataHolder.getInstance()
                    .getConfigurationMap().get(OpenBankingConstants.SIGNATURE_ALGORITHMS);
            if (allowedAlgorithms instanceof List) {
                allowedAlgorithmsList = (List<String>) allowedAlgorithms;
            } else {
                allowedAlgorithmsList.add(allowedAlgorithms.toString());
            }
            isValid = allowedAlgorithmsList.isEmpty() || allowedAlgorithmsList.contains(algorithm);
        }
        if (!isValid) {
            log.error("Invalid request object signing algorithm");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                    "Invalid request object signing algorithm");
        }

    }

    /**
     * Checks whether the given authentication flow requires nonce as a mandatory parameter.
     */
    public static boolean isNonceMandatory(String responseType) {

        // nonce parameter is required for the OIDC hybrid flow and implicit flow grant types requesting ID_TOKEN.
        return Arrays.stream(responseType.split("\\s+")).anyMatch(OAuthConstants.ID_TOKEN::equals);
    }

    /**
     * Validates the nonce parameter as mandatory.
     */
    public static void validateNonceParameter(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

        if (StringUtils.isNotBlank(requestBodyJson.getAsString(PushAuthRequestConstants.RESPONSE_TYPE))) {
            if (isNonceMandatory(requestBodyJson.getAsString(PushAuthRequestConstants.RESPONSE_TYPE)) &&
                    requestBodyJson.getAsString(PushAuthRequestConstants.NONCE) == null) {
                log.error("Invalid Nonce parameter in the request");
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST, "Invalid Nonce parameter in the request");
            }
        }
    }

    /**
     * Validates the mandatory PKCE parameters.
     */
    public static void validatePKCEParameters(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

        if (StringUtils.isEmpty(requestBodyJson.getAsString(PushAuthRequestConstants.CODE_CHALLENGE))) {
            log.error("Mandatory parameter code_challenge, not found in the request");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                    "Mandatory parameter code_challenge, not found in the request");
        }

        if (StringUtils.isEmpty(requestBodyJson.getAsString(PushAuthRequestConstants.CODE_CHALLENGE_METHOD))) {
            log.error("Mandatory parameter code_challenge_method, not found in the request");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                    "Mandatory parameter code_challenge_method, not found in the request");
        }
    }

    /**
     * Validates the mandatory response_type parameter.
     */
    public static void validateResponseType(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

        if (StringUtils.isEmpty(requestBodyJson.getAsString(PushAuthRequestConstants.RESPONSE_TYPE))) {
            log.error("Mandatory parameter response_type, not found in the request");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                    "Mandatory parameter response_type, not found in the request");
        }
    }

    /**
     * Validates the redirect URI parameter to verify if the parameter is available and not null.
     */
    public static void validateRedirectUri(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

        if (StringUtils.isBlank(requestBodyJson.getAsString(PushAuthRequestConstants.REDIRECT_URI))) {
            log.error("Mandatory parameter redirect_uri, not found in the request");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST,
                    "Mandatory parameter redirect_uri, not found in the request");
        }
    }

    /**
     * Validates the scope parameter to verify only allowed scopes for the application are passed in the request.
     */
    public static void validateScope(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

        if (StringUtils.isNotBlank(requestBodyJson.getAsString(PushAuthRequestConstants.SCOPE))) {

            List<String> requestedScopes = Arrays.asList(requestBodyJson.getAsString(PushAuthRequestConstants.SCOPE)
                    .split("\\s+"));

            List<String> allowedScopes;
            try {
                allowedScopes = Arrays.asList(new IdentityCommonHelper()
                        .getAppPropertyFromSPMetaData(requestBodyJson.getAsString(PushAuthRequestConstants.CLIENT_ID),
                                IdentityCommonConstants.SCOPE).split("\\s+"));
            } catch (OpenBankingException e) {
                log.error("Error while retrieving sp meta data", e);
                throw new PushAuthRequestValidatorException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        PushAuthRequestConstants.SERVER_ERROR, "Error while retrieving sp meta data", e);
            }

            for (String scope : requestedScopes) {
                if (!allowedScopes.contains(scope)) {
                    log.error("Invalid scopes in the request");
                    throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                            PushAuthRequestConstants.INVALID_REQUEST, "Invalid scopes in the request");
                }
            }
        } else {
            log.error("Mandatory parameter scope, not found in the request");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST,
                    "Mandatory parameter scope, not found in the request");
        }
    }

    /**
     * Validates the audience parameter to check whether it matches any supported audience value.
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static void validateAudience(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

        String clientId = requestBodyJson.getAsString(PushAuthRequestConstants.CLIENT_ID);
        Object audValue = requestBodyJson.get(PushAuthRequestConstants.AUDIENCE);
        boolean isValid = false;

        if (audValue == null) {
            log.error("aud parameter is missing in the request object");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT, "aud parameter is missing in the request object");
        }

        List<String> validAudUrls = getAllowedPARAudienceValues(getSPTenantDomainFromClientId(clientId));

        if (audValue instanceof String) {
            // If the aud value is a string, check whether it is equal to the allowed audience value.
            isValid = validAudUrls.contains(audValue);
        } else if (audValue instanceof JSONArray) {
            // If the aud value is an array, check whether it is equal to one of the allowed audience values.
            JSONArray audArray = (JSONArray) audValue;
            for (Object aud : audArray) {
                if (validAudUrls.contains(aud)) {
                    isValid = true;
                    break;
                }
            }
        }

        if (!isValid) {
            log.error("Invalid audience value in the request");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT, "Invalid audience value in the request");
        }
    }

    /**
     * Validates the issuer parameter to check whether its similar to the client id.
     */
    public static void validateIssuer(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

        String issuer = requestBodyJson.getAsString(PushAuthRequestConstants.ISSUER);
        String clientId = requestBodyJson.getAsString(PushAuthRequestConstants.CLIENT_ID);
        boolean isValid = false;

        if (StringUtils.isNotBlank(issuer) && StringUtils.isNotBlank(clientId)) {
            isValid =  issuer.equals(clientId);
        }
        if (!isValid) {
            log.error("Invalid issuer in the request");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT, "Invalid issuer in the request");
        }
    }


    /**
     * Validates the expiration status of the request object.
     */
    public static void validateExpirationTime(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

        String exp = requestBodyJson.getAsString(PushAuthRequestConstants.EXPIRY);

        if (StringUtils.isNotBlank(exp)) {
            Date expirationTime = new Date(Integer.parseInt(exp) * 1000L);
            long timeStampSkewMillis = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
            long expirationTimeInMillis = expirationTime.getTime();
            long currentTimeInMillis = System.currentTimeMillis();
            // exp parameter should not be over 1 hour in the future.
            if ((expirationTimeInMillis - (currentTimeInMillis + timeStampSkewMillis)) >
                    PushAuthRequestConstants.ONE_HOUR_IN_MILLIS) {
                log.error("exp parameter in the request object is over 1 hour in the future");
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST_OBJECT, "exp parameter in the request object " +
                        "is over 1 hour in the future");
            }
            // exp parameter should not be in the past.
            if ((currentTimeInMillis + timeStampSkewMillis) > expirationTimeInMillis) {
                log.error("Request object expired");
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST, "Request object expired");
            }
        } else {
            log.error("exp parameter is missing in the request object");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT, "exp parameter is missing in the request object");
        }
    }

    /**
     * Validates the nbf claim in the request object.
     */
    public static void validateNotBeforeClaim(JSONObject requestBodyJson) throws PushAuthRequestValidatorException {

            String nbf = requestBodyJson.getAsString(PushAuthRequestConstants.NOT_BEFORE);

            if (StringUtils.isNotBlank(nbf)) {
                Date notBeforeTime = new Date(Integer.parseInt(nbf) * 1000L);
                long timeStampSkewMillis = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
                long notBeforeTimeInMillis = notBeforeTime.getTime();
                long currentTimeInMillis = System.currentTimeMillis();
                //request object should be used on or after nbf value.
                if ((currentTimeInMillis + timeStampSkewMillis) < notBeforeTimeInMillis) {
                    log.error("Request object is not valid yet");
                    throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                            PushAuthRequestConstants.INVALID_REQUEST_OBJECT, "Request object is not valid yet");
                }
                // nbf parameter should not be over 1 hour in the past.
                if (((currentTimeInMillis + timeStampSkewMillis) - notBeforeTimeInMillis) >
                        PushAuthRequestConstants.ONE_HOUR_IN_MILLIS) {
                    log.error("nbf parameter in the request object is over 1 hour in the past");
                    throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                            PushAuthRequestConstants.INVALID_REQUEST_OBJECT, "nbf parameter in the request object " +
                            "is over 1 hour in the past");
                }
            } else {
                log.error("nbf parameter is missing in the request object");
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                        "nbf parameter is missing in the request object");
            }
    }

    /**
     * Validates signature of the request object.
     */
    @Generated(message = "Excluding from code coverage since it requires several service calls")
    public static void validateSignature(String requestObject, JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        String jwksUri = null;
        ServiceProviderProperty[] spProperties = null;

        // Get Service provider properties
        try {
            spProperties = OAuth2Util.getServiceProvider(requestBodyJson
                    .getAsString(PushAuthRequestConstants.CLIENT_ID)).getSpProperties();
        } catch (IdentityOAuth2Exception exception) {
            log.error("Unable to extract Service Provider Properties", exception);
            throw new PushAuthRequestValidatorException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    PushAuthRequestConstants.SERVER_ERROR, exception.getMessage(), exception);
        }

        // Extract JWKS Uri from properties
        if (spProperties != null) {
            for (ServiceProviderProperty spProperty : spProperties) {
                if (JWKS_URI.equals(spProperty.getName())) {
                    jwksUri = spProperty.getValue();
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieved JWKS URI: " + jwksUri);
        }

        SignedJWT jwt;

        try {
            jwt = SignedJWT.parse(requestObject);
        } catch (ParseException exception) {
            log.error("Unable to parse JWT object", exception);
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST, exception.getMessage(), exception);
        }

        boolean isVerified = false;

        if (StringUtils.isBlank(jwksUri)) {
            log.debug("Validating from certificate");
            String tenantDomain = getSPTenantDomainFromClientId(requestBodyJson
                    .getAsString(PushAuthRequestConstants.CLIENT_ID));

            // Validate from Certificate Content
            Certificate certificate;
            try {
                certificate = getX509CertOfOAuthApp(requestBodyJson
                        .getAsString(PushAuthRequestConstants.CLIENT_ID), tenantDomain);
            } catch (IdentityOAuth2Exception exception) {
                log.error("Unable to get certificate from app", exception);
                throw new PushAuthRequestValidatorException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        PushAuthRequestConstants.SERVER_ERROR, exception.getMessage(), exception);
            }

            isVerified = isSignatureVerified(jwt, certificate);
        } else {
            log.debug("Validating from JWKS URI");

            // Validate from JWKS Uri
            String alg = jwt.getHeader().getAlgorithm().getName();
            Map<String, Object> options = new HashMap<>();
            try {
                isVerified = new JWKSBasedJWTValidator().validateSignature(jwt.getParsedString(), jwksUri,
                        alg, options);
            } catch (IdentityOAuth2Exception exception) {
                log.error("Unable to validate JWT using JWKS URL", exception);
                String errorMessage = getCustomSignatureValidationErrorMessage(exception);
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST_OBJECT, errorMessage, exception);
            }
        }
        if (!isVerified) {
            log.error("Request object signature validation failed");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST,
                    "Request object signature validation failed");
        }
    }

    /**
     * Validate signature of a sign JWT against a given certificate.
     */
    @Generated(message = "Excluding from code coverage since it requires several service calls")
    private static boolean isSignatureVerified(SignedJWT signedJWT, Certificate x509Certificate) {
        JWSHeader header = signedJWT.getHeader();
        if (x509Certificate == null) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to locate certificate for JWT " + header.toString());
            }
            return false;
        } else {
            String alg = signedJWT.getHeader().getAlgorithm().getName();
            if (log.isDebugEnabled()) {
                log.debug("Signature Algorithm found in the JWT Header: " + alg);
            }

            // allowed RS and PS for the moment
            if (alg.indexOf("RS") != 0 && alg.indexOf("PS") != 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Signature Algorithm not supported yet : " + alg);
                }
                return false;
            } else {
                PublicKey publicKey = x509Certificate.getPublicKey();
                if (publicKey instanceof RSAPublicKey) {
                    RSASSAVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);

                    try {
                        return signedJWT.verify(verifier);
                    } catch (JOSEException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Unable to verify the signature of the request object: " + signedJWT.serialize());
                        }
                        return false;
                    }
                } else {
                    log.debug("Public key is not an RSA public key.");
                    return false;
                }
            }
        }
    }

    /**
     * Return the alias of the resident IDP (issuer of Authorization Server), PAR Endpoint and Token Endpoint
     * to validate the audience value of the PAR Request Object.
     */
    @Generated(message = "Excluding from code coverage since it requires several service calls")
    private static List<String> getAllowedPARAudienceValues(String tenantDomain)
            throws PushAuthRequestValidatorException {

        List<String> validAudUrls = new ArrayList<>();
        String residentIdpAlias = StringUtils.EMPTY;
        IdentityProvider residentIdP;
        try {
            residentIdP = IdentityProviderManager.getInstance().getResidentIdP(tenantDomain);
            FederatedAuthenticatorConfig oidcFedAuthn = IdentityApplicationManagementUtil
                    .getFederatedAuthenticator(residentIdP.getFederatedAuthenticatorConfigs(),
                            IdentityApplicationConstants.Authenticator.OIDC.NAME);

            Property idPEntityIdProperty =
                    IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(), OIDC_IDP_ENTITY_ID);
            if (idPEntityIdProperty != null) {
                residentIdpAlias = idPEntityIdProperty.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("Found IdPEntityID: " + residentIdpAlias + " for tenantDomain: " + tenantDomain);
                }
            }

            Property oAuth2TokenEPUrlProperty =
                    IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(), OAUTH2_TOKEN_EP_URL);
            if (oAuth2TokenEPUrlProperty != null) {
                // add Token EP Url as a valid "aud" value
                validAudUrls.add(oAuth2TokenEPUrlProperty.getValue());
                if (log.isDebugEnabled()) {
                    log.debug("Found OAuth2TokenEPUrl: " + oAuth2TokenEPUrlProperty.getValue() +
                            " for tenantDomain: " + tenantDomain);
                }
            }
        } catch (IdentityProviderManagementException e) {
            log.error("Error while loading OAuth2TokenEPUrl of the resident IDP of tenant:" + tenantDomain, e);
            throw new PushAuthRequestValidatorException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    OAuth2ErrorCodes.SERVER_ERROR, "Server Error while validating audience " +
                    "of Request Object.", e);
        }

        if (StringUtils.isEmpty(residentIdpAlias)) {
            residentIdpAlias = IdentityUtil.getProperty(OIDC_ID_TOKEN_ISSUER_ID);
            if (StringUtils.isNotEmpty(residentIdpAlias)) {
                if (log.isDebugEnabled()) {
                    log.debug("'IdPEntityID' property was empty for tenantDomain: " + tenantDomain + ". Using " +
                            "OIDC IDToken Issuer value: " + residentIdpAlias + " as alias to identify Resident IDP.");
                }
            }
        }

        // add IdPEntityID or the "issuer" as a valid "aud" value
        validAudUrls.add(residentIdpAlias);

        try {
            URL residentIdPUrl = new URL(residentIdpAlias);
            // derive PAR EP URL from the residentIdP base URL
            URL parEpUrl = new URL(residentIdPUrl, IdentityCommonConstants.PAR_ENDPOINT);
            // add PAR EP URL as a valid "aud" value
            validAudUrls.add(parEpUrl.toString());
        } catch (MalformedURLException exception) {
            log.error("Error occurred while deriving PAR endpoint URL.", exception);
            throw new PushAuthRequestValidatorException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    OAuth2ErrorCodes.SERVER_ERROR, "Server Error while deriving PAR endpoint URL.", exception);
        }

        return validAudUrls;
    }

    /**
     * Return the tenant domain for a given client.
     */
    public static String getSPTenantDomainFromClientId(String clientId) {

        try {
            OAuthAppDO oAuthAppDO = OAuth2Util.getAppInformationByClientId(clientId);
            return OAuth2Util.getTenantDomainOfOauthApp(oAuthAppDO);
        } catch (IdentityOAuth2Exception | InvalidOAuthClientException e) {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
    }

    /**
     * Decrypt an encrypted request object.
     */
    public static String decrypt(String requestObject, String clientId) throws PushAuthRequestValidatorException {
        EncryptedJWT encryptedJWT;
        try {
            encryptedJWT = EncryptedJWT.parse(requestObject);
            RSAPrivateKey rsaPrivateKey = getRSAPrivateKey(clientId);
            RSADecrypter decrypter = new RSADecrypter(rsaPrivateKey);
            encryptedJWT.decrypt(decrypter);

            JWEObject jweObject = JWEObject.parse(requestObject);
            jweObject.decrypt(decrypter);

            if (jweObject.getPayload() != null && jweObject.getPayload().toString()
                    .split(JWT_PART_DELIMITER).length == NUMBER_OF_PARTS_IN_JWS) {
                return jweObject.getPayload().toString();
            } else {
                return new PlainJWT(encryptedJWT.getJWTClaimsSet()).serialize();
            }

        } catch (JOSEException | IdentityOAuth2Exception | ParseException e) {
            String errorMessage = "Failed to decrypt Request Object";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage + " from " + requestObject, e);
            }
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST, "invalid_request", errorMessage, e);
        }
    }

    /**
     * Get RSA private key from tenant domain for registered client.
     */
    private static RSAPrivateKey getRSAPrivateKey(String clientId) throws IdentityOAuth2Exception {

        String tenantDomain = PushAuthRequestValidatorUtils.getSPTenantDomainFromClientId(clientId);
        int tenantId = OAuth2Util.getTenantId(tenantDomain);
        Key key = OAuth2Util.getPrivateKey(tenantDomain, tenantId);
        return (RSAPrivateKey) key;
    }

    /**
     * Get custom error message for signature validation errors.
     */
    private static String getCustomSignatureValidationErrorMessage(IdentityOAuth2Exception exception) {

        String errorMessage = exception.getCause().getMessage();
        if (StringUtils.isEmpty(errorMessage)) {
            return exception.getMessage();
        }

        if (errorMessage.equalsIgnoreCase("JWT before use time")) {
            return "Invalid not before time. 'nbf' must be a past value.";
        }

        if (errorMessage.equalsIgnoreCase("Expired JWT")) {
            return "Invalid expiry time. 'exp' claim must be a future value.";
        }

        return errorMessage;
    }
}
