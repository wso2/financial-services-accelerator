package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.Valid;


@JsonTypeName("ConsentUserMappingResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-03-03T09:27:49.560668411+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentUserMappingResponse implements Serializable {
    private String consentID;
    private AuthResponse authResource;
    private Integer updatedTime;

    public ConsentUserMappingResponse() {
    }

    /**
     *
     **/
    public ConsentUserMappingResponse consentID(String consentID) {
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
    public ConsentUserMappingResponse authResource(AuthResponse authResource) {
        this.authResource = authResource;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("authResource")
    @Valid public AuthResponse getAuthResource() {
        return authResource;
    }

    @JsonProperty("authResource")
    public void setAuthResource(AuthResponse authResource) {
        this.authResource = authResource;
    }

    /**
     *
     **/
    public ConsentUserMappingResponse updatedTime(Integer updatedTime) {
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
        ConsentUserMappingResponse consentUserMappingResponse = (ConsentUserMappingResponse) o;
        return Objects.equals(this.consentID, consentUserMappingResponse.consentID) &&
                Objects.equals(this.authResource, consentUserMappingResponse.authResource) &&
                Objects.equals(this.updatedTime, consentUserMappingResponse.updatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consentID, authResource, updatedTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentUserMappingResponse {\n");

        sb.append("    consentID: ").append(toIndentedString(consentID)).append("\n");
        sb.append("    authResource: ").append(toIndentedString(authResource)).append("\n");
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

