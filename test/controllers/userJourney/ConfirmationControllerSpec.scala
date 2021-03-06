/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.userJourney

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.AppConfig
import connectors.{EmailDifficulties, EmailSent}
import helpers.auth.AuthHelpers
import helpers.{PayeComponentSpec, PayeFakedApp}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class ConfirmationControllerSpec extends PayeComponentSpec with PayeFakedApp {

  class Setup extends AuthHelpers {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector

    val controller = new ConfirmationController(stubMessagesControllerComponents()) {
      override val redirectToLogin = MockAuthRedirects.redirectToLogin
      override val redirectToPostSign = MockAuthRedirects.redirectToPostSign
      override val emailService = mockEmailService
      override val s4LService = mockS4LService
      override val authConnector = mockAuthConnector
      override val keystoreConnector = mockKeystoreConnector
      override val confirmationService = mockConfirmationService
      override val incorporationInformationConnector = mockIncorpInfoConnector
      override val payeRegistrationService = mockPayeRegService
      override implicit val appConfig: AppConfig = mockAppConfig
    }
  }

  val successHttpResponse = HttpResponse(status = 200, body = "")

  "showConfirmation" should {
    "display the confirmation page with an acknowledgement reference retrieved from backend with Inclusive content not shown" in new Setup {
      when(mockConfirmationService.getAcknowledgementReference(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("BRPY00000000001")))
      when(mockConfirmationService.determineIfInclusiveContentIsShown) thenReturn false

      when(mockEmailService.sendAcknowledgementEmail(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future(EmailSent))
      when(mockS4LService.clear(any())(any())) thenReturn Future(successHttpResponse)

      showAuthorisedWithCP(controller.showConfirmation, Fixtures.validCurrentProfile, FakeRequest()) {
        result =>
          status(result) mustBe OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementById("ack-ref").html mustBe "BRPY00000000001"
          doc.getElementsByAttributeValueContaining("id", "standard-content").isEmpty mustBe false
          doc.getElementsByAttributeValueContaining("id", "inclusive-content").isEmpty mustBe true
      }
    }

    "display the confirmation page with an acknowledgement reference retrieved from backend even if email fails and inclusive content is shown" in new Setup {
      when(mockConfirmationService.getAcknowledgementReference(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("BRPY00000000001")))
      when(mockConfirmationService.determineIfInclusiveContentIsShown) thenReturn true

      when(mockEmailService.sendAcknowledgementEmail(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future(EmailDifficulties))
      when(mockS4LService.clear(any())(any())) thenReturn Future(successHttpResponse)

      showAuthorisedWithCP(controller.showConfirmation, Fixtures.validCurrentProfile, FakeRequest()) {
        result =>
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementById("ack-ref").html mustBe "BRPY00000000001"
          doc.getElementsByAttributeValueContaining("id", "standard-content").isEmpty mustBe true
          doc.getElementsByAttributeValueContaining("id", "inclusive-content").isEmpty mustBe false
      }
    }

    "show an error page when there is no acknowledgement reference returned from the backend" in new Setup {
      when(mockConfirmationService.getAcknowledgementReference(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(None))

      showAuthorisedWithCP(controller.showConfirmation, Fixtures.validCurrentProfile, FakeRequest()) {
        result =>
          status(result) mustBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }
}