/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize;

import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for ConsentAuthorizeUtil.
 */
public class ConsentAuthorizeUtilTest {

    MockedStatic<FinancialServicesConfigParser> configParserStaticMock;

    @BeforeClass
    public void setUp() {
        configParserStaticMock = Mockito.mockStatic(FinancialServicesConfigParser.class);
    }

    @AfterClass
    public void tearDown() {
        configParserStaticMock.close();
    }

    @Test
    public void testExtractConsentIdFromRequestObject() throws ConsentException {

        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        when(configParserMock.getAuthFlowConsentIdSource()).thenReturn(FinancialServicesConstants.REQUEST_OBJECT);
        doReturn("/id_token/openbanking_intent_id/value").when(configParserMock)
                .getConsentIdExtractionJsonPath();
        doReturn(FinancialServicesConstants.SCOPE).when(configParserMock).getConsentIdExtractionKey();
        doReturn("([a-fA-F0-9\\-]+)").when(configParserMock).getConsentIdExtractionRegexPattern();
        configParserStaticMock.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        String consentId = ConsentAuthorizeUtil.extractConsentIdFromRequestObject(TestConstants.VALID_REQUEST_OBJECT);

       assertNotNull(consentId);
    }

    @Test
    public void testExtractConsentIdFromRequestParam() throws ConsentException {

        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        when(configParserMock.getAuthFlowConsentIdSource()).thenReturn(FinancialServicesConstants.REQUEST_OBJECT);
        doReturn("/id_token/openbanking_intent_id/value").when(configParserMock)
                .getConsentIdExtractionJsonPath();
        doReturn(FinancialServicesConstants.SCOPE).when(configParserMock).getConsentIdExtractionKey();
        doReturn(":([a-fA-F0-9\\-]+)").when(configParserMock).getConsentIdExtractionRegexPattern();
        configParserStaticMock.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        String decodedReqObj = decodeRequestObjectPayload(TestConstants.VALID_REQUEST_OBJECT_WITH_CONSENT_ID_IN_SCOPE);
        String consentId = ConsentAuthorizeUtil.extractConsentIdFromRequestParam(new JSONObject(decodedReqObj));

        assertNotNull(consentId);
    }

    @Test
    public void testGetQueryParamJson() throws ConsentException {

        JSONObject queryParamJson = ConsentAuthorizeUtil.getQueryParamJson("request=dummy");
        assertNotNull(queryParamJson);
    }

    @Test
    public void testGetConsentDataForPreInitiatedConsentForAccounts() throws ConsentException {

        ConsentResource consentResource = TestUtil.getSampleConsentResource(TestConstants.ACCOUNTS,
                TestConstants.VALID_INITIATION, TestConstants.AWAITING_AUTH_STATUS);
        JSONObject consentJson = ConsentAuthorizeUtil.getConsentDataForPreInitiatedConsent(consentResource);
        assertNotNull(consentJson);
    }

    @Test
    public void testGetConsentDataForPreInitiatedConsentForCOF() throws ConsentException {

        ConsentResource consentResource = TestUtil.getSampleConsentResource(TestConstants.FUNDS_CONFIRMATIONS,
                TestConstants.COF_RECEIPT, TestConstants.AWAITING_AUTH_STATUS);
        JSONObject consentJson = ConsentAuthorizeUtil.getConsentDataForPreInitiatedConsent(consentResource);
        assertNotNull(consentJson);
    }

    @Test
    public void testGetConsentDataForPreInitiatedConsentForPayments() throws ConsentException {

        ConsentResource consentResource = TestUtil.getSampleConsentResource(TestConstants.PAYMENTS,
                TestConstants.PAYMENT_INITIATION, TestConstants.AWAITING_AUTH_STATUS);
        JSONObject consentJson = ConsentAuthorizeUtil.getConsentDataForPreInitiatedConsent(consentResource);
        assertNotNull(consentJson);
    }

    private static String decodeRequestObjectPayload(String requestObject) {
        String[] jwtTokenValues = requestObject.split("\\.");
        if (jwtTokenValues.length == 3) {
            return new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]),
                    StandardCharsets.UTF_8);
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "request object is not signed JWT");
        }
    }
}
