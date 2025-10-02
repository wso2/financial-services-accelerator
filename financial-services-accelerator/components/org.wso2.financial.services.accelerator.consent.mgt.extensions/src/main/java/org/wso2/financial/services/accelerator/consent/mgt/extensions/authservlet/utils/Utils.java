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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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
     * Expand sub-attributes within the retrieved payload.
     *
     * @param dataSet dataSet received from the execution of retrieval steps
     * @return updated request attribute map
     */
    public static Map<String, Object> extractAttributesFromDataSet(JSONObject dataSet) {
        Map<String, Object> attributeMap = new HashMap<>();

        if (dataSet == null) {
            return attributeMap;
        }

        // Convert JSON object to map
        Map<String, Object> dataSetMap = jsonObjectToMap(dataSet);

        appendCustomizationAttributes(dataSetMap, attributeMap);
        appendI18nAttributes(dataSetMap, attributeMap);

        return attributeMap;
    }

    private static void appendCustomizationAttributes(Map<String, Object> dataSetMap,
                                                      Map<String, Object> attributeMap) {

        Map<String, Object> consentData = (Map<String, Object>) dataSetMap.get(ConsentAuthorizeConstants.CONSENT_DATA);
        Map<String, Object> consumerData = (Map<String, Object>)
                dataSetMap.get(ConsentAuthorizeConstants.CONSUMER_DATA);

        Map<String, List<String>> basicConsentData = null;
        List<Map<String, Object>> permissions = null;
        List<Object> initiatedAccountsForConsent = null;
        Boolean isReauthorization = false;
        Boolean allowMultipleAccounts = false;
        Boolean handleAccountSelectionSeparately = false;
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
            handleAccountSelectionSeparately = (Boolean) consentData.getOrDefault(
                    ConsentAuthorizeConstants.HANDLE_ACCOUNT_SELECTION_SEPARATELY, false);
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
        attributeMap.put(ConsentAuthorizeConstants.HANDLE_ACCOUNT_SELECTION_SEPARATELY,
                handleAccountSelectionSeparately);
        attributeMap.put(ConsentAuthorizeConstants.IS_REAUTHORIZATION, isReauthorization);
        attributeMap.put(ConsentAuthorizeConstants.TYPE, type);
        attributeMap.put(ConsentAuthorizeConstants.HAS_MULTIPLE_PERMISSIONS,
                (permissions != null && permissions.size() > 1));
    }

    private static void appendI18nAttributes(Map<String, Object> dataSetMap, Map<String, Object> attributeMap) {
        Map<String, String> dataFromResourceBundle =
                (Map<String, String>) dataSetMap.getOrDefault(Constants.RESOURCE_BUNDLE_DATA, null);
        if (dataFromResourceBundle == null) {
            return;
        }

        attributeMap.putAll(dataFromResourceBundle);
    }

    /**
     * Builds response map from request retrieved from consent page.
     *
     * @param request request retrieved from consent page
     * @return a map of attributes to forward to retrieval
     */
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getParameterMap().entrySet()
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 1
    public static Map<String, Object> buildResponseMap(HttpServletRequest request) {
        Map<String, Object> persistMap = new HashMap<>();

        // Add account and permission request parameters
        persistMap.put(ConsentAuthorizeConstants.REQUEST_PARAMETERS,
                new JSONObject(request.getParameterMap()));

        return persistMap;
    }

    /**
     * Adds optional backslash if it's missing from configured jsp path.
     *
     * @param configuredPath    JSP path retrieved from configuration file
     * @return  correctly formatted path as required
     */
    public static String formatPath(String configuredPath) {
        if (configuredPath.charAt(0) != '/') {
            return "/" + configuredPath;
        }
        return configuredPath;
    }

    /**
     * Adds resource bundle parameters as request attributes for the JSP dispatch.
     *
     * @param dataSet   dataSet to append
     * @param resourceBundle    retrieved resource bundle
     */
    public static void appendResourceBundleParams(JSONObject dataSet,
                                                  ResourceBundle resourceBundle) {

        log.debug("Appending resource bundle parameters to dataSet");

        JSONObject dataFromResourceBundle = dataSet.optJSONObject(Constants.RESOURCE_BUNDLE_DATA);
        if (dataFromResourceBundle == null) {
            dataFromResourceBundle = new JSONObject();
            dataSet.put(Constants.RESOURCE_BUNDLE_DATA, dataFromResourceBundle);
        }

        dataFromResourceBundle.put(Constants.APP_REQUESTS_DETAILS,
                MessageFormat.format(i18n(resourceBundle, Constants.APP_REQUESTS_DETAILS_KEY),
                        dataSet.getString("application")));
        dataFromResourceBundle.put(Constants.DATA_REQUESTED, i18n(resourceBundle, Constants.DATA_REQUESTED_KEY));
        dataFromResourceBundle.put(Constants.REQUESTED_PERMISSIONS, i18n(resourceBundle,
                Constants.REQUESTED_PERMISSIONS_KEY));
        dataFromResourceBundle.put(Constants.ON_FOLLOWING_ACCOUNTS, i18n(resourceBundle,
                Constants.ON_FOLLOWING_ACCOUNTS_KEY));
        dataFromResourceBundle.put(Constants.SELECT_ACCOUNTS, i18n(resourceBundle, Constants.SELECT_ACCOUNTS_KEY));
        dataFromResourceBundle.put(Constants.SELECT_DEFAULT, i18n(resourceBundle, Constants.SELECT_DEFAULT_KEY));
        dataFromResourceBundle.put(Constants.NO_CONSUMER_ACCOUNTS, i18n(resourceBundle,
                Constants.NO_CONSUMER_ACCOUNTS_KEY));
        dataFromResourceBundle.put(Constants.RE_AUTHENTICATION_DISCLAIMER, i18n(resourceBundle,
                Constants.RE_AUTHENTICATION_DISCLAIMER_KEY));
        dataFromResourceBundle.put(Constants.IF_STOP_DATA_SHARING, i18n(resourceBundle,
                Constants.IF_STOP_DATA_SHARING_KEY));
        dataFromResourceBundle.put(Constants.CONFIRM_BUTTON, i18n(resourceBundle,
                Constants.CONFIRM_BUTTON_KEY));
        dataFromResourceBundle.put(Constants.NEXT_BUTTON, i18n(resourceBundle,
                Constants.NEXT_BUTTON_KEY));
        dataFromResourceBundle.put(Constants.DENY_BUTTON, i18n(resourceBundle,
                Constants.DENY_BUTTON_KEY));
        dataFromResourceBundle.put(Constants.GO_BACK_BUTTON, i18n(resourceBundle,
                Constants.GO_BACK_BUTTON_KEY));
        dataFromResourceBundle.put(Constants.DO_YOU_CONFIRM,
                MessageFormat.format(i18n(resourceBundle, Constants.DO_YOU_CONFIRM_KEY),
                        dataSet.getString("application")));
    }

    /**
     * Appends language options based on locale.
     *
     * @param dataSet   dataSet to append
     * @param request   server request
     */
    public static void appendLanguageOptions(JSONObject dataSet, HttpServletRequest request) {

        log.debug("Appending language options based on locale");

        JSONObject dataFromResourceBundle = dataSet.optJSONObject(Constants.RESOURCE_BUNDLE_DATA);
        if (dataFromResourceBundle == null) {
            dataFromResourceBundle = new JSONObject();
            dataSet.put(Constants.RESOURCE_BUNDLE_DATA, dataFromResourceBundle);
        }

        // default text direction
        String direction = "ltr";

        String[] langParts = getLanguagePropertiesForLocale(request.getLocale());
        if (langParts != null && langParts.length >= 3) {
            direction = langParts[2].trim().toLowerCase();
        }

        dataFromResourceBundle.put("textDirection", direction);
    }

    /**
     * Return language properties based on locale.
     *
     * @param locale    request locale
     * @return  fetched language property (with fallback)
     */
    public static String[] getLanguagePropertiesForLocale(Locale locale) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Getting language properties for locale: %s",
                    locale.toString().replaceAll("[\r\n]", "")));
        }
        try (InputStream inputStream = getClassLoaderResourceAsStream("LanguageOptions.properties")) {

            if (inputStream == null) {
                log.warn("LanguageOptions.properties file not found in classpath");
                return null; // No config file
            }

            Properties langOptions = new Properties();
            langOptions.load(inputStream);

            // Attempt full locale match (e.g., en_US)
            String fullKey = "lang.switch." + locale.toString();
            if (langOptions.containsKey(fullKey)) {
                return langOptions.getProperty(fullKey).split(",");
            }

            // Attempt language-only fallback (e.g., en)
            String langKey = "lang.switch." + locale.getLanguage();
            if (langOptions.containsKey(langKey)) {
                return langOptions.getProperty(langKey).split(",");
            }

            // Return default
            return new String[] {"English", "EN", "ltr"};

        } catch (IOException e) {
            log.error("Failed to load language options", e); // Log in production
        }

        return null;
    }

    /**
     * Method used to load options file. Separated out for testing parent method.
     *
     * @param resource resource file name
     * @return input stream of resource file data
     */
    protected static InputStream getClassLoaderResourceAsStream(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }
}
