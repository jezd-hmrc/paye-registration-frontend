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

package connectors

import config.WSHttp
import enums.DownstreamOutcome
import models.api.{PAYERegistration => PAYERegistrationAPI}
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import models.api.{CompanyDetails => CompanyDetailsAPI}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait PAYERegistrationResponse
case class PAYERegistrationSuccessResponse[T](response: T) extends PAYERegistrationResponse
case object PAYERegistrationNotFoundResponse extends PAYERegistrationResponse
case object PAYERegistrationBadRequestResponse extends PAYERegistrationResponse
case object PAYERegistrationForbiddenResponse extends PAYERegistrationResponse
case class PAYERegistrationErrorResponse(err: Exception) extends PAYERegistrationResponse

object PAYERegistrationConnector extends PAYERegistrationConnector with ServicesConfig {
  //$COVERAGE-OFF$
  val payeRegUrl = baseUrl("paye-registration")
  val http = WSHttp
  //$COVERAGE-ON$
}

trait PAYERegistrationConnector {

  val payeRegUrl: String
  val http: HttpGet with HttpPost with HttpPatch

  def createNewRegistration(regID: String)(implicit hc: HeaderCarrier, rds: HttpReads[PAYERegistrationAPI]): Future[DownstreamOutcome.Value] = {
    http.PATCH[String, HttpResponse](s"$payeRegUrl/paye-registration/$regID/new", regID) map {
      response => response.status match {
        case Status.OK => DownstreamOutcome.Success
        case _ => DownstreamOutcome.Failure
      }
    } recover {
      case e: BadRequestException =>
        Logger.warn("[PAYERegistrationConnector] [createNewRegistration] received Bad Request response creating/asserting new PAYE Registration in microservice")
        DownstreamOutcome.Failure
      case e: ForbiddenException =>
        Logger.warn("[PAYERegistrationConnector] [createNewRegistration] received Forbidden response when creating/asserting new PAYE Registration in microservice")
        DownstreamOutcome.Failure
      case e: Exception =>
        Logger.warn(s"[PAYERegistrationConnector] [createNewRegistration] received error when creating/asserting new PAYE Registration in microservice - Error: ${e.getMessage}")
        DownstreamOutcome.Failure
    }
  }

  def getRegistration(regID: String)(implicit hc: HeaderCarrier, rds: HttpReads[PAYERegistrationAPI]): Future[PAYERegistrationResponse] = {
    http.GET[PAYERegistrationAPI](s"$payeRegUrl/paye-registration/$regID") map {
      reg => PAYERegistrationSuccessResponse(reg)
    } recover {
      case e: ForbiddenException =>
        Logger.warn("[PAYERegistrationConnector] [getRegistration] received Forbidden response when fetching completed PAYE Registration from microservice")
        PAYERegistrationForbiddenResponse
      case e: NotFoundException =>
        Logger.warn("[PAYERegistrationConnector] [getRegistration] received Not Found response when fetching completed PAYE Registration from microservice")
        PAYERegistrationNotFoundResponse
      case e: Exception =>
        Logger.warn(s"[PAYERegistrationConnector] [getRegistration] received error when fetching completed PAYE Registration from microservice - Error: ${e.getMessage}")
        PAYERegistrationErrorResponse(e)
    }
  }

  def getCompanyDetails(regID: String)(implicit hc: HeaderCarrier, rds: HttpReads[CompanyDetailsAPI]): Future[PAYERegistrationResponse] = {
    http.GET[CompanyDetailsAPI](s"$payeRegUrl/paye-registration/$regID/company-details") map {
      details => PAYERegistrationSuccessResponse[CompanyDetailsAPI](details)
    } recover {
      case e: NotFoundException =>
        PAYERegistrationNotFoundResponse
      case e: BadRequestException =>
        Logger.warn("[PAYERegistrationConnector] [getCompanyDetails] received Bad Request response when getting company details from microservice")
        PAYERegistrationBadRequestResponse
      case e: ForbiddenException =>
        Logger.warn("[PAYERegistrationConnector] [getCompanyDetails] received Forbidden response when getting company details from microservice")
        PAYERegistrationForbiddenResponse
      case e: Exception =>
        Logger.warn(s"[PAYERegistrationConnector] [getCompanyDetails] received error when getting company details from microservice - Error: ${e.getMessage}")
        PAYERegistrationErrorResponse(e)
    }
  }

  def upsertCompanyDetails(regID: String, companyDetails: CompanyDetailsAPI)(implicit hc: HeaderCarrier, rds: HttpReads[CompanyDetailsAPI]): Future[PAYERegistrationResponse] = {
    http.PATCH[CompanyDetailsAPI, CompanyDetailsAPI](s"$payeRegUrl/paye-registration/$regID/company-details", companyDetails) map {
      details => PAYERegistrationSuccessResponse[CompanyDetailsAPI](details)
    } recover {
      case e: NotFoundException =>
        Logger.warn("[PAYERegistrationConnector] [upsertCompanyDetails] received Not Found response when upserting company details in microservice")
        PAYERegistrationNotFoundResponse
      case e: ForbiddenException =>
        Logger.warn("[PAYERegistrationConnector] [upsertCompanyDetails] received Forbidden response when upserting company details in microservice")
        PAYERegistrationForbiddenResponse
      case e: Exception =>
        Logger.warn(s"[PAYERegistrationConnector] [upsertCompanyDetails] received error when upserting company details in microservice - Error: ${e.getMessage}")
        PAYERegistrationErrorResponse(e)
    }
  }

}