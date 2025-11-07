package com.wso2.openbanking.toolkittemplate.extensions.utils;

import org.json.JSONObject;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.SuccessResponse;

public class ErrorUtil {

    /**
     * Method to construct the error data object.
     *
     * @param errorMessage     Error Message
     * @param errorDescription Error Description
     * @return
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
     * @param requestId
     * @return
     */
    public static JSONObject getSuccessResponse(String requestId) {

        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setResponseId(requestId);
        successResponse.setStatus(SuccessResponse.StatusEnum.SUCCESS);

        return new JSONObject(successResponse);
    }

}
