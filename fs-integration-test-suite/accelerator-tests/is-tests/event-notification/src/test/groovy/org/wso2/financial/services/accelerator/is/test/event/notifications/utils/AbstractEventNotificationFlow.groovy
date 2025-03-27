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

package org.wso2.financial.services.accelerator.is.test.event.notifications.utils

import com.wso2.openbanking.test.framework.utility.OBTestUtil
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.request_builder.EventNotificationRequestBuilder

import java.nio.charset.Charset

/**
 * Base class for Event Notification Test.
 * All common functions that directly required for test class are implemented in here.
 */
class AbstractEventNotificationFlow extends FSConnectorTest {

    String eventCreationPayload
    final String eventCreationPath = EventNotificationConstants.URL_EVENT_CREATE
    Response eventCreationResponse
    String resourceID
    String pollingPayload
    final String pollingPath = EventNotificationConstants.URL_EVENT_POLLING
    Response pollingResponse
    String subscriptionPayload
    String subscriptionUpdatePayload
    final String subscriptionPath = EventNotificationConstants.URL_EVENT_SUBSCRIPTION
    String subscriptionId
    Response subscriptionResponse
    Response subscriptionRetrievalResponse
    Response subscriptionUpdateResponse
    Response subscriptionDeletionResponse

    ConfigurationService configuration = new ConfigurationService()
    EventNotificationRequestBuilder requestBuilder = new EventNotificationRequestBuilder()

    /**
     * Event polling
     */
    void doDefaultEventCreation() {
        //initiation
        eventCreationResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.URLENC)
                .header(EventNotificationConstants.X_WSO2_RESOURCE_ID, resourceID)
                .baseUri(configuration.getISServerUrl())
                .body(constructEventCreationPayload(eventCreationPayload))
                .post(eventCreationPath)

    }

    /**
    * Event polling
    */
    void doDefaultEventPolling() {
        //initiation
        pollingResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.URLENC)
                .baseUri(configuration.getISServerUrl())
                .body(constructPollingPayload(pollingPayload))
                .post(pollingPath)
    }

    /**
     * Event Subscription Creation
     */
    void doDefaultSubscriptionCreation() {

        subscriptionResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionPayload)
                .post(subscriptionPath)

        subscriptionId = OBTestUtil.parseResponseBody(subscriptionResponse,
                EventNotificationConstants.PATH_EVENT_SUBSCRIPTION_ID)
    }

    /**
     * Event Subscription Retrieval
     */
    void doDefaultSubscriptionRetrieval() {

        subscriptionRetrievalResponse = requestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .get(subscriptionPath + "/" + subscriptionId)
    }

    /**
     * Bulk Event Subscription Retrieval
     */
    void doDefaultSubscriptionBulkRetrieval() {

        subscriptionRetrievalResponse = requestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .get(subscriptionPath)
    }

    /**
     * Bulk Event Subscription Retrieval
     */
    void doDefaultSubscriptionRetrievalByEventType() {

        subscriptionRetrievalResponse = requestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .get(subscriptionPath + EventNotificationConstants.URL_EVENT_SUBSCRIPTION_BY_EVENT_TYPE)
    }

    /**
     * Event Subscription Update
     */
    void doDefaultSubscriptionUpdate() {

        subscriptionUpdateResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(subscriptionUpdatePayload)
                .put(subscriptionPath + "/" + subscriptionId)
    }

    /**
     * Event Subscription Deletion
     */
    void doDefaultSubscriptionDeletion() {

        subscriptionDeletionResponse = requestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .delete(subscriptionPath + "/" + subscriptionId)
    }

    static String constructEventCreationPayload(String jsonPayload) {

        return "request=" + getBase64EncodedPayload(jsonPayload)
    }

    static String constructPollingPayload(String jsonPayload) {

        return "request=" + getBase64EncodedPayload(jsonPayload)
    }

    /**
     * Method to get the Base64 encoded the payload
     * @param payload  Payload to be encoded
     * @return Base64 encoded payload
     */
    static String getBase64EncodedPayload(String payload) {
        return Base64.encoder.encodeToString(payload.getBytes(Charset.defaultCharset()))
    }
}
