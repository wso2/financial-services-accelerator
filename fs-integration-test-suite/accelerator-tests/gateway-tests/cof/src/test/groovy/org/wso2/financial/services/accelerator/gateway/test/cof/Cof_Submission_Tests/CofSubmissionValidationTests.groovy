package org.wso2.financial.services.accelerator.gateway.test.cof.Cof_Submission_Tests

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
    void "OBA-797_COF submission request with revoked consent"(){

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
        Assert.assertEquals(TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_ERRORS_0_DESCRIPTION),
                "Consent is not in the correct state")
        Assert.assertEquals(TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_ERRORS_0_MSG),
                "Consent Enforcement Error")
        Assert.assertEquals(TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_ERRORS_0_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "COF submission request with application access token"(){

        //Initiate consent
        doDefaultInitiation()
        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.CREATED)
        Assert.assertNotNull(consentId)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,
                ConnectorTestConstants.DATA_STATUS).toString(), "AwaitingAuthorisation")

        //Authorise the consent and generate user access token
        doCofAuthorization(scopeList)
        Assert.assertNotNull(userAccessToken)

        //COF Submission Request
        submissionResponse = consentRequestBuilder.buildBasicRequest(applicationAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .body(CofRequestPayloads.getCofSubmissionPayload(consentId))
                .post(ConnectorTestConstants.CBPII_PATH + ConnectorTestConstants.COF_SUBMISSION_PATH)

        Assert.assertEquals(submissionResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(submissionResponse, ConnectorTestConstants.ERROR_ERRORS_0_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Incorrect Access Token Type provided"))
        Assert.assertEquals(TestUtil.parseResponseBody(submissionResponse,ConnectorTestConstants.ERROR_ERRORS_0_CODE),
                "200001")
        Assert.assertEquals(TestUtil.parseResponseBody(submissionResponse,ConnectorTestConstants.ERROR_ERRORS_0_MSG),
                "Access failure for API: grant type validation failed.")
    }
}
