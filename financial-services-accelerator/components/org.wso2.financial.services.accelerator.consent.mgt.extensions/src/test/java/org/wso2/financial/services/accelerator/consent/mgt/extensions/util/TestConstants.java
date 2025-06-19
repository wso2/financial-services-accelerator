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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class of constants for tests.
 */
public class TestConstants {

    public static final OffsetDateTime EXP_DATE = OffsetDateTime.now().plusDays(50);
    public static final OffsetDateTime FIRST_PAYMENT_DATE = OffsetDateTime.now().plusDays(5);
    public static final OffsetDateTime FINAL_PAYMENT_DATE = OffsetDateTime.now().plusDays(10);
    public static final String SAMPLE_CONSENT_ID = "464ef174-9877-4c71-940c-93d6e069eaf9";
    public static final String SAMPLE_CONSENT_RECEIPT = "{\"validUntil\": \"2020-10-20\", \"frequencyPerDay\": 1," +
            " \"recurringIndicator\": false, \"combinedServiceIndicator\": true}";
    public static final String SAMPLE_CONSENT_TYPE = "accounts";
    public static final String INVALID_REQUEST_PATH = "accounts";
    public static final String REQUEST_PATH_WITH_INVALID_CONSENT_ID = "accounts/1234";
    public static final String ACCOUNT_CONSENT_GET_PATH = "account-access-consents/" + SAMPLE_CONSENT_ID;
    public static final String PAYMENTS_FILE_UPLOAD_PATH = "fileUpload/" + SAMPLE_CONSENT_ID;
    public static final String INVALID_INITIATION_OBJECT = "Invalid Object";
    public static final int SAMPLE_CONSENT_FREQUENCY = 1;
    public static final Long SAMPLE_CONSENT_VALIDITY_PERIOD = 1638337852L;
    public static final String SAMPLE_CURRENT_STATUS = "Authorised";
    public static final boolean SAMPLE_RECURRING_INDICATOR = true;
    public static final String SAMPLE_CLIENT_ID = "sampleClientID";
    public static final String SAMPLE_AUTH_ID = "88888";
    public static final String SAMPLE_AUTH_TYPE = "authorizationType";
    public static final String SAMPLE_USER_ID = "admin@wso2.com";
    public static final String SAMPLE_AUTHORIZATION_STATUS = "Created";
    public static final String SAMPLE_MAPPING_ID = "sampleMappingId";
    public static final String SAMPLE_MAPPING_ID_2 = "sampleMappingId2";
    public static final String SAMPLE_ACCOUNT_ID = "123456789";
    public static final String SAMPLE_MAPPING_STATUS = "active";
    public static final String SAMPLE_NEW_MAPPING_STATUS = "inactive";
    public static final String SAMPLE_PERMISSION = "samplePermission";
    public static final String AUTHORISED_STATUS = "Authorised";
    public static final String AWAITING_AUTH_STATUS = "AwaitingAuthorisation";
    public static final String CREATED_STATUS = "Created";
    public static final String ACCOUNTS = "accounts";
    public static final String PAYMENTS = "payments";
    public static final String FUNDS_CONFIRMATIONS = "fundsconfirmations";
    public static final String COF_PATH = "/funds-confirmations";
    public static final long SAMPLE_CONSENT_AMENDMENT_TIMESTAMP = 1638337852;
    public static final String SAMPLE_AMENDMENT_REASON = "sampleReason";
    public static final String SAMPLE_REASON = "sample reason";
    public static final String SAMPLE_ACTION_BY = "admin@wso2.com";
    public static final String  SAMPLE_PREVIOUS_STATUS = "Received";
    public static final String SAMPLE_CONSENT_FILE = "sample file content";
    public static final long CREATED_TIME = Instant.now().toEpochMilli();
    public static final Map<String, String> SAMPLE_CONSENT_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("x-request-id", UUID.randomUUID().toString());
            put("idempotency-key", UUID.randomUUID().toString());
            put("sampleAttributeKey", "sampleAttributeValue");

        }
    };
    public static final String VALID_INITIATION = "{" +
            "   \"Data\": {" +
            "       \"Permissions\": [" +
            "           \"ReadAccountsBasic\"," +
            "           \"ReadAccountsDetail\"," +
            "           \"ReadTransactionsDetail\"," +
            "           \"ReadBalances\"" +
            "       ]," +
            "       \"ExpirationDateTime\": \"" + EXP_DATE + "\"," +
            "       \"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "       \"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"" +
            "   }," +
            "   \"Risk\": {}" +
            "}";

    public static final String ACC_INITIATION_WITH_LIMITED_PERMISSIONS = "{" +
            "   \"Data\": {" +
            "       \"Permissions\": [" +
            "           \"ReadProducts\"," +
            "           \"ReadStandingOrdersDetail\"," +
            "           \"ReadTransactionsCredits\"," +
            "           \"ReadTransactionsDebits\"," +
            "           \"ReadTransactionsDetail\"," +
            "           \"ReadOffers\"," +
            "           \"ReadPAN\"," +
            "           \"ReadParty\"," +
            "           \"ReadPartyPSU\"," +
            "           \"ReadScheduledPaymentsDetail\"," +
            "           \"ReadStatementsDetail\"" +
            "       ]," +
            "       \"ExpirationDateTime\": \"" + EXP_DATE + "\"," +
            "       \"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "       \"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"" +
            "   }," +
            "   \"Risk\": {}" +
            "}";

    public static final String ACC_INITIATION_EXPIRED = "{" +
            "   \"Data\": {" +
            "       \"Permissions\": [" +
            "           \"ReadAccountsDetail\"," +
            "           \"ReadBalances\"," +
            "           \"ReadBeneficiariesDetail\"," +
            "           \"ReadDirectDebits\"," +
            "           \"ReadAccountsBasic\"" +
            "       ]," +
            "       \"ExpirationDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "       \"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "       \"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"" +
            "   }," +
            "   \"Risk\": {}" +
            "}";

    public static final String COF_RECEIPT = "{" +
            "   \"Data\": {" +
            "       \"DebtorAccount\": {" +
            "           \"SchemeName\": \"OB.IBAN\"," +
            "            \"Identification\": \"GB76LOYD30949301273801\"," +
            "            \"Name\": \"Andrea Smith\"," +
            "            \"SecondaryIdentification\": \"Roll 56988\"" +
            "        }," +
            "       \"ExpirationDateTime\": \"" + EXP_DATE + "\"" +
            "   }" +
            "}";

    public static final String COF_RECEIPT_EXPIRED = "{" +
            "   \"Data\": {" +
            "       \"DebtorAccount\": {" +
            "           \"SchemeName\": \"OB.IBAN\"," +
            "            \"Identification\": \"GB76LOYD30949301273801\"," +
            "            \"Name\": \"Andrea Smith\"," +
            "            \"SecondaryIdentification\": \"Roll 56988\"" +
            "        }," +
            "       \"ExpirationDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "   }" +
            "}";

    public static final String COF_SUBMISSION = "{" +
            "   \"Data\": {" +
            "       \"ConsentId\": \"" + SAMPLE_CONSENT_ID + "\"," +
            "       \"DebtorAccount\": {" +
            "           \"SchemeName\": \"OB.IBAN\"," +
            "            \"Identification\": \"GB76LOYD30949301273801\"," +
            "            \"Name\": \"Andrea Smith\"," +
            "            \"SecondaryIdentification\": \"Roll 56988\"" +
            "        }," +
            "       \"ExpirationDateTime\": \"" + EXP_DATE + "\"" +
            "   }" +
            "}";

    public static final String PAYMENT_INITIATION = "{\n" +
            "   \"Data\":{\n" +
            "      \"ReadRefundAccount\":\"Yes\",\n" +
            "      \"Permission\":\"Create\",\n" +
            "      \"Initiation\":{\n" +
            "         \"Frequency\":\"EvryDay\",\n" +
            "         \"Reference\":\"Pocket money for Damien\",\n" +
            "         \"NumberOfPayments\":\"10\",\n" +
            "         \"Purpose\":\"1234\",\n" +
            "         \"ChargeBearer\":\"BorneByCreditor\",\n" +
            "         \"FirstPaymentDateTime\":\"" + FIRST_PAYMENT_DATE + "\"," +
            "         \"FinalPaymentDateTime\":\"" + FINAL_PAYMENT_DATE + "\"," +
            "         \"DebtorAccount\":{\n" +
            "            \"SchemeName\":\"OB.SortCodeAccountNumber\",\n" +
            "            \"Identification\":\"30080012343456\",\n" +
            "            \"Name\":\"Andrea Smith\",\n" +
            "            \"SecondaryIdentification\":\"30080012343456\"\n" +
            "         },\n" +
            "         \"CreditorAccount\":{\n" +
            "            \"SchemeName\":\"OB.SortCodeAccountNumber\",\n" +
            "            \"Identification\":\"08080021325698\",\n" +
            "            \"Name\":\"ACME Inc\",\n" +
            "            \"SecondaryIdentification\":\"0002\"\n" +
            "         },\n" +
            "         \"InstructedAmount\":{\n" +
            "            \"Amount\":\"30.80\",\n" +
            "            \"Currency\":\"GBP\"\n" +
            "         }\n" +
            "      }\n" +
            "   },\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";

    public static final String PAYMENT_SUBMISSION = "{\n" +
            "   \"Data\":{\n" +
            "      \"ReadRefundAccount\":\"Yes\",\n" +
            "      \"Permission\":\"Create\",\n" +
            "      \"ConsentId\": \"" + SAMPLE_CONSENT_ID + "\"," +
            "      \"Initiation\":{\n" +
            "         \"Frequency\":\"EvryDay\",\n" +
            "         \"Reference\":\"Pocket money for Damien\",\n" +
            "         \"NumberOfPayments\":\"10\",\n" +
            "         \"Purpose\":\"1234\",\n" +
            "         \"ChargeBearer\":\"BorneByCreditor\",\n" +
            "         \"FirstPaymentDateTime\":\"" + FIRST_PAYMENT_DATE + "\"," +
            "         \"FinalPaymentDateTime\":\"" + FINAL_PAYMENT_DATE + "\"," +
            "         \"DebtorAccount\":{\n" +
            "            \"SchemeName\":\"OB.SortCodeAccountNumber\",\n" +
            "            \"Identification\":\"30080012343456\",\n" +
            "            \"Name\":\"Andrea Smith\",\n" +
            "            \"SecondaryIdentification\":\"30080012343456\"\n" +
            "         },\n" +
            "         \"CreditorAccount\":{\n" +
            "            \"SchemeName\":\"OB.SortCodeAccountNumber\",\n" +
            "            \"Identification\":\"08080021325698\",\n" +
            "            \"Name\":\"ACME Inc\",\n" +
            "            \"SecondaryIdentification\":\"0002\"\n" +
            "         },\n" +
            "         \"InstructedAmount\":{\n" +
            "            \"Amount\":\"30.80\",\n" +
            "            \"Currency\":\"GBP\"\n" +
            "         }\n" +
            "      }\n" +
            "   },\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";

    public static final String PAYMENT_SUBMISSION_WITHOUT_DATA = "{\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";

    public static final String PAYMENT_SUBMISSION_WITHOUT_INITIATION = "{\n" +
            "   \"Data\":{\n" +
            "      \"ReadRefundAccount\":\"Yes\",\n" +
            "      \"Permission\":\"Create\",\n" +
            "      \"ConsentId\": \"" + SAMPLE_CONSENT_ID + "\"" +
            "   },\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";


    public static final String PAYMENT_SUBMISSION_WITH_DIFFERENT_INITIATION = "{\n" +
            "   \"Data\":{\n" +
            "      \"ReadRefundAccount\":\"Yes\",\n" +
            "      \"Permission\":\"Create\",\n" +
            "      \"ConsentId\": \"" + SAMPLE_CONSENT_ID + "\"," +
            "      \"Initiation\":{\n" +
            "         \"Frequency\":\"EvryDay\",\n" +
            "         \"Reference\":\"Pocket money for Damien\",\n" +
            "         \"NumberOfPayments\":\"10\",\n" +
            "         \"Purpose\":\"1234\",\n" +
            "         \"ChargeBearer\":\"BorneByCreditor\",\n" +
            "         \"FirstPaymentDateTime\":\"" + FIRST_PAYMENT_DATE + "\"," +
            "         \"FinalPaymentDateTime\":\"" + FINAL_PAYMENT_DATE + "\"," +
            "         \"InstructedAmount\":{\n" +
            "            \"Amount\":\"30.80\",\n" +
            "            \"Currency\":\"GBP\"\n" +
            "         }\n" +
            "      }\n" +
            "   },\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";

    public static final String ACCOUNT_AUTH_SERVLET_DATA = "{\n" +
            "  \"consentData\": {\n" +
            "    \"type\": \"accounts\",\n" +
            "    \"basicConsentData\": {\n" +
            "      \"Permissions\": [\n" +
            "        \"ReadAccountsBasic\",\n" +
            "        \"ReadAccountsDetail\",\n" +
            "        \"ReadBalances\",\n" +
            "        \"ReadBeneficiariesBasic\",\n" +
            "        \"ReadBeneficiariesDetail\",\n" +
            "        \"ReadDirectDebits\",\n" +
            "        \"ReadProducts\",\n" +
            "        \"ReadStandingOrdersBasic\",\n" +
            "        \"ReadStandingOrdersDetail\",\n" +
            "        \"ReadTransactionsBasic\",\n" +
            "        \"ReadTransactionsCredits\",\n" +
            "        \"ReadTransactionsDebits\",\n" +
            "        \"ReadTransactionsDetail\",\n" +
            "        \"ReadStatementsBasic\",\n" +
            "        \"ReadStatementsDetail\",\n" +
            "        \"ReadOffers\",\n" +
            "        \"ReadParty\",\n" +
            "        \"ReadPartyPSU\",\n" +
            "        \"ReadScheduledPaymentsBasic\",\n" +
            "        \"ReadScheduledPaymentsDetail\",\n" +
            "        \"ReadPAN\"\n" +
            "      ],\n" +
            "      \"Expiration Date Time\": \"2025-06-02T10:58:09.581346300+05:30\",\n" +
            "      \"Transaction From Date Time\": \"2025-05-28T10:58:09.581346300+05:30\",\n" +
            "      \"Transaction To Date Time\": \"2025-05-31T10:58:09.582341200+05:30\"\n" +
            "    },\n" +
            "    \"allowMultipleAccounts\": true\n" +
            "  },\n" +
            "  \"consumerData\": {\n" +
            "    \"accounts\": [\n" +
            "      {\n" +
            "        \"displayName\": \"account_1\",\n" +
            "        \"accountId\": \"30080012343456\",\n" +
            "        \"selected\": false\n" +
            "      },\n" +
            "      {\n" +
            "        \"displayName\": \"account_2\",\n" +
            "        \"accountId\": \"30080098763459\",\n" +
            "        \"selected\": false\n" +
            "      },\n" +
            "      {\n" +
            "        \"displayName\": \"multi_auth_account\",\n" +
            "        \"accountId\": \"30080098971337\",\n" +
            "        \"selected\": false\n" +
            "      },\n" +
            "      {\n" +
            "        \"displayName\": \"Extra_account\",\n" +
            "        \"accountId\": \"650-000 N1232\",\n" +
            "        \"selected\": false\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    public static final String COF_AUTH_SERVLET_DATA = "{\n" +
            "  \"consentData\": {\n" +
            "    \"type\": \"funds-confirmation\",\n" +
            "    \"basicConsentData\": {\n" +
            "      \"Expiration Date Time\": \"2025-06-02T12:14:23.061411600+05:30\",\n" +
            "      \"Debtor Account\": [\n" +
            "        \"Scheme Name : UK.OBIE.IBAN\",\n" +
            "        \"Identification : 1234\",\n" +
            "        \"Name : Account1\",\n" +
            "        \"Secondary Identification : Account1\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"initiatedAccountsForConsent\": [\n" +
            "      {\n" +
            "        \"displayName\": \"Account1\",\n" +
            "        \"accountId\": \"1234\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    public static final String PAYMENT_AUTH_SERVLET_DATA = "{\n" +
            "  \"consentData\": {\n" +
            "    \"type\": \"payments\",\n" +
            "    \"basicConsentData\": {\n" +
            "      \"Payment Type\": \"Domestic Payments\",\n" +
            "      \"Instruction Identification\": \"ACME412\",\n" +
            "      \"End to End Identification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"Instructed Amount\": [\n" +
            "        \"Amount : 30.80\",\n" +
            "        \"Currency : GBP\"\n" +
            "      ],\n" +
            "      \"Debtor Account\": [\n" +
            "        \"Scheme Name : UK.OBIE.SortCodeAccountNumber\",\n" +
            "        \"Identification : 30080012343456\",\n" +
            "        \"Name : Andrea Smith\",\n" +
            "        \"Secondary Identification : 30080012343456\"\n" +
            "      ],\n" +
            "      \"Creditor Account\": [\n" +
            "        \"Scheme Name : UK.OBIE.SortCodeAccountNumber\",\n" +
            "        \"Identification : 08080021325698\",\n" +
            "        \"Name : ACME Inc\",\n" +
            "        \"Secondary Identification : 0002\"\n" +
            "      ],\n" +
            "      \"Multi Authorisation\": [\n" +
            "        \"Multi Authorisation Type : Any\",\n" +
            "        \"Multi Authrisation Expiry : 1748848010149\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"initiatedAccountsForConsent\": [\n" +
            "      {\n" +
            "        \"displayName\": \"ACME Inc\",\n" +
            "        \"accountId\": \"08080021325698\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    public static final String AUTH_SERVLET_JSON_WITH_TYPE = "{" +
            "   \"type\":\"test\"" +
            "}";

    public static final String INVALID_REQUEST_OBJECT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.aWF0.TIygRaBn7MUFR9Zzy3" +
            "yu9K8uKVe8KXdAty0Ckrg2vFI";

    public static final String VALID_REQUEST_OBJECT = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkR3TUtkV01tajdQV2" +
            "ludm9xZlF5WFZ6eVo2USJ9.eyJtYXhfYWdlIjo4NjQwMCwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTQ0Ni9vYXV0aDIvdG9rZW4iL" +
            "CJzY29wZSI6Im9wZW5pZCBhY2NvdW50cyIsImlzcyI6InF3ZGZnaGpwbG1nZmRhYWhrZ2pvcGhuayIsImNsYWltcyI6eyJpZF90b2tlb" +
            "iI6eyJhY3IiOnsidmFsdWVzIjpbInVybjpvcGVuYmFua2luZzpwc2QyOnNjYSIsInVybjpvcGVuYmFua2luZzpwc2QyOmNhIl0sImVzc" +
            "2VudGlhbCI6dHJ1ZX0sIm9wZW5iYW5raW5nX2ludGVudF9pZCI6eyJ2YWx1ZSI6IjEyMzQ1Njc3NjU0MzIxMjM0MjM0IiwiZXNzZW50a" +
            "WFsIjp0cnVlfX0sInVzZXJpbmZvIjp7Im9wZW5iYW5raW5nX2ludGVudF9pZCI6eyJ2YWx1ZSI6IjEyMzQ1Njc3NjU0MzIxMjM0MjM0I" +
            "iwiZXNzZW50aWFsIjp0cnVlfX19LCJyZXNwb25zZV90eXBlIjoiY29kZSBpZF90b2tlbiIsInJlZGlyZWN0X3VyaSI6Imh0dHBzOi8vd" +
            "3NvMi5jb20iLCJzdGF0ZSI6IllXbHpjRG96TVRRMiIsImV4cCI6MTY1MzcxNzQ3OCwibm9uY2UiOiJuLTBTNl9XekEyTSIsImNsaWVud" +
            "F9pZCI6InF3ZGZnaGpwbG1nZmRhYWhrZ2pvcGhuayJ9.lOvcc81dqjqdv4dslB_Kg4K3TKd13UQWaUKl3dBiPPlnu9y-R84Xfx-bMMnH" +
            "atYyW9hYWJcUlprIm_dqgFXauCSTgBz6-vacrXLzuaGtj07d-8bL_qta45qbpbKPTY2pnM_PXe7fzs4RMCGEoiRLRs7lJUBfIbV9GzlS" +
            "pHkOZiOjiFxxeYm0cNpZRvXkZNd59_GLdW2kKmWaGQHpQ9Ci_QpQENRzF8KEV1QtNd3cK2DjL5tKSw824C6AmXp-PKfvhurqPaVkz5p-" +
            "iPA6bRaNBPY4hj_nsZpfuCnE8-V7YXWXXzWbK3gWo_dMOV1CZcHS6KqP7DANqDEEP4LoN081uQ";

    public static final String VALID_REQUEST_OBJECT_WITH_CONSENT_ID_IN_SCOPE = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsI" +
            "mtpZCI6IkR3TUtkV01tajdQV2ludm9xZlF5WFZ6eVo2USJ9.eyJtYXhfYWdlIjo4NjQwMCwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6" +
            "OTQ0Ni9vYXV0aDIvdG9rZW4iLCJzY29wZSI6Im9wZW5pZCBhaXM6MTIzNDU2Nzc2NTQzMjEyMzQyMzQiLCJpc3MiOiJxd2RmZ2hqcGxt" +
            "Z2ZkYWFoa2dqb3BobmsiLCJyZXNwb25zZV90eXBlIjoiY29kZSBpZF90b2tlbiIsInJlZGlyZWN0X3VyaSI6Imh0dHBzOi8vd3NvMi5j" +
            "b20iLCJzdGF0ZSI6IllXbHpjRG96TVRRMiIsImV4cCI6MTY1MzcxNzQ3OCwibm9uY2UiOiJuLTBTNl9XekEyTSIsImNsaWVudF9pZCI6" +
            "InF3ZGZnaGpwbG1nZmRhYWhrZ2pvcGhuayJ9.oKLtOzEr04DtL9Ps5oNNQv6xolrl3Fn_aWpkGYC1bbuj5DDqnnjjtifPrJHEMNz-qpI" +
            "4qrSVaiyPghMH9XtMKZ85v9iNcVBKVnYO0Ej5sCW3EuTWHhfCcuOo2ASwVk1xX_bVyyB2_YetDxaVZ005EXv_1XvJpDyXJh5Sqt99Ha1" +
            "h-FE59928Wf16BkGaKfBRW0fDEn3CKzU4ENhrT6WIJ5DiMldc1oN-nbAhqfFSQY6Aqo8qD0gQdXMaNROGgHEVYUBsiCfu7aqMXVDZEcm" +
            "8e6rXQCHwbTaGLMFhUY3zg-_suWDss8bBcMv6mGw4fwQAT4WNoxWfud3de3JGeJ4mQw";

    public static final String ACCOUNT_PERSIST_PAYLOAD = " " +
            "{\n" +
            "  \"approval\": true,\n" +
            "  \"type\": \"accounts\",\n" +
            "  \"isReauthorization\": false,\n" +
            "  \"authorizedData\": [\n" +
            "    {\n" +
            "      \"accounts\": [\n" +
            "        {\n" +
            "          \"displayName\": \"account_1\",\n" +
            "          \"accountId\": \"30080012343456\",\n" +
            "          \"selected\": false\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";

    public static final String COF_PERSIST_PAYLOAD = "{\n" +
            "  \"approval\": true,\n" +
            "  \"type\": \"funds-confirmation\",\n" +
            "  \"isReauthorization\": false,\n" +
            "  \"authorizedData\": [\n" +
            "    {\n" +
            "      \"accounts\": [\n" +
            "        {\n" +
            "          \"displayName\": \"Account1\",\n" +
            "          \"accountId\": \"1234\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";

    public static final String PAYMENT_PERSIST_PAYLOAD =
            "{\n" +
                    "  \"approval\": true,\n" +
                    "  \"type\": \"payment\",\n" +
                    "  \"isReauthorization\": false,\n" +
                    "  \"authorizedData\": [\n" +
                    "    {\n" +
                    "      \"accounts\": [\n" +
                    "        {\n" +
                    "          \"displayName\": \"ACME Inc\",\n" +
                    "          \"accountId\": \"08080021325698\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n";

    public static final String ADMIN_SEARCH_RESPONSE = "{\n" +
            "   \"metadata\":{\n" +
            "      \"total\":88,\n" +
            "      \"count\":20,\n" +
            "      \"limit\":20\n" +
            "   },\n" +
            "   \"data\":[\n" +
            "      {\n" +
            "         \"clientId\":\"MILJqVfWKGd9gXw3fmTAHr2qZwAa\",\n" +
            "         \"currentStatus\":\"Authorised\",\n" +
            "         \"createdTimestamp\":1746536842,\n" +
            "         \"recurringIndicator\":false,\n" +
            "         \"authorizationResources\":[\n" +
            "            {\n" +
            "               \"updatedTime\":1746536861,\n" +
            "               \"consentId\":\"a18f2f56-4ad7-4128-a507-f006d5bdf40d\",\n" +
            "               \"authorizationId\":\"113b12ff-359f-4da8-afd1-40d9d824575d\",\n" +
            "               \"authorizationType\":\"authorise\",\n" +
            "               \"userId\":\"test@wso2.com\",\n" +
            "               \"authorizationStatus\":\"Authorised\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"updatedTimestamp\":1746536861,\n" +
            "         \"softwareClientName\":\"oQ4KoaavpOuoE7rvQsZEOV\",\n" +
            "         \"consent_type\":\"accounts\",\n" +
            "         \"validityPeriod\":1754312861,\n" +
            "         \"consentAttributes\":{\n" +
            "            \n" +
            "         },\n" +
            "         \"consentId\":\"a18f2f56-4ad7-4128-a507-f006d5bdf40d\",\n" +
            "         \"consentMappingResources\":[\n" +
            "            {\n" +
            "               \"mappingId\":\"dc716c7a-1a7f-44ac-a461-a0513fc6f75d\",\n" +
            "               \"mappingStatus\":\"active\",\n" +
            "               \"accountId\":\"30080012343456\",\n" +
            "               \"authorizationId\":\"113b12ff-359f-4da8-afd1-40d9d824575d\",\n" +
            "               \"permission\":\"n/a\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"receipt\":{\n" +
            "            \"Risk\":{\n" +
            "               \n" +
            "            },\n" +
            "            \"Data\":{\n" +
            "               \"TransactionToDateTime\":\"2025-05-09T18:13:50.070878+05:30\",\n" +
            "               \"ExpirationDateTime\":\"2025-05-11T18:13:50.070489+05:30\",\n" +
            "               \"Permissions\":[\n" +
            "                  \"ReadAccountsBasic\",\n" +
            "                  \"ReadAccountsDetail\",\n" +
            "                  \"ReadBalances\",\n" +
            "                  \"ReadBeneficiariesBasic\",\n" +
            "                  \"ReadBeneficiariesDetail\",\n" +
            "                  \"ReadDirectDebits\",\n" +
            "                  \"ReadProducts\",\n" +
            "                  \"ReadStandingOrdersBasic\",\n" +
            "                  \"ReadStandingOrdersDetail\",\n" +
            "                  \"ReadTransactionsBasic\",\n" +
            "                  \"ReadTransactionsCredits\",\n" +
            "                  \"ReadTransactionsDebits\",\n" +
            "                  \"ReadTransactionsDetail\",\n" +
            "                  \"ReadStatementsBasic\",\n" +
            "                  \"ReadStatementsDetail\",\n" +
            "                  \"ReadOffers\",\n" +
            "                  \"ReadParty\",\n" +
            "                  \"ReadPartyPSU\",\n" +
            "                  \"ReadScheduledPaymentsBasic\",\n" +
            "                  \"ReadScheduledPaymentsDetail\",\n" +
            "                  \"ReadPAN\"\n" +
            "               ],\n" +
            "               \"TransactionFromDateTime\":\"2025-05-06T18:13:50.070760+05:30\"\n" +
            "            }\n" +
            "         },\n" +
            "         \"consentFrequency\":0\n" +
            "      },\n" +
            "      {\n" +
            "         \"clientId\":\"MILJqVfWKGd9gXw3fmTAHr2qZwAa\",\n" +
            "         \"currentStatus\":\"Authorised\",\n" +
            "         \"createdTimestamp\":1746536822,\n" +
            "         \"recurringIndicator\":false,\n" +
            "         \"authorizationResources\":[\n" +
            "            {\n" +
            "               \"updatedTime\":1746536841,\n" +
            "               \"consentId\":\"1aa69e75-84c2-4d3a-8cf3-5164933fa3ca\",\n" +
            "               \"authorizationId\":\"9c8751dd-91cf-4140-8e1e-1ff42c4394e3\",\n" +
            "               \"authorizationType\":\"authorise\",\n" +
            "               \"userId\":\"test@wso2.com\",\n" +
            "               \"authorizationStatus\":\"Authorised\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"updatedTimestamp\":1746536841,\n" +
            "         \"softwareClientName\":\"oQ4KoaavpOuoE7rvQsZEOV\",\n" +
            "         \"consent_type\":\"accounts\",\n" +
            "         \"validityPeriod\":1754312841,\n" +
            "         \"consentAttributes\":{\n" +
            "            \n" +
            "         },\n" +
            "         \"consentId\":\"1aa69e75-84c2-4d3a-8cf3-5164933fa3ca\",\n" +
            "         \"consentMappingResources\":[\n" +
            "            {\n" +
            "               \"mappingId\":\"156bd151-8b2b-4fcd-936f-f324a145bf75\",\n" +
            "               \"mappingStatus\":\"active\",\n" +
            "               \"accountId\":\"123456789\",\n" +
            "               \"authorizationId\":\"9c8751dd-91cf-4140-8e1e-1ff42c4394e3\",\n" +
            "               \"permission\":\"n/a\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"receipt\":{\n" +
            "            \"Risk\":{\n" +
            "               \n" +
            "            },\n" +
            "            \"Data\":{\n" +
            "               \"TransactionToDateTime\":\"2025-05-09T18:13:50.070878+05:30\",\n" +
            "               \"ExpirationDateTime\":\"2025-05-11T18:13:50.070489+05:30\",\n" +
            "               \"Permissions\":[\n" +
            "                  \"ReadAccountsBasic\",\n" +
            "                  \"ReadAccountsDetail\",\n" +
            "                  \"ReadBalances\",\n" +
            "                  \"ReadBeneficiariesBasic\",\n" +
            "                  \"ReadBeneficiariesDetail\",\n" +
            "                  \"ReadDirectDebits\",\n" +
            "                  \"ReadProducts\",\n" +
            "                  \"ReadStandingOrdersBasic\",\n" +
            "                  \"ReadStandingOrdersDetail\",\n" +
            "                  \"ReadTransactionsBasic\",\n" +
            "                  \"ReadTransactionsCredits\",\n" +
            "                  \"ReadTransactionsDebits\",\n" +
            "                  \"ReadTransactionsDetail\",\n" +
            "                  \"ReadStatementsBasic\",\n" +
            "                  \"ReadStatementsDetail\",\n" +
            "                  \"ReadOffers\",\n" +
            "                  \"ReadParty\",\n" +
            "                  \"ReadPartyPSU\",\n" +
            "                  \"ReadScheduledPaymentsBasic\",\n" +
            "                  \"ReadScheduledPaymentsDetail\",\n" +
            "                  \"ReadPAN\"\n" +
            "               ],\n" +
            "               \"TransactionFromDateTime\":\"2025-05-06T18:13:50.070760+05:30\"\n" +
            "            }\n" +
            "         },\n" +
            "         \"consentFrequency\":0\n" +
            "      }\n" +
            "   ]\n" +
            "}";

}
