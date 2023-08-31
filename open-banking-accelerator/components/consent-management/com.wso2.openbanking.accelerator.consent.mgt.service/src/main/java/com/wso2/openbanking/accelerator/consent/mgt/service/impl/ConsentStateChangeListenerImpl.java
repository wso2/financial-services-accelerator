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

package com.wso2.openbanking.accelerator.consent.mgt.service.impl;

import com.wso2.openbanking.accelerator.common.event.executor.OBEventQueue;
import com.wso2.openbanking.accelerator.common.event.executor.model.OBEvent;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.mgt.service.internal.ConsentManagementDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.service.listener.ConsentStateChangeListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Consent state change listener implementation.
 */
public class ConsentStateChangeListenerImpl implements ConsentStateChangeListener {

    private static volatile ConsentStateChangeListenerImpl instance;

    private ConsentStateChangeListenerImpl() {

    }

    public static ConsentStateChangeListenerImpl getInstance() {

        if (instance == null) {
            synchronized (ConsentStateChangeListenerImpl.class) {
                if (instance == null) {
                    instance = new ConsentStateChangeListenerImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void onStateChange(String consentID, String userID, String newConsentStatus, String previousConsentStatus,
                              String reason, String clientId, Map<String, Object> consentDataMap)
            throws ConsentManagementException {

        OBEventQueue obEventQueue = ConsentManagementDataHolder.getInstance().getOBEventQueue();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("ConsentId", consentID);
        eventData.put("UserId", userID);
        eventData.put("PreviousConsentStatus", previousConsentStatus);
        eventData.put("Reason", reason);
        eventData.put("ClientId", clientId);
        eventData.put("ConsentDataMap", consentDataMap);

        obEventQueue.put(new OBEvent(newConsentStatus, eventData));

    }
}
