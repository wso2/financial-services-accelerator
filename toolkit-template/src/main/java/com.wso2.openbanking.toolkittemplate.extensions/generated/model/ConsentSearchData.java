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



@JsonTypeName("ConsentSearchData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class ConsentSearchData   {
  public enum SearchTypeEnum {

    BULK_SEARCH(String.valueOf("BULK_SEARCH")), AMENDMENT_HISTORY(String.valueOf("AMENDMENT_HISTORY"));


    private String value;

    SearchTypeEnum (String v) {
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
    public static SearchTypeEnum fromString(String s) {
        for (SearchTypeEnum b : SearchTypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static SearchTypeEnum fromValue(String value) {
        for (SearchTypeEnum b : SearchTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private SearchTypeEnum searchType;
  private Object searchResult;
  private Object enrichmentParams;

  public ConsentSearchData() {
  }

  /**
   **/
  public ConsentSearchData searchType(SearchTypeEnum searchType) {
    this.searchType = searchType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("searchType")
  public SearchTypeEnum getSearchType() {
    return searchType;
  }

  @JsonProperty("searchType")
  public void setSearchType(SearchTypeEnum searchType) {
    this.searchType = searchType;
  }

  /**
   * payload
   **/
  public ConsentSearchData searchResult(Object searchResult) {
    this.searchResult = searchResult;
    return this;
  }

  
  @ApiModelProperty(value = "payload")
  @JsonProperty("searchResult")
  public Object getSearchResult() {
    return searchResult;
  }

  @JsonProperty("searchResult")
  public void setSearchResult(Object searchResult) {
    this.searchResult = searchResult;
  }

  /**
   * query params
   **/
  public ConsentSearchData enrichmentParams(Object enrichmentParams) {
    this.enrichmentParams = enrichmentParams;
    return this;
  }

  
  @ApiModelProperty(value = "query params")
  @JsonProperty("enrichmentParams")
  public Object getEnrichmentParams() {
    return enrichmentParams;
  }

  @JsonProperty("enrichmentParams")
  public void setEnrichmentParams(Object enrichmentParams) {
    this.enrichmentParams = enrichmentParams;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentSearchData consentSearchData = (ConsentSearchData) o;
    return Objects.equals(this.searchType, consentSearchData.searchType) &&
        Objects.equals(this.searchResult, consentSearchData.searchResult) &&
        Objects.equals(this.enrichmentParams, consentSearchData.enrichmentParams);
  }

  @Override
  public int hashCode() {
    return Objects.hash(searchType, searchResult, enrichmentParams);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentSearchData {\n");
    
    sb.append("    searchType: ").append(toIndentedString(searchType)).append("\n");
    sb.append("    searchResult: ").append(toIndentedString(searchResult)).append("\n");
    sb.append("    enrichmentParams: ").append(toIndentedString(enrichmentParams)).append("\n");
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

