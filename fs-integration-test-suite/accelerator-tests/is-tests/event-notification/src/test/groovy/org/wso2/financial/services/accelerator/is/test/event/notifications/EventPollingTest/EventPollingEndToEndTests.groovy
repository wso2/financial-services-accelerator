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

package org.wso2.financial.services.accelerator.is.test.event.notifications.EventPollingTest

import com.wso2.openbanking.test.framework.utility.OBTestUtil
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.event.notifications.utils.AbstractEventNotificationFlow
import org.wso2.financial.services.accelerator.test.event.notifications.utils.EventNotificationConstants
import org.wso2.financial.services.accelerator.test.event.notifications.utils.EventNotificationPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads

/**
 * Aggregated Polling Flow SCA Tests.
 */
class EventPollingEndToEndTests extends AbstractEventNotificationFlow {

    @Test(groups = "SmokeTest", invocationCount = 6)
    void "Create events before polling"() {

        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
        doDefaultInitiation()

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
        resourceID = consentId

        eventCreationPayload = EventNotificationPayloads.eventCreationRequestPayload(resourceID)

        doDefaultEventCreation()

        Assert.assertEquals(eventCreationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(OBTestUtil.parseResponseBody(eventCreationResponse,
                EventNotificationConstants.NOTIFICATION_ID))

    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Create events before polling")
    void "Initial Event polling request with valid inputs"() {

        pollingPayload = EventNotificationPayloads.initialEventPollingRequestPayload

        doDefaultEventPolling()

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(OBTestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.SETS))
        Assert.assertNotNull(OBTestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.MORE_AVAILABLE))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "Initial Event polling request with valid inputs")
    void "Acknowledgement and poll for events Event polling request with valid inputs"() {

        pollingPayload = EventNotificationPayloads.getAcknowledgementAndPollEventPollingRequestPayload(pollingResponse)

        doDefaultEventPolling()

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(OBTestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.SETS))
        Assert.assertNotNull(OBTestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.MORE_AVAILABLE))
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "Acknowledgement and poll for events Event polling request with valid inputs")
    void "Acknowledgement Only Event polling request with valid inputs"() {

        pollingPayload = EventNotificationPayloads.getAcknowledgementOnlyEventPollingRequestPayload(pollingResponse)

        doDefaultEventPolling()

        Assert.assertEquals(pollingResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(OBTestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.SETS))
        Assert.assertNotNull(OBTestUtil.parseResponseBody(pollingResponse, EventNotificationConstants.MORE_AVAILABLE))
    }

}
