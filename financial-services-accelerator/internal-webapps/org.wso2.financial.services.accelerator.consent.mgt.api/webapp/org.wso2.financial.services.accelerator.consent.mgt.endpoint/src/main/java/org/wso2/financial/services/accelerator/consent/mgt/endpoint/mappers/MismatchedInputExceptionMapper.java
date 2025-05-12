package org.wso2.financial.services.accelerator.consent.mgt.endpoint.mappers;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.constants.ConsentConstant;


import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 * Exception mapper for ConstraintViolationException.
 */
@Provider
public class MismatchedInputExceptionMapper implements ExceptionMapper<MismatchedInputException> {

    @Override
    public Response toResponse(MismatchedInputException exception) {


        Map<String, String> error = new LinkedHashMap<>();
        error.put("errorCode", ConsentConstant.PAYLOAD_SCHEMA_VALIDATION_ERROR);
        error.put("message", "Invalid request payload");
        error.put("description", "Please ensure the JSON structure matches the expected " +
                "schema. " + "Missing required field: " + exception.getPathReference().replaceAll("([a-zA-Z0" +
                "-9_]+\\.)" +
                "+[A-Z][a-zA-Z0-9_]+", ""));

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


}
