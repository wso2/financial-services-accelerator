package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@JsonTypeName("AuthorizationResourceDTO")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-04-18T10:47:38.068853078+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class CreateAuthorizationResourceRequestBody implements Serializable {
    private String authorizationStatus;
    private String authorizationType;
    private String userID;
    private @Valid Object resource = new ArrayList<>();

    public CreateAuthorizationResourceRequestBody() {
    }

    @JsonCreator
    public CreateAuthorizationResourceRequestBody(
            @JsonProperty(required = true, value = "authorizationStatus") String authorizationStatus,
            @JsonProperty(required = true, value = "authorizationType") String authorizationType,
            @JsonProperty(required = true, value = "userID") String userID
                                                 ) {
        this.authorizationStatus = authorizationStatus;
        this.authorizationType = authorizationType;
        this.userID = userID;
    }

    /**
     *
     **/
    public CreateAuthorizationResourceRequestBody authorizationStatus(String authorizationStatus) {
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
    public CreateAuthorizationResourceRequestBody authorizationType(String authorizationType) {
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
    public CreateAuthorizationResourceRequestBody userID(String userID) {
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
    public CreateAuthorizationResourceRequestBody resources(Object resource) {
        this.resource = resource;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("resources")
    public Object getResource() {
        return resource;
    }

    @JsonProperty("resources")
    public void setResource(Object resources) {
        this.resource = resources;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateAuthorizationResourceRequestBody authorizationResourceDTO = (CreateAuthorizationResourceRequestBody) o;
        return Objects.equals(this.authorizationStatus, authorizationResourceDTO.authorizationStatus) &&
                Objects.equals(this.authorizationType, authorizationResourceDTO.authorizationType) &&
                Objects.equals(this.userID, authorizationResourceDTO.userID) &&
                Objects.equals(this.resource, authorizationResourceDTO.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorizationStatus, authorizationType, userID, resource);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorizationResourceDTO {\n");

        sb.append("    authorizationStatus: ").append(toIndentedString(authorizationStatus)).append("\n");
        sb.append("    authorizationType: ").append(toIndentedString(authorizationType)).append("\n");
        sb.append("    userID: ").append(toIndentedString(userID)).append("\n");
        sb.append("    resources: ").append(toIndentedString(resource)).append("\n");
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

