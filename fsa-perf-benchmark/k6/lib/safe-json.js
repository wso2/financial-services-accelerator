// safe-json.js
// A failed/refused request (connection error, timeout, TLS failure) comes
// back from k6 as a response with status 0 and a null body. Calling
// res.json() on that throws a hard GoError that kills the iteration instead
// of just failing a check - which turns one network hiccup into a crashed
// VU and a confusing stack trace instead of a normal failed-request metric.
//
// Use this wherever a field needs to be pulled out of a response body.
export function safeJson(res, jsonPath) {
  if (!res || res.status === 0 || res.body == null) return null;
  try {
    return jsonPath !== undefined ? res.json(jsonPath) : res.json();
  } catch (e) {
    return null;
  }
}
