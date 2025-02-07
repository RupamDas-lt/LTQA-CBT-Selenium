# LTQA Selenium Automation Framework

A Selenium-based test automation framework using Cucumber for BDD testing.

## Prerequisites

1. Install all dependencies by running:
   ```bash
   mvn clean compile
   ```
2. Create a `cucumber.yaml` file in the path `src/test/resources/cucumber.yaml` using the same structure as
   `cucumber.sample.yaml`, but with your own credentials.

---

## Running Tests

### Basic Command

To run tests:

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DsuiteXmlFile=testng.xml -DENV=prod
```

### Re-running Failed Tests

To rerun failed tests:

```bash
  mvn test -DsuiteXmlFile=testng-rerun.xml -DENV=prod
```

---

## Flags and Their Use Cases

- **`ENV`**: Sets the environment for running tests. Options:
    - `prod`: Run tests in the production environment.
    - `stage`: Run tests in the staging environment.
    - `local`: Run tests on the local machine instead of a remote grid.

- **`RUN_N_TIMES`**: Specifies how many times to rerun the same scenario. Default value is `1`. Example:

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DRUN_N_TIMES=5
```

- **`PARALLEL`**: Sets concurrency for running tests. Example:

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DPARALLEL=3
```

- **`CUSTOM_USER_NAME`, `CUSTOM_USER_KEY`, `CUSTOM_USER_EMAIL`, `CUSTOM_USER_PASS`, `CUSTOM_GRID_URL`**: Use these
  flags to provide custom configurations if you want to run the tests with a custom user not defined in the
  `cucumber.yaml` file.
  Example:

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DCUSTOM_USER_NAME=username -DCUSTOM_USER_KEY=key -DCUSTOM_GRID_URL=gridUrl
```

---

## Tunnel

- Install brew and wget

```bash
  brew install wget
```

- **Setup Tunnel Binary**: We can execute Utility/Bash/SetupTunnelBinary.sh to setup tunnel binary in the required
  directory. For setting up binary of specific ENV we can pass --env flag value as stage or prod, by default it will
  download the Prod tunnel binary:

```bash
  bash Utility/Bash/SetupTunnelBinary.sh --env stage
```

---

## Random caps

- This framework has support of assigning random value of certain caps upon passing the caps value `.*`.
- Supported caps for random value: `geoLocation`, `browserVersion`

---

## Notes

- Always ensure the `cucumber.yaml` file is correctly set up with valid credentials before running tests.
- Adjust the `-DENV` flag as needed for your desired environment.
- Use the `-DPARALLEL` flag to optimize test execution time by running tests concurrently.

---

