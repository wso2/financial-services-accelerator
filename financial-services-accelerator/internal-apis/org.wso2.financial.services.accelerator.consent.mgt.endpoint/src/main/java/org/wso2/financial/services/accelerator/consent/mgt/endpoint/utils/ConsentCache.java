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

package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.financial.services.accelerator.common.caching.FinancialServicesBaseCache;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Cache definition to store objects in consent management component implementations.
 */
public class ConsentCache extends FinancialServicesBaseCache<ConsentCacheKey, Object> {

    private static final Log log = LogFactory.getLog(ConsentCache.class);

    private static final String cacheName = "CONSENT_CACHE";
    private static volatile ConsentCache consentCache;

    private final Integer accessExpiryMinutes;
    private final Integer modifiedExpiryMinutes;

    private static final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();

    /**
     * Initialize with unique cache name.
     */
    public ConsentCache() {

        super(cacheName);
        this.accessExpiryMinutes = setAccessExpiryMinutes();
        this.modifiedExpiryMinutes = setModifiedExpiryMinutes();
    }

    /**
     * Get consent cache instance.
     * @return consent cache instance
     */
    public static ConsentCache getInstance() {

        if (consentCache == null) {
            synchronized (ConsentCache.class) {
                if (consentCache == null) {
                    consentCache = new ConsentCache();
                }
            }
        }
        return consentCache;
    }

    /**
     * Add consent data to consent data cache.
     * @param sessionDataKey session data key
     * @param consentData consent data
     * @throws ConsentManagementException if an error occurs while adding consent data to cache
     */
    public static void addConsentDataToCache(String sessionDataKey, ConsentData consentData)
            throws ConsentManagementException {

        ConsentCache.getInstance().addToCache(ConsentCacheKey.of(sessionDataKey), consentData);
        storeConsent(consentData, sessionDataKey);
    }

    /**
     * Add consent data to database.
     * @param sessionDataKey session data key
     * @param consentData consent data
     * @throws ConsentManagementException if an error occurs while storing consent data
     */
    public static void storeConsent(ConsentData consentData, String sessionDataKey) throws ConsentManagementException {

        Gson gson = new Gson();
        if (ConsentConstants.STORE_CONSENT) {
            String consent = gson.toJson(consentData);
            Map<String, String> authorizeData = new HashMap<>();
            authorizeData.put(consentData.getSessionDataKey(), consent);
            if (consentCoreService.getConsentAttributesByName(sessionDataKey).isEmpty()) {
                consentCoreService.storeConsentAttributes(consentData.getConsentId(), authorizeData);
            }
        }
    }

    /**
     * Get Cache Entry by Session Data Key.
     * @param sessionDataKey Session Data Key
     * @return Session data cache entry
     */
    public static SessionDataCacheEntry getCacheEntryFromSessionDataKey(String sessionDataKey) {

        return ConsentCache.getCacheEntryFromCacheKey(ConsentCache.getCacheKey(sessionDataKey));
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
     * Get new session data cache key using session data key.
     * @param sessionDataKey Session data key
     * @return session data cache key
     */
    public static SessionDataCacheKey getCacheKey(String sessionDataKey) {

        return new SessionDataCacheKey(sessionDataKey);
    }

    /**
     * Get Consent data from the consent cache.
     * @param sessionDataKey Session data key
     * @return consent data
     */
    public static ConsentData getConsentDataFromCache(String sessionDataKey) {

        ConsentData consentData = (ConsentData) ConsentCache.getInstance()
                .getFromCache(ConsentCacheKey.of(sessionDataKey));
        if (consentData == null) {
            if (ConsentConstants.STORE_CONSENT) {
                Map<String, String> consentDetailsMap = null;
                try {
                    consentDetailsMap = consentCoreService.getConsentAttributesByName(sessionDataKey);
                    if (consentDetailsMap.isEmpty()) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
                    }
                    Set<String> keys = consentDetailsMap.keySet();
                    String consentId = new ArrayList<>(keys).get(0);
                    JsonObject consentDetails = JsonParser.parseString(consentDetailsMap.get(consentId))
                            .getAsJsonObject();
                    consentData = ConsentUtils.getConsentDataFromAttributes(consentDetails, sessionDataKey);

                    if (consentDetailsMap.isEmpty()) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
                    }
                    // remove all session data related to the consent from consent attributes
                    ArrayList<String> keysToDelete = new ArrayList<>();

                    Map<String, String> consentAttributes = consentCoreService.
                            getConsentAttributes(consentData.getConsentId()).getConsentAttributes();

                    consentAttributes.forEach((key, value) -> {
                        if (ConsentUtils.isValidJson(value) &&
                                value.contains(ConsentExtensionConstants.SESSION_DATA_KEY)) {
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

    @Override
    public int getCacheAccessExpiryMinutes() {
        return accessExpiryMinutes;
    }

    @Override
    public int getCacheModifiedExpiryMinutes() {
        return modifiedExpiryMinutes;
    }

    public int setAccessExpiryMinutes() {

        return FinancialServicesConfigParser.getInstance().getCommonCacheAccessExpiryTime();
    }

    public int setModifiedExpiryMinutes() {

        return FinancialServicesConfigParser.getInstance().getCommonCacheModifiedExpiryTime();
    }
}
