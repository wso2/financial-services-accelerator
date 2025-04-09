/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.claims;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mockStatic;

/**
 * Test class for FSClaimProvider.
 */
public class FSClaimProviderTest {

    private FSClaimProvider fsClaimProvider;
    private static MockedStatic<FinancialServicesConfigParser> mockedConfigParser;
    private static MockedStatic<JWTUtils> mockedJwtUtils;
    private static MockedStatic<IdentityCommonUtils> mockedIdentityCommonUtils;
    private static MockedStatic<ServiceExtensionUtils> mockedServiceExtensionUtils;
    private ExternalServiceResponse response;

    @BeforeClass
    public void setUp() throws JsonProcessingException {
        fsClaimProvider = new FSClaimProvider();
        mockedConfigParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        mockedJwtUtils = Mockito.mockStatic(JWTUtils.class);
        mockedIdentityCommonUtils = Mockito.mockStatic(IdentityCommonUtils.class);
        mockedServiceExtensionUtils = mockStatic(ServiceExtensionUtils.class);

        String json = "{ \"claims\": [ " +
                "{ \"key\": \"claim1\", \"value\": \"123\" }, " +
                "{ \"key\": \"claim2\", \"value\": \"456\" } " +
                "] }";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode data = objectMapper.readTree(json);

        ExternalServiceResponse externalServiceResponse = new ExternalServiceResponse();
        externalServiceResponse.setStatus(StatusEnum.SUCCESS);
        externalServiceResponse.setData(data);
        response = externalServiceResponse;

        mockedIdentityCommonUtils.when(() -> IdentityCommonUtils
                .removeInternalScopes(Mockito.any())).thenReturn(new String[]{"accounts", "payments"});
    }

    @AfterClass
    public static void afterClass() {
        mockedConfigParser.close();
        mockedJwtUtils.close();
        mockedIdentityCommonUtils.close();
        mockedServiceExtensionUtils.close();
    }

    @Test
    public void testDefaultGetAdditionalClaimsAuthzResponse() throws IdentityOAuth2Exception {
        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext = new OAuthAuthzReqMessageContext(
                new OAuth2AuthorizeReqDTO());
        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTO = new OAuth2AuthorizeRespDTO();

        fsClaimProvider.getAdditionalClaims(oAuthAuthzReqMessageContext, oAuth2AuthorizeRespDTO);
    }

    @Test
    public void testDefaultGetAdditionalClaimsTokenResponse() throws IdentityOAuth2Exception {
        OAuthTokenReqMessageContext oAuthTokenReqMessageContext = new OAuthTokenReqMessageContext(
                new OAuth2AccessTokenReqDTO());
        OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO = new OAuth2AccessTokenRespDTO();
        oAuth2AccessTokenRespDTO.setAuthorizedScopes("accounts payments");

        fsClaimProvider.getAdditionalClaims(oAuthTokenReqMessageContext, oAuth2AccessTokenRespDTO);
    }

    @Test
    public void testDefaultClassGetAdditionalClaimsAuthzResponse() throws IdentityOAuth2Exception {
        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext = new OAuthAuthzReqMessageContext(
                new OAuth2AuthorizeReqDTO());
        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTO = new OAuth2AuthorizeRespDTO();

        FSClaimProvider.setClaimProvider(new FSDefaultClaimProvider());
        fsClaimProvider.getAdditionalClaims(oAuthAuthzReqMessageContext, oAuth2AuthorizeRespDTO);
    }

    @Test
    public void testDefaultClassGetAdditionalClaimsTokenResponse() throws IdentityOAuth2Exception {
        OAuthTokenReqMessageContext oAuthTokenReqMessageContext = new OAuthTokenReqMessageContext(
                new OAuth2AccessTokenReqDTO());
        OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO = new OAuth2AccessTokenRespDTO();
        oAuth2AccessTokenRespDTO.setAuthorizedScopes("accounts payments");

        FSClaimProvider.setClaimProvider(new FSDefaultClaimProvider());
        fsClaimProvider.getAdditionalClaims(oAuthTokenReqMessageContext, oAuth2AccessTokenRespDTO);
    }

    @Test
    public void testServiceExtensionGetAdditionalClaimsAuthzResponse() throws IdentityOAuth2Exception {
        mockedServiceExtensionUtils.when(() -> ServiceExtensionUtils
                        .isInvokeExternalService(Mockito.any())).thenReturn(true);

        mockedServiceExtensionUtils.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(Mockito.any(), Mockito.any())).thenReturn(response);

        OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO = new OAuth2AuthorizeReqDTO();
        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName("abc@wso2.com");
        oAuth2AuthorizeReqDTO.setUser(user);

        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext =
                new OAuthAuthzReqMessageContext(oAuth2AuthorizeReqDTO);
        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTO =
                new OAuth2AuthorizeRespDTO();

        Map<String, Object> claims = fsClaimProvider.getAdditionalClaims(oAuthAuthzReqMessageContext,
                oAuth2AuthorizeRespDTO);
        Assert.assertNotNull(claims);
        Assert.assertEquals(claims.get("claim1"), "123");
        Assert.assertEquals(claims.get("claim2"), "456");
    }

    @Test
    public void testServiceExtensionGetAdditionalClaimsTokenResponse() throws IdentityOAuth2Exception {
        mockedServiceExtensionUtils.when(() -> ServiceExtensionUtils
                .isInvokeExternalService(Mockito.any())).thenReturn(true);

        mockedServiceExtensionUtils.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(Mockito.any(), Mockito.any())).thenReturn(response);

        String sampleJson = "{\"jti\": \"123\"}";
        mockedJwtUtils.when(() -> JWTUtils.decodeRequestJWT(Mockito.any(), Mockito.any())).thenReturn(sampleJson);

        mockedIdentityCommonUtils.when(() -> IdentityCommonUtils.retrieveIntentIDFromReqObjService(Mockito.any(),
                Mockito.any())).thenReturn(Optional.empty());

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName("abc@wso2.com");

        OAuthTokenReqMessageContext oAuthTokenReqMessageContext =
                new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
        oAuthTokenReqMessageContext.setAuthorizedUser(user);
        OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO =
                new OAuth2AccessTokenRespDTO();
        oAuth2AccessTokenRespDTO.setAuthorizedScopes("accounts payments");

        Map<String, Object> claims = fsClaimProvider.getAdditionalClaims(oAuthTokenReqMessageContext,
                oAuth2AccessTokenRespDTO);
        Assert.assertNotNull(claims);
        Assert.assertEquals(claims.get("claim1"), "123");
        Assert.assertEquals(claims.get("claim2"), "456");
    }

}
