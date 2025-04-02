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

package org.wso2.financial.services.accelerator.test.framework.request_builder

import io.restassured.specification.RequestSpecification
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder

import java.nio.charset.Charset

/**
 * Event Notification Request Builder.
 */
class EventNotificationRequestBuilder {

    static configurationService = new ConfigurationService()
    static authToken = "${configurationService.getUserKeyManagerAdminName()}:" +
            "${configurationService.getUserKeyManagerAdminPWD()}"
    static basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"


    /**
     * Build Request Specification for Event Notification.
     * @return
     */
    static RequestSpecification buildEventNotificationRequest() {

        return FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configurationService.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
    }

    /**
     * Build Request Specification for Event Notification Without ClientID.
     * @return
     */
    static RequestSpecification buildEventNotificationRequestWithoutClientID() {

        return FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
    }

    /**
     * Build Request Specification for Event Notification Without Auth header.
     * @return
     */
    static RequestSpecification buildEventNotificationRequestWithoutAuthHeader() {

        return FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configurationService.getAppInfoClientID())

    }

    /**
     * Build Request Specification for Event Notification with invalid Auth header.
     * @return
     */
    static RequestSpecification buildEventNotificationRequestWithInvalidAuthHeader() {

        return FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configurationService.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Basic vgqlvhqhvoycvoyvcq")
    }
}
