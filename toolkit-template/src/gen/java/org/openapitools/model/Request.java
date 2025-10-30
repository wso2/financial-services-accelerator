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



@JsonTypeName("Request")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class Request   {
  private Object consentInitiationData;
  private Object requestHeaders;
  private String consentResourcePath;

  public Request() {
  }

  /**
   * The initiation payload used by third parties which includes detailed information on data access request.
   **/
  public Request consentInitiationData(Object consentInitiationData) {
    this.consentInitiationData = consentInitiationData;
    return this;
  }

  
  @ApiModelProperty(value = "The initiation payload used by third parties which includes detailed information on data access request.")
  @JsonProperty("consentInitiationData")
  public Object getConsentInitiationData() {
    return consentInitiationData;
  }

  @JsonProperty("consentInitiationData")
  public void setConsentInitiationData(Object consentInitiationData) {
    this.consentInitiationData = consentInitiationData;
  }

  /**
   * Request headers sent by the TPP. Filtered set of headers are sent to the external service.
   **/
  public Request requestHeaders(Object requestHeaders) {
    this.requestHeaders = requestHeaders;
    return this;
  }

  
  @ApiModelProperty(value = "Request headers sent by the TPP. Filtered set of headers are sent to the external service.")
  @JsonProperty("requestHeaders")
  public Object getRequestHeaders() {
    return requestHeaders;
  }

  @JsonProperty("requestHeaders")
  public void setRequestHeaders(Object requestHeaders) {
    this.requestHeaders = requestHeaders;
  }

  /**
   * To identify requested consent type
   **/
  public Request consentResourcePath(String consentResourcePath) {
    this.consentResourcePath = consentResourcePath;
    return this;
  }

  
  @ApiModelProperty(value = "To identify requested consent type")
  @JsonProperty("consentResourcePath")
  public String getConsentResourcePath() {
    return consentResourcePath;
  }

  @JsonProperty("consentResourcePath")
  public void setConsentResourcePath(String consentResourcePath) {
    this.consentResourcePath = consentResourcePath;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Request request = (Request) o;
    return Objects.equals(this.consentInitiationData, request.consentInitiationData) &&
        Objects.equals(this.requestHeaders, request.requestHeaders) &&
        Objects.equals(this.consentResourcePath, request.consentResourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentInitiationData, requestHeaders, consentResourcePath);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Request {\n");
    
    sb.append("    consentInitiationData: ").append(toIndentedString(consentInitiationData)).append("\n");
    sb.append("    requestHeaders: ").append(toIndentedString(requestHeaders)).append("\n");
    sb.append("    consentResourcePath: ").append(toIndentedString(consentResourcePath)).append("\n");
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

