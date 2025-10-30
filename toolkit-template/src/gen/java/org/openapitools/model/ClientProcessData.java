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
 * Defines the context data related to the client registration.
 **/
@ApiModel(description = "Defines the context data related to the client registration.")
@JsonTypeName("ClientProcessData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class ClientProcessData   {
  private Object clientData;
  private Object softwareStatement;
  private Object existingClientData;

  public ClientProcessData() {
  }

  /**
   * Client Registration Data. Mandatory for pre-process-client-creation and pre-process-client-update.
   **/
  public ClientProcessData clientData(Object clientData) {
    this.clientData = clientData;
    return this;
  }

  
  @ApiModelProperty(value = "Client Registration Data. Mandatory for pre-process-client-creation and pre-process-client-update.")
  @JsonProperty("clientData")
  public Object getClientData() {
    return clientData;
  }

  @JsonProperty("clientData")
  public void setClientData(Object clientData) {
    this.clientData = clientData;
  }

  /**
   * Parameters of the decoded SSA. Mandatory for pre-process-client-creation, pre-process-client-update and pre-process-client-retrieval.
   **/
  public ClientProcessData softwareStatement(Object softwareStatement) {
    this.softwareStatement = softwareStatement;
    return this;
  }

  
  @ApiModelProperty(value = "Parameters of the decoded SSA. Mandatory for pre-process-client-creation, pre-process-client-update and pre-process-client-retrieval.")
  @JsonProperty("softwareStatement")
  public Object getSoftwareStatement() {
    return softwareStatement;
  }

  @JsonProperty("softwareStatement")
  public void setSoftwareStatement(Object softwareStatement) {
    this.softwareStatement = softwareStatement;
  }

  /**
   * properties of the existing client application. Mandatory for pre-process-client-update.
   **/
  public ClientProcessData existingClientData(Object existingClientData) {
    this.existingClientData = existingClientData;
    return this;
  }

  
  @ApiModelProperty(value = "properties of the existing client application. Mandatory for pre-process-client-update.")
  @JsonProperty("existingClientData")
  public Object getExistingClientData() {
    return existingClientData;
  }

  @JsonProperty("existingClientData")
  public void setExistingClientData(Object existingClientData) {
    this.existingClientData = existingClientData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClientProcessData clientProcessData = (ClientProcessData) o;
    return Objects.equals(this.clientData, clientProcessData.clientData) &&
        Objects.equals(this.softwareStatement, clientProcessData.softwareStatement) &&
        Objects.equals(this.existingClientData, clientProcessData.existingClientData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientData, softwareStatement, existingClientData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientProcessData {\n");
    
    sb.append("    clientData: ").append(toIndentedString(clientData)).append("\n");
    sb.append("    softwareStatement: ").append(toIndentedString(softwareStatement)).append("\n");
    sb.append("    existingClientData: ").append(toIndentedString(existingClientData)).append("\n");
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

