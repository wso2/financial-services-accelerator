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

package org.wso2.bfsi.test.framework.request_builder


import org.json.JSONArray
import org.json.JSONObject
import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService
import org.wso2.bfsi.test.framework.constant.Constants

/**
 *  Class for create DCR requests
 */
class RegistrationRequestBuilder {

    private CommonConfigurationService obConfiguration
    private JSONObject claims
    protected int tppNumber

    RegistrationRequestBuilder() {
        obConfiguration = new CommonConfigurationService()
        claims = new JSONObject()
        tppNumber = 0
    }

    /**
     * Tpp number determine which application details are read
     * @param tpp
     * @return
     */
    RegistrationRequestBuilder setTPP(int tpp) {
        this.tppNumber = tpp
        return this
    }

    /**
     * @return current Claims object
     */
    String getClaimsJsonAsString() {
        return claims.toString()
    }

    /**
     * Use separate building method for each attribute and return same class instance
     * Provide more flexible parameter passing method for any number of parameters.
     *
     * add Issuer to the Claims
     * @param clientID
     * @return
     */
    RegistrationRequestBuilder addIssuer(String softwareID = null) {
        String id = softwareID
        if (id == null) {
            id = obConfiguration.getAppDCRSoftwareId(tppNumber)
        }
        claims.put(Constants.ISSUER_KEY, id)
        return this
    }

    /**
     * Add IssuerAt to the Claims
     * @return
     */
    RegistrationRequestBuilder addIssuedAt() {
        long iat = System.currentTimeMillis() / 1000
        claims.put(Constants.ISSUED_AT_KEY, iat)
        return this
    }

    /**
     * Add expire date to the Claims
     * @param expDate
     * @return
     */
    RegistrationRequestBuilder addExpireDate(Long expDate = null) {
        Long exp = expDate
        if (expDate == null) {
            long currentTimeInSeconds = (long) (System.currentTimeMillis() / 1000)
            //expire time is read from configs and converted to milli seconds
            exp = currentTimeInSeconds + 3600
        }
        claims.put(Constants.EXPIRE_DATE_KEY, exp)
        return this
    }

    /**
     * Add jti value to the Claims
     * @param jtiValue
     * @return
     */
    RegistrationRequestBuilder addJti(String jtiValue = null) {
        String jti = jtiValue
        if (jti == null) {
            jti = String.valueOf(System.currentTimeMillis())
        }
        claims.put(Constants.JTI_KEY, jti)
        return this
    }

    /**
     * Add Audience to the Claims
     * @param aud
     * @return
     */
    RegistrationRequestBuilder addAudience(String aud = null) {
        String audience = aud
        if (audience == null) {
            audience = obConfiguration.getConsentAudienceValue()
        }
        claims.put(Constants.AUDIENCE_KEY, audience)
        return this
    }

    /**
     * Add Redirect URI
     * @param uri
     * @return
     */
    RegistrationRequestBuilder addRedirectURI(String uri = null) {
        String redirectURI = uri
        if (redirectURI == null) {
            redirectURI = obConfiguration.getAppDCRRedirectUri(tppNumber)
        }
        def urlList
        try {
            urlList = claims.get(Constants.REDIRECT_URIS_KEY)
            urlList.put(redirectURI)
        } catch (Exception e) {
            urlList = new JSONArray().put(redirectURI)
        }
        claims.put(Constants.REDIRECT_URIS_KEY, urlList)
        return this
    }

    /**
     * Add custom redirect URIs
     * @param uri
     * @return
     */
    RegistrationRequestBuilder addCustomRedirectURI(JSONArray uri) {
        claims.put(Constants.REDIRECT_URIS_KEY, uri)
        return this
    }

    /**
     * Add Alternative Redirect URI
     * @param uri
     * @return
     */
    RegistrationRequestBuilder addAltRedirectURI(String uri = null) {
        String redirectURI = uri
        if (redirectURI == null) {
            redirectURI = obConfiguration.getAppDCRAlternateRedirectUri(tppNumber)
        }
        def urlList
        try {
            urlList = claims.get(Constants.REDIRECT_URIS_KEY)
            urlList.put(redirectURI)
        } catch (Exception e) {
            urlList = new JSONArray().put(redirectURI)
        }
        claims.put(Constants.REDIRECT_URIS_KEY, urlList)
        return this
    }

    /**
     * Add token endpoint auth signing algorithm
     * @param algo
     * @return
     */
    RegistrationRequestBuilder addTokenEndpointAuthSignAlg(String algo = null) {
        String signAlg = algo
        if (signAlg == null) {
            signAlg = "PS256"
        }
        claims.put(Constants.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_KEY, signAlg)
        return this
    }

    /**
     * Add toKen endpoint auth signing algorithm
     * @param method
     * @return
     */
    RegistrationRequestBuilder addTokenEndpointAuthMethod(String method = null) {
        String authMethod = method
        if (authMethod == null) {
            authMethod = "private_key_jwt"
        }
        claims.put(Constants.TOKEN_ENDPOINT_AUTH_METHOD_KEY, authMethod)
        return this
    }

    /**
     * Add Response Type
     * @param type
     * @return
     */
    RegistrationRequestBuilder addGrantType(JSONArray type = null) {
        JSONArray grantType = type
        if (grantType == null) {
            grantType = new JSONArray()
            grantType.put("client_credentials")
            grantType.put("authorization_code")
            grantType.put("refresh_token")
        }
        claims.put(Constants.GRANT_TYPES_KEY, grantType)
        return this
    }

    /**
     * Add Response Type
     * @param uri
     * @return
     */
    RegistrationRequestBuilder addResponseType(String type = null) {
        String resType = type
        if (resType == null) {
            resType = "code id_token"
        }
        def typeList
        try {
            typeList = claims.get(Constants.RESPONSE_TYPES_KEY)
            typeList.put(resType)
        } catch (Exception e) {
            typeList = new JSONArray().put(resType)
        }
        claims.put(Constants.RESPONSE_TYPES_KEY, typeList)
        return this
    }

    /**
     * Add custom Response Types
     * @param uri
     * @return
     */
    RegistrationRequestBuilder addCustomResponseTypes(JSONArray types) {
        claims.put(Constants.RESPONSE_TYPES_KEY, types)
        return this
    }

    /**
     * Add Application Type
     * @param type
     * @return
     */
    RegistrationRequestBuilder addApplicationType(String type = null) {
        String appType = type
        if (appType == null) {
            appType = "web"
        }
        claims.put(Constants.APPLICATION_TYPE_KEY, appType)
        return this
    }

    /**
     * Add ID Token Signed Response Algorithm
     * @param algo
     * @return
     */
    RegistrationRequestBuilder addIDTokenSignedResponseAlg(String algo = null) {
        String resAlgo = algo
        if (resAlgo == null) {
            resAlgo = "PS256"
        }
        claims.put(Constants.ID_TOKEN_SIGNED_RESPONSE_ALG_KEY, resAlgo)
        return this
    }

    /**
     * Add ID Token Encrypted Response Algorithm
     * @param algo
     * @return
     */
    RegistrationRequestBuilder addIDTokenEncResponseAlg(String algo = null) {
        String encAlgo = algo
        if (encAlgo == null) {
            encAlgo = "RSA-OAEP"
        }
        claims.put(Constants.ID_TOKEN_ENCRYPTED_RESPONSE_ALG_KEY, encAlgo)
        return this
    }

    /**
     * Add ID Token Encrypted Response ENC
     * @param enc
     * @return
     */
    RegistrationRequestBuilder addIDTokenEncResponseEnc(String enc = null) {
        String encAlgo = enc
        if (encAlgo == null) {
            encAlgo = "A256GCM"
        }
        claims.put(Constants.ID_TOKEN_ENCRYPTED_RESPONSE_ENC_KEY, encAlgo)
        return this
    }

    /**
     * Add Request Object Signing Algorithm
     * @param algo
     * @return
     */
    RegistrationRequestBuilder addRequestObjectSigningAlgo(String algo = null) {
        String encAlgo = algo
        if (encAlgo == null) {
            encAlgo = "PS256"
        }
        claims.put(Constants.REQUEST_OBJECT_SIGNING_ALG_KEY, encAlgo)
        return this
    }

    /**
     * Add Application Type
     * @param type
     * @return
     */
    RegistrationRequestBuilder addSoftwareStatement(String SSA = null) {
        String softwareSt = SSA
        if (softwareSt == null) {
            softwareSt = new File(obConfiguration.getAppDCRSSAPath()).text
        }
        claims.put(Constants.SOFTWARE_STATEMENT_KEY, softwareSt)
        return this
    }

    /**
     * Add Scope
     * @param scope
     * @return
     */
    RegistrationRequestBuilder addScope(String scope = null) {
        String scopeSt = scope
        if (scopeSt == null) {
            scopeSt = "accounts payments"
        }
        claims.put(Constants.SCOPE_KEY, scopeSt)
        return this
    }

    /**
     * Add custom value to the claims
     * @param key
     * @param value
     * @return
     */
    RegistrationRequestBuilder addCustomValue(String key, Object value) {
        claims.put(key, value)
        return this
    }

    /**
     * Add custom value to the Claims
     * @param key
     * @param value
     * @return
     */
    RegistrationRequestBuilder addCustomJson(String key, JSONObject value) {
        claims.put(key, value)
        return this
    }

    /**
     * Remove value from the Claims
     * @param key
     * @param value
     * @return
     */
    RegistrationRequestBuilder removeKeyValue(String key) {
        claims.remove(key)
        return this
    }

    /**
     * Method for provide basic Claims set
     * @return
     */
    RegistrationRequestBuilder getRegularClaims() {
        return new RegistrationRequestBuilder()
                .addIssuer()
                .addIssuedAt()
                .addExpireDate()
                .addJti()
                .addAudience()
                .addRedirectURI()
                .addAltRedirectURI()
                .addTokenEndpointAuthSignAlg()
                .addTokenEndpointAuthMethod()
                .addGrantType()
                .addResponseType()
                .addApplicationType()
                .addIDTokenSignedResponseAlg()
                .addRequestObjectSigningAlgo()
                .addSoftwareStatement()
    }

}

