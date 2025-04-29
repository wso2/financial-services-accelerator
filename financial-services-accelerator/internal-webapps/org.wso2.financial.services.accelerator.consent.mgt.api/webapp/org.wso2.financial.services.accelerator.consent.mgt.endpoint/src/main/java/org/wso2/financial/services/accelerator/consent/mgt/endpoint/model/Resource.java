package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;
import net.minidev.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;


@JsonTypeName("Resource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class Resource implements Serializable {
    private JSONObject resource;
    private String consentMappingStatus;
    private String resourceMappingId;

    public Resource() {
    }

    /**
     *
     **/
    public Resource resource(JSONObject resource) {
        this.resource = resource;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("resource")
    public JSONObject getResource() {
        return resource;
    }

    @JsonProperty("resource")
    public void setResource(JSONObject resource) {
        this.resource = resource;
    }

    /**
     *
     **/
    public Resource consentMappingStatus(String consentMappingStatus) {
        this.consentMappingStatus = consentMappingStatus;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("consentMappingStatus")
    public String getConsentMappingStatus() {
        return consentMappingStatus;
    }

    @JsonProperty("consentMappingStatus")
    public void setConsentMappingStatus(String consentMappingStatus) {
        this.consentMappingStatus = consentMappingStatus;
    }

    /**
     *
     **/
    public Resource resourceMappingId(String resourceMappingId) {
        this.resourceMappingId = resourceMappingId;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("resourceMappingId")
    public String getResourceMappingId() {
        return resourceMappingId;
    }

    @JsonProperty("resourceMappingId")
    public void setResourceMappingId(String resourceMappingId) {
        this.resourceMappingId = resourceMappingId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Resource resource = (Resource) o;
        return Objects.equals(this.resource, resource.resource) &&
                Objects.equals(this.consentMappingStatus, resource.consentMappingStatus) &&
                Objects.equals(this.resourceMappingId, resource.resourceMappingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, consentMappingStatus, resourceMappingId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Resource {\n");

        sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
        sb.append("    consentMappingStatus: ").append(toIndentedString(consentMappingStatus)).append("\n");
        sb.append("    resourceMappingId: ").append(toIndentedString(resourceMappingId)).append("\n");
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

