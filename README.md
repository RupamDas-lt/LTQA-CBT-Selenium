# LTQA Selenium Automation Framework

A Selenium-based test automation framework using Cucumber for BDD testing.

## Prerequisites

1. Install all dependencies by running:
   ```bash
   mvn clean compile
   ```
2. Create a `cucumber.yaml` file in the path `src/test/resources/cucumber.yaml` using the same structure as
   `cucumber.sample.yaml`, but with your own credentials.
3. Install ffmpeg, this library is used for video verification. You can ignore this if that is not your use-case:
    ```bash
    brew install ffmpeg
    ```

---

## Running Tests

### Basic Command

To run tests:

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DsuiteXmlFile=testng.xml -DENV=prod -Dcucumber.features="src/test/features"
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

- **`CUSTOM_TEST_CAPS`**: This flag can be used to add or modify any capability test capability. Like if we want to pass
  custom build name NewRepoTesting, then we can use this flag like:

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DENV=prod -DCUSTOM_TEST_CAPS=build=NewRepoTesting -DPARALLEL=10
```

- **`REMOVE_TEST_CAPS`**: This flag can be used to bypass any specific test capabilities. It will also skip any
  test action which is dependent on that test action. Like if we want to
  bypass
  `geoLocation` and `resolution` caps then we can use this flag like:

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DENV=prod -DPARALLEL=10 -DREMOVE_TEST_CAPS=resolution,geoLocation
```

- **`PUSH_DATA_LOGS_TO_RP_FROM_LOCAL_CONFIG`**: This flag can be used to push the logs of tests running on your local
  machine to Report portal. In this scenario the rp config will be picked from
  `src/main/resources/reportportal.properties`, so make sure this file is present.

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DENV=prod -DPARALLEL=10 -DPUSH_DATA_LOGS_TO_RP_FROM_LOCAL_CONFIG=true
```

- **`REPEAT_TEST_ACTIONS`**: This is used to repeat the same test actions multiple times. The same thing can also be
  done in the step : `Then I set test actions repeat count to <testActionsRepeatCount>`.

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DENV=prod -DPARALLEL=10 -DREPEAT_TEST_ACTIONS=5
```

- **`SEND_DATA_TO_SUMO`**: Set this value to true if you want to push the test data to sumo logic. Before using this
  make sure `sumologic_url`, this variable has correct endpoint available in your `cucumber.yaml` file.

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DENV=prod -DPARALLEL=10 -DSEND_DATA_TO_SUMO=true
```

- **`PUT_CUSTOM_DATA_TO_SUMO_PAYLOAD`**: This can be used to add custom data in sumo payload from CLI. Use this with
  `SEND_DATA_TO_SUMO` flag. This data will be available under key: `custom_data_from_cli`.

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DENV=prod -DPARALLEL=10 -DSEND_DATA_TO_SUMO=true -DPUT_CUSTOM_DATA_TO_SUMO_PAYLOAD="key1=value1,key2=value2"
```

- **`JOB_PURPOSE`**: This variable can be passed through the CLI to define the job's purpose. Accepted values are
  `smoke`
  and `regression`. When the value `smoke` is passed, the framework randomizes the capability values to those most
  commonly used by customers.

```bash
  CUCUMBER_FILTER_TAGS="@test_tag" mvn test -DENV=prod -DPARALLEL=10 -DJOB_PURPOSE="true"
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

- **Update Hosts in Local Machine**: For tunnel sanity, we have pointed a custom domain to the localHost which is
  `locallambda.com`.

    - To add the entry, the flag is `--addEntry`.
    - To remove the entry, the flag is `--removeEntry`.

```bash
  bash ./Utility/Bash/UpdateHostEntry.sh --addEntry
```

---

## Random caps

- This framework has support of assigning random value of certain caps upon passing the caps value `.*`.
- Supported caps for random value: `geoLocation`, `browserVersion`, `version`, `timezone`

---

## Notes

- Always ensure the `cucumber.yaml` file is correctly set up with valid credentials before running tests.
- Adjust the `-DENV` flag as needed for your desired environment.
- Use the `-DPARALLEL` flag to optimize test execution time by running tests concurrently.
- For cleaning up the test results, cached test data like browser versions, geoLocation, you can run the following
  command:

```bash
  bash ./Utility/Bash/Cleanup.sh
```

- Sumo query for getting the customer's test data

```sql
  _index
=prod_test_datails
| where (product == "Web Automation Desktop Browsers") and user_status!="Internal" and %"capability.desiredcapabilities.lt:options.geoLocation" != null
```

- Sumo query for getting the test data pushed from this framework

```sql
_source
="ml_qa_logs" AND _collector="qa-logs" 
| where message="New_Framework_Testing-2"
```

---

