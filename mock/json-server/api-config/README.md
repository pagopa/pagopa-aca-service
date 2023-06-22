# pagopa-personal-data-vault-tokenizer

## Overview

A simple `json-server` used to mock api config.

## Mocked services

The mocked services are:

### /creditorinstitutions/{creditorinstitutioncode}/ibans/enhanced?label={label}

The mock return a statically configured response.
The only handled `{creditorinstitutioncode}` is 77777777777 and label is ACA, all other calls with different results in
a 404 being returned

## Notes

It uses `src/middleware.js` to transform the HTTP verb used in the request
in order to make the db immutable (read only -> `GET`).