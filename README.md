**This is a template README.md.  Be sure to update this with project specific content that describes your api test project.**

# cip-phone-number-acceptance-tests
API test suite for the `<digital service name>` using ScalaTest and [play-ws](https://github.com/playframework/play-ws) client.  

## Running the tests

Prior to executing the tests ensure you have:
 - Installed [MongoDB](https://docs.mongodb.com/manual/installation/) 
 - Installed/configured [service manager](https://github.com/hmrc/service-manager).  

Run the following commands to start services locally:

    docker run --rm -d --name mongo -d -p 27017:27017 mongo:4.0
    sm --start CIP_PHONE_NUMBER_ALL -r --wait 100

Using the `--wait 100` argument ensures a health check is run on all the services started as part of the profile. `100` refers to the given number of seconds to wait for services to pass health checks.    

Then execute the `run_tests.sh` script:

`./run_tests.sh <environment>` 

The tests default to the `local` environment.  For a complete list of supported param values, see:
 - `src/test/resources/application.conf` for **environment** 

#### Running the tests against a test environment

To run the tests against an environment set the corresponding `host` environment property as specified under
 `<env>.host.services` in the [application.conf](src/test/resources/application.conf). 
