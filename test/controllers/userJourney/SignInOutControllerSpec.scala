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

import builders.AuthBuilder
import enums.DownstreamOutcome
import helpers.PAYERegSpec
import services.{CoHoAPIService, CurrentProfileService}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Matchers
import org.mockito.Mockito._

class SignInOutControllerSpec extends PAYERegSpec {

  val mockCurrentProfileService = mock[CurrentProfileService]
  val mockCoHoAPIService = mock[CoHoAPIService]

  class Setup {
    val controller = new SignInOutController {
      override val authConnector = mockAuthConnector
      override val currentProfileService = mockCurrentProfileService
      override val coHoAPIService = mockCoHoAPIService
    }
  }

  val fakeRequest = FakeRequest("GET", "/")


  "Calling the postSignIn action" should {
    "redirect to Trading Name page for an authorised user with a registration ID" in new Setup {
      when(mockCurrentProfileService.fetchAndStoreCurrentProfile(Matchers.any())).thenReturn(DownstreamOutcome.Success)
      when(mockCoHoAPIService.fetchAndStoreCoHoCompanyDetails(Matchers.any())).thenReturn(DownstreamOutcome.Success)

      AuthBuilder.showWithAuthorisedUser(controller.postSignIn, mockAuthConnector) {
        result =>
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${controllers.userJourney.routes.CompanyDetailsController.tradingName()}")
      }
    }

    "show an Error page for an authorised user without a registration ID" in new Setup {
      when(mockCurrentProfileService.fetchAndStoreCurrentProfile(Matchers.any())).thenReturn(DownstreamOutcome.Failure)

      AuthBuilder.showWithAuthorisedUser(controller.postSignIn, mockAuthConnector) {
        result =>
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "show an Error page for an authorised user with a registration ID but no CoHo Company Details" in new Setup {
      when(mockCurrentProfileService.fetchAndStoreCurrentProfile(Matchers.any())).thenReturn(DownstreamOutcome.Success)
      when(mockCoHoAPIService.fetchAndStoreCoHoCompanyDetails(Matchers.any())).thenReturn(DownstreamOutcome.Failure)

      AuthBuilder.showWithAuthorisedUser(controller.postSignIn, mockAuthConnector) {
        result =>
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }

}
