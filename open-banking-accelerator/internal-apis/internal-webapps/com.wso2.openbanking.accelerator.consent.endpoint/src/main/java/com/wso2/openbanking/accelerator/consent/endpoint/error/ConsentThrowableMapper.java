/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.consent.endpoint.error;

import com.wso2.openbanking.accelerator.consent.endpoint.util.ConsentConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Map exceptions to custom error format.
 */
public class ConsentThrowableMapper implements ExceptionMapper<Throwable> {

    private static final Log log = LogFactory.getLog(ConsentThrowableMapper.class);
    private static String defaultError = "A runtime error has occurred while handling the request";

    @Override
    public Response toResponse(Throwable throwable) {

        if (throwable instanceof ConsentException) {
            if (((ConsentException) throwable).getErrorRedirectURI() != null) {
                return Response.status(((ConsentException) throwable).getStatus().getStatusCode()).
                        location(((ConsentException) throwable).getErrorRedirectURI()).build();
            } else {
                return Response.status(((ConsentException) throwable).getStatus().getStatusCode())
                        .entity(((ConsentException) throwable).getPayload().toJSONString())
                        .header(ConsentConstants.HEADER_CONTENT_TYPE, ConsentConstants.DEFAULT_RESPONSE_CONTENT_TYPE)
                        .build();
            }
        } else {
            log.error("Generic exception. Cause: " + throwable.getMessage(), throwable);
            if (throwable instanceof ClientErrorException) {
                return toResponse(new ConsentException(ResponseStatus.fromStatusCode(((ClientErrorException) throwable)
                        .getResponse().getStatus()), throwable.getMessage()));
            } else {
                return toResponse(new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, defaultError));
            }
        }
    }
}
