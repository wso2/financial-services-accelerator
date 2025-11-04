package org.openapitools.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.model.StoredAuthorization;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("StoredDetailedConsentResourceData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class StoredDetailedConsentResourceData   {
  private String id;
  private Object receipt;
  private Integer createdTime;
  private Integer updatedTime;
  private String clientId;
  private String type;
  private String status;
  private Integer frequency;
  private Integer validityTime;
  private Boolean recurringIndicator;
  private Object attributes;
  private @Valid List<@Valid StoredAuthorization> authorizations = new ArrayList<>();
  private String fileContent;

  public StoredDetailedConsentResourceData() {
  }

  /**
   **/
  public StoredDetailedConsentResourceData id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public StoredDetailedConsentResourceData receipt(Object receipt) {
    this.receipt = receipt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("receipt")
  public Object getReceipt() {
    return receipt;
  }

  @JsonProperty("receipt")
  public void setReceipt(Object receipt) {
    this.receipt = receipt;
  }

  /**
   **/
  public StoredDetailedConsentResourceData createdTime(Integer createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdTime")
  public Integer getCreatedTime() {
    return createdTime;
  }

  @JsonProperty("createdTime")
  public void setCreatedTime(Integer createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public StoredDetailedConsentResourceData updatedTime(Integer updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("updatedTime")
  public Integer getUpdatedTime() {
    return updatedTime;
  }

  @JsonProperty("updatedTime")
  public void setUpdatedTime(Integer updatedTime) {
    this.updatedTime = updatedTime;
  }

  /**
   **/
  public StoredDetailedConsentResourceData clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   **/
  public StoredDetailedConsentResourceData type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public StoredDetailedConsentResourceData status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public StoredDetailedConsentResourceData frequency(Integer frequency) {
    this.frequency = frequency;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("frequency")
  public Integer getFrequency() {
    return frequency;
  }

  @JsonProperty("frequency")
  public void setFrequency(Integer frequency) {
    this.frequency = frequency;
  }

  /**
   **/
  public StoredDetailedConsentResourceData validityTime(Integer validityTime) {
    this.validityTime = validityTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("validityTime")
  public Integer getValidityTime() {
    return validityTime;
  }

  @JsonProperty("validityTime")
  public void setValidityTime(Integer validityTime) {
    this.validityTime = validityTime;
  }

  /**
   **/
  public StoredDetailedConsentResourceData recurringIndicator(Boolean recurringIndicator) {
    this.recurringIndicator = recurringIndicator;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("recurringIndicator")
  public Boolean getRecurringIndicator() {
    return recurringIndicator;
  }

  @JsonProperty("recurringIndicator")
  public void setRecurringIndicator(Boolean recurringIndicator) {
    this.recurringIndicator = recurringIndicator;
  }

  /**
   **/
  public StoredDetailedConsentResourceData attributes(Object attributes) {
    this.attributes = attributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("attributes")
  public Object getAttributes() {
    return attributes;
  }

  @JsonProperty("attributes")
  public void setAttributes(Object attributes) {
    this.attributes = attributes;
  }

  /**
   **/
  public StoredDetailedConsentResourceData authorizations(List<@Valid StoredAuthorization> authorizations) {
    this.authorizations = authorizations;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authorizations")
  @Valid public List<@Valid StoredAuthorization> getAuthorizations() {
    return authorizations;
  }

  @JsonProperty("authorizations")
  public void setAuthorizations(List<@Valid StoredAuthorization> authorizations) {
    this.authorizations = authorizations;
  }

  public StoredDetailedConsentResourceData addAuthorizationsItem(StoredAuthorization authorizationsItem) {
    if (this.authorizations == null) {
      this.authorizations = new ArrayList<>();
    }

    this.authorizations.add(authorizationsItem);
    return this;
  }

  public StoredDetailedConsentResourceData removeAuthorizationsItem(StoredAuthorization authorizationsItem) {
    if (authorizationsItem != null && this.authorizations != null) {
      this.authorizations.remove(authorizationsItem);
    }

    return this;
  }
  /**
   **/
  public StoredDetailedConsentResourceData fileContent(String fileContent) {
    this.fileContent = fileContent;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("fileContent")
  public String getFileContent() {
    return fileContent;
  }

  @JsonProperty("fileContent")
  public void setFileContent(String fileContent) {
    this.fileContent = fileContent;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StoredDetailedConsentResourceData storedDetailedConsentResourceData = (StoredDetailedConsentResourceData) o;
    return Objects.equals(this.id, storedDetailedConsentResourceData.id) &&
        Objects.equals(this.receipt, storedDetailedConsentResourceData.receipt) &&
        Objects.equals(this.createdTime, storedDetailedConsentResourceData.createdTime) &&
        Objects.equals(this.updatedTime, storedDetailedConsentResourceData.updatedTime) &&
        Objects.equals(this.clientId, storedDetailedConsentResourceData.clientId) &&
        Objects.equals(this.type, storedDetailedConsentResourceData.type) &&
        Objects.equals(this.status, storedDetailedConsentResourceData.status) &&
        Objects.equals(this.frequency, storedDetailedConsentResourceData.frequency) &&
        Objects.equals(this.validityTime, storedDetailedConsentResourceData.validityTime) &&
        Objects.equals(this.recurringIndicator, storedDetailedConsentResourceData.recurringIndicator) &&
        Objects.equals(this.attributes, storedDetailedConsentResourceData.attributes) &&
        Objects.equals(this.authorizations, storedDetailedConsentResourceData.authorizations) &&
        Objects.equals(this.fileContent, storedDetailedConsentResourceData.fileContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, receipt, createdTime, updatedTime, clientId, type, status, frequency, validityTime, recurringIndicator, attributes, authorizations, fileContent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StoredDetailedConsentResourceData {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    receipt: ").append(toIndentedString(receipt)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
    sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
    sb.append("    recurringIndicator: ").append(toIndentedString(recurringIndicator)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    authorizations: ").append(toIndentedString(authorizations)).append("\n");
    sb.append("    fileContent: ").append(toIndentedString(fileContent)).append("\n");
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

