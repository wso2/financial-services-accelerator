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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.ConsentManageHandler;

/**
 * Builder class for consent manage handler.
 */
public class ConsentManageBuilder {

    private static final Log log = LogFactory.getLog(ConsentManageBuilder.class);
    private ConsentManageHandler consentManageHandler = null;

    public ConsentManageBuilder() {
        build();
    }

    private void build() {

        String handlerConfig = (String) ConsentExtensionsDataHolder.getInstance().getConfigurationService()
                .getConfigurations().get(FinancialServicesConstants.MANAGE_HANDLER);
        consentManageHandler = ConsentExtensionUtils.getClassInstanceFromFQN(handlerConfig, ConsentManageHandler.class);

        log.debug("Manage handler loaded successfully");
    }

    public ConsentManageHandler getConsentManageHandler() {
        return consentManageHandler;
    }
}
