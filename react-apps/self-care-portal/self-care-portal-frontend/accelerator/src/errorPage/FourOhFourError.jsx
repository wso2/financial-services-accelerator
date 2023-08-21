/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import Container from "react-bootstrap/Col";

export const FourOhFourError = () => {
  return (
    <Container
      className="fourOhFour"
      style={{ padding: "1rem 1rem", height: "84.5vh" }}
    >
      <h5 style={{ margin: "1rem 1rem", color: "rgba(8, 18, 71, 1)" }}>
        Error 404 : Page not found
      </h5>
    </Container>
  );
};
