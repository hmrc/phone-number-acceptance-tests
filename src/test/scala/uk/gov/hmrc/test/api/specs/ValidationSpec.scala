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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json.{JsNull, JsValue}
import uk.gov.hmrc.test.api.helpers.requests.ValidationRequests
import uk.gov.hmrc.test.api.helpers.requests.ValidationRequests.{callValidateEndpoint, phoneNumberRequest}
import uk.gov.hmrc.test.api.helpers.validate.ValidationResponses.{phoneNumberErrorResponse, phoneNumberResponse}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class ValidationSpec extends BaseSpec {

  Feature("Validation - phonenumber") {

    Scenario("I wish to validate a valid UK mobile number") {
      val expectedPhoneType = "Mobile"
      // a list of
      // input, expected
      val validUkMobileData = Table(
        ("07843274323", "+447843274323"),
        ("0044(0)7890056734", "+447890056734"),
        ("+44-7890056734", "+447890056734")
      )

      forAll(validUkMobileData) { (phoneNumberInput, expectedPhoneNumber) =>
        Given("I have a valid mobile number")
        val phoneNumber = phoneNumberInput

        When("I validate it against the validation service")
        val jsonBody = phoneNumberRequest(phoneNumber)
        val result: Future[JsValue] = callValidateEndpoint(jsonBody)

        Then("I should receive a valid message")
        Await.result(result, 50 seconds) must not be JsNull
        phoneNumberResponse.phoneNumber shouldBe expectedPhoneNumber
        phoneNumberResponse.phoneNumberType shouldBe expectedPhoneType
      }
    }

    Scenario("I wish to validate an invalid UK mobile number") {
      // input, expected code, expected message
      val invalidUkMobileData = Table(
        ("0784327432e", "VALIDATION_ERROR", "Enter a valid telephone number"),
        ("999", "VALIDATION_ERROR", "Enter a valid telephone number"),
        ("78900567343", "VALIDATION_ERROR", "Enter a valid telephone number")
      )

      forAll(invalidUkMobileData) { (phoneNumberInput, expectedCodeInErrorResponse, expectedMessageInErrorResponse) =>
        Given("I have an invalid mobile number")
        val phoneNumber = phoneNumberInput

        When("I validate it against the validation service")
        val jsonBody = phoneNumberRequest(phoneNumber)
        val result: Future[JsValue] = callValidateEndpoint(jsonBody)

        Then("I should receive a validation error message")
        Await.result(result, 50 seconds) must not be JsNull
        phoneNumberErrorResponse.code shouldBe expectedCodeInErrorResponse
        phoneNumberErrorResponse.message shouldBe expectedMessageInErrorResponse
      }
    }

    Scenario("I wish to validate a UK Fixed Line") {
      // inputs
      val phoneNumberInput = "02088203456"
      // expected
      val expectedPhoneType = "Fixed_line"
      val expectedPhoneNumber = "+442088203456"

      Given("I have a valid fixed line number")
      val phoneNumber = phoneNumberInput

      When("I validate it against the validation service")
      val jsonBody = phoneNumberRequest(phoneNumber)
      val result: Future[JsValue] = callValidateEndpoint(jsonBody)

      Then("I should receive a valid message")
      Await.result(result, 50 seconds) must not be JsNull
      phoneNumberResponse.phoneNumber shouldBe expectedPhoneNumber
      phoneNumberResponse.phoneNumberType shouldBe expectedPhoneType
    }

    Scenario("I wish to invalid a UK Fixed Line") {
      // inputs
      val phoneNumberInput = "02088w03456"
      // expected
      val expectedErrorCode = "VALIDATION_ERROR"
      val expectedErrorMessage = "Enter a valid telephone number"

      Given("I have a valid fixed line number")
      val phoneNumber = phoneNumberInput

      When("I validate it against the validation service")
      val jsonBody = ValidationRequests.phoneNumberRequest(phoneNumber)
      val result: Future[JsValue] = ValidationRequests.callValidateEndpoint(jsonBody)
      Await.result(result, 50 seconds) must not be JsNull

      Then("I should receive an invalid message")
      phoneNumberErrorResponse.code shouldBe expectedErrorCode
      phoneNumberErrorResponse.message shouldBe expectedErrorMessage
    }
  }
}
