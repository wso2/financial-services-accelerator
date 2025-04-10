package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;


@JsonTypeName("ConsentFile")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-03-03T09:27:49.560668411+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentFile implements Serializable {
    private String consentID;
    private String consentFile;
    private String clientID;
    private String userID;
    private String applicableStatus;

    public ConsentFile() {
    }

    /**
     *
     **/
    public ConsentFile consentID(String consentID) {
        this.consentID = consentID;
        return this;
    }


    @ApiModelProperty(example = "604d9278-4c3b-45d5-b3bb-1e428acdf1ec", value = "")
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
    public ConsentFile consentFile(String consentFile) {
        this.consentFile = consentFile;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("consentFile")
    public String getConsentFile() {
        return consentFile;
    }

    @JsonProperty("consentFile")
    public void setConsentFile(String consentFile) {
        this.consentFile = consentFile;
    }

    /**
     *
     **/
    public ConsentFile clientID(String clientID) {
        this.clientID = clientID;
        return this;
    }


    @ApiModelProperty(example = "TUwYBlObBMmu7zvDnnhs96rZHxka", value = "")
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
    public ConsentFile userID(String userID) {
        this.userID = userID;
        return this;
    }


    @ApiModelProperty(example = "psu@wso2.com", value = "")
    @JsonProperty("userID")
    public String getUserId() {
        return userID;
    }

    @JsonProperty("userID")
    public void setUserId(String userID) {
        this.userID = userID;
    }

    /**
     *
     **/
    public ConsentFile applicableStatus(String applicableStatus) {
        this.applicableStatus = applicableStatus;
        return this;
    }


    @ApiModelProperty(example = "awaitingAuthorization", value = "")
    @JsonProperty("applicableStatus")
    public String getApplicableStatus() {
        return applicableStatus;
    }

    @JsonProperty("applicableStatus")
    public void setApplicableStatus(String applicableStatus) {
        this.applicableStatus = applicableStatus;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentFile consentFile = (ConsentFile) o;
        return Objects.equals(this.consentID, consentFile.consentID) &&
                Objects.equals(this.consentFile, consentFile.consentFile) &&
                Objects.equals(this.clientID, consentFile.clientID) &&
                Objects.equals(this.userID, consentFile.userID) &&
                Objects.equals(this.applicableStatus, consentFile.applicableStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consentID, consentFile, clientID, userID, applicableStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentFile {\n");

        sb.append("    consentID: ").append(toIndentedString(consentID)).append("\n");
        sb.append("    consentFile: ").append(toIndentedString(consentFile)).append("\n");
        sb.append("    clientID: ").append(toIndentedString(clientID)).append("\n");
        sb.append("    userID: ").append(toIndentedString(userID)).append("\n");
        sb.append("    applicableStatus: ").append(toIndentedString(applicableStatus)).append("\n");
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

