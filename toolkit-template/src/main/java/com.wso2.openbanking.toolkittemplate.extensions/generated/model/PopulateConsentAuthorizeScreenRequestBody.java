package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("PopulateConsentAuthorizeScreenRequestBody")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class PopulateConsentAuthorizeScreenRequestBody   {
  private String requestId;
  private PopulateConsentAuthorizeScreenData data;

  public PopulateConsentAuthorizeScreenRequestBody() {
  }

  @JsonCreator
  public PopulateConsentAuthorizeScreenRequestBody(
    @JsonProperty(required = true, value = "requestId") String requestId,
    @JsonProperty(required = true, value = "data") PopulateConsentAuthorizeScreenData data
  ) {
    this.requestId = requestId;
    this.data = data;
  }

  /**
   **/
  public PopulateConsentAuthorizeScreenRequestBody requestId(String requestId) {
    this.requestId = requestId;
    return this;
  }

  
  @ApiModelProperty(example = "Ec1wMjmiG8", required = true, value = "")
  @JsonProperty(required = true, value = "requestId")
  @NotNull public String getRequestId() {
    return requestId;
  }

  @JsonProperty(required = true, value = "requestId")
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  /**
   **/
  public PopulateConsentAuthorizeScreenRequestBody data(PopulateConsentAuthorizeScreenData data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "data")
  @NotNull @Valid public PopulateConsentAuthorizeScreenData getData() {
    return data;
  }

  @JsonProperty(required = true, value = "data")
  public void setData(PopulateConsentAuthorizeScreenData data) {
    this.data = data;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PopulateConsentAuthorizeScreenRequestBody populateConsentAuthorizeScreenRequestBody = (PopulateConsentAuthorizeScreenRequestBody) o;
    return Objects.equals(this.requestId, populateConsentAuthorizeScreenRequestBody.requestId) &&
        Objects.equals(this.data, populateConsentAuthorizeScreenRequestBody.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PopulateConsentAuthorizeScreenRequestBody {\n");
    
    sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

