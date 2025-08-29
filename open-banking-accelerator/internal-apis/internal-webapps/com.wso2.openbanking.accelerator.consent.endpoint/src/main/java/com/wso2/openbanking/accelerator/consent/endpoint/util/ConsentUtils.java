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

package com.wso2.openbanking.accelerator.consent.endpoint.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

/**
 * Utils class for consent authorize endpoints.
 */
public class ConsentUtils {

    private static final Log log = LogFactory.getLog(ConsentUtils.class);
    private static final String ERROR_FETCHING_SP = "Error while fetching service provider";
    private static Gson gson = new Gson();

    /**
     * Send authorize request in order to complete the authorize flow and get the redirect.
     *
     * @param consent     The approval/denial of the consent of the user
     * @param cookies     The session cookies used in auth flow
     * @param consentData Consent data object which contains consent information
     * @return The redirect URI to end the authorize flow
     */
    public static URI authorizeRequest(String consent, Map<String, String> cookies, ConsentData consentData) {

        String authorizeURL = IdentityUtil.getProperty("OAuth.OAuth2AuthzEPUrl");
        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClientInstance()) {

            BasicCookieStore cookieStore = new BasicCookieStore();
            String cookieDomain = new URI(authorizeURL).getHost();
            for (Map.Entry<String, String> cookieValue : cookies.entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(cookieValue.getKey(), cookieValue.getValue());
                cookie.setDomain(cookieDomain);
                cookie.setPath("/");
                cookie.setSecure(true);
                cookieStore.addCookie(cookie);
            }
            HttpPost authorizeRequest = new HttpPost(authorizeURL);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("hasApprovedAlways", "false"));
            params.add(new BasicNameValuePair("sessionDataKeyConsent", consentData.getSessionDataKey()));
            params.add(new BasicNameValuePair("consent", consent));
            params.add(new BasicNameValuePair("user", consentData.getUserId()));
            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
            authorizeRequest.setEntity(entity);
            HttpResponse authorizeResponse = client.execute(authorizeRequest, localContext);

            if (authorizeResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Error while getting authorize redirect", consentData.getState());
            } else {
                //Extract the location header from the authorization redirect
                return new URI(authorizeResponse.getLastHeader("Location").getValue());
            }
        } catch (IOException e) {
            log.error("Error while sending authorize request to complete the authorize flow", e);
            return null;
        } catch (URISyntaxException e) {
            log.error("Authorize response URI syntax error", e);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    "Internal server error", consentData.getState());
        } catch (OpenBankingException e) {
            log.error("Error while obtaining HTTP client", e);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    "Internal server error", consentData.getState());
        }
    }

    /**
     * Util method to extract the payload from a HTTP request object. Can be JSONObject or JSONArray
     *
     * @param request The HTTP request object
     * @return Object payload can be either an instance of JSONObject or JSONArray only. Can be a ConsentException if
     * is and error scenario. Error is returned instead of throwing since the error response should be handled by the
     * toolkit is the manage scenario.
     */
    public static Object getPayload(HttpServletRequest request) {
        try {
            Object payload = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(getStringPayload(request));
            if (payload == null) {
                log.debug("Payload is empty. Returning null");
                return null;
            }
            if (!(payload instanceof JSONObject || payload instanceof JSONArray)) {
                //Not throwing error since error should be formatted by manage toolkit
                log.error("Payload is not a JSON. Returning null");
                return null;
            }
            return payload;
        } catch (ParseException e) {
            //Not throwing error since error should be formatted by manage toolkit
            log.error(ConsentConstants.ERROR_PAYLOAD_PARSE + ". Returning null", e);
            return null;
        } catch (ConsentException e) {
            //Not throwing error since error should be formatted by manage toolkit
            log.error(e.getMessage() + ". Returning null", e);
            return null;
        }
    }

    /**
     * Util method to extract the payload from a HTTP request object. Can be only JSONObject
     *
     * @param request The request object
     * @return JSONObject payload can only be an instance of JSONObject
     * @throws ConsentException Parser errors and payload type is not JSON object
     */
    public static JSONObject getJSONObjectPayload(HttpServletRequest request) throws ConsentException {
        try {
            Object payload = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(getStringPayload(request));
            //JSONArray not supported here. If requirement arises, cast the object to JSONArray from here
            if (payload == null) {
                return null;
            }
            if (!(payload instanceof JSONObject)) {
                return null;
            }
            return (JSONObject) payload;
        } catch (ParseException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ConsentConstants.ERROR_PAYLOAD_PARSE);
        } catch (ConsentException e) {
            //Not throwing error since error should be formatted by manage toolkit
            log.error(e.getMessage() + ". Returning null", e);
            return null;
        }
    }

    /**
     * Extract string payload from request object.
     *
     * @param request The request object
     * @return String payload
     * @throws ConsentException Payload read errors
     */
    public static String getStringPayload(HttpServletRequest request) throws ConsentException {
        try {
            return IOUtils.toString(request.getInputStream());
        } catch (IOException e) {
            log.error(ConsentConstants.ERROR_PAYLOAD_READ, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ConsentConstants.ERROR_PAYLOAD_READ);
        }
    }

    /**
     * Util method to extract the payload from a HTTP request object.
     *
     * @param request The HTTP request object
     * @return Object payload can be xml or json. Can be a ConsentException if is and error scenario.
     * Error is returned instead of throwing since the error response should be handled by the
     * toolkit is the manage scenario.
     */
    public static Object getFileUploadPayload(HttpServletRequest request) {
        try {
            String payload = getStringPayload(request);
            if (payload == null) {
                log.debug("Payload is empty. Returning null");
                return null;
            }
            return payload;
        } catch (ConsentException e) {
            //Not throwing error since error should be formatted by manage toolkit
            log.error(e.getMessage() + ". Returning null", e);
            return null;
        }
    }


    /**
     * Extract and add query parameters from a URL to existing resource map.
     * Resource parameter map will contain the resource path(ex: /aisp/accounts/{AccountId}?queryParam=queryParamValue),
     * http method, context(ex: /open-banking/v3.1/aisp)
     *
     * @param resourceParams Map containing the resource parameters
     * @return Extracted query parameter map
     */
    public static Map<String, String> addQueryParametersToResourceParamMap(Map<String, String> resourceParams)
            throws URISyntaxException {

        if (resourceParams.isEmpty()) {
            return new HashMap<String, String>();
        }

        URI url = new URI((String) resourceParams.get("resource"));

        resourceParams.put(ConsentConstants.RESOURCE_PATH, url.getRawPath());

        if (url.getRawQuery() != null) {
            String[] params = url.getRawQuery().split("&");

            for (String param : params) {
                if (param.split("=").length == 2) {
                    String name = param.split("=")[0];
                    String value = param.split("=")[1];
                    resourceParams.put(name, value);
                }
            }
        }
        return resourceParams;
    }

    /**
     * Get Service provider from clientId.
     *
     * @param clientId of application.
     * @return Service Provider.
     * @throws WebApplicationException client error.
     */
    public static ServiceProvider getOAuthServiceProvider(String clientId) throws WebApplicationException {

        ApplicationManagementService managementService = getApplicationManagementService();
        Optional<ServiceProvider> serviceProvider;
        try {
            serviceProvider = Optional.ofNullable(managementService.getServiceProviderByClientId(clientId,
                    IdentityApplicationConstants.OAuth2.NAME, getTenantDomain()));
        } catch (IdentityApplicationManagementException e) {

            log.error(String.format("Unable to retrieve service provider information for clientId %s", clientId), e);
            // Throw Web Application exception
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ERROR_FETCHING_SP);
        }

        // Reject default service provider and empty service provider.
        if (!serviceProvider.isPresent() ||
                serviceProvider.get().getApplicationName().equals(IdentityApplicationConstants.DEFAULT_SP_CONFIG)) {

            final String errorMessage = String.format("Unable to find application for clientId %s", clientId);

            if (log.isDebugEnabled()) {
                log.debug(errorMessage);
            }

            // Throw client error for not found service provider.
            throw new ConsentException(ResponseStatus.NOT_FOUND, errorMessage);
        }
        return serviceProvider.get();
    }

    /**
     * Get WSO2 IS Application Mgt Service from threadlocal carbon context.
     *
     * @return Application Management Service Implementation.
     */
    public static ApplicationManagementService getApplicationManagementService() {

        return (ApplicationManagementService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(ApplicationManagementService.class, null);

    }

    /**
     * Get Tenant Domain String from carbon context.
     *
     * @return tenant domain of current context.
     */
    private static String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
    }

    /**
     * @param consentDetails json object of consent data
     * @param sessionDataKey
     * @return
     * @throws URISyntaxException
     */
    public static ConsentData getConsentDataFromAttributes(JsonObject consentDetails, String sessionDataKey)
            throws URISyntaxException {

        JsonObject sensitiveDataMap = consentDetails.get(ConsentConstants.SENSITIVE_DATA_MAP).getAsJsonObject();
        ConsentData consentData = new ConsentData(sessionDataKey,
                sensitiveDataMap.get(ConsentConstants.LOGGED_IN_USER).getAsString(),
                sensitiveDataMap.get(ConsentConstants.SP_QUERY_PARAMS).getAsString(),
                consentDetails.get(ConsentConstants.SCOPES).getAsString(),
                sensitiveDataMap.get(ConsentConstants.APPLICATION).getAsString(),
                gson.fromJson(consentDetails.get(ConsentConstants.REQUEST_HEADERS), Map.class));
        consentData.setSensitiveDataMap(gson.fromJson(sensitiveDataMap, Map.class));
        URI redirectURI = new URI(consentDetails.get(ConsentConstants.REQUEST_URI).getAsString());
        consentData.setRedirectURI(redirectURI);
        consentData.setUserId(consentDetails.get(ConsentConstants.USERID).getAsString());
        consentData.setConsentId(consentDetails.get(ConsentConstants.CONSENT_ID).getAsString());
        consentData.setClientId(consentDetails.get(ConsentConstants.CLIENT_ID).getAsString());
        consentData.setRegulatory(Boolean.parseBoolean(consentDetails.get(ConsentConstants.REGULATORY).getAsString()));
        ConsentResource consentResource = gson.fromJson(consentDetails.get(ConsentConstants.CONSENT_RESOURCE),
                ConsentResource.class);
        consentData.setConsentResource(consentResource);
        AuthorizationResource authorizationResource =
                gson.fromJson(consentDetails.get(ConsentConstants.AUTH_RESOURCE), AuthorizationResource.class);
        consentData.setAuthResource(authorizationResource);
        consentData.setMetaDataMap(gson.fromJson(consentDetails.get(ConsentConstants.META_DATA), Map.class));
        consentData.setType(consentDetails.get(ConsentConstants.TYPE).getAsString());
        return consentData;
    }
}
