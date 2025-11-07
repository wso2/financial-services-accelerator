package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("Response200ForErrorMapper")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class Response200ForErrorMapper   {
  private String responseId;
  private Integer errorCode;
  private Object data;

  public Response200ForErrorMapper() {
  }

  @JsonCreator
  public Response200ForErrorMapper(
    @JsonProperty(required = true, value = "responseId") String responseId,
    @JsonProperty(required = true, value = "data") Object data
  ) {
    this.responseId = responseId;
    this.data = data;
  }

  /**
   **/
  public Response200ForErrorMapper responseId(String responseId) {
    this.responseId = responseId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "responseId")
  @NotNull public String getResponseId() {
    return responseId;
  }

  @JsonProperty(required = true, value = "responseId")
  public void setResponseId(String responseId) {
    this.responseId = responseId;
  }

  /**
   **/
  public Response200ForErrorMapper errorCode(Integer errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("errorCode")
  public Integer getErrorCode() {
    return errorCode;
  }

  @JsonProperty("errorCode")
  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * Defines the custom error response.
   **/
  public Response200ForErrorMapper data(Object data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Defines the custom error response.")
  @JsonProperty(required = true, value = "data")
  @NotNull public Object getData() {
    return data;
  }

  @JsonProperty(required = true, value = "data")
  public void setData(Object data) {
    this.data = data;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Response200ForErrorMapper response200ForErrorMapper = (Response200ForErrorMapper) o;
    return Objects.equals(this.responseId, response200ForErrorMapper.responseId) &&
        Objects.equals(this.errorCode, response200ForErrorMapper.errorCode) &&
        Objects.equals(this.data, response200ForErrorMapper.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(responseId, errorCode, data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Response200ForErrorMapper {\n");
    
    sb.append("    responseId: ").append(toIndentedString(responseId)).append("\n");
    sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

