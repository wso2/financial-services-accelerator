package org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model;

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
import javax.validation.constraints.NotEmpty;

import javax.validation.constraints.Size;

@JsonTypeName("ConsentResourceRequestBody")
public class ConsentResourceRequestBody implements Serializable {
    private String clientId;
    private String consentType;
    private String currentStatus;
    private String receipt;
    private Integer expiryTime;
    private Boolean recurringIndicator;
    private Object consentAttributes;
    private @Valid List<@Valid AuthorizationResourceRequestBody> authorizationResources = new ArrayList<>();

    public ConsentResourceRequestBody() {
    }

    @JsonCreator
    public ConsentResourceRequestBody(
            @JsonProperty(required = true, value = "clientId") String clientId,
            @JsonProperty(required = true, value = "consentType") String consentType,
            @JsonProperty(required = true, value = "currentStatus") String currentStatus,
            @JsonProperty(required = true, value = "receipt") String receipt,
            @JsonProperty(required = true, value = "expiryTime") Integer expiryTime
                                     ) {
        this.clientId = clientId;
        this.consentType = consentType;
        this.currentStatus = currentStatus;
        this.receipt = receipt;
        this.expiryTime = expiryTime;
    }

    /**
     *
     **/
    public ConsentResourceRequestBody clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "clientId")
    @NotNull @NotEmpty public String getClientId() {
        return clientId;
    }

    @JsonProperty(required = true, value = "clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     *
     **/
    public ConsentResourceRequestBody consentType(String consentType) {
        this.consentType = consentType;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "consentType")
    @NotNull @NotEmpty public String getConsentType() {
        return consentType;
    }

    @JsonProperty(required = true, value = "consentType")
    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    /**
     *
     **/
    public ConsentResourceRequestBody currentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "currentStatus")
    @NotNull @NotEmpty @Size(min = 3) public String getCurrentStatus() {
        return currentStatus;
    }

    @JsonProperty(required = true, value = "currentStatus")
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    /**
     *
     **/
    public ConsentResourceRequestBody receipt(String receipt) {
        this.receipt = receipt;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "receipt")
    @NotNull @NotEmpty public String getReceipt() {
        return receipt;
    }

    @JsonProperty(required = true, value = "receipt")
    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    /**
     *
     **/
    public ConsentResourceRequestBody expiryTime(Integer expiryTime) {
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
    public ConsentResourceRequestBody recurringIndicator(Boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("recurringIndicator")
    public Boolean getRecurringIndicator() {
        return recurringIndicator;
    }

    @JsonProperty("recurringIndicator")
    public void setRecurringIndicator(Boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
    }

    /**
     *
     **/
    public ConsentResourceRequestBody consentAttributes(Object consentAttributes) {
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
    public ConsentResourceRequestBody authorizationResources(
            List<@Valid AuthorizationResourceRequestBody> authorizationResources) {
        this.authorizationResources = authorizationResources;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("authorizationResources")
    @Valid public List<@Valid AuthorizationResourceRequestBody> getAuthorizationResources() {
        return authorizationResources;
    }

    @JsonProperty("authorizationResources")
    public void setAuthorizationResources(List<@Valid AuthorizationResourceRequestBody> authorizationResources) {
        this.authorizationResources = authorizationResources;
    }

    public ConsentResourceRequestBody addAuthorizationResourcesItem(
            AuthorizationResourceRequestBody authorizationResourcesItem) {
        if (this.authorizationResources == null) {
            this.authorizationResources = new ArrayList<>();
        }

        this.authorizationResources.add(authorizationResourcesItem);
        return this;
    }

    public ConsentResourceRequestBody removeAuthorizationResourcesItem(
            AuthorizationResourceRequestBody authorizationResourcesItem) {
        if (authorizationResourcesItem != null && this.authorizationResources != null) {
            this.authorizationResources.remove(authorizationResourcesItem);
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentResourceRequestBody consentResourceRequestBody = (ConsentResourceRequestBody) o;
        return Objects.equals(this.clientId, consentResourceRequestBody.clientId) &&
                Objects.equals(this.consentType, consentResourceRequestBody.consentType) &&
                Objects.equals(this.currentStatus, consentResourceRequestBody.currentStatus) &&
                Objects.equals(this.receipt, consentResourceRequestBody.receipt) &&
                Objects.equals(this.expiryTime, consentResourceRequestBody.expiryTime) &&
                Objects.equals(this.recurringIndicator, consentResourceRequestBody.recurringIndicator) &&
                Objects.equals(this.consentAttributes, consentResourceRequestBody.consentAttributes) &&
                Objects.equals(this.authorizationResources, consentResourceRequestBody.authorizationResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, consentType, currentStatus, receipt, expiryTime, recurringIndicator,
                consentAttributes, authorizationResources);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentResourceRequestBody {\n");

        sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
        sb.append("    consentType: ").append(toIndentedString(consentType)).append("\n");
        sb.append("    currentStatus: ").append(toIndentedString(currentStatus)).append("\n");
        sb.append("    receipt: ").append(toIndentedString(receipt)).append("\n");
        sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
        sb.append("    recurringIndicator: ").append(toIndentedString(recurringIndicator)).append("\n");
        sb.append("    consentAttributes: ").append(toIndentedString(consentAttributes)).append("\n");
        sb.append("    authorizationResources: ").append(toIndentedString(authorizationResources)).append("\n");
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

