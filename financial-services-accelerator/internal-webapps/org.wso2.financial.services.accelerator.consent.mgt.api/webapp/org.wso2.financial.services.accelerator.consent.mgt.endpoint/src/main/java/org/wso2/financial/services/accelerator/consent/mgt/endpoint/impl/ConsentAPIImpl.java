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
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentExpiryTimeUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.CreateAuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.CreateConsentResourceRequestBody;
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
 * ConsentAPIImpl.
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

    public Response consentPost(
            CreateConsentResourceRequestBody createConsentResourceRequestBody, String orgInfo) {

        try {
            // --------------------  handle request -----------------------//
            ConsentResource consentResource = new ConsentResource();
            ConsentUtils.copyPropertiesToConsentResource(consentResource, createConsentResourceRequestBody);
            consentResource.setOrgID(orgInfo);


            ArrayList<AuthorizationResource> authorizations = new ArrayList<>();
            if (createConsentResourceRequestBody.getAuthorizationResources() != null) {
                for (CreateAuthorizationResourceRequestBody authorizationResourceDTO :
                        createConsentResourceRequestBody.getAuthorizationResources()) {
                    AuthorizationResource authorizationResource = new AuthorizationResource();
                    ConsentUtils.copyPropertiesToAuthorizationResource(authorizationResource, authorizationResourceDTO);
                    authorizations.add(authorizationResource);
                }
            }

            // --------------------  service call -----------------------//
            DetailedConsentResource result;
            result = consentCoreService.createAuthorizableConsentWithBulkAuth(consentResource,
                    authorizations);

            // --------------------  build response -----------------------//
            ConsentResourceResponse consentResponse = new ConsentResourceResponse();
            ConsentUtils.buildConsentResourceResponse(consentResponse, result, result.getAuthorizationResources(),
                    true);
            return Response.status(Response.Status.CREATED).entity(consentResponse).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            log.error("Error Occurred while handling the request", e);
            JSONObject error = new JSONObject();
            error.put("errorMessage", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }


    public Response consentConsentIdGet(
            String consentID, String orgInfo) {

        try {

            // --------------------  service call -----------------------//
            DetailedConsentResource detailedConsentResource =
                    consentCoreService.getDetailedConsent(consentID);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    detailedConsentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            //--------------------- build response -----------------------//
            ConsentResourceResponse consentResponse = new ConsentResourceResponse();
            ConsentUtils.buildConsentResourceResponse(consentResponse,
                    detailedConsentResource,
                    detailedConsentResource.getAuthorizationResources(), true);


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

    public Response consentGet(
            String orgInfo,
            String consentType, String consentStatus
            , String clientId, String userID, long fromTimeValue, long toTimeValue, int limitValue, int offsetValue) {


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
            if (userID != null) {
                userIDs.add(userID);
            }
            if (clientId != null) {
                Collections.addAll(clientIDs, clientId.split(","));
            }

            Long fromTime = null;
            Long toTime = null;
            Integer limit = null;
            Integer offset = null;
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
            ArrayList<ConsentResourceResponse> consentResponses = new ArrayList<>();

            for (DetailedConsentResource detailedConsentResource : results) {

                ConsentResourceResponse consentResponse = new ConsentResourceResponse();
                ConsentUtils.buildConsentResourceResponse(consentResponse, detailedConsentResource,
                        detailedConsentResource.getAuthorizationResources(),
                        true);
                consentResponses.add(consentResponse);
            }

            return Response.ok().entity(consentResponses).build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public Response consentConsentIdStatusPut(
            String consentID, ConsentStatusUpdateRequestBody consentStatusUpdateResource
            , String orgInfo) {
        try {
            consentCoreService.updateConsentStatusWithImplicitReasonAndUserId(consentID,
                    consentStatusUpdateResource.getStatus(),
                    consentStatusUpdateResource.getReason(),
                    consentStatusUpdateResource.getUserID(),
                    orgInfo);
            return Response.ok().entity("Status Updated").build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    public Response consentStatusPut(BulkConsentStatusUpdateResourceRequestBody bulkConsentStatusUpdateResource,
                                     String orgInfo) {
        try {

            consentCoreService.bulkUpdateConsentStatus(
                    orgInfo,
                    bulkConsentStatusUpdateResource.getClientID(),
                    bulkConsentStatusUpdateResource.getStatus(),
                    bulkConsentStatusUpdateResource.getReason(),
                    bulkConsentStatusUpdateResource.getUserID(),
                    bulkConsentStatusUpdateResource.getConsentType(),
                    bulkConsentStatusUpdateResource.getApplicableStatusesForStateChange());

            return Response.ok().entity("Status Updated").build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }


    public Response consentAuthorizationIdGet(String authorizationId, String orgInfo, String consentId) {

        AuthorizationResourceResponse authResponse = new AuthorizationResourceResponse();

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

    public Response consentAuthorizationGet(String consentId, String orgInfo) {
        try {
            DetailedConsentResource consentResource = consentCoreService.getDetailedConsent(consentId);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            List<AuthorizationResource> authorizationResources = consentResource.getAuthorizationResources();

            return Response.ok().entity(authorizationResources).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    public Response consentAuthorizationIdPost(String consentId, String orgInfo,
                                               List<CreateAuthorizationResourceRequestBody>
                                                       authorizationResourceDTOList) {
        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }


            for (CreateAuthorizationResourceRequestBody authorizationResourceDTO : authorizationResourceDTOList) {
                AuthorizationResource
                        authorizationResource = new AuthorizationResource();
                ConsentUtils.copyPropertiesToAuthorizationResource(authorizationResource, authorizationResourceDTO);
                authorizationResource.setConsentID(consentId);
                consentCoreService.createConsentAuthorization(authorizationResource);

            }

            return Response.ok().entity("Authorization Resources Stored").build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Response consentAuthorizationIdPut(String authorizationId, String consentId, String orgInfo,
                                              CreateAuthorizationResourceRequestBody authorizationResource) {

        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

//            consentCoreService.updateAuthorizationResource( authorizationId, authorizationResource,orgInfo);

            return Response.ok().entity("Authorization Resource Updated").build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    // consentConsentIdExpiryTimePut
    public Response consentConsentIdExpiryTimePut(String consentID, String orgInfo, ConsentExpiryTimeUpdateRequestBody
            consentExpiryTimeUpdateDTO) {
        try {
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource consentResource =
                    consentCoreService.getConsent(consentID,
                            false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            boolean result = consentCoreService.updateConsentExpiryTime(consentID,
                    consentExpiryTimeUpdateDTO.getExpiryTime());
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


    public Response consentConsentIdDelete(String consentID, String orgInfo) {
        try {
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource consentResource =
                    consentCoreService.getConsent(consentID,
                            false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            boolean result = consentCoreService.deleteConsent(consentID);
            if (result) {


                return Response.ok().entity("Consent purged").build();
            } else {
                return Response.serverError().build();

            }


        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }

    }

    public Response consentRevokeConsentIdPost(String consentID,
                                               ConsentRevokeRequestBody consentStatusUpdateResource, String orgInfo) {
        try {
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource consentResource =
                    consentCoreService.getConsent(consentID,
                            false);


            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }


            boolean result = consentCoreService.revokeConsentWithReason(consentID,
                    "revoked",
                    consentStatusUpdateResource.getUserID(),
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

    public Response consentConsentIdAttributesGet(String consentId, String orgInfo) {
        try {
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource consentResource =
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

    public Response consentConsentIdAttributesPost(String consentId, String orgInfo,
                                                   Map<String, String> consentAttributes) {
        try {
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource consentResource =
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

    public Response consentConsentIdAttributesPut(String consentId,
                                                  String orgInfo, Map<String, String> consentAttributes) {
        try {
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource consentResource =
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


    public Response consentConsentIdHistoryGet(
            String consentID, String orgInfo, Boolean detailed, String status, String actionBy, long fromTimeValue,
            long toTimeValue, String statusAuditId) {

        try {

            Long fromTime = null;
            Long toTime = null;
            Integer limit = null;
            Integer offset = null;
            fromTime = fromTimeValue == 0L ? null : fromTimeValue;
            toTime = toTimeValue == 0L ? null : toTimeValue;

            ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords =
                    consentCoreService.searchConsentStatusAuditRecords(consentID, status,
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
                            consentID).values());
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
                        if (!consentMappingResourcesMap.containsKey(consentMappingResource.getAuthorizationID())) {
                            consentMappingResourcesMap.put(consentMappingResource.getAuthorizationID(),
                                    new ArrayList<>());
                        }
                        consentMappingResourcesMap.get(consentMappingResource.getAuthorizationID())
                                .add(consentMappingResource);
                    }

                    ArrayList<org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource>
                            authorizationResourcesWithResources = new ArrayList<>();
                    for (AuthorizationResource authorizationResource :
                            detailedConsentResource.getAuthorizationResources()) {
                        AuthorizationResource
                                auth =
                                new AuthorizationResource();
                        ConsentUtils.buildAuthorizationResourceResponse(auth,
                                authorizationResource,
                                consentMappingResourcesMap.get(authorizationResource.getAuthorizationID()));
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

    private Response handleConsentMgtException(ConsentMgtException e) {
        log.error("Error Occurred while handling the request", e);
        JSONObject error = new JSONObject();
        error.put("errorMessage", e.getMessage());
        return Response.status(e.getErrorCode()).entity(error).build();
    }


}
