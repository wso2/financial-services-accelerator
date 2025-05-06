package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;


@JsonTypeName("ConsentHistory")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentHistoryResponse implements Serializable {
    private String historyId;
    private String consentID;
    private Integer timestamp;
    private String reason;
    private ConsentResourceResponseBody consentResource;

    public ConsentHistoryResponse() {
    }

    /**
     *
     **/
    public ConsentHistoryResponse historyId(String historyId) {
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
    public ConsentHistoryResponse consentID(String consentID) {
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
    public ConsentHistoryResponse timestamp(Integer timestamp) {
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
    public ConsentHistoryResponse reason(String reason) {
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
    public ConsentHistoryResponse consentResource(ConsentResourceResponseBody consentResource) {
        this.consentResource = consentResource;
        return this;
    }


    public ConsentResourceResponseBody getDetailedConsent() {
        return consentResource;
    }

    public void setDetailedConsent(
            ConsentResourceResponseBody detailedConsent) {
        this.consentResource = detailedConsent;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentHistoryResponse consentHistory = (ConsentHistoryResponse) o;
        return Objects.equals(this.historyId, consentHistory.historyId) &&
                Objects.equals(this.consentID, consentHistory.consentID) &&
                Objects.equals(this.timestamp, consentHistory.timestamp) &&
                Objects.equals(this.reason, consentHistory.reason) &&
                Objects.equals(this.consentResource, consentHistory.consentResource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId, consentID, timestamp, reason, consentResource);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentHistory {\n");

        sb.append("    historyId: ").append(toIndentedString(historyId)).append("\n");
        sb.append("    consentID: ").append(toIndentedString(consentID)).append("\n");
        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
        sb.append("    consentResource: ").append(toIndentedString(consentResource)).append("\n");
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

