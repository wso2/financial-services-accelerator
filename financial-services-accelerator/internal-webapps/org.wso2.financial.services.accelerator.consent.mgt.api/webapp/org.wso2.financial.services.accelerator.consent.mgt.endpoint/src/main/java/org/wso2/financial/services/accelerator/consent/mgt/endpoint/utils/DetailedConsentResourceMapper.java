package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import org.mapstruct.Mapper;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceResponseBody;

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
}
