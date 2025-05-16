package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotEmpty;

import javax.validation.constraints.Size;


@JsonTypeName("ConsentStatusUpdateRequestBody")

public class ConsentStatusUpdateRequestBody implements Serializable {
    private String status;
    private String reason;
    private String userId;

    public ConsentStatusUpdateRequestBody() {
    }

    @JsonCreator
    public ConsentStatusUpdateRequestBody(
            @JsonProperty(required = true, value = "status") String status,
            @JsonProperty(required = true, value = "reason") String reason,
            @JsonProperty(required = true, value = "userId") String userId
                                         ) {
        this.status = status;
        this.reason = reason;
        this.userId = userId;
    }

    /**
     *
     **/
    public ConsentStatusUpdateRequestBody status(String status) {
        this.status = status;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "status")
    @NotNull @NotEmpty public String getStatus() {
        return status;
    }

    @JsonProperty(required = true, value = "status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     **/
    public ConsentStatusUpdateRequestBody reason(String reason) {
        this.reason = reason;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "reason")
    @NotNull @NotEmpty public String getReason() {
        return reason;
    }

    @JsonProperty(required = true, value = "reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     *
     **/
    public ConsentStatusUpdateRequestBody userId(String userId) {
        this.userId = userId;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "userId")
    @NotNull @NotEmpty public String getUserId() {
        return userId;
    }

    @JsonProperty(required = true, value = "userId")
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
        ConsentStatusUpdateRequestBody consentStatusUpdateRequestBody = (ConsentStatusUpdateRequestBody) o;
        return Objects.equals(this.status, consentStatusUpdateRequestBody.status) &&
                Objects.equals(this.reason, consentStatusUpdateRequestBody.reason) &&
                Objects.equals(this.userId, consentStatusUpdateRequestBody.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, reason, userId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentStatusUpdateRequestBody {\n");

        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

