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

import com.nimbusds.jwt.JWTClaimsSet;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.token.util.TestConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.model.HttpRequestHeader;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;

/**
 * Class which tests OBDefaultOIDCClaimsCallbackHandlerTest.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({FrameworkUtils.class, IdentityCommonUtil.class, JWTClaimsSet.class})
public class OBDefaultOIDCClaimsCallbackHandlerTest {

    @Spy
    private OBDefaultOIDCClaimsCallbackHandler obDefaultOIDCClaimsCallbackHandler;

    @BeforeClass
    public void beforeClass() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME, "consent_id");
        configMap.put(IdentityCommonConstants.REMOVE_TENANT_DOMAIN_FROM_SUBJECT, "true");
        configMap.put(IdentityCommonConstants.REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT, "true");
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        mockStatic(FrameworkUtils.class);
        mockStatic(IdentityCommonUtil.class);
        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(MULTI_ATTRIBUTE_SEPARATOR_DEFAULT);
        obDefaultOIDCClaimsCallbackHandler = Mockito.spy(OBDefaultOIDCClaimsCallbackHandler.class);
    }

    public static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            URL url = OBDefaultOIDCClaimsCallbackHandlerTest.class.getClassLoader().getResource(fileName);
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

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test(description = "Test the best case scenario in handling custom claims")
    public void testHandleCustomClaims() throws OpenBankingException, IdentityOAuth2Exception {

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
        JWTClaimsSet jwtClaimsSetInitial = PowerMockito.mock(JWTClaimsSet.class);
        PowerMockito.when(jwtClaimsSetInitial.getClaims()).thenReturn(new SingletonMap("scope", "test"));
        Mockito.doReturn(jwtClaimsSetInitial).when(obDefaultOIDCClaimsCallbackHandler)
                .getJwtClaimsFromSuperClass(jwtClaimsSetBuilder, oAuthTokenReqMessageContext);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("123")).thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        JWTClaimsSet jwtClaimsSet = obDefaultOIDCClaimsCallbackHandler.handleCustomClaims(jwtClaimsSetBuilder,
                oAuthTokenReqMessageContext);


        assertEquals("123", jwtClaimsSet.getClaim("consent_id"));
        assertEquals("{x5t#S256=GA370hkNKyI1C060VmxL4xZtKyjD6aQUjrGKYWoeZX8}", jwtClaimsSet.getClaim(
                "cnf").toString());
        assertEquals("aaa@gold.com", jwtClaimsSet.getClaim("sub"));
    }
}
