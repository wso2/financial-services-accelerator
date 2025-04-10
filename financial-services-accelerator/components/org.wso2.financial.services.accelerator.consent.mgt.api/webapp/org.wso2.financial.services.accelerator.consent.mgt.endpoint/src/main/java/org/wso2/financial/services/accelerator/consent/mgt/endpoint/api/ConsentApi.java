package org.wso2.financial.services.accelerator.consent.mgt.endpoint.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.ConsentAPIImpl;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AmendmentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AmendmentResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentHistory;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceDTO;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.DetailedConsentResource;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Represents a collection of functions to interact with the API endpoints.
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
// Suppressed content - Endpoints
// Suppression reason - False Positive : These endpoints are secured with access
// control
@Path("/consent")
@Api(description = "the consent API")
@javax.annotation.Generated
        (value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-03-03T09:27:49.560668411+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentApi {
    ConsentAPIImpl consentAPIImpl = new ConsentAPIImpl();


    @DELETE
    @Path("/{consentId}")
    @ApiOperation(value = "Consent purging", notes = "", response = Void.class, tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation", response = Void.class),
            @ApiResponse(code = 404, message = "Invalid consent id", response = Void.class)
    })
    public Response consentConsentIdDelete(

            @PathParam("consentId") @ApiParam("consent id") String consentId,

            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo,
            @QueryParam("userId") String userId

                                          ) {
        try {
            return Response.ok().entity(consentAPIImpl.consentConsentIdDelete(consentId, orgInfo, userId))
                    .build();
        } catch (Exception e) {
            // Handle other errors
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{consentId}")
    @Produces({"application/json"})
    @ApiOperation(value = "Consent retrieval", notes = "", response = DetailedConsentResource.class, tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = DetailedConsentResource.class),
            @ApiResponse(code = 404, message = "Invalid consent id", response = Void.class)
    })
    public Response consentConsentIdGet(

            @PathParam("consentId") @ApiParam("consent id") String consentId,
            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo,
            @QueryParam("isDetailedConsentResource")  boolean isDetailedConsentResource,
            @QueryParam("withAttributes") @DefaultValue("true") boolean withAttributes

                                       ) {
        try {
            return consentAPIImpl.consentConsentIdGet(consentId, orgInfo, isDetailedConsentResource,
                    withAttributes);
        } catch (Exception e) {
            // Handle other errors
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{consentId}/authorizationResource/{authorizationId}")
    @Produces({"application/json"})
    @ApiOperation(value = "Consent retrieval", notes = "", response = DetailedConsentResource.class, tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = DetailedConsentResource.class),
            @ApiResponse(code = 404, message = "Invalid consent id", response = Void.class)
    })
    public Response consentAuthorizationIdGet(

            @PathParam("authorizationId") @ApiParam("authorization Id") String authorizationId,
            @PathParam("consentId") @ApiParam("consent Id") String consentId,
            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo


                                             ) {
        return consentAPIImpl.consentAuthorizationAuthorizationIdGet(authorizationId, orgInfo, consentId);

    }

    @GET
    @Path("/{consentId}/history")
    @Produces({"application/json"})
    @ApiOperation(value = "get consent history", notes = "", response = ConsentHistory.class,
            responseContainer = "List", tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ConsentHistory.class,
                    responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid consent id", response = Void.class)
    })
    public Response consentConsentIdHistoryGet(

            @PathParam("consentId") @ApiParam("consent id") String consentId,
            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo,
            @QueryParam("detailed") boolean detailed,
            @QueryParam("status") @ApiParam("status") String status,
            @QueryParam("actionBy") @ApiParam("actionBy") String actionBy,
            @QueryParam("fromTime") @ApiParam("fromTime") long fromTime,
            @QueryParam("toTime") @ApiParam("toTime") long toTime,
            @QueryParam("statusAuditId") @ApiParam("statusAuditId") String statusAuditId

                                              ) {
        return consentAPIImpl.consentConsentIdHistoryGet(consentId, orgInfo, detailed, status,
                actionBy, fromTime, toTime, statusAuditId);

    }

    @PUT
    @Path("/{consentId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Consent amendment", notes = "", response = AmendmentResponse.class, tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = AmendmentResponse.class),
            @ApiResponse(code = 400, message = "Invalid request body", response = Void.class)
    })
    public Response consentConsentIdPut(

            @PathParam("consentId") @ApiParam("consent id") String consentId,
            @Valid @NotNull AmendmentResource amendmentResource,
            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo) {

        return consentAPIImpl.consentConsentIdPut(consentId, amendmentResource, orgInfo);

    }

    @PUT
    @Path("/{consentId}/status")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Consent status update", notes = "", response = String.class, tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 400, message = "Invalid consent id", response = Void.class)
    })
    public Response consentConsentIdStatusPut(

            @PathParam("consentId") @ApiParam("consent id") String consentId,
            @Valid @NotNull ConsentStatusUpdateResource consentStatusUpdateResource,
            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo) {

        return consentAPIImpl.consentConsentIdStatusPut(consentId, consentStatusUpdateResource,
                orgInfo);

    }

    @GET
    @Produces({"application/json"})
    @ApiOperation(value = "consent search", notes = "", response = DetailedConsentResource.class,
            responseContainer = "List", tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = DetailedConsentResource.class,
                    responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid consent id", response = Void.class)
    })
    public Response consentGet(

            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo,
            @QueryParam("consentTypes") String consentTypes,
            @QueryParam("consentStatuses") String consentStatuses,
            @QueryParam("clientIds") String clientIds,
            @QueryParam("userIds") String userIds,
            @QueryParam("fromTime") int fromTime,
            @QueryParam("toTime") int toTime,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset

                              ) {
        return consentAPIImpl.consentGet(orgInfo, consentTypes, consentStatuses, userIds,
                fromTime,
                toTime, limit, offset);
    }

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Consent initiation", notes = "", response = ConsentResponse.class, tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ConsentResponse.class),
            @ApiResponse(code = 400, message = "Invalid request body", response = Void.class)
    })
    public Response consentPost(

            @Valid @NotNull ConsentResourceDTO consentResource,
            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo,
            @HeaderParam("isImplicitAuth") @ApiParam("Flag to determine whether authorization is implicit or not")
            boolean isImplicitAuth,
            @HeaderParam("isExclusiveConsent") @ApiParam("Flag to determine whether this is an exclusive consent")
            boolean exclusiveConsent) {

        return consentAPIImpl.consentPost(consentResource, orgInfo, isImplicitAuth,
                exclusiveConsent);

    }

    @PUT
    @Path("/status")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Bulk consent status change", notes = "", response = String.class, tags = {"consent"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 400, message = "Invalid consent id", response = Void.class)
    })
    public Response consentStatusPut(

            @Valid @NotNull BulkConsentStatusUpdateResource bulkConsentStatusUpdateResource,
            @HeaderParam("OrgInfo") @ApiParam("jwt header containing tenant related information")
            @DefaultValue("DEFAULT_ORG") String orgInfo) {

        return consentAPIImpl.consentStatusPut(bulkConsentStatusUpdateResource, orgInfo);


    }
}
