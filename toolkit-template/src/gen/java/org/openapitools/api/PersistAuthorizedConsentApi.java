package org.openapitools.api;

import org.openapitools.model.ErrorResponse;
import org.openapitools.model.PersistAuthorizedConsentRequestBody;
import org.openapitools.model.Response200ForPersistAuthorizedConsent;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/persist-authorized-consent")
@Api(description = "the persist-authorized-consent API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class PersistAuthorizedConsentApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "handle consent persistence logic and enrich response with user authorization and account mapping data", notes = "", response = Response200ForPersistAuthorizedConsent.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Consent" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response", response = Response200ForPersistAuthorizedConsent.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response persistAuthorizedConsentPost(@Valid @NotNull PersistAuthorizedConsentRequestBody persistAuthorizedConsentRequestBody) {
        return Response.ok().entity("magic!").build();
    }
}
