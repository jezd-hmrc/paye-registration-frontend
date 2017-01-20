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

import uk.gov.hmrc.play.frontend.controller.FrontendController
import config.FrontendAuthConnector
import auth.PAYERegime
import forms.employmentDetails._

import scala.concurrent.Future
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.auth.Actions
import views.html.pages.employmentDetails.{employingStaff => EmployingStaffPage, subcontractors => SubcontractorsPage}

object EmploymentController extends EmploymentController {
  //$COVERAGE-OFF$
  override val authConnector = FrontendAuthConnector
  //$COVERAGE-ON$
}

trait EmploymentController extends FrontendController with Actions {
  val employingStaff = AuthorisedFor(taxRegime = new PAYERegime, pageVisibility = GGConfidence).async { implicit user => implicit request =>
    Future.successful(Ok(EmployingStaffPage(EmployingStaffForm.form)))
  }

  val submitEmployingStaff = AuthorisedFor(taxRegime = new PAYERegime, pageVisibility = GGConfidence).async { implicit user => implicit request =>
    Future.successful(
      EmployingStaffForm.form.bindFromRequest.fold(
        errors => BadRequest(EmployingStaffPage(errors)),
        model => model.currentYear match {
          case true => Redirect(controllers.userJourney.routes.SummaryController.summary()) // Redirect to Pension Scheme
          case false => Redirect(controllers.userJourney.routes.SummaryController.summary()) // Redirect to Subcontractors
        }
      )
    )
  }

  val subcontractors = AuthorisedFor(taxRegime = new PAYERegime, pageVisibility = GGConfidence).async { implicit user => implicit request =>
    Future.successful(Ok(SubcontractorsPage(SubcontractorsForm.form)))
  }

  val submitSubcontractors = AuthorisedFor(taxRegime = new PAYERegime, pageVisibility = GGConfidence).async { implicit user => implicit request =>
    Future.successful(
      SubcontractorsForm.form.bindFromRequest.fold(
        errors => BadRequest(SubcontractorsPage(errors)),
        model => model.hasContractors match {
          case true => Redirect(controllers.userJourney.routes.SummaryController.summary()) // Redirect to Pension Scheme
          case false => Redirect(controllers.userJourney.routes.SummaryController.summary()) // Redirect to Subcontractors
        }
      )
    )
  }
}
