package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Display data object for authorization UI consent retrieval.
 * Stores dynamic UI display items such as unavailable / blocked accounts.
 */
public class DisplayDataDTO {

    /**
     * List of display data items.
     *
     * Example JSON:
     * [
     *   { "accountId": "123", "displayName": "Blocked Account" }
     * ]
     */
    @Valid
    private List<Map<String, Object>> displayData = new ArrayList<>();

    public DisplayDataDTO() {
    }

    public List<Map<String, Object>> getDisplayData() {
        return displayData;
    }

    public void setDisplayData(List<Map<String, Object>> displayData) {
        this.displayData = displayData;
    }

    /**
     * Convenience method to add a display item.
     *
     * @param item display data item
     * @return this instance for chaining
     */
    public DisplayDataDTO addItem(Map<String, Object> item) {
        this.displayData.add(item);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisplayDataDTO that = (DisplayDataDTO) o;
        return Objects.equals(displayData, that.displayData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayData);
    }
}
