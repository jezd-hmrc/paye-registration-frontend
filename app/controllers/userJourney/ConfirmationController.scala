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

import config.AppConfig
import connectors.{IncorporationInformationConnector, KeystoreConnector}
import controllers.{AuthRedirectUrls, PayeBaseController}
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.pages.{confirmation => ConfirmationPage}

import scala.concurrent.ExecutionContext.Implicits.global

class ConfirmationControllerImpl @Inject()(val keystoreConnector: KeystoreConnector,
                                           val confirmationService: ConfirmationService,
                                           val s4LService: S4LService,
                                           val companyDetailsService: CompanyDetailsService,
                                           val incorpInfoService: IncorporationInformationService,
                                           val emailService: EmailService,
                                           val authConnector: AuthConnector,
                                           val incorporationInformationConnector: IncorporationInformationConnector,
                                           val payeRegistrationService: PAYERegistrationService,
                                           mcc: MessagesControllerComponents
                                          )(implicit val appConfig: AppConfig) extends ConfirmationController(mcc) with AuthRedirectUrls

abstract class ConfirmationController(mcc: MessagesControllerComponents) extends PayeBaseController(mcc) {
  implicit val appConfig: AppConfig
  val confirmationService: ConfirmationService
  val emailService: EmailService
  val s4LService: S4LService

  def showConfirmation: Action[AnyContent] = isAuthorisedWithProfileNoSubmissionCheck { implicit request =>
    profile =>
      (for {
        refs <- confirmationService.getAcknowledgementReference(profile.registrationID)
        _ <- emailService.sendAcknowledgementEmail(profile, refs.get)
        _ <- s4LService.clear(profile.registrationID)
      } yield refs.fold(InternalServerError(views.html.pages.error.restart())) {
        ref => Ok(ConfirmationPage(ref, confirmationService.determineIfInclusiveContentIsShown))
      }).recover {
        case _ => InternalServerError(views.html.pages.error.restart())
      }
  }
}
