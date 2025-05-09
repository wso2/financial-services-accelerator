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

import com.nimbusds.jwt.JWTClaimsSet;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for FSDefaultOIDCClaimsCallbackHandler.
 */
public class FSDefaultOIDCClaimsCallbackHandlerTest {

    private FSDefaultOIDCClaimsCallbackHandler fsClaimsCallbackHandler;
    private MockedStatic<IdentityCommonUtils> identityUtilsMockedStatic;

    @BeforeClass
    public void beforeClass() {
        identityUtilsMockedStatic = mockStatic(IdentityCommonUtils.class);
        fsClaimsCallbackHandler = new FSDefaultOIDCClaimsCallbackHandler();
    }

    @AfterClass
    public void afterClass() {
        identityUtilsMockedStatic.close();
    }

    @Test
    public void testRemoveConsentIdScope() {

        // Arrange
        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        Map<String, Object> claimsInJwtToken = new HashMap<>();
        claimsInJwtToken.put(FinancialServicesConstants.SCOPE, "scope1 scope2 internal_scope");
        claimsInJwtToken.put("customClaim", "customValue");

        // Mock the behavior of IdentityCommonUtils.removeInternalScopes
        identityUtilsMockedStatic.when(() -> IdentityCommonUtils.removeInternalScopes(Mockito.any()))
                .thenReturn(new String[]{"scope1", "scope2"});

        // Act
        fsClaimsCallbackHandler.removeConsentIdScope(jwtClaimsSetBuilder, claimsInJwtToken);

        // Assert
        JWTClaimsSet jwtClaimsSet = jwtClaimsSetBuilder.build();
        Assert.assertEquals(jwtClaimsSet.getClaim(FinancialServicesConstants.SCOPE), "scope1 scope2");
        Assert.assertEquals(jwtClaimsSet.getClaim("customClaim"), "customValue");
    }

    @Test
    public void testAddConsentIDClaim() {

        // Arrange
        OAuthTokenReqMessageContext tokenReqMessageContext =
                new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
        tokenReqMessageContext.setScope(new String[]{"fs_scope1", "scope2", "consent_Id123"});
        Map<String, Object> claimsInJwtToken = new HashMap<>();

        // Mock the behavior of IdentityCommonUtils.removeInternalScopes
        identityUtilsMockedStatic.when(IdentityCommonUtils::getConfiguredConsentIdClaimName)
                .thenReturn("consent_Id");

        // Act
        fsClaimsCallbackHandler.addConsentIDClaim(tokenReqMessageContext, claimsInJwtToken);

        // Assert
        Assert.assertTrue(claimsInJwtToken.containsKey("consent_Id"));
        Assert.assertEquals(claimsInJwtToken.get("consent_Id"), "123");
    }
}
