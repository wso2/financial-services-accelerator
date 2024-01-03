package com.wso2.openbanking.accelerator.consent.extensions.manage.vrp;

/**
 * test.
 */
public class VRPDataProviders {

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


    public static final String VRP_PAYLOAD_WITHOUT_DATE = "{\n" +
            "   \"Data\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"\": \"2023-09-12T12:43:07.956Z\",\n" +
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

    public static final String VRP_PAYLOAD_WITHOUT_DATE_TIME = "{\n" +
            "   \"Data\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"\": \"2023-09-12T12:43:07.956Z\",\n" +
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






    public static String vrpInitiationPayloadWithoutMaximumIndividualAmount = "{\n" +
            "   \"\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
            "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
            "           \"\": {\n" +
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


    public static String vrpInitiationPayloadWithInvalidMaximumIndividualAmount = "{\n" +
            "   \"Data\": {\n" +
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



    public static String vrpInitiationPayloadNotInstanceOfJsonObject = "{\n" +
            "   \"\"" + " " +
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

    public static String vrpInitiationPayloadIsEmpty = "{\n" +
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


    public static String vrpInitiationPayloadWithoutInitiation = "{\n" +
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


    public static String vrpInitiationPayloadWithoutDebtAcc = "{\n" +
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


    public static String vrpInitiationPayloadWithoutCreditorAcc = "{\n" +
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


    public static String vrpInitiationPayloadWithoutControlParameterKey = "{\n" +
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

    public static String vrpInitiationPayloadWithoutAmount = "{\n" +
            "    \"Data\": {\n" +
            "       \"ReadRefundAccount\": \"true\",\n" +
            "       \"ControlParameters\": {\n" +
            "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
            "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
            "           \"MaximumIndividualAmount\": {\n" +
            "               \"\": \"9\",\n" +
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

    /**
     * test.
     */
    public static final class DataProviders {

        public static final Object[][] METADATA_DATA_HOLDER = new Object[][]{
                {
                        vrpInitiationPayloadWithoutData,
//                        vrpInitiationPayloadNotInstanceOfJsonObject,
//                        vrpInitiationPayloadIsEmpty
                }
        };

        public static final Object[][] METADATA_CONTROL_PARAMETER = new Object[][]{
                {
                        vrpInitiationPayloadWithoutMaximumIndividualAmount,
                        vrpInitiationPayloadWithInvalidMaximumIndividualAmount
                }
        };

        public static final Object[][] METADATA_DATA_STRING = new Object[][]{
                {
                        vrpInitiationPayloadWithStringData
                }
        };

        public static final Object[][] METADATA_DATA_JSONOBJECT = new Object[][]{
                {
                        vrpInitiationPayloadWithOutJsonObject
                }
        };

        public static final Object[][] METADATA_INITIATION = new Object[][]{
                {
                        vrpInitiationPayloadWithoutInitiation
                }
        };

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


        public static final String METADATA_VRP_PERIODIC_ALIGNMENT = "{\n" +
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
                "                   \"PeriodAlignment\": \"invalid\",\n" +
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




        public static final String METADATA_VRP_WITH_ALL_PARAMETERS = "{\n" +
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

        public static final String METADATA_VRP_WITHOUT_MAXIMUM_INDIVIDUAL_AMOUNT = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"ValidFromDateTime\": \"2023-09-12T12:43:07.956Z\",\n" +
                "           \"ValidToDateTime\": \"2024-05-12T12:43:07.956Z\",\n" +
                "           \"\": {\n" +
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



        public static final String METADATA_VRP_WITH_EMPTY_RISK = "{\n" +
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
                "   \"Risk\": \"\"\n" +  // Empty string for Risk
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


        public static final String METADATA_VRP_WITH_INVALID_VALIDFROM_DATE = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"ValidFromDateTime\": \"\",\n" +  // Empty string for ValidFromDateTime
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


        public static final String METADATA_VRP_WITH_DATE_NOT_STRING = "{\n" +
                "    \"Data\": {\n" +
                "       \"ReadRefundAccount\": \"true\",\n" +
                "       \"ControlParameters\": {\n" +
                "           \"\": \"2024-05-12T12:43:07.956Z\",\n" +
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







        public static final Object[][] METADATA_WITHOUT_CREDITOR_ACC = new Object[][]{
                {
                        vrpInitiationPayloadWithoutCreditorAcc
                }
        };


        public static final Object[][] METADATA_WITHOUT_CONTROL_PARAMETER = new Object[][]{
                {
                        vrpInitiationPayloadWithoutControlParameterKey
                }
        };

        public static final Object[][] METADATA_WITHOUT_AMOUNT = new Object[][]{
                {
                        vrpInitiationPayloadWithoutAmount
                }
        };

    }
}

