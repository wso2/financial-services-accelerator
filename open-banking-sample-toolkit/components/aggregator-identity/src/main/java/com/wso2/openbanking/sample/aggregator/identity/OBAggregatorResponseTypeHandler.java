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

package com.wso2.openbanking.sample.aggregator.identity;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.OBResponseTypeHandler;
import com.wso2.openbanking.sample.aggregator.identity.util.AggregatorConstants;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;

import java.util.Arrays;

import javax.servlet.http.Cookie;


/**
 * OBAggregatorResponseTypeHandler to bind consent id to scopes
 */
public class OBAggregatorResponseTypeHandler implements OBResponseTypeHandler {

    private static final Log log = LogFactory.getLog(OBAggregatorResponseTypeHandler.class);

    public long updateRefreshTokenValidityPeriod(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {
        return oAuthAuthzReqMessageContext.getRefreshTokenvalidityPeriod();
    }

    public String[] updateApprovedScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {
        Cookie[] cookies = oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getCookie();
        String commonAuthId = null;
        String[] scopes = oAuthAuthzReqMessageContext.getApprovedScope();
        ConsentCoreService consentCoreService = new ConsentCoreServiceImpl();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(AggregatorConstants.COMMON_AUTH_ID_TAG)) {
                commonAuthId = cookie.getValue();
            }
        }
        if (commonAuthId == null) {
            log.error("Cannot find Common Auth ID in the cookies");
        } else {
            try {
                String consentId = consentCoreService.getConsentIdByConsentAttributeNameAndValue(
                        AggregatorConstants.COMMON_AUTH_ID_TAG, commonAuthId).get(0);
                String consentScope = "consent_id" + consentId;
                String[] updatedScopes = (String[]) ArrayUtils.addAll(scopes, new String[]{consentScope});
                if (log.isDebugEnabled()) {
                    log.debug("Updated scopes: " + Arrays.toString(updatedScopes));
                }
                return updatedScopes;
            } catch (ConsentManagementException e) {
                log.error("Failed to retrieve Consent ID from the Common Auth ID", e);
            }
        }
        return scopes;
    }
}
