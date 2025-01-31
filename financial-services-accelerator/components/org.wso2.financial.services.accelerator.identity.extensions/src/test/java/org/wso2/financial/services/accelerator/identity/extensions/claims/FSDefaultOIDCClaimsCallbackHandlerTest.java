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

import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang3.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.internal.OAuth2ServiceComponentHolder;
import org.wso2.carbon.identity.oauth2.model.HttpRequestHeader;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;
import org.wso2.financial.services.accelerator.identity.extensions.util.TestConstants;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;

/**
 * Class which tests FSDefaultOIDCClaimsCallbackHandlerTest.
 */
public class FSDefaultOIDCClaimsCallbackHandlerTest {

    @Spy
    private FSDefaultOIDCClaimsCallbackHandler obDefaultOIDCClaimsCallbackHandler;

    @BeforeClass
    public void beforeClass() throws IdentityApplicationManagementException {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME, "consent_id");
        configMap.put(FinancialServicesConstants.REMOVE_TENANT_DOMAIN_FROM_SUBJECT, "true");
        configMap.put(FinancialServicesConstants.REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT, "true");
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        ServiceProvider serviceProvider = Mockito.mock(ServiceProvider.class);
        ApplicationManagementService applicationMgtService = Mockito.mock(ApplicationManagementService.class);
        doReturn("TestApp").when(applicationMgtService)
                .getServiceProviderNameByClientId(anyString(), anyString(), anyString());
        doReturn(serviceProvider).when(applicationMgtService)
                .getApplicationExcludingFileBasedSPs(anyString(), anyString());

        OAuth2ServiceComponentHolder.setApplicationMgtService(applicationMgtService);

        obDefaultOIDCClaimsCallbackHandler = Mockito.spy(FSDefaultOIDCClaimsCallbackHandler.class);
    }

    public static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            URL url = FSDefaultOIDCClaimsCallbackHandlerTest.class.getClassLoader().getResource(fileName);
            if (url != null) {
                try {
                    File file = new File(url.toURI());
                    return file.getAbsolutePath();
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Could not resolve a file with given path: " +
                            url.toExternalForm());
                }
            }
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    @Test(description = "Test the best case scenario in handling custom claims")
    public void testHandleCustomClaims() throws IdentityOAuth2Exception {

        try (MockedStatic<IdentityCommonUtils> identityCommonUtilsMock =
                     Mockito.mockStatic(IdentityCommonUtils.class);
             MockedStatic<FinancialServicesUtils> fsUtilMock = Mockito.mockStatic(FinancialServicesUtils.class);
             MockedStatic<FrameworkUtils> frameworkUtilsMock = Mockito.mockStatic(FrameworkUtils.class);
             MockedStatic<X509CertUtils> x509CertUtilsMock = Mockito.mockStatic(X509CertUtils.class)) {

            JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
            OAuth2AccessTokenReqDTO oauth2AccessTokenReqDTO = new OAuth2AccessTokenReqDTO();

            HttpRequestHeader[] httpRequestHeaders = new HttpRequestHeader[1];
            httpRequestHeaders[0] = new HttpRequestHeader(IdentityCommonConstants.CERTIFICATE_HEADER,
                    TestConstants.CERTIFICATE_CONTENT);
            oauth2AccessTokenReqDTO.setHttpRequestHeaders(httpRequestHeaders);

            oauth2AccessTokenReqDTO.setGrantType("client_credentials");
            oauth2AccessTokenReqDTO.setClientId("123");
            OAuthTokenReqMessageContext oAuthTokenReqMessageContext =
                    new OAuthTokenReqMessageContext(oauth2AccessTokenReqDTO);

            String[] scopes = new String[1];
            scopes[0] = "consent_id" + "123";
            oAuthTokenReqMessageContext.setScope(scopes);
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            authenticatedUser.setUserName("aaa@gold.com");
            authenticatedUser.setTenantDomain("carbon.super");
            authenticatedUser.setUserStoreDomain("PRIMARY");
            authenticatedUser.setFederatedIdPName("LOCAL");
            authenticatedUser.setFederatedUser(false);
            authenticatedUser.setAuthenticatedSubjectIdentifier("aaa@gold.com@carbon.super");
            oAuthTokenReqMessageContext.setAuthorizedUser(authenticatedUser);
            JWTClaimsSet jwtClaimsSetInitial = Mockito.mock(JWTClaimsSet.class);
            doReturn(new SingletonMap("scope", "test")).when(jwtClaimsSetInitial).getClaims();
            doReturn(jwtClaimsSetInitial).when(obDefaultOIDCClaimsCallbackHandler)
                    .getJwtClaimsFromSuperClass(jwtClaimsSetBuilder, oAuthTokenReqMessageContext);
            identityCommonUtilsMock.when(IdentityCommonUtils::getMTLSAuthHeader)
                    .thenReturn(TestConstants.CERTIFICATE_HEADER);

            fsUtilMock.when(() -> FinancialServicesUtils.isRegulatoryApp(anyString())).thenReturn(true);
            frameworkUtilsMock.when(FrameworkUtils::getMultiAttributeSeparator)
                    .thenReturn(MULTI_ATTRIBUTE_SEPARATOR_DEFAULT);
            Base64URL base64URL = Base64URL.encode(TestConstants.CERTIFICATE_CONTENT);
            x509CertUtilsMock.when(() -> X509CertUtils.computeSHA256Thumbprint(any())).thenReturn(base64URL);
            JWTClaimsSet jwtClaimsSet = obDefaultOIDCClaimsCallbackHandler.handleCustomClaims(jwtClaimsSetBuilder,
                    oAuthTokenReqMessageContext);

            assertEquals("123", jwtClaimsSet.getClaim("consent_id"));
            assertTrue(jwtClaimsSet.getClaim("cnf").toString().contains("{x5t#S256=LS0tLS1CRUdJTiBDRVJUSUZJQ0F" +
                    "URS0tLS0tCk1JSUZPRENDQkNDZ0F3SUJBZ0lFV2NiaWlUQU5CZ2txaGtpRzl3MEJBUXNGQURCVE1Rc3dDUVlEVlFRR0V3Sk" +
                    "gKUWpFVU1CSUdBMVVFQ2hNTFQzQmxia0poYm10cGJtY3hMakFzQmdOVkJBTVRKVTl3Wlc1Q1lXNXJhVzVuSUZCeQpaUzFRY" +
                    "205a2RXTjBhVzl1SUVsemMzVnBibWNnUTBFd0hoY05Nak14TVRFMU1EVXhNRE14V2hjTk1qUXhNakUxCk1EVTBNRE14V2pC" +
                    "aE1Rc3dDUVlEV"));
            assertEquals("aaa@gold.com", jwtClaimsSet.getClaim("sub"));
        }
    }
}
