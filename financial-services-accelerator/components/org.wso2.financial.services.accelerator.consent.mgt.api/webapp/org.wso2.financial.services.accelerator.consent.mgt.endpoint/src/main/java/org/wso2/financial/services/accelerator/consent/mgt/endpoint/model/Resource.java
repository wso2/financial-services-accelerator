package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;
import net.minidev.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;


@JsonTypeName("Resource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-03-03T09:27:49.560668411+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class Resource implements Serializable {
    private JSONObject resource;
    private String resourceMappingId;
    private String consentMappingStatus;


    public Resource() {
    }

    public String getConsentMappingStatus() {
        return consentMappingStatus;
    }

    public void setConsentMappingStatus(String consentMappingStatus) {
        this.consentMappingStatus = consentMappingStatus;
    }

    public Resource resourceMappingId(String resourceMappingId) {
        this.resourceMappingId = resourceMappingId;
        return this;
    }


    @ApiModelProperty(example = "1242334", value = "")
    @JsonProperty("resourceMappingId")
    public String getResourceMappingId() {
        return resourceMappingId;
    }

    @JsonProperty("resourceMappingId")
    public void setResourceMappingId(String resourceMappingId) {
        this.resourceMappingId = resourceMappingId;
    }


    public JSONObject getResource() {
        return resource;
    }

    public void setResource(JSONObject resource) {
        this.resource = resource;
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
        return
                Objects.equals(this.resourceMappingId, resource.resourceMappingId) &&
                        Objects.equals(this.resource, resource.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceMappingId, resource);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Resource {\n");
        sb.append("    resourceMappingId: ").append(toIndentedString(resourceMappingId)).append("\n");
        sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
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

