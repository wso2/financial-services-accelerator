package org.openapitools.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("SuccessResponseForEventWithDetails_data")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponseForEventWithDetailsData   {
  private String callbackUrl;
  private String version;
  private @Valid List<String> eventTypes = new ArrayList<>();

  public SuccessResponseForEventWithDetailsData() {
  }

  /**
   **/
  public SuccessResponseForEventWithDetailsData callbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("callbackUrl")
  public String getCallbackUrl() {
    return callbackUrl;
  }

  @JsonProperty("callbackUrl")
  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  /**
   **/
  public SuccessResponseForEventWithDetailsData version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public SuccessResponseForEventWithDetailsData eventTypes(List<String> eventTypes) {
    this.eventTypes = eventTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("eventTypes")
  public List<String> getEventTypes() {
    return eventTypes;
  }

  @JsonProperty("eventTypes")
  public void setEventTypes(List<String> eventTypes) {
    this.eventTypes = eventTypes;
  }

  public SuccessResponseForEventWithDetailsData addEventTypesItem(String eventTypesItem) {
    if (this.eventTypes == null) {
      this.eventTypes = new ArrayList<>();
    }

    this.eventTypes.add(eventTypesItem);
    return this;
  }

  public SuccessResponseForEventWithDetailsData removeEventTypesItem(String eventTypesItem) {
    if (eventTypesItem != null && this.eventTypes != null) {
      this.eventTypes.remove(eventTypesItem);
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
    SuccessResponseForEventWithDetailsData successResponseForEventWithDetailsData = (SuccessResponseForEventWithDetailsData) o;
    return Objects.equals(this.callbackUrl, successResponseForEventWithDetailsData.callbackUrl) &&
        Objects.equals(this.version, successResponseForEventWithDetailsData.version) &&
        Objects.equals(this.eventTypes, successResponseForEventWithDetailsData.eventTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(callbackUrl, version, eventTypes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponseForEventWithDetailsData {\n");
    
    sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    eventTypes: ").append(toIndentedString(eventTypes)).append("\n");
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

