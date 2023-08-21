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

package com.wso2.openbanking.accelerator.identity.builders;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.openidconnect.RequestObjectBuilder;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;

import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;
import java.time.Instant;

import static org.wso2.carbon.identity.openidconnect.model.Constants.JWT_PART_DELIMITER;
import static org.wso2.carbon.identity.openidconnect.model.Constants.NUMBER_OF_PARTS_IN_JWE;
import static org.wso2.carbon.identity.openidconnect.model.Constants.NUMBER_OF_PARTS_IN_JWS;

/**
 * Build Request Object from request_uri for authorize call's request object.
 * Object is stored as JWT string in Session DataStore Cache.
 *
 * Works in-coordination with Push Authorization endpoint.
 *
 * To differentiate 'request' and 'request_uri' auth calls, an internal claim is added to the request object.
 */
public class DefaultOBRequestUriRequestObjectBuilder implements RequestObjectBuilder {

    private static final Log log = LogFactory.getLog(DefaultOBRequestUriRequestObjectBuilder.class);
    private static final String PAR_INITIATED_REQ_OBJ = "par_initiated_request_object";

    @Override
    public RequestObject buildRequestObject(String urn, OAuth2Parameters oAuth2Parameters)
            throws RequestObjectException {

        String[] sessionKey = urn.split(":");

        SessionDataCacheKey sessionDataCacheKey = new SessionDataCacheKey(sessionKey[(sessionKey.length - 1)]);
        SessionDataCacheEntry sessionDataCacheEntry = SessionDataCache.getInstance()
                .getValueFromCache(sessionDataCacheKey);
        RequestObject requestObject = new RequestObject();

        if (sessionDataCacheEntry == null) {
            throw new RequestObjectException(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid request URI");
        }

        // Making a copy of requestObjectParam to prevent editing initial reference
        String requestObjectParamValue = sessionDataCacheEntry.getoAuth2Parameters().getEssentialClaims();

        // validate expiry
        String[] jwtWithExpiry = requestObjectParamValue.split(":");
        if (Instant.now().getEpochSecond() > Long.parseLong(jwtWithExpiry[1])) {
            throw new RequestObjectException(OAuth2ErrorCodes.INVALID_REQUEST, "Expired request URI");
        }

        requestObjectParamValue = jwtWithExpiry[0];

        if (isEncrypted(requestObjectParamValue)) {
            requestObjectParamValue = decrypt(requestObjectParamValue, oAuth2Parameters);
            if (StringUtils.isEmpty(requestObjectParamValue)) {
                return requestObject;
            }
        }

        setRequestObjectValues(requestObjectParamValue, requestObject);
        return requestObject;

    }

    @Override
    public String decrypt(String requestObject, OAuth2Parameters oAuth2Parameters) throws RequestObjectException {
        EncryptedJWT encryptedJWT;
        try {
            encryptedJWT = EncryptedJWT.parse(requestObject);
            RSAPrivateKey rsaPrivateKey = getRSAPrivateKey(oAuth2Parameters);
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
            log.error(errorMessage + " from " + requestObject, e);
            throw new RequestObjectException(RequestObjectException.ERROR_CODE_INVALID_REQUEST, errorMessage);
        }
    }

    /**
     * Retrieve RSA private key.
     *
     * @param oAuth2Parameters oAuth2Parameters
     * @return RSA private key
     */
    private RSAPrivateKey getRSAPrivateKey(OAuth2Parameters oAuth2Parameters) throws IdentityOAuth2Exception {

        String tenantDomain = getTenantDomainForDecryption(oAuth2Parameters);
        int tenantId = OAuth2Util.getTenantId(tenantDomain);
        Key key = OAuth2Util.getPrivateKey(tenantDomain, tenantId);
        return (RSAPrivateKey) key;
    }

    /**
     * Get tenant domain from oAuth2Parameters.
     *
     * @param oAuth2Parameters oAuth2Parameters
     * @return Tenant domain
     */
    private String getTenantDomainForDecryption(OAuth2Parameters oAuth2Parameters) {

        if (StringUtils.isNotEmpty(oAuth2Parameters.getTenantDomain())) {
            return oAuth2Parameters.getTenantDomain();
        }
        return MultitenantConstants.SUPER_TENANT_NAME;
    }

    /**
     * Check whether given request object is encrypted.
     *
     * @param requestObject request object string
     * @return true if its encrypted
     */
    private boolean isEncrypted(String requestObject) {

        return requestObject.split(JWT_PART_DELIMITER).length == NUMBER_OF_PARTS_IN_JWE;
    }

    /**
     * Set retrieved claims to the request object instance.
     *
     * @param requestObjectString request object string
     * @param requestObjectInstance request object instance
     * @return
     */
    private void setRequestObjectValues(String requestObjectString, RequestObject requestObjectInstance) throws
            RequestObjectException {

        try {
            JOSEObject jwt = JOSEObject.parse(requestObjectString);
            if (jwt.getHeader().getAlgorithm() == null || jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.NONE)) {
                requestObjectInstance.setPlainJWT(PlainJWT.parse(requestObjectString));
            } else {
                requestObjectInstance.setSignedJWT(SignedJWT.parse(requestObjectString));
            }
            JSONObject claimSet = requestObjectInstance.getClaimsSet().toJSONObject();
            claimSet.put(PAR_INITIATED_REQ_OBJ, "true");
            requestObjectInstance.setClaimSet(JWTClaimsSet.parse(claimSet));
        } catch (ParseException e) {
            String errorMessage = "No Valid JWT is found for the Request Object.";
            log.error(errorMessage, e);
            throw new RequestObjectException(OAuth2ErrorCodes.INVALID_REQUEST, errorMessage);
        }
    }
}
