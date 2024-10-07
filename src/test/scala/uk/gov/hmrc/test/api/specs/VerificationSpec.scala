/*
 * Copyright 2023 HM Revenue & Customs
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

import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import play.api.http.Status._
class VerificationSpec extends BaseSpec {
  Scenario("I wish to verify a UK mobile number and use an invalid passcode") {
    // inputs
    val phoneNumber = "07843274323"
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

    forAll(invalidPasscodes) { passcode =>
      Given("I have a valid mobile number")
      When("I validate it against the verification service")
      val verifyResponse = verifyMatchingHelper.sendCode(phoneNumber)

      Then("I should receive 200 status code")
      verifyResponse.status shouldBe OK

      And("Once I receive the correct passcode on my mobile and ignore it")

      When("I verify incorrect passcode")

      val verifyPasscodeResponse = verifyMatchingHelper.verifyCode(normalisedPhoneNumber, passcode)

      And("I get an error response")
      verifyPasscodeResponse.status shouldBe BAD_REQUEST
      (verifyPasscodeResponse.body[JsValue] \ "status").as[String] shouldBe "VALIDATION_ERROR"
      (verifyPasscodeResponse.body[JsValue] \ "message").as[String] shouldBe "Enter a valid telephone number/passcode"
    }
  }

  Scenario("I wish to verify a UK mobile number and use an incorrect passcode") {
    // inputs
    val phoneNumber = "07843274323"
    // expected
    val normalisedPhoneNumber = "+447843274323"

    Given("I have a valid mobile number")
    When("I validate it against the verification service")
    val verifyResponse = verifyMatchingHelper.sendCode(phoneNumber)

    Then("I should receive 200 status code")
    verifyResponse.status shouldBe OK

    And("Once I receive the correct passcode on my mobile and ignore it")
    When("I verify incorrect passcode")
    val verifyPasscodeResponse = verifyMatchingHelper.verifyCode(normalisedPhoneNumber, "123456")

    And("I get a not verified response")
    verifyPasscodeResponse.status shouldBe NOT_FOUND
    (verifyPasscodeResponse.body[JsValue] \ "status").as[String] shouldBe "CODE_VERIFY_FAILURE"
    (verifyPasscodeResponse.body[JsValue] \ "message").as[String] shouldBe "Enter a valid passcode"
  }

  Scenario("I wish to verify a valid UK mobile number and use correct passcode") {
    val validUkMobileData = Table(
      ("0091(0)98981 220 93", "+919898122093"),
      ("+0044(0)791.220-4199", "+447912204199"),
      ("07915509999", "+447915509999"),
      ("0044(0)7912204232", "+447912204232"),
      ("+0044(0)791.220-4199", "+447912204199")
    )

    forAll(validUkMobileData) { (phoneNumber, normalisedPhoneNumber) =>
      Given("I have a valid mobile number")
      When("I verify it against the verification service")
      val verifyResponse = verifyMatchingHelper.sendCode(phoneNumber)

      Then("I should receive 200 status code")
      verifyResponse.status shouldBe OK

      And("Once I receive the correct passcode on my mobile")
      // retrieve the expected passcode from the stubs repo
      val phoneNumberAndPasscode = testDataHelper.getPasscodeForPhoneNumber(normalisedPhoneNumber).get

      When("I verify correct passcode")
      val verifyPasscodeResponse = verifyMatchingHelper.verifyCode(normalisedPhoneNumber, phoneNumberAndPasscode.verificationCode)

      And("I get verified status with verified message")
      (verifyPasscodeResponse.body[JsValue] \ "status").as[String] shouldBe "CODE_VERIFIFIED"
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

    forAll(invalidPhoneNumberData) { phoneNumber: String =>
      Given("I have a invalid phone number")
      When("I verify it against the verification service")
      val verifyResponse = verifyMatchingHelper.sendCode(phoneNumber)

      Then("I should receive a validation error")
      verifyResponse.status shouldBe BAD_REQUEST
      (verifyResponse.body[JsValue] \ "status").as[String] shouldBe "VALIDATION_ERROR"
      (verifyResponse.body[JsValue] \ "message").as[String] shouldBe "Enter a valid telephone number"
    }
  }

  Scenario("I wish to verify an invalid phone number type") {
    // a list of input
    val invalidPhoneTypeData = Table(
      "00358 600189089", // Premium Number
      "0049(221)-5429.44 79", // Fixed-line (alphanumeric)
    )

    forAll(invalidPhoneTypeData) { phoneNumber: String =>
      Given("I have a invalid phone number type")
      When("I verify it against the verification service")
      val verifyResponse = verifyMatchingHelper.sendCode(phoneNumber)

      Then("I should receive an indeterminate response")
      (verifyResponse.body[JsValue] \ "status").as[String] shouldBe "INDETERMINATE"
      (verifyResponse.body[JsValue] \ "message").as[String] shouldBe "Only mobile numbers can be verified"
    }
  }
}
