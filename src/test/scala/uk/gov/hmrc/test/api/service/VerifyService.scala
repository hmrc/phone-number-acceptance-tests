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

package uk.gov.hmrc.test.api.service

import play.api.libs.json.Json
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.test.api.client.HttpClientHelper
import uk.gov.hmrc.test.api.conf.TestConfiguration

class VerifyService extends HttpClientHelper {

  private val url = TestConfiguration.url("phone-number-gateway") + "/phone-number-gateway"
  private val headers = Seq(
    "Content-Type" -> "application/json",
    "Accept" -> "application/json",
    "Authorization" -> "fake-token"
  )

  def sendCode(phoneNumber: String): StandaloneWSResponse =
    post(s"$url/send-code", Json.obj("phoneNumber" -> phoneNumber), headers: _*)

  def verifyCode(phoneNumber: String,
                 verificationCode: String): StandaloneWSResponse =
    post(
      s"$url/verify-code",
      Json.obj(
        "phoneNumber" -> phoneNumber,
        "verificationCode" -> verificationCode
      ),
      headers: _*
    )

}
