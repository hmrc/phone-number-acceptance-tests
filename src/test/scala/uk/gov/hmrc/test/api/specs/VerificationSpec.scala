/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.test.api.specs

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json.{JsNull, JsValue}
import uk.gov.hmrc.test.api.helpers.requests.VerificationRequests.{callVerifyEndpoint, callVerifyPasscodeEndpoint, phoneNumberRequest, verifyPasscodeRequest}
import uk.gov.hmrc.test.api.helpers.verify.VerificationResponses.{indeterminateResponse, phoneNumberErrorResponse, verifyResponseHeaders}
import uk.gov.hmrc.test.api.helpers.verifyPasscode.VerifyPasscodeResponses.verifyPasscodeResponse
import uk.gov.hmrc.test.api.models.passcode.PasscodeData

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class VerificationSpec extends BaseSpec {
  Scenario("I wish to verify a UK mobile number and use an invalid passcode") {
    // inputs
    val phoneNumberInput = "07843274323"
    // expected
    val normalisedPhoneNumber = "+447843274323"

    // a list of passcode input
    val invalidPasscodes = Table(
      "", // blank
      "PLY", // too short
      "NHYTREL", // too long
      "LKJH", // invalid length
      " " // empty
    )

    forAll(invalidPasscodes) { passcodeInput =>
      Given("I have a valid mobile number")
      val phoneNumber = phoneNumberInput

      When("I validate it against the verification service")
      val jsonBody = phoneNumberRequest(phoneNumber)
      val result: Future[Any] = callVerifyEndpoint(jsonBody)
      Await.result(result, 50 seconds) must not be JsNull

      Then("I should receive a notification ID")
      verifyResponseHeaders("Location").head shouldBe a[String]

      And("Once I receive the correct passcode on my mobile and ignore it")

      When("I verify incorrect passcode")
      val jsonPasscodeBody = verifyPasscodeRequest(normalisedPhoneNumber, passcodeInput)
      val resultPasscode: Future[JsValue] = callVerifyPasscodeEndpoint(jsonPasscodeBody)
      Await.result(resultPasscode, 50 seconds) must not be JsNull

      And("I get not verified status with invalid message")
      phoneNumberErrorResponse.code shouldBe 1002
      phoneNumberErrorResponse.message shouldBe "Enter a valid passcode"
    }
  }

  Scenario("I wish to verify a UK mobile number and use an incorrect passcode") {
    // inputs
    val phoneNumberInput = "07843274323"
    // expected
    val normalisedPhoneNumber = "+447843274323"

    Given("I have a valid mobile number")
    val phoneNumber = phoneNumberInput

    When("I validate it against the verification service")
    val jsonBody = phoneNumberRequest(phoneNumber)
    val result: Future[Any] = callVerifyEndpoint(jsonBody)
    Await.result(result, 50 seconds) must not be JsNull

    Then("I should receive a notification ID")
    verifyResponseHeaders("Location").head shouldBe a[String]

    And("Once I receive the correct passcode on my mobile and ignore it")

    When("I verify incorrect passcode")
    val jsonPasscodeBody = verifyPasscodeRequest(normalisedPhoneNumber, "123456")
    val resultPasscode: Future[JsValue] = callVerifyPasscodeEndpoint(jsonPasscodeBody)
    Await.result(resultPasscode, 50 seconds) must not be JsNull

    And("I get not verified status with invalid message")
    phoneNumberErrorResponse.code shouldBe 1002
    phoneNumberErrorResponse.message shouldBe "Enter a valid passcode"
  }
  Scenario("I wish to verify a valid UK mobile number and use correct passcode") {
    val validUkMobileData = Table(
      ("0091(0)98981 220 93", "+919898122093"),
      ("+0044(0)791.220-4199", "+447912204199"),
      ("07915509999", "+447915509999"),
      ("0044(0)7912204232", "+447912204232"),
      ("+0044(0)791.220-4199", "+447912204199")
    )

    forAll(validUkMobileData) { (phoneNumberInput, normalisedPhoneNumber) =>
      Given("I have a valid mobile number")
      val phoneNumber = phoneNumberInput

      When("I verify it against the verification service")
      val jsonBody = phoneNumberRequest(phoneNumber)
      val verifyResult: Future[Any] = callVerifyEndpoint(jsonBody)
      Await.result(verifyResult, 50 seconds) must not be JsNull

      Then("I should receive a notification ID")
      verifyResponseHeaders("Location").head shouldBe a[String]

      And("Once I receive the correct passcode on my mobile")
      // retrieve the expected passcode from the stubs repo
      val maybePasscodeData: Option[PasscodeData] = passcodeHelper.getPasscodeForPhoneNumber(normalisedPhoneNumber).futureValue
      maybePasscodeData must not be None
      val passcode = maybePasscodeData.get
      maybePasscodeData.get shouldBe a[PasscodeData]
      val expectedPasscode = passcode

      When("I verify correct passcode")
      val jsonPasscodeBody = verifyPasscodeRequest(phoneNumber, expectedPasscode.passcode)
      val resultPasscode: Future[JsValue] = callVerifyPasscodeEndpoint(jsonPasscodeBody)
      Await.result(resultPasscode, 50 seconds) must not be JsNull

      And("I get verified status with verified message")
      verifyPasscodeResponse.status shouldBe "Verified"
    }
  }

  Scenario("I wish to verify an invalid phone number") {
    // a list of input
    val invalidPhoneNumberData = Table(
      "o7915569873", // Mobile with letters
      "", // Blank submission
      "7915598769", // Invalid format
      "0800111", // Not minimum character length
      "+358 04 57 123- 45 67", // Not Maximum character length
      " " // empty submission
    )

    forAll(invalidPhoneNumberData) { input: String =>
      Given("I have a invalid phone number")
      val phoneNumber = input

      When("I verify it against the verification service")
      val jsonBody = phoneNumberRequest(phoneNumber)
      val verifyResult: Future[Any] = callVerifyEndpoint(jsonBody)
      Await.result(verifyResult, 50 seconds) must not be JsNull

      Then("I should receive a validation error")
      phoneNumberErrorResponse.code shouldBe a[Int]
      phoneNumberErrorResponse.code shouldBe 1002
      phoneNumberErrorResponse.message shouldBe a[String]
      phoneNumberErrorResponse.message shouldBe "Enter a valid telephone number"
    }
  }

  Scenario("I wish to verify an invalid phone number type") {
    // a list of input
    val invalidPhoneTypeData = Table(
      "00358 600189089", // Premium Number
      "0049(221)-5429.44 79", // Fixed-line (alphanumeric)
    )

    forAll(invalidPhoneTypeData) { input: String =>
      Given("I have a invalid phone number type")
      val phoneNumber = input

      When("I verify it against the verification service")
      val jsonBody = phoneNumberRequest(phoneNumber)
      val verifyResult: Future[Any] = callVerifyEndpoint(jsonBody)
      Await.result(verifyResult, 50 seconds) must not be JsNull

      Then("I should receive an indeterminate response")
      indeterminateResponse.status shouldBe a[String]
      indeterminateResponse.status shouldBe "Indeterminate"
      indeterminateResponse.message shouldBe a[String]
      indeterminateResponse.message shouldBe "Only mobile numbers can be verified"
    }
  }
}
