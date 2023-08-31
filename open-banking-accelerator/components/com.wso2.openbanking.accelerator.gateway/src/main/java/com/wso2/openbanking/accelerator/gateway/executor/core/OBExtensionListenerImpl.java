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

package com.wso2.openbanking.accelerator.gateway.executor.core;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ResponseContextDTO;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.ExtensionListener;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Open Banking implementation for Extension listener.
 */
public class OBExtensionListenerImpl implements ExtensionListener {

    private static final Log log = LogFactory.getLog(OBExtensionListenerImpl.class);

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO preProcessRequest(RequestContextDTO requestContextDTO) {

        OBAPIRequestContext obapiRequestContext = new OBAPIRequestContext(requestContextDTO, new HashMap<>(),
                new HashMap<>());
        for (OpenBankingGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForRequest(obapiRequestContext)) {
            gatewayExecutor.preProcessRequest(obapiRequestContext);
        }

        if (!obapiRequestContext.isError()) {
            setPropertiesToCache(requestContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.CONTEXT_PROP_CACHE_KEY, obapiRequestContext.getContextProps());

            setPropertiesToCache(requestContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.ANALYTICS_PROP_CACHE_KEY, obapiRequestContext.getAnalyticsData());
        } else {
            publishAnalyticsData(obapiRequestContext.getAnalyticsData());
        }
        return getResponseDTOForRequest(obapiRequestContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO postProcessRequest(RequestContextDTO requestContextDTO) {

        Map<String, String> contextProps = getPropertiesFromCache(requestContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        Map<String, Object> analyticsData = getPropertiesFromCache(requestContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.ANALYTICS_PROP_CACHE_KEY);

        OBAPIRequestContext obapiRequestContext =
                new OBAPIRequestContext(requestContextDTO, contextProps, analyticsData);
        for (OpenBankingGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForRequest(obapiRequestContext)) {
            gatewayExecutor.postProcessRequest(obapiRequestContext);
        }

        if (!obapiRequestContext.isError()) {
            setPropertiesToCache(requestContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.CONTEXT_PROP_CACHE_KEY, obapiRequestContext.getContextProps());

            setPropertiesToCache(requestContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.ANALYTICS_PROP_CACHE_KEY, obapiRequestContext.getAnalyticsData());
        } else {
            publishAnalyticsData(obapiRequestContext.getAnalyticsData());
        }
        return getResponseDTOForRequest(obapiRequestContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO preProcessResponse(ResponseContextDTO responseContextDTO) {

        Map<String, String> contextProps = getPropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        Map<String, Object> analyticsData = getPropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.ANALYTICS_PROP_CACHE_KEY);
        OBAPIResponseContext obapiResponseContext =
                new OBAPIResponseContext(responseContextDTO, contextProps, analyticsData);
        for (OpenBankingGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForResponse(obapiResponseContext)) {
            gatewayExecutor.preProcessResponse(obapiResponseContext);
        }

        if (!obapiResponseContext.isError()) {
            setPropertiesToCache(responseContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.CONTEXT_PROP_CACHE_KEY, obapiResponseContext.getContextProps());

            setPropertiesToCache(responseContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.ANALYTICS_PROP_CACHE_KEY, obapiResponseContext.getAnalyticsData());
        } else {
            publishAnalyticsData(obapiResponseContext.getAnalyticsData());
        }
        return getResponseDTOForResponse(obapiResponseContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO postProcessResponse(ResponseContextDTO responseContextDTO) {

        Map<String, String> contextProps = getPropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        Map<String, Object> analyticsData = getPropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.ANALYTICS_PROP_CACHE_KEY);
        OBAPIResponseContext obapiResponseContext =
                new OBAPIResponseContext(responseContextDTO, contextProps, analyticsData);
        for (OpenBankingGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForResponse(obapiResponseContext)) {
            gatewayExecutor.postProcessResponse(obapiResponseContext);
        }
        publishAnalyticsData(obapiResponseContext.getAnalyticsData());
        ExtensionResponseDTO responseDTOForResponse = getResponseDTOForResponse(obapiResponseContext);
        removePropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        removePropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.ANALYTICS_PROP_CACHE_KEY);
        return responseDTOForResponse;
    }

    protected ExtensionResponseDTO getResponseDTOForRequest(OBAPIRequestContext obapiRequestContext) {

        ExtensionResponseDTO extensionResponseDTO = new ExtensionResponseDTO();
        if (obapiRequestContext.isError()) {

            int statusCode = (!obapiRequestContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) ?
                    HttpStatus.SC_INTERNAL_SERVER_ERROR :
                    Integer.parseInt(obapiRequestContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP));
            extensionResponseDTO.setStatusCode(statusCode);
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else if (obapiRequestContext.getContextProps().containsKey(GatewayConstants.IS_RETURN_RESPONSE) &&
                Boolean.parseBoolean(obapiRequestContext.getContextProps().get(GatewayConstants.IS_RETURN_RESPONSE))) {
            Map<String, String> headers = obapiRequestContext.getMsgInfo().getHeaders();
            headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            obapiRequestContext.getMsgInfo().setHeaders(headers);
            extensionResponseDTO.setHeaders(headers);
            if (obapiRequestContext.getContextProps().containsKey(GatewayConstants.MODIFIED_STATUS)) {
                extensionResponseDTO.setStatusCode(Integer.parseInt(obapiRequestContext.getContextProps()
                        .get(GatewayConstants.MODIFIED_STATUS)));
            }
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else {
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.CONTINUE.toString());
        }

        String modifiedPayload = obapiRequestContext.getModifiedPayload();
        if (modifiedPayload != null) {
            extensionResponseDTO.setPayload(new ByteArrayInputStream(modifiedPayload.getBytes(StandardCharsets.UTF_8)));
        }
        Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
        if (addedHeaders.size() != 0) {
            TreeMap<String, String> headers = new TreeMap<>();
            headers.putAll(obapiRequestContext.getMsgInfo().getHeaders());
            for (Map.Entry<String, String> headerEntry : addedHeaders.entrySet()) {
                headers.put(headerEntry.getKey(), headerEntry.getValue());
            }
            extensionResponseDTO.setHeaders(headers);
        }
        return extensionResponseDTO;
    }

    protected ExtensionResponseDTO getResponseDTOForResponse(OBAPIResponseContext obapiResponseContext) {

        ExtensionResponseDTO extensionResponseDTO = new ExtensionResponseDTO();
        if (obapiResponseContext.isError()) {
            int statusCode = (!obapiResponseContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) ?
                    HttpStatus.SC_INTERNAL_SERVER_ERROR :
                    Integer.parseInt(obapiResponseContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP));
            extensionResponseDTO.setStatusCode(statusCode);
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else if (obapiResponseContext.getContextProps().containsKey(GatewayConstants.IS_RETURN_RESPONSE) &&
                Boolean.parseBoolean(obapiResponseContext.getContextProps().get(GatewayConstants.IS_RETURN_RESPONSE))) {
            Map<String, String> headers = obapiResponseContext.getMsgInfo().getHeaders();
            headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            obapiResponseContext.getMsgInfo().setHeaders(headers);
            extensionResponseDTO.setHeaders(headers);
            if (obapiResponseContext.getContextProps().containsKey(GatewayConstants.MODIFIED_STATUS)) {
                extensionResponseDTO.setStatusCode(Integer.parseInt(obapiResponseContext.getContextProps()
                        .get(GatewayConstants.MODIFIED_STATUS)));
            }
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else {
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.CONTINUE.toString());
        }

        String modifiedPayload = obapiResponseContext.getModifiedPayload();
        if (modifiedPayload != null) {
            extensionResponseDTO.setPayload(new ByteArrayInputStream(modifiedPayload.getBytes(StandardCharsets.UTF_8)));
        }
        Map<String, String> addedHeaders = obapiResponseContext.getAddedHeaders();
        if (addedHeaders.size() != 0) {
            HashMap<String, String> headers = new HashMap<>();
            headers.putAll(obapiResponseContext.getMsgInfo().getHeaders());
            for (Map.Entry<String, String> headerEntry : addedHeaders.entrySet()) {
                headers.put(headerEntry.getKey(), headerEntry.getValue());
            }
            extensionResponseDTO.setHeaders(headers);
        }
        return extensionResponseDTO;
    }

    @Override
    public String getType() {

        return null;
    }

    /**
     * Method to store properties to cache.
     *
     * @param key          unique cache key
     * @param contextProps properties to store
     */
    private void setPropertiesToCache(String key, Map contextProps) {

        GatewayDataHolder.getGatewayCache().addToCache(GatewayCacheKey.of(key), contextProps);
    }

    /**
     * Method to retrieve context properties from cache.
     *
     * @param key unique cache key
     * @return context properties
     */
    private Map getPropertiesFromCache(String key) {
        //Need to implement after adding base cache implementation to the common module.
        Object cachedObject = GatewayDataHolder.getGatewayCache().getFromCache(GatewayCacheKey.of(key));
        return cachedObject == null ? new HashMap<>() : (Map) cachedObject;
    }

    /**
     * Method to remove context properties from cache.
     *
     * @param key unique cache key
     * @return context properties
     */
    private void removePropertiesFromCache(String key) {
        //Need to implement after adding base cache implementation to the common module.
        GatewayDataHolder.getGatewayCache().removeFromCache(GatewayCacheKey.of(key));
    }

    private void publishAnalyticsData(Map<String, Object> analyticsData) {

        if (analyticsData != null && !analyticsData.isEmpty()) {
            OBDataPublisherUtil.
                    publishData(GatewayConstants.API_DATA_STREAM, GatewayConstants.API_DATA_VERSION, analyticsData);
        }
    }

}
