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

package org.wso2.financial.services.accelerator.is.test.consent.management.PaymentsApiTests

import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Payment Initiation Request Header Validation Tests.
 */
class InitiationRequestHeaderValidationTests extends FSConnectorTest{

    String basicHeader

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPaymentPayload
        basicHeader = getBasicAuthHeader(configuration.getUserKeyManagerAdminName(),
                configuration.getUserKeyManagerAdminPWD())
    }

    @Test
    void "Initiation Request With Authorization Code Type Access Token"() {

        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)

        doDefaultInitiationForPayments()
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        String userAccessToken = getUserAccessToken(code, scopeList)

        consentResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION),
                "AuthenticationHandler not found.")
    }

    @Test
    void "Initiation Request Without Authorization Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION),
                "AuthenticationHandler not found.")
    }

    @Test
    void "Initiation Request With Invalid Authorization Header"() {

        def authHeader = getBasicAuthHeader(configuration.getUserPSUName(),
                configuration.getUserPSUPWD())

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, authHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
        Assert.assertEquals(errorMessage, "Operation is not permitted. You do not have permissions to make this request.")
    }

    @Test
    void "Initiation Request Without Content Type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Initiation Request With Invalid Content Type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.XML)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
    }

    @Test
    void "Initiation Request With Invalid Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_406)
    }

    @Test
    void "Initiation Request With Headers with Capital Case"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID_CAPS, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID_CAPS, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Validate_Payments_Initiation_Request_with_same_payload_same_x-idempotency-key"() {

        String idempotencyKey = TestUtil.idempotency

        //initiation request 1
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        //initiation request 2
        Response consentResponse2 = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertEquals(consentResponse2.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID),
                TestUtil.parseResponseBody(consentResponse2, ConnectorTestConstants.DATA_CONSENT_ID))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS),
                TestUtil.parseResponseBody(consentResponse2, ConnectorTestConstants.DATA_STATUS))
    }

    @Test
    void "Validate_Payments_Initiation_Request_with_same_payload_different_x-idempotency-key"() {

        //initiation request 1
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        //initiation request 2
        Response consentResponse2 = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, TestUtil.idempotency)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertEquals(consentResponse2.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertNotEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID),
                TestUtil.parseResponseBody(consentResponse2, ConnectorTestConstants.DATA_CONSENT_ID))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS),
                TestUtil.parseResponseBody(consentResponse2, ConnectorTestConstants.DATA_STATUS))
    }

    @Test
    void "Validate_Payments Initiation_Request_with_duplicate_x-idempotency-key"() {

        String idempotencyKey = TestUtil.idempotency

        //initiation request 1
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)

        //initiation request 2
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(RequestPayloads.initiationPaymentPayloadModified)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                "400")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Payloads are not similar. Hence this is not a valid idempotent request")
    }

    @Test
    void "Validate_Payments_Initiation_Request_with_same_x-idempotency-key_different_clientId"() {

        String idempotencyKey = TestUtil.idempotency

        //initiation request 1
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        //initiation request 2
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_IDEMPOTENCY_KEY, idempotencyKey)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, "testClientId")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .baseUri(configuration.getISServerUrl())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                "400")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Client ID sent in the request does not match with the client ID in the retrieved consent. " +
                        "Hence this is not a valid idempotent request")
    }
}
