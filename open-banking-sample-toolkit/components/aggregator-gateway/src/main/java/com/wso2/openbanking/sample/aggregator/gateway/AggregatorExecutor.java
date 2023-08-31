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

package com.wso2.openbanking.sample.aggregator.gateway;

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Executor used in aggregator solution.
 */
public class AggregatorExecutor implements OpenBankingGatewayExecutor {

    private static final String INFO_HEADER_TAG = "Account-Request-Information";
    private static final Log log = LogFactory.getLog(AggregatorExecutor.class);

    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {
        if (obapiRequestContext.getAddedHeaders().containsKey(INFO_HEADER_TAG)) {
            Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
            String informationHeader = addedHeaders.get(INFO_HEADER_TAG);
            try {
                JSONObject body = JWTUtils.decodeRequestJWT(informationHeader, "body");
                JSONObject additionalConsentInfo = (JSONObject) body.get("additionalConsentInfo");
                if (additionalConsentInfo.containsKey("access_token")) {
                    String accessToken = (String) additionalConsentInfo.get("access_token");
                    addedHeaders.put("Authorization", "Bearer " + accessToken);
                    obapiRequestContext.setAddedHeaders(addedHeaders);
                }
                if (additionalConsentInfo.containsKey("bank_code")) {
                    String bankCode = (String) additionalConsentInfo.get("bank_code");
                    addedHeaders.put("bank_code", bankCode);
                    obapiRequestContext.setAddedHeaders(addedHeaders);
                }
            } catch (ParseException e) {
                String errorMessage = "Failed to decode information JWT";
                log.error(errorMessage, e);
                obapiRequestContext.setError(true);
                OpenBankingExecutorError error = new OpenBankingExecutorError();
                error.setMessage(errorMessage);
                error.setTitle(errorMessage);
                ArrayList<OpenBankingExecutorError> errors = obapiRequestContext.getErrors();
                errors.add(error);
                obapiRequestContext.setErrors(errors);
            }
        }
    }

    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }
}
