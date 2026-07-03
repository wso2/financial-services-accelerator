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

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Registry of built-in {@link NonceStrategy} implementations. Each constant owns its
 * factory so that adding a new strategy requires touching only this file.
 */
public enum NonceStrategyType {

    NEVER("never", (uses, cap) -> new NeverNonceStrategy()),
    ALWAYS("always", (uses, cap) -> new AlwaysNonceStrategy()),
    ROTATING("rotating", RotatingNonceStrategy::new);

    private final String configKey;
    private final BiFunction<Integer, Integer, NonceStrategy> factory;

    NonceStrategyType(String configKey, BiFunction<Integer, Integer, NonceStrategy> factory) {

        this.configKey = configKey;
        this.factory = factory;
    }

    /**
     * Instantiates the strategy with the supplied rotation parameters.
     * Strategies that do not use these parameters simply ignore them.
     */
    public NonceStrategy create(int rotateAfterUses, int maxTrackedClients) {

        return factory.apply(rotateAfterUses, maxTrackedClients);
    }

    /**
     * Looks up a {@link NonceStrategyType} by its config-file key (case-insensitive).
     * Returns {@link Optional#empty()} for unrecognised names — the caller decides
     * whether to warn and fall back or to fail fast.
     */
    public static Optional<NonceStrategyType> fromName(String name) {

        return Arrays.stream(values())
                .filter(t -> t.configKey.equalsIgnoreCase(name))
                .findFirst();
    }
}
