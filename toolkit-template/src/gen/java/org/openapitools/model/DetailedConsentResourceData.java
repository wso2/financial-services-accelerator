package org.openapitools.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.model.Authorization;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("DetailedConsentResourceData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class DetailedConsentResourceData   {
  private String type;
  private String status;
  private Long validityTime;
  private Boolean recurringIndicator;
  private Integer frequency;
  private Object receipt;
  private Object attributes;
  private @Valid List<@Valid Authorization> authorizations = new ArrayList<>();

  public DetailedConsentResourceData() {
  }

  /**
   **/
  public DetailedConsentResourceData type(String type) {
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
  public DetailedConsentResourceData status(String status) {
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
  public DetailedConsentResourceData validityTime(Long validityTime) {
    this.validityTime = validityTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("validityTime")
  public Long getValidityTime() {
    return validityTime;
  }

  @JsonProperty("validityTime")
  public void setValidityTime(Long validityTime) {
    this.validityTime = validityTime;
  }

  /**
   **/
  public DetailedConsentResourceData recurringIndicator(Boolean recurringIndicator) {
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
  public DetailedConsentResourceData frequency(Integer frequency) {
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
  public DetailedConsentResourceData receipt(Object receipt) {
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
  public DetailedConsentResourceData attributes(Object attributes) {
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
  public DetailedConsentResourceData authorizations(List<@Valid Authorization> authorizations) {
    this.authorizations = authorizations;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authorizations")
  @Valid public List<@Valid Authorization> getAuthorizations() {
    return authorizations;
  }

  @JsonProperty("authorizations")
  public void setAuthorizations(List<@Valid Authorization> authorizations) {
    this.authorizations = authorizations;
  }

  public DetailedConsentResourceData addAuthorizationsItem(Authorization authorizationsItem) {
    if (this.authorizations == null) {
      this.authorizations = new ArrayList<>();
    }

    this.authorizations.add(authorizationsItem);
    return this;
  }

  public DetailedConsentResourceData removeAuthorizationsItem(Authorization authorizationsItem) {
    if (authorizationsItem != null && this.authorizations != null) {
      this.authorizations.remove(authorizationsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DetailedConsentResourceData detailedConsentResourceData = (DetailedConsentResourceData) o;
    return Objects.equals(this.type, detailedConsentResourceData.type) &&
        Objects.equals(this.status, detailedConsentResourceData.status) &&
        Objects.equals(this.validityTime, detailedConsentResourceData.validityTime) &&
        Objects.equals(this.recurringIndicator, detailedConsentResourceData.recurringIndicator) &&
        Objects.equals(this.frequency, detailedConsentResourceData.frequency) &&
        Objects.equals(this.receipt, detailedConsentResourceData.receipt) &&
        Objects.equals(this.attributes, detailedConsentResourceData.attributes) &&
        Objects.equals(this.authorizations, detailedConsentResourceData.authorizations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, status, validityTime, recurringIndicator, frequency, receipt, attributes, authorizations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DetailedConsentResourceData {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
    sb.append("    recurringIndicator: ").append(toIndentedString(recurringIndicator)).append("\n");
    sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
    sb.append("    receipt: ").append(toIndentedString(receipt)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    authorizations: ").append(toIndentedString(authorizations)).append("\n");
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

