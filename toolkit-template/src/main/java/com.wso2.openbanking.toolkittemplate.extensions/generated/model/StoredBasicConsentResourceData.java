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



@JsonTypeName("StoredBasicConsentResourceData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class StoredBasicConsentResourceData   {
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

  public StoredBasicConsentResourceData() {
  }

  /**
   **/
  public StoredBasicConsentResourceData id(String id) {
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
  public StoredBasicConsentResourceData receipt(Object receipt) {
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
  public StoredBasicConsentResourceData createdTime(Integer createdTime) {
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
  public StoredBasicConsentResourceData updatedTime(Integer updatedTime) {
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
  public StoredBasicConsentResourceData clientId(String clientId) {
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
  public StoredBasicConsentResourceData type(String type) {
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
  public StoredBasicConsentResourceData status(String status) {
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
  public StoredBasicConsentResourceData frequency(Integer frequency) {
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
  public StoredBasicConsentResourceData validityTime(Integer validityTime) {
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
  public StoredBasicConsentResourceData recurringIndicator(Boolean recurringIndicator) {
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
  public StoredBasicConsentResourceData attributes(Object attributes) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StoredBasicConsentResourceData storedBasicConsentResourceData = (StoredBasicConsentResourceData) o;
    return Objects.equals(this.id, storedBasicConsentResourceData.id) &&
        Objects.equals(this.receipt, storedBasicConsentResourceData.receipt) &&
        Objects.equals(this.createdTime, storedBasicConsentResourceData.createdTime) &&
        Objects.equals(this.updatedTime, storedBasicConsentResourceData.updatedTime) &&
        Objects.equals(this.clientId, storedBasicConsentResourceData.clientId) &&
        Objects.equals(this.type, storedBasicConsentResourceData.type) &&
        Objects.equals(this.status, storedBasicConsentResourceData.status) &&
        Objects.equals(this.frequency, storedBasicConsentResourceData.frequency) &&
        Objects.equals(this.validityTime, storedBasicConsentResourceData.validityTime) &&
        Objects.equals(this.recurringIndicator, storedBasicConsentResourceData.recurringIndicator) &&
        Objects.equals(this.attributes, storedBasicConsentResourceData.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, receipt, createdTime, updatedTime, clientId, type, status, frequency, validityTime, recurringIndicator, attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StoredBasicConsentResourceData {\n");
    
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

