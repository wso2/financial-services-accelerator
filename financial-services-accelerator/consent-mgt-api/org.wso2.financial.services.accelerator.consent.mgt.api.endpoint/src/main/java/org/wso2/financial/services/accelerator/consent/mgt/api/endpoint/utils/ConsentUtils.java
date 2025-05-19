package org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.utils;

import javax.ws.rs.core.Response;


/**
 * ConsentUtils.
 */
public class ConsentUtils {

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
