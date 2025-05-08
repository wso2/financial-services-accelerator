package org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceResponseBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentExpiryTimeUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceResponseBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.AuthorizationResourceMapper;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentResourceMapper;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentCoreServiceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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


    // ============================================authorization resource=============================================

    /**
     * This method is used to handle the API used to get the consent authorization resource
     *
     * @param authorizationId
     * @param orgInfo
     * @param consentId
     * @return Response
     */
    public Response consentAuthorizationIdGet(String authorizationId, String orgInfo, String consentId) {

        AuthorizationResourceResponseBody authResponse = new AuthorizationResourceResponseBody();

        try {

            AuthorizationResource authorizationResource = consentCoreService.getAuthorizationResource(authorizationId,
                    orgInfo);
            ConsentUtils.buildAuthorizationResourceResponse(authResponse, authorizationResource);
            return Response.ok().entity(authResponse).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * This method is used to handle the API used to get the consent authorization resource
     *
     * @param consentId
     * @param orgInfo
     * @return Response
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
     * This method is used to handle the API used to create a consent authorization resource
     *
     * @param consentId
     * @param orgInfo
     * @param authorizationResourceDTOList
     * @return Response
     */
    public Response consentAuthorizationIdPost(String consentId, String orgInfo,
                                               List<AuthorizationResourceRequestBody>
                                                       authorizationResourceDTOList) {
        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }


            for (AuthorizationResourceRequestBody authorizationResourceDTO : authorizationResourceDTOList) {
                AuthorizationResource authorizationResource = new AuthorizationResource();
                ConsentUtils.copyPropertiesToAuthorizationResource(authorizationResource, authorizationResourceDTO);
                authorizationResource.setConsentId(consentId);
                consentCoreService.createConsentAuthorization(authorizationResource);

            }
            return Response.ok().entity("Authorization Resources Stored").build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to handle the API used to update the authorization resource
     *
     * @param authorizationId
     * @param consentId
     * @param orgInfo
     * @param authorizationResourceRequestBody
     * @return Response
     */
    public Response consentAuthorizationIdPut(String authorizationId, String consentId, String orgInfo,
                                              AuthorizationResourceRequestBody authorizationResourceRequestBody) {

        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }
            AuthorizationResource authorizationResource = new AuthorizationResource();
            ConsentUtils.copyPropertiesToAuthorizationResource(authorizationResource, authorizationResourceRequestBody);

            consentCoreService.updateAuthorizationResource(authorizationId, authorizationResource, orgInfo);

            return Response.ok().entity("Authorization Resource Updated").build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            log.error("Error Occurred while handling the request", e);
            JSONObject error = new JSONObject();
            error.put("errorMessage", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();

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
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

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

    //============================================= consent Attributes =============================================/

    /**
     * This method is used to handle the API used to get the consent attributes
     *
     * @param consentId
     * @param orgInfo
     * @return Response
     */
    public Response consentConsentIdAttributesGet(String consentId, String orgInfo) {
        try {
            ConsentResource consentResource =
                    consentCoreService.getConsent(consentId,
                            true);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            Map<String, String> consentAttributes = consentResource.getConsentAttributes();
            return Response.ok().entity(consentAttributes).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * This method is used to handle the API used to store the consent attributes
     *
     * @param consentId
     * @param orgInfo
     * @param consentAttributes
     * @return Response
     */
    public Response consentConsentIdAttributesPost(String consentId, String orgInfo,
                                                   Map<String, String> consentAttributes) {
        try {
            ConsentResource consentResource =
                    consentCoreService.getConsent(consentId,
                            true);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            boolean result = consentCoreService.storeConsentAttributes(consentId,
                    consentAttributes);
            if (result) {
                JSONObject message = new JSONObject();
                message.put("message", "Consent Attributes Stored");

                return Response.ok().entity(message).build();
            } else {
                return Response.serverError().build();

            }
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }

    }

    /**
     * This method is used to handle the API used to update the consent attributes
     *
     * @param consentId
     * @param orgInfo
     * @param consentAttributes
     * @return Response
     */
    public Response consentConsentIdAttributesPut(String consentId,
                                                  String orgInfo, Map<String, String> consentAttributes) {
        try {
            ConsentResource consentResource =
                    consentCoreService.getConsent(consentId,
                            true);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            consentCoreService.updateConsentAttributes(consentId,
                    consentAttributes);
            return Response.ok().build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    //============================================= consent resource =============================================//

    /**
     * This method is used to handle the API used to create a consent
     *
     * @param createConsentResourceRequestBody payload which contains the consent resource, authorization resource
     *                                         and consent attributes
     * @param orgInfo                          tenant information
     * @return Response
     */
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


            ConsentResourceResponseBody consentResourceResponseBody = new ConsentResourceResponseBody();
            ConsentUtils.buildConsentResourceResponse(consentResourceResponseBody, result,
                    result.getAuthorizationResources());
            return Response.status(Response.Status.CREATED).entity(consentResourceResponseBody).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            log.error("Error Occurred while handling the request", e);
            JSONObject error = new JSONObject();
            error.put("errorMessage", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }


    /**
     * This method is used to handle the API used to get the consent resource
     *
     * @param consentId
     * @param orgInfo
     * @return Response
     */
    public Response consentConsentIdGet(
            String consentId, String orgInfo) {

        try {

            // --------------------  service call -----------------------//
            DetailedConsentResource detailedConsentResource =
                    consentCoreService.getDetailedConsent(consentId, orgInfo);


            //--------------------- build response -----------------------//
            ConsentResourceResponseBody consentResponse = new ConsentResourceResponseBody();
            ConsentUtils.buildConsentResourceResponse(consentResponse,
                    detailedConsentResource,
                    detailedConsentResource.getAuthorizationResources());
            return Response.ok().entity(consentResponse).build();


        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            log.error("Error Occurred while handling the request", e);
            JSONObject error = new JSONObject();
            error.put("errorMessage", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    /**
     * This method is used to handle the API used to search the consent resource
     *
     * @param orgInfo
     * @param consentType
     * @param consentStatus
     * @param clientId
     * @param userId
     * @param fromTimeValue
     * @param toTimeValue
     * @param limitValue
     * @param offsetValue
     * @return Response
     */
    public Response consentGet(
            String orgInfo,
            String consentType, String consentStatus
            , String clientId, String userId, long fromTimeValue, long toTimeValue, int limitValue, int offsetValue) {
        try {
            //------------ initialize the search query ---------------//
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

            //----------------- service call -------------------//
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


            //--------------------- build response -----------------------------//
            ArrayList<ConsentResourceResponseBody> consentResponses = new ArrayList<>();

            for (DetailedConsentResource detailedConsentResource : results) {

                ConsentResourceResponseBody consentResponse = new ConsentResourceResponseBody();
                ConsentUtils.buildConsentResourceResponse(consentResponse, detailedConsentResource,
                        detailedConsentResource.getAuthorizationResources());
                consentResponses.add(consentResponse);
            }

            return Response.ok().entity(consentResponses).build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            log.error("Error Occurred while handling the request", e);
            JSONObject error = new JSONObject();
            error.put("errorMessage", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }


    /**
     * This method is used to handle the API used to update the consent status
     *
     * @param consentId
     * @param orgInfo
     * @param consentStatusUpdateResource
     * @return Response
     */
    public Response consentConsentIdStatusPut(
            String consentId, ConsentStatusUpdateRequestBody consentStatusUpdateResource
            , String orgInfo) {
        try {
            consentCoreService.updateConsentStatusWithImplicitReasonAndUserId(consentId,
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
     * This method is used to handle the API used to update bulk consent status
     *
     * @param bulkConsentStatusUpdateResource
     * @param orgInfo
     * @return Response
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
                    bulkConsentStatusUpdateResource.getApplicableStatusesForStateChange());

            return Response.ok().entity("Status Updated").build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    /**
     * This method is used to handle the API used to update the consent expiry time
     *
     * @param consentId
     * @param orgInfo
     * @param consentExpiryTimeUpdateDTO
     * @return Response
     */
    public Response consentConsentIdExpiryTimePut(String consentId, String orgInfo, ConsentExpiryTimeUpdateRequestBody
            consentExpiryTimeUpdateDTO) {
        try {
            ConsentResource consentResource =
                    consentCoreService.getConsent(consentId,
                            false);


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
            ConsentResource consentResource =
                    consentCoreService.getConsent(consentId,
                            false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            boolean result = consentCoreService.deleteConsent(consentId);
            if (result) {
                return Response.ok().entity("Consent purged").build();
            } else {
                return Response.serverError().build();

            }


        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }

    }

    /**
     * This method is used to handle the API used to revoke the consent resource
     *
     * @param consentId
     * @param orgInfo
     * @param consentStatusUpdateResource
     * @return Response
     */
    public Response consentRevokeConsentIdPost(String consentId,
                                               ConsentRevokeRequestBody consentStatusUpdateResource, String orgInfo) {
        try {
            ConsentResource consentResource =
                    consentCoreService.getConsent(consentId,
                            false);


            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }


            boolean result = consentCoreService.revokeConsentWithReason(consentId,
                    "revoked",
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
     * This method is used to handle the API used to get the consent history
     *
     * @param consentId
     * @param orgInfo
     * @param detailed
     * @param status
     * @param actionBy
     * @param fromTimeValue
     * @param toTimeValue
     * @param statusAuditId
     * @return Response
     */
    public Response consentConsentIdHistoryGet(
            String consentId, String orgInfo, Boolean detailed, String status, String actionBy, long fromTimeValue,
            long toTimeValue, String statusAuditId) {

        try {

            Long fromTime = null;
            Long toTime = null;
            Integer limit = null;
            Integer offset = null;
            fromTime = fromTimeValue == 0L ? null : fromTimeValue;
            toTime = toTimeValue == 0L ? null : toTimeValue;

            ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords =
                    consentCoreService.searchConsentStatusAuditRecords(consentId, status,
                            actionBy, fromTime,
                            toTime,
                            statusAuditId);

            // get the consent status audit record Ids
            ArrayList<String> consentStatusAuditRecordIds = new ArrayList<>();
            for (ConsentStatusAuditRecord consentStatusAuditRecord : consentStatusAuditRecords) {
                consentStatusAuditRecordIds.add(consentStatusAuditRecord.getStatusAuditID());
            }

            ArrayList<ConsentHistoryResource> results =
                    new ArrayList<>(consentCoreService.getConsentAmendmentHistoryData(consentStatusAuditRecordIds,
                            consentId).values());
            ArrayList<ConsentHistoryResource> newResults = new ArrayList<>();

            if (!detailed) {
                for (ConsentHistoryResource result : results) {
                    result.getDetailedConsentResource().setAuthorizationResources(null);
                    result.getDetailedConsentResource().setConsentAttributes(null);
                    result.getDetailedConsentResource().setConsentMappingResources(null);
                    newResults.add(result);
                }
                // remove null values from the consent
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                try {
                    return Response.ok().entity(objectMapper.writeValueAsString(newResults)).build();

                } catch (JsonProcessingException e) {
                    log.error("Error Occurred while handling the request", e);
                    JSONObject error = new JSONObject();
                    error.put("errorMessage", e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
                }

            } else {


                for (ConsentHistoryResource result : results) {
                    DetailedConsentResource detailedConsentResource =
                            result.getDetailedConsentResource();

                    // get consent mapping resources for each AuthorizationResource
                    Map<String, ArrayList<ConsentMappingResource>> consentMappingResourcesMap = new HashMap<>();
                    for (ConsentMappingResource consentMappingResource :
                            detailedConsentResource.getConsentMappingResources()) {
                        if (!consentMappingResourcesMap.containsKey(consentMappingResource.getAuthorizationId())) {
                            consentMappingResourcesMap.put(consentMappingResource.getAuthorizationId(),
                                    new ArrayList<>());
                        }
                        consentMappingResourcesMap.get(consentMappingResource.getAuthorizationId())
                                .add(consentMappingResource);
                    }

                    ArrayList<AuthorizationResource>
                            authorizationResourcesWithResources = new ArrayList<>();
                    for (AuthorizationResource authorizationResource :
                            detailedConsentResource.getAuthorizationResources()) {
                        AuthorizationResource
                                auth =
                                new AuthorizationResource();
                        ConsentUtils.buildAuthorizationResourceResponse(auth,
                                authorizationResource,
                                consentMappingResourcesMap.get(authorizationResource.getAuthorizationId()));
                        authorizationResourcesWithResources.add(auth);
                    }
                    detailedConsentResource.setAuthorizationResources(authorizationResourcesWithResources);
                    newResults.add(result);
                }

                // remove null values from the consent
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                try {
                    return Response.ok().entity(objectMapper.writeValueAsString(newResults)).build();
                } catch (JsonProcessingException e) {
                    log.error("Error Occurred while handling the request", e);
                    JSONObject error = new JSONObject();
                    error.put("errorMessage", e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
                }

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
        log.error("Error Occurred while handling the request", e);
        JSONObject error = new JSONObject();
        error.put("errorMessage", e.getMessage());
        return Response.status(e.getErrorCode()).entity(error).build();
    }


}
