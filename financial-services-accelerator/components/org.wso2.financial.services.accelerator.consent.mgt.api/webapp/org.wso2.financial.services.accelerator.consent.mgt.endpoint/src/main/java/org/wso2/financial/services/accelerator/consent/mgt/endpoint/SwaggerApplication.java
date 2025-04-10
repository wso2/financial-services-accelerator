package org.wso2.financial.services.accelerator.consent.mgt.endpoint;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application class.
 */
@ApplicationPath("/api")  // Base path for JAX-RS
@OpenAPIDefinition(info = @Info(title = "Consent Management API", version = "1.0", description = "Consent Management " +
        "application" +
        " API"))
public class SwaggerApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(OpenApiResource.class);  // Registers Swagger OpenAPI Resource
        return classes;
    }
}
