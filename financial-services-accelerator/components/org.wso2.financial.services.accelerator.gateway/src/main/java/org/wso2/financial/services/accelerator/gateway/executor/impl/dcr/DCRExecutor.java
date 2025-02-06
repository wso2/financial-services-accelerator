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

package org.wso2.financial.services.accelerator.gateway.executor.impl.dcr;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesErrorCodes;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.gateway.executor.core.FinancialServicesGatewayExecutor;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSExecutorError;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;
import org.wso2.financial.services.accelerator.gateway.util.GatewayUtils;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

/**
 * Executor for DCR.
 */
public class DCRExecutor implements FinancialServicesGatewayExecutor {

    private static final Log log = LogFactory.getLog(DCRExecutor.class);
    private static final Map<String, Object> configs = GatewayDataHolder.getInstance()
            .getFinancialServicesConfigurationService().getConfigurations();

    @Override
    public void preProcessRequest(FSAPIRequestContext fsapiRequestContext) {

        if (fsapiRequestContext.isError()) {
            return;
        }

        boolean validateJWT = true;
        if (configs.containsKey(FinancialServicesConstants.VALIDATE_JWT)) {
            validateJWT = Boolean.parseBoolean(configs.get(FinancialServicesConstants.VALIDATE_JWT).toString());
        }

        if (validateJWT) {
            String payload = fsapiRequestContext.getRequestPayload();
            String httpMethod = fsapiRequestContext.getMsgInfo().getHttpMethod();
            if (HttpMethod.POST.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod)) {
                if (payload != null) {
                    //decode request jwt
                    String decodedRequest = null;
                    try {
                        decodedRequest = JWTUtils.decodeRequestJWT(payload, FinancialServicesConstants.JWT_BODY);
                    } catch (ParseException e) {
                        log.error("Error occurred while decoding the provided jwt", e);
                        handleBadRequestError(fsapiRequestContext, GatewayConstants.INVALID_REQUEST,
                                "Malformed request JWT");
                    }

                    //Check whether decodedRequest is null
                    if (decodedRequest == null) {
                        handleBadRequestError(fsapiRequestContext, GatewayConstants.INVALID_REQUEST,
                                "Provided jwt is malformed and cannot be decoded");
                        return;
                    }

                    JSONObject decodedRequestObj = new JSONObject(decodedRequest);
                    JSONObject decodedSSA;
                    //Check whether the SSA exists and decode the SSA
                    if (decodedRequestObj.has(GatewayConstants.SOFTWARE_STATEMENT) &&
                            decodedRequestObj.getString(GatewayConstants.SOFTWARE_STATEMENT) != null) {
                        try {
                            String ssa = JWTUtils.decodeRequestJWT(decodedRequestObj
                                            .getString(GatewayConstants.SOFTWARE_STATEMENT),
                                    FinancialServicesConstants.JWT_BODY);
                            decodedSSA = new JSONObject(ssa);
                        } catch (ParseException e) {
                            log.error("Error occurred while decoding the provided jwt", e);
                            handleBadRequestError(fsapiRequestContext, GatewayConstants.INVALID_SSA,
                                    "Malformed Software Statement JWT found");
                            return;
                        }
                    } else {
                        handleBadRequestError(fsapiRequestContext, GatewayConstants.INVALID_SSA,
                                "Required parameter software statement cannot be null");
                        return;
                    }

                    try {
                        // Validate the request signature
                        JWTClaimsSet requestClaims = GatewayUtils.validateRequestSignature(payload, decodedSSA);
                        // Construct the IS DCR request payload
                        String dcrISPayload = GatewayUtils.constructISDcrRequestPayload(requestClaims, decodedSSA);
                        // Add the request payload to the context to use in response processing
                        fsapiRequestContext.addContextProperty(GatewayConstants.REQUEST_PAYLOAD,
                                String.valueOf(new JSONObject(requestClaims.getClaims())));

                        // Set the modified payload to the context
                        fsapiRequestContext.setModifiedPayload(dcrISPayload);
                        Map<String, String> requestHeaders = fsapiRequestContext.getMsgInfo().getHeaders();
                        requestHeaders.remove(GatewayConstants.CONTENT_TYPE_TAG);
                        Map<String, String> addedHeaders = fsapiRequestContext.getAddedHeaders();
                        addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
                        fsapiRequestContext.setAddedHeaders(addedHeaders);
                        fsapiRequestContext.getMsgInfo().setHeaders(requestHeaders);
                    } catch (BadJOSEException | JOSEException | MalformedURLException e) {
                        log.error("Error occurred while validating the signature", e);
                        handleBadRequestError(fsapiRequestContext, GatewayConstants.INVALID_REQUEST,
                                "Invalid request signature. " + e.getMessage());
                    } catch (ParseException e) {
                        log.error("Error occurred while decoding the provided jwt", e);
                        handleBadRequestError(fsapiRequestContext, GatewayConstants.INVALID_REQUEST,
                                "Malformed request JWT");
                    }
                } else {
                    handleBadRequestError(fsapiRequestContext, GatewayConstants.INVALID_REQUEST,
                            "Malformed request found");
                }
            }
        }
    }

    @Generated(message = "Excluding since nothing implemented")
    @Override
    public void postProcessRequest(FSAPIRequestContext fsapiRequestContext) {

        if (fsapiRequestContext.isError()) {
            return;
        }
        String httpMethod = fsapiRequestContext.getMsgInfo().getHttpMethod();

        // For DCR retrieval, update and delete requests, check whether the token is bound to the correct client id
        if (HttpMethod.GET.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod) ||
                HttpMethod.DELETE.equals(httpMethod)) {

            String[] contextPathValues = fsapiRequestContext.getMsgInfo().getResource().split("/");
            String clientIdSentInRequest = "";
            List paramList = Arrays.asList(contextPathValues);
            int count = paramList.size();
            clientIdSentInRequest = paramList.stream().skip(count - 1).findFirst().get().toString();
            String clientIdBoundToToken = fsapiRequestContext.getApiRequestInfo().getConsumerKey();

            if (!clientIdSentInRequest.equals(clientIdBoundToToken)) {
                fsapiRequestContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP,
                        FinancialServicesErrorCodes.UNAUTHORIZED_CODE);
                Map<String, String> requestHeaders = fsapiRequestContext.getMsgInfo().getHeaders();
                requestHeaders.remove(GatewayConstants.CONTENT_TYPE_TAG);
                requestHeaders.remove(GatewayConstants.CONTENT_LENGTH);
                fsapiRequestContext.getMsgInfo().setHeaders(requestHeaders);
                log.error("Token is not bound to the client id sent in the request");
                handleUnAuthorizedError(fsapiRequestContext,
                        "Token is not bound to the client id sent in the request");
            }
        }
    }

    @Generated(message = "Excluding since nothing implemented")
    @Override
    public void preProcessResponse(FSAPIResponseContext fsapiResponseContext) {

    }

    @Override
    public void postProcessResponse(FSAPIResponseContext fsapiResponseContext) {

        if (fsapiResponseContext.isError()) {
            return;
        }

        if ((HttpMethod.POST.equals(fsapiResponseContext.getMsgInfo().getHttpMethod()) &&
                HttpStatus.SC_CREATED == fsapiResponseContext.getStatusCode()) ||
                (HttpMethod.PUT.equals(fsapiResponseContext.getMsgInfo().getHttpMethod()) &&
                        HttpStatus.SC_OK == fsapiResponseContext.getStatusCode())) {

            //Constructing OB response payload from IS DCR response
            String modifiedResponsePayload = GatewayUtils
                    .constructDCRResponseForCreate(fsapiResponseContext);
            fsapiResponseContext.setModifiedPayload(modifiedResponsePayload);
        }

        if (HttpMethod.GET.equals(fsapiResponseContext.getMsgInfo().getHttpMethod()) &&
                HttpStatus.SC_OK == fsapiResponseContext.getStatusCode()) {
            //Constructing OB response payload from IS DCR response
            String modifiedResponsePayload = GatewayUtils
                    .constructDCRResponseForRetrieval(fsapiResponseContext);
            fsapiResponseContext.setModifiedPayload(modifiedResponsePayload);
        }

    }

    /**
     * Method to handle bad request error.
     *
     * @param fsapiRequestContext  FSAPIRequestContext
     * @param message              Error message
     */
    private void handleBadRequestError(FSAPIRequestContext fsapiRequestContext, String errorCode , String message) {

        //catch errors and set to context
        FSExecutorError error = new FSExecutorError(errorCode,
                "dcr_error", message, "400");
        ArrayList<FSExecutorError> executorErrors = fsapiRequestContext.getErrors();
        executorErrors.add(error);
        fsapiRequestContext.setError(true);
        fsapiRequestContext.setErrors(executorErrors);
    }

    /**
     * Method to handle unauthorized error.
     *
     * @param fsapiRequestContext  FSAPIRequestContext
     * @param message              Error message
     */
    private void handleUnAuthorizedError(FSAPIRequestContext fsapiRequestContext, String message) {

        //catch errors and set to context
        FSExecutorError error = new FSExecutorError("unauthorized_request",
                "dcr_error", message, "401");
        ArrayList<FSExecutorError> executorErrors = fsapiRequestContext.getErrors();
        executorErrors.add(error);
        fsapiRequestContext.setError(true);
        fsapiRequestContext.setErrors(executorErrors);
    }
}
