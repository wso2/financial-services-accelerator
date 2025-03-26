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

import groovy.json.JsonOutput
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.request_builder.ConsentRequestBuilder

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Accounts Request Payloads.
 */
class AccountsRequestPayloads {

    static OffsetDateTime expirationInstant = OffsetDateTime.now().plusDays(5)
    static OffsetDateTime fromInstant = OffsetDateTime.now()
    static OffsetDateTime toInstant = OffsetDateTime.now().plusDays(3)
    static OffsetDateTime transactionToDateTime = OffsetDateTime.now().plusDays(5)
    static OffsetDateTime transactionFromDateTime = OffsetDateTime.now().plusDays(5)
    static OffsetDateTime pastTransactionToDateTime = OffsetDateTime.now().minusDays(5)
    static OffsetDateTime expirationDateTime = OffsetDateTime.now().minusDays(5)
    static DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME

    static ConfigurationService acceleratorConfiguration = new ConfigurationService()

    static String[] permissionsArray = [
            ConnectorTestConstants.READ_ACCOUNTS_BASIC,
            ConnectorTestConstants.READ_ACCOUNTS_DETAIL,
            ConnectorTestConstants.READ_BALANCES,
            ConnectorTestConstants.READ_TRANSACTIONS_DETAIL,
            ConnectorTestConstants.READ_TRANSACTIONS_DEBITS
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
    static String buildValidationAccountsPayload(String accessToken, String userId, String consentId,
                                                 String clientId = acceleratorConfiguration.getAppInfoClientID()) {
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
     		 	"electedResource": "/accounts",
				"clientId": "${clientId}"
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
					"resource": "/aisp/accounts/30080012343456/transactions",
					"context": "/open-banking/v3.1/aisp",
					"httpMethod": "GET"
				},
				"userId": "${userId}",
				"electedResource": "/accounts/{AccountId}/transactions",
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
	    			"resource": "/aisp/accounts/30080012343456/balances",
	    			"context": "/open-banking/v3.1/aisp",
	    			"httpMethod": "GET"
	  				},
	  				"userId": "${userId}",
	  				"electedResource": "/accounts/{accountId}/balances",
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
    static String buildValidationAccountPayloadWithValidAccountId(String accessToken, String userId, String consentId,
                                                                    String clientId = acceleratorConfiguration.getAppInfoClientID()) {
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
					"resource": "/aisp/accounts/30080012343456",
					"context": "/open-banking/v3.1/aisp",
					"httpMethod": "GET"
 			 	 },
 			    "userId": "${userId}",
     		 	"electedResource": "/accounts/{AccountId}",
				"clientId": "${clientId}"
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
    static String buildValidationAccountPayloadWithInvalidAccountId(String accessToken, String userId, String consentId,
                                                 String clientId = acceleratorConfiguration.getAppInfoClientID()) {
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
					"resource": "/aisp/accounts/1234",
					"context": "/open-banking/v3.1/aisp",
					"httpMethod": "GET"
 			 	 },
 			    "userId": "${userId}",
     		 	"electedResource": "/accounts/{AccountId}",
				"clientId": "${clientId}"
 			}
    			"""
    }

    /**
     * Get Initiation Payload with Updated Permissions
     * @param permissionsList
     * @return initiation payload
     */
    static String getUpdatedInitiationPayload(ArrayList<String> permissionsList) {
        return """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsList.toArray())},
                "ExpirationDateTime":"${expirationInstant}",
                "TransactionFromDateTime":"${fromInstant}",
                "TransactionToDateTime":"${toInstant}"
            },
                "Risk":{

                }
            }
        """.stripIndent()
    }

    public static String initiationPayloadWithoutExpirationDate = """
{
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsArray)},
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
    """.stripIndent()

    public static String initiationPayloadWithInvalidExpirationDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${formatter.format(expirationDateTime)}",
            "TransactionFromDateTime":"${fromInstant}",
            "TransactionToDateTime":"${toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithPastExpirationDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${expirationDateTime}",
            "TransactionFromDateTime":"${fromInstant}",
            "TransactionToDateTime":"${toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithPastTransactionToDateTime = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${expirationInstant}",
            "TransactionFromDateTime":"${fromInstant}",
            "TransactionToDateTime":"${pastTransactionToDateTime}"
        },
            "Risk":{

            }
        }
    """.stripIndent()


    public static String initiationPayloadWithoutTransactionToDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithoutTransactionFromDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithInvalidTransactionToDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${expirationInstant}",
            "TransactionFromDateTime":"${transactionToDateTime}",
            "TransactionToDateTime":"${formatter.format(toInstant)}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithInvalidTransactionFromDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${expirationInstant}",
            "TransactionFromDateTime":"${formatter.format(transactionFromDateTime)}",
            "TransactionToDateTime":"${toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadEmptyPayload = """""".stripIndent()

    public static String initiationPayloadEmptyJsonPayload= """
        {}
    """.stripIndent()

    public static String initiationPayloadWithEmptyString = '""'

    public static String initiationPayloadWithSameTransactionFromToDates = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson(permissionsArray)},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.fromInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()
}
