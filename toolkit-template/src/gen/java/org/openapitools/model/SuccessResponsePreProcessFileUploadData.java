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



@JsonTypeName("SuccessResponsePreProcessFileUploadData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponsePreProcessFileUploadData   {
  private String consentStatus;
  private String userId;

  public SuccessResponsePreProcessFileUploadData() {
  }

  /**
   * New consent status after the file upload is successful.
   **/
  public SuccessResponsePreProcessFileUploadData consentStatus(String consentStatus) {
    this.consentStatus = consentStatus;
    return this;
  }

  
  @ApiModelProperty(value = "New consent status after the file upload is successful.")
  @JsonProperty("consentStatus")
  public String getConsentStatus() {
    return consentStatus;
  }

  @JsonProperty("consentStatus")
  public void setConsentStatus(String consentStatus) {
    this.consentStatus = consentStatus;
  }

  /**
   * Id of the user doing the file upload. Used for auditing purposes.
   **/
  public SuccessResponsePreProcessFileUploadData userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(value = "Id of the user doing the file upload. Used for auditing purposes.")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponsePreProcessFileUploadData successResponsePreProcessFileUploadData = (SuccessResponsePreProcessFileUploadData) o;
    return Objects.equals(this.consentStatus, successResponsePreProcessFileUploadData.consentStatus) &&
        Objects.equals(this.userId, successResponsePreProcessFileUploadData.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentStatus, userId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponsePreProcessFileUploadData {\n");
    
    sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
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

