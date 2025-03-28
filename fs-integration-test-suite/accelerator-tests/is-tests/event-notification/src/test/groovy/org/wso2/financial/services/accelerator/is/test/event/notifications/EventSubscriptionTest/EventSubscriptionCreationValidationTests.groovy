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

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.AbstractEventNotificationFlow
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationConstants
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Event Subscription Creation Validation Tests
 */
class EventSubscriptionCreationValidationTests extends AbstractEventNotificationFlow {

    @Test
    void "Event Subscription creation request with invalid access token"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription

        subscriptionResponse = requestBuilder.buildEventNotificationRequestWithInvalidAuthHeader()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Event subscription creation request without authorization header"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription

        subscriptionResponse = requestBuilder.buildEventNotificationRequestWithoutAuthHeader()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Event subscription creation request with wrong content type"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription

        subscriptionResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.XML)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Event subscription creation requests without content type header"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription

        subscriptionResponse = requestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Event subscription creation without client Id"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription

        subscriptionResponse = requestBuilder.buildEventNotificationRequestWithoutClientID()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Event subscription creation with no callbackUrl in the payload"() {

        subscriptionPayload = EventNotificationPayloads.subscriptionCreationPayloadWithNoCallbackUrl

        subscriptionResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)


        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }


    @Test
    void "Event subscription creation with no event type element in the payload"() {

        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscriptionWithNoEventTypes

        subscriptionResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_EVENT_TYPES))
    }

    @Test
    void "Event subscription creation without the callbackUrl parameter"() {

        subscriptionPayload = EventNotificationPayloads.subscriptionCreationPayloadWithoutCallbackUrl

        subscriptionResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_CALLBACK_URL))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_VERSION))
        Assert.assertNotNull(TestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_EVENT_TYPES))
    }

}
