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



@JsonTypeName("SuccessResponseForConsentSearchData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class SuccessResponseForConsentSearchData   {
  private Object enrichedSearchResult;

  public SuccessResponseForConsentSearchData() {
  }

  /**
   * Enriched search result
   **/
  public SuccessResponseForConsentSearchData enrichedSearchResult(Object enrichedSearchResult) {
    this.enrichedSearchResult = enrichedSearchResult;
    return this;
  }

  
  @ApiModelProperty(value = "Enriched search result")
  @JsonProperty("enrichedSearchResult")
  public Object getEnrichedSearchResult() {
    return enrichedSearchResult;
  }

  @JsonProperty("enrichedSearchResult")
  public void setEnrichedSearchResult(Object enrichedSearchResult) {
    this.enrichedSearchResult = enrichedSearchResult;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponseForConsentSearchData successResponseForConsentSearchData = (SuccessResponseForConsentSearchData) o;
    return Objects.equals(this.enrichedSearchResult, successResponseForConsentSearchData.enrichedSearchResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enrichedSearchResult);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponseForConsentSearchData {\n");
    
    sb.append("    enrichedSearchResult: ").append(toIndentedString(enrichedSearchResult)).append("\n");
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

