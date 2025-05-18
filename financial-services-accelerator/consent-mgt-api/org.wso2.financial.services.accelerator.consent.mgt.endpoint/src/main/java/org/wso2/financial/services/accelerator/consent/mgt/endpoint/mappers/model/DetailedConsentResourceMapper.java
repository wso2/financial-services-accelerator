package org.wso2.financial.services.accelerator.consent.mgt.endpoint.mappers.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentError;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceResponseBody;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;


/**
 * This interface is used to map the detailed consent resource.
 */
@Mapper
public interface DetailedConsentResourceMapper {
    DetailedConsentResourceMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(
            DetailedConsentResourceMapper.class);

    /**
     * Maps the detailed consent resource to the request body.
     *
     * @param detailedConsentResource the detailed consent resource
     * @return the detailed consent resource request body
     */
    ConsentResourceResponseBody toConsentResourceRequestBody(DetailedConsentResource detailedConsentResource);

    /**
     * Maps the request body to the detailed consent resource.
     *
     * @param consentResourceResponseBody the detailed consent resource request body
     * @return the detailed consent resource
     */
    DetailedConsentResource toDetailedConsentResource(ConsentResourceRequestBody consentResourceResponseBody);
    /**
     * Converts an Object to a Map<String, String> if possible.
     */
    default Map<String, Object> mapObjectToMap(Object value) throws
            ConsentMgtException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.convertValue(value, Map.class);
            if (map == null) {
                return new HashMap<>();

            }
            return map.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        } catch (IllegalArgumentException e) {
            throw new ConsentMgtException(Response.Status.BAD_REQUEST, ConsentError.PAYLOAD_SCHEMA_VALIDATION_ERROR);
        }
    }

}
