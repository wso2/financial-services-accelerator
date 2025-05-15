package org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.mappers.model.AuthorizationResourceMapper;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.mappers.model.ConsentResourceMapper;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.mappers.model.DetailedConsentResourceMapper;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceResponseBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentExpiryTimeUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceResponseBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * Consent API Implementation of ConsentAPI class
 */
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
public class ConsentAPIImpl {
    private static final Log log = LogFactory.getLog(ConsentAPIImpl.class);
    private ConsentCoreServiceImpl consentCoreService;

    public ConsentAPIImpl() {
        consentCoreService = new ConsentCoreServiceImpl();
    }

    public void setConsentCoreService(ConsentCoreServiceImpl consentCoreService) {
        this.consentCoreService = consentCoreService;
    }

        /**
     * Handles the API request to create a new consent resource.
     * <p>
     * This method processes the payload containing the consent resource, authorization resources,
     * and consent attributes. It maps the input data to the appropriate domain models,
     * invokes the service layer to create the consent, and returns the created consent resource
     * in the response.
     * </p>
     *
     * @param createConsentResourceRequestBody The payload containing the consent resource,
     *                                         authorization resources, and consent attributes.
     * @param orgInfo                          The tenant or organization information.
     * @return Response                        A JAX-RS Response containing the created consent resource
     * or an error message if the operation fails.
     **/
    public Response consentPost(
            ConsentResourceRequestBody createConsentResourceRequestBody, String orgInfo) {

        try {
            ConsentResource consentResource = ConsentResourceMapper.INSTANCE.toConsentResource(
                    createConsentResourceRequestBody);
            consentResource.setOrgID(orgInfo);
            ArrayList<AuthorizationResource> authorizations = new ArrayList<>();
            if (createConsentResourceRequestBody.getAuthorizationResources() != null) {
                for (AuthorizationResourceRequestBody authorizationResourceDTO :
                        createConsentResourceRequestBody.getAuthorizationResources()) {
                    AuthorizationResource authorizationResource =
                            AuthorizationResourceMapper.INSTANCE.
                                    toAuthorizationResource(authorizationResourceDTO);

                    authorizations.add(authorizationResource);
                }
            }
            DetailedConsentResource result = consentCoreService.createConsent(consentResource, authorizations);
            ConsentResourceResponseBody consentResourceResponseBody =
                    DetailedConsentResourceMapper.INSTANCE.toConsentResourceRequestBody(result);
            return Response.status(Response.Status.CREATED).entity(consentResourceResponseBody).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * Handles the API request to retrieve a specific consent resource by its ID.
     * This method fetches the full details of a consent resource, including its type, status,
     * associated user, client information, and any custom attributes.
     *
     * @param consentId Unique identifier of the consent resource to retrieve.
     * @param orgInfo   Object containing tenant or organization-related context information.
     * @return A JAX-RS Response containing the consent resource details, or an error message if the retrieval fails.
     */
    public Response consentConsentIdGet(
            String consentId, String orgInfo) {

        try {
            DetailedConsentResource detailedConsentResource =
                    consentCoreService.getDetailedConsent(consentId, orgInfo);
            ConsentResourceResponseBody consentResponse =
                    DetailedConsentResourceMapper.INSTANCE.
                            toConsentResourceRequestBody(detailedConsentResource);
            return Response.ok().entity(consentResponse).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * This method is used to handle the API used to search the consent resource
     *
     * @param orgInfo       Object containing tenant or organization-related context information.
     * @param consentType   Type of the consent to filter the search results.
     * @param consentStatus Status of the consent (e.g., ACTIVE, REVOKED) to filter results.
     * @param clientId      Identifier of the client application that created the consent.
     * @param userId        Identifier of the user associated with the consent.
     * @param fromTimeValue Start of the time range (inclusive) to filter consents based on creation or update time.
     * @param toTimeValue   End of the time range (inclusive) to filter consents based on creation or update time.
     * @param limitValue    Maximum number of results to return (for pagination).
     * @param offsetValue   Number of results to skip before starting to collect the result set (for pagination).
     * @return Response      A JAX-RS Response object containing the filtered list of consents or an error message.
     */

    public Response consentGet(
            String orgInfo,
            String consentType, String consentStatus
            , String clientId, String userId, long fromTimeValue, long toTimeValue, int limitValue, int offsetValue) {
        try {
            ArrayList<String> consentIDs = new ArrayList<>();
            ArrayList<String> clientIDs = new ArrayList<>();
            ArrayList<String> consentTypes = new ArrayList<>();
            ArrayList<String> consentStatuses = new ArrayList<>();
            ArrayList<String> userIDs = new ArrayList<>();
            if (consentType != null) {
                Collections.addAll(consentTypes, consentType.split(","));
            }
            if (consentStatus != null) {
                Collections.addAll(consentStatuses, consentStatus.split(","));
            }
            if (userId != null) {
                userIDs.add(userId);
            }
            if (clientId != null) {
                Collections.addAll(clientIDs, clientId.split(","));
            }

            Long fromTime;
            Long toTime;
            Integer limit;
            Integer offset;
            fromTime = fromTimeValue == 0L ? null : fromTimeValue;
            toTime = toTimeValue == 0L ? null : toTimeValue;
            limit = limitValue == 0 ? null : limitValue;
            offset = offsetValue == 0 ? null : offsetValue;

            ArrayList<DetailedConsentResource> results;
            results = consentCoreService.searchDetailedConsents(
                    orgInfo,
                    consentIDs,
                    clientIDs,
                    consentTypes,
                    consentStatuses,
                    userIDs,
                    fromTime,
                    toTime,
                    limit,
                    offset);

            ArrayList<ConsentResourceResponseBody> consentResponses = new ArrayList<>();
            for (DetailedConsentResource detailedConsentResource : results) {
                ConsentResourceResponseBody consentResponse =
                        DetailedConsentResourceMapper.INSTANCE.toConsentResourceRequestBody(detailedConsentResource);
                consentResponses.add(consentResponse);
            }

            return Response.ok().entity(consentResponses).build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }


    /**
     * This method is used to handle the API used to update the consent status
     *
     * @param consentId                   Unique identifier of the consent resource to be updated.
     * @param orgInfo                     Object containing tenant or organization-related context information.
     * @param consentStatusUpdateResource Object containing the new status and any associated update information.
     * @return Response A JAX-RS Response indicating the result of the update operation—success or failure with details.
     */
    public Response consentConsentIdStatusPut(
            String consentId, ConsentStatusUpdateRequestBody consentStatusUpdateResource
            , String orgInfo) {
        try {
            consentCoreService.updateConsentStatus(consentId,
                    consentStatusUpdateResource.getStatus(),
                    consentStatusUpdateResource.getReason(),
                    consentStatusUpdateResource.getUserId(),
                    orgInfo);

            JSONObject message = new JSONObject();
            message.put("message", "Status Updated");
            return Response.ok().entity(message).build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * Handles the API request to update the status of multiple consent resources in bulk.
     * This method processes a bulk status update for a set of consent resources. It receives a payload
     * containing a list of consent IDs along with the new status to be applied to each. This is useful for
     * administrative operations or automated workflows where multiple consents need to be updated simultaneously.
     *
     * @param bulkConsentStatusUpdateResource Object containing the list of consent IDs and the status to apply.
     * @param orgInfo                         Object containing tenant or organization-related context information.
     * @return Response                       A JAX-RS Response summarizing the outcome of the operation, including
     * the number of successful updates and any failures.
     */

    public Response consentStatusPut(BulkConsentStatusUpdateResourceRequestBody bulkConsentStatusUpdateResource,
                                     String orgInfo) {
        try {

            consentCoreService.bulkUpdateConsentStatus(
                    orgInfo,
                    bulkConsentStatusUpdateResource.getClientId(),
                    bulkConsentStatusUpdateResource.getStatus(),
                    bulkConsentStatusUpdateResource.getReason(),
                    bulkConsentStatusUpdateResource.getUserId(),
                    bulkConsentStatusUpdateResource.getConsentType(),
                    new ArrayList<>(bulkConsentStatusUpdateResource.getApplicableStatusesForStateChange()));

            return Response.ok().entity("Status Updated").build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * Handles the API request to update the expiry time of a specific consent resource.
     * <p>
     * This method allows updating the expiry time of a consent identified by its ID. The new expiry
     * time is provided in the request body, and the consent's status will be adjusted accordingly once
     * the new expiry time is set.
     *
     * @param consentId                  Unique identifier of the consent resource whose expiry time is being updated.
     * @param orgInfo                    Object containing tenant or organization-related context information.
     * @param consentExpiryTimeUpdateDTO Data transfer object (DTO) containing the new expiry time for the consent.
     * @return Response                 A JAX-RS Response indicating the result of the update operation—success or
     * failure with details.
     */

    public Response consentConsentIdExpiryTimePut(String consentId, ConsentExpiryTimeUpdateRequestBody
            consentExpiryTimeUpdateDTO, String orgInfo) {
        try {
            boolean result = consentCoreService.updateConsentExpiryTime(consentId,
                    consentExpiryTimeUpdateDTO.getExpiryTime(), orgInfo);
            if (result) {

                JSONObject message = new JSONObject();
                message.put("message", "Expiry Time Updated");
                return Response.ok().entity(message).build();
            } else {
                return Response.serverError().build();

            }

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }


    /**
     * This method is used to handle the API used to delete the consent resource
     *
     * @param consentId
     * @param orgInfo
     * @return Response
     */
    public Response consentConsentIdDelete(String consentId, String orgInfo) {
        try {
            // check if the consent exists, else throws exception
            consentCoreService.getDetailedConsent(consentId, orgInfo);

            boolean result = consentCoreService.deleteConsent(consentId);
            if (result) {
                JSONObject message = new JSONObject();
                message.put("message", "Consent purged");
                return Response.ok().entity(message).build();
            } else {
                return Response.serverError().build();

            }


        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }

    }

    /**
     * Handles the API request to delete a specific consent resource.
     * This method deletes the consent resource identified by its ID. Once deleted, the consent record is
     * permanently removed, and it will no longer be accessible or retrievable through the API.
     *
     * @param consentId Unique identifier of the consent resource to be deleted.
     * @param orgInfo   Object containing tenant or organization-related context information.
     * @return Response A JAX-RS Response indicating the result of the deletion operation—success or failure with
     * details.
     */

    public Response consentRevokeConsentIdPost(String consentId,
                                               ConsentRevokeRequestBody consentStatusUpdateResource, String orgInfo) {
        try {
            boolean result = consentCoreService.revokeConsent(consentId,
                    orgInfo,
                    consentStatusUpdateResource.getUserId(),
                    consentStatusUpdateResource.getReason());
            if (result) {

                JSONObject message = new JSONObject();
                message.put("message", "Consent Revoked");
                return Response.ok().entity(message).build();
            } else {
                return Response.serverError().build();

            }

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }

    }


    /**
     * Handles the API request to retrieve the specific authorization resource associated with a given consent.
     * This method fetches the authorization details for a consent identified by its ID and the associated
     * authorization resource, which is identified by the provided authorization ID. The returned resource
     * contains any relevant permissions or metadata related to the authorization for the consent.
     *
     * @param authorizationId Unique identifier of the authorization resource to retrieve.
     * @param orgInfo         Object containing tenant or organization-related context information.
     * @param consentId       Unique identifier of the consent resource associated with the authorization.
     * @return Response      A JAX-RS Response containing the authorization details if found, or an error message if
     * not.
     */

    public Response consentAuthorizationIdGet(String authorizationId, String consentId, String orgInfo) {

        try {
            AuthorizationResource authorizationResource = consentCoreService.getAuthorizationResource(authorizationId,
                    orgInfo);
            AuthorizationResourceResponseBody authorizationResourceResponseBody =
                    AuthorizationResourceMapper.INSTANCE.toAuthorizationResourceResponseBody(authorizationResource);
            return Response.ok().entity(authorizationResourceResponseBody).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }

    }

    /**
     * Handles the API request to retrieve the authorization resource associated with a specific consent.
     * <p>
     * This method retrieves the authorization details linked to a consent identified by its ID.
     * The returned resource includes any metadata or specific rules/permissions related to the consent's authorization.
     *
     * @param consentId Unique identifier of the consent resource whose authorization details are being retrieved.
     * @param orgInfo   Object containing tenant or organization-related context information.
     * @return Response A JAX-RS Response containing the consent authorization details if found, or an error message
     * if not.
     */

    public Response consentAuthorizationGet(String consentId, String orgInfo) {
        try {
            DetailedConsentResource consentResource = consentCoreService.getDetailedConsent(consentId, orgInfo);
            List<AuthorizationResource> authorizationResources = consentResource.getAuthorizationResources();
            return Response.ok().entity(authorizationResources).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * Handles the API request to create one or more authorization resources for a specific consent.
     * <p>
     * This method allows creating one or more authorization resources associated with a given consent ID.
     * The authorization details are provided as a list in the request body, and they will be linked to the
     * specified consent. This is useful for managing and storing multiple authorization rules or permissions
     * related to a particular consent.
     *
     * @param consentId                    Unique identifier of the consent resource to associate the authorization
     *                                     with.
     * @param orgInfo                      Object containing tenant or organization-related context information.
     * @param authorizationResourceDTOList List of data transfer objects (DTOs) representing the authorization
     *                                     resources to be created.
     * @return Response                  A JAX-RS Response indicating the result of the creation operation—success or
     * failure with details.
     */

    public Response consentAuthorizationIdPost(String consentId,
                                               List<AuthorizationResourceRequestBody>
                                                       authorizationResourceDTOList,
                                               String orgInfo) {
        try {
            ///  check if the consent exists, else throws exception
            consentCoreService.getDetailedConsent(consentId, orgInfo);


            List<AuthorizationResourceResponseBody> authorizationResourceResponseBodyList =
                    new ArrayList<>();
            for (AuthorizationResourceRequestBody authorizationResourceDTO : authorizationResourceDTOList) {
                AuthorizationResource authorizationResource =
                        AuthorizationResourceMapper.INSTANCE.toAuthorizationResource(authorizationResourceDTO);
                authorizationResource.setConsentId(consentId);
                authorizationResourceResponseBodyList.add(
                        AuthorizationResourceMapper.INSTANCE.toAuthorizationResourceResponseBody(
                                consentCoreService.createConsentAuthorization(authorizationResource)));

            }
            return Response.status(Response.Status.CREATED).entity(authorizationResourceResponseBodyList).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * Handles the API request to update an authorization resource associated with a specific consent.
     * <p>
     * This method updates the authorization details for a consent identified by its ID, including any
     * associated metadata or authorization parameters. The new details are provided in the request body.
     *
     * @param authorizationId                  Unique identifier of the authorization resource to be updated.
     * @param consentId                        Unique identifier of the consent resource related to the authorization.
     * @param orgInfo                          Object containing tenant or organization-related context information.
     * @param authorizationResourceRequestBody Object containing the new authorization details to be updated.
     * @return Response                    A JAX-RS Response indicating the result of the update operation—success or
     * failure with details.
     */

    public Response consentAuthorizationIdPut(String authorizationId, String consentId,
                                              AuthorizationResourceRequestBody authorizationResourceRequestBody,
                                              String orgInfo) {

        try {
            ///  check if the consent exists, else throws exception
            consentCoreService.getDetailedConsent(consentId, orgInfo);

            AuthorizationResource authorizationResource =
                    AuthorizationResourceMapper.INSTANCE.toAuthorizationResource(authorizationResourceRequestBody);

            consentCoreService.updateAuthorizationResource(authorizationId, authorizationResource, orgInfo);

            return Response.ok().entity("Authorization Resource Updated").build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * This method is used to handle the API used to delete the authorization resource
     *
     * @param authorizationId
     * @param consentId
     * @param orgInfo
     * @return Response
     */
    public Response consentAuthorizationIdDelete(String authorizationId, String consentId, String orgInfo) {
        try {
            ///  check if the consent exists, else throws exception
            consentCoreService.getDetailedConsent(consentId, orgInfo);

            boolean result = consentCoreService.deleteAuthorizationResource(authorizationId);
            if (result) {
                return Response.ok().entity("Authorization Resource Deleted").build();
            } else {
                return Response.serverError().build();

            }

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }

    }


    /**
     * This method is used to build the error response
     *
     * @param e
     * @return Response
     */
    private Response handleConsentMgtException(ConsentMgtException e) {

        // parse status code from error code
        Response.Status status = ConsentUtils.getStatusFromErrorCode(e.getError().getCode());

        log.error("Error Occurred while handling the request", e);
        Map<String, String> error = new LinkedHashMap<>();


        error.put("code", e.getError().getCode());
        error.put("message", e.getError().getMessage());
        error.put("description", e.getError().getDescription());

        return Response.status(status).entity(error).build();
    }


}
