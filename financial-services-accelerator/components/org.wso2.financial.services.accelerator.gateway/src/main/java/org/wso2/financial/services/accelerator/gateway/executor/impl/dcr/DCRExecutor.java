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
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesErrorCodes;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.gateway.executor.core.FinancialServicesGatewayExecutor;
import org.wso2.financial.services.accelerator.gateway.executor.exception.FSExecutorException;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSExecutorError;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;

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
                            throw new FSExecutorException("invalid_client_metadata",
                                    FinancialServicesErrorCodes.BAD_REQUEST_CODE,
                                    "Provided jwt is malformed and cannot be decoded");
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
                            //Throwing an exception whn SSA is not found
                            throw new FSExecutorException("invalid_client_metadata",
                                    FinancialServicesErrorCodes.BAD_REQUEST_CODE,
                                    "Required parameter software statement cannot be null");
                        }
                        JWTClaimsSet requestClaims = validateRequestSignature(payload, decodedSSA);
                        String isDcrPayload = constructIsDcrPayload(requestClaims, decodedSSA);

                        fsapiRequestContext.setModifiedPayload(isDcrPayload);
                        Map<String, String> requestHeaders = fsapiRequestContext.getMsgInfo().getHeaders();
                        requestHeaders.remove("Content-Type");
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
            } catch (FSExecutorException e) {
                log.error("Error occurred while validating the signature", e);
                handleBadRequestError(fsapiRequestContext, e.getErrorPayload());
            }
        }
    }

    @Override
    public void postProcessRequest(FSAPIRequestContext fsapiRequestContext) {

    }

    @Override
    public void preProcessResponse(FSAPIResponseContext fsapiResponseContext) {

    }

    @Override
    public void postProcessResponse(FSAPIResponseContext fsapiResponseContext) {

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

    @Generated(message = "Excluding from unit tests since there is an external http call")
    private JWTClaimsSet validateRequestSignature(String payload, JSONObject decodedSSA)
            throws ParseException, JOSEException, BadJOSEException, MalformedURLException,
            FSExecutorException {

        String jwksEndpointName = configs.get(FinancialServicesConstants.JWKS_ENDPOINT_NAME).toString();
        //validate request signature
        String jwksEndpoint = decodedSSA.getString(jwksEndpointName);
        SignedJWT signedJWT = SignedJWT.parse(payload);
        String alg = signedJWT.getHeader().getAlgorithm().getName();
        return JWTUtils.validateJWTSignature(payload, jwksEndpoint, alg);
    }

    /**
     * Convert the given JWT claims set to a JSON string.
     *
     * @param jwtClaimsSet The JWT claims set.
     *
     * @return The JSON string.
     */
    @SuppressWarnings("unchecked")
    public static String constructIsDcrPayload(JWTClaimsSet jwtClaimsSet, JSONObject decodedSSA) {

        JSONObject jsonObject = new JSONObject(jwtClaimsSet.getClaims());

        // Convert the iat and exp claims into seconds
        if (jwtClaimsSet.getIssueTime() != null) {
            jsonObject.put(GatewayConstants.IAT, jwtClaimsSet.getIssueTime().getTime() / 1000);
        }
        if (jwtClaimsSet.getExpirationTime() != null) {
            jsonObject.put(GatewayConstants.EXP, jwtClaimsSet.getExpirationTime().getTime() / 1000);
        }

        jsonObject.put(GatewayConstants.CLIENT_NAME, getApplicationName(jwtClaimsSet, decodedSSA));
        jsonObject.put(GatewayConstants.JWKS_URI, decodedSSA.getString(configs
                .get(FinancialServicesConstants.JWKS_ENDPOINT_NAME).toString()));
        jsonObject.put(GatewayConstants.TOKEN_TYPE, GatewayConstants.JWT);
        jsonObject.put(GatewayConstants.REQUIRE_SIGNED_OBJ, true);
        jsonObject.put(GatewayConstants.TLS_CLIENT_CERT_ACCESS_TOKENS, true);

        return jsonObject.toString();
    }

    /**
     * Retrieves the application name from the registration request.
     *
     * @param request     registration or update request
     * @param decodedSSA  Decoded SSA
     * @return The application name
     */
    public static String getApplicationName(JWTClaimsSet request, JSONObject decodedSSA) {
        boolean useSoftwareIdAsAppName = Boolean.parseBoolean(configs
                .get(FinancialServicesConstants.DCR_USE_SOFTWAREID_AS_APPNAME).toString());
        if (useSoftwareIdAsAppName) {
            // If the request does not contain a software statement, get the software Id directly from the request
            if (StringUtils.isEmpty(request.getClaims().get(GatewayConstants.SOFTWARE_STATEMENT).toString())) {
                return request.getClaims().get(GatewayConstants.SOFTWARE_STATEMENT).toString();
            }
            return decodedSSA.getString(GatewayConstants.SOFTWARE_ID);
        }
        return getSafeApplicationName(decodedSSA
                .getString(configs.get(FinancialServicesConstants.SSA_CLIENT_NAME).toString()));
    }

    public static String getSafeApplicationName(String applicationName) {

        if (StringUtils.isEmpty(applicationName)) {
            throw new IllegalArgumentException("Application name should be a valid string");
        }

        String sanitizedInput = applicationName.trim().replaceAll(GatewayConstants.DISALLOWED_CHARS_PATTERN,
                GatewayConstants.SUBSTITUTE_STRING);
        return StringUtils.abbreviate(sanitizedInput, GatewayConstants.ABBREVIATED_STRING_LENGTH);

    }
}
