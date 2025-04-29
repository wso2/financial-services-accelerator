package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import javax.validation.Valid;


@JsonTypeName("BulkConsentStatusUpdateResource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class BulkConsentStatusUpdateResource implements Serializable {
    private String clientID;
    private String consentType;
    private @Valid ArrayList<String> applicableStatusesForStateChange = new ArrayList<>();
    private String status;
    private String reason;
    private String userID;

    public BulkConsentStatusUpdateResource() {
    }

    /**
     *
     **/
    public BulkConsentStatusUpdateResource clientID(String clientID) {
        this.clientID = clientID;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("clientID")
    public String getClientID() {
        return clientID;
    }

    @JsonProperty("clientID")
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    /**
     *
     **/
    public BulkConsentStatusUpdateResource consentType(String consentType) {
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
    public BulkConsentStatusUpdateResource applicableStatusesForStateChange(
            ArrayList<String> applicableStatusesForStateChange) {
        this.applicableStatusesForStateChange = applicableStatusesForStateChange;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("applicableStatusesForStateChange")
    public ArrayList<String> getApplicableStatusesForStateChange() {
        return applicableStatusesForStateChange;
    }

    @JsonProperty("applicableStatusesForStateChange")
    public void setApplicableStatusesForStateChange(ArrayList<String> applicableStatusesForStateChange) {
        this.applicableStatusesForStateChange = applicableStatusesForStateChange;
    }

    public BulkConsentStatusUpdateResource addApplicableStatusesForStateChangeItem(
            String applicableStatusesForStateChangeItem) {
        if (this.applicableStatusesForStateChange == null) {
            this.applicableStatusesForStateChange = new ArrayList<>();
        }

        this.applicableStatusesForStateChange.add(applicableStatusesForStateChangeItem);
        return this;
    }

    public BulkConsentStatusUpdateResource removeApplicableStatusesForStateChangeItem(
            String applicableStatusesForStateChangeItem) {
        if (applicableStatusesForStateChangeItem != null && this.applicableStatusesForStateChange != null) {
            this.applicableStatusesForStateChange.remove(applicableStatusesForStateChangeItem);
        }

        return this;
    }

    /**
     *
     **/
    public BulkConsentStatusUpdateResource status(String status) {
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
    public BulkConsentStatusUpdateResource reason(String reason) {
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
    public BulkConsentStatusUpdateResource userID(String userID) {
        this.userID = userID;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("userID")
    public String getUserID() {
        return userID;
    }

    @JsonProperty("userID")
    public void setUserID(String userID) {
        this.userID = userID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BulkConsentStatusUpdateResource bulkConsentStatusUpdateResource = (BulkConsentStatusUpdateResource) o;
        return Objects.equals(this.clientID, bulkConsentStatusUpdateResource.clientID) &&
                Objects.equals(this.consentType, bulkConsentStatusUpdateResource.consentType) &&
                Objects.equals(this.applicableStatusesForStateChange,
                        bulkConsentStatusUpdateResource.applicableStatusesForStateChange) &&
                Objects.equals(this.status, bulkConsentStatusUpdateResource.status) &&
                Objects.equals(this.reason, bulkConsentStatusUpdateResource.reason) &&
                Objects.equals(this.userID, bulkConsentStatusUpdateResource.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientID, consentType, applicableStatusesForStateChange, status, reason, userID);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BulkConsentStatusUpdateResource {\n");

        sb.append("    clientID: ").append(toIndentedString(clientID)).append("\n");
        sb.append("    consentType: ").append(toIndentedString(consentType)).append("\n");
        sb.append("    applicableStatusesForStateChange: ").append(toIndentedString(applicableStatusesForStateChange))
                .append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
        sb.append("    userID: ").append(toIndentedString(userID)).append("\n");
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

