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

package org.wso2.financial.services.accelerator.test.consent.management.FundsConfirmationTests

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.CofRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Funds Confirmation Initiation Payload Validation Tests.
 */
class InitiationPayloadValidationTests extends FSConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.COF_CONSENT_PATH
    }

    @Test
    void "Confirmation of Funds Initiation Without Payload"() {

        doDefaultInitiation(CofRequestPayloads.emptyInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Payload is not in the correct format")
    }

    @Test
    void "Confirmation of Funds Initiation With Empty Json Payload"() {

        doDefaultInitiation(CofRequestPayloads.emptyJsonInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Invalid request payload"))
    }

    @Test
    void "Confirmation of Funds Initiation With Empty String Payload"() {

        doDefaultInitiation(CofRequestPayloads.emptyStringInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Payload is not in the correct format"))
    }

    @Test
    void "Funds Confirmation Initiation with an invalid date"() {

        doDefaultInitiation(CofRequestPayloads.cofInitiationPayloadWithInvalidDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "ExpirationDateTime is invalid"))
    }

    @Test
    void "Funds Confirmation  Initiation without date"() {

        doDefaultInitiation(CofRequestPayloads.cofInitiationPayloadWithoutDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString())
    }

    @Test
    void "Funds Confirmation Initiation with a null value for SchemeName"() {

        doDefaultInitiation(CofRequestPayloads.initiationPayloadWithNullSchemeName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "Funds Confirmation Initiation with invalid SchemeName"() {

        doDefaultInitiation(CofRequestPayloads.initiationPayloadWithInvalidSchemeName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "Funds Confirmation Initiation without identification "() {

        doDefaultInitiation(CofRequestPayloads.initiationPayloadWithoutIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                        .contains("Debtor Account Identification should be present in the request"))
    }

    @Test
    void "Funds Confirmation Initiation without Name "() {

        doDefaultInitiation(CofRequestPayloads.initiationPayloadWithoutName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString())
    }

    @Test
    void "Funds Confirmation Initiation with invalid identification"() {

        doDefaultInitiation(CofRequestPayloads.initiationPayloadWithInvalidIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Debtor Account Identification is not in the correct format"))
    }

    @Test
    void "Funds Confirmation Initiation with invalid name "() {

        doDefaultInitiation(CofRequestPayloads.initiationPayloadWithInvalidName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Debtor Account Name is not in the correct format"))
    }

    @Test
    void "Funds Confirmation Initiation with invalid secondary identification"() {

        doDefaultInitiation(CofRequestPayloads.initiationPayloadWithInvalidSecondaryIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Debtor Account Secondary Identification is not in the correct format"))
    }

    @Test
    void "Funds Confirmation Initiation With Past Expiration Date Time"(){

        doDefaultInitiation(CofRequestPayloads.initiationPayloadWithPastExp)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
x        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("ExpirationDateTime should be a future date"))
    }
}
