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

package com.wso2.openbanking.accelerator.gateway.executor.impl.common.reporting.data.executor;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Instant;
import java.util.Map;

/**
 * Common Reporting Data Executor.
 */
public class CommonReportingDataExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(CommonReportingDataExecutor.class);

    private static final String CLIENT_USER_AGENT = "User-Agent";
    private static final String USER_AGENT = "userAgent";
    private static final String TIMESTAMP = "timestamp";
    private static final String ELECTED_RESOURCE = "electedResource";
    private static final String RESPONSE_PAYLOAD_SIZE = "responsePayloadSize";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String STATUS_CODE = "statusCode";
    private static final String CONSENT_ID = "consentId";
    private static final String CONSUMER_ID = "consumerId";
    private static final String API_NAME = "apiName";
    private static final String API_SPEC_VERSION = "apiSpecVersion";
    private static final String CLIENT_ID = "clientId";
    private static final String MESSAGE_ID = "messageId";
    private static final String NAME_TAG = "_name";

    /**
     * Method to handle pre request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        Map<String, Object> analyticsData = obapiRequestContext.getAnalyticsData();

        String httpMethod = obapiRequestContext.getMsgInfo().getHttpMethod();
        analyticsData.put(HTTP_METHOD, httpMethod);

        Map<String, String> headers = obapiRequestContext.getMsgInfo().getHeaders();

        String userAgent = headers.get(CLIENT_USER_AGENT);
        analyticsData.put(USER_AGENT, userAgent);

        String electedResource = obapiRequestContext.getMsgInfo().getElectedResource();
        analyticsData.put(ELECTED_RESOURCE, electedResource);

        String apiName = getApiName(obapiRequestContext);

        analyticsData.put(API_NAME, apiName);

        String apiSpecVersion = obapiRequestContext.getApiRequestInfo().getVersion();
        analyticsData.put(API_SPEC_VERSION, apiSpecVersion);

        analyticsData.put(MESSAGE_ID, obapiRequestContext.getMsgInfo().getMessageId());
        analyticsData.put(TIMESTAMP, Instant.now().getEpochSecond());

        // Add analytics data to a map
        obapiRequestContext.setAnalyticsData(analyticsData);
    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        Map<String, Object> analyticsData = obapiRequestContext.getAnalyticsData();
        String consentId = obapiRequestContext.getConsentId();
        analyticsData.put(CONSENT_ID, consentId);
        analyticsData.put(CLIENT_ID, obapiRequestContext.getApiRequestInfo().getConsumerKey());
        analyticsData.put(CONSUMER_ID, obapiRequestContext.getApiRequestInfo().getUsername());

        // Add analytics data to a map
        obapiRequestContext.setAnalyticsData(analyticsData);
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

        Map<String, Object> analyticsData = obapiResponseContext.getAnalyticsData();

        analyticsData.put(STATUS_CODE, obapiResponseContext.getStatusCode());

        String payload = obapiResponseContext.getModifiedPayload() != null ?
                obapiResponseContext.getModifiedPayload() : obapiResponseContext.getResponsePayload();
        long responsePayloadSize = payload != null ? payload.length() : 0;
        analyticsData.put(RESPONSE_PAYLOAD_SIZE, responsePayloadSize);

        // Add data to analytics data map
        obapiResponseContext.setAnalyticsData(analyticsData);

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
     * Method to get api name from cache.
     * @param obapiRequestContext ob api request context
     * @return api name
     */
    @Generated(message = "Ignoring tests since this method is used to get name from cache")
    protected String getApiName(OBAPIRequestContext obapiRequestContext) {

        String apiName;
        String apiNameCacheKey = obapiRequestContext.getApiRequestInfo().getApiId() + NAME_TAG;
        Object cacheObject = GatewayDataHolder.getGatewayCache().getFromCache(GatewayCacheKey.of(apiNameCacheKey));

        if (cacheObject == null) {
            apiName = obapiRequestContext.getOpenAPI().getInfo().getTitle();
            GatewayDataHolder.getGatewayCache().addToCache(GatewayCacheKey.of(apiNameCacheKey), apiName);
        } else {
            apiName = (String) cacheObject;
        }
        return apiName;
    }
}
