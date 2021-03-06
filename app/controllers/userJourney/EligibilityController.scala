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

import scala.concurrent.Future

class EligibilityControllerImpl @Inject()(val keystoreConnector: KeystoreConnector,
                                          val authConnector: AuthConnector,
                                          val s4LService: S4LService,
                                          val companyDetailsService: CompanyDetailsService,
                                          val incorpInfoService: IncorporationInformationService,
                                          val incorporationInformationConnector: IncorporationInformationConnector,
                                          val payeRegistrationService: PAYERegistrationService,
                                          mcc: MessagesControllerComponents
                                         )(val appConfig: AppConfig) extends EligibilityController(mcc) with AuthRedirectUrls {
}

abstract class EligibilityController(mcc: MessagesControllerComponents) extends PayeBaseController(mcc) {
  val appConfig: AppConfig
  val config = appConfig.servicesConfig

  val compRegFEURL: String
  val compRegFEURI: String

  def questionnaire: Action[AnyContent] = isAuthorisedWithProfile { implicit request =>
    _ =>
      Future.successful(Redirect(s"$compRegFEURL$compRegFEURI/questionnaire"))
  }
}
