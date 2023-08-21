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

package com.wso2.openbanking.sample.aggregator.identity.util;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.DefaultConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.exception.PushAuthRequestValidatorException;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util.PushAuthRequestValidatorUtils;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for Consent Extensions.
 */
public class AggregatorConsentExtensionUtil {
    private static final Log log = LogFactory.getLog(DefaultConsentPersistStep.class);

    public AggregatorConsentExtensionUtil() {
    }

    /**
     * Method to validate and extract consent from request object.
     * @param requestObject String Request Object
     * @return String Consent ID
     */
    public static String validateRequestObjectAndExtractConsent(String requestObject) {
        try {
            String[] jwtTokenValues = requestObject.split("\\.");
            if (jwtTokenValues.length == 3) {
                String requestObjectPayload = new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]),
                        StandardCharsets.UTF_8);
                Object payload = (new JSONParser(-1)).parse(requestObjectPayload);
                if (!(payload instanceof JSONObject)) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Payload is not a JSON object");
                } else {
                    JSONObject jsonObject = (JSONObject) payload;
                    return jsonObject.getAsString("consent");
                }
            } else {
                log.error("request object is not signed JWT");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "request object is not signed JWT");
            }
        } catch (ParseException var6) {
            log.error("Error while parsing the request object : ", var6);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while parsing the request object ");
        }
    }

    /**
     * Method to extract request object.
     * @param spQueryParams Query Params as a single String
     * @return Request object value.
     */
    public static String extractRequestObject(String spQueryParams) {
        if (spQueryParams != null && !spQueryParams.trim().isEmpty()) {
            String requestObject = null;
            String[] spQueries = spQueryParams.split("&");
            String clientId = null;
            String[] queryArr = spQueries;
            int queryParamCount = spQueries.length;
            for (int queryElement = 0; queryElement < queryParamCount; ++queryElement) {
                String param = queryArr[queryElement];
                if (param.contains("client_id=")) {
                    clientId = param.split("client_id=")[1];
                }
                if (param.contains("request=")) {
                    requestObject = param.substring("request=".length())
                            .replaceAll("\\r\\n|\\r|\\n|\\%20", "");
                } else if (param.contains("request_uri=")) {
                    log.debug("Resolving request URI during Steps execution");
                    String[] requestUri = param.substring("request_uri=".length())
                            .replaceAll("\\%3A", ":").split(":");
                    String sessionKey = requestUri[requestUri.length - 1];
                    SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionKey);
                    SessionDataCacheEntry sessionDataCacheEntry =
                            SessionDataCache.getInstance().getValueFromCache(cacheKey);
                    if (sessionDataCacheEntry == null) {
                        log.error("Could not find cache entry with request URI");
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                "Request object cannot be extracted");
                    }
                    String requestObjectFromCache = sessionDataCacheEntry.getoAuth2Parameters()
                            .getEssentialClaims().split(":")[0];
                    if (requestObjectFromCache.split("\\.").length == 5) {
                        try {
                            requestObject = PushAuthRequestValidatorUtils.decrypt(requestObjectFromCache, clientId);
                        } catch (PushAuthRequestValidatorException var14) {
                            log.error("Error occurred while decrypting", var14);
                            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                    "Request object cannot be extracted");
                        }
                    } else {
                        requestObject = requestObjectFromCache;
                    }
                    log.debug("Removing request_URI entry from cache");
                    SessionDataCache.getInstance().clearCacheEntry(cacheKey);
                }
            }
            if (requestObject != null) {
                return requestObject;
            }
        }
        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Request object cannot be extracted");
    }
}
