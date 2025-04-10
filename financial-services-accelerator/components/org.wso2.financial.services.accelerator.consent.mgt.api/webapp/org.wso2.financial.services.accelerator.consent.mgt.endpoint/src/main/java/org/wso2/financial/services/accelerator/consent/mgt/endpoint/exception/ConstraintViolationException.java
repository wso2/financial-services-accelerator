package org.wso2.financial.services.accelerator.consent.mgt.endpoint.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception mapper for ConstraintViolationException.
 */
@Provider
public class ConstraintViolationException implements ExceptionMapper<MismatchedInputException> {

    @Override
    public Response toResponse(MismatchedInputException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error",
                exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(response)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


}
