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

package uk.gov.hmrc.test.api.testdata

import play.api.libs.json.{JsObject, Json}

trait JourneyTestData:

  val journeyConfigWithNoPhoneNumber: JsObject = Json.obj(
    "credentialId" -> "123456789",
    "options" -> Json.obj(
      "serviceUrl"                -> "http://test-service-url",
      "completionUrl"             -> "http://test-completion-url",
      "signOutUrl"                -> "http://test-sign-out-url",
      "accessibilityStatementUrl" -> "http://test-accessibility-url"
    )
  )

  val journeyConfigWithPhoneNumber: JsObject =
    journeyConfigWithNoPhoneNumber ++ Json.obj(
      "phoneNumberToVerify" -> "+0700123456789"
    )
