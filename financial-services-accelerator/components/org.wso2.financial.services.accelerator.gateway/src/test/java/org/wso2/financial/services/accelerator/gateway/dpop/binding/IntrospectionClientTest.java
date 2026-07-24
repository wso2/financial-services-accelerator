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

package org.wso2.financial.services.accelerator.gateway.dpop.binding;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link IntrospectionClient}. Uses {@code mockStatic} for
 * {@link PrivilegedCarbonContext} and {@link KeyManagerHolder} to avoid a Carbon runtime.
 */
public class IntrospectionClientTest {

    private static final String TENANT = "carbon.super";
    private static final String TOKEN = "opaque-access-token";
    private static final String JKT = "some-jwk-thumbprint-value";
    private static final long CACHE_TTL = 60L;

    @BeforeClass
    public void beforeClass() {
        // Required to allow Carbon context classes to initialize their static blocks
        // before mockStatic intercepts them — same pattern used in other Carbon tests.
        System.setProperty("carbon.home", "/");
    }

    @Test
    public void secondCallShouldUseCacheAndNotIntrospect() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        try (MockedStatic<PrivilegedCarbonContext> pccStatic =
                     Mockito.mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<KeyManagerHolder> kmhStatic =
                     Mockito.mockStatic(KeyManagerHolder.class)) {

            PrivilegedCarbonContext mockPcc = Mockito.mock(PrivilegedCarbonContext.class);
            pccStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPcc);
            Mockito.when(mockPcc.getTenantDomain(true)).thenReturn(TENANT);

            KeyManager mockKm = buildKeyManager(TOKEN, JKT);
            kmhStatic.when(() -> KeyManagerHolder.getTenantKeyManagers(TENANT))
                    .thenReturn(buildKmDtoMap(mockKm));

            // First call triggers introspection
            String first = client.getJwkThumbprint(TOKEN);
            assertEquals(first, JKT);

            // Second call must hit the cache — KeyManagerHolder must NOT be called again
            String second = client.getJwkThumbprint(TOKEN);
            assertEquals(second, JKT);

            kmhStatic.verify(() -> KeyManagerHolder.getTenantKeyManagers(TENANT),
                    Mockito.times(1));
        }
    }

    @Test
    public void cacheMissWithJktShouldReturnThumbprint() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        try (MockedStatic<PrivilegedCarbonContext> pccStatic =
                     Mockito.mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<KeyManagerHolder> kmhStatic =
                     Mockito.mockStatic(KeyManagerHolder.class)) {

            PrivilegedCarbonContext mockPcc = Mockito.mock(PrivilegedCarbonContext.class);
            pccStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPcc);
            Mockito.when(mockPcc.getTenantDomain(true)).thenReturn(TENANT);

            KeyManager mockKm = buildKeyManager(TOKEN, JKT);
            kmhStatic.when(() -> KeyManagerHolder.getTenantKeyManagers(TENANT))
                    .thenReturn(buildKmDtoMap(mockKm));

            String result = client.getJwkThumbprint(TOKEN);

            assertEquals(result, JKT);
        }
    }

    // ─── Cache miss, token valid but no cnf claim → null not cached ───────────

    @Test
    public void cacheMissWithNoCnfShouldReturnNullAndNotCache() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        try (MockedStatic<PrivilegedCarbonContext> pccStatic =
                     Mockito.mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<KeyManagerHolder> kmhStatic =
                     Mockito.mockStatic(KeyManagerHolder.class)) {

            PrivilegedCarbonContext mockPcc = Mockito.mock(PrivilegedCarbonContext.class);
            pccStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPcc);
            Mockito.when(mockPcc.getTenantDomain(true)).thenReturn(TENANT);

            // Token valid but has no cnf claim
            KeyManager mockKm = buildKeyManagerNoCnf(TOKEN);
            kmhStatic.when(() -> KeyManagerHolder.getTenantKeyManagers(TENANT))
                    .thenReturn(buildKmDtoMap(mockKm));

            String first = client.getJwkThumbprint(TOKEN);
            assertNull(first);

            // Second call should also call introspection (null was not cached)
            String second = client.getJwkThumbprint(TOKEN);
            assertNull(second);

            kmhStatic.verify(() -> KeyManagerHolder.getTenantKeyManagers(TENANT),
                    Mockito.times(2));
        }
    }

    // ─── APIManagementException from one KM → tries next; all fail → throws ──

    @Test
    public void allKeyManagersThrowingShouldThrowIntrospectionException() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        try (MockedStatic<PrivilegedCarbonContext> pccStatic =
                     Mockito.mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<KeyManagerHolder> kmhStatic =
                     Mockito.mockStatic(KeyManagerHolder.class)) {

            PrivilegedCarbonContext mockPcc = Mockito.mock(PrivilegedCarbonContext.class);
            pccStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPcc);
            Mockito.when(mockPcc.getTenantDomain(true)).thenReturn(TENANT);

            KeyManager throwingKm = Mockito.mock(KeyManager.class);
            Mockito.when(throwingKm.getTokenMetaData(TOKEN))
                    .thenThrow(new APIManagementException("service unavailable"));
            kmhStatic.when(() -> KeyManagerHolder.getTenantKeyManagers(TENANT))
                    .thenReturn(buildKmDtoMap(throwingKm));

            try {
                client.getJwkThumbprint(TOKEN);
                fail("Expected IntrospectionException");
            } catch (IntrospectionClient.IntrospectionException e) {
                // expected — all key managers exhausted
            }
        }
    }

    @Test
    public void noKeyManagersConfiguredShouldThrowIntrospectionException() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        try (MockedStatic<PrivilegedCarbonContext> pccStatic =
                     Mockito.mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<KeyManagerHolder> kmhStatic =
                     Mockito.mockStatic(KeyManagerHolder.class)) {

            PrivilegedCarbonContext mockPcc = Mockito.mock(PrivilegedCarbonContext.class);
            pccStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPcc);
            Mockito.when(mockPcc.getTenantDomain(true)).thenReturn(TENANT);

            kmhStatic.when(() -> KeyManagerHolder.getTenantKeyManagers(TENANT))
                    .thenReturn(Collections.emptyMap());

            try {
                client.getJwkThumbprint(TOKEN);
                fail("Expected IntrospectionException");
            } catch (IntrospectionClient.IntrospectionException e) {
                // expected
            }
        }
    }

    @Test
    public void nullAccessTokenInfoShouldContinueToNextKeyManager() throws Exception {
        IntrospectionClient client = new IntrospectionClient(CACHE_TTL);

        try (MockedStatic<PrivilegedCarbonContext> pccStatic =
                     Mockito.mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<KeyManagerHolder> kmhStatic =
                     Mockito.mockStatic(KeyManagerHolder.class)) {

            PrivilegedCarbonContext mockPcc = Mockito.mock(PrivilegedCarbonContext.class);
            pccStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPcc);
            Mockito.when(mockPcc.getTenantDomain(true)).thenReturn(TENANT);

            // First KM returns null; second returns a valid result with jkt
            KeyManager nullKm = Mockito.mock(KeyManager.class);
            Mockito.when(nullKm.getTokenMetaData(TOKEN)).thenReturn(null);

            KeyManager validKm = buildKeyManager(TOKEN, JKT);

            Map<String, KeyManagerDto> dtoMap = new HashMap<>();
            dtoMap.put("nullKm", buildDto(nullKm));
            dtoMap.put("validKm", buildDto(validKm));

            kmhStatic.when(() -> KeyManagerHolder.getTenantKeyManagers(TENANT))
                    .thenReturn(dtoMap);

            String result = client.getJwkThumbprint(TOKEN);
            assertEquals(result, JKT);
        }
    }

    private KeyManager buildKeyManager(String token, String jkt) throws APIManagementException {
        Map<String, Object> cnf = new HashMap<>();
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
