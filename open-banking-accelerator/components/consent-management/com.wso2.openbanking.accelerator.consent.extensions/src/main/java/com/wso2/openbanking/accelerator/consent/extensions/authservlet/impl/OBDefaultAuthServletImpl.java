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

package com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl;

import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.util.Utils;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * The default implementation for OB flow.
 */
public class OBDefaultAuthServletImpl implements OBAuthServletInterface {

    private String jspPath;
    @Override
    public Map<String, Object> updateRequestAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {

        String consentType = dataSet.getString("type");

        //store consent type in a global variable to return required jsp file in getJSPPath() method
        jspPath = consentType;

        switch (consentType) {

            case ConsentExtensionConstants.ACCOUNTS:
                return Utils.populateAccountsData(request, dataSet);
            case ConsentExtensionConstants.PAYMENTS:
                return Utils.populatePaymentsData(request, dataSet);
            case ConsentExtensionConstants.FUNDSCONFIRMATIONS:
                return Utils.populateCoFData(request, dataSet);
            case ConsentExtensionConstants.VRP:
                return Utils.populateVRPDataRetrieval(request, dataSet);
            default:
                return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> updateSessionAttribute(HttpServletRequest request, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {

        return new HashMap<>();
    }

    @Override
    public Map<String, Object> updateConsentData(HttpServletRequest request) {

        Map<String, Object> returnMaps = new HashMap<>();

        String[] accounts = request.getParameter("accounts[]").split(":");
        returnMaps.put("accountIds", new JSONArray(accounts));
        returnMaps.put(ConsentExtensionConstants.PAYMENT_ACCOUNT,
                request.getParameter(ConsentExtensionConstants.PAYMENT_ACCOUNT));
        returnMaps.put(ConsentExtensionConstants.COF_ACCOUNT,
                request.getParameter(ConsentExtensionConstants.COF_ACCOUNT));

        return returnMaps;
    }

    @Override
    public Map<String, String> updateConsentMetaData(HttpServletRequest request) {

        return new HashMap<>();
    }

    @Override
    public String getJSPPath() {

        if (jspPath.equalsIgnoreCase(ConsentExtensionConstants.ACCOUNTS)) {
            return "/ob_default.jsp";
        } else if (jspPath.equalsIgnoreCase(ConsentExtensionConstants.VRP)) {
            return "/ob_default.jsp";
        } else {
            return "/default_displayconsent.jsp";
        }

    }
}
