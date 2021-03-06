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

package controllers.test

import config.AppConfig
import connectors._
import connectors.test.TestBusinessRegConnector
import controllers.{AuthRedirectUrls, PayeBaseController}
import javax.inject.Inject
import models.external.BusinessProfile
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import services.{CompanyDetailsService, IncorporationInformationService, PAYERegistrationService, S4LService}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessProfileControllerImpl @Inject()(val keystoreConnector: KeystoreConnector,
                                              val businessRegConnector: BusinessRegistrationConnector,
                                              val authConnector: AuthConnector,
                                              val s4LService: S4LService,
                                              val companyDetailsService: CompanyDetailsService,
                                              val incorpInfoService: IncorporationInformationService,
                                              val testBusinessRegConnector: TestBusinessRegConnector,
                                              val incorporationInformationConnector: IncorporationInformationConnector,
                                              val payeRegistrationService: PAYERegistrationService,
                                              mcc: MessagesControllerComponents
                                             )(val appConfig: AppConfig) extends BusinessProfileController(mcc) with AuthRedirectUrls

abstract class BusinessProfileController(mcc: MessagesControllerComponents) extends PayeBaseController(mcc) {
  val appConfig: AppConfig
  val businessRegConnector: BusinessRegistrationConnector
  val testBusinessRegConnector: TestBusinessRegConnector

  def businessProfileSetup = isAuthorised { implicit request =>
    doBusinessProfileSetup map { res =>
      Ok(res.toString)
    }
  }

  protected[controllers] def doBusinessProfileSetup(implicit request: Request[AnyContent]): Future[BusinessProfile] = {
    businessRegConnector.retrieveCurrentProfile
      .recoverWith { case _ => testBusinessRegConnector.createBusinessProfileEntry }
  }
}
