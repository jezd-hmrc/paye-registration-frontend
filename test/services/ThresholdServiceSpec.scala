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

import java.time.LocalDate

import helpers.PayeComponentSpec
import utils.SystemDate

class ThresholdServiceSpec extends PayeComponentSpec {

  val testService = new ThresholdService {
    override def now: LocalDate   = SystemDate.getSystemDate.toLocalDate
    override val nextTaxYearStart = LocalDate.of(2018, 4, 6)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    System.setProperty("feature.system-date", "")
  }

  "getCurrentThresholds" should {
    "return a map of the previous tax years thresholds" when {
      "the system date is before the 6 Apr 2018" in {
        System.setProperty("feature.system-date", "2018-04-05T12:00:00")

        val result = testService.getCurrentThresholds
        result mustBe Map("weekly" -> 113, "monthly" -> 490, "annually" -> 5876)
      }
    }

    "return a map of the next tax years thresholds" when {
      "the system date is on the 6 Apr 2018" in {
        System.setProperty("feature.system-date", "2018-04-06T12:00:00")

        val result = testService.getCurrentThresholds
        result mustBe Map("weekly" -> 116, "monthly" -> 503, "annually" -> 6032)
      }

      "the system date is after the 6 Apr 2018" in {
        System.setProperty("feature.system-date", "2018-04-10T12:00:00")

        val result = testService.getCurrentThresholds
        result mustBe Map("weekly" -> 116, "monthly" -> 503, "annually" -> 6032)
      }
    }
  }
}
