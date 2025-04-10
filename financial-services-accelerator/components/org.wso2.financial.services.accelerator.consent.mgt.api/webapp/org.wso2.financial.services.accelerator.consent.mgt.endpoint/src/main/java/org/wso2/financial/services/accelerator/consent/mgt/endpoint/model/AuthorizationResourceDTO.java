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


@JsonTypeName("AuthResource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-03-03T09:27:49.560668411+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class AuthorizationResourceDTO implements Serializable {
    private String authorizationStatus;
    private String authorizationType;
    private String userID;
    private @Valid List<Object> resources = new ArrayList<>();

    public AuthorizationResourceDTO() {
    }

    @JsonCreator
    public AuthorizationResourceDTO(
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
    public AuthorizationResourceDTO authorizationStatus(String authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
        return this;
    }


    @ApiModelProperty(example = "created", required = true, value = "")
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
    public AuthorizationResourceDTO authorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
        return this;
    }


    @ApiModelProperty(example = "authorization", required = true, value = "")
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
    public AuthorizationResourceDTO userID(String userID) {
        this.userID = userID;
        return this;
    }


    @ApiModelProperty(example = "psu@wso2.com", required = true, value = "")
    @JsonProperty(required = true, value = "userID")
    @NotNull public String getUserId() {
        return userID;
    }

    @JsonProperty(required = true, value = "userID")
    public void setUserId(String userID) {
        this.userID = userID;
    }

    /**
     *
     **/
    public AuthorizationResourceDTO resources(List<Object> resources) {
        this.resources = resources;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("resources")
    @Valid public List<Object> getResources() {
        return resources;
    }

    @JsonProperty("resources")
    public void setResources(List<Object> resources) {
        this.resources = resources;
    }

    public AuthorizationResourceDTO addResourcesItem(String resourcesItem) {
        if (this.resources == null) {
            this.resources = new ArrayList<>();
        }

        this.resources.add(resourcesItem);
        return this;
    }

    public AuthorizationResourceDTO removeResourcesItem(ResourcePermission resourcesItem) {
        if (resourcesItem != null && this.resources != null) {
            this.resources.remove(resourcesItem);
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorizationResourceDTO authResource = (AuthorizationResourceDTO) o;
        return Objects.equals(this.authorizationStatus, authResource.authorizationStatus) &&
                Objects.equals(this.authorizationType, authResource.authorizationType) &&
                Objects.equals(this.userID, authResource.userID) &&
                Objects.equals(this.resources, authResource.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorizationStatus, authorizationType, userID, resources);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorizationResourceDTO {\n");

        sb.append("    authorizationStatus: ").append(toIndentedString(authorizationStatus)).append("\n");
        sb.append("    authorizationType: ").append(toIndentedString(authorizationType)).append("\n");
        sb.append("    userID: ").append(toIndentedString(userID)).append("\n");
        sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
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

