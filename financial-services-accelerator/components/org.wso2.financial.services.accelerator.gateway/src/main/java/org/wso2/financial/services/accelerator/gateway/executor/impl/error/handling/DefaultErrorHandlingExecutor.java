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
import java.util.Set;

/**
 * Default Executor to handle gateway errors.
 */
public class DefaultErrorHandlingExecutor implements FinancialServicesGatewayExecutor {

    private static final String ERRORS_TAG = "errors";

    /**
     * Method to handle pre request.
     *
     * @param fsApiRequestContext FS request context object
     */
    @Override
    public void preProcessRequest(FSAPIRequestContext fsApiRequestContext) {

        handleRequestError(fsApiRequestContext);

    }

    /**
     * Method to handle post request.
     *
     * @param fsApiRequestContext FS request context object
     */
    @Override
    public void postProcessRequest(FSAPIRequestContext fsApiRequestContext) {

        handleRequestError(fsApiRequestContext);
    }

    /**
     * Method to handle pre response.
     *
     * @param fsApiResponseContext FS response context object
     */
    @Override
    public void preProcessResponse(FSAPIResponseContext fsApiResponseContext) {

        handleResponseError(fsApiResponseContext);
    }

    /**
     * Method to handle post response.
     *
     * @param fsApiResponseContext FS response context object
     */
    @Override
    public void postProcessResponse(FSAPIResponseContext fsApiResponseContext) {

        handleResponseError(fsApiResponseContext);
    }

    private void handleRequestError(FSAPIRequestContext fsApiRequestContext) {

        if (!fsApiRequestContext.isError()) {
            return;
        }
        JSONObject payload = new JSONObject();
        ArrayList<FSExecutorError> errors = fsApiRequestContext.getErrors();
        JSONArray errorList = getErrorJSON(errors);
        HashSet<String> statusCodes = new HashSet<>();

        for (FSExecutorError error : errors) {
            statusCodes.add(error.getHttpStatusCode());
        }

        payload.put(ERRORS_TAG, errorList);
        if (!errorList.isEmpty()) {
            fsApiRequestContext.setModifiedPayload(payload.toString());
            Map<String, String> addedHeaders = fsApiRequestContext.getAddedHeaders();
            addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            fsApiRequestContext.setAddedHeaders(addedHeaders);
        }
        int statusCode;
        if (fsApiRequestContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) {
            statusCode = Integer.parseInt(fsApiRequestContext
                    .getContextProperty(GatewayConstants.ERROR_STATUS_PROP).toString());
        } else if (isAnyClientErrors(statusCodes)) {
            statusCode = HttpStatus.SC_BAD_REQUEST;
        } else {
            statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        fsApiRequestContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP,
                String.valueOf(statusCode));
    }

    private void handleResponseError(FSAPIResponseContext fsApiResponseContext) {

        if (!fsApiResponseContext.isError()) {
            return;
        }
        JSONObject payload = new JSONObject();
        ArrayList<FSExecutorError> errors = fsApiResponseContext.getErrors();
        JSONArray errorList = getErrorJSON(errors);
        HashSet<String> statusCodes = new HashSet<>();

        for (FSExecutorError error : errors) {
            statusCodes.add(error.getHttpStatusCode());
        }

        payload.put(ERRORS_TAG, errorList);
        fsApiResponseContext.setModifiedPayload(payload.toString());
        Map<String, String> addedHeaders = fsApiResponseContext.getAddedHeaders();
        addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
        fsApiResponseContext.setAddedHeaders(addedHeaders);
        int statusCode;
        if (fsApiResponseContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) {
            statusCode = Integer.parseInt(fsApiResponseContext
                    .getContextProperty(GatewayConstants.ERROR_STATUS_PROP).toString());
        } else if (isAnyClientErrors(statusCodes)) {
            statusCode = HttpStatus.SC_BAD_REQUEST;
        } else {
            statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        fsApiResponseContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP,
                String.valueOf(statusCode));
    }

    private JSONArray getErrorJSON(List<FSExecutorError> errors) {

        JSONArray errorList = new JSONArray();
        for (FSExecutorError error : errors) {
            JSONObject errorObj = new JSONObject();
            errorObj.put(GatewayConstants.CODE, error.getCode());
            errorObj.put(GatewayConstants.MESSAGE, error.getTitle());
            errorObj.put(GatewayConstants.DESCRIPTION, error.getMessage());
            Map<String, String> links = error.getLinks();
            if (links != null && !links.isEmpty()) {
                JSONObject linksObj = new JSONObject();
                links.forEach(linksObj::put);
                errorObj.put(GatewayConstants.LINKS, linksObj);
            }
            errorList.put(errorObj);
        }
        return errorList;
    }

    private boolean isAnyClientErrors(Set<String> statusCodes) {

        return statusCodes.stream().anyMatch(statusCode -> statusCode.startsWith("4"));
    }
}
