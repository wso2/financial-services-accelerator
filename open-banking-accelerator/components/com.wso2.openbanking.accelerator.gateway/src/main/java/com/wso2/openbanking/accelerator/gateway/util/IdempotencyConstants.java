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

package com.wso2.openbanking.accelerator.gateway.util;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;

/**
 * IdempotencyConstants.
 */
public class IdempotencyConstants {

    // config parser keys
    public static final String IDEMPOTENCY_ALLOWED_TIME = "Gateway.Idempotency.AllowedTimeDuration";
    public static final String IDEMPOTENCY_CACHE_TIME_TO_LIVE =
            "Gateway.Cache.IdempotencyValidationCache.CacheTimeToLive";
    public static final String IDEMPOTENCY_KEY_HEADER = "Gateway.Idempotency.IdempotencyKeyHeader";
    public static final String IDEMPOTENCY_IS_ENABLED = "Gateway.Idempotency.IsEnabled";

    public static final String HTTP_STATUS = "httpStatus";
    public static final String PAYLOAD = "payload";


    /**
     * Error.
     */
    public static class Error {

        public static final String DATE_MISSING = "Date header is missing in the request";
        public static final String EXECUTOR_IDEMPOTENCY_KEY_ERROR =
                "Error while handling Idempotency check.:Header." + getPathIdemKey();;
        public static final String EXECUTOR_IDEMPOTENCY_KEY_FRAUDULENT =
                "Idempotency check failed.:Header." + getPathIdemKey();
        public static final String HEADER_INVALID = "Header Invalid";
        public static final String IDEMPOTENCY_HANDLE_ERROR =
                "Error occurred while handling the idempotency available request";

        private static String getPathIdemKey() {

            return (String) OpenBankingConfigParser.getInstance().getConfiguration()
                    .get(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER);
        }
    }

}
