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

import React, { useEffect, useState } from "react";

/**
 * Handles pagination functionality.
 */
const usePagination = (total, limit, offset, setOffset) => {
  const [canShowPreviousPage, setCanShowPreviousPage] = useState(false);
  const [canShowNextPage, setCanShowNextPage] = useState(false);

  useEffect(() => {
    hasPreviousPage();
    hasNextPage();
  });

  // Method to navigate to the previous page.
  const goToPreviousPage = () => {
    // Checks if there is a previous page and new offset is not negative.
    if (canShowPreviousPage && offset - limit >= 0) {
      setOffset(offset - limit);
    }
  };

  // Method to check if there is a previous page.
  const hasPreviousPage = () => {
    // If the offset is 0, there is no previous page
    if (offset === 0) {
      setCanShowPreviousPage(false);
    } else {
      setCanShowPreviousPage(true);
    }
  };

  // Method to navigate to the first page.
  const goToFirstPage = () => {
    setOffset(0);
  };

  // Method to navigate to the next page.
  const goToNextPage = () => {
    // Check if there is a next page
    if (canShowNextPage) {
      setOffset(offset + limit);
    }
  };

  // Method to check if there is a next page.
  const hasNextPage = () => {
    // Check if the current offset plus the limit is within the range of available consents
    setCanShowNextPage(offset + limit < total);
  };

  // Method to navigate to the last page.
  const goToLastPage = () => {
    // Calculate the maximum offset that can be reached with the given limit
    setOffset(Math.floor((total - 1) / limit) * limit);
  };

  // Method to generate the current page number from limit and offset.
  const getCurrentPage = () => {
    if (limit <= 0) return 1;
    return offset / limit + 1;
  };

  return {
    page: getCurrentPage(),
    goToFirstPage,
    goToPreviousPage,
    goToNextPage,
    goToLastPage,
    canShowPreviousPage,
    canShowNextPage,
  };
};

export default usePagination;
