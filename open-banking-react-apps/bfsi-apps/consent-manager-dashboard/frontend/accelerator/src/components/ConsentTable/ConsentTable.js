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

import React, { useContext, useMemo, useRef, useState, useEffect } from "react";
import "./ConsentTable.scss";

// Libraries & Packages
import GlobalStateContext from "../../utils/state/GlobalStateContext";
import {
  useTable,
  useGlobalFilter,
  useFilters,
  usePagination,
} from "react-table";
import {
  Button,
  Card,
  Input,
  Title,
  Text,
  ThemeContext,
  CheckboxGroup,
  Checkbox,
} from "@bfsi-react/bfsi-ui";
import Color from "color";

// Icons
import SearchOffRoundedIcon from "@mui/icons-material/SearchOffRounded";
import KeyboardArrowLeftRoundedIcon from "@mui/icons-material/KeyboardArrowLeftRounded";
import KeyboardDoubleArrowLeftRoundedIcon from "@mui/icons-material/KeyboardDoubleArrowLeftRounded";
import KeyboardArrowRightRoundedIcon from "@mui/icons-material/KeyboardArrowRightRounded";
import KeyboardDoubleArrowRightRoundedIcon from "@mui/icons-material/KeyboardDoubleArrowRightRounded";
import PageDetails from "../../components/PageDetails/PageDetails";
import TuneRoundedIcon from "@mui/icons-material/TuneRounded";

const ConsentTable = ({ data: propData, columns: propColumns }) => {
  // State
  const { globalState } = useContext(GlobalStateContext);
  const { theme } = useContext(ThemeContext);

  const [filteredData, setFilteredData] = useState(propData);

  const [hoveredRow, setHoveredRow] = useState(null);
  const [displayMenu, setDisplayMenu] = useState(false);

  const [consentStatus, setconsentStatus] = useState(
    [...new Set(propData.map((item) => item.status))].sort().map((item) => {
      return { value: item, checked: true };
    })
  );

  // Memo
  const columns = useMemo(() => propColumns, []);

  // Effect
  useEffect(() => {
    const filteredItems = propData.filter((item) => {
      const correspondingConsentStatus = consentStatus.find(
        (status) => status.value === item.status
      );
      return correspondingConsentStatus && correspondingConsentStatus.checked;
    });
    setFilteredData(filteredItems);
  }, [consentStatus, propData]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        filterMenuRef.current &&
        !filterMenuRef.current.contains(event.target) &&
        event.target !== filterButtonRef.current.querySelector("button")
      ) {
        setDisplayMenu(false);
      }
    };

    document.addEventListener("click", handleClickOutside);

    return () => {
      document.removeEventListener("click", handleClickOutside);
    };
  }, []);

  // Ref
  const filterMenuRef = useRef(null);
  const filterButtonRef = useRef(null);

  const {
    getTableProps,
    getTableBodyProps,
    headerGroups,
    setFilter,
    page,
    state: { pageIndex, globalFilter },
    setGlobalFilter,
    prepareRow,

    // The rest of these things are super handy, too ;)
    canPreviousPage,
    canNextPage,
    pageOptions,
    pageCount,
    gotoPage,
    nextPage,
    previousPage,
    setPageSize,
  } = useTable(
    {
      columns,
      data: filteredData,
      initialState: { pageIndex: 0 },
    },
    useGlobalFilter,
    useFilters,
    usePagination
  );

  return (
    <div className="consentTable">
      {/* Filters */}
      <div className="consentTable__filters">
        <div className="consentTable__filters__container">
          {/* Page Details */}
          <div className="consentTable__filters__pageDetails">
            <PageDetails globalState={globalState} theme={theme} />
          </div>
          {/* Search */}
          <div className="consentTable__filters__search">
            <Input
              placeholder="Search"
              width="250px"
              value={globalFilter || ""}
              onChange={(e) => {
                setGlobalFilter(e.target.value);
              }}
            />
          </div>
          {/* Filter Menu */}
          <div
            className="consentTable__filters__menu__button"
            ref={filterButtonRef}
          >
            <div className="consentTable__filters__menu__button__container">
              <Button
                type="secondary"
                variant="light"
                onClick={() => setDisplayMenu(!displayMenu)}
                leftIcon={
                  <TuneRoundedIcon
                    style={{
                      fontSize: "14px",
                      color: theme.palette.miscellaneous.grey.main,
                    }}
                  />
                }
              >
                Filter
              </Button>
            </div>
          </div>
        </div>
        <div
          className="consentTable__filters__menu"
          style={{ display: displayMenu ? "flex" : "none" }}
          ref={filterMenuRef}
          onClick={(event) => {
            event.stopPropagation();
          }}
        >
          <Card width="300px" height="auto">
            {/* Status */}
            <div className="consentTable__filters__menu__status">
              <Title text="Status" />
              <CheckboxGroup>
                {consentStatus.map((item, index) => {
                  return (
                    <Checkbox
                      key={index}
                      checked={item.checked}
                      value={item.value}
                      onChange={() => {
                        setconsentStatus((prevState) =>
                          prevState.map((item, i) =>
                            i === index
                              ? { ...item, checked: !item.checked }
                              : item
                          )
                        );
                      }}
                    >
                      <Text textTransform="capitalize">{item.value}</Text>
                    </Checkbox>
                  );
                })}
              </CheckboxGroup>
            </div>
            {/* Search */}
            <div className="consentTable__filters__menu__search">
              <Title text="Search" />
              <Input
                placeholder="Search"
                width="100%"
                value={globalFilter || ""}
                onChange={(e) => {
                  setGlobalFilter(e.target.value);
                }}
              />
            </div>
          </Card>
        </div>
      </div>
      {/* Table */}
      <div className="consentTable__table">
        <table {...getTableProps()}>
          <thead>
            {headerGroups.map((headerGroup) => (
              <tr
                style={{ backgroundColor: theme.palette.primary.light }}
                {...headerGroup.getHeaderGroupProps()}
              >
                {headerGroup.headers.map((column) => (
                  <th
                    style={{ color: theme.palette.primary.main }}
                    {...column.getHeaderProps()}
                  >
                    {column.render("Header")}
                  </th>
                ))}
              </tr>
            ))}
          </thead>
          <tbody {...getTableBodyProps()}>
            {page.length > 0 ? (
              page.map((row, i) => {
                prepareRow(row);
                return (
                  <tr
                    onMouseEnter={() => setHoveredRow(i)}
                    onMouseLeave={() => setHoveredRow(null)}
                    style={
                      hoveredRow === i
                        ? {
                            backgroundColor: Color(
                              theme.palette.primary.light
                            ).alpha(0.5),
                            transition: "background-color 0.5s ease-in-out",
                          }
                        : {
                            backgroundColor: theme.palette.common.background,
                            transition: "background-color 0.5s ease-in-out",
                          }
                    }
                    {...row.getRowProps()}
                  >
                    {row.cells.map((cell) => {
                      return (
                        <td
                          style={{
                            borderBottom: `1px solid ${Color(
                              theme.palette.miscellaneous.grey.main
                            ).alpha(0.1)}`,
                          }}
                          {...cell.getCellProps()}
                        >
                          {cell.render("Cell", {
                            color: theme.palette.miscellaneous.grey.main,
                          })}
                        </td>
                      );
                    })}
                  </tr>
                );
              })
            ) : (
              <tr>
                <td colSpan={columns.length} height="400px">
                  <div
                    className="consentTable__table__fallback"
                    style={{
                      borderColor: theme.palette.miscellaneous.grey.light,
                    }}
                  >
                    <div className="consentTable__table__fallback__icon">
                      <SearchOffRoundedIcon
                        style={{
                          color: theme.palette.miscellaneous.grey.light,
                          fontSize: "64px",
                        }}
                      />
                    </div>
                    <div className="consentTable__table__fallback__text">
                      <Text color={theme.palette.miscellaneous.grey.light}>
                        No Result Found!
                      </Text>
                    </div>
                  </div>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {/* Pagination */}
      <div className="consentTable__pagination">
        {/*  Backward Double Arrow - << */}
        <div
          className="consentTable__pagination__icon"
          onClick={() => gotoPage(0)}
        >
          <KeyboardDoubleArrowLeftRoundedIcon
            style={{
              color: canPreviousPage
                ? theme.palette.miscellaneous.grey.main
                : theme.palette.miscellaneous.grey.light,
            }}
          />
        </div>
        {/*  Backward Single Arrow - < */}
        <div
          className="consentTable__pagination__icon"
          onClick={() => previousPage()}
        >
          <KeyboardArrowLeftRoundedIcon
            style={{
              color: canPreviousPage
                ? theme.palette.miscellaneous.grey.main
                : theme.palette.miscellaneous.grey.light,
            }}
          />
        </div>
        {/* Page */}
        <div className="consentTable__pagination__icon">
          <Text align="center">{pageIndex + 1}</Text>
        </div>
        {/*  Forward Single Arrow - > */}
        <div
          className="consentTable__pagination__icon"
          onClick={() => nextPage()}
        >
          <KeyboardArrowRightRoundedIcon
            style={{
              color: canNextPage
                ? theme.palette.miscellaneous.grey.main
                : theme.palette.miscellaneous.grey.light,
            }}
          />
        </div>
        {/* Forward Double Arrow - >> */}
        <div
          className="consentTable__pagination__icon"
          onClick={() => gotoPage(pageCount - 1)}
        >
          <KeyboardDoubleArrowRightRoundedIcon
            style={{
              color: canNextPage
                ? theme.palette.miscellaneous.grey.main
                : theme.palette.miscellaneous.grey.light,
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default ConsentTable;
