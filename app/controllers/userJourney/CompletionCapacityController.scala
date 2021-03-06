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
import forms.completionCapacity.CompletionCapacityForm
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.pages.{completionCapacity => CompletionCapacityView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompletionCapacityControllerImpl @Inject()(val completionCapacityService: CompletionCapacityService,
                                                 val keystoreConnector: KeystoreConnector,
                                                 val s4LService: S4LService,
                                                 val companyDetailsService: CompanyDetailsService,
                                                 val incorpInfoService: IncorporationInformationService,
                                                 val authConnector: AuthConnector,
                                                 val incorporationInformationConnector: IncorporationInformationConnector,
                                                 val payeRegistrationService: PAYERegistrationService,
                                                 mcc: MessagesControllerComponents
                                                )(implicit val appConfig: AppConfig) extends CompletionCapacityController(mcc) with AuthRedirectUrls

abstract class CompletionCapacityController(mcc: MessagesControllerComponents) extends PayeBaseController(mcc) {
  implicit val appConfig: AppConfig
  val completionCapacityService: CompletionCapacityService

  def completionCapacity: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    profile =>
      completionCapacityService.getCompletionCapacity(profile.registrationID) map {
        case Some(capacity) => Ok(CompletionCapacityView(CompletionCapacityForm.form.fill(capacity)))
        case None => Ok(CompletionCapacityView(CompletionCapacityForm.form))
      }
  }

  def submitCompletionCapacity: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    profile =>
      CompletionCapacityForm.form.bindFromRequest.fold(
        errors => Future.successful(BadRequest(CompletionCapacityView(errors))),
        success => {
          completionCapacityService.saveCompletionCapacity(profile.registrationID, success) map {
            _ => Redirect(routes.CompanyDetailsController.tradingName())
          }
        }
      )
  }
}
