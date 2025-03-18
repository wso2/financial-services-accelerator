/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.framework.request_builder

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.oauth2.sdk.id.State
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants

/**
 * Authorization Requests Builder class.
 */
class AuthorisationBuilder {

    private AuthorizationRequest request
    private ConfigurationService configuration = new ConfigurationService()

    private URI endpoint
    private ResponseType responseType
    private ClientID clientId
    private URI redirectURI
    private State state
    private int tppNumber

    private params = [
            (ConnectorTestConstants.ENDPOINT_PARAMETER)     : configuration.getISServerUrl() + ConnectorTestConstants.AUTH_URL,
            (ConnectorTestConstants.RESPONSE_TYPE_PARAMETER): ConnectorTestConstants.AUTH_RESPONSE_TYPE,
            (ConnectorTestConstants.REDIRECT_URI_PARAMETER) : configuration.getAppInfoRedirectURL(),
            (ConnectorTestConstants.STATE_PARAMETER)        : UUID.randomUUID().toString(),
            (ConnectorTestConstants.NONCE_PARAMETER)        : UUID.randomUUID().toString(),
            (ConnectorTestConstants.SCOPE_PARAMETER)        : getScopeString([ConnectorTestConstants.ApiScope.ACCOUNTS]),
            (ConnectorTestConstants.AUD_PARAMETER)          : configuration.getConsentAudienceValue()
    ]

    /**
     * Accelerator Authorisation Builder for Default Authorisation Flow
     * @param clientId
     * @param consentId
     * @param scopes
     * @param isRegulatory
     */
    AuthorizationRequest getAuthorizationRequest(String clientId = getClientID().getValue(), String consentId,
                                                 List<ConnectorTestConstants.ApiScope> scopes, boolean isRegulatory) {

        JWTGenerator generator = new JWTGenerator()
        String scopeString = getScopeString(scopes)

        if(isRegulatory){
            request = new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                    .responseType(ResponseType.parse(ConnectorTestConstants.AUTH_RESPONSE_TYPE))
                    .endpointURI(getEndpoint())
                    .redirectionURI(getRedirectURI())
                    .requestObject(generator.getSignedAuthRequestObject(scopeString, new ClientID(clientId),
                            new Issuer(clientId), consentId))
                    .scope(new Scope(scopeString))
                    .state(getState())
                    .customParameter("nonce", ConnectorTestConstants.NONCE_PARAMETER)
                    .build()
        }else{
            request = new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                    .responseType(ResponseType.parse(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER))
                    .endpointURI(getEndpoint())
                    .redirectionURI(getRedirectURI())
                    .scope(new Scope(scopeString))
                    .state(getState())
                    .customParameter("nonce", ConnectorTestConstants.NONCE_PARAMETER)
                    .build()
        }
    }

    /**
     * Accelerator Authorisation Builder for Default Authorisation Flow
     * @param clientId
     * @param consentId
     * @param scopes
     * @param isRegulatory
     */
    AuthorizationRequest getAuthorizationRequestWithCustomValues(String clientId = getClientID().getValue(),
                                                                 String consentId, List<ConnectorTestConstants.ApiScope> scopes,
                                                                 boolean isRegulatory, String requestParamKey = "",
                                                                 String requestParamValue = "") {

        params.put(requestParamKey, requestParamValue)

        JWTGenerator generator = new JWTGenerator()


        if (isRegulatory) {
            request = new AuthorizationRequest.Builder(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)),
                    new ClientID(clientId))
                    .responseType(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)))
                    .endpointURI(new URI(params.get(ConnectorTestConstants.ENDPOINT_PARAMETER)))
                    .redirectionURI(new URI(params.get(ConnectorTestConstants.REDIRECT_URI_PARAMETER)))
                    .requestObject(generator.getSignedAuthRequestObject(getScopeString(scopes), new ClientID(clientId),
                            new Issuer(clientId), consentId))
                    .scope(new Scope(params.get(ConnectorTestConstants.SCOPE_PARAMETER)))
                    .state(new State(params.get(ConnectorTestConstants.STATE_PARAMETER)))
                    .customParameter("nonce", params.get(ConnectorTestConstants.NONCE_PARAMETER))
                    .build()

        } else {
            request = new AuthorizationRequest.Builder(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)),
                    new ClientID(clientId))
                    .responseType(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)))
                    .endpointURI(new URI(params.get(ConnectorTestConstants.ENDPOINT_PARAMETER)))
                    .redirectionURI(new URI(params.get(ConnectorTestConstants.REDIRECT_URI_PARAMETER)))
                    .scope(new Scope(params.get(ConnectorTestConstants.SCOPE_PARAMETER)))
                    .state(new State(params.get(ConnectorTestConstants.STATE_PARAMETER)))
                    .customParameter("nonce", params.get(ConnectorTestConstants.NONCE_PARAMETER))
                    .build()
        }
    }

    /**
     * Accelerator Authorisation Builder for Default Authorisation Flow
     * @param clientId
     * @param consentId
     * @param scopes
     * @param isRegulatory
     */
    AuthorizationRequest getAuthorizationRequestWithCustomResponseType(String clientId = getClientID().getValue(), String consentId,
                                                                       List<ConnectorTestConstants.ApiScope> scopesForRequestObject,
                                                                       List<ConnectorTestConstants.ApiScope> scopesForRequest,
                                                                       boolean isRegulatory) {

        JWTGenerator generator = new JWTGenerator()
        String scopeStringForRequestObj = "${ConnectorTestConstants.ApiScope.OPEN_ID.scopeString} " +
                "${String.join(" ", scopesForRequestObject.collect({ it.scopeString }))}"
        String scopeStringForRequest = "${ConnectorTestConstants.ApiScope.OPEN_ID.scopeString} " +
                "${String.join(" ", scopesForRequest.collect({ it.scopeString }))}"


        if(isRegulatory){
            request = new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                    .responseType(ResponseType.parse(ConnectorTestConstants.AUTH_RESPONSE_TYPE))
                    .endpointURI(getEndpoint())
                    .redirectionURI(getRedirectURI())
                    .requestObject(generator.getSignedAuthRequestObject(scopeStringForRequestObj, new ClientID(clientId),
                            new Issuer(clientId), consentId))
                    .scope(new Scope(scopeStringForRequest))
                    .state(getState())
                    .customParameter("nonce", ConnectorTestConstants.NONCE_PARAMETER)
                    .build()
        }else{
            request = new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                    .responseType(ResponseType.parse(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER))
                    .endpointURI(getEndpoint())
                    .redirectionURI(getRedirectURI())
                    .scope(new Scope(scopeStringForRequest))
                    .state(getState())
                    .customParameter("nonce", ConnectorTestConstants.NONCE_PARAMETER)
                    .build()
        }
    }

    /***
     * Build Authorization Request Without Redirect Uri
     * @param clientId
     * @param scopes
     * @return authorization_response
     */
    AuthorizationRequest getOAuthRequestWithoutRedirectUri(String clientId, List<ConnectorTestConstants.ApiScope> scopes, String consentId) {

        JWTGenerator generator = new JWTGenerator()
        return new AuthorizationRequest.Builder(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)),
                new ClientID(clientId))
                .responseType(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)))
                .endpointURI(new URI(params.get(ConnectorTestConstants.ENDPOINT_PARAMETER)))
                .requestObject(generator.getSignedAuthRequestObject(getScopeString(scopes), new ClientID(clientId),
                        new Issuer(clientId), consentId))
                .scope(new Scope(params.get(ConnectorTestConstants.SCOPE_PARAMETER)))
                .state(new State(params.get(ConnectorTestConstants.STATE_PARAMETER)))
                .customParameter("nonce", params.get(ConnectorTestConstants.NONCE_PARAMETER))
                .build()
    }

    /***
     * Build Authorization Request Without Scope
     * @param clientId
     * @param scopes
     * @return authorization_response
     */
    AuthorizationRequest getOAuthRequestWithoutScope(String clientId, List<ConnectorTestConstants.ApiScope> scopes, String consentId) {

        JWTGenerator generator = new JWTGenerator()
        return new AuthorizationRequest.Builder(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)),
                new ClientID(clientId))
                .responseType(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)))
                .endpointURI(new URI(params.get(ConnectorTestConstants.ENDPOINT_PARAMETER)))
                .redirectionURI(new URI(params.get(ConnectorTestConstants.REDIRECT_URI_PARAMETER)))
                .requestObject(generator.getSignedAuthRequestObject(getScopeString(scopes), new ClientID(clientId),
                        new Issuer(clientId), consentId))
                .state(new State(params.get(ConnectorTestConstants.STATE_PARAMETER)))
                .customParameter("nonce", params.get(ConnectorTestConstants.NONCE_PARAMETER))
                .build()
    }

    /***
     * Build Authorization Request with defined cert
     * @param clientId
     * @param scopes
     * @return authorization_response
     */
    AuthorizationRequest getOAuthRequestWithDefinedCert(String clientId, List<ConnectorTestConstants.ApiScope> scopes,
                                                        String appKeystoreLocation, String appKeystorePassword,
                                                        String appKeystoreAlias, String consentId) {

        JWTGenerator generator = new JWTGenerator()
        return new AuthorizationRequest.Builder(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)),
                new ClientID(clientId))
                .responseType(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)))
                .endpointURI(new URI(params.get(ConnectorTestConstants.ENDPOINT_PARAMETER)))
                .redirectionURI(new URI(params.get(ConnectorTestConstants.REDIRECT_URI_PARAMETER)))
                .requestObject(generator.getSignedRequestObjectWithDefinedCert(getScopeString(scopes),
                        new ClientID(clientId), new Issuer(clientId), consentId, appKeystoreLocation, appKeystorePassword,
                        appKeystoreAlias))
                .scope(new Scope(params.get(ConnectorTestConstants.SCOPE_PARAMETER)))
                .state(new State(params.get(ConnectorTestConstants.STATE_PARAMETER)))
                .customParameter("nonce", params.get(ConnectorTestConstants.NONCE_PARAMETER))
                .build()
    }

    /**
     * Accelerator Authorisation Builder for Default Authorisation Flow
     * @param clientId
     * @param consentId
     * @param scopes
     * @param isRegulatory
     */
    AuthorizationRequest getAuthorizationRequestWithCustomAud(String clientId = getClientID().getValue(), String consentId,
                                                              List<ConnectorTestConstants.ApiScope> scopes, boolean isRegulatory) {

        JWTGenerator generator = new JWTGenerator()
        String scopeString = getScopeString(scopes)

        if(isRegulatory){
            request = new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                    .responseType(ResponseType.parse(ConnectorTestConstants.AUTH_RESPONSE_TYPE))
                    .endpointURI(getEndpoint())
                    .redirectionURI(getRedirectURI())
                    .requestObject(generator.getSignedAuthRequestObject(scopeString, new ClientID(clientId),
                            new Issuer(clientId), consentId, ConnectorTestConstants.STATE_PARAMETER,
                            ConnectorTestConstants.NONCE_PARAMETER, ConnectorTestConstants.RESPONSE_TYPE_PARAMETER,
                            "localhost:8243/aud", ConnectorTestConstants.REDIRECT_URI_PARAMETER))
                    .scope(new Scope(scopeString))
                    .state(getState())
                    .customParameter("nonce", ConnectorTestConstants.NONCE_PARAMETER)
                    .build()
        }else{
            request = new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                    .responseType(ResponseType.parse(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER))
                    .endpointURI(getEndpoint())
                    .redirectionURI(getRedirectURI())
                    .scope(new Scope(scopeString))
                    .state(getState())
                    .customParameter("nonce", ConnectorTestConstants.NONCE_PARAMETER)
                    .build()
        }
    }

    /**
     * Accelerator Authorisation Builder for Default Authorisation Flow
     * @param clientId
     * @param consentId
     * @param scopes
     * @param isRegulatory
     */
    AuthorizationRequest getAuthorizationRequestWithCustomAlgorithm(String clientId = getClientID().getValue(), String consentId,
                                                                    List<ConnectorTestConstants.ApiScope> scopes, boolean isRegulatory) {

        JWTGenerator generator = new JWTGenerator()
        String scopeString = getScopeString(scopes)

        request = new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                .responseType(ResponseType.parse(ConnectorTestConstants.AUTH_RESPONSE_TYPE))
                .endpointURI(getEndpoint())
                .redirectionURI(getRedirectURI())
                .requestObject(generator.getSignedAuthRequestObjectWithCustomAlgorithm(scopeString, new ClientID(clientId),
                        new Issuer(clientId), consentId))
                .scope(new Scope(scopeString))
                .state(getState())
                .customParameter("nonce", ConnectorTestConstants.NONCE_PARAMETER)
                .build()
    }

    /**
     * Accelerator Authorisation Builder for Default Authorisation Flow
     * @param clientId
     * @param consentId
     * @param scopes
     * @param isRegulatory
     */
    AuthorizationRequest getAuthorizationRequestWithoutOpenIDScope(String clientId = getClientID().getValue(), String consentId,
                                                                   List<ConnectorTestConstants.ApiScope> scopes, boolean isRegulatory) {

        JWTGenerator generator = new JWTGenerator()
        String scopeString = getScopeStringWithoutOpenIdScope(scopes)

        request = new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                .responseType(ResponseType.parse(ConnectorTestConstants.AUTH_RESPONSE_TYPE))
                .endpointURI(getEndpoint())
                .redirectionURI(getRedirectURI())
                .requestObject(generator.getSignedAuthRequestObject(scopeString, new ClientID(clientId),
                        new Issuer(clientId), consentId))
                .scope(new Scope(scopeString))
                .state(getState())
                .customParameter("nonce", ConnectorTestConstants.NONCE_PARAMETER)
                .build()
    }

    /**
     * Provide authorization request
     * @return
     */
    AuthorizationRequest getRequest() {
        return request
    }

    /**
     * Getter for other functions
     */
    private ResponseType getResponseType() {
        if (responseType == null) {
            responseType = new ResponseType(ConnectorTestConstants.AUTH_RESPONSE_TYPE)
        }
        return responseType
    }

    ClientID getClientID() {
        if (clientId == null) {
            clientId = new ClientID(configuration.getAppInfoClientID(tppNumber))
        }
        return clientId
    }

    private URI getRedirectURI() {
        if (redirectURI == null) {
            redirectURI = new URI(configuration.getAppInfoRedirectURL(tppNumber))
        }
        return redirectURI
    }

    private State getState() {
        if (state == null) {
            state = new State(UUID.randomUUID().toString())
        }
        return state
    }

    private URI getEndpoint() {
        if (endpoint == null) {
            endpoint = new URI("${configuration.getISServerUrl()}/oauth2/authorize/")
        }
        return endpoint
    }

    /**
     * Setter of parameters
     */
    void setEndpoint(String endpoint) {
        this.endpoint = new URI(endpoint)
    }

    void setResponseType(String responseType) {
        this.responseType = new ResponseType(responseType)
    }

    void setClientID(String clientID) {
        this.clientId = new ClientID(clientID)
    }

    void setTppNumber(int tpp) {
        this.tppNumber = tpp
    }

    void setRedirectURI(String redirectURI) {
        this.redirectURI = new URI(redirectURI)
    }

    void setState(String state) {
        this.state = new State(state)
    }

    public static String getScopeString(List<ConnectorTestConstants.ApiScope> scopes) {
        return "${ConnectorTestConstants.ApiScope.OPEN_ID.scopeString} " +
                "${String.join(" ", scopes.collect({ it.scopeString }))}"
    }

    public static String getScopeStringWithoutOpenIdScope(List<ConnectorTestConstants.ApiScope> scopes) {
        return String.join(" ", scopes.collect({ it.scopeString }))
    }

    /**
     * Get Authorization Request Without Openid Scope in Request Object
     * @param clientId
     * @param consentId
     * @param scopes
     * @param isRegulatory
     * @param requestParamKey
     * @param requestParamValue
     * @return
     */
    AuthorizationRequest getAuthRequestWithoutOpenidScopeInReqObj(String clientId = getClientID().getValue(),
                                                                 String consentId, List<ConnectorTestConstants.ApiScope> scopes,
                                                                 boolean isRegulatory, String requestParamKey = "",
                                                                 String requestParamValue = "") {

        params.put(requestParamKey, requestParamValue)

        JWTGenerator generator = new JWTGenerator()

            request = new AuthorizationRequest.Builder(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)),
                    new ClientID(clientId))
                    .responseType(new ResponseType(params.get(ConnectorTestConstants.RESPONSE_TYPE_PARAMETER)))
                    .endpointURI(new URI(params.get(ConnectorTestConstants.ENDPOINT_PARAMETER)))
                    .redirectionURI(new URI(params.get(ConnectorTestConstants.REDIRECT_URI_PARAMETER)))
                    .requestObject(generator.getSignedAuthRequestObject(getScopeStringWithoutOpenIdScope(scopes), new ClientID(clientId),
                            new Issuer(clientId), consentId))
                    .scope(new Scope(params.get(ConnectorTestConstants.SCOPE_PARAMETER)))
                    .state(new State(params.get(ConnectorTestConstants.STATE_PARAMETER)))
                    .customParameter("nonce", params.get(ConnectorTestConstants.NONCE_PARAMETER))
                    .build()
    }
}
