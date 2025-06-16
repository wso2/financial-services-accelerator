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

package org.wso2.financial.services.accelerator.gateway.test.cof.EndToEndTest

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * End to End Funds Confirmation Flow Tests.
 */
class EndToEndCOFFlowTest extends FSAPIMConnectorTest {

    List<ConnectorTestConstants.ApiScope> scopeList

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.COF_CONSENT_API_PATH
        initiationPayload = RequestPayloads.cofInitiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.COF_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Verify Cof Initiation Request"() {

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test (dependsOnMethods = "Verify Cof Initiation Request")
    void "Verify Retrieving for a Created Cof Consent"() {

        doConsentRetrieval()
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID)

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, "AwaitingAuthorisation")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Cof Consent")
    void "Verify Authorize Consent in AwaitingAuthorisation Status"() {

        //Authorise Consent
        doCofAuthorization(scopeList)

        Assert.assertNotNull(code)
        Assert.assertNotNull(userAccessToken)
    }

    @Test (dependsOnMethods = "Verify Authorize Consent in AwaitingAuthorisation Status")
    void "Verify Retrieving for a Created Consent After authorizing"() {

        doConsentRetrieval()
        consentId = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_CONSENT_ID)

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, "Authorised")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Consent After authorizing")
    void "Funds Confirmation SCA accept: Submission"() {

        doDefaultSubmission()
        Assert.assertEquals(submissionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test (dependsOnMethods = "Funds Confirmation SCA accept: Submission")
    void "Revoke a Created Consent"() {

        doConsentRevocation(consentId)

        Assert.assertEquals(consentRevocationResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_204)
    }
}
