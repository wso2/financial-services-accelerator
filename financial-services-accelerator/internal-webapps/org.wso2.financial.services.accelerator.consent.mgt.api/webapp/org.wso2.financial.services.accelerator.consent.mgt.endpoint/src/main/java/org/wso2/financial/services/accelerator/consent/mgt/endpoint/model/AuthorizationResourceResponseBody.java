package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@JsonTypeName("AuthResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class AuthorizationResourceResponseBody implements Serializable {
    private String authId;
    private String authorizationStatus;
    private String authorizationType;
    private String userId;
    private @Valid Object resource = new ArrayList<>();

    public AuthorizationResourceResponseBody() {
    }

    @JsonCreator
    public AuthorizationResourceResponseBody(
            @JsonProperty(required = true, value = "authId") String authId,
            @JsonProperty(required = true, value = "authorizationStatus") String authorizationStatus,
            @JsonProperty(required = true, value = "authorizationType") String authorizationType,
            @JsonProperty(required = true, value = "userId") String userId
                                            ) {
        this.authId = authId;
        this.authorizationStatus = authorizationStatus;
        this.authorizationType = authorizationType;
        this.userId = userId;
    }

    /**
     *
     **/
    public AuthorizationResourceResponseBody authId(String authId) {
        this.authId = authId;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "authId")
    @NotNull public String getAuthId() {
        return authId;
    }

    @JsonProperty(required = true, value = "authId")
    public void setAuthId(String authId) {
        this.authId = authId;
    }

    /**
     *
     **/
    public AuthorizationResourceResponseBody authorizationStatus(String authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "authorizationStatus")
    @NotNull public String getAuthorizationStatus() {
        return authorizationStatus;
    }

    @JsonProperty(required = true, value = "authorizationStatus")
    public void setAuthorizationStatus(String authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
    }

    /**
     *
     **/
    public AuthorizationResourceResponseBody authorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "authorizationType")
    @NotNull public String getAuthorizationType() {
        return authorizationType;
    }

    @JsonProperty(required = true, value = "authorizationType")
    public void setAuthorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
    }

    /**
     *
     **/
    public AuthorizationResourceResponseBody userId(String userId) {
        this.userId = userId;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "userId")
    @NotNull public String getUserId() {
        return userId;
    }

    @JsonProperty(required = true, value = "userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     *
     **/
    public AuthorizationResourceResponseBody resources(List<@Valid Object> resources) {
        this.resource = resources;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("resources")
    @Valid public Object getResource() {
        return resource;
    }

    @JsonProperty("resources")
    public void setResource(Object resource) {
        this.resource = resource;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorizationResourceResponseBody authResponse = (AuthorizationResourceResponseBody) o;
        return Objects.equals(this.authId, authResponse.authId) &&
                Objects.equals(this.authorizationStatus, authResponse.authorizationStatus) &&
                Objects.equals(this.authorizationType, authResponse.authorizationType) &&
                Objects.equals(this.userId, authResponse.userId) &&
                Objects.equals(this.resource, authResponse.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authId, authorizationStatus, authorizationType, userId, resource);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthResponse {\n");

        sb.append("    authId: ").append(toIndentedString(authId)).append("\n");
        sb.append("    authorizationStatus: ").append(toIndentedString(authorizationStatus)).append("\n");
        sb.append("    authorizationType: ").append(toIndentedString(authorizationType)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }


}

