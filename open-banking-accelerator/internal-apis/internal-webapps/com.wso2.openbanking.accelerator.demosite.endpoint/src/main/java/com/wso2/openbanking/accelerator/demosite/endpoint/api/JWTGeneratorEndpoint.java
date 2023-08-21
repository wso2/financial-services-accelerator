/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.demosite.endpoint.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.demosite.endpoint.model.JWTGeneratorEndpointErrorResponse;
import com.wso2.openbanking.accelerator.demosite.endpoint.model.PayloadData;
import com.wso2.openbanking.accelerator.demosite.endpoint.util.GeneratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Demo-site Endpoint
 * This specifies the RESTful APIs to generate the payloads required to try out the OB flows in the demo-site.
 */
@Path("/")
public class JWTGeneratorEndpoint {

    private static Log log = LogFactory.getLog(JWTGeneratorEndpoint.class);

    /**
     * Generate the RequestObject, DCR Payload and Token Assertion
     */
    @POST
    @Path("/getJWT")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response getJWT(@Context HttpServletRequest request, @Context HttpServletResponse response,
                           MultivaluedMap parameterMap) {
        String requestPayload;
        try {
            requestPayload = new ObjectMapper().writeValueAsString(parameterMap);
        } catch (JsonProcessingException e) {
            String error = "Error in formatting the request payload";
            log.error(error, e);
            JWTGeneratorEndpointErrorResponse errorResponse = GeneratorUtil.createErrorResponse(400, error);
            return Response.status(400).entity(errorResponse.getPayload()).build();
        }
        PayloadData data = new Gson().fromJson(requestPayload.replaceAll("\\\\r|\\\\n|\\r|\\n|\\[|]", ""),
                PayloadData.class);
        try {
            return Response.status(201).entity(GeneratorUtil.generateJWT(data)).build();
        } catch (OpenBankingException e) {
            String error = "Error occurred while building the JWT";
            log.error(error, e);
            JWTGeneratorEndpointErrorResponse errorResponse = GeneratorUtil.createErrorResponse(500, error);
            return Response.status(500).entity(errorResponse.getPayload()).build();
        }
    }

    /**
     * Update key and certificate used to sign the JWT content
     */
    @GET
    @Path("/updateCerts")
    @Produces({"application/json; charset=utf-8"})
    public Response updateCertificates(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            return Response.status(201).entity(GeneratorUtil.updateConfigurations()).build();
        } catch (OpenBankingException e) {
            String error = "Error occurred while updating the certificates";
            log.error(error, e);
            JWTGeneratorEndpointErrorResponse errorResponse = GeneratorUtil.createErrorResponse(500, error);
            return Response.status(500).entity(errorResponse.getPayload()).build();
        }
    }

}
