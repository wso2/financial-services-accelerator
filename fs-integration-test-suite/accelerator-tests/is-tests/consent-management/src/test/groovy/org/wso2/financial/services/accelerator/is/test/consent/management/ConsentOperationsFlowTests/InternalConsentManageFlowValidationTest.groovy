/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.is.test.consent.management.ConsentOperationsFlowTests

import io.restassured.http.ContentType
import io.restassured.response.Response
import org.json.JSONObject
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ConsentRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Basic Consent Update Flow Tests.
 */
class InternalConsentManageFlowValidationTest extends FSConnectorTest {

    String updatePayload
    ConsentRequestBuilder consentRequestBuilder = new ConsentRequestBuilder()
    String authIdBeforeUpdate
    String mappingIdBeforeUpdate

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        consentUpdatePath = ConnectorTestConstants.CONSENT_UPDATE_PATH

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        doConsentRetrieval(consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "Data.Status"), "AwaitingAuthorisation")

        updatePayload = AccountsRequestPayloads.getAccountConsentUpdatePayload(consentId)
    }

    @Test(priority = 1)
    void "Verify internal retrieval of a Created Consent"() {

        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "updatedTime"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "consentID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "clientID"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "status"),
                "AwaitingAuthorisation")
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "authorizationResources"))
        List<JSONObject> authorizationResources = extractListFromResponse(consentResponse, "authorizationResources")
        authIdBeforeUpdate = authorizationResources.get(0).get("authorizationID")
    }

    @Test(priority = 1, dependsOnMethods = "Verify internal retrieval of a Created Consent")
    void "Verify internal updating a Created Consent"() {

        doConsentUpdate(updatePayload, consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "updatedTime"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "consentID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "clientID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "authorizationResources"))
        List<JSONObject> authorizationResources = extractListFromResponse(consentUpdateResponse, "authorizationResources")
        String newAuthId = authorizationResources.get(0).get("authorizationID")
        Assert.assertNotEquals(newAuthId, authIdBeforeUpdate)
    }

    @Test(priority = 1, dependsOnMethods = "Verify internal updating a Created Consent")
    void "Verify internal retrieval of a Created Consent after authorization"() {

        // Authorise Consent
        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils
                .getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)

        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "updatedTime"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "consentID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "clientID"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "status"),
                "Authorised")
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "authorizationResources"))
        List<JSONObject> authorizationResources = extractListFromResponse(consentResponse, "authorizationResources")
        authIdBeforeUpdate = authorizationResources.get(0).get("authorizationID")
        List<JSONObject> resources = authorizationResources.get(0).get("resources") as List<JSONObject>
        Assert.assertNotNull(resources)
        mappingIdBeforeUpdate = resources.get(0).get("mappingID")
    }

    @Test(priority = 1, dependsOnMethods = "Verify internal retrieval of a Created Consent after authorization")
    void "Verify internal updating a Created Consent after authorization"() {

        doConsentUpdate(updatePayload, consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "updatedTime"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "consentID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "clientID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "authorizationResources"))
        List<JSONObject> authorizationResources = extractListFromResponse(consentUpdateResponse, "authorizationResources")
        String newAuthId = authorizationResources.get(0).get("authorizationID")
        Assert.assertNotEquals(newAuthId, authIdBeforeUpdate)
        List<JSONObject> resources = authorizationResources.get(0).get("resources") as List<JSONObject>
        Assert.assertNotNull(resources)
        String newMappingId = resources.get(0).get("mappingID")
        Assert.assertNotEquals(newMappingId, mappingIdBeforeUpdate)
    }

    @Test
    void "Verify internal retrieval of a Created Consent without Authorization header"() {

        consentUpdateResponse = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, "${configuration.getAppInfoClientID()}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Verify internal consent retrieval With Invalid Authorization Header"() {

        def basicHeader = getBasicAuthHeader(configuration.getUserPSUName(),
                configuration.getUserPSUPWD())

        consentUpdateResponse = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, "${configuration.getAppInfoClientID()}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_403)
    }

    @Test
    void "Verify internal consent retrieval without client ID"() {

        consentResponse = buildKeyManagerRequest("")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify internal consent retrieval with invalid client ID"() {

        consentResponse = buildKeyManagerRequest("tyionwbbvqhhwvh")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify updating a Created Consent without consent ID"() {

        consentUpdateResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .body(updatePayload)
                .baseUri(configuration.getISServerUrl())
                .put(consentUpdatePath + "/")

        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify updating a Created Consent without Authorization header"() {

        consentUpdateResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, "${configuration.getAppInfoClientID()}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .body(updatePayload)
                .put(consentUpdatePath + "/${consentId}")

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Get Accounts Initiation With Invalid Authorization Header"() {

        def basicHeader = getBasicAuthHeader(configuration.getUserPSUName(),
                configuration.getUserPSUPWD())

        consentUpdateResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, "${configuration.getAppInfoClientID()}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .baseUri(configuration.getISServerUrl())
                .body(updatePayload)
                .put(consentUpdatePath + "/${consentId}")

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_403)
    }

    @Test
    void "Verify updating a Created Consent with Incorrect request path"() {

        consentUpdateResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .body(updatePayload)
                .put(incorrectConsentPath + "/${consentId}")

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Verify updating a Created Consent with incorrect Consent ID"() {

        String incorrectConsentID = 'c1b6c5c9-1ec9-4ccf-8f68-e18df87777bfaaa'

        consentUpdateResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .body(updatePayload)
                .put(consentUpdatePath + "/${incorrectConsentID}")
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent updating without client ID"() {

        consentResponse = buildKeyManagerRequest("")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .body(updatePayload)
                .baseUri(configuration.getISServerUrl())
                .put(consentUpdatePath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Consent updating with invalid client ID"() {

        consentResponse = buildKeyManagerRequest("tyionwbbvqhhwvh")
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .body(updatePayload)
                .baseUri(configuration.getISServerUrl())
                .put(consentUpdatePath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test(priority = 2)
    void "Verify internal updating a Created Consent Status"() {

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "status"),
                "AwaitingAuthorisation")
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "authorizationResources"))
        List<JSONObject> authorizationResources = extractListFromResponse(consentResponse, "authorizationResources")
        authIdBeforeUpdate = authorizationResources.get(0).get("authorizationID")

        String updateStatusPayload = AccountsRequestPayloads.getAccountConsentStatusUpdatePayload(consentId)
        doConsentUpdate(updateStatusPayload, consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "updatedTime"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "consentID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "clientID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "status"))
         Assert.assertEquals(TestUtil.parseResponseBody(consentUpdateResponse, "status"), "Rejected")
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "authorizationResources"))
        List<JSONObject> updatedAuthResources = extractListFromResponse(consentUpdateResponse, "authorizationResources")
        String newAuthId = updatedAuthResources.get(0).get("authorizationID")
        Assert.assertEquals(newAuthId, authIdBeforeUpdate)
    }

    @Test(priority = 2)
    void "Verify internal updating a Created basic Consent"() {

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "status"),
                "AwaitingAuthorisation")
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "authorizationResources"))
        List<JSONObject> authorizationResources = extractListFromResponse(consentResponse, "authorizationResources")
        authIdBeforeUpdate = authorizationResources.get(0).get("authorizationID")

        String updateStatusPayload = AccountsRequestPayloads.getAccountBasicConsentUpdatePayload(consentId)
        doConsentUpdate(updateStatusPayload, consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "updatedTime"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "consentID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "clientID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "status"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentUpdateResponse, "status"), "Rejected")
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "authorizationResources"))
        List<JSONObject> updatedAuthResources = extractListFromResponse(consentUpdateResponse, "authorizationResources")
        String newAuthId = updatedAuthResources.get(0).get("authorizationID")
        Assert.assertEquals(newAuthId, authIdBeforeUpdate)
    }

    @Test(priority = 2)
    void "Verify internal updating a Created Consent with null authorizations"() {

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        consentResponse = consentRequestBuilder.buildKeyManagerRequest(configuration.getAppInfoClientID())
                .header(ConnectorTestConstants.X_WSO2_INTERNAL_HEADER, "true")
                .baseUri(configuration.getISServerUrl())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "status"),
                "AwaitingAuthorisation")
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "authorizationResources"))
        List<JSONObject> authorizationResources = extractListFromResponse(consentResponse, "authorizationResources")
        authIdBeforeUpdate = authorizationResources.get(0).get("authorizationID")

        String updateStatusPayload = AccountsRequestPayloads.getAccountConsentUpdatePayloadWithNullAuthorizations(consentId)
        doConsentUpdate(updateStatusPayload, consentId)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentUpdateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "updatedTime"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "consentID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "clientID"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "status"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentUpdateResponse, "status"), "Rejected")
        Assert.assertNotNull(TestUtil.parseResponseBody(consentUpdateResponse, "authorizationResources"))
        List<JSONObject> updatedAuthResources = extractListFromResponse(consentUpdateResponse, "authorizationResources")
        String newAuthId = updatedAuthResources.get(0).get("authorizationID")
        Assert.assertEquals(newAuthId, authIdBeforeUpdate)
    }

    static List<JSONObject> extractListFromResponse(Response response, String jsonPath) {
        return response.jsonPath().getList(jsonPath)
    }

}
