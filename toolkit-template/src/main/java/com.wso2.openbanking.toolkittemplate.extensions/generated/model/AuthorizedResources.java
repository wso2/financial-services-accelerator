package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("AuthorizedResources")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class AuthorizedResources extends HashMap<String, Object>  {
  private Boolean approval;
  private Boolean isReauthorization;
  private String type;
  private @Valid List<@Valid AuthorizedResourcesAuthorizedDataInner> authorizedData = new ArrayList<>();
  private Object metadata;

  public AuthorizedResources() {
  }

  /**
   * Whether the user approved the consent
   **/
  public AuthorizedResources approval(Boolean approval) {
    this.approval = approval;
    return this;
  }

  
  @ApiModelProperty(value = "Whether the user approved the consent")
  @JsonProperty("approval")
  public Boolean getApproval() {
    return approval;
  }

  @JsonProperty("approval")
  public void setApproval(Boolean approval) {
    this.approval = approval;
  }

  /**
   * Indicates if this was a reauthorization flow (optional)
   **/
  public AuthorizedResources isReauthorization(Boolean isReauthorization) {
    this.isReauthorization = isReauthorization;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates if this was a reauthorization flow (optional)")
  @JsonProperty("isReauthorization")
  public Boolean getIsReauthorization() {
    return isReauthorization;
  }

  @JsonProperty("isReauthorization")
  public void setIsReauthorization(Boolean isReauthorization) {
    this.isReauthorization = isReauthorization;
  }

  /**
   * Type of consent granted (e.g., &#39;accounts&#39;, &#39;payments&#39;, etc.)
   **/
  public AuthorizedResources type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "Type of consent granted (e.g., 'accounts', 'payments', etc.)")
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  /**
   * List of granted permissions and corresponding user-selected account data
   **/
  public AuthorizedResources authorizedData(List<@Valid AuthorizedResourcesAuthorizedDataInner> authorizedData) {
    this.authorizedData = authorizedData;
    return this;
  }

  
  @ApiModelProperty(value = "List of granted permissions and corresponding user-selected account data")
  @JsonProperty("authorizedData")
  @Valid public List<@Valid AuthorizedResourcesAuthorizedDataInner> getAuthorizedData() {
    return authorizedData;
  }

  @JsonProperty("authorizedData")
  public void setAuthorizedData(List<@Valid AuthorizedResourcesAuthorizedDataInner> authorizedData) {
    this.authorizedData = authorizedData;
  }

  public AuthorizedResources addAuthorizedDataItem(AuthorizedResourcesAuthorizedDataInner authorizedDataItem) {
    if (this.authorizedData == null) {
      this.authorizedData = new ArrayList<>();
    }

    this.authorizedData.add(authorizedDataItem);
    return this;
  }

  public AuthorizedResources removeAuthorizedDataItem(AuthorizedResourcesAuthorizedDataInner authorizedDataItem) {
    if (authorizedDataItem != null && this.authorizedData != null) {
      this.authorizedData.remove(authorizedDataItem);
    }

    return this;
  }
  /**
   * Consent authorization related metadata.
   **/
  public AuthorizedResources metadata(Object metadata) {
    this.metadata = metadata;
    return this;
  }

  
  @ApiModelProperty(value = "Consent authorization related metadata.")
  @JsonProperty("metadata")
  public Object getMetadata() {
    return metadata;
  }

  @JsonProperty("metadata")
  public void setMetadata(Object metadata) {
    this.metadata = metadata;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthorizedResources authorizedResources = (AuthorizedResources) o;
    return Objects.equals(this.approval, authorizedResources.approval) &&
        Objects.equals(this.isReauthorization, authorizedResources.isReauthorization) &&
        Objects.equals(this.type, authorizedResources.type) &&
        Objects.equals(this.authorizedData, authorizedResources.authorizedData) &&
        Objects.equals(this.metadata, authorizedResources.metadata) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(approval, isReauthorization, type, authorizedData, metadata, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorizedResources {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    approval: ").append(toIndentedString(approval)).append("\n");
    sb.append("    isReauthorization: ").append(toIndentedString(isReauthorization)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    authorizedData: ").append(toIndentedString(authorizedData)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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

