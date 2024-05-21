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

package com.wso2.openbanking.accelerator.gateway.executor.dcr;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.exception.OpenBankingExecutorException;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.HttpMethod;

/**
 * Executor for signature validation, am app creation and API subscription for DCR.
 */
public class DCRExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(DCRExecutor.class);
    private static String clientIdParam = "client_id";
    private static String registrationAccessTokenParam = "registration_access_token";
    private static String clientSecret = "client_secret";
    private static String applicationIdParam = "applicationId";
    private static String userName = "userName";
    private static String obDCREndpoint = "api/openbanking/dynamic-client-registration/register";
    private static Map<String, Object> urlMap = GatewayDataHolder.getInstance().getUrlMap();

    public static void setUrlMap(Map<String, Object> conf) {

        if (urlMap == null) {
            DCRExecutor.urlMap = conf;
        }
    }

    @Generated(message = "Excluding from unit tests since there is an external http call")
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        if (obapiRequestContext.isError()) {
            return;
        }
        boolean validateJWT = true;
        Map<String, Object> configs = GatewayDataHolder.getInstance()
                .getOpenBankingConfigurationService().getConfigurations();
        if (configs.containsKey(GatewayConstants.VALIDATE_JWT)) {
            validateJWT = Boolean.parseBoolean(configs.get(GatewayConstants.VALIDATE_JWT).toString());
        }

        if (validateJWT) {
            String payload = obapiRequestContext.getRequestPayload();
            try {
                String httpMethod = obapiRequestContext.getMsgInfo().getHttpMethod();
                if (HttpMethod.POST.equalsIgnoreCase(httpMethod) || HttpMethod.PUT.equalsIgnoreCase(httpMethod)) {
                    if (payload != null) {
                        //decode request jwt
                        validateRequestSignature(payload, obapiRequestContext);
                    } else {
                        handleBadRequestError(obapiRequestContext, "Malformed request found");
                    }
                }
            } catch (ParseException e) {
                log.error("Error occurred while decoding the provided jwt", e);
                handleBadRequestError(obapiRequestContext, "Malformed request JWT");
            } catch (JOSEException | BadJOSEException | MalformedURLException e) {
                log.error("Error occurred while validating the signature", e);
                handleBadRequestError(obapiRequestContext, "Invalid request signature");
            } catch (OpenBankingExecutorException e) {
                log.error("Error occurred while validating the signature", e);
                handleBadRequestError(obapiRequestContext, e.getErrorPayload());
            }
        }

    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

        if (obapiResponseContext.isError()) {
            return;
        }
        String basicAuthHeader = GatewayUtils.getBasicAuthHeader(urlMap.get(userName).toString(),
                String.valueOf((char[]) urlMap.get(GatewayConstants.PASSWORD)));
        Map<String, List<String>> regulatoryAPIs = GatewayDataHolder.getInstance()
                .getOpenBankingConfigurationService().getAllowedAPIs();

        switch (obapiResponseContext.getMsgInfo().getHttpMethod().toUpperCase()) {
            case HttpMethod.POST :
                if (HttpStatus.SC_CREATED == obapiResponseContext.getStatusCode()) {
                    String fullBackEndURL = urlMap.get(GatewayConstants.IAM_HOSTNAME).toString().concat("/")
                            .concat(obDCREndpoint);
                    postProcessResponseForRegister(obapiResponseContext, basicAuthHeader, fullBackEndURL,
                            regulatoryAPIs);
                }
                break;
            case HttpMethod.PUT :
                if (HttpStatus.SC_OK == obapiResponseContext.getStatusCode()) {
                    postProcessResponseForUpdate(obapiResponseContext, basicAuthHeader, regulatoryAPIs);
                }
                break;
            case HttpMethod.DELETE :
                if (HttpStatus.SC_NO_CONTENT == obapiResponseContext.getStatusCode()) {
                    postProcessResponseForDelete(obapiResponseContext, basicAuthHeader);
                }
        }
    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Generated(message = "Ignoring since it's implemented as an extension point")
    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        if (obapiRequestContext.isError()) {
            return;
        }
        String httpMethod = obapiRequestContext.getMsgInfo().getHttpMethod();

        if (HttpMethod.GET.equalsIgnoreCase(httpMethod) || HttpMethod.PUT.equalsIgnoreCase(httpMethod) ||
                HttpMethod.DELETE.equalsIgnoreCase(httpMethod)) {
            String[] contextPathValues = obapiRequestContext.getMsgInfo().getResource().split("/");
            String clientIdSentInRequest = "";
            List paramList = Arrays.asList(contextPathValues);
            int count = paramList.size();
            clientIdSentInRequest = paramList.stream().skip(count - 1).findFirst().get().toString();
            String clientIdBoundToToken = obapiRequestContext.getApiRequestInfo().getConsumerKey();
            if (!clientIdSentInRequest.equals(clientIdBoundToToken)) {
                obapiRequestContext.setError(true);
                obapiRequestContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP,
                        String.valueOf(OpenBankingErrorCodes.UNAUTHORIZED_CODE));
                Map<String, String> requestHeaders = obapiRequestContext.getMsgInfo().getHeaders();
                requestHeaders.remove(GatewayConstants.CONTENT_TYPE_TAG);
                requestHeaders.remove(GatewayConstants.CONTENT_LENGTH);
                obapiRequestContext.getMsgInfo().setHeaders(requestHeaders);
                return;
            }
        }
        char[] adminPassword = (char[]) urlMap.get(GatewayConstants.PASSWORD);
        String basicAuthHeader = GatewayUtils.getBasicAuthHeader(urlMap.get(userName).toString(),
                String.valueOf(adminPassword));
        Map<String, String> headers = new HashMap<>();
        String bearerAccessToken = "";
        if (obapiRequestContext.getMsgInfo().getHeaders() != null &&
                obapiRequestContext.getMsgInfo().getHeaders().get(GatewayConstants.AUTH_HEADER) != null) {
            bearerAccessToken = obapiRequestContext.getMsgInfo().getHeaders().get(GatewayConstants.AUTH_HEADER)
                    .replace(GatewayConstants.BEARER_TAG, "").trim();
        }
        headers.put(GatewayConstants.AUTH_HEADER, basicAuthHeader);
        headers.put(registrationAccessTokenParam, bearerAccessToken);
        obapiRequestContext.setAddedHeaders(headers);
        if (HttpMethod.DELETE.equalsIgnoreCase(httpMethod)) {
            try {
                //call dcr endpoint of IS to get the application name to be deleted
                JsonObject createdSpDetails = callGet(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                                .concat(obapiRequestContext.getApiRequestInfo().getConsumerKey()),
                        basicAuthHeader, "", "").getAsJsonObject();
                String applicationName = createdSpDetails.get("client_name").getAsString();

                //add application name to the cache
                String cacheKey = obapiRequestContext.getApiRequestInfo().getConsumerKey()
                        .concat(GatewayConstants.AM_APP_NAME_CACHEKEY);
                GatewayDataHolder.getGatewayCache().addToCache(GatewayCacheKey.of(cacheKey), applicationName);

            } catch (IOException | OpenBankingException | URISyntaxException e) {
                log.error("Error occurred while deleting application", e);
                handleRequestInternalServerError(obapiRequestContext, OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
            }

        }
    }

    /**
     * Method to handle response for DCR POST requests.
     *
     * @param obapiResponseContext OB response context object
     * @param basicAuthHeader Basic authentication header for accessing the DCR endpoint.
     * @param fullBackEndURL URL of the OB DCR Endpoint
     * @param regulatoryAPIs A map containing regulatory API names and the related authorized roles
     */
    private void postProcessResponseForRegister(OBAPIResponseContext obapiResponseContext, String basicAuthHeader,
                                            String fullBackEndURL, Map<String, List<String>> regulatoryAPIs) {

        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject createdDCRAppDetails = ((JsonObject) jsonParser
                    .parse(obapiResponseContext.getResponsePayload()));
            //get software statement from dcr app details
            String softwareStatement = createdDCRAppDetails.has(OpenBankingConstants.SOFTWARE_STATEMENT) ?
                    createdDCRAppDetails.get(OpenBankingConstants.SOFTWARE_STATEMENT).toString() : null;

            //call IS DCR endpoint to create application for obtaining a token to invoke devportal REST APIs
            JsonElement registrationResponse = createServiceProvider(basicAuthHeader,
                    createdDCRAppDetails.get("software_id").getAsString());
            if (registrationResponse == null) {
                log.error("Error while creating AM app for invoking APIM rest apis");
                String clientId = createdDCRAppDetails.get(clientIdParam).getAsString();
                //delete service provider
                callDelete(fullBackEndURL.concat("/").concat(clientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }
            //call token endpoint to retrieve a token for invoking the devportal REST apis
            String amRestAPIInvokeClientId = registrationResponse.getAsJsonObject()
                    .get(clientIdParam).getAsString();

            String authHeaderForTokenRequest = GatewayUtils
                    .getBasicAuthHeader(registrationResponse.getAsJsonObject().get(clientIdParam).getAsString(),
                            registrationResponse.getAsJsonObject().get(clientSecret).getAsString());

            JsonElement tokenResponse = getToken(authHeaderForTokenRequest,
                    urlMap.get(GatewayConstants.TOKEN_URL).toString(), amRestAPIInvokeClientId);

            if (tokenResponse == null || tokenResponse.getAsJsonObject().get("access_token") == null) {
                log.error("Error while creating tokens");
                String clientId = createdDCRAppDetails.get(clientIdParam).getAsString();
                //delete service provider
                callDelete(fullBackEndURL.concat("/").concat(clientId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(amRestAPIInvokeClientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }
            String token = tokenResponse.getAsJsonObject().get("access_token").getAsString();
            String getSPDetails = urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                    .concat(createdDCRAppDetails.get(clientIdParam).getAsString());
            //call IS dcr api to get client secret and client name
            JsonElement createdSpDetails = callGet(getSPDetails, basicAuthHeader, "", "");
            if (createdSpDetails == null) {
                log.error("Error while retrieving client id and secret");
                String clientId = createdDCRAppDetails.get(clientIdParam).getAsString();
                //delete service provider
                callDelete(fullBackEndURL.concat("/").concat(clientId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(amRestAPIInvokeClientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }

            //create am application
            JsonObject amAppCreatePayload = getAppCreatePayload(createdSpDetails.getAsJsonObject()
                    .get("client_name").getAsString());
            JsonElement amApplicationCreateResponse =
                    callPost(urlMap.get(GatewayConstants.APP_CREATE_URL).toString(),
                            amAppCreatePayload.toString(), GatewayConstants.BEARER_TAG.concat(token));

            if (amApplicationCreateResponse == null) {
                log.error("Error while creating AM app");
                String clientId = createdDCRAppDetails.get(clientIdParam).getAsString();
                //delete service provider
                callDelete(fullBackEndURL.concat("/").concat(clientId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(amRestAPIInvokeClientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }
            String keyMapURL = urlMap.get(GatewayConstants.KEY_MAP_URL).toString()
                    .replace("application-id", amApplicationCreateResponse.getAsJsonObject()
                            .get(applicationIdParam).getAsString());
            String keyManagerName = GatewayDataHolder.getInstance().getOpenBankingConfigurationService()
                    .getConfigurations().get(OpenBankingConstants.OB_KM_NAME).toString();

            //map keys to am application
            JsonObject keyMapPayload = getKeyMapPayload(createdDCRAppDetails.get(clientIdParam).getAsString(),
                    createdSpDetails.getAsJsonObject().get(clientSecret).getAsString(),
                    OpenBankingUtils.getSoftwareEnvironmentFromSSA(softwareStatement), keyManagerName);

            JsonElement amKeyMapResponse = callPost(keyMapURL, keyMapPayload.toString(),
                    GatewayConstants.BEARER_TAG.concat(token));
            if (amKeyMapResponse == null) {
                log.error("Error while mapping keys to AM app");
                String clientId = createdDCRAppDetails.get(clientIdParam).getAsString();
                //delete service provider
                callDelete(fullBackEndURL.concat("/").concat(clientId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(amRestAPIInvokeClientId), basicAuthHeader);
                //delete AM application
                callDelete(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()
                        .concat("/").concat(amApplicationCreateResponse.getAsJsonObject()
                                .get(applicationIdParam).getAsString()), GatewayConstants.BEARER_TAG.concat(token));
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }
            //get list of published APIs
            JsonElement publishedAPIsResponse = callGet(urlMap.get(GatewayConstants.API_RETRIEVE_URL).toString(),
                    GatewayConstants.BEARER_TAG.concat(token), "", "");
            if (publishedAPIsResponse == null) {
                log.error("Error while retrieving published APIs");
                String clientId = createdDCRAppDetails.get(clientIdParam).getAsString();
                //delete service provider
                callDelete(fullBackEndURL.concat("/").concat(clientId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(amRestAPIInvokeClientId), basicAuthHeader);
                //delete AM application
                callDelete(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()
                        .concat("/").concat(amApplicationCreateResponse.getAsJsonObject()
                                .get(applicationIdParam).getAsString()), GatewayConstants.BEARER_TAG.concat(token));
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }

            List<String> apiIDList = new ArrayList<>();
            if (regulatoryAPIs != null) {
                if (StringUtils.isEmpty(softwareStatement)) {
                    apiIDList = filterRegulatoryAPIs(regulatoryAPIs, publishedAPIsResponse.getAsJsonObject()
                            .get(OpenBankingConstants.API_LIST).getAsJsonArray(), Collections.emptyList());
                } else {
                    apiIDList = filterRegulatoryAPIs(regulatoryAPIs, publishedAPIsResponse.getAsJsonObject()
                            .get(OpenBankingConstants.API_LIST).getAsJsonArray(), getRolesFromSSA(softwareStatement));
                }
            } else {
                log.warn("No regulatory APIs configured. Application will be subscribed to all published APIs");
                //subscribe to all APIs if there are no configured regulatory APIs
                for (JsonElement apiInfo : publishedAPIsResponse.getAsJsonObject().get("list").getAsJsonArray()) {
                    apiIDList.add(apiInfo.getAsJsonObject().get("id").getAsString());
                }
            }
            //subscribe to apis
            JsonArray subscribeAPIsPayload = getAPISubscriptionPayload(amApplicationCreateResponse
                    .getAsJsonObject().get(applicationIdParam).getAsString(), apiIDList);
            JsonElement subscribeAPIsResponse = callPost(urlMap.get(GatewayConstants.API_SUBSCRIBE_URL).toString(),
                    subscribeAPIsPayload.toString(), "Bearer ".concat(token));
            if (subscribeAPIsResponse == null) {
                log.error("Error while subscribing to APIs");
                String clientId = createdDCRAppDetails.get(clientIdParam).getAsString();
                //delete service provider
                callDelete(fullBackEndURL.concat("/").concat(clientId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(amRestAPIInvokeClientId), basicAuthHeader);
                //delete AM application
                callDelete(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()
                        .concat("/").concat(amApplicationCreateResponse.getAsJsonObject()
                                .get(applicationIdParam).getAsString()), GatewayConstants.BEARER_TAG.concat(token));
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }

            //delete IAM application used to invoke am rest endpoints
            if (!callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                    .concat(amRestAPIInvokeClientId), basicAuthHeader)) {
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
            }

        } catch (IOException | OpenBankingException | URISyntaxException | ParseException e) {
            log.error("Error occurred while creating application", e);
            handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
        }
    }

    /**
     * Method to handle response for DCR PUT requests.
     *
     * @param obapiResponseContext OB response context object
     * @param basicAuthHeader Basic authentication header for accessing the DCR endpoint.
     * @param regulatoryAPIs A map containing regulatory API names and the related authorized roles
     */
    private void postProcessResponseForUpdate(OBAPIResponseContext obapiResponseContext, String basicAuthHeader,
                                           Map<String, List<String>> regulatoryAPIs) {

        JsonParser jsonParser = new JsonParser();
        JsonObject createdDCRAppDetails = ((JsonObject) jsonParser.parse(obapiResponseContext
                .getResponsePayload()));
        try {
            JsonObject dcrPayload = getIAMDCRPayload(createdDCRAppDetails.get("software_id").getAsString());
            JsonElement registrationResponse = callPost(urlMap.get(GatewayConstants.IAM_DCR_URL).toString(),
                    dcrPayload.toString(), basicAuthHeader);
            if (registrationResponse == null) {
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
                return;
            }
            //call token endpoint to retrieve a token for invoking the devportal REST apis
            String clientId = registrationResponse.getAsJsonObject().get(clientIdParam).getAsString();
            String authHeaderForTokenRequest = GatewayUtils.getBasicAuthHeader(clientId,
                    registrationResponse.getAsJsonObject().get(clientSecret).getAsString());

            JsonElement tokenResponse = getToken(authHeaderForTokenRequest,
                    urlMap.get(GatewayConstants.TOKEN_URL).toString(), clientId);
            if (tokenResponse == null || tokenResponse.getAsJsonObject().get("access_token") == null) {
                log.error("Error while creating tokens");
                //delete SP created to call dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(clientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
                return;
            }
            String token = tokenResponse.getAsJsonObject().get("access_token").getAsString();

            String applicationName = getApplicationName(obapiResponseContext.getResponsePayload(),
                    GatewayDataHolder.getInstance().getOpenBankingConfigurationService().getConfigurations());
            if (StringUtils.isEmpty(applicationName)) {
                log.error("Error while retrieving application name during update");
                //delete SP created to call dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(clientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
                return;
            }
            //call application get endpoint to retrieve the application id
            JsonElement applicationSearchResponse =
                    callGet(urlMap.get(GatewayConstants.APP_CREATE_URL).toString(),
                            GatewayConstants.BEARER_TAG.concat(token), "query", applicationName);
            if (applicationSearchResponse == null) {
                log.error("Error while searching for created application during update");
                //delete SP created to call dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(clientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
                return;
            }
            String applicationId = applicationSearchResponse.getAsJsonObject().get("list").getAsJsonArray().get(0)
                    .getAsJsonObject().get(applicationIdParam).getAsString();

            //get list of subscribed APIs
            JsonElement subscribedAPIsResponse = callGet(urlMap.get(GatewayConstants.API_GET_SUBSCRIBED).toString(),
                    GatewayConstants.BEARER_TAG.concat(token), "applicationId", applicationId);
            if (subscribedAPIsResponse == null) {
                log.error("Error while retrieving subscribed APIs");
                //delete SP created to call dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(clientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
                return;
            }
            List<String> subscribedAPIIdList = new ArrayList<>();
            for (JsonElement subscribedAPI : subscribedAPIsResponse.getAsJsonObject().get("list")
                    .getAsJsonArray()) {
                String apiId = subscribedAPI.getAsJsonObject().get("apiId").getAsString();
                subscribedAPIIdList.add(apiId);
            }

            //get software statement from dcr app details
            String softwareStatement = createdDCRAppDetails.has(OpenBankingConstants.SOFTWARE_STATEMENT) ?
                    createdDCRAppDetails.get(OpenBankingConstants.SOFTWARE_STATEMENT).getAsString() : null;
            if (StringUtils.isNotEmpty(softwareStatement)) {
                final JsonArray subscribedAPIs = subscribedAPIsResponse.getAsJsonObject()
                        .get("list").getAsJsonArray();
                //check whether the ssa still contains the roles related to the subscribed APIs and unsubscribe if not
                Optional.of(getRolesFromSSA(softwareStatement))
                        .map(ssaRoles -> getUnAuthorizedAPIs(subscribedAPIs, regulatoryAPIs, ssaRoles))
                        .flatMap(unAuthorizedApis -> unAuthorizedApis.stream()
                                .map(unAuthorizedApi -> String.format("%s/%s",
                                        urlMap.get(GatewayConstants.API_GET_SUBSCRIBED).toString(), unAuthorizedApi))
                                .filter(endpoint -> isSubscriptionDeletionFailed(endpoint, GatewayConstants.BEARER_TAG
                                        .concat(token)))
                                .findAny())
                        .ifPresent(endpoint -> {
                            log.error("Error while unsubscribing from API: " + endpoint);
                            //delete SP created to call dev portal REST APIs
                            try {
                                callDelete(String.format("%s/%s", urlMap.get(GatewayConstants.IAM_DCR_URL).toString(),
                                        clientId), basicAuthHeader);
                            } catch (OpenBankingException | IOException e) {
                                handleInternalServerError(obapiResponseContext,
                                        OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                            }
                        });
            }
            //subscribe to new APIs if new roles were added to the SSA
            //get list of published APIs
            JsonElement publishedAPIsResponse = callGet(urlMap.get(GatewayConstants.API_RETRIEVE_URL).toString(),
                    GatewayConstants.BEARER_TAG.concat(token), "", "");
            if (publishedAPIsResponse == null) {
                log.error("Error while retrieving published APIs");
                //delete SP created to call dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(clientId), basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
                return;
            }
            List<String> apiIDList = new ArrayList<>();
            if (StringUtils.isEmpty(softwareStatement)) {
                filterRegulatoryAPIs(regulatoryAPIs, publishedAPIsResponse.getAsJsonObject()
                        .get(OpenBankingConstants.API_LIST).getAsJsonArray(), Collections.emptyList());
            } else {
                filterRegulatoryAPIs(regulatoryAPIs, publishedAPIsResponse.getAsJsonObject()
                        .get(OpenBankingConstants.API_LIST).getAsJsonArray(), getRolesFromSSA(softwareStatement));
            }

            List<String> newApisListToSubscribe = getNewAPIsToSubscribe(apiIDList, subscribedAPIIdList);
            if (!newApisListToSubscribe.isEmpty()) {
                JsonArray subscribeAPIsPayload = getAPISubscriptionPayload(applicationId, newApisListToSubscribe);
                JsonElement subscribeAPIsResponse = callPost(urlMap.get(GatewayConstants.API_SUBSCRIBE_URL)
                        .toString(), subscribeAPIsPayload.toString(), "Bearer ".concat(token));
                if (subscribeAPIsResponse == null) {
                    log.error("Error while subscribing to APIs");
                    //delete SP created to call dev portal REST APIs
                    callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                            .concat(clientId), basicAuthHeader);
                    handleInternalServerError(obapiResponseContext,
                            OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
                    return;
                }
            }
            //delete IAM application used to invoke am rest endpoints
            if (!callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                    .concat(clientId), basicAuthHeader)) {
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
            }
        } catch (ParseException | IOException | URISyntaxException | OpenBankingException e) {
            log.error("Error occurred while creating application", e);
            handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTRATION_UPDATE_ERROR);
        }
    }


    /**
     * Method to handle response for DCR DELETE requests.
     *
     * @param obapiResponseContext OB response context object
     * @param basicAuthHeader Basic authentication header for accessing the DCR endpoint.
     */
    private void postProcessResponseForDelete(OBAPIResponseContext obapiResponseContext, String basicAuthHeader) {

        try {
            JsonObject dcrPayload = getIAMDCRPayload(obapiResponseContext.getApiRequestInfo().getConsumerKey());
            JsonElement registrationResponse = callPost(urlMap.get(GatewayConstants.IAM_DCR_URL).toString(),
                    dcrPayload.toString(), basicAuthHeader);
            if (registrationResponse == null) {
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
                return;
            }

            //call token endpoint to retrieve a token for invoking the devportal REST apis
            String clientId = registrationResponse.getAsJsonObject().get(clientIdParam).getAsString();
            String authHeaderForTokenRequest = GatewayUtils.getBasicAuthHeader(clientId,
                    registrationResponse.getAsJsonObject().get(clientSecret).getAsString());

            JsonElement tokenResponse = getToken(authHeaderForTokenRequest,
                    urlMap.get(GatewayConstants.TOKEN_URL).toString(), clientId);
            if (tokenResponse == null || tokenResponse.getAsJsonObject().get("access_token") == null) {
                log.error("Error while creating tokens during delete");
                //delete IAM application used to invoke am rest endpoints
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/").concat(clientId),
                        basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
                return;
            }
            String token = tokenResponse.getAsJsonObject().get("access_token").getAsString();

            //get application id of the sent request
            String cacheKey = obapiResponseContext.getApiRequestInfo().getConsumerKey()
                    .concat(GatewayConstants.AM_APP_NAME_CACHEKEY);
            String applicationName = GatewayDataHolder.getGatewayCache()
                    .getFromCache(GatewayCacheKey.of(cacheKey)).toString();

            //Adding applicationName to contextProps for use in next steps
            Map<String, String> contextProps = obapiResponseContext.getContextProps();
            contextProps.put("AppName", applicationName);
            obapiResponseContext.setContextProps(contextProps);

            //call application get endpoint to retrieve the application id
            JsonElement applicationSearchResponse =
                    callGet(urlMap.get(GatewayConstants.APP_CREATE_URL).toString(),
                            GatewayConstants.BEARER_TAG.concat(token), "query", applicationName);
            if (applicationSearchResponse == null) {
                log.error("Error while searching application during delete");
                //delete IAM application used to invoke am rest endpoints
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/").concat(clientId),
                        basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
                return;
            }

            String applicationId = applicationSearchResponse.getAsJsonObject().get("list").getAsJsonArray().get(0)
                    .getAsJsonObject().get(applicationIdParam).getAsString();

            if (!callDelete(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()
                    .concat("/").concat(applicationId), GatewayConstants.BEARER_TAG.concat(token))) {
                log.error("Error while deleting AM application");
                //delete IAM application used to invoke am rest endpoints
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/").concat(clientId),
                        basicAuthHeader);
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
                return;
            }

            //delete IAM application used to invoke am rest endpoints
            if (!callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/").concat(clientId),
                    basicAuthHeader)) {
                handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
            }
        } catch (IOException | OpenBankingException | URISyntaxException e) {
            log.error("Error occurred while deleting application", e);
            handleInternalServerError(obapiResponseContext, OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
        }
    }

    private JsonObject getIAMDCRPayload(String uniqueId) {

        JsonObject jsonObject = new JsonObject();
        JsonElement jsonElement = new JsonArray();
        /* Concatenating the unique id (software id/client id) to the rest api invoking SP name to avoid
             issues in parallel requests
         */
        String restApiInvokerName = "AM_RESTAPI_INVOKER_".concat(uniqueId);
        ((JsonArray) jsonElement).add("client_credentials");
        jsonObject.addProperty("client_name", restApiInvokerName);
        jsonObject.add("grant_types", jsonElement);
        return jsonObject;
    }

    private JsonObject getAppCreatePayload(String applicationName) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", applicationName);
        jsonObject.addProperty("throttlingPolicy", "Unlimited");
        return jsonObject;

    }

    private JsonObject getKeyMapPayload(String consumerKey, String consumerSecret, String keyType,
                                        String keyManagerName) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("consumerKey", consumerKey);
        jsonObject.addProperty("consumerSecret", consumerSecret);
        jsonObject.addProperty("keyType", keyType);
        jsonObject.addProperty("keyManager", keyManagerName);
        return jsonObject;

    }

    private JsonArray getAPISubscriptionPayload(String applicationId, List<String> apiIdList) {

        JsonArray jsonArray = new JsonArray();
        for (String apiID : apiIdList) {
            JsonObject apiInfo = new JsonObject();
            apiInfo.addProperty(applicationIdParam, applicationId);
            apiInfo.addProperty("apiId", apiID);
            apiInfo.addProperty("throttlingPolicy", "Unlimited");
            jsonArray.add(apiInfo);
        }
        return jsonArray;
    }

    @Generated(message = "Excluding since it requires an Http response")
    private JsonElement getResponse(HttpResponse response) throws IOException {

        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK ||
                response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            String responseStr = EntityUtils.toString(entity);
            JsonParser parser = new JsonParser();
            return parser.parse(responseStr);

        } else {
            String error = String.format("Error while invoking rest api : %s %s",
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            log.error(error);
            return null;
        }

    }

    @Generated(message = "Excluding from test coverage since it is an HTTP call")
    protected JsonElement callPost(String endpoint, String payload, String authenticationHeader)
            throws IOException, OpenBankingException {

        try (CloseableHttpClient httpClient = HTTPClientUtils.getHttpsClient()) {
            HttpPost httpPost = new HttpPost(endpoint);
            StringEntity entity = new StringEntity(payload);
            httpPost.setEntity(entity);
            httpPost.setHeader(GatewayConstants.ACCEPT, GatewayConstants.JSON_CONTENT_TYPE);
            httpPost.setHeader(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, authenticationHeader);
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            return getResponse(httpResponse);
        }
    }

    @Generated(message = "Excluding from test coverage since it is an HTTP call")
    protected JsonElement getToken(String authHeader, String url, String clientId) throws IOException, JSONException,
            OpenBankingException {

        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpPost request = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            params.add(new BasicNameValuePair("scope", "apim:subscribe apim:api_key apim:app_manage " +
                    "apim:sub_manage openid"));
            //params.add(new BasicNameValuePair("client_id", clientId));
            request.setEntity(new UrlEncodedFormEntity(params));
            request.addHeader(HTTPConstants.HEADER_AUTHORIZATION, authHeader);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Obtaining access token  failed with status code: " +
                        response.getStatusLine().getStatusCode());
                return new JsonObject();
            }
            return getResponse(response);
        }
    }

    /**
     * Filters the regulatory APIs based on the  given software roles.
     *
     * @param regulatoryAPIs A map containing regulatory API names and their allowed roles
     * @param publishedAPIs The list of published APIs as JSON array
     * @param softwareRoles The list of software roles provided in the request
     * @return A list of API IDs that the application is authorized to access
     */
    protected List<String> filterRegulatoryAPIs(Map<String, List<String>> regulatoryAPIs, JsonArray publishedAPIs,
                                                List<String> softwareRoles) {

        List<String> filteredAPIs = new ArrayList<>();
        for (JsonElement publishedAPIInfo : publishedAPIs) {
            String publishedAPIName = publishedAPIInfo.getAsJsonObject().get(OpenBankingConstants.API_NAME)
                    .getAsString();
            if (regulatoryAPIs.containsKey(publishedAPIName)) {
                List<String> allowedRolesForAPI = regulatoryAPIs.get(publishedAPIName);
                // Check if no specific roles are configured for the API or if software roles contain any of the
                // allowed roles
                if (allowedRolesForAPI.isEmpty() || allowedRolesForAPI.stream().anyMatch(softwareRoles::contains)) {
                    filteredAPIs.add(publishedAPIInfo.getAsJsonObject().get(OpenBankingConstants.API_ID).getAsString());
                }
            }
        }
        return filteredAPIs;
    }

    @Generated(message = "Excluding from test coverage since it is an HTTP call")
    protected JsonElement callGet(String endpoint, String authHeader, String queryParamKey, String paramValue)
            throws IOException, OpenBankingException, URISyntaxException {

        try (CloseableHttpClient httpClient = HTTPClientUtils.getHttpsClient()) {
            HttpGet httpGet = new HttpGet(endpoint);
            List nameValuePairs = new ArrayList();
            if (StringUtils.isNotEmpty(queryParamKey)) {
                nameValuePairs.add(new BasicNameValuePair(queryParamKey, paramValue));
                URI uri = new URIBuilder(httpGet.getURI()).addParameters(nameValuePairs).build();
                ((HttpRequestBase) httpGet).setURI(uri);
            }
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            CloseableHttpResponse restAPIResponse = httpClient.execute(httpGet);
            return getResponse(restAPIResponse);
        }
    }

    private void handleInternalServerError(OBAPIResponseContext obapiResponseContext, String message) {

        //catch errors and set to context
        OpenBankingExecutorError error = new OpenBankingExecutorError(OpenBankingErrorCodes.SERVER_ERROR_CODE,
                "Internal server error", message, OpenBankingErrorCodes.SERVER_ERROR_CODE);
        ArrayList<OpenBankingExecutorError> executorErrors = obapiResponseContext.getErrors();
        executorErrors.add(error);
        obapiResponseContext.setError(true);
        obapiResponseContext.setErrors(executorErrors);

    }

    private void handleRequestInternalServerError(OBAPIRequestContext obapiResponseContext, String message) {

        //catch errors and set to context
        OpenBankingExecutorError error = new OpenBankingExecutorError(OpenBankingErrorCodes.SERVER_ERROR_CODE,
                "Internal server error", message, OpenBankingErrorCodes.SERVER_ERROR_CODE);
        ArrayList<OpenBankingExecutorError> executorErrors = obapiResponseContext.getErrors();
        executorErrors.add(error);
        obapiResponseContext.setError(true);
        obapiResponseContext.setErrors(executorErrors);

    }

    @Generated(message = "Excluding from test coverage since it is an HTTP call")
    protected boolean callDelete(String endpoint, String authHeader) throws OpenBankingException, IOException {

        try (CloseableHttpClient httpClient = HTTPClientUtils.getHttpsClient()) {
            HttpDelete httpDelete = new HttpDelete(endpoint);
            httpDelete.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            CloseableHttpResponse appDeletedResponse = httpClient.execute(httpDelete);
            int status = appDeletedResponse.getStatusLine().getStatusCode();
            return (status == 204 || status == 200);
        }
    }

    /**
     * Check if the deletion of subscription at a given endpoint failed.
     *
     * @param endpoint   The URL of the endpoint where the subscription deletion is attempted
     * @param authHeader The authorization header to be used in the HTTP request
     * @return True if the subscription deletion fails or an exception occurs, false otherwise
     */
  protected boolean isSubscriptionDeletionFailed(String endpoint, String authHeader) {

        try {
            return !callDelete(endpoint, GatewayConstants.BEARER_TAG.concat(authHeader));
        } catch (OpenBankingException | IOException e) {
            return true;
        }
    }

    private void handleBadRequestError(OBAPIRequestContext obapiRequestContext, String message) {

        //catch errors and set to context
        OpenBankingExecutorError error = new OpenBankingExecutorError("Bad request",
                "invalid_client_metadata", message, "400");
        ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
        executorErrors.add(error);
        obapiRequestContext.setError(true);
        obapiRequestContext.setErrors(executorErrors);

    }

    @Generated(message = "Excluding from unit tests since there is an external http call")
    private void validateRequestSignature(String payload, OBAPIRequestContext obapiRequestContext)
            throws ParseException, JOSEException, BadJOSEException, MalformedURLException,
            OpenBankingExecutorException {

        String jwksEndpointName = GatewayDataHolder.getInstance().getOpenBankingConfigurationService()
                .getConfigurations().get(OpenBankingConstants.JWKS_ENDPOINT_NAME).toString();
        //decode request jwt
        JSONObject decodedSSA;
        JSONObject decodedRequest = JWTUtils.decodeRequestJWT(payload, "body");

        //Check whether decodedRequest is null
        if (decodedRequest == null) {
            throw new OpenBankingExecutorException("invalid_client_metadata", OpenBankingErrorCodes.BAD_REQUEST_CODE,
                    "Provided jwt is malformed and cannot be decoded");
        }

        //Check whether the SSA exists and decode the SSA
        if (decodedRequest.containsKey(OpenBankingConstants.SOFTWARE_STATEMENT) &&
                decodedRequest.getAsString(OpenBankingConstants.SOFTWARE_STATEMENT) != null) {
            decodedSSA = JWTUtils.decodeRequestJWT(decodedRequest
                    .getAsString(OpenBankingConstants.SOFTWARE_STATEMENT), "body");
        } else {
            //Throwing an exception whn SSA is not found
            throw new OpenBankingExecutorException("invalid_client_metadata", OpenBankingErrorCodes.BAD_REQUEST_CODE,
                    "Required parameter software statement cannot be null");
        }

        //validate request signature
        String jwksEndpoint = decodedSSA.getAsString(jwksEndpointName);
        SignedJWT signedJWT = SignedJWT.parse(payload);
        String alg = signedJWT.getHeader().getAlgorithm().getName();
        JWTUtils.validateJWTSignature(payload, jwksEndpoint, alg);
        obapiRequestContext.setModifiedPayload(decodedRequest.toJSONString());
        Map<String, String> requestHeaders = obapiRequestContext.getMsgInfo().getHeaders();
        requestHeaders.remove("Content-Type");
        Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
        addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
        obapiRequestContext.setAddedHeaders(addedHeaders);
        obapiRequestContext.getMsgInfo().setHeaders(requestHeaders);
    }

    /**
     * Extract roles from SSA.
     *
     * @param softwareStatement software statement extracted from request payload
     * @return list of roles
     * @throws ParseException
     */
    public List<String> getRolesFromSSA(String softwareStatement) throws ParseException {

        List<String> softwareRoleList = new ArrayList<>();
        // decode software statement and get payload
        JSONObject softwareStatementBody = JWTUtils.decodeRequestJWT(softwareStatement, "body");
        Object softwareRolesStr = softwareStatementBody.get(OpenBankingConstants.SOFTWARE_ROLES);
        if (softwareRolesStr instanceof JSONArray) {
            JSONArray softwareRoles = (JSONArray) softwareRolesStr;
            for (Object role : softwareRoles) {
                softwareRoleList.add(role.toString());
            }
        } else if (softwareRolesStr instanceof String) {
            softwareRoleList = Arrays.asList(softwareRolesStr.toString().split(" "));
        }
        return softwareRoleList;
    }

    protected String getApplicationName(String responsePayload, Map<String, Object> configurations)
            throws ParseException {

        JsonParser jsonParser = new JsonParser();
        JsonObject createdDCRAppDetails = ((JsonObject) jsonParser.parse(responsePayload));
        String softwareStatement = createdDCRAppDetails.has(OpenBankingConstants.SOFTWARE_STATEMENT) ?
                createdDCRAppDetails.get(OpenBankingConstants.SOFTWARE_STATEMENT).getAsString() : null;
        boolean isSoftwareIdAppName = Boolean.parseBoolean(configurations
                .get(OpenBankingConstants.DCR_USE_SOFTWAREID_AS_APPNAME).toString());
        String applicationNameKey = configurations.get(OpenBankingConstants.DCR_APPLICATION_NAME_KEY).toString();

        // If a software statement is not provided, get the software id directly from created app details
        if (StringUtils.isEmpty(softwareStatement)) {
            if (isSoftwareIdAppName) {
                return createdDCRAppDetails.get(OpenBankingConstants.SOFTWARE_ID).getAsString();
            }
        } else {
            JSONObject softwareStatementBody = JWTUtils.decodeRequestJWT(softwareStatement,
                    OpenBankingConstants.JWT_BODY);
            if (isSoftwareIdAppName) {
                //get software id form the software statement
                return softwareStatementBody.get(OpenBankingConstants.SOFTWARE_ID).toString();
            } else if (softwareStatementBody.containsKey(applicationNameKey)) {
                return softwareStatementBody.get(applicationNameKey).toString();
            }
        }
        return createdDCRAppDetails.get(applicationNameKey).getAsString();
    }

    protected List<String> getUnAuthorizedAPIs(JsonArray subscribedAPIs, Map<String, List<String>> configuredAPIs,
                                               List<String> allowedRoles) {

        List<String> apisToUnsubscribe = new ArrayList<>();
        for (JsonElement apiName : subscribedAPIs) {
            for (Map.Entry<String, List<String>> entry : configuredAPIs.entrySet()) {
                if (entry.getKey().equals(apiName.getAsJsonObject().get("apiInfo").getAsJsonObject().get("name")
                        .getAsString())) {
                    List<String> allowedRolesForAPI = entry.getValue();
                    boolean allowedAPI = false;
                    for (String allowedRole : allowedRolesForAPI) {
                        if (allowedRoles.contains(allowedRole)) {
                            allowedAPI = true;
                            break;
                        }
                    }
                    if (!allowedAPI) {
                        apisToUnsubscribe.add(apiName.getAsJsonObject().get("subscriptionId").getAsString());
                    }
                }
            }
        }
        return apisToUnsubscribe;
    }

    protected List<String> getNewAPIsToSubscribe(List<String> filteredAPIs, List<String> subscribedAPIs) {

        List<String> apisToSubscribe = new ArrayList<>();
        for (String publishedAPI : filteredAPIs) {
            if (!subscribedAPIs.contains(publishedAPI)) {
                apisToSubscribe.add(publishedAPI);
            }
        }
        return apisToSubscribe;
    }

    protected JsonElement createServiceProvider(String basicAuthHeader, String softwareId)
            throws IOException, OpenBankingException {

        JsonObject dcrPayload = getIAMDCRPayload(softwareId);
        return callPost(urlMap.get(GatewayConstants.IAM_DCR_URL).toString(),
                dcrPayload.toString(), basicAuthHeader);
    }
}
