/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.gateway.executor.core;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.gateway.executor.exception.OpenBankingExecutorException;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for utility methods used in gateway.
 */
public class UtilityTest {

    private static final String TEST_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwI" +
            "iwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String B64_PAYLOAD = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
            "G4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
    private static final String XML_PAYLOAD = "<soapenv:Body " +
            "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.08\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema- instance\">\n" +
            "    <CstmrCdtTrfInitn>\n" +
            "        <PmtInf>\n" +
            "            <PmtInfId>ABC/086</PmtInfId>\n" +
            "            <PmtMtd>TRF</PmtMtd>\n" +
            "            <BtchBookg>false</BtchBookg>\n" +
            "            <ReqdExctnDt>\n" +
            "                <Dt>2012-09-29</Dt>\n" +
            "            </ReqdExctnDt>\n" +
            "        </PmtInf>\n" +
            "    </CstmrCdtTrfInitn>\n" +
            "</Document></soapenv:Body>";
    private String signingPayload = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.08\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema- instance\">\n" +
            "    <CstmrCdtTrfInitn>\n" +
            "        <PmtInf>\n" +
            "            <PmtInfId>ABC/086</PmtInfId>\n" +
            "            <PmtMtd>TRF</PmtMtd>\n" +
            "            <BtchBookg>false</BtchBookg>\n" +
            "            <ReqdExctnDt>\n" +
            "                <Dt>2012-09-29</Dt>\n" +
            "            </ReqdExctnDt>\n" +
            "        </PmtInf>\n" +
            "    </CstmrCdtTrfInitn>\n" +
            "</Document>";
    private static final String APPLICATION = "APPLICATION";
    private static final String APPLICATION_USER = "APPLICATION_USER";

    @Test(priority = 1)
    public void testB64Encode() throws UnsupportedEncodingException {

        JSONObject payload = GatewayUtils.decodeBase64(B64_PAYLOAD);
        Assert.assertEquals(payload.getString("sub"), "1234567890");
        Assert.assertEquals(payload.getString("name"), "John Doe");
        Assert.assertEquals(payload.getInt("iat"), 1516239022);
    }

    @Test(priority = 1)
    public void testJWTPayloadLoad() {

        Assert.assertEquals(GatewayUtils.getPayloadFromJWT(TEST_JWT), B64_PAYLOAD);
    }

    @Test(priority = 1)
    public void testBasicAuthHeader() {

        Assert.assertEquals(GatewayUtils.getBasicAuthHeader("admin", "admin"),
                "Basic YWRtaW46YWRtaW4=");
    }

    @Test(priority = 2)
    public void testGetXMLPayloadToSign() throws OpenBankingException {

        Assert.assertEquals(GatewayUtils.getXMLPayloadToSign(XML_PAYLOAD), signingPayload);
    }

    @Test (priority = 2)
    public void testIsEligibleRequest() {

        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
    }

    @Test (priority = 3)
    public void testIsEligibleResponse() {

        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.GET_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.GET_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.GET_HTTP_METHOD));
    }

    @Test(description = "Test the extraction of grant type from jwt token payload")
    public void getTokenTypeForUserAccessTokens() throws OpenBankingExecutorException {

        String tokenPayload = "eyJzdWIiOiJhZG1pbkB3c28yLmNvbUBjYXJib24uc3VwZXIiLCJhdXQiOiJBUFBMSUNBVElPTl9VU0VSIiwiY" +
                "XVkIjoiZldwTmNEVzFZM3FwVFVwcHp3SGFGMnZaWllBYSIsIm5iZiI6MTYxNzc5NjM3OCwiZ3JhbnRfdHlwZSI6ImNsaWVudF9j" +
                "cmVkZW50aWFscyIsImNvbnNlbnRfaWQiOiJPQl8xMjM0IiwiYXpwIjoiZldwTmNEVzFZM3FwVFVwcHp3SGFGMnZaWllBYSIsInN" +
                "jb3BlIjoiYWNjb3VudHMgcGF5bWVudHMiLCJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ2L29hdXRoMi90b2tlbiIsImNuZi" +
                "I6eyJ4NXQjUzI1NiI6ImRtaVk1ZE03cE81VzdpbjhrUmFqZkFycXlUTU9uRlcyOVdCVU5rUUlYZTgifSwiZXhwIjoxNjE3Nzk5O" +
                "Tc4LCJpYXQiOjE2MTc3OTYzNzgsImp0aSI6Ijk5NjQwN2RjLTY5MmYtNDk0Ni1hMGRlLTRlOWJkNTU3NWRmNSIsImFsZyI6IkhT" +
                "MjU2In0";
        String grantType = GatewayUtils.getTokenType(tokenPayload);
        Assert.assertEquals(grantType, APPLICATION_USER);
    }


    @Test
    public void getTokenTypeForApplicationAccessTokens() throws OpenBankingExecutorException {

        String tokenPayload = "eyJzdWIiOiJhZG1pbkB3c28yLmNvbUBjYXJib24uc3VwZXIiLCJhdXQiOiJBUFBMSUNBVElPTiIsImF1ZCI6I" +
                "mZXcE5jRFcxWTNxcFRVcHB6d0hhRjJ2WlpZQWEiLCJuYmYiOjE2MTc3OTYzNzgsImdyYW50X3R5cGUiOiJjbGllbnRfY3JlZGVu" +
                "dGlhbHMiLCJhenAiOiJmV3BOY0RXMVkzcXBUVXBwendIYUYydlpaWUFhIiwic2NvcGUiOiJhY2NvdW50cyBwYXltZW50cyIsIml" +
                "zcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0Nlwvb2F1dGgyXC90b2tlbiIsImNuZiI6eyJ4NXQjUzI1NiI6ImRtaVk1ZE03cE" +
                "81VzdpbjhrUmFqZkFycXlUTU9uRlcyOVdCVU5rUUlYZTgifSwiZXhwIjoxNjE3Nzk5OTc4LCJpYXQiOjE2MTc3OTYzNzgsImp0a" +
                "SI6Ijk5NjQwN2RjLTY5MmYtNDk0Ni1hMGRlLTRlOWJkNTU3NWRmNSJ9";
        String grantType = GatewayUtils.getTokenType(tokenPayload);
        Assert.assertEquals(grantType, APPLICATION);
    }

    @Test(description = "Test the extraction of grant type from jwt token payload for GET requests")
    public void getAllowedOAuthFlowsForGetRequest() {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("authorization_code");

        OpenAPI openAPI = getOpenAPI();
        String electedResource = "/testResource";
        String httpMethod = "GET";

        Assert.assertEquals(GatewayUtils.getAllowedOAuthFlowsFromSwagger(openAPI, electedResource, httpMethod),
                oauthFlows);
    }

    @Test(description = "Test the extraction of grant type from jwt token payload for POST requests")
    public void getAllowedOAuthFlowsForPostRequest() {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("authorization_code");

        OpenAPI openAPI = getOpenAPI();
        String electedResource = "/testResource";
        String httpMethod = "POST";

        Assert.assertEquals(GatewayUtils.getAllowedOAuthFlowsFromSwagger(openAPI, electedResource, httpMethod),
                oauthFlows);
    }

    @Test(description = "Test the extraction of grant type from jwt token payload for PUT requests")
    public void getAllowedOAuthFlowsForPutRequest() {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("authorization_code");

        OpenAPI openAPI = getOpenAPI();
        String electedResource = "/testResource";
        String httpMethod = "PUT";

        Assert.assertEquals(GatewayUtils.getAllowedOAuthFlowsFromSwagger(openAPI, electedResource, httpMethod),
                oauthFlows);
    }

    @Test(description = "Test the extraction of grant type from jwt token payload for DELETE requests")
    public void getAllowedOAuthFlowsForDeleteRequest() {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("authorization_code");

        OpenAPI openAPI = getOpenAPI();
        String electedResource = "/testResource";
        String httpMethod = "DELETE";

        Assert.assertEquals(GatewayUtils.getAllowedOAuthFlowsFromSwagger(openAPI, electedResource, httpMethod),
                oauthFlows);
    }

    @Test(description = "Test the extraction of grant type from jwt token payload for PATCH requests")
    public void getAllowedOAuthFlowsForPatchRequest() {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("authorization_code");

        OpenAPI openAPI = getOpenAPI();
        String electedResource = "/testResource";
        String httpMethod = "PATCH";

        Assert.assertEquals(GatewayUtils
                .getAllowedOAuthFlowsFromSwagger(openAPI, electedResource, httpMethod), oauthFlows);
    }

    @Test
    public void checkValidityForCorrectClientCredentialsGrantType() throws OpenBankingExecutorException {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("client_credentials");
        String grantType = APPLICATION;
        GatewayUtils.validateGrantType(grantType, oauthFlows);
    }

    @Test
    public void checkValidityForCorrectAuthCodeGrantType() throws OpenBankingExecutorException {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("authorization_code");
        String grantType = APPLICATION_USER;
        GatewayUtils.validateGrantType(grantType, oauthFlows);
    }

    @Test(expectedExceptions = OpenBankingExecutorException.class)
    public void checkValidityForInvalidClientCredentialsGrantType() throws OpenBankingExecutorException {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("authorization_code");
        String grantType = APPLICATION;
        GatewayUtils.validateGrantType(grantType, oauthFlows);
    }

    @Test(expectedExceptions = OpenBankingExecutorException.class)
    public void checkValidityForInvalidAuthCodeGrantType() throws OpenBankingExecutorException {

        List<String> oauthFlows = new ArrayList<>();
        oauthFlows.add("client_credentials");
        String grantType = APPLICATION_USER;
        GatewayUtils.validateGrantType(grantType, oauthFlows);
    }

    @Test(expectedExceptions = OpenBankingExecutorException.class)
    public void getTokenPayloadWhenAuhHeaderNotPresent() throws OpenBankingExecutorException {

        Map<String, String> transportHeaders = new HashMap<>();
        GatewayUtils.getBearerTokenPayload(transportHeaders);
    }

    @Test
    public void getTokenPayloadWhenAuhHeaderPresent() throws OpenBankingExecutorException {

        String sampleToken = "Bearer abc.xyz.123";
        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put("Authorization", sampleToken);
        Assert.assertEquals(GatewayUtils.getBearerTokenPayload(transportHeaders), "xyz");
    }

    private OpenAPI getOpenAPI() {

        String swagger = "openapi: 3.0.1\n" +
                "info:\n" +
                "  title: TestAPI\n" +
                "  version: \"1.0.0\"\n" +
                "servers:\n" +
                "- url: /testapi/{version}\n" +
                "paths:\n" +
                "  /testResource:\n" +
                "    get:\n" +
                "      tags:\n" +
                "      - Client Registration\n" +
                "      summary: Get a Client Registration for a given Client ID\n" +
                "      responses:\n" +
                "        200:\n" +
                "          description: Client registration retrieval success\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema: {}\n" +
                "      security:\n" +
                "      - PSUOAuth2Security:             \n" +
                "        - accounts\n" +
                "      - default:\n" +
                "        - accounts\n" +
                "      x-auth-type: Application\n" +
                "      x-throttling-tier: Unlimited\n" +
                "    post:\n" +
                "      tags:\n" +
                "      - Client Registration\n" +
                "      summary: Get a Client Registration for a given Client ID\n" +
                "      responses:\n" +
                "        200:\n" +
                "          description: Client registration retrieval success\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema: {}\n" +
                "      security:\n" +
                "      - PSUOAuth2Security:             \n" +
                "        - accounts\n" +
                "      - default:\n" +
                "        - accounts\n" +
                "      x-auth-type: Application\n" +
                "      x-throttling-tier: Unlimited\n" +
                "    delete:\n" +
                "      tags:\n" +
                "      - Client Registration\n" +
                "      summary: Get a Client Registration for a given Client ID\n" +
                "      responses:\n" +
                "        200:\n" +
                "          description: Client registration retrieval success\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema: {}\n" +
                "      security:\n" +
                "      - PSUOAuth2Security:             \n" +
                "        - accounts\n" +
                "      - default:\n" +
                "        - accounts\n" +
                "      x-auth-type: Application\n" +
                "      x-throttling-tier: Unlimited\n" +
                "    put:\n" +
                "      tags:\n" +
                "      - Client Registration\n" +
                "      summary: Get a Client Registration for a given Client ID\n" +
                "      responses:\n" +
                "        200:\n" +
                "          description: Client registration retrieval success\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema: {}\n" +
                "      security:\n" +
                "      - PSUOAuth2Security:             \n" +
                "        - accounts\n" +
                "      - default:\n" +
                "        - accounts\n" +
                "      x-auth-type: Application\n" +
                "      x-throttling-tier: Unlimited\n" +
                "    patch:\n" +
                "      tags:\n" +
                "      - Client Registration\n" +
                "      summary: Get a Client Registration for a given Client ID\n" +
                "      responses:\n" +
                "        200:\n" +
                "          description: Client registration retrieval success\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema: {}\n" +
                "      security:\n" +
                "      - PSUOAuth2Security:             \n" +
                "        - accounts\n" +
                "      - default:\n" +
                "        - accounts\n" +
                "      x-auth-type: Application\n" +
                "      x-throttling-tier: Unlimited\n" +
                "components:\n" +
                "  securitySchemes:\n" +
                "    TPPOAuth2Security:\n" +
                "      type: oauth2\n" +
                "      description: TPP client credential authorisation flow with the ASPSP\n" +
                "      flows:\n" +
                "        clientCredentials:\n" +
                "          tokenUrl: https://authserver.example/token\n" +
                "          scopes: \n" +
                "            accounts: Ability to read Accounts information\n" +
                "    PSUOAuth2Security:\n" +
                "      type: oauth2\n" +
                "      description: >-\n" +
                "        OAuth flow, it is required when the PSU needs to perform SCA with the\n" +
                "        ASPSP when a TPP wants to access an ASPSP resource owned by the PSU\n" +
                "      flows:\n" +
                "        authorizationCode:\n" +
                "          authorizationUrl: 'https://authserver.example/authorization'\n" +
                "          tokenUrl: 'https://authserver.example/token'\n" +
                "          scopes:\n" +
                "            accounts: Ability to read Accounts information\n" +
                "    default:\n" +
                "      type: oauth2\n" +
                "      flows:\n" +
                "        implicit: \n" +
                "            authorizationUrl: https://test.com\n" +
                "            scopes:\n" +
                "              accounts: Ability to read Accounts information";
        OpenAPIParser parser = new OpenAPIParser();
        return parser.readContents(swagger,
                null, null).getOpenAPI();
    }
}
