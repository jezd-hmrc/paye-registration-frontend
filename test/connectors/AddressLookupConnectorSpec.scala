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

package connectors

import com.codahale.metrics.Counter
import helpers.mocks.MockMetrics
import helpers.{PayeComponentSpec, PayeFakedApp}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{ForbiddenException, NotFoundException}

import scala.concurrent.Future

class AddressLookupConnectorSpec extends PayeComponentSpec with PayeFakedApp {

  val mockMetrics = new MockMetrics

  class Setup extends CodeMocks {
    val testConnector: AddressLookupConnector = new AddressLookupConnector(
      mockMetrics,
      mockWSHttp
    ) {
      override lazy val addressLookupFrontendUrl = "testBusinessRegUrl"
      override val successCounter: Counter = mockMetrics.addressLookupSuccessResponseCounter
      override val failedCounter: Counter = mockMetrics.addressLookupFailedResponseCounter
    }
  }

  val testAddress: JsObject = Json.obj("x" -> "y")

  "getAddress" should {
    "return an address response" in new Setup {
      mockHttpGet[JsObject](testConnector.addressLookupFrontendUrl, testAddress)

      await(testConnector.getAddress("123")) mustBe testAddress
    }

    "return a Not Found response" in new Setup {
      when(mockWSHttp.GET[JsObject](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NotFoundException("Bad request")))

      intercept[NotFoundException](await(testConnector.getAddress("123")))
    }

    "return a Forbidden response when a CurrentProfile record can not be accessed by the user" in new Setup {
      when(mockWSHttp.GET[JsObject](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new ForbiddenException("Forbidden")))

      intercept[ForbiddenException](await(testConnector.getAddress("321")))
    }

    "return an Exception response when an unspecified error has occurred" in new Setup {
      when(mockWSHttp.GET[JsObject](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new IndexOutOfBoundsException("other exception")))

      intercept[IndexOutOfBoundsException](await(testConnector.getAddress("321")))
    }
  }
}
