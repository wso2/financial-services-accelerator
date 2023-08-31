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

import React, { useState } from "react";

/**
 * This is the template for a filter for a specific column.
 */
export const Filter = ({ column, placeholder, setFilter }) => {
  const [filterInput, setFilterInput] = useState("");

  const handleFilterChange = (e) => {
    const value = e.target.value ?? "";
    setFilter(column, value);
    setFilterInput(value);
  };

  return (
    <input
      value={filterInput}
      onChange={handleFilterChange}
      placeholder={placeholder}
      className={"search"}
    />
  );
};

/**
 * This component contains all the filters applied on the table.
 */
export const AdvancedSearch = ({ setFilter }) => {
  return (
    <>
      <Filter
        column={"softwareId"}
        placeholder={"Service Provider"}
        setFilter={setFilter}
      />
      <Filter
        column={"createdTimestamp"}
        placeholder={"Consented Date"}
        setFilter={setFilter}
      />
      <Filter
        column={"currentStatus"}
        placeholder={"Status"}
        setFilter={setFilter}
      />
    </>
  );
};

/**
 * A global search bar
 */
export const GlobalFilter = ({ filter, setFilter }) => {
  return (
    <input
      value={filter || ""}
      placeholder="Global Search"
      onChange={(e) => {
        setFilter(e.target.value);
      }}
    />
  );
};
