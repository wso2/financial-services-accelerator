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
import org.mockito.Spy;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.financial.services.accelerator.identity.extensions.util.TestConstants;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Test class for FSDefaultClaimProvider.
 */
public class FSDefaultClaimProviderTest {

    @Spy
    private FSClaimProvider fsDefaultClaimProvider = spy(FSClaimProvider.class);

    @Test
    public void testHandleCustomClaims() throws IdentityOAuth2Exception {

        try (MockedStatic<OAuthServerConfiguration> configuration = mockStatic(OAuthServerConfiguration.class)) {

            OAuthServerConfiguration oAuthServerConfigurationInstance = mock(OAuthServerConfiguration.class);
            doReturn("PS256").when(oAuthServerConfigurationInstance).getIdTokenSignatureAlgorithm();

            configuration.when(OAuthServerConfiguration::getInstance).thenReturn(oAuthServerConfigurationInstance);

            try (MockedStatic<SessionDataCache> sessionDataCache = mockStatic(SessionDataCache.class)) {

                Map<String, String[]> paramMap = Map.of("request", new String[]{TestConstants.VALID_REQUEST});
                SessionDataCacheEntry sessionDataCacheEntry = mock(SessionDataCacheEntry.class);
                doReturn(paramMap).when(sessionDataCacheEntry).getParamMap();
                SessionDataCache sessionDataCacheInstance = mock(SessionDataCache.class);
                doReturn(sessionDataCacheEntry).when(sessionDataCacheInstance).getValueFromCache(any());
                sessionDataCache.when(SessionDataCache::getInstance).thenReturn(sessionDataCacheInstance);

                OAuth2AuthorizeReqDTO authorizeReqDTO = new OAuth2AuthorizeReqDTO();
                authorizeReqDTO.setSessionDataKey("1234");
                authorizeReqDTO.setResponseType("code id-token");

                OAuth2AuthorizeRespDTO authorizeRespDTO = new OAuth2AuthorizeRespDTO();
                OAuthAuthzReqMessageContext authzReqMessageContext = new OAuthAuthzReqMessageContext(authorizeReqDTO);

                Map<String, Object> claims = fsDefaultClaimProvider
                        .getAdditionalClaims(authzReqMessageContext, authorizeRespDTO);

                assertFalse(claims.isEmpty());
            }
        }
    }
}
