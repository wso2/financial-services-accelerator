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

package com.wso2.openbanking.accelerator.identity.interceptor;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.IntrospectionDataProvider;
import org.wso2.carbon.identity.oauth2.dto.OAuth2IntrospectionResponseDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;

import java.util.Map;

/**
 * OB specific introspection data provider.
 */
public class OBIntrospectionDataProvider extends AbstractIdentityHandler implements IntrospectionDataProvider {

    private static IntrospectionDataProvider introspectionDataProvider;

    @Override
    public Map<String, Object> getIntrospectionData(OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO,
                                                    OAuth2IntrospectionResponseDTO oAuth2IntrospectionResponseDTO)
            throws IdentityOAuth2Exception {

        Map<String, Object> additionalDataMap = getIntrospectionDataProvider()
                .getIntrospectionData(oAuth2TokenValidationRequestDTO, oAuth2IntrospectionResponseDTO);
        String[] nonInternalScopes = IdentityCommonUtil.removeInternalScopes(oAuth2IntrospectionResponseDTO.getScope()
                .split(IdentityCommonConstants.SPACE_SEPARATOR));
        oAuth2IntrospectionResponseDTO.setScope(StringUtils.join(nonInternalScopes,
                IdentityCommonConstants.SPACE_SEPARATOR));
        additionalDataMap.put(IdentityCommonConstants.SCOPE, StringUtils.join(nonInternalScopes,
                IdentityCommonConstants.SPACE_SEPARATOR));
        oAuth2IntrospectionResponseDTO.setProperties(additionalDataMap);
        return additionalDataMap;
    }

    public static IntrospectionDataProvider getIntrospectionDataProvider() {

        return introspectionDataProvider;
    }

    public static void setIntrospectionDataProvider(IntrospectionDataProvider introspectionDataProvider) {

        OBIntrospectionDataProvider.introspectionDataProvider = introspectionDataProvider;
    }
}
