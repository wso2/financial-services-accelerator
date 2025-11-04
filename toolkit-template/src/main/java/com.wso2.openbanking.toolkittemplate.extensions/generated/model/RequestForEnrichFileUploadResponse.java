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



@JsonTypeName("RequestForEnrichFileUploadResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class RequestForEnrichFileUploadResponse   {
  private String consentId;
  private String fileUploadCreatedTime;

  public RequestForEnrichFileUploadResponse() {
  }

  /**
   * To identify consent.
   **/
  public RequestForEnrichFileUploadResponse consentId(String consentId) {
    this.consentId = consentId;
    return this;
  }

  
  @ApiModelProperty(value = "To identify consent.")
  @JsonProperty("consentId")
  public String getConsentId() {
    return consentId;
  }

  @JsonProperty("consentId")
  public void setConsentId(String consentId) {
    this.consentId = consentId;
  }

  /**
   * Timestamp which the file was stored in the database.
   **/
  public RequestForEnrichFileUploadResponse fileUploadCreatedTime(String fileUploadCreatedTime) {
    this.fileUploadCreatedTime = fileUploadCreatedTime;
    return this;
  }

  
  @ApiModelProperty(value = "Timestamp which the file was stored in the database.")
  @JsonProperty("fileUploadCreatedTime")
  public String getFileUploadCreatedTime() {
    return fileUploadCreatedTime;
  }

  @JsonProperty("fileUploadCreatedTime")
  public void setFileUploadCreatedTime(String fileUploadCreatedTime) {
    this.fileUploadCreatedTime = fileUploadCreatedTime;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestForEnrichFileUploadResponse requestForEnrichFileUploadResponse = (RequestForEnrichFileUploadResponse) o;
    return Objects.equals(this.consentId, requestForEnrichFileUploadResponse.consentId) &&
        Objects.equals(this.fileUploadCreatedTime, requestForEnrichFileUploadResponse.fileUploadCreatedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentId, fileUploadCreatedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RequestForEnrichFileUploadResponse {\n");
    
    sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
    sb.append("    fileUploadCreatedTime: ").append(toIndentedString(fileUploadCreatedTime)).append("\n");
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

