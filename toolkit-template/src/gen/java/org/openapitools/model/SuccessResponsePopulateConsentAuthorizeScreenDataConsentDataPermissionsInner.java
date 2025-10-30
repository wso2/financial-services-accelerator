package org.openapitools.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.model.Account;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("SuccessResponsePopulateConsentAuthorizeScreenData_consentData_permissions_inner")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner   {
  private String uid;
  private @Valid List<String> displayValues = new ArrayList<>();
  private @Valid List<Account> initiatedAccounts = new ArrayList<>();

  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner() {
  }

  @JsonCreator
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner(
    @JsonProperty(required = true, value = "uid") String uid,
    @JsonProperty(required = true, value = "displayValues") List<String> displayValues
  ) {
    this.uid = uid;
    this.displayValues = displayValues;
  }

  /**
   * Unique ID for the permission
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner uid(String uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Unique ID for the permission")
  @JsonProperty(required = true, value = "uid")
  @NotNull public String getUid() {
    return uid;
  }

  @JsonProperty(required = true, value = "uid")
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * Permission display values
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner displayValues(List<String> displayValues) {
    this.displayValues = displayValues;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Permission display values")
  @JsonProperty(required = true, value = "displayValues")
  @NotNull public List<String> getDisplayValues() {
    return displayValues;
  }

  @JsonProperty(required = true, value = "displayValues")
  public void setDisplayValues(List<String> displayValues) {
    this.displayValues = displayValues;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner addDisplayValuesItem(String displayValuesItem) {
    if (this.displayValues == null) {
      this.displayValues = new ArrayList<>();
    }

    this.displayValues.add(displayValuesItem);
    return this;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner removeDisplayValuesItem(String displayValuesItem) {
    if (displayValuesItem != null && this.displayValues != null) {
      this.displayValues.remove(displayValuesItem);
    }

    return this;
  }
  /**
   * Accounts initiated with this permission
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner initiatedAccounts(List<Account> initiatedAccounts) {
    this.initiatedAccounts = initiatedAccounts;
    return this;
  }

  
  @ApiModelProperty(value = "Accounts initiated with this permission")
  @JsonProperty("initiatedAccounts")
  @Valid public List<@Valid Account> getInitiatedAccounts() {
    return initiatedAccounts;
  }

  @JsonProperty("initiatedAccounts")
  public void setInitiatedAccounts(List<Account> initiatedAccounts) {
    this.initiatedAccounts = initiatedAccounts;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner addInitiatedAccountsItem(Account initiatedAccountsItem) {
    if (this.initiatedAccounts == null) {
      this.initiatedAccounts = new ArrayList<>();
    }

    this.initiatedAccounts.add(initiatedAccountsItem);
    return this;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner removeInitiatedAccountsItem(Account initiatedAccountsItem) {
    if (initiatedAccountsItem != null && this.initiatedAccounts != null) {
      this.initiatedAccounts.remove(initiatedAccountsItem);
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
    SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner successResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner = (SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner) o;
    return Objects.equals(this.uid, successResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner.uid) &&
        Objects.equals(this.displayValues, successResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner.displayValues) &&
        Objects.equals(this.initiatedAccounts, successResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner.initiatedAccounts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uid, displayValues, initiatedAccounts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner {\n");
    
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    displayValues: ").append(toIndentedString(displayValues)).append("\n");
    sb.append("    initiatedAccounts: ").append(toIndentedString(initiatedAccounts)).append("\n");
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

