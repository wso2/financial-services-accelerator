package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("EventCreationRequest")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class EventCreationRequest   {
  private Object eventData;

  public EventCreationRequest() {
  }

  /**
   * Event creation Payload
   **/
  public EventCreationRequest eventData(Object eventData) {
    this.eventData = eventData;
    return this;
  }

  
  @ApiModelProperty(value = "Event creation Payload")
  @JsonProperty("eventData")
  public Object getEventData() {
    return eventData;
  }

  @JsonProperty("eventData")
  public void setEventData(Object eventData) {
    this.eventData = eventData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventCreationRequest eventCreationRequest = (EventCreationRequest) o;
    return Objects.equals(this.eventData, eventCreationRequest.eventData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EventCreationRequest {\n");
    
    sb.append("    eventData: ").append(toIndentedString(eventData)).append("\n");
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

