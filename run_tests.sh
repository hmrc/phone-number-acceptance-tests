#!/bin/bash -e
ENV=${1:-local}

# Scalafmt checks have been separated from the test command to avoid OutOfMemoryError in Jenkins
#sbt scalafmtCheckAll scalafmtSbtCheck

echo "======== cip phone number api acceptance tests ========"
if [[ $ENV="local" ]]
then
  echo "Before you run tests ensure all cip phone number services are running"
  echo "Are all CIP PHONE NUMBER services running?"
  select yn in "Yes" "No"; do
      case $yn in
          Yes ) break;;
          No ) exit;;
      esac
  done
fi
echo "=================================================================="

sbt -Denvironment=$ENV "testOnly uk.gov.hmrc.test.api.specs.*"