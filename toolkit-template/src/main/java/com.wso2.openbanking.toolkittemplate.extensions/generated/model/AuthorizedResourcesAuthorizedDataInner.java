package com.wso2.openbanking.toolkittemplate.extensions.generated.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;


@JsonTypeName("AuthorizedResources_authorizedData_inner")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class AuthorizedResourcesAuthorizedDataInner   {
  private @Valid List<String> permissions = new ArrayList<>();
  private @Valid List<Account> accounts = new ArrayList<>();

  public AuthorizedResourcesAuthorizedDataInner() {
  }

  @JsonCreator
  public AuthorizedResourcesAuthorizedDataInner(
    @JsonProperty(required = true, value = "accounts") List<@Valid Account> accounts
  ) {
    this.accounts = accounts;
  }

  /**
   * Granted permissions (optional if no permissions exist)
   **/
  public AuthorizedResourcesAuthorizedDataInner permissions(List<String> permissions) {
    this.permissions = permissions;
    return this;
  }

  
  @ApiModelProperty(value = "Granted permissions (optional if no permissions exist)")
  @JsonProperty("permissions")
  public List<String> getPermissions() {
    return permissions;
  }

  @JsonProperty("permissions")
  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  public AuthorizedResourcesAuthorizedDataInner addPermissionsItem(String permissionsItem) {
    if (this.permissions == null) {
      this.permissions = new ArrayList<>();
    }

    this.permissions.add(permissionsItem);
    return this;
  }

  public AuthorizedResourcesAuthorizedDataInner removePermissionsItem(String permissionsItem) {
    if (permissionsItem != null && this.permissions != null) {
      this.permissions.remove(permissionsItem);
    }

    return this;
  }
  /**
   * Accounts selected for the permissions
   **/
  public AuthorizedResourcesAuthorizedDataInner accounts(List<Account> accounts) {
    this.accounts = accounts;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Accounts selected for the permissions")
  @JsonProperty(required = true, value = "accounts")
  @NotNull @Valid public List<@Valid Account> getAccounts() {
    return accounts;
  }

  @JsonProperty(required = true, value = "accounts")
  public void setAccounts(List<Account> accounts) {
    this.accounts = accounts;
  }

  public AuthorizedResourcesAuthorizedDataInner addAccountsItem(Account accountsItem) {
    if (this.accounts == null) {
      this.accounts = new ArrayList<>();
    }

    this.accounts.add(accountsItem);
    return this;
  }

  public AuthorizedResourcesAuthorizedDataInner removeAccountsItem(Account accountsItem) {
    if (accountsItem != null && this.accounts != null) {
      this.accounts.remove(accountsItem);
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
    AuthorizedResourcesAuthorizedDataInner authorizedResourcesAuthorizedDataInner = (AuthorizedResourcesAuthorizedDataInner) o;
    return Objects.equals(this.permissions, authorizedResourcesAuthorizedDataInner.permissions) &&
        Objects.equals(this.accounts, authorizedResourcesAuthorizedDataInner.accounts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissions, accounts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorizedResourcesAuthorizedDataInner {\n");
    
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
    sb.append("    accounts: ").append(toIndentedString(accounts)).append("\n");
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

