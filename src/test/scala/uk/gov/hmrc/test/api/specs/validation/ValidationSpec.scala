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

package uk.gov.hmrc.test.api.specs.validation

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.{JsNull, JsValue}
import uk.gov.hmrc.test.api.client.HttpClient
import uk.gov.hmrc.test.api.helpers.ValidationRequests
import uk.gov.hmrc.test.api.helpers.ValidationResponses.{phoneNumberErrorResponse, phoneNumberResponse}
import uk.gov.hmrc.test.api.specs.BaseSpec

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class ValidationSpec  extends BaseSpec with HttpClient{

  Feature("Validation - phonenumber") {

    Scenario("I wish to validate a UK mobile number") {
      // inputs
      val phoneNumberInput = "07843274323"
      // expected
      val expectedPhoneType = "Mobile"
      val expectedPhoneNumber = "+447843274323"

      Given("I have a valid mobile number")
        val phoneNumber = phoneNumberInput

      When("I validate it against the validation service")
        val jsonBody = ValidationRequests.phoneNumberRequest(phoneNumber)
        val result: Future[JsValue] = ValidationRequests.callService(jsonBody)


      Then("I should receive a valid message")
      Await.result(result, 50 seconds) must not be JsNull
      phoneNumberResponse.phoneNumber shouldBe expectedPhoneNumber
      phoneNumberResponse.phoneNumberType shouldBe expectedPhoneType

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
        val jsonBody = ValidationRequests.phoneNumberRequest(phoneNumber)
        val result: Future[JsValue] = ValidationRequests.callService(jsonBody)


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
        val result: Future[JsValue] = ValidationRequests.callService(jsonBody)
        Await.result(result, 50 seconds) must not be JsNull

      Then("I should receive an invalid message")
      phoneNumberErrorResponse.code shouldBe expectedErrorCode
      phoneNumberErrorResponse.message shouldBe expectedErrorMessage

    }

  }
}
