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

    public static final String ACCOUNT_AUTH_SERVLET_DATA = "{" +
            "   \"consentData\": [" +
            "       {" +
            "           \"data\":[" +
            "               \"ReadAccountsBasic\"," +
            "               \"ReadAccountsDetail\"," +
            "               \"ReadBalances\"," +
            "               \"ReadBeneficiariesBasic\"," +
            "               \"ReadBeneficiariesDetail\"," +
            "               \"ReadDirectDebits\"," +
            "               \"ReadProducts\"," +
            "               \"ReadStandingOrdersBasic\"," +
            "               \"ReadStandingOrdersDetail\"," +
            "               \"ReadTransactionsBasic\"," +
            "               \"ReadTransactionsCredits\"," +
            "               \"ReadTransactionsDebits\"," +
            "               \"ReadTransactionsDetail\"," +
            "               \"ReadStatementsBasic\"," +
            "               \"ReadStatementsDetail\"" +
            "           ]," +
            "           \"title\":\"Permissions\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"2021-07-19T13:51:43.347+05:30\"]," +
            "           \"title\":\"Expiration Date Time\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"2021-07-14T13:51:43.397+05:30\"]," +
            "           \"title\":\"Transaction From Date Time\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"2021-07-17T13:51:43.397+05:30\"]," +
            "           \"title\":\"Transaction To Date Time\"" +
            "       }" +
            "   ]," +
            "   \"application\":\"9b5usDpbNtmxDcTzs7GzKp\"," +
            "   \"accounts\":[" +
            "       {" +
            "           \"accountId\":\"30080012343456\"," +
            "           \"account_id\":\"30080012343456\"," +
            "           \"authorizationMethod\":\"single\"," +
            "           \"accountName\":\"account_1\"," +
            "           \"nickName\":\"not-working\"," +
            "           \"display_name\":\"account_1\"" +
            "       }," +
            "       {" +
            "           \"accountId\":\"30080098763459\"," +
            "           \"account_id\":\"30080098763459\"," +
            "           \"authorizationMethod\":\"single\"," +
            "           \"accountName\":\"account_2\"," +
            "           \"display_name\":\"account_2\"" +
            "       }" +
            "   ]," +
            "   \"type\":\"accounts\"" +
            "}";

    public static final String COF_AUTH_SERVLET_DATA = "{" +
            "   \"consentData\":[" +
            "       {" +
            "           \"data\":[\"2021-07-19T20:14:11.069+05:30\"]," +
            "           \"title\":\"Expiration Date Time\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : UK.OBIE.SortCodeAccountNumber\"," +
            "               \"Identification : 1234\"," +
            "               \"Name : Account1\"," +
            "               \"Secondary Identification : Account1\"" +
            "           ]," +
            "           \"title\":\"Debtor Account\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               {" +
            "                   \"isReauthorization\":false" +
            "               }" +
            "           ]," +
            "           \"title\":\"Reauthorization\"" +
            "       }" +
            "   ]," +
            "   \"application\":\"9b5usDpbNtmxDcTzs7GzKp\"," +
            "   \"type\":\"fundsconfirmations\"," +
            "   \"debtor_account\":\"1234\"" +
            "}";

    public static final String PAYMENT_AUTH_SERVLET_DATA = "{" +
            "   \"consentData\":[" +
            "       {" +
            "           \"data\":[\"Domestic Payments\"]," +
            "           \"title\":\"Payment Type\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"ACME412\"]," +
            "           \"title\":\"Instruction Identification\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"FRESCO.21302.GFX.20\"]," +
            "           \"title\":\"End to End Identification\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"Amount : 30.80\",\"Currency : GBP\"]," +
            "           \"title\":\"Instructed Amount\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : UK.OBIE.SortCodeAccountNumber\"," +
            "               \"Identification : 30080012343456\"," +
            "               \"Name : Andrea Smith\"," +
            "               \"Secondary Identification : 30080012343456\"" +
            "           ]," +
            "           \"title\":\"Debtor Account\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : UK.OBIE.SortCodeAccountNumber\"," +
            "               \"Identification : 08080021325698\"," +
            "               \"Name : ACME Inc\"," +
            "               \"Secondary Identification : 0002\"" +
            "           ]," +
            "           \"title\":\"Creditor Account\"" +
            "       }" +
            "   ]," +
            "   \"application\":\"9b5usDpbNtmxDcTzs7GzKp\"," +
            "   \"type\":\"payments\"," +
            "   \"debtor_account\":\"30080012343456\"" +
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

    public static final String ACCOUNT_PERSIST_PAYLOAD = " " +
            "{" +
            "   \"metadata\": {" +
            "       \"commonAuthId\":\"b37b9c9b-b5ce-4889-966e-9cb30f70cc78\"" +
            "   }," +
            "   \"cofAccount\":\"\"," +
            "   \"approval\":\"true\"," +
            "   \"accountIds\":[" +
            "       \"30080012343456\"" +
            "   ]," +
            "   \"isReauthorization\":\"false\"," +
            "   \"type\":\"accounts\"," +
            "   \"paymentAccount\":\"\"" +
            "}";

    public static final String COF_PERSIST_PAYLOAD = " " +
            "{" +
            "   \"metadata\": {" +
            "       \"commonAuthId\":\"b37b9c9b-b5ce-4889-966e-9cb30f70cc78\"" +
            "   }," +
            "   \"approval\":\"true\"," +
            "   \"cofAccount\":\"1234\"," +
            "   \"accountIds\": \"\"," +
            "   \"isReauthorization\":\"false\"," +
            "   \"type\":\"accounts\"," +
            "   \"paymentAccount\":\"\"" +
            "}";

    public static final String PAYMENT_PERSIST_PAYLOAD =
            "   {" +
                    "       \"metadata\":{" +
                    "           \"commonAuthId\":\"4b3f5911-85b7-4489-86e8-3916f953f484\"" +
                    "       }," +
                    "       \"cofAccount\":\"\"," +
                    "       \"approval\":\"true\"," +
                    "       \"accountIds\":[\"\"]," +
                    "       \"isReauthorization\":\"\"," +
                    "       \"type\":\"payments\"," +
                    "       \"paymentAccount\":\"30080012343456\"," +
                    "       \"MultiAuthType\":\"Any\"," +
                    "       \"MultiAuthExpiry\":\"1626755005019\"" +
                    "   }";

}
