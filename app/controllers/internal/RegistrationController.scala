/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.internal

import javax.inject.Inject

import connectors.{KeystoreConnector, PAYERegistrationConnector}
import controllers.{AuthRedirectUrls, PayeBaseController}
import enums.RegistrationDeletion
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{CompanyDetailsService, IncorporationInformationService, PAYERegistrationService, S4LService}
import uk.gov.hmrc.auth.core.AuthConnector

class RegistrationControllerImpl @Inject()(val keystoreConnector: KeystoreConnector,
                                           val payeRegistrationConnector: PAYERegistrationConnector,
                                           val authConnector: AuthConnector,
                                           val messagesApi: MessagesApi,
                                           val companyDetailsService: CompanyDetailsService,
                                           val s4LService: S4LService,
                                           val config: Configuration,
                                           val incorpInfoService: IncorporationInformationService,
                                           val payeRegistrationService: PAYERegistrationService) extends RegistrationController with AuthRedirectUrls

trait RegistrationController extends PayeBaseController {
  val payeRegistrationConnector: PAYERegistrationConnector
  val payeRegistrationService: PAYERegistrationService

  def delete(regId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      payeRegistrationService.deletePayeRegistrationInProgress(regId)(hc) map {
        case RegistrationDeletion.success       => Ok
        case RegistrationDeletion.invalidStatus => PreconditionFailed
        case RegistrationDeletion.forbidden     =>
          logger.warn(s"[RegistrationController] [delete] - Requested document regId $regId to be deleted is not corresponding to the CurrentProfile regId")
          BadRequest
      } recover {
        case ex: Exception =>
          logger.error(s"[RegistrationController] [delete] - Received an error when deleting Registration regId: $regId - error: ${ex.getMessage}")
          InternalServerError
      }
    } recover {
      case _ =>
        logger.warn(s"[RegistrationController] [delete] - Can't get the Authority")
        Unauthorized
    }
  }
}
