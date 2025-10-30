package org.openapitools.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
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



@JsonTypeName("SuccessResponseForEnrichEventSubscription_data")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponseForEnrichEventSubscriptionData   {
  private Object eventSubscriptionResponse;

  public SuccessResponseForEnrichEventSubscriptionData() {
  }

  /**
   * Event Subscription Response
   **/
  public SuccessResponseForEnrichEventSubscriptionData eventSubscriptionResponse(Object eventSubscriptionResponse) {
    this.eventSubscriptionResponse = eventSubscriptionResponse;
    return this;
  }

  
  @ApiModelProperty(value = "Event Subscription Response")
  @JsonProperty("eventSubscriptionResponse")
  public Object getEventSubscriptionResponse() {
    return eventSubscriptionResponse;
  }

  @JsonProperty("eventSubscriptionResponse")
  public void setEventSubscriptionResponse(Object eventSubscriptionResponse) {
    this.eventSubscriptionResponse = eventSubscriptionResponse;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponseForEnrichEventSubscriptionData successResponseForEnrichEventSubscriptionData = (SuccessResponseForEnrichEventSubscriptionData) o;
    return Objects.equals(this.eventSubscriptionResponse, successResponseForEnrichEventSubscriptionData.eventSubscriptionResponse);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventSubscriptionResponse);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponseForEnrichEventSubscriptionData {\n");
    
    sb.append("    eventSubscriptionResponse: ").append(toIndentedString(eventSubscriptionResponse)).append("\n");
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

