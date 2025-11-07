package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("EventPollingRequest")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class EventPollingRequest   {
  private Object eventPollingData;

  public EventPollingRequest() {
  }

  /**
   * Event polling data
   **/
  public EventPollingRequest eventPollingData(Object eventPollingData) {
    this.eventPollingData = eventPollingData;
    return this;
  }

  
  @ApiModelProperty(value = "Event polling data")
  @JsonProperty("eventPollingData")
  public Object getEventPollingData() {
    return eventPollingData;
  }

  @JsonProperty("eventPollingData")
  public void setEventPollingData(Object eventPollingData) {
    this.eventPollingData = eventPollingData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventPollingRequest eventPollingRequest = (EventPollingRequest) o;
    return Objects.equals(this.eventPollingData, eventPollingRequest.eventPollingData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventPollingData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EventPollingRequest {\n");
    
    sb.append("    eventPollingData: ").append(toIndentedString(eventPollingData)).append("\n");
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

