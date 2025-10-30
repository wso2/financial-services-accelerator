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



@JsonTypeName("SuccessResponseConsentRevocationData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponseConsentRevocationData   {
  private String revocationStatusName;
  private String requireTokenRevocation;

  public SuccessResponseConsentRevocationData() {
  }

  /**
   * Name for the revoked status
   **/
  public SuccessResponseConsentRevocationData revocationStatusName(String revocationStatusName) {
    this.revocationStatusName = revocationStatusName;
    return this;
  }

  
  @ApiModelProperty(value = "Name for the revoked status")
  @JsonProperty("revocationStatusName")
  public String getRevocationStatusName() {
    return revocationStatusName;
  }

  @JsonProperty("revocationStatusName")
  public void setRevocationStatusName(String revocationStatusName) {
    this.revocationStatusName = revocationStatusName;
  }

  /**
   * Require access token to be revoked
   **/
  public SuccessResponseConsentRevocationData requireTokenRevocation(String requireTokenRevocation) {
    this.requireTokenRevocation = requireTokenRevocation;
    return this;
  }

  
  @ApiModelProperty(value = "Require access token to be revoked")
  @JsonProperty("requireTokenRevocation")
  public String getRequireTokenRevocation() {
    return requireTokenRevocation;
  }

  @JsonProperty("requireTokenRevocation")
  public void setRequireTokenRevocation(String requireTokenRevocation) {
    this.requireTokenRevocation = requireTokenRevocation;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponseConsentRevocationData successResponseConsentRevocationData = (SuccessResponseConsentRevocationData) o;
    return Objects.equals(this.revocationStatusName, successResponseConsentRevocationData.revocationStatusName) &&
        Objects.equals(this.requireTokenRevocation, successResponseConsentRevocationData.requireTokenRevocation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revocationStatusName, requireTokenRevocation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponseConsentRevocationData {\n");
    
    sb.append("    revocationStatusName: ").append(toIndentedString(revocationStatusName)).append("\n");
    sb.append("    requireTokenRevocation: ").append(toIndentedString(requireTokenRevocation)).append("\n");
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

