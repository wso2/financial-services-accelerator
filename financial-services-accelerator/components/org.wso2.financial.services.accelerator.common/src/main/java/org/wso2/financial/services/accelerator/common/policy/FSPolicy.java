/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.financial.services.accelerator.common.policy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for Financial Services Policy.
 */
public abstract class FSPolicy {

    private Map<String, Object> propertyMap;

    public Map<String, Object> getPropertyMap() {
        return propertyMap != null ? Collections.unmodifiableMap(propertyMap) : Collections.emptyMap();
    }

    public void setPropertyMap(Map<String, Object> propertyMap) {
        this.propertyMap = propertyMap != null ? new HashMap<>(propertyMap) : new HashMap<>();
    }
}
