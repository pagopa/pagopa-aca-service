# pagopa-api-config json server mock

## Overview

A simple `json-server` used to mock api config.

## Mocked services

The mocked services are:

### /creditorinstitutions/{creditorinstitutioncode}/ibans/enhanced?label={label}

The mock return a statically configured response.
The only handled `{creditorinstitutioncode}` is 77777777777 and label is ACA, all other calls with different results in
a 404 being returned

| Organization fiscal code | Iban response             |
|--------------------------|---------------------------|
| 77777777777              | 200 OK                    |
| 00000000000              | 400 BAD request           |
| 11111111111              | 401 Unauthorized          |
| 22222222222              | 403 Forbidden             |
| 33333333333              | 429 Too many requests     |
| 44444444444              | 500 Internal server error |
| any other value          | 404 Not found             |

## Notes

It uses `src/middleware.js` to transform the HTTP verb used in the request
in order to make the db immutable (read only -> `GET`).