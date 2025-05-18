package org.wso2.financial.services.accelerator.consent.mgt.endpoint.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.ConsentAPIImpl;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentExpiryTimeUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceResponseBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateRequestBody;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.Valid;
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
@Path("/consents")
@Api(description = "the consents API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-05-16T09:24:29.903308+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentsApi {
    ConsentAPIImpl consentApiImpl = new ConsentAPIImpl();

    @GET
    @Path("/{consentId}/authorization-resources")
    @Produces({"application/json"})
    @Operation(
            summary = "Retrieve Consent Authorization Resources",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Authorization Resource")
    public Response consentAuthorizationGet(
            @PathParam("consentId") @ApiParam("Consent id") String consentId,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId) {

        return consentApiImpl.consentAuthorizationGet(consentId, orgId);

    }

    @DELETE
    @Path("/{consentId}/authorization-resource/{authorizationId}")
    @Produces({"application/json"})
    @Operation(
            summary = "Delete Consent Authorization Resource",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Invalid consent id, or authorization resource id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Authorization Resource")
    public Response consentAuthorizationIdDelete(

            @PathParam("authorizationId") @ApiParam("Authorization Id") String authorizationId,
            @PathParam("consentId") @ApiParam("Consent Id") String consentId,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG") @ApiParam("JWT header containing tenant-related" +
                    " information") String orgId) {

        return consentApiImpl.consentAuthorizationIdDelete(authorizationId, consentId, orgId);

    }

    @GET
    @Path("/{consentId}/authorization-resource/{authorizationId}")
    @Produces({"application/json"})
    @Operation(
            summary = "Retrieve Consent Authorization Resource",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResourceResponseBody.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Authorization Resource")
    public Response consentAuthorizationIdGet(
            @PathParam("authorizationId") @ApiParam("Authorization Id") String authorizationId,
            @PathParam("consentId") @ApiParam("Consent Id") String consentId,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId) {

        return consentApiImpl.consentAuthorizationIdGet(authorizationId, consentId, orgId);

    }

    @POST
    @Path("/{consentId}/authorization-resource")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(
            summary = "Add Consent Authorization Resource",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Authorization Resource")
    public Response consentAuthorizationIdPost(
            @PathParam("consentId") @ApiParam("Consent id") String consentId,
            @Valid @NotNull List<@Valid AuthorizationResourceRequestBody> authorizationResourceRequestBody,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG") @ApiParam("JWT header containing tenant-related" +
                    " information") String orgId) {

        return consentApiImpl.consentAuthorizationIdPost(consentId, authorizationResourceRequestBody, orgId);

    }

    @PUT
    @Path("/{consentId}/authorization-resource/{authorizationId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(
            summary = "Update Consent Authorization Resource",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Authorization Resource")
    public Response consentAuthorizationIdPut(

            @PathParam("authorizationId") @ApiParam("Authorization Id") String authorizationId,
            @PathParam("consentId") @ApiParam("Consent Id") String consentId,
            @Valid @NotNull AuthorizationResourceRequestBody authorizationResourceRequestBody,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG") @ApiParam("JWT header containing tenant-related " +
                    "information") String orgId) {

        return consentApiImpl.consentAuthorizationIdPut(authorizationId, consentId, authorizationResourceRequestBody,
                orgId);

    }

    @DELETE
    @Path("/{consentId}")
    @Operation(
            summary = "Consent Purging",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Consent Resource")
    public Response consentConsentIdDelete(

            @PathParam("consentId") @ApiParam("Consent id") String consentId,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG") @ApiParam("JWT header containing " +
                    "tenant-related information") String orgId) {

        return consentApiImpl.consentConsentIdDelete(consentId, orgId);

    }

    @PUT
    @Path("/{consentId}/expiry-time")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(
            summary = "Consent Expiry Time Update (Seconds)",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Consent Resource")
    public Response consentConsentIdExpiryTimePut(
            @PathParam("consentId") @ApiParam("Consent id") String consentId,
            @Valid @NotNull ConsentExpiryTimeUpdateRequestBody consentExpiryTimeUpdateRequestBody,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId) {

        return consentApiImpl.consentConsentIdExpiryTimePut(consentId, consentExpiryTimeUpdateRequestBody, orgId);

    }

    @GET
    @Path("/{consentId}")
    @Produces({"application/json"})
    @Operation(
            summary = "Consent Retrieval",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResourceResponseBody.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Consent Resource")
    public Response consentConsentIdGet(
            @PathParam("consentId") @ApiParam("Consent id") String consentId,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId) {

        return consentApiImpl.consentConsentIdGet(consentId, orgId);

    }

    @PUT
    @Path("/{consentId}/status")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(
            summary = "Consent Status Update",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Consent Resource")
    public Response consentConsentIdStatusPut(
            @PathParam("consentId") @ApiParam("Consent id") String consentId,
            @Valid @NotNull ConsentStatusUpdateRequestBody consentStatusUpdateRequestBody,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId) {

        return consentApiImpl.consentConsentIdStatusPut(consentId, consentStatusUpdateRequestBody, orgId);

    }

    @GET
    @Produces({"application/json"})
    @Operation(
            summary = "Consent Search",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResourceResponseBody.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Consent Resource")
    public Response consentGet(
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId,
            @QueryParam("consentTypes") String consentTypes,
            @QueryParam("consentStatuses") String consentStatuses,
            @QueryParam("clientIds") String clientIds,
            @QueryParam("userIds") String userIds,
            @QueryParam("fromTime") int fromTime,
            @QueryParam("toTime") int toTime,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset) {

        return consentApiImpl.consentGet(orgId, consentTypes, consentStatuses, clientIds, userIds, fromTime, toTime,
                limit, offset);

    }

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(
            summary = "Consent Initiation",
            description = " Creates ConsentResource, AuthorizationResource and ConsentMapping Entity in the database." +
                    " Creates Authorization resources provided in the authorizationResources array if implicitAuth " +
                    "flag is true. ",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResourceResponseBody.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request body",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Consent Resource")
    public Response consentPost(
            @Valid @NotNull ConsentResourceRequestBody consentResourceRequestBody,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId) {

        return consentApiImpl.consentPost(consentResourceRequestBody, orgId);

    }

    @POST
    @Path("/{consentId}/revoke")
    @Consumes({"application/json"})
    @Operation(
            summary = "Consent Revoke",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Consent Resource")
    public Response consentRevokeConsentIdPost(
            @PathParam("consentId") @ApiParam("Consent id") String consentId,
            @Valid @NotNull ConsentRevokeRequestBody consentRevokeRequestBody,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId) {

        return consentApiImpl.consentRevokeConsentIdPost(consentId, consentRevokeRequestBody, orgId);

    }

    @PUT
    @Path("/status")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(
            summary = "Bulk Consent Status Change",
            description = "",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid consent id",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @Tag(name = "Consent Resource")
    public Response consentStatusPut(
            @Valid @NotNull BulkConsentStatusUpdateResourceRequestBody bulkConsentStatusUpdateResourceRequestBody,
            @HeaderParam("orgId") @DefaultValue("DEFAULT_ORG")
            @ApiParam("JWT header containing tenant-related information") String orgId) {

        return consentApiImpl.consentStatusPut(bulkConsentStatusUpdateResourceRequestBody, orgId);

    }
}
