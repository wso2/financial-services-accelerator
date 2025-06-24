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

package org.wso2.financial.services.accelerator.gateway.test.cof.Cof_Initiation_Tests

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.CofRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Funds Confirmation Initiation Payload Validation Tests.
 */
class InitiationPayloadValidationTests extends FSAPIMConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.COF_CONSENT_API_PATH
        initiationPayload = CofRequestPayloads.cofInitiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.COF_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Confirmation of Funds Initiation Without Payload"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.emptyInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: A request body is required but none found., "))
    }

    @Test
    void "Confirmation of Funds Initiation With Empty Json Payload"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.emptyJsonInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: Object has missing required properties ([\"Data\"]), "))
    }

    @Test
    void "Confirmation of Funds Initiation With Empty String Payload"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.emptyStringInitiationPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: Instance type (string) does not match any " +
                        "allowed primitive type (allowed: [\"object\"]), "))
    }

    @Test
    void "Funds Confirmation Initiation with an invalid date"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.cofInitiationPayloadWithInvalidDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z], "))
    }

    @Test
    void "Funds Confirmation  Initiation without date"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.cofInitiationPayloadWithoutDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString())
    }

    @Test
    void "Funds Confirmation Initiation with a null value for SchemeName"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.initiationPayloadWithNullSchemeName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Debtor Account Scheme Name should be present in the request"))
    }

    @Test
    void "Funds Confirmation Initiation with invalid SchemeName"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.initiationPayloadWithInvalidSchemeName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Debtor Account Scheme Name is not in the correct format"))
    }

    @Test
    void "Funds Confirmation Initiation without identification"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.initiationPayloadWithoutIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: [Path '/Data/DebtorAccount'] Object has missing " +
                        "required properties ([\"Identification\"]), "))
    }

    @Test
    void "Funds Confirmation Initiation without Name"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.initiationPayloadWithoutName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID).toString())
    }

    @Test
    void "Funds Confirmation Initiation with invalid identification"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.initiationPayloadWithInvalidIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: [Path '/Data/DebtorAccount/Identification'] " +
                        "String \"Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1" +
                        "Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1" +
                        "Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1" +
                        "Account1\" is too long (length: 264, maximum allowed: 256), "))
    }

    @Test
    void "Funds Confirmation Initiation with invalid name"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.initiationPayloadWithInvalidName)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("is too long (length: 557, maximum allowed: 350), "))
    }

    @Test
    void "Funds Confirmation Initiation with invalid secondary identification"() {

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.initiationPayloadWithInvalidSecondaryIdentification)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("is too long (length: 56, maximum allowed: 34), "))
    }

    @Test
    void "Funds Confirmation Initiation With Past Expiration Date Time"(){

        doDefaultCofInitiationWithUpdatedPayload(CofRequestPayloads.initiationPayloadWithPastExp)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("ExpirationDateTime should be a future date"))
    }
}
