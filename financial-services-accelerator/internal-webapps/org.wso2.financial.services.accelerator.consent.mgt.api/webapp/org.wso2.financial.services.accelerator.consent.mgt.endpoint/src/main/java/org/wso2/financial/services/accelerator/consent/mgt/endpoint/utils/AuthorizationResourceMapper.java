package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;

import java.io.IOException;
import java.util.Map;

/**
 * Mapper interface for mapping between AuthorizationResource and AuthorizationResourceRequestBody.
 */
@Mapper
public interface AuthorizationResourceMapper {
    AuthorizationResourceMapper INSTANCE = Mappers.getMapper(AuthorizationResourceMapper.class);

    // DAO -> DTO
    @Mapping(source = "resource", target = "resource", qualifiedByName = "mapStringToJSONObject")
    AuthorizationResourceRequestBody toAuthorizationResourceRequestBody(AuthorizationResource authorizationResource);

    // DTO -> DAO
    @Mapping(source = "resource", target = "resource", qualifiedByName = "mapObjectToString")
    AuthorizationResource toAuthorizationResource(AuthorizationResourceRequestBody authorizationResourceRequestBody);

    @Named("mapStringToJSONObject")
    default JSONObject mapStringToJSONObject(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.readValue(jsonString, new TypeReference<>() {
            });
            return new JSONObject(map);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON resource string", e);
        }
    }

    @Named("mapObjectToString")
    default String mapObjectToString(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSONObject to JSON string", e);
        }
    }
}
