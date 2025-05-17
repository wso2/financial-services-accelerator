package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;

@JsonTypeName("ConsentRevokeRequestBody")
public class ConsentRevokeRequestBody implements Serializable {
    private String reason;
    private String userId;

    public ConsentRevokeRequestBody() {
    }

    public ConsentRevokeRequestBody reason(String reason) {
        this.reason = reason;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    @JsonProperty("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    public ConsentRevokeRequestBody userId(String userId) {
        this.userId = userId;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    @JsonProperty("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentRevokeRequestBody consentRevokeRequestBody = (ConsentRevokeRequestBody) o;
        return Objects.equals(this.reason, consentRevokeRequestBody.reason) &&
                Objects.equals(this.userId, consentRevokeRequestBody.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason, userId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentRevokeRequestBody {\n");

        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
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

