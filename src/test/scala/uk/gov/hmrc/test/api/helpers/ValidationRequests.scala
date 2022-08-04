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

package uk.gov.hmrc.test.api.helpers

import play.api.libs.json.Json
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.test.api.client.HttpClient
import uk.gov.hmrc.test.api.conf.TestConfiguration
import uk.gov.hmrc.test.api.helpers.ValidationResponses._
import uk.gov.hmrc.test.api.models.{PhoneNumberErrorResponse, PhoneNumberResponse}

import scala.concurrent.Future
import scala.util.Try

object ValidationRequests extends HttpClient {

  val headers = ("content-type" -> "application/json")

  val urlValidation = TestConfiguration.url("cip-phone-number-validation")
  val path = "/customer-insight-platform/phone-number/validate"

  def callService(jsonBody: String, headers: (String, String) = ValidationRequests.headers) =
    post(s"$urlValidation$path", jsonBody, headers)
    .collect {
      case r: StandaloneWSResponse if r.status >= 200 && r.status < 300 =>
        val jsonResp = Json.parse(r.body)
        Try{
          phoneNumberResponse = jsonResp.as[PhoneNumberResponse]
        } recover {case e: Exception => println("could not parse phone" )}
        jsonResp
      case r: StandaloneWSResponse =>
        val jsonResp = Json.parse(r.body)
        phoneNumberErrorResponse = jsonResp.as[PhoneNumberErrorResponse]
        jsonResp
    } recoverWith {
    case _ => Future.failed(new Exception)
  }

  def phoneNumberRequest(phoneNumber: String) = s"""{"phoneNumber" : "$phoneNumber" }"""

}
