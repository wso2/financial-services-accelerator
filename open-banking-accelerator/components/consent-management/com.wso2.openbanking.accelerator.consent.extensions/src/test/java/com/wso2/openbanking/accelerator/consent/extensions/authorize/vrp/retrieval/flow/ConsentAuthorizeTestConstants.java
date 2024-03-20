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

package com.wso2.openbanking.accelerator.consent.extensions.authorize.vrp.retrieval.flow;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Constant class for  consent authorize tests.
 */
public class ConsentAuthorizeTestConstants {
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

    public static final OffsetDateTime EXP_DATE = OffsetDateTime.now().plusDays(50);

    public static final OffsetDateTime INVALID_EXP_DATE = OffsetDateTime.now().plusDays(0);

    public static final OffsetDateTime NULL_EXP_DATE = null;
    public static final String VALID_INITIATION_OBJECT = "{\"Data\": {\"Permissions\": [\"ReadAccountsDetail\"," +
            "\"ReadBalances\",\"ReadBeneficiariesDetail\",\"ReadDirectDebits\",\"ReadProducts\"," +
            "\"ReadStandingOrdersDetail\",\"ReadTransactionsCredits\",\"ReadTransactionsDebits\"," +
            "\"ReadTransactionsDetail\",\"ReadOffers\",\"ReadPAN\",\"ReadParty\",\"ReadPartyPSU\"," +
            " \"ReadScheduledPaymentsDetail\",\"ReadStatementsDetail\"],\"ExpirationDateTime\": " +
            "\"" + EXP_DATE + "\",\"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "\"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"},\"Risk\": {}}";

    public static final String INVALID_INITIATION_OBJECT = "{\"Data\": {\"Permissions\": [\"ReadAccountsDetail\"," +
            "\"ReadBalances\",\"ReadBeneficiariesDetail\",\"ReadDirectDebits\",\"ReadProducts\"," +
            "\"ReadStandingOrdersDetail\",\"ReadTransactionsCredits\",\"ReadTransactionsDebits\"," +
            "\"ReadTransactionsDetail\",\"ReadOffers\",\"ReadPAN\",\"ReadParty\",\"ReadPartyPSU\"," +
            " \"ReadScheduledPaymentsDetail\",\"ReadStatementsDetail\"],\"ExpirationDateTime\": " +
            "\"" + INVALID_EXP_DATE + "\",\"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "\"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"},\"Risk\": {}}";


    public static final String AWAITING_AUTH_STATUS = "awaitingAuthorisation";

    public static final long CREATED_TIME = Instant.now().toEpochMilli();
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

    public static final String INVALID_COF_RECEIPT = "{" +
            "   \"Data\": {" +
            "       \"DebtorAccount\": {" +
            "           \"SchemeName\": \"OB.IBAN\"," +
            "            \"Identification\": \"GB76LOYD30949301273801\"," +
            "            \"Name\": \"Andrea Smith\"," +
            "            \"SecondaryIdentification\": \"Roll 56988\"" +
            "        }," +
            "       \"ExpirationDateTime\": \"" + INVALID_EXP_DATE + "\"" +
            "   }" +
            "}";

    public static final String NULL_COF_RECEIPT = "{" +
            "   \"Data\": {" +
            "       \"DebtorAccount\": {" +
            "           \"SchemeName\": \"OB.IBAN\"," +
            "            \"Identification\": \"GB76LOYD30949301273801\"," +
            "            \"Name\": \"Andrea Smith\"," +
            "            \"SecondaryIdentification\": \"Roll 56988\"" +
            "        }," +
            "       \"ExpirationDateTime\": \"" + NULL_EXP_DATE + "\"" +
            "   }" +
            "}";
    public static final String VRP_INITIATION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ReadRefundAccount\": \"Yes\",\n" +
            "    \"ControlParameters\": {\n" +
            "      \"ValidFromDateTime\": \"2017-06-05T15:15:13+00:00\",\n" +
            "      \"ValidToDateTime\": \"2022-07-05T15:15:13+00:00\",\n" +
            "      \"MaximumIndividualAmount\": {\n" +
            "        \"Amount\": \"100.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"PeriodicLimits\": [\n" +
            "        {\n" +
            "          \"Amount\": \"200.00\",\n" +
            "          \"Currency\": \"GBP\",\n" +
            "          \"PeriodAlignment\": \"Consent\",\n" +
            "          \"PeriodType\": \"Week\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "     \"Initiation\": {\n" +
            "        \"DebtorAccount\": {" +
            "               \"Name\": \"Andrea Smith\", " +
            "               \"SchemeName\": \"OB.SortCodeAccountNumber\", " +
            "               \"Identification\": \"30080012343456\", " +
            "               \"SecondaryIdentification\": \"30080012343456\"" +
            "       }," +
            "       \"CreditorAccount\": {" +
            "               \"Name\": \"Andrea Smith\", " +
            "               \"SchemeName\": \"OB.SortCodeAccountNumber\", " +
            "               \"Identification\": \"30080012343456\", " +
            "               \"SecondaryIdentification\": \"30080012343456\"" +
            "       }," +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_WITHOUT_CONTROLPARAMETERS = "{\n" +
            "  \"Data\": {\n" +
            "    \"ReadRefundAccount\": \"Yes\",\n" +
            "    \"\": {\n" +
            "      \"ValidFromDateTime\": \"2017-06-05T15:15:13+00:00\",\n" +
            "      \"ValidToDateTime\": \"2022-07-05T15:15:13+00:00\",\n" +
            "      \"MaximumIndividualAmount\": {\n" +
            "        \"Amount\": \"100.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"PeriodicLimits\": [\n" +
            "        {\n" +
            "          \"Amount\": \"200.00\",\n" +
            "          \"Currency\": \"GBP\",\n" +
            "          \"PeriodAlignment\": \"Consent\",\n" +
            "          \"PeriodType\": \"Week\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "     \"Initiation\": {\n" +
            "        \"DebtorAccount\": {" +
            "               \"Name\": \"Andrea Smith\", " +
            "               \"SchemeName\": \"OB.SortCodeAccountNumber\", " +
            "               \"Identification\": \"30080012343456\", " +
            "               \"SecondaryIdentification\": \"30080012343456\"" +
            "       }," +
            "       \"CreditorAccount\": {" +
            "               \"Name\": \"Andrea Smith\", " +
            "               \"SchemeName\": \"OB.SortCodeAccountNumber\", " +
            "               \"Identification\": \"30080012343456\", " +
            "               \"SecondaryIdentification\": \"30080012343456\"" +
            "       }," +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";


    public static final String VRP_WITHOUT_DATA = "{\n" +
            "  \"\": {\n" +
            "    \"ReadRefundAccount\": \"Yes\",\n" +
            "    \"ControlParameters\": {\n" +
            "      \"ValidFromDateTime\": \"2017-06-05T15:15:13+00:00\",\n" +
            "      \"ValidToDateTime\": \"2022-07-05T15:15:13+00:00\",\n" +
            "      \"MaximumIndividualAmount\": {\n" +
            "        \"Amount\": \"100.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"PeriodicLimits\": [\n" +
            "        {\n" +
            "          \"Amount\": \"200.00\",\n" +
            "          \"Currency\": \"GBP\",\n" +
            "          \"PeriodAlignment\": \"Consent\",\n" +
            "          \"PeriodType\": \"Week\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "     \"Initiation\": {\n" +
            "        \"DebtorAccount\": {" +
            "               \"Name\": \"Andrea Smith\", " +
            "               \"SchemeName\": \"OB.SortCodeAccountNumber\", " +
            "               \"Identification\": \"30080012343456\", " +
            "               \"SecondaryIdentification\": \"30080012343456\"" +
            "       }," +
            "       \"CreditorAccount\": {" +
            "               \"Name\": \"Andrea Smith\", " +
            "               \"SchemeName\": \"OB.SortCodeAccountNumber\", " +
            "               \"Identification\": \"30080012343456\", " +
            "               \"SecondaryIdentification\": \"30080012343456\"" +
            "       }," +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    static OffsetDateTime expirationInstant = OffsetDateTime.now().plusDays(50);
    public static final String PAYMENT_INITIATION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ReadRefundAccount\": \"Yes\",\n" +
            "    \"Authorisation\": {\n" +
            "       \"AuthorisationType\": \"Any\",\n" +
            "       \"CompletionDateTime\": \"" + expirationInstant + "\"\n" +
            "    },\n" +
            "    \"Initiation\": {\n" +
            "      \"InstructionIdentification\": \"ACME412\",\n" +
            "      \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"165\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "       \"DebtorAccount\": {\n" +
                    "\"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
                    "\"Identification\": \"30080012343456\",\n" +
                    "\"Name\": \"Andrea Smith\",\n" +
                    "\"SecondaryIdentification\": \"30080012343456\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
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
            "    \"PaymentContextCode\": \"EcommerceGoods\",\n" +
            "    \"MerchantCategoryCode\": \"5967\",\n" +
            "    \"MerchantCustomerIdentification\": \"053598653254\",\n" +
            "    \"DeliveryAddress\": {\n" +
            "      \"AddressLine\": [\n" +
            "        \"Flat 7\",\n" +
            "        \"Acacia Lodge\"\n" +
            "      ],\n" +
            "      \"StreetName\": \"Acacia Avenue\",\n" +
            "      \"BuildingNumber\": \"27\",\n" +
            "      \"PostCode\": \"GU31 2ZZ\",\n" +
            "      \"TownName\": \"Sparsholt\",\n" +
            "      \"CountySubDivision\": [\n" +
            "        \"Wessex\"\n" +
            "      ],\n" +
            "      \"Country\": \"UK\"\n" +
            "    }\n" +
            "  }\n" +
            "}";
    public static final String INTERNATIONAL_PAYMENT_INITIATION = "" +
            "{\n" +
            "  \"Data\": {\n" +
            "    \"ReadRefundAccount\": \"Yes\",\n" +
            "    \"Initiation\": {\n" +
            "      \"InstructionIdentification\": \"ACME412\",\n" +
            "      \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"InstructionPriority\": \"Normal\",\n" +
            "      \"CurrencyOfTransfer\": \"USD\",\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"165.88\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "       \"DebtorAccount\": {\n" +
            "       \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
            "       \"Identification\": \"30080012343456\",\n" +
            "       \"Name\": \"Andrea Smith\",\n" +
            "       \"SecondaryIdentification\": \"30080012343456\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
            "        \"Identification\": \"08080021325698\",\n" +
            "        \"Name\": \"ACME Inc\",\n" +
            "        \"SecondaryIdentification\": \"0002\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"FRESCO-101\",\n" +
            "        \"Unstructured\": \"Internal ops code 5120101\"\n" +
            "      },\n" +
            "      \"ExchangeRateInformation\": {\n" +
            "        \"UnitCurrency\": \"GBP\",\n" +
            "        \"RateType\": \"Actual\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"TransferToThirdParty\"\n" +
            "  }\n" +
            "}";
}
