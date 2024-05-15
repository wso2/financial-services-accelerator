package com.wso2.openbanking.accelerator.identity.app2app.cache;

import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCacheKey;

public class JTICache {

    private static volatile IdentityCache consentCache;

    /**
     * Get consent cache instance.
     * @return consent cache instance
     */
    public static IdentityCache getInstance() {
        if (consentCache == null) {
            synchronized (JTICache.class) {
                if (consentCache == null) {
                    consentCache = new IdentityCache();
                }
            }
        }

        return consentCache;
    }

    public static void addJtiDataToCache(String jti) {

        JTICache.getInstance().addToCache(IdentityCacheKey.of(jti),
                jti);

    }

    public static Object getJtiDataFromCache(String jti){
        return JTICache.getInstance().getFromCache(IdentityCacheKey.of(jti));
    }
}
