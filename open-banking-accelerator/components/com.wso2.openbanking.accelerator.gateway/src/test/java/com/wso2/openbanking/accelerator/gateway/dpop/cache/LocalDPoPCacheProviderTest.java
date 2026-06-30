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

package com.wso2.openbanking.accelerator.gateway.dpop.cache;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyLong;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link LocalDPoPCacheProvider}. Uses reflection to inject mocked
 * {@link DPoPJtiCache} and {@link DPoPNonceCache} so that {@code initialize()} —
 * which would otherwise build real Carbon-backed caches — is bypassed.
 * <p>
 * {@code @PrepareForTest(LocalDPoPCacheProvider.class)} is required so PowerMock
 * can intercept the {@code new DPoPJtiCache(...)} / {@code new DPoPNonceCache(...)}
 * calls inside {@code initialize()} (PowerMock instruments the calling class).
 */
@PrepareForTest({LocalDPoPCacheProvider.class})
public class LocalDPoPCacheProviderTest extends PowerMockTestCase {

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

    // ─── initialize() — verifies the caches are constructed via PowerMock whenNew ──

    @Test
    public void initializeShouldCreateCachesWithProvidedTtl() throws Exception {
        DPoPJtiCache stubJti = Mockito.mock(DPoPJtiCache.class);
        DPoPNonceCache stubNonce = Mockito.mock(DPoPNonceCache.class);
        PowerMockito.whenNew(DPoPJtiCache.class).withAnyArguments().thenReturn(stubJti);
        PowerMockito.whenNew(DPoPNonceCache.class).withAnyArguments().thenReturn(stubNonce);

        Map<String, Object> props = new HashMap<>();
        props.put("JtiCacheTtlSeconds", "180");
        props.put("NonceTtlSeconds", "600");

        new LocalDPoPCacheProvider().initialize(props);

        PowerMockito.verifyNew(DPoPJtiCache.class).withArguments(180L);
        PowerMockito.verifyNew(DPoPNonceCache.class).withArguments(600L);
    }

    @Test
    public void initializeShouldUseDefaultsWhenPropertiesAbsent() throws Exception {
        DPoPJtiCache stubJti = Mockito.mock(DPoPJtiCache.class);
        DPoPNonceCache stubNonce = Mockito.mock(DPoPNonceCache.class);
        PowerMockito.whenNew(DPoPJtiCache.class).withAnyArguments().thenReturn(stubJti);
        PowerMockito.whenNew(DPoPNonceCache.class).withAnyArguments().thenReturn(stubNonce);

        new LocalDPoPCacheProvider().initialize(new HashMap<>());

        // verifyNew(Class).withAnyArguments() defaults to times(1).
        PowerMockito.verifyNew(DPoPJtiCache.class).withArguments(anyLong());
        PowerMockito.verifyNew(DPoPNonceCache.class).withArguments(anyLong());
    }

    @Test
    public void initializeShouldFallbackToDefaultForInvalidTtlValue() throws Exception {
        DPoPJtiCache stubJti = Mockito.mock(DPoPJtiCache.class);
        DPoPNonceCache stubNonce = Mockito.mock(DPoPNonceCache.class);
        PowerMockito.whenNew(DPoPJtiCache.class).withAnyArguments().thenReturn(stubJti);
        PowerMockito.whenNew(DPoPNonceCache.class).withAnyArguments().thenReturn(stubNonce);

        Map<String, Object> props = new HashMap<>();
        props.put("JtiCacheTtlSeconds", "not-a-number");

        new LocalDPoPCacheProvider().initialize(props);

        PowerMockito.verifyNew(DPoPJtiCache.class).withArguments(anyLong());
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
