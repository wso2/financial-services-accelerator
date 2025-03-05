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

import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService;

/**
* Payment Request Payloads
*/
class PaymentRequestPayloads {

    static ConfigurationService acceleratorConfiguration = new ConfigurationService()

    public static String initiationPaymentPayload = """
		{
            "Data": {
                "ReadRefundAccount": "Yes",
                "Initiation": {
                    "InstructionIdentification": "ACME412",
                    "EndToEndIdentification": "FRESCO.21302.GFX.20",
                    "InstructedAmount": {
                        "Amount": "165.88",
                        "Currency": "GBP"
                    },
                    "CreditorAccount": {
                        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
                        "Identification": "08080021325698",
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
                    "BuildingNumber": "27",
                    "PostCode": "GU31 2ZZ",
                    "TownName": "Sparsholt",
                    "CountrySubDivision": "Wessex",
                    "Country": "UK"
                }
            }
        }
	""".stripIndent()

    static String getSubmissionPaymentPayload(String consentID) {
        return """{
			"Data": {
				"ConsentId": "${consentID}",
				"ReadRefundAccount": "Yes",
				"Initiation": {
					"InstructionIdentification": "ACME412",
					"EndToEndIdentification": "FRESCO.21302.GFX.20",
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
					"RemittanceInformation": {
						"Reference": "FRESCO-101",
						"Unstructured": "Internal ops code 5120101"
					}
				}
			},
			"Risk": {
				"PaymentContextCode": "EcommerceMerchantInitiatedPayment"
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
}
