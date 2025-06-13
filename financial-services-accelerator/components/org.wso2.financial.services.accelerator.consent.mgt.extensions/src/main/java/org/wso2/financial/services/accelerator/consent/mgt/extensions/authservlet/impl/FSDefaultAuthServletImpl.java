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

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.FSAuthServletInterface;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils.Utils;

import java.util.Collections;
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
        // Format all entries of basic consent data to be JSONArrays
        Utils.formatBasicConsentData(dataSet);

        // Append encoded accounts permissions data to build persist payload in ConsentConfirmServlet
        Utils.appendEncodedAccountsPermissionsData(dataSet);

        // Extracts a map of attributes to push to the JSP
        return Utils.returnAttributesFromDataSet(dataSet);
    }

    @Override
    public Map<String, Object> updateSessionAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {

        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> updateConsentData(HttpServletRequest request) {

        // Builds response map to be forwarded to persistence
        return Utils.buildResponseMap(request);
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
