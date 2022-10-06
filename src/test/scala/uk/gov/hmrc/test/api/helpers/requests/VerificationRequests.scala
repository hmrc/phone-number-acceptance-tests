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
import uk.gov.hmrc.test.api.helpers.verify.VerificationResponses.{indeterminateResponse, phoneNumberErrorResponse, verifyResponseHeaders}
import uk.gov.hmrc.test.api.helpers.verifyPasscode.VerifyPasscodeResponses.verifyPasscodeResponse
import uk.gov.hmrc.test.api.models.common.PhoneNumberErrorResponse
import uk.gov.hmrc.test.api.models.passcode.VerifyPasscodeResponse
import uk.gov.hmrc.test.api.models.verify.IndeterminateResponse

import scala.concurrent.Future
import scala.util.Try

object VerificationRequests extends HttpClient with JsonRequests {

  private val urlVerification = TestConfiguration.url("cip-phone-number")

  def callVerifyEndpoint(jsonBody: String, headers: (String, String) = headers): Future[Any] =
    post(s"$urlVerification$pathPrefix/verify", jsonBody, headers)
      .collect {
        case r: StandaloneWSResponse if r.status >= 200 && r.status < 300 =>
          val body = r.body
          Try {
            if (isIndeterminateResponse(body)) {
              indeterminateResponse = Json.parse(body).as[IndeterminateResponse]
            } else {
              verifyResponseHeaders = r.headers
            }
          } recover { case _: Exception => println("could not parse phone number") }
        case r: StandaloneWSResponse =>
          val jsonResp = Json.parse(r.body)
          Try {
            phoneNumberErrorResponse = jsonResp.as[PhoneNumberErrorResponse]
          } recover { case _: Exception => println("could not parse phone number error response") }
          jsonResp
      } recoverWith {
      case err =>
        println(err)
        Future.failed(new Exception(err))
    }

  def callVerifyPasscodeEndpoint(jsonBody: String, headers: (String, String) = headers): Future[JsValue] =
    post(s"$urlVerification$pathPrefix/verify/passcode", jsonBody, headers)
      .collect {
        case r: StandaloneWSResponse if r.status >= 200 && r.status < 300 =>
          val jsonResp = Json.parse(r.body)
          Try {
            verifyPasscodeResponse = jsonResp.as[VerifyPasscodeResponse]
          } recover { case _: Exception => println("could not parse verify passcode response") }
          jsonResp
        case r: StandaloneWSResponse =>
          val jsonResp = Json.parse(r.body)
          Try {
            phoneNumberErrorResponse = jsonResp.as[PhoneNumberErrorResponse]
          } recover { case _: Exception => println("could not parse phone number error response") }
          jsonResp
      } recoverWith {
      case err =>
        println(err)
        Future.failed(new Exception(err))
    }

  def isIndeterminateResponse(body: String): Boolean = {
    body.contains("Indeterminate")
  }
}
