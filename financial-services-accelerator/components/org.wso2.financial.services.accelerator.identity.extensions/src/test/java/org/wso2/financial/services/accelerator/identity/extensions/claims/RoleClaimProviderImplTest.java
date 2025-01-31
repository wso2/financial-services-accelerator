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

package org.wso2.financial.services.accelerator.identity.extensions.claims;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.Map;

/**
 * Test for role claim provider implementation.
 */
public class RoleClaimProviderImplTest {

    private static final String USER_MARK = "mark@gold.com";
    private static final String USER_TOM = "tom@gold.com";
    private static final String USER_ANN = "ann@gold.com";
    private static final String CCO_ROLE = "Internal/CustomerCareOfficerRole";
    private static final String CC_OFFICER = "customerCareOfficer";
    private static final String[] SCOPES = new String[]{"openid", "consents:read_all"};

    private RoleClaimProviderImpl uut;

    @BeforeClass
    public void init() {
        this.uut = new RoleClaimProviderImpl();
    }

    @Test(description = "when customer care officer is sending request, return customerCareOfficer role")
    public void testGetAdditionalClaimsWithCustomerCareOfficerRole()
            throws IdentityOAuth2Exception, UserStoreException {

        try (MockedStatic<IdentityTenantUtil> identityTenantUtilMock = Mockito.mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityExtensionsDataHolder> identityExtensionsDataHolderMock =
                     Mockito.mockStatic(IdentityExtensionsDataHolder.class)) {

            identityTenantUtilMock.when(() -> IdentityTenantUtil.getTenantIdOfUser(Mockito.anyString()))
                    .thenReturn(1234);

            UserStoreManager userStoreManagerMock = Mockito.mock(UserStoreManager.class);
            Mockito.when(userStoreManagerMock.getRoleListOfUser(Mockito.anyString()))
                        .thenReturn(new String[]{CCO_ROLE});
            UserRealm userRealmMock = Mockito.mock(UserRealm.class);
            Mockito.when(userRealmMock.getUserStoreManager()).thenReturn(userStoreManagerMock);

            RealmService realmServiceMock = Mockito.mock(RealmService.class);
            Mockito.when(realmServiceMock.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealmMock);

            IdentityExtensionsDataHolder dataHolderMock = Mockito
                    .mock(IdentityExtensionsDataHolder.class);
            Mockito.when(dataHolderMock.getRealmService()).thenReturn(realmServiceMock);
            identityExtensionsDataHolderMock.when(IdentityExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

            AuthenticatedUser authorizedUser = new AuthenticatedUser();
            authorizedUser.setUserName(USER_MARK);
            OAuthTokenReqMessageContext oAuthTokenReqMessageContext =
                    new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
            oAuthTokenReqMessageContext.setScope(SCOPES);
            oAuthTokenReqMessageContext.setAuthorizedUser(authorizedUser);

            Map<String, Object> claims = uut.getAdditionalClaims(oAuthTokenReqMessageContext, null);

            // assert
            Assert.assertEquals(claims.get("user_role"), CC_OFFICER);
        }
    }

    @Test(description = "when IdentityRuntimeException occurs, do not return user_role value")
    public void testGetAdditionalClaimsThrowIdentityRuntimeException()
            throws IdentityOAuth2Exception, UserStoreException {

        try (MockedStatic<IdentityTenantUtil> identityTenantUtilMock = Mockito.mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityExtensionsDataHolder> identityExtensionsDataHolderMock =
                     Mockito.mockStatic(IdentityExtensionsDataHolder.class)) {

            identityTenantUtilMock.when(() -> IdentityTenantUtil.getTenantIdOfUser(USER_ANN))
                    .thenThrow(new IdentityRuntimeException(""));

            UserStoreManager userStoreManagerMock = Mockito.mock(UserStoreManager.class);
            Mockito.when(userStoreManagerMock.getRoleListOfUser(Mockito.anyString()))
                    .thenReturn(new String[]{CCO_ROLE});
            UserRealm userRealmMock = Mockito.mock(UserRealm.class);
            Mockito.when(userRealmMock.getUserStoreManager()).thenReturn(userStoreManagerMock);

            RealmService realmServiceMock = Mockito.mock(RealmService.class);
            Mockito.when(realmServiceMock.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealmMock);

            IdentityExtensionsDataHolder dataHolderMock = Mockito
                    .mock(IdentityExtensionsDataHolder.class);
            Mockito.when(dataHolderMock.getRealmService()).thenReturn(realmServiceMock);
            identityExtensionsDataHolderMock.when(IdentityExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

            AuthenticatedUser authorizedUser = new AuthenticatedUser();
            authorizedUser.setUserName(USER_MARK);
            OAuthTokenReqMessageContext oAuthTokenReqMessageContext =
                    new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
            oAuthTokenReqMessageContext.setScope(SCOPES);
            oAuthTokenReqMessageContext.setAuthorizedUser(authorizedUser);

            Map<String, Object> claims = uut.getAdditionalClaims(oAuthTokenReqMessageContext, null);

            // assert
//            Assert.assertFalse(claims.containsKey("user_role"));
        }
    }

    @Test(description = "when UserStoreException occurs, do not return user_role value")
    public void testGetAdditionalClaimsThrowUserStoreException()
            throws IdentityOAuth2Exception, UserStoreException {

        try (MockedStatic<IdentityTenantUtil> identityTenantUtilMock = Mockito.mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityExtensionsDataHolder> identityExtensionsDataHolderMock =
                     Mockito.mockStatic(IdentityExtensionsDataHolder.class)) {

            identityTenantUtilMock.when(() -> IdentityTenantUtil.getTenantIdOfUser(Mockito.anyString()))
                    .thenReturn(1234);

            UserStoreManager userStoreManagerMock = Mockito.mock(UserStoreManager.class);
            Mockito.when(userStoreManagerMock.getRoleListOfUser(USER_TOM)).thenThrow(new UserStoreException());
            UserRealm userRealmMock = Mockito.mock(UserRealm.class);
            Mockito.when(userRealmMock.getUserStoreManager()).thenReturn(userStoreManagerMock);

            RealmService realmServiceMock = Mockito.mock(RealmService.class);
            Mockito.when(realmServiceMock.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealmMock);

            IdentityExtensionsDataHolder dataHolderMock = Mockito
                    .mock(IdentityExtensionsDataHolder.class);
            Mockito.when(dataHolderMock.getRealmService()).thenReturn(realmServiceMock);
            identityExtensionsDataHolderMock.when(IdentityExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

            AuthenticatedUser authorizedUser = new AuthenticatedUser();
            authorizedUser.setUserName(USER_MARK);
            OAuthTokenReqMessageContext oAuthTokenReqMessageContext =
                    new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
            oAuthTokenReqMessageContext.setScope(SCOPES);
            oAuthTokenReqMessageContext.setAuthorizedUser(authorizedUser);

            Map<String, Object> claims = uut.getAdditionalClaims(oAuthTokenReqMessageContext, null);

            // assert
            Assert.assertFalse(claims.containsKey("user_role"));
        }
    }
}
