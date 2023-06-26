# pagopa-api-config json server mock

## Overview

A simple `json-server` used to mock api config.

## Mocked services

The mocked services are:

### /creditorinstitutions/{creditorinstitutioncode}/ibans/enhanced?label={label}

The mock return responses based on input fiscal code as explained in table below:

| Organization fiscal code                  | Iban response             |
|-------------------------------------------|---------------------------|
| 77777777777                               | 200 OK                    |
| 77777777770                               | 400 BAD request           |
| 77777777771                               | 401 Unauthorized          |
| 77777777772                               | 403 Forbidden             |
| 77777777773                               | 429 Too many requests     |
| 77777777774                               | 500 Internal server error |
| 66666666666 (used for creation test flow) | 200 OK                    |

Other values than the above ones will result in a 500 internal server response error

## Notes

It uses `src/middleware.js` to transform the HTTP verb used in the request
in order to make the db immutable (read only -> `GET`).