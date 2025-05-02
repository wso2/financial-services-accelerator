package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ConsentExpiryTimeUpdateDTO implements Serializable {
    private long expiryTime;

    public ConsentExpiryTimeUpdateDTO(
            @JsonProperty(required = true, value = "expiryTime") long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }
}
