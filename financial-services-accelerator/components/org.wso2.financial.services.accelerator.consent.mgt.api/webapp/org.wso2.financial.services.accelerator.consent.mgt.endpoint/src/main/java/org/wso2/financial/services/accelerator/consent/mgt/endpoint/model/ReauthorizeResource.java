package org.wso2.financial.services.accelerator.consent.mgt.endpoint.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;


@JsonTypeName("ReauthorizeResource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-03-03T09:27:49.560668411+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ReauthorizeResource implements Serializable {
    private String authorizationId;
    private String userID;
    private String authorizationStatus;
    private String authorizationType;
    private @Valid List<@Valid Resource> resources = new ArrayList<>();

    public ReauthorizeResource() {
    }

    /**
     *
     **/
    public ReauthorizeResource authId(String authId) {
        this.authorizationId = authId;
        return this;
    }


    @ApiModelProperty(example = "5162ab67-b4e1-4e88-b429-f0bf34fae343", value = "")
    @JsonProperty("authId")
    public String getAuthorizationId() {
        return authorizationId;
    }

    @JsonProperty("authId")
    public void setAuthorizationId(String authId) {
        this.authorizationId = authId;
    }

    /**
     *
     **/
    public ReauthorizeResource userID(String userID) {
        this.userID = userID;
        return this;
    }


    @ApiModelProperty(example = "psu@wso2.com", value = "")
    @JsonProperty("userID")
    public String getUserId() {
        return userID;
    }

    @JsonProperty("userID")
    public void setUserId(String userID) {
        this.userID = userID;
    }

    /**
     *
     **/
    public ReauthorizeResource authorizationStatus(String authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
        return this;
    }


    @ApiModelProperty(example = "created", value = "")
    @JsonProperty("authorizationStatus")
    public String getAuthorizationStatus() {
        return authorizationStatus;
    }

    @JsonProperty("authorizationStatus")
    public void setAuthorizationStatus(String authorizationStatus) {
        this.authorizationStatus = authorizationStatus;
    }

    /**
     *
     **/
    public ReauthorizeResource authorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
        return this;
    }


    @ApiModelProperty(example = "authorization", value = "")
    @JsonProperty("authorizationType")
    public String getAuthorizationType() {
        return authorizationType;
    }

    @JsonProperty("authorizationType")
    public void setAuthorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
    }

    /**
     *
     **/
    public ReauthorizeResource resources(List<@Valid Resource> resources) {
        this.resources = resources;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("resources")
    @Valid public List<@Valid Resource> getResources() {
        return resources;
    }

    @JsonProperty("resources")
    public void setResources(List<@Valid Resource> resources) {
        this.resources = resources;
    }

    public ReauthorizeResource addResourcesItem(Resource resourcesItem) {
        if (this.resources == null) {
            this.resources = new ArrayList<>();
        }

        this.resources.add(resourcesItem);
        return this;
    }

    public ReauthorizeResource removeResourcesItem(Resource resourcesItem) {
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
        ReauthorizeResource reauthorizeResource = (ReauthorizeResource) o;
        return Objects.equals(this.authorizationId, reauthorizeResource.authorizationId) &&
                Objects.equals(this.userID, reauthorizeResource.userID) &&
                Objects.equals(this.authorizationStatus, reauthorizeResource.authorizationStatus) &&
                Objects.equals(this.authorizationType, reauthorizeResource.authorizationType) &&
                Objects.equals(this.resources, reauthorizeResource.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorizationId, userID, authorizationStatus, authorizationType, resources);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ReauthorizeResource {\n");

        sb.append("    authId: ").append(toIndentedString(authorizationId)).append("\n");
        sb.append("    userID: ").append(toIndentedString(userID)).append("\n");
        sb.append("    authorizationStatus: ").append(toIndentedString(authorizationStatus)).append("\n");
        sb.append("    authorizationType: ").append(toIndentedString(authorizationType)).append("\n");
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

