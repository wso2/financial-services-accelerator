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

package org.wso2.financial.services.accelerator.integration.test.accounts.EndToEndFlowTest

import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.integration.test.accounts.util.AbstractAccountsFlow

/**
 * End to Account Flow Tests.
 */
class EndToEndAccountFlowTest extends AbstractAccountsFlow {

    @Test
    void "Verify Account Initiation Request With All Permissions"() {

        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_201)
    }

    @Test (dependsOnMethods = "Verify Account Initiation Request With All Permissions")
    void "Verify Retrieving for a Created Account Consent"() {

        doAccountConsentRetrieval()
        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID)

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, "AwaitingAuthorisation")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Account Consent")
    void "Verify Authorize Consent in AwaitingAuthorisation Status"() {

        //Authorise Consent
        doAccountConsentAuthorisation()

        Assert.assertNotNull(code)
        Assert.assertNotNull(userAccessToken)
    }

    @Test (dependsOnMethods = "Verify Authorize Consent in AwaitingAuthorisation Status")
    void "Verify Retrieving for a Created Consent After authorizing"() {

        doAccountConsentRetrieval()
        consentId = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_CONSENT_ID)

        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, "Authorised")
    }

    @Test (dependsOnMethods = "Verify Retrieving for a Created Consent After authorizing")
    void "Verify Account Retrieval Request for a Authorised Consent"() {

        doDefaultAccountRetrieval()
        Assert.assertEquals(retrievalResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
    }
}
