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

package com.wso2.openbanking.accelerator.consent.extensions.validate.builder;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Builder class for consent validator.
 */
public class ConsentValidateBuilder {

    private static final Log log = LogFactory.getLog(ConsentValidateBuilder.class);
    private ConsentValidator consentValidator = null;
    private String requestSignatureAlias = null;
    private static String validatorConfigPath = "Consent.Validation.Validator";
    private static String signatureAliasConfigPath = "Consent.Validation.RequestSignatureAlias";


    public void build() {

        OpenBankingConfigurationService configurationService =
                ConsentExtensionsDataHolder.getInstance().getOpenBankingConfigurationService();
        String handlerConfig = (String) configurationService.getConfigurations().get(validatorConfigPath);
        consentValidator = (ConsentValidator) OpenBankingUtils.getClassInstanceFromFQN(handlerConfig);
        requestSignatureAlias = (String) configurationService.getConfigurations().get(signatureAliasConfigPath);
        log.debug("Admin handler loaded successfully");
    }

    public ConsentValidator getConsentValidator() {
        return consentValidator;
    }

    public String getRequestSignatureAlias() {
        return requestSignatureAlias;
    }
}
