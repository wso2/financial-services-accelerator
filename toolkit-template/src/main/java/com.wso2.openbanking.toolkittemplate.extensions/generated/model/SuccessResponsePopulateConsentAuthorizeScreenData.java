package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("SuccessResponsePopulateConsentAuthorizeScreenData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponsePopulateConsentAuthorizeScreenData   {
  private SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData;
  private SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData;

  public SuccessResponsePopulateConsentAuthorizeScreenData() {
  }

  /**
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenData consentData(SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData) {
    this.consentData = consentData;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("consentData")
  @Valid public SuccessResponsePopulateConsentAuthorizeScreenDataConsentData getConsentData() {
    return consentData;
  }

  @JsonProperty("consentData")
  public void setConsentData(SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData) {
    this.consentData = consentData;
  }

  /**
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenData consumerData(SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData) {
    this.consumerData = consumerData;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("consumerData")
  @Valid public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData getConsumerData() {
    return consumerData;
  }

  @JsonProperty("consumerData")
  public void setConsumerData(SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData) {
    this.consumerData = consumerData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponsePopulateConsentAuthorizeScreenData successResponsePopulateConsentAuthorizeScreenData = (SuccessResponsePopulateConsentAuthorizeScreenData) o;
    return Objects.equals(this.consentData, successResponsePopulateConsentAuthorizeScreenData.consentData) &&
        Objects.equals(this.consumerData, successResponsePopulateConsentAuthorizeScreenData.consumerData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentData, consumerData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponsePopulateConsentAuthorizeScreenData {\n");
    
    sb.append("    consentData: ").append(toIndentedString(consentData)).append("\n");
    sb.append("    consumerData: ").append(toIndentedString(consumerData)).append("\n");
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

