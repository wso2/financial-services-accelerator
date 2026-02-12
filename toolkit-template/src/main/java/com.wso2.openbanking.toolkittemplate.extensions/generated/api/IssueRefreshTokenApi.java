package com.wso2.openbanking.toolkittemplate.extensions.generated.api;

import com.wso2.openbanking.toolkittemplate.extensions.generated.model.ErrorResponse;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.IssueRefreshTokenRequestBody;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.Response200ForIssueRefreshToken;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.wso2.openbanking.toolkittemplate.extensions.impls.IssueRefreshTokenApiImpl;
import io.swagger.annotations.*;

import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/issue-refresh-token")
@Api(description = "the issue-refresh-token API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class IssueRefreshTokenApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Handles refresh token issuance and validations", notes = "", response = Response200ForIssueRefreshToken.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Token" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response", response = Response200ForIssueRefreshToken.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response issueRefreshTokenPost(@Valid @NotNull IssueRefreshTokenRequestBody issueRefreshTokenRequestBody) {
        return IssueRefreshTokenApiImpl.handleIssueRefreshToken(issueRefreshTokenRequestBody);
    }
}
