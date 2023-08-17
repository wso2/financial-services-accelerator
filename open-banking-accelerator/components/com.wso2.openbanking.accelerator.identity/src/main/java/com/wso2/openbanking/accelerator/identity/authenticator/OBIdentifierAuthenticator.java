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
package com.wso2.openbanking.accelerator.identity.authenticator;

import com.wso2.openbanking.accelerator.common.exception.OBThrottlerException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.authenticator.constants.IdentifierHandlerConstants;
import com.wso2.openbanking.accelerator.identity.authenticator.util.OBIdentifierAuthUtil;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import com.wso2.openbanking.accelerator.throttler.service.OBThrottleService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticator;
import org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticatorConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.IDENTIFIER_CONSENT;

/**
 * OB Identifier based authenticator.
 */
public class OBIdentifierAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 1819664539416029785L;
    private static final Log log = LogFactory.getLog(OBIdentifierAuthenticator.class);
    private static final String PROMPT_CONFIRMATION_WINDOW = "promptConfirmationWindow";
    private static final String CONTINUE = "continue";
    private static final String RESET = "reset";
    private static final String RE_CAPTCHA_USER_DOMAIN = "user-domain-recaptcha";
    private static final String USER_TENANT_DOMAIN_MISMATCH = "UserTenantDomainMismatch";
    private static final String OB_IDENTIFIER_AUTHENTICATOR = "OBIdentifierAuthenticator";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String REQUEST_URI = "request_uri";

    @Override
    public boolean canHandle(HttpServletRequest request) {

        String userName = request.getParameter(IdentifierHandlerConstants.USER_NAME);
        String identifierConsent = request.getParameter(IDENTIFIER_CONSENT);
        return userName != null || identifierConsent != null;
    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {

        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            if (context.getPreviousAuthenticatedIdPs().get(BasicAuthenticatorConstants.LOCAL) != null) {
                AuthenticatedIdPData local =
                        context.getPreviousAuthenticatedIdPs().get(BasicAuthenticatorConstants.LOCAL);
                if (local.getAuthenticators().size() > 0) {
                    for (AuthenticatorConfig authenticatorConfig : local.getAuthenticators()) {
                        if (authenticatorConfig.getApplicationAuthenticator() instanceof BasicAuthenticator) {
                            boolean isPrompt = Boolean.parseBoolean(context.getAuthenticatorParams(this
                                    .getName()).get(PROMPT_CONFIRMATION_WINDOW));

                            if (isPrompt) {
                                String identifierConsent = request.getParameter(IDENTIFIER_CONSENT);
                                if (identifierConsent != null && CONTINUE.equals(identifierConsent)) {
                                    context.setSubject(local.getUser());
                                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                                } else if (identifierConsent != null && RESET.equals(identifierConsent)) {
                                    initiateAuthenticationRequest(request, response, context);
                                    return AuthenticatorFlowStatus.INCOMPLETE;
                                } else if (request.getParameter(IdentifierHandlerConstants.USER_NAME) != null) {
                                    processAuthenticationResponse(request, response, context);
                                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                                } else {
                                    String identifierFirstConfirmationURL =
                                            ConfigurationFacade.getInstance().getIdentifierFirstConfirmationURL();
                                    String queryParams = context.getContextIdIncludedQueryParams();
                                    try {
                                        queryParams = queryParams + "&username=" + local.getUser()
                                                .toFullQualifiedUsername();
                                        response.sendRedirect(identifierFirstConfirmationURL +
                                                ("?" + queryParams));
                                        return AuthenticatorFlowStatus.INCOMPLETE;
                                    } catch (IOException e) {
                                        throw new AuthenticationFailedException(e.getMessage(), e);
                                    }
                                }
                            } else {
                                context.setSubject(local.getUser());
                                return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                            }
                        }
                    }
                }
            } else if (request.getParameter(IDENTIFIER_CONSENT) != null) {
                //submit from the confirmation page.
                initiateAuthenticationRequest(request, response, context);
                return AuthenticatorFlowStatus.INCOMPLETE;
            }
            return super.process(request, response, context);
        }
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        int throttleLimit = 3; //default allowed attempts (use this if config is not defined)
        int throttleTimePeriod = 180; //default blocked time period (3 minutes)
        String showAuthFailureReason = "";
        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        String retryPage = ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL();
        String queryParams = context.getContextIdIncludedQueryParams();
        OBThrottleService obThrottleService = IdentityExtensionsDataHolder.getInstance().getOBThrottleService();

        //Read authenticator configs
        Map<String, String> parameterMap = getAuthenticatorConfig().getParameterMap();
        if (parameterMap != null) {
            throttleLimit = Integer.parseInt(parameterMap.get("throttleLimit"));
            throttleTimePeriod = Integer.parseInt(parameterMap.get("throttleTimePeriod"));
            showAuthFailureReason = parameterMap.get("showAuthFailureReason");
        }

        try {
            String retryParam = "";
            if (context.isRetrying()) {
                // Update throttling data
                String userIp = IdentityUtil.getClientIpAddress(request);
                obThrottleService
                        .updateThrottleData(OB_IDENTIFIER_AUTHENTICATOR, userIp, throttleLimit, throttleTimePeriod);
                // Check if the client-ip is throttled
                if (obThrottleService.isThrottled(OB_IDENTIFIER_AUTHENTICATOR, userIp)) {
                    retryParam = "&authFailure=true&authFailureMsg=Too.many.attempts";
                } else {
                    if (context.getProperty(IdentifierHandlerConstants.CONTEXT_PROP_INVALID_EMAIL_USERNAME) != null &&
                            (Boolean) context.getProperty(IdentifierHandlerConstants.
                                    CONTEXT_PROP_INVALID_EMAIL_USERNAME)) {
                        retryParam = "&authFailure=true&authFailureMsg=Login.failed";
                        context.setProperty(IdentifierHandlerConstants.CONTEXT_PROP_INVALID_EMAIL_USERNAME, false);
                    } else {
                        retryParam = "&authFailure=true&authFailureMsg=Login.failed";
                    }
                }
            }

            if (context.getProperty(USER_TENANT_DOMAIN_MISMATCH) != null &&
                    (Boolean) context.getProperty(USER_TENANT_DOMAIN_MISMATCH)) {
                retryParam = "&authFailure=true&authFailureMsg=user.tenant.domain.mismatch.message";
                context.setProperty(USER_TENANT_DOMAIN_MISMATCH, false);
            }

            IdentityErrorMsgContext errorContext = IdentityUtil.getIdentityErrorMsg();
            IdentityUtil.clearIdentityErrorMsg();

            if (errorContext != null && errorContext.getErrorCode() != null) {
                log.debug("Identity error message context is not null");
                String errorCode = errorContext.getErrorCode();

                if (errorCode.equals(IdentityCoreConstants.USER_ACCOUNT_NOT_CONFIRMED_ERROR_CODE)) {
                    retryParam = "&authFailure=true&authFailureMsg=account.confirmation.pending";
                    String username = request.getParameter(IdentifierHandlerConstants.USER_NAME);
                    Object domain = IdentityUtil.threadLocalProperties.get().get(RE_CAPTCHA_USER_DOMAIN);
                    if (domain != null) {
                        username = IdentityUtil.addDomainToName(username, domain.toString());
                    }
                    String redirectURL = loginPage + ("?" + queryParams) + IdentifierHandlerConstants.FAILED_USERNAME
                            + URLEncoder.encode(username, IdentifierHandlerConstants.UTF_8) +
                            IdentifierHandlerConstants.ERROR_CODE + errorCode + IdentifierHandlerConstants
                            .AUTHENTICATORS + getName() + ":" + IdentifierHandlerConstants.LOCAL + retryParam;
                    response.sendRedirect(redirectURL);

                } else if ("true".equals(showAuthFailureReason)) {
                    String reason = null;
                    if (errorCode.contains(":")) {
                        String[] errorCodeReason = errorCode.split(":");
                        errorCode = errorCodeReason[0];
                        if (errorCodeReason.length > 1) {
                            reason = errorCodeReason[1];
                        }
                    }
                    int remainingAttempts =
                            errorContext.getMaximumLoginAttempts() - errorContext.getFailedLoginAttempts();

                    if (log.isDebugEnabled()) {
                        log.debug("errorCode : " + errorCode);
                        log.debug("username : " + request.getParameter(IdentifierHandlerConstants.USER_NAME));
                        log.debug("remainingAttempts : " + remainingAttempts);
                    }

                    if (errorCode.equals(UserCoreConstants.ErrorCode.INVALID_CREDENTIAL)) {
                        retryParam = retryParam + IdentifierHandlerConstants.ERROR_CODE + errorCode
                                + IdentifierHandlerConstants.FAILED_USERNAME + URLEncoder
                                .encode(request.getParameter(IdentifierHandlerConstants.USER_NAME),
                                        IdentifierHandlerConstants.UTF_8)
                                + "&remainingAttempts=" + remainingAttempts;
                        response.sendRedirect(loginPage + ("?" + queryParams)
                                + IdentifierHandlerConstants.AUTHENTICATORS + getName() + ":" +
                                IdentifierHandlerConstants.LOCAL + retryParam);
                    } else if (errorCode.equals(UserCoreConstants.ErrorCode.USER_IS_LOCKED)) {
                        String redirectURL = retryPage;
                        if (remainingAttempts == 0) {
                            if (StringUtils.isBlank(reason)) {
                                redirectURL = URLEncoder.encode((redirectURL + ("?" + queryParams)),
                                        IdentifierHandlerConstants.UTF_8) + IdentifierHandlerConstants.ERROR_CODE
                                        + errorCode + IdentifierHandlerConstants.FAILED_USERNAME +
                                        URLEncoder.encode(request.getParameter(IdentifierHandlerConstants.USER_NAME),
                                                IdentifierHandlerConstants.UTF_8) +
                                        "&remainingAttempts=0";
                            } else {
                                redirectURL = URLEncoder.encode((redirectURL + ("?" + queryParams)),
                                        IdentifierHandlerConstants.UTF_8) + IdentifierHandlerConstants.ERROR_CODE
                                        + errorCode + "&lockedReason=" + reason +
                                        IdentifierHandlerConstants.FAILED_USERNAME +
                                        URLEncoder.encode(request.getParameter(IdentifierHandlerConstants.USER_NAME),
                                                IdentifierHandlerConstants.UTF_8) + "&remainingAttempts=0";
                            }
                        } else {
                            if (StringUtils.isBlank(reason)) {
                                redirectURL = URLEncoder.encode((redirectURL + ("?" + queryParams)),
                                        IdentifierHandlerConstants.UTF_8) + IdentifierHandlerConstants.ERROR_CODE +
                                        errorCode + IdentifierHandlerConstants.FAILED_USERNAME +
                                        URLEncoder.encode(request.getParameter(IdentifierHandlerConstants.USER_NAME),
                                                IdentifierHandlerConstants.UTF_8);
                            } else {
                                redirectURL = URLEncoder.encode((redirectURL + ("?" + queryParams)),
                                        IdentifierHandlerConstants.UTF_8) + IdentifierHandlerConstants.ERROR_CODE +
                                        errorCode + "&lockedReason=" + reason +
                                        IdentifierHandlerConstants.FAILED_USERNAME +
                                        URLEncoder.encode(request.getParameter(IdentifierHandlerConstants.USER_NAME),
                                                IdentifierHandlerConstants.UTF_8);
                            }
                        }
                        response.sendRedirect(redirectURL);
                    } else {
                        retryParam = retryParam + IdentifierHandlerConstants.ERROR_CODE + errorCode
                                + IdentifierHandlerConstants.FAILED_USERNAME + URLEncoder
                                .encode(request.getParameter(IdentifierHandlerConstants.USER_NAME),
                                        IdentifierHandlerConstants.UTF_8);
                        response.sendRedirect(loginPage + ("?" + queryParams)
                                + IdentifierHandlerConstants.AUTHENTICATORS + getName() + ":"
                                + IdentifierHandlerConstants.LOCAL + retryParam);
                    }
                } else {
                    log.debug("Unknown identity error code.");
                    response.sendRedirect(loginPage + ("?" + queryParams)
                            + IdentifierHandlerConstants.AUTHENTICATORS + getName() + ":" +
                            IdentifierHandlerConstants.LOCAL + retryParam);
                }
            } else {
                log.debug("Identity error message context is null");
                response.sendRedirect(loginPage + ("?" + queryParams)
                        + IdentifierHandlerConstants.AUTHENTICATORS + getName() + ":" +
                        IdentifierHandlerConstants.LOCAL + retryParam);
            }
        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), User.getUserFromUserName(request.getParameter
                    (IdentifierHandlerConstants.USER_NAME)), e);
        }  catch (OBThrottlerException e) {
            throw new AuthenticationFailedException("Error occurred while deleting throttle data.", e);
        }
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        OBIdentifierAuthUtil.validateUsername(request.getParameter(BasicAuthenticatorConstants.USER_NAME), context);
        OBThrottleService obThrottleService = IdentityExtensionsDataHolder.getInstance().getOBThrottleService();
        String username = OBIdentifierAuthUtil.preprocessUsername(
                request.getParameter(IdentifierHandlerConstants.USER_NAME), context);
        Map<String, Object> authProperties = context.getProperties();
        if (authProperties == null) {
            authProperties = new HashMap<>();
            context.setProperties(authProperties);
        }

        String userIp = IdentityUtil.getClientIpAddress(request);
        try {
            // Check if the client-ip is throttled.
            if (obThrottleService.isThrottled(OB_IDENTIFIER_AUTHENTICATOR, userIp)) {
                throw new AuthenticationFailedException("Too many attempts to log in.",
                        User.getUserFromUserName(username));
            }
        } catch (OBThrottlerException e) {
            throw new AuthenticationFailedException("Error occurred while deleting throttle data.", e);
        }

        if (getAuthenticatorConfig().getParameterMap() != null) {
            String validateUsername = getAuthenticatorConfig().getParameterMap().get("ValidateUsername");
            if (Boolean.valueOf(validateUsername)) {
                boolean isUserExists;
                UserStoreManager userStoreManager;
                // Check if the username exists.
                try {
                    int tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
                    UserRealm userRealm = IdentityExtensionsDataHolder.getInstance().getRealmService()
                            .getTenantUserRealm(tenantId);

                    if (userRealm != null) {
                        userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                        isUserExists = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername
                                (username));
                    } else {
                        throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " +
                                tenantId, User.getUserFromUserName(username));
                    }
                } catch (IdentityRuntimeException e) {
                    log.error("OBIdentifierAuthenticator failed while trying to get the tenant ID of " +
                                "the user " + username, e);
                    throw new AuthenticationFailedException(e.getMessage(), User.getUserFromUserName(username), e);
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    log.error("OBIdentifierAuthenticator failed while trying to authenticate", e);
                    throw new AuthenticationFailedException(e.getMessage(), User.getUserFromUserName(username), e);
                }
                if (!isUserExists) {
                    log.debug("User does not exist.");
                    if (IdentityUtil.threadLocalProperties.get().get(RE_CAPTCHA_USER_DOMAIN) != null) {
                        username = IdentityUtil.addDomainToName(
                                username, IdentityUtil.threadLocalProperties.get().get(RE_CAPTCHA_USER_DOMAIN)
                                        .toString());
                    }
                    IdentityUtil.threadLocalProperties.get().remove(RE_CAPTCHA_USER_DOMAIN);
                    throw new InvalidCredentialsException("User does not exist.", User.getUserFromUserName(username));
                }
                String tenantDomain = MultitenantUtils.getTenantDomain(username);
                authProperties.put("user-tenant-domain", tenantDomain);
            }
        }

        username = FrameworkUtils.prependUserStoreDomainToName(username);
        authProperties.put("username", username);
        Map<String, String> identifierParams = new HashMap<>();
        identifierParams.put(FrameworkConstants.JSAttributes.JS_OPTIONS_USERNAME, username);
        Map<String, Map<String, String>> contextParams = new HashMap<>();
        contextParams.put(FrameworkConstants.JSAttributes.JS_COMMON_OPTIONS, identifierParams);
        //Identifier first is the first authenticator.
        context.getPreviousAuthenticatedIdPs().clear();
        context.addAuthenticatorParams(contextParams);
        context.setSubject(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username));
        if (context.getParameters().containsKey("username")) {
            try {
                obThrottleService.deleteRecordOnSuccessAttempt(OB_IDENTIFIER_AUTHENTICATOR, userIp);
            } catch (OBThrottlerException e) {
                throw new AuthenticationFailedException("Error occurred while deleting throttle data.", e);
            }
        }
    }

    @Override
    protected boolean retryAuthenticationEnabled() {
        return true;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }

    @Override
    public String getFriendlyName() {
        return IdentifierHandlerConstants.HANDLER_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return IdentifierHandlerConstants.HANDLER_NAME;
    }

    /**
     * To get session details from SessionDataKey.
     * authRequestURL need be configured in the IAM deployment.toml file.
     * @param sessionDataKey session data key
     * @return session data
     * @throws OpenBankingException openbanking exception
     */
    public String getSessionData(String sessionDataKey) throws OpenBankingException {

        BufferedReader reader = null;
        String authRequestURL = null;
        RealmConfiguration realmConfig = null;

        //Read authenticator configs
        Map<String, String> parameterMap = getAuthenticatorConfig().getParameterMap();
        if (parameterMap != null) {
            authRequestURL = parameterMap.get(IdentifierHandlerConstants.AUTH_REQ_URL);
        }
        try {
            realmConfig = IdentityExtensionsDataHolder.getInstance().getRealmService()
                    .getBootstrapRealm().getUserStoreManager().getRealmConfiguration();
        } catch (UserStoreException e) {
            throw new OpenBankingException("Error while retrieving session data", e);
        }
        String adminUsername = realmConfig.getAdminUserName();
        char[] adminPassword = realmConfig.getAdminPassword().toCharArray();

        String credentials = adminUsername + ":" + String.valueOf(adminPassword);
        credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpGet dataRequest = new HttpGet(authRequestURL + sessionDataKey);
            dataRequest.addHeader(IdentifierHandlerConstants.ACCEPT_HEADER,
                    IdentifierHandlerConstants.ACCEPT_HEADER_VALUE);
            dataRequest.addHeader(IdentifierHandlerConstants.AUTH_HEADER, "Basic " + credentials);
            CloseableHttpResponse dataResponse = client.execute(dataRequest);

            reader = new BufferedReader(new InputStreamReader(dataResponse.getEntity()
                    .getContent(), "UTF-8"));
            String inputLine;
            StringBuffer buffer = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                buffer.append(inputLine);
            }

            if (dataResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                return null;
            } else {
                JSONObject sessionData = new JSONObject(buffer.toString());
                appendRedirectUri(sessionData);
                return sessionData.toString();
            }
        } catch (IOException e) {
            throw new OpenBankingException("Error while retrieving session data", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing buffered reader", e);
                }
            }
        }
    }

    /**
     * Append redirect_uri value to session data for par requests.
     *
     * @param sessionData
     */
    private void appendRedirectUri(JSONObject sessionData) throws OpenBankingException {

        // Handle redirect uri for par request.
        // In par requests, there's no redirect_uri in request object itself, so fetch redirect uri from cached request.
        if (!sessionData.has(REDIRECT_URI) && sessionData.has(REQUEST_URI)) {

            JSONObject requestObjectVal = getParRequestObject(sessionData);
            if (requestObjectVal.has(REDIRECT_URI)) {
                sessionData.put(REDIRECT_URI, requestObjectVal.get(REDIRECT_URI));
            } else {
                log.error("redirect_uri could not be found in the par request object.");
                throw new OpenBankingException("redirect_uri could not be found in the par request object.");
            }
        }
    }

    /**
     * Get redirect_uri using request_uri.
     *
     * @param requestUri - request_uri
     * @return redirect_uri
     * @throws OpenBankingException - OpenBankingException
     */
    public String getRedirectUri(String requestUri) throws OpenBankingException {

        JSONObject requestObjectVal = getParRequestObject(requestUri);
        if (requestObjectVal.has(REDIRECT_URI)) {
            return requestObjectVal.get(REDIRECT_URI).toString();
        } else {
            log.error("redirect_uri could not be found in the par request object.");
            throw new OpenBankingException("redirect_uri could not be found in the par request object.");
        }
    }

    /**
     * Retrieve PAR request object from session data cache.
     *
     * @param sessionData session data
     * @return Request object json.
     * @throws OpenBankingException
     */
    @Generated(message = "Excluding from code coverage since it requires a valid cache entry")
    private JSONObject getParRequestObject(JSONObject sessionData) throws OpenBankingException {

        //get request ref Ex -> "IVL...." from "urn::IVL..."
        String[] requestUri = sessionData.get(REQUEST_URI).toString().split(":");
        String requestUriRef = requestUri[requestUri.length - 1];

        SessionDataCacheKey cacheKey = new SessionDataCacheKey(requestUriRef);
        SessionDataCacheEntry cacheEntry = SessionDataCache.getInstance().getValueFromCache(cacheKey);

        if (cacheEntry != null) {
            String essentialClaims = cacheEntry.getoAuth2Parameters().getEssentialClaims();
            byte[] requestObject;
            try {
                requestObject = Base64.getDecoder().decode(essentialClaims.split("\\.")[1]);
            } catch (IllegalArgumentException e) {
                // Decode if the requestObject is base64-url encoded.
                requestObject = Base64.getUrlDecoder().decode(essentialClaims.split("\\.")[1]);
            }
            return new JSONObject(new String(requestObject, StandardCharsets.UTF_8));
        } else {
            log.error("Could not able to fetch par request object from session data cache.");
            throw new OpenBankingException("Could not able to fetch par request object from session data cache.");
        }
    }

    /**
     * Retrieve PAR request object from request_uri.
     *
     * @param requestUri - request_uri
     * @return Request object json.
     * @throws OpenBankingException - OpenBankingException
     */
    @Generated(message = "Excluding from code coverage since it requires a valid cache entry")
    private JSONObject getParRequestObject(String requestUri) throws OpenBankingException {

        String[] requestUriArr = requestUri.split(":");
        String requestUriRef = requestUriArr[requestUriArr.length - 1];
        SessionDataCacheKey cacheKey = new SessionDataCacheKey(requestUriRef);
        SessionDataCacheEntry cacheEntry = SessionDataCache.getInstance().getValueFromCache(cacheKey);

        if (cacheEntry != null) {
            String essentialClaims = cacheEntry.getoAuth2Parameters().getEssentialClaims();
            byte[] requestObject;
            try {
                requestObject = Base64.getDecoder().decode(essentialClaims.split("\\.")[1]);
            } catch (IllegalArgumentException e) {
                // Decode if the requestObject is base64-url encoded.
                requestObject = Base64.getUrlDecoder().decode(essentialClaims.split("\\.")[1]);
            }
            return new JSONObject(new String(requestObject, StandardCharsets.UTF_8));
        } else {
            log.error("Could not able to fetch par request object from session data cache.");
            throw new OpenBankingException("Could not able to fetch par request object from session data cache.");
        }
    }

    /**
     * Get SSA client_name using clientId.
     *
     * @param clientId client id.
     * @param property required property.
     * @return service provider value.
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public String getSPProperty (String clientId, String property) throws OpenBankingException {

        return new IdentityCommonHelper().getAppPropertyFromSPMetaData(clientId, property);
    }
}
