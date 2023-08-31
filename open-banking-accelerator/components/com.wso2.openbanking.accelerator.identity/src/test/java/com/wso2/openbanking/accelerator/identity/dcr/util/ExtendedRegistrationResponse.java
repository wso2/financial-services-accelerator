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

package com.wso2.openbanking.accelerator.identity.dcr.util;

import com.google.gson.annotations.SerializedName;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationResponse;
/**
 * Extended class for RegistrationResponse.
 */
public class ExtendedRegistrationResponse extends RegistrationResponse {

    @SerializedName("additional_attribute_1")
    protected String additionalAttribute1 = null;

    @SerializedName("additional_attribute_2")
    protected String additionalAttribute2 = null;

    public String getAdditionalAttribute1() {
        return additionalAttribute1;
    }

    public void setAdditionalAttribute1(String additionalAttribute1) {
        this.additionalAttribute1 = additionalAttribute1;
    }

    public String getAdditionalAttribute2() {
        return additionalAttribute2;
    }

    public void setAdditionalAttribute2(String additionalAttribute2) {
        this.additionalAttribute2 = additionalAttribute2;
    }
}
