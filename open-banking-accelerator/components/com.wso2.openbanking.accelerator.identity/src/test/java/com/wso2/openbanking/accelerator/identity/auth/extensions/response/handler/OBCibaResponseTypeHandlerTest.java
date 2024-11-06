/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.ciba.dao.CibaDAOFactory;
import org.wso2.carbon.identity.oauth.ciba.dao.CibaMgtDAO;
import org.wso2.carbon.identity.oauth.ciba.exceptions.CibaCoreException;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;

import java.util.ArrayList;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class, IdentityExtensionsDataHolder.class, CibaDAOFactory.class})
public class OBCibaResponseTypeHandlerTest extends PowerMockTestCase {

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;
    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    @Mock
    CibaDAOFactory cibaDAOFactory;

    @BeforeMethod
    private void mockStaticClasses() throws ConsentManagementException {

        PowerMockito.mockStatic(IdentityExtensionsDataHolder.class);
        IdentityExtensionsDataHolder mock = PowerMockito.mock(IdentityExtensionsDataHolder.class);
        PowerMockito.when(IdentityExtensionsDataHolder.getInstance()).thenReturn(mock);
        PowerMockito.when(IdentityExtensionsDataHolder.getInstance().getConsentCoreService())
                .thenReturn(consentCoreServiceMock);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        openBankingConfigParser = PowerMockito.mock(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        PowerMockito.mockStatic(CibaDAOFactory.class);
        cibaDAOFactory = PowerMockito.mock(CibaDAOFactory.class);
        PowerMockito.when(CibaDAOFactory.getInstance())
                .thenReturn(cibaDAOFactory);
    }

    @Test()
    public void obCibaResponseTypeHandlerSuccessTest() throws Exception {

        PowerMockito.when(OpenBankingConfigParser.getInstance().getCibaAuthenticationRedirectEndpoint())
                .thenReturn("testURL");
        OBCibaResponseTypeHandler obCibaResponseTypeHandler = new OBCibaResponseTypeHandler();
        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext = mock(OAuthAuthzReqMessageContext.class);
        AuthenticatedUser cibaAuthenticatedUser = mock(AuthenticatedUser.class);
        OAuth2AuthorizeReqDTO authorizationReqDTO = mock(OAuth2AuthorizeReqDTO.class);
        doReturn(authorizationReqDTO).when(oAuthAuthzReqMessageContext).getAuthorizationReqDTO();
        doReturn(cibaAuthenticatedUser).when(authorizationReqDTO).getUser();
        obCibaResponseTypeHandler.issue(oAuthAuthzReqMessageContext);

    }

    @Test()
    public void obCibaResponseTypeHandlerValidConsentIdTest() throws Exception {

        PowerMockito.when(OpenBankingConfigParser.getInstance().getCibaAuthenticationRedirectEndpoint())
                .thenReturn("testURL");
        OBCibaResponseTypeHandler obCibaResponseTypeHandler = new OBCibaResponseTypeHandler();
        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext = mock(OAuthAuthzReqMessageContext.class);
        AuthenticatedUser cibaAuthenticatedUser = mock(AuthenticatedUser.class);
        OAuth2AuthorizeReqDTO authorizationReqDTO = mock(OAuth2AuthorizeReqDTO.class);
        doReturn(authorizationReqDTO).when(oAuthAuthzReqMessageContext).getAuthorizationReqDTO();
        doReturn(cibaAuthenticatedUser).when(authorizationReqDTO).getUser();
        ArrayList<Object> consentIds = new ArrayList<>();
        consentIds.add("test1");
        doReturn(consentIds).when(consentCoreServiceMock)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        DetailedConsentResource consentResourceMock = mock(DetailedConsentResource.class);
        doReturn(consentResourceMock).when(consentCoreServiceMock).getDetailedConsent(anyString());
        doReturn("authorised").when(consentResourceMock).getCurrentStatus();

        CibaMgtDAO cibaMgtDAO = mock(CibaMgtDAO.class);
        doReturn(cibaMgtDAO).when(cibaDAOFactory).getCibaAuthMgtDAO();
        doReturn("authCode").when(cibaMgtDAO).getCibaAuthCodeKey(anyString());
        obCibaResponseTypeHandler.issue(oAuthAuthzReqMessageContext);

    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void obCibaResponseTypeHandlerInValidCallbackTest() throws Exception {

        PowerMockito.when(OpenBankingConfigParser.getInstance().getCibaAuthenticationRedirectEndpoint())
                .thenReturn("");
        OBCibaResponseTypeHandler obCibaResponseTypeHandler = new OBCibaResponseTypeHandler();
        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext = mock(OAuthAuthzReqMessageContext.class);
        AuthenticatedUser cibaAuthenticatedUser = mock(AuthenticatedUser.class);
        OAuth2AuthorizeReqDTO authorizationReqDTO = mock(OAuth2AuthorizeReqDTO.class);
        doReturn(authorizationReqDTO).when(oAuthAuthzReqMessageContext).getAuthorizationReqDTO();
        doReturn(cibaAuthenticatedUser).when(authorizationReqDTO).getUser();
        ArrayList<Object> consentIds = new ArrayList<>();
        consentIds.add("test1");
        doReturn(consentIds).when(consentCoreServiceMock)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        DetailedConsentResource consentResourceMock = mock(DetailedConsentResource.class);
        doReturn(consentResourceMock).when(consentCoreServiceMock).getDetailedConsent(anyString());
        doReturn("authorised").when(consentResourceMock).getCurrentStatus();

        CibaMgtDAO cibaMgtDAO = mock(CibaMgtDAO.class);
        doReturn(cibaMgtDAO).when(cibaDAOFactory).getCibaAuthMgtDAO();
        doReturn("authCode").when(cibaMgtDAO).getCibaAuthCodeKey(anyString());
        obCibaResponseTypeHandler.issue(oAuthAuthzReqMessageContext);

    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void obCibaResponseTypeHandlerInValidTest() throws Exception {

        PowerMockito.when(OpenBankingConfigParser.getInstance().getCibaAuthenticationRedirectEndpoint())
                .thenReturn("test");
        OBCibaResponseTypeHandler obCibaResponseTypeHandler = new OBCibaResponseTypeHandler();
        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext = mock(OAuthAuthzReqMessageContext.class);
        AuthenticatedUser cibaAuthenticatedUser = mock(AuthenticatedUser.class);
        OAuth2AuthorizeReqDTO authorizationReqDTO = mock(OAuth2AuthorizeReqDTO.class);
        doReturn(authorizationReqDTO).when(oAuthAuthzReqMessageContext).getAuthorizationReqDTO();
        doReturn(cibaAuthenticatedUser).when(authorizationReqDTO).getUser();
        ArrayList<Object> consentIds = new ArrayList<>();
        consentIds.add("test1");
        doReturn(consentIds).when(consentCoreServiceMock)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        DetailedConsentResource consentResourceMock = mock(DetailedConsentResource.class);
        doReturn(consentResourceMock).when(consentCoreServiceMock).getDetailedConsent(anyString());
        doReturn("authorised").when(consentResourceMock).getCurrentStatus();

        CibaMgtDAO cibaMgtDAO = mock(CibaMgtDAO.class);
        doReturn(cibaMgtDAO).when(cibaDAOFactory).getCibaAuthMgtDAO();
        doThrow(new CibaCoreException("")).when(cibaMgtDAO).getCibaAuthCodeKey(anyString());
        obCibaResponseTypeHandler.issue(oAuthAuthzReqMessageContext);

    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void obCibaResponseTypeHandlerConsentManagementErrorTest() throws Exception {

        PowerMockito.when(OpenBankingConfigParser.getInstance().getCibaAuthenticationRedirectEndpoint())
                .thenReturn("test");
        OBCibaResponseTypeHandler obCibaResponseTypeHandler = new OBCibaResponseTypeHandler();
        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext = mock(OAuthAuthzReqMessageContext.class);
        AuthenticatedUser cibaAuthenticatedUser = mock(AuthenticatedUser.class);
        OAuth2AuthorizeReqDTO authorizationReqDTO = mock(OAuth2AuthorizeReqDTO.class);
        doReturn(authorizationReqDTO).when(oAuthAuthzReqMessageContext).getAuthorizationReqDTO();
        doReturn(cibaAuthenticatedUser).when(authorizationReqDTO).getUser();
        ArrayList<Object> consentIds = new ArrayList<>();
        consentIds.add("test1");
        doReturn(consentIds).when(consentCoreServiceMock)
                .getConsentIdByConsentAttributeNameAndValue(anyString(), anyString());
        DetailedConsentResource consentResourceMock = mock(DetailedConsentResource.class);
        doThrow(new ConsentManagementException("")).when(consentCoreServiceMock).getDetailedConsent(anyString());
        doReturn("authorised").when(consentResourceMock).getCurrentStatus();
        CibaMgtDAO cibaMgtDAO = mock(CibaMgtDAO.class);
        doReturn(cibaMgtDAO).when(cibaDAOFactory).getCibaAuthMgtDAO();
        obCibaResponseTypeHandler.issue(oAuthAuthzReqMessageContext);

    }
}
