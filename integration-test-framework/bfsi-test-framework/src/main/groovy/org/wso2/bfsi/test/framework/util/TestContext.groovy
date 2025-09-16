/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.test.framework.util

/**
 *  Optional Context Management class
 *  Use for manage context details through the test class
 */
class TestContext {

    private static TestContext context
    // To enable attempted thread-safety using double-check locking
    private static final Object lock = new Object()

    private Map<String, Object> contextMap;

    /**
     * Private Constructor of config parser.
     */
    private TestContext() {
        contextMap = new HashMap<>()
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return ConfigParser object
     */
    static TestContext getInstance() {
        if (context == null) {
            synchronized (lock) {
                if (context == null) {
                    return new TestContext()
                }
            }
        }
        return context
    }

    void addContext(String key, Object object) {
        contextMap.put(key, object)
    }

    Object getContext(String key) {
        return contextMap.get(key)
    }

    Map getContextMap() {
        return contextMap
    }
}

