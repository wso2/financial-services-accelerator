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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet;

import org.json.JSONObject;

import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * The interface to define how the servlet extension should be implemented.
 */
public interface FSAuthServletInterface {

    /**
     * Method to update the request attributes.
     * @param request         Http Servlet Request
     * @param dataSet         Data Set containing required parameters
     * @param resourceBundle  Resource Bundle
     * @return Map of updated request attributes
     */
    Map<String, Object> updateRequestAttribute(HttpServletRequest request,
                                               JSONObject dataSet, ResourceBundle resourceBundle);

    /**
     * Method to update the session attributes.
     * @param request         Http Servlet Request
     * @param dataSet         Data Set containing required parameters
     * @param resourceBundle  Resource Bundle
     * @return Map of updated session attributes
     */
    Map<String, Object> updateSessionAttribute(HttpServletRequest request,
                                               JSONObject dataSet, ResourceBundle resourceBundle);

    /**
     * Method to update the consent data.
     * @param request  Http Servlet Request
     * @return  Map of updated consent data
     */
    Map<String, Object> updateConsentData(HttpServletRequest request);

    /**
     * Method to update the consent metadata.
     * @param request  Http Servlet Request
     * @return  Map of updated consent metadata
     */
    Map<String, String> updateConsentMetaData(HttpServletRequest request);

    /**
     * Method to get the JSP path.
     * @return
     */
    String getJSPPath();
}
