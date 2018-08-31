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

package views.pages

import helpers.{PayeComponentSpec, PayeFakedApp}
import models.view.{Summary, SummaryRow, SummarySection}
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.test.FakeRequest
import views.html.pages.summary

class SummarySpec extends PayeComponentSpec with PayeFakedApp with I18nSupport {

  implicit val request = FakeRequest()
  implicit lazy val messagesApi : MessagesApi = mockMessagesApi

  val suffixIdSectionHeading = "SectionHeading"
  val suffixIdQuestion = "Question"
  val suffixIdAnswer = "Answer"
  val suffixIdChangeLink = "ChangeLink"

  "The summary page" should {
    lazy val summaryModelNoTradingName = Summary(Seq())

    lazy val view = summary(summaryModelNoTradingName)
    lazy val document = Jsoup.parse(view.body)

    "have the correct title" in {
      document.getElementById("pageHeading").text mustBe messagesApi("pages.summary.heading")
    }
  }

  "The summary page, Completion Capacity section" should {

    lazy val summaryModelCompletionCapacity: Summary = Summary(
      Seq(
        SummarySection(
          id = "completionCapacity",
          Seq(
            SummaryRow(
              id ="completionCapacity",
              answers = List(Left("director")),
              changeLink = Some(controllers.userJourney.routes.CompletionCapacityController.completionCapacity())
            )
          )
        )
      )
    )

    lazy val view = summary(summaryModelCompletionCapacity)
    lazy val document = Jsoup.parse(view.body)

    "have Applicant in section title" in {
      document.getElementById(s"completionCapacity$suffixIdSectionHeading").text mustBe messagesApi("pages.summary.completionCapacity.sectionHeading")
    }

    "have the correct question text for Completion Capacity" in {
      document.getElementById(s"completionCapacity$suffixIdQuestion").text mustBe messagesApi("pages.summary.completionCapacity.question")
    }

    "have the correct answer text when no Completion Capacity" in {
      document.getElementById(s"completionCapacity$suffixIdAnswer").text mustBe messagesApi("pages.summary.completionCapacity.answers.director")
    }

    "have the correct change link for Completion Capacity" in {
      document.getElementById(s"completionCapacity$suffixIdChangeLink").attr("href") mustBe controllers.userJourney.routes.CompletionCapacityController.completionCapacity().toString
    }
  }

  "The summary page, Company Details section" should {

    lazy val summaryModelNoTradingName: Summary = Summary(
      Seq(
        SummarySection(
          id = "companyDetails",
          Seq(
            SummaryRow(
              id ="tradingName",
              answers = List(Left("noAnswerGiven")),
              changeLink = Some(controllers.userJourney.routes.CompanyDetailsController.tradingName())
            ),
            SummaryRow(
              id = "roAddress",
              answers = List(Right("14 St Test Walk"), Right("Testley")),
              changeLink = None
            )
          )
        )
      )
    )

    lazy val view = summary(summaryModelNoTradingName)
    lazy val document = Jsoup.parse(view.body)

    "have Company Information in section title" in {
      document.getElementById(s"companyDetails$suffixIdSectionHeading").text mustBe messagesApi("pages.summary.companyDetails.sectionHeading")
    }

    "have the correct question text for Other trading name" in {
      document.getElementById(s"tradingName$suffixIdQuestion").text mustBe messagesApi("pages.summary.tradingName.question")
    }

    "have the correct answer text when no Other trading name" in {
      document.getElementById(s"tradingName$suffixIdAnswer").text mustBe messagesApi("pages.summary.tradingName.answers.noAnswerGiven")
    }

    "have the correct change link for Other trading name" in {
      document.getElementById(s"tradingName$suffixIdChangeLink").attr("href") mustBe controllers.userJourney.routes.CompanyDetailsController.tradingName().toString
    }

    "have the correct question text for Registered office address" in {
      document.getElementById(s"roAddress$suffixIdQuestion").text mustBe messagesApi("pages.summary.roAddress.question")
    }

    "have the correct answer text for Registered office address" in {
      document.getElementById(s"roAddress$suffixIdAnswer").text.contains("14 St Test Walk") &&
        document.getElementById(s"roAddress$suffixIdAnswer").text.contains("Testley") mustBe true
    }

    "not have a change link for Registered office address" in {
      an[NullPointerException] mustBe thrownBy(document.getElementById(s"roAddress$suffixIdChangeLink").text)
    }
  }
  val employeeAPISS = SummarySection(
    id = "employees",
    Seq(
      SummaryRow(
        id = "employing",
        answers = List(Left("true")),
        Some(controllers.userJourney.routes.EmploymentController.paidEmployees())
      ),
      SummaryRow(
        id = "earliestDate",
        answers = List(Right("20/12/2016")),
        Some(controllers.userJourney.routes.EmploymentController.paidEmployees())
      ),
      SummaryRow(
        id = "inConstructionIndustry",
        answers = List(Left("true")),
        Some(controllers.userJourney.routes.EmploymentController.constructionIndustry())
      ),
      SummaryRow(
        id = "paysPension",
        answers = List(Left("true")),
        Some(controllers.userJourney.routes.EmploymentController.pensions())
      )
    )
  )

  "The summary page, Employment section" should {

    lazy val summaryModelEmployment = Summary(
      Seq(
        SummarySection(
          id = "employees",
          Seq(
            SummaryRow(
              id = "employing",
              answers = List(Left("false")),
              Some(controllers.userJourney.routes.EmploymentController.paidEmployees())
            ),
            SummaryRow(
              id = "earliestDate",
              answers = List(Right("2/9/2016")),
              Some(controllers.userJourney.routes.EmploymentController.paidEmployees())
            ),
            SummaryRow(
              id = "inConstructionIndustry",
              answers = List(Left("false")),
              Some(controllers.userJourney.routes.EmploymentController.constructionIndustry())
            ),
            SummaryRow(
              id = "paysPension",
              answers = List(Left("true")),
              Some(controllers.userJourney.routes.EmploymentController.pensions())
            )
          )
        )
      )
    )

    lazy val view = summary(summaryModelEmployment)
    lazy val document = Jsoup.parse(view.body)

    "have Employment information in section title" in {
      document.getElementById(s"employees$suffixIdSectionHeading").text mustBe messagesApi("pages.summary.employees.sectionHeading")
    }

    "have the correct question text for Employing staff" in {
      document.getElementById(s"employing$suffixIdQuestion").text mustBe messagesApi("pages.summary.employing.question")
    }

    "have the correct answer text for Employing staff" in {
      document.getElementById(s"employing$suffixIdAnswer").text mustBe messagesApi("pages.summary.employing.answers.false")
    }

    "have the correct change link for Employing staff" in {
      document.getElementById(s"employing$suffixIdChangeLink").attr("href") mustBe controllers.userJourney.routes.EmploymentController.paidEmployees().toString
    }

    "have the correct question text for Company pension" in {
      document.getElementById(s"paysPension$suffixIdQuestion").text mustBe messagesApi("pages.summary.paysPension.question")
    }

    "have the correct answer text for Company pension" in {
      document.getElementById(s"paysPension$suffixIdAnswer").text mustBe messagesApi("pages.summary.paysPension.answers.true")
    }

    "have the correct change link for Company pension" in {
      document.getElementById(s"paysPension$suffixIdChangeLink").attr("href") mustBe controllers.userJourney.routes.EmploymentController.pensions().toString
    }

    "have the correct question text for Construction industry" in {
      document.getElementById(s"inConstructionIndustry$suffixIdQuestion").text mustBe messagesApi("pages.summary.inConstructionIndustry.question")
    }

    "have the correct answer text for Construction industry" in {
      document.getElementById(s"inConstructionIndustry$suffixIdAnswer").text mustBe messagesApi("pages.summary.inConstructionIndustry.answers.false")
    }

    "have the correct change link for Construction industry" in {
      document.getElementById(s"inConstructionIndustry$suffixIdChangeLink").attr("href") mustBe controllers.userJourney.routes.EmploymentController.constructionIndustry().toString
    }

    "have the correct question text for First payment" in {
      document.getElementById(s"earliestDate$suffixIdQuestion").text mustBe messagesApi("pages.summary.earliestDate.question")
    }

    "have the correct answer text for First payment" in {
      document.getElementById(s"earliestDate$suffixIdAnswer").text.matches("([0-9]{1,2}\\/[0-9]{1,2}\\/[0-9]{4})") mustBe true
    }

    "have the correct change link for First payment" in {
      document.getElementById(s"earliestDate$suffixIdChangeLink").attr("href") mustBe controllers.userJourney.routes.EmploymentController.paidEmployees().toString
    }
  }

  "The summary page, Business contact details section" should {
    lazy val summaryModelEmployment = Summary(
      Seq(
        SummarySection(
          id = "businessContactDetails",
          Seq(
            SummaryRow(
              id = "businessEmail",
              answers = List(Right("test@email.com")),
              changeLink = None
            ),
            SummaryRow(
              id = "mobileNumber",
              answers = List(Right("1234567890")),
              changeLink = None
            ),
            SummaryRow(
              id = "businessTelephone",
              answers = List(Right("0987654321")),
              changeLink = None
            )
          )
        )
      )
    )

    lazy val view = summary(summaryModelEmployment)
    lazy val document = Jsoup.parse(view.body)

    "have Business contact details as the summary heading" in {
      document.getElementById(s"businessContactDetails$suffixIdSectionHeading").text mustBe messagesApi("pages.summary.businessContactDetails.sectionHeading")
    }

    "have the correct question text for business email" in {
      document.getElementById(s"businessEmail$suffixIdQuestion").text mustBe messagesApi("pages.summary.businessEmail.question")
    }

    "have the correct question text for mobile phone" in {
      document.getElementById(s"mobileNumber$suffixIdQuestion").text mustBe messagesApi("pages.summary.mobileNumber.question")
    }

    "have the correct question text for business telephone" in {
      document.getElementById(s"businessTelephone$suffixIdQuestion").text mustBe messagesApi("pages.summary.businessTelephone.question")
    }

    "have the correct answer text for business email" in {
      document.getElementById(s"businessEmail$suffixIdAnswer").text mustBe "test@email.com"
    }

    "have the correct answer text for mobile number" in {
      document.getElementById(s"mobileNumber$suffixIdAnswer").text mustBe "1234567890"
    }

    "have the correct answer text for business telephone" in {
      document.getElementById(s"businessTelephone$suffixIdAnswer").text mustBe "0987654321"
    }
  }

  "The summary page, Nature Of Business section" should {
    lazy val summaryModelDirectorDetails = Summary(
      Seq(
        SummarySection(
          id = "natureOfBusiness",
          Seq(
            SummaryRow(
              id = "natureOfBusiness",
              answers = List(Right("<h1>Flower Arranging</h1>")),
              changeLink = Some(controllers.userJourney.routes.NatureOfBusinessController.natureOfBusiness()),
              questionArgs = None,
              commonQuestionKey = None
            )
          )
        )
      )
    )

    lazy val view = summary(summaryModelDirectorDetails)
    lazy val document = Jsoup.parse(view.body)

    "have Director details as the summary heading" in {
      document.getElementById(s"natureOfBusiness$suffixIdSectionHeading").text mustBe messagesApi("pages.summary.natureOfBusiness.sectionHeading")
    }

    "have the correct question text for Nature Of Business" in {
      document.getElementById(s"natureOfBusiness$suffixIdQuestion").text mustBe messagesApi("pages.summary.natureOfBusiness.question")
    }

    "have the correct answer text for Nature Of Business" in {
      document.getElementById(s"natureOfBusiness$suffixIdAnswer").text mustBe "<h1>Flower Arranging</h1>"
    }
  }

  "The summary page, Director details section" should {
    lazy val summaryModelDirectorDetails = Summary(
      Seq(
        SummarySection(
          id = "directorDetails",
          Seq(
            SummaryRow(
              id = "director0",
              answers = List(Right("ZZ123456A")),
              changeLink = Some(controllers.userJourney.routes.DirectorDetailsController.directorDetails()),
              questionArgs = Some(Seq("Timothy Buttersford")),
              commonQuestionKey = Some("director")
            ),
            SummaryRow(
              id = "director1",
              answers = List(Right("")),
              changeLink = Some(controllers.userJourney.routes.DirectorDetailsController.directorDetails()),
              questionArgs = Some(Seq("Pierre Simpson")),
              commonQuestionKey = Some("director")
            )
          )
        )
      )
    )

    lazy val view = summary(summaryModelDirectorDetails)
    lazy val document = Jsoup.parse(view.body)

    "have Director details as the summary heading" in {
      document.getElementById(s"directorDetails$suffixIdSectionHeading").text mustBe messagesApi("pages.summary.directorDetails.sectionHeading")
    }

    "have the correct question text for director0" in {
      document.getElementById(s"director0$suffixIdQuestion").text mustBe messagesApi("pages.summary.director.question", "Timothy Buttersford")
    }

    "have the correct question text for director1" in {
      document.getElementById(s"director1$suffixIdQuestion").text mustBe messagesApi("pages.summary.director.question", "Pierre Simpson")
    }

    "have the correct answer text for director0" in {
      document.getElementById(s"director0$suffixIdAnswer").text mustBe "ZZ123456A"
    }

    "have the correct answer text for director1" in {
      document.getElementById(s"director1$suffixIdAnswer").text mustBe ""
    }

    "have the correct change link for director0" in {
      document.getElementById(s"director0$suffixIdChangeLink").attr("href") mustBe controllers.userJourney.routes.DirectorDetailsController.directorDetails().toString
    }

    "have the correct change link for director1" in {
      document.getElementById(s"director1$suffixIdChangeLink").attr("href") mustBe controllers.userJourney.routes.DirectorDetailsController.directorDetails().toString
    }
  }
}
