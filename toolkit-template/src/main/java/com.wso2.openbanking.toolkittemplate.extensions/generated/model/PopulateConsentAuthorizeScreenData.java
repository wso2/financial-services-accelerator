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



@JsonTypeName("PopulateConsentAuthorizeScreenData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class PopulateConsentAuthorizeScreenData   {
  private String consentId;
  private String userId;
  private Object requestParameters;
  private StoredDetailedConsentResourceData consentResource;

  public PopulateConsentAuthorizeScreenData() {
  }

  /**
   **/
  public PopulateConsentAuthorizeScreenData consentId(String consentId) {
    this.consentId = consentId;
    return this;
  }

  
  @ApiModelProperty(example = "An UUID", value = "")
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
  public PopulateConsentAuthorizeScreenData userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(example = "Username", value = "")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Custom object with request parameters
   **/
  public PopulateConsentAuthorizeScreenData requestParameters(Object requestParameters) {
    this.requestParameters = requestParameters;
    return this;
  }

  
  @ApiModelProperty(value = "Custom object with request parameters")
  @JsonProperty("requestParameters")
  public Object getRequestParameters() {
    return requestParameters;
  }

  @JsonProperty("requestParameters")
  public void setRequestParameters(Object requestParameters) {
    this.requestParameters = requestParameters;
  }

  /**
   **/
  public PopulateConsentAuthorizeScreenData consentResource(StoredDetailedConsentResourceData consentResource) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PopulateConsentAuthorizeScreenData populateConsentAuthorizeScreenData = (PopulateConsentAuthorizeScreenData) o;
    return Objects.equals(this.consentId, populateConsentAuthorizeScreenData.consentId) &&
        Objects.equals(this.userId, populateConsentAuthorizeScreenData.userId) &&
        Objects.equals(this.requestParameters, populateConsentAuthorizeScreenData.requestParameters) &&
        Objects.equals(this.consentResource, populateConsentAuthorizeScreenData.consentResource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentId, userId, requestParameters, consentResource);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PopulateConsentAuthorizeScreenData {\n");
    
    sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    requestParameters: ").append(toIndentedString(requestParameters)).append("\n");
    sb.append("    consentResource: ").append(toIndentedString(consentResource)).append("\n");
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

