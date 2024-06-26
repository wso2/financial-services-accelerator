/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.consent.extensions.utils;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentAttributes;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * Utils class for consent executor tests.
 */
public class ConsentExtensionTestUtils {

    static JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    public static void injectEnvironmentVariable(String key, String value)
            throws ReflectiveOperationException {

        Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");

        Field unmodifiableMapField = getAccessibleField(processEnvironment, "theUnmodifiableEnvironment");
        Object unmodifiableMap = unmodifiableMapField.get(null);
        injectIntoUnmodifiableMap(key, value, unmodifiableMap);

        Field mapField = getAccessibleField(processEnvironment, "theEnvironment");
        Map<String, String> map = (Map<String, String>) mapField.get(null);
        map.put(key, value);
    }

    private static Field getAccessibleField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private static void injectIntoUnmodifiableMap(String key, String value, Object map)
            throws ReflectiveOperationException {

        Class unmodifiableMap = Class.forName("java.util.Collections$UnmodifiableMap");
        Field field = getAccessibleField(unmodifiableMap, "m");
        Object obj = field.get(map);
        ((Map<String, String>) obj).put(key, value);
    }

    public static JSONObject getInitiationPayload(JSONObject payload) {
        return (JSONObject) ((JSONObject) payload.get(ConsentExtensionConstants.DATA))
                .get(ConsentExtensionConstants.INITIATION);
    }

    public static JSONObject getJsonPayload(String payload) throws ParseException {
        return (JSONObject) parser.parse(payload);
    }

    public static ConsentAttributes getConsentAttributes(String paymentType) {

        Map<String, String> consentAttributesMap = new HashMap<String, String>();
        consentAttributesMap.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, "100.00");
        consentAttributesMap.put(ConsentExtensionConstants.PAYMENT_TYPE, paymentType);
        consentAttributesMap.put(ConsentExtensionConstants.PAID_AMOUNT, "20.00");
        consentAttributesMap.put(ConsentExtensionConstants.LAST_PAYMENT_DATE,
                OffsetDateTime.now().minusDays(50).toString());
        consentAttributesMap.put(ConsentExtensionConstants.PREVIOUS_PAID_AMOUNT, "20.00");
        consentAttributesMap.put(ConsentExtensionConstants.PREVIOUS_LAST_PAYMENT_DATE,
                OffsetDateTime.now().minusDays(50).toString());

        ConsentAttributes consentAttributes = new ConsentAttributes();
        consentAttributes.setConsentAttributes(consentAttributesMap);

        return consentAttributes;
    }

}
