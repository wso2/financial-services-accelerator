package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.Valid;


@JsonTypeName("ConsentHistory")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentHistory implements Serializable {
    private String historyId;
    private String consentID;
    private Integer timestamp;
    private String reason;
    private DetailedConsentResource detailedConsent;
    private Object changedMetadata;

    public ConsentHistory() {
    }

    /**
     *
     **/
    public ConsentHistory historyId(String historyId) {
        this.historyId = historyId;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("historyId")
    public String getHistoryId() {
        return historyId;
    }

    @JsonProperty("historyId")
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    /**
     *
     **/
    public ConsentHistory consentID(String consentID) {
        this.consentID = consentID;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("consentID")
    public String getConsentID() {
        return consentID;
    }

    @JsonProperty("consentID")
    public void setConsentID(String consentID) {
        this.consentID = consentID;
    }

    /**
     *
     **/
    public ConsentHistory timestamp(Integer timestamp) {
        this.timestamp = timestamp;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("timestamp")
    public Integer getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    /**
     *
     **/
    public ConsentHistory reason(String reason) {
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
    public ConsentHistory detailedConsent(DetailedConsentResource detailedConsent) {
        this.detailedConsent = detailedConsent;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("detailedConsent")
    @Valid public DetailedConsentResource getDetailedConsent() {
        return detailedConsent;
    }

    @JsonProperty("detailedConsent")
    public void setDetailedConsent(DetailedConsentResource detailedConsent) {
        this.detailedConsent = detailedConsent;
    }

    /**
     *
     **/
    public ConsentHistory changedMetadata(Object changedMetadata) {
        this.changedMetadata = changedMetadata;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("changedMetadata")
    public Object getChangedMetadata() {
        return changedMetadata;
    }

    @JsonProperty("changedMetadata")
    public void setChangedMetadata(Object changedMetadata) {
        this.changedMetadata = changedMetadata;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentHistory consentHistory = (ConsentHistory) o;
        return Objects.equals(this.historyId, consentHistory.historyId) &&
                Objects.equals(this.consentID, consentHistory.consentID) &&
                Objects.equals(this.timestamp, consentHistory.timestamp) &&
                Objects.equals(this.reason, consentHistory.reason) &&
                Objects.equals(this.detailedConsent, consentHistory.detailedConsent) &&
                Objects.equals(this.changedMetadata, consentHistory.changedMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId, consentID, timestamp, reason, detailedConsent, changedMetadata);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentHistory {\n");

        sb.append("    historyId: ").append(toIndentedString(historyId)).append("\n");
        sb.append("    consentID: ").append(toIndentedString(consentID)).append("\n");
        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
        sb.append("    detailedConsent: ").append(toIndentedString(detailedConsent)).append("\n");
        sb.append("    changedMetadata: ").append(toIndentedString(changedMetadata)).append("\n");
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

