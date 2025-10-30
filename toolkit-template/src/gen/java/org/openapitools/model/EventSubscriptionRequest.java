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



@JsonTypeName("EventSubscriptionRequest")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class EventSubscriptionRequest   {
  public enum EventTypeEnum {

    SUBSCRIPTION_CREATION(String.valueOf("SubscriptionCreation")), SINGLE_SUBSCRIPTION_RETRIEVAL(String.valueOf("SingleSubscriptionRetrieval")), BULK_SUBSCRIPTION_RETRIEVAL(String.valueOf("BulkSubscriptionRetrieval")), SUBSCRIPTION_RETRIEVAL_FOR_EVENT_TYPES(String.valueOf("SubscriptionRetrievalForEventTypes")), SUBSCRIPTION_UPDATE(String.valueOf("SubscriptionUpdate")), SUBSCRIPTION_DELETE(String.valueOf("SubscriptionDelete"));


    private String value;

    EventTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static EventTypeEnum fromString(String s) {
        for (EventTypeEnum b : EventTypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static EventTypeEnum fromValue(String value) {
        for (EventTypeEnum b : EventTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private EventTypeEnum eventType;
  private Object eventSubscriptionData;

  public EventSubscriptionRequest() {
  }

  /**
   **/
  public EventSubscriptionRequest eventType(EventTypeEnum eventType) {
    this.eventType = eventType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("eventType")
  public EventTypeEnum getEventType() {
    return eventType;
  }

  @JsonProperty("eventType")
  public void setEventType(EventTypeEnum eventType) {
    this.eventType = eventType;
  }

  /**
   * Event Subscription Payload
   **/
  public EventSubscriptionRequest eventSubscriptionData(Object eventSubscriptionData) {
    this.eventSubscriptionData = eventSubscriptionData;
    return this;
  }

  
  @ApiModelProperty(value = "Event Subscription Payload")
  @JsonProperty("eventSubscriptionData")
  public Object getEventSubscriptionData() {
    return eventSubscriptionData;
  }

  @JsonProperty("eventSubscriptionData")
  public void setEventSubscriptionData(Object eventSubscriptionData) {
    this.eventSubscriptionData = eventSubscriptionData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventSubscriptionRequest eventSubscriptionRequest = (EventSubscriptionRequest) o;
    return Objects.equals(this.eventType, eventSubscriptionRequest.eventType) &&
        Objects.equals(this.eventSubscriptionData, eventSubscriptionRequest.eventSubscriptionData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventType, eventSubscriptionData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EventSubscriptionRequest {\n");
    
    sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
    sb.append("    eventSubscriptionData: ").append(toIndentedString(eventSubscriptionData)).append("\n");
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

