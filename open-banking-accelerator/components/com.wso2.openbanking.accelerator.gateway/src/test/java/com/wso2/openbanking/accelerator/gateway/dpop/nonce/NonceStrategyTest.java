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

package com.wso2.openbanking.accelerator.gateway.dpop.nonce;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the three built-in {@link NonceStrategy} implementations.
 */
public class NonceStrategyTest {

    @Test
    public void neverStrategyShouldNeverRequireNonce() {
        NeverNonceStrategy strategy = new NeverNonceStrategy();
        assertFalse(strategy.requiresNonce("any-client"));
        assertFalse(strategy.requiresNonce("any-client"));
        assertEquals(strategy.getName(), "never");
    }

    @Test
    public void neverStrategyShouldNeverRotate() {
        NeverNonceStrategy strategy = new NeverNonceStrategy();
        assertFalse(strategy.shouldRotate("any-client"));
        assertFalse(strategy.shouldRotate("any-client"));
    }

    @Test
    public void alwaysStrategyShouldAlwaysRequireNonce() {
        AlwaysNonceStrategy strategy = new AlwaysNonceStrategy();
        assertTrue(strategy.requiresNonce("client-1"));
        assertTrue(strategy.requiresNonce("client-2"));
        assertEquals(strategy.getName(), "always");
    }

    @Test
    public void alwaysStrategyShouldNeverRotate() {
        // AlwaysNonceStrategy reuses nonce until TTL expiry — no proactive rotation needed.
        AlwaysNonceStrategy strategy = new AlwaysNonceStrategy();
        assertFalse(strategy.shouldRotate("client-1"));
        assertFalse(strategy.shouldRotate("client-1"));
    }

    @Test
    public void rotatingStrategyShouldAlwaysRequireNonce() {
        // requiresNonce is pure/side-effect-free — always true regardless of call count.
        RotatingNonceStrategy strategy = new RotatingNonceStrategy(3);
        String client = "client-x";
        assertTrue(strategy.requiresNonce(client));
        assertTrue(strategy.requiresNonce(client));
        assertTrue(strategy.requiresNonce(client));
        assertTrue(strategy.requiresNonce(client));
        assertEquals(strategy.getName(), "rotating");
    }

    @Test
    public void rotatingStrategyShouldRotateAtModuloIntervals() {
        RotatingNonceStrategy strategy = new RotatingNonceStrategy(3);
        String client = "client-y";

        assertFalse(strategy.shouldRotate(client)); // count=1
        assertFalse(strategy.shouldRotate(client)); // count=2
        assertTrue(strategy.shouldRotate(client));  // count=3 → 3%3==0
        assertFalse(strategy.shouldRotate(client)); // count=4
        assertFalse(strategy.shouldRotate(client)); // count=5
        assertTrue(strategy.shouldRotate(client));  // count=6 → 6%3==0
    }

    @Test
    public void rotatingStrategyShouldFloorRotateAfterUsesAtOne() {
        // A misconfiguration of 0 or negative must not cause a div-by-zero in the strategy.
        RotatingNonceStrategy strategy = new RotatingNonceStrategy(0);
        // rotateAfterUses is floored to 1 — every validated proof triggers rotation
        assertTrue(strategy.shouldRotate("c")); // count=1 → 1%1==0
        assertTrue(strategy.shouldRotate("c")); // count=2 → 2%1==0
        assertTrue(strategy.shouldRotate("c")); // count=3 → 3%1==0
    }
}
