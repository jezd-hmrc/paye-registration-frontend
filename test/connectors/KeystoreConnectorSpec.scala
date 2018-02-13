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

package connectors

import helpers.PayeComponentSpec
import helpers.mocks.MockMetrics
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class KeystoreConnectorSpec extends PayeComponentSpec {

  val connector = new KeystoreConnector {
    override val metricsService = new MockMetrics
    override val sessionCache = mockSessionCache
    override val successCounter = metricsService.keystoreSuccessResponseCounter
    override val failedCounter = metricsService.keystoreFailedResponseCounter
    override val emptyResponseCounter = metricsService.keystoreFailedResponseCounter
    override def timer = metricsService.keystoreResponseTimer.time()
  }

  case class TestModel(test: String)
  object TestModel {
    implicit val formats = Json.format[TestModel]
  }

  "Saving into KeyStore" should {
    "save the model" in {
      val testModel = TestModel("test")

      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(testModel)))

      when(mockSessionCache.cache[TestModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      val result = await(connector.cache[TestModel]("testKey", testModel))
      result mustBe returnCacheMap
    }
  }

  "Fetching and getting from KeyStore" should {
    "return a list" in {
      val testModel = TestModel("test")
      val list = List(testModel)

      when(mockSessionCache.fetchAndGetEntry[List[TestModel]](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(list)))

      val result = await(connector.fetchAndGet[List[TestModel]]("testKey"))
      result mustBe Some(list)
    }
  }

  "Fetching from KeyStore" should {
    "return a CacheMap" in {
      val testModel = TestModel("test")

      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(testModel)))

      when(mockSessionCache.fetch()(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(returnCacheMap)))

      val result = await(connector.fetch())
      result mustBe Some(returnCacheMap)
    }
  }

  "Removing from KeyStore" should {
    "return a HTTP Response" in {
      when(mockSessionCache.remove()(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val result = await(connector.remove())
      result.status mustBe HttpResponse(OK).status
    }
  }
}
