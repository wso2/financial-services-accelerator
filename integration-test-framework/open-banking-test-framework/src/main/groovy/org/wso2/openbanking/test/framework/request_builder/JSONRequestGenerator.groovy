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

package org.wso2.openbanking.test.framework.request_builder


import org.json.JSONObject
import org.wso2.openbanking.test.framework.configuration.OBConfigurationService
import org.wso2.openbanking.test.framework.constant.OBConstants

/**
 * Class for generate JSON request objects.
 * Can be used to generate client assertion.
 * Use builder pattern.
 */
class JSONRequestGenerator {

    private OBConfigurationService configuration
    private JSONObject payload
    int tppNumber

    JSONRequestGenerator() {
        configuration = new OBConfigurationService()
        payload = new JSONObject()
        tppNumber = 0
    }

    /**
     * Tpp number determine which application details are read
     * @param tpp
     * @return
     */
    JSONRequestGenerator setTPP(int tpp) {
        this.tppNumber = tpp
        return this
    }

    /**
     * @return current json object
     */
    JSONObject getJsonObject() {
        return payload
    }

    /**
     * Use separate building method for each attribute and return same class instance
     * Provide more flexible parameter passing method for any number of parameters.
     *
     * add Issuer to the client assertion
     * @param clientID
     * @return
     */
    JSONRequestGenerator addIssuer(String clientID = null) {
        String id = clientID
        if (id == null) {
            id = configuration.getAppInfoClientID(tppNumber)
        }
        payload.put(OBConstants.ISSUER_KEY, id)
        return this
    }

    /**
     * Add subject  to the client assertion
     * @param clientID
     * @return
     */
    JSONRequestGenerator addSubject(String clientID = null) {
        String id = clientID
        if (id == null) {
            id = configuration.getAppInfoClientID(tppNumber)
        }
        payload.put(OBConstants.SUBJECT_KEY, id)
        return this
    }

    /**
     * Add Audience  to the client assertion
     * @param aud
     * @return
     */
    JSONRequestGenerator addAudience(String aud = null) {
        String audience = aud
        if (audience == null) {
            audience = configuration.getConsentAudienceValue()
        }
        payload.put(OBConstants.AUDIENCE_KEY, audience)
        return this
    }

    /**
     * Add expire date to the client assertion
     * @param expDate
     * @return
     */
    JSONRequestGenerator addExpireDate(Long expDate = null) {
        Long exp = expDate
        if (expDate == null) {
            long currentTimeInSeconds = (long) (System.currentTimeMillis() / 1000)
            //expire time is read from configs and converted to milli seconds
            exp = currentTimeInSeconds + (Integer.parseInt(configuration.getCommonAccessTokenExpireTime()) * 1000)
        }
        payload.put(OBConstants.EXPIRE_DATE_KEY, exp)
        return this
    }

    /**
     * Add IssuerAt to the client assertion
     * @return
     */
    JSONRequestGenerator addIssuedAt() {
        long iat = System.currentTimeMillis() / 1000
        payload.put(OBConstants.ISSUED_AT_KEY, iat)
        return this
    }

    /**
     * Add jti value to the client assertion
     * @param jtiValue
     * @return
     */
    JSONRequestGenerator addJti(String jtiValue = null) {
        String jti = jtiValue
        if (jti == null) {
            jti = String.valueOf(System.currentTimeMillis())
        }
        payload.put(OBConstants.JTI_KEY, jti)
        return this
    }

    /**
     * Add Response Type
     * @param type
     * @return
     */
    JSONRequestGenerator addResponseType(String type = null) {
        String resType = type
        if (resType == null) {
            resType = OBConstants.AUTH_RESPONSE_TYPE
        }
        payload.put(OBConstants.AUTH_RESPONSE_TYPE_KEY, resType)
        return this
    }

    /**
     * Add Client ID
     * @param client
     * @return
     */
    JSONRequestGenerator addClientID(String client = null) {
        String clientID = client
        if (clientID == null) {
            clientID = configuration.getAppInfoClientID(tppNumber)
        }
        payload.put(OBConstants.CLIENT_ID_KEY, clientID)
        return this
    }

    /**
     * Add Redirect URI
     * @param uri
     * @return
     */
    JSONRequestGenerator addRedirectURI(String uri = null) {
        String redirectURI = uri
        if (redirectURI == null) {
            redirectURI = configuration.getAppInfoRedirectURL(tppNumber)
        }
        payload.put(OBConstants.REDIRECT_URI_KEY, redirectURI)
        return this
    }

    /**
     * Add Scope
     * @param scope
     * @return
     */
    JSONRequestGenerator addScope(String scope) {
        payload.put(OBConstants.SCOPE_KEY, scope)
        return this
    }

    /**
     * Add State
     * @param state
     * @return
     */
    JSONRequestGenerator addState(String state) {
        payload.put(OBConstants.STATE_KEY, state)
        return this
    }

    /**
     * Add Redirect URI
     * @param nonce
     * @return
     */
    JSONRequestGenerator addNonce(String nonce = null) {
        String nonceString = nonce
        if (nonceString == null) {
            nonceString = UUID.randomUUID()
        }
        payload.put(OBConstants.NONCE_KEY, nonceString)
        return this
    }

    /**
     * Add custom value to the client assertion
     * @param key
     * @param value
     * @return
     */
    JSONRequestGenerator addCustomValue(String key, Object value) {
        payload.put(key, value)
        return this
    }

    /**
     * Add custom value to the client assertion
     * @param key
     * @param value
     * @return
     */
    JSONRequestGenerator addCustomJson(String key, JSONObject value) {
        payload.put(key, value)
        return this
    }

}

