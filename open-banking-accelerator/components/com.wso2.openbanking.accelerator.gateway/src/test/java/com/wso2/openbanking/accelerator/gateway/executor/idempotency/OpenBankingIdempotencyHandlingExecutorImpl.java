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

package com.wso2.openbanking.accelerator.gateway.executor.idempotency;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.IdempotencyConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.util.HashMap;
import java.util.Map;


/**
 * OpenBankingIdempotencyHandlingExecutorImpl.
 */
public class OpenBankingIdempotencyHandlingExecutorImpl extends OpenBankingIdempotencyHandlingExecutor {

    private static final Log log = LogFactory.getLog(OpenBankingIdempotencyHandlingExecutorImpl.class);


    @Override
    public String getCreatedTimeFromResponse(OBAPIResponseContext obapiResponseContext) {
        MsgInfoDTO msgInfoDTO = obapiResponseContext.getMsgInfo();
        String createdTime = null;
        if (msgInfoDTO.getHeaders().get("CreatedTime") != null) {
            //Retrieve response created time from headers
            createdTime = msgInfoDTO.getHeaders().get("CreatedTime");
        }
        return createdTime;
    }

    @Override
    public Map<String, Object> getPayloadFromRequest(OBAPIRequestContext obapiRequestContext) {
        Map<String, Object> map = new HashMap<>();
        map.put(IdempotencyConstants.PAYLOAD, obapiRequestContext.getRequestPayload());
        map.put(IdempotencyConstants.HTTP_STATUS, HttpStatus.SC_CREATED);
        return map;
    }

    @Override
    public boolean isValidIdempotencyRequest(OBAPIRequestContext obapiRequestContext) {
        return true;
    }

    @Override
    public boolean isValidIdempotencyResponse(OBAPIResponseContext obapiResponseContext) {
        return true;
    }
}
