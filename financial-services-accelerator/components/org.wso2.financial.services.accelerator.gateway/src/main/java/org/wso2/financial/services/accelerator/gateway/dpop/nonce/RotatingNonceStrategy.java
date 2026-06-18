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
 * Rotates nonces after a configurable number of uses per client identity. Requires
 * a nonce on the first request from a client, then every {@code rotateAfterUses}
 * subsequent successful proof validations.
 */
public class RotatingNonceStrategy implements NonceStrategy {

    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final int rotateAfterUses;
    // Access-ordered LRU map — evicts the least-recently-seen client identity once the
    // cap is reached, preventing unbounded growth for long-running gateway instances.
    private final Map<String, AtomicLong> useCounts = Collections.synchronizedMap(
            new LinkedHashMap<String, AtomicLong>(MAX_TRACKED_CLIENTS, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, AtomicLong> eldest) {
                    return size() > MAX_TRACKED_CLIENTS;
                }
            });

    public RotatingNonceStrategy(int rotateAfterUses) {

        this.rotateAfterUses = Math.max(1, rotateAfterUses);
    }

    /**
     * Returns {@code true} on the first call for a given identity and on every
     * {@code rotateAfterUses}-th subsequent call. Increments the use counter on each
     * invocation — must not be called more than once per request.
     */
    @Override
    public boolean requiresNonce(String clientIdentity) {

        AtomicLong counter;
        synchronized (useCounts) {
            counter = useCounts.computeIfAbsent(clientIdentity, k -> new AtomicLong(0));
        }
        long count = counter.incrementAndGet();
        return count == 1 || (count % rotateAfterUses == 0);
    }

    @Override
    public String getName() {

        return "rotating";
    }
}
