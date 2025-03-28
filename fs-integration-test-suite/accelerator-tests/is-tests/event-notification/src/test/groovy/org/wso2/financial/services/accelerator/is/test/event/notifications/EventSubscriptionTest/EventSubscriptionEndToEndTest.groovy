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

package org.wso2.financial.services.accelerator.is.test.event.notifications.EventSubscriptionTest

import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.AbstractEventNotificationFlow
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationConstants
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Event Subscription Flow  Tests.
 */
class EventSubscriptionEndToEndTest extends AbstractEventNotificationFlow {

    @Test(groups = "SmokeTest")
    void "Event Subscription creation request with valid inputs"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription

        doDefaultSubscriptionCreation()

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(subscriptionId)
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_CALLBACK_URL))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_VERSION))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_EVENT_TYPES))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Event Subscription creation request with valid inputs")
    void "Event Subscription retrieval request with valid inputs"() {

        doDefaultSubscriptionRetrieval()

        Assert.assertEquals(subscriptionRetrievalResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_CALLBACK_URL))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_VERSION))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_EVENT_TYPES))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Event Subscription retrieval request with valid inputs")
    void "Retrieve All Event Subscription Requests for client Id"() {

        doDefaultSubscriptionBulkRetrieval()

        Assert.assertEquals(subscriptionRetrievalResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_CALLBACK_URL))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_VERSION))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_EVENT_TYPES))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Retrieve All Event Subscription Requests for client Id")
    void "Retrieve All Event Subscription Requests for a specific event type"() {

        doDefaultSubscriptionRetrievalByEventType()

        Assert.assertEquals(subscriptionRetrievalResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_CALLBACK_URL))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_VERSION))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_EVENT_TYPES))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Retrieve All Event Subscription Requests for a specific event type")
    void "Event Subscription update request with valid inputs"() {

        subscriptionUpdatePayload = EventNotificationPayloads.getSubscriptionUpdatePayload(subscriptionId)

        doDefaultSubscriptionUpdate()

        Assert.assertEquals(subscriptionUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        String callbackUrl = TestUtil.parseResponseBody(subscriptionUpdateResponse,
                EventNotificationConstants.PATH_CALLBACK_URL)
        Assert.assertNotNull(callbackUrl)
        Assert.assertEquals(callbackUrl, EventNotificationConstants.CALLBACK_URL_UPDATE)
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionUpdateResponse,
                EventNotificationConstants.PATH_VERSION))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionUpdateResponse,
                EventNotificationConstants.PATH_EVENT_TYPES))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Event Subscription update request with valid inputs")
    void "Event Subscription deletion request with valid inputs"() {

        doDefaultSubscriptionDeletion()

        Assert.assertEquals(subscriptionDeletionResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_204)

    }
}
