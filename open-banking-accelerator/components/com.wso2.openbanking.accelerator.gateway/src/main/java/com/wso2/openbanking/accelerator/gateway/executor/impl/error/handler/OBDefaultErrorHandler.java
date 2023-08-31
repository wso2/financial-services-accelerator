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

package com.wso2.openbanking.accelerator.gateway.executor.impl.error.handler;

import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Default Executor to handle gateway errors.
 */
public class OBDefaultErrorHandler implements OpenBankingGatewayExecutor {

    private static final String ERRORS_TAG = "errors";
    private static final String STATUS_CODE = "statusCode";
    private static final String RESPONSE_PAYLOAD_SIZE = "responsePayloadSize";

    /**
     * Method to handle pre request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        handleRequestError(obapiRequestContext);

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        handleRequestError(obapiRequestContext);
    }

    /**
     * Method to handle pre response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

        handleResponseError(obapiResponseContext);
    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

        handleResponseError(obapiResponseContext);
    }

    private void handleRequestError(OBAPIRequestContext obapiRequestContext) {

        if (!obapiRequestContext.isError()) {
            return;
        }
        JSONObject payload = new JSONObject();
        ArrayList<OpenBankingExecutorError> errors = obapiRequestContext.getErrors();
        JSONArray errorList = getErrorJSON(errors);
        HashSet<String> statusCodes = new HashSet<>();

        for (OpenBankingExecutorError error : errors) {
            statusCodes.add(error.getHttpStatusCode());
        }

        payload.put(ERRORS_TAG, errorList);
        if (errorList.length() != 0) {
            obapiRequestContext.setModifiedPayload(payload.toString());
            Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
            addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            obapiRequestContext.setAddedHeaders(addedHeaders);
        }
        int statusCode;
        if (obapiRequestContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) {
            statusCode = Integer.parseInt(obapiRequestContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP));
        } else if (isAnyClientErrors(statusCodes)) {
            statusCode = HttpStatus.SC_BAD_REQUEST;
        } else {
            statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        obapiRequestContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP,
                String.valueOf(statusCode));

        // Add error data to analytics map
        Map<String, Object> analyticsData = obapiRequestContext.getAnalyticsData();
        analyticsData.put(STATUS_CODE, statusCode);
        analyticsData.put(RESPONSE_PAYLOAD_SIZE, (long) payload.toString().length());
        obapiRequestContext.setAnalyticsData(analyticsData);
    }

    private void handleResponseError(OBAPIResponseContext obapiResponseContext) {

        if (!obapiResponseContext.isError()) {
            return;
        }
        JSONObject payload = new JSONObject();
        ArrayList<OpenBankingExecutorError> errors = obapiResponseContext.getErrors();
        JSONArray errorList = getErrorJSON(errors);
        HashSet<String> statusCodes = new HashSet<>();

        for (OpenBankingExecutorError error : errors) {
            statusCodes.add(error.getHttpStatusCode());
        }

        payload.put(ERRORS_TAG, errorList);
        obapiResponseContext.setModifiedPayload(payload.toString());
        Map<String, String> addedHeaders = obapiResponseContext.getAddedHeaders();
        addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
        obapiResponseContext.setAddedHeaders(addedHeaders);
        int statusCode;
        if (obapiResponseContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) {
            statusCode = Integer.parseInt(obapiResponseContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP));
        } else if (isAnyClientErrors(statusCodes)) {
            statusCode = HttpStatus.SC_BAD_REQUEST;
        } else {
            statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        obapiResponseContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP,
                String.valueOf(statusCode));

        // Add error data to analytics map
        Map<String, Object> analyticsData = obapiResponseContext.getAnalyticsData();
        analyticsData.put(STATUS_CODE, statusCode);
        analyticsData.put(RESPONSE_PAYLOAD_SIZE, (long) payload.toString().length());
        obapiResponseContext.setAnalyticsData(analyticsData);
    }

    private JSONArray getErrorJSON(List<OpenBankingExecutorError> errors) {

        JSONArray errorList = new JSONArray();
        for (OpenBankingExecutorError error : errors) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("Code", error.getCode());
            errorObj.put("Title", error.getTitle());
            errorObj.put("Message", error.getMessage());
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
