package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("FailedResponseApplicationProcessData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class FailedResponseApplicationProcessData   {
  private String errorMessage;

  public FailedResponseApplicationProcessData() {
  }

  @JsonCreator
  public FailedResponseApplicationProcessData(
    @JsonProperty(required = true, value = "errorMessage") String errorMessage
  ) {
    this.errorMessage = errorMessage;
  }

  /**
   * Error message to be returned
   **/
  public FailedResponseApplicationProcessData errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Error message to be returned")
  @JsonProperty(required = true, value = "errorMessage")
  @NotNull public String getErrorMessage() {
    return errorMessage;
  }

  @JsonProperty(required = true, value = "errorMessage")
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FailedResponseApplicationProcessData failedResponseApplicationProcessData = (FailedResponseApplicationProcessData) o;
    return Objects.equals(this.errorMessage, failedResponseApplicationProcessData.errorMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FailedResponseApplicationProcessData {\n");
    
    sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
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

