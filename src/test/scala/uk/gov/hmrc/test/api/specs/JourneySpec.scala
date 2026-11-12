/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import uk.gov.hmrc.test.api.testdata.JourneyTestData

import java.util.UUID

class JourneySpec extends BaseSpec with JourneyTestData:

  private var persistedJourneyId: String = ""

  Scenario("Journey can be initialised without a phone number"):
    assert(persistedJourneyId.isEmpty, "Persisted journey id should be empty before initialisation")

    Given("I have a journey initialisation request")
    When("I send the request to the journey service")
    val response = journeyHelper.sendJourneyInitialisationRequest(journeyConfigWithNoPhoneNumber)

    Then("the response should be successful CREATED")
    response.status shouldBe 201

    And("the response should contain a URL to the frontend journey page")
    val responseBodyAsString: String = response.body[JsValue].as[String]
    responseBodyAsString should fullyMatch regex s"/phone-number-verification/journey/[a-f0-9\\-]+/enter-phone-number"

    And("the journey identifier in the redirect url should be a valid UUID")
    persistedJourneyId = responseBodyAsString.split("/")(3)
    val parsedJourneyId = UUID.fromString(persistedJourneyId)
    parsedJourneyId shouldBe a[UUID]

  Scenario("Journey configuration without a phone number can be retrieved"):
    assert(persistedJourneyId.nonEmpty, "Persisted journey id should not be empty before retrieval")

    Given("I have a persisted journey ID from the initialisation step")
    persistedJourneyId should not be empty

    When("I send a request to the journey service using that journey ID")
    val journeyConfigResponse = journeyHelper.getJourneyConfig(persistedJourneyId)

    Then("the response should be successful OK")
    journeyConfigResponse.status shouldBe 200

    And("the response should contain the expected journey configuration")
    val journeyConfigResponseBody: JsValue = journeyConfigResponse.body[JsValue]
    (journeyConfigResponseBody \ "credentialId").as[String]           shouldBe "123456789"
    (journeyConfigResponseBody \ "options" \ "serviceUrl").as[String] shouldBe (journeyConfigWithNoPhoneNumber \ "options" \ "serviceUrl").as[String]
    (journeyConfigResponseBody \ "options" \ "completionUrl").as[String] shouldBe (journeyConfigWithNoPhoneNumber \ "options" \ "completionUrl")
      .as[String]
    (journeyConfigResponseBody \ "options" \ "signOutUrl").as[String] shouldBe (journeyConfigWithNoPhoneNumber \ "options" \ "signOutUrl").as[String]
    (journeyConfigResponseBody \ "options" \ "accessibilityStatementUrl")
      .as[String] shouldBe (journeyConfigWithNoPhoneNumber \ "options" \ "accessibilityStatementUrl").as[String]

  Scenario("Journey can be initialised with a phone number"):
    persistedJourneyId = ""
    assert(persistedJourneyId.isEmpty, "Persisted journey id should be empty before initialisation")

    Given("I have a journey initialisation request")
    When("I send the request to the journey service")
    val response = journeyHelper.sendJourneyInitialisationRequest(journeyConfigWithPhoneNumber)

    Then("the response should be successful CREATED")
    response.status shouldBe 201

    And("the response should contain a URL to the frontend journey page")
    val responseBodyAsString: String = response.body[JsValue].as[String]
    responseBodyAsString should fullyMatch regex s"/phone-number-verification/journey/[a-f0-9\\-]+/enter-pass-code"

    And("the journey identifier in the redirect url should be a valid UUID")
    persistedJourneyId = responseBodyAsString.split("/")(3)
    val parsedJourneyId = UUID.fromString(persistedJourneyId)
    parsedJourneyId shouldBe a[UUID]

  Scenario("Journey configuration with a phone number can be retrieved"):
    assert(persistedJourneyId.nonEmpty, "Persisted journey id should not be empty before retrieval")

    Given("I have a persisted journey ID from the initialisation step")
    persistedJourneyId should not be empty

    When("I send a request to the journey service using that journey ID")
    val journeyConfigResponse = journeyHelper.getJourneyConfig(persistedJourneyId)

    Then("the response should be successful OK")
    journeyConfigResponse.status shouldBe 200

    And("the response should contain the expected journey configuration")
    val journeyConfigResponseBody: JsValue = journeyConfigResponse.body[JsValue]
    print(journeyConfigResponseBody)
    (journeyConfigResponseBody \ "credentialId").as[String]           shouldBe "123456789"
    (journeyConfigResponseBody \ "options" \ "serviceUrl").as[String] shouldBe (journeyConfigWithPhoneNumber \ "options" \ "serviceUrl").as[String]
    (journeyConfigResponseBody \ "options" \ "completionUrl").as[String] shouldBe (journeyConfigWithPhoneNumber \ "options" \ "completionUrl")
      .as[String]
    (journeyConfigResponseBody \ "options" \ "signOutUrl").as[String] shouldBe (journeyConfigWithPhoneNumber \ "options" \ "signOutUrl").as[String]
    (journeyConfigResponseBody \ "options" \ "accessibilityStatementUrl")
      .as[String] shouldBe (journeyConfigWithPhoneNumber \ "options" \ "accessibilityStatementUrl").as[String]
    (journeyConfigResponseBody \ "phoneNumberToVerify").as[String] shouldBe (journeyConfigWithPhoneNumber \ "phoneNumberToVerify").as[String]
