/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.common.test.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Test for Financial Services Utils.
 */
public class FinancialServicesUtilsTest {

    @Mock
    private MockedStatic<FinancialServicesConfigParser> configParser;
    private FinancialServicesConfigParser configParserMock;

    @BeforeClass
    public void initClass() {

        configParser = mockStatic(FinancialServicesConfigParser.class);
        configParserMock = mock(FinancialServicesConfigParser.class);
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        configParser.close();
    }


    @Test
    public void testReducingStringLength() throws Exception {

        String body = "String Body";
        Assert.assertEquals(FinancialServicesUtils.reduceStringLength(body, 25), body);
        Assert.assertEquals(FinancialServicesUtils.reduceStringLength(body, 6), "String");
    }

    @Test
    public void testStartsWithUUID() {

        Assert.assertFalse(FinancialServicesUtils.startsWithUUID("String Body"));

        Assert.assertTrue(FinancialServicesUtils.startsWithUUID(UUID.randomUUID().toString()));
    }

    @Test
    public void testConvertToISO8601() {

        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        // Define the format you want
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FinancialServicesConstants.ISO_FORMAT);
        // Format the date
        String formattedDate = offsetDateTime.format(formatter);
        Assert.assertEquals(FinancialServicesUtils.convertToISO8601(offsetDateTime.toEpochSecond()), formattedDate);
    }

    @Test
    public void testGetConsentIdFromEssentialClaims() throws JsonProcessingException {

        doReturn("/essential/consentId").when(configParserMock).getConsentIdExtractionJsonPath();
        doReturn("").when(configParserMock).getConsentIdExtractionRegexPattern();

        String consentId = "consentId";
        String json = "{\n" +
                "  \"essential\": {\n" +
                "    \"consentId\": \"" + consentId + "\"\n" +
                "  }\n" +
                "}";

        Assert.assertEquals(FinancialServicesUtils.getConsentIdFromEssentialClaims(json), consentId);
    }

    @Test
    public void testGetConsentIdFromScopesRequestParam() {
        String[] scopes = {"scope1", "ais:123", "scope2"};

        doReturn(":([a-fA-F0-9\\-]+)").when(configParserMock).getConsentIdExtractionRegexPattern();

        String consentId = FinancialServicesUtils.getConsentIdFromScopesRequestParam(scopes);
        Assert.assertEquals(consentId, "123");
    }

    @Test
    public void testExtractConsentIdFromRegex() {
        String consentId = "da5c57ca-dcab-45db-8620-65fca406fd91";
        String value = "ais: accounts ais:" + consentId + " payments ais:: pis";

        doReturn(":([a-fA-F0-9\\-]+)").when(configParserMock).getConsentIdExtractionRegexPattern();

        String extractedConsentId = FinancialServicesUtils.extractConsentIdFromRegex(value);
        Assert.assertEquals(extractedConsentId, consentId);
    }

    @Test(dataProviderClass = FinancialServicesUtilsTest.class, dataProvider = "consentFlowTypesForScopes")
    public void testisPreInitiatedConsentFlow(String scope, List<String> preInitiatedConsentScopes,
                                              List<String> scopeBasedConsentScopes,
                                              boolean expected) {

        boolean result = FinancialServicesUtils.isPreInitiatedConsentFlow(scope, preInitiatedConsentScopes,
                scopeBasedConsentScopes);
        Assert.assertEquals(result, expected);
    }

    @Test(dataProviderClass = FinancialServicesUtilsTest.class, dataProvider = "consentFlowTypesForScopeList")
    public void testisPreInitiatedConsentFlow(String[] scope, List<String> preInitiatedConsentScopes,
                                              List<String> scopeBasedConsentScopes,
                                              boolean expected) {

        boolean result = FinancialServicesUtils.isPreInitiatedConsentFlow(scope, preInitiatedConsentScopes,
                scopeBasedConsentScopes);
        Assert.assertEquals(result, expected);
    }

    @DataProvider(name = "consentFlowTypesForScopes")
    public Object[][] getConsentFlowTypesForScopes() {

        return new Object[][] {
                {"accounts", Collections.emptyList(), Collections.emptyList(), true},
                {"accounts", Collections.singletonList("payments"), Collections.singletonList("accounts"), false},
                {"payments", Collections.singletonList("payments"), Collections.singletonList("accounts"), true},
                {"accounts", List.of("payments", "accounts"), Collections.emptyList(), true},
                {"accounts", Collections.emptyList(), List.of("payments", "accounts"), false}
        };
    }

    @DataProvider(name = "consentFlowTypesForScopeList")
    public Object[][] getConsentFlowTypesForScopeList() {

        return new Object[][] {
                {new String[] {"accounts", "openid"}, Collections.emptyList(), Collections.emptyList(), true},
                {new String[] {"accounts", "openid"}, Collections.singletonList("payments"),
                        Collections.singletonList("accounts"), false},
                {new String[] {"payments", "openid"}, Collections.singletonList("payments"),
                        Collections.singletonList("accounts"), true},
                {new String[] {"accounts", "openid"}, List.of("payments", "accounts"), Collections.emptyList(), true},
                {new String[] {"accounts", "openid"}, Collections.emptyList(), List.of("payments", "accounts"), false}
        };
    }

}
