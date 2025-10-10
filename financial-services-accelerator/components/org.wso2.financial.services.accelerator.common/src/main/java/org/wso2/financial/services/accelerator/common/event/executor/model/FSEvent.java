/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package  org.wso2.financial.services.accelerator.common.event.executor.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Open Banking event model class.
 */
public class FSEvent {

    private String eventType;
    private Map<String, Object> eventData;
    private static final Log log = LogFactory.getLog(FSEvent.class);

    public FSEvent(String eventType, Map<String, Object> eventData) {
        if (log.isDebugEnabled()) {
            log.debug("Creating FSEvent with type: " + eventType);
        }
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public String getEventType() {

        return eventType;
    }

    public Map<String, Object> getEventData() {

        return eventData;
    }
}
