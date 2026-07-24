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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rotates the nonce every {@code rotateAfterUses} successful proof validations per client.
 * The nonce is reused between rotations; a fresh nonce is delivered proactively in the
 * {@code 200} response (RFC 9449 §8.2) so the client never pays an extra round-trip.
 */
public class RotatingNonceStrategy implements NonceStrategy {

    private final int rotateAfterUses;
    private final int maxTrackedClients;
    // Access-ordered LRU map — evicts the least-recently-seen client identity once the cap is reached.
    private final Map<String, AtomicLong> useCounts;

    public RotatingNonceStrategy(int rotateAfterUses, int maxTrackedClients) {

        this.rotateAfterUses = Math.max(1, rotateAfterUses);
        this.maxTrackedClients = Math.max(1, maxTrackedClients);
        this.useCounts = Collections.synchronizedMap(
                new LinkedHashMap<String, AtomicLong>(this.maxTrackedClients, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, AtomicLong> eldest) {
                        return size() > RotatingNonceStrategy.this.maxTrackedClients;
                    }
                });
    }

    /**
     * Pure policy check — always returns {@code true} because once a nonce is issued to a
     * client it must be required on every subsequent request (RFC 9449 §11.3). First-issue
     * is handled by the {@code activeNonce == null} path in the handler, not here.
     */
    @Override
    public boolean requiresNonce(String clientIdentity) {

        return true;
    }

    /**
     * Increments the per-client use counter and returns {@code true} every
     * {@code rotateAfterUses}-th call, triggering proactive nonce rotation.
     */
    @Override
    public boolean shouldRotate(String clientIdentity) {

        AtomicLong counter;
        synchronized (useCounts) {
            counter = useCounts.computeIfAbsent(clientIdentity, k -> new AtomicLong(0));
        }
        long count = counter.incrementAndGet();
        return count % rotateAfterUses == 0;
    }

    @Override
    public String getName() {

        return "rotating";
    }
}
