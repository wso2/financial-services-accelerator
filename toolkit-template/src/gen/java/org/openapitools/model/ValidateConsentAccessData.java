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



@JsonTypeName("ValidateConsentAccessData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class ValidateConsentAccessData   {
  private String consentId;
  private StoredDetailedConsentResourceData consentResource;
  private Object dataRequestPayload;

  public ValidateConsentAccessData() {
  }

  /**
   * The consent id
   **/
  public ValidateConsentAccessData consentId(String consentId) {
    this.consentId = consentId;
    return this;
  }

  
  @ApiModelProperty(value = "The consent id")
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
  public ValidateConsentAccessData consentResource(StoredDetailedConsentResourceData consentResource) {
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
   * The receipt used by Third parties which includes detailed information on data access request
   **/
  public ValidateConsentAccessData dataRequestPayload(Object dataRequestPayload) {
    this.dataRequestPayload = dataRequestPayload;
    return this;
  }

  
  @ApiModelProperty(value = "The receipt used by Third parties which includes detailed information on data access request")
  @JsonProperty("dataRequestPayload")
  public Object getDataRequestPayload() {
    return dataRequestPayload;
  }

  @JsonProperty("dataRequestPayload")
  public void setDataRequestPayload(Object dataRequestPayload) {
    this.dataRequestPayload = dataRequestPayload;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidateConsentAccessData validateConsentAccessData = (ValidateConsentAccessData) o;
    return Objects.equals(this.consentId, validateConsentAccessData.consentId) &&
        Objects.equals(this.consentResource, validateConsentAccessData.consentResource) &&
        Objects.equals(this.dataRequestPayload, validateConsentAccessData.dataRequestPayload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentId, consentResource, dataRequestPayload);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ValidateConsentAccessData {\n");
    
    sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
    sb.append("    consentResource: ").append(toIndentedString(consentResource)).append("\n");
    sb.append("    dataRequestPayload: ").append(toIndentedString(dataRequestPayload)).append("\n");
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

