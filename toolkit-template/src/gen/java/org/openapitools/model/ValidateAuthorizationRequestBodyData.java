package org.openapitools.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * full request object
 **/
@ApiModel(description = "full request object")
@JsonTypeName("ValidateAuthorizationRequestBodyData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class ValidateAuthorizationRequestBodyData   {
  private Object requestObject;

  public ValidateAuthorizationRequestBodyData() {
  }

  /**
   **/
  public ValidateAuthorizationRequestBodyData requestObject(Object requestObject) {
    this.requestObject = requestObject;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("requestObject")
  public Object getRequestObject() {
    return requestObject;
  }

  @JsonProperty("requestObject")
  public void setRequestObject(Object requestObject) {
    this.requestObject = requestObject;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidateAuthorizationRequestBodyData validateAuthorizationRequestBodyData = (ValidateAuthorizationRequestBodyData) o;
    return Objects.equals(this.requestObject, validateAuthorizationRequestBodyData.requestObject);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestObject);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ValidateAuthorizationRequestBodyData {\n");
    
    sb.append("    requestObject: ").append(toIndentedString(requestObject)).append("\n");
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

