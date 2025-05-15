package org.wso2.financial.services.accelerator.consent.mgt.endpoint.mappers.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentError;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

/**
 * Mapper interface for mapping between ConsentResource and ConsentResourceRequestBody.
 */
@Mapper
public interface ConsentResourceMapper {
    ConsentResourceMapper INSTANCE = Mappers.getMapper(ConsentResourceMapper.class);

    @Mapping(source = "consentAttributes", target = "consentAttributes")
    ConsentResource toConsentResource(ConsentResourceRequestBody consentResourceRequestBody);

    @Mapping(source = "consentAttributes", target = "consentAttributes")
    ConsentResourceRequestBody toConsentResourceRequestBody(ConsentResource consentResource);

    /**
     * Converts an Object to a Map<String, String> if possible.
     */
    default Map<String, String> mapObjectToMap(Object value) throws
            ConsentMgtException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.convertValue(value, Map.class);
            if (map == null) {
                return new HashMap<>();
            }
            return map.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        } catch (IllegalArgumentException e) {
            throw new ConsentMgtException(Response.Status.BAD_REQUEST, ConsentError.PAYLOAD_SCHEMA_VALIDATION_ERROR);
        }
    }

    /**
     * Converts a Map<String, String> to Object (if needed).
     * This may be useful depending on how the ConsentResource model stores the attributes.
     */
    default Object mapMapToObject(Map<String, String> map) {
        return map;
    }
}
