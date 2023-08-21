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

package com.wso2.openbanking.accelerator.identity.idtoken;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test for Open banking token builder.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OAuthServerConfiguration.class, IdentityCommonUtil.class})
public class OBIDTokenBuilderTests extends PowerMockTestCase {

    private OBIDTokenBuilder obidTokenBuilder;
    private OAuthServerConfiguration oAuthServerConfigurationMock;
    private OBIDTokenBuilderForSectorId obidTokenBuilderForSecId;
    OBIDTokenBuilderForCallBackUri obidTokenBuilderForCallBackUri;
    private AuthenticatedUser authenticatedUser = new AuthenticatedUser();
    private static final String USER = "admin@wso2.com";
    private static final String USER2 = "aaa@gold.com";
    private static final String CLIENT_ID = "DummyClientId";
    private static final String TENANT_DOMAIN = "DummyTenantDomain";
    private static final String SCOPES = "accounts basic:read openid";

    @BeforeClass
    public void setup() throws Exception {

        oAuthServerConfigurationMock = mock(OAuthServerConfiguration.class);
        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfigurationMock);
        when(oAuthServerConfigurationMock.getIdTokenSignatureAlgorithm()).thenReturn("SHA256withRSA");

        authenticatedUser.setUserName("aaa@gold.com");
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserStoreDomain("PRIMARY");
        authenticatedUser.setFederatedIdPName("LOCAL");
        authenticatedUser.setFederatedUser(false);
        authenticatedUser.setAuthenticatedSubjectIdentifier("aaa@gold.com@carbon.super");

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.ENABLE_SUBJECT_AS_PPID, true);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);
        obidTokenBuilder = new OBIDTokenBuilder();
        obidTokenBuilderForSecId = new OBIDTokenBuilderForSectorId();
        obidTokenBuilderForCallBackUri = new OBIDTokenBuilderForCallBackUri();
    }

    @Test
    public void getSubjectClaimAuthFlowFromSectorIdentifierSuccess() throws Exception {

        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContextMock = mock(OAuthAuthzReqMessageContext.class);
        OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTOMock = mock(OAuth2AuthorizeReqDTO.class);
        Mockito.doReturn(oAuth2AuthorizeReqDTOMock).when(oAuthAuthzReqMessageContextMock).getAuthorizationReqDTO();
        Mockito.doReturn("https://www.google.com/redirects/redirect1").when(oAuth2AuthorizeReqDTOMock)
                .getCallbackUrl();

        AuthenticatedUser authenticatedUserMock = mock(AuthenticatedUser.class);
        Mockito.doReturn(authenticatedUserMock).when(oAuth2AuthorizeReqDTOMock).getUser();
        Mockito.doReturn(USER).when(authenticatedUserMock)
                .getUsernameAsSubjectIdentifier(Mockito.anyBoolean(), Mockito.anyBoolean());

        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTOMock = mock(OAuth2AuthorizeRespDTO.class);

        mockStatic(IdentityCommonUtil.class);
        when(IdentityCommonUtil.getRegulatoryFromSPMetaData(Mockito.anyString())).thenReturn(true);

        String subject = obidTokenBuilderForSecId.getSubjectClaim(oAuthAuthzReqMessageContextMock,
                oAuth2AuthorizeRespDTOMock, CLIENT_ID, TENANT_DOMAIN, authenticatedUserMock);
        Assert.assertNotNull(subject);
    }

    @Test
    public void getSubjectClaimTokenFlowFromSectorIdentifierSuccess() throws Exception {

        OAuthTokenReqMessageContext oAuthTokenReqMessageContextMock = mock(OAuthTokenReqMessageContext.class);
        OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTOMock = mock(OAuth2AccessTokenRespDTO.class);
        Mockito.doReturn("https://www.google.com/redirects/redirect1").when(oAuth2AccessTokenRespDTOMock)
                .getCallbackURI();

        Mockito.doReturn(SCOPES).when(oAuth2AccessTokenRespDTOMock)
                .getAuthorizedScopes();

        AuthenticatedUser authenticatedUserMock = mock(AuthenticatedUser.class);
        Mockito.doReturn(authenticatedUserMock).when(oAuthTokenReqMessageContextMock).getAuthorizedUser();
        Mockito.doReturn(USER).when(authenticatedUserMock)
                .getUsernameAsSubjectIdentifier(Mockito.anyBoolean(), Mockito.anyBoolean());

        mockStatic(IdentityCommonUtil.class);
        when(IdentityCommonUtil.getRegulatoryFromSPMetaData(Mockito.anyString())).thenReturn(true);

        String subject = obidTokenBuilderForSecId.getSubjectClaim(oAuthTokenReqMessageContextMock,
                oAuth2AccessTokenRespDTOMock, CLIENT_ID, TENANT_DOMAIN, authenticatedUserMock);
        Assert.assertNotNull(subject);
    }

    @Test
    public void getSubjectClaimAuthFlowFromCallBackUriSuccess() throws Exception {

        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContextMock = mock(OAuthAuthzReqMessageContext.class);
        OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTOMock = mock(OAuth2AuthorizeReqDTO.class);
        Mockito.doReturn(oAuth2AuthorizeReqDTOMock).when(oAuthAuthzReqMessageContextMock).getAuthorizationReqDTO();
        Mockito.doReturn("https://www.google.com/redirects/redirect1").when(oAuth2AuthorizeReqDTOMock)
                .getCallbackUrl();

        AuthenticatedUser authenticatedUserMock = mock(AuthenticatedUser.class);
        Mockito.doReturn(authenticatedUserMock).when(oAuth2AuthorizeReqDTOMock).getUser();
        Mockito.doReturn(USER).when(authenticatedUserMock)
                .getUsernameAsSubjectIdentifier(Mockito.anyBoolean(), Mockito.anyBoolean());

        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTOMock = mock(OAuth2AuthorizeRespDTO.class);

        mockStatic(IdentityCommonUtil.class);
        when(IdentityCommonUtil.getRegulatoryFromSPMetaData(Mockito.anyString())).thenReturn(true);

        String subject = obidTokenBuilderForCallBackUri.getSubjectClaim(oAuthAuthzReqMessageContextMock,
                oAuth2AuthorizeRespDTOMock, CLIENT_ID, TENANT_DOMAIN, authenticatedUserMock);
        Assert.assertNotNull(subject);
    }

    @Test
    public void getSubjectClaimTokenFlowFromCallBackUriSuccess() throws Exception {

        OAuthTokenReqMessageContext oAuthTokenReqMessageContextMock = mock(OAuthTokenReqMessageContext.class);
        OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTOMock = mock(OAuth2AccessTokenRespDTO.class);
        Mockito.doReturn("regexp=(https://www.google.com/redirects/redirect1|" +
                "https://www.google.com/redirects/redirect2)").when(oAuth2AccessTokenRespDTOMock)
                .getCallbackURI();

        Mockito.doReturn(SCOPES).when(oAuth2AccessTokenRespDTOMock)
                .getAuthorizedScopes();

        AuthenticatedUser authenticatedUserMock = mock(AuthenticatedUser.class);
        Mockito.doReturn(authenticatedUserMock).when(oAuthTokenReqMessageContextMock).getAuthorizedUser();
        Mockito.doReturn(USER).when(authenticatedUserMock)
                .getUsernameAsSubjectIdentifier(Mockito.anyBoolean(), Mockito.anyBoolean());

        mockStatic(IdentityCommonUtil.class);
        when(IdentityCommonUtil.getRegulatoryFromSPMetaData(Mockito.anyString())).thenReturn(true);

        String subject = obidTokenBuilderForCallBackUri.getSubjectClaim(oAuthTokenReqMessageContextMock,
                oAuth2AccessTokenRespDTOMock, CLIENT_ID, TENANT_DOMAIN, authenticatedUserMock);
        Assert.assertNotNull(subject);
    }

    @Test
    public void getNonPPIDSubjectClaimWithoutTenantAndUserDomainSuccess() throws Exception {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT, true);
        configMap.put(IdentityCommonConstants.REMOVE_TENANT_DOMAIN_FROM_SUBJECT, true);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        OBIDTokenBuilderForCallBackUri obidTokenBuilderForCallBackUri = new OBIDTokenBuilderForCallBackUri();

        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContextMock = mock(OAuthAuthzReqMessageContext.class);
        OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTOMock = mock(OAuth2AuthorizeReqDTO.class);
        Mockito.doReturn(oAuth2AuthorizeReqDTOMock).when(oAuthAuthzReqMessageContextMock).getAuthorizationReqDTO();
        Mockito.doReturn("https://www.google.com/redirects/redirect1").when(oAuth2AuthorizeReqDTOMock)
                .getCallbackUrl();

        oAuth2AuthorizeReqDTOMock.setUser(authenticatedUser);
        Mockito.doReturn(authenticatedUser).when(oAuth2AuthorizeReqDTOMock).getUser();

        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTOMock = mock(OAuth2AuthorizeRespDTO.class);

        mockStatic(IdentityCommonUtil.class);
        when(IdentityCommonUtil.getRegulatoryFromSPMetaData(Mockito.anyString())).thenReturn(true);

        String subject = obidTokenBuilderForCallBackUri.getSubjectClaim(oAuthAuthzReqMessageContextMock,
                oAuth2AuthorizeRespDTOMock, CLIENT_ID, TENANT_DOMAIN, authenticatedUser);
        Assert.assertNotNull(subject);
        Assert.assertEquals(subject, USER2);

        OAuthTokenReqMessageContext oAuthTokenReqMessageContextMock = mock(OAuthTokenReqMessageContext.class);
        OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTOMock = mock(OAuth2AccessTokenRespDTO.class);
        Mockito.doReturn("https://www.google.com/redirects/redirect1").when(oAuth2AccessTokenRespDTOMock)
                .getCallbackURI();

        Mockito.doReturn(SCOPES).when(oAuth2AccessTokenRespDTOMock)
                .getAuthorizedScopes();

        oAuthTokenReqMessageContextMock.setAuthorizedUser(authenticatedUser);
        Mockito.doReturn(authenticatedUser).when(oAuthTokenReqMessageContextMock).getAuthorizedUser();

        mockStatic(IdentityCommonUtil.class);
        when(IdentityCommonUtil.getRegulatoryFromSPMetaData(Mockito.anyString())).thenReturn(true);

        String subjectTokenFlow = obidTokenBuilderForCallBackUri.getSubjectClaim(oAuthTokenReqMessageContextMock,
                oAuth2AccessTokenRespDTOMock, CLIENT_ID, TENANT_DOMAIN, authenticatedUser);
        Assert.assertNotNull(subjectTokenFlow);
        Assert.assertEquals(subjectTokenFlow, USER2);
    }

}

class OBIDTokenBuilderForSectorId extends OBIDTokenBuilder {

    public OBIDTokenBuilderForSectorId() throws IdentityOAuth2Exception {
    }

    @Override
    protected String getSectorIdentifierUri(String clientId) throws OpenBankingException {

        return "https://wso2.com/";
    }
}

class OBIDTokenBuilderForCallBackUri extends OBIDTokenBuilder {

    public OBIDTokenBuilderForCallBackUri() throws IdentityOAuth2Exception {
    }

    @Override
    protected String getSectorIdentifierUri(String clientId) throws OpenBankingException {

        return null;
    }
}
