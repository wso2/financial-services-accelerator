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

package org.wso2.financial.services.accelerator.consent.mgt.endpoint.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentUtils;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.PATCH;

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
 * ConsentManageEndpoint.
 * <p>
 * This specifies a REST API for Open Banking specification based consent requests.
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
// Suppressed content - Endpoints
// Suppression reason - False Positive : These endpoints are secured with access control
// as defined in the IS deployment.toml file
// Suppressed warning count - 7
@Path("/manage")
public class ConsentManageEndpoint {

    private static final Log log = LogFactory.getLog(ConsentManageEndpoint.class);

    private static ConsentManageHandler consentManageHandler = null;
    private static final String CLIENT_ID_HEADER = "x-wso2-client-id";

    public ConsentManageEndpoint() {

        if (consentManageHandler == null) {
            initializeConsentManageHandler();
        }
    }

    private static void initializeConsentManageHandler() {

        ConsentManageBuilder consentManageBuilder = ConsentExtensionExporter.getConsentManageBuilder();

        if (consentManageBuilder != null) {
            consentManageHandler = consentManageBuilder.getConsentManageHandler();
        }
        if (consentManageHandler != null) {
            log.info(String.format("Consent manage handler %s initialized",
                    consentManageHandler.getClass().getName().replaceAll("\n\r", "")));
        } else {
            log.warn("Consent manage handler is null");
        }
    }

    /**
     * Consent GET requests.
     */
    @GET
    @Path("/{s:.*}")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response manageGet(@Context HttpServletRequest request, @Context HttpServletResponse response,
                              @Context UriInfo uriInfo) {

        ConsentManageData consentManageData = new ConsentManageData(ConsentUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getPathParameters().getFirst("s"), request, response);
        consentManageData.setClientId(consentManageData.getHeaders().get(CLIENT_ID_HEADER));
        consentManageHandler.handleGet(consentManageData);
        return sendResponse(consentManageData);
    }

    /**
     * Consent POST requests.
     */
    @POST
    @Path("/{s:.*}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response managePost(@Context HttpServletRequest request, @Context HttpServletResponse response,
                               @Context UriInfo uriInfo) {

        ConsentManageData consentManageData = new ConsentManageData(ConsentUtils.getHeaders(request),
                ConsentUtils.getPayload(request), uriInfo.getQueryParameters(),
                uriInfo.getPathParameters().getFirst("s"), request, response);
        consentManageData.setClientId(consentManageData.getHeaders().get(CLIENT_ID_HEADER));
        consentManageHandler.handlePost(consentManageData);
        return sendResponse(consentManageData);
    }

    /**
     * Consent DELETE requests.
     */
    @DELETE
    @Path("/{s:.*}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response manageDelete(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                 @Context UriInfo uriInfo) {

        ConsentManageData consentManageData = new ConsentManageData(ConsentUtils.getHeaders(request),
                ConsentUtils.getPayload(request), uriInfo.getQueryParameters(),
                uriInfo.getPathParameters().getFirst("s"), request, response);
        consentManageData.setClientId(consentManageData.getHeaders().get(CLIENT_ID_HEADER));
        consentManageHandler.handleDelete(consentManageData);
        return sendResponse(consentManageData);
    }

    /**
     * Consent PUT requests.
     */
    @PUT
    @Path("/{s:.*}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response managePut(@Context HttpServletRequest request, @Context HttpServletResponse response,
                              @Context UriInfo uriInfo) {

        ConsentManageData consentManageData = new ConsentManageData(ConsentUtils.getHeaders(request),
                ConsentUtils.getPayload(request), uriInfo.getQueryParameters(),
                uriInfo.getPathParameters().getFirst("s"), request, response);
        consentManageData.setClientId(consentManageData.getHeaders().get(CLIENT_ID_HEADER));
        consentManageHandler.handlePut(consentManageData);
        return sendResponse(consentManageData);
    }

    /**
     * Consent PATCH requests.
     */
    @PATCH
    @Path("/{s:.*}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response managePatch(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                @Context UriInfo uriInfo) {

        ConsentManageData consentManageData = new ConsentManageData(ConsentUtils.getHeaders(request),
                ConsentUtils.getPayload(request), uriInfo.getQueryParameters(),
                uriInfo.getPathParameters().getFirst("s"), request, response);
        consentManageData.setClientId(consentManageData.getHeaders().get(CLIENT_ID_HEADER));
        consentManageHandler.handlePatch(consentManageData);
        return sendResponse(consentManageData);
    }

    /**
     * Consent File Upload POST requests.
     */
    @POST
    @Path("/fileUpload/{s:.*}")
    @Consumes({"*/*"})
    @Produces({"application/json; charset=utf-8"})
    public Response manageFileUploadPost(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                         @Context UriInfo uriInfo) {

        ConsentManageData consentManageData = new ConsentManageData(ConsentUtils.getHeaders(request),
                ConsentUtils.getFileUploadPayload(request), uriInfo.getQueryParameters(),
                uriInfo.getPathParameters().getFirst("s"), request, response);
        consentManageData.setClientId(consentManageData.getHeaders().get(CLIENT_ID_HEADER));
        consentManageHandler.handleFileUploadPost(consentManageData);
        return sendFileUploadResponse(consentManageData);
    }

    /**
     * Consent File GET requests.
     */
    @GET
    @Path("/fileUpload/{s:.*}")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"*/*"})
    public Response manageFileGet(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                  @Context UriInfo uriInfo) {

        ConsentManageData consentManageData = new ConsentManageData(ConsentUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getPathParameters().getFirst("s"), request, response);
        consentManageData.setClientId(consentManageData.getHeaders().get(CLIENT_ID_HEADER));
        consentManageHandler.handleFileGet(consentManageData);
        return sendResponse(consentManageData);
    }

    /**
     * Method to send response using the payload and response status.
     * @param consentManageData  Consent manage data
     * @return Response
     */
    private Response sendResponse(ConsentManageData consentManageData) {
        if (consentManageData.getResponseStatus() != null &&
                ResponseStatus.NO_CONTENT == consentManageData.getResponseStatus()) {
            return Response.status(consentManageData.getResponseStatus().getStatusCode()).build();
        } else if (consentManageData.getResponsePayload() != null && consentManageData.getResponseStatus() != null) {
            return Response.status(consentManageData.getResponseStatus().getStatusCode()).
                    entity(consentManageData.getResponsePayload().toString()).build();
        } else {
            log.debug("Response status or payload unavailable. Throwing exception");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Response data unavailable");
        }
    }

    /**
     * Method to send file upload response using the payload and response status.
     * @param consentManageData  Consent manage data
     * @return Response
     */
    private Response sendFileUploadResponse(ConsentManageData consentManageData) {
        if (consentManageData.getPayload() != null || consentManageData.getResponseStatus() != null) {
            return Response.status(consentManageData.getResponseStatus().getStatusCode()).build();
        } else {
            log.debug("Response status or payload unavailable. Throwing exception");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Response data unavailable");
        }
    }
}
