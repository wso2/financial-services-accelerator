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



@JsonTypeName("SuccessResponseApplicationCreation_data")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponseApplicationCreationData   {
  private String clientId;
  private Object additionalAppData;

  public SuccessResponseApplicationCreationData() {
  }

  /**
   * Unique id to be stored as the clientId
   **/
  public SuccessResponseApplicationCreationData clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  
  @ApiModelProperty(value = "Unique id to be stored as the clientId")
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * Defines the additional properties to store against the application.
   **/
  public SuccessResponseApplicationCreationData additionalAppData(Object additionalAppData) {
    this.additionalAppData = additionalAppData;
    return this;
  }

  
  @ApiModelProperty(value = "Defines the additional properties to store against the application.")
  @JsonProperty("additionalAppData")
  public Object getAdditionalAppData() {
    return additionalAppData;
  }

  @JsonProperty("additionalAppData")
  public void setAdditionalAppData(Object additionalAppData) {
    this.additionalAppData = additionalAppData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponseApplicationCreationData successResponseApplicationCreationData = (SuccessResponseApplicationCreationData) o;
    return Objects.equals(this.clientId, successResponseApplicationCreationData.clientId) &&
        Objects.equals(this.additionalAppData, successResponseApplicationCreationData.additionalAppData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, additionalAppData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponseApplicationCreationData {\n");
    
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    additionalAppData: ").append(toIndentedString(additionalAppData)).append("\n");
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

