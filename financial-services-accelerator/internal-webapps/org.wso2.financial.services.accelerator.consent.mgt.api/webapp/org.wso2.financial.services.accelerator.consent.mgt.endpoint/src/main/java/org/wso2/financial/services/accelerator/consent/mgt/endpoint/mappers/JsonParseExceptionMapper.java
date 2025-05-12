package org.wso2.financial.services.accelerator.consent.mgt.endpoint.mappers;

import com.fasterxml.jackson.core.JsonParseException;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.constants.ConsentConstant;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 * Invalid JSON syntax Exception Mapper
 */
@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {
    @Override
    public Response toResponse(JsonParseException ex) {
        {


            Map<String, String> error = new LinkedHashMap<>();
            error.put("code", ConsentConstant.PAYLOAD_SCHEMA_VALIDATION_ERROR);
            error.put("message", "Invalid request payload");
            error.put("description", ex.getOriginalMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
