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

package com.wso2.openbanking.accelerator.consent.mgt.service.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.event.executor.OBEventQueue;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.persistence.JDBCPersistenceManager;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.oauth2.OAuth2Service;

import java.sql.SQLException;

/**
 * Consent Management Core Service Component.
 */
@Component(name = "com.wso2.openbanking.accelerator.consent.mgt.service.ConsentManagementServiceComponent",
        immediate = true)
public class ConsentManagementServiceComponent {

    private static Log log = LogFactory.getLog(ConsentManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        ConsentCoreService consentCoreService = new ConsentCoreServiceImpl();

        // Verify Open Banking consent database connection when the server starts up
        try {
            boolean isConnectionActive = JDBCPersistenceManager.getInstance().getDBConnection()
                    .isValid(OpenBankingConfigParser.getInstance().getConnectionVerificationTimeout());

            if (!isConnectionActive) {
                log.error("The connection is not active");
                throw new OpenBankingRuntimeException("The connection is not active");
            }
        } catch (SQLException e) {
            log.error("Database connection is not active, cannot proceed");
            throw new OpenBankingRuntimeException("Database connection is not active, cannot proceed");
        }

        // Verify Open Banking retention database connection when the server starts up
        if (OpenBankingConfigParser.getInstance().isConsentDataRetentionEnabled()) {
            try {
                boolean isConnectionActive = JDBCPersistenceManager.getInstance().getDBConnection().isValid(
                        OpenBankingConfigParser.getInstance().getRetentionDataSourceConnectionVerificationTimeout());

                if (!isConnectionActive) {
                    log.error("The connection is not active for retention datasource");
                    throw new OpenBankingRuntimeException("The connection is not active for retention datasource");
                }
            } catch (SQLException e) {
                log.error("Database connection is not active for retention datasource, cannot proceed");
                throw new OpenBankingRuntimeException("Database connection is not active for retention datasource, " +
                        "cannot proceed");
            }
        }

        context.getBundleContext().registerService(ConsentCoreService.class.getName(), consentCoreService, null);
        log.debug("Consent Management Service is registered successfully.");
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
         log.debug("Consent Management Service is deactivated");
    }

    @Reference(
            name = "identity.oauth.service",
            service = OAuth2Service.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuth2Service"
    )
    protected void setOAuth2Service(OAuth2Service oAuth2Service) {

        ConsentManagementDataHolder.getInstance().setOAuth2Service(oAuth2Service);
        log.debug("OAuth2Service is activated");
    }

    protected void unsetOAuth2Service(OAuth2Service oAuth2Service) {

        ConsentManagementDataHolder.getInstance().setOAuth2Service(oAuth2Service);
    }

    @Reference(
            service = OBEventQueue.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOBEventQueue"
    )

    protected void setOBEventQueue(OBEventQueue obEventQueue) {

        ConsentManagementDataHolder.getInstance().setOBEventQueue(obEventQueue);
    }

    protected void unsetOBEventQueue(OBEventQueue obEventQueue) {

        ConsentManagementDataHolder.getInstance().setOBEventQueue(null);
    }


}
