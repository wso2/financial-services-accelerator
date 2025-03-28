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

package org.wso2.financial.services.accelerator.is.test.event.notifications.EventCreationTest

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.AbstractEventNotificationFlow
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationConstants
import org.wso2.financial.services.accelerator.is.test.event.notifications.utils.EventNotificationPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Event Creation Request Validation Test
 */
class EventCreationRequestValidationTest extends AbstractEventNotificationFlow {

    @BeforeClass(alwaysRun = true)
    void setUp() {

        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = EventNotificationPayloads.accountInitiationPayload
        doDefaultInitiation()

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
        resourceID = consentId
    }

    @Test
    void "Initial Event creation request without client Id"() {

        eventCreationPayload = EventNotificationPayloads.eventCreationRequestPayload(resourceID)

        eventCreationResponse = requestBuilder.buildEventNotificationRequestWithoutClientID()
                .contentType(ContentType.URLENC)
                .header(EventNotificationConstants.X_WSO2_RESOURCE_ID, resourceID)
                .baseUri(configuration.getISServerUrl())
                .body(constructEventCreationPayload(eventCreationPayload))
                .post(eventCreationPath)

        Assert.assertEquals(eventCreationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(eventCreationResponse, EventNotificationConstants.ERROR),
                EventNotificationConstants.MISSING_REQUEST_HEADER)
        Assert.assertEquals(TestUtil.parseResponseBody(eventCreationResponse, EventNotificationConstants.ERROR_DESCRIPTION),
                "Missing header x-wso2-client-id")
    }

    @Test
    void "Initial Event creation request without authrization header"() {

        eventCreationPayload = EventNotificationPayloads.eventCreationRequestPayload(resourceID)

        eventCreationResponse = requestBuilder.buildEventNotificationRequestWithoutAuthHeader()
                .contentType(ContentType.URLENC)
                .header(EventNotificationConstants.X_WSO2_RESOURCE_ID, resourceID)
                .baseUri(configuration.getISServerUrl())
                .body(constructEventCreationPayload(eventCreationPayload))
                .post(eventCreationPath)

        Assert.assertEquals(eventCreationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Initial Event creation request with invalid authorization header"() {

        eventCreationPayload = EventNotificationPayloads.eventCreationRequestPayload(resourceID)

        eventCreationResponse = requestBuilder.buildEventNotificationRequestWithInvalidAuthHeader()
                .contentType(ContentType.URLENC)
                .header(EventNotificationConstants.X_WSO2_RESOURCE_ID, resourceID)
                .baseUri(configuration.getISServerUrl())
                .body(constructEventCreationPayload(eventCreationPayload))
                .post(eventCreationPath)

        Assert.assertEquals(eventCreationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Initial Event creation request without resource ID header"() {

        eventCreationPayload = EventNotificationPayloads.eventCreationRequestPayload(resourceID)

        eventCreationResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.URLENC)
                .baseUri(configuration.getISServerUrl())
                .body(constructEventCreationPayload(eventCreationPayload))
                .post(eventCreationPath)

        Assert.assertEquals(eventCreationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(eventCreationResponse, EventNotificationConstants.ERROR),
                EventNotificationConstants.MISSING_REQUEST_HEADER)
        Assert.assertEquals(TestUtil.parseResponseBody(eventCreationResponse, EventNotificationConstants.ERROR_DESCRIPTION),
                "Missing header x-wso2-resource-id")
    }

    @Test
    void "Initial Event creation request without payload"() {

        eventCreationResponse = requestBuilder.buildEventNotificationRequest()
                .contentType(ContentType.URLENC)
                .header(EventNotificationConstants.X_WSO2_RESOURCE_ID, resourceID)
                .baseUri(configuration.getISServerUrl())
                .post(eventCreationPath)

        Assert.assertEquals(eventCreationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(eventCreationResponse, EventNotificationConstants.ERROR),
                EventNotificationConstants.MISSING_REQUEST_PAYLOAD)
        Assert.assertEquals(TestUtil.parseResponseBody(eventCreationResponse, EventNotificationConstants.ERROR_DESCRIPTION),
                "No request payload found")
    }
}
