package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceResponseBody;

/**
 * Mapper interface for mapping between AuthorizationResource and AuthorizationResourceRequestBody.
 */
@Mapper
public interface AuthorizationResourceMapper {
    AuthorizationResourceMapper INSTANCE = Mappers.getMapper(AuthorizationResourceMapper.class);

    // DAO -> DTO
    AuthorizationResourceRequestBody toAuthorizationResourceRequestBody(AuthorizationResource authorizationResource);

    // DTO -> DAO
    AuthorizationResource toAuthorizationResource(AuthorizationResourceRequestBody authorizationResourceRequestBody);

    AuthorizationResourceResponseBody toAuthorizationResourceResponseBody(AuthorizationResource authorizationResource);
}
