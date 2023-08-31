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
import { FormattedMessage } from "react-intl";

import { CONFIG } from "../../config";

/**
 * Renders the pagination UI.
 */
const Pagination = ({
  page,
  limit,
  updateLimit,
  goToFirstPage,
  goToPreviousPage,
  goToNextPage,
  goToLastPage,
  canShowPreviousPage,
  canShowNextPage,
}) => {
  return (
    <>
      <div>
        <FormattedMessage
          id="app.consent-page.pagination.rows_per_page"
          defaultMessage="Rows per page: "
        />
        {/* todo: get the below dropdown from common ui library*/}
        <select
          value={limit}
          onChange={(e) => updateLimit(Number(e.target.value))}
        >
          {CONFIG.NUMBER_OF_CONSENTS_PER_PAGE.map((limit) => (
            <option key={limit} value={limit}>
              {limit}
            </option>
          ))}
        </select>
      </div>
      <div>
        <button onClick={() => goToFirstPage()} disabled={!canShowPreviousPage}>
          <FormattedMessage
            id="app.consent-page.pagination.first"
            defaultMessage="<<"
          />
        </button>
        <button
          onClick={() => goToPreviousPage()}
          disabled={!canShowPreviousPage}
        >
          <FormattedMessage
            id="app.consent-page.pagination.previous"
            defaultMessage="<"
          />
        </button>
        <span>{page}</span>
        <button onClick={() => goToNextPage()} disabled={!canShowNextPage}>
          <FormattedMessage
            id="app.consent-page.pagination.next"
            defaultMessage=">"
          />
        </button>
        <button onClick={() => goToLastPage()} disabled={!canShowNextPage}>
          <FormattedMessage
            id="app.consent-page.pagination.last"
            defaultMessage=">>"
          />
        </button>
      </div>
    </>
  );
};

export default Pagination;
