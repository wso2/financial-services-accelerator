package org.openapitools.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.openapitools.model.StoredDetailedConsentResourceData;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("RequestForPreProcessFileUpload")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class RequestForPreProcessFileUpload   {
  private String consentId;
  private StoredDetailedConsentResourceData consentResource;
  private String fileContent;
  private String consentResourcePath;
  private Object requestHeaders;

  public RequestForPreProcessFileUpload() {
  }

  /**
   * To identify requested
   **/
  public RequestForPreProcessFileUpload consentId(String consentId) {
    this.consentId = consentId;
    return this;
  }

  
  @ApiModelProperty(value = "To identify requested")
  @JsonProperty("consentId")
  public String getConsentId() {
    return consentId;
  }

  @JsonProperty("consentId")
  public void setConsentId(String consentId) {
    this.consentId = consentId;
  }

  /**
   **/
  public RequestForPreProcessFileUpload consentResource(StoredDetailedConsentResourceData consentResource) {
    this.consentResource = consentResource;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("consentResource")
  @Valid public StoredDetailedConsentResourceData getConsentResource() {
    return consentResource;
  }

  @JsonProperty("consentResource")
  public void setConsentResource(StoredDetailedConsentResourceData consentResource) {
    this.consentResource = consentResource;
  }

  /**
   * content of the uploaded file
   **/
  public RequestForPreProcessFileUpload fileContent(String fileContent) {
    this.fileContent = fileContent;
    return this;
  }

  
  @ApiModelProperty(value = "content of the uploaded file")
  @JsonProperty("fileContent")
  public String getFileContent() {
    return fileContent;
  }

  @JsonProperty("fileContent")
  public void setFileContent(String fileContent) {
    this.fileContent = fileContent;
  }

  /**
   * consent resource path
   **/
  public RequestForPreProcessFileUpload consentResourcePath(String consentResourcePath) {
    this.consentResourcePath = consentResourcePath;
    return this;
  }

  
  @ApiModelProperty(value = "consent resource path")
  @JsonProperty("consentResourcePath")
  public String getConsentResourcePath() {
    return consentResourcePath;
  }

  @JsonProperty("consentResourcePath")
  public void setConsentResourcePath(String consentResourcePath) {
    this.consentResourcePath = consentResourcePath;
  }

  /**
   * Request headers sent by the TPP. Filtered set of headers are sent to the external service.
   **/
  public RequestForPreProcessFileUpload requestHeaders(Object requestHeaders) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestForPreProcessFileUpload requestForPreProcessFileUpload = (RequestForPreProcessFileUpload) o;
    return Objects.equals(this.consentId, requestForPreProcessFileUpload.consentId) &&
        Objects.equals(this.consentResource, requestForPreProcessFileUpload.consentResource) &&
        Objects.equals(this.fileContent, requestForPreProcessFileUpload.fileContent) &&
        Objects.equals(this.consentResourcePath, requestForPreProcessFileUpload.consentResourcePath) &&
        Objects.equals(this.requestHeaders, requestForPreProcessFileUpload.requestHeaders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentId, consentResource, fileContent, consentResourcePath, requestHeaders);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RequestForPreProcessFileUpload {\n");
    
    sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
    sb.append("    consentResource: ").append(toIndentedString(consentResource)).append("\n");
    sb.append("    fileContent: ").append(toIndentedString(fileContent)).append("\n");
    sb.append("    consentResourcePath: ").append(toIndentedString(consentResourcePath)).append("\n");
    sb.append("    requestHeaders: ").append(toIndentedString(requestHeaders)).append("\n");
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

