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


@JsonTypeName("AmendmentResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class AmendmentResponse implements Serializable {
    private String clientID;
    private String consentType;
    private Object receipt;
    private Integer expiryTime;
    private Boolean recurringIndicator;
    private String currentStatus;
    private Object consentAttributes;
    private @Valid List<@Valid ReauthorizeResource> authorizationResources = new ArrayList<>();

    public AmendmentResponse() {
    }

    @JsonCreator
    public AmendmentResponse(
            @JsonProperty(required = true, value = "clientID") String clientID,
            @JsonProperty(required = true, value = "consentType") String consentType,
            @JsonProperty(required = true, value = "receipt") Object receipt,
            @JsonProperty(required = true, value = "expiryTime") Integer expiryTime,
            @JsonProperty(required = true, value = "recurringIndicator") Boolean recurringIndicator
                            ) {
        this.clientID = clientID;
        this.consentType = consentType;
        this.receipt = receipt;
        this.expiryTime = expiryTime;
        this.recurringIndicator = recurringIndicator;
    }

    /**
     *
     **/
    public AmendmentResponse clientID(String clientID) {
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
    public AmendmentResponse consentType(String consentType) {
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
    public AmendmentResponse receipt(Object receipt) {
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
    public AmendmentResponse expiryTime(Integer expiryTime) {
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
    public AmendmentResponse recurringIndicator(Boolean recurringIndicator) {
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
    public AmendmentResponse currentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("currentStatus")
    public String getCurrentStatus() {
        return currentStatus;
    }

    @JsonProperty("currentStatus")
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    /**
     *
     **/
    public AmendmentResponse consentAttributes(Object consentAttributes) {
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
    public AmendmentResponse authorizationResources(List<@Valid ReauthorizeResource> authorizationResources) {
        this.authorizationResources = authorizationResources;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("authorizationResources")
    @Valid public List<@Valid ReauthorizeResource> getAuthorizationResources() {
        return authorizationResources;
    }

    @JsonProperty("authorizationResources")
    public void setAuthorizationResources(List<@Valid ReauthorizeResource> authorizationResources) {
        this.authorizationResources = authorizationResources;
    }

    public AmendmentResponse addAuthorizationResourcesItem(ReauthorizeResource authorizationResourcesItem) {
        if (this.authorizationResources == null) {
            this.authorizationResources = new ArrayList<>();
        }

        this.authorizationResources.add(authorizationResourcesItem);
        return this;
    }

    public AmendmentResponse removeAuthorizationResourcesItem(ReauthorizeResource authorizationResourcesItem) {
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
        AmendmentResponse amendmentResponse = (AmendmentResponse) o;
        return Objects.equals(this.clientID, amendmentResponse.clientID) &&
                Objects.equals(this.consentType, amendmentResponse.consentType) &&
                Objects.equals(this.receipt, amendmentResponse.receipt) &&
                Objects.equals(this.expiryTime, amendmentResponse.expiryTime) &&
                Objects.equals(this.recurringIndicator, amendmentResponse.recurringIndicator) &&
                Objects.equals(this.currentStatus, amendmentResponse.currentStatus) &&
                Objects.equals(this.consentAttributes, amendmentResponse.consentAttributes) &&
                Objects.equals(this.authorizationResources, amendmentResponse.authorizationResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientID, consentType, receipt, expiryTime, recurringIndicator, currentStatus,
                consentAttributes, authorizationResources);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmendmentResponse {\n");

        sb.append("    clientID: ").append(toIndentedString(clientID)).append("\n");
        sb.append("    consentType: ").append(toIndentedString(consentType)).append("\n");
        sb.append("    receipt: ").append(toIndentedString(receipt)).append("\n");
        sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
        sb.append("    recurringIndicator: ").append(toIndentedString(recurringIndicator)).append("\n");
        sb.append("    currentStatus: ").append(toIndentedString(currentStatus)).append("\n");
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

