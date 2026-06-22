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

/**
 * Strategy interface deciding whether a DPoP-Nonce is required for a given client.
 * Client identity is normally the JWK thumbprint of the proof's public key.
 */
public interface NonceStrategy {

    /**
     * Returns {@code true} if the handler must demand a nonce from this client.
     */
    boolean requiresNonce(String clientIdentity);

    /**
     * Returns the strategy name used in {@code financial-services.xml}.
     */
    String getName();
}
