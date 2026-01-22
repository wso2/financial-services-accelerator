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

package org.wso2.financial.services.accelerator.gateway.test.encryption

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.request_builder.EncryptedPayloadGenerator
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * End to Account Flow Tests.
 */
class EncryptedPayloadValidationTest extends FSAPIMConnectorTest {

    @BeforeClass
    void init() {
        EncryptedPayloadGenerator payloadGenerator = new EncryptedPayloadGenerator()
        consentPath = "/open-banking/v3.1/encrypt/3.1.11/account-access-consents"
        initiationPayload = payloadGenerator.generateEncryptedPayload(AccountsRequestPayloads.initiationPayload)
        scopeList = [ConnectorTestConstants.ApiScope.ENCRYPT, ConnectorTestConstants.ApiScope.OPEN_ID]

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Verify Account Initiation Request With All Permissions"() {

        //initiation
        consentResponse = FSRestAsRequestBuilder.buildRequestForEncryptedPayload()
                .contentType("application/jose+jwe")
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept("application/json")
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID)
        consentStatus = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }
}