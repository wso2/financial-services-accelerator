/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.endpoint.error;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentOperationEnum;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;

import java.util.UUID;

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
                return Response.status(((ConsentException) throwable).getStatus().getStatusCode())
                        .location(((ConsentException) throwable).getErrorRedirectURI()).build();
            }

            if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.MAP_CUSTOM_ERROR_RESPONSE)) {
                // Perform FS customized error response mapping with service extension
                try {
                    JSONObject payload = ((ConsentException) throwable).getPayload();
                    if (!isConsentManageException(payload)) {
                        return Response.status(((ConsentException) throwable).getStatus().getStatusCode())
                                .entity(((ConsentException) throwable).getPayload().toString())
                                .header(ConsentConstants.HEADER_CONTENT_TYPE,
                                        ConsentConstants.DEFAULT_RESPONSE_CONTENT_TYPE)
                                .build();
                    }

                    return customErrorResponseWithServiceExtension(payload);
                } catch (FinancialServicesException e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(e.getMessage())
                            .header(ConsentConstants.HEADER_CONTENT_TYPE,
                                    ConsentConstants.DEFAULT_RESPONSE_CONTENT_TYPE)
                            .build();
                }
            }

            return Response.status(((ConsentException) throwable).getStatus().getStatusCode())
                    .entity(((ConsentException) throwable).getPayload().toString())
                    .header(ConsentConstants.HEADER_CONTENT_TYPE,
                            ConsentConstants.DEFAULT_RESPONSE_CONTENT_TYPE)
                    .build();
        } else {
            log.error(String.format("Generic exception. Cause: %s", throwable.getMessage().replaceAll("[\r\n]", "")),
                    throwable);
            if (throwable instanceof ClientErrorException) {
                return toResponse(new ConsentException(ResponseStatus.fromStatusCode(((ClientErrorException) throwable)
                        .getResponse().getStatus()), throwable.getMessage()));
            } else {
                return toResponse(new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, defaultError));
            }
        }
    }

    private Response customErrorResponseWithServiceExtension(JSONObject payload) throws FinancialServicesException {

        // Construct the payload
        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(UUID.randomUUID().toString(),
                payload);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.MAP_CUSTOM_ERROR_RESPONSE);

        return Response.status(Integer.parseInt(response.getErrorCode()))
                .entity(response.getData().toString())
                .header(ConsentConstants.HEADER_CONTENT_TYPE,
                        ConsentConstants.DEFAULT_RESPONSE_CONTENT_TYPE)
                .build();
    }

    private boolean isConsentManageException(JSONObject payload) {
        JSONArray errorsArray = payload.getJSONArray(ConsentExtensionConstants.ERRORS);

        for (int i = 0; i < errorsArray.length(); i++) {
            JSONObject error = errorsArray.getJSONObject(i);
            String message = error.getString(ConsentExtensionConstants.ERROR_MSG);

            for (ConsentOperationEnum operationEnum : ConsentOperationEnum.values()) {
                if (message.contains(operationEnum.toString())) {
                    // update the "message" key with "operation"
                    error.remove(ConsentExtensionConstants.ERROR_MSG);
                    error.put(ConsentExtensionConstants.ERROR_MSG, message);
                    return true;
                }

            }
        }
        return false;
    }

}
