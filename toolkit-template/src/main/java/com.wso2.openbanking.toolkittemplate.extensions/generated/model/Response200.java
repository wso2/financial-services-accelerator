package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("Response200")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class Response200   {
  private String responseId;
  public enum StatusEnum {

    ERROR(String.valueOf("ERROR"));


    private String value;

    StatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static StatusEnum fromString(String s) {
        for (StatusEnum b : StatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
        for (StatusEnum b : StatusEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private StatusEnum status;
  private Integer errorCode;
  private Object data;

  public Response200() {
  }

  @JsonCreator
  public Response200(
    @JsonProperty(required = true, value = "responseId") String responseId,
    @JsonProperty(required = true, value = "status") StatusEnum status,
    @JsonProperty(required = true, value = "errorCode") Integer errorCode,
    @JsonProperty(required = true, value = "data") Object data
  ) {
    this.responseId = responseId;
    this.status = status;
    this.errorCode = errorCode;
    this.data = data;
  }

  /**
   **/
  public Response200 responseId(String responseId) {
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
   * Indicates the outcome of the request. For a failed operation, this should be set to ERROR.
   **/
  public Response200 status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Indicates the outcome of the request. For a failed operation, this should be set to ERROR.")
  @JsonProperty(required = true, value = "status")
  @NotNull public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty(required = true, value = "status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * If any HTTP error code to return.
   **/
  public Response200 errorCode(Integer errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "If any HTTP error code to return.")
  @JsonProperty(required = true, value = "errorCode")
  @NotNull public Integer getErrorCode() {
    return errorCode;
  }

  @JsonProperty(required = true, value = "errorCode")
  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * :\&quot;Custom error object to response back\&quot;
   **/
  public Response200 data(Object data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(required = true, value = ":\"Custom error object to response back\"")
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
    Response200 response200 = (Response200) o;
    return Objects.equals(this.responseId, response200.responseId) &&
        Objects.equals(this.status, response200.status) &&
        Objects.equals(this.errorCode, response200.errorCode) &&
        Objects.equals(this.data, response200.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(responseId, status, errorCode, data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Response200 {\n");
    
    sb.append("    responseId: ").append(toIndentedString(responseId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

