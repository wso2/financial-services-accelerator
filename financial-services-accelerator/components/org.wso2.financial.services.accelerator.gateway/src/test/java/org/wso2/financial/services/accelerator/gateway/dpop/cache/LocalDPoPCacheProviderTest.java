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

package org.wso2.financial.services.accelerator.gateway.dpop.cache;

import org.apache.commons.lang3.StringUtils;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link LocalDPoPCacheProvider}.
 * Uses reflection to inject mocked {@link DPoPJtiCache} and {@link DPoPNonceCache}
 * so that {@code initialize()} — which requires the Carbon cache infrastructure — is
 * never called.
 */
public class LocalDPoPCacheProviderTest {

    private static final String KEY_ID = "test-proof-key-id";
    private static final String JTI = "unique-jti-value";
    private static final String JKT = "some-jwk-thumbprint";
    private static final long TTL = 120L;

    private LocalDPoPCacheProvider provider;
    private DPoPJtiCache mockJtiCache;
    private DPoPNonceCache mockNonceCache;

    @BeforeMethod
    public void setUp() throws Exception {
        provider = new LocalDPoPCacheProvider();
        mockJtiCache = Mockito.mock(DPoPJtiCache.class);
        mockNonceCache = Mockito.mock(DPoPNonceCache.class);
        injectField(provider, "jtiCache", mockJtiCache);
        injectField(provider, "nonceCache", mockNonceCache);
    }

    // ─── isJtiFirstUse ────────────────────────────────────────────────────────

    @Test
    public void isJtiFirstUseShouldReturnTrueOnFirstSeen() {
        Mockito.when(mockJtiCache.isJtiFirstUse(JTI, JKT)).thenReturn(true);

        boolean result = provider.isJtiFirstUse(JTI, JKT, TTL);

        assertTrue(result);
        Mockito.verify(mockJtiCache).isJtiFirstUse(JTI, JKT);
    }

    @Test
    public void isJtiFirstUseShouldReturnFalseOnReplay() {
        Mockito.when(mockJtiCache.isJtiFirstUse(JTI, JKT)).thenReturn(false);

        boolean result = provider.isJtiFirstUse(JTI, JKT, TTL);

        assertFalse(result);
        Mockito.verify(mockJtiCache).isJtiFirstUse(JTI, JKT);
    }

    @Test
    public void issueNonceShouldReturnNonBlankValue() {
        String nonce = provider.issueNonce(KEY_ID);

        assertNotNull(nonce);
        assertFalse(StringUtils.isBlank(nonce));
    }

    @Test
    public void issueNonceShouldCallNonceCacheStoreNonce() {
        String nonce = provider.issueNonce(KEY_ID);

        Mockito.verify(mockNonceCache).storeNonce(Mockito.eq(KEY_ID), Mockito.eq(nonce));
    }

    @Test
    public void consecutiveIssueNonceCallsShouldReturnDifferentValues() {
        String first = provider.issueNonce(KEY_ID);
        String second = provider.issueNonce(KEY_ID);

        assertNotEquals(first, second,
                "Two consecutive nonces for the same key should differ (SecureRandom-backed)");
    }

    @Test
    public void getActiveNonceShouldDelegateToNonceCache() {
        String expected = "active-nonce-value";
        Mockito.when(mockNonceCache.getActiveNonce(KEY_ID)).thenReturn(expected);

        String result = provider.getActiveNonce(KEY_ID);

        Mockito.verify(mockNonceCache).getActiveNonce(KEY_ID);
        org.testng.Assert.assertEquals(result, expected);
    }

    @Test
    public void getActiveNonceShouldReturnNullWhenNoneIssued() {
        Mockito.when(mockNonceCache.getActiveNonce(KEY_ID)).thenReturn(null);

        String result = provider.getActiveNonce(KEY_ID);

        assertNull(result);
    }

    @Test
    public void initializeShouldCreateCachesWithProvidedTtl() {
        try (MockedConstruction<DPoPJtiCache> jtiCons = Mockito.mockConstruction(DPoPJtiCache.class);
             MockedConstruction<DPoPNonceCache> nonceCons = Mockito.mockConstruction(DPoPNonceCache.class)) {

            Map<String, Object> props = new HashMap<>();
            props.put("JtiCacheTtlSeconds", "180");
            props.put("NonceTtlSeconds", "600");

            new LocalDPoPCacheProvider().initialize(props);

            org.testng.Assert.assertEquals(jtiCons.constructed().size(), 1);
            org.testng.Assert.assertEquals(nonceCons.constructed().size(), 1);
        }
    }

    @Test
    public void initializeShouldUseDefaultsWhenPropertiesAbsent() {
        try (MockedConstruction<DPoPJtiCache> jtiCons = Mockito.mockConstruction(DPoPJtiCache.class);
             MockedConstruction<DPoPNonceCache> nonceCons = Mockito.mockConstruction(DPoPNonceCache.class)) {

            new LocalDPoPCacheProvider().initialize(new HashMap<>());

            org.testng.Assert.assertEquals(jtiCons.constructed().size(), 1);
            org.testng.Assert.assertEquals(nonceCons.constructed().size(), 1);
        }
    }

    @Test
    public void initializeShouldFallbackToDefaultForInvalidTtlValue() {
        try (MockedConstruction<DPoPJtiCache> jtiCons = Mockito.mockConstruction(DPoPJtiCache.class);
             MockedConstruction<DPoPNonceCache> ignored = Mockito.mockConstruction(DPoPNonceCache.class)) {

            Map<String, Object> props = new HashMap<>();
            props.put("JtiCacheTtlSeconds", "not-a-number");

            new LocalDPoPCacheProvider().initialize(props);

            org.testng.Assert.assertEquals(jtiCons.constructed().size(), 1);
        }
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
