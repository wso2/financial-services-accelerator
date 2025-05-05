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
public class AuthorizationResourceResponse implements Serializable {
    private String authId;
    private String authorizationStatus;
    private String authorizationType;
    private String userID;
    private @Valid Object resource = new ArrayList<>();

    public AuthorizationResourceResponse() {
    }

    @JsonCreator
    public AuthorizationResourceResponse(
            @JsonProperty(required = true, value = "authId") String authId,
            @JsonProperty(required = true, value = "authorizationStatus") String authorizationStatus,
            @JsonProperty(required = true, value = "authorizationType") String authorizationType,
            @JsonProperty(required = true, value = "userID") String userID
                                        ) {
        this.authId = authId;
        this.authorizationStatus = authorizationStatus;
        this.authorizationType = authorizationType;
        this.userID = userID;
    }

    /**
     *
     **/
    public AuthorizationResourceResponse authId(String authId) {
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
    public AuthorizationResourceResponse authorizationStatus(String authorizationStatus) {
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
    public AuthorizationResourceResponse authorizationType(String authorizationType) {
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
    public AuthorizationResourceResponse userID(String userID) {
        this.userID = userID;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty(required = true, value = "userID")
    @NotNull public String getUserID() {
        return userID;
    }

    @JsonProperty(required = true, value = "userID")
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     *
     **/
    public AuthorizationResourceResponse resources(List<@Valid Object> resources) {
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
        AuthorizationResourceResponse authResponse = (AuthorizationResourceResponse) o;
        return Objects.equals(this.authId, authResponse.authId) &&
                Objects.equals(this.authorizationStatus, authResponse.authorizationStatus) &&
                Objects.equals(this.authorizationType, authResponse.authorizationType) &&
                Objects.equals(this.userID, authResponse.userID) &&
                Objects.equals(this.resource, authResponse.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authId, authorizationStatus, authorizationType, userID, resource);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthResponse {\n");

        sb.append("    authId: ").append(toIndentedString(authId)).append("\n");
        sb.append("    authorizationStatus: ").append(toIndentedString(authorizationStatus)).append("\n");
        sb.append("    authorizationType: ").append(toIndentedString(authorizationType)).append("\n");
        sb.append("    userID: ").append(toIndentedString(userID)).append("\n");
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

