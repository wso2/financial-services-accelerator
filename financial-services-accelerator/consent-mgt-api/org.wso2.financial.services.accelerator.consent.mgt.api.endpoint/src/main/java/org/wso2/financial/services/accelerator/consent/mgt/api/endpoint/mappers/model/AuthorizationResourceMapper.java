package org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.mappers.model;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model.AuthorizationResourceResponseBody;

/**
 * Mapper interface for mapping between AuthorizationResource and AuthorizationResourceRequestBody.
 */
@Mapper
public interface AuthorizationResourceMapper {
    AuthorizationResourceMapper INSTANCE = Mappers.getMapper(AuthorizationResourceMapper.class);

    AuthorizationResource toAuthorizationResource(AuthorizationResourceRequestBody authorizationResourceRequestBody);

    AuthorizationResourceResponseBody toAuthorizationResourceResponseBody(AuthorizationResource authorizationResource);
}
