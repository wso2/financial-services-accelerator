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


@JsonTypeName("ConsentResourceResponseBody")

public class ConsentResourceResponseBody implements Serializable {
    private String consentId;
    private String clientId;
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
            @JsonProperty(required = true, value = "consentId") String consentId,
            @JsonProperty(required = true, value = "clientId") String clientId,
            @JsonProperty(required = true, value = "consentType") String consentType,
            @JsonProperty(required = true, value = "currentStatus") String currentStatus,
            @JsonProperty(required = true, value = "receipt") Object receipt,
            @JsonProperty(required = true, value = "expiryTime") Integer expiryTime,
            @JsonProperty(required = true, value = "recurringIndicator") Boolean recurringIndicator
                                      ) {
        this.consentId = consentId;
        this.clientId = clientId;
        this.consentType = consentType;
        this.currentStatus = currentStatus;
        this.receipt = receipt;
        this.expiryTime = expiryTime;
        this.recurringIndicator = recurringIndicator;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody consentId(String consentId) {
        this.consentId = consentId;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "consentId")
    @NotNull @NotEmpty public String getConsentId() {
        return consentId;
    }

    @JsonProperty(required = true, value = "consentId")
    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    /**
     *
     **/
    public ConsentResourceResponseBody clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "clientId")
    @NotNull @NotEmpty @Size(min = 1) public String getClientId() {
        return clientId;
    }

    @JsonProperty(required = true, value = "clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
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
    @NotNull @NotEmpty @Size(min = 1) public String getConsentType() {
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
    @NotNull @NotEmpty @Size(min = 1) public String getCurrentStatus() {
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
        ConsentResourceResponseBody consentResourceResponseBody = (ConsentResourceResponseBody) o;
        return Objects.equals(this.consentId, consentResourceResponseBody.consentId) &&
                Objects.equals(this.clientId, consentResourceResponseBody.clientId) &&
                Objects.equals(this.consentType, consentResourceResponseBody.consentType) &&
                Objects.equals(this.currentStatus, consentResourceResponseBody.currentStatus) &&
                Objects.equals(this.receipt, consentResourceResponseBody.receipt) &&
                Objects.equals(this.expiryTime, consentResourceResponseBody.expiryTime) &&
                Objects.equals(this.recurringIndicator, consentResourceResponseBody.recurringIndicator) &&
                Objects.equals(this.consentAttributes, consentResourceResponseBody.consentAttributes) &&
                Objects.equals(this.authorizationResources, consentResourceResponseBody.authorizationResources) &&
                Objects.equals(this.createdTime, consentResourceResponseBody.createdTime) &&
                Objects.equals(this.updatedTime, consentResourceResponseBody.updatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consentId, clientId, consentType, currentStatus, receipt, expiryTime, recurringIndicator,
                consentAttributes, authorizationResources, createdTime, updatedTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentResourceResponseBody {\n");

        sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
        sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
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

