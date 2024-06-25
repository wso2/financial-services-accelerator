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
package com.wso2.openbanking.accelerator.consent.extensions.utils;

import java.time.OffsetDateTime;

/**
 * comment.
 */
public class ConsentValidateTestConstants {
    public static final OffsetDateTime EXPIRATION_DATE = OffsetDateTime.now().plusDays(50);
    public static final String CONSENT_ID = "0ba972a9-08cd-4cad-b7e2-20655bcbd9e0";
    public static final String VRP_PATH = "/domestic-vrps";
    public static final String USER_ID = "admin@wso2.com";
    public static final String CLIENT_ID = "xzX8t9fx6VxYMx_B6Lgpd5_yyUEa";
    public static final String SAMPLE_AUTHORIZATION_TYPE = "authorizationType";
    public static final String VRP_INITIATION = "{\n" +
            "   \"Data\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
            "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
            "           \"MaximumIndividualAmount\": {\n" +
            "               \"Amount\": \"9\",\n" +
            "               \"Currency\": \"GBP\"\n" +
            "           },\n" +
            "           \"PeriodicLimits\": [\n" +
            "               {\n" +
            "                   \"Amount\": \"1000\",\n" +
            "                   \"Currency\": \"GBP\",\n" +
            "                   \"PeriodAlignment\": \"Consent\",\n" +
            "                   \"PeriodType\": \"Half-year\"\n" +
            "               }\n" +
            "           ]\n" +
            "       },\n" +
            "       \"Initiation\": {\n" +
            "           \"DebtorAccount\": {\n" +
            "               \"SchemeName\": \"OB.IBAN\",\n" +
            "               \"Identification\": \"30080012343456\",\n" +
            "               \"Name\": \"Marcus Sweepimus\"\n" +
            "           },\n" +
            "           \"CreditorAccount\": {\n" +
            "               \"SchemeName\": \"OB.IBAN\",\n" +
            "               \"Identification\": \"30949330000010\",\n" +
            "               \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "               \"Name\": \"Marcus Sweepimus\"\n" +
            "           },\n" +
            "           \"RemittanceInformation\": {\n" +
            "               \"Reference\": \"Sweepco\"\n" +
            "           }\n" +
            "       }\n" +
            "   },\n" +
            "   \"Risk\": {\n" +
            "       \"PaymentContextCode\": \"PartyToParty\"\n" +
            "   }\n" +
            "}";

    public static final String VRP_INITIATION_WITHOUT_DEBTOR_ACC = "{\n" +
            "   \"Data\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
            "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
            "           \"MaximumIndividualAmount\": {\n" +
            "               \"Amount\": \"9\",\n" +
            "               \"Currency\": \"GBP\"\n" +
            "           },\n" +
            "           \"PeriodicLimits\": [\n" +
            "               {\n" +
            "                   \"Amount\": \"1000\",\n" +
            "                   \"Currency\": \"GBP\",\n" +
            "                   \"PeriodAlignment\": \"Consent\",\n" +
            "                   \"PeriodType\": \"Half-year\"\n" +
            "               }\n" +
            "           ]\n" +
            "       },\n" +
            "       \"Initiation\": {\n" +
            "           \"\": {\n" +
            "               \"SchemeName\": \"OB.IBAN\",\n" +
            "               \"Identification\": \"30080012343456\",\n" +
            "               \"Name\": \"Marcus Sweepimus\"\n" +
            "           },\n" +
            "           \"CreditorAccount\": {\n" +
            "               \"SchemeName\": \"OB.IBAN\",\n" +
            "               \"Identification\": \"30949330000010\",\n" +
            "               \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "               \"Name\": \"Marcus Sweepimus\"\n" +
            "           },\n" +
            "           \"RemittanceInformation\": {\n" +
            "               \"Reference\": \"Sweepco\"\n" +
            "           }\n" +
            "       }\n" +
            "   },\n" +
            "   \"Risk\": {\n" +
            "       \"PaymentContextCode\": \"PartyToParty\"\n" +
            "   }\n" +
            "}";

    public static final String VRP_INITIATION_WITHOUT_CREDITOR_ACC = "{\n" +
            "   \"Data\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
            "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
            "           \"MaximumIndividualAmount\": {\n" +
            "               \"Amount\": \"9\",\n" +
            "               \"Currency\": \"GBP\"\n" +
            "           },\n" +
            "           \"PeriodicLimits\": [\n" +
            "               {\n" +
            "                   \"Amount\": \"1000\",\n" +
            "                   \"Currency\": \"GBP\",\n" +
            "                   \"PeriodAlignment\": \"Consent\",\n" +
            "                   \"PeriodType\": \"Half-year\"\n" +
            "               }\n" +
            "           ]\n" +
            "       },\n" +
            "       \"Initiation\": {\n" +
            "           \"DebtorAccount\": {\n" +
            "               \"SchemeName\": \"OB.IBAN\",\n" +
            "               \"Identification\": \"30080012343456\",\n" +
            "               \"Name\": \"Marcus Sweepimus\"\n" +
            "           },\n" +
            "           \"RemittanceInformation\": {\n" +
            "               \"Reference\": \"Sweepco\"\n" +
            "           }\n" +
            "       }\n" +
            "   },\n" +
            "   \"Risk\": {\n" +
            "       \"PaymentContextCode\": \"PartyToParty\"\n" +
            "   }\n" +
            "}";
    public static final String VRP_SUBMISSION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_RISK = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "}";

    public static final String VRP_SUBMISSION_WITH_INVALID_INSTRUCTION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"UK.OBIE.IBAN\",\n" +
            "        \"Identification\": \"GB76LOYD30949301273801\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"SortCodeAccountNumber\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITH_INVALID_RISK = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"CreditToThirdParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_INSTRUCTION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "    \"PSUInteractionType\": \"OffSession\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"GB76LOYD30949301273801\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"SortCodeAccountNumber\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_CREDITOR_ACC = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_INSTRUCTION_REMITTANCE_INFO = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_DEBTOR_ACC_MISMATCH = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_REMITTANCE_INFO = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_REMITTANCE_INFO_MISMATCH = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"ThirdParty\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_INSTRUCTED_AMOUNT = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_INSTRUCTION_IDENTIFICATION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_END_TO_IDENTIFICATION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_DEBTOR_ACC = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITH_INTEGER_INSTRUCTION_IDENTIFICATION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": 788,\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITH_INTEGER_END_TO_IDENTIFICATION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": 5666,\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_INSTRUCTION_REMITTANCE_INFO_MISMATCH = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"ThirdParty\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITH_DEBTOR_ACC = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITH_INSTRUCTION_CREDITOR_ACC = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_SUBMISSION_WITHOUT_INSTRUCTION_CREDITOR_ACC = "{\n" +
            "  \"Data\": {\n" +
            "    \"ConsentId\": \"" + CONSENT_ID + "\",\n" +
            "    \"PSUAuthenticationMethod\": \"OB.SCA\",\n" +
            "\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30080012343456\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    \"Instruction\": {\n" +
            "        \"InstructionIdentification\": \"ACME412\",\n" +
            "        \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
            "      \"\": {\n" +
            "        \"SchemeName\": \"OB.IBAN\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"InstructedAmount\": {\n" +
            "        \"Amount\": \"10.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"PartyToParty\"\n" +
            "  }\n" +
            "}";

    public static final String VRP_INSTRUCTION = "{\n" +
            "   \"Data\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
            "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
            "           \"MaximumIndividualAmount\": {\n" +
            "               \"Amount\": \"9\",\n" +
            "               \"Currency\": \"GBP\"\n" +
            "           },\n" +
            "           \"PeriodicLimits\": [\n" +
            "               {\n" +
            "                   \"Amount\": \"1000\",\n" +
            "                   \"Currency\": \"GBP\",\n" +
            "                   \"PeriodAlignment\": \"Consent\",\n" +
            "                   \"PeriodType\": \"Half-year\"\n" +
            "               }\n" +
            "           ]\n" +
            "       },\n" +
            "       \"Initiation\": {\n" +
            "           \"DebtorAccount\": {\n" +
            "               \"SchemeName\": \"OB.IBAN\",\n" +
            "               \"Identification\": \"30080012343456\",\n" +
            "               \"Name\": \"Marcus Sweepimus\"\n" +
            "           },\n" +
            "           \"CreditorAccount\": {\n" +
            "               \"SchemeName\": \"OB.IBAN\",\n" +
            "               \"Identification\": \"30949330000010\",\n" +
            "               \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "               \"Name\": \"Marcus Sweepimus\"\n" +
            "           },\n" +
            "           \"RemittanceInformation\": {\n" +
            "               \"Reference\": \"Sweepco\"\n" +
            "           }\n" +
            "       }\n" +
            "   },\n" +
            "   \"Risk\": {\n" +
            "       \"PaymentContextCode\": \"PartyToParty\"\n" +
            "   }\n" +
            "}";
}
