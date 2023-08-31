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

package com.wso2.openbanking.accelerator.push.authorization.endpoint.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for push authorisation response.
 */
public class PushAuthorisationResponse {

    private String requestUri = null;
    private Integer expiresIn = null;

    @JsonProperty("request_uri")
    public String getRequest_uri() {

        return requestUri;
    }

    public void setRequestUri(String requestUri) {

        this.requestUri = requestUri;
    }

    public PushAuthorisationResponse requestUri(String requestUri) {

        this.requestUri = requestUri;
        return this;
    }

    @JsonProperty("expires_in")
    public Integer getExpires_in() {

        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {

        this.expiresIn = expiresIn;
    }

    public PushAuthorisationResponse expiresIn(Integer expiresIn) {

        this.expiresIn = expiresIn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PushAuthorisationResponse {\n");

        sb.append("    requestUri: ").append(toIndentedString(requestUri)).append("\n");
        sb.append("    expiresIn: ").append(toIndentedString(expiresIn)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object object) {
        if (object == null) {
            return "null";
        }
        return object.toString().replace("\n", "\n    ");
    }
}
