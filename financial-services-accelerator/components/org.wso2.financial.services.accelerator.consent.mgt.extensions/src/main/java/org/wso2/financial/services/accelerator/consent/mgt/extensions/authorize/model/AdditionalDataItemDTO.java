/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model;

import java.util.Objects;

/**
 * Represents a single item entry within AdditionalDataDTO.
 * Contains a display value and an optional type classifier.
 */
public class AdditionalDataItemDTO {

    private String item;
    private String type;

    public AdditionalDataItemDTO() {
    }

    public AdditionalDataItemDTO(String item, String type) {
        this.item = item;
        this.type = type;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdditionalDataItemDTO)) {
            return false;
        }
        AdditionalDataItemDTO that = (AdditionalDataItemDTO) o;
        return Objects.equals(item, that.item) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, type);
    }
}
