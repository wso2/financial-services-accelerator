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



@JsonTypeName("ConsentHistoryResponse")

public class ConsentHistoryResponse  implements Serializable {
    private ConsentResourceResponseBody detailedConsent;
    private String historyId;
    private String consentId;
    private Integer timestamp;
    private String reason;

public ConsentHistoryResponse() {
}

    /**
    **/
    public ConsentHistoryResponse detailedConsent(ConsentResourceResponseBody detailedConsent) {
    this.detailedConsent = detailedConsent;
    return this;
    }

    
        @ApiModelProperty(value = "")
    @JsonProperty("detailedConsent")
    @Valid public ConsentResourceResponseBody getDetailedConsent() {
    return detailedConsent;
    }

    @JsonProperty("detailedConsent")
    public void setDetailedConsent(ConsentResourceResponseBody detailedConsent) {
    this.detailedConsent = detailedConsent;
    }

    /**
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
    **/
    public ConsentHistoryResponse consentId(String consentId) {
    this.consentId = consentId;
    return this;
    }

    
        @ApiModelProperty(value = "")
    @JsonProperty("consentId")
    public String getConsentId() {
    return consentId;
    }

    @JsonProperty("consentId")
    public void setConsentId(String consentId) {
    this.consentId = consentId;
    }

    /**
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


@Override
public boolean equals(Object o) {
if (this == o) {
return true;
}
if (o == null || getClass() != o.getClass()) {
return false;
}
    ConsentHistoryResponse consentHistoryResponse = (ConsentHistoryResponse) o;
    return Objects.equals(this.detailedConsent, consentHistoryResponse.detailedConsent) &&
    Objects.equals(this.historyId, consentHistoryResponse.historyId) &&
    Objects.equals(this.consentId, consentHistoryResponse.consentId) &&
    Objects.equals(this.timestamp, consentHistoryResponse.timestamp) &&
    Objects.equals(this.reason, consentHistoryResponse.reason);
}

@Override
public int hashCode() {
return Objects.hash(detailedConsent, historyId, consentId, timestamp, reason);
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append("class ConsentHistoryResponse {\n");

sb.append("    detailedConsent: ").append(toIndentedString(detailedConsent)).append("\n");
sb.append("    historyId: ").append(toIndentedString(historyId)).append("\n");
sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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

