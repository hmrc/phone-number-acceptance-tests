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
import uk.gov.hmrc.test.api.helpers.requests.ValidationRequests.otpRequest
import uk.gov.hmrc.test.api.helpers.requests.VerificationRequests.{callVerifyEndpoint, callVerifyOtpEndpoint, phoneNumberRequest}
import uk.gov.hmrc.test.api.helpers.verify.VerificationResponses.{indeterminateResponse, phoneNumberErrorResponse, verifyResponse}
import uk.gov.hmrc.test.api.helpers.verifyOtp.OtpVerificationResponses.otpResponse
import uk.gov.hmrc.test.api.models.otp.OtpData

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class VerificationSpec extends BaseSpec {

  Scenario("I wish to verify a UK mobile number and use an invalid OTP") {
    // inputs
    val phoneNumberInput = "07843274323"
    // expected
    val normalisedPhoneNumber = "+447843274323"

    // a list of OTP input
    val invalidOtps = Table(
      "", // blank
      "PLY", // too short
      "NHYTREL", // too long
      "LKJH", // invalid length
      " " // empty
    )

    forAll(invalidOtps) { otpInput =>
      Given("I have a valid mobile number")
      val phoneNumber = phoneNumberInput

      When("I validate it against the verification service")
      val jsonBody = phoneNumberRequest(phoneNumber)
      val result: Future[JsValue] = callVerifyEndpoint(jsonBody)
      Await.result(result, 50 seconds) must not be JsNull

      Then("I should receive a notification ID")
      verifyResponse.notificationId shouldBe a[String]

      And("Once I receive the correct OTP on my mobile and ignore it")

      When("I verify incorrect OTP")
      val jsonOtpBody = otpRequest(normalisedPhoneNumber, otpInput)
      val resultOtp: Future[JsValue] = callVerifyOtpEndpoint(jsonOtpBody)
      Await.result(resultOtp, 50 seconds) must not be JsNull

      And("I get not verified status with invalid message")
      phoneNumberErrorResponse.code shouldBe "VALIDATION_ERROR"
      phoneNumberErrorResponse.message shouldBe "Enter a valid passcode"
    }
  }

  Scenario("I wish to verify a UK mobile number and use an incorrect OTP") {
    // inputs
    val phoneNumberInput = "07843274323"
    // expected
    val normalisedPhoneNumber = "+447843274323"

    Given("I have a valid mobile number")
    val phoneNumber = phoneNumberInput

    When("I validate it against the verification service")
    val jsonBody = phoneNumberRequest(phoneNumber)
    val result: Future[JsValue] = callVerifyEndpoint(jsonBody)
    Await.result(result, 50 seconds) must not be JsNull

    Then("I should receive a notification ID")
    verifyResponse.notificationId shouldBe a[String]

    And("Once I receive the correct OTP on my mobile and ignore it")

    When("I verify incorrect OTP")
    val jsonOtpBody = otpRequest(normalisedPhoneNumber, "123456")
    val resultOtp: Future[JsValue] = callVerifyOtpEndpoint(jsonOtpBody)
    Await.result(resultOtp, 50 seconds) must not be JsNull

    And("I get not verified status with invalid message")
    otpResponse.status shouldBe "Not verified"
  }

  Scenario("I wish to verify a valid UK mobile number and use correct OTP") {
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
      val verifyResult: Future[JsValue] = callVerifyEndpoint(jsonBody)
      Await.result(verifyResult, 50 seconds) must not be JsNull

      Then("I should receive a notification ID")
      verifyResponse.notificationId shouldBe a[String]

      And("Once I receive the correct OTP on my mobile")
      // retrieve the expected Otp from the stubs repo
      val maybeOtp: Option[OtpData] = otpHelper.getOtpForPhoneNumber(normalisedPhoneNumber).futureValue
      maybeOtp must not be None
      val otp = maybeOtp.get
      maybeOtp.get shouldBe a[OtpData]
      val expectedOtp = otp

      When("I verify correct OTP")
      val jsonOtpBody = otpRequest(phoneNumber, expectedOtp.otp)
      val resultOtp: Future[JsValue] = callVerifyOtpEndpoint(jsonOtpBody)
      Await.result(resultOtp, 50 seconds) must not be JsNull

      And("I get verified status with verified message")
      otpResponse.status shouldBe "Verified"
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
      val verifyResult: Future[JsValue] = callVerifyEndpoint(jsonBody)
      Await.result(verifyResult, 50 seconds) must not be JsNull

      Then("I should receive a validation error")
      phoneNumberErrorResponse.code shouldBe a[String]
      phoneNumberErrorResponse.code shouldBe "VALIDATION_ERROR"
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
      val verifyResult: Future[JsValue] = callVerifyEndpoint(jsonBody)
      Await.result(verifyResult, 50 seconds) must not be JsNull

      Then("I should receive an indeterminate response")
      indeterminateResponse.status shouldBe a[String]
      indeterminateResponse.status shouldBe "Indeterminate"
      indeterminateResponse.message shouldBe a[String]
      indeterminateResponse.message shouldBe "Only mobile numbers can be verified"
    }
  }
}
