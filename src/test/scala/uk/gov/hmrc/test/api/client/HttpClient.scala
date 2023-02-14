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

package uk.gov.hmrc.test.api.client

import akka.actor.ActorSystem
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.{AhcCurlRequestLogger, StandaloneAhcWSClient}
import play.api.libs.ws.{DefaultWSProxyServer, StandaloneWSRequest}
import uk.gov.hmrc.test.api.conf.TestConfiguration

import scala.concurrent.{ExecutionContext, Future}

trait HttpClient {

  implicit val actorSystem: ActorSystem = ActorSystem()
  val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()
  implicit val ec: ExecutionContext = ExecutionContext.global

  private def req(url: String, headers: (String, String)*) = {
    val req = wsClient.url(url)
      .withRequestFilter(AhcCurlRequestLogger())
      .withHttpHeaders(headers: _*)

    if (TestConfiguration.useZap) {
      val PROXY_PORT = 11000
      req.withProxyServer(DefaultWSProxyServer("localhost", PROXY_PORT))
    } else {
      req
    }
  }

  def get(url: String, headers: (String, String)*): Future[StandaloneWSRequest#Self#Response] = {
    req(url, headers: _*).get
  }

  def post(url: String, bodyAsJson: String, headers: (String, String)*): Future[StandaloneWSRequest#Self#Response] =
    req(url, headers: _*).post(bodyAsJson)
}
