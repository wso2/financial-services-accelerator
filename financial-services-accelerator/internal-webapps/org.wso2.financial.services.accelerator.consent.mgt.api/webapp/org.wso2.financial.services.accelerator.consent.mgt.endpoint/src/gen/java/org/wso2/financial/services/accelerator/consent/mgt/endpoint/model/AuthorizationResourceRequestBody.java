package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonTypeName("AuthorizationResourceRequestBody")
public class AuthorizationResourceRequestBody implements Serializable {
    private String authorizationStatus;
    private String authorizationType;
    private String userId;
    private Object resource;

    public AuthorizationResourceRequestBody() {
    }

    @JsonCreator
    public AuthorizationResourceRequestBody(
            @JsonProperty(required = true, value = "authorizationStatus") String authorizationStatus,
            @JsonProperty(required = true, value = "authorizationType") String authorizationType,
            @JsonProperty(required = true, value = "userId") String userId
                                           ) {
        this.authorizationStatus = authorizationStatus;
        this.authorizationType = authorizationType;
        this.userId = userId;
    }

    /**
     *
     **/
    public AuthorizationResourceRequestBody authorizationStatus(String authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "authorizationStatus")
    @NotNull @NotEmpty public String getAuthorizationStatus() {
        return authorizationStatus;
    }

    @JsonProperty(required = true, value = "authorizationStatus")
    public void setAuthorizationStatus(String authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
    }

    /**
     *
     **/
    public AuthorizationResourceRequestBody authorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "authorizationType")
    @NotNull @NotEmpty public String getAuthorizationType() {
        return authorizationType;
    }

    @JsonProperty(required = true, value = "authorizationType")
    public void setAuthorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
    }

    /**
     *
     **/
    public AuthorizationResourceRequestBody userId(String userId) {
        this.userId = userId;
        return this;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "userId")
    @NotNull @NotEmpty public String getUserId() {
        return userId;
    }

    @JsonProperty(required = true, value = "userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     *
     **/
    public AuthorizationResourceRequestBody resource(Object resource) {
        this.resource = resource;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("resource")
    public Object getResource() {
        return resource;
    }

    @JsonProperty("resource")
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
        AuthorizationResourceRequestBody authorizationResourceRequestBody = (AuthorizationResourceRequestBody) o;
        return Objects.equals(this.authorizationStatus, authorizationResourceRequestBody.authorizationStatus) &&
                Objects.equals(this.authorizationType, authorizationResourceRequestBody.authorizationType) &&
                Objects.equals(this.userId, authorizationResourceRequestBody.userId) &&
                Objects.equals(this.resource, authorizationResourceRequestBody.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorizationStatus, authorizationType, userId, resource);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorizationResourceRequestBody {\n");

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

