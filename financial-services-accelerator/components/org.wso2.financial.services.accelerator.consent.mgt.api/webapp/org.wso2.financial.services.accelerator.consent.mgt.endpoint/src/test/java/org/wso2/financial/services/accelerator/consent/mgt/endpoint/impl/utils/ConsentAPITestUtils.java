package org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.utils;

import org.json.JSONObject;

import javax.ws.rs.core.Response;

public class ConsentAPITestUtils {
    public static String parseErrorMessage(Response response) {
        String jsonResponse = response.getEntity().toString();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        return jsonObject.getString("errorMessage");
    }


}
