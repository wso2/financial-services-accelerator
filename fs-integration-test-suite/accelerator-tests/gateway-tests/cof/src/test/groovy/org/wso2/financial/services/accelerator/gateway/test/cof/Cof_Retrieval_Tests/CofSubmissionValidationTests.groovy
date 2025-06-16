package org.wso2.financial.services.accelerator.gateway.test.cof.Cof_Retrieval_Tests

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.CofRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

class CofSubmissionValidationTests extends FSAPIMConnectorTest{

    List<ConnectorTestConstants.ApiScope> scopeList

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
    void "OBA-797_COF retrieval request with revoked consent"(){

        //Initiate consent
        doDefaultInitiation()
        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,
                ConnectorTestConstants.DATA_STATUS).toString(), "AwaitingAuthorisation")

        //Authorise the consent and generate user access token
        doCofAuthorization(scopeList)
        Assert.assertNotNull(userAccessToken)

        //Revoke the consent
        doConsentRevocation(consentId)
        Assert.assertEquals(consentRevocationResponse.statusCode(), ConnectorTestConstants.NO_CONTENT)

        //COF Submission Request
        def submissionResponse = doDefaultSubmission()
        Assert.assertEquals(submissionResponse.statusCode(), ConnectorTestConstants.BAD_REQUEST)
    }
}
