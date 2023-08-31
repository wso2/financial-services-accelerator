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
import com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.OBHybridResponseTypeHandlerExtension;
import com.wso2.openbanking.sample.aggregator.identity.util.AggregatorConstants;
import com.wso2.openbanking.sample.aggregator.identity.util.AggregatorIdentityUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Aggregator Response Type Handler.
 */
public class AggregatorHybridResponseTypeHandler extends OBHybridResponseTypeHandlerExtension {

    private static final Log log = LogFactory.getLog(AggregatorHybridResponseTypeHandler.class);

    @Override
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext)
            throws IdentityOAuth2Exception {
        updateConsentAccessToken(oAuthAuthzReqMessageContext);
        return super.issue(oAuthAuthzReqMessageContext);
    }

    /**
     *
     * @param oAuthAuthzReqMessageContext
     */
    private void updateConsentAccessToken(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        ConsentCoreService consentCoreService = new ConsentCoreServiceImpl();
        Set<Map.Entry<ClaimMapping, String>> entries = oAuthAuthzReqMessageContext
                .getAuthorizationReqDTO().getUser().getUserAttributes().entrySet();
        Map<String, String> consentAttributes = new HashMap<String, String>();
        for (Map.Entry<ClaimMapping, String> entry : entries) {
            if (entry.getKey().getRemoteClaim().getClaimUri().equals(AggregatorConstants.ACCESS_TOKEN_TAG)) {
                consentAttributes.put(AggregatorConstants.ACCESS_TOKEN_TAG, entry.getValue());
            } else if (entry.getKey().getRemoteClaim().getClaimUri().equals(AggregatorConstants.BANK_CODE_TAG)) {
                consentAttributes.put(AggregatorConstants.BANK_CODE_TAG, entry.getValue());
            }
        }
        try {
            String consentId = consentCoreService.getConsentIdByConsentAttributeNameAndValue(
                    AggregatorConstants.COMMON_AUTH_ID_TAG, AggregatorIdentityUtil.getCommonAuthId(
                            oAuthAuthzReqMessageContext)).get(0);
            consentCoreService.storeConsentAttributes(consentId, consentAttributes);
        } catch (ConsentManagementException ex) {
            log.error("Failed to obtain or update the Consent", ex);
        }
    }
}
