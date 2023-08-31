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

package com.wso2.openbanking.accelerator.consent.extensions.manage.builder;

import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Builder class for consent manage handler.
 */
public class ConsentManageBuilder {

    private static final Log log = LogFactory.getLog(ConsentManageBuilder.class);
    private ConsentManageHandler consentManageHandler = null;
    private static String manageHandlerConfigPath = "Consent.ManageHandler";

    public void build() {

        String handlerConfig = (String) ConsentExtensionsDataHolder.getInstance().getOpenBankingConfigurationService().
                getConfigurations().get(manageHandlerConfigPath);
        consentManageHandler = (ConsentManageHandler) OpenBankingUtils.getClassInstanceFromFQN(handlerConfig);

        log.debug("Manage handler loaded successfully");
    }

    public ConsentManageHandler getConsentManageHandler() {
        return consentManageHandler;
    }
}
