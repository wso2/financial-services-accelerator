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

package org.wso2.financial.services.accelerator.gateway.executor.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ResponseContextDTO;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.ExtensionListener;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.gateway.cache.GatewayCacheKey;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Financial Services implementation for Extension listener.
 */
public class FSExtensionListenerImpl implements ExtensionListener {

    private static final Log log = LogFactory.getLog(FSExtensionListenerImpl.class);

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO preProcessRequest(RequestContextDTO requestContextDTO) {

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        for (FinancialServicesGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForRequest(fsapiRequestContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing preProcessRequest for executor: " + gatewayExecutor.getClass().getName());
            }
            gatewayExecutor.preProcessRequest(fsapiRequestContext);
        }

        if (!fsapiRequestContext.isError()) {
            setPropertiesToCache(requestContextDTO.getMsgInfo().getMessageId(), fsapiRequestContext.getContextProps());
        }
        return getResponseDTOForRequest(fsapiRequestContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO postProcessRequest(RequestContextDTO requestContextDTO) {

        Map<String, String> contextProps = getPropertiesFromCache(requestContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, contextProps);
        for (FinancialServicesGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForRequest(fsapiRequestContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing postProcessRequest for executor: " + gatewayExecutor.getClass().getName());
            }
            gatewayExecutor.postProcessRequest(fsapiRequestContext);
        }

        if (!fsapiRequestContext.isError()) {
            setPropertiesToCache(requestContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.CONTEXT_PROP_CACHE_KEY, fsapiRequestContext.getContextProps());
        }
        return getResponseDTOForRequest(fsapiRequestContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO preProcessResponse(ResponseContextDTO responseContextDTO) {

        Map<String, String> contextProps = getPropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        FSAPIResponseContext fsapiResponseContext = new FSAPIResponseContext(responseContextDTO, contextProps);
        for (FinancialServicesGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForResponse(fsapiResponseContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing preProcessResponse for executor: " + gatewayExecutor.getClass().getName());
            }
            gatewayExecutor.preProcessResponse(fsapiResponseContext);
        }

        if (!fsapiResponseContext.isError()) {
            setPropertiesToCache(responseContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.CONTEXT_PROP_CACHE_KEY, fsapiResponseContext.getContextProps());
        }
        return getResponseDTOForResponse(fsapiResponseContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO postProcessResponse(ResponseContextDTO responseContextDTO) {

        Map<String, String> contextProps = getPropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        FSAPIResponseContext fsapiResponseContext = new FSAPIResponseContext(responseContextDTO, contextProps);
        for (FinancialServicesGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForResponse(fsapiResponseContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing postProcessResponse for executor: " + gatewayExecutor.getClass().getName());
            }
            gatewayExecutor.postProcessResponse(fsapiResponseContext);
        }
        ExtensionResponseDTO responseDTOForResponse = getResponseDTOForResponse(fsapiResponseContext);
        removePropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        return responseDTOForResponse;
    }

    /**
     * Method to get response DTO for request path.
     *
     * @param fsapiRequestContext  API Request Context
     * @return ExtensionResponseDTO Extension Response DTO
     */
    protected ExtensionResponseDTO getResponseDTOForRequest(FSAPIRequestContext fsapiRequestContext) {

        ExtensionResponseDTO extensionResponseDTO = new ExtensionResponseDTO();
        if (fsapiRequestContext.isError()) {
            int statusCode = (!fsapiRequestContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) ?
                    HttpStatus.SC_INTERNAL_SERVER_ERROR :
                    (int) (fsapiRequestContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP));
            extensionResponseDTO.setStatusCode(statusCode);
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else if (fsapiRequestContext.getContextProps().containsKey(GatewayConstants.IS_RETURN_RESPONSE) &&
                Boolean.parseBoolean(fsapiRequestContext.getContextProps().get(GatewayConstants.IS_RETURN_RESPONSE))) {
            Map<String, String> headers = fsapiRequestContext.getMsgInfo().getHeaders();
            headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            fsapiRequestContext.getMsgInfo().setHeaders(headers);
            extensionResponseDTO.setHeaders(headers);
            if (fsapiRequestContext.getContextProps().containsKey(GatewayConstants.MODIFIED_STATUS)) {
                extensionResponseDTO.setStatusCode(Integer.parseInt(fsapiRequestContext.getContextProps()
                        .get(GatewayConstants.MODIFIED_STATUS)));
            }
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else {
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.CONTINUE.toString());
        }

        String modifiedPayload = fsapiRequestContext.getModifiedPayload();
        if (modifiedPayload != null) {
            extensionResponseDTO.setPayload(new ByteArrayInputStream(modifiedPayload.getBytes(StandardCharsets.UTF_8)));
        }

        setHeadersToResponse(extensionResponseDTO, fsapiRequestContext.getAddedHeaders(),
                fsapiRequestContext.getMsgInfo().getHeaders());
        return extensionResponseDTO;
    }

    /**
     * Method to get response DTO for response path.
     *
     * @param fsapiResponseContext  API Response Context
     * @return ExtensionResponseDTO Extension Response DTO
     */
    protected ExtensionResponseDTO getResponseDTOForResponse(FSAPIResponseContext fsapiResponseContext) {

        ExtensionResponseDTO extensionResponseDTO = new ExtensionResponseDTO();
        if (fsapiResponseContext.isError()) {
            int statusCode = (!fsapiResponseContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) ?
                    HttpStatus.SC_INTERNAL_SERVER_ERROR :
                    (int) (fsapiResponseContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP));
            extensionResponseDTO.setStatusCode(statusCode);
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else if (fsapiResponseContext.getContextProps().containsKey(GatewayConstants.IS_RETURN_RESPONSE) &&
                Boolean.parseBoolean(fsapiResponseContext.getContextProps().get(GatewayConstants.IS_RETURN_RESPONSE))) {
            Map<String, String> headers = fsapiResponseContext.getMsgInfo().getHeaders();
            headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            fsapiResponseContext.getMsgInfo().setHeaders(headers);
            extensionResponseDTO.setHeaders(headers);
            if (fsapiResponseContext.getContextProps().containsKey(GatewayConstants.MODIFIED_STATUS)) {
                extensionResponseDTO.setStatusCode((Integer.parseInt(fsapiResponseContext.getContextProps()
                        .get(GatewayConstants.MODIFIED_STATUS))));
            }
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else {
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.CONTINUE.toString());
        }

        String modifiedPayload = fsapiResponseContext.getModifiedPayload();
        if (modifiedPayload != null) {
            extensionResponseDTO.setPayload(new ByteArrayInputStream(modifiedPayload.getBytes(StandardCharsets.UTF_8)));
        }

        setHeadersToResponse(extensionResponseDTO, fsapiResponseContext.getAddedHeaders(),
                fsapiResponseContext.getMsgInfo().getHeaders());
        return extensionResponseDTO;
    }

    @Override
    public String getType() {

        return null;
    }

    /**
     * Method to store properties to cache
     *
     * @param key          unique cache key
     * @param contextProps properties to store
     */
    private void setPropertiesToCache(String key, Map<String, String> contextProps) {

        GatewayDataHolder.getGatewayCache().addToCache(GatewayCacheKey.of(key), contextProps);
    }

    /**
     * Method to retrieve context properties from cache.
     *
     * @param key unique cache key
     * @return context properties
     */
    private Map<String, String> getPropertiesFromCache(String key) {
        Object cachedObject = GatewayDataHolder.getGatewayCache().getFromCache(GatewayCacheKey.of(key));
        return cachedObject == null ? new HashMap<>() : (Map<String, String>) cachedObject;
    }

    /**
     * Method to remove context properties from cache.
     *
     * @param key unique cache key
     */
    private void removePropertiesFromCache(String key) {
        GatewayDataHolder.getGatewayCache().removeFromCache(GatewayCacheKey.of(key));
    }

    /**
     * Method to add headers to the response.
     *
     * @param extensionResponseDTO  Extension response DTO
     * @param addedHeaders          Added headers
     * @param requestHeaders        Request headers
     */
    private void setHeadersToResponse(ExtensionResponseDTO extensionResponseDTO, Map<String, String> addedHeaders,
                            Map<String, String> requestHeaders) {
        if (addedHeaders.size() != 0) {
            HashMap<String, String> headers = new HashMap<>();
            headers.putAll(requestHeaders);
            headers.putAll(addedHeaders);
            extensionResponseDTO.setHeaders(headers);
        }
    }
}
