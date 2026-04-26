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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

/**
 * Display data object for authorization UI consent retrieval.
 * Stores UI metadata and dynamic display items such as unavailable accounts.
 */
public class AdditionalDataDTO {

    /**
     * UI Title.
     */
    private String title;

    /**
     * UI Subtitle.
     */
    private String subtitle;

    /**
     * Tooltip/help description.
     */
    private String description;

    /**
     * Dynamic display items.
     */
    @Valid
    private List<AdditionalDataItemDTO> items = new ArrayList<>();

    public AdditionalDataDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AdditionalDataItemDTO> getItems() {
        return items;
    }

    public void setItems(List<AdditionalDataItemDTO> items) {
        this.items = items;
    }

    public AdditionalDataDTO addItem(AdditionalDataItemDTO item) {
        this.items.add(item);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdditionalDataDTO)) {
            return false;
        }
        AdditionalDataDTO that = (AdditionalDataDTO) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(subtitle, that.subtitle) &&
                Objects.equals(description, that.description) &&
                Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, subtitle, description, items);
    }
}
