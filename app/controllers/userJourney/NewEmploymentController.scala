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

package controllers.userJourney

import javax.inject.Inject

import connectors.KeystoreConnector
import controllers.{AuthRedirectUrls, PayeBaseController}
import forms.employmentDetails._
import models.view.{EmployingStaff, Subcontractors}
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{CompanyDetailsService, EmploymentService, IncorporationInformationService, S4LService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.pages.employmentDetails.{companyPension => CompanyPensionPage, employingStaff => EmployingStaffPage, firstPayment => FirstPaymentPage, subcontractors => SubcontractorsPage}

import scala.concurrent.Future

class EmploymentControllerImpl @Inject()(val employmentService: EmploymentService,
                                         val keystoreConnector: KeystoreConnector,
                                         val config: Configuration,
                                         val authConnector: AuthConnector,
                                         val s4LService: S4LService,
                                         val companyDetailsService: CompanyDetailsService,
                                         val incorpInfoService: IncorporationInformationService,
                                         implicit val messagesApi: MessagesApi) extends EmploymentController with AuthRedirectUrls


trait EmploymentController extends PayeBaseController {
  val employmentService: EmploymentService


  // PAID EMPLOYEES
  def paidEmployees: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    profile =>
      employmentService.fetchEmploymentView(profile.registrationID) map {
        _.employing match {
          case Some(model) => Ok(EmployingStaffPage(EmployingStaffForm.form.fill(model)))
          case _ => Ok(EmployingStaffPage(EmployingStaffForm.form))
        }
      }
  }

  def submitPaidEmployees: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    profile =>
      EmployingStaffForm.form.bindFromRequest.fold(
        errors => Future.successful(BadRequest(EmployingStaffPage(errors))),
        model => employmentService.saveEmployingStaff(model, profile.registrationID) map { model =>
          (model.employing, model.subcontractors) match {
            case (Some(EmployingStaff(false)), Some(Subcontractors(false))) => Redirect(controllers.errors.routes.ErrorController.ineligible())
            case (Some(EmployingStaff(true)), _) => Redirect(controllers.userJourney.routes.EmploymentController.companyPension())
            case _ => Redirect(controllers.userJourney.routes.EmploymentController.firstPayment())
          }
        }
      )
    }

  // CONSTRUCTION INDUSTRY
  def constructionIndustry: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    profile =>
      employmentService.fetchEmploymentView(profile.registrationID) map {
        _.employing match {
          case Some(model) => Ok(EmployingStaffPage(EmployingStaffForm.form.fill(model)))
          case _ => Ok(EmployingStaffPage(EmployingStaffForm.form))
        }
      }
  }

  def submitConstructionIndustry: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    profile =>
      EmployingStaffForm.form.bindFromRequest.fold(
        errors => Future.successful(BadRequest(EmployingStaffPage(errors))),
        model => employmentService.saveEmployingStaff(model, profile.registrationID) map { model =>
          (model.employing, model.subcontractors) match {
            case (Some(EmployingStaff(false)), Some(Subcontractors(false))) => Redirect(controllers.errors.routes.ErrorController.ineligible())
            case (Some(EmployingStaff(true)), _) => Redirect(controllers.userJourney.routes.EmploymentController.companyPension())
            case _ => Redirect(controllers.userJourney.routes.EmploymentController.firstPayment())
          }
        }
      )
  }

  // APPLICATION DELAYED
  def applicationDelayed: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    profile =>
      employmentService.fetchEmploymentView(profile.registrationID) map {
        _.employing match {
          case Some(model) => Ok(EmployingStaffPage(EmployingStaffForm.form.fill(model)))
          case _ => Ok(EmployingStaffPage(EmployingStaffForm.form))
        }
      }
  }

  def submitApplicationDelayed: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    profile =>
      EmployingStaffForm.form.bindFromRequest.fold(
        errors => Future.successful(BadRequest(EmployingStaffPage(errors))),
        model => employmentService.saveEmployingStaff(model, profile.registrationID) map { model =>
          (model.employing, model.subcontractors) match {
            case (Some(EmployingStaff(false)), Some(Subcontractors(false))) => Redirect(controllers.errors.routes.ErrorController.ineligible())
            case (Some(EmployingStaff(true)), _) => Redirect(controllers.userJourney.routes.EmploymentController.companyPension())
            case _ => Redirect(controllers.userJourney.routes.EmploymentController.firstPayment())
          }
        }
      )
    }
  }
