package org.wso2.financial.services.accelerator.consent.mgt.endpoint.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.ConsentAPIImpl;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentExpiryTimeUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentHistoryResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceResponseBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateRequestBody;

import java.util.List;
import java.util.Map;

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
 * ConsentApi
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
@Path("/consent")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ConsentApi {

    ConsentAPIImpl consentApiImpl = new ConsentAPIImpl();


    // ------------------------------ Authorization Resource ------------------------------ //
    @GET
    @Path("/{consentId}/authorizationResource/{authorizationId}")
    @Produces("application/json")
    @Operation(summary = "Retrieve Consent Authorization Resource",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResourceResponseBody.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "authorization resource")
    public Response consentAuthorizationIdGet(
            @PathParam("authorizationId") @Parameter(description = "Authorization Id") String authorizationId,
            @PathParam("consentId") @Parameter(description = "Consent Id") String consentId,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);
    }

    @GET
    @Path("/{consentId}/authorization-resources")
    @Produces("application/json")
    @Operation(summary = "Retrieve Consent Authorization Resources",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "authorization resource")
    public Response consentAuthorizationIdGet(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentAuthorizationGet(consentId, orgInfo);
    }


    @POST
    @Path("/{consentId}/authorizationResource")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Add Consent Authorization Resource",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "authorization resource")
    public Response consentAuthorizationIdPost(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull List<AuthorizationResourceRequestBody> authorizationResourceDTOList,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentAuthorizationIdPost(consentId, orgInfo, authorizationResourceDTOList);
    }

    @PUT
    @Path("/{consentId}/authorizationResource/{authorizationId}")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Update Consent Authorization Resource",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "authorization resource")
    public Response consentAuthorizationIdPut(
            @PathParam("authorizationId") @Parameter(description = "Authorization Id") String authorizationId,
            @PathParam("consentId") @Parameter(description = "Consent Id") String consentId,
            @Valid @NotNull AuthorizationResourceRequestBody authorizationResource,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentAuthorizationIdPut(authorizationId, consentId, orgInfo, authorizationResource);
    }


    @GET
    @Path("/{consentId}/attributes")
    @Produces("application/json")
    @Operation(summary = "Retrieve Consent Attributes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "consent atttributes")
    public Response consentConsentIdAttributesGet(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdAttributesGet(consentId, orgInfo);
    }

    @POST
    @Path("/{consentId}/attributes")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Add Consent Attributes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "consent atttributes")
    public Response consentConsentIdAttributesPost(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull Map<String, String> consentAttributes,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdAttributesPost(consentId, orgInfo, consentAttributes);
    }


    @PUT
    @Path("/{consentId}/attributes")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Update Consent Attributes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "consent atttributes")
    public Response consentConsentIdAttributesPut(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull Map<String, String> consentAttributes,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdAttributesPut(consentId, orgInfo, consentAttributes);
    }


    // ------------------------------ Consent Resource ------------------------------ //
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Consent Initiation", description = "\n" +
            "Creates ConsentResource, AuthorizationResource and ConsentMapping Entity in the database.\n" +
            "Creates Authorization resources provided in the authorizationResources array if implicitAuth flag is " +
            "true.\n",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResourceResponseBody.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body")
            })
    @Tag(name = "consent resource")
    public Response consentPost(
            @Valid @NotNull ConsentResourceRequestBody consentResourceDTO,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentPost(consentResourceDTO, orgInfo);
    }

    @GET
    @Path("/{consentId}")
    @Produces("application/json")
    @Operation(summary = "Consent Retrieval",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResourceResponseBody.class))),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "consent resource")
    public Response consentConsentIdGet(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdGet(consentId, orgInfo);
    }

    @GET
    @Produces("application/json")
    @Operation(summary = "Consent Search",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentResourceResponseBody.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid consent id")
            })
    @Tag(name = "consent resource")
    public Response consentGet(
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
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
    @Tag(name = "consent resource")
    public Response consentConsentIdDelete(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdDelete(consentId, orgInfo);
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
    @Tag(name = "consent resource")
    public Response consentConsentIdStatusPut(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull ConsentStatusUpdateRequestBody consentStatusUpdateResource,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdStatusPut(consentId, consentStatusUpdateResource, orgInfo);
    }

    @PUT
    @Path("/{consentId}/expiry-time")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Consent Expiry Time Update (Seconds)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid consent id")
            })
    @Tag(name = "consent resource")
    public Response consentConsentIdExpiryTimePut(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull ConsentExpiryTimeUpdateRequestBody consentExpiryTimeUpdateDTO,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentConsentIdExpiryTimePut(consentId, orgInfo, consentExpiryTimeUpdateDTO);
    }


    @POST
    @Path("/{consentId}/revoke")
    @Consumes("application/json")
    @Produces("application/json")
    @Operation(summary = "Consent Revoke",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successful operation"),
                    @ApiResponse(responseCode = "404", description = "Invalid consent id")
            })
    @Tag(name = "consent resource")
    public Response consentRevokeConsentIdPost(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @Valid @NotNull ConsentRevokeRequestBody consentRevokeResource,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentRevokeConsentIdPost(consentId, consentRevokeResource, orgInfo);
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
    @Tag(name = "consent resource")
    public Response consentStatusPut(
            @Valid @NotNull BulkConsentStatusUpdateResourceRequestBody bulkConsentStatusUpdateResource,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
            @Parameter(description = "JWT header containing tenant-related information") String orgInfo) {

        return consentApiImpl.consentStatusPut(bulkConsentStatusUpdateResource, orgInfo);
    }


    // ------------------------------ Consent History ------------------------------ //

    @GET
    @Path("/{consentId}/history")
    @Produces("application/json")
    @Operation(summary = "Consent History Retrieval",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = ConsentHistoryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid consent id")
            })
    @Tag(name = "history")
    public Response consentConsentIdHistoryGet(
            @PathParam("consentId") @Parameter(description = "Consent id") String consentId,
            @HeaderParam("orgInfo") @DefaultValue("DEFAULT_ORG")
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
