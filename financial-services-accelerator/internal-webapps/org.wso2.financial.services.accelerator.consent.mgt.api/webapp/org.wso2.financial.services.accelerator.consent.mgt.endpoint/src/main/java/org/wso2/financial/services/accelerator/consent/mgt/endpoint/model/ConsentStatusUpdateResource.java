package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;


@JsonTypeName("ConsentStatusUpdateResource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentStatusUpdateResource implements Serializable {
    private String status;
    private String reason;
    private String userID;

    public ConsentStatusUpdateResource() {
    }

    /**
     *
     **/
    public ConsentStatusUpdateResource status(String status) {
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
    public ConsentStatusUpdateResource reason(String reason) {
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
    public ConsentStatusUpdateResource userID(String userID) {
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
        ConsentStatusUpdateResource consentStatusUpdateResource = (ConsentStatusUpdateResource) o;
        return Objects.equals(this.status, consentStatusUpdateResource.status) &&
                Objects.equals(this.reason, consentStatusUpdateResource.reason) &&
                Objects.equals(this.userID, consentStatusUpdateResource.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, reason, userID);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentStatusUpdateResource {\n");

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

