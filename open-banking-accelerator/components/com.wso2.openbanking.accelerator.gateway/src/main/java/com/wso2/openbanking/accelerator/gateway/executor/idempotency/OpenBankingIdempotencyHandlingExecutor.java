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

package com.wso2.openbanking.accelerator.gateway.executor.idempotency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.gateway.cache.OpenBankingIdempotencyCacheKey;
import com.wso2.openbanking.accelerator.gateway.cache.OpenBankingIdempotencyValidationCache;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.IdempotencyConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Executor to handle Payment Idempotency.
 */
public abstract class OpenBankingIdempotencyHandlingExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(OpenBankingIdempotencyHandlingExecutor.class);
    private OpenBankingIdempotencyValidationCache openBankingIdempotencyValidationCache =
            OpenBankingIdempotencyValidationCache.getInstance();
    private OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance();

    /**
     * Method to handle pre request.
     *
     * @param obapiRequestContext OB request context object.
     */
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object.
     */
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        // Checking if idempotency is enabled.
        if (!isIdempotencyEnabledFromConfig()) {
            return;
        }

        // Validating if the request is a valid idempotency available request.
        if (!isValidIdempotencyRequest(obapiRequestContext)) {
            return;
        }

        //Retrieve headers and payload
        Map<String, String> requestHeaders = obapiRequestContext.getMsgInfo().getHeaders();

        //Retrieve consumer key from headers
        String consumerKey = obapiRequestContext.getApiRequestInfo().getConsumerKey();
        //Retrieve idempotency key from headers
        String idempotencyKey = requestHeaders.get(getIdempotencyKeyConstantFromConfig());
        //Retrieve context properties
        Map<String, String> contextProps = obapiRequestContext.getContextProps();

        // Retrieve elected resources
        String resource = obapiRequestContext.getMsgInfo().getResource();

        //Construct cache keys for request and response using client Id and idempotency key
        String idempotencyCacheKey = consumerKey + "_" + resource + "_" + idempotencyKey;

        try {
            Map<String, Object> payloadMap = getPayloadFromRequest(obapiRequestContext);

            String payload;
            if (payloadMap.containsKey(IdempotencyConstants.PAYLOAD)) {
                payload = (String) payloadMap.get(IdempotencyConstants.PAYLOAD);
            } else {
                log.error("Error reading payload, " + IdempotencyConstants.PAYLOAD + " is not set.");
                return;
            }

            int httpStatus;
            if (payloadMap.containsKey(IdempotencyConstants.HTTP_STATUS)) {
                httpStatus = (int) payloadMap.get(IdempotencyConstants.HTTP_STATUS);
            } else {
                log.error("Error reading HTTP status, " + IdempotencyConstants.HTTP_STATUS + " is not set.");
                return;
            }

            Map<String, String> cachedObjectMap = getPropertiesFromCache(idempotencyCacheKey);
            //Check whether the request exists in the cache
            if (!cachedObjectMap.isEmpty()) {
                log.debug("Handling idempotency through gateway");

                // previous result is present in cache, retrieving request from cache
                String cachedRequest = cachedObjectMap.get(GatewayConstants.REQUEST_CACHE_KEY);
                String createdTime = cachedObjectMap.get(GatewayConstants.CREATED_TIME_CACHE_KEY);
                //Check whether payload received is similar to the payload stored
                if (isJSONPayloadSimilar(cachedRequest, payload)) {
                    log.debug("Payloads are similar for idempotent request");
                    //Payloads are similar, hence checking whether request came within allowed time
                    if (isRequestReceivedWithinAllowedTime(createdTime)) {
                        log.debug("Idempotent request received within allowed time");
                        //Retrieving the response from cache
                        String cachedResponse = cachedObjectMap.get(GatewayConstants.RESPONSE_CACHE_KEY);

                        //Setting payload as modified payload
                        log.debug("Setting cached payload as the response");
                        obapiRequestContext.setModifiedPayload(cachedResponse);

                        //Setting Context Properties to return response without executing further
                        contextProps.put(GatewayConstants.IS_RETURN_RESPONSE, GatewayConstants.TRUE);
                        contextProps.put(GatewayConstants.MODIFIED_STATUS, String.valueOf(httpStatus));
                    }
                } else {
                    //Payloads are not similar, hence returning an error
                    log.error(IdempotencyConstants.Error.EXECUTOR_IDEMPOTENCY_KEY_FRAUDULENT);
                    obapiRequestContext.setError(true);
                    obapiRequestContext.setErrors(handleIdempotencyErrors(obapiRequestContext,
                            IdempotencyConstants.Error.EXECUTOR_IDEMPOTENCY_KEY_FRAUDULENT,
                            IdempotencyConstants.Error.HEADER_INVALID));
                }
            } else {
                log.debug("Request is not found in cache, adding the request to cache.");
                //Since request is not in cache, adding the request to the cache against the idempotency key
                contextProps.put(GatewayConstants.REQUEST_CACHE_KEY, payload);
            }
        } catch (IOException e) {
            log.error(IdempotencyConstants.Error.EXECUTOR_IDEMPOTENCY_KEY_ERROR, e);
            obapiRequestContext.setError(true);
            obapiRequestContext.setErrors(handleIdempotencyErrors(obapiRequestContext,
                    IdempotencyConstants.Error.EXECUTOR_IDEMPOTENCY_KEY_ERROR,
                    IdempotencyConstants.Error.HEADER_INVALID));
            return;
        }
        //Adding idempotency key to the context properties
        contextProps.put(GatewayConstants.IDEMPOTENCY_KEY_CACHE_KEY, idempotencyKey);
        obapiRequestContext.setContextProps(contextProps);
    }

    /**
     * Method to handle pre response.
     *
     * @param obapiResponseContext OB response context object.
     */
    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object.
     */
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

        // Checking if idempotency is enabled.
        if (!isIdempotencyEnabledFromConfig()) {
            return;
        }

        // Validating if the response is a valid idempotency available response.
        if (!isValidIdempotencyResponse(obapiResponseContext)) {
            return;
        }

        //Retrieving payload
        String responsePayload = obapiResponseContext.getResponsePayload();
        //Retrieve idempotency key from headers
        String consumerKey = obapiResponseContext.getApiRequestInfo().getConsumerKey();
        //Retrieve context properties
        Map<String, String> contextProps = obapiResponseContext.getContextProps();

        MsgInfoDTO msgInfoDTO = obapiResponseContext.getMsgInfo();

        String idempotencyKey;
        if (msgInfoDTO.getHeaders().get(getIdempotencyKeyConstantFromConfig()) != null) {
            //Retrieve idempotency key from headers
            idempotencyKey = msgInfoDTO.getHeaders().get(getIdempotencyKeyConstantFromConfig());
        } else {
            //Retrieve idempotency key from context props if it does not exist as a header
            idempotencyKey = contextProps.get(GatewayConstants.IDEMPOTENCY_KEY_CACHE_KEY);
        }

        String createdTime = getCreatedTimeFromResponse(obapiResponseContext);
        if (createdTime == null) {
            log.error(IdempotencyConstants.Error.DATE_MISSING);
            return;
        }

        // Retrieve elected resources
        String resource = msgInfoDTO.getResource();

        //Construct cache keys for request and response using client Id and idempotency key
        String idempotencyCacheKey = consumerKey + "_" + resource + "_" + idempotencyKey;

        //Add response and created time to the cache
        HashMap<String, String> cachedObject = getPropertiesFromCache(idempotencyCacheKey);
        if (contextProps.get(GatewayConstants.REQUEST_CACHE_KEY) != null) {
            cachedObject.put(GatewayConstants.REQUEST_CACHE_KEY, contextProps.get(GatewayConstants.REQUEST_CACHE_KEY));
        }
        cachedObject.put(GatewayConstants.RESPONSE_CACHE_KEY, responsePayload);
        cachedObject.put(GatewayConstants.CREATED_TIME_CACHE_KEY, createdTime);

        log.debug("Setting properties to cache");
        setPropertiesToCache(idempotencyCacheKey, cachedObject);
    }

    /**
     * Method to handle errors in Idempotency validation.
     *
     * @param obapiRequestContext obapiRequestContext.
     * @param message             message.
     * @return Arraylist of OpenBankingExecutorError.
     */
    protected ArrayList<OpenBankingExecutorError> handleIdempotencyErrors(OBAPIRequestContext obapiRequestContext,
                                                                        String message, String errorCode) {

        OpenBankingExecutorError error = new OpenBankingExecutorError(errorCode,
                IdempotencyConstants.Error.IDEMPOTENCY_HANDLE_ERROR, message,
                OpenBankingErrorCodes.BAD_REQUEST_CODE);
        ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
        executorErrors.add(error);
        return executorErrors;
    }

    /**
     * Method to store properties to cache.
     *
     * @param key               unique cache key.
     * @param idempotentDetails properties to store.
     */
    private void setPropertiesToCache(String key, HashMap<String, String> idempotentDetails) {

        openBankingIdempotencyValidationCache.addToCache(
                OpenBankingIdempotencyCacheKey.of(key), idempotentDetails);
    }

    /**
     * Method to retrieve context properties from cache.
     *
     * @param key unique cache key.
     * @return context properties.
     */
    private HashMap<String, String> getPropertiesFromCache(String key) {

        HashMap<String, String> cachedObject = openBankingIdempotencyValidationCache.getFromCache(
                OpenBankingIdempotencyCacheKey.of(key));
        return cachedObject == null ? new HashMap<>() : cachedObject;
    }

    /**
     * Method to compare whether JSON payloads are equal.
     *
     * @param jsonString1    JSON payload retrieved from database
     * @param jsonString2    JSON payload received from current request
     * @return
     * @throws IOException
     */
    private boolean isJSONPayloadSimilar(String jsonString1, String jsonString2) throws IOException {

        JsonNode expectedNode = new ObjectMapper().readTree(jsonString1);
        JsonNode actualNode = new ObjectMapper().readTree(jsonString2);
        return expectedNode.equals(actualNode);
    }

    /**
     * Method to check whether difference between two dates is less than the configured time.
     *
     * @param createdTime    Created Time of the request
     * @return
     */
    protected boolean isRequestReceivedWithinAllowedTime(String createdTime) {

        if (createdTime == null) {
            return true;
        }
        String allowedTimeDuration = (String) openBankingConfigParser.getConfiguration()
                .get(IdempotencyConstants.IDEMPOTENCY_ALLOWED_TIME);
        if (allowedTimeDuration != null) {
            OffsetDateTime createdDate = OffsetDateTime.parse(createdTime);
            OffsetDateTime currDate = OffsetDateTime.now(createdDate.getOffset());

            long diffInHours = Duration.between(createdDate, currDate).toMinutes();
            return diffInHours <= Long.parseLong(allowedTimeDuration);
        } else {
            log.error("Idempotency Allowed duration is null");
            return false;
        }
    }

    /**
     * Method to check whether Idempotency handling is required.
     *
     * @return True if idempotency is required, else False.
     */
    private boolean isIdempotencyEnabledFromConfig() {

        String isIdempotencyEnabled = (String) openBankingConfigParser.getConfiguration()
                .get(IdempotencyConstants.IDEMPOTENCY_IS_ENABLED);

        return Boolean.parseBoolean(isIdempotencyEnabled);
    }

    /**
     * Method to get the Idempotency Key from the config.
     *
     * @return idempotency key.
     */
    protected String getIdempotencyKeyConstantFromConfig() {

        return (String) openBankingConfigParser.getConfiguration()
                .get(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER);
    }

    /**
     * Method to get Created time from response.
     *
     * @param obapiResponseContext obapiResponseContext.
     * @return created time.
     */
    public abstract String getCreatedTimeFromResponse(OBAPIResponseContext obapiResponseContext);

    /**
     * Method to get payload from request.
     *
     * @param obapiRequestContext obapiRequestContext.
     * @return Map containing the payload and the http status.
     */
    public abstract Map<String, Object> getPayloadFromRequest(OBAPIRequestContext obapiRequestContext);

    /**
     * Method to check if the request is a valid idempotency request.
     *
     * @param obapiRequestContext obapiRequestContext.
     * @return True if the request is valid, False if not.
     */
    public abstract boolean isValidIdempotencyRequest(OBAPIRequestContext obapiRequestContext);

    /**
     * Method to check if the method is a valid idempotency response.
     *
     * @param obapiResponseContext obapiResponseContext.
     * @return True if the response is valid, False if not.
     */
    public abstract boolean isValidIdempotencyResponse(OBAPIResponseContext obapiResponseContext);
}
