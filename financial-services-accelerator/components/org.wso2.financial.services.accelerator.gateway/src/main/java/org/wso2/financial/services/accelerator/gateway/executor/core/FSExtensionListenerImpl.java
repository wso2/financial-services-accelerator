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

        FSAPIRequestContext fsApiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        for (FinancialServicesGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForRequest(fsApiRequestContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing preProcessRequest for executor: " + gatewayExecutor.getClass().getName());
            }
            gatewayExecutor.preProcessRequest(fsApiRequestContext);
        }

        if (!fsApiRequestContext.isError()) {
            setPropertiesToCache(requestContextDTO.getMsgInfo().getMessageId(), fsApiRequestContext.getContextProps());
        }
        return getResponseDTOForRequest(fsApiRequestContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO postProcessRequest(RequestContextDTO requestContextDTO) {

        Map<String, Object> contextProps = getPropertiesFromCache(requestContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);

        FSAPIRequestContext fsApiRequestContext = new FSAPIRequestContext(requestContextDTO, contextProps);
        for (FinancialServicesGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForRequest(fsApiRequestContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing postProcessRequest for executor: " + gatewayExecutor.getClass().getName());
            }
            gatewayExecutor.postProcessRequest(fsApiRequestContext);
        }

        if (!fsApiRequestContext.isError()) {
            setPropertiesToCache(requestContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.CONTEXT_PROP_CACHE_KEY, fsApiRequestContext.getContextProps());
        }
        return getResponseDTOForRequest(fsApiRequestContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO preProcessResponse(ResponseContextDTO responseContextDTO) {

        Map<String, Object> contextProps = getPropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        FSAPIResponseContext fsApiResponseContext = new FSAPIResponseContext(responseContextDTO, contextProps);
        for (FinancialServicesGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForResponse(fsApiResponseContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing preProcessResponse for executor: " + gatewayExecutor.getClass().getName());
            }
            gatewayExecutor.preProcessResponse(fsApiResponseContext);
        }

        if (!fsApiResponseContext.isError()) {
            setPropertiesToCache(responseContextDTO.getMsgInfo().getMessageId() +
                    GatewayConstants.CONTEXT_PROP_CACHE_KEY, fsApiResponseContext.getContextProps());
        }
        return getResponseDTOForResponse(fsApiResponseContext);
    }

    @Override
    @Generated(message = "Ignoring since the method has covered in other tests")
    public ExtensionResponseDTO postProcessResponse(ResponseContextDTO responseContextDTO) {

        Map<String, Object> contextProps = getPropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        FSAPIResponseContext fsApiResponseContext = new FSAPIResponseContext(responseContextDTO, contextProps);
        for (FinancialServicesGatewayExecutor gatewayExecutor :
                GatewayDataHolder.getInstance().getRequestRouter().getExecutorsForResponse(fsApiResponseContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Executing postProcessResponse for executor: " + gatewayExecutor.getClass().getName());
            }
            gatewayExecutor.postProcessResponse(fsApiResponseContext);
        }
        ExtensionResponseDTO responseDTOForResponse = getResponseDTOForResponse(fsApiResponseContext);
        removePropertiesFromCache(responseContextDTO.getMsgInfo().getMessageId() +
                GatewayConstants.CONTEXT_PROP_CACHE_KEY);
        return responseDTOForResponse;
    }

    /**
     * Method to get response DTO for request path.
     *
     * @param fsApiRequestContext  API Request Context
     * @return ExtensionResponseDTO Extension Response DTO
     */
    protected ExtensionResponseDTO getResponseDTOForRequest(FSAPIRequestContext fsApiRequestContext) {

        ExtensionResponseDTO extensionResponseDTO = new ExtensionResponseDTO();
        if (fsApiRequestContext.isError()) {
            int statusCode = (!fsApiRequestContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) ?
                    HttpStatus.SC_INTERNAL_SERVER_ERROR : Integer.parseInt(fsApiRequestContext
                    .getContextProperty(GatewayConstants.ERROR_STATUS_PROP).toString());
            extensionResponseDTO.setStatusCode(statusCode);
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else if (fsApiRequestContext.getContextProps().containsKey(GatewayConstants.IS_RETURN_RESPONSE) &&
                Boolean.parseBoolean(fsApiRequestContext.getContextProps()
                        .get(GatewayConstants.IS_RETURN_RESPONSE).toString())) {
            Map<String, String> headers = fsApiRequestContext.getMsgInfo().getHeaders();
            headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            fsApiRequestContext.getMsgInfo().setHeaders(headers);
            extensionResponseDTO.setHeaders(headers);
            if (fsApiRequestContext.getContextProps().containsKey(GatewayConstants.MODIFIED_STATUS)) {
                extensionResponseDTO.setStatusCode(Integer.parseInt(fsApiRequestContext.getContextProps()
                        .get(GatewayConstants.MODIFIED_STATUS).toString()));
            }
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else {
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.CONTINUE.toString());
        }

        String modifiedPayload = fsApiRequestContext.getModifiedPayload();
        if (modifiedPayload != null) {
            extensionResponseDTO.setPayload(new ByteArrayInputStream(modifiedPayload.getBytes(StandardCharsets.UTF_8)));
        }

        setHeadersToResponse(extensionResponseDTO, fsApiRequestContext.getAddedHeaders(),
                fsApiRequestContext.getMsgInfo().getHeaders());
        return extensionResponseDTO;
    }

    /**
     * Method to get response DTO for response path.
     *
     * @param fsApiResponseContext  API Response Context
     * @return ExtensionResponseDTO Extension Response DTO
     */
    protected ExtensionResponseDTO getResponseDTOForResponse(FSAPIResponseContext fsApiResponseContext) {

        ExtensionResponseDTO extensionResponseDTO = new ExtensionResponseDTO();
        if (fsApiResponseContext.isError()) {
            int statusCode = (!fsApiResponseContext.getContextProps().containsKey(GatewayConstants.ERROR_STATUS_PROP)) ?
                    HttpStatus.SC_INTERNAL_SERVER_ERROR : Integer.parseInt(fsApiResponseContext
                    .getContextProperty(GatewayConstants.ERROR_STATUS_PROP).toString());
            extensionResponseDTO.setStatusCode(statusCode);
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else if (fsApiResponseContext.getContextProps().containsKey(GatewayConstants.IS_RETURN_RESPONSE) &&
                Boolean.parseBoolean(fsApiResponseContext.getContextProps()
                        .get(GatewayConstants.IS_RETURN_RESPONSE).toString())) {
            Map<String, String> headers = fsApiResponseContext.getMsgInfo().getHeaders();
            headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            fsApiResponseContext.getMsgInfo().setHeaders(headers);
            extensionResponseDTO.setHeaders(headers);
            if (fsApiResponseContext.getContextProps().containsKey(GatewayConstants.MODIFIED_STATUS)) {
                extensionResponseDTO.setStatusCode((Integer.parseInt(fsApiResponseContext.getContextProps()
                        .get(GatewayConstants.MODIFIED_STATUS).toString())));
            }
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
        } else {
            extensionResponseDTO.setResponseStatus(ExtensionResponseStatus.CONTINUE.toString());
        }

        String modifiedPayload = fsApiResponseContext.getModifiedPayload();
        if (modifiedPayload != null) {
            extensionResponseDTO.setPayload(new ByteArrayInputStream(modifiedPayload.getBytes(StandardCharsets.UTF_8)));
        }

        setHeadersToResponse(extensionResponseDTO, fsApiResponseContext.getAddedHeaders(),
                fsApiResponseContext.getMsgInfo().getHeaders());
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
    private void setPropertiesToCache(String key, Map<String, Object> contextProps) {

        GatewayDataHolder.getGatewayCache().addToCache(GatewayCacheKey.of(key), contextProps);
    }

    /**
     * Method to retrieve context properties from cache.
     *
     * @param key unique cache key
     * @return context properties
     */
    private Map<String, Object> getPropertiesFromCache(String key) {
        Object cachedObject = GatewayDataHolder.getGatewayCache().getFromCache(GatewayCacheKey.of(key));
        return cachedObject == null ? new HashMap<>() : (Map<String, Object>) cachedObject;
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
