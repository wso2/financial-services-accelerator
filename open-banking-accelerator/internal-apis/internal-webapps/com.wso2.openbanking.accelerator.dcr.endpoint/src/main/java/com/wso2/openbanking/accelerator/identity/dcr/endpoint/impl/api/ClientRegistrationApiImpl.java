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

package com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.RegistrationConstants;
import com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.service.RegistrationServiceHandler;
import com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.util.RegistrationUtils;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.RegistrationValidator;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.oauth.dcr.exception.DCRMException;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Implementation class for the DCR API.
 */
@Path("/register")
public class ClientRegistrationApiImpl {

    private static final Log log = LogFactory.getLog(ClientRegistrationApiImpl.class);

    private final IdentityCommonHelper identityCommonHelper = new IdentityCommonHelper();
    private RegistrationServiceHandler registrationServiceHandler = new RegistrationServiceHandler();
    private static Gson gson = new Gson();

    @DELETE
    @Path("/{s:.*}")
    @Produces({"application/json; charset=utf-8"})
    public Response registerClientIdDelete(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                           @Context UriInfo uriInfo) {

        RegistrationValidator validator = RegistrationValidator.getRegistrationValidator();
        if (log.isDebugEnabled()) {
            log.debug("Invoking the configured registration validator:" + validator);
        }
        String clientId = uriInfo.getPathParameters().getFirst("s");

        try {
            validator.validateDelete(clientId);
            identityCommonHelper.revokeAccessTokensByClientId(clientId);
            return registrationServiceHandler.deleteRegistration(clientId);
        } catch (DCRMException e) {
            log.error("Error while deleting the application", e);
            if (e.getErrorCode().contains(RegistrationConstants.CLIENT_NOT_FOUND)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (DCRValidationException e) {
            log.error("Error occurred while validating request", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(RegistrationUtils.getErrorDTO(e.getErrorCode(),
                    e.getErrorDescription())).build();
        } catch (IdentityOAuth2Exception e) {
            log.error("Error occurred while revoking application access tokens", e);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(RegistrationUtils
                .getErrorDTO(RegistrationConstants.INTERNAL_SERVER_ERROR,
                        "Error occurred while deleting the application")).build();
    }

    @GET
    @Path("/{s:.*}")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response registerClientIdGet(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                        @Context UriInfo uriInfo) {

        try {
            String clientId = uriInfo.getPathParameters().getFirst("s");

            RegistrationValidator validator = RegistrationValidator.getRegistrationValidator();
            if (log.isDebugEnabled()) {
                log.debug("Invoking the configured registration validator:" + validator);
            }
            validator.validateGet(clientId);
            Map<String, String> headers =  getHeaders(request);
            String accessToken =  headers.get(RegistrationConstants.REGISTRATION_ACCESS_TOKEN);

            Map<String, Object> additionalAttributes = new HashMap<>();

            try {
                String tlsCert = new IdentityCommonHelper().encodeCertificateContent(
                        IdentityCommonUtil.getCertificateFromAttribute(
                                request.getAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE)));

                // add TLS cert as additional attribute
                additionalAttributes.put(IdentityCommonConstants.TLS_CERT, tlsCert);
            } catch (CertificateEncodingException e) {
                log.error("Certificate not valid", e);
            }

            return registrationServiceHandler.retrieveRegistration(additionalAttributes, clientId, accessToken);
        } catch (DCRMException e) {
            log.error("Error while retrieving application", e);
            if (e.getErrorCode().contains(RegistrationConstants.CLIENT_NOT_FOUND)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (IdentityApplicationManagementException e) {
            log.error("Error while retrieving Service Provider details", e);
        } catch (DCRValidationException e) {
            log.error("Error occurred while validating request", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(RegistrationUtils.getErrorDTO(e.getErrorCode(),
                    e.getErrorDescription())).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                RegistrationUtils.getErrorDTO(RegistrationConstants.INTERNAL_SERVER_ERROR,
                        "Error occurred while processing the request")).build();
    }

    @PUT
    @Path("/{s:.*}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response registerClientIdPut(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                        @Context UriInfo uriInfo) {

        //RegistrationRequest registrationRequest = RegistrationUtils.getRegistrationRequest(requestBody);

        RegistrationValidator registrationValidator = RegistrationValidator.getRegistrationValidator();
        if (log.isDebugEnabled()) {
            log.debug("Invoking the configured registration validator:" + registrationValidator);
        }
        try {
            JsonElement registrationRequestDetails = gson.toJsonTree(RegistrationUtils.getPayload(request));

            RegistrationRequest registrationRequest =
                    gson.fromJson(registrationRequestDetails, RegistrationRequest.class);

            Map<String, Object> requestAttributes = (Map<String, Object>)
                    gson.fromJson(registrationRequestDetails, Map.class);

            registrationRequest.setRequestParameters(requestAttributes);

            if (StringUtils.isNotEmpty(registrationRequest.getSoftwareStatement())) {
                //decode SSA if provided in the registration request
                String ssaBody = JWTUtils.decodeRequestJWT(registrationRequest.getSoftwareStatement(),
                                OpenBankingConstants.JWT_BODY)
                        .toString();
                Map<String, Object> ssaAttributesMap = gson.fromJson(ssaBody, Map.class);
                registrationRequest.setSsaParameters(ssaAttributesMap);
            }

            String clientId = uriInfo.getPathParameters().getFirst("s");
            RegistrationUtils.validateRegistrationCreation(registrationRequest);
            log.debug("Invoking specific validations");
            RegistrationValidator.getRegistrationValidator().validateUpdate(registrationRequest);
            Map<String, String> headers =  getHeaders(request);
            String accessToken =  headers.get(RegistrationConstants.REGISTRATION_ACCESS_TOKEN);

            Map<String, Object> additionalAttributes = new HashMap<>();

            try {
                String tlsCert = new IdentityCommonHelper().encodeCertificateContent(
                        IdentityCommonUtil.getCertificateFromAttribute(
                                request.getAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE)));

                // add TLS cert as additional attribute
                additionalAttributes.put(IdentityCommonConstants.TLS_CERT, tlsCert);
            } catch (CertificateEncodingException e) {
                log.error("Certificate not valid", e);
            }

            return registrationServiceHandler.
                    updateRegistration(registrationRequest, additionalAttributes, clientId, accessToken);
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
        } catch (DCRMException e) {
            log.error("Error occurred while creating the Service provider", e);
            if (e.getErrorCode().contains(RegistrationConstants.CLIENT_NOT_FOUND)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (IdentityApplicationManagementException e) {
            log.error("Error occurred while retrieving the Service provider details", e);
        } catch (DCRValidationException e) {
            log.error("Error occurred while validating request", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(RegistrationUtils.getErrorDTO(e.getErrorCode(),
                    e.getErrorDescription())).build();
        } catch (net.minidev.json.parser.ParseException | IOException e) {
            log.error("Error occurred while parsing the request", e);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                RegistrationUtils.getErrorDTO(RegistrationConstants.INTERNAL_SERVER_ERROR,
                        "Error occurred while processing the request")).build();

    }

    @POST
    @Path("/{s:.*}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response registerPost(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                 @Context UriInfo uriInfo) {

        RegistrationValidator registrationValidator = RegistrationValidator.getRegistrationValidator();
        //invoke the configured registration VALIDATOR
        if (log.isDebugEnabled()) {
            log.debug("Invoking the configured registration validator:" + registrationValidator);
        }
        try {
            JsonElement registrationRequestDetails = gson.toJsonTree(RegistrationUtils.getPayload(request));

            RegistrationRequest registrationRequest =
                    gson.fromJson(registrationRequestDetails, RegistrationRequest.class);

            Map<String, Object> requestAttributes = (Map<String, Object>)
                    gson.fromJson(registrationRequestDetails, Map.class);

            Map<String, Object> additionalAttributes = new HashMap<>();

            try {
                String tlsCert = new IdentityCommonHelper().encodeCertificateContent(
                        IdentityCommonUtil.getCertificateFromAttribute(
                                request.getAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE)));

                // add TLS cert as additional attribute
                additionalAttributes.put(IdentityCommonConstants.TLS_CERT, tlsCert);
            } catch (CertificateEncodingException e) {
                log.error("Certificate not valid", e);
            }

            registrationRequest.setRequestParameters(requestAttributes);
            if (StringUtils.isNotEmpty(registrationRequest.getSoftwareStatement())) {
                //decode SSA if provided in the registration request
                String ssaBody = JWTUtils.decodeRequestJWT(registrationRequest.getSoftwareStatement(),
                                OpenBankingConstants.JWT_BODY)
                        .toString();
                Map<String, Object> ssaAttributesMap = gson.fromJson(ssaBody, Map.class);
                registrationRequest.setSsaParameters(ssaAttributesMap);
            }

            RegistrationUtils.validateRegistrationCreation(registrationRequest);
            //do specific validations
            registrationValidator.validatePost(registrationRequest);
            return registrationServiceHandler.createRegistration(registrationRequest, additionalAttributes);
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
        } catch (DCRMException e) {
            log.error("Error occurred while creating the Service provider", e);
            if (DCRCommonConstants.DUPLICATE_APPLICATION_NAME.equalsIgnoreCase(e.getErrorCode())) {
                return Response.status(Response.Status.BAD_REQUEST).entity(RegistrationUtils
                                .getErrorDTO(DCRCommonConstants.INVALID_META_DATA, e.getErrorDescription()))
                        .build();
            }
        } catch (IdentityApplicationManagementException e) {
            log.error("Error occurred while retrieving the Service provider details", e);
        } catch (DCRValidationException e) {
            log.error("Error occurred while validating request", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(RegistrationUtils
                            .getErrorDTO(e.getErrorCode(), e.getErrorDescription()))
                    .build();
        } catch (net.minidev.json.parser.ParseException | IOException e) {
            log.error("Error occurred while parsing the request", e);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                RegistrationUtils.getErrorDTO(RegistrationConstants.INTERNAL_SERVER_ERROR,
                        "Error occurred while processing the request")).build();
    }

    /**
     * Extract headers from a request object.
     *
     * @param request The request object
     * @return Map of header key value pairs
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

}
