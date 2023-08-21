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

package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentCache;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test class for CIBAPushAuthenticator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({SessionDataCache.class, AuthenticationContextCache.class, ConsentExtensionUtils.class,
        ConsentCache.class, ConsentData.class, IdentityCommonUtil.class, FrameworkUtils.class,
        AuthenticationContext.class, SequenceConfig.class, ServiceProvider.class, ApplicationConfig.class,
        AuthenticationRequest.class, SessionDataCacheEntry.class, IdentityCache.class, OAuth2Parameters.class,
        HttpServletRequest.class, ConsentCoreServiceImpl.class, ConsentExtensionsDataHolder.class,
        OpenBankingConfigurationService.class, OpenBankingConfigParser.class})
public class CIBAPushAuthenticatorTests extends PowerMockTestCase {

    private final String dummyString = "dummyString";

    @Mock
    OpenBankingConfigurationService openBankingConfigurationService;

    @Mock
    ConsentExtensionsDataHolder consentExtensionsDataHolder;

    @DataProvider(name = "splitQueryParams")
    public Object[][] getSplitQueryParams() {

        String validQueryParms = "key1=val1&key2=val2";
        Map<String, String> validQueryParamMap = new HashMap<>();
        validQueryParamMap.put("key1", "val1");
        validQueryParamMap.put("key2", "val2");

        String invalidQueryParams = "key1:val1&key2:val2";
        Map<String, String> invalidQueryParamMap = new HashMap<>();
        invalidQueryParamMap.put("key1:val1", null);
        invalidQueryParamMap.put("key2:val2", null);

        return new Object[][]{
                {validQueryParms, validQueryParamMap},
                {invalidQueryParams, invalidQueryParamMap}
        };
    }

    @Test(dataProvider = "splitQueryParams")
    public void splitQueryValidQueryTest(String dummyQueryParms, Map<String, String> queryParamMap) throws Exception {
        CIBAPushAuthenticator cibaPushAuthenticator = new CIBAPushAuthenticator();
        Map<String, String> splitQueryParamMap = cibaPushAuthenticator.splitQuery(dummyQueryParms);

        assertEquals(queryParamMap, splitQueryParamMap);
    }

    @Test
    public void handlePreConsentTest() {
        AuthenticationContext mockAuthnCtxt = spy(AuthenticationContext.class);

        Map<String, String> params = new HashMap<String, String>() {
            {
                put(CIBAPushAuthenticatorConstants.LOGIN_HINT, dummyString);
                put(CIBAPushAuthenticatorConstants.REQUEST_OBJECT, dummyString);
                put(CIBAPushAuthenticatorConstants.SCOPE, dummyString);
            }
        };

        Map<String, String[]> queryParamMap = new HashMap<>();
        String[] queryParamArray = new String[1];
        queryParamArray[0] = dummyString;
        queryParamMap.put(CIBAPushAuthenticatorConstants.NONCE, queryParamArray);

        SequenceConfig mockSequenceConfig = mock(SequenceConfig.class);
        ApplicationConfig mockApplicationConfig = mock(ApplicationConfig.class);
        ServiceProvider mockServiceProvider = mock(ServiceProvider.class);
        AuthenticationRequest mockAuthenticationRequest = mock(AuthenticationRequest.class);

        when(mockAuthnCtxt.getSequenceConfig()).thenReturn(mockSequenceConfig);
        when(mockSequenceConfig.getApplicationConfig()).thenReturn(mockApplicationConfig);
        when(mockApplicationConfig.getServiceProvider()).thenReturn(mockServiceProvider);
        when(mockServiceProvider.getApplicationName()).thenReturn(dummyString);
        when(mockAuthnCtxt.getAuthenticationRequest()).thenReturn(mockAuthenticationRequest);
        when(mockAuthenticationRequest.getRequestQueryParams()).thenReturn(queryParamMap);

        int initialSize = mockAuthnCtxt.getEndpointParams().size();
        CIBAPushAuthenticator cibaPushAuthenticator = new CIBAPushAuthenticator();
        cibaPushAuthenticator.handlePreConsent(mockAuthnCtxt, params);
        int finalSize = mockAuthnCtxt.getEndpointParams().size();

        assertTrue(finalSize > initialSize);
    }

    @Test
    public void setMetadataTest() throws Exception {
        CIBAPushAuthenticator mockAuthenticator = spy(new CIBAPushAuthenticatorMock());

        mockStatic(SessionDataCache.class);
        mockStatic(AuthenticationContextCache.class);
        mockStatic(FrameworkUtils.class);

        SessionDataCache sessionDataCache = mock(SessionDataCache.class);
        SessionDataCacheEntry sessionDataCacheEntry = mock(SessionDataCacheEntry.class);
        AuthenticationContextCache authenticationContextCache = mock(AuthenticationContextCache.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletRequest.getParameter(CIBAPushAuthenticatorConstants.BINDING_MESSAGE)).thenReturn(dummyString);

        when(mockAuthenticator.splitQuery(Mockito.anyString())).thenReturn(new HashMap<>());
        when(FrameworkUtils.getQueryStringWithFrameworkContextId
                (Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(dummyString);
        when(SessionDataCache.getInstance()).thenReturn(sessionDataCache);
        when(sessionDataCache.getValueFromCache(Mockito.anyObject())).thenReturn(sessionDataCacheEntry);
        when(AuthenticationContextCache.getInstance()).thenReturn(authenticationContextCache);

        doNothing().when(mockAuthenticator).handlePreConsent(Mockito.anyObject(), Mockito.anyObject());
        doNothing().when(authenticationContextCache).addToCache(Mockito.anyObject(), Mockito.anyObject());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(dummyString, dummyString);
        doReturn(jsonObject).when(mockAuthenticator).retrieveConsent(Mockito.anyObject(),
                Mockito.anyObject(), Mockito.anyString());

        assertNotNull(mockAuthenticator.getAdditionalInfo(httpServletRequest, httpServletResponse,
                dummyString));
    }

    @Test
    public void retrieveConsentTest() throws OpenBankingException {

        CIBAPushAuthenticator mockAuthenticator = spy(new CIBAPushAuthenticatorMock());
        Map<String, Object> configs = new HashMap<>();
        configs.put("Consent.PreserveConsentLink", "true");
        mockStatic(ConsentExtensionUtils.class);
        mockStatic(ConsentCoreServiceImpl.class);
        mockStatic(ConsentExtensionsDataHolder.class);

        OpenBankingConfigParser openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Mockito.doReturn(configs).when(openBankingConfigParserMock).getConfiguration();
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        mockStatic(ConsentCache.class);
        mockStatic(ConsentData.class);
        mockStatic(IdentityCommonUtil.class);

        ConsentData consentData = mock(ConsentData.class);
        IdentityCache identityCache = mock(IdentityCache.class);
        SessionDataCacheEntry cacheEntry = mock(SessionDataCacheEntry.class);
        OAuth2Parameters oAuth2Parameters = mock(OAuth2Parameters.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        IdentityCommonUtil identityCommonUtil = mock(IdentityCommonUtil.class);

        when(ConsentCache.getInstance()).thenReturn(identityCache);
        when(ConsentCache.getCacheEntryFromSessionDataKey(Mockito.anyString())).thenReturn(cacheEntry);
        when(cacheEntry.getoAuth2Parameters()).thenReturn(oAuth2Parameters);
        when(oAuth2Parameters.getRedirectURI()).thenReturn(dummyString);
        when(oAuth2Parameters.getClientId()).thenReturn(dummyString);
        when(oAuth2Parameters.getState()).thenReturn(dummyString);
        when(identityCommonUtil.getRegulatoryFromSPMetaData(dummyString)).thenReturn(true);
        when(consentData.getType()).thenReturn(dummyString);
        when(consentData.getApplication()).thenReturn(dummyString);

        Map<String, Serializable> sensitiveDataMap = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        sensitiveDataMap.put(CIBAPushAuthenticatorConstants.IS_ERROR, "false");
        sensitiveDataMap.put(CIBAPushAuthenticatorConstants.LOGGED_IN_USER, dummyString);
        sensitiveDataMap.put(CIBAPushAuthenticatorConstants.SP_QUERY_PARAMS, dummyString);
        sensitiveDataMap.put(CIBAPushAuthenticatorConstants.SCOPE, dummyString);

        when(ConsentExtensionUtils.getSensitiveDataWithConsentKey(Mockito.anyString())).thenReturn(sensitiveDataMap);
        when(ConsentExtensionUtils.getHeaders(httpServletRequest)).thenReturn(headers);
        when(mockAuthenticator.createConsentData(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyObject())).thenReturn(consentData);

        doNothing().when(consentData).setSensitiveDataMap(Mockito.anyObject());
        doNothing().when(consentData).setRedirectURI(Mockito.anyObject());
        doNothing().when(consentData).setRegulatory(Mockito.anyObject());
        doNothing().when(mockAuthenticator).executeRetrieval(Mockito.anyObject(), Mockito.anyObject());

        assertNotNull(mockAuthenticator.retrieveConsent(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyString()));

    }

}

class CIBAPushAuthenticatorMock extends CIBAPushAuthenticator {

    @Override
    protected AuthenticationContext getAutenticationContext(String sessionDataKey) {

        return mock(AuthenticationContext.class);
    }

    @Override
    protected ConsentData createConsentData(String sessionDataKey, String loggedInUser, String spQueryParams,
                                            String scopeString, String app, HttpServletRequest request) {
        return mock(ConsentData.class);
    }

}
