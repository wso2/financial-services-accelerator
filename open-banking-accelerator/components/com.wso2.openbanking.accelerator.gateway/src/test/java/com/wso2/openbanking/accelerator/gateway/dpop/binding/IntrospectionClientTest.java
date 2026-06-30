/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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

package com.wso2.openbanking.accelerator.gateway.dpop.binding;

import com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link IntrospectionClient}. Mocks the static {@link PrivilegedCarbonContext}
 * and {@link KeyManagerHolder} entry points so the tests run without a Carbon runtime.
 */
@PrepareForTest({PrivilegedCarbonContext.class, KeyManagerHolder.class})
public class IntrospectionClientTest extends PowerMockTestCase {

    private static final String TENANT = "carbon.super";
    private static final String TOKEN = "opaque-access-token";
    private static final String JKT = "some-jwk-thumbprint-value";
    private static final long CACHE_TTL = 60L;

    @BeforeClass
    public void beforeClass() {
        // Required so Carbon context static initializers can run before PowerMock intercepts them.
        System.setProperty("carbon.home", "/");
    }

    @BeforeMethod
    public void setUpStaticMocks() {
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);

        PrivilegedCarbonContext mockPcc = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(mockPcc);
        Mockito.when(mockPcc.getTenantDomain(true)).thenReturn(TENANT);
    }

    @Test
    public void secondCallShouldUseCacheAndNotIntrospect() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        KeyManager mockKm = buildKeyManager(TOKEN, JKT);
        PowerMockito.when(KeyManagerHolder.getTenantKeyManagers(TENANT))
                .thenReturn(buildKmDtoMap(mockKm));

        // First call triggers introspection
        String first = client.getJwkThumbprint(TOKEN);
        assertEquals(first, JKT);

        // Second call must hit the cache — KeyManagerHolder must NOT be called again
        String second = client.getJwkThumbprint(TOKEN);
        assertEquals(second, JKT);

        PowerMockito.verifyStatic(Mockito.times(1));
        KeyManagerHolder.getTenantKeyManagers(TENANT);
    }

    @Test
    public void cacheMissWithJktShouldReturnThumbprint() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        KeyManager mockKm = buildKeyManager(TOKEN, JKT);
        PowerMockito.when(KeyManagerHolder.getTenantKeyManagers(TENANT))
                .thenReturn(buildKmDtoMap(mockKm));

        String result = client.getJwkThumbprint(TOKEN);

        assertEquals(result, JKT);
    }

    // ─── Cache miss, token valid but no cnf claim → null not cached ───────────

    @Test
    public void cacheMissWithNoCnfShouldReturnNullAndNotCache() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        KeyManager mockKm = buildKeyManagerNoCnf(TOKEN);
        PowerMockito.when(KeyManagerHolder.getTenantKeyManagers(TENANT))
                .thenReturn(buildKmDtoMap(mockKm));

        String first = client.getJwkThumbprint(TOKEN);
        assertNull(first);

        // Second call should also call introspection (null was not cached)
        String second = client.getJwkThumbprint(TOKEN);
        assertNull(second);

        PowerMockito.verifyStatic(Mockito.times(2));
        KeyManagerHolder.getTenantKeyManagers(TENANT);
    }

    // ─── APIManagementException from one KM → tries next; all fail → throws ──

    @Test
    public void allKeyManagersThrowingShouldThrowIntrospectionException() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        KeyManager throwingKm = Mockito.mock(KeyManager.class);
        Mockito.when(throwingKm.getTokenMetaData(TOKEN))
                .thenThrow(new APIManagementException("service unavailable"));
        PowerMockito.when(KeyManagerHolder.getTenantKeyManagers(TENANT))
                .thenReturn(buildKmDtoMap(throwingKm));

        try {
            client.getJwkThumbprint(TOKEN);
            fail("Expected IntrospectionException");
        } catch (IntrospectionClient.IntrospectionException e) {
            // expected — all key managers exhausted
        }
    }

    @Test
    public void noKeyManagersConfiguredShouldThrowIntrospectionException() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        PowerMockito.when(KeyManagerHolder.getTenantKeyManagers(TENANT))
                .thenReturn(Collections.<String, KeyManagerDto>emptyMap());

        try {
            client.getJwkThumbprint(TOKEN);
            fail("Expected IntrospectionException");
        } catch (IntrospectionClient.IntrospectionException e) {
            // expected
        }
    }

    @Test
    public void nullAccessTokenInfoShouldContinueToNextKeyManager() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        // First KM returns null; second returns a valid result with jkt
        KeyManager nullKm = Mockito.mock(KeyManager.class);
        Mockito.when(nullKm.getTokenMetaData(TOKEN)).thenReturn(null);

        KeyManager validKm = buildKeyManager(TOKEN, JKT);

        Map<String, KeyManagerDto> dtoMap = new HashMap<String, KeyManagerDto>();
        dtoMap.put("nullKm", buildDto(nullKm));
        dtoMap.put("validKm", buildDto(validKm));

        PowerMockito.when(KeyManagerHolder.getTenantKeyManagers(TENANT)).thenReturn(dtoMap);

        String result = client.getJwkThumbprint(TOKEN);
        assertEquals(result, JKT);
    }

    private KeyManager buildKeyManager(String token, String jkt) throws APIManagementException {
        Map<String, Object> cnf = new HashMap<String, Object>();
        cnf.put(DPoPConstants.Claims.JKT_CLAIM, jkt);

        AccessTokenInfo tokenInfo = Mockito.mock(AccessTokenInfo.class);
        Mockito.when(tokenInfo.isTokenValid()).thenReturn(true);
        Mockito.when(tokenInfo.getParameter(DPoPConstants.Claims.CNF_CLAIM)).thenReturn(cnf);

        KeyManager km = Mockito.mock(KeyManager.class);
        Mockito.when(km.getTokenMetaData(token)).thenReturn(tokenInfo);
        return km;
    }

    private KeyManager buildKeyManagerNoCnf(String token) throws APIManagementException {
        AccessTokenInfo tokenInfo = Mockito.mock(AccessTokenInfo.class);
        Mockito.when(tokenInfo.isTokenValid()).thenReturn(true);
        Mockito.when(tokenInfo.getParameter(DPoPConstants.Claims.CNF_CLAIM)).thenReturn(null);

        KeyManager km = Mockito.mock(KeyManager.class);
        Mockito.when(km.getTokenMetaData(token)).thenReturn(tokenInfo);
        return km;
    }

    private Map<String, KeyManagerDto> buildKmDtoMap(KeyManager km) {
        return Collections.singletonMap("defaultKm", buildDto(km));
    }

    private KeyManagerDto buildDto(KeyManager km) {
        KeyManagerDto dto = new KeyManagerDto();
        dto.setKeyManager(km);
        return dto;
    }
}
