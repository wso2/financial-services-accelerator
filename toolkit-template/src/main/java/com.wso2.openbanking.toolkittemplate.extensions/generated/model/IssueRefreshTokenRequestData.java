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



@JsonTypeName("IssueRefreshTokenRequestData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class IssueRefreshTokenRequestData   {
  private String grantType;
  private Long consentCreatedTime;
  private Long consentValidityPeriod;
  private Long defaultRefreshTokenValidityPeriod;

  public IssueRefreshTokenRequestData() {
  }

  /**
   **/
  public IssueRefreshTokenRequestData grantType(String grantType) {
    this.grantType = grantType;
    return this;
  }

  
  @ApiModelProperty(example = "authorization_code", value = "")
  @JsonProperty("grantType")
  public String getGrantType() {
    return grantType;
  }

  @JsonProperty("grantType")
  public void setGrantType(String grantType) {
    this.grantType = grantType;
  }

  /**
   **/
  public IssueRefreshTokenRequestData consentCreatedTime(Long consentCreatedTime) {
    this.consentCreatedTime = consentCreatedTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("consentCreatedTime")
  public Long getConsentCreatedTime() {
    return consentCreatedTime;
  }

  @JsonProperty("consentCreatedTime")
  public void setConsentCreatedTime(Long consentCreatedTime) {
    this.consentCreatedTime = consentCreatedTime;
  }

  /**
   **/
  public IssueRefreshTokenRequestData consentValidityPeriod(Long consentValidityPeriod) {
    this.consentValidityPeriod = consentValidityPeriod;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("consentValidityPeriod")
  public Long getConsentValidityPeriod() {
    return consentValidityPeriod;
  }

  @JsonProperty("consentValidityPeriod")
  public void setConsentValidityPeriod(Long consentValidityPeriod) {
    this.consentValidityPeriod = consentValidityPeriod;
  }

  /**
   **/
  public IssueRefreshTokenRequestData defaultRefreshTokenValidityPeriod(Long defaultRefreshTokenValidityPeriod) {
    this.defaultRefreshTokenValidityPeriod = defaultRefreshTokenValidityPeriod;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("defaultRefreshTokenValidityPeriod")
  public Long getDefaultRefreshTokenValidityPeriod() {
    return defaultRefreshTokenValidityPeriod;
  }

  @JsonProperty("defaultRefreshTokenValidityPeriod")
  public void setDefaultRefreshTokenValidityPeriod(Long defaultRefreshTokenValidityPeriod) {
    this.defaultRefreshTokenValidityPeriod = defaultRefreshTokenValidityPeriod;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IssueRefreshTokenRequestData issueRefreshTokenRequestData = (IssueRefreshTokenRequestData) o;
    return Objects.equals(this.grantType, issueRefreshTokenRequestData.grantType) &&
        Objects.equals(this.consentCreatedTime, issueRefreshTokenRequestData.consentCreatedTime) &&
        Objects.equals(this.consentValidityPeriod, issueRefreshTokenRequestData.consentValidityPeriod) &&
        Objects.equals(this.defaultRefreshTokenValidityPeriod, issueRefreshTokenRequestData.defaultRefreshTokenValidityPeriod);
  }

  @Override
  public int hashCode() {
    return Objects.hash(grantType, consentCreatedTime, consentValidityPeriod, defaultRefreshTokenValidityPeriod);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IssueRefreshTokenRequestData {\n");
    
    sb.append("    grantType: ").append(toIndentedString(grantType)).append("\n");
    sb.append("    consentCreatedTime: ").append(toIndentedString(consentCreatedTime)).append("\n");
    sb.append("    consentValidityPeriod: ").append(toIndentedString(consentValidityPeriod)).append("\n");
    sb.append("    defaultRefreshTokenValidityPeriod: ").append(toIndentedString(defaultRefreshTokenValidityPeriod)).append("\n");
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

