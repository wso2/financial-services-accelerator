/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.framework.constant

import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.time.format.DateTimeFormatter;

/**
* Payment Request Payloads
*/
class PaymentRequestPayloads {

    static ConfigurationService acceleratorConfiguration = new ConfigurationService()

    public static String initiationPaymentPayload = """{
            "Data": {
                "Initiation": {
                    "InstructionIdentification": "ACME412",
                    "EndToEndIdentification": "FRESCO.21302.GFX.20",
                    "LocalInstrument": "OB.Paym",
                    "InstructedAmount": {
                        "Amount": "165.88",
                        "Currency": "GBP"
                    },
                    "CreditorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "08080021325698",
                        "Name": "ACME Inc",
                        "SecondaryIdentification": "0002"
                    },
                    "DebtorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "08080025612489",
                        "Name": "Jane Smith",
                        "SecondaryIdentification": "080801562314789"
                    },
                    "SupplementaryData": {
                        "additionalProp1": {
                        }
                    }
                }
            },
            "Risk": {
            }
        }""".stripIndent()

//    public static String initiationPaymentPayload = "{\n" +
//    "            \"Data\": {\n" +
//    "                \"Initiation\": {\n" +
//    "                    \"InstructionIdentification\": \"ACME412\",\n" +
//    "                    \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
//    "                    \"LocalInstrument\": \"OB.Paym\",\n" +
//    "                    \"InstructedAmount\": {\n" +
//    "                        \"Amount\": \"165.88\",\n" +
//    "                        \"Currency\": \"GBP\"\n" +
//    "                    },\n" +
//    "                    \"CreditorAccount\": {\n" +
//    "                        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
//    "                        \"Identification\": \"08080021325698\",\n" +
//    "                        \"Name\": \"ACME Inc\",\n" +
//    "                        \"SecondaryIdentification\": \"0002\"\n" +
//    "                    },\n" +
//    "                    \"DebtorAccount\": {\n" +
//    "                        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
//    "                        \"Identification\": \"08080025612489\",\n" +
//    "                        \"Name\": \"Jane Smith\",\n" +
//    "                        \"SecondaryIdentification\": \"080801562314789\"\n" +
//    "                    },\n" +
//    "                    \"SupplementaryData\": {\n" +
//    "                        \"additionalProp1\": {\n" +
//    "                        }\n" +
//    "                    }\n" +
//    "                }\n" +
//    "            },\n" +
//    "            \"Risk\": {\n" +
//    "            }\n" +
//    "        }";

//    static String getSubmissionPaymentPayload(String consentID) {
//        return "{\n" +
//                "\"Data\": {\n" +
//                "\"ConsentId\": \"${consentID}\",\n" +
//                "\"Initiation\": {\n" +
//                "                    \"InstructionIdentification\": \"ACME412\",\n" +
//                "                    \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
//                "                    \"LocalInstrument\": \"OB.Paym\",\n" +
//                "                    \"InstructedAmount\": {\n" +
//                "                        \"Amount\": \"165.88\",\n" +
//                "                        \"Currency\": \"GBP\"\n" +
//                "                    },\n" +
//                "                    \"CreditorAccount\": {\n" +
//                "                        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
//                "                        \"Identification\": \"08080021325698\",\n" +
//                "                        \"Name\": \"ACME Inc\",\n" +
//                "                        \"SecondaryIdentification\": \"0002\"\n" +
//                "                    },\n" +
//                "                    \"DebtorAccount\": {\n" +
//                "                        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
//                "                        \"Identification\": \"08080025612489\",\n" +
//                "                        \"Name\": \"Jane Smith\",\n" +
//                "                        \"SecondaryIdentification\": \"080801562314789\"\n" +
//                "                    },\n" +
//                "                    \"SupplementaryData\": {\n" +
//                "                        \"additionalProp1\": {}\n" +
//                "                    }\n" +
//                "                }\n" +
//                "},\n" +
//                "\"Risk\": {\n" +
//                "}\n" +
//                "}"
//    }

    static String getSubmissionPaymentPayload(String consentID) {
        return """{ 
            "Data": {
				"ConsentId": "${consentID}",
				"Initiation": {
                    "InstructionIdentification": "ACME412",
                    "EndToEndIdentification": "FRESCO.21302.GFX.20",
                    "LocalInstrument": "OB.Paym",
                    "InstructedAmount": {
                        "Amount": "165.88",
                        "Currency": "GBP"
                    },
                    "CreditorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "08080021325698",
                        "Name": "ACME Inc",
                        "SecondaryIdentification": "0002"
                    },
                    "DebtorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "08080025612489",
                        "Name": "Jane Smith",
                        "SecondaryIdentification": "080801562314789"
                    },
                    "SupplementaryData": {
                        "additionalProp1": {}
                    }
                }
			},
			"Risk": {
			}
		}""".stripIndent()
    }

    /**
     * Build Validation Payload
     * @param clientId - Client Id
     * @param userId - User Id
     * @param consentId - Consent Id
     * @param requestUri - Request url
     * @return
     */
    static String buildPaymentValidationPayload(String accessToken, String userId, String consentId,
                                                String clientId = acceleratorConfiguration.getAppInfoClientID(),
                                                String requestUri = "/domestic-payments") {

        String initiationPayload = """{
            "headers": {
                "Authorization": "Basic ${accessToken}",
                "x-request-id": "1b91e649-3d06-4e16-ada7-bf5af2136b44",
                "consent-id": "${consentId}",
                "activityid": "8666aa84-fc5a-425e-91c9-37fa30a95784",
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "User-Agent": "PostmanRuntime/7.28.4",
                "Host": "localhost:8243",
                "Postman-Token": "244d15b6-eb18-4045-ba87-8ee6c830b84c",
                "Accept-Encoding": "gzip, deflate, br",
                "accept": "application/json; charset=utf-8"
                },
                "consentId": "${consentId}",
                "resourceParams": {
                    "resource": "/pisp/domestic-payments",
                    "context": "/open-banking/v3.1/pisp",
                    "httpMethod": "GET"
                },
                "userId": "${userId}",
                "electedResource": "${requestUri}",
                "clientId": "${clientId}",
                "body": ${getSubmissionPaymentPayload(consentId)}
            }""".stripIndent()
        return initiationPayload
    }

    static String initiationPayloadPayloadWithModifiableParams(def parameterMap)  {

        String initiationPayloadDomesticStandingOrder = """{
            "Data": {
                "Initiation": {
                    "InstructionIdentification": "${this.getParameterValue("InstructionIdentification", parameterMap)}",
                    "EndToEndIdentification": "${this.getParameterValue("EndToEndIdentification", parameterMap)}",
                    "LocalInstrument": "${this.getParameterValue("LocalInstrument", parameterMap)}",
                    "InstructedAmount": {
                        "Amount": "${this.getParameterValue("Amount", parameterMap)}",
                        "Currency": "${this.getParameterValue("Currency", parameterMap)}"
                    },
                    "CreditorAccount": {
                        "SchemeName": "${this.getParameterValue("CreditorSchemeName", parameterMap)}",
                        "Identification": "${this.getParameterValue("CreditorIdentification", parameterMap)}",
                        "Name": "${this.getParameterValue("CreditorAccountName", parameterMap)}",
                        "SecondaryIdentification": "${this.getParameterValue("CreditorSecondaryIdentification", parameterMap)}"
                    },
                    "DebtorAccount": {
                        "SchemeName": "${this.getParameterValue("DebtorSchemeName", parameterMap)}",
                        "Identification": "${this.getParameterValue("DebtorIdentification", parameterMap)}",
                        "Name": "${this.getParameterValue("DebtorName", parameterMap)}",
                        "SecondaryIdentification": "${this.getParameterValue("DebtorSecondaryIdentification", parameterMap)}"
                    },
                    "SupplementaryData": {
                        "additionalProp1": {}
                    }
                }
            },
            "Risk": {
            }
        }""".stripIndent()

        return initiationPayloadDomesticStandingOrder
    }

    public static String getParameterValue(String parameter, def parameterMap) {

        if (parameterMap.containsKey(parameter)) {
            return parameterMap.get(parameter)
        } else {
            if (parameter == "RequestedExecutionDateTime") {
                return TestUtil.getDateAndTime(5)
            } else if (parameter == "FirstPaymentDateTime") {
                return TestUtil.getDateAndTime(1)
            } else if (parameter == "RecurringPaymentDateTime") {
                return TestUtil.getDateAndTime(3)
            } else if(parameter == "FinalPaymentDateTime") {
                return TestUtil.getDateAndTime(5)
            } else if (parameter == "CompletionDateTime") {
                return DateTimeFormatter.ISO_INSTANT.format(ConnectorTestConstants.DATE_TIME)
            } else {
                return this.defaultValueMap[parameter]
            }

        }
    }

    static final def defaultValueMap = [
            "Permission": "Create",
            "Amount":"30.80",
            "Currency":"GBP",
            "CreditorSchemeName":"OB.SortCodeAccountNumber",
            "CreditorIdentification":"08080021325698",
            "CreditorAccountName":"ACME Inc",
            "CreditorSecondaryIdentification":"0002",
            "DebtorSchemeName":"OB.SortCodeAccountNumber",
            "DebtorIdentification":"30080012343456",
            "DebtorName":"Andrea Smith",
            "DebtorSecondaryIdentification":"30080012343456",
            "Frequency": "EvryDay",
            "Reference": "Pocket money for Damien",
            "NumberOfPayments" : "10",
            "FirstPaymentAmount": "6.66",
            "FirstPaymentCurrency": "GBP",
            "RecurringPaymentAmount": "7.00",
            "RecurringPaymentCurrency": "GBP",
            "FinalPaymentAmount": "12.00",
            "FinalPaymentCurrency": "GBP",
            "AuthorisationType": "Any",
            "PaymentContextCode":"EcommerceGoods",
            "MerchantCategoryCode":"5967",
            "MerchantCustomerIdentification":"053598653254",
            "AddressLine":"[\"Flat 7\",\"Acacia Lodge\"]",
            "StreetName":"Acacia Avenue",
            "BuildingNumber":"27",
            "PostCode":"GU31 2ZZ",
            "TownName":"Sparsholt",
            "CountrySubDivision":"\"Wessex\"",
            "Country":"UK",
            "InstructionIdentification": "ACME412",
            "EndToEndIdentification": "FRESCO.21302.GFX.20",
            "LocalInstrument": "OB.Paym"
    ]

    public static String modifiedInitiationPaymentPayload = """{
            "Data": {
                "Initiation": {
                    "InstructionIdentification": "ACME412",
                    "EndToEndIdentification": "FRESCO.21302.GFX.20",
                    "LocalInstrument": "OB.Paym",
                    "InstructedAmount": {
                        "Amount": "180.88",
                        "Currency": "GBP"
                    },
                    "CreditorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "08080021325698",
                        "Name": "ACME Inc",
                        "SecondaryIdentification": "0002"
                    },
                    "DebtorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "08080025612489",
                        "Name": "Jane Smith",
                        "SecondaryIdentification": "080801562314789"
                    },
                    "SupplementaryData": {
                        "additionalProp1": {}
                    }
                }
            },
            "Risk": {
            }
        }""".stripIndent()

//    public static String modifiedInitiationPaymentPayload = "{\n" +
//            "            \"Data\": {\n" +
//            "                \"Initiation\": {\n" +
//            "                    \"InstructionIdentification\": \"ACME412\",\n" +
//            "                    \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
//            "                    \"LocalInstrument\": \"OB.Paym\",\n" +
//            "                    \"InstructedAmount\": {\n" +
//            "                        \"Amount\": \"180.88\",\n" +
//            "                        \"Currency\": \"GBP\"\n" +
//            "                    },\n" +
//            "                    \"CreditorAccount\": {\n" +
//            "                        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
//            "                        \"Identification\": \"08080021325698\",\n" +
//            "                        \"Name\": \"ACME Inc\",\n" +
//            "                        \"SecondaryIdentification\": \"0002\"\n" +
//            "                    },\n" +
//            "                    \"DebtorAccount\": {\n" +
//            "                        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
//            "                        \"Identification\": \"08080025612489\",\n" +
//            "                        \"Name\": \"Jane Smith\",\n" +
//            "                        \"SecondaryIdentification\": \"080801562314789\"\n" +
//            "                    },\n" +
//            "                    \"SupplementaryData\": {\n" +
//            "                        \"additionalProp1\": {}\n" +
//            "                    }\n" +
//            "                }\n" +
//            "            },\n" +
//            "            \"Risk\": {\n" +
//            "            }\n" +
//            "        }"

    static String getModifiedSubmissionPaymentPayload(String consentID) {

        return """{
			"Data": {
				"ConsentId": "${consentID}",
				"Initiation": {
                    "InstructionIdentification": "ACME412",
                    "EndToEndIdentification": "FRESCO.21302.GFX.20",
                    "LocalInstrument": "OB.Paym",
                    "InstructedAmount": {
                        "Amount": "170.90",
                        "Currency": "USD"
                    },
                    "CreditorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "08080021325698",
                        "Name": "ACME Inc",
                        "SecondaryIdentification": "0002"
                    },
                    "DebtorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "08080025612489",
                        "Name": "Jane Smith",
                        "SecondaryIdentification": "080801562314789"
                    },
                    "SupplementaryData": {
                        "additionalProp1": {}
                    }
                }
			},
			"Risk": {
			}
		}""".stripIndent()
    }

//    static String getModifiedSubmissionPaymentPayload(String consentID) {
//
//        return "{\n" +
//                "\"Data\": {\n" +
//                "\"ConsentId\": \"${consentID}\",\n" +
//                "\"Initiation\": {\n" +
//                "                    \"InstructionIdentification\": \"ACME412\",\n" +
//                "                    \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
//                "                    \"LocalInstrument\": \"OB.Paym\",\n" +
//                "                    \"InstructedAmount\": {\n" +
//                "                        \"Amount\": \"170.90\",\n" +
//                "                        \"Currency\": \"USD\"\n" +
//                "                    },\n" +
//                "                    \"CreditorAccount\": {\n" +
//                "                        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
//                "                        \"Identification\": \"08080021325698\",\n" +
//                "                        \"Name\": \"ACME Inc\",\n" +
//                "                        \"SecondaryIdentification\": \"0002\"\n" +
//                "                    },\n" +
//                "                    \"DebtorAccount\": {\n" +
//                "                        \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
//                "                        \"Identification\": \"08080025612489\",\n" +
//                "                        \"Name\": \"Jane Smith\",\n" +
//                "                        \"SecondaryIdentification\": \"080801562314789\"\n" +
//                "                    },\n" +
//                "                    \"SupplementaryData\": {\n" +
//                "                        \"additionalProp1\": {}\n" +
//                "                    }\n" +
//                "                }\n" +
//                "},\n" +
//                "\"Risk\": {\n" +
//                "}\n" +
//                "}"
//    }
}
