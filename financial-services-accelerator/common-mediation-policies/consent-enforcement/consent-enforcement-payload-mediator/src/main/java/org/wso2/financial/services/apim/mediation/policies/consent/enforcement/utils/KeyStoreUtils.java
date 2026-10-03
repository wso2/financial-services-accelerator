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

package org.wso2.financial.services.apim.mediation.policies.consent.enforcement.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;

import java.security.Key;

/**
 * Utility class for managing the keystore and retrieving the signing key.
 * Uses Carbon KeyStoreManager to retrieve the configured signing key for the tenant.
 */
public class KeyStoreUtils {

    private static final Log log = LogFactory.getLog(KeyStoreUtils.class);

    private static volatile Key key;

    /**
     * Method to obtain the signing key using Carbon KeyStoreManager.
     *
     * @return Key as an Object.
     * @throws RuntimeException if the key cannot be loaded from KeyStoreManager
     */
    public static Key getSigningKey() {

        Key localKey = key;
        if (localKey == null) {
            synchronized (KeyStoreUtils.class) {
                localKey = key;
                if (localKey == null) {
                    log.debug("Initializing signing key from KeyStoreManager");
                    try {
                        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                        localKey = keyStoreManager.getDefaultPrivateKey();
                        key = localKey;
                        log.info("Signing key loaded successfully for tenant: " + tenantId
                                + ". Key type: " + localKey.getClass().getName());
                    } catch (Exception e) {
                        log.error("Error occurred while retrieving private key from KeyStoreManager", e);
                        throw new RuntimeException("Failed to load signing key from KeyStoreManager. " +
                                "Ensure keystore is properly configured.", e);
                    }
                }
            }
        }
        return localKey;
    }

}
