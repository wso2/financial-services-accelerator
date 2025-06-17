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

import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentOperationEnum;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyValidationException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyValidationResult;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency.IdempotencyValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIModifiedResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostFileUploadRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ExternalAPIConsentManageUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
    public void testValidateIdempotency()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        headers.put("content-type", "application/json");
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        doReturn("/payments").when(consentManageData).getRequestPath();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);

        Assert.assertTrue(result.isIdempotent());
        Assert.assertTrue(result.isValid());
        Assert.assertNotNull(result.getConsent());
        Assert.assertEquals(consentId, result.getConsentId());
    }

    @Test
    public void testValidateIdempotencyForFileUpload()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        doReturn(getConsentFile()).when(consentCoreServiceImpl).getConsentFile(anyString());
        headers.put("content-type", "application/xml");
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(FILE_UPLOAD_REQUEST_BODY).when(consentManageData).getPayload();
        doReturn("/file-payments").when(consentManageData).getRequestPath();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_FILE_UPLOAD);

        Assert.assertTrue(result.isIdempotent());
        Assert.assertTrue(result.isValid());
        Assert.assertNotNull(result.getConsent());
        Assert.assertEquals(consentId, result.getConsentId());
    }

    @Test
    public void testIsIdempotentForFileUploadWithExtensionEnabled()
            throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        doReturn(true).when(configParserMock).isServiceExtensionsEndpointEnabled();
        doReturn(List.of(ServiceExtensionTypeEnum.ENRICH_CONSENT_FILE_RESPONSE)).when(configParserMock)
                .getServiceExtensionTypes();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        doReturn(getConsentFile()).when(consentCoreServiceImpl).getConsentFile(anyString());
        headers.put("content-type", "application/xml");
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(FILE_UPLOAD_REQUEST_BODY).when(consentManageData).getPayload();
        doReturn("/file-payments").when(consentManageData).getRequestPath();

        try (MockedStatic<ExternalAPIConsentManageUtils> mockedStatic = mockStatic(
                ExternalAPIConsentManageUtils.class)) {
            ExternalAPIModifiedResponseDTO responseDTO =
                    new ExternalAPIModifiedResponseDTO();
            mockedStatic.when(() ->
                            ExternalAPIConsentManageUtils.callExternalService(
                                    any(ExternalAPIPostFileUploadRequestDTO.class)))
                    .thenReturn(responseDTO);

            boolean isIdempotent = new IdempotencyValidator().isIdempotent(consentManageData,
                    ConsentOperationEnum.CONSENT_FILE_UPLOAD);
            Assert.assertTrue(isIdempotent);
        }
    }

    @Test
    public void testIsIdempotent() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        headers.put("content-type", "application/json");
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(new JSONObject(PAYLOAD)).when(consentManageData).getPayload();
        doReturn("/payments").when(consentManageData).getRequestPath();
        boolean isIdempotent = new IdempotencyValidator().isIdempotent(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);

        Assert.assertTrue(isIdempotent);
    }

    @Test
    public void testIsIdempotentWithExtensionEnabled() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        doReturn(true).when(configParserMock).isServiceExtensionsEndpointEnabled();
        doReturn(List.of(ServiceExtensionTypeEnum.ENRICH_CONSENT_CREATION_RESPONSE)).when(configParserMock)
                .getServiceExtensionTypes();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        headers.put("content-type", "application/json");
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(new JSONObject(PAYLOAD)).when(consentManageData).getPayload();
        doReturn("/payments").when(consentManageData).getRequestPath();

        try (MockedStatic<ExternalAPIConsentManageUtils> mockedStatic = mockStatic(
                ExternalAPIConsentManageUtils.class)) {
            ExternalAPIModifiedResponseDTO postConsentGenerateResponseDTO =
                    new ExternalAPIModifiedResponseDTO();
            mockedStatic.when(() ->
                            ExternalAPIConsentManageUtils.callExternalService(
                                    any(ExternalAPIPostConsentGenerateRequestDTO.class)))
                    .thenReturn(postConsentGenerateResponseDTO);

            boolean isIdempotent = new IdempotencyValidator().isIdempotent(consentManageData,
                    ConsentOperationEnum.CONSENT_CREATE);
            Assert.assertTrue(isIdempotent);
        }
    }

    @Test
    public void testIsIdempotentForFileUpload() throws ConsentManagementException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        doReturn(getConsentFile()).when(consentCoreServiceImpl).getConsentFile(anyString());
        headers.put("content-type", "application/xml");
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(FILE_UPLOAD_REQUEST_BODY).when(consentManageData).getPayload();
        doReturn("/file-payments").when(consentManageData).getRequestPath();
        boolean isIdempotent = new IdempotencyValidator().isIdempotent(consentManageData,
                ConsentOperationEnum.CONSENT_FILE_UPLOAD);

        Assert.assertTrue(isIdempotent);
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyForRequestsWithoutPayload()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
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
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);
    }

    @Test
    public void testValidateIdempotencyWithoutIdempotencyKeyValue() throws IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(new HashMap<>()).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutRequest() throws IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn("").when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyRetrievingAttributesWithException()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doThrow(ConsentManagementException.class).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutAttribute()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(new ArrayList<>()).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        doReturn(headers).when(consentManageData).getHeaders();
        doReturn(CLIENT_ID).when(consentManageData).getClientId();
        doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyWithNullConsentRequest()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
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
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyWithNonMatchingClientId()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
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
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyAfterAllowedTime()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
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
        doReturn("/payments").when(consentManageData).getRequestPath();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyWithNonMatchingPayload()
            throws ConsentManagementException, IdempotencyValidationException {

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        doReturn(true).when(configParserMock).isIdempotencyValidationEnabled();
        doReturn("1").when(configParserMock).getIdempotencyAllowedTime();
        doReturn(true).when(configParserMock).isIdempotencyAllowedForAllAPIs();
        doReturn(IdempotencyConstants.X_IDEMPOTENCY_KEY).when(configParserMock).getIdempotencyHeaderName();
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
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData,
                ConsentOperationEnum.CONSENT_CREATE);
    }

    private DetailedConsentResource getConsent(long createdTime) {
        DetailedConsentResource consent = new DetailedConsentResource();
        consent.setConsentID(consentId);
        consent.setReceipt(PAYLOAD);
        consent.setClientID(CLIENT_ID);
        consent.setCreatedTime(createdTime);
        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.FILE_UPLOAD_CREATED_TIME, String.valueOf(createdTime));
        consent.setConsentAttributes(consentAttributes);
        return consent;
    }

    private ConsentFile getConsentFile() {
        ConsentFile consentFile = new ConsentFile();
        consentFile.setConsentID(consentId);
        consentFile.setConsentFile(FILE_UPLOAD_REQUEST_BODY);
        return consentFile;
    }

    public static final String FILE_UPLOAD_REQUEST_BODY = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.08\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema- instance\">\n" +
            "\t<CstmrCdtTrfInitn>\n" +
            "\t<GrpHdr>\n" +
            "\t\t<MsgId>ABC/120928/CCT001</MsgId>\n" +
            "\t\t<CreDtTm>2012-09-28T14:07:00</CreDtTm>\n" +
            "\t\t<NbOfTxs>3</NbOfTxs>\n" +
            "\t\t<CtrlSum>11500000</CtrlSum>\n" +
            "\t\t<InitgPty>\n" +
            "\t\t\t<Nm>ABC Corporation</Nm>\n" +
            "\t\t\t<PstlAdr>\n" +
            "\t\t\t\t<StrtNm>Times Square</StrtNm>\n" +
            "\t\t\t\t<BldgNb>7</BldgNb>\n" +
            "\t\t\t\t<PstCd>NY 10036</PstCd>\n" +
            "\t\t\t\t<TwnNm>New York</TwnNm>\n" +
            "\t\t\t\t<Ctry>US</Ctry>\n" +
            "\t\t\t</PstlAdr>\n" +
            "\t\t</InitgPty>\n" +
            "\t</GrpHdr>\n" +
            "\t<PmtInf>\n" +
            "\t\t<PmtInfId>ABC/086</PmtInfId>\n" +
            "\t\t<PmtMtd>TRF</PmtMtd>\n" +
            "\t\t<BtchBookg>false</BtchBookg>\n" +
            "\t\t<ReqdExctnDt>\n" +
            "\t\t\t<Dt>2012-09-29</Dt>\n" +
            "\t\t</ReqdExctnDt>\n" +
            "\t\t<Dbtr>\n" +
            "\t\t\t<Nm>ABC Corporation</Nm>\n" +
            "\t\t\t<PstlAdr>\n" +
            "\t\t\t\t<StrtNm>Times Square</StrtNm>\n" +
            "\t\t\t\t<BldgNb>7</BldgNb>\n" +
            "\t\t\t\t<PstCd>NY 10036</PstCd>\n" +
            "\t\t\t\t<TwnNm>New York</TwnNm>\n" +
            "\t\t\t\t<Ctry>US</Ctry>\n" +
            "\t\t\t</PstlAdr>\n" +
            "\t\t</Dbtr>\n" +
            "\t\t<DbtrAcct>\n" +
            "\t\t\t<Id>\n" +
            "\t\t\t\t<Othr>\n" +
            "\t\t\t\t\t<Id>00125574999</Id>\n" +
            "\t\t\t\t</Othr>\n" +
            "\t\t\t</Id>\n" +
            "\t\t</DbtrAcct>\n" +
            "\t\t<DbtrAgt>\n" +
            "\t\t\t<FinInstnId>\n" +
            "\t\t\t\t<BICFI>BBBBUS33</BICFI>\n" +
            "\t\t\t</FinInstnId>\n" +
            "\t\t</DbtrAgt>\n" +
            "\t\t<CdtTrfTxInf>\n" +
            "\t\t\t<PmtId>\n" +
            "\t\t\t\t<InstrId>ABC/120928/CCT001/01</InstrId>\n" +
            "\t\t\t\t<EndToEndId>ABC/4562/2012-09-08</EndToEndId>\n" +
            "\t\t\t</PmtId>\n" +
            "\t\t\t<Amt>\n" +
            "\t\t\t\t<InstdAmt Ccy=\"JPY\">10000000</InstdAmt>\n" +
            "\t\t\t</Amt>\n" +
            "\t\t\t<ChrgBr>SHAR</ChrgBr>\n" +
            "\t\t\t<CdtrAgt>\n" +
            "\t\t\t\t<FinInstnId>\n" +
            "\t\t\t\t\t<BICFI>AAAAGB2L</BICFI>\n" +
            "\t\t\t\t</FinInstnId>\n" +
            "\t\t\t</CdtrAgt>\n" +
            "\t\t\t<Cdtr>\n" +
            "\t\t\t\t<Nm>DEF Electronics</Nm>\n" +
            "\t\t\t\t<PstlAdr>\n" +
            "\t\t\t\t\t<AdrLine>Corn Exchange 5th Floor</AdrLine>\n" +
            "\t\t\t\t\t<AdrLine>Mark Lane 55</AdrLine>\n" +
            "\t\t\t\t\t<AdrLine>EC3R7NE London</AdrLine>\n" +
            "\t\t\t\t\t<AdrLine>GB</AdrLine>\n" +
            "\t\t\t\t</PstlAdr>\n" +
            "\t\t\t</Cdtr>\n" +
            "\t\t\t<CdtrAcct>\n" +
            "\t\t\t\t<Id>\n" +
            "\t\t\t\t\t<Othr>\n" +
            "\t\t\t\t\t\t<Id>23683707994125</Id>\n" +
            "\t\t\t\t\t</Othr>\n" +
            "\t\t\t\t</Id>\n" +
            "\t\t\t</CdtrAcct>\n" +
            "\t\t\t<Purp>\n" +
            "\t\t\t\t<Cd>GDDS</Cd>\n" +
            "\t\t\t</Purp>\n" +
            "\t\t\t<RmtInf>\n" +
            "\t\t\t\t<Strd>\n" +
            "\t\t\t\t\t<RfrdDocInf>\n" +
            "\t\t\t\t\t\t<Tp>\n" +
            "\t\t\t\t\t\t\t<CdOrPrtry>\n" +
            "\t\t\t\t\t\t\t\t<Cd>CINV</Cd>\n" +
            "\t\t\t\t\t\t\t</CdOrPrtry>\n" +
            "\t\t\t\t\t\t</Tp>\n" +
            "\t\t\t\t\t\t<Nb>4562</Nb>\n" +
            "\t\t\t\t\t\t<RltdDt>2012-09-08</RltdDt>\n" +
            "\t\t\t\t\t</RfrdDocInf>\n" +
            "\t\t\t\t</Strd>\n" +
            "\t\t\t</RmtInf>\n" +
            "\t\t</CdtTrfTxInf>\n" +
            "\t</PmtInf>\n" +
            "</CstmrCdtTrfInitn>\n" +
            "</Document>";
}
