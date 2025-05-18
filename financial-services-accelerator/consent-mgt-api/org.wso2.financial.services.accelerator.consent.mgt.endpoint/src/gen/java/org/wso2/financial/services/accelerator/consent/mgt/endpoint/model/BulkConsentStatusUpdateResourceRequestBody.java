package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@JsonTypeName("BulkConsentStatusUpdateResourceRequestBody")
public class BulkConsentStatusUpdateResourceRequestBody implements Serializable {
    private String clientId;
    private String consentType;
    private @Valid List<String> applicableStatusesForStateChange = new ArrayList<>();
    private String status;
    private String reason;
    private String userId;

    public BulkConsentStatusUpdateResourceRequestBody() {
    }

    /**
     *
     **/
    public BulkConsentStatusUpdateResourceRequestBody clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @JsonProperty("clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     *
     **/
    public BulkConsentStatusUpdateResourceRequestBody consentType(String consentType) {
        this.consentType = consentType;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("consentType")
    public String getConsentType() {
        return consentType;
    }

    @JsonProperty("consentType")
    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    /**
     *
     **/
    public BulkConsentStatusUpdateResourceRequestBody applicableStatusesForStateChange(
            List<String> applicableStatusesForStateChange) {
        this.applicableStatusesForStateChange = applicableStatusesForStateChange;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("applicableStatusesForStateChange")
    public List<String> getApplicableStatusesForStateChange() {
        return applicableStatusesForStateChange;
    }

    @JsonProperty("applicableStatusesForStateChange")
    public void setApplicableStatusesForStateChange(List<String> applicableStatusesForStateChange) {
        this.applicableStatusesForStateChange = applicableStatusesForStateChange;
    }

    public BulkConsentStatusUpdateResourceRequestBody addApplicableStatusesForStateChangeItem(
            String applicableStatusesForStateChangeItem) {
        if (this.applicableStatusesForStateChange == null) {
            this.applicableStatusesForStateChange = new ArrayList<>();
        }

        this.applicableStatusesForStateChange.add(applicableStatusesForStateChangeItem);
        return this;
    }

    public BulkConsentStatusUpdateResourceRequestBody removeApplicableStatusesForStateChangeItem(
            String applicableStatusesForStateChangeItem) {
        if (applicableStatusesForStateChangeItem != null && this.applicableStatusesForStateChange != null) {
            this.applicableStatusesForStateChange.remove(applicableStatusesForStateChangeItem);
        }

        return this;
    }

    /**
     *
     **/
    public BulkConsentStatusUpdateResourceRequestBody status(String status) {
        this.status = status;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     **/
    public BulkConsentStatusUpdateResourceRequestBody reason(String reason) {
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

    /**
     *
     **/
    public BulkConsentStatusUpdateResourceRequestBody userId(String userId) {
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
        BulkConsentStatusUpdateResourceRequestBody bulkConsentStatusUpdateResourceRequestBody =
                (BulkConsentStatusUpdateResourceRequestBody) o;
        return Objects.equals(this.clientId, bulkConsentStatusUpdateResourceRequestBody.clientId) &&
                Objects.equals(this.consentType, bulkConsentStatusUpdateResourceRequestBody.consentType) &&
                Objects.equals(this.applicableStatusesForStateChange,
                        bulkConsentStatusUpdateResourceRequestBody.applicableStatusesForStateChange) &&
                Objects.equals(this.status, bulkConsentStatusUpdateResourceRequestBody.status) &&
                Objects.equals(this.reason, bulkConsentStatusUpdateResourceRequestBody.reason) &&
                Objects.equals(this.userId, bulkConsentStatusUpdateResourceRequestBody.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, consentType, applicableStatusesForStateChange, status, reason, userId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BulkConsentStatusUpdateResourceRequestBody {\n");

        sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
        sb.append("    consentType: ").append(toIndentedString(consentType)).append("\n");
        sb.append("    applicableStatusesForStateChange: ").append(toIndentedString(applicableStatusesForStateChange))
                .append("\n");
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

