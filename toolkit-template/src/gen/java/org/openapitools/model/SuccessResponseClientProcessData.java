package org.openapitools.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
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
 * Defines the success response.
 **/
@ApiModel(description = "Defines the success response.")
@JsonTypeName("SuccessResponseClientProcess_data")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponseClientProcessData   {
  private Object clientData;

  public SuccessResponseClientProcessData() {
  }

  /**
   **/
  public SuccessResponseClientProcessData clientData(Object clientData) {
    this.clientData = clientData;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("clientData")
  public Object getClientData() {
    return clientData;
  }

  @JsonProperty("clientData")
  public void setClientData(Object clientData) {
    this.clientData = clientData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponseClientProcessData successResponseClientProcessData = (SuccessResponseClientProcessData) o;
    return Objects.equals(this.clientData, successResponseClientProcessData.clientData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponseClientProcessData {\n");
    
    sb.append("    clientData: ").append(toIndentedString(clientData)).append("\n");
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

