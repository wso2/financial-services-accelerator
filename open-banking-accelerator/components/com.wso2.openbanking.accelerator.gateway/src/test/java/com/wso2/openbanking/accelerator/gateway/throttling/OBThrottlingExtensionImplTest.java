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
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;

/**
 * Test for Open Banking throttling extension implementation.
 */
public class OBThrottlingExtensionImplTest {

    OBThrottlingExtensionImpl obThrottlingExtension;
    RequestContextDTO requestContextDTO;

    @BeforeClass
    public void beforeClass() {

        GatewayDataHolder.getInstance().setThrottleDataPublisher(new ThrottleDataPublisherTestImpl());
        obThrottlingExtension = new OBThrottlingExtensionImpl();
        requestContextDTO = Mockito.mock(RequestContextDTO.class);
    }

    @Test(priority = 1)
    public void testAddCustomThrottlingKeys() {

        ExtensionResponseDTO extensionResponseDTO = obThrottlingExtension.preProcessRequest(requestContextDTO);
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get("open"), "banking");
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get("wso2"), "ob");
    }

}
