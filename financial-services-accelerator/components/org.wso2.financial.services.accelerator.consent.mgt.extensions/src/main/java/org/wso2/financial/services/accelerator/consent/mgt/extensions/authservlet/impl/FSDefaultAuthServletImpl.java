/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.FSAuthServletInterface;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils.Utils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * The default implementation for FS flow.
 */
public class FSDefaultAuthServletImpl implements FSAuthServletInterface {

    private final FinancialServicesConfigParser configParser;
    private final boolean isPreInitiatedConsent;

    public FSDefaultAuthServletImpl() {
        configParser = FinancialServicesConfigParser.getInstance();
        isPreInitiatedConsent = configParser.isPreInitiatedConsent();
    }

    @Override
    public Map<String, Object> updateRequestAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {
        Utils.formatBasicConsentData(dataSet);
        return Utils.returnAttributesFromDataSet(dataSet);
    }

    @Override
    public Map<String, Object> updateSessionAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {

        return Collections.emptyMap();
    }

    @Override
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getParameter("accounts[]")
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 1
    public Map<String, Object> updateConsentData(HttpServletRequest request) {

        Map<String, Object> returnMaps = new HashMap<>();

        String[] accounts = request.getParameter("accounts[]")
                .replaceAll(FinancialServicesConstants.NEW_LINE, StringUtils.EMPTY).split(":");
        returnMaps.put("accountIds", List.of(accounts));
        returnMaps.put(ConsentExtensionConstants.PAYMENT_ACCOUNT,
                request.getParameter(ConsentExtensionConstants.PAYMENT_ACCOUNT)
                        .replaceAll(FinancialServicesConstants.NEW_LINE, StringUtils.EMPTY));
        returnMaps.put(ConsentExtensionConstants.COF_ACCOUNT,
                request.getParameter(ConsentExtensionConstants.COF_ACCOUNT)
                        .replaceAll(FinancialServicesConstants.NEW_LINE, StringUtils.EMPTY));
        return returnMaps;
    }

    @Override
    public Map<String, String> updateConsentMetaData(HttpServletRequest request) {

        return Collections.emptyMap();
    }

    @Override
    public String getJSPPath() {

        return "/fs_default.jsp";
    }
}
