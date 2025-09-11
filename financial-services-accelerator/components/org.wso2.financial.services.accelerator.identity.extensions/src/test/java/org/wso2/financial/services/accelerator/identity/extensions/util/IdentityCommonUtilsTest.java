/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.util;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for IdentityCommonUtils.
 */
public class IdentityCommonUtilsTest {

    @BeforeClass
    public void beforeClass() throws ConsentManagementException {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME, "consent_id");
        configMap.put(FinancialServicesConstants.APPEND_CONSENT_ID_TO_ACCESS_TOKEN, "true");
        configMap.put(FinancialServicesConstants.PRE_INITIATED_CONSENT_SCOPES, Collections.singletonList("accounts"));
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        ConsentCoreService consentCoreService = mock(ConsentCoreService.class);
        when(consentCoreService.getConsentIdByConsentAttributeNameAndValue(Mockito.any(), Mockito.any()))
                .thenReturn(new ArrayList<>(Arrays.asList("123")));
        IdentityExtensionsDataHolder.getInstance().setConsentCoreService(consentCoreService);
    }

    @Test
    public void testRemoveInternalScopes() {
        String[] scopes = {"scope1", "scope2", "consent_id123", "FS_scope", "TIME_scope", "x5t#_scope"};

        String[] result = IdentityCommonUtils.removeInternalScopes(scopes);
        Assert.assertEquals(result, new String[]{"scope1", "scope2"});
    }

    @Test
    public void testGetConsentIdFromScopesArray() {
        String[] scopes = {"scope1", "consent_id123", "scope2"};

        String consentId = IdentityCommonUtils.getConsentIdFromScopesArray(scopes);
        Assert.assertEquals(consentId, "123");
    }

    @Test
    public void testGetConfiguredConsentIdClaimName() {
        String consentIdClaimName = IdentityCommonUtils.getConfiguredConsentIdClaimName();
        Assert.assertEquals(consentIdClaimName, "consent_id");
    }

    @Test
    public void testAddConsentIdToTokenResponse() {
        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        tokenRespDTO.setAuthorizedScopes("scope1 consent_id123 scope2");

        IdentityCommonUtils.addConsentIdToTokenResponse(tokenRespDTO);
        Assert.assertEquals(tokenRespDTO.getParameters().get("consent_id"), "123");
    }

    @Test
    public void testGetCommonAuthIdFromCookies() {
        Cookie[] cookies = {
                new Cookie("other_cookie", "value1"),
                new Cookie("commonAuthId", "testCommonAuthId")
        };

        String commonAuthId = IdentityCommonUtils.getCommonAuthIdFromCookies(cookies);
        Assert.assertEquals(commonAuthId, "testCommonAuthId");
    }

    @Test
    public void testGetConsentIdFromAuthzRequestContext() throws Exception {
        OAuth2AuthorizeReqDTO authzReqDTO = new OAuth2AuthorizeReqDTO();
        authzReqDTO.setCookie(new Cookie[]{new Cookie("commonAuthId", "123")});
        OAuthAuthzReqMessageContext authzReqMessageContext = new OAuthAuthzReqMessageContext(authzReqDTO);
        authzReqMessageContext.setApprovedScope(new String[] { "payments" });

        Map<String, Object> configMap = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();
        configMap.put(FinancialServicesConstants.PRE_INITIATED_CONSENT_SCOPES, Collections.singletonList("accounts"));
        configMap.put(FinancialServicesConstants.SCOPE_BASED_CONSENT_SCOPES, Collections.singletonList("payments"));

        String consentId = IdentityCommonUtils.getConsentIdFromAuthzRequestContext(authzReqMessageContext);
        Assert.assertNotNull(consentId);
    }


    @Test
    public void testUpdateApprovedScopes() throws Exception {
        OAuth2AuthorizeReqDTO authzReqDTO = new OAuth2AuthorizeReqDTO();
        authzReqDTO.setCookie(new Cookie[]{new Cookie("commonAuthId", "123")});
        OAuthAuthzReqMessageContext authzReqMessageContext = new OAuthAuthzReqMessageContext(authzReqDTO);
        authzReqMessageContext.setApprovedScope(new String[]{"scope1", "scope2"});

        Map<String, Object> configMap = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();
        configMap.put(FinancialServicesConstants.PRE_INITIATED_CONSENT_SCOPES, Collections.singletonList("scope"));
        configMap.put(FinancialServicesConstants.SCOPE_BASED_CONSENT_SCOPES, Collections.singletonList("scope1"));

        String[] updatedScopes = IdentityCommonUtils.updateApprovedScopes(authzReqMessageContext);
        Assert.assertEquals(updatedScopes, new String[]{"scope1", "scope2", "consent_id123"});
    }

    @Test
    public void testAddConsentIdToTokenResponse_NoConsentId() {
        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        tokenRespDTO.setAuthorizedScopes("scope1 scope2");

        IdentityCommonUtils.addConsentIdToTokenResponse(tokenRespDTO);
        Assert.assertNull(tokenRespDTO.getParameters().get("consent_id"));
    }

    @Test
    public void testGetSpMetaData() {
        ServiceProvider serviceProvider = new ServiceProvider();
        ServiceProviderProperty property1 = new ServiceProviderProperty();
        property1.setName("key1");
        property1.setValue("value1");

        ServiceProviderProperty property2 = new ServiceProviderProperty();
        property2.setName("key2");
        property2.setValue("value2,value3");

        serviceProvider.setSpProperties(new ServiceProviderProperty[]{property1, property2});

        Map<String, Object> spMetaData = IdentityCommonUtils.getSpMetaData(serviceProvider);
        Assert.assertEquals(spMetaData.get("key1"), "value1");
        Assert.assertEquals(spMetaData.get("key2"), "value2,value3");
    }
}
