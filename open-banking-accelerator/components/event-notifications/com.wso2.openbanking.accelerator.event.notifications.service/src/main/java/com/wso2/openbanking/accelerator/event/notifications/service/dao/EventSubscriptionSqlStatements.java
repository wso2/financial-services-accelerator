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

/**
 * SQL queries to store, retrieve, update and delete event notification subscriptions.
 */
public class EventSubscriptionSqlStatements {

    public String storeEventSubscriptionQuery() {
        return "INSERT INTO OB_NOTIFICATION_SUBSCRIPTION (SUBSCRIPTION_ID, CLIENT_ID, CALLBACK_URL, TIMESTAMP, " +
                "SPEC_VERSION, STATUS, REQUEST) VALUES (?,?,?,?,?,?,?)";
    }

    public String storeSubscribedEventTypesQuery() {
        return "INSERT INTO OB_NOTIFICATION_SUBSCRIBED_EVENTS (SUBSCRIPTION_ID, EVENT_TYPE) VALUES (?,?)";
    }

    public String getEventSubscriptionBySubscriptionIdQuery() {
        return "SELECT ns.SUBSCRIPTION_ID, ns.CLIENT_ID, ns.REQUEST, ns.CALLBACK_URL, ns.TIMESTAMP, ns.SPEC_VERSION, " +
                "ns.STATUS, nse.EVENT_TYPE FROM OB_NOTIFICATION_SUBSCRIPTION ns LEFT JOIN " +
                "OB_NOTIFICATION_SUBSCRIBED_EVENTS nse ON ns.SUBSCRIPTION_ID = nse.SUBSCRIPTION_ID WHERE " +
                "ns.SUBSCRIPTION_ID = ? AND ns.STATUS = 'CREATED'";
    }

    public String getEventSubscriptionsByClientIdQuery() {
        return "SELECT ns.SUBSCRIPTION_ID, ns.CLIENT_ID, ns.REQUEST, ns.CALLBACK_URL, ns.TIMESTAMP, ns.SPEC_VERSION, " +
                "ns.STATUS, nse.EVENT_TYPE FROM OB_NOTIFICATION_SUBSCRIPTION ns LEFT JOIN " +
                "OB_NOTIFICATION_SUBSCRIBED_EVENTS nse ON ns.SUBSCRIPTION_ID = nse.SUBSCRIPTION_ID WHERE " +
                "ns.CLIENT_ID = ? AND ns.STATUS = 'CREATED'";
    }

    public String getEventSubscriptionsByEventTypeQuery() {
        return "SELECT ns.SUBSCRIPTION_ID, ns.CLIENT_ID, ns.REQUEST, ns.CALLBACK_URL, ns.TIMESTAMP, ns.SPEC_VERSION, " +
                "ns.STATUS, nse.EVENT_TYPE FROM OB_NOTIFICATION_SUBSCRIPTION ns LEFT JOIN " +
                "OB_NOTIFICATION_SUBSCRIBED_EVENTS nse ON ns.SUBSCRIPTION_ID = nse.SUBSCRIPTION_ID WHERE " +
                "ns.SUBSCRIPTION_ID IN (SELECT ns.SUBSCRIPTION_ID FROM OB_NOTIFICATION_SUBSCRIPTION ns LEFT " +
                "JOIN OB_NOTIFICATION_SUBSCRIBED_EVENTS nse ON ns.SUBSCRIPTION_ID = nse.SUBSCRIPTION_ID WHERE " +
                "nse.EVENT_TYPE = ? AND ns.STATUS = 'CREATED')";
    }

    public String updateEventSubscriptionQuery() {
        return "UPDATE OB_NOTIFICATION_SUBSCRIPTION SET CALLBACK_URL = ?, TIMESTAMP = ?, REQUEST = ?" +
                "WHERE SUBSCRIPTION_ID = ?";
    }

    public String updateEventSubscriptionStatusQuery() {
        return "UPDATE OB_NOTIFICATION_SUBSCRIPTION SET STATUS = ? WHERE SUBSCRIPTION_ID = ? AND STATUS = 'CREATED'";
    }

    public String deleteSubscribedEventTypesQuery() {
        return "DELETE FROM OB_NOTIFICATION_SUBSCRIBED_EVENTS WHERE SUBSCRIPTION_ID = ?";
    }

}
