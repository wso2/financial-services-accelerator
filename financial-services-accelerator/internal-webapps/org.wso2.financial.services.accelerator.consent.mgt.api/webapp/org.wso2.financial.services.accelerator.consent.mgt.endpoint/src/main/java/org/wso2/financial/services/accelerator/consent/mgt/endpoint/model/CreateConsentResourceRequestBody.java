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


@JsonTypeName("ConsentResourceDTO")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class CreateConsentResourceRequestBody implements Serializable {
    private String clientID;
    private String consentType;
    private String currentStatus;
    private String receipt;
    private Integer expiryTime;
    private Boolean recurringIndicator;
    private Object consentAttributes;
    private @Valid List<@Valid CreateAuthorizationResourceRequestBody> authorizationResources = new ArrayList<>();

    public CreateConsentResourceRequestBody() {
    }

    @JsonCreator
    public CreateConsentResourceRequestBody(
            @JsonProperty(required = true, value = "clientID") String clientID,
            @JsonProperty(required = true, value = "consentType") String consentType,
            @JsonProperty(required = true, value = "currentStatus") String currentStatus,
            @JsonProperty(required = true, value = "receipt") String receipt,
            @JsonProperty(required = true, value = "expiryTime") Integer expiryTime,
            @JsonProperty(required = true, value = "recurringIndicator") Boolean recurringIndicator
                                           ) {
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
    public CreateConsentResourceRequestBody clientID(String clientID) {
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
    public CreateConsentResourceRequestBody consentType(String consentType) {
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
    public CreateConsentResourceRequestBody currentStatus(String currentStatus) {
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
    public CreateConsentResourceRequestBody receipt(String receipt) {
        this.receipt = receipt;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "receipt")
    @NotNull public String getReceipt() {
        return receipt;
    }

    @JsonProperty(required = true, value = "receipt")
    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    /**
     *
     **/
    public CreateConsentResourceRequestBody expiryTime(Integer expiryTime) {
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
    public CreateConsentResourceRequestBody recurringIndicator(Boolean recurringIndicator) {
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
    public CreateConsentResourceRequestBody consentAttributes(Object consentAttributes) {
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
    public CreateConsentResourceRequestBody authorizationResources(
            List<@Valid CreateAuthorizationResourceRequestBody> authorizationResources) {
        this.authorizationResources = authorizationResources;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("authorizationResources")
    @Valid public List<@Valid CreateAuthorizationResourceRequestBody> getAuthorizationResources() {
        return authorizationResources;
    }

    @JsonProperty("authorizationResources")
    public void setAuthorizationResources(List<@Valid CreateAuthorizationResourceRequestBody> authorizationResources) {
        this.authorizationResources = authorizationResources;
    }

    public CreateConsentResourceRequestBody addAuthorizationResourcesItem(
            CreateAuthorizationResourceRequestBody authorizationResourcesItem) {
        if (this.authorizationResources == null) {
            this.authorizationResources = new ArrayList<>();
        }

        this.authorizationResources.add(authorizationResourcesItem);
        return this;
    }

    public CreateConsentResourceRequestBody removeAuthorizationResourcesItem(
            CreateAuthorizationResourceRequestBody authorizationResourcesItem) {
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
        CreateConsentResourceRequestBody consentResourceDTO = (CreateConsentResourceRequestBody) o;
        return Objects.equals(this.clientID, consentResourceDTO.clientID) &&
                Objects.equals(this.consentType, consentResourceDTO.consentType) &&
                Objects.equals(this.currentStatus, consentResourceDTO.currentStatus) &&
                Objects.equals(this.receipt, consentResourceDTO.receipt) &&
                Objects.equals(this.expiryTime, consentResourceDTO.expiryTime) &&
                Objects.equals(this.recurringIndicator, consentResourceDTO.recurringIndicator) &&
                Objects.equals(this.consentAttributes, consentResourceDTO.consentAttributes) &&
                Objects.equals(this.authorizationResources, consentResourceDTO.authorizationResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientID, consentType, currentStatus, receipt, expiryTime, recurringIndicator,
                consentAttributes, authorizationResources);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentResourceDTO {\n");

        sb.append("    clientID: ").append(toIndentedString(clientID)).append("\n");
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

