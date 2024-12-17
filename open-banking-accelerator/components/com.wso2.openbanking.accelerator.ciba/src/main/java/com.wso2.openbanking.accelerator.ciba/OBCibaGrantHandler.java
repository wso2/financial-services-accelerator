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

package com.wso2.openbanking.accelerator.ciba;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.ciba.grant.CibaGrantHandler;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.util.Arrays;

/**
 * OB specific CIBA grant handler.
 */
@Deprecated
public class OBCibaGrantHandler extends CibaGrantHandler {

    private static final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();
    private static Log log = LogFactory.getLog(CibaGrantHandler.class);

    public void setConsentIdScope(OAuthTokenReqMessageContext tokReqMsgCtx, String authReqId)
            throws IdentityOAuth2Exception {
        String[] scopesArray;
        String[] tokenRequestMessageContextArray = tokReqMsgCtx.getScope();
        if (tokenRequestMessageContextArray != null) {
            scopesArray = Arrays.copyOf(tokenRequestMessageContextArray, tokenRequestMessageContextArray.length + 1);
        } else {
            throw new IdentityOAuth2Exception(CIBAConstants.MESSAGE_CONTEXT_EMPTY_ERROR);
        }
        try {
            scopesArray[scopesArray.length - 1] = CIBAConstants.CONSENT_ID_PREFIX + consentCoreService.
                    getConsentIdByConsentAttributeNameAndValue("auth_req_id", authReqId).get(0);
        } catch (ConsentManagementException e) {
            throw new IdentityOAuth2Exception(CIBAConstants.SCOPE_ADDING_ERROR, e);
        }
        tokReqMsgCtx.setScope(scopesArray);

    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        if (!super.validateGrant(tokReqMsgCtx)) {
            log.error("Successful in validating grant.Validation failed for the token request made by client: "
                    + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId());
            return false;
        } else {
            setConsentIdScope(tokReqMsgCtx, super.getAuthReqId(tokReqMsgCtx));
            return true;
        }
    }
}
