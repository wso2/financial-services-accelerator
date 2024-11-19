/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.scp.webapp.exception;

/**
 * TokenGenerationException
 *
 * Throws if errors occurred during the request forwarding process
 */
public class TokenGenerationException extends Exception {
    private static final long serialVersionUID = -6044462346016688554L;

    public TokenGenerationException(String s) {
        super(s);
    }

    public TokenGenerationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
