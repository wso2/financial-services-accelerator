package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

/**
 * Display data object for authorization UI consent retrieval.
 * Contains a list of display data sections.
 */
public class DisplayDataDTO {

    @Valid
    private List<DisplayDataInnerItemDTO> items = new ArrayList<>();

    public DisplayDataDTO() {
    }

    public List<DisplayDataInnerItemDTO> getItems() {
        return items;
    }

    public void setItems(List<DisplayDataInnerItemDTO> items) {
        this.items = items;
    }

    public DisplayDataDTO addItem(DisplayDataInnerItemDTO item) {
        this.items.add(item);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DisplayDataDTO)) {
            return false;
        }
        DisplayDataDTO that = (DisplayDataDTO) o;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}
