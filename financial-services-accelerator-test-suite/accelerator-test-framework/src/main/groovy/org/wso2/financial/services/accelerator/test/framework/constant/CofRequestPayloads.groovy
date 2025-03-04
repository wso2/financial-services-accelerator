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

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * COF Request Payloads
 */
class CofRequestPayloads {

    static ConfigurationService acceleratorConfiguration = new ConfigurationService()
    static OffsetDateTime expirationInstant = OffsetDateTime.now().plusDays(5)
    static OffsetDateTime fromInstant = OffsetDateTime.now()
    static OffsetDateTime toInstant = OffsetDateTime.now().plusDays(3)
    static OffsetDateTime transactionToDateTime = OffsetDateTime.now().plusDays(5)
    static OffsetDateTime transactionFromDateTime = OffsetDateTime.now().plusDays(5)
    static OffsetDateTime pastExpirationInstant = OffsetDateTime.now().minusDays(5)
    static OffsetDateTime expirationDateTime = OffsetDateTime.now().minusDays(5)
    static DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME
    static String accountID = "1234"

    public static String cofInitiationPayload = """
    {
              "Data": {
                "ExpirationDateTime": "${expirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "OB.IBAN",
                  "Identification": "${accountID}",
                  "Name": "Account1",
                  "SecondaryIdentification": "Account1"
                }
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

    public static String emptyInitiationPayload = """""".stripIndent()

    public static String emptyJsonInitiationPayload = """
        {}
    """.stripIndent()

    public static String emptyStringInitiationPayload = '""'

    public static String cofInitiationPayloadWithInvalidDate = """
    {
              "Data": {
                "ExpirationDateTime": "${formatter.format(expirationDateTime)}",
                "DebtorAccount": {
                  "SchemeName": "OB.IBAN",
                  "Identification": "${accountID}",
                  "Name": "Account1",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
	""".stripIndent()

    public static String cofInitiationPayloadWithoutDate = """
	{
              "Data": {
                "DebtorAccount": {
                  "SchemeName": "OB.IBAN",
                  "Identification": "${accountID}",
                  "Name": "Account1",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
	""".stripIndent()

    public static String initiationPayloadWithNullSchemeName = """
            {
              "Data": {
                "ExpirationDateTime": "${expirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "",
                  "Identification": "${accountID}",
                  "Name": "Account1",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
            """.stripIndent()

    public static String initiationPayloadWithInvalidSchemeName = """
            {
              "Data": {
                "ExpirationDateTime": "${expirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "${ConnectorTestConstants.SCHEME_NAME_MAXLENGTH}",
                  "Identification": "${accountID}",
                  "Name": "Account1",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
            """.stripIndent()

    public static String initiationPayloadWithoutIdentification = """
            {
              "Data": {
                "ExpirationDateTime": "${expirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "UK.OBIE.SortCodeAccountNumber",
                  "Name": "Account1",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
            """.stripIndent()

    public static String initiationPayloadWithoutName = """
            {
              "Data": {
                "ExpirationDateTime": "${expirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "UK.OBIE.SortCodeAccountNumber",
                  "Identification": "${accountID}",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
            """.stripIndent()

    public static String initiationPayloadWithInvalidIdentification = """
            {
              "Data": {
                "ExpirationDateTime": "${expirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "UK.OBIE.SortCodeAccountNumber",
                  "Identification": "${ConnectorTestConstants.IDENTIFICATION_MAXLENGTH}",
                  "Name": "Account1",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
            """.stripIndent()

    public static String initiationPayloadWithInvalidName = """
            {
              "Data": {
                "ExpirationDateTime": "${expirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "UK.OBIE.SortCodeAccountNumber",
                  "Identification": "${accountID}",
                  "Name": "${ConnectorTestConstants.NAME_MAXLENGTH}",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
            """.stripIndent()

    public static String initiationPayloadWithInvalidSecondaryIdentification = """
            {
              "Data": {
                "ExpirationDateTime": "${expirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "UK.OBIE.SortCodeAccountNumber",
                  "Identification": "${accountID}",
                  "Name": "Account1",
                  "SecondaryIdentification": "${ConnectorTestConstants.SECONDARY_IDENTIFICATION_MAXLENGTH}"
                }
              }
            }
            """.stripIndent()

    public static String initiationPayloadWithPastExp = """
            {
              "Data": {
                "ExpirationDateTime": "${pastExpirationInstant}",
                "DebtorAccount": {
                  "SchemeName": "UK.OBIE.SortCodeAccountNumber",
                  "Identification": "${accountID}",
                  "Name": "Account1",
                  "SecondaryIdentification": "Account1"
                }
              }
            }
            """.stripIndent()

}
