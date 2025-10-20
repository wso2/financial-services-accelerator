/**
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.consent.endpoint.api;

import com.wso2.openbanking.accelerator.consent.endpoint.util.ConsentUtils;
import com.wso2.openbanking.accelerator.consent.extensions.admin.builder.ConsentAdminBuilder;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminData;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminHandler;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionExporter;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * ConsentSearchEndpoint.
 * <p>
 * This specifies a RESTful API for consent search to be used at consent user and customer service portals.
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
// Suppressed content - Endpoints
// Suppression reason - False Positive : These endpoints are secured with access control
// as defined in the IS deployment.toml file
// Suppressed warning count - 7
@Path("/admin")
public class ConsentAdminEndpoint {

    private static final Log log = LogFactory.getLog(ConsentAdminEndpoint.class);

    private static ConsentAdminHandler consentAdminHandler = null;

    public ConsentAdminEndpoint() {
        if (consentAdminHandler == null) {
            initializeConsentAdminHandler();
        }
    }

    private static void initializeConsentAdminHandler() {
        ConsentAdminBuilder consentAdminBuilder = ConsentExtensionExporter.getConsentAdminBuilder();

        if (consentAdminBuilder != null) {
            consentAdminHandler = consentAdminBuilder.getConsentAdminHandler();
        }

        if (consentAdminHandler != null) {
            log.info("Consent admin handler " + consentAdminHandler.getClass().getName() + "initialized");
        } else {
            log.warn("Consent admin handler is null");
        }
    }

    /**
     * Search consent data.
     */
    @GET
    @Path("/search")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response search(@Context HttpServletRequest request, @Context HttpServletResponse response,
                           @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentExtensionUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleSearch(consentAdminData);
        return sendResponse(consentAdminData);
    }


    /**
     * Search consent status audit records.
     */
    @GET
    @Path("/search/consent-status-audit")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response searchConsentStatusAudit(@Context HttpServletRequest request, @Context HttpServletResponse response,
                           @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentExtensionUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleConsentStatusAuditSearch(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Search consent file.
     */
    @GET
    @Path("/search/consent-file")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response searchConsentFile(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                             @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentExtensionUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleConsentFileSearch(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Get consent amendment history.
     */
    @GET
    @Path("/consent-amendment-history")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response getConsentAmendmentHistoryById(@Context HttpServletRequest request,
                           @Context HttpServletResponse response, @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentExtensionUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleConsentAmendmentHistoryRetrieval(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Revoke consent data.
     */
    @DELETE
    @Path("/revoke")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response revoke(@Context HttpServletRequest request, @Context HttpServletResponse response,
                           @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentExtensionUtils.getHeaders(request),
                ConsentUtils.getJSONObjectPayload(request), uriInfo.getQueryParameters(),
                uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleRevoke(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Invoke consent expiration task.
     */
    @GET
    @Path("/expire-consents")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response expireConsents(@Context HttpServletRequest request,
                                                   @Context HttpServletResponse response, @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentExtensionUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleConsentExpiry(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Invoke retention data db sync task.
     */
    @GET
    @Path("/sync-temporary-retention-data")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response syncTemporaryRetentionData(@Context HttpServletRequest request,
                                               @Context HttpServletResponse response, @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentExtensionUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleTemporaryRetentionDataSyncing(consentAdminData);
        return sendResponse(consentAdminData);
    }

    private Response sendResponse(ConsentAdminData consentAdminData) {
        if (consentAdminData.getPayload() != null || consentAdminData.getResponseStatus() != null) {
            return Response.status(consentAdminData.getResponseStatus().getStatusCode()).
                    entity(consentAdminData.getResponsePayload()).build();
        } else {
            log.debug("Response status or payload unavailable. Throwing exception");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Response data unavailable");
        }
    }
}
