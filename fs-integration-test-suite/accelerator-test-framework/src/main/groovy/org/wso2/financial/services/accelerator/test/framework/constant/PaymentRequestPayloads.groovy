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

    public static String initiationPaymentPayload = """
		{
            "Data": {
                "Initiation": {
                    "InstructionIdentification": "ACME412",
                    "EndToEndIdentification": "FRESCO.21302.GFX.20",
                    "LocalInstrument": "UK.OBIE.Paym",
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
                        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
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
        }
	""".stripIndent()

    static String getSubmissionPaymentPayload(String consentID) {
        return """{
			"Data": {
				"ConsentId": "${consentID}",
				"Initiation": {
                    "InstructionIdentification": "ACME412",
                    "EndToEndIdentification": "FRESCO.21302.GFX.20",
                    "LocalInstrument": "UK.OBIE.Paym",
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
                        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
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
		}
		""".stripIndent()
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
            }
            """.stripIndent()
        return initiationPayload
    }

    static String initiationPayloadDomesticStandingOrderPayment(def parameterMap)  {

        String initiationPayloadDomesticStandingOrder = """
            {
                "Data": {
                    "Permission": "${this.getParameterValue("Permission", parameterMap)}",
                    "Authorisation": {
                          "AuthorisationType": "${this.getParameterValue("AuthorisationType", parameterMap)}",
                          "CompletionDateTime": "${this.getParameterValue("CompletionDateTime", parameterMap)}"
                    },
                    "Initiation": {
                          "Frequency": "${this.getParameterValue("Frequency", parameterMap)}",
                          "Reference": "${this.getParameterValue("Reference", parameterMap)}",
                          "NumberOfPayments" : "${this.getParameterValue("NumberOfPayments", parameterMap)}",
                          "FirstPaymentDateTime": "${this.getParameterValue("FirstPaymentDateTime", parameterMap)}",
                          "RecurringPaymentDateTime": "${this.getParameterValue("RecurringPaymentDateTime", parameterMap)}",
                          "FirstPaymentAmount": {
                                "Amount": "${this.getParameterValue("FirstPaymentAmount", parameterMap)}",
                                "Currency": "${this.getParameterValue("FirstPaymentCurrency", parameterMap)}"
                          },
                          "RecurringPaymentAmount": {
                                "Amount": "${this.getParameterValue("RecurringPaymentAmount", parameterMap)}",
                                "Currency": "${this.getParameterValue("RecurringPaymentCurrency", parameterMap)}"
                          },
                          "FinalPaymentDateTime": "${this.getParameterValue("FinalPaymentDateTime", parameterMap)}",
                          "FinalPaymentAmount": {
                                "Amount": "${this.getParameterValue("FinalPaymentAmount", parameterMap)}",
                                "Currency": "${this.getParameterValue("FinalPaymentCurrency", parameterMap)}"
                          },
                          "DebtorAccount": {
                                "SchemeName": "${this.getParameterValue("DebtorSchemeName", parameterMap)}",
                                "Identification": "${this.getParameterValue("DebtorIdentification", parameterMap)}",
                                "Name": "${this.getParameterValue("DebtorName", parameterMap)}",
                                "SecondaryIdentification": "${this.getParameterValue("DebtorSecondaryIdentification", parameterMap)}"
                          },
                          "CreditorAccount": {
                                "SchemeName": "${this.getParameterValue("CreditorSchemeName", parameterMap)}",
                                "Identification": "${this.getParameterValue("CreditorIdentification", parameterMap)}",
                                "Name": "${this.getParameterValue("CreditorAccountName", parameterMap)}",
                                "SecondaryIdentification": "${this.getParameterValue("CreditorSecondaryIdentification", parameterMap)}"
                          }
                    }
                },
                "Risk": {
                    "PaymentContextCode": "${this.getParameterValue("PaymentContextCode", parameterMap)}",
                    "MerchantCategoryCode": "${this.getParameterValue("MerchantCategoryCode", parameterMap)}",
                    "MerchantCustomerIdentification": "${this.getParameterValue("MerchantCustomerIdentification", parameterMap)}",
                    "DeliveryAddress": {
                          "AddressLine": ${this.getParameterValue("AddressLine", parameterMap)},
                          "StreetName": "${this.getParameterValue("StreetName", parameterMap)}",
                          "BuildingNumber": "${this.getParameterValue("BuildingNumber", parameterMap)}",
                          "PostCode": "${this.getParameterValue("PostCode", parameterMap)}",
                          "TownName": "${this.getParameterValue("TownName", parameterMap)}",
                          "CountrySubDivision": ${this.getParameterValue("CountrySubDivision", parameterMap)},
                          "Country": "${this.getParameterValue("Country", parameterMap)}"
                    }
                }
            }
        """.stripIndent()

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
            "CreditorSchemeName":"UK.OBIE.SortCodeAccountNumber",
            "CreditorIdentification":"08080021325698",
            "CreditorAccountName":"ACME Inc",
            "CreditorSecondaryIdentification":"0002",
            "DebtorSchemeName":"UK.OBIE.SortCodeAccountNumber",
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
            "Country":"UK"
    ]

    public static String modifiedInitiationPaymentPayload = """
		{
            "Data": {
                "ReadRefundAccount": "Yes",
                "Initiation": {
                    "InstructionIdentification": "ACME412",
                    "EndToEndIdentification": "FRESCO.21302.GFX.20",
                    "InstructedAmount": {
                        "Amount": "170.25",
                        "Currency": "GBP"
                    },
                    "CreditorAccount": {
                        "SchemeName": "OB.SortCodeAccountNumber",
                        "Identification": "080800213256912",
                        "Name": "ACME Inc",
                        "SecondaryIdentification": "0002"
                    },
                    "RemittanceInformation": {
                        "Reference": "FRESCO-101",
                        "Unstructured": "Internal ops code 5120101"
                    }
                }
            },
            "Risk": {
                "PaymentContextCode": "EcommerceGoods",
                "MerchantCustomerIdentification": "053598653254",
                "DeliveryAddress": {
                    "AddressLine": [
                        "Flat 7",
                        "Acacia Lodge"
                    ],
                    "StreetName": "Acacia Avenue",
                    "BuildingNumber": "20",
                    "PostCode": "GU31 2ZZ",
                    "TownName": "Sparsholt",
                    "CountrySubDivision": "Wessex",
                    "Country": "UK"
                }
            }
        }
	""".stripIndent()
}
