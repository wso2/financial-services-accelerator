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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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

            if (value == JSONObject.NULL) {
                map.put(key, null);
            } else if (value instanceof JSONObject) {
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

            if (value == JSONObject.NULL) {
                list.add(null);
            } else if (value instanceof JSONObject) {
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
     * Converts String occurrences of values in BasicConsentData to arrays with single value
     *
     * @param dataSet dataSet to be formatted
     */
    public static void formatBasicConsentData(JSONObject dataSet) {
        if (dataSet == null) return;

        JSONObject consentData = dataSet.optJSONObject(ConsentAuthorizeConstants.CONSENT_DATA);
        if (consentData == null) return;

        JSONObject basicConsentData = consentData.optJSONObject(ConsentAuthorizeConstants.BASIC_CONSENT_DATA);
        if (basicConsentData == null) return;

        JSONObject formatted = new JSONObject();

        for (String key : basicConsentData.keySet()) {
            Object value = basicConsentData.get(key);

            if (value instanceof JSONArray) {
                formatted.put(key, value);
            } else {
                JSONArray arr = new JSONArray();
                arr.put(value);
                formatted.put(key, arr);
            }
        }

        consentData.put(ConsentAuthorizeConstants.BASIC_CONSENT_DATA, formatted);
    }

    /**
     * Expand sub-attributes within the retrieved payload
     *
     * @param dataSet dataSet received from the execution of retrieval steps
     * @return updated request attribute map
     */
    public static Map<String, Object> returnAttributesFromDataSet(JSONObject dataSet) {
        Map<String, Object> attributeMap = new HashMap<>();

        if (dataSet == null) {
            return attributeMap;
        }

        // Convert JSON object to map
        Map<String, Object> dataSetMap = jsonObjectToMap(dataSet);

        Map<String, Object> consentData = (Map<String, Object>) dataSetMap.get(ConsentAuthorizeConstants.CONSENT_DATA);
        Map<String, Object> consumerData = (Map<String, Object>)
                dataSetMap.get(ConsentAuthorizeConstants.CONSUMER_DATA);

        Map<String, List<String>> basicConsentData = null;
        List<Map<String, Object>> permissions = null;
        List<Object> initiatedAccountsForConsent = null;
        Boolean isReauthorization = false;
        Boolean allowMultipleAccounts = false;

        if (consentData != null) {
            basicConsentData = (Map<String, List<String>>) consentData.getOrDefault(
                    ConsentAuthorizeConstants.BASIC_CONSENT_DATA, null);
            permissions = (List<Map<String, Object>>) consentData.getOrDefault(
                    ConsentAuthorizeConstants.PERMISSIONS, null);
            initiatedAccountsForConsent = (List<Object>) consentData.getOrDefault(
                    ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT, null);
            isReauthorization = (Boolean) consentData.getOrDefault(
                    ConsentAuthorizeConstants.IS_REAUTHORIZATION, false);
            allowMultipleAccounts = (Boolean) consentData.getOrDefault(
                    ConsentAuthorizeConstants.ALLOW_MULTIPLE_ACCOUNTS, false);
        }

        List<Map<String, Object>> consumerAccounts = null;
        if (consumerData != null) {
            consumerAccounts = (List<Map<String, Object>>) consumerData.get(
                    ConsentAuthorizeConstants.ACCOUNTS);
        }

        attributeMap.put(ConsentAuthorizeConstants.BASIC_CONSENT_DATA, basicConsentData);
        attributeMap.put(ConsentAuthorizeConstants.PERMISSIONS, permissions);
        attributeMap.put(ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT, initiatedAccountsForConsent);
        attributeMap.put(Constants.CONSUMER_ACCOUNTS, consumerAccounts);
        attributeMap.put(ConsentAuthorizeConstants.ALLOW_MULTIPLE_ACCOUNTS, allowMultipleAccounts);
        attributeMap.put(ConsentAuthorizeConstants.IS_REAUTHORIZATION, isReauthorization);

        return attributeMap;
    }
}
