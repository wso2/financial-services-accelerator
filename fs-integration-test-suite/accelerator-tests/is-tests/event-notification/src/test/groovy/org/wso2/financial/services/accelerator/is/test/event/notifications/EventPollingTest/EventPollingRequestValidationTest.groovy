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

package org.wso2.financial.services.accelerator.is.test.event.notifications.EventPollingTest

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationConstants
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationPayloads
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.EventNotificationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Event Polling Request Validation Test
 */
class EventPollingRequestValidationTest extends FSConnectorTest {

    @BeforeClass(alwaysRun = true)
    void initializePayloadBeforeTests() {
        pollingPayload = EventNotificationPayloads.initialEventPollingRequestPayload
    }

    @Test
    void "Event Polling request with invalid access token"() {

        pollingResponse = EventNotificationRequestBuilder.buildEventNotificationRequestWithInvalidAuthHeader()
                .contentType(ContentType.URLENC)
                .baseUri(configuration.getISServerUrl())
                .body(constructPollingPayload(pollingPayload))
                .post(pollingPath)

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Event Polling request request without authorization header"() {

        pollingResponse = EventNotificationRequestBuilder.buildEventNotificationRequestWithoutAuthHeader()
                .contentType(ContentType.URLENC)
                .baseUri(configuration.getISServerUrl())
                .body(constructPollingPayload(pollingPayload))
                .post(pollingPath)

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Event Polling request request without client id"() {

        pollingResponse = EventNotificationRequestBuilder.buildEventNotificationRequestWithoutClientID()
                .contentType(ContentType.URLENC)
                .baseUri(configuration.getISServerUrl())
                .body(constructPollingPayload(pollingPayload))
                .post(pollingPath)

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Event polling request with wrong content type"() {

        pollingResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.JSON)
                .baseUri(configuration.getISServerUrl())
                .body(constructPollingPayload(pollingPayload))
                .post(pollingPath)

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Event polling request without content type header"() {

        pollingResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .baseUri(configuration.getISServerUrl())
                .body(constructPollingPayload(pollingPayload))
                .post(pollingPath)

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Event polling request Without request Payload"() {

        pollingResponse = EventNotificationRequestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.URLENC)
                .baseUri(configuration.getISServerUrl())
                .post(pollingPath)

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.ERROR),
                EventNotificationConstants.MISSING_REQUEST_PAYLOAD)
        Assert.assertEquals(TestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.ERROR_DESCRIPTION),
                "No request payload found")
    }

    @Test
    void "Event polling request With Empty request Payload"() {

        pollingPayload = ''

        doDefaultEventPolling()

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.ERROR),
                EventNotificationConstants.INVALID_REQUEST_PAYLOAD)
        Assert.assertEquals(TestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.ERROR_DESCRIPTION),
                "Request payload cannot be empty")
    }
}
