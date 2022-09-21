#!/bin/bash -e
ENV=${1:-local}

sbt -Denvironment=$ENV "testOnly uk.gov.hmrc.test.api.specs.*"
