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

package com.wso2.openbanking.accelerator.identity.token;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * Default token filter class to represent the abstract implementation.
 */
public class DefaultTokenFilter {

    private static final Log log = LogFactory.getLog(DefaultTokenFilter.class);

    /**
     * Handle filter request.
     *
     * @param request
     * @return ServletRequest
     * @throws ServletException
     */
    public ServletRequest handleFilterRequest(ServletRequest request) throws ServletException {
        return request;
    }

    /**
     * Handle filter response.
     *
     * @param response
     * @return ServletResponse
     * @throws ServletException
     */
    public ServletResponse handleFilterResponse(ServletResponse response) throws ServletException {

        return response;
    }

    /**
     * Respond when there is a failure in filter validation.
     *
     * @param response     HTTP servlet response object
     * @param status       HTTP status code
     * @param error        error
     * @param errorMessage error description
     * @throws IOException
     */
    public void handleValidationFailure(HttpServletResponse response, int status, String error, String errorMessage)
            throws IOException {

        JSONObject errorJSON = new JSONObject();
        errorJSON.put(IdentityCommonConstants.OAUTH_ERROR, error);
        errorJSON.put(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION, errorMessage);

        try (OutputStream outputStream = response.getOutputStream()) {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON);
            outputStream.write(errorJSON.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
