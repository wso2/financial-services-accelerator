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

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import org.testng.Assert
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

    static ConfigurationService configurationService
    static APIConfigurationService apiConfiguration
    String revisionID

    String publisherUrl

    ApiPublisherRequestBuilder() {
        configurationService = new ConfigurationService()
        apiConfiguration = new APIConfigurationService()

        publisherUrl = configurationService.getApimServerUrl() + ConnectorTestConstants.REST_API_PUBLISHER_ENDPOINT
    }

    /**
     * Create Common Policy for the API.
     * @param accessToken
     * @return
     */
    Map<String, String> createCommonOperationPolicy(String accessToken) {

        List<Map> policyList = apiConfiguration.getPolicyList()
        Map<String, String> policyIdMap = [:]

        policyList.each { policy ->
            String policyName = policy.get("policyName")
            List<Map> policyAttributes = (List<Map>) policy.get("policyAttribute")

            String workingDir = new File(".").getCanonicalPath()
            String policyPath = policy.get("policyFilePath").toString()
            File file = new File(workingDir, policyPath)

            publisherResponse = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + " $accessToken")
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_MULTIPART)
                    .multiPart("synapsePolicyDefinitionFile", file)
                    .multiPart("policySpecFile", new Gson().toJson(buildAllPolicyPayloads(policyName, policyAttributes)))
                    .post(publisherUrl + "/operation-policies")

            String policyId = TestUtil.parseResponseBody(publisherResponse, "id")
            policyIdMap.put(policyName, policyId)
        }

        return policyIdMap
    }

    /**
     * Build all policy payloads based on the api-config-provisioning.yaml.
     * @return
     */
    static JsonObject buildAllPolicyPayloads(String policyName, List<Map> policyAttributes) {

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

        return payload
    }

    /**
     * Create API
     * @param accessToken
     * @return
     */
    String createAPIs(String accessToken, Map<String, Object> apiInfo) {

        String apiID
        def response

        String workingDir = new File(".").getCanonicalPath()
        String apiPath = apiInfo.get("apiFilePath").toString()
        File apiSwaggerFile = new File(workingDir, apiPath)

        String apiName = apiInfo.get("apiName").toString()
        String apiContext = apiInfo.get("context").toString()
        String apiVersion = apiInfo.get("apiVersion").toString()
        String endpointType = apiInfo.get("endpointType").toString()
        String isSchemaEnabled = apiInfo.get("enableSchemaValidation").toString()
        List<Map> apiProperties = (List<Map>) apiInfo["apiProperty"]

        //If selecting Dynamic Endpoint
        if (endpointType == "default") {

            response = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + " $accessToken")
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_MULTIPART)
                    .multiPart("file", apiSwaggerFile)
                    .multiPart("additionalProperties", getApiPayload(apiName, apiVersion, apiContext, endpointType,
                            isSchemaEnabled, apiProperties))
                    .post(publisherUrl + "/apis/import-openapi")

        } else {
            String productionEndpoint = apiInfo.get("productionEndpoint").toString()
            String sandboxEndpoint = apiInfo.get("sandboxEndpoint").toString()

            response = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + " $accessToken")
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_MULTIPART)
                    .multiPart("file", apiSwaggerFile)
                    .multiPart("additionalProperties", getApiPayload(apiName, apiVersion, apiContext, endpointType,
                            isSchemaEnabled, apiProperties, productionEndpoint, sandboxEndpoint))
                    .post(publisherUrl + "/apis/import-openapi")
        }

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
        Assert.assertEquals(TestUtil.parseResponseBody(response, "name"), apiName)
        apiID = TestUtil.parseResponseBody(response, "id")
        return apiID
    }


    /**
     * Create mediation policy by referring the api-config-provisioning.yaml.
     * @param accessToken
     */
    String createRevision(String accessToken, String apiIDs) {

        URI apiEndpoint = new URI(publisherUrl + "/apis/" + apiIDs + "/revisions")
        def response = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + " $accessToken")
                .contentType(ConnectorTestConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(getCreateRevisionPayload("revision1"))
                .post(apiEndpoint)

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
        revisionID = TestUtil.parseResponseBody(response, "id")
        return revisionID
    }

    /**
     * Deploy revision by referring the api-config-provisioning.yaml.
     * @param accessToken
     */
    void deployRevision(String accessToken, String apiID, String revisionID) {

        URI apiEndpoint = new URI(publisherUrl + "/apis/" + apiID + "/deploy-revision")
        String apimHostname = apiEndpoint.getHost()
        def response = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + " $accessToken")
                .contentType(ConnectorTestConstants.CONTENT_TYPE_APPLICATION_JSON)
                .queryParam("revisionId", revisionID)
                .body(getDeployRevisionPayload("localhost", revisionID))
                .post(apiEndpoint)

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
    }

    /**
     * Publish API.
     * @param accessToken
     * @param apiIDs
     */
    void publishAPI(String accessToken, String apiID) {

        URI apiEndpoint = new URI(publisherUrl + "/apis/change-lifecycle")

        def response = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + " $accessToken")
                .contentType(ConnectorTestConstants.CONTENT_TYPE_APPLICATION_JSON)
                .queryParam("apiId", apiID)
                .queryParam("action", "Publish")
                .post(apiEndpoint)

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
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

    /**
     * Generate API payload based on the api-config-provisioning.yaml.
     * @param api_name
     * @param api_version
     * @param api_context
     * @param api_endpoint_type
     * @param isSchemaEnabled
     * @param sandbox_endpoints
     * @param production_endpoints
     * @return
     */
    static String getApiPayload(String api_name, String api_version, String api_context, String api_endpoint_type,
                                          String isSchemaEnabled, List<Map> apiProperties, String sandbox_endpoints = "default",
                                          String production_endpoints = "default") {

        // Generate operations JSON string
        String operationsJson = new Gson().toJson(generateOperations(apiProperties))

        if (!api_endpoint_type.equalsIgnoreCase("default")) {
            sandbox_endpoints = configurationService.getISServerUrl() + sandbox_endpoints
            production_endpoints = configurationService.getISServerUrl() + production_endpoints
        }

        String payload = """
            {
                "name": "$api_name",
                "context": "$api_context",
                "version": "$api_version",
                "gatewayType": "wso2/synapse",
                "gatewayVendor": "wso2",
                "policies":["Unlimited"],
                "enableSchemaValidation": $isSchemaEnabled,
                "apiThrottlingPolicy": "Unlimited",
                "endpointConfig": {
                  "endpoint_type": "$api_endpoint_type",
                  "sandbox_endpoints": {
                    "url": "$sandbox_endpoints"
                  },
                  "production_endpoints": {
                    "url": "$production_endpoints"
                  }
                },
                "apiPolicies": {
                    "request": [{
                        "policyName": "MTLSEnforcement",
                        "parameters": {
                            "transportCertHeaderName": "x-wso2-client-certificate"
                        }
                    }]
                },
                "operations": $operationsJson,
                "lifeCycleStatus": "CREATED",
                "visibility": "PUBLIC"
              }
            """.stripIndent()

        return payload
    }

    /**
     * Generate operations for the API based on the api-properties.
     * @param apiProperties
     * @return
     */
    static JsonArray generateOperations(List<Map> apiProperties) {
        JsonArray operations = new JsonArray()

        apiProperties.each { resource ->
            String apiResource = resource['api-resource']
            String requestType = resource['requestType']
            List<Map> policies = (List<Map>) resource['policy']

            JsonObject operation = new JsonObject()
            operation.addProperty("target", apiResource)
            operation.addProperty("verb", requestType.toUpperCase())

            JsonObject operationPolicies = new JsonObject()
            JsonArray requestPolicyArray = new JsonArray()

            policies.each { policy ->
                JsonObject policyJson = new JsonObject()
                policyJson.addProperty("policyName", policy["name"])

                JsonObject parameters = new JsonObject()
                List<Map> attributes = (List<Map>) policy["policyAttributes"]
                attributes.each { attr ->
                    String attrName = attr["attribute"]
                    String attrValue = attr["attributeValue"]
                    parameters.addProperty(attrName, "null".equals(attrValue) ? null : attrValue)
                }

                policyJson.add("parameters", parameters)
                requestPolicyArray.add(policyJson)
            }

            operationPolicies.add("request", requestPolicyArray)
            operation.add("operationPolicies", operationPolicies)
            operations.add(operation)
        }

        return operations
    }

    /**
     * Update API with the given API information.
     * @param accessToken
     * @param apiInfo
     * @param apiId
     * @return
     */
    List<String> updateAPIs(String accessToken, Map<String, Object> apiInfo, String apiId) {

        def response

        String apiName = apiInfo.get("apiName").toString()
        String apiContext = apiInfo.get("context").toString()
        String apiVersion = apiInfo.get("apiVersion").toString()
        String endpointType = apiInfo.get("endpointType").toString()
        String isSchemaEnabled = apiInfo.get("enableSchemaValidation").toString()
        List<Map> apiProperties = (List<Map>) apiInfo["apiProperty"]

        //If selecting Dynamic Endpoint
        if (endpointType == "default") {

            response = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + " $accessToken")
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .body(getApiPayload(apiName, apiVersion, apiContext, endpointType,
                            isSchemaEnabled, apiProperties))
                    .put(publisherUrl + "/apis/$apiId")

        } else {
            String productionEndpoint = apiInfo.get("productionEndpoint").toString()
            String sandboxEndpoint = apiInfo.get("sandboxEndpoint").toString()

            response = FSRestAsRequestBuilder.buildRequest()
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER_KEY, ConnectorTestConstants.BEARER + " $accessToken")
                    .contentType(ConnectorTestConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .body(getApiPayload(apiName, apiVersion, apiContext, endpointType,
                            isSchemaEnabled, apiProperties, productionEndpoint, sandboxEndpoint))
                    .put(publisherUrl + "/apis/$apiId")
        }

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
        Assert.assertEquals(TestUtil.parseResponseBody(response, "name"), apiName)
    }
}
