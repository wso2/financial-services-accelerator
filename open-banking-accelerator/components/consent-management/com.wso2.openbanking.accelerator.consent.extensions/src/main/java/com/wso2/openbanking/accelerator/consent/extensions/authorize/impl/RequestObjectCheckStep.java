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
package com.wso2.openbanking.accelerator.consent.extensions.authorize.impl;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONObject;
/**
 * Step to check whether the request object is sent in the authorization reques for
 * regulatory app.
 */
public class RequestObjectCheckStep implements ConsentRetrievalStep {

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (consentData.isRegulatory() && !checkRequestObject(consentData.getSpQueryParams())) {
            JSONObject json = new JSONObject();
            json.put("error", AuthErrorCode.INVALID_REQUEST.toString());
            json.put("redirect_uri", consentData.getRedirectURI().toString());
            throw new ConsentException(ResponseStatus.BAD_REQUEST, json);
        }
    }

    private boolean checkRequestObject(String spQueryParams) {

        boolean requestObjectExist = false;
        if (spQueryParams != null && !spQueryParams.trim().isEmpty()) {
            String requestObject = null;
            String[] spQueries = spQueryParams.split("&");
            for (String param : spQueries) {
                if (param.contains("request=")) {
                    requestObjectExist = true;
                }
            }
        }
        return requestObjectExist;
    }
}
