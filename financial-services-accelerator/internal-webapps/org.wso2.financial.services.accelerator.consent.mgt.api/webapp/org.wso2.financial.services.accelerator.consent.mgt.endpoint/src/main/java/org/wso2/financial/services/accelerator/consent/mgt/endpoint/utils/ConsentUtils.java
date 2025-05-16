package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.constants.ConsentConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;


/**
 * ConsentUtils.
 */
public class ConsentUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Response.Status getStatusFromErrorCode(String code) {

        if (code.endsWith("0")) {
            return Response.Status.BAD_REQUEST;
        } else if (code.endsWith("1")) {
            return Response.Status.NOT_FOUND;
        } else if (code.endsWith("9")) {
            return Response.Status.CONFLICT;
        } else {
            return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }
}
