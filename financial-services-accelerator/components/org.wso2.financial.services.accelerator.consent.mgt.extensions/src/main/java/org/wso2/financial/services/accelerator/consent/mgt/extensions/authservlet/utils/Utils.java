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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.encoder.Encode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility methods.
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    /**
     * To get the property value for the given key from the ResourceBundle.
     * Retrieve the value of property entry for key, return key if a value is not found for key
     *
     * @param resourceBundle  ResourceBundle
     * @param key  Key
     * @return  Value of the property entry for key
     */
    public static String i18n(ResourceBundle resourceBundle, String key) {

        try {
            return Encode.forHtml((StringUtils.isNotBlank(resourceBundle.getString(key)) ?
                    resourceBundle.getString(key) : key));
        } catch (Exception e) {
            // Intentionally catching Exception and if something goes wrong while finding the value for key, return
            // default, not to break the UI
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error while retrieving the value for key: %s from resource bundle.",
                        key.replaceAll("[\n\r]", "")));
            }
            return Encode.forHtml(key);
        }
    }

    /**
     * Split claims based on a deliminator and create map of claimID and displayName.
     *
     * @param requestedClaimList   Requested claim list
     * @return  List of claims
     */
    public static List<Map<String, String>> splitClaims(String[] requestedClaimList) {

        List<Map<String, String>> requestedClaims = new ArrayList<>();

        for (String claim : requestedClaimList) {
            String[] requestedClaimData = claim.split("_", 2);
            if (requestedClaimData.length == 2) {
                Map<String, String> data = new HashMap<>();
                data.put("claimId", requestedClaimData[0]);
                data.put("displayName", requestedClaimData[1]);
                requestedClaims.add(data);
            }
        }
        return requestedClaims;
    }

    /**
     * Recursively converts jsonObject to a Map.
     *
     * @param jsonObject JSONObject to be converted to a Map
     * @return jsonObject converted to a nested HashMap
     */
    public static Map<String, Object> jsonObjectToMap(JSONObject jsonObject) {

        Map<String, Object> map = new HashMap<>();

        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.put(key, jsonObjectToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.put(key, jsonArrayToList((JSONArray) value));
            } else {
                map.put(key, value);
            }
        }

        return map;
    }

    /**
     * Recursively converts jsonObject to a Map.
     *
     * @param jsonArray jsonArray to be converted to a Map
     * @return jsonArray converted to a nested ArrayList
     */
    private static List<Object> jsonArrayToList(JSONArray jsonArray) {

        List<Object> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                list.add(jsonObjectToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                list.add(jsonArrayToList((JSONArray) value));
            } else {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * Method to flatten basicConsentData.
     *
     * @param payload
     */
    public static void flattenBasicConsentData(JSONObject payload) {
        if (payload.has(ConsentExtensionConstants.CONSENT_DATA)) {
            JSONObject consentData = payload.getJSONObject(ConsentExtensionConstants.CONSENT_DATA);
            if (consentData != null && consentData.has(ConsentAuthorizeConstants.BASIC_CONSENT_DATA)) {
                JSONArray basicConsentArray = consentData.getJSONArray(ConsentAuthorizeConstants.BASIC_CONSENT_DATA);

                if (basicConsentArray != null) {
                    JSONObject flattened = new JSONObject();

                    for (int i = 0; i < basicConsentArray.length(); i++) {
                        JSONObject item = basicConsentArray.optJSONObject(i);
                        if (item != null) {
                            String title = item.optString(ConsentExtensionConstants.TITLE, null);
                            JSONArray data = item.optJSONArray(ConsentExtensionConstants.DATA_CC);

                            if (title != null && data != null) {
                                flattened.put(title, data);
                            }
                        }
                    }

                    consentData.put(ConsentAuthorizeConstants.BASIC_CONSENT_DATA, flattened);
                }
            }
        }
    }
}
