package org.openapitools.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.model.AmendedResource;
import org.openapitools.model.Resource;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("AmendedAuthorization")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class AmendedAuthorization   {
  private String id;
  private String type;
  private String status;
  private @Valid List<@Valid Resource> resources = new ArrayList<>();
  private @Valid List<@Valid AmendedResource> amendedResources = new ArrayList<>();

  public AmendedAuthorization() {
  }

  /**
   **/
  public AmendedAuthorization id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public AmendedAuthorization type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public AmendedAuthorization status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public AmendedAuthorization resources(List<@Valid Resource> resources) {
    this.resources = resources;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("resources")
  @Valid public List<@Valid Resource> getResources() {
    return resources;
  }

  @JsonProperty("resources")
  public void setResources(List<@Valid Resource> resources) {
    this.resources = resources;
  }

  public AmendedAuthorization addResourcesItem(Resource resourcesItem) {
    if (this.resources == null) {
      this.resources = new ArrayList<>();
    }

    this.resources.add(resourcesItem);
    return this;
  }

  public AmendedAuthorization removeResourcesItem(Resource resourcesItem) {
    if (resourcesItem != null && this.resources != null) {
      this.resources.remove(resourcesItem);
    }

    return this;
  }
  /**
   **/
  public AmendedAuthorization amendedResources(List<@Valid AmendedResource> amendedResources) {
    this.amendedResources = amendedResources;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("amendedResources")
  @Valid public List<@Valid AmendedResource> getAmendedResources() {
    return amendedResources;
  }

  @JsonProperty("amendedResources")
  public void setAmendedResources(List<@Valid AmendedResource> amendedResources) {
    this.amendedResources = amendedResources;
  }

  public AmendedAuthorization addAmendedResourcesItem(AmendedResource amendedResourcesItem) {
    if (this.amendedResources == null) {
      this.amendedResources = new ArrayList<>();
    }

    this.amendedResources.add(amendedResourcesItem);
    return this;
  }

  public AmendedAuthorization removeAmendedResourcesItem(AmendedResource amendedResourcesItem) {
    if (amendedResourcesItem != null && this.amendedResources != null) {
      this.amendedResources.remove(amendedResourcesItem);
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
    AmendedAuthorization amendedAuthorization = (AmendedAuthorization) o;
    return Objects.equals(this.id, amendedAuthorization.id) &&
        Objects.equals(this.type, amendedAuthorization.type) &&
        Objects.equals(this.status, amendedAuthorization.status) &&
        Objects.equals(this.resources, amendedAuthorization.resources) &&
        Objects.equals(this.amendedResources, amendedAuthorization.amendedResources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, status, resources, amendedResources);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AmendedAuthorization {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
    sb.append("    amendedResources: ").append(toIndentedString(amendedResources)).append("\n");
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

