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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.financial.services.apim.mediation.policies.consent.enforcement.constants.ConsentEnforcementConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

/**
 * Utility class for managing the keystore and retrieving the signing key.
 */
public class KeyStoreUtils {

    private static final Log log = LogFactory.getLog(KeyStoreUtils.class);
    private static ServerConfiguration serverConfigs = ServerConfiguration.getInstance();

    private static final String keyStoreLocation = serverConfigs
            .getFirstProperty(ConsentEnforcementConstants.KEYSTORE_LOCATION_TAG);
    private static final char[] keyStorePassword = serverConfigs
            .getFirstProperty(ConsentEnforcementConstants.KEYSTORE_PASSWORD_TAG).toCharArray();
    private static final String keyAlias = serverConfigs
            .getFirstProperty(ConsentEnforcementConstants.SIGNING_ALIAS_TAG);
    private static final String keyPassword = serverConfigs
            .getFirstProperty(ConsentEnforcementConstants.SIGNING_KEY_PASSWORD);

    private static volatile Key key;


    /**
     * Method to obtain signing key.
     *
     * @return Key as an Object.
     */
    public static Key getSigningKey() {

        if (key == null) {
            synchronized (ConsentEnforcementUtils.class) {
                if (key == null) {
                    try (FileInputStream is = new FileInputStream(getKeyStoreLocation())) {
                        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                        keystore.load(is, getKeyStorePassword());
                        key = keystore.getKey(getKeyAlias(), getKeyPassword().toCharArray());
                    } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException
                             | UnrecoverableKeyException e) {
                        log.error("Error occurred while retrieving private key from keystore ", e);
                    }
                }
            }
        }
        return key;
    }

    private static char[] getKeyStorePassword() {

        return Arrays.copyOf(keyStorePassword, keyStorePassword.length);
    }

    private static String getKeyStoreLocation() {

        return keyStoreLocation;
    }

    private static String getKeyAlias() {

        return keyAlias;
    }

    private static String getKeyPassword() {

        return keyPassword;
    }

    static void setServerConfiguration(ServerConfiguration config) {

        serverConfigs = config;
    }

}
