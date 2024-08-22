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

package com.wso2.openbanking.accelerator.event.notifications.service.handler;

import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationCreationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventCreationResponse;

/**
 * Event creation service handler is used to map the creation request and validate the date before
 * calling the service. In need of a custom handling this class can be extended and the extended class
 * can be added to the deployment.toml under event_creation_handler to execute the specific class.
 */
public interface EventCreationServiceHandler {
    /**
     * This method is used to publish OB events in the accelerator database. The method is a generic
     * method that is used to persist data into the OB_NOTIFICATION and OB_NOTIFICATION_EVENT tables.
     * @param notificationCreationDTO Notification details DTO
     * @return For successful request the API will return a JSON with the notificationID
     */
    EventCreationResponse publishOBEvent(NotificationCreationDTO notificationCreationDTO);

}
