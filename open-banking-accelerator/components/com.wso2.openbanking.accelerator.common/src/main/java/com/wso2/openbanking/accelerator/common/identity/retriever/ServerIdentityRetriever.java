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

package com.wso2.openbanking.accelerator.common.identity.retriever;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.identity.IdentityConstants;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Optional;

/**
 * Utility to retrieve ASPSP certificates.
 */
public class ServerIdentityRetriever {

    private static KeyStore keyStore = null;
    // Internal KeyStore Password.
    private static char[] keyStorePassword;

    private static final Log log = LogFactory.getLog(ServerIdentityRetriever.class);

    static {
        // Static Initialize Internal Keystore.
        String keyStoreLocation = ServerConfiguration.getInstance()
                .getFirstProperty(IdentityConstants.KEYSTORE_LOCATION_CONF_KEY);
        String keyStorePassword = ServerConfiguration.getInstance()
                .getFirstProperty(IdentityConstants.KEYSTORE_PASS_CONF_KEY);

        try {
            ServerIdentityRetriever.keyStore = HTTPClientUtils.loadKeyStore(keyStoreLocation, keyStorePassword);
            ServerIdentityRetriever.keyStorePassword = keyStorePassword.toCharArray();
        } catch (OpenBankingException e) {
            log.error("Unable to load InternalKeyStore", e);
        }
    }

    /**
     * Returns the signing key using the signing Certificate.
     * @param certificateType Signing certificate
     * @param environmentType Sandbox or Production environment
     * @return Key The signing key
     * @throws OpenBankingException throws at KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException
     */
    public static Optional<Key> getPrimaryCertificate(IdentityConstants.CertificateType certificateType,
                                                      IdentityConstants.EnvironmentType environmentType)
            throws OpenBankingException {
        Optional<String> certAlias;

        if (certificateType.equals(IdentityConstants.CertificateType.SIGNING)) {

            certAlias = getCertAlias(certificateType, environmentType);

            if (certAlias.isPresent()) {
                try {
                    // The requested key, or
                    // null if the given alias does not exist or does not identify a key-related entry.
                    return Optional.of(keyStore.getKey(certAlias.get(), keyStorePassword));
                } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
                    throw new OpenBankingException("Unable to retrieve certificate", e);
                }
            }

        }
        return Optional.empty();
    }

    /**
     * Returns signing key used at production environment.
     * @param certificateType signing certificate
     * @return Key signing key
     * @throws OpenBankingException throws OpenBankingException
     */
    public static Optional<Key> getPrimaryCertificate(IdentityConstants.CertificateType certificateType)
            throws OpenBankingException {

        return getPrimaryCertificate(certificateType, IdentityConstants.EnvironmentType.PRODUCTION);
    }

    /**
     * Get certificate from keystore with the given alias.
     * Used in toolkits to get public signing certificate from keystore to retrieve the issuer.
     *
     * @param alias alias of the signing certificate of the bank
     * @return signing certificate
     * @throws KeyStoreException throw a generic KeyStore exception
     */
    public static Certificate getCertificate(String alias) throws KeyStoreException {

        return keyStore.getCertificate(alias);
    }

    /**
     * Returns Signing certificate alias at Production environment.
     * @param certificateType Signing
     * @return String Certificate alias
     * @throws OpenBankingException
     */
    public static Optional<String> getCertAlias(IdentityConstants.CertificateType certificateType)
            throws OpenBankingException {
        return getCertAlias(certificateType, IdentityConstants.EnvironmentType.PRODUCTION);
    }

    /**
     * Returns Signing certificate alias.
     * @param certificateType signing
     * @param environmentType Production or Sandbox
     * @return Signing certificate alias
     * @throws OpenBankingException throws OpenBankingException
     */
    public static Optional<String> getCertAlias(IdentityConstants.CertificateType certificateType,
                                                IdentityConstants.EnvironmentType environmentType)
            throws OpenBankingException {
        Optional<String> certAlias = Optional.empty();

        if (certificateType.equals(IdentityConstants.CertificateType.SIGNING)) {
            if (keyStore == null) {
                throw new OpenBankingException("Internal Key Store not initialized");
            }

            if (environmentType == IdentityConstants.EnvironmentType.SANDBOX) {
                certAlias = IdentityConstants.SANDBOX_SIGNING_CERT_ALIAS;
            } else {
                certAlias = IdentityConstants.PRIMARY_SIGNING_CERT_ALIAS;
            }
        }
        return certAlias;
    }
}
