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

package org.wso2.financial.services.accelerator.consent.mgt.service.internal;

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
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementRuntimeException;
import org.wso2.financial.services.accelerator.common.persistence.JDBCPersistenceManager;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.sql.SQLException;

/**
 * Consent Management Core Service Component.
 */
@Component(name = "org.wso2.financial.services.accelerator.consent.mgt.service.internal.ConsentMgtServiceComponent",
        immediate = true)
public class ConsentMgtServiceComponent {

    private static final Log log = LogFactory.getLog(ConsentMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        ConsentCoreService consentCoreService = new ConsentCoreServiceImpl();

        // Verify BFSI consent database connection when the server starts up
        try {
            boolean isConnectionActive = JDBCPersistenceManager.getInstance().getDBConnection()
                    .isValid(FinancialServicesConfigParser.getInstance().getConnectionVerificationTimeout());
            if (!isConnectionActive) {
                log.error("The connection is not active");
                throw new ConsentManagementRuntimeException("The connection is not active");
            }
        } catch (SQLException e) {
            log.error("Database connection is not active, cannot proceed");
            throw new ConsentManagementRuntimeException("Database connection is not active, cannot proceed");
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

        ConsentMgtDataHolder.getInstance().setOAuth2Service(oAuth2Service);
        log.debug("OAuth2Service is activated");
    }

    protected void unsetOAuth2Service(OAuth2Service oAuth2Service) {

        ConsentMgtDataHolder.getInstance().setOAuth2Service(oAuth2Service);
    }
}
