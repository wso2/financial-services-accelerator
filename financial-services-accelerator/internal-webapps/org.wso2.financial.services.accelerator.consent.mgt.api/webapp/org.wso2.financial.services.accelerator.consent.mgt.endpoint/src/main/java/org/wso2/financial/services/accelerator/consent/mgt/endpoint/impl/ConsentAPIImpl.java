package org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AmendmentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceDTO;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentExpiryTimeUpdateDTO;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceDTO;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ReauthorizeResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentCoreServiceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            ConsentResourceDTO consentResourceDTO, String orgInfo) {

        try {
            //////////////  handle request //////////////
            ConsentResource consentResource = new ConsentResource();
            ConsentUtils.copyPropertiesToConsentResource(consentResource, consentResourceDTO);
            consentResource.setOrgID(orgInfo);


            // parse authorization resources
            ArrayList<AuthorizationResource> authorizations = new ArrayList<>();

            if (consentResourceDTO.getAuthorizationResources() != null) {

                for (AuthorizationResourceDTO authorizationResourceDTO :
                        consentResourceDTO.getAuthorizationResources()) {
                    AuthorizationResource authorizationResource = new AuthorizationResource();
                    ConsentUtils.copyPropertiesToAuthorizationResource(authorizationResource, authorizationResourceDTO);
                    authorizations.add(authorizationResource);

                }
            }

            ////////////// Service call //////////////
            DetailedConsentResource result = null;

            result = consentCoreService.createAuthorizableConsentWithBulkAuth(consentResource,
                    authorizations);


            //////////////  build response //////////////
            ConsentResponse consentResponse = new ConsentResponse();
            ConsentUtils.buildConsentResourceResponse(consentResponse, result, result.getAuthorizationResources(),
                    result.getConsentMappingResources(),
                    true);

            return Response.status(Response.Status.CREATED).entity(consentResponse).build();
        }  catch (ConsentMgtException e) {
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


            if (ConsentUtils.isConsentIdValid(consentID)) {

                try {

                        ////////////// Service call //////////////
                        DetailedConsentResource detailedConsentResource =
                                consentCoreService.getDetailedConsent(consentID);

                        if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                                detailedConsentResource.getOrgID())) {
                            log.error("OrgInfo does not match");
                            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                                    "OrgInfo does not match, please provide the correct OrgInfo");
                        }

                        //////////////  build Response  //////////////
                        ConsentResponse consentResponse = new ConsentResponse();
                        ConsentUtils.buildConsentResourceResponse(consentResponse,
                                detailedConsentResource,
                                detailedConsentResource.getAuthorizationResources(),
                                detailedConsentResource.getConsentMappingResources(), true);



                        return Response.ok().entity(consentResponse).build();


                } catch (JSONException e) {
                    log.error("Error Occurred while handling the request", e);
                    throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
                } catch (JsonProcessingException e) {
                    throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
                }
            } else {
                log.error("Invalid Consent ID");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "Invalid Consent ID");
            }
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    public Response consentGet(
            String orgInfo,
            String consentType, String consentStatus
            , String clientId, String userID, long fromTimeValue, long toTimeValue, int limitValue, int offsetValue) {


        try {


            ////////////// initialize the search query //////////////
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

            ////////////// service call //////////////
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




            ////////////// build response //////////////
            ArrayList<ConsentResponse> consentResponses = new ArrayList<>();

            for (DetailedConsentResource detailedConsentResource : results) {

                ConsentResponse consentResponse = new ConsentResponse();
                ConsentUtils.buildConsentResourceResponse(consentResponse, detailedConsentResource,
                        detailedConsentResource.getAuthorizationResources(),
                        detailedConsentResource.getConsentMappingResources(), true);
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
            String consentID, ConsentStatusUpdateResource consentStatusUpdateResource
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

    public Response consentStatusPut(BulkConsentStatusUpdateResource bulkConsentStatusUpdateResource, String orgInfo) {
        try {

            consentCoreService.bulkUpdateConsentStatus(
                    orgInfo,
                    bulkConsentStatusUpdateResource.getClientID(),
                    bulkConsentStatusUpdateResource.getStatus(),
                    bulkConsentStatusUpdateResource.getReason(),
                    bulkConsentStatusUpdateResource.getUserID(),
                    bulkConsentStatusUpdateResource.getConsentType(),
                    (ArrayList<String>) bulkConsentStatusUpdateResource.getApplicableStatusesForStateChange());

            return Response.ok().entity("Status Updated").build();
        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    public Response consentConsentIdPut(
            String consentID, AmendmentResource amendmentResource,
            String orgInfo) {

        try {
            ////////////// handle request //////////////
            // get authorization resources without authId
            ArrayList<AuthorizationResource> newAuthorization = new ArrayList<>();

            // get authorization resources with authId
            ArrayList<AuthorizationResource> reAuthorization = new ArrayList<>();

            // get resources without authId
            Map<String, ArrayList<ConsentMappingResource>> newResources = new HashMap<>();

            // get resources with authId
            ArrayList<ConsentMappingResource> reResources = new ArrayList<>();

            // iterate through authorization resources and build new and existing auth and resource objects
            for (ReauthorizeResource authResourceDTO : amendmentResource.getAuthorizationResources()) {
                //existing auth
                if (authResourceDTO.getAuthId() != null) {
                    AuthorizationResource auth = new AuthorizationResource();
                    ConsentUtils.copyPropertiesToAuthorizationResource(auth, authResourceDTO);
                    auth.setConsentID(consentID);
                    reAuthorization.add(auth);

                } else {
                    // new auth
                    AuthorizationResource auth = new AuthorizationResource();
                    ConsentUtils.copyPropertiesToAuthorizationResource(auth, authResourceDTO);
                    auth.setConsentID(consentID);
                    newAuthorization.add(auth);
                }
            }

            // TODO : // action by ??
            String userID;
            if (amendmentResource.getAuthorizationResources().isEmpty()) {
                userID = null;
            } else {
                userID = amendmentResource.getAuthorizationResources().get(0).getUserID();
            }

            ////////////// service call //////////////
            DetailedConsentResource amendmentResponse = consentCoreService.amendDetailedConsentWithBulkAuthResource(
                    orgInfo,
                    consentID,
                    amendmentResource.getReceipt(),
                    Long.valueOf(amendmentResource.getExpiryTime()),
                    reAuthorization,
                    amendmentResource.getCurrentStatus(),
                    ConsentUtils.convertToMap(amendmentResource.getConsentAttributes()),
                    userID,
                    newAuthorization);


            ////////////// build response //////////////
            ConsentResponse consentResponse = new ConsentResponse();
            ConsentUtils.buildConsentResourceResponse(consentResponse, amendmentResponse,
                    amendmentResponse.getAuthorizationResources(), amendmentResponse.getConsentMappingResources(),
                    true);

            return Response.ok().entity(consentResponse).build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }




    public Response consentAuthorizationIdGet(String authorizationId, String orgInfo, String consentId) {
        AuthResponse authResponse = new AuthResponse();

        try {

            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

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

    public Response consentAuthorizationIdPost( String consentId, String orgInfo,
                                                List<AuthorizationResourceDTO> authorizationResourceDTOList){
        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }


            for (AuthorizationResourceDTO authorizationResourceDTO : authorizationResourceDTOList) {
                AuthorizationResource authorizationResource = new AuthorizationResource();
                ConsentUtils.copyPropertiesToAuthorizationResource(authorizationResource, authorizationResourceDTO);
                authorizationResource.setConsentID(consentId);
                consentCoreService.createConsentAuthorization( authorizationResource);

            }


            return Response.ok().entity("Authorization Resources Stored").build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public  Response consentAuthorizationIdPut (String authorizationId, String consentId, String orgInfo,
                                                 ReauthorizeResource reauthorizeResource){

        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            AuthorizationResource authorizationResource = new AuthorizationResource();
            ConsentUtils.copyPropertiesToAuthorizationResource(authorizationResource, reauthorizeResource);
            authorizationResource.setConsentID(consentId);
//            consentCoreService.upa(authorizationResource);

            return Response.ok().entity("Authorization Resource Updated").build();

        } catch (ConsentMgtException e) {
            return handleConsentMgtException(e);
        }
    }

    // consentConsentIdExpiryTimePut
    public Response consentConsentIdExpiryTimePut(String consentID, String orgInfo, ConsentExpiryTimeUpdateDTO
            consentExpiryTimeUpdateDTO) {
        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentID,
                    false);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgInfo,
                    consentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            boolean result = consentCoreService.updateConsentExpiryTime(consentID,
                    consentExpiryTimeUpdateDTO.getExpiryTime() );
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
            ConsentResource consentResource = consentCoreService.getConsent(consentID,
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
                                              ConsentRevokeResource consentStatusUpdateResource, String orgInfo) {
        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentID,
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

    public Response consentConsentIdAttributesGet (String consentId, String orgInfo){
        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId,
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

    public Response consentConsentIdAttributesPost (String consentId, String orgInfo,
                                                    Map<String, String> consentAttributes) {
        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId,
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

    public Response consentConsentIdAttributesPut (String consentId,
                                                   String orgInfo, Map<String , String > consentAttributes){
        try {
            ConsentResource consentResource = consentCoreService.getConsent(consentId,
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

                    ArrayList<AuthorizationResource> authorizationResourcesWithResources = new ArrayList<>();
                    for (AuthorizationResource authorizationResource :
                            detailedConsentResource.getAuthorizationResources()) {
                        AuthorizationResource auth = new AuthorizationResource();
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
