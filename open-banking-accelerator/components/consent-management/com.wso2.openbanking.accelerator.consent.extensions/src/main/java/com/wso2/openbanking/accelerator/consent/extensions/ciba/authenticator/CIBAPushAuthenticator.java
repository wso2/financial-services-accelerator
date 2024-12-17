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

package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.builder.ConsentStepsBuilder;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.model.CIBAAuthenticationEndpointErrorResponse;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentCache;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionExporter;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authenticator.push.PushAuthenticator;
import org.wso2.carbon.identity.application.authenticator.push.common.PushAuthContextManager;
import org.wso2.carbon.identity.application.authenticator.push.common.impl.PushAuthContextManagerImpl;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * CIBA Push Authenticator for sending push notifications to authentication device.
 */
@Deprecated
public class CIBAPushAuthenticator extends PushAuthenticator {

    private static final Log log = LogFactory.getLog(CIBAPushAuthenticator.class);
    private static final long serialVersionUID = 6106269076155338045L;

    private static List<ConsentRetrievalStep> consentRetrievalSteps = null;
    private static List<ConsentPersistStep> consentPersistSteps = null;

    public CIBAPushAuthenticator() {
        initializeConsentSteps();
    }

    @Override
    public String getFriendlyName() {

        return CIBAPushAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {

        return CIBAPushAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    /**
     * Initialize consent builder.
     */
    public static synchronized void initializeConsentSteps() {

        if (consentRetrievalSteps == null || consentPersistSteps == null) {
            ConsentStepsBuilder consentStepsBuilder = ConsentExtensionExporter.getConsentStepsBuilder();

            if (consentStepsBuilder != null) {
                consentRetrievalSteps = consentStepsBuilder.getConsentRetrievalSteps();
                consentPersistSteps = consentStepsBuilder.getConsentPersistSteps();
            }

            if (consentRetrievalSteps != null && !consentRetrievalSteps.isEmpty()) {
                log.info("Consent retrieval steps are not null or empty");
            } else {
                log.warn("Consent retrieval steps are null or empty");
            }
            if (consentPersistSteps != null && !consentPersistSteps.isEmpty()) {
                log.info("Consent persist steps are not null or empty");
            } else {
                log.warn("Consent persist steps are null or empty");
            }
        } else {
            log.debug("Retrieval and persist steps are available");
        }
    }

    /**
     * Execute consent retrieval steps.
     *
     * @param consentData Consent Data
     * @param jsonObject Json object to store consent data
     * @throws ConsentException when an error occurs while executing retrieval steps
     */
    protected void executeRetrieval(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        for (ConsentRetrievalStep step : consentRetrievalSteps) {
            if (log.isDebugEnabled()) {
                log.debug("Executing retrieval step " + step.getClass().toString());
            }
            step.execute(consentData, jsonObject);
        }
    }

    /**
     * Retrieve consent.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param sessionDataKey Session data key
     * @return Consent data
     * @throws ConsentException when an error occurs while retrieving consent
     */
    protected JSONObject retrieveConsent(HttpServletRequest request, HttpServletResponse response,
                                  String sessionDataKey) throws ConsentException {

        String loggedInUser;
        String app;
        String spQueryParams;
        String scopeString;

        SessionDataCacheEntry cacheEntry = ConsentCache.getCacheEntryFromSessionDataKey(sessionDataKey);
        OAuth2Parameters oAuth2Parameters = cacheEntry.getoAuth2Parameters();
        URI redirectURI;
        try {
            redirectURI = new URI(oAuth2Parameters.getRedirectURI());
        } catch (URISyntaxException e) {
            //Unlikely to happen. In case it happens, error response is sent
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Invalid redirect URI");
        }
        //Extracting client ID for regulatory identification and redirect URI for error redirects
        String clientId = oAuth2Parameters.getClientId();
        String state = oAuth2Parameters.getState();

        Map<String, Serializable> sensitiveDataMap =
                ConsentExtensionUtils.getSensitiveDataWithConsentKey(sessionDataKey);

        if ("false".equals(sensitiveDataMap.get(CIBAPushAuthenticatorConstants.IS_ERROR))) {
            loggedInUser = (String) sensitiveDataMap.get(CIBAPushAuthenticatorConstants.LOGGED_IN_USER);
            app = (String) sensitiveDataMap.get(CIBAPushAuthenticatorConstants.APPLICATION);
            spQueryParams = (String) sensitiveDataMap.get(CIBAPushAuthenticatorConstants.SP_QUERY_PARAMS);
            scopeString = (String) sensitiveDataMap.get(CIBAPushAuthenticatorConstants.SCOPE);
        } else {
            String isError = (String) sensitiveDataMap.get(CIBAPushAuthenticatorConstants.IS_ERROR);
            //Have to throw standard error because cannot access redirect URI with this error
            log.error("Error while getting endpoint parameters. " + isError);
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR,
                    CIBAPushAuthenticatorConstants.ERROR_SERVER_ERROR, state);
        }

        JSONObject jsonObject = new JSONObject();
        ConsentData consentData = createConsentData(sessionDataKey, loggedInUser, spQueryParams, scopeString, app,
                request);
        consentData.setSensitiveDataMap(sensitiveDataMap);
        consentData.setRedirectURI(redirectURI);

        if (clientId == null) {
            log.error("Client Id not available");
            //Unlikely error. Included just in case.
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR,
                    CIBAPushAuthenticatorConstants.ERROR_SERVER_ERROR, state);
        }
        consentData.setClientId(clientId);
        consentData.setState(state);

        try {
            consentData.setRegulatory(IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId));
        } catch (OpenBankingException e) {
            log.error("Error while getting regulatory data", e);
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR, "Error while obtaining regulatory data",
                    state);
        }

        executeRetrieval(consentData, jsonObject);
        if (consentData.getType() == null || consentData.getApplication() == null) {
            log.error(CIBAPushAuthenticatorConstants.ERROR_NO_TYPE_AND_APP_DATA);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    CIBAPushAuthenticatorConstants.ERROR_SERVER_ERROR, state);
        }
        ConsentExtensionUtils.setCommonDataToResponse(consentData, jsonObject);
        try {
            ConsentCache.addConsentDataToCache(sessionDataKey, consentData);
        } catch (ConsentManagementException e) {
            log.error("Error while adding consent data to cache", e);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    CIBAPushAuthenticatorConstants.ERROR_SERVER_ERROR, state);
        }
        return jsonObject;
    }

    @Generated(message = "This method is separated for unit testing purposes")
    protected ConsentData createConsentData(String sessionDataKey, String loggedInUser, String spQueryParams,
                                    String scopeString, String app, HttpServletRequest request) {
        return new ConsentData(sessionDataKey, loggedInUser, spQueryParams, scopeString, app,
                ConsentExtensionUtils.getHeaders(request));
    }

    /**
     * Get the authenticated user.
     *
     * @param request Push authenticator HTTP request
     * @return Authenticated User
     */
    @Override
    protected AuthenticatedUser getAuthenticatedUser(HttpServletRequest request) {

        // In OB CIBA, only this Push Authenticator IDP is expected to be executed during the CIBA auth flow
        // Hence, the login_hint attribute in the CIBA request object is used to identify the user
        return AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(request.
                getParameter(CIBAPushAuthenticatorConstants.LOGIN_HINT));
    }

    @Generated(message = "This method is separated for unit testing purposes")
    protected AuthenticationContext getAutenticationContext(String sessionDataKey) {
        PushAuthContextManager contextManager = new PushAuthContextManagerImpl();

        return contextManager.getContext(sessionDataKey);
    }

    /**
     * OB specific implementation to retrieve consent data.
     * @param sessionDataKey Session data key
     * @return consent data
     * @throws AuthenticationFailedException  Authentication failed exception
     */
    @Override
    protected Optional<String> getAdditionalInfo(HttpServletRequest request, HttpServletResponse response,
                                                 String sessionDataKey) throws AuthenticationFailedException {

        AuthenticationContext context = getAutenticationContext(sessionDataKey);

        // update the authentication context with required values for OB specific requirements
        try {
            String queryParams = FrameworkUtils
                    .getQueryStringWithFrameworkContextId(context.getQueryParams(), context.getCallerSessionKey(),
                            context.getContextIdentifier());
            Map<String, String> params = splitQuery(queryParams);
            handlePreConsent(context, params);
        } catch (UnsupportedEncodingException e) {
            throw new AuthenticationFailedException("Error occurred when processing the request object", e);
        }

        SessionDataCacheKey cacheKey = ConsentCache.getCacheKey(sessionDataKey);
        SessionDataCacheEntry cacheEntry = ConsentCache.getCacheEntryFromCacheKey(cacheKey);

        cacheEntry.setLoggedInUser(context.getSubject());
        SessionDataCache.getInstance().addToCache(cacheKey, cacheEntry);

        // Authentication context is added to cache as it is obtained from the cache in a later step by the Parameter
        // Resolver object
        AuthenticationContextCache.getInstance().addToCache(
                new AuthenticationContextCacheKey(sessionDataKey), new AuthenticationContextCacheEntry(context));

        JSONObject additionalInfo = retrieveConsent(request, response, sessionDataKey);
        String bindingMessage = request.getParameter(CIBAPushAuthenticatorConstants.BINDING_MESSAGE);
        if (StringUtils.isNotEmpty(bindingMessage)) {
            additionalInfo.put(CIBAPushAuthenticatorConstants.BINDING_MESSAGE, bindingMessage);
        }
        return Optional.ofNullable(additionalInfo.toJSONString());
    }

    /**
     * set attributes to context which will be required to prompt the consent page.
     *
     * @param context authentication context
     * @param  params query params
     */
    protected void handlePreConsent(AuthenticationContext context, Map<String, String> params) {
        ServiceProvider serviceProvider = context.getSequenceConfig().getApplicationConfig().getServiceProvider();

        context.addEndpointParam(CIBAPushAuthenticatorConstants.LOGGED_IN_USER,
                params.get(CIBAPushAuthenticatorConstants.LOGIN_HINT));
        context.addEndpointParam(CIBAPushAuthenticatorConstants.USER_TENANT_DOMAIN,
                "@carbon.super");
        context.addEndpointParam(CIBAPushAuthenticatorConstants.REQUEST,
                params.get(CIBAPushAuthenticatorConstants.REQUEST_OBJECT));
        context.addEndpointParam(CIBAPushAuthenticatorConstants.SCOPE,
                params.get(CIBAPushAuthenticatorConstants.SCOPE));
        context.addEndpointParam(CIBAPushAuthenticatorConstants.APPLICATION, serviceProvider.getApplicationName());
        context.addEndpointParam(CIBAPushAuthenticatorConstants.CONSENT_PROMPTED, true);
        context.addEndpointParam(CIBAPushAuthenticatorConstants.AUTH_REQ_ID,
                context.getAuthenticationRequest().getRequestQueryParams()
                        .get(CIBAPushAuthenticatorConstants.NONCE)[0]);
    }

    /**
     * Returns a map of query parameters from the given query param string.
     * @param queryParamsString HTTP request query parameters
     * @return Query parameter map
     * @throws UnsupportedEncodingException  Unsupported encoding exception
     */
    protected Map<String, String> splitQuery(String queryParamsString) throws UnsupportedEncodingException {
        final Map<String, String> queryParams = new HashMap<>();
        final String[] pairs = queryParamsString.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            final String value =
                    idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            queryParams.put(key, value);
        }
        return queryParams;
    }

    /**
     * Extend this method to create error response on toolkits. Set necessary status codes and error payloads to
     * CIBAAuthenticationEndpointErrorResponse.
     *
     * @param httpStatusCode   Http status code
     * @param errorCode        Error code
     * @param errorDescription Error description
     * @return CIBAAuthenticationEndpointErrorResponse  CIBA Authentication Endpoint Error Response
     */
    public static CIBAAuthenticationEndpointErrorResponse createErrorResponse(int httpStatusCode, String errorCode,
                                                                              String errorDescription) {

        CIBAAuthenticationEndpointErrorResponse cibaPushServletErrorResponse =
                new CIBAAuthenticationEndpointErrorResponse();
        JSONObject errorResponse = new JSONObject();
        errorResponse.put(CIBAPushAuthenticatorConstants.ERROR_DESCRIPTION, errorDescription);
        errorResponse.put(CIBAPushAuthenticatorConstants.ERROR, errorCode);
        cibaPushServletErrorResponse.setPayload(errorResponse);
        cibaPushServletErrorResponse.setHttpStatusCode(httpStatusCode);

        return cibaPushServletErrorResponse;
    }

}
