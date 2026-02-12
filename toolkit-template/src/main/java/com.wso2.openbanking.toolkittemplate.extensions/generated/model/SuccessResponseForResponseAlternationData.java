package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("SuccessResponseForResponseAlternationData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponseForResponseAlternationData   {
  private Object responseHeaders;
  private Object modifiedResponse;

  public SuccessResponseForResponseAlternationData() {
  }

  /**
   * Headers to be included in the response.
   **/
  public SuccessResponseForResponseAlternationData responseHeaders(Object responseHeaders) {
    this.responseHeaders = responseHeaders;
    return this;
  }

  
  @ApiModelProperty(value = "Headers to be included in the response.")
  @JsonProperty("responseHeaders")
  public Object getResponseHeaders() {
    return responseHeaders;
  }

  @JsonProperty("responseHeaders")
  public void setResponseHeaders(Object responseHeaders) {
    this.responseHeaders = responseHeaders;
  }

  /**
   * Generated custom response body
   **/
  public SuccessResponseForResponseAlternationData modifiedResponse(Object modifiedResponse) {
    this.modifiedResponse = modifiedResponse;
    return this;
  }

  
  @ApiModelProperty(value = "Generated custom response body")
  @JsonProperty("modifiedResponse")
  public Object getModifiedResponse() {
    return modifiedResponse;
  }

  @JsonProperty("modifiedResponse")
  public void setModifiedResponse(Object modifiedResponse) {
    this.modifiedResponse = modifiedResponse;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponseForResponseAlternationData successResponseForResponseAlternationData = (SuccessResponseForResponseAlternationData) o;
    return Objects.equals(this.responseHeaders, successResponseForResponseAlternationData.responseHeaders) &&
        Objects.equals(this.modifiedResponse, successResponseForResponseAlternationData.modifiedResponse);
  }

  @Override
  public int hashCode() {
    return Objects.hash(responseHeaders, modifiedResponse);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponseForResponseAlternationData {\n");
    
    sb.append("    responseHeaders: ").append(toIndentedString(responseHeaders)).append("\n");
    sb.append("    modifiedResponse: ").append(toIndentedString(modifiedResponse)).append("\n");
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

