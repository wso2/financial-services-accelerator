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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
  * defines metadata for requested applications
 **/
@ApiModel(description="defines metadata for requested applications")
public class ApplicationBulkMetadataSuccessDTO  {

  @ApiModelProperty(required = true, value = "Key value pairs of clientId and attributes")
  @Valid
 /**
   * Key value pairs of clientId and attributes
  **/
  private Map<String, ApplicationMetadataResourceDTO> data = new HashMap<String, ApplicationMetadataResourceDTO>();
 /**
   * Key value pairs of clientId and attributes
   * @return data
  **/
  @JsonProperty("data")
  @NotNull
  public Map<String, ApplicationMetadataResourceDTO> getData() {
    return data;
  }

  public void setData(Map<String, ApplicationMetadataResourceDTO> data) {
    this.data = data;
  }

  public ApplicationBulkMetadataSuccessDTO data(Map<String, ApplicationMetadataResourceDTO> data) {
    this.data = data;
    return this;
  }

  public ApplicationBulkMetadataSuccessDTO putDataItem(String key, ApplicationMetadataResourceDTO dataItem) {
    this.data.put(key, dataItem);
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationBulkMetadataSuccessDTO {\n");

    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

