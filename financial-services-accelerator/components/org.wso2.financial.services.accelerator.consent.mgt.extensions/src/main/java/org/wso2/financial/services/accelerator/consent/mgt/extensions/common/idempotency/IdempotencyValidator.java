/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentOperationEnum;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIModifiedResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ExternalAPIConsentManageUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Class to handle idempotency related operations.
 */
public class IdempotencyValidator {

    private static final Log log = LogFactory.getLog(IdempotencyValidator.class);
    private static ConsentCoreService consentCoreService = null;
    private static FinancialServicesConfigParser configParser = null;
    private static boolean isExtensionsEnabled = false;
    private static boolean isExternalPostConsentGenerationEnabled = false;

    public IdempotencyValidator () {
        configParser = FinancialServicesConfigParser.getInstance();
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        isExtensionsEnabled = configParser.isServiceExtensionsEndpointEnabled();
        isExternalPostConsentGenerationEnabled = configParser.getServiceExtensionTypes()
                .contains(ServiceExtensionTypeEnum.ENRICH_CONSENT_CREATION_RESPONSE);
    }

    /**
     * Method to check whether the request is a valid idempotent request.
     *
     * @param consentManageData  Consent Manage Data object
     * @param type               Payment type
     * @return whether the request is idempotent
     */
    public boolean isIdempotent(ConsentManageData consentManageData, String type) {

        try {
            IdempotencyValidationResult result = validateIdempotency(consentManageData);
            if (result.isIdempotent()) {
                if (result.isValid()) {
                    if (consentManageData.getRequestPath().contains("fileUpload")) {
                        consentManageData.setResponseStatus(ResponseStatus.OK);
                    } else {
                        if (isExtensionsEnabled && isExternalPostConsentGenerationEnabled) {
                            ExternalAPIPostConsentGenerateRequestDTO postRequestDTO =
                                    getPostRequestDTO(consentManageData, result);
                            ExternalAPIModifiedResponseDTO postResponseDTO = ExternalAPIConsentManageUtils.
                                    callExternalService(postRequestDTO);

                            if (postResponseDTO.getModifiedResponse() != null) {
                                consentManageData.setResponsePayload(postResponseDTO.getModifiedResponse());
                            } else {
                                consentManageData.setResponsePayload(new JSONObject());
                            }
                            if (postResponseDTO.getResponseHeaders() != null) {
                                consentManageData.setResponseHeaders(postResponseDTO.getResponseHeaders());
                            } else {
                                consentManageData.setResponseHeaders(new HashMap<>());
                            }
                        } else {
                            consentManageData.setResponsePayload(ConsentExtensionUtils
                                    .getInitiationResponse(consentManageData.getPayload(), result.getConsent()));
                        }
                        consentManageData.setResponseStatus(ResponseStatus.CREATED);
                    }
                    return true;
                } else {
                    log.error(ErrorConstants.IDEMPOTENCY_KEY_FRAUDULENT);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.IDEMPOTENCY_KEY_FRAUDULENT,
                            ConsentOperationEnum.CONSENT_DEFAULT);
                }
            }
        } catch (IdempotencyValidationException e) {
            log.error(ErrorConstants.IDEMPOTENCY_KEY_FRAUDULENT, e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.IDEMPOTENCY_KEY_FRAUDULENT,
                    ConsentOperationEnum.CONSENT_DEFAULT);
        } catch (ConsentManagementException e) {
            log.error("Error Occurred while handling the request", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error Occurred while handling the request", ConsentOperationEnum.CONSENT_CREATE);
        }
        return false;
    }

    private static ExternalAPIPostConsentGenerateRequestDTO getPostRequestDTO(ConsentManageData consentManageData,
                                                                              IdempotencyValidationResult result) {
        DetailedConsentResource createdConsentResource = result.getConsent();
        ExternalAPIConsentResourceRequestDTO externalAPIConsentResource =
                new ExternalAPIConsentResourceRequestDTO(createdConsentResource);
        return new ExternalAPIPostConsentGenerateRequestDTO(externalAPIConsentResource,
        consentManageData.getRequestPath());
    }

    /**
     * Method to check whether the request is idempotent.
     * This method will first check whether idempotency validation is enabled. After that it will validate whether
     * required parameters for validation is present.
     * For validation, need to check whether the idempotency key values is present as a consent attribute, if present
     * the consent will be retrieved. Finally following conditions will be validated.
     *  - Whether the client id sent in the request and client id retrieved from the database are equal
     *  - Whether the difference between two dates is less than the configured time
     *  - Whether payloads are equal
     *
     * @param consentManageData            Consent Manage Data
     * @return  IdempotencyValidationResult
     * @throws IdempotencyValidationException    If an error occurs while validating idempotency
     */
    public IdempotencyValidationResult validateIdempotency(ConsentManageData consentManageData)
            throws IdempotencyValidationException {

        if (!configParser.isIdempotencyValidationEnabled()) {
            return new IdempotencyValidationResult(false, false);
        }

        // If client id is empty then cannot proceed with idempotency validation
        if (StringUtils.isBlank(consentManageData.getClientId())) {
            log.error("Client ID is empty. Hence cannot proceed with idempotency validation");
            return new IdempotencyValidationResult(false, false);
        }
        //idempotencyKeyValue is the value of the idempotency key sent in the request
        String idempotencyKeyValue = consentManageData.getHeaders().get(getIdempotencyHeaderName()) == null ?  null :
                consentManageData.getHeaders().get(getIdempotencyHeaderName()).replaceAll("[\r\n]", "");
        // If idempotency key value is empty then cannot proceed with idempotency validation
        if (StringUtils.isBlank(idempotencyKeyValue)) {
            log.error("Idempotency Key Value is empty. Hence cannot proceed with idempotency validation");
            return new IdempotencyValidationResult(false, false);
        }

        try {
            // idempotencyKeyName is the name of the attribute key which the idempotency key
            // is stored against in the consent attributes
            String idempotencyKeyName = getIdempotencyAttributeName(consentManageData);
            // Retrieve consent ids that have the idempotency key name and value as attribute
            List<String> consentIds = IdempotencyValidationUtils
                    .getConsentIdsFromIdempotencyKey(idempotencyKeyName, idempotencyKeyValue);
            // Check whether the consent id list is not empty. If idempotency key exists in the database then
            // the consent Id list will be not empty.
            if (!consentIds.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Idempotency Key  %s exists in the database. Hence this is an" +
                            " idempotent request", idempotencyKeyValue.replaceAll("[\r\n]", "")));
                }
                for (String consentId : consentIds) {
                    DetailedConsentResource consentResource = consentCoreService.getDetailedConsent(consentId);
                    if (consentResource != null) {
                        if (!configParser.getIdempotencyAllowedConsentTypes().contains(
                                consentResource.getConsentType())) {
                            return new IdempotencyValidationResult(false, false);
                        }
                        return validateIdempotencyConditions(consentManageData, consentResource);
                    } else {
                        String errorMsg = String.format(IdempotencyConstants.ERROR_NO_CONSENT_DETAILS, consentId);
                        log.error(errorMsg.replaceAll("[\r\n]", ""));
                        throw new IdempotencyValidationException(errorMsg);
                    }
                }
            }
        } catch (IOException e) {
            log.error(IdempotencyConstants.JSON_COMPARING_ERROR, e);
            throw new IdempotencyValidationException(IdempotencyConstants.JSON_COMPARING_ERROR);
        } catch (ConsentManagementException e) {
            log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
            return new IdempotencyValidationResult(true, false);
        }
        return new IdempotencyValidationResult(false, false);
    }


    /**
     * Method to check whether the idempotency conditions are met.
     * This method will validate the following conditions.
     *  - Whether the client id sent in the request and client id retrieved from the database are equal
     *  - Whether the difference between two dates is less than the configured time
     *  - Whether payloads are equal
     *
     * @param consentManageData        Consent Manage Data
     * @param consentResource          Detailed Consent Resource
     * @return  IdempotencyValidationResult
     */
    private IdempotencyValidationResult validateIdempotencyConditions(ConsentManageData consentManageData,
                                                                      DetailedConsentResource consentResource)
            throws IdempotencyValidationException, IOException {
        // Compare the client ID sent in the request and client id retrieved from the database
        // to validate whether the request is received from the same client
        if (IdempotencyValidationUtils.isClientIDEqual(consentResource.getClientID(),
                consentManageData.getClientId())) {
            // Check whether difference between two dates is less than the configured time
            if (IdempotencyValidationUtils.isRequestReceivedWithinAllowedTime(getCreatedTimeOfPreviousRequest(
                    consentManageData, consentResource))) {

                // Compare whether JSON payloads are equal
                if (isPayloadSimilar(consentManageData, getPayloadOfPreviousRequest(consentManageData,
                        consentResource))) {
                    log.debug("Payloads are similar and request received within allowed" +
                            " time. Hence this is a valid idempotent request");
                    return new IdempotencyValidationResult(true, true,
                            consentResource, consentResource.getConsentID());
                } else {
                    log.error(IdempotencyConstants.ERROR_PAYLOAD_NOT_SIMILAR);
                    throw new IdempotencyValidationException(IdempotencyConstants.ERROR_PAYLOAD_NOT_SIMILAR);
                }
            } else {
                log.error(IdempotencyConstants.ERROR_AFTER_ALLOWED_TIME);
                throw new IdempotencyValidationException(IdempotencyConstants.ERROR_AFTER_ALLOWED_TIME);
            }
        } else {
            log.error(IdempotencyConstants.ERROR_MISMATCHING_CLIENT_ID);
            throw new IdempotencyValidationException(IdempotencyConstants.ERROR_MISMATCHING_CLIENT_ID);
        }
    }

    /**
     * Method to get the Idempotency Attribute Name store in consent Attributes.
     *
     * @param consentManageData Consent Manage Data Object
     * @return idempotency Attribute Name.
     */
    private String getIdempotencyAttributeName(ConsentManageData consentManageData) {
        if (consentManageData.getRequestPath().contains("fileUpload")) {
            return ConsentExtensionConstants.FILE_UPLOAD_IDEMPOTENCY_KEY;
        } else {
            return ConsentExtensionConstants.IDEMPOTENCY_KEY;
        }
    }

    /**
     * Method to get the Idempotency Header Name according to the request.
     *
     * @return idempotency Header Name.
     */
    private String getIdempotencyHeaderName() {
        return configParser.getIdempotencyHeaderName();
    }

    /**
     * Method to get created time from the Detailed Consent Resource.
     *
     * @param consentManageData     Consent Manage Data Object
     * @param consentResource       Consent Resource
     * @return Created Time.
     */
    private long getCreatedTimeOfPreviousRequest(ConsentManageData consentManageData,
                                                 DetailedConsentResource consentResource)  {

        if (consentManageData.getRequestPath().contains("fileUpload")) {
            if (consentResource.getConsentAttributes() != null && consentResource.getConsentAttributes()
                    .containsKey(ConsentExtensionConstants.FILE_UPLOAD_CREATED_TIME)) {
                return Long.parseLong(consentResource.getConsentAttributes()
                        .get(ConsentExtensionConstants.FILE_UPLOAD_CREATED_TIME));
            } else {
                return 0L;
            }
        }
        return  consentResource.getCreatedTime();
    }

    /**
     * Method to get payload from previous request.
     *
     * @param consentManageData     Consent Manage Data Object
     * @param consentResource       Consent Resource
     * @return Map containing the payload.
     */
    private String getPayloadOfPreviousRequest(ConsentManageData consentManageData,
                                               DetailedConsentResource consentResource) {

        if (consentManageData.getRequestPath().contains("fileUpload")) {
            try {
                ConsentFile consentFile = consentCoreService.getConsentFile(consentResource.getConsentID());
                if (consentFile == null) {
                    return null;
                }
                return consentFile.getConsentFile();
            } catch (ConsentManagementException e) {
                log.error("Error occurred while getting the consent file for the consent ID: " +
                        consentResource.getConsentID().replaceAll("[\r\n]", ""), e);
                return null;
            }
        }
        return consentResource.getReceipt();
    }

    /**
     * Method to compare whether payloads are equal.
     *
     * @param consentManageData   Consent Manage Data Object
     * @param consentReceipt      Payload received from database
     * @return   Whether payloads are equal
     */
    private boolean isPayloadSimilar(ConsentManageData consentManageData, String consentReceipt) {

        try {
            if (consentManageData.getHeaders().get("content-type").contains("xml")) {
                return isXMLPayloadSimilar(consentManageData.getPayload().toString(), consentReceipt);
            } else {
                return isJSONPayloadSimilar(consentManageData.getPayload().toString(), consentReceipt);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Error occurred while comparing payloads", e);
            return false;
        }
    }

    /**
     * Method to compare whether payloads are equal.
     *
     * @param incomingPayload             Payload received from request
     * @param storedConsentReceipt        Payload retrieved from database
     * @return   Whether payloads are equal
     * @throws IOException If an error occurs while comparing JSON payloads
     */
    private boolean isJSONPayloadSimilar(String incomingPayload, String storedConsentReceipt)
            throws IOException {

        if (incomingPayload == null || storedConsentReceipt == null) {
            return false;
        }

        JsonNode expectedNode = new ObjectMapper().readTree(storedConsentReceipt);
        JsonNode actualNode = new ObjectMapper().readTree(incomingPayload);
        return expectedNode.equals(actualNode);
    }

    /**
     * Method to compare whether XML payloads are equal.
     *
     * @param incomingPayload     Payload received from current request
     * @param storedPayload       Payload retrieved from database
     * @return   Whether XML payloads are equal
     * @throws IOException If an error occurs while comparing XML payloads
     */
    private boolean isXMLPayloadSimilar(String incomingPayload, String storedPayload)
            throws ParserConfigurationException, IOException, SAXException {

        if (storedPayload == null || incomingPayload == null) {
            return false;
        }
        // Compare XML payloads
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // To avoid XXE_DOCUMENT vulnerability
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document storedDoc = db.parse(new InputSource(new StringReader(storedPayload)));
        Document incomingDoc = db.parse(new InputSource(new StringReader(incomingPayload)));
        return storedDoc.isEqualNode(incomingDoc);
    }
}
