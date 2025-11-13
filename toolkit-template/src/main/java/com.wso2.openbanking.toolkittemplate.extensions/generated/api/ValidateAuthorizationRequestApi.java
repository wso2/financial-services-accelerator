package com.wso2.openbanking.toolkittemplate.extensions.generated.api;

import com.wso2.openbanking.toolkittemplate.extensions.generated.model.ErrorResponse;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.Response200ForValidateAuthorizationRequest;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.ValidateAuthorizationRequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.wso2.openbanking.toolkittemplate.extensions.impls.ValidateAuthorizationRequestApiImpl;
import io.swagger.annotations.*;

import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/validate-authorization-request")
@Api(description = "the validate-authorization-request API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class ValidateAuthorizationRequestApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Handles pre-user authorization requests", notes = "", response = Response200ForValidateAuthorizationRequest.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Authorize" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response", response = Response200ForValidateAuthorizationRequest.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response preUserAuthorization(@Valid @NotNull ValidateAuthorizationRequestBody validateAuthorizationRequestBody) {
        return ValidateAuthorizationRequestApiImpl.handlePushedAuthorisationRequest(validateAuthorizationRequestBody);
    }
}
