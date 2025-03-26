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

package org.wso2.financial.services.accelerator.gateway.test.accounts.util


import io.restassured.response.Response
import jdk.internal.net.http.Response
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.ConsentRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Base class for Accounts Test.
 * All common functions that directly required for test class are implemented in here.
 */
class AbstractAccountsFlow extends FSConnectorTest {

    String applicationAccessToken
    String consentStatus
    String userAccessToken
    Response retrievalResponse

    final String consentPath = AccountConstants.ACCOUNT_CONSENT_PATH
    final String accountsPath = AccountConstants.ACCOUNTS_PATH
    List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

    String initiationPayload= RequestPayloads.initiationPayload

    ConfigurationService configuration = new ConfigurationService()
    protected static Logger log = LogManager.getLogger(FSConnectorTest.class.getName())
    ConsentRequestBuilder consentRequestBuilder = new ConsentRequestBuilder()

    @BeforeClass(alwaysRun = true)
    void setup() {
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    /**
     * Account Consent Initiation Step
     */
    void doDefaultAccountInitiation() {
        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID)
        consentStatus = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
    }

    /**
     * Account Consent Retrieval.
     * @param consentId
     */
    void doAccountConsentRetrieval() {

        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        consentStatus = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
    }

    /**
     * Account Consent Authorisation.
     * @param consentId
     */
    void doAccountConsentAuthorisation() {

        //initiation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, scopeList)
        Assert.assertNotNull(code)
        Assert.assertNotNull(OBTestUtil.getIdTokenFromUrl(automation.currentUrl.get()))

        userAccessToken = getUserAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), code as String, scopeList)
    }

    /**
     * Retrieve Accounts List
     */
    void doDefaultAccountRetrieval() {
        // Check consent valid status
        retrievalResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(accountsPath)
    }

    /**
     * Account Consent Initiation Step
     */
    Response doDefaultAccountInitiationWithUpdatedPayload(String updatedPayload) {
        //initiation
        consentResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .body(updatedPayload)
                .post(consentPath)

        return consentResponse
    }

    /**
     * Account Consent Revocation Step.
     * @param consentId
     */
    Response doAccountConsentRevocation(String consentId) {

        //initiation
        consentRevocationResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/${consentId}")

        return consentRevocationResponse
    }

    /**
     * Account Consent Authorisation Deny scenario.
     * @param consentId
     */
    void doAccountConsentAuthorisationDeny() {

        //initiation
        consentDenial(configuration.getAppInfoClientID(), true, scopeList)
    }
}
