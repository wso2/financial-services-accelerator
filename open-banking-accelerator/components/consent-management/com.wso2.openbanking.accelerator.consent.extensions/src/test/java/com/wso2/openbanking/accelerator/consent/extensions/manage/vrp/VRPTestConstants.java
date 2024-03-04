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

package com.wso2.openbanking.accelerator.consent.extensions.manage.vrp;

/**
 * Constant class for consent manage tests.
 */
public class VRPTestConstants {

    public static String vrpInitiationPayloadWithoutData = "{\n" +
            "   \"\": {\n" +
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

    public static String vrpInitiationPayloadWithoutDate = "{\n" +
            "   \"\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"ValidFromDateTime\": \"\",\n" +
            "           \"ValidToDateTime\": null, // Set to null instead of an empty string\n" +
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


    public static String vrpInitiationPayloadWithStringData = "{\n" +
            "   \"\": {\n" +
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

    public static String vrpInitiationPayloadWithOutJsonObject = "{\n" +
            "   \"\": { }" +
            ",\n" +
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

        public static final String METADATA_VRP_CREDITOR_ACCOUNT = "{\n" +
                "    \"Data\": {\n" +
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
                "           \"\": {\n" +
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


        public static final String METADATA_VRP_DEBTOR_ACCOUNT = "{\n" +
                "    \"Data\": {\n" +
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
        ;


        public static final String METADATA_VRP_DEBTOR_ACCOUNT_SCHEME_NAME = "{\n" +
                "    \"Data\": {\n" +
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
                "               \"\": \"OB.IBAN\",\n" +
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

        public static final String METADATA_VRP_CREDITOR_ACCOUNT_SCHEME_NAME = "{\n" +
                "    \"Data\": {\n" +
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
                "               \"\": \"OB.IBAN\",\n" +
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


        public static final String METADATA_VRP_DEBTOR_ACCOUNT_IDENTIFICATION = "{\n" +
                "    \"Data\": {\n" +
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
                "               \"\": \"30080012343456\",\n" +
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

        public static final String METADATA_VRP_CREDITOR_ACCOUNT_IDENTIFICATION = "{\n" +
                "    \"Data\": {\n" +
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
                "               \"\": \"30949330000010\",\n" +
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


        public static final String METADATA_VRP_WITHOUT_INITIATION = "{\n" +
                "    \"Data\": {\n" +
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
                "       \"\": {\n" +
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


        public static final String METADATA_VRP_WITHOUT_CONTROL_PARAMETERS = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"\": {\n" +
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


        public static final String METADATA_VRP_WITHOUT_CURRENCY = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
                "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
                "           \"MaximumIndividualAmount\": {\n" +
                "               \"Amount\": \"9\",\n" +
                "               \"\": \"GBP\"\n" +
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

        public static final String METADATA_VRP_WITHOUT_PERIODIC_LIMIT_CURRENCY = "{\n" +
                "    \"Data\": {\n" +
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
                "                   \"\": \"GBP\",\n" +
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

        public static final String METADATA_VRP_WITHOUT_PERIODIC_LIMIT_AMOUNT = "{\n" +
                "    \"Data\": {\n" +
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
                "                   \"\": \"1000\",\n" +
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


        public static final String METADATA_VRP_WITHOUT_PERIODIC_TYPE = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
                "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
                "           \"MaximumIndividualAmount\": {\n" +
                "               \"Amount\": \"9\",\n" +
                "               \"\": \"GBP\"\n" +
                "           },\n" +
                "           \"PeriodicLimits\": [\n" +
                "               {\n" +
                "                   \"Amount\": \"1000\",\n" +
                "                   \"Currency\": \"GBP\",\n" +
                "                   \"PeriodAlignment\": \"Consent\",\n" +
                "                   \"\": \"Half-year\"\n" +
                "               }\n" +
                "           ]\n" +
                "       },\n" +
                "       \"Initiation\": {\n" +
                "           \"DebtorAccount\": {\n" +
                "               \"SchemeName\": \"\",\n" +
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


    public static final String METADATA_VRP_WITHOUT_PERIODIC_TYPE_CURRENCY = "{\n" +
                "    \"Data\": {\n" +
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
                "                   \"\": \"\",\n" +
                "                   \"PeriodAlignment\": \"Consent\",\n" +
                "                   \"PeriodicType\": \"Half-year\"\n" +
                "               }\n" +
                "           ]\n" +
                "       },\n" +
                "       \"Initiation\": {\n" +
                "           \"DebtorAccount\": {\n" +
                "               \"SchemeName\": \"\",\n" +
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

        public static final String METADATA_VRP_WITHOUT_VALID_TO_DATE = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
                "           \"\": \"2024-05-12T12:43:07.956Z\",\n" +
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

        public static final String METADATA_VRP_WITH_INVALID_VALID_FROM_DATETIME = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"\": \"2023-09-12T\",\n" +
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

        public static final String METADATA_VRP_WITH_INVALID_MAX_INDIVIDUAL_AMOUNT = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
                "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
                "           \"MaximumIndividualAmount\": \"\",\n" +  // Empty string for MaximumIndividualAmount
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


        public static final String METADATA_VRP_WITHOUT_RISK = "{\n" +
                "    \"Data\": {\n" +
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
                "   \"\": {\n" +
                "       \"PaymentContextCode\": \"PartyToParty\"\n" +
                "   }\n" +
                "}";

        public static final String METADATA_VRP_WITH_EMPTY_CONTROL_PARAMETERS = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": \"\",\n" +  // Empty string for ControlParameters
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


        public static final String METADATA_VRP_EMPTY_INITIATION = "{\n" +
                "    \"Data\": {\n" +
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
                "       \"Initiation\": \"\",\n" +  // Empty string for Initiation
                "   },\n" +
                "   \"Risk\": {\n" +
                "       \"PaymentContextCode\": \"PartyToParty\"\n" +
                "   }\n" +
                "}";

        public static final String METADATA_VRP_WITH_EMPTY_MAX_INDIVIDUAL_AMOUNT = "{\n" +
                "   \"\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
                "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
                "           \"MaximumIndividualAmount\": \"\",\n" +  // Empty string for MaximumIndividualAmount
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

        public static final String METADATA_VRP_WITHOUT_VALID_FROM_DATE = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"\": \"2023-09-12T12:43:07.956Z\",\n" +
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

        public static final String METADATA_VRP_WITHOUT_DEB_ACC = "{\n" +
                "    \"Data\": {\n" +
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
                "           \"DebtorAccount\": \"\",\n" +  // Change DebtorAccount to an empty string
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


    public static final String METADATA_VRP_WITHOUT_DEBTOR_ACC = "{\n" +
            "    \"Data\": {\n" +
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
            "           \"\": \"\",\n" +  // Change DebtorAccount to an empty string
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


        public static final String METADATA_VRP_WITHOUT_CREDITOR_ACC = "{\n" +
                "    \"Data\": {\n" +
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
                "           \"CreditorAccount\": \"\",  // Change CreditorAccount to an empty string\n" +
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
