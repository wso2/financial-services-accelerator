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

package com.wso2.openbanking.accelerator.identity.claims;

import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Map;

/**
 * Test for role claim provider implementation.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityTenantUtil.class, IdentityExtensionsDataHolder.class})
public class RoleClaimProviderImplTest extends PowerMockTestCase {

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

    public void setup(String userName) throws UserStoreException {
        PowerMockito.mockStatic(IdentityTenantUtil.class);
        if (USER_ANN.equals(userName)) {
            PowerMockito.when(IdentityTenantUtil.getTenantIdOfUser(USER_ANN))
                    .thenThrow(new IdentityRuntimeException(""));
        } else {
            PowerMockito.when(IdentityTenantUtil.getTenantIdOfUser(Mockito.anyString())).thenReturn(1234);
        }

        UserStoreManager userStoreManagerMock = Mockito.mock(UserStoreManager.class);
        if (USER_TOM.equals(userName)) {
            Mockito.when(userStoreManagerMock.getRoleListOfUser(USER_TOM)).thenThrow(new UserStoreException());
        } else {
            Mockito.when(userStoreManagerMock.getRoleListOfUser(Mockito.anyString()))
                    .thenReturn(new String[]{CCO_ROLE});
        }
        UserRealm userRealmMock = Mockito.mock(UserRealm.class);
        Mockito.when(userRealmMock.getUserStoreManager()).thenReturn(userStoreManagerMock);

        RealmService realmServiceMock = Mockito.mock(RealmService.class);
        Mockito.when(realmServiceMock.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealmMock);

        IdentityExtensionsDataHolder identityExtensionsDataHolderMock = Mockito
                .mock(IdentityExtensionsDataHolder.class);
        Mockito.when(identityExtensionsDataHolderMock.getRealmService()).thenReturn(realmServiceMock);

        PowerMockito.mockStatic(IdentityExtensionsDataHolder.class);
        PowerMockito.when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolderMock);
    }

    @Test(description = "when customer care officer is sending request, return customerCareOfficer role")
    public void testGetAdditionalClaimsWithCustomerCareOfficerRole()
            throws IdentityOAuth2Exception, UserStoreException {
        setup(USER_MARK);
        // mock
        OAuthTokenReqMessageContext oAuthTokenReqMessageContextMock = Mockito.mock(OAuthTokenReqMessageContext.class);
        AuthenticatedUser authorizedUserMock = Mockito.mock(AuthenticatedUser.class);

        // when
        Mockito.when(authorizedUserMock.getUserName()).thenReturn(USER_MARK);

        Mockito.when(oAuthTokenReqMessageContextMock.getScope()).thenReturn(SCOPES);
        Mockito.when(oAuthTokenReqMessageContextMock.getAuthorizedUser()).thenReturn(authorizedUserMock);

        Map<String, Object> claims = uut.getAdditionalClaims(oAuthTokenReqMessageContextMock, null);

        // assert
        Assert.assertEquals(claims.get("user_role"), CC_OFFICER);
    }

    @Test(description = "when IdentityRuntimeException occurs, do not return user_role value")
    public void testGetAdditionalClaimsThrowIdentityRuntimeException()
            throws IdentityOAuth2Exception, UserStoreException {
        setup(USER_ANN);
        // mock
        OAuthTokenReqMessageContext oAuthTokenReqMessageContextMock = Mockito.mock(OAuthTokenReqMessageContext.class);
        AuthenticatedUser authorizedUserMock = Mockito.mock(AuthenticatedUser.class);

        // when
        Mockito.when(authorizedUserMock.getUserName()).thenReturn(USER_ANN);

        Mockito.when(oAuthTokenReqMessageContextMock.getScope()).thenReturn(SCOPES);
        Mockito.when(oAuthTokenReqMessageContextMock.getAuthorizedUser()).thenReturn(authorizedUserMock);

        Map<String, Object> claims = uut.getAdditionalClaims(oAuthTokenReqMessageContextMock, null);

        // assert
        Assert.assertFalse(claims.containsKey("user_role"));
    }

    @Test(description = "when UserStoreException occurs, do not return user_role value")
    public void testGetAdditionalClaimsThrowUserStoreException()
            throws IdentityOAuth2Exception, UserStoreException {
        setup(USER_TOM);
        // mock
        OAuthTokenReqMessageContext oAuthTokenReqMessageContextMock = Mockito.mock(OAuthTokenReqMessageContext.class);
        AuthenticatedUser authorizedUserMock = Mockito.mock(AuthenticatedUser.class);

        // when
        Mockito.when(authorizedUserMock.getUserName()).thenReturn(USER_TOM);

        Mockito.when(oAuthTokenReqMessageContextMock.getScope()).thenReturn(SCOPES);
        Mockito.when(oAuthTokenReqMessageContextMock.getAuthorizedUser()).thenReturn(authorizedUserMock);

        Map<String, Object> claims = uut.getAdditionalClaims(oAuthTokenReqMessageContextMock, null);

        // assert
        Assert.assertFalse(claims.containsKey("user_role"));
    }
}
