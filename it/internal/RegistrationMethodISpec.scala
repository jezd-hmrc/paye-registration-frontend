/*
 * Copyright 2017 HM Revenue & Customs
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

package internal

import itutil.{CachingStub, IntegrationSpecBase, LoginStub, WiremockHelper}
import org.scalatest.BeforeAndAfterEach
import play.api.http.HeaderNames
import play.api.test.FakeApplication

class RegistrationMethodISpec extends IntegrationSpecBase
                          with LoginStub
                          with CachingStub
                          with BeforeAndAfterEach
                          with WiremockHelper {
  val mockHost = WiremockHelper.wiremockHost
  val mockPort = WiremockHelper.wiremockPort
  val mockUrl = s"http://$mockHost:$mockPort"

  override implicit lazy val app = FakeApplication(additionalConfiguration = Map(
    "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "play.filters.csrf.header.bypassHeaders.Csrf-Bypass" -> "nocheck",
    "auditing.consumer.baseUri.host" -> s"$mockHost",
    "auditing.consumer.baseUri.port" -> s"$mockPort",
    "microservice.services.cachable.session-cache.host" -> s"$mockHost",
    "microservice.services.cachable.session-cache.port" -> s"$mockPort",
    "microservice.services.cachable.session-cache.domain" -> "keystore",
    "microservice.services.cachable.short-lived-cache.host" -> s"$mockHost",
    "microservice.services.cachable.short-lived-cache.port" -> s"$mockPort",
    "microservice.services.cachable.short-lived-cache.domain" -> "save4later",
    "microservice.services.auth.host" -> s"$mockHost",
    "microservice.services.auth.port" -> s"$mockPort",
    "microservice.services.paye-registration.host" -> s"$mockHost",
    "microservice.services.paye-registration.port" -> s"$mockPort"
  ))

  override def beforeEach() {
    resetWiremock()
  }

  val regId = "3"
  val companyName = "Foo Ltd"

  "DELETE registration" should {
    "return 403 if the Registration ID requested for delete is not same as the CurrentProfile" in {
      setupSimpleAuthMocks()

      stubSuccessfulLogin()

      stubKeystoreMetadata(SessionId, regId, companyName)

      val fResponse = buildClientInternal(s"/4/delete").
        withHeaders(HeaderNames.COOKIE -> getSessionCookie()).
        delete()

      val response = await(fResponse)

      response.status shouldBe 403
    }

    "return 412 if the Registration is not in Draft or Rejected status for deletion" in {
      setupSimpleAuthMocks()

      stubSuccessfulLogin()

      stubKeystoreMetadata(SessionId, regId, companyName)

      stubDelete(s"/save4later/paye-registration-frontend/${regId}", 200, "")

      stubDelete(s"/paye-registration/${regId}/delete-in-progress", 412, "")

      val fResponse = buildClientInternal(s"/$regId/delete").
        withHeaders(HeaderNames.COOKIE -> getSessionCookie()).
        delete()

      val response = await(fResponse)

      response.status shouldBe 412
    }

    "return 404 if the Registration is not found" in {
      setupSimpleAuthMocks()

      stubSuccessfulLogin()

      stubKeystoreMetadata(SessionId, regId, companyName)

      stubDelete(s"/save4later/paye-registration-frontend/${regId}", 200, "")

      stubDelete(s"/paye-registration/${regId}/delete-in-progress", 404, "")

      val fResponse = buildClientInternal(s"/$regId/delete").
        withHeaders(HeaderNames.COOKIE -> getSessionCookie()).
        delete()

      val response = await(fResponse)

      response.status shouldBe 404
    }

    "return 500 if PAYERegistration microservice returns 403" in {
      setupSimpleAuthMocks()

      stubSuccessfulLogin()

      stubKeystoreMetadata(SessionId, regId, companyName)

      stubDelete(s"/save4later/paye-registration-frontend/${regId}", 200, "")

      stubDelete(s"/paye-registration/${regId}/delete-in-progress", 403, "")

      val fResponse = buildClientInternal(s"/$regId/delete").
        withHeaders(HeaderNames.COOKIE -> getSessionCookie()).
        delete()

      val response = await(fResponse)

      response.status shouldBe 500
    }

    "return 500 if PAYERegistration microservice fail to delete Registration" in {
      setupSimpleAuthMocks()

      stubSuccessfulLogin()

      stubKeystoreMetadata(SessionId, regId, companyName)

      stubDelete(s"/save4later/paye-registration-frontend/${regId}", 200, "")

      stubDelete(s"/paye-registration/${regId}/delete-in-progress", 500, "")

      val fResponse = buildClientInternal(s"/$regId/delete").
        withHeaders(HeaderNames.COOKIE -> getSessionCookie()).
        delete()

      val response = await(fResponse)

      response.status shouldBe 500
    }

    "return 500 if S4L fail to clear Registration" in {
      setupSimpleAuthMocks()

      stubSuccessfulLogin()

      stubKeystoreMetadata(SessionId, regId, companyName)

      stubDelete(s"/save4later/paye-registration-frontend/${regId}", 500, "")

      val fResponse = buildClientInternal(s"/$regId/delete").
        withHeaders(HeaderNames.COOKIE -> getSessionCookie()).
        delete()

      val response = await(fResponse)

      response.status shouldBe 500
    }
  }
}
