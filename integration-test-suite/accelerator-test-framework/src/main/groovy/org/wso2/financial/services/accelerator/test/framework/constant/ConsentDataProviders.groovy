/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.framework.constant

import org.testng.annotations.DataProvider

class ConsentDataProviders {

    @DataProvider(name = "ConsentTypes")
    Iterator<Object[]> consentTypes() {

        Collection<Object[]> consentTypesCollection = new ArrayList<Object[]>()

        List<Map<String, Object>> listOfParamMaps = new ArrayList<Map<String, String>>()
        Map<String, Object> accountParameterMap = new HashMap<String, String>()
        Map<String, Object> cofParameterMap = new HashMap<String, String>()
        Map<String, Object> paymentsParameterMap = new HashMap<String, String>()

        accountParameterMap.put("initiationPayload", RequestPayloads.initiationPayload)
        accountParameterMap.put("initiationPath", AcceleratorTestConstants.ACCOUNT_CONSENT_PATH)
        accountParameterMap.put("consentType", AcceleratorTestConstants.ACCOUNTS_TYPE)
        accountParameterMap.put("submissionPath", AcceleratorTestConstants.ACCOUNT_SUBMISSION_PATH)

//        cofParameterMap.put("initiationPayload", RequestPayloads.cofInitiationPayload)
//        cofParameterMap.put("initiationPath", ConnectorTestConstants.COF_CONSENT_PATH)
//        cofParameterMap.put("consentType", ConnectorTestConstants.COF_TYPE)
//        cofParameterMap.put("submissionPath", ConnectorTestConstants.COF_SUBMISSION_PATH)
//
//        paymentsParameterMap.put("initiationPayload", RequestPayloads.initiationPaymentPayload)
//        paymentsParameterMap.put("initiationPath", ConnectorTestConstants.PAYMENT_CONSENT_PATH)
//        paymentsParameterMap.put("consentType", ConnectorTestConstants.PAYMENTS_TYPE)
//        paymentsParameterMap.put("submissionPath", ConnectorTestConstants.PAYMENT_SUBMISSION_PATH)

        listOfParamMaps.add(accountParameterMap)
//        listOfParamMaps.add(cofParameterMap)
//        listOfParamMaps.add(paymentsParameterMap)

        for (Map<String, Object> map : listOfParamMaps) {
            consentTypesCollection.add([map] as Object[])
        }
        return consentTypesCollection.iterator()
    }
}
