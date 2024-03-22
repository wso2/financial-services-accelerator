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

package com.wso2.openbanking.accelerator.consent.extensions.common.idempotency;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test class for IdempotencyValidator.
 */
@PrepareForTest({OpenBankingConfigParser.class, ConsentExtensionsDataHolder.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class IdempotencyValidatorTests {

    @Mock
    private ConsentManageData consentManageData;
    private ConsentCoreServiceImpl consentCoreServiceImpl;
    private ArrayList<String> consentIdList;
    private String consentId;
    private Map<String, Object> configs;
    private Map<String, String> headers;
    private static final String IDEMPOTENCY_IS_ENABLED = "Consent.Idempotency.Enabled";
    private static final String IDEMPOTENCY_ALLOWED_TIME = "Consent.Idempotency.AllowedTimeDuration";
    private static final String CLIENT_ID = "testClientId";

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
        configs.put(IDEMPOTENCY_IS_ENABLED, "true");
        configs.put(IDEMPOTENCY_ALLOWED_TIME, "1");

        headers = new HashMap<>();
        headers.put(IdempotencyConstants.X_IDEMPOTENCY_KEY, "123456");
        headers.put(IdempotencyConstants.CONTENT_TYPE_TAG, "application/json");

        consentManageData = Mockito.mock(ConsentManageData.class);
        consentCoreServiceImpl = Mockito.mock(ConsentCoreServiceImpl.class);
        OpenBankingConfigParser openBankingConfigParserMock = PowerMockito.mock(OpenBankingConfigParser.class);
        Mockito.doReturn(configs).when(openBankingConfigParserMock).getConfiguration();
        ConsentExtensionsDataHolder consentExtensionsDataHolderMock = PowerMockito
                .mock(ConsentExtensionsDataHolder.class);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        PowerMockito.mockStatic(ConsentExtensionsDataHolder.class);
        PowerMockito.when(ConsentExtensionsDataHolder.getInstance()).thenReturn(consentExtensionsDataHolderMock);
        PowerMockito.when(consentExtensionsDataHolderMock.getConsentCoreService()).thenReturn(consentCoreServiceImpl);

        consentId = UUID.randomUUID().toString();
        consentIdList = new ArrayList<>();
        consentIdList.add(consentId);
    }

    @Test
    public void testValidateIdempotency() throws ConsentManagementException, IdempotencyValidationException {
        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(Mockito.anyString());
        Mockito.doReturn(headers).when(consentManageData).getHeaders();
        Mockito.doReturn(CLIENT_ID).when(consentManageData).getClientId();
        Mockito.doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertTrue(result.isIdempotent());
        Assert.assertTrue(result.isValid());
        Assert.assertNotNull(result.getConsent());
        Assert.assertEquals(consentId, result.getConsentId());
    }

    @Test
    public void testValidateIdempotencyWithoutIdempotencyKeyValue() throws IdempotencyValidationException {

        Mockito.doReturn(new HashMap<>()).when(consentManageData).getHeaders();
        Mockito.doReturn(CLIENT_ID).when(consentManageData).getClientId();
        Mockito.doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutRequest() throws IdempotencyValidationException {
        Mockito.doReturn(headers).when(consentManageData).getHeaders();
        Mockito.doReturn(CLIENT_ID).when(consentManageData).getClientId();
        Mockito.doReturn("").when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyRetrievingAttributesWithException()
            throws ConsentManagementException, IdempotencyValidationException {

        Mockito.doThrow(ConsentManagementException.class).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(headers).when(consentManageData).getHeaders();
        Mockito.doReturn(CLIENT_ID).when(consentManageData).getClientId();
        Mockito.doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutAttribute()
            throws ConsentManagementException, IdempotencyValidationException {

        Mockito.doReturn(new ArrayList<>()).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(headers).when(consentManageData).getHeaders();
        Mockito.doReturn(CLIENT_ID).when(consentManageData).getClientId();
        Mockito.doReturn(PAYLOAD).when(consentManageData).getPayload();
        IdempotencyValidationResult result = new IdempotencyValidator().validateIdempotency(consentManageData);

        Assert.assertFalse(result.isIdempotent());
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyWithNullConsentRequest()
            throws ConsentManagementException, IdempotencyValidationException {

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(headers).when(consentManageData).getHeaders();
        Mockito.doReturn(CLIENT_ID).when(consentManageData).getClientId();
        Mockito.doReturn(PAYLOAD).when(consentManageData).getPayload();
        Mockito.doReturn(null).when(consentCoreServiceImpl).getDetailedConsent(Mockito.anyString());
        new IdempotencyValidator().validateIdempotency(consentManageData);
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyWithNonMatchingClientId()
            throws ConsentManagementException, IdempotencyValidationException {

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(headers).when(consentManageData).getHeaders();
        Mockito.doReturn("sampleClientID").when(consentManageData).getClientId();
        Mockito.doReturn(PAYLOAD).when(consentManageData).getPayload();
        Mockito.doReturn(null).when(consentCoreServiceImpl).getDetailedConsent(Mockito.anyString());
        new IdempotencyValidator().validateIdempotency(consentManageData);
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyAfterAllowedTime()
            throws ConsentManagementException, IdempotencyValidationException {

        OffsetDateTime offsetDateTime = OffsetDateTime.now().minusHours(2);

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(Mockito.anyString());
        Mockito.doReturn(headers).when(consentManageData).getHeaders();
        Mockito.doReturn(CLIENT_ID).when(consentManageData).getClientId();
        Mockito.doReturn(PAYLOAD).when(consentManageData).getPayload();
        new IdempotencyValidator().validateIdempotency(consentManageData);
    }

    @Test(expectedExceptions = IdempotencyValidationException.class)
    public void testValidateIdempotencyWithNonMatchingPayload()
            throws ConsentManagementException, IdempotencyValidationException {

        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(Mockito.anyString());
        Mockito.doReturn(headers).when(consentManageData).getHeaders();
        Mockito.doReturn(CLIENT_ID).when(consentManageData).getClientId();
        Mockito.doReturn(DIFFERENT_PAYLOAD).when(consentManageData).getPayload();
        new IdempotencyValidator().validateIdempotency(consentManageData);

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
