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

package org.wso2.financial.services.accelerator.identity.extensions.grant.type.handlers;

import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

/**
 * Interface to extend grant handler functionality.
 */
public interface FSGrantHandler {

    /**
     * append parameters to token response.
     *
     * @param oAuth2AccessTokenRespDTO
     * @param tokReqMsgCtx
     */
    void appendParametersToTokenResponse(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO,
                                         OAuthTokenReqMessageContext tokReqMsgCtx);

    /**
     * Return should issue refresh token
     *
     * @return should issue refresh token
     */
    boolean issueRefreshToken(String grantType);

}
