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

package com.wso2.openbanking.accelerator.consent.mgt.service.listener;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;

import java.util.Map;

/**
 * Consent state change listener interface.
 */
public interface ConsentStateChangeListener {

    /**
     * This method is used to put events to OBEventQueue related to different consent state changes.
     *
     * @param consentID consent ID
     * @param userID user ID
     * @param newConsentStatus new consent status after state change
     * @param previousConsentStatus previous consent status
     * @param reason reason for changing consent state
     * @param clientId client ID
     * @param consentDataMap consent data map holding different consent related data
     * @throws ConsentManagementException thrown if an error occurs
     */
    public void onStateChange(String consentID, String userID, String newConsentStatus,
                              String previousConsentStatus, String reason, String clientId,
                              Map<String, Object> consentDataMap) throws ConsentManagementException;
}
