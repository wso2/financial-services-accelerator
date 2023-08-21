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

package com.wso2.openbanking.accelerator.consent.extensions.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCacheKey;
import net.minidev.json.JSONValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Class for maintaining Consent Cache.
 */
public class ConsentCache {

    private static volatile IdentityCache consentCache;

    private static Log log = LogFactory.getLog(ConsentCache.class);

    private static ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();

    private static final String preserveConsent = (String) OpenBankingConfigParser.getInstance().getConfiguration()
            .get(ConsentExtensionConstants.PRESERVE_CONSENT);
    private static boolean storeConsent = preserveConsent == null ? false : Boolean.parseBoolean(preserveConsent);

    /**
     * Get consent cache instance.
     * @return consent cache instance
     */
    public static IdentityCache getInstance() {
        if (consentCache == null) {
            synchronized (ConsentCache.class) {
                if (consentCache == null) {
                    consentCache = new IdentityCache();
                }
            }
        }

        return consentCache;
    }

    /**
     * Add consent data to consent data cache.
     * @param sessionDataKey session data key
     * @param consentData consent data
     */
    public static void addConsentDataToCache(String sessionDataKey, ConsentData consentData)
            throws ConsentManagementException {

        ConsentCache.getInstance().addToCache(IdentityCacheKey.of(sessionDataKey),
                consentData);

        storeConsent(consentData, sessionDataKey);
    }

    /**
     * Add consent data to database.
     * @param sessionDataKey session data key
     * @param consentData consent data
     */
    public static void storeConsent(ConsentData consentData, String sessionDataKey) throws ConsentManagementException {

        Gson gson = new Gson();
        if (storeConsent) {
            String consent = gson.toJson(consentData);
            Map<String, String> authorizeData = new HashMap<>();
            authorizeData.put(consentData.getSessionDataKey(), consent);
            if (consentCoreService.getConsentAttributesByName(sessionDataKey).isEmpty()) {
                consentCoreService.storeConsentAttributes(consentData.getConsentId(), authorizeData);
            }
        }
    }

    /**
     * Get new session data cache key using session data key.
     * @param sessionDataKey Session data key
     * @return session data cache key
     */
    public static SessionDataCacheKey getCacheKey(String sessionDataKey) {

        return new SessionDataCacheKey(sessionDataKey);
    }

    /**
     * Get session data cache entry by session data cache key.
     * @param cacheKey session data cache key
     * @return Session data cache entry
     */
    public static SessionDataCacheEntry getCacheEntryFromCacheKey(SessionDataCacheKey cacheKey) {

        return SessionDataCache.getInstance().getValueFromCache(cacheKey);
    }

    /**
     * Get Consent data from the consent cache.
     * @param sessionDataKey Session data key
     * @return consent data
     */
    public static ConsentData getConsentDataFromCache(String sessionDataKey) {

        ConsentData consentData = (ConsentData) ConsentCache.getInstance()
                .getFromCache(IdentityCacheKey.of(sessionDataKey));
        if (consentData == null) {
            if (storeConsent) {
                Map<String, String> consentDetailsMap =
                        null;
                try {
                    consentDetailsMap = consentCoreService.getConsentAttributesByName(sessionDataKey);
                    if (consentDetailsMap.isEmpty()) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
                    }
                    Set<String> keys = consentDetailsMap.keySet();
                    String consentId = new ArrayList<>(keys).get(0);
                    JsonObject consentDetails = new JsonParser()
                            .parse(consentDetailsMap.get(consentId)).getAsJsonObject();
                    consentData = ConsentExtensionUtils.getConsentDataFromAttributes(consentDetails, sessionDataKey);

                    if (consentDetailsMap.isEmpty()) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
                    }
                    // remove all session data related to the consent from consent attributes
                    ArrayList<String> keysToDelete = new ArrayList<>();

                    Map<String, String> consentAttributes = consentCoreService.
                            getConsentAttributes(consentData.getConsentId()).getConsentAttributes();

                    consentAttributes.forEach((key, value) -> {
                        if (JSONValue.isValidJson(value) && value.contains(ConsentMgtDAOConstants.SESSION_DATA_KEY)) {
                            keysToDelete.add(key);
                        }
                    });
                    consentCoreService.deleteConsentAttributes(consentData.getConsentId(), keysToDelete);
                } catch (ConsentManagementException | URISyntaxException e) {
                    log.error("Error while retrieving consent data from cache", e);
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
                }
            } else {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
            }
        }
        return consentData;
    }

    /**
     * Get Cache Entry by Session Data Key.
     * @param sessionDataKey Session Data Key
     * @return Session data cache entry
     */
    public static SessionDataCacheEntry getCacheEntryFromSessionDataKey(String sessionDataKey) {

        return ConsentCache.getCacheEntryFromCacheKey(ConsentCache.getCacheKey(sessionDataKey));
    }
}
