package org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

@JsonTypeName("ConsentExpiryTimeUpdateRequestBody")
public class ConsentExpiryTimeUpdateRequestBody implements Serializable {
    private Long expiryTime;

    public ConsentExpiryTimeUpdateRequestBody() {
    }

    @JsonCreator
    public ConsentExpiryTimeUpdateRequestBody(
            @JsonProperty(required = true, value = "expiryTime") Long expiryTime
                                             ) {
        this.expiryTime = expiryTime;
    }

    /**
     *
     **/
    public ConsentExpiryTimeUpdateRequestBody expiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "expiryTime")
    @NotNull public Long getExpiryTime() {
        return expiryTime;
    }

    @JsonProperty(required = true, value = "expiryTime")
    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentExpiryTimeUpdateRequestBody consentExpiryTimeUpdateRequestBody = (ConsentExpiryTimeUpdateRequestBody) o;
        return Objects.equals(this.expiryTime, consentExpiryTimeUpdateRequestBody.expiryTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiryTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentExpiryTimeUpdateRequestBody {\n");

        sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

