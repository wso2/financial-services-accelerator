package org.wso2.financial.services.accelerator.is.setup;
import io.restassured.RestAssured
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService

class CreateUserStepTests {
    static userId
    static roleId
    private static ConfigurationService configuration = new ConfigurationService()


    static String getUserCreationPayload(String userName, String password , String firstName, String lastName ,
                                         String email) {
        return """
            {
                "userName": "${userName}",
                "password": "${password}",
                "name": {
                    "familyName": "${firstName}",
                    "givenName": "${lastName}"
                },
                "emails": [
                    {
                        "value": "${email}"
                    
                    }
                ]
            }
        """.stripIndent()
    }

    static  Response createUser (String userName, String password , String firstName, String lastName , String email) {
      return  RestAssured.given().relaxedHTTPSValidation()
              .urlEncodingEnabled(true).contentType("application/json")
                .baseUri(configuration.getISServerUrl())
                .header("Authorization", FSConnectorTest.getBasicAuthHeader(configuration.getISAdminUserName(),
                        configuration.getISAdminPassword()))
                .body(getUserCreationPayload(userName, password, firstName, lastName, email))
                .post("/scim2/Users")

    }


    static Response createRole (String roleName, ArrayList<String> scopes ){
        StringBuilder scopesArray = new StringBuilder()
        scopes.each {
            scopesArray.append(" { \"value\": \"${it}\", \"displayName\": \"${it}\"},")
        }
        return RestAssured.given().relaxedHTTPSValidation()
                .urlEncodingEnabled(true).contentType("application/json")
                .baseUri(configuration.getISServerUrl())
                .header("Authorization", org.wso2.financial.services.accelerator.test.framework.FSConnectorTest.getBasicAuthHeader(configuration.getISAdminUserName(),
                        configuration.getISAdminPassword()))
                .body("""
                    {
                        "displayName": "${roleName}",
                        "name": "${roleName}",
                        "permissions": [
                            ${scopesArray}
                        ]
                    }
                """.stripIndent())
                .post("scim2/v2/Roles")
    }

    static Response assignRoleToUser(String userId, String roleId) {
        return RestAssured.given().relaxedHTTPSValidation()
                .urlEncodingEnabled(true).contentType("application/json")
                .baseUri(configuration.getISServerUrl())
                .header("Authorization", FSConnectorTest.getBasicAuthHeader(configuration.getISAdminUserName(),
                        configuration.getISAdminPassword()))
                .body("""
                    {
                        "schemas": ["urn:ietf:params:scim:api:messages:2.0:PatchOp"],
                        "Operations": [
                            {
                                "op": "add",
                                "path": "users",
                                "value": [
                                    {
                                        "value": "${userId}"
                                    }
                                ]
                            }
                        ]
                    }
                """.stripIndent())
                .patch("scim2/v2/Roles/${roleId}")
    }


    @Test
    void "Create User"() {
        String userName = "testUser@wso2.com"
        String password = "testUser@wso2123"
        String firstName = "testFirstName"
        String lastName = "testLastName"
        String email = "test${UUID.randomUUID()}@gmail.com"

        Response createUserResponse = createUser(userName, password, firstName, lastName, email)

        userId = createUserResponse.jsonPath().getString("id")

        Assert.assertEquals(createUserResponse.statusCode(), 201)
    }

    @Test(dependsOnGroups = "api")
    void "Create Role"() {
        String roleName = "subscriber"
        ArrayList<String> scopes = ["accounts", "payments", "fundsconfirmations"]

        Response createRoleResponse = createRole(roleName, scopes)

        roleId = createRoleResponse.jsonPath().getString("id")
        Assert.assertEquals(createRoleResponse.statusCode(), 201)
    }

    @Test(dependsOnMethods = ["Create User", "Create Role"])
    void testAssignRoleToUser() {
        Response assignRoleToUserResponse = assignRoleToUser(userId, roleId)

        Assert.assertEquals(assignRoleToUserResponse.statusCode(), 200)
    }
}
