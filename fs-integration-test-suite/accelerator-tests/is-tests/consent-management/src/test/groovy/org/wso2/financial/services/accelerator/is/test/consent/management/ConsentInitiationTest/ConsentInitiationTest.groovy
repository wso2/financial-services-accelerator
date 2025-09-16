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

package org.wso2.financial.services.accelerator.is.test.consent.management.ConsentInitiationTest

import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.bfsi.test.framework.request_builder.SignedObject

/**
 * Consent Initiation Test class
 */
class ConsentInitiationTest extends FSConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
    }

    @Test
    void "Verify Creation of a consent with valid inputs"() {

        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Verify Creation of a consent with incorrect request payload"() {

        doDefaultInitiationWithIncorrectPayload()
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Verify Creation of a consent without ReadAccountsDetail permission"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutReadAccountsDetail)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorize consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()
        SignedObject signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(configuration.getCommonSigningAlgorithm())

        //Consent Validate Request
        consentValidateResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequestWithTruststore(AccountsRequestPayloads.buildValidationAccountsPayload(GenerateBasicHeader(),
                        userId, consentId)))
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, consentRequestBuilder.GenerateBasicHeader())
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.ACCOUNT_VALIDATE_PATH)

        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_CODE),
                "Forbidden")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.HTTP_CODE),
                "403")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_MESSAGE),
                "Permission mismatch. Consent does not contain necessary permissions")
    }

    @Test
    void "Verify Creation of a consent without ReadTransactionsDetail permission"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutReadTransactionsDetail)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorize consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()
        SignedObject signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(configuration.getCommonSigningAlgorithm())

        //Consent Validate Request
        consentValidateResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequestWithTruststore(AccountsRequestPayloads.buildValidationTransactionPayload(userId, consentId, host)))
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, consentRequestBuilder.GenerateBasicHeader())
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.ACCOUNT_VALIDATE_PATH)


        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_CODE),
                "Forbidden")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.HTTP_CODE),
                "403")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_MESSAGE),
                "Permission mismatch. Consent does not contain necessary permissions")
    }

    @Test
    void "Verify Creation of a consent without ReadBalances permission"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutReadBalances)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Authorize consent
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)

        def host = configuration.getISServerUrl()
        SignedObject signedObject = new SignedObject()
        signedObject.setSigningAlgorithm(configuration.getCommonSigningAlgorithm())

        //Consent Validate Request
        consentValidateResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.CONTENT_TYPE_JWT)
                .body(signedObject.getSignedRequestWithTruststore(AccountsRequestPayloads.buildValidationBalancePayload(userId, consentId, host)))
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, consentRequestBuilder.GenerateBasicHeader())
                .header(ConnectorTestConstants.X_WSO2_CLIENT_ID_KEY, configuration.getAppInfoClientID())
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(
                                TestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.ACCOUNT_VALIDATE_PATH)


        Assert.assertEquals(consentValidateResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_CODE),
                "Forbidden")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.HTTP_CODE),
                "403")
        Assert.assertEquals(TestUtil.parseResponseBody(consentValidateResponse, ConnectorTestConstants.ERROR_MESSAGE),
                "Permission mismatch. Consent does not contain necessary permissions")
    }

    @Test
    void "Verify Creation of a consent without client ID"() {

        consentResponse = buildKeyManagerRequest("")
                .body(initiationPayload)
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_create")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Client ID missing in the request.")
    }

    @Test
    void "Verify Create Consent Without the Payload Body"() {

        consentResponse = buildKeyManagerRequest(configuration.getAppInfoClientID())
                .body("")
                .baseUri(configuration.getISServerUrl())
                .post(consentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Payload is not in the correct format")
    }
}
