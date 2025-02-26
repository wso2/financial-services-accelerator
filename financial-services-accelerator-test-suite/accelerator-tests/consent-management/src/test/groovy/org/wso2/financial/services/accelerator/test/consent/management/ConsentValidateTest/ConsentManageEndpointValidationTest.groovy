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

package org.wso2.financial.services.accelerator.test.consent.management.ConsentValidateTest

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads

/**
 * Consent Manage Endpoint Validation Test.
 */
class ConsentManageEndpointValidationTest extends FSConnectorTest {

    @Test
    void "Verify Updating of a consent"() {

        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .put(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_405)
    }

    @Test
    void "Verify Patching of a created consent"() {

        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        Response response = buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .patch(consentPath + "/${consentId}")

        Assert.assertEquals(response.getStatusCode(), ConnectorTestConstants.STATUS_CODE_405)
    }

    @Test
    void "Verify File Upload Post"() {

        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPaymentPayload
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        Response response = buildKeyManagerRequest(configuration.getAppInfoClientID())
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.FILE_UPLOAD_POST + "/${consentId}")

        Assert.assertEquals(response.getStatusCode(), ConnectorTestConstants.STATUS_CODE_405)
    }

    @Test
    void "Verify File Upload GET"() {

        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPaymentPayload
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        Response response = buildKeyManagerRequest(configuration.getAppInfoClientID())
                .baseUri(configuration.getISServerUrl())
                .get(ConnectorTestConstants.FILE_UPLOAD_POST + "/${consentId}")

        Assert.assertEquals(response.getStatusCode(), ConnectorTestConstants.STATUS_CODE_405)
    }
}
