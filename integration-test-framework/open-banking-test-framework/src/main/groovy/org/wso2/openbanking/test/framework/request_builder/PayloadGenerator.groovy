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


import org.wso2.openbanking.test.framework.configuration.OBConfigurationService
import org.wso2.openbanking.test.framework.constant.OBConstants
import org.wso2.openbanking.test.framework.utility.OBTestUtil

/**
 * Class for generate JWT payload
 */
        class PayloadGenerator {

    private OBConfigurationService configuration
    private int tppNumber
    private String payload;
    private static String delimiter = "&"
    StringBuilder sb

    PayloadGenerator() {
        configuration = new OBConfigurationService()
        tppNumber = 0
        payload = ""
        sb = new StringBuilder()
    }

    /**
     * Set tpp number
     * Tpp number will determine which application details will be retrieved
     * @param tpp
     * @return
     */
    PayloadGenerator setTPP(int tpp) {
        this.tppNumber = tpp
        return this
    }

    /**
     * Use separate building method for each attribute and return same class instance
     * Provide more flexible parameter passing method for any number of parameters.
     * If method parameters not given, required details will be get from Constants and configuration classes
     */
    /**
     * Add any key value pair to the payload
     * @param key
     * @param value
     * @return
     */
    PayloadGenerator addCustomValue(String key, String value) {
        sb.append(key + "=" + value + delimiter)
        return this
    }

    /**
     * Add grant type for payload
     * @param grantType
     * @return
     */
    PayloadGenerator addGrantType(String grantType = null) {
        String gType = grantType
        if (gType == null) {
            gType = OBConstants.CLIENT_CREDENTIALS
        }
        sb.append(OBConstants.GRANT_TYPE_KEY + "=" + gType + delimiter)
        return this
    }

    /**
     * Add Scopes for payload
     * @param scopes
     * @return
     */
    PayloadGenerator addScopes(List<String> scopes = null) {
        List<String> scopesList = scopes
        if (scopesList == null) {
            scopesList = OBConstants.ACCOUNTS_DEFAULT_SCOPES
        }
        sb.append(OBConstants.SCOPE_KEY + "=" + OBTestUtil.getParamListAsString(scopesList, ' ') + delimiter)
        return this
    }

    /**
     * Add Client Assertion Type for payload
     * @param type
     * @return
     */
    PayloadGenerator addClientAsType(String type = null) {
        String clientAsType = type
        if (clientAsType == null) {
            clientAsType = OBConstants.CLIENT_ASSERTION_TYPE
        }
        sb.append(OBConstants.CLIENT_ASSERTION_TYPE_KEY + "=" + clientAsType + delimiter)
        return this
    }

    /**
     * Add Redirect URI for payload
     * @param redirectURI
     * @return
     */
    PayloadGenerator addRedirectUri(String redirectURI = null) {
        String redirect = redirectURI
        if (redirect == null) {
            redirect = configuration.getAppInfoRedirectURL(tppNumber)
        }
        sb.append(OBConstants.REDIRECT_URI_KEY + "=" + redirect + delimiter)
        return this
    }

    /**
     * Add Client ID for payload
     * @param client
     * @return
     */
    PayloadGenerator addClientID(String client = null) {
        String clientID = client
        if (clientID == null) {
            clientID = configuration.getAppInfoClientID(tppNumber)
        }
        sb.append(OBConstants.CLIENT_ID_KEY + "=" + clientID + delimiter)
        return this
    }

    /**
     * Add Client Assertion for payload
     * @param clientAssertion
     * @return
     */
    PayloadGenerator addClientAssertion(String clientAssertion) {
        sb.append(OBConstants.CLIENT_ASSERTION_KEY + "=" + clientAssertion + delimiter)
        return this
    }

    /**
     * Add UserName for payload
     * @param userName
     * @return
     */
    PayloadGenerator addUserName(String userName) {
        sb.append(OBConstants.USER_NAME + "=" + userName + delimiter)
        return this
    }

    /**
     * Add Password for payload
     * @param pwd
     * @return
     */
    PayloadGenerator addPassword(String pwd) {
        sb.append(OBConstants.PASSWORD + "=" + pwd + delimiter)
        return this
    }

    /**
     * Add Refresh token for payload
     * @param rToken
     * @return
     */
    PayloadGenerator addRefreshToken(String rToken) {
        sb.append(OBConstants.REFRESH_TOKEN + "=" + rToken + delimiter)
        return this
    }

    /**
     * Add Code for payload
     * @param code
     * @return
     */
    PayloadGenerator addCode(String code) {
        sb.append(OBConstants.CODE_KEY + "=" + code + delimiter)
        return this
    }

    /**
     * Add Code verifier for payload
     * @param codeVerifier
     * @return
     */
    PayloadGenerator addCodeVerifier(String codeVerifier) {
        sb.append(OBConstants.CODE_VERIFIER_KEY + "=" + codeVerifier + delimiter)
        return this
    }

    /**
     * Return currently built Payload
     * @return
     */
    String getPayload() {
        sb.deleteCharAt(sb.length() - 1)
        return sb.toString()
    }

}

