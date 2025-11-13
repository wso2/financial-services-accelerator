package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonTypeName("FailedResponseClientProcess_data")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class FailedResponseClientProcessData   {
  public enum ErrorEnum {

    INVALID_CLIENT_METADATA(String.valueOf("invalid_client_metadata")), INVALID_REDIRECT_URI(String.valueOf("invalid_redirect_uri")), INVALID_SOFTWARE_STATEMENT(String.valueOf("invalid_software_statement"));


    private String value;

    ErrorEnum (String v) {
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
    public static ErrorEnum fromString(String s) {
        for (ErrorEnum b : ErrorEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static ErrorEnum fromValue(String value) {
        for (ErrorEnum b : ErrorEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private ErrorEnum error;
  private String errorDescription;

  public FailedResponseClientProcessData() {
  }

  /**
   * Provides the error code for error.
   **/
  public FailedResponseClientProcessData error(ErrorEnum error) {
    this.error = error;
    return this;
  }

  
  @ApiModelProperty(value = "Provides the error code for error.")
  @JsonProperty("error")
  public ErrorEnum getError() {
    return error;
  }

  @JsonProperty("error")
  public void setError(ErrorEnum error) {
    this.error = error;
  }

  /**
   * Offers a detailed explanation of the error.
   **/
  public FailedResponseClientProcessData errorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
    return this;
  }

  
  @ApiModelProperty(value = "Offers a detailed explanation of the error.")
  @JsonProperty("errorDescription")
  public String getErrorDescription() {
    return errorDescription;
  }

  @JsonProperty("errorDescription")
  public void setErrorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FailedResponseClientProcessData failedResponseClientProcessData = (FailedResponseClientProcessData) o;
    return Objects.equals(this.error, failedResponseClientProcessData.error) &&
        Objects.equals(this.errorDescription, failedResponseClientProcessData.errorDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(error, errorDescription);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FailedResponseClientProcessData {\n");
    
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    errorDescription: ").append(toIndentedString(errorDescription)).append("\n");
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

