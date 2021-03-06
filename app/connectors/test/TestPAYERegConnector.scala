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

package connectors.test

import common.Logging
import config.{AppConfig, WSHttp}
import connectors._
import enums.DownstreamOutcome
import javax.inject.Inject
import models.api.{Employment, CompanyDetails => CompanyDetailsAPI, PAYEContact => PAYEContactAPI, PAYERegistration => PAYERegistrationAPI}
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{CoreGet, CorePost, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class TestPAYERegConnectorImpl @Inject()(val payeRegConnector: PAYERegistrationConnector,
                                         val http: WSHttp,
                                         appConfig: AppConfig) extends TestPAYERegConnector {
  val payeRegUrl = appConfig.servicesConfig.baseUrl("paye-registration")
}

trait TestPAYERegConnector extends Logging {

  val payeRegUrl: String
  val http: CoreGet with CorePost
  val payeRegConnector: PAYERegistrationConnector

  def addPAYERegistration(reg: PAYERegistrationAPI)(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    http.POST[PAYERegistrationAPI, HttpResponse](s"$payeRegUrl/paye-registration/test-only/update-registration/${reg.registrationID}", reg) map {
      _.status match {
        case Status.OK => DownstreamOutcome.Success
        case _ => DownstreamOutcome.Failure
      }
    }
  }

  def addTestCompanyDetails(details: CompanyDetailsAPI, regId: String)(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    payeRegConnector.upsertCompanyDetails(regId, details) map {
      _ => DownstreamOutcome.Success
    } recover {
      case _ => DownstreamOutcome.Failure
    }
  }

  def addTestPAYEContact(details: PAYEContactAPI, regId: String)(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    payeRegConnector.upsertPAYEContact(regId, details) map {
      _ => DownstreamOutcome.Success
    } recover {
      case _ => DownstreamOutcome.Failure
    }
  }

  def addTestEmploymentInfo(details: Employment, regId: String)(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    payeRegConnector.upsertEmployment(regId, details) map {
      _ => DownstreamOutcome.Success
    } recover {
      case _ => DownstreamOutcome.Failure
    }
  }

  def testRegistrationTeardown()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    http.GET[HttpResponse](s"$payeRegUrl/paye-registration/test-only/registration-teardown") map {
      resp => DownstreamOutcome.Success
    } recover {
      case e: Exception =>
        logger.warn(s"[PAYERegistrationConnector] [testRegistrationTeardown] received error when clearing registration details - Error: ${e.getMessage}")
        DownstreamOutcome.Failure
    }
  }

  def tearDownIndividualRegistration(regId: String)(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    http.GET[HttpResponse](s"$payeRegUrl/paye-registration/test-only/delete-registration/$regId") map {
      resp => DownstreamOutcome.Success
    } recover {
      case e: Exception =>
        logger.warn(s"[PAYERegistrationConnector] [tearDownIndividualRegistration] received error when clearing registration details - Error: ${e.getMessage}")
        DownstreamOutcome.Failure
    }
  }

  def updateStatus(regId: String, status: String)(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] = {
    http.POST[JsObject, HttpResponse](s"$payeRegUrl/paye-registration/test-only/update-status/$regId/$status", Json.obj()) map {
      resp => DownstreamOutcome.Success
    } recover {
      case e: Exception =>
        logger.warn(s"[PAYERegistrationConnector] [updateStatus] received error when updating status details - Error: ${e.getMessage}")
        DownstreamOutcome.Failure
    }
  }
}
