# PagoPA ACA Service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-aca-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=pagopa_pagopa-aca-service)

This microservice is responsible for keeping ACAs inside the PagoPA platform.

- [PagoPA ACA Service](#pagopa-aca-service)
    * [Api Documentation 📖](#api-documentation-)
    * [Technology Stack](#technology-stack)
    * [Environment Variables 🛠️](#environment-variables-)
    * [Start Project Locally 🚀](#start-project-locally-)
        + [Prerequisites](#prerequisites)
        + [Run docker container](#run-docker-container)
    * [Develop Locally 💻](#develop-locally-)
        + [Prerequisites](#prerequisites-1)
        + [Run the project](#run-the-project)
        + [Testing 🧪](#testing-)
            - [Unit testing](#unit-testing)
            - [Integration testing](#integration-testing)
            - [Local test with mocks](#local-test-with-mock)
            - [Performance testing](#performance-testing)
    * [Dependency management 🔧](#dependency-management-)
        + [Dependency lock](#dependency-lock)
        + [Dependency verification](#dependency-verification)
    * [Contributors 👥](#contributors-)
        + [Maintainers](#maintainers)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with
markdown-toc</a></i></small>

---

## Api Documentation 📖

See
the [OpenAPI 3 here.](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-aca-service/main/aca-openapi.yaml)

---

## Technology Stack

- Kotlin
- Spring Boot

---

## Environment variables 🛠️

There are all environment variables required by the application:

| Variable name                         | Description                                | Type    | Default Value |
|---------------------------------------|--------------------------------------------|---------|---------------|
| DEFAULT_LOGGING_LEVEL                 | Root logger logging level                  | String  | INFO          |
| APP_LOGGING_LEVEL                     | Application logging level                  | String  | INFO          |
| WEB_LOGGING_LEVEL                     | Web logging level                          | String  | INFO          |
| API_CONFIG_IBANS_URI                  | ApiConfig "Ibans" URI base path            | String  |               |
| API_CONFIG_IBANS_READ_TIMEOUT         | ApiConfig "Ibans" HTTP read timeout        | Integer |               |
| API_CONFIG_IBANS_CONNECTION_TIMEOUT   | ApiConfig "Ibans" HTTP connection timeout  | Integer |               |
| API_CONFIG_IBANS_API_KEY              | ApiConfig "Ibans" API key                  | String  |               |
| GPD_DEBIT_POSITION_URI                | GPD Debit Position URI base path           | String  |               | 
| GPD_DEBIT_POSITION_READ_TIMEOUT       | GPD Debit Position HTTP read timeout       | Integer |               |
| GPD_DEBIT_POSITION_CONNECTION_TIMEOUT | GPD Debit Position HTTP connection timeout | Integer |               |
| GPD_DEBIT_POSITION_API_KEY            | GPD Debit Position API key                 | String  |               |

---

## Start Project Locally 🚀

### Prerequisites

- docker

### Populate the environment

The microservice needs a valid `.env` file in order to be run.

If you want to start the application without too much hassle, you can just copy `.env.example` with

```shell
$ cp .env.example .env
```

to get a good default configuration.

If you want to customize the application environment, reference [this table](#environment-variables-)

### Run docker container

```shell
$ docker compose up --build
```

---

## Develop Locally 💻

### Prerequisites

- git
- gradle
- jdk-17

### Run the project

```shell
$ ./gradlew bootRun
```

### Testing 🧪

#### Unit testing

To run the **Junit** tests:

```shell
$ ./gradlew test
```

#### Integration testing

TODO

#### Local test with mock

Project docker compose starts two json-server mocks for external services:

- [api-config](./mock/json-server/api-config/README.md)
- [gpd](./mock/json-server/gpd/README.md)

Those are the downstream dependency of ACA service.
Refers to their README files (links above) for how to mock any particular response starting from request parameters.

#### Performance testing

install [k6](https://k6.io/) and then from `./performance-test/src`

1. `k6 run --env VARS=local.environment.json --env TEST_TYPE=./test-types/load.json main_scenario.js`

### Dependency management 🔧

For support reproducible build this project has the following gradle feature enabled:

- [dependency lock](https://docs.gradle.org/8.1/userguide/dependency_locking.html)
- [dependency verification](https://docs.gradle.org/8.1/userguide/dependency_verification.html)

#### Dependency lock

This feature use the content of `gradle.lockfile` to check the declared dependencies against the locked one.

If a transitive dependencies have been upgraded the build will fail because of the locked version mismatch.

The following command can be used to upgrade dependency lockfile:

```shell
./gradlew dependencies --write-locks 
```

Running the above command will cause the `gradle.lockfile` to be updated against the current project dependency
configuration

#### Dependency verification

This feature is enabled by adding the gradle `./gradle/verification-metadata.xml` configuration file.

Perform checksum comparison against dependency artifact (jar files, zip, ...) and metadata (pom.xml, gradle module
metadata, ...) used during build
and the ones stored into `verification-metadata.xml` file raising error during build in case of mismatch.

The following command can be used to recalculate dependency checksum:

```shell
./gradlew --write-verification-metadata sha256 clean spotlessApply build 
```

In the above command the `clean`, `spotlessApply` `build` tasks where chosen to be run
in order to discover all transitive dependencies used during build and also the ones used during
spotless apply task used to format source code.

The above command will upgrade the `verification-metadata.xml` adding all the newly discovered dependencies' checksum.
Those checksum should be checked against a trusted source to check for corrispondence with the library author published
checksum.

`/gradlew --write-verification-metadata sha256` command appends all new dependencies to the verification files but does
not remove
entries for unused dependencies.

This can make this file grow every time a dependency is upgraded.

To detect and remove old dependencies make the following steps:

1. Delete, if present, the `gradle/verification-metadata.dryrun.xml`
2. Run the gradle write-verification-metadata in dry-mode (this will generate a verification-metadata-dryrun.xml file
   leaving untouched the original verification file)
3. Compare the verification-metadata file and the verification-metadata.dryrun one checking for differences and removing
   old unused dependencies

The 1-2 steps can be performed with the following commands

```Shell
rm -f ./gradle/verification-metadata.dryrun.xml 
./gradlew --write-verification-metadata sha256 clean spotlessApply build --dry-run
```

The resulting `verification-metadata.xml` modifications must be reviewed carefully checking the generated
dependencies checksum against official websites or other secure sources.

If a dependency is not discovered during the above command execution it will lead to build errors.

You can add those dependencies manually by modifying the `verification-metadata.xml`
file adding the following component:

```xml

<verification-metadata>
    <!-- other configurations... -->
    <components>
        <!-- other components -->
        <component group="GROUP_ID" name="ARTIFACT_ID" version="VERSION">
            <artifact name="artifact-full-name.jar">
                <sha256 value="sha value"
                        origin="Description of the source of the checksum value"/>
            </artifact>
            <artifact name="artifact-pom-file.pom">
                <sha256 value="sha value"
                        origin="Description of the source of the checksum value"/>
            </artifact>
        </component>
    </components>
</verification-metadata>
```

Add those components at the end of the components list and then run the

```shell
./gradlew --write-verification-metadata sha256 clean spotlessApply build 
```

that will reorder the file with the added dependencies checksum in the expected order.

Finally, you can add new dependencies both to gradle.lockfile writing verification metadata running

```shell
 ./gradlew dependencies --write-locks --write-verification-metadata sha256
```

For more information read the
following [article](https://docs.gradle.org/8.1/userguide/dependency_verification.html#sec:checksum-verification)

## Contributors 👥

Made with ❤️ by PagoPA S.p.A.

### Maintainers

See `CODEOWNERS` file
