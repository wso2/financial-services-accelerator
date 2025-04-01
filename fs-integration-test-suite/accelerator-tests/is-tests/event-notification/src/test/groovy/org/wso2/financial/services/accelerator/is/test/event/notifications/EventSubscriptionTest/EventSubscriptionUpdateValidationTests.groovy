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
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationPayloads
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.EventNotificationRequestBuilder

/**
 * Event Subscription Update Validation Tests
 */
class EventSubscriptionUpdateValidationTests extends FSConnectorTest {

    Response subscriptionUpdateResponse

    @BeforeClass (alwaysRun = true)
    void createSubscriptionBeforeTests() {
        subscriptionPayload = EventNotificationPayloads.creationPayloadEventSubscription
        subscriptionUpdatePayload = EventNotificationPayloads.getSubscriptionUpdatePayload(subscriptionId)
        doDefaultSubscriptionCreation()

        Assert.assertEquals(subscriptionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Event Subscription update request with invalid access token"() {

//        subscriptionUpdateResponse = EventNotificationRequestBuilder.buildEventNotificationRequestWithInvalidAuthHeader()
//                .contentType(ContentType.JSON)
//                .baseUri(configuration.getISServerUrl())
//                .body(subscriptionUpdatePayload)
//                .put(subscriptionPath + "/" + subscriptionId)

        subscriptionUpdateResponse = EventNotificationRequestBuilder.buildEventNotificationRequestWithInvalidAuthHeader()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionUpdatePayload)
                .post(subscriptionPath + "/" + subscriptionId)

        Assert.assertEquals(subscriptionUpdateResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Event subscription update request without authorization header"() {

        subscriptionUpdateResponse = EventNotificationRequestBuilder.buildEventNotificationRequestWithoutAuthHeader()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionUpdatePayload)
                .put(subscriptionPath + "/" + subscriptionId)

        Assert.assertEquals(subscriptionUpdateResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Event subscription update request without client Id"() {

        subscriptionUpdateResponse = EventNotificationRequestBuilder.buildEventNotificationRequestWithoutClientID()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionUpdatePayload)
                .put(subscriptionPath + "/" + subscriptionId)

        Assert.assertEquals(subscriptionUpdateResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Event subscription update request with wrong content type"() {

        subscriptionUpdateResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.XML)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionUpdatePayload)
                .put(subscriptionPath + "/" + subscriptionId)

        Assert.assertEquals(subscriptionUpdateResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Event subscription update request without content type header"() {

        subscriptionUpdateResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionUpdatePayload)
                .put(subscriptionPath + "/" + subscriptionId)

        Assert.assertEquals(subscriptionUpdateResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }
}
