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
    void "TC0301016_Confirmation of Funds Initiation Without Payload"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.emptyInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Permissions are not in correct format")
    }

    @Test
    void "TC0301017_Confirmation of Funds Initiation With Empty Json Payload"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.emptyJsonInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Object has missing required properties ([\"Data\",\"Risk\"])"))
    }

    @Test
    void "TC0301018_Confirmation of Funds Initiation With Empty String Payload"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.emptyStringInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Instance type (string) does not match any allowed primitive type (allowed: [\"object\"])"))
    }

    @Test
    void "TC0301019_Funds Confirmation Initiation with an invalid date"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.cofInitiationPayloadWithInvalidDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z]"))
    }

    @Test
    void "TC0301020_Funds Confirmation  Initiation without date"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.cofInitiationPayloadWithoutDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString())
    }

    @Test
    void "TC0301021_Funds Confirmation Initiation with a null value for SchemeName"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.initiationPayloadWithNullSchemeName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "TC0301022_Funds Confirmation Initiation with invalid SchemeName"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.initiationPayloadWithInvalidSchemeName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "TC0301023_Funds Confirmation Initiation without identification "() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.initiationPayloadWithoutIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                        .contains("Object has missing required properties ([\"Identification\"])"))
    }

    @Test
    void "TC0301024_Funds Confirmation Initiation without Name "() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.initiationPayloadWithoutName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString())
    }

    @Test
    void "TC0301025_Funds Confirmation Initiation with invalid identification"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.initiationPayloadWithInvalidIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("too long (length: 264, maximum allowed: 256)"))
    }

    @Test
    void "TC0301026_Funds Confirmation Initiation with invalid name "() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.initiationPayloadWithInvalidName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("maximum allowed: 350"))
    }

    @Test
    void "TC0301027_Funds Confirmation Initiation with invalid secondary identification"() {

        consentResponse = doDefaultInitiation(CofRequestPayloads.initiationPayloadWithInvalidSecondaryIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("too long (length: 56, maximum allowed: 34)"))
    }

    @Test
    void "TC0301013_Funds Confirmation Initiation With Past Expiration Date Time"(){

        consentResponse = doDefaultInitiation(CofRequestPayloads.initiationPayloadWithPastExp)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("The ExpirationDateTime value has to be a future date."))
    }
}
