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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
     * Converts String occurrences of values in BasicConsentData to arrays with single value.
     *
     * @param dataSet dataSet to be formatted
     */
    public static void formatBasicConsentData(JSONObject dataSet) {
        if (dataSet == null) {
            return;
        }

        JSONObject consentData = dataSet.optJSONObject(ConsentAuthorizeConstants.CONSENT_DATA);
        if (consentData == null) {
            return;
        }

        JSONObject basicConsentData = consentData.optJSONObject(ConsentAuthorizeConstants.BASIC_CONSENT_DATA);
        if (basicConsentData == null) {
            return;
        }

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
     * Expand sub-attributes within the retrieved payload.
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
        String type = null;

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
            type = (String) consentData.getOrDefault(
                    ConsentAuthorizeConstants.TYPE, null);
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
        attributeMap.put(ConsentAuthorizeConstants.TYPE, type);
        attributeMap.put(ConsentAuthorizeConstants.HAS_MULTIPLE_PERMISSIONS,
                (permissions != null && permissions.size() > 1));

        return attributeMap;
    }

    /**
     * Builds response map from request retrieved from consent page.
     *
     * @param request request retrieved from consent page
     * @return a map of attributes to forward to retrieval
     */
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getParameter("isReauthorization")
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 1
    public static Map<String, Object> buildResponseMap(HttpServletRequest request) {
        Map<String, Object> persistMap = new HashMap<>();

        // Add is reauthorization
        persistMap.put(ConsentAuthorizeConstants.IS_REAUTHORIZATION,
                String.valueOf(true).equals(request.getParameter(ConsentAuthorizeConstants.IS_REAUTHORIZATION)));

        // Add account and permission request parameters
        persistMap.put(ConsentAuthorizeConstants.REQUEST_ACCOUNT_PERMISSION_PARAMETERS,
                filterAccountPermissionParameters(request));

        return persistMap;
    }

    /**
     * Filters only accounts and permissions from the JSP response.
     * @param request server request
     * @return request parameter object
     */
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getParameter("encodedAccountsPermissionsData")
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed content - request.getParameterMap().entrySet()
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 2
    private static JSONObject filterAccountPermissionParameters(HttpServletRequest request) {
        JSONObject filteredParameters =  new JSONObject();

        // Append all included permission and selected accounts to authorizedData Object
        for (Map.Entry<String, String[]> parameter: request.getParameterMap().entrySet()) {
            if (parameter.getKey().contains("permission")) {
                // Permission for a specific index should not have multiple hashes
                filteredParameters.put(parameter.getKey(), parameter.getValue()[0]);
            } else if (parameter.getKey().contains("accounts")) {
                filteredParameters.put(parameter.getKey(), new JSONArray(parameter.getValue()));
            }
        }

        return filteredParameters;
    }
}
