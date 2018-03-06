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

package controllers.test

import javax.inject.Inject

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils._

import scala.concurrent.Future

class FeatureSwitchControllerImpl @Inject()(val featureManager: FeatureManager,
                                            val payeFeatureSwitch: PAYEFeatureSwitches) extends FeatureSwitchController

trait FeatureSwitchController extends FrontendController {

  val featureManager: FeatureManager
  val payeFeatureSwitch: PAYEFeatureSwitches

  def switcher(featureName: String, featureState: String): Action[AnyContent] = Action.async {
    implicit request =>
      def feature: FeatureSwitch = featureState match {
        case "true"                                      => featureManager.enable(BooleanFeatureSwitch(featureName, enabled = true))
        case "addressLookupFrontend"                     => featureManager.enable(BooleanFeatureSwitch(featureName, enabled = true))
        case x if x.matches(Validators.datePatternRegex) => featureManager.setSystemDate(ValueSetFeatureSwitch(featureName, featureState))
        case x@"time-clear"                              => featureManager.clearSystemDate(ValueSetFeatureSwitch(featureName, x))
        case _                                           => featureManager.disable(BooleanFeatureSwitch(featureName, enabled = false))
      }

      payeFeatureSwitch(featureName) match {
        case Some(_)  => Future.successful(Ok(feature.toString))
        case None     => Future.successful(BadRequest)
      }
  }
}
