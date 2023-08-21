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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.base.ServerConfiguration;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service Component For Gateway Component.
 **/
@Component(name = "com.wso2.openbanking.accelerator.gateway.internal.TPPCertValidatorComponent",
        immediate = true)
public class TPPCertValidatorComponent {

    private static final Log log = LogFactory.getLog(TPPCertValidatorComponent.class);
    private static final Integer SCHEDULED_INITIAL_DELAY_IN_SECONDS = 1;

    @Activate
    protected void activate(ComponentContext context) {

        Object certificateRevocationEnabled = OpenBankingConfigParser.getInstance().
                getConfiguration().get(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_ENABLED);
        final boolean isCertificateRevocationEnabled =
                certificateRevocationEnabled != null && Boolean.parseBoolean((String) certificateRevocationEnabled);

        Object transportCertIssuerValidationEnabled = OpenBankingConfigParser.getInstance().
                getConfiguration().get(OpenBankingConstants.TRANSPORT_CERT_ISSUER_VALIDATION_ENABLED);
        final boolean isTransportCertIssuerValidationEnabled = transportCertIssuerValidationEnabled != null
                && Boolean.parseBoolean((String) transportCertIssuerValidationEnabled);

        // Loading truststore
        if (isCertificateRevocationEnabled || isTransportCertIssuerValidationEnabled) {
            ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
            Runnable readTruststore = () -> {
                try {
                    CertificateValidationUtils.loadTrustStore(
                            ServerConfiguration.getInstance().getFirstProperty(CertificateValidationUtils
                                    .TRUSTSTORE_PASS_CONF_KEY).toCharArray());
                    log.info("client truststore successfully loaded into certificate validator");
                } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
                    log.error("Unable to load the client truststore", e);
                }
            };

            // Initiate the scheduled truststore loading with an interval value configured as the truststore
            // dynamic loading interval in open-banking.xml.
            ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleAtFixedRate(readTruststore,
                    SCHEDULED_INITIAL_DELAY_IN_SECONDS, OpenBankingConfigParser.getInstance()
                            .getTruststoreDynamicLoadingInterval(), TimeUnit.SECONDS);
            if (scheduledFuture.isCancelled()) {
                log.error("Error occurred while loading the client truststore into certificate validator");
            }
        }
        TPPCertValidatorDataHolder.getInstance().initializeTPPValidationDataHolder();
        log.debug("OB Gateway component is activated ");
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Client registration validation handler is deactivated");
    }

    @Reference(
            service = OpenBankingConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(OpenBankingConfigurationService openBankingConfigurationService) {
        TPPCertValidatorDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }

    public void unsetConfigService(OpenBankingConfigurationService openBankingConfigurationService) {
        TPPCertValidatorDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }

    @Reference(name = "api.manager.config.service",
            service = APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService"
    )
    protected void setAPIConfigurationService(APIManagerConfigurationService confService) {
        log.debug("API manager configuration service bound to the OB Gateway component");
        TPPCertValidatorDataHolder.getInstance().setApiManagerConfiguration(confService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        log.debug("API manager configuration service unbound from the OB Gateway component");
        TPPCertValidatorDataHolder.getInstance().setApiManagerConfiguration(null);
    }
}
