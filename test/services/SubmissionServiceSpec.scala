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

package services

import connectors.{Failed, Success, TimedOut}
import enums.CacheKeys
import helpers.PayeComponentSpec
import models.external.{CompanyRegistrationProfile, CurrentProfile}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SubmissionServiceSpec extends PayeComponentSpec with GuiceOneAppPerSuite {

  class Setup extends CodeMocks {
    val service = new SubmissionService {
      override val payeRegistrationConnector = mockPAYERegConnector
      override val keystoreConnector         = mockKeystoreConnector
    }
  }

  val regId = "12345"

  def currentProfile(regId: String) = CurrentProfile(
    registrationID = regId,
    companyTaxRegistration = CompanyRegistrationProfile(
      status = "acknowledged",
      transactionId = "40-123456"
    ),
    language = "ENG",
    payeRegistrationSubmitted = false,
    None
  )

  "submitRegistration" should {
    "return a Success DES Response" in new Setup {
      when(mockPAYERegConnector.submitRegistration(ArgumentMatchers.eq(regId))(ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(Success))

      mockKeystoreCache(CacheKeys.CurrentProfile.toString, CacheMap("CurrentProfile", Map.empty))

      when(mockKeystoreConnector.cache(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("CurrentProfile", Map.empty)))

      await(service.submitRegistration(currentProfile(regId))) mustBe Success
    }

    "return a Failed DES Response" in new Setup {
      when(mockPAYERegConnector.submitRegistration(ArgumentMatchers.eq(regId))(ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(Failed))

      await(service.submitRegistration(currentProfile(regId))) mustBe Failed
    }

    "return a TimedOut DES Response" in new Setup {
      when(mockPAYERegConnector.submitRegistration(ArgumentMatchers.eq(regId))(ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(TimedOut))

      await(service.submitRegistration(currentProfile(regId))) mustBe TimedOut
    }
  }
}