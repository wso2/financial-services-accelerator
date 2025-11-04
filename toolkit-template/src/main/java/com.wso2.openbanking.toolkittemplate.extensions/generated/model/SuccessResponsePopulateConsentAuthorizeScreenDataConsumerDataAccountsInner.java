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



@JsonTypeName("SuccessResponsePopulateConsentAuthorizeScreenData_consumerData_accounts_inner")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner   {
  private Boolean selected;
  private String displayName;

  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner() {
  }

  @JsonCreator
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner(
    @JsonProperty(required = true, value = "selected") Boolean selected,
    @JsonProperty(required = true, value = "displayName") String displayName
  ) {
    this.selected = selected;
    this.displayName = displayName;
  }

  /**
   * Whether the account is selected by default
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner selected(Boolean selected) {
    this.selected = selected;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Whether the account is selected by default")
  @JsonProperty(required = true, value = "selected")
  @NotNull public Boolean getSelected() {
    return selected;
  }

  @JsonProperty(required = true, value = "selected")
  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  /**
   * Account display name
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Account display name")
  @JsonProperty(required = true, value = "displayName")
  @NotNull public String getDisplayName() {
    return displayName;
  }

  @JsonProperty(required = true, value = "displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner successResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner = (SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner) o;
    return Objects.equals(this.selected, successResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner.selected) &&
        Objects.equals(this.displayName, successResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner.displayName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(selected, displayName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner {\n");
    
    sb.append("    selected: ").append(toIndentedString(selected)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
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

