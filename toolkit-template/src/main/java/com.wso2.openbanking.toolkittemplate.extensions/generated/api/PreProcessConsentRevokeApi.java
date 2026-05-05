package com.wso2.openbanking.toolkittemplate.extensions.generated.api;

import com.wso2.openbanking.toolkittemplate.extensions.generated.model.ErrorResponse;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.PreProcessConsentRequestBody;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.Response200ForConsentRevocation;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/pre-process-consent-revoke")
@Api(description = "the pre-process-consent-revoke API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class PreProcessConsentRevokeApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "handle pre-consent revocation validations when a TPP calls consent /DELETE", notes = "", response = Response200ForConsentRevocation.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Consent" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = Response200ForConsentRevocation.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response preProcessConsentRevokePost(@Valid @NotNull PreProcessConsentRequestBody preProcessConsentRequestBody) {
        return Response.ok().entity("magic!").build();
    }
}
