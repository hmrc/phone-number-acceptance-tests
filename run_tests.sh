#!/bin/bash -e
ENV=${1:-local}
DAST=${2:-false}

sbt -Denvironment=$ENV -Dsecurity.assessment=$DAST "testOnly uk.gov.hmrc.test.api.specs.*"
