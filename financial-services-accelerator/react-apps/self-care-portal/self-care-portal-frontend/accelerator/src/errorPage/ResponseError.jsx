/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from "react";
import {decode as base64_decode } from 'base-64';
import wso2Logo from "../images/wso2Logo.png";

export const ResponseError = (error = {}) => {
  let message = error.message;
  let description = error.description;

  if (!description) {
    // Reads the URL and retrieves the error params.
    const url = new URL(window.location.href);

    message = url.searchParams.get("message");
    description = url.searchParams.get("description");

    if (message && description) {
      message = base64_decode(message);
      description = base64_decode(description);
    } else {
      message = "Redirecting Failed!";
      description =
        "Something went wrong during the authentication process. Please try signing in again.";
    }
  }

  return (
    <div className="container">
      <div className="row justify-content-md-center top-auto">
        <div className="col col-md-6">
          <img
            className="mx-auto d-block navLogoImage"
            alt="WSO2 logo"
            src={wso2Logo}
          />
          <div className="border p-5">
            <div className="alert alert-danger m-0" role="alert">
              <h4 className="alert-heading">{message}</h4>
              <p>{description}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
