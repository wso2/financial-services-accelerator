/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.open.banking.application.info.endpoint.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
  * defines resource object for application
 **/
@ApiModel(description="defines resource object for application")
public class ApplicationMetadataResourceDTO  {

  @ApiModelProperty(value = "type of object")
 /**
   * type of object
  **/
  private String type;

  @ApiModelProperty(value = "OAuth Client id of the application")
 /**
   * OAuth Client id of the application
  **/
  private String id;

  @ApiModelProperty(value = "Key-Value pairs of application metadata")
 /**
   * Key-Value pairs of application metadata
  **/
  private Map<String, String> metadata = null;
 /**
   * type of object
   * @return type
  **/
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ApplicationMetadataResourceDTO type(String type) {
    this.type = type;
    return this;
  }

 /**
   * OAuth Client id of the application
   * @return id
  **/
  @JsonProperty("Id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ApplicationMetadataResourceDTO id(String id) {
    this.id = id;
    return this;
  }

 /**
   * Key-Value pairs of application metadata
   * @return metadata
  **/
  @JsonProperty("metadata")
  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public ApplicationMetadataResourceDTO metadata(Map<String, String> metadata) {
    this.metadata = metadata;
    return this;
  }

  public ApplicationMetadataResourceDTO putMetadataItem(String key, String metadataItem) {
    this.metadata.put(key, metadataItem);
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationMetadataResourceDTO {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private static String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

