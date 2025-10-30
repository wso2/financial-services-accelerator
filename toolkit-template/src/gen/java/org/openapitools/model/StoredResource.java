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



@JsonTypeName("StoredResource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class StoredResource   {
  private String id;
  private String accountId;
  private String permission;
  private String status;

  public StoredResource() {
  }

  /**
   **/
  public StoredResource id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public StoredResource accountId(String accountId) {
    this.accountId = accountId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("accountId")
  public String getAccountId() {
    return accountId;
  }

  @JsonProperty("accountId")
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  /**
   **/
  public StoredResource permission(String permission) {
    this.permission = permission;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("permission")
  public String getPermission() {
    return permission;
  }

  @JsonProperty("permission")
  public void setPermission(String permission) {
    this.permission = permission;
  }

  /**
   **/
  public StoredResource status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StoredResource storedResource = (StoredResource) o;
    return Objects.equals(this.id, storedResource.id) &&
        Objects.equals(this.accountId, storedResource.accountId) &&
        Objects.equals(this.permission, storedResource.permission) &&
        Objects.equals(this.status, storedResource.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, accountId, permission, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StoredResource {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
    sb.append("    permission: ").append(toIndentedString(permission)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

