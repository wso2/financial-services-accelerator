package org.wso2.financial.services.accelerator.is.setup;

import io.restassured.RestAssured
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

class AuthorizeAPIStepTests {

    static String  apiResourceId
    static String clientId
    static String clientSecret
    static String dcrPath
    static ClientRegistrationRequestBuilder registrationRequestBuilder
    static String ssa
    private static ConfigurationService configuration = new ConfigurationService()


    static String encodeCredentials(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

//


    static Response createStandardApplication (String applicationName) {
        return   RestAssured.given().contentType("application/json")
                .relaxedHTTPSValidation()
                .urlEncodingEnabled(true)
                .baseUri(configuration.getISServerUrl())
                .basePath("/api/server/v1/applications")
                .header("Authorization", "Basic ${encodeCredentials(configuration.getISAdminUserName(),configuration.getISAdminPassword())}")
                .body("""
                    {
                    "name": "${applicationName}",
                    "advancedConfigurations": {
                        "skipLogoutConsent": true,
                        "skipLoginConsent": true
                    },
                    "templateId": "custom-application-oidc",
                    "associatedRoles": {
                        "allowedAudience": "APPLICATION",
                        "roles": []
                    },
                    "inboundProtocolConfiguration": {
                        "oidc": {
                            "grantTypes": [
                                "client_credentials"
                            ],
                            "isFAPIApplication": true
                        }
                    }
                }
                    """).post()
    }

/**
 * get application
 *
 */
    static Response getApplication( String applicationId){
        return RestAssured.given().contentType("application/json")
                .relaxedHTTPSValidation()
                .urlEncodingEnabled(true).baseUri(configuration.getISServerUrl()).basePath(
                "/api/server/v1/applications/{applicationId}")
                .pathParam("applicationId",applicationId)
                .header("Authorization",
                        "Basic ${encodeCredentials(configuration.getISAdminUserName(),configuration.getISAdminPassword())}")
                .get();
    }

    static Response dcrGet( String clientId){
        return RestAssured.given().contentType("application/json")
                .relaxedHTTPSValidation()
                .urlEncodingEnabled(true).baseUri(configuration.getISServerUrl()).basePath(
                "/api/identity/oauth2/dcr/v1.1/register")
                .queryParam("client-id",clientId)
                .header("Authorization",
                        "Basic ${encodeCredentials(configuration.getISAdminUserName(),configuration.getISAdminPassword())}")
                .get();
    }




/**
 * create an API resource
 *
 * @param
 * @return
 */
     static Response createAPIResource(String identifier,String displayName, ArrayList<String> scopes) {

         print(configuration.getISAdminPassword())
         print(configuration.getISAdminUserName())
         // build scope array
            StringBuilder scopesArray = new StringBuilder()
            scopes.each {
                scopesArray.append(" { \"name\": \"${it}\", \"displayName\": \"${it}\", \"description\": \"${it}\" },")
            }
         print(scopesArray)

        return  RestAssured.given().contentType("application/json")
                .relaxedHTTPSValidation()
                .urlEncodingEnabled(true).baseUri (configuration.getISServerUrl()+"/api/server/v1/api-resources")
                .header("Authorization",
                  "Basic ${encodeCredentials(configuration.getISAdminUserName(),configuration.getISAdminPassword())}")
                .body("""
                    {
                        "name": "${displayName}",
                        "identifier": "${identifier}",
                        "description": "apiscopes",
                        "requiresAuthorization": true,
                        "scopes": [
                            {
                                "name": "accounts",
                                "displayName": "accounts",
                                "description": "accounts"
                            },
                             {
                                "name": "payments",
                                "displayName": "payments",
                                "description": "payments"
                            },
                             {
                                "name": "fundsconfirmations",
                                "displayName": "fundsconfirmations",
                                "description": "fundsconfirmations"
                            }
                        ]
                    }
                    """).post()
     }

    static Response authorizeAPI(String apiResourceId, String applicationId) {
        return  RestAssured.given().contentType("application/json")
                .relaxedHTTPSValidation()
                .urlEncodingEnabled(true)
                .baseUri(configuration.getISServerUrl())
                .basePath("/api/server/v1/applications/{applicationId}/authorized-apis") // Set base path
                .pathParam("applicationId", applicationId) // Set path parameter
                .header("Authorization", "Basic aXNfYWRtaW5Ad3NvMi5jb206d3NvMjEyMw==")
                .body("""
                {
                    "id": "${apiResourceId}",
                    "policyIdentifier": "RBAC",
                    "scopes": [
                        "accounts",
                        "payments",
                        "fundsconfirmations"
                    ]
                }
                """).post()
    }


    @BeforeTest
    void setup() {
        dcrPath = configuration.getISServerUrl() + ConnectorTestConstants.REGISTRATION_ENDPOINT
        ssa = new File(configuration.getAppDCRSSAPath()).text
        registrationRequestBuilder = new ClientRegistrationRequestBuilder()
    }


    @Test(groups = "api")
    void "Create API Resource"() {
        Response response = createAPIResource("User-defined-oauth2-resource","User-defined-oauth2-resource",["accounts","payments","fundsconfirmations"])
        print(response.prettyPrint())

        apiResourceId = response.header("Location").split("/").last()

        print(response.getBody().prettyPrint())

        // assert status

        Assert.assertEquals( response.statusCode(), 201)

    }
    @Test(groups = "api" ,dependsOnMethods = ["Create API Resource"])
    void "Create Application"() {


        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
                .body(registrationRequestBuilder.getRegularClaims(ssa))
                .post(dcrPath)

        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
        clientSecret = TestUtil.parseResponseBody(registrationResponse, "client_secret")

        if(registrationResponse.statusCode() != 201) {
            Assert.fail("Client registration failed with status code: " + registrationResponse.statusCode())
        }
        File xmlFile = new File(System.getProperty("user.dir").toString().concat("/../../../accelerator-test-framework/src/main/resources/TestConfiguration.xml"))
//        print(xmlFile)
        TestUtil.writeXMLContent(xmlFile.toString(), "Application", "ClientID", clientId,
                0)
        TestUtil.writeXMLContent(xmlFile.toString(), "Application", "ClientSecret", clientSecret,
                0)



    }





}