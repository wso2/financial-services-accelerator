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

package com.wso2.openbanking.accelerator.push.authorization.endpoint.api;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.PushAuthRequestValidator;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.exception.PushAuthRequestValidatorException;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.model.PushAuthErrorResponse;
import com.wso2.openbanking.accelerator.push.authorization.endpoint.model.PushAuthorisationResponse;
import com.wso2.openbanking.accelerator.runtime.identity.authn.filter.OBOAuthClientAuthenticatorProxy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.http.HttpStatus;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Pushed Authorization Requests API
 *
 * <p>This specification defines the pushed authorization request endpoint, which allows clients to push the payload of
 * an OAuth 2.0 authorization request to the authorization server via a direct request and provides them with a request
 * URI that is used as reference to the data in a subsequent authorization request.
 * Life cycle :
 * This endpoint creates and returns request_uri to the user. The request object is stored in IDN_AUTH_SESSION_STORE
 * DB fronted by IS SessionDataCache. During auth call, request_uri is resolved using
 * DefaultOBRequestUriRequestObjectBuilder extension implementation.
 * Finally when the request_uri is used once, it should be removed from cache in consent-authorize-steps.
 */
@Path("/")
@InInterceptors(classes = OBOAuthClientAuthenticatorProxy.class)
public class PushAuthorisationEndpoint {

    private static final String REQUEST = "request";
    public static final String CLIENT_AUTHENTICATION_CONTEXT = "oauth.client.authentication.context";
    private OpenBankingConfigurationService openBankingConfigurationService;

    /**
     * Push an OAuth authorisation request object in exchange for a request_uri.
     * <p>
     * Endpoint maybe secured with basic auth in base 64
     */
    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is secured with access control lists in the configuration
    // Suppressed warning count - 1
    @POST
    @Path("/par")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json"})
    public Response parPost(@Context HttpServletRequest request, @Context HttpServletResponse response,
                            MultivaluedMap parameterMap) {

        PushAuthRequestValidator pushAuthRequestValidator = PushAuthRequestValidator.getPushAuthRequestValidator();

        Map<String, Object> paramMap;
        String requestJWT = StringUtils.EMPTY;

        OAuthClientAuthnContext clientAuthnContext = (OAuthClientAuthnContext)
                request.getAttribute(CLIENT_AUTHENTICATION_CONTEXT);

        // Check if the client authentication is successful
        if (!clientAuthnContext.isAuthenticated()) {
            // create error response
            PushAuthErrorResponse errorResponse = pushAuthRequestValidator
                    .createErrorResponse(HttpServletResponse.SC_UNAUTHORIZED,
                            clientAuthnContext.getErrorCode(), clientAuthnContext.getErrorMessage());
            return Response.status(errorResponse.getHttpStatusCode())
                    .entity(errorResponse.getPayload()).build();
        }

        try {
            paramMap = pushAuthRequestValidator.validateParams(request, (Map<String, List<String>>) parameterMap);
        } catch (PushAuthRequestValidatorException exception) {
            // create error response
            PushAuthErrorResponse errorResponse = pushAuthRequestValidator
                    .createErrorResponse(exception.getHttpStatusCode(), exception.getErrorCode(),
                            exception.getErrorDescription());
            return Response.status(errorResponse.getHttpStatusCode() != 0 ?
                    errorResponse.getHttpStatusCode() : exception.getHttpStatusCode())
                    .entity(errorResponse.getPayload()).build();
        }

        if (!paramMap.isEmpty() && paramMap.containsKey(REQUEST)) {

            requestJWT = paramMap.get(REQUEST).toString();
        }

        // Generate a urn with cryptographically strong pseudo random algorithm
        String urn = RandomStringUtils.randomAlphanumeric(32);

        OpenBankingConfigurationService openBankingConfigurationService = getOBConfigService();

        int expiryTime = Integer.parseInt(openBankingConfigurationService.getConfigurations()
                .get(OpenBankingConstants.PUSH_AUTH_EXPIRY_TIME).toString());

        // Add to auth cache
        addToIdnOAuthCache(requestJWT, urn, expiryTime);

        return Response.status(HttpStatus.SC_CREATED)
                .entity(getSuccessResponse("urn" + ":" + openBankingConfigurationService.getConfigurations()
                        .get(OpenBankingConstants.PUSH_AUTH_REQUEST_URI_SUBSTRING).toString() + ":" + urn, expiryTime))
                .build();

    }

    /**
     * Add Request Object to Session-Key Cache (Database). Validation is set as one minute.
     */
    private static void addToIdnOAuthCache(String requestJWT, String sessionKey, int expiry) {

        SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionKey);
        SessionDataCacheEntry sessionDataCacheEntry = new SessionDataCacheEntry();
        OAuth2Parameters oAuth2Parameters = new OAuth2Parameters();
        long expiryTimestamp = Instant.now().getEpochSecond() + expiry;
        oAuth2Parameters.setEssentialClaims(requestJWT + ":" + expiryTimestamp);
        sessionDataCacheEntry.setoAuth2Parameters(oAuth2Parameters);

        SessionDataCache.getInstance().addToCache(cacheKey, sessionDataCacheEntry);
    }

    /**
     * Create success response.
     */
    private static PushAuthorisationResponse getSuccessResponse(String requestUri, int expiry) {

        PushAuthorisationResponse pushAuthorisationResponse = new PushAuthorisationResponse();

        pushAuthorisationResponse.setRequestUri(requestUri);
        pushAuthorisationResponse.setExpiresIn(expiry);
        return pushAuthorisationResponse;
    }

    /**
     * Retrieve Open Banking configuration service.
     */
    private OpenBankingConfigurationService getOBConfigService() {

        if (this.openBankingConfigurationService == null) {
            OpenBankingConfigurationService openBankingConfigurationService =
                    (OpenBankingConfigurationService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(OpenBankingConfigurationService.class, null);
            if (openBankingConfigurationService != null) {
                this.openBankingConfigurationService = openBankingConfigurationService;
            }
        }
        return this.openBankingConfigurationService;
    }
}
