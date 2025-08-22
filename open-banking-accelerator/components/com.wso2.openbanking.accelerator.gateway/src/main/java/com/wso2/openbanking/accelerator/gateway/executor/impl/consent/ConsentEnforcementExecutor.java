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

package com.wso2.openbanking.accelerator.gateway.executor.impl.consent;

import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Consent Enforcement executor.
 */
public class ConsentEnforcementExecutor implements OpenBankingGatewayExecutor {

    protected static final String ERROR_TITLE = "Consent Enforcement Error";
    protected static final String HEADERS_TAG = "headers";
    protected static final String BODY_TAG = "body";
    protected static final String CONTEXT_TAG = "context";
    protected static final String RESOURCE_TAG = "resource";
    protected static final String ELECTED_RESOURCE_TAG = "electedResource";
    protected static final String HTTP_METHOD = "httpMethod";
    protected static final String CONSENT_ID_TAG = "consentId";
    protected static final String USER_ID_TAG = "userId";
    protected static final String CLIENT_ID_TAG = "clientId";
    protected static final String RESOURCE_PARAMS = "resourceParams";
    private static final Log log = LogFactory.getLog(ConsentEnforcementExecutor.class);
    private static final GatewayDataHolder dataHolder = GatewayDataHolder.getInstance();
    private static final String INFO_HEADER_TAG = "Account-Request-Information";
    private static final String IS_VALID = "isValid";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String HTTP_CODE = "httpCode";
    private static final String MODIFIED_PAYLOAD = "modifiedPayload";
    private static final String CONSENT_INFO = "consentInformation";
    private static volatile String consentValidationEndpoint;
    private static volatile Key key;

    private static String getValidationEndpoint() {

        if (consentValidationEndpoint == null) {
            synchronized (ConsentEnforcementExecutor.class) {
                if (consentValidationEndpoint == null) {
                    consentValidationEndpoint = dataHolder
                            .getOpenBankingConfigurationService().getConfigurations()
                            .get(GatewayConstants.CONSENT_VALIDATION_ENDPOINT_TAG).toString();
                }
            }
        }
        return consentValidationEndpoint;

    }

    /**
     * Method to obtain signing key.
     *
     * @return Key as an Object.
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    // Suppressed content - dataHolder.getKeyStoreLocation()
    // Suppression reason - False Positive : Keystore location is obtained from deployment.toml. So it can be marked
    //                      as a trusted filepath
    // Suppressed warning count - 1
    protected static Key getJWTSigningKey() {

        if (key == null) {
            synchronized (ConsentEnforcementExecutor.class) {
                if (key == null) {
                    try (FileInputStream is = new FileInputStream(dataHolder.getKeyStoreLocation())) {
                        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                        keystore.load(is, dataHolder.getKeyStorePassword());
                        key = keystore.getKey(dataHolder.getKeyAlias(), dataHolder.getKeyPassword().toCharArray());
                    } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException
                            | UnrecoverableKeyException e) {
                        log.error("Error occurred while retrieving private key from keystore ", e);
                    }
                }
            }
        }
        return key;
    }

    /**
     * Method to handle request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Generated(message = "Unit testable components are covered")
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    /**
     * Method to handle response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    protected String generateJWT(String payload) {

        return Jwts.builder()
                .setPayload(payload)
                .signWith(SignatureAlgorithm.RS512, getJWTSigningKey())
                .compact();
    }

    /**
     * Method to invoke consent validation service when the JWT payload is provided.
     *
     * @param enforcementJWTPayload JWT Payload
     * @return Response as a String
     * @throws IOException When failed to invoke the validation endpoint or failed to parse the response.
     */
    @Generated(message = "Ignoring from unit tests since this method require calling external component to function")
    private String invokeConsentValidationService(String enforcementJWTPayload) throws IOException,
            OpenBankingException {

        HttpPost httpPost = new HttpPost(getValidationEndpoint());
        StringEntity params;
        params = new StringEntity(enforcementJWTPayload);
        httpPost.setEntity(params);
        httpPost.setHeader(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JWT_CONTENT_TYPE);
        String userName = GatewayUtils.getAPIMgtConfig(GatewayConstants.API_KEY_VALIDATOR_USERNAME);
        String password = GatewayUtils.getAPIMgtConfig(GatewayConstants.API_KEY_VALIDATOR_PASSWORD);
        httpPost.setHeader(GatewayConstants.AUTH_HEADER, GatewayUtils.getBasicAuthHeader(userName, password));
        try (CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(httpPost)) {
            InputStream in = response.getEntity().getContent();
            in.close();
            return IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
        }
    }

    /**
     * Method to handle errors.
     *
     * @param obapiRequestContext API Context
     * @param errorCode           Error Code
     * @param errorMessage        Error Message
     * @param httpCode            HTTP status code ( in 4XX range)
     */
    protected void handleError(OBAPIRequestContext obapiRequestContext, String errorCode, String errorMessage,
                               String httpCode) {

        obapiRequestContext.setError(true);
        ArrayList<OpenBankingExecutorError> errors = obapiRequestContext.getErrors();
        errors.add(new OpenBankingExecutorError(errorCode, ERROR_TITLE, errorMessage, httpCode));
        obapiRequestContext.setErrors(errors);
        obapiRequestContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP, httpCode);
    }

    /**
     * Method to create validation payload.
     *
     * @param requestHeaders Request headers of original request
     * @param requestPayload Request payload of original request
     * @return JSON Object with added attributes.
     */
    protected JSONObject createValidationRequestPayload(Map<String, String> requestHeaders, String requestPayload,
                                                        Map<String, Object> additionalParams) {

        JSONObject validationRequest = new JSONObject();
        JSONObject headers = new JSONObject();
        requestHeaders.forEach(headers::put);
        validationRequest.put(HEADERS_TAG, headers);
        /*requestContextDTO.getMsgInfo().getPayloadHandler().consumeAsString() method sets the request payload as a
        null string, hence adding string null check to the validation*/
        if (requestPayload != null && !requestPayload.isEmpty() && !requestPayload.equals("null")) {
            //This assumes all input payloads are in Content-Type : Application/JSON
            validationRequest.put(BODY_TAG, new JSONObject(requestPayload));
        }
        additionalParams.forEach(validationRequest::put);
        return validationRequest;
    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {
        // Consent ID is required for consent enforcement. If the consent ID is null, we are assume this is a
        // pre-consent creation call. Therefore consent enforcement is not required.
        if (obapiRequestContext.isError() || obapiRequestContext.getConsentId() == null) {
            return;
        }

        Map<String, String> requestHeaders = obapiRequestContext.getMsgInfo().getHeaders();
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(ELECTED_RESOURCE_TAG, obapiRequestContext.getMsgInfo().getElectedResource());
        additionalParams.put(CONSENT_ID_TAG, obapiRequestContext.getConsentId());
        additionalParams.put(USER_ID_TAG, obapiRequestContext.getApiRequestInfo().getUsername());
        additionalParams.put(CLIENT_ID_TAG, obapiRequestContext.getApiRequestInfo().getConsumerKey());
        additionalParams.put(RESOURCE_PARAMS, getResourceParamMap(obapiRequestContext));

        JSONObject validationRequest;
        if (StringUtils.isNotBlank(obapiRequestContext.getModifiedPayload())) {
            validationRequest = createValidationRequestPayload(requestHeaders,
                    obapiRequestContext.getModifiedPayload(), additionalParams);
        } else {
            validationRequest = createValidationRequestPayload(requestHeaders,
                    obapiRequestContext.getRequestPayload(), additionalParams);
        }
        String enforcementJWTPayload = generateJWT(validationRequest.toString());
        JSONObject jsonResponse;
        try {
            String response = invokeConsentValidationService(enforcementJWTPayload);
            jsonResponse = new JSONObject(response);
        } catch (IOException | OpenBankingException e) {
            handleError(obapiRequestContext, OpenBankingErrorCodes.CONSENT_VALIDATION_REQUEST_FAILURE, e.getMessage(),
                    OpenBankingErrorCodes.SERVER_ERROR_CODE);
            return;
        }

        boolean isValid = (boolean) jsonResponse.get(IS_VALID);
        if (!isValid) {
            String errorCode = jsonResponse.get(ERROR_CODE).toString();
            String errorMessage = jsonResponse.get(ERROR_MESSAGE).toString();
            String httpCode = jsonResponse.get(HTTP_CODE).toString();
            obapiRequestContext.setError(true);
            handleError(obapiRequestContext, errorCode, errorMessage, httpCode);
            return;
        } else if (!jsonResponse.isNull(MODIFIED_PAYLOAD)) {
            Object modifiedPayloadObj = jsonResponse.get(MODIFIED_PAYLOAD);
            if (modifiedPayloadObj != null) {
                obapiRequestContext.setModifiedPayload(modifiedPayloadObj.toString());
            }
        } else if (!jsonResponse.isNull(CONSENT_INFO)) {
            Object consentInformationObj = jsonResponse.get(CONSENT_INFO);
            if (consentInformationObj != null) {
                requestHeaders.put(INFO_HEADER_TAG, consentInformationObj.toString());
                obapiRequestContext.setAddedHeaders(requestHeaders);
            }
        }
    }

    /**
     * Method to construct resource parameter map to invoke the validation service.
     *
     * @param obapiRequestContext
     * @return A Map containing resource path(ex: /aisp/accounts/{AccountId}?queryParam=urlEncodedQueryParamValue),
     * http method and context(ex: /open-banking/v3.1/aisp)
     */
    private Map<String, String> getResourceParamMap(OBAPIRequestContext obapiRequestContext) {

        Map<String, String> resourceMap = new HashMap();
        resourceMap.put(RESOURCE_TAG, obapiRequestContext.getMsgInfo().getResource());
        resourceMap.put(HTTP_METHOD, obapiRequestContext.getMsgInfo().getHttpMethod());
        resourceMap.put(CONTEXT_TAG, obapiRequestContext.getApiRequestInfo().getContext());

        return resourceMap;
    }
}
