# pagopa-gpd json server mock

## Overview

A simple `json-server` used to mock gpd service.

## Mocked services

The mocked services are:

### Retrieve debt positions

#### Path GET /organizations/{organizationfiscalcode}/debtpositions/{iupd}

Mock returns statically configured responses based on `organizationfiscalcode` and `iupd` path parameters.

| Organization fiscal code | IUPD                              | Debt position status | Response code | 
|--------------------------|-----------------------------------|----------------------|---------------|
| 77777777777              | ACA_77777777777_88888888888888880 | DRAFT                | 200           |
| 77777777777              | ACA_77777777777_88888888888888881 | PUBLISHED            | 200           |
| 77777777777              | ACA_77777777777_88888888888888882 | VALID                | 200           |
| 77777777777              | ACA_77777777777_88888888888888883 | INVALID              | 200           |
| 77777777777              | ACA_77777777777_88888888888888884 | EXPIRED              | 200           |
| 77777777777              | ACA_77777777777_88888888888888885 | PARTIALLY_PAID       | 200           |
| 77777777777              | ACA_77777777777_88888888888888886 | PAID                 | 200           |
| 77777777777              | ACA_77777777777_88888888888888887 | REPORTED             | 200           |
| 77777777777              | any other then above              | -                    | 404           |
| 77777777770              | any                               | -                    | 401           |
| 77777777771              | any                               | -                    | 500           |

Other values than the above ones will result in 500 internal server error response

### Create debt position

#### Path POST /organizations/{organizationfiscalcode}/debtpositions/{iupd}

Mock returns statically configured responses based on `organizationfiscalcode` and `iupd` path parameters.

| Organization fiscal code | IUPD | Debt position status | Response code | 
|--------------------------|------|----------------------|---------------|
| 77777777777              | any  | -                    | 201           |
| 77777777770              | any  | -                    | 400           |
| 77777777771              | any  | -                    | 401           |
| 77777777772              | any  | -                    | 409           |
| 77777777773              | any  | -                    | 500           |

Other values than the above ones will result in a 500 internal server response error

### Update debt position

#### Path PUT /organizations/{organizationfiscalcode}/debtpositions/{iupd}

Mock returns statically configured responses based on `organizationfiscalcode` and `iupd` path parameters.

| Organization fiscal code | IUPD                              | Debt position status | Response code | 
|--------------------------|-----------------------------------|----------------------|---------------|
| 77777777777              | ACA_77777777777_88888888888888880 | DRAFT                | 200           |
| 77777777777              | ACA_77777777777_88888888888888881 | PUBLISHED            | 200           |
| 77777777777              | ACA_77777777777_88888888888888882 | VALID                | 200           |
| 77777777777              | ACA_77777777777_88888888888888883 | INVALID              | 200           |
| 77777777777              | ACA_77777777777_88888888888888884 | EXPIRED              | 200           |
| 77777777777              | ACA_77777777777_88888888888888885 | PARTIALLY_PAID       | 200           |
| 77777777777              | ACA_77777777777_88888888888888886 | PAID                 | 200           |
| 77777777777              | ACA_77777777777_88888888888888887 | REPORTED             | 200           |
| 77777777777              | any other then above              | -                    | 404           |
| 77777777770              | any                               | -                    | 400           |
| 77777777771              | any                               | -                    | 401           |
| 77777777772              | any                               | -                    | 409           |
| 77777777773              | any                               | -                    | 500           |

Other values than the above ones will result in 500 internal server error response

### Invalidate debt position

#### Path POST /organizations/{organizationfiscalcode}/debtpositions/{iupd}/invalidate

The mock return responses based on input organizationfiscalcode as explained in table below:

| Organization fiscal code | IUPD | Debt position status | Response code | 
|--------------------------|------|----------------------|---------------|
| 77777777777              | any  | DRAFT                | 200           |
| 77777777770              | any  | -                    | 401           |
| 77777777771              | any  | -                    | 404           |
| 77777777772              | any  | -                    | 409           |
| 77777777773              | any  | -                    | 500           |

Other values than the above ones will result in 500 internal server error response

## Notes

It uses `src/middleware.js` to transform the HTTP verb used in the request
in order to make the db immutable (read only -> `GET`).