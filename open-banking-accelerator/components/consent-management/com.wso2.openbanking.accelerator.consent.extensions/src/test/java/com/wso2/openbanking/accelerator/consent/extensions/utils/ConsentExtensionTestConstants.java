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

/**
comment.
 */
public class ConsentExtensionTestConstants {

    public static final String VALID_INITIATION_OBJECT = "{\n" +
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
