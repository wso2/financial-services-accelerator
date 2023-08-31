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

package com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler;

import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;

/**
 *  Extension interface for setting values in response type handling. Toolkits have to implement this.
 */
public interface OBResponseTypeHandler {

    /**
     * return the new refresh validity period.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     */
    public long updateRefreshTokenValidityPeriod(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext);

    /**
     * return the new approved scope.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     */
    public String[] updateApprovedScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext);


}
