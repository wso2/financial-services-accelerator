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

package org.wso2.financial.services.accelerator.gateway.executor.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.json.XML;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ResponseContextDTO;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesErrorCodes;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;
import org.wso2.financial.services.accelerator.gateway.util.GatewayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Open Banking executor response context.
 */
public class FSAPIResponseContext extends ResponseContextDTO {

    private static final Log log = LogFactory.getLog(FSAPIResponseContext.class);
    private ResponseContextDTO responseContextDTO;
    private Map<String, String> contextProps;
    private String responsePayload;
    private String modifiedPayload;
    private Map<String, String> addedHeaders;
    private boolean isError;
    private ArrayList<FSExecutorError> errors;
    private Map<String, Object> analyticsData;

    public FSAPIResponseContext(ResponseContextDTO responseContextDTO, Map<String, String> contextProps,
                                Map<String, Object> analyticsData) {

        this.responseContextDTO = responseContextDTO;
        this.contextProps = contextProps;
        this.errors = new ArrayList<>();
        this.analyticsData = analyticsData;
        this.addedHeaders = new HashMap<>();
        if (responseContextDTO.getMsgInfo().getHeaders().get(GatewayConstants.CONTENT_TYPE_TAG) != null) {
            String contentType = responseContextDTO.getMsgInfo().getHeaders().get(GatewayConstants.CONTENT_TYPE_TAG);
            String httpMethod = responseContextDTO.getMsgInfo().getHttpMethod();
            String errorMessage = "Request Content-Type header does not match any allowed types";
            if (contentType.startsWith(GatewayConstants.JWT_CONTENT_TYPE)) {
                try {
                    this.responsePayload = GatewayUtils.getTextPayload(responseContextDTO.getMsgInfo()
                            .getPayloadHandler().consumeAsString());
                } catch (Exception e) {
                    log.error(String.format("Failed to read the text payload from response. %s",
                            e.getMessage().replaceAll("\n\r", "")));
                    handleContentTypeErrors(FinancialServicesErrorCodes.INVALID_CONTENT_TYPE, errorMessage);
                }
            } else if (GatewayUtils.isEligibleResponse(contentType, httpMethod) &&
                    HttpStatus.SC_NO_CONTENT != responseContextDTO.getStatusCode()) {
                try {
                    this.responsePayload = responseContextDTO.getMsgInfo().getPayloadHandler().consumeAsString();
                    if (contentType.contains(GatewayConstants.JSON_CONTENT_TYPE) &&
                            this.responsePayload.contains("soapenv:Body")) {
                        JSONObject soapPayload = XML.toJSONObject(responseContextDTO.getMsgInfo().getPayloadHandler()
                                .consumeAsString()).getJSONObject("soapenv:Body");
                        if (soapPayload.has("jsonObject")) {
                            this.responsePayload = soapPayload.getJSONObject("jsonObject").toString();
                        } else {
                            this.responsePayload = null;
                        }
                    }
                } catch (Exception e) {
                    log.error(String.format("Failed to read the payload from response. %s",
                            e.getMessage().replaceAll("\n\r", "")));
                    handleContentTypeErrors(FinancialServicesErrorCodes.INVALID_CONTENT_TYPE, errorMessage);
                }
            } else {
                this.responsePayload = null;
            }
        }
    }

    public String getModifiedPayload() {

        return modifiedPayload;
    }

    public void setModifiedPayload(String modifiedPayload) {

        this.modifiedPayload = modifiedPayload;
    }

    public Map<String, String> getAddedHeaders() {

        return addedHeaders;
    }

    public void setAddedHeaders(Map<String, String> addedHeaders) {

        this.addedHeaders = addedHeaders;
    }

    public Map<String, String> getContextProps() {

        return contextProps;
    }

    public void setContextProps(Map<String, String> contextProps) {

        this.contextProps = contextProps;
    }

    public boolean isError() {

        return isError;
    }

    public void setError(boolean error) {

        isError = error;
    }

    public ArrayList<FSExecutorError> getErrors() {

        return errors;
    }

    public void setErrors(
            ArrayList<FSExecutorError> errors) {

        this.errors = errors;
    }

    public Map<String, Object> getAnalyticsData() {

        return analyticsData;
    }

    public void setAnalyticsData(Map<String, Object> analyticsData) {

        this.analyticsData = analyticsData;
    }

    @Override
    public APIRequestInfoDTO getApiRequestInfo() {

        return this.responseContextDTO.getApiRequestInfo();
    }

    @Override
    public int getStatusCode() {

        return responseContextDTO.getStatusCode();
    }

    @Override
    public MsgInfoDTO getMsgInfo() {

        return responseContextDTO.getMsgInfo();
    }

    public String getResponsePayload() {

        return responsePayload;
    }

    public void addContextProperty(String key, String value) {

        this.contextProps.put(key, value);
    }

    public String getContextProperty(String key) {

        return this.contextProps.get(key);
    }

    private void handleContentTypeErrors(String errorCode, String errorMessage) {
        FSExecutorError error = new FSExecutorError(errorCode, errorMessage, errorMessage,
                FinancialServicesErrorCodes.UNSUPPORTED_MEDIA_TYPE_CODE);

        this.isError = true;
        this.errors.add(error);
    }

}
