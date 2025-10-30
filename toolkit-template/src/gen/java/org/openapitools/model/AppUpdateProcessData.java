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

/**
 * Defines the context data related to the application update.
 **/
@ApiModel(description = "Defines the context data related to the application update.")
@JsonTypeName("AppUpdateProcessData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class AppUpdateProcessData   {
  private Object appData;
  private Object additionalProperties;
  private Object existingAppData;

  public AppUpdateProcessData() {
  }

  @JsonCreator
  public AppUpdateProcessData(
    @JsonProperty(required = true, value = "appData") Object appData,
    @JsonProperty(required = true, value = "additionalProperties") Object additionalProperties,
    @JsonProperty(required = true, value = "existingAppData") Object existingAppData
  ) {
    this.appData = appData;
    this.additionalProperties = additionalProperties;
    this.existingAppData = existingAppData;
  }

  /**
   * OAuth Application Data. Mandatory for pre-process-application-update.
   **/
  public AppUpdateProcessData appData(Object appData) {
    this.appData = appData;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "OAuth Application Data. Mandatory for pre-process-application-update.")
  @JsonProperty(required = true, value = "appData")
  @NotNull public Object getAppData() {
    return appData;
  }

  @JsonProperty(required = true, value = "appData")
  public void setAppData(Object appData) {
    this.appData = appData;
  }

  /**
   * Additional properties reterived from devportal UI. Mandatory for pre-process-application-update.
   **/
  public AppUpdateProcessData additionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Additional properties reterived from devportal UI. Mandatory for pre-process-application-update.")
  @JsonProperty(required = true, value = "additionalProperties")
  @NotNull public Object getAdditionalProperties() {
    return additionalProperties;
  }

  @JsonProperty(required = true, value = "additionalProperties")
  public void setAdditionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   * Existing OAuth Application Data. Mandatory for pre-process-application-update.
   **/
  public AppUpdateProcessData existingAppData(Object existingAppData) {
    this.existingAppData = existingAppData;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Existing OAuth Application Data. Mandatory for pre-process-application-update.")
  @JsonProperty(required = true, value = "existingAppData")
  @NotNull public Object getExistingAppData() {
    return existingAppData;
  }

  @JsonProperty(required = true, value = "existingAppData")
  public void setExistingAppData(Object existingAppData) {
    this.existingAppData = existingAppData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AppUpdateProcessData appUpdateProcessData = (AppUpdateProcessData) o;
    return Objects.equals(this.appData, appUpdateProcessData.appData) &&
        Objects.equals(this.additionalProperties, appUpdateProcessData.additionalProperties) &&
        Objects.equals(this.existingAppData, appUpdateProcessData.existingAppData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appData, additionalProperties, existingAppData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppUpdateProcessData {\n");
    
    sb.append("    appData: ").append(toIndentedString(appData)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    existingAppData: ").append(toIndentedString(existingAppData)).append("\n");
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

