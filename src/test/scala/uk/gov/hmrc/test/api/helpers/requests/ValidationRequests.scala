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

package uk.gov.hmrc.test.api.helpers.requests

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.test.api.client.HttpClient
import uk.gov.hmrc.test.api.conf.TestConfiguration
import uk.gov.hmrc.test.api.helpers.common.JsonRequests
import uk.gov.hmrc.test.api.helpers.validate.ValidationResponses._
import uk.gov.hmrc.test.api.models.common.PhoneNumberErrorResponse
import uk.gov.hmrc.test.api.models.validate.PhoneNumberResponse

import scala.concurrent.Future
import scala.util.Try

object ValidationRequests extends HttpClient with JsonRequests {

  private val urlValidation = TestConfiguration.url("cip-phone-number-validation")

  def callValidateEndpoint(jsonBody: String, headers: (String, String) = headers): Future[JsValue] =
    post(s"$urlValidation$pathPrefix/validate", jsonBody, headers)
      .collect {
        case r: StandaloneWSResponse if r.status >= 200 && r.status < 300 =>
          val jsonResp = Json.parse(r.body)
          Try {
            phoneNumberResponse = jsonResp.as[PhoneNumberResponse]
          } recover { case _: Exception => println("could not parse phone number") }
          jsonResp
        case r: StandaloneWSResponse =>
          val jsonResp = Json.parse(r.body)
          phoneNumberErrorResponse = jsonResp.as[PhoneNumberErrorResponse]
          jsonResp
      } recoverWith {
      case _ => Future.failed(new Exception)
    }
}
