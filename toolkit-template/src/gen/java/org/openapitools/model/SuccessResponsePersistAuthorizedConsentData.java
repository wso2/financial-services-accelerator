package org.openapitools.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.openapitools.model.DetailedConsentResourceDataWithAmendments;
import org.openapitools.model.SuccessResponseWithDetailedConsentData;
import org.openapitools.model.SuccessResponseWithDetailedConsentDataAndAmendments;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("SuccessResponsePersistAuthorizedConsent_data")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponsePersistAuthorizedConsentData   {
  private DetailedConsentResourceDataWithAmendments consentResource;

  public SuccessResponsePersistAuthorizedConsentData() {
  }

  /**
   **/
  public SuccessResponsePersistAuthorizedConsentData consentResource(DetailedConsentResourceDataWithAmendments consentResource) {
    this.consentResource = consentResource;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("consentResource")
  @Valid public DetailedConsentResourceDataWithAmendments getConsentResource() {
    return consentResource;
  }

  @JsonProperty("consentResource")
  public void setConsentResource(DetailedConsentResourceDataWithAmendments consentResource) {
    this.consentResource = consentResource;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponsePersistAuthorizedConsentData successResponsePersistAuthorizedConsentData = (SuccessResponsePersistAuthorizedConsentData) o;
    return Objects.equals(this.consentResource, successResponsePersistAuthorizedConsentData.consentResource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentResource);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponsePersistAuthorizedConsentData {\n");
    
    sb.append("    consentResource: ").append(toIndentedString(consentResource)).append("\n");
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

