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

package controllers.userJourney

import javax.inject.{Inject, Singleton}

import auth.PAYERegime
import config.FrontendAuthConnector
import forms.completionCapacity.CompletionCapacityForm
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.{CompletionCapacityService, CompletionCapacitySrv}
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.pages.{completionCapacity => CompletionCapacityView}

import scala.concurrent.Future

@Singleton
class CompletionCapacityController @Inject()(injMessagesApi: MessagesApi, injCompletionCapacityService: CompletionCapacityService) extends CompletionCapacityCtrl {
  val authConnector = FrontendAuthConnector
  val messagesApi = injMessagesApi
  val completionCapacityService = injCompletionCapacityService
}

trait CompletionCapacityCtrl extends FrontendController with Actions with I18nSupport {

  val completionCapacityService: CompletionCapacitySrv

  val completionCapacity : Action[AnyContent] = AuthorisedFor(taxRegime = new PAYERegime, pageVisibility = GGConfidence).async {
    implicit user =>
      implicit request =>
        completionCapacityService.getCompletionCapacity map {
          case Some(capacity) => Ok(CompletionCapacityView(CompletionCapacityForm.form.fill(capacity)))
          case None => Ok(CompletionCapacityView(CompletionCapacityForm.form))
        }
  }

  val submitCompletionCapacity : Action[AnyContent] = AuthorisedFor(taxRegime = new PAYERegime, pageVisibility = GGConfidence).async {
    implicit user =>
      implicit request =>
        CompletionCapacityForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(CompletionCapacityView(errors))),
          success => {
            completionCapacityService.saveCompletionCapacity(success) map {
              _ => Redirect(routes.CompanyDetailsController.tradingName())
            }
          }
        )
  }
}
