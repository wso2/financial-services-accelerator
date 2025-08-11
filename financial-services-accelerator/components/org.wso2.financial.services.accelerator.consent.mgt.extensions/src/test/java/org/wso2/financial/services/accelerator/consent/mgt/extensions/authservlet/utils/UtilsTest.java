/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit test class for utility methods.
 */
public class UtilsTest {

    @Test
    public void testSplitClaims_withValidClaims() {
        String[] input = {"email_Email Address", "dob_Date of Birth"};
        List<Map<String, String>> result = Utils.splitClaims(input);

        assertEquals(result.size(), 2);
        assertEquals(result.get(0).get("claimId"), "email");
        assertEquals(result.get(0).get("displayName"), "Email Address");
        assertEquals(result.get(1).get("claimId"), "dob");
        assertEquals(result.get(1).get("displayName"), "Date of Birth");
    }

    @Test
    public void testSplitClaims_withInvalidClaims() {
        String[] input = {"email_Email Address", "invalidClaim"};
        List<Map<String, String>> result = Utils.splitClaims(input);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).get("claimId"), "email");
    }

    @Test
    public void testJsonObjectToMap_withPrimitiveValues() {
        JSONObject input = new JSONObject();
        input.put("claimId", "email");
        input.put("claimIdx", 3);
        input.put("isActive", true);

        Map<String, Object> result = Utils.jsonObjectToMap(input);

        assertEquals(result.get("claimId"), "email");
        assertEquals(result.get("claimIdx"), 3);
        assertEquals(result.get("isActive"), true);
    }

    @Test
    public void testJsonObjectToMap_withNestedJSONObject() {
        JSONObject nested = new JSONObject();
        nested.put("type", "Payment");

        JSONObject input = new JSONObject();
        input.put("consentData", nested);

        Map<String, Object> result = Utils.jsonObjectToMap(input);

        assertTrue(result.get("consentData") instanceof Map);
        Map<String, Object> nestedMap = (Map<String, Object>) result.get("consentData");
        assertEquals(nestedMap.get("type"), "Payment");
    }

    @Test
    public void testJsonObjectToMap_withJSONArray() {
        JSONArray array = new JSONArray();
        array.put("accounts");
        array.put("balance");
        array.put("transactions");

        JSONObject input = new JSONObject();
        input.put("access", array);

        Map<String, Object> result = Utils.jsonObjectToMap(input);

        assertTrue(result.get("access") instanceof List);
        List<?> list = (List<?>) result.get("access");
        assertEquals(Arrays.asList("accounts", "balance", "transactions"), list);
    }

    @Test
    public void testJsonObjectToMap_withNestedArrayAndObject() {
        JSONObject obj1 = new JSONObject();
        obj1.put("id", 123);

        JSONObject obj2 = new JSONObject();
        obj2.put("id", 456);

        JSONArray array = new JSONArray();
        array.put(obj1);
        array.put(obj2);

        JSONObject input = new JSONObject();
        input.put("accounts", array);

        Map<String, Object> result = Utils.jsonObjectToMap(input);

        assertTrue(result.get("accounts") instanceof List);
        List<?> list = (List<?>) result.get("accounts");

        Map<String, Object> first = (Map<String, Object>) list.get(0);
        Map<String, Object> second = (Map<String, Object>) list.get(1);

        assertEquals(first.get("id"), 123);
        assertEquals(second.get("id"), 456);
    }

    @Test
    public void testJsonObjectToMap_withEmptyObjects() {
        JSONObject input = new JSONObject();
        input.put("emptyObject", new JSONObject());
        input.put("emptyArray", new JSONArray());

        Map<String, Object> result = Utils.jsonObjectToMap(input);

        assertTrue(result.get("emptyObject") instanceof Map);
        assertTrue(((Map<?, ?>) result.get("emptyObject")).isEmpty());

        assertTrue(result.get("emptyArray") instanceof List);
        assertTrue(((List<?>) result.get("emptyArray")).isEmpty());
    }

    @Test
    public void testAppendResourceBundleParams_populatesExpectedKeys() {
        ResourceBundle resourceBundleMock = mock(ResourceBundle.class);
        when(resourceBundleMock.getString(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JSONObject dataSet = new JSONObject();
        dataSet.put("application", "MyApp");

        Utils.appendResourceBundleParams(dataSet, resourceBundleMock);

        JSONObject rbData = dataSet.optJSONObject(Constants.RESOURCE_BUNDLE_DATA);
        assertNotNull(rbData);

        assertEquals(rbData.getString(Constants.APP_REQUESTS_DETAILS), Constants.APP_REQUESTS_DETAILS_KEY);
        assertEquals(rbData.getString(Constants.DATA_REQUESTED), Constants.DATA_REQUESTED_KEY);
        assertEquals(rbData.getString(Constants.REQUESTED_PERMISSIONS), Constants.REQUESTED_PERMISSIONS_KEY);
    }

    @Test
    public void testAppendLanguageOptions_setsTextDirection() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getLocale()).thenReturn(Locale.forLanguageTag("ar"));

        JSONObject dataSet = new JSONObject();

        Utils.appendLanguageOptions(dataSet, requestMock);

        JSONObject rbData = dataSet.optJSONObject(Constants.RESOURCE_BUNDLE_DATA);
        assertNotNull(rbData);

        String direction = rbData.getString("textDirection");
        assertNotNull(direction);
        assertTrue(direction.equals("ltr") || direction.equals("rtl"));
    }

    @Test
    public void testGetLanguagePropertiesForLocale_withMockedProperties() throws Exception {
        String propertiesContent = "lang.switch.en=English,EN,ltr\nlang.switch.ar=Arabic,AR,rtl\n";

        try (MockedStatic<Utils> mockedStatic = Mockito.mockStatic(Utils.class)) {
            // Mock only getClassLoaderResourceAsStream
            mockedStatic.when(() -> Utils.getClassLoaderResourceAsStream("LanguageOptions.properties"))
                    .thenAnswer(invocation -> new ByteArrayInputStream(propertiesContent.getBytes()));

            // For getLanguagePropertiesForLocale, call the real method
            mockedStatic.when(() -> Utils.getLanguagePropertiesForLocale(Mockito.any(Locale.class)))
                    .thenCallRealMethod();

            String[] enProps = Utils.getLanguagePropertiesForLocale(Locale.ENGLISH);
            assertNotNull(enProps);
            assertEquals(enProps[0], "English");
            assertEquals(enProps[2], "ltr");

            String[] arProps = Utils.getLanguagePropertiesForLocale(new Locale("ar"));
            assertNotNull(arProps);
            assertEquals(arProps[0], "Arabic");
            assertEquals(arProps[2], "rtl");

            String[] fallbackProps = Utils.getLanguagePropertiesForLocale(new Locale("fr"));
            assertNotNull(fallbackProps);
            assertEquals(fallbackProps[2], "ltr");
        }
    }
}
