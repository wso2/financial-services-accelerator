# FS Integration Test Suite – Configuration & Execution Guide

---

## Overview

The `fs-integration-test-suite` is a Groovy/REST Assured and TestNG-based integration test suite for the WSO2 Financial Services Accelerator. It validates IS-layer and gateway-layer behaviour including DCR, token flows, consent management, and event notifications.

### Repository Structure

```
fs-integration-test-suite/
├── bfsi-test-framework/          # Base BFSI test framework
├── accelerator-test-framework/   # FS Accelerator test framework & TestConfiguration
│   └── src/main/resources/
│       └── TestConfigurationExample.xml
├── accelerator-tests/
│   ├── is-tests/                 # IS integration tests
│   │   ├── dcr/
│   │   ├── token/
│   │   ├── pre-configuration-step/
│   │   ├── consent-management/
│   │   └── event-notification/
│   └── gateway-tests/            # Gateway integration tests
│       ├── accounts/
│       ├── payments/
│       ├── cof/
│       ├── dcr/
│       ├── token/
│       ├── schema.validation/
│       └── non.regulatory.scenarios/
│       └── manual.client.registration/
├── test-artifacts/               # Keystores, SSAs, and other test artifacts
├── end-to-end-test-suite-execution/  # Automated scripts to execute the entier test suite on TestGrid
└── pom.xml
```

---

## Prerequisites

Before running the suite you need:

| Requirement                    | Notes                                                               |
|--------------------------------|---------------------------------------------------------------------|
| **Java 11**                    | `maven.compiler.source` and `maven.compiler.target` are set to `11` |
| **Apache Maven**               | Used to build and run all modules                                   |
| **WSO2 Identity Server x.x.x** | Download and setup accelerator deployment                           |
| **WSO2 API Manager x.x.x**     | Download and setup accelerator deployment                           |
| **MySQL or any DB**            | Required for consent DB                                             |
| **Firefox + GeckoDriver**      | Required for browser automation steps                               |

---

## Environment Setup

1. Follow the steps given in [Open Banking Accelerator Documentation](https://ob.docs.wso2.com/en/latest/install-and-setup/prerequisites/) to set up WSO2 IS and APIM with the accelerator pack. 
3. Download Firefox or Chrome specific web-driver according to the installed Browser. Make-sure to use the web-driver which support your Browser version and the operating system. Downloaded web-driver should be placed in the relevant folder inside fs-integration-test-suite/test-artifacts/selenium-libs.

---

## Running the Test Suite

## Method 1 - Manual Execution to run IS tests and Gateway tests separately.

### Step 1 - Configure `TestConfiguration.xml`

The test framework reads all settings from `TestConfiguration.xml`, which must be placed at:

```
fs-integration-test-suite/accelerator-test-framework/src/main/resources/TestConfiguration.xml
```

Take a copy of the [SampleTestConfiguration.xml](accelerator-test-framework%2Fsrc%2Fmain%2Fresources%2FSampleTestConfiguration.xml) file and place it in fs-integration-test-suite/accelerator-test-framework/src/main/resources folder with the name `TestConfiguration.xml`.
Then fill in your values. For further reference, we have provided sample values next to each configuration in the SampleTestConfiguration.xml file. Default values are already filled; you only need to fill in the placeholders. Additionally, either copy the appropriate client truststore (APIM or IS) to the `test-artifacts/client-truststore` directory or configure the truststore location as described in the [**`<Transport>` – Truststore**](#transport--truststore) section.

```bash
cp fs-integration-test-suite/accelerator-test-framework/src/main/resources/SampleTestConfiguration.xml \
   fs-integration-test-suite/accelerator-test-framework/src/main/resources/TestConfiguration.xml
```

The `[test.sh](end-to-end-test-suite-execution%2Ftest.sh)` script automates filling in this file using `sed` substitutions. Below is the full breakdown of every configurable section.

---

#### `<Common>` – General Settings

| XML Element | Description | Example Value |
|---|---|---|
| `<SolutionVersion>` | Accelerator version | `4.0.0` |
| `<IS_Version>` | WSO2 IS version | `7.0.0` |
| `<AccessTokenExpireTime>` | Access token expiry in seconds | `30` |
| `<TenantDomain>` | WSO2 tenant domain | `carbon.super` |
| `<SigningAlgorithm>` | JWT signing algorithm | `PS256` |
| `<TestArtifactLocation>` | Absolute path to the `test-artifacts` directory | `/path/to/fs-integration-test-suite/test-artifacts` |

---

#### `<Server>` – Server URLs

| XML Element | Description | Example Value |
|---|---|---|
| `<BaseURL>` | APIM Gateway base URL | `https://<AM_HOST>:8243` |
| `<ISServerUrl>` | WSO2 IS URL | `https://localhost:9446` |
| `<APIMServerUrl>` | WSO2 APIM URL | `https://<AM_HOST>:9443` |

---

#### `<Provisioning>` – API Provisioning

| XML Element | Description | Example Value |
|---|---|---|
| `<Enabled>` | Whether to publish/subscribe APIs via tests | `false` |
| `<ProvisionFilePath>` | Absolute path to YAML provisioning file | `/path/to/api-config-provisioning.yaml` |

---

#### `<ApplicationConfigList>` – TPP Application Configs

Two applications (TPP1 and TPP2) must be configured. Each `<AppConfig>` block has:

#### Signing Keystore

| XML Element | Description | Example |
|---|---|---|
| `<Location>` | Absolute path to signing `.jks` file | `.../tpp1/signing-keystore/signing.jks` |
| `<Alias>` | Key alias in the keystore | `signing` |
| `<Password>` | Keystore password | `wso2carbon` |
| `<SigningKid>` | Key ID (`kid`) used in JWT headers | `cIYo-5zX4OTWZpHrmmiZDVxACJM` |

#### Transport Keystore (mTLS)

| XML Element | Description | Example |
|---|---|---|
| `<MTLSEnabled>` | Enable mTLS for transport | `true` |
| `<Location>` | Absolute path to transport `.jks` file | `.../tpp1/transport-keystore/transport.jks` |
| `<Password>` | Keystore password | `wso2carbon` |
| `<Alias>` | Transport certificate alias | `transport` |

#### DCR Settings

| XML Element | Description | Example |
|---|---|---|
| `<SSAPath>` | Absolute path to the SSA (Software Statement Assertion) `.txt` file | `.../tpp1/ssa.txt` |
| `<SelfSignedSSAPath>` | Absolute path to self-signed SSA (for negative tests) | `.../tpp1/self_ssa.txt` |
| `<SoftwareId>` | Software ID embedded in the SSA | `oQ4KoaavpOuoE7rvQsZEOV` |
| `<RedirectUri>` | Primary redirect URI | `https://www.google.com/redirects/redirect1` |
| `<AlternateRedirectUri>` | Alternate redirect URI | `https://www.google.com/redirects/redirect2` |
| `<DCRAPIVersion>` | DCR API version | `0.1` |

#### Application Info (pre-registered app)

| XML Element | Description |
|---|---|
| `<ClientID>` | Pre-registered OAuth client ID |
| `<ClientSecret>` | Pre-registered OAuth client secret |
| `<RedirectURL>` | Registered redirect URL |  

---

#### `<Transport>` – Truststore

Points to the IS or APIM `client-truststore.jks`, which must already contain the server's public certificate.
If you are running IS tests, make sure to point to the IS client truststore; for gateway tests, point to the APIM client truststore. Alternatively, you can either use the server’s client truststore directly or copy the respective client truststore file to the test-artifacts/client-truststore folder.

| XML Element | Example Value                |
|---|------------------------------|
| `<Location>` | `/path/to/client-truststore.jks` |
| `<Type>` | `jks`                        |
| `<Password>` | `wso2carbon`                 |

---

#### `<NonRegulatoryApplication>` – Non-Regulatory App

| XML Element | Description |
|---|---|
| `<ClientID>` | Non-regulatory application client ID |
| `<ClientSecret>` | Non-regulatory application client secret |
| `<RedirectURL>` | Redirect URL |

---

#### `<PSUList>` – Payment Service Users

List one or more PSU credentials for browser automation consent flows:

```xml
<PSUList>
  <PSUInfo>
    <Credentials>
      <User>testUser@wso2.com</User>
      <Password>testUser@wso2123</Password>
    </Credentials>
  </PSUInfo>
</PSUList>
```

---

#### `<TPPInfo>` and `<KeyManagerAdmin>`

---

#### `<BrowserAutomation>` – Selenium WebDriver

| XML Element | Description | Example |
|---|---|---|
| `<BrowserPreference>` | `firefox` or `chrome` | `firefox` |
| `<HeadlessEnabled>` | Run browser without UI | `true` |
| `<WebDriverLocation>` | Absolute path to geckodriver or chromedriver | `/path/to/geckodriver` |

---

#### `<ConsentApi>` – Audience

| XML Element | Example Value |
|---|---|
| `<AudienceValue>` | `https://localhost:9446/oauth2/token` |

---

#### `<ISSetup>` – IS Admin Credentials

This block is appended after `</ConsentApi>`:

```xml
<ISSetup>
    <ISAdminUserName>is_admin@wso2.com</ISAdminUserName>
    <ISAdminPassword>wso2123</ISAdminPassword>
</ISSetup>
```

---

### Step 2 – Build the Test Framework

From within `fs-integration-test-suite/`:

```bash
cd fs-integration-test-suite
mvn clean install -Dmaven.test.skip=true
```

The parent `pom.xml` builds three modules in order:
1. `bfsi-test-framework`
2. `accelerator-test-framework`
3. `accelerator-tests`

---

### Step 3 – Run the IS Test Suite or Gateway Test Suite

```bash
cd fs-integration-test-suite/accelerator-tests/is-tests
mvn clean install
```


```bash
cd fs-integration-test-suite/accelerator-tests/gateway-tests
mvn clean install
```

---

## Method 2 – Full Automated Run

1. Fill the [deployment.properties](end-to-end-test-suite-execution%2Fdeployment.properties) with the relevant server hostnames and versions.

2. Copy the APIM client truststore files to the `test-artifacts/client-truststore` directory.

3. The `fs-integration-test-suite/end-to-end-test-suite-execution/test.sh` script reads a `deployment.properties` file and performs all configuration + test execution end-to-end.

4. Goto `fs-integration-test-suite/end-to-end-test-suite-execution` folder. 

5. Then execute the test.sh script.

```bash
./test.sh --input-dir <path_to_end-to-end-test-suite-execution_folder> --output-dir <path_to_output_reports>
```

!! Note: If you want to run the test suite against MCR application, make-sure to run the [test_mcr.sh](end-to-end-test-suite-execution%2Ftest_mcr.sh) script.

---

## Test Reports

After execution, HTML surefire reports are generated per module under target folder.

As an example: 

| Report | Location |
|---|---|
| DCR | `is-tests/dcr/target/surefire-reports/emailable-report.html` |

---

---

## Notes

- The `TestConfiguration.xml` is loaded at runtime from the classpath. The `ConfigParser` looks for it at the path defined in `ConfigConstants.OB_CONFIG_FILE_LOCATION` if no path is explicitly given; otherwise it uses the file placed in `accelerator-test-framework/src/main/resources/`. 

- The `SampleTestConfiguration.xml` must be copied to `TestConfiguration.xml` before running - the automated `test.sh` script does this automatically.

- The `is-setup` module is commented out in the IS test modules' `pom.xml` and is not run by default. It contains Groovy scripts for user creation and API authorization pre-steps.

- The `test-artifacts/DynamicClientRegistration/` directory (containing signing keystores, transport keystores, and SSA files for TPP1 and TPP2) must exist and be populated before running the tests. The `test.sh` script references these paths explicitly.

- The `pre-configuration-step` module (`CommonApplicationCreation`) registers a DCR application and writes the resulting `ClientID` back into `TestConfiguration.xml`, so it must run **before** tests that depend on a pre-registered client ID.

- Browser automation uses Selenium with Firefox and can be run in headless mode by setting `<HeadlessEnabled>true</HeadlessEnabled>`.

- Before re-running the automated test suite, clean up all resources created during the previous run - applications, key manager, user roles, and API resources.

