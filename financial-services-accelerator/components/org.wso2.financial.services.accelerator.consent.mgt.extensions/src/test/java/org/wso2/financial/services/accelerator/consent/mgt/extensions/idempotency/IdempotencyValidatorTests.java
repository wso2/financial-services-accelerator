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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.idempotency;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyValidationException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyValidationResult;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Test class for IdempotencyValidator.
 */
public class IdempotencyValidatorTests {

    @Mock
    private ConsentManageData consentManageData;
    private ConsentCoreServiceImpl consentCoreServiceImpl;
    private ArrayList<String> consentIdList;
    private Map<String, String> attributeList;
    private String consentId;
    private Map<String, Object> configs;
    private Map<String, String> headers;
    private static final String CLIENT_ID = "testClientId";
    private MockedStatic<FinancialServicesConfigParser> configParser;
    MockedStatic<ConsentExtensionsDataHolder> consentExtensionsDataHolder;

    private static final String PAYLOAD = "{\n" +
            "  \"Data\": {\n" +
            "    \"ReadRefundAccount\": \"Yes\",\n" +
            "    \"Initiation\": {\n" +
            "      \"InstructionIdentification\": \"ACME412\",\n" +
            "      \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"165.88\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"UK.OBIE.SortCodeAccountNumber\",\n" +
            "        \"Identification\": \"08080021325698\",\n" +
            "        \"Name\": \"ACME Inc\",\n" +
            "        \"SecondaryIdentification\": \"0002\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"FRESCO-101\",\n" +
            "        \"Unstructured\": \"Internal ops code 5120101\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"Risk\": {\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String DIFFERENT_PAYLOAD = "{\n" +
            "  \"Data\": {\n" +
            "    \"ReadRefundAccount\": \"No\",\n" +
            "    \"Initiation\": {\n" +
            "      \"InstructionIdentification\": \"ACME413\",\n" +
            "      \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"165.88\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"UK.OBIE.SortCodeAccountNumber\",\n" +
            "        \"Identification\": \"08080021325698\",\n" +
            "        \"Name\": \"ACME Inc\",\n" +
            "        \"SecondaryIdentification\": \"0002\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"FRESCO-101\",\n" +
            "        \"Unstructured\": \"Internal ops code 5120101\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"Risk\": {\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @BeforeClass
    public void beforeTest() {
        configs = new HashMap<>();

        headers = new HashMap<>();
        headers.put(IdempotencyConstants.X_IDEMPOTENCY_KEY, "123456");
        headers.put(IdempotencyConstants.CONTENT_TYPE_TAG, "application/json");

        consentManageData = mock(ConsentManageData.class);
        consentCoreServiceImpl = mock(ConsentCoreServiceImpl.class);

        consentId = UUID.randomUUID().toString();
        consentIdList = new ArrayList<>();
        consentIdList.add(consentId);

        attributeList = new HashMap<>();
        attributeList.put(consentId, "123456");
        configParser = mockStatic(FinancialServicesConfigParser.class);
        consentExtensionsDataHolder = mockStatic(ConsentExtensionsDataHolder.class);
    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        configParser.close();
        consentExtensionsDataHolder.close();
    }

    @Test
    public void testValidateIdempotency() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertTrue(result.isIdempotent());
        Assert.assertTrue(result.isValid());
        Assert.assertNotNull(result.getConsent());
        Assert.assertEquals(consentId, result.getConsentId());
    }

    @Test
    public void testValidateIdempotencyForRequestsWithoutPayload() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(attributeList).when(consentCoreServiceImpl)
                .getConsentAttributesByName(anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn("{}").when(consentManageData).getPayload();
        doReturn("{}").when(consentManageData).getPayload();
        doReturn("/payments/".concat(consentId)).when(consentManageData).getRequestPath();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);
        Assert.assertTrue(result.isIdempotent());
        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testValidateIdempotencyWithoutIdempotencyKeyValue() throws IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(new HashMap<>()).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutRequest() throws IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn("").when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyRetrievingAttributesWithException() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doThrow(ConsentManagementException.class).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutAttribute() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(new ArrayList<>()).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithNullConsentRequest() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        doReturn(null).when(consentCoreServiceImpl).getDetailedConsent(anyString());
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);
        Assert.assertTrue(result.isIdempotent());
        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testValidateIdempotencyWithNonMatchingClientId() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn("sampleClientID").when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        doReturn(null).when(consentCoreServiceImpl).getDetailedConsent(anyString());
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);
        Assert.assertTrue(result.isIdempotent());
        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testValidateIdempotencyAfterAllowedTime() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now().minusHours(2);

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);
        Assert.assertTrue(result.isIdempotent());
        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testValidateIdempotencyWithNonMatchingPayload() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(DIFFERENT_PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);
        Assert.assertTrue(result.isIdempotent());
        Assert.assertFalse(result.isValid());
    }

    private DetailedConsentResource getConsent(long createdTime) {
        DetailedConsentResource consent = new DetailedConsentResource();
        consent.setConsentID(consentId);
        consent.setReceipt(PAYLOAD);
        consent.setClientID(CLIENT_ID);
        consent.setCreatedTime(createdTime);
        return consent;
    }
}
