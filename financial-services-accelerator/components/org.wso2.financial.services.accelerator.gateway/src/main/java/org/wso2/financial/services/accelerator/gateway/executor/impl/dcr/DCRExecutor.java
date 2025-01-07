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
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
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
            try {
                String httpMethod = fsapiRequestContext.getMsgInfo().getHttpMethod();
                if (HttpMethod.POST.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod)) {
                    if (payload != null) {
                        //decode request jwt
                        String decodedRequest = JWTUtils.decodeRequestJWT(payload, "body");

                        //Check whether decodedRequest is null
                        if (decodedRequest == null) {
                            handleBadRequestError(fsapiRequestContext,
                                    "Provided jwt is malformed and cannot be decoded");
                            return;
                        }

                        JSONObject decodedRequestObj = new JSONObject(decodedRequest);
                        JSONObject decodedSSA;
                        //Check whether the SSA exists and decode the SSA
                        if (decodedRequestObj.has(GatewayConstants.SOFTWARE_STATEMENT) &&
                                decodedRequestObj.getString(GatewayConstants.SOFTWARE_STATEMENT) != null) {
                            String ssa = JWTUtils.decodeRequestJWT(decodedRequestObj
                                    .getString(GatewayConstants.SOFTWARE_STATEMENT), "body");
                            decodedSSA = new JSONObject(ssa);
                        } else {
                            handleBadRequestError(fsapiRequestContext,
                                    "Required parameter software statement cannot be null");
                            return;
                        }
                        JWTClaimsSet requestClaims = GatewayUtils.validateRequestSignature(payload, decodedSSA);
                        String isDcrPayload = GatewayUtils.constructIsDcrPayload(requestClaims, decodedSSA);

                        fsapiRequestContext.setModifiedPayload(isDcrPayload);
                        Map<String, String> requestHeaders = fsapiRequestContext.getMsgInfo().getHeaders();
                        requestHeaders.remove(GatewayConstants.CONTENT_TYPE_TAG);
                        Map<String, String> addedHeaders = fsapiRequestContext.getAddedHeaders();
                        addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
                        fsapiRequestContext.setAddedHeaders(addedHeaders);
                        fsapiRequestContext.getMsgInfo().setHeaders(requestHeaders);
                    } else {
                        handleBadRequestError(fsapiRequestContext, "Malformed request found");
                    }
                }
            } catch (ParseException e) {
                log.error("Error occurred while decoding the provided jwt", e);
                handleBadRequestError(fsapiRequestContext, "Malformed request JWT");
            } catch (BadJOSEException e) {
                log.error("Error occurred while validating the signature", e);
                handleBadRequestError(fsapiRequestContext, "Invalid request signature. " + e.getMessage());
            } catch (JOSEException | MalformedURLException e) {
                log.error("Error occurred while validating the signature", e);
                handleBadRequestError(fsapiRequestContext, "Invalid request signature");
            }
        }
    }

    @Generated(message = "Excluding since nothing implemented")
    @Override
    public void postProcessRequest(FSAPIRequestContext fsapiRequestContext) {

    }

    @Generated(message = "Excluding since nothing implemented")
    @Override
    public void preProcessResponse(FSAPIResponseContext fsapiResponseContext) {

    }

    @Generated(message = "Excluding since nothing implemented")
    @Override
    public void postProcessResponse(FSAPIResponseContext fsapiResponseContext) {

        if (fsapiResponseContext.isError()) {
            return;
        }

    }

    private void handleBadRequestError(FSAPIRequestContext fsapiRequestContext, String message) {

        //catch errors and set to context
        FSExecutorError error = new FSExecutorError("Bad request",
                "invalid_client_metadata", message, "400");
        ArrayList<FSExecutorError> executorErrors = fsapiRequestContext.getErrors();
        executorErrors.add(error);
        fsapiRequestContext.setError(true);
        fsapiRequestContext.setErrors(executorErrors);
    }
}
