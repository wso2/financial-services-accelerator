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
import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;

/**
 * Display data object for authorization UI consent retrieval.
 * Stores UI metadata and dynamic display items such as unavailable accounts.
 */
public class AdditionalDisplayDataDTO {

    /**
     * UI Heading.
     */
    private String heading;

    /**
     * UI Sub heading.
     */
    private String subHeading;

    /**
     * Tooltip/help description.
     */
    private String description;

    /**
     * Dynamic display items.
     */
    @Valid
    private List<Map<String, Object>> displayList = new ArrayList<>();

    public AdditionalDisplayDataDTO() {
    }

    // Heading
    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    // SubHeading
    public String getSubHeading() {
        return subHeading;
    }

    public void setSubHeading(String subHeading) {
        this.subHeading = subHeading;
    }

    // Tooltip Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Display Data
    public List<Map<String, Object>> getDisplayList() {
        return displayList;
    }

    public void setDisplayList(List<Map<String, Object>> displayList) {
        this.displayList = displayList;
    }

    /**
     * Convenience method to add a display item.
     */
    public AdditionalDisplayDataDTO addItem(Map<String, Object> item) {
        this.displayList.add(item);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdditionalDisplayDataDTO)) {
            return false;
        }
        AdditionalDisplayDataDTO that = (AdditionalDisplayDataDTO) o;
        return Objects.equals(heading, that.heading) &&
                Objects.equals(subHeading, that.subHeading) &&
                Objects.equals(description, that.description) &&
                Objects.equals(displayList, that.displayList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heading, subHeading, description, displayList);
    }
}
