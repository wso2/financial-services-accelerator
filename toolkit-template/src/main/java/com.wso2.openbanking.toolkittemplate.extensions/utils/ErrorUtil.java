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

package com.wso2.openbanking.toolkittemplate.extensions.utils;

import com.wso2.openbanking.toolkittemplate.extensions.generated.model.ErrorResponse;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.FailedResponse;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.SuccessResponse;
import org.json.JSONObject;


/**
 * Util class for Error Handling.
 */
public class ErrorUtil {

    /**
     * Build a failed response object.
     *
     * @param errorCode error code of the failure
     * @param data additional error details
     * @return JSON object representing the failed response
     */
    public static JSONObject buildFailedResponse(Integer errorCode, JSONObject data) {
        FailedResponse failedResponse = new FailedResponse();
        failedResponse.setStatus(FailedResponse.StatusEnum.ERROR);
        failedResponse.setErrorCode(errorCode);
        failedResponse.setData(data);

        return new JSONObject(failedResponse);
    }


    /**
     * Get formatted error object for internal errors.
     *
     * @return JSONObject representing the error
     */
    public static JSONObject getFormattedErrorResponse(JSONObject data) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(ErrorResponse.StatusEnum.ERROR);
        errorResponse.setData(data);
        return new JSONObject(errorResponse);
    }


    /**
     * Method to get ErrorResponse object for error.
     *
     * @param errorMessage error message
     * @param errorDescription error description
     * @return built error response object
     */

    public static ErrorResponse getErrorResponse(String errorMessage, String errorDescription) {
        return new ErrorResponse(ErrorResponse.StatusEnum.ERROR, getErrorDataObject(errorMessage, errorDescription));
    }

//    (Method to construct the error response. UK)
//    public static JSONObject getErrorResponse(Integer errorCode, String errorMessage, String errorDescription) {
//        return buildFailedResponse(errorCode, getErrorDataObject(errorMessage, errorDescription));
//    }



    /**
     * Method to construct the error data object.
     *
     * @param errorMessage     Error Message
     * @param errorDescription Error Description
     * @return JSON object containing error message and description
     */
    public static JSONObject getErrorDataObject(String errorMessage, String errorDescription) {

        JSONObject data = new JSONObject();
        data.put("errorMessage", errorMessage);
        data.put("errorDescription", errorDescription);

        return data;
    }

    /**
     * Method to construct the consent manage success response.
     *
     * @param requestId unique request identifier
     * @return JSON object representing the success response
     */
    public static JSONObject getSuccessResponse(String requestId) {

        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setResponseId(requestId);
        successResponse.setStatus(SuccessResponse.StatusEnum.SUCCESS);

        return new JSONObject(successResponse);
    }

}
