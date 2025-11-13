/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.toolkittemplate.extensions.utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for common consent validation operations.
 */
public class CommonConsentValidationUtil {

    /**
     * Converts a generic Java object to a {@link JSONObject}.
     *
     * @param object the Java object to be converted to JSON
     * @return a {@link JSONObject} representation of the given object
     * @throws JsonProcessingException if the object cannot be serialized to a JSON string
     */
    public static JSONObject convertObjectToJson(Object object) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(object);

        return new JSONObject(jsonString);
    }

    /**
     * Build the complete URL with query parameters sent in the map.
     *
     * @param baseURL    the base URL
     * @param parameters map of parameters
     * @return the output URL
     */
    public static String buildRequestURL(String baseURL, Map<String, String> parameters) {

        List<NameValuePair> pairs = new ArrayList<>();

        for (Map.Entry<String, String> key : parameters.entrySet()) {
            if (key.getKey() != null && key.getValue() != null) {
                pairs.add(new BasicNameValuePair(key.getKey(), key.getValue()));
            }
        }
        String queries = URLEncodedUtils.format(pairs, StandardCharsets.UTF_8);
        return baseURL + "?" + queries;
    }

    /**
     * Validate whether the date is a valid ISO 8601 format.
     *
     * @param dateValue date string to validate
     * @return true if the date is valid ISO 8601 format, otherwise false
     */
    public static boolean isValid8601(String dateValue) {
        try {
            OffsetDateTime.parse(dateValue);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Check if consent is expired based on validUntilDate.
     *
     * @param validUntilDate valid until time in epoch seconds
     * @return whether consent is expired or not
     */
    public static boolean isConsentExpired(long validUntilDate) {
        LocalDateTime expDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(validUntilDate), ZoneOffset.UTC);
        LocalDate expDate = expDateTime.toLocalDate();

        LocalDate currDate = LocalDate.now(ZoneOffset.UTC);

        return currDate.isAfter(expDate);
    }

}
