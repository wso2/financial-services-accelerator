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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
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

    private ConsentCoreServiceImpl consentCoreServiceImpl;
    private ArrayList<String> consentIdList;
    private String consentId;
    private Map<String, Object> configs;
    private static final String IDEMPOTENCY_IS_ENABLED = "Consent.Idempotency.Enabled";
    private static final String IDEMPOTENCY_ALLOWED_TIME = "Consent.Idempotency.AllowedTimeDuration";

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
    public void testValidateIdempotency() throws ConsentManagementException {
        OffsetDateTime offsetDateTime = OffsetDateTime.now();

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(Mockito.anyString());
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency("IdempotencyKey",
                "123456", PAYLOAD);

        Assert.assertTrue(result.isIdempotent());
        Assert.assertTrue(result.isValid());
        Assert.assertNotNull(result.getConsent());
        Assert.assertEquals(consentId, result.getConsentId());
    }

    @Test
    public void testValidateIdempotencyWithoutIdempotencyKeyName() {
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency(null, "", "");

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutIdempotencyKeyValue() {
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency("IdempotencyKey", null, "");

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutRequest() {
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency("IdempotencyKey", "123456", "");

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyRetrievingAttributesWithException() throws ConsentManagementException {

        Mockito.doThrow(ConsentManagementException.class).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency("IdempotencyKey",
                "123456", "test");

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithoutAttribute() throws ConsentManagementException {

        Mockito.doReturn(new ArrayList<>()).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency("IdempotencyKey",
                "123456", "test");

        Assert.assertFalse(result.isIdempotent());
    }

    @Test
    public void testValidateIdempotencyWithNullConsentRequest() throws ConsentManagementException {

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(null).when(consentCoreServiceImpl).getDetailedConsent(Mockito.anyString());
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency("IdempotencyKey",
                "123456", "test");

        Assert.assertTrue(result.isIdempotent());
        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testValidateIdempotencyWithNonMatchingPayload() throws ConsentManagementException {

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(getConsent(System.currentTimeMillis())).when(consentCoreServiceImpl)
                .getDetailedConsent(Mockito.anyString());
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency("IdempotencyKey",
                "123456", DIFFERENT_PAYLOAD);

        Assert.assertTrue(result.isIdempotent());
        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testValidateIdempotencyAfterAllowedTime() throws ConsentManagementException {

        OffsetDateTime offsetDateTime = OffsetDateTime.now().minusHours(2);

        Mockito.doReturn(consentIdList).when(consentCoreServiceImpl)
                .getConsentIdByConsentAttributeNameAndValue(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(getConsent(offsetDateTime.toEpochSecond())).when(consentCoreServiceImpl)
                .getDetailedConsent(Mockito.anyString());
        IdempotencyValidationResult result = IdempotencyValidator.validateIdempotency("IdempotencyKey",
                "123456", DIFFERENT_PAYLOAD);

        Assert.assertTrue(result.isIdempotent());
        Assert.assertFalse(result.isValid());
    }

    private DetailedConsentResource getConsent(long createdTime) {
        DetailedConsentResource consent = new DetailedConsentResource();
        consent.setConsentID(consentId);
        consent.setReceipt(PAYLOAD);
        consent.setCreatedTime(createdTime);
        return consent;
    }
}
