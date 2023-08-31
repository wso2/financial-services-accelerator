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

package com.wso2.openbanking.accelerator.gateway.throttling;

import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ResponseContextDTO;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.ExtensionListener;

/**
 * Throttling Extension listener implementation for OB.
 */
public class OBThrottlingExtensionImpl implements ExtensionListener {

    @Override
    public ExtensionResponseDTO preProcessRequest(RequestContextDTO requestContextDTO) {

        ExtensionResponseDTO responseDTO = null;
        ThrottleDataPublisher throttleDataPublisher = GatewayDataHolder.getInstance().getThrottleDataPublisher();
        if (throttleDataPublisher != null) {
            responseDTO = new ExtensionResponseDTO();
            responseDTO.setCustomProperty(throttleDataPublisher.getCustomProperties(requestContextDTO));
            responseDTO.setResponseStatus(ExtensionResponseStatus.CONTINUE.toString());
        }
        return responseDTO;

    }

    @Override
    public ExtensionResponseDTO postProcessRequest(RequestContextDTO requestContextDTO) {

        return null;
    }

    @Override
    public ExtensionResponseDTO preProcessResponse(ResponseContextDTO responseContextDTO) {

        return null;
    }

    @Override
    public ExtensionResponseDTO postProcessResponse(ResponseContextDTO responseContextDTO) {

        return null;
    }

    @Override
    public String getType() {

        return null;
    }
}
