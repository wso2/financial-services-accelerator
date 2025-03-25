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

package org.wso2.financial.services.accelerator.gateway.test.accounts.util

import groovy.json.JsonOutput
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants

import java.time.OffsetDateTime

/**
 * Payloads for Account scenarios.
 */
class AccountPayloads {

    static String[] permissionsArray = [
            ConnectorTestConstants.READ_ACCOUNTS_BASIC,
            ConnectorTestConstants.READ_ACCOUNTS_DETAIL,
            ConnectorTestConstants.READ_BALANCES,
            ConnectorTestConstants.READ_TRANSACTIONS_DETAIL
    ]

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

    public static String initiationPayloadEmptyPayload = """""".stripIndent()
    public static String initiationPayloadWithEmptyString = '""'
    public static String initiationPayloadEmptyJsonPayload= """
        {}
    """.stripIndent()

    public static String initiationPayloadWithInvalidExpirationDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${ConnectorTestConstants.fromInstant.toEpochSecond()}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithPastExpirationDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${OffsetDateTime.now().minusDays(3)}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
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
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
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
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant.toEpochSecond()}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithInvalidTransactionFromDate = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant.toEpochSecond()}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithPastTransactionToDateTime = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson(permissionsArray)},
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
            "TransactionToDateTime":"${OffsetDateTime.now().minusDays(3)}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

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

    public static String initiationPayloadWithoutReadAccountBasic = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson([permissionsArray[1], permissionsArray[2], permissionsArray[3]])},
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithoutReadAccountDetails = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson([permissionsArray[0], permissionsArray[2], permissionsArray[3]])},
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithoutReadAccountBalance = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson([permissionsArray[0], permissionsArray[1], permissionsArray[3]])},
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()

    public static String initiationPayloadWithoutReadAccountTransactions = """
        {
            "Data":{
            "Permissions": ${JsonOutput.toJson([permissionsArray[0], permissionsArray[1], permissionsArray[2]])},
            "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
            "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
            "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
        },
            "Risk":{

            }
        }
    """.stripIndent()
}
