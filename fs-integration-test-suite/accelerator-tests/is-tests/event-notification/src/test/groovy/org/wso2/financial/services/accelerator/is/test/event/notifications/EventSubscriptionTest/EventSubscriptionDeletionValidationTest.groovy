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

package org.wso2.financial.services.accelerator.is.test.event.notifications.EventSubscriptionTest

import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.event.notifications.utils.AbstractEventNotificationFlow
import org.wso2.financial.services.accelerator.test.event.notifications.utils.EventNotificationPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants

/**
 * Event Subscription Deletion Validation Tests
 */
class EventSubscriptionDeletionValidationTest extends AbstractEventNotificationFlow {


    @Test
    void "Event Subscription deletion request with an invalid access token"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription
        doDefaultSubscriptionCreation()

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)

        subscriptionDeletionResponse = requestBuilder.buildEventNotificationRequestWithInvalidAuthHeader()
                .baseUri(configuration.getISServerUrl())
                .delete(subscriptionPath + "/" + subscriptionId)

        Assert.assertEquals(subscriptionDeletionResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Event Subscription deletion request with no authorization header"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription
        doDefaultSubscriptionCreation()

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)

        subscriptionDeletionResponse = requestBuilder.buildEventNotificationRequestWithoutAuthHeader()
                .baseUri(configuration.getISServerUrl())
                .delete(subscriptionPath + "/" + subscriptionId)

        Assert.assertEquals(subscriptionDeletionResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Event Subscription deletion request without client id"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription
        doDefaultSubscriptionCreation()

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)

        subscriptionDeletionResponse = requestBuilder.buildEventNotificationRequestWithoutClientID()
                .baseUri(configuration.getISServerUrl())
                .delete(subscriptionPath + "/" + subscriptionId)

        Assert.assertEquals(subscriptionDeletionResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }
}
