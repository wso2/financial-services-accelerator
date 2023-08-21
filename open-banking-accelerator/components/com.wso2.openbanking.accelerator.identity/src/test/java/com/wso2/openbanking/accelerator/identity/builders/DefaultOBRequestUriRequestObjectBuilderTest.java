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

package com.wso2.openbanking.accelerator.identity.builders;

import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util.test.jwt.builder.TestJwtBuilder;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test for default Open Banking request, uri request builder.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({SessionDataCacheEntry.class, SessionDataCache.class, OAuth2Util.class, OAuthAppDO.class,
        OAuthServerConfiguration.class})
public class DefaultOBRequestUriRequestObjectBuilderTest extends PowerMockTestCase {

    private DefaultOBRequestUriRequestObjectBuilder defaultOBRequestUriRequestObjectBuilder;

    @Test()
    public void buildRequestObjectSuccessScenario() throws Exception {

        String requestUri = "urn:ietf:params:oauth:request_uri:XKnDFSbXJWjuf0AY6gOT1EIuvdP8BQLo";
        String requestObjectString = TestJwtBuilder.getValidSignedJWT();
        OAuth2Parameters oAuth2Parameters = new OAuth2Parameters();
        oAuth2Parameters.setEssentialClaims(requestObjectString + ":" + "3600666666");

        defaultOBRequestUriRequestObjectBuilder = new DefaultOBRequestUriRequestObjectBuilder();

        SessionDataCache sessionDataCacheMock = mock(SessionDataCache.class);
        SessionDataCacheEntry sessionDataCacheEntry = new SessionDataCacheEntry();
        mockStatic(SessionDataCacheEntry.class);
        mockStatic(SessionDataCache.class);
        when(SessionDataCache.getInstance()).thenReturn(sessionDataCacheMock);
        when(sessionDataCacheMock.getValueFromCache(Mockito.anyObject())).thenReturn(sessionDataCacheEntry);

        sessionDataCacheEntry.setoAuth2Parameters(oAuth2Parameters);

        RequestObject result = defaultOBRequestUriRequestObjectBuilder
                .buildRequestObject(requestUri, oAuth2Parameters);

        Assert.assertNotNull(result);
    }

    @Test(expectedExceptions = RequestObjectException.class)
    public void buildRequestObjectExpiredScenario() throws Exception {

        String requestUri = "urn:ietf:params:oauth:request_uri:XKnDFSbXJWjuf0AY6gOT1EIuvdP8BQLo";
        String requestObjectString = TestJwtBuilder.getValidSignedJWT();
        OAuth2Parameters oAuth2Parameters = new OAuth2Parameters();
        oAuth2Parameters.setEssentialClaims(requestObjectString + ":" + "60");

        defaultOBRequestUriRequestObjectBuilder = new DefaultOBRequestUriRequestObjectBuilder();

        SessionDataCache sessionDataCacheMock = mock(SessionDataCache.class);
        SessionDataCacheEntry sessionDataCacheEntry = new SessionDataCacheEntry();
        mockStatic(SessionDataCacheEntry.class);
        mockStatic(SessionDataCache.class);
        when(SessionDataCache.getInstance()).thenReturn(sessionDataCacheMock);
        when(sessionDataCacheMock.getValueFromCache(Mockito.anyObject())).thenReturn(sessionDataCacheEntry);

        sessionDataCacheEntry.setoAuth2Parameters(oAuth2Parameters);

        defaultOBRequestUriRequestObjectBuilder.buildRequestObject(requestUri, oAuth2Parameters);
    }

    @Test(expectedExceptions = RequestObjectException.class)
    public void testDecryptEncryptedReqObjFailure() throws Exception {

        defaultOBRequestUriRequestObjectBuilder = new DefaultOBRequestUriRequestObjectBuilder();
        String requestUri = "urn:ietf:params:oauth:request_uri:XKnDFSbXJWjuf0AY6gOT1EIuvdP8BQLo";
        String requestObjectString = TestJwtBuilder.getValidEncryptedJWT();
        OAuth2Parameters oAuth2Parameters = new OAuth2Parameters();
        oAuth2Parameters.setTenantDomain("dummyTenantDomain");
        oAuth2Parameters.setEssentialClaims(requestObjectString + ":" + "3600666666");

        SessionDataCache sessionDataCacheMock = mock(SessionDataCache.class);
        SessionDataCacheEntry sessionDataCacheEntry = new SessionDataCacheEntry();
        mockStatic(SessionDataCacheEntry.class);
        mockStatic(SessionDataCache.class);
        when(SessionDataCache.getInstance()).thenReturn(sessionDataCacheMock);
        when(sessionDataCacheMock.getValueFromCache(Mockito.anyObject())).thenReturn(sessionDataCacheEntry);

        sessionDataCacheEntry.setoAuth2Parameters(oAuth2Parameters);

        OAuthServerConfiguration oAuthServerConfigurationMock = mock(OAuthServerConfiguration.class);
        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfigurationMock);

        mockStatic(OAuth2Util.class);
        when(OAuth2Util.getTenantId(Mockito.anyString())).thenReturn(5);

        String path = "src/test/resources";
        File file = new File(path);
        String absolutePathForTestResources = file.getAbsolutePath();

        InputStream keystoreFile = new FileInputStream(absolutePathForTestResources +
                "/wso2carbon.jks");
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(keystoreFile, "wso2carbon".toCharArray());

        String alias = "wso2carbon";

        // Get the private key. Password for the key store is 'wso2carbon'.
        RSAPrivateKey privateKey = (RSAPrivateKey) keystore.getKey(alias, "wso2carbon".toCharArray());

        when(OAuth2Util.getPrivateKey(Mockito.anyString(), Mockito.anyInt())).thenReturn(privateKey);

        defaultOBRequestUriRequestObjectBuilder
                .buildRequestObject(requestUri, oAuth2Parameters);
    }
}
