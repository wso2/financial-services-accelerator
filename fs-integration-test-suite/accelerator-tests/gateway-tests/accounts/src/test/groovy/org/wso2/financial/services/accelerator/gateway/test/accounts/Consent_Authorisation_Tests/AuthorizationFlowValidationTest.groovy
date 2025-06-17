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

package org.wso2.financial.services.accelerator.gateway.test.accounts.Consent_Authorisation_Tests

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.automation.consent.AuthorizationFlowNavigationAutomationStep
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Authorization Flow Validation Tests.
 */
class AuthorizationFlowValidationTest extends FSAPIMConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Generate authorization code when valid request object is present in the authorization request"() {

        //Create Consent
        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        doAccountConsentAuthorisation()

        Assert.assertNotNull(code)
        Assert.assertNotNull(userAccessToken)
    }

    @Test
    void "Generate authorization code when Request Object is signed with expired certificate"() {

        String appKeystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String password = "wso2carbon"
        String alias = "tpp4-sig"

        //Create Consent
        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithDefinedCert(configuration.getAppInfoClientID(),
                scopeList, appKeystoreLocation, password, alias, consentId).toURI().toString()
        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()
        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(ConnectorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
    }

    @Test
    void "Generate authorization code when Request Object is signed with Invalid certificate"() {

        String appKeystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "berlin-certs", "eidas-qwac.jks")
        String password = "wso2carbon"
        String alias = "1"

        //Create Consent
        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        String authoriseUrl = acceleratorAuthorisationBuilder.getOAuthRequestWithDefinedCert(configuration.getAppInfoClientID(),
                scopeList, appKeystoreLocation, password, alias, consentId).toURI().toString()
        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new AuthorizationFlowNavigationAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                }
                .execute()
        Assert.assertTrue(URLDecoder.decode(automation.currentUrl.get())
                .contains(ConnectorTestConstants.INVALID_REQUEST_OBJECT_ERROR))
    }

    @Test
    void "Deny consent in consent authorisation"() {

        //Create Consent
        doDefaultAccountInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorise Consent
        doAccountConsentAuthorisationDeny()
        Assert.assertEquals(denyResponse, "User+denied+the+consent")

        //Retrieve Consent Status
        doAccountConsentRetrieval()
        Assert.assertEquals(consentStatus, "Rejected")
    }
}
