package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonTypeName("SuccessResponseApplicationUpdate_data")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponseApplicationUpdateData   {
  private Object additionalAppData;

  public SuccessResponseApplicationUpdateData() {
  }

  /**
   * Defines the additional properties to store against the application.
   **/
  public SuccessResponseApplicationUpdateData additionalAppData(Object additionalAppData) {
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
    SuccessResponseApplicationUpdateData successResponseApplicationUpdateData = (SuccessResponseApplicationUpdateData) o;
    return Objects.equals(this.additionalAppData, successResponseApplicationUpdateData.additionalAppData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(additionalAppData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponseApplicationUpdateData {\n");
    
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

