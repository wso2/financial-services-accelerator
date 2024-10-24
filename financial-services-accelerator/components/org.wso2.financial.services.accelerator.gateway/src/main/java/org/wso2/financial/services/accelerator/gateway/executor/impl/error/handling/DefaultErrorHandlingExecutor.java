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

package org.wso2.financial.services.accelerator.gateway.executor.impl.error.handling;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.gateway.executor.core.FinancialServicesGatewayExecutor;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSExecutorError;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Default Executor to handle gateway errors.
 */
public class DefaultErrorHandlingExecutor implements FinancialServicesGatewayExecutor {

    private static final String ERRORS_TAG = "errors";

    /**
     * Method to handle pre request.
     *
     * @param fsapiRequestContext FS request context object
     */
    @Override
    public void preProcessRequest(FSAPIRequestContext fsapiRequestContext) {

        handleRequestError(fsapiRequestContext);

    }

    /**
     * Method to handle post request.
     *
     * @param fsapiRequestContext FS request context object
     */
    @Override
    public void postProcessRequest(FSAPIRequestContext fsapiRequestContext) {

        handleRequestError(fsapiRequestContext);
    }

    /**
     * Method to handle pre response.
     *
     * @param fsapiResponseContext FS response context object
     */
    @Override
    public void preProcessResponse(FSAPIResponseContext fsapiResponseContext) {

        handleResponseError(fsapiResponseContext);
    }

    /**
     * Method to handle post response.
     *
     * @param fsapiResponseContext FS response context object
     */
    @Override
    public void postProcessResponse(FSAPIResponseContext fsapiResponseContext) {

        handleResponseError(fsapiResponseContext);
    }

    private void handleRequestError(FSAPIRequestContext fsapiRequestContext) {

        if (!fsapiRequestContext.isError()) {
            return;
        }
        JSONObject payload = new JSONObject();
        ArrayList<FSExecutorError> errors = fsapiRequestContext.getErrors();
        JSONArray errorList = getErrorJSON(errors);
        HashSet<String> statusCodes = new HashSet<>();

        for (FSExecutorError error : errors) {
            statusCodes.add(error.getHttpStatusCode());
        }

        payload.put(ERRORS_TAG, errorList);
        if (errorList.length() != 0) {
            fsapiRequestContext.setModifiedPayload(payload.toString());
            Map<String, String> addedHeaders = fsapiRequestContext.getAddedHeaders();
            addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            fsapiRequestContext.setAddedHeaders(addedHeaders);
        }
        int statusCode;
        if (fsapiRequestContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) {
            statusCode = Integer.parseInt(fsapiRequestContext
                    .getContextProperty(GatewayConstants.ERROR_STATUS_PROP).toString());
        } else if (isAnyClientErrors(statusCodes)) {
            statusCode = HttpStatus.SC_BAD_REQUEST;
        } else {
            statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        fsapiRequestContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP,
                String.valueOf(statusCode));
    }

    private void handleResponseError(FSAPIResponseContext fsapiResponseContext) {

        if (!fsapiResponseContext.isError()) {
            return;
        }
        JSONObject payload = new JSONObject();
        ArrayList<FSExecutorError> errors = fsapiResponseContext.getErrors();
        JSONArray errorList = getErrorJSON(errors);
        HashSet<String> statusCodes = new HashSet<>();

        for (FSExecutorError error : errors) {
            statusCodes.add(error.getHttpStatusCode());
        }

        payload.put(ERRORS_TAG, errorList);
        fsapiResponseContext.setModifiedPayload(payload.toString());
        Map<String, String> addedHeaders = fsapiResponseContext.getAddedHeaders();
        addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
        fsapiResponseContext.setAddedHeaders(addedHeaders);
        int statusCode;
        if (fsapiResponseContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) {
            statusCode = Integer.parseInt(fsapiResponseContext
                    .getContextProperty(GatewayConstants.ERROR_STATUS_PROP).toString());
        } else if (isAnyClientErrors(statusCodes)) {
            statusCode = HttpStatus.SC_BAD_REQUEST;
        } else {
            statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        fsapiResponseContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP,
                String.valueOf(statusCode));
    }

    private JSONArray getErrorJSON(List<FSExecutorError> errors) {

        JSONArray errorList = new JSONArray();
        for (FSExecutorError error : errors) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("code", error.getCode());
            errorObj.put("message", error.getTitle());
            errorObj.put("description", error.getMessage());
            Map<String, String> links = error.getLinks();
            if (links != null && links.size() > 0) {
                JSONObject linksObj = new JSONObject();
                links.forEach(linksObj::put);
                errorObj.put("Links", linksObj);
            }
            errorList.put(errorObj);
        }
        return errorList;
    }

    private boolean isAnyClientErrors(HashSet<String> statusCodes) {

        for (String statusCode : statusCodes) {
            if (statusCode.startsWith("4")) {
                return true;
            }
        }
        return false;
    }
}
