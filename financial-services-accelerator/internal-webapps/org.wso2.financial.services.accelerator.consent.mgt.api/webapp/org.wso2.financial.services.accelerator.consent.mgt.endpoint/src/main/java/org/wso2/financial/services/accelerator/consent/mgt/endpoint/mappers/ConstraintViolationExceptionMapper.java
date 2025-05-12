package org.wso2.financial.services.accelerator.consent.mgt.endpoint.mappers;

import org.wso2.financial.services.accelerator.consent.mgt.endpoint.constants.ConsentConstant;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 * Exception mapper for ConstraintViolationException.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        // Get all constraint violations
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        Map<String, String> error = new LinkedHashMap<>();

        if (!violations.isEmpty()) {

            ConstraintViolation<?> violation = violations.iterator().next();
            error.put("code", ConsentConstant.PAYLOAD_SCHEMA_VALIDATION_ERROR);
            error.put("message", "Invalid request payload");
            error.put("description", "Please ensure the JSON structure matches the expected " +
                    "schema. Violation: " + violation.getPropertyPath().toString() + " " +
                    violation.getMessage());


        }
        // Return a BAD_REQUEST response with the violation details in JSON format
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
