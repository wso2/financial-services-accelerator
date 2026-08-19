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

package org.wso2.financial.services.accelerator.common.logging;

import java.util.Objects;

/**
 * Factory for {@link Log} instances. Drop-in replacement for
 * {@code org.apache.commons.logging.LogFactory} — change only the import.
 *
 * <pre>
 *     private static final Log log = LogFactory.getLog(MyClass.class);
 * </pre>
 */
public final class LogFactory {

    private LogFactory() {
    }

    /**
     * Returns a {@link Log} for {@code clazz}.
     *
     * @param clazz the class whose name is used as the logger name; must not be {@code null}
     */
    public static Log getLog(Class<?> clazz) {
        return new Log(org.apache.commons.logging.LogFactory.getLog(
                Objects.requireNonNull(clazz, "clazz")));
    }
}
