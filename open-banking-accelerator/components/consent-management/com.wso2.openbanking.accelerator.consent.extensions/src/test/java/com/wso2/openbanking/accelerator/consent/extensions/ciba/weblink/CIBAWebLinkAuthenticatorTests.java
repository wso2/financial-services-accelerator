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

package com.wso2.openbanking.accelerator.consent.extensions.ciba.weblink;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink.CIBAWebLinkAuthenticator;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink.CIBAWebLinkAuthenticatorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.helper.FederatedAuthenticatorUtil;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({ConsentExtensionsDataHolder.class, IdentityExtensionsDataHolder.class, AuthenticatedUser.class,
        FederatedAuthenticatorUtil.class, SignedJWT.class, OpenBankingConfigParser.class, CarbonUtils.class,
        IdentityEventService.class})
public class CIBAWebLinkAuthenticatorTests extends PowerMockTestCase {

    private ConsentCoreServiceImpl consentCoreServiceImpl;
    private OpenBankingConfigParser openBankingConfigParser;

    private static ByteArrayOutputStream outContent;
    private static PrintStream printStream;

    @BeforeClass
    public void initTest() {

        consentCoreServiceImpl = Mockito.mock(ConsentCoreServiceImpl.class);
        openBankingConfigParser = Mockito.mock(OpenBankingConfigParser.class);
        PowerMockito.when(openBankingConfigParser.getCIBAWebLinkAuthenticatorExtension()).thenReturn(
                "com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink" +
                        ".CIBAWebLinkAuthenticatorExtensionImpl");
    }

    @BeforeClass
    public void beforeTests() {

        outContent = new ByteArrayOutputStream();
        printStream = new PrintStream(outContent);
        System.setOut(printStream);
    }

    @BeforeMethod
    private void mockStaticClasses() {

        PowerMockito.mockStatic(ConsentExtensionsDataHolder.class);
        PowerMockito.mockStatic(CarbonUtils.class);
        ConsentExtensionsDataHolder mock = PowerMockito.mock(ConsentExtensionsDataHolder.class);
        PowerMockito.when(ConsentExtensionsDataHolder.getInstance()).thenReturn(mock);
        PowerMockito.when(ConsentExtensionsDataHolder.getInstance().getConsentCoreService())
                .thenReturn(consentCoreServiceImpl);
        IdentityEventService identityEventService = PowerMockito.mock(IdentityEventService.class);
        PowerMockito.when(ConsentExtensionsDataHolder.getInstance().getIdentityEventService())
                .thenReturn(identityEventService);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            expectedExceptionsMessageRegExp = "User does not exist in the User store")
    public void testInvalidLoginHintCase() throws AuthenticationFailedException, UserStoreException {

        CIBAWebLinkAuthenticatorMock mockAuthenticator = spy(new CIBAWebLinkAuthenticatorMock());

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        PowerMockito.when(httpServletRequest.getParameter(Mockito.anyString())).thenReturn("invalidUser");

        PowerMockito.mockStatic(AuthenticatedUser.class);
        PowerMockito.mockStatic(FederatedAuthenticatorUtil.class);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("invalidUser");
        PowerMockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUser);
        PowerMockito.when(FederatedAuthenticatorUtil.isUserExistInUserStore(Mockito.anyString()))
                .thenReturn(false);
        mockAuthenticator.initiateAuthenticationRequest(httpServletRequest, httpServletResponse, authenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            expectedExceptionsMessageRegExp = "Cannot find the user in User store")
    public void testUserStoreExceptionCase() throws AuthenticationFailedException, UserStoreException {

        CIBAWebLinkAuthenticatorMock mockAuthenticator = spy(new CIBAWebLinkAuthenticatorMock());

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        PowerMockito.when(httpServletRequest.getParameter(Mockito.anyString())).thenReturn("invalidUser");

        PowerMockito.mockStatic(AuthenticatedUser.class);
        PowerMockito.mockStatic(FederatedAuthenticatorUtil.class);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("invalidUser");
        PowerMockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUser);
        PowerMockito.when(FederatedAuthenticatorUtil.isUserExistInUserStore(Mockito.anyString()))
                .thenThrow(new UserStoreException());
        mockAuthenticator.initiateAuthenticationRequest(httpServletRequest, httpServletResponse, authenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            expectedExceptionsMessageRegExp = "Could not extract request object from the request.")
    public void testAuthResourcesCreationErrorCase() throws AuthenticationFailedException, UserStoreException {
        CIBAWebLinkAuthenticatorMock mockAuthenticator = spy(new CIBAWebLinkAuthenticatorMock());

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        PowerMockito.when(httpServletRequest.getParameter(Mockito.anyString())).thenReturn("validUser");

        PowerMockito.mockStatic(AuthenticatedUser.class);
        PowerMockito.mockStatic(FederatedAuthenticatorUtil.class);
        PowerMockito.mockStatic(SignedJWT.class);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("validUser");
        PowerMockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUser);
        PowerMockito.when(FederatedAuthenticatorUtil.isUserExistInUserStore(Mockito.anyString()))
                .thenReturn(true);
        authenticationContext.setQueryParams("&object=test");
        mockAuthenticator.initiateAuthenticationRequest(httpServletRequest, httpServletResponse, authenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            expectedExceptionsMessageRegExp = "No matching authorisation resources found for the given consent.")
    public void testAuthResourcesCreationMultiUserErrorCase() throws AuthenticationFailedException, UserStoreException,
            ParseException, ConsentManagementException {
        CIBAWebLinkAuthenticatorMock mockAuthenticator = spy(new CIBAWebLinkAuthenticatorMock());

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        PowerMockito.when(httpServletRequest.getParameter(Mockito.anyString())).thenReturn("user1 ,   user2");

        PowerMockito.mockStatic(AuthenticatedUser.class);
        PowerMockito.mockStatic(FederatedAuthenticatorUtil.class);
        PowerMockito.mockStatic(SignedJWT.class);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("user1");
        AuthenticatedUser authenticatedUser2 = new AuthenticatedUser();
        authenticatedUser2.setUserName("user2");
        PowerMockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier("user1"))
                .thenReturn(authenticatedUser);
        PowerMockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier("user2"))
                .thenReturn(authenticatedUser2);
        PowerMockito.when(FederatedAuthenticatorUtil.isUserExistInUserStore(Mockito.anyString()))
                .thenReturn(true);
        authenticationContext.setQueryParams("&request_object=test");

        SignedJWT signedJWT = mock(SignedJWT.class);
        JSONObject claim = new JSONObject();
        JSONObject userInfoClaim = new JSONObject();
        JSONObject consentIdObject = new JSONObject();

        consentIdObject.put(CIBAWebLinkAuthenticatorConstants.VALUE, "abc");
        userInfoClaim.put(CIBAWebLinkAuthenticatorConstants.OPEN_BANKING_INTENT_ID, consentIdObject);
        claim.put(CIBAWebLinkAuthenticatorConstants.USER_INFO, userInfoClaim);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .claim(CIBAWebLinkAuthenticatorConstants.CLAIMS, claim).build();
        PowerMockito.when(SignedJWT.parse(Mockito.anyString())).thenReturn(signedJWT);
        PowerMockito.when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationStatus(OpenBankingConstants.CREATED_AUTHORISATION_RESOURCE_STATE);
        authorizationResource.setAuthorizationID(String.valueOf(UUID.randomUUID()));
        authorizationResource.setUserID("user1@carbon.super");

        AuthorizationResource authorizationResource2 = new AuthorizationResource();
        authorizationResource2.setAuthorizationStatus(OpenBankingConstants.AUTHORISED_STATUS);
        authorizationResource2.setAuthorizationID(String.valueOf(UUID.randomUUID()));
        authorizationResource2.setUserID("user3@carbon.super");

        authorizationResources.add(authorizationResource);
        authorizationResources.add(authorizationResource2);
        PowerMockito.when(consentCoreServiceImpl.searchAuthorizations(Mockito.anyString()))
                .thenReturn(authorizationResources);
        mockAuthenticator.initiateAuthenticationRequest(httpServletRequest, httpServletResponse, authenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            expectedExceptionsMessageRegExp = "Authorisation resources partially exists for the given consent.")
    public void testAuthResourcesCreationMultiUserErrorCase2() throws AuthenticationFailedException, UserStoreException,
            ParseException, ConsentManagementException {
        CIBAWebLinkAuthenticatorMock mockAuthenticator = spy(new CIBAWebLinkAuthenticatorMock());

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        PowerMockito.when(httpServletRequest.getParameter(Mockito.anyString())).thenReturn("user1 ,   user2");

        PowerMockito.mockStatic(AuthenticatedUser.class);
        PowerMockito.mockStatic(FederatedAuthenticatorUtil.class);
        PowerMockito.mockStatic(SignedJWT.class);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("user1");
        AuthenticatedUser authenticatedUser2 = new AuthenticatedUser();
        authenticatedUser2.setUserName("user2");
        PowerMockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier("user1"))
                .thenReturn(authenticatedUser);
        PowerMockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier("user2"))
                .thenReturn(authenticatedUser2);
        PowerMockito.when(FederatedAuthenticatorUtil.isUserExistInUserStore(Mockito.anyString()))
                .thenReturn(true);
        authenticationContext.setQueryParams("&request_object=test");

        SignedJWT signedJWT = mock(SignedJWT.class);
        JSONObject claim = new JSONObject();
        JSONObject userInfoClaim = new JSONObject();
        JSONObject consentIdObject = new JSONObject();

        consentIdObject.put(CIBAWebLinkAuthenticatorConstants.VALUE, "abc");
        userInfoClaim.put(CIBAWebLinkAuthenticatorConstants.OPEN_BANKING_INTENT_ID, consentIdObject);
        claim.put(CIBAWebLinkAuthenticatorConstants.USER_INFO, userInfoClaim);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .claim(CIBAWebLinkAuthenticatorConstants.CLAIMS, claim).build();
        PowerMockito.when(SignedJWT.parse(Mockito.anyString())).thenReturn(signedJWT);
        PowerMockito.when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationStatus(OpenBankingConstants.CREATED_AUTHORISATION_RESOURCE_STATE);
        authorizationResource.setAuthorizationID(String.valueOf(UUID.randomUUID()));
        authorizationResource.setUserID("user1@carbon.super");

        AuthorizationResource authorizationResource2 = new AuthorizationResource();
        authorizationResource2.setAuthorizationStatus(OpenBankingConstants.AUTHORISED_STATUS);
        authorizationResource2.setAuthorizationID(String.valueOf(UUID.randomUUID()));
        authorizationResource2.setUserID("user2@carbon.super");

        authorizationResources.add(authorizationResource);
        authorizationResources.add(authorizationResource2);
        authorizationResources.add(authorizationResource2);
        PowerMockito.when(consentCoreServiceImpl.searchAuthorizations(Mockito.anyString()))
                .thenReturn(authorizationResources);
        mockAuthenticator.initiateAuthenticationRequest(httpServletRequest, httpServletResponse, authenticationContext);
    }

    @Test()
    public void testAuthResourcesCreationSuccessCase() throws AuthenticationFailedException, UserStoreException,
            ParseException, ConsentManagementException {
        CIBAWebLinkAuthenticatorMock mockAuthenticator = spy(new CIBAWebLinkAuthenticatorMock());

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        PowerMockito.when(httpServletRequest.getParameter(Mockito.anyString())).thenReturn("validUser");

        PowerMockito.mockStatic(AuthenticatedUser.class);
        PowerMockito.mockStatic(FederatedAuthenticatorUtil.class);
        PowerMockito.mockStatic(SignedJWT.class);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("validUser");
        PowerMockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUser);
        PowerMockito.when(FederatedAuthenticatorUtil.isUserExistInUserStore(Mockito.anyString()))
                .thenReturn(true);
        authenticationContext.setQueryParams("&request_object=test");

        SignedJWT signedJWT = mock(SignedJWT.class);
        JSONObject claim = new JSONObject();
        JSONObject userInfoClaim = new JSONObject();
        JSONObject consentIdObject = new JSONObject();

        consentIdObject.put(CIBAWebLinkAuthenticatorConstants.VALUE, "abc");
        userInfoClaim.put(CIBAWebLinkAuthenticatorConstants.OPEN_BANKING_INTENT_ID, consentIdObject);
        claim.put(CIBAWebLinkAuthenticatorConstants.USER_INFO, userInfoClaim);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .claim(CIBAWebLinkAuthenticatorConstants.CLAIMS, claim).build();
        PowerMockito.when(SignedJWT.parse(Mockito.anyString())).thenReturn(signedJWT);
        PowerMockito.when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationStatus(OpenBankingConstants.CREATED_AUTHORISATION_RESOURCE_STATE);
        authorizationResource.setAuthorizationID(String.valueOf(UUID.randomUUID()));
        authorizationResources.add(authorizationResource);
        PowerMockito.when(consentCoreServiceImpl.searchAuthorizations(Mockito.anyString()))
                .thenReturn(authorizationResources);
        PowerMockito.when(openBankingConfigParser.getCibaWebLinkAllowedParams()).thenReturn(new ArrayList<>());
        PowerMockito.when(CarbonUtils.getCarbonServerUrl()).thenReturn("");
        mockAuthenticator.initiateAuthenticationRequest(httpServletRequest, httpServletResponse, authenticationContext);
    }

    static class CIBAWebLinkAuthenticatorMock extends CIBAWebLinkAuthenticator {

        @Override
        protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                     AuthenticationContext context)
                throws AuthenticationFailedException {
            super.initiateAuthenticationRequest(request, response, context);
        }
    }

}
