/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.policy.retrieval;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.policy.util.ConsentStepPolicyConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.policy.util.ConsentStepPolicyUtil;

import java.util.List;
import java.util.Map;

/**
 * Financial Services Consent Retrieval Step Policy for Account Retrieval.
 */
public class ConsentJsonBuildPolicy extends ConsentRetrievalStepPolicy {

    private static final String CONSENT_DATA_KEY = "consentData";
    private static final String CONSENT_DATA_CONFIG_KEY = "consent-data";
    private static final String ACCOUNTS_KEY = "accounts";
    private static final String PATH_KEY = "path";
    private static final String TITLE_KEY = "title";
    private static final String DATA_KEY = "data";

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject, Map<String, Object> propertyMap,
                        Map<String, Object> retrievalContext) throws ConsentException {

        ConsentResource consentResource = (ConsentResource) retrievalContext.get(
                ConsentStepPolicyConstants.CONSENT_RESOURCE);
        Map<String, Object> consentDataMap = (Map<String, Object>) propertyMap.get(CONSENT_DATA_CONFIG_KEY);
        List<Map<String, String>> accountsConfig = (List<Map<String, String>>) consentDataMap.get(ACCOUNTS_KEY);

        JSONArray consentDataJSON = new JSONArray();
        JSONObject consentDataObject = new JSONObject(new JSONTokener(consentResource.getReceipt()));

        // Build consentData JSON array
        for (Map<String, String> fieldConfig : accountsConfig) {
            String path = fieldConfig.get(PATH_KEY);
            String title = fieldConfig.get(TITLE_KEY);

            Object extractedData = ConsentStepPolicyUtil.getJsonValue(consentDataObject, path);

            if (extractedData != null) {
                JSONObject jsonElement = new JSONObject();
                jsonElement.put(TITLE_KEY, title);

                // If extractedData is an array, add directly, else wrap in an array
                if (extractedData instanceof JSONArray) {
                    jsonElement.put(DATA_KEY, extractedData);
                } else {
                    JSONArray dataArray = new JSONArray();
                    dataArray.put(extractedData.toString());
                    jsonElement.put(DATA_KEY, dataArray);
                }

                consentDataJSON.put(jsonElement);
            }
        }
        jsonObject.put(CONSENT_DATA_KEY, consentDataJSON);

    }
}

