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

import React, { useMemo } from "react";

import Pagination from "../components/consents/consent-pagination.jsx";
import ConsentTable from "../components/consents/consent-table.jsx";
import Header from "../components/header.jsx";

import usePagination from "../hook/use-pagination.jsx";
import useConsents from "../hook/use-consents.jsx";

import { columns } from "../configs/consent-table-configs.js";

/**
 * The container component that handles fetching of consent data, renders consent table and renders pagination
 */
const ConsentPage = ({ activeLocale, changeLocale, supportedLocales }) => {
  const {
    limit,
    offset,
    setLimit,
    setOffset,
    getConsents,
    metadata,
    isLoading,
    isError,
    error,
  } = useConsents("accounts");

  const {
    page,
    goToFirstPage,
    goToPreviousPage,
    goToNextPage,
    goToLastPage,
    canShowPreviousPage,
    canShowNextPage,
  } = usePagination(metadata?.total, limit, offset, setOffset);

  const consentTableColumns = useMemo(() => columns, []);

  if (!isLoading) {
    return (
      <>
        <Header
          value={activeLocale}
          onChange={changeLocale}
          options={supportedLocales}
        />
        <ConsentTable columns={consentTableColumns} data={getConsents()} />
        <Pagination
          page={page}
          limit={limit}
          updateLimit={setLimit}
          canShowNextPage={canShowNextPage}
          canShowPreviousPage={canShowPreviousPage}
          goToFirstPage={goToFirstPage}
          goToLastPage={goToLastPage}
          goToNextPage={goToNextPage}
          goToPreviousPage={goToPreviousPage}
        />
      </>
    );
  } else if (isError) {
    // todo: need to properly handle this error
    return <h3>{error.message}</h3>;
  } else {
    return <h3>Loading...</h3>;
  }
};

export default ConsentPage;
