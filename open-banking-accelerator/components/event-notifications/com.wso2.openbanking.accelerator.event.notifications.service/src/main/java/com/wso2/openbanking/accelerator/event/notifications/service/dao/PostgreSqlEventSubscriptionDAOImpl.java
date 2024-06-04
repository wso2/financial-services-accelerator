/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.accelerator.event.notifications.service.dao;

import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

/**
 * Postgres SQL EventSubscriptionDAO Impl.
 */
public class PostgreSqlEventSubscriptionDAOImpl extends EventSubscriptionDAOImpl {

    private static final Log log = LogFactory.getLog(PostgreSqlEventSubscriptionDAOImpl.class);

    public PostgreSqlEventSubscriptionDAOImpl(EventSubscriptionSqlStatements sqlStatements) {
        super(sqlStatements);
    }

    @Override
    public EventSubscription storeEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws OBEventNotificationException {

        int storeSubscriptionAffectedRows;

        UUID subscriptionId = UUID.randomUUID();
        long unixTime = Instant.now().getEpochSecond();
        eventSubscription.setSubscriptionId(subscriptionId.toString());
        eventSubscription.setTimeStamp(unixTime);
        eventSubscription.setStatus(EventNotificationConstants.CREATED);

        final String sql = sqlStatements.storeEventSubscriptionQuery();
        try (PreparedStatement storeEventSubscriptionStatement = connection.prepareStatement(sql)) {
            storeEventSubscriptionStatement.setString(1, eventSubscription.getSubscriptionId());
            storeEventSubscriptionStatement.setString(2, eventSubscription.getClientId());
            storeEventSubscriptionStatement.setString(3, eventSubscription.getCallbackUrl());
            storeEventSubscriptionStatement.setLong(4, eventSubscription.getTimeStamp());
            storeEventSubscriptionStatement.setString(5, eventSubscription.getSpecVersion());
            storeEventSubscriptionStatement.setString(6, eventSubscription.getStatus());
            storeEventSubscriptionStatement.setObject(7, eventSubscription.getRequestData(),
                    java.sql.Types.OTHER);
            storeSubscriptionAffectedRows = storeEventSubscriptionStatement.executeUpdate();
            if (storeSubscriptionAffectedRows == 0) {
                log.error("Failed to store the event notification subscription.");
                throw new OBEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION);
            }
        } catch (SQLException e) {
            log.error("SQL exception when storing the event types of the subscription", e);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION);
        }
        return eventSubscription;
    }
}
