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
package com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.dto.RegistrationErrorDTO;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationError;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.utils.ValidatorUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.RegistrationValidator;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

/**
 * Util class which includes helper methods required for DCR.
 */
public class RegistrationUtils {

    private static final Log log = LogFactory.getLog(RegistrationUtils.class);

    private static final String DISALLOWED_CHARS_PATTERN = "([~!#$;%^&*+={}\\s\\|\\\\<>\\\"\\'\\/,\\]\\[\\(\\)])";
    private static final String SUBSTITUTE_STRING = "_";
    private static final int ABBREVIATED_STRING_LENGTH = 70;
    private static Gson gson = new Gson();

    /**
     * this method invokes the configured registration VALIDATOR class
     * by default the DefaultRegistrationValidatorImpl class will  be configured.
     *
     * @param registrationRequest object containing the client registration request details
     */
    public static void validateRegistrationCreation(RegistrationRequest registrationRequest)
            throws ParseException, DCRValidationException {

        RegistrationValidator dcrRequestValidator;
        dcrRequestValidator = RegistrationValidator.getRegistrationValidator();
        // set the ssa payload according to the specification format
        if (registrationRequest.getSoftwareStatement() != null) {
            String decodedSSA = JWTUtils
                    .decodeRequestJWT(registrationRequest.getSoftwareStatement(), "body").toJSONString();
            dcrRequestValidator.setSoftwareStatementPayload(registrationRequest, decodedSSA);
        }

        // do common validations
        ValidatorUtils.getValidationViolations(registrationRequest);

    }

    public static RegistrationErrorDTO getErrorDTO(String errorCode, String errorMessage) {

        RegistrationErrorDTO registrationErrorDTO = new RegistrationErrorDTO();
        registrationErrorDTO.setError(errorCode);
        registrationErrorDTO.setErrorDescription(errorMessage);
        return registrationErrorDTO;
    }

    public static RegistrationError getRegistrationError(String errorCode, String errorMessage) {

        RegistrationError registrationError = new RegistrationError();
        registrationError.setErrorCode(errorCode);
        registrationError.setErrorMessage(errorMessage);
        return registrationError;
    }

    public static ApplicationRegistrationRequest getApplicationRegistrationRequest(
            RegistrationRequest registrationRequest, boolean useSoftwareIdAsAppName) {

        String applicationName;
        if (useSoftwareIdAsAppName) {
            applicationName = (registrationRequest.getSoftwareStatement() != null) ?
                    registrationRequest.getSoftwareStatementBody().getSoftwareId() :
                    registrationRequest.getSoftwareId();
        } else {
            applicationName = RegistrationUtils.getSafeApplicationName(registrationRequest.getSoftwareStatementBody()
                            .getClientName());
        }

        ApplicationRegistrationRequest appRegistrationRequest = new ApplicationRegistrationRequest();
        appRegistrationRequest.setClientName(applicationName);

        // Set the redirect URIs based on the presence of software statement
        appRegistrationRequest.setRedirectUris(
                StringUtils.isBlank(registrationRequest.getSoftwareStatement()) ?
                        registrationRequest.getCallbackUris() :
                        registrationRequest.getSoftwareStatementBody().getCallbackUris());

        appRegistrationRequest.setGrantTypes(registrationRequest.getGrantTypes());

        return appRegistrationRequest;

    }

    public static ApplicationUpdateRequest getApplicationUpdateRequest(RegistrationRequest registrationRequest,
                                                                       boolean useSoftwareIdAsAppName) {
        String applicationName;
        if (useSoftwareIdAsAppName) {
            applicationName = (registrationRequest.getSoftwareStatement() != null) ?
                    registrationRequest.getSoftwareStatementBody().getSoftwareId() :
                    registrationRequest.getSoftwareId();
        } else {
            applicationName = RegistrationUtils.getSafeApplicationName(registrationRequest.getSoftwareStatementBody()
                            .getClientName());
        }

        ApplicationUpdateRequest applicationUpdateRequest = new ApplicationUpdateRequest();
        applicationUpdateRequest.setClientName(applicationName);

        // Set the redirect URIs based on the presence of the software statement
        applicationUpdateRequest.setRedirectUris(
                StringUtils.isBlank(registrationRequest.getSoftwareStatement()) ?
                        registrationRequest.getCallbackUris() :
                        registrationRequest.getSoftwareStatementBody().getCallbackUris());

        applicationUpdateRequest.setGrantTypes(registrationRequest.getGrantTypes());

        return applicationUpdateRequest;

    }

    public static ArrayList<ServiceProviderProperty> getServiceProviderPropertyList
            (Map<String, String> clientMetaData) {

        ArrayList<ServiceProviderProperty> spPropList = new ArrayList<>();
        for (Map.Entry<String, String> entry : clientMetaData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            ServiceProviderProperty serviceProviderproperty = new ServiceProviderProperty();
            if (value != null) {
                serviceProviderproperty.setDisplayName(key);
                serviceProviderproperty.setName(key);
                serviceProviderproperty.setValue(value);
                spPropList.add(serviceProviderproperty);
            }
        }
        return spPropList;

    }

    public static Map<String, Object> getSpMetaDataMap(List<ServiceProviderProperty> spPropertyList) {

        Map<String, Object> spMetaDataMap = new HashMap<>();
        for (ServiceProviderProperty spProperty : spPropertyList) {
            if (spProperty.getValue().contains(DCRCommonConstants.ARRAY_ELEMENT_SEPERATOR)) {
                List<Object> metaDataList = Stream.of(spProperty.getValue()
                        .split(DCRCommonConstants.ARRAY_ELEMENT_SEPERATOR))
                        .map(String::trim)
                        .collect(Collectors.toList());
                getJsonElementListFromString(metaDataList);
                spMetaDataMap.put(spProperty.getName(), metaDataList);
            } else if (spProperty.getValue().contains("{")) {
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = ((JsonObject) jsonParser
                        .parse(spProperty.getValue()));
                spMetaDataMap.put(spProperty.getName(), jsonObject);
            } else {
                spMetaDataMap.put(spProperty.getName(), spProperty.getValue());
            }

        }
        return spMetaDataMap;
    }

    public static String getSafeApplicationName(String applicationName) {

        if (StringUtils.isEmpty(applicationName)) {
            throw new IllegalArgumentException("Application name should be a valid string");
        }

        String sanitizedInput = applicationName.trim().replaceAll(DISALLOWED_CHARS_PATTERN, SUBSTITUTE_STRING);
        return StringUtils.abbreviate(sanitizedInput, ABBREVIATED_STRING_LENGTH);

    }

    public static Map<String, String> getAlteredApplicationAttributes(RegistrationRequest registrationRequest)
            throws ParseException {

        Map<String, String> alteredAppAttributeMap = new HashMap<>();
        addAttributes(registrationRequest.getRequestParameters(), alteredAppAttributeMap);

        if (registrationRequest.getSoftwareStatement() != null) {
            //add ssa attributes
            addAttributes(registrationRequest.getSsaParameters(), alteredAppAttributeMap);
            //add ssa issuer
            alteredAppAttributeMap.put("ssaIssuer", registrationRequest.getSsaParameters().get("iss").toString());
        }

        return alteredAppAttributeMap;
    }

    public static void addAttributes(Map<String, Object> requestAttributes,
                                     Map<String, String> alteredAttributes) {

        String alteredValue = "";
        for (Map.Entry entry : requestAttributes.entrySet()) {
            alteredValue = "";
            if (entry.getValue() instanceof ArrayList<?>) {

                ArrayList<Object> list = ((ArrayList<Object>) entry.getValue());
                Object lastListElement = new Object();
                if (list.size() > 0) {
                    lastListElement = list.get(list.size() - 1);
                }
                getJsonElementList(list);
                if (list.size() == 1) {
                    alteredValue = list.get(0).toString().concat(DCRCommonConstants.ARRAY_ELEMENT_SEPERATOR);
                    alteredAttributes.put(entry.getKey().toString(), alteredValue);
                } else if (list.size() > 0) {
                    for (Object listElement : list) {
                        if (!lastListElement.equals(listElement)) {
                            alteredValue = alteredValue.concat(
                                    listElement.toString().concat(DCRCommonConstants.ARRAY_ELEMENT_SEPERATOR));
                        } else {
                            alteredValue = alteredValue.concat(lastListElement.toString());
                        }
                    }
                    alteredAttributes.put(entry.getKey().toString(), alteredValue);
                }
            } else if (entry.getValue() instanceof Map) {
                alteredAttributes.put(entry.getKey().toString(), gson.toJson(entry.getValue()));
            } else {
                //remove unnecessary inverted commas.
                if (entry.getValue() != null) {
                    // This is to handle optional nullable params.
                    // Ex: "software_on_behalf_of_org":null
                    alteredAttributes.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
    }

    public static Map<String, Object> getRegistrationDetailsForResponse(RegistrationRequest registrationRequest) {

        String registrationRequestJson = gson.toJson(registrationRequest);
        return gson.fromJson(registrationRequestJson, Map.class);
    }

    /**
     * Extract headers from a request object.
     *
     * @param request The request object
     * @return Map of header key value pairs
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {

        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    /**
     * Util method to extract the payload from a HTTP request object. Can be JSONObject or JSONArray
     *
     * @param request The HTTP request object
     * @return Object payload can be either an instance of JSONObject or JSONArray only. Can be a ConsentException if
     * is and error scenario. Error is returned instead of throwing since the error response should be handled by the
     * toolkit is the manage scenario.
     */
    public static Object getPayload(HttpServletRequest request) throws IOException,
            net.minidev.json.parser.ParseException {

        Object payload = new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(IOUtils.toString(request.getInputStream()));
        if (payload == null) {
            log.debug("Payload is empty. Returning null");
            return null;
        }
        if (!(payload instanceof JSONObject || payload instanceof JSONArray)) {
            //Not throwing error since error should be formatted by manage toolkit
            log.error("Payload is not a JSON. Returning null");
            return null;
        }
        return payload;
    }

    /**
     * check whether the elemet is a json and convert to a json.
     *
     * @param metaDataList meta data property list
     */
    public static void getJsonElementList(List<Object> metaDataList) {

        for (Iterator<Object> iterator = metaDataList.iterator(); iterator.hasNext(); ) {
            Object element = iterator.next();
            if (element.toString().contains("{")) {
                //Object elementToRemove = element;
                metaDataList.set(metaDataList.indexOf(element), gson.toJson(element));
            }
        }
    }

    public static void getJsonElementListFromString(List<Object> metaDataList) {

        for (Iterator<Object> iterator = metaDataList.iterator(); iterator.hasNext(); ) {
            Object element = iterator.next();
            if (element.toString().contains("{")) {
                metaDataList.set(metaDataList.indexOf(element),
                        new JsonParser().parse(element.toString()).getAsJsonObject());
            }
        }
    }
}
