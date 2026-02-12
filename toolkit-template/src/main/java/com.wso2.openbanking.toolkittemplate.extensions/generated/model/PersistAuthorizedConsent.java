package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("PersistAuthorizedConsent")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class PersistAuthorizedConsent   {
  private String consentId;
  private Boolean isApproved;
  private UserGrantedData userGrantedData;
  private StoredDetailedConsentResourceData consentResource;

  public PersistAuthorizedConsent() {
  }

  /**
   **/
  public PersistAuthorizedConsent consentId(String consentId) {
    this.consentId = consentId;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public PersistAuthorizedConsent isApproved(Boolean isApproved) {
    this.isApproved = isApproved;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("isApproved")
  public Boolean getIsApproved() {
    return isApproved;
  }

  @JsonProperty("isApproved")
  public void setIsApproved(Boolean isApproved) {
    this.isApproved = isApproved;
  }

  /**
   **/
  public PersistAuthorizedConsent userGrantedData(UserGrantedData userGrantedData) {
    this.userGrantedData = userGrantedData;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userGrantedData")
  @Valid public UserGrantedData getUserGrantedData() {
    return userGrantedData;
  }

  @JsonProperty("userGrantedData")
  public void setUserGrantedData(UserGrantedData userGrantedData) {
    this.userGrantedData = userGrantedData;
  }

  /**
   **/
  public PersistAuthorizedConsent consentResource(StoredDetailedConsentResourceData consentResource) {
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
    PersistAuthorizedConsent persistAuthorizedConsent = (PersistAuthorizedConsent) o;
    return Objects.equals(this.consentId, persistAuthorizedConsent.consentId) &&
        Objects.equals(this.isApproved, persistAuthorizedConsent.isApproved) &&
        Objects.equals(this.userGrantedData, persistAuthorizedConsent.userGrantedData) &&
        Objects.equals(this.consentResource, persistAuthorizedConsent.consentResource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentId, isApproved, userGrantedData, consentResource);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PersistAuthorizedConsent {\n");
    
    sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
    sb.append("    isApproved: ").append(toIndentedString(isApproved)).append("\n");
    sb.append("    userGrantedData: ").append(toIndentedString(userGrantedData)).append("\n");
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

