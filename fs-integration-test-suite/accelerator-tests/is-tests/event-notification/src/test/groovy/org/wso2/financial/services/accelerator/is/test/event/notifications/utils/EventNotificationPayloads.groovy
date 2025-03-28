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

package org.wso2.financial.services.accelerator.is.test.event.notifications.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import io.restassured.response.Response
import org.json.JSONObject
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants

/**
 * Event Notification Payloads
 */
class EventNotificationPayloads {

    final static String eventCreationRequestPayload(String resourceId) {
        return """
        {
           "urn_uk_org_openbanking_events_resource-update":{
              "resourceID":"${resourceId}",
              "ref":"https://scim.example.com/Users/44f6142df96bd6ab61e7521d9"
           },
           "urn_uk_org_openbanking_events_consent-authorization-revoked":{
              "ref":"https://scim.example.com/Users/44f6142df96bd6ab61e7521d9",
              "attributes":[
                 "id",
                 "name",
                 "userName",
                 "password",
                 "emails"
              ]
           },
           "urn:ietf:params:scim:event:create":{
              "ref":"https://scim.example.com/Users/44f6142df96bd6ab61e7521d9",
              "attributes":[
                 "id",
                 "name",
                 "userName",
                 "password",
                 "emails"
              ],
              "resourceID":"${resourceId}"
           }
        }"""
    }

    final static String initialEventPollingRequestPayload = """
        {
            "returnImmediately" : ${EventNotificationConstants.SHORT_POLLING},
            "maxEvents" : ${EventNotificationConstants.RETURN_MORE_EVENT_NOTIFICATIONS}
        }
    """.stripIndent()

    final static String getAcknowledgementOnlyEventPollingRequestPayload(Response pollingResponse) {

        ObjectMapper oMapper = new ObjectMapper()
        JSONObject jsonPollingResponse = new JSONObject(pollingResponse.body().asString())
        Map<String, String> receivedSets = oMapper.readValue(jsonPollingResponse.get("sets").toString(), HashMap.class)
        String[] jtiArray = receivedSets.keySet().toArray()

        return """
        {
            "returnImmediately" : ${EventNotificationConstants.SHORT_POLLING},
            "maxEvents" : ${EventNotificationConstants.RETURN_NO_EVENT_NOTIFICATIONS},
            "ack" : ["${jtiArray[0]}"],
            "setErrs" : {"${jtiArray[1]}" : {
                    "err": "jwtIss",
                    "description": "Issuer is invalid or could not be verified"
                    }
            }
        }""".stripIndent()
    }

    final static String getAcknowledgementAndPollEventPollingRequestPayload(Response pollingResponse) {

        ObjectMapper oMapper = new ObjectMapper()
        JSONObject jsonPollingResponse = new JSONObject(pollingResponse.body().asString())
        Map<String, String> receivedSets = oMapper.readValue(jsonPollingResponse.get("sets").toString(), HashMap.class)
        String[] jtiArray = receivedSets.keySet().toArray()

        return """
        {
            "returnImmediately" : ${EventNotificationConstants.SHORT_POLLING},
            "maxEvents" : ${EventNotificationConstants.RETURN_MORE_EVENT_NOTIFICATIONS},
            "ack" : ["${jtiArray[0]}"],
            "setErrs" : {"${jtiArray[1]}" : {
                    "err": "jwtIss",
                    "description": "Issuer is invalid or could not be verified"
                    }
            }
        }""".stripIndent()
    }

    final static String creationPayloadEventSubscription = """
    {
        "callbackUrl": "${EventNotificationConstants.CALLBACK_URL_CREATE}",
        "version": "${EventNotificationConstants.VERSION}",
        "eventTypes": [
            "${EventNotificationConstants.CONSENT_AUTHORIZATION_REVOKED_EVENT_TYPE}",
            "${EventNotificationConstants.RESOURCE_UPDATE_EVENT_TYPE}",
            "${EventNotificationConstants.CREATE_EVENT_TYPE}"
        ]
    }
    """.stripIndent()

    static String getSubscriptionUpdatePayload(String subscriptionId) {
        return """
            {
                "subscriptionId": "${subscriptionId}",
                "callbackUrl": "${EventNotificationConstants.CALLBACK_URL_UPDATE}",
                "version": "${EventNotificationConstants.VERSION}",
                "eventTypes": [
                    "${EventNotificationConstants.RESOURCE_UPDATE_EVENT_TYPE}"
                ]
            }
        """.stripIndent()
    }

    final static String subscriptionCreationPayloadWithNoCallbackUrl = """
        {
            "callbackUrl": "",
            "version": "${EventNotificationConstants.VERSION}",
            "eventTypes": [
                "${EventNotificationConstants.CONSENT_AUTHORIZATION_REVOKED_EVENT_TYPE}",
                "${EventNotificationConstants.RESOURCE_UPDATE_EVENT_TYPE}",
                "${EventNotificationConstants.CREATE_EVENT_TYPE}"
            ]
        }
    """.stripIndent()

    final static String creationPayloadEventSubscriptionWithNoEventTypes = """
        {
            "callbackUrl": "${EventNotificationConstants.CALLBACK_URL_CREATE}",
            "version": "${EventNotificationConstants.VERSION}",
            "eventTypes": [
                
            ]
        }
    """.stripIndent()

    final static String subscriptionCreationPayloadWithoutCallbackUrl = """
        {
            "version": "${EventNotificationConstants.VERSION}",
            "eventTypes": [
                "${EventNotificationConstants.CONSENT_AUTHORIZATION_REVOKED_EVENT_TYPE}",
                "${EventNotificationConstants.RESOURCE_UPDATE_EVENT_TYPE}",
                "${EventNotificationConstants.CREATE_EVENT_TYPE}"
            ]
        }
    """.stripIndent()

    final static String accountInitiationPayload = """
            {
                "Data":{
                "Permissions": ${JsonOutput.toJson([ConnectorTestConstants.READ_ACCOUNTS_BASIC,
                                                    ConnectorTestConstants.READ_ACCOUNTS_DETAIL,
                                                    ConnectorTestConstants.READ_BALANCES])},
                "ExpirationDateTime":"${ConnectorTestConstants.expirationInstant}",
                "TransactionFromDateTime":"${ConnectorTestConstants.fromInstant}",
                "TransactionToDateTime":"${ConnectorTestConstants.toInstant}"
            },
                "Risk":{

                }
            }
""".stripIndent()
}
