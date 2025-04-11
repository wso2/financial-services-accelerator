/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.response.handler;

import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;

/**
 *  Extension interface for setting values in response type handling. Toolkits have to implement this.
 */
public interface FSResponseTypeHandler {

    /**
     * return the new refresh validity period.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     */
    long getRefreshTokenValidityPeriod(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext);

    /**
     * return the new approved scope.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     */
    String[] getApprovedScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext);

}
