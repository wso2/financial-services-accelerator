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

package org.wso2.financial.services.accelerator.test.framework.request_builder

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.APIConfigurationService
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Class to contain steps to create a user role in API Manager Console.
 */
class ApiPublisherRequestBuilder extends FSAPIMConnectorTest {

    static ConfigurationService configurationService = new ConfigurationService()
    static APIConfigurationService apiConfiguration
    String publisherUrl
    List<String> mediationPolicyID = new ArrayList<>()
    List<String> revisionID = new ArrayList<>()
    ArrayList<String> apiFilePaths

    @BeforeClass
    void init() {
        FSRestAsRequestBuilder.init()
        apiConfiguration = new APIConfigurationService()
        publisherUrl = configurationService.getApimServerUrl() + publisherUrl
        apiFilePaths = apiConfiguration.getApiFilePath()
    }

        /**
         * Create API
         * @param accessToken
         * @return
         */
        List<String> createAPIs(String accessToken) {
            URI apiEndpoint = new URI("${configurationService.getApimServerUrl()}" + publisherUrl + "/apis/import-openapi")
            List<String> apiIDs = new ArrayList<String>()

            def apis = apiFilePaths.size()
            for (int i = 0; i < apis; i++) {

                //If selecting Dynamic Endpoint
                if (apiConfiguration.getApiEndpointType()[i] == "default") {

                    def response = FSRestAsRequestBuilder.buildRequest()
                            .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + accessToken)
                            .contentType(ConnectorTestConstants.CONTENT_TYPE_MULTIPART)
                            .multiPart("file", new File(apiFilePaths[i]))
                            .multiPart("additionalProperties", getAdditionalProperties(apiConfiguration.getApiName()[i],
                                    "v" + apiConfiguration.getApiProperty()[i]["ob-api-version"].toString().substring(0, 3),
                                    apiConfiguration.getApiContext()[i],
                                    apiConfiguration.getApiEndpointType()[i],
                                    apiConfiguration.getEnableSchemaValidation()[i]))
                            .post apiEndpoint.toString()

                    Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
                    Assert.assertEquals(TestUtil.parseResponseBody(response, "name"), apiConfiguration.getApiName()[i])
                    apiIDs.add(TestUtil.parseResponseBody(response, "id"))

                } else {

                    //If selecting HTTP/Rest Endpoint
                    def response = FSRestAsRequestBuilder.buildRequest()
                            .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + accessToken)
                            .contentType(ConnectorTestConstants.CONTENT_TYPE_MULTIPART)
                            .multiPart("file", new File(apiFilePaths[i]))
                            .multiPart("additionalProperties", getAdditionalProperties(apiConfiguration.getApiName()[i],
                                    apiConfiguration.getApiProperty()[i]["ob-api-version"].toString(),
                                    apiConfiguration.getApiContext()[i],
                                    apiConfiguration.getApiEndpointType()[i],
                                    apiConfiguration.getEnableSchemaValidation()[i],
                                    apiConfiguration.getSandboxEndpoint()[i],
                                    apiConfiguration.getProductionEndpoint()[i]))
                            .post apiEndpoint.toString()

                    Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
                    Assert.assertEquals(TestUtil.parseResponseBody(response, "name"), apiConfiguration.getApiName()[i])
                    apiIDs.add(TestUtil.parseResponseBody(response, "id"))
                }
            }
            return apiIDs
        }

    /**
     * Create mediation policy by referring the api-config-provisioning.yaml.
     * @param accessToken
     */
    void createRevision(String accessToken, List<String> apiIDs) {
        for (int i = 0; i < apiIDs.size(); i++) {
            URI apiEndpoint = new URI("${configurationService.getApimServerUrl()}" + publisherUrl + apiIDs.get(i) + "/revisions")
            def response = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + accessToken)
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .body(getCreateRevisionPayload("revision1"))
                    .post(apiEndpoint)
            Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
            revisionID.add(TestUtil.parseResponseBody(response, "id"))

        }
    }

    /**
     * Deploy revision by referring the api-config-provisioning.yaml.
     * @param accessToken
     */
    void deployRevision(String accessToken, List<String> apiIDs) {
        for (int i = 0; i < apiIDs.size(); i++) {
            URI apiEndpoint = new URI("${configurationService.getServerGatewayURL()}" + publisherUrl + apiIDs.get(i) + "/deploy-revision")
            String apimHostname = apiEndpoint.getHost()
            apimHostname = apimHostname.startsWith("www.") ? apimHostname.substring(4) : apimHostname;
            def response = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + accessToken)
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .queryParam("revisionId", revisionID.get(i))
                    .body(getDeployRevisionPayload(apimHostname, revisionID.get(i)))
                    .post(apiEndpoint)
            Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
        }
    }

    /**
     * Publish API.
     * @param accessToken
     * @param apiIDs
     */
    void publishAPI(String accessToken, List<String> apiIDs) {
        for (int i = 0; i < apiIDs.size(); i++) {
            URI apiEndpoint = new URI("${configurationService.getServerGatewayURL()}" + publisherUrl + "change-lifecycle")
            def response = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + accessToken)
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .queryParam("apiId", apiIDs.get(i))
                    .queryParam("action", "Publish")
                    .post(apiEndpoint)

            Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
        }
    }

        /**
         * Get the additional properties for the API creation.
         * @param api_name
         * @param api_version
         * @param api_context
         * @param api_endpoint_type
         * @param isSchemaEnabled
         * @param sandbox_endpoints
         * @param production_endpoints
         * @return
         */
    static String getAdditionalProperties(String api_name, String api_version, String api_context, String api_endpoint_type,
                                          String isSchemaEnabled, String sandbox_endpoints = "default",
                                          String production_endpoints = "default") {

        if (!api_endpoint_type.equalsIgnoreCase("default")) {
            sandbox_endpoints = configurationService.getISServerUrl() + sandbox_endpoints
            production_endpoints = configurationService.getISServerUrl() + production_endpoints
        }

        return """
            {
              "name": "$api_name",
              "version": "$api_version",
              "context": "$api_context",
              "gatewayType": "wso2/synapse",
              "enableSchemaValidation": $isSchemaEnabled,
              "policies": [
                "Unlimited"
              ],
              "apiThrottlingPolicy": "Unlimited",
              "endpointConfig": {
                "endpoint_type": "$api_endpoint_type",
                "sandbox_endpoints": {
                  "url": "$sandbox_endpoints"
                },
                "production_endpoints": {
                  "url": "$production_endpoints"
                }
              }
            }
            """.stripIndent()
    }

    /**
     * Get create revision payload.
     * @param description
     * @return create revision payload
     */
    static String getCreateRevisionPayload(String description) {
        return """
            {
              "description": "$description"
            }
             """.stripIndent()
    }

    /**
     * Get deploy revision payload.
     * @param apimHostname
     * @return deploy revision payload
     */
    static String getDeployRevisionPayload(String apimHostname, String revisionId){
        return """
            [{
                "revisionUuid": "$revisionId",
                "name": "Default",
                "vhost": "$apimHostname",
                "displayOnDevportal": true
            }]
             """.stripIndent()
    }

    private static JsonObject getPolicySpecFileDefinition(String policyName, JsonArray policyAttribute) {

        JsonObject policySpecObject = new JsonObject()
        policySpecObject.addProperty("category", "Mediation")
        policySpecObject.addProperty("name", policyName)
        policySpecObject.addProperty("displayName", policyName)
        policySpecObject.addProperty("version", "v1")
        policySpecObject.addProperty("description", policyName)
        JsonArray applicableFlows = new JsonArray()
        applicableFlows.add("request")
        policySpecObject.add("applicableFlows", applicableFlows)
        JsonArray supportedApiTypes = new JsonArray()
        supportedApiTypes.add("HTTP")
        policySpecObject.add("supportedApiTypes", supportedApiTypes)
        JsonArray supportedGateways = new JsonArray()
        supportedGateways.add("Synapse")
        policySpecObject.add("supportedGateways", supportedGateways)
        JsonArray policyAttributes = new JsonArray()
        policySpecObject.add("policyAttributes", policyAttributes)
        return policySpecObject
    }


    /**
     * Build all policy payloads based on the api-config-provisioning.yaml.
     * @return
     */
    static Map<String, JsonObject> buildAllPolicyPayloads(String policyName, List<Map> policyAttributes) {

        List<Map> policyList = apiConfiguration.getPolicyList()
        Map<String, JsonObject> payloadMap = [:]

        policyList.each { policy ->
            policyName = policy.get("policyName")
            policyAttributes = (List<Map>) policy.get("policyAttribute")

            JsonObject payload = new JsonObject()
            payload.addProperty("category", "Mediation")
            payload.addProperty("name", policyName)
            payload.addProperty("displayName", policyName)
            payload.addProperty("version", "v1")
            payload.addProperty("description", policyName)

            JsonArray applicableFlows = new JsonArray()
            applicableFlows.add("request")
            payload.add("applicableFlows", applicableFlows)

            JsonArray supportedApiTypes = new JsonArray()
            supportedApiTypes.add("HTTP")
            payload.add("supportedApiTypes", supportedApiTypes)

            JsonArray supportedGateways = new JsonArray()
            supportedGateways.add("Synapse")
            payload.add("supportedGateways", supportedGateways)

            JsonArray attributesArray = new JsonArray()
            policyAttributes.each { attr ->
                JsonObject attribute = new JsonObject()
                attribute.addProperty("name", attr.get("name"))
                attribute.addProperty("displayName", attr.get("name"))
                attribute.add("version", null)
                attribute.addProperty("description", attr.get("description") ?: "")
                // Convert required string ('true'/'false') to boolean
                def requiredValue = attr.get("required")
                def requiredStr = requiredValue != null ? requiredValue.toString().toLowerCase() : ""
                attribute.addProperty("required", requiredStr == "true")
                attribute.addProperty("type", attr.get("type") ?: "String")
                attribute.add("allowedValues", new JsonArray())
                attributesArray.add(attribute)
            }
            payload.add("policyAttributes", attributesArray)

            payloadMap[policyName] = payload
        }

        return payloadMap
    }

    /**
     * Create Common Policy for the API.
     * @param accessToken
     * @param filePath
     * @param payload
     * @return
     */
    String createCommonOperationPolicy(String accessToken, String filePath) {

        List<Map> policyList = apiConfiguration.getPolicyList()

        policyList.each { policy ->
            String policyName = policy.get("policyName")
            List<Map> policyAttributes = (List<Map>) policy.get("policyAttribute")

            publisherResponse = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + accessToken)
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_MULTIPART)
                    .multiPart("synapsePolicyDefinitionFile", new File(filePath))
                    .multiPart("policySpecFile", buildAllPolicyPayloads(policyName, policyAttributes))
                    .post(publisherUrl + "operation-policies")

            return TestUtil.parseResponseBody(publisherResponse, "id")
        }
    }
}
