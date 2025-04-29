package org.wso2.financial.services.accelerator.consent.mgt.endpoint.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.ConsentAPIImpl;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AmendmentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AmendmentResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentHistory;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceDTO;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.DetailedConsentResource;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


/**
 * ConsentApi
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
@Path("/consent")
@Tag(name = "Consent API", description = "Operations related to consents")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentApi {

    ConsentAPIImpl consentApiImpl = new ConsentAPIImpl();

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Consent Initiation", description = "\n" +
            "Creates ConsentResource, AuthorizationResource and ConsentMapping Entity in the database.\n" +
            "Creates Authorization resources provided in the authorizationResources array if implicitAuth flag is " +
            "true.\n",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body")
            })
    public Response consentPost(
            @Valid @NotNull ConsentResourceDTO consentResourceDTO,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentPost(consentResourceDTO, orgInfo);
    }

    @GET
    @Path("/{consentId}")
    @Produces("application/json")
    @Operation(summary = "Consent Retrieval",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = DetailedConsentResource.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    public Response consentConsentIdGet(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo,
            @QueryParam("isDetailedConsentResource") boolean isDetailedConsentResource,
            @QueryParam("withAttributes") @DefaultValue("true") boolean withAttributes) {

        return consentApiImpl.consentConsentIdGet(consentId, orgInfo, isDetailedConsentResource, withAttributes);
    }

    @GET
    @Produces("application/json")
    @Operation(summary = "Consent Search",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = DetailedConsentResource.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid consent id")
            })
    public Response consentGet(
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo,
            @QueryParam("consentTypes") String consentTypes,
            @QueryParam("consentStatuses") String consentStatuses,
            @QueryParam("clientIds") String clientIds,
            @QueryParam("userIds") String userIds,
            @QueryParam("fromTime") int fromTime,
            @QueryParam("toTime") int toTime,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset) {

        return consentApiImpl.consentGet(orgInfo, consentTypes, consentStatuses, clientIds, userIds, fromTime, toTime,
                limit, offset);
    }

    @DELETE
    @Path("/{consentId}")
    @Operation(summary = "Consent Purging",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successful operation"),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    public Response consentConsentIdDelete(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdDelete(consentId, orgInfo);
    }

    @PUT
    @Path("/{consentId}")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Consent Amendment",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = AmendmentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body")
            })
    public Response consentConsentIdPut(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull AmendmentResource amendmentResource,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdPut(consentId, amendmentResource, orgInfo);
    }

    @PATCH
    @Path("/{consentId}")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Consent Update",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body")
            })
    public Response consentConsentIdPatch(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull ConsentResourceDTO consentResourceDTO,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return Response.ok().build();
    }

    @PUT
    @Path("/{consentId}/status")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Consent Status Update",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid consent id")
            })
    public Response consentConsentIdStatusPut(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull ConsentStatusUpdateResource consentStatusUpdateResource,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdStatusPut(consentId, consentStatusUpdateResource, orgInfo);
    }

    @PUT
    @Path("/{consentId}/revoke")
    @Operation(summary = "Consent Revoke",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successful operation"),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    public Response consentRevokeConsentIdPut(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull ConsentRevokeResource consentRevokeResource,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentRevokeConsentIdPut(consentId, consentRevokeResource, orgInfo);
    }

    @PUT
    @Path("/status")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Bulk Consent Status Change",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid consent id")
            })
    public Response consentStatusPut(
            @Valid @NotNull BulkConsentStatusUpdateResource bulkConsentStatusUpdateResource,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentStatusPut(bulkConsentStatusUpdateResource, orgInfo);
    }

    @GET
    @Path("/{consentId}/authorizationResource/{authorizationId}")
    @Produces("application/json")
    @Operation(summary = "Retrieve Consent Authorization Resource",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = DetailedConsentResource.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    public Response consentAuthorizationIdGet(
            @PathParam("authorizationId") @Parameter(description = "Authorization Id") String authorizationId,
            @PathParam("consentId") @Parameter(description = "Consent Id") String consentId,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentAuthorizationIdGet(authorizationId, consentId, orgInfo);
    }

    // post authourizationResources
    @POST
    @Path("/{consentId}/authorizationResource")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Create Consent Authorization Resource",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body")
            })
    public Response consentAuthorizationResourcePost(
            @PathParam("consentId") @Parameter(description = "Consent Id") String consentId,
            @Valid @NotNull ConsentResourceDTO consentResourceDTO,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentAuthorizationResourcePost(consentId, consentResourceDTO, orgInfo);
    }


    @GET
    @Path("/{consentId}/history")
    @Produces("application/json")
    @Operation(summary = "Consent History Retrieval",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentHistory.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid consent id")
            })
    public Response consentConsentIdHistoryGet(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @HeaderParam("OrgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo,
            @QueryParam("detailed") boolean detailed,
            @QueryParam("status") String status,
            @QueryParam("actionBy") String actionBy,
            @QueryParam("fromTime") long fromTime,
            @QueryParam("toTime") long toTime,
            @QueryParam("statusAuditId") String statusAuditId) {

        return consentApiImpl.consentConsentIdHistoryGet(consentId, orgInfo, detailed, status, actionBy, fromTime,
                toTime, statusAuditId);
    }
}
