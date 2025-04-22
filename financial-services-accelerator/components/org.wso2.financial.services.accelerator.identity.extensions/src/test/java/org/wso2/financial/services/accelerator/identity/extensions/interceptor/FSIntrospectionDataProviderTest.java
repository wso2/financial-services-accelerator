/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.interceptor;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2IntrospectionResponseDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test class for FSIntrospectionDataProvider.
 */
public class FSIntrospectionDataProviderTest {

    @BeforeClass
    public void beforeClass() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME, "consent_id");
        configMap.put(FinancialServicesConstants.APPEND_CONSENT_ID_TO_TOKEN_INTROSPECT_RESPONSE, "true");
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);
    }

    @Test
    public void testGetIntrospectionData() throws IdentityOAuth2Exception {

        FSIntrospectionDataProvider fsDefaultIntrospectionDataProvider = new FSIntrospectionDataProvider();

        OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2IntrospectionResponseDTO oAuth2IntrospectionResponseDTO = new OAuth2IntrospectionResponseDTO();
        oAuth2IntrospectionResponseDTO.setActive(true);
        oAuth2IntrospectionResponseDTO.setScope("accounts payments consent_id123");

        Map<String, Object> introspectionData = fsDefaultIntrospectionDataProvider
                .getIntrospectionData(oAuth2TokenValidationRequestDTO, oAuth2IntrospectionResponseDTO);

        // Assert
        assertEquals(introspectionData.get("consent_id"), "123");
        assertEquals(introspectionData.get("scope"), "accounts payments");
    }

}
