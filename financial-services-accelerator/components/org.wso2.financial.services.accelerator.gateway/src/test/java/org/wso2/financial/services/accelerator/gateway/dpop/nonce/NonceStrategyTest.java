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

package org.wso2.financial.services.accelerator.gateway.dpop.nonce;

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
    public void alwaysStrategyShouldAlwaysRequireNonce() {
        AlwaysNonceStrategy strategy = new AlwaysNonceStrategy();
        assertTrue(strategy.requiresNonce("client-1"));
        assertTrue(strategy.requiresNonce("client-2"));
        assertEquals(strategy.getName(), "always");
    }

    @Test
    public void rotatingStrategyShouldRequireNonceOnFirstAndEveryN() {
        RotatingNonceStrategy strategy = new RotatingNonceStrategy(3);
        String client = "client-x";

        assertTrue(strategy.requiresNonce(client));   // count=1 (first use)
        assertFalse(strategy.requiresNonce(client));  // count=2
        assertTrue(strategy.requiresNonce(client));   // count=3 (3%3==0)
        assertFalse(strategy.requiresNonce(client));  // count=4
    }

    @Test
    public void rotatingStrategyShouldRequireNonceAtModuloIntervals() {
        RotatingNonceStrategy strategy = new RotatingNonceStrategy(3);
        String client = "client-y";

        assertTrue(strategy.requiresNonce(client));
        assertFalse(strategy.requiresNonce(client));
        assertTrue(strategy.requiresNonce(client));   // count=3
        assertFalse(strategy.requiresNonce(client));
        assertFalse(strategy.requiresNonce(client));
        assertTrue(strategy.requiresNonce(client));   // count=6
        assertEquals(strategy.getName(), "rotating");
    }

    @Test
    public void rotatingStrategyShouldFloorRotateAfterUsesAtOne() {
        // A misconfiguration of 0 or negative must not cause a div-by-zero in the strategy.
        RotatingNonceStrategy strategy = new RotatingNonceStrategy(0);
        assertTrue(strategy.requiresNonce("c"));
        assertTrue(strategy.requiresNonce("c"));
        assertTrue(strategy.requiresNonce("c"));
    }
}
