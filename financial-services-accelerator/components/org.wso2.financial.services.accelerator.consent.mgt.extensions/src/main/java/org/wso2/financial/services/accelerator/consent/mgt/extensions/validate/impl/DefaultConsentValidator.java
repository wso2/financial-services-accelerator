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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.ConsentValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidateData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Consent validator default implementation.
 */
public class DefaultConsentValidator implements ConsentValidator {

    private static final Log log = LogFactory.getLog(DefaultConsentValidator.class);
    private static final String ACTIVE_MAPPING_STATUS = "active";
    private static final String CONSENT = "Consent";
    private static final String ACCOUNT_ID_LIST = "AccountIds";
    private static final String CONSENT_ID = "ConsentId";

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        List<String> filteredAccountIds = filterActiveConsentMappings(consentValidateData);
        consentValidationResult.setConsentInformation(appendConsentInformation(
                consentValidationResult.getConsentInformation(),
                consentValidateData.getComprehensiveConsent().getReceipt(),
                filteredAccountIds, consentValidateData.getConsentId()));

        consentValidationResult.setValid(true);

    }

    /**
     * Method to filter active consent mappings from consentValidateData.
     *
     * @param consentValidateData consentValidateData
     * @return List of Active AccountIds
     */
    public static List<String> filterActiveConsentMappings(ConsentValidateData consentValidateData) {
        ArrayList<ConsentMappingResource> activeMappingResources = new ArrayList<>();
        List<String> activeAccountIds = new ArrayList<>();

        consentValidateData.getComprehensiveConsent().getConsentMappingResources().stream()
                .filter(mapping -> ACTIVE_MAPPING_STATUS.equals(
                        mapping.getMappingStatus()))
                .forEach(accountMapping -> {
                    activeAccountIds.add(accountMapping.getAccountID());
                    activeMappingResources.add(accountMapping);
                });

        // remove inactive consent mapping resources from consentValidateData
        consentValidateData.getComprehensiveConsent().setConsentMappingResources(activeMappingResources);
        return activeAccountIds;
    }

    /**
     * Append Consent data for backend integration to the consent information object.
     *
     * @param consentInfo  Consent Information object
     * @param receipt      Initiation Request
     * @param accountIds   Account IDs from consent mapping
     * @param consentId    Consent Id
     * @return
     */
    public static JSONObject appendConsentInformation(JSONObject consentInfo, String receipt,
                                                             List<String> accountIds, String consentId) {

        consentInfo.put(ACCOUNT_ID_LIST, accountIds);
        consentInfo.put(CONSENT_ID, consentId);
        consentInfo.put(CONSENT, receipt);

        return consentInfo;
    }
}
