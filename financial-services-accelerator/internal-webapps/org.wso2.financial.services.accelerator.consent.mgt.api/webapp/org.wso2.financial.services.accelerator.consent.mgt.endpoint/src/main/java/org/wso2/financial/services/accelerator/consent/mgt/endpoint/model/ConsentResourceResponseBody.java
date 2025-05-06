package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@JsonTypeName("ConsentResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentResourceResponseBody implements Serializable {
    private String consentID;
    private String clientID;
    private String consentType;
    private String currentStatus;
    private Object receipt;
    private Integer expiryTime;
    private Boolean recurringIndicator;
    private Object consentAttributes;
    private @Valid List<@Valid AuthorizationResourceResponseBody> authorizationResources = new ArrayList<>();
    private Integer createdTime;
    private Integer updatedTime;

    public ConsentResourceResponseBody() {
    }

    @JsonCreator
    public ConsentResourceResponseBody(
            @JsonProperty(required = true, value = "consentID") String consentID,
            @JsonProperty(required = true, value = "clientID") String clientID,
            @JsonProperty(required = true, value = "consentType") String consentType,
            @JsonProperty(required = true, value = "currentStatus") String currentStatus,
            @JsonProperty(required = true, value = "receipt") Object receipt,
            @JsonProperty(required = true, value = "expiryTime") Integer expiryTime,
            @JsonProperty(required = true, value = "recurringIndicator") Boolean recurringIndicator
                                      ) {
        this.consentID = consentID;
        this.clientID = clientID;
        this.consentType = consentType;
        this.currentStatus = currentStatus;
        this.receipt = receipt;
        this.expiryTime = expiryTime;
        this.recurringIndicator = recurringIndicator;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody consentID(String consentID) {
        this.consentID = consentID;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "consentID")
    @NotNull public String getConsentID() {
        return consentID;
    }

    @JsonProperty(required = true, value = "consentID")
    public void setConsentID(String consentID) {
        this.consentID = consentID;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody clientID(String clientID) {
        this.clientID = clientID;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "clientID")
    @NotNull public String getClientID() {
        return clientID;
    }

    @JsonProperty(required = true, value = "clientID")
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody consentType(String consentType) {
        this.consentType = consentType;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "consentType")
    @NotNull public String getConsentType() {
        return consentType;
    }

    @JsonProperty(required = true, value = "consentType")
    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody currentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "currentStatus")
    @NotNull public String getCurrentStatus() {
        return currentStatus;
    }

    @JsonProperty(required = true, value = "currentStatus")
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody receipt(Object receipt) {
        this.receipt = receipt;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "receipt")
    @NotNull public Object getReceipt() {
        return receipt;
    }

    @JsonProperty(required = true, value = "receipt")
    public void setReceipt(Object receipt) {
        this.receipt = receipt;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody expiryTime(Integer expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "expiryTime")
    @NotNull public Integer getExpiryTime() {
        return expiryTime;
    }

    @JsonProperty(required = true, value = "expiryTime")
    public void setExpiryTime(Integer expiryTime) {
        this.expiryTime = expiryTime;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody recurringIndicator(Boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "recurringIndicator")
    @NotNull public Boolean getRecurringIndicator() {
        return recurringIndicator;
    }

    @JsonProperty(required = true, value = "recurringIndicator")
    public void setRecurringIndicator(Boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody consentAttributes(Object consentAttributes) {
        this.consentAttributes = consentAttributes;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("consentAttributes")
    public Object getConsentAttributes() {
        return consentAttributes;
    }

    @JsonProperty("consentAttributes")
    public void setConsentAttributes(Object consentAttributes) {
        this.consentAttributes = consentAttributes;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody authorizationResources(
            List<@Valid AuthorizationResourceResponseBody> authorizationResources) {
        this.authorizationResources = authorizationResources;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("authorizationResources")
    @Valid public List<@Valid AuthorizationResourceResponseBody> getAuthorizationResources() {
        return authorizationResources;
    }

    @JsonProperty("authorizationResources")
    public void setAuthorizationResources(List<@Valid AuthorizationResourceResponseBody> authorizationResources) {
        this.authorizationResources = authorizationResources;
    }

    public ConsentResourceResponseBody addAuthorizationResourcesItem(
            AuthorizationResourceResponseBody authorizationResourcesItem) {
        if (this.authorizationResources == null) {
            this.authorizationResources = new ArrayList<>();
        }

        this.authorizationResources.add(authorizationResourcesItem);
        return this;
    }

    public ConsentResourceResponseBody removeAuthorizationResourcesItem(
            AuthorizationResourceResponseBody authorizationResourcesItem) {
        if (authorizationResourcesItem != null && this.authorizationResources != null) {
            this.authorizationResources.remove(authorizationResourcesItem);
        }

        return this;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody createdTime(Integer createdTime) {
        this.createdTime = createdTime;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("createdTime")
    public Integer getCreatedTime() {
        return createdTime;
    }

    @JsonProperty("createdTime")
    public void setCreatedTime(Integer createdTime) {
        this.createdTime = createdTime;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody updatedTime(Integer updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("updatedTime")
    public Integer getUpdatedTime() {
        return updatedTime;
    }

    @JsonProperty("updatedTime")
    public void setUpdatedTime(Integer updatedTime) {
        this.updatedTime = updatedTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentResourceResponseBody consentResponse = (ConsentResourceResponseBody) o;
        return Objects.equals(this.consentID, consentResponse.consentID) &&
                Objects.equals(this.clientID, consentResponse.clientID) &&
                Objects.equals(this.consentType, consentResponse.consentType) &&
                Objects.equals(this.currentStatus, consentResponse.currentStatus) &&
                Objects.equals(this.receipt, consentResponse.receipt) &&
                Objects.equals(this.expiryTime, consentResponse.expiryTime) &&
                Objects.equals(this.recurringIndicator, consentResponse.recurringIndicator) &&
                Objects.equals(this.consentAttributes, consentResponse.consentAttributes) &&
                Objects.equals(this.authorizationResources, consentResponse.authorizationResources) &&
                Objects.equals(this.createdTime, consentResponse.createdTime) &&
                Objects.equals(this.updatedTime, consentResponse.updatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consentID, clientID, consentType, currentStatus, receipt, expiryTime,
                recurringIndicator, consentAttributes, authorizationResources, createdTime, updatedTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentResponse {\n");

        sb.append("    consentID: ").append(toIndentedString(consentID)).append("\n");
        sb.append("    clientID: ").append(toIndentedString(clientID)).append("\n");
        sb.append("    consentType: ").append(toIndentedString(consentType)).append("\n");
        sb.append("    currentStatus: ").append(toIndentedString(currentStatus)).append("\n");
        sb.append("    receipt: ").append(toIndentedString(receipt)).append("\n");
        sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
        sb.append("    recurringIndicator: ").append(toIndentedString(recurringIndicator)).append("\n");
        sb.append("    consentAttributes: ").append(toIndentedString(consentAttributes)).append("\n");
        sb.append("    authorizationResources: ").append(toIndentedString(authorizationResources)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
        sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
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

