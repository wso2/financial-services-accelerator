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

package com.wso2.openbanking.accelerator.gateway.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.gateway.executor.service.TPPValidationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Holder For Gateway Component.
 **/
public class TPPCertValidatorDataHolder {

    private static final Log log = LogFactory.getLog(TPPCertValidatorDataHolder.class);

    private static TPPCertValidatorDataHolder instance = null;

    private int tppValidationCacheExpiry;
    private int tppCertRevocationCacheExpiry;
    private int certificateRevocationProxyPort;
    private int certificateRevocationValidationRetryCount;
    private int connectTimeout;
    private int connectionRequestTimeout;
    private int socketTimeout;

    private boolean psd2RoleValidationEnabled;
    private boolean certificateRevocationProxyEnabled;
    private boolean transportCertIssuerValidationEnabled;
    private boolean certificateRevocationValidationEnabled;

    private String tppValidationServiceImpl;
    private String certificateRevocationProxyHost;

    private List<String> revocationValidationExcludedIssuersList;

    private TPPValidationService tppValidationService;
    private OpenBankingConfigurationService openBankingConfigurationService;
    private APIManagerConfigurationService apiManagerConfigurationService;

    private TPPCertValidatorDataHolder() {
        // Disable direct object creation
    }

    public static synchronized TPPCertValidatorDataHolder getInstance() {
        if (instance == null) {
            instance = new TPPCertValidatorDataHolder();
        }
        return instance;
    }

    public TPPValidationService getTppValidationService() {
        return tppValidationService;
    }

    public void setTppValidationService() {
        if (isTppValidationEnabled()) {

            String tppValidationServiceImplClass = getTPPValidationServiceImpl();
            if (StringUtils.isNotBlank(tppValidationServiceImplClass)) {
                try {
                    this.tppValidationService = (TPPValidationService) Class.forName(tppValidationServiceImplClass)
                            .newInstance();
                } catch (ClassNotFoundException e) {
                    log.error("Unable to find the TPP validation service class " +
                            "implementation", e);
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("Error occurred while loading the TPP validation " +
                            "service class implementation", e);
                }
            } else {
                log.error("TPP validation service class implementation cannot be empty");
            }
        }
    }

    /**
     * Certificate revocation cache expiry time has been configured in the open-banking.xml.
     * Default value is 3600 seconds.
     */
    public int getTppCertRevocationCacheExpiry() {
        return this.tppCertRevocationCacheExpiry;
    }

    public void setTppCertRevocationCacheExpiry() {
        try {
            Object clientCertificateCacheExpiry = this.openBankingConfigurationService.
                    getConfigurations().get(OpenBankingConstants.CLIENT_CERTIFICATE_CACHE_EXPIRY);
            if (clientCertificateCacheExpiry != null) {
                this.tppCertRevocationCacheExpiry = Integer.parseInt((String) clientCertificateCacheExpiry);
            } else {
                this.tppCertRevocationCacheExpiry = 3600;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the client certificate cache expiry value " +
                    "in open-banking.xml. caused by, " + e.getMessage());
        }
    }

    /**
     * Tpp validation cache expiry time has been configured in the open-banking.xml.
     * Default value is 3600 seconds.
     */
    public int getTppValidationCacheExpiry() {
        return this.tppValidationCacheExpiry;
    }

    public void setTppValidationCacheExpiry() {
        try {
            Object clientCertificateCacheExpiry = this.openBankingConfigurationService.
                    getConfigurations().get(OpenBankingConstants.TPP_VALIDATION_CACHE_EXPIRY);
            if (clientCertificateCacheExpiry != null) {
                this.tppValidationCacheExpiry = Integer.parseInt((String) clientCertificateCacheExpiry);
            } else {
                this.tppValidationCacheExpiry = 3600;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the tpp validation cache expiry value " +
                    "in open-banking.xml. caused by, " + e.getMessage());
        }
    }

    /**
     * Check if the certificate revocation validation is enabled.
     *
     * @return true if the certificate validation is enabled. Default value has been sent to true.
     */
    public boolean isCertificateRevocationValidationEnabled() {
        return this.certificateRevocationValidationEnabled;
    }

    public void setCertificateRevocationValidationEnabled() {

        Object isCertificateRevocationEnabled = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_ENABLED);
        if (isCertificateRevocationEnabled != null) {
            this.certificateRevocationValidationEnabled = Boolean.parseBoolean((String) isCertificateRevocationEnabled);
        } else {
            this.certificateRevocationValidationEnabled = true;
        }
    }

    public void setCertificateRevocationValidationExcludedIssuers() {
        Object revocationValidationExcludedIssuers = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_EXCLUDED_ISSUERS);

        this.revocationValidationExcludedIssuersList = new ArrayList<>();
        if (revocationValidationExcludedIssuers instanceof ArrayList) {
            revocationValidationExcludedIssuersList.addAll((ArrayList) revocationValidationExcludedIssuers);
        } else if (revocationValidationExcludedIssuers instanceof String) {
            revocationValidationExcludedIssuersList.add((String) revocationValidationExcludedIssuers);
        }
    }

    /**
     * Get the certificate issuers whose issued certificates are excluded from revocation validation.
     *
     * @return Returns a list of certificate issuers whose issued certificates are excluded from revocation validation
     */
    public List<String> getCertificateRevocationValidationExcludedIssuers() {
        return this.revocationValidationExcludedIssuersList;

    }

    /**
     * Validate the TPP using external service implementation.
     *
     * @return Default value has been set to false
     */
    public boolean isTppValidationEnabled() {
        Object isTppValidationEnabled = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.TPP_VALIDATION_ENABLED);
        if (isTppValidationEnabled != null) {
            return Boolean.parseBoolean((String) isTppValidationEnabled);
        } else {
            return false;
        }
    }

    public void setCertificateRevocationValidationRetryCount() {
        try {
            Object certificateRevocationValidationRetryCountObj = this.openBankingConfigurationService.
                    getConfigurations().get(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_RETRY_COUNT);
            if (certificateRevocationValidationRetryCountObj != null) {
                this.certificateRevocationValidationRetryCount =
                        Integer.parseInt((String) certificateRevocationValidationRetryCountObj);
            } else {
                this.certificateRevocationValidationRetryCount = 3;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the certificate revocation validation" +
                    " retry count in open-banking.xml. " + e.getMessage());
        }
    }

    /**
     * Get the certificate revocation validation retry count for CRL and OCSP validations.
     *
     * @return returns the certificate revocation validation retry count for CRL and OCSP validations.
     * Default value has been set to 3.
     */
    public int getCertificateRevocationValidationRetryCount() {
        return this.certificateRevocationValidationRetryCount;
    }

    /**
     * Get the certificate revocation validation connectTimeout for CRL and OCSP validations.
     *
     * @return returns the certificate revocation validation connectTimeout for CRL and OCSP validations.
     */
    public int getConnectTimeout() {

        return connectTimeout;
    }

    /**
     * Set the certificate revocation validation connectTimeout for CRL and OCSP validations.
     *
     */
    public void setConnectTimeout() {
        try {
            Object certValidationConnectTimeout = this.openBankingConfigurationService.
                    getConfigurations().get(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_CONNECT_TIMEOUT);
            if (certValidationConnectTimeout != null) {
                this.connectTimeout = Integer.parseInt((String) certValidationConnectTimeout);
            } else {
                // Default value has been set to 10000 milliseconds.
                this.connectTimeout = 10000;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the connectTimeout value " +
                    "in open-banking.xml. caused by, " + e.getMessage());
        }
    }

    /**
     * Get the certificate revocation validation connectionRequestTimeout for CRL and OCSP validations.
     *
     * @return returns the certificate revocation validation connectionRequestTimeout for CRL and OCSP validations.
     */
    public int getConnectionRequestTimeout() {

        return connectionRequestTimeout;
    }

    /**
     * Set the certificate revocation validation connectionRequestTimeout for CRL and OCSP validations.
     *
     */
    public void setConnectionRequestTimeout() {

        try {
            Object certValidationConnectionRequestTimeout = this.openBankingConfigurationService.getConfigurations()
                    .get(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_CONNECTION_REQUEST_TIMEOUT);
            if (certValidationConnectionRequestTimeout != null) {
                this.connectionRequestTimeout = Integer.parseInt((String) certValidationConnectionRequestTimeout);
            } else {
                // Default value has been set to 10000 milliseconds.
                this.connectionRequestTimeout = 10000;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the connection request timeout value " +
                    "in open-banking.xml. caused by, " + e.getMessage());
        }
    }

    /**
     * Get the certificate revocation validation socketTimeout for CRL and OCSP validations.
     *
     * @return returns the certificate revocation validation socketTimeout for CRL and OCSP validations.
     */
    public int getSocketTimeout() {

        return socketTimeout;
    }

    /**
     * Set the certificate revocation validation socketTimeout for CRL and OCSP validations.
     *
     */
    public void setSocketTimeout() {

        try {
            Object certValidationSocketTimeout = this.openBankingConfigurationService.getConfigurations()
                    .get(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_SOCKET_TIMEOUT);
            if (certValidationSocketTimeout != null) {
                this.socketTimeout = Integer.parseInt((String) certValidationSocketTimeout);
            } else {
                // Default value has been set to 10000 milliseconds.
                this.socketTimeout = 10000;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the socket timeout value " +
                    "in open-banking.xml. caused by, " + e.getMessage());
        }
    }

    /**
     * Get the certificate revocation validation manager implementation class to validate the revocation status
     * of a certificate.
     *
     * @return class name of the certificate revocation validator implementation class
     */
    public String getTPPValidationServiceImpl() {
        return this.tppValidationServiceImpl;
    }

    public void setTPPValidationServiceImpl() {
        Object revocationValidationManagerImpl = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.TPP_VALIDATION_SERVICE_IMPL_CLASS);
        if (revocationValidationManagerImpl != null) {
            this.tppValidationServiceImpl = String.valueOf(revocationValidationManagerImpl)
                    .replaceAll("[\\n\\t ]", "");
        }
    }

    /**
     * Returns whether the certification revocation proxy is enabled.
     * <p>
     * If enabled, the certificate revocation checks will be done via the configured proxy
     *
     * @return {@code true} if certificate revocation proxy is enabled, {@code false} otherwise. The default value is
     * {@code false}
     */
    public boolean isCertificateRevocationProxyEnabled() {
        return certificateRevocationProxyEnabled;
    }

    public void setCertificateRevocationProxyEnabled() {
        Object isCertificateRevocationProxyEnabled = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.CERTIFICATE_REVOCATION_PROXY_ENABLED);
        if (isCertificateRevocationProxyEnabled != null) {
            this.certificateRevocationProxyEnabled = Boolean.parseBoolean((String) isCertificateRevocationProxyEnabled);
        } else {
            this.certificateRevocationProxyEnabled = false;
        }
    }

    /**
     * Returns the certificate revocation proxy port.
     * <p>
     * The certificate revocation checks will be done via this proxy port, if the
     * {@code CertificateRevocationProxyEnabled} value is set to {@code true}
     *
     * @return certificate revocation proxy port
     */
    public int getCertificateRevocationProxyPort() {
        return this.certificateRevocationProxyPort;
    }

    public void setCertificateRevocationProxyPort() {
        Object certificateRevocationProxyPortObj = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.CERTIFICATE_REVOCATION_PROXY_PORT);
        this.certificateRevocationProxyPort = certificateRevocationProxyPortObj != null ?
                Integer.parseInt((String) certificateRevocationProxyPortObj) : 8080;
    }

    /**
     * Returns the certificate revocation proxy host.
     * <p>
     * The certificate revocation checks will be done via this proxy host, if the
     * {@code CertificateRevocationProxyEnabled} value is set to {@code true}
     *
     * @return certificate revocation proxy host
     */
    public String getCertificateRevocationProxyHost() {
        return this.certificateRevocationProxyHost;
    }

    public void setCertificateRevocationProxyHost() {
        Object certificateRevocationProxyHostObj = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.CERTIFICATE_REVOCATION_PROXY_HOST);
        this.certificateRevocationProxyHost =
                certificateRevocationProxyHostObj != null ? ((String) certificateRevocationProxyHostObj).trim() : "";
    }

    /**
     * Validate the issuer of the client certificate when the client certificate is sent as  a header when the TLS
     * session is terminated before gateway.
     *
     * @return Default value has been set to true
     */
    public boolean isTransportCertIssuerValidationEnabled() {
        return this.transportCertIssuerValidationEnabled;
    }

    public void setTransportCertIssuerValidationEnabled() {
        Object transportCertIssuerValidationEnabledObj = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.TRANSPORT_CERT_ISSUER_VALIDATION_ENABLED);
        if (transportCertIssuerValidationEnabledObj != null) {
            this.transportCertIssuerValidationEnabled =
                    Boolean.parseBoolean((String) transportCertIssuerValidationEnabledObj);
        } else {
            this.transportCertIssuerValidationEnabled = true;
        }
    }

    /**
     * Validate the TPP PSD2 roles.
     *
     * @return Default value has been set to true
     */
    public boolean isPsd2RoleValidationEnabled() {
        return this.psd2RoleValidationEnabled;
    }

    public void setPsd2RoleValidationEnabled() {
        Object psd2RoleValidationEnabledObj = this.openBankingConfigurationService.
                getConfigurations().get(OpenBankingConstants.PSD2_ROLE_VALIDATION_ENABLED);
        if (psd2RoleValidationEnabledObj != null) {
            this.psd2RoleValidationEnabled = Boolean.parseBoolean((String) psd2RoleValidationEnabledObj);
        } else {
            this.psd2RoleValidationEnabled = true;
        }
    }

    public OpenBankingConfigurationService getOpenBankingConfigurationService() {
        return openBankingConfigurationService;
    }

    public void setOpenBankingConfigurationService(OpenBankingConfigurationService openBankingConfigurationService) {
        this.openBankingConfigurationService = openBankingConfigurationService;
    }

    public void setApiManagerConfiguration(APIManagerConfigurationService apiManagerConfigurationService) {

        this.apiManagerConfigurationService = apiManagerConfigurationService;
    }

    public APIManagerConfigurationService getApiManagerConfigurationService() {

        return apiManagerConfigurationService;
    }

    public void initializeTPPValidationDataHolder() {
        setTPPValidationServiceImpl();
        setTppValidationService();

        setTppValidationCacheExpiry();
        setPsd2RoleValidationEnabled();
        setTppCertRevocationCacheExpiry();
        setCertificateRevocationProxyHost();
        setCertificateRevocationProxyPort();
        setCertificateRevocationProxyEnabled();
        setTransportCertIssuerValidationEnabled();
        setCertificateRevocationValidationEnabled();
        setCertificateRevocationValidationRetryCount();
        setCertificateRevocationValidationExcludedIssuers();
        setConnectTimeout();
        setConnectionRequestTimeout();
        setSocketTimeout();

    }
}
