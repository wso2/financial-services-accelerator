package com.wso2.openbanking.toolkittemplate.extensions.generated.api;

import com.wso2.openbanking.toolkittemplate.extensions.generated.model.ErrorResponse;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.EventSubscriptionRequestBody;
import com.wso2.openbanking.toolkittemplate.extensions.generated.model.Response200ForEnrichEventSubscription;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/enrich-event-subscription-response")
@Api(description = "the enrich-event-subscription-response API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-10-30T12:14:10.540948+05:30[Asia/Colombo]", comments = "Generator version: 7.16.0")
public class EnrichEventSubscriptionResponseApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "handle post event-subscription-creation response generation", notes = "", response = Response200ForEnrichEventSubscription.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Event Subscription" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = Response200ForEnrichEventSubscription.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response enrichEventSubscriptionResponsePost(@Valid @NotNull EventSubscriptionRequestBody eventSubscriptionRequestBody) {
        return Response.ok().entity("magic!").build();
    }
}
