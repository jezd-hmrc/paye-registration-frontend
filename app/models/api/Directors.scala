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

package models.api

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Name(
                 forename: Option[String],
                 otherForenames: Option[String],
                 surname: Option[String],
                 title: Option[String]
                 )

object Name {
  implicit val format = (
    (__ \ "forename").formatNullable[String] and
      (__ \ "other_forenames").formatNullable[String] and
      (__ \ "surname").formatNullable[String] and
      (__ \ "title").formatNullable[String]
  )(Name.apply, unlift(Name.unapply))
}

case class Director(name: Name, nino: Option[String])

object Director {
  implicit val format = (
    (__ \ "director").format[Name] and
      (__ \ "nino").formatNullable[String]
    )(Director.apply, unlift(Director.unapply))
}