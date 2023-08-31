/* ----- ConsentStatusUtil.js ----- */

export const getConsentStatus = (status) => {
  const statusLookupTable = {
    active: "success",
    expired: "warning",
    revoked: "error",
    authorized: "success",
    consumed: "warning",
  };

  return statusLookupTable[status];
};
