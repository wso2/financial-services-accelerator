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

package com.wso2.openbanking.sample.aggregator.identity;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.validate.impl.DefaultConsentValidator;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.sample.aggregator.identity.util.AggregatorConstants;
import net.minidev.json.JSONObject;

/**
 * Consent Validator for Aggregator.
 */
public class AggregatorConsentValidator extends DefaultConsentValidator {
    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        super.validate(consentValidateData, consentValidationResult);
        JSONObject consentInformation = new JSONObject();
        consentInformation.put(AggregatorConstants.ACCESS_TOKEN_TAG, consentValidateData.getComprehensiveConsent()
                .getConsentAttributes().get(AggregatorConstants.ACCESS_TOKEN_TAG));
        consentInformation.put(AggregatorConstants.BANK_CODE_TAG, consentValidateData.getComprehensiveConsent()
                .getConsentAttributes().get(AggregatorConstants.BANK_CODE_TAG));
        consentValidationResult.setConsentInformation(consentInformation);
    }
}
