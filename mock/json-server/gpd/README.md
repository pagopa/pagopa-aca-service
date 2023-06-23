# pagopa-gpd json server mock

## Overview

A simple `json-server` used to mock gpd service.

## Mocked services

The mocked services are:

### Retrieve debt positions

#### Path /organizations/{organizationfiscalcode}/debtpositions/{iupd}

Mock returns statically configured responses based on `organizationfiscalcode` and `iupd` path parameters.

| Organization fiscal code | IUPD                              | Debt position status |
|--------------------------|-----------------------------------|----------------------|
| 77777777777              | ACA_77777777777_88888888888888880 | DRAFT                |
| 77777777777              | ACA_77777777777_88888888888888881 | PUBLISHED            |
| 77777777777              | ACA_77777777777_88888888888888882 | VALID                |
| 77777777777              | ACA_77777777777_88888888888888883 | INVALID              |
| 77777777777              | ACA_77777777777_88888888888888884 | EXPIRED              |
| 77777777777              | ACA_77777777777_88888888888888885 | PARTIALLY_PAID       |
| 77777777777              | ACA_77777777777_88888888888888886 | PAID                 |
| 77777777777              | ACA_77777777777_88888888888888887 | REPORTED             |

Other values than the above ones will result in a 404 NOT FOUND response

### Invalidate debt position

#### Path /organizations/{organizationfiscalcode}/debtpositions/{iupd}/invalidate

Mock return statically configured response that does not depend on input `organizationfiscalcode` and `iupd`

### Update debt position

#### Path /organizations/{organizationfiscalcode}/debtpositions/{iupd}

Mock returns statically configured responses based on `organizationfiscalcode` and `iupd` path parameters.

See [Retrieve debt position](#Retrieve-debt-positions)

## Notes

It uses `src/middleware.js` to transform the HTTP verb used in the request
in order to make the db immutable (read only -> `GET`).