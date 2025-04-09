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

package org.wso2.financial.services.accelerator.event.notifications.service.handler;

import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventPollingDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPollingResponse;

/**
 * EventPolling Service handler is used to validate and map the polling request to the DTO before calling the
 * polling service. For custom validations this class can be extended and the extended class
 * can be added to the deployment.toml under event_polling_handler to execute the specific class.
 */
public interface EventPollingServiceHandler {
    /**
     * This method follows the IETF Specification for SET delivery over HTTP.
     * The method supports event acknowledgment in both positive and negative.
     * Also, can be used to POLL for available OPEN notifications.
     * @param eventPollingDTO  Event polling DTO
     * @return EventPollingResponse to the polling endpoint.
     */
    EventPollingResponse pollEvents(EventPollingDTO eventPollingDTO);

}
