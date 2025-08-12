# phone-number-api-tests

API test suite for the `Phone Number Verification Service` using `api-test-runner` library.  

## Running the tests

Prior to executing the tests ensure you have:
  - Installed/configured Docker/Colima
  - Installed/configured [service manager 2](https://github.com/hmrc/sm2).  

Start Mongo:
```bash
    docker run --restart unless-stopped --name mongodb -p 27017:27017 -d percona/percona-server-mongodb:7.0 --replSet rs0
    docker exec -it mongodb mongosh --eval "rs.initiate();"
```
Run the following commands to start services locally:

```bash
    sm2 --start PHONE_NUMBER_ALL
```

Then execute the `run_tests.sh` script:

`./run_tests.sh <environment>` 

The tests default to the `local` environment.  For a complete list of supported param values, see:
 - `src/test/resources/application.conf` for **environment** 

#### Running the tests against a test environment

To run the tests against an environment set the corresponding `host` environment property as specified under
 `<env>.host.services` in the [application.conf](src/test/resources/application.conf).
