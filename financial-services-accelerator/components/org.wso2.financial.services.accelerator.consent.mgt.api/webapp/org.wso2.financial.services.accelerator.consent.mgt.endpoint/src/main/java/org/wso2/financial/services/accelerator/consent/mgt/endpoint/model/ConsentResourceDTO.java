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


@JsonTypeName("ConsentResource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-03-03T09:27:49.560668411+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentResourceDTO implements Serializable {
    private String clientID;
    private String consentType;
    private String currentStatus;
    private String receipt;
    private Integer validityPeriod;
    private Boolean recurringIndicator;
    private Object consentAttributes;
    private @Valid List<@Valid AuthorizationResourceDTO> authorizationResources = new ArrayList<>();

    public ConsentResourceDTO() {
    }

    @JsonCreator
    public ConsentResourceDTO(
            @JsonProperty(required = true, value = "clientID") String clientID,
            @JsonProperty(required = true, value = "consentType") String consentType,
            @JsonProperty(required = true, value = "currentStatus") String currentStatus,
            @JsonProperty(required = true, value = "receipt") String receipt,
            @JsonProperty(required = true, value = "validityPeriod") Integer validityPeriod,
            @JsonProperty(required = true, value = "recurringIndicator") Boolean recurringIndicator
                          ) {
        this.clientID = clientID;
        this.consentType = consentType;
        this.currentStatus = currentStatus;
        this.receipt = receipt;
        this.validityPeriod = validityPeriod;
        this.recurringIndicator = recurringIndicator;
    }

    /**
     *
     **/
    public ConsentResourceDTO clientID(String clientID) {
        this.clientID = clientID;
        return this;
    }


    @ApiModelProperty(example = "TUwYBlObBMmu7zvDnnhs96rZHxka", required = true, value = "")
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
    public ConsentResourceDTO consentType(String consentType) {
        this.consentType = consentType;
        return this;
    }


    @ApiModelProperty(example = "Accounts", required = true, value = "")
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
    public ConsentResourceDTO currentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }


    @ApiModelProperty(example = "awaitingAuthorisation", required = true, value = "")
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
    public ConsentResourceDTO receipt(String receipt) {
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
    public ConsentResourceDTO validityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
        return this;
    }


    @ApiModelProperty(example = "3600", required = true, value = "")
    @JsonProperty(required = true, value = "validityPeriod")
    @NotNull public Integer getValidityPeriod() {
        return validityPeriod;
    }

    @JsonProperty(required = true, value = "validityPeriod")
    public void setValidityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    /**
     *
     **/
    public ConsentResourceDTO recurringIndicator(Boolean recurringIndicator) {
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
    public ConsentResourceDTO consentAttributes(Object consentAttributes) {
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
    public ConsentResourceDTO authorizationResources(List<@Valid AuthorizationResourceDTO> authorizationResources) {
        this.authorizationResources = authorizationResources;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("authorizationResources")
    @Valid public List<@Valid AuthorizationResourceDTO> getAuthorizationResources() {
        return authorizationResources;
    }

    @JsonProperty("authorizationResources")
    public void setAuthorizationResources(List<@Valid AuthorizationResourceDTO> authorizationResources) {
        this.authorizationResources = authorizationResources;
    }

    public ConsentResourceDTO addAuthorizationResourcesItem(AuthorizationResourceDTO authorizationResourcesItem) {
        if (this.authorizationResources == null) {
            this.authorizationResources = new ArrayList<>();
        }

        this.authorizationResources.add(authorizationResourcesItem);
        return this;
    }

    public ConsentResourceDTO removeAuthorizationResourcesItem(AuthorizationResourceDTO authorizationResourcesItem) {
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
        ConsentResourceDTO consentResource = (ConsentResourceDTO) o;
        return Objects.equals(this.clientID, consentResource.clientID) &&
                Objects.equals(this.consentType, consentResource.consentType) &&
                Objects.equals(this.currentStatus, consentResource.currentStatus) &&
                Objects.equals(this.receipt, consentResource.receipt) &&
                Objects.equals(this.validityPeriod, consentResource.validityPeriod) &&
                Objects.equals(this.recurringIndicator, consentResource.recurringIndicator) &&
                Objects.equals(this.consentAttributes, consentResource.consentAttributes) &&
                Objects.equals(this.authorizationResources, consentResource.authorizationResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientID, consentType, currentStatus, receipt, validityPeriod, recurringIndicator,
                consentAttributes, authorizationResources);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class  {\n");

        sb.append("    clientID: ").append(toIndentedString(clientID)).append("\n");
        sb.append("    consentType: ").append(toIndentedString(consentType)).append("\n");
        sb.append("    currentStatus: ").append(toIndentedString(currentStatus)).append("\n");
        sb.append("    receipt: ").append(toIndentedString(receipt)).append("\n");
        sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
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

