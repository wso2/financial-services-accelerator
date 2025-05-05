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

        apiResourceId = response.header("location").split("/").last()
        // assert status
        Assert.assertEquals( response.statusCode(), 201)

    }
//    @Test(groups = "api" ,dependsOnMethods = ["Create API Resource"])
//    void "Create Application"() {
//
//
//        def registrationResponse = registrationRequestBuilder.buildRegistrationRequest()
//                .body(registrationRequestBuilder.getRegularClaims(ssa))
//                .post(dcrPath)
//
//        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
//        clientSecret = TestUtil.parseResponseBody(registrationResponse, "client_secret")
//
//        if(registrationResponse.statusCode() != 201) {
//            Assert.fail("Client registration failed with status code: " + registrationResponse.statusCode())
//        }
//        File xmlFile = new File(System.getProperty("user.dir").toString().concat("/../../../accelerator-test-framework/src/main/resources/TestConfiguration.xml"))
//        TestUtil.writeXMLContent(xmlFile.toString(), "Application", "ClientID", clientId,
//                0)
//        TestUtil.writeXMLContent(xmlFile.toString(), "Application", "ClientSecret", clientSecret,
//                0)
//
//
//
//    }





}