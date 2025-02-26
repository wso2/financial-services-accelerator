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


package org.wso2.financial.services.accelerator.test.framework.constant

import groovy.json.JsonOutput
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.request_builder.ConsentRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.time.format.DateTimeFormatter

/**
 * Test Request Payloads.
 */
class RequestPayloads {

    static ConfigurationService acceleratorConfiguration = new ConfigurationService()

    static String[] permissionsArray = [
            ConnectorTestConstants.READ_ACCOUNTS_BASIC,
            ConnectorTestConstants.READ_ACCOUNTS_DETAIL,
            ConnectorTestConstants.READ_BALANCES,
            ConnectorTestConstants.READ_TRANSACTIONS_DETAIL
    ]

    static String[] permissionsArrayWithoutReadAccountsDetail = [
            ConnectorTestConstants.READ_BALANCES,
            ConnectorTestConstants.READ_TRANSACTIONS_DETAIL
    ]

    static String[] permissionsArrayWithoutReadTransactionsDetail = [
            ConnectorTestConstants.READ_BALANCES,
            ConnectorTestConstants.READ_ACCOUNTS_DETAIL
    ]

    static String[] permissionsArrayWithoutReadBalances = [
            ConnectorTestConstants.READ_ACCOUNTS_DETAIL,
            ConnectorTestConstants.READ_TRANSACTIONS_DETAIL
    ]

    public static String initiationPayload = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsArray)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()
    /**
     * Consent Initiation Payload generation
     */
    public static String  generateConsentInitiationPayload (ArrayList<String> permissionsList) {

        return """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsList)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    }

    public static String initiationIncorrectPayload = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsArray)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDate":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    public static String initiationPayloadWithoutPermissions = """
            {
                "Data":{
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    public static String initiationPayloadWithoutDataProperty = """
            {
                "":{
                "Permissions": ${JsonOutput.toJson(permissionsArray)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    public static String initiationPayloadWithInvalidPermission = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(["ReadAccountsDetails"])},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    public static String initiationPayloadWithEmptyPermission = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson([])},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    public static String initiationPayloadWithInvalidDate = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsArray)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant.toEpochSecond()}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    public static String initiationPayloadWithoutArrayFormat = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(ConnectorTestConstants.READ_ACCOUNTS_DETAIL)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    public static String initiationPayloadWithoutReadAccountsDetail = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsArrayWithoutReadAccountsDetail)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    public static String initiationPayloadWithoutReadTransactionsDetail = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsArrayWithoutReadTransactionsDetail)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()


    public static String initiationPayloadWithoutReadBalances = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsArrayWithoutReadBalances)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()

    /**
     * Build Validation Payload
     * @param clientId - Client Id
     * @param userId - User Id
     * @param consentId - Consent Id
     * @param requestUri - Request url
     * @return
     */
    static String buildValidationPayload(String userId, String consentId, String hostname, String requestUri) {

        String initiationPayload = """{
	  			"headers": {
	    			"Authorization": "Basic ${ConsentRequestBuilder.GenerateBasicHeader()}",
	    			"x-request-id": "1b91e649-3d06-4e16-ada7-bf5af2136b44",
	    			"consent-id": "${consentId}",
	    			"activityid": "8666aa84-fc5a-425e-91c9-37fa30a95784",
	    			"Cache-Control": "no-cache",
	    			"Connection": "keep-alive",
	    			"User-Agent": "PostmanRuntime/7.28.4",
	    			"Host": "${hostname}",
	    			"Postman-Token": "244d15b6-eb18-4045-ba87-8ee6c830b84c",
	    			"Accept-Encoding": "gzip, deflate, br",
	    			"accept": "application/json; charset=utf-8"
	  				},
	  				"consentId": "${consentId}",
					"resourceParams": {
						"resource": "/aisp/accounts",
						"context": "/open-banking/v3.1/aisp",
						"httpMethod": "GET"
	  				},
	  				"userId": "${userId}",
	  				"electedResource": "${requestUri}",
					"clientId": "${acceleratorConfiguration.getAppInfoClientID()}"
			}
""".stripIndent()
        return initiationPayload
    }

    /**
     * Build Validation Payload for accounts
     * @param clientId - Client Id
     * @param userId - User Id
     * @param consentId - Consent Id
     * @param requestUri - Request url
     * @return
     */
    static String buildValidationAccountsPayload(String accessToken, String userId, String consentId) {
        return """
			{
      			"headers": {
 			  		"Authorization": "${accessToken}",
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
					"resource": "/aisp/accounts",
					"context": "/open-banking/v3.1/aisp",
					"httpMethod": "GET"
 			 	 },
 			    "userId": "${userId}",
     		 	"electedResource": "/accounts/1234",
				"clientId": "${acceleratorConfiguration.getAppInfoClientID()}"
 			}
    			"""
    }

    /**
     * Build Validation Payload for accounts
     * @param clientId - Client Id
     * @param userId - User Id
     * @param consentId - Consent Id
     * @param requestUri - Request url
     * @return
     */
    static String buildValidationTransactionPayload(String userId, String consentId, String hostname) {

        String initiationPayload = """{
	  			"headers": {
	    			"Authorization": "Basic ${ConsentRequestBuilder.GenerateBasicHeader()}",
	    			"x-request-id": "1b91e649-3d06-4e16-ada7-bf5af2136b44",
	    			"consent-id": "${consentId}",
	    			"activityid": "8666aa84-fc5a-425e-91c9-37fa30a95784",
	    			"Cache-Control": "no-cache",
	    			"Connection": "keep-alive",
	    			"User-Agent": "PostmanRuntime/7.28.4",
	    			"Host": "${hostname}",
	    			"Postman-Token": "244d15b6-eb18-4045-ba87-8ee6c830b84c",
	    			"Accept-Encoding": "gzip, deflate, br",
	    			"accept": "application/json; charset=utf-8"
				},
				"consentId": "${consentId}",
				"resourceParams": {
					"resource": "/aisp/accounts",
					"context": "/open-banking/v3.1/aisp",
					"httpMethod": "GET"
				},
				"userId": "${userId}",
				"electedResource": "/accounts/1234/transactions",
				"clientId": "${acceleratorConfiguration.getAppInfoClientID()}"
			}
""".stripIndent()
        return initiationPayload
    }

    /**
     * Build Validation Payload for balances
     * @param clientId - Client Id
     * @param userId - User Id
     * @param consentId - Consent Id
     * @param requestUri - Request url
     * @return
     */
    static String buildValidationBalancePayload(String userId, String consentId, String hostname) {

        String initiationPayload = """{
	  			"headers": {
	    			"Authorization": "Basic ${ConsentRequestBuilder.GenerateBasicHeader()}",
	    			"x-request-id": "1b91e649-3d06-4e16-ada7-bf5af2136b44",
	    			"consent-id": "${consentId}",
	    			"activityid": "8666aa84-fc5a-425e-91c9-37fa30a95784",
	    			"Cache-Control": "no-cache",
	    			"Connection": "keep-alive",
	    			"User-Agent": "PostmanRuntime/7.28.4",
	    			"Host": "${hostname}",
	    			"Postman-Token": "244d15b6-eb18-4045-ba87-8ee6c830b84c",
	    			"Accept-Encoding": "gzip, deflate, br",
	    			"accept": "application/json; charset=utf-8"
	  				},
	  				"consentId": "${consentId}",
					"resourceParams": {
	    			"resource": "/aisp/accounts",
	    			"context": "/open-banking/v3.1/aisp",
	    			"httpMethod": "GET"
	  				},
	  				"userId": "${userId}",
	  				"electedResource": "/accounts/1234/balances",
					"clientId": "${acceleratorConfiguration.getAppInfoClientID()}"
		}
		""".stripIndent()
        return initiationPayload
    }

    /**
     * Build Validation Payload
     * @param clientId - Client Id
     * @param userId - User Id
     * @param consentId - Consent Id
     * @param requestUri - Request url
     * @return
     */
    static String buildCofValidationPayload(String accessToken, String userId, String consentId, String requestUri) {

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
						"resource": "/aisp/accounts",
						"context": "/open-banking/v3.1/aisp",
						"httpMethod": "GET"
	  				},
	  				"userId": "${userId}",
	  				"electedResource": "${requestUri}",
					"clientId": "${acceleratorConfiguration.getAppInfoClientID()}",
					"body": ${getCofSubmissionPayload(consentId)}
			}
""".stripIndent()
        return initiationPayload
    }

    /**
     * Build Validation Payload
     * @param clientId - Client Id
     * @param userId - User Id
     * @param consentId - Consent Id
     * @param requestUri - Request url
     * @return
     */
    static String buildPaymentValidationPayload(String accessToken, String userId, String consentId, String requestUri) {

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
						"resource": "/aisp/accounts",
						"context": "/open-banking/v3.1/aisp",
						"httpMethod": "GET"
	  				},
	  				"userId": "${userId}",
	  				"electedResource": "${requestUri}",
					"clientId": "${acceleratorConfiguration.getAppInfoClientID()}",
					"body": ${getSubmissionPaymentPayload(consentId)}
			}
""".stripIndent()
        return initiationPayload
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
            "Country":"UK"
    ]

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

    public static String getSubmissionPaymentPayload(String consentID) {
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

    public static String initiationPaymentPayloadWithCustomValues(def parameterMap)  {

        String initiationPayloadDomesticStandingOrder = """
            {
                "Data": {
                    "Permission": "${getParameterValue("Permission", parameterMap)}",
                    "Authorisation": {
                          "AuthorisationType": "${getParameterValue("AuthorisationType", parameterMap)}",
                          "CompletionDateTime": "${getParameterValue("CompletionDateTime", parameterMap)}"
                    },
                    "Initiation": {
                          "Frequency": "${getParameterValue("Frequency", parameterMap)}",
                          "Reference": "${getParameterValue("Reference", parameterMap)}",
                          "NumberOfPayments" : "${getParameterValue("NumberOfPayments", parameterMap)}",
                          "FirstPaymentDateTime": "${getParameterValue("FirstPaymentDateTime", parameterMap)}",
                          "RecurringPaymentDateTime": "${getParameterValue("RecurringPaymentDateTime", parameterMap)}",
                          "FirstPaymentAmount": {
                                "Amount": "${getParameterValue("FirstPaymentAmount", parameterMap)}",
                                "Currency": "${getParameterValue("FirstPaymentCurrency", parameterMap)}"
                          },
                          "RecurringPaymentAmount": {
                                "Amount": "${getParameterValue("RecurringPaymentAmount", parameterMap)}",
                                "Currency": "${getParameterValue("RecurringPaymentCurrency", parameterMap)}"
                          },
                          "FinalPaymentDateTime": "${getParameterValue("FinalPaymentDateTime", parameterMap)}",
                          "FinalPaymentAmount": {
                                "Amount": "${getParameterValue("FinalPaymentAmount", parameterMap)}",
                                "Currency": "${getParameterValue("FinalPaymentCurrency", parameterMap)}"
                          },
                          "DebtorAccount": {
                                "SchemeName": "${getParameterValue("DebtorSchemeName", parameterMap)}",
                                "Identification": "${getParameterValue("DebtorIdentification", parameterMap)}",
                                "Name": "${getParameterValue("DebtorName", parameterMap)}",
                                "SecondaryIdentification": "${getParameterValue("DebtorSecondaryIdentification", parameterMap)}"
                          },
                          "CreditorAccount": {
                                "SchemeName": "${getParameterValue("CreditorSchemeName", parameterMap)}",
                                "Identification": "${getParameterValue("CreditorIdentification", parameterMap)}",
                                "Name": "${getParameterValue("CreditorAccountName", parameterMap)}",
                                "SecondaryIdentification": "${getParameterValue("CreditorSecondaryIdentification", parameterMap)}"
                          }
                    }
                },
                "Risk": {
                    "PaymentContextCode": "${getParameterValue("PaymentContextCode", parameterMap)}",
                    "MerchantCategoryCode": "${getParameterValue("MerchantCategoryCode", parameterMap)}",
                    "MerchantCustomerIdentification": "${getParameterValue("MerchantCustomerIdentification", parameterMap)}",
                    "DeliveryAddress": {
                          "AddressLine": ${getParameterValue("AddressLine", parameterMap)},
                          "StreetName": "${getParameterValue("StreetName", parameterMap)}",
                          "BuildingNumber": "${getParameterValue("BuildingNumber", parameterMap)}",
                          "PostCode": "${getParameterValue("PostCode", parameterMap)}",
                          "TownName": "${getParameterValue("TownName", parameterMap)}",
                          "CountrySubDivision": ${getParameterValue("CountrySubDivision", parameterMap)},
                          "Country": "${getParameterValue("Country", parameterMap)}"
                    }
                }
            }
        """.stripIndent()

        return initiationPayloadDomesticStandingOrder
    }

    public static String initiationPayloadWithIntegerValueForString(def parameterMap)  {

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
                                "SecondaryIdentification": ${this.getParameterValue("DebtorSecondaryIdentification", parameterMap)}
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
                return TestUtil.getDateAndTime()
            } else if (parameter == "FirstPaymentDateTime") {
                return TestUtil.getDateAndTime()
            } else if (parameter == "RecurringPaymentDateTime") {
                return TestUtil.getDateAndTime()
            } else if(parameter == "FinalPaymentDateTime") {
                return TestUtil.getDateAndTime()
            } else if (parameter == "CompletionDateTime") {
                return DateTimeFormatter.ISO_INSTANT.format(ConnectorTestConstants.DATE_TIME)
            } else {
                return this.defaultValueMap[parameter]
            }

        }
    }

    public static String cofInitiationPayload = """
	{
	  	"Data": {
			"DebtorAccount": {
			  "SchemeName": "OB.IBAN",
			  "Identification": "GB76LOYD30949301273801",
			  "SecondaryIdentification": "Roll 56988"
			},
			"ExpirationDateTime": "${ConnectorTestConstants.expirationInstant}",
	  	}
	}
	""".stripIndent()

    public static String getCofSubmissionPayload(String consentID) {
        return """{
			"Data": {
				"ConsentId": "${consentID}",
				"Reference": "Purchase01",
				"InstructedAmount": {
				   "Amount": "20.00",
				   "Currency": "GBP"
				}
		  	}
		}
		""".stripIndent()
    }
}
