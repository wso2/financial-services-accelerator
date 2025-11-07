package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("UserGrantedData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class UserGrantedData   {
  private Object requestParameters;
  private AuthorizedResources authorizedResources;
  private String userId;

  public UserGrantedData() {
  }

  /**
   **/
  public UserGrantedData requestParameters(Object requestParameters) {
    this.requestParameters = requestParameters;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("requestParameters")
  public Object getRequestParameters() {
    return requestParameters;
  }

  @JsonProperty("requestParameters")
  public void setRequestParameters(Object requestParameters) {
    this.requestParameters = requestParameters;
  }

  /**
   **/
  public UserGrantedData authorizedResources(AuthorizedResources authorizedResources) {
    this.authorizedResources = authorizedResources;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authorizedResources")
  @Valid public AuthorizedResources getAuthorizedResources() {
    return authorizedResources;
  }

  @JsonProperty("authorizedResources")
  public void setAuthorizedResources(AuthorizedResources authorizedResources) {
    this.authorizedResources = authorizedResources;
  }

  /**
   **/
  public UserGrantedData userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserGrantedData userGrantedData = (UserGrantedData) o;
    return Objects.equals(this.requestParameters, userGrantedData.requestParameters) &&
        Objects.equals(this.authorizedResources, userGrantedData.authorizedResources) &&
        Objects.equals(this.userId, userGrantedData.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestParameters, authorizedResources, userId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserGrantedData {\n");
    
    sb.append("    requestParameters: ").append(toIndentedString(requestParameters)).append("\n");
    sb.append("    authorizedResources: ").append(toIndentedString(authorizedResources)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
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

