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

package org.wso2.financial.services.accelerator.identity.extensions.dcr.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

/**
 * DCR Utils Class
 */
public class DCRUtils {

    private static final Log log = LogFactory.getLog(DCRUtils.class);
    private static final IdentityExtensionsDataHolder identityDataHolder = IdentityExtensionsDataHolder.getInstance();

    /**
     * Method to extract Fapi Compliant Property From Service Provider
     * @param serviceProvider   Service Provider
     *
     * @return Fapi Compliant Property
     * @throws IdentityOAuthAdminException When there is an error while retrieving OAuthConsumerAppDTO
     * @throws RequestObjectException When there is an error while retrieving Fapi compliant property
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static boolean getFapiCompliantPropertyFromSP(ServiceProvider serviceProvider)
            throws IdentityOAuthAdminException, RequestObjectException {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getOAuthConsumerAppDTO(serviceProvider.getApplicationName());
        return FinancialServicesUtils.isRegulatoryApp(oAuthConsumerAppDTO.getOauthConsumerKey());
    }

    /**
     * Method to get OAuthConsumerAppDTO using service provider application name.
     *
     * @param spApplicationName    Service provider application name
     * @return OAuthConsumerAppDTO
     * @throws IdentityOAuthAdminException when there is an error
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static OAuthConsumerAppDTO getOAuthConsumerAppDTO(String spApplicationName)
            throws IdentityOAuthAdminException {

        OAuthAdminServiceImpl oAuthAdminService = identityDataHolder.getOauthAdminService();
        return oAuthAdminService.getOAuthApplicationDataByAppName(spApplicationName);
    }
}
