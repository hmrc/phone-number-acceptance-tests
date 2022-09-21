#!/bin/bash -e
ENV=${1:-local}

sbt -Denvironment=$ENV -Dzap.proxy=true "testOnly uk.gov.hmrc.test.api.specs.*"
