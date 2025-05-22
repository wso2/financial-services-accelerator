/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.mappers.exceptions;

import org.wso2.financial.services.accelerator.consent.mgt.api.dao.constants.ConsentError;

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
            error.put("code", ConsentError.PAYLOAD_SCHEMA_VALIDATION_ERROR.getCode());
            error.put("message", ConsentError.PAYLOAD_SCHEMA_VALIDATION_ERROR.getMessage());
            error.put("description",
                    ConsentError.PAYLOAD_SCHEMA_VALIDATION_ERROR.getDescription() + " Violation: " +
                            violation.getPropertyPath().toString() + " " +
                            violation.getMessage());

        }
        // Return a BAD_REQUEST response with the violation details in JSON format
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
