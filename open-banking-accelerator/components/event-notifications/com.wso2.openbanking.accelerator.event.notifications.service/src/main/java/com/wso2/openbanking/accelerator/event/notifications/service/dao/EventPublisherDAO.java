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

package com.wso2.openbanking.accelerator.event.notifications.service.dao;

import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;

import java.sql.Connection;
import java.util.ArrayList;

/**
 * Event Publisher DAO interface.
 */
public interface EventPublisherDAO {

    /**
     * This method is used to persist event notifications in the database.
     * @param connection
     * @param notificationDTO
     * @param eventsList
     * @return NotificationID of the saved notification.
     * @throws OBEventNotificationException
     */
    String persistEventNotification(Connection connection, NotificationDTO notificationDTO,
                                    ArrayList<NotificationEvent> eventsList) throws OBEventNotificationException;

}
