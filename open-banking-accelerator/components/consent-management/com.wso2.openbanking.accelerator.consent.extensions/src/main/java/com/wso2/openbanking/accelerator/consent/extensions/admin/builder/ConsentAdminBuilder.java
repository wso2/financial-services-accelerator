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

package com.wso2.openbanking.accelerator.consent.extensions.admin.builder;

import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminHandler;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Builder class for consent admin handler.
 */
public class ConsentAdminBuilder {

    private static final Log log = LogFactory.getLog(ConsentAdminBuilder.class);
    private ConsentAdminHandler consentAdminHandler = null;
    private static String adminBuilderConfigPath = "Consent.AdminHandler";

    public void build() {

        String handlerConfig = (String) ConsentExtensionsDataHolder.getInstance().getOpenBankingConfigurationService().
                getConfigurations().get(adminBuilderConfigPath);
        consentAdminHandler = (ConsentAdminHandler) OpenBankingUtils.getClassInstanceFromFQN(handlerConfig);

        log.debug("Admin handler loaded successfully");
    }

    public ConsentAdminHandler getConsentAdminHandler() {
        return consentAdminHandler;
    }
}
