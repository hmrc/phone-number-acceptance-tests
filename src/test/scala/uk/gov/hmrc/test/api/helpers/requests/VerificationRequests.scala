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

import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.test.api.client.HttpClient
import uk.gov.hmrc.test.api.conf.TestConfiguration
import uk.gov.hmrc.test.api.helpers.common.JsonRequests
import uk.gov.hmrc.test.api.helpers.verify.VerificationResponses.{indeterminateResponse, phoneNumberErrorResponse, verifyResponse}
import uk.gov.hmrc.test.api.helpers.verifyOtp.OtpVerificationResponses.otpResponse
import uk.gov.hmrc.test.api.models.common.PhoneNumberErrorResponse
import uk.gov.hmrc.test.api.models.verify.{IndeterminateResponse, VerifyResponse}
import uk.gov.hmrc.test.api.models.otp.OtpResponse

import scala.concurrent.Future
import scala.util.Try

object VerificationRequests extends HttpClient with JsonRequests {

  private val urlVerification = TestConfiguration.url("cip-phone-number")

  def callVerifyEndpoint(jsonBody: String, headers: (String, String) = headers): Future[JsValue] =
    post(s"$urlVerification$pathPrefix/verify", jsonBody, headers)
      .collect {
        case r: StandaloneWSResponse if r.status >= 200 && r.status < 300 =>
          val jsonResp: JsValue = Json.parse(r.body)
          Try {
            if (isIndeterminateResponse(jsonResp)) {
              indeterminateResponse = jsonResp.as[IndeterminateResponse]
            } else {
              verifyResponse = jsonResp.as[VerifyResponse]
            }
          } recover { case _: Exception => println("could not parse phone number") }
          jsonResp
        case r: StandaloneWSResponse =>
          val jsonResp = Json.parse(r.body)
          phoneNumberErrorResponse = jsonResp.as[PhoneNumberErrorResponse]
          jsonResp
      } recoverWith {
      case _ => Future.failed(new Exception)
    }

  def callVerifyOtpEndpoint(jsonBody: String, headers: (String, String) = headers): Future[JsValue] =
    post(s"$urlVerification$pathPrefix/verify/otp", jsonBody, headers)
      .collect {
        case r: StandaloneWSResponse if r.status >= 200 && r.status < 300 =>
          val jsonResp = Json.parse(r.body)
          Try {
            otpResponse = jsonResp.as[OtpResponse]
          } recover { case _: Exception => println("could not parse verify otp response") }
          jsonResp
        case r: StandaloneWSResponse =>
          val jsonResp = Json.parse(r.body)
          phoneNumberErrorResponse = jsonResp.as[PhoneNumberErrorResponse]
          jsonResp
      } recoverWith {
      case _ => Future.failed(new Exception)
    }

  def isIndeterminateResponse(jsValue: JsValue): Boolean = {
    val fields = jsValue.asInstanceOf[JsObject].fields
    if (fields.size == 2 && "status".equals(fields.head._1) && "message".equals(fields(1)._1)) {
      true
    } else {
      false
    }
  }
}
