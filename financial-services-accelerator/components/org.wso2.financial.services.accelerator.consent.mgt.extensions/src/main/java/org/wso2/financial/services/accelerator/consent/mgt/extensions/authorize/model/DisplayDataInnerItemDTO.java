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
public class DisplayDataInnerItemDTO {

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

    public DisplayDataInnerItemDTO() {
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
    public DisplayDataInnerItemDTO addItem(Map<String, Object> item) {
        this.displayList.add(item);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DisplayDataInnerItemDTO)) {
            return false;
        }
        DisplayDataInnerItemDTO that = (DisplayDataInnerItemDTO) o;
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
